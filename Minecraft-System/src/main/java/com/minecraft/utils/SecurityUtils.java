package com.minecraft.utils;

import com.minecraft.security.core.LoginUser;
import com.minecraft.security.core.PermissionChecker;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;

/**
 * 当前登录用户工具类。
 *
 * <p>兼容两种 Authentication principal：RBAC 重构后的 {@link LoginUser}，
 * 以及历史上仅存放 userId 的 Long。</p>
 */
public class SecurityUtils {

    public static LoginUser getLoginUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof LoginUser loginUser) {
            return loginUser;
        }
        return null;
    }

    public static Long getCurrentUserId() {
        LoginUser loginUser = getLoginUser();
        if (loginUser != null) {
            return loginUser.getUserId();
        }
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        String name = authentication.getName();
        // 检查是否为匿名用户或非数字字符串
        if ("anonymousUser".equals(name) || !name.matches("\\d+")) {
            return null;
        }
        return Long.valueOf(name);
    }

    public static String getCurrentUsername() {
        LoginUser loginUser = getLoginUser();
        if (loginUser != null) {
            return loginUser.getUsername();
        }
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        return authentication.getName();
    }

    public static boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.isAuthenticated()
                && !"anonymousUser".equals(authentication.getName());
    }

    public static boolean isSuperAdmin() {
        LoginUser loginUser = getLoginUser();
        return loginUser != null && loginUser.isSuperAdmin();
    }

    /** 当前用户是否拥有指定权限（与后端 @PreAuthorize 同一套匹配规则） */
    public static boolean hasPermission(String code) {
        LoginUser loginUser = getLoginUser();
        if (loginUser == null || code == null || code.isBlank()) {
            return false;
        }
        if (loginUser.isSuperAdmin()) {
            return true;
        }
        return new PermissionChecker().match(loginUser.getPermissions(), code);
    }

    public static String getCurrentToken() {
        HttpServletRequest request = getRequest();
        if (request == null) {
            return null;
        }
        String authorization = request.getHeader("Authorization");
        if (authorization != null && authorization.startsWith("Bearer ")) {
            return authorization.substring(7);
        }
        return null;
    }

    private static HttpServletRequest getRequest() {
        try {
            return ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        } catch (Exception e) {
            return null;
        }
    }
}
