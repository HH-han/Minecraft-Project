package com.minecraft.security.core;

import com.minecraft.utils.SecurityUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Set;

/**
 * 权限判断 Bean，供 Spring Security SpEL 调用：
 *
 * <pre>
 * &#64;PreAuthorize("@perm.has('sys:user:add')")
 * &#64;PreAuthorize("@perm.hasAny('a:b:c','d:e:f')")
 * &#64;PreAuthorize("@perm.hasAll('a:b:c','d:e:f')")
 * &#64;PreAuthorize("@perm.hasRole('super_admin')")
 * </pre>
 */
@Component("perm")
public class PermissionChecker {

    /** 应急旁路开关：perm.enabled=false 时全部放行 */
    @Value("${perm.enabled:true}")
    private boolean enabled;

    public boolean has(String code) {
        if (!enabled) {
            return true;
        }
        LoginUser user = SecurityUtils.getLoginUser();
        if (user == null || !StringUtils.hasText(code)) {
            return false;
        }
        if (user.isSuperAdmin() || user.getPermissions().contains(UserPermissionCodes.WILDCARD_ALL)) {
            return true;
        }
        return match(user.getPermissions(), code);
    }

    public boolean hasAny(String... codes) {
        if (codes == null || codes.length == 0) {
            return false;
        }
        for (String code : codes) {
            if (has(code)) {
                return true;
            }
        }
        return false;
    }

    public boolean hasAll(String... codes) {
        if (codes == null || codes.length == 0) {
            return false;
        }
        for (String code : codes) {
            if (!has(code)) {
                return false;
            }
        }
        return true;
    }

    public boolean hasRole(String roleCode) {
        if (!enabled) {
            return true;
        }
        LoginUser user = SecurityUtils.getLoginUser();
        return user != null && user.getRoleCodes().contains(roleCode);
    }

    public boolean isSuperAdmin() {
        LoginUser user = SecurityUtils.getLoginUser();
        return user != null && user.isSuperAdmin();
    }

    /**
     * 通配符匹配：持有码中的 * 可匹配请求码任意段。
     * 持有 travel:*:*，请求 travel:hotel:edit → true
     */
    public boolean match(Set<String> owned, String required) {
        if (owned == null || owned.isEmpty()) {
            return false;
        }
        if (owned.contains(required)) {
            return true;
        }
        String[] req = required.split(":");
        for (String ownCode : owned) {
            String[] own = ownCode.split(":");
            if (own.length != req.length) {
                continue;
            }
            boolean ok = true;
            for (int i = 0; i < own.length; i++) {
                if (!"*".equals(own[i]) && !own[i].equals(req[i])) {
                    ok = false;
                    break;
                }
            }
            if (ok) {
                return true;
            }
        }
        return false;
    }
}
