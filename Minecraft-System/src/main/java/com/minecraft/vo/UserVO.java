package com.minecraft.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class UserVO {
    private Long id;
    /** 前端（Vben）使用的用户ID字段别名 */
    private Long userId;
    private Long deptId;
    private Long tenantId;
    private String account;
    private String username;
    private String email;
    private String phone;
    private String avatar;
    private String gender;
    private Integer age;
    private String occupation;
    private String hobbies;
    private String permissions;
    private String signature;
    private String nickname;
    private String experience;
    private String bio;
    /** 在线状态：0-在线 1-离线 */
    private Integer online;
    /** 账号状态：0-禁用 1-正常 */
    private Integer status;
    /** 积分余额 */
    private Integer points;
    /** 角色编码（RBAC） */
    private List<String> roles;
    /** 是否超级管理员 */
    private Boolean superAdmin;
    /** 前端（Vben）展示名称 */
    private String realName;
    private LocalDateTime createTime;
}
