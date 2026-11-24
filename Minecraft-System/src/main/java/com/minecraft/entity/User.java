package com.minecraft.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("user")
public class User {
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 部门ID（数据权限用） */
    private Long deptId;

    /** 租户ID（预留） */
    private Long tenantId;

    /** 是否超级管理员 1是 */
    private Integer superAdmin;
    
    private String account;
    
    private String username;
    
    private String password;
    
    private String email;
    
    private String phone;
    
    private String avatar;
    
    private String gender;
    
    private Integer age;
    
    private String occupation;
    
    private String hobbies;
    
    private String bio;
    
    private Integer online;
    
    private Integer status;
    
    private String signature;
    
    private String nickname;
    
    private String experience;

    private Integer points;

    private String permissions;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
