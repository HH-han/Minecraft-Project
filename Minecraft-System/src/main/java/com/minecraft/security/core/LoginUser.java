package com.minecraft.security.core;

import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 登录用户上下文，承载 RBAC 权限信息。
 *
 * <p>JWT 中只保存 userId/username，权限在服务端 Redis 缓存并在此对象中还原，
 * 便于动态调权与踢人下线。</p>
 */
@Data
public class LoginUser implements UserDetails {

    private Long userId;

    /** 登录账号（account） */
    private String username;

    private String nickname;

    private String password;

    private Integer status;

    private Long deptId;

    private Long tenantId;

    private boolean superAdmin;

    /** 角色编码集合 */
    private Set<String> roleCodes = new HashSet<>();

    /** 权限编码集合 */
    private Set<String> permissions = new HashSet<>();

    /** 数据范围（取并集中最宽松的） */
    private Integer dataScope = 4;

    /** 自定义数据范围部门ID */
    private Set<Long> scopeDeptIds = new HashSet<>();

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return permissions.stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toSet());
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return status != null && status == 1;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return status != null && status == 1;
    }
}
