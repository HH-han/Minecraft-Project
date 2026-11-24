package com.minecraft.vo;

import lombok.Data;

/**
 * 用户-角色行（批量查询用户角色时使用）。
 */
@Data
public class UserRoleRowVO {

    private Long userId;

    private Long roleId;

    private String roleCode;

    private String roleName;
}
