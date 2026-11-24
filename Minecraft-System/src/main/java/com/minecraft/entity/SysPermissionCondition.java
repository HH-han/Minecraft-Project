package com.minecraft.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 权限条件表达式（ABAC 扩展预留）。
 */
@Data
@TableName("sys_permission_condition")
public class SysPermissionCondition {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long permissionId;

    private String expr;

    private Integer enabled;

    private String remark;
}
