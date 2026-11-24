package com.minecraft.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.minecraft.entity.Month;
import com.minecraft.entity.MonthlyRecommendation;
import com.minecraft.mapper.MonthMapper;
import com.minecraft.mapper.MonthlyRecommendationMapper;
import com.minecraft.service.MonthlyRecommendationService;
import com.minecraft.utils.ImageUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class MonthlyRecommendationServiceImpl extends ServiceImpl<MonthlyRecommendationMapper, MonthlyRecommendation> implements MonthlyRecommendationService {
    
    @Autowired
    private MonthlyRecommendationMapper monthlyRecommendationMapper;
    
    @Autowired
    private MonthMapper monthMapper;

    @Autowired
    private ImageUtils imageUtils;

    @Override
    public boolean updateById(MonthlyRecommendation recommendation) {
        MonthlyRecommendation old = recommendation.getId() != null ? getById(recommendation.getId()) : null;
        boolean updated = super.updateById(recommendation);
        if (updated && old != null) {
            // 图片被替换后删除旧图片文件
            imageUtils.deleteReplacedImages(old.getImageUrl(), recommendation.getImageUrl());
        }
        return updated;
    }
    
    @Override
    public List<MonthlyRecommendation> getRecommendationsByMonthName(String monthName) {
        return monthlyRecommendationMapper.getRecommendationsByMonthName(monthName);
    }
    
    @Override
    public List<String> getAllMonths() {
        List<Month> months = monthMapper.selectList(null);
        return months.stream()
                .map(Month::getMonthName)
                .collect(Collectors.toList());
    }
}
