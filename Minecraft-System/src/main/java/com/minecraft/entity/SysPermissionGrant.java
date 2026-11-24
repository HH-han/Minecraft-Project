package com.minecraft.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 临时权限授权（带时效）。
 */
@Data
@TableName("sys_permission_grant")
public class SysPermissionGrant {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long permissionId;

    /** 1授予 2拒绝（拒绝优先于角色授予） */
    private Integer effectType;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private String reason;

    private Long createBy;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
