package com.minecraft.recommendation.service;

import com.minecraft.entity.User;
import com.minecraft.exception.BusinessException;
import com.minecraft.security.core.LoginUser;
import com.minecraft.service.UserService;
import com.minecraft.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 推荐后台统一鉴权入口。
 *
 * <p>SecurityConfig 对业务接口默认放行，管理端身份在此兜底校验：
 * 超级管理员、拥有 RBAC 权限码的用户、以及历史 {@code permissions='0'} 的管理员均可通过。
 * 细粒度权限仍由各接口上的 {@code @PreAuthorize} 控制。</p>
 */
@Service
@RequiredArgsConstructor
public class AdminAuthorizationService {

    /** 历史管理员权限标识 */
    public static final String ADMIN_PERMISSION = "0";

    private final UserService userService;

    /**
     * 要求当前请求具备管理员身份，返回管理员用户 ID。
     *
     * @throws BusinessException 401 未登录/用户不存在，403 非管理员
     */
    public Long requireAdmin() {
        Long userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException(401, "请先登录");
        }
        LoginUser loginUser = SecurityUtils.getLoginUser();
        if (loginUser != null) {
            if (loginUser.isSuperAdmin()) {
                return userId;
            }
            // 已通过 @PreAuthorize 校验的 RBAC 用户
            if (!loginUser.getPermissions().isEmpty()) {
                return userId;
            }
            throw new BusinessException(403, "无推荐后台访问权限");
        }

        User user = userService.getById(userId);
        if (user == null) {
            throw new BusinessException(401, "用户不存在，请重新登录");
        }
        if (!ADMIN_PERMISSION.equals(user.getPermissions())) {
            throw new BusinessException(403, "无推荐后台访问权限");
        }
        return userId;
    }
}
