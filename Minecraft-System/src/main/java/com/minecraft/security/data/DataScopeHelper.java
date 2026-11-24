package com.minecraft.security.data;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.minecraft.security.core.PermissionContext;
import org.springframework.util.StringUtils;

/**
 * 数据范围 SQL 拼接工具。
 */
public final class DataScopeHelper {

    private DataScopeHelper() {
    }

    public static <T> void apply(QueryWrapper<T> wrapper) {
        if (wrapper == null) {
            return;
        }
        String scope = PermissionContext.getDataScope();
        if (StringUtils.hasText(scope)) {
            wrapper.apply(scope);
        }
    }
}
