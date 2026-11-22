package com.minecraft.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.minecraft.entity.LoginInfo;
import com.minecraft.mapper.LoginInfoMapper;
import com.minecraft.service.LoginInfoService;
import com.minecraft.utils.RedisUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

/**
 * 登录信息服务实现
 */
@Service
public class LoginInfoServiceImpl extends ServiceImpl<LoginInfoMapper, LoginInfo> implements LoginInfoService {
    
    @Autowired
    private RedisUtil redisUtil;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    private static final String LOGIN_INFO_CACHE_KEY = "login_info:page:";
    private static final long CACHE_EXPIRE_TIME = 10; // 缓存10分钟
    
    @Override
    public Page<LoginInfo> getLoginInfoPage(int page, int pageSize, String keyword) {
        // 生成缓存key
        String cacheKey = LOGIN_INFO_CACHE_KEY + page + ":" + pageSize + ":" + (keyword != null ? keyword : "");
        
        // 尝试从缓存获取
        try {
            Object cachedData = redisUtil.get(cacheKey);
            if (cachedData != null) {
                return objectMapper.readValue(cachedData.toString(), new com.fasterxml.jackson.core.type.TypeReference<Page<LoginInfo>>() {});
            }
        } catch (Exception e) {
            // 缓存读取失败，继续从数据库获取
            e.printStackTrace();
        }
        
        // 从数据库获取
        Page<LoginInfo> loginInfoPage = new Page<>(page, pageSize);
        Page<LoginInfo> result = baseMapper.selectLoginInfoPage(loginInfoPage, keyword);
        
        // 存入缓存
        try {
            String jsonData = objectMapper.writeValueAsString(result);
            redisUtil.set(cacheKey, jsonData, CACHE_EXPIRE_TIME, TimeUnit.MINUTES);
        } catch (Exception e) {
            // 缓存写入失败，不影响业务
            e.printStackTrace();
        }
        
        return result;
    }
    
    @Override
    public boolean deleteLoginInfo(Long id) {
        boolean result = removeById(id);
        if (result) {
            // 清除缓存
            clearLoginInfoCache();
        }
        return result;
    }
    
    @Override
    public boolean deleteBatchLoginInfo(String ids) {
        String[] idArray = ids.split(",");
        boolean result = removeByIds(Arrays.asList(idArray));
        if (result) {
            // 清除缓存
            clearLoginInfoCache();
        }
        return result;
    }
    
    /**
     * 清除登录信息缓存
     */
    private void clearLoginInfoCache() {
        // 使用 SCAN 分批删除，避免 KEYS 一次性返回大量键造成阻塞与内存尖峰
        redisUtil.deleteByPattern(LOGIN_INFO_CACHE_KEY + "*");
    }
}