package com.minecraft.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.minecraft.dto.request.PageRequest;
import com.minecraft.dto.response.PageResponse;
import com.minecraft.entity.News;
import com.minecraft.mapper.NewsMapper;
import com.minecraft.service.NewsService;
import com.minecraft.utils.ImageUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class NewsServiceImpl extends ServiceImpl<NewsMapper, News> implements NewsService {
    @Autowired
    private ImageUtils imageUtils;

    @Override
    public PageResponse<?> getNewsList(PageRequest request) {
        Page<News> page = new Page<>(request.getPageNum(), request.getPageSize());
        LambdaQueryWrapper<News> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(News::getStatus, 1)
                .orderByDesc(News::getCreateTime);

        Page<News> result = page(page, wrapper);
        return new PageResponse<>(result.getRecords(), result.getTotal(), request.getPageNum(), request.getPageSize());
    }

    @Override
    public News getNewsDetail(Long id) {
        News news = getById(id);
        if (news != null) {
            news.setViewCount(news.getViewCount() + 1);
            updateById(news);
        }
        return news;
    }

    @Override
    public List<News> getHotNews(Integer limit) {
        LambdaQueryWrapper<News> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(News::getStatus, 1)
                .orderByDesc(News::getViewCount)
                .last("LIMIT " + (limit != null ? limit : 10));
        return list(wrapper);
    }

    @Override
    public void addNews(News news) {
        save(news);
    }

    @Override
    public void updateNews(News news) {
        updateById(news);
    }

    @Override
    public void deleteNews(Long id) {
        removeById(id);
    }
    
    @Override
    public boolean save(News news) {
        try {
            // 处理封面图片上传
            if (news.getCoverImage() != null && news.getCoverImage().startsWith("data:image")) {
                String processedCoverImage = imageUtils.processBase64Image(news.getCoverImage());
                news.setCoverImage(processedCoverImage);
            }
            
            return super.save(news);
        } catch (Exception e) {
            log.warn("操作失败", e);
            return false;
        }
    }
    
    @Override
    public boolean updateById(News news) {
        try {
            News old = news.getId() != null ? getById(news.getId()) : null;

            // 处理封面图片上传
            if (news.getCoverImage() != null && news.getCoverImage().startsWith("data:image")) {
                String processedCoverImage = imageUtils.processBase64Image(news.getCoverImage());
                news.setCoverImage(processedCoverImage);
            }
            
            boolean updated = super.updateById(news);
            if (updated && old != null) {
                // 封面被替换后删除旧图片文件
                imageUtils.deleteReplacedImages(old.getCoverImage(), news.getCoverImage());
            }
            return updated;
        } catch (Exception e) {
            log.warn("操作失败", e);
            return false;
        }
    }
}
