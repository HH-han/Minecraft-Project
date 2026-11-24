package com.minecraft.security.core;

import lombok.Data;

import java.util.HashSet;
import java.util.Set;

/**
 * 权限缓存载体。
 *
 * <p>与 {@link LoginUser} 分离，避免 UserDetails 的派生 getter 参与 JSON 序列化。</p>
 */
@Data
public class UserAuthCache {

    private Long userId;

    private String username;

    private String nickname;

    private Integer status;

    private Long deptId;

    private Long tenantId;

    private boolean superAdmin;

    private Set<String> roleCodes = new HashSet<>();

    private Set<String> permissions = new HashSet<>();

    private Integer dataScope = 4;

    private Set<Long> scopeDeptIds = new HashSet<>();

    public static UserAuthCache from(LoginUser user) {
        UserAuthCache cache = new UserAuthCache();
        cache.setUserId(user.getUserId());
        cache.setUsername(user.getUsername());
        cache.setNickname(user.getNickname());
        cache.setStatus(user.getStatus());
        cache.setDeptId(user.getDeptId());
        cache.setTenantId(user.getTenantId());
        cache.setSuperAdmin(user.isSuperAdmin());
        cache.setRoleCodes(new HashSet<>(user.getRoleCodes()));
        cache.setPermissions(new HashSet<>(user.getPermissions()));
        cache.setDataScope(user.getDataScope());
        cache.setScopeDeptIds(new HashSet<>(user.getScopeDeptIds()));
        return cache;
    }

    public LoginUser toLoginUser() {
        LoginUser user = new LoginUser();
        user.setUserId(userId);
        user.setUsername(username);
        user.setNickname(nickname);
        user.setStatus(status);
        user.setDeptId(deptId);
        user.setTenantId(tenantId);
        user.setSuperAdmin(superAdmin);
        user.setRoleCodes(roleCodes == null ? new HashSet<>() : new HashSet<>(roleCodes));
        user.setPermissions(permissions == null ? new HashSet<>() : new HashSet<>(permissions));
        user.setDataScope(dataScope == null ? 4 : dataScope);
        user.setScopeDeptIds(scopeDeptIds == null ? new HashSet<>() : new HashSet<>(scopeDeptIds));
        return user;
    }
}
