package com.minecraft.service.impl;

import cloud.tianai.captcha.common.constant.CaptchaTypeConstant;
import cloud.tianai.captcha.resource.common.model.dto.Resource;
import cloud.tianai.captcha.resource.impl.LocalMemoryResourceStore;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.minecraft.dto.request.CaptchaImageQuery;
import com.minecraft.dto.response.CaptchaImageVO;
import com.minecraft.dto.response.PageResponse;
import com.minecraft.entity.CaptchaImage;
import com.minecraft.exception.BusinessException;
import com.minecraft.mapper.CaptchaImageMapper;
import com.minecraft.service.CaptchaImageService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CaptchaImageServiceImpl implements CaptchaImageService {

    private static final Logger logger = LoggerFactory.getLogger(CaptchaImageServiceImpl.class);

    /** 上传文件大小上限：5MB */
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024L;
    /** 验证码背景图最小尺寸，过小会导致拼图块无法生成 */
    private static final int MIN_WIDTH = 300;
    private static final int MIN_HEIGHT = 150;
    /** 验证码背景图最大尺寸，避免超大图片导致内存与数据库字段溢出 */
    private static final int MAX_WIDTH = 4096;
    private static final int MAX_HEIGHT = 4096;
    /** captcha_image.file_name 字段长度 */
    private static final int FILE_NAME_MAX_LENGTH = 250;
    /** 访问已上传图片的 URL 前缀，与 WebConfig 中的资源映射保持一致 */
    private static final String ACCESS_URL_PREFIX = "/captcha-upload/";
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png");

    private final CaptchaImageMapper captchaImageMapper;
    private final LocalMemoryResourceStore resourceStore;

    /** 滑块验证码图片上传目录 */
    @Value("${captcha.upload-dir:./uploads/captcha}")
    private String uploadDir;

    @Override
    public PageResponse<CaptchaImageVO> pageImages(CaptchaImageQuery query) {
        int pageNum = query.getPage() == null ? 1 : query.getPage();
        int pageSize = query.getSize() == null ? 12 : query.getSize();

        LambdaQueryWrapper<CaptchaImage> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(query.getKeyword())) {
            wrapper.like(CaptchaImage::getFileName, query.getKeyword().trim());
        }
        wrapper.orderByDesc(CaptchaImage::getCreateTime);

        Page<CaptchaImage> page = captchaImageMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
        List<CaptchaImageVO> records = page.getRecords().stream().map(this::toVO).toList();
        return new PageResponse<>(records, page.getTotal(), pageNum, pageSize);
    }

    @Override
    public CaptchaImageVO upload(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(400, "请选择要上传的图片");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BusinessException(400, "图片大小不能超过 5MB");
        }

        String originalName = sanitizeFileName(file.getOriginalFilename());
        String extension = resolveExtension(originalName);
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new BusinessException(400, "仅支持 JPG、PNG 格式图片");
        }

        byte[] bytes;
        try {
            bytes = file.getBytes();
        } catch (IOException e) {
            logger.error("Failed to read captcha image bytes", e);
            throw new BusinessException(500, "读取图片失败，请重试");
        }

        BufferedImage bufferedImage = readImage(bytes);
        if (bufferedImage == null) {
            throw new BusinessException(400, "图片内容无效，请上传有效的 JPG/PNG 图片");
        }
        int width = bufferedImage.getWidth();
        int height = bufferedImage.getHeight();
        if (width < MIN_WIDTH || height < MIN_HEIGHT) {
            throw new BusinessException(400, "图片尺寸不能小于 " + MIN_WIDTH + "x" + MIN_HEIGHT);
        }
        if (width > MAX_WIDTH || height > MAX_HEIGHT) {
            throw new BusinessException(400, "图片尺寸不能超过 " + MAX_WIDTH + "x" + MAX_HEIGHT);
        }

        String md5 = DigestUtils.md5DigestAsHex(bytes);
        Long duplicated = captchaImageMapper.selectCount(
                new LambdaQueryWrapper<CaptchaImage>().eq(CaptchaImage::getMd5Hash, md5));
        if (duplicated != null && duplicated > 0) {
            throw new BusinessException(409, "该图片已存在，请勿重复上传");
        }

        Path destination = saveFile(bytes, extension);
        try {
            CaptchaImage entity = new CaptchaImage();
            entity.setImageKey(md5);
            entity.setFileName(originalName);
            entity.setFilePath(destination.toString());
            entity.setFileSize(bytes.length);
            entity.setWidth(width);
            entity.setHeight(height);
            entity.setMimeType(resolveMimeType(extension));
            entity.setMd5Hash(md5);
            entity.setStatus(1);
            captchaImageMapper.insert(entity);

            addToResourceStore(entity.getId(), destination.toString());
            logger.info("Captcha image uploaded: id={}, file={}, size={}x{}",
                    entity.getId(), destination, width, height);
            return toVO(entity);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Failed to persist uploaded captcha image", e);
            deleteQuietly(destination);
            throw new BusinessException(500, "保存验证码图片失败，请重试");
        }
    }

    @Override
    public void delete(String id) {
        if (!StringUtils.hasText(id)) {
            throw new BusinessException(400, "图片ID不能为空");
        }
        CaptchaImage entity = captchaImageMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException(404, "验证码图片不存在");
        }

        captchaImageMapper.deleteById(id);
        removeFromResourceStore(id);
        deleteFileQuietly(entity.getFilePath());
        logger.info("Captcha image deleted: id={}, file={}", id, entity.getFilePath());
    }

    private void addToResourceStore(String id, String filePath) {
        try {
            resourceStore.addResource(CaptchaTypeConstant.SLIDER,
                    new Resource(id, "file", filePath, "default", null));
        } catch (Exception e) {
            logger.error("Failed to add captcha image to resource store: id={}", id, e);
        }
    }

    private void removeFromResourceStore(String id) {
        try {
            resourceStore.deleteResource(CaptchaTypeConstant.SLIDER, id);
        } catch (Exception e) {
            logger.error("Failed to remove captcha image from resource store: id={}", id, e);
        }
    }

    private Path saveFile(byte[] bytes, String extension) {
        Path directory = resolveUploadDirectory();
        String storedName = UUID.randomUUID().toString().replace("-", "") + "." + extension;
        Path destination = directory.resolve(storedName);
        try {
            Files.write(destination, bytes);
            return destination;
        } catch (IOException e) {
            logger.error("Failed to write captcha image file: {}", destination, e);
            throw new BusinessException(500, "保存图片文件失败，请检查上传目录权限");
        }
    }

    private Path resolveUploadDirectory() {
        Path directory = Paths.get(uploadDir);
        if (!directory.isAbsolute()) {
            directory = Paths.get(System.getProperty("user.dir")).resolve(directory);
        }
        directory = directory.normalize();
        try {
            Files.createDirectories(directory);
        } catch (IOException e) {
            logger.error("Failed to create captcha upload directory: {}", directory, e);
            throw new BusinessException(500, "创建上传目录失败");
        }
        return directory;
    }

    private void deleteFileQuietly(String filePath) {
        if (!StringUtils.hasText(filePath)) {
            return;
        }
        try {
            Path directory = resolveUploadDirectory();
            Path path = Paths.get(filePath).toAbsolutePath().normalize();
            // 仅允许删除上传目录内的文件，避免误删其他资源
            if (path.startsWith(directory)) {
                Files.deleteIfExists(path);
            }
        } catch (Exception e) {
            logger.error("Failed to delete captcha image file: {}", filePath, e);
        }
    }

    private void deleteQuietly(Path path) {
        try {
            Files.deleteIfExists(path);
        } catch (IOException e) {
            logger.warn("Failed to delete temp captcha image file: {}", path, e);
        }
    }

    private BufferedImage readImage(byte[] bytes) {
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes)) {
            return ImageIO.read(inputStream);
        } catch (IOException e) {
            logger.warn("Failed to parse uploaded captcha image", e);
            return null;
        }
    }

    private String sanitizeFileName(String originalFilename) {
        String cleaned = StringUtils.cleanPath(originalFilename == null ? "" : originalFilename);
        int slashIndex = Math.max(cleaned.lastIndexOf('/'), cleaned.lastIndexOf('\\'));
        String fileName = slashIndex >= 0 ? cleaned.substring(slashIndex + 1) : cleaned;
        if (fileName.isBlank()) {
            fileName = "captcha-image";
        }
        return fileName.length() > FILE_NAME_MAX_LENGTH
                ? fileName.substring(0, FILE_NAME_MAX_LENGTH)
                : fileName;
    }

    private String resolveExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex < 0 || dotIndex == fileName.length() - 1) {
            return "";
        }
        return fileName.substring(dotIndex + 1).toLowerCase(Locale.ROOT);
    }

    private String resolveMimeType(String extension) {
        return "png".equals(extension) ? "image/png" : "image/jpeg";
    }

    private CaptchaImageVO toVO(CaptchaImage entity) {
        CaptchaImageVO vo = new CaptchaImageVO();
        vo.setId(entity.getId());
        vo.setImageKey(entity.getImageKey());
        vo.setFileName(entity.getFileName());
        vo.setFileSize(entity.getFileSize());
        vo.setWidth(entity.getWidth());
        vo.setHeight(entity.getHeight());
        vo.setMimeType(entity.getMimeType());
        vo.setMd5Hash(entity.getMd5Hash());
        vo.setStatus(entity.getStatus());
        vo.setCreateTime(entity.getCreateTime());
        vo.setUrl(buildAccessUrl(entity.getFilePath()));
        return vo;
    }

    private String buildAccessUrl(String filePath) {
        if (!StringUtils.hasText(filePath)) {
            return null;
        }
        Path fileName = Paths.get(filePath).getFileName();
        return fileName == null ? null : ACCESS_URL_PREFIX + fileName;
    }
}
