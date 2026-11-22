package com.minecraft.config;

import cloud.tianai.captcha.common.constant.CaptchaTypeConstant;
import cloud.tianai.captcha.resource.common.model.dto.Resource;
import cloud.tianai.captcha.resource.impl.LocalMemoryResourceStore;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.minecraft.entity.CaptchaImage;
import com.minecraft.mapper.CaptchaImageMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Configuration
public class CaptchaResourceConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(CaptchaResourceConfiguration.class);

    @Value("${captcha.image-dir.windows}")
    private String windowsImageDir;

    @Value("${captcha.image-dir.unix}")
    private String unixImageDir;

    /** Linux 候选回退目录（逗号分隔，来自 application.yml） */
    @Value("#{'${captcha.image-dir.fallbacks:}'.split(',')}")
    private List<String> fallbackDirs;

    @Bean
    public LocalMemoryResourceStore resourceStore(CaptchaImageMapper captchaImageMapper) {
        LocalMemoryResourceStore resourceStore = new LocalMemoryResourceStore();
        Set<String> loadedPaths = new HashSet<>();

        int localImageCount = loadLocalImages(resourceStore, loadedPaths);
        int persistedImageCount = loadPersistedImages(captchaImageMapper, resourceStore, loadedPaths);

        if (localImageCount + persistedImageCount == 0) {
            logger.error("No captcha images found in configured directories or database. " +
                    "Slider captcha will fail! Please upload jpg/png images via the admin console " +
                    "or put them into the configured directory.");
        } else {
            logger.info("Loaded {} captcha image(s) in total: {} from local directories, {} from database",
                    localImageCount + persistedImageCount, localImageCount, persistedImageCount);
        }

        return resourceStore;
    }

    /**
     * 加载本地目录中的验证码图片
     */
    private int loadLocalImages(LocalMemoryResourceStore resourceStore, Set<String> loadedPaths) {
        String configuredDir = resolveImageDir();
        List<String> candidateDirs = buildCandidateDirs(configuredDir);

        String usedDir = null;
        List<String> imageFiles = new ArrayList<>();

        for (String dir : candidateDirs) {
            List<String> files = getImageFiles(dir);
            if (!files.isEmpty()) {
                usedDir = dir;
                imageFiles = files;
                break;
            }
            logger.debug("No captcha images in candidate directory: {}", dir);
        }

        if (usedDir == null) {
            return 0;
        }

        logger.info("Loaded {} captcha image(s) from {}", imageFiles.size(), usedDir);
        int count = 0;
        for (String imageFile : imageFiles) {
            if (!loadedPaths.add(normalizePath(imageFile))) {
                continue;
            }
            resourceStore.addResource(CaptchaTypeConstant.SLIDER, new Resource("file", imageFile, "default"));
            count++;
        }
        return count;
    }

    /**
     * 加载数据库中已上传且启用的验证码图片，保证服务重启后上传素材仍然可用
     */
    private int loadPersistedImages(CaptchaImageMapper captchaImageMapper,
                                    LocalMemoryResourceStore resourceStore,
                                    Set<String> loadedPaths) {
        try {
            List<CaptchaImage> images = captchaImageMapper.selectList(new LambdaQueryWrapper<CaptchaImage>()
                    .eq(CaptchaImage::getStatus, 1)
                    .orderByAsc(CaptchaImage::getCreateTime));
            int count = 0;
            for (CaptchaImage image : images) {
                if (image == null || !StringUtils.hasText(image.getFilePath())) {
                    continue;
                }
                Path path = Paths.get(image.getFilePath());
                if (!Files.isRegularFile(path)) {
                    logger.warn("Captcha image file not found, skip loading: id={}, path={}",
                            image.getId(), image.getFilePath());
                    continue;
                }
                String normalizedPath = normalizePath(path.toString());
                if (!loadedPaths.add(normalizedPath)) {
                    continue;
                }
                resourceStore.addResource(CaptchaTypeConstant.SLIDER,
                        new Resource(image.getId(), "file", normalizedPath, "default", null));
                count++;
            }
            return count;
        } catch (Exception e) {
            logger.error("Failed to load captcha images from database", e);
            return 0;
        }
    }

    /**
     * 根据操作系统选择验证码图片目录
     */
    private String resolveImageDir() {
        String osName = System.getProperty("os.name", "").toLowerCase();
        if (osName.contains("win")) {
            return windowsImageDir;
        }
        return unixImageDir;
    }

    /**
     * 构建候选目录列表：优先使用配置目录，其次使用配置的回退目录
     */
    private List<String> buildCandidateDirs(String configuredDir) {
        List<String> dirs = new ArrayList<>();
        dirs.add(configuredDir);
        String osName = System.getProperty("os.name", "").toLowerCase();
        if (!osName.contains("win") && fallbackDirs != null) {
            fallbackDirs.stream()
                    .map(String::trim)
                    .filter(dir -> !dir.isEmpty())
                    .forEach(dirs::add);
        }
        return dirs;
    }

    private List<String> getImageFiles(String dirPath) {
        File dir = new File(dirPath);
        if (!dir.exists() || !dir.isDirectory()) {
            return Arrays.asList();
        }

        File[] files = dir.listFiles();
        if (files == null) {
            return Arrays.asList();
        }

        return Arrays.stream(files)
                .filter(file -> file.isFile() && isImageFile(file.getName()))
                .map(file -> file.getAbsolutePath())
                .limit(20)
                .collect(Collectors.toList());
    }

    private boolean isImageFile(String fileName) {
        String lowerName = fileName.toLowerCase();
        return lowerName.endsWith(".jpg") || lowerName.endsWith(".jpeg") ||
               lowerName.endsWith(".png") || lowerName.endsWith(".webp");
    }

    private String normalizePath(String path) {
        return Paths.get(path).toAbsolutePath().normalize().toString();
    }
}
