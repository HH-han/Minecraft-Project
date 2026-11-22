package com.minecraft.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 滑块验证记录视图对象
 */
@Data
public class CaptchaRecordVO {

    private Long id;

    /** 验证会话ID（天爱验证码 id） */
    private String traceId;

    /** 验证码图片ID */
    private String imageId;

    /** 客户端IP */
    private String clientIp;

    /** 客户端 User-Agent */
    private String userAgent;

    /** 用户拖动偏移量（像素） */
    private Integer sliderOffset;

    /** 目标位置偏移量（像素） */
    private Integer targetOffset;

    /** 验证结果：1-通过，0-失败 */
    private Integer isPassed;

    /** 验证耗时（毫秒） */
    private Integer costTime;

    /** 创建时间 */
    private LocalDateTime createTime;
}
