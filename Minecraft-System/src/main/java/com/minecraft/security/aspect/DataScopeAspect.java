package com.minecraft.security.aspect;

import com.minecraft.security.annotation.DataScope;
import com.minecraft.security.core.LoginUser;
import com.minecraft.security.core.PermissionContext;
import com.minecraft.utils.SecurityUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

/**
 * 数据权限切面：根据当前用户的数据范围写入 SQL 条件。
 */
@Aspect
@Component
public class DataScopeAspect {

    @Around("@annotation(dataScope)")
    public Object around(ProceedingJoinPoint joinPoint, DataScope dataScope) throws Throwable {
        try {
            applyScope(dataScope);
            return joinPoint.proceed();
        } finally {
            PermissionContext.clear();
        }
    }

    private void applyScope(DataScope dataScope) {
        LoginUser user = SecurityUtils.getLoginUser();
        if (user == null || user.isSuperAdmin()) {
            return;
        }
        String field = dataScope.tableAlias().isBlank()
                ? dataScope.field()
                : dataScope.tableAlias() + "." + dataScope.field();

        Integer scope = user.getDataScope();
        String condition = switch (scope == null ? 4 : scope) {
            case 1 -> null;
            case 2 -> String.format("%s = %d", field, user.getDeptId() == null ? 0L : user.getDeptId());
            case 3 -> String.format("%s = %d", field, user.getDeptId() == null ? 0L : user.getDeptId());
            case 4 -> String.format("create_by = %d", user.getUserId());
            case 5 -> user.getScopeDeptIds().isEmpty()
                    ? "1 = 0"
                    : String.format("%s IN (%s)", field, user.getScopeDeptIds().stream()
                            .map(String::valueOf).collect(Collectors.joining(",")));
            default -> "1 = 0";
        };
        if (condition != null) {
            PermissionContext.setDataScope(condition);
        }
    }
}
