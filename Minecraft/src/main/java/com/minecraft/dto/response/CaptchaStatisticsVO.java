package com.minecraft.dto.response;

import lombok.Data;

/**
 * 滑块验证数据概览
 */
@Data
public class CaptchaStatisticsVO {

    /** 累计验证次数 */
    private Long total = 0L;

    /** 累计通过次数 */
    private Long passed = 0L;

    /** 累计失败次数 */
    private Long failed = 0L;

    /** 通过率（百分比，保留两位小数） */
    private Double passRate = 0D;

    /** 平均验证耗时（毫秒） */
    private Double avgCostTime = 0D;

    /** 今日验证次数 */
    private Long todayTotal = 0L;

    /** 今日通过次数 */
    private Long todayPassed = 0L;

    /** 今日失败次数 */
    private Long todayFailed = 0L;
}
