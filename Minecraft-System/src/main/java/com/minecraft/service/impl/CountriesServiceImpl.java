package com.minecraft.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.minecraft.entity.Countries;
import com.minecraft.mapper.CountriesMapper;
import com.minecraft.service.CountriesService;
import com.minecraft.utils.ImageUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class CountriesServiceImpl extends ServiceImpl<CountriesMapper, Countries> implements CountriesService {

    @Autowired
    private ImageUtils imageUtils;

    @Override
    public Countries getCountryById(Integer id) {
        return getById(id);
    }

    @Override
    public void addCountry(Countries country) {
        save(country);
    }

    @Override
    public void updateCountry(Countries country) {
        updateById(country);
    }

    @Override
    public void deleteCountry(Integer id) {
        removeById(id);
    }
    
    @Override
    public boolean save(Countries country) {
        try {
            // 处理图片上传
            if (country.getImage() != null && country.getImage().startsWith("data:image")) {
                String processedImage = imageUtils.processBase64Image(country.getImage());
                country.setImage(processedImage);
            }
            
            return super.save(country);
        } catch (Exception e) {
            log.warn("操作失败", e);
            return false;
        }
    }
    
    @Override
    public boolean updateById(Countries country) {
        try {
            Countries old = country.getId() != null ? getById(country.getId()) : null;

            // 处理图片上传
            if (country.getImage() != null && country.getImage().startsWith("data:image")) {
                String processedImage = imageUtils.processBase64Image(country.getImage());
                country.setImage(processedImage);
            }
            
            boolean updated = super.updateById(country);
            if (updated && old != null) {
                // 图片被替换后删除旧图片文件
                imageUtils.deleteReplacedImages(old.getImage(), country.getImage());
            }
            return updated;
        } catch (Exception e) {
            log.warn("操作失败", e);
            return false;
        }
    }
}
