package com.minecraft.vo;

import lombok.Data;

import java.util.Set;

/**
 * 当前登录用户信息 + 权限快照。
 */
@Data
public class UserMeVO {

    private UserVO userInfo;

    private Set<String> roles;

    private Set<String> permissions;

    private Integer dataScope;

    private Boolean superAdmin;
}
