package com.minecraft.security.core;

/**
 * 线程级权限上下文，承载数据范围 SQL 片段。
 */
public final class PermissionContext {

    private static final ThreadLocal<String> DATA_SCOPE = new ThreadLocal<>();

    private PermissionContext() {
    }

    public static void setDataScope(String condition) {
        DATA_SCOPE.set(condition);
    }

    public static String getDataScope() {
        return DATA_SCOPE.get();
    }

    public static void clear() {
        DATA_SCOPE.remove();
    }
}
