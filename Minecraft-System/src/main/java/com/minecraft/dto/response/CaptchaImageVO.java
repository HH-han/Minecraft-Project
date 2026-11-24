package com.minecraft.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 滑块验证码图片视图对象
 */
@Data
public class CaptchaImageVO {

    private String id;

    /** 图片唯一标识（当前使用 MD5） */
    private String imageKey;

    /** 原始文件名 */
    private String fileName;

    /** 访问地址（相对后端根路径） */
    private String url;

    /** 文件大小（字节） */
    private Integer fileSize;

    private Integer width;

    private Integer height;

    private String mimeType;

    private String md5Hash;

    /** 状态：1-启用，0-禁用 */
    private Integer status;

    private LocalDateTime createTime;
}
