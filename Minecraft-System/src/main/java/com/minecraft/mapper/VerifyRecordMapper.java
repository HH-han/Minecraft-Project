package com.minecraft.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.minecraft.dto.response.CaptchaStatisticsVO;
import com.minecraft.entity.VerifyRecord;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface VerifyRecordMapper extends BaseMapper<VerifyRecord> {

    /**
     * 统计滑块验证记录概览（累计/今日、通过/失败、平均耗时）
     */
    CaptchaStatisticsVO selectStatistics();
}
