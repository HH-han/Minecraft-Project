package com.minecraft.service;

import com.minecraft.dto.request.CaptchaImageQuery;
import com.minecraft.dto.response.CaptchaImageVO;
import com.minecraft.dto.response.PageResponse;
import org.springframework.web.multipart.MultipartFile;

/**
 * 滑块验证码图片素材管理
 */
public interface CaptchaImageService {

    /**
     * 分页查询已上传的验证码图片
     */
    PageResponse<CaptchaImageVO> pageImages(CaptchaImageQuery query);

    /**
     * 上传验证码图片，持久化元数据并立即加入验证码资源库
     */
    CaptchaImageVO upload(MultipartFile file);

    /**
     * 删除验证码图片（数据库记录、磁盘文件与资源库中的引用）
     */
    void delete(String id);
}
