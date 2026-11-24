package com.minecraft.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 权限树节点（管理端使用，包含按钮/接口字段）。
 */
@Data
public class PermissionVO {

    private Long id;

    private Long parentId;

    private String code;

    private String name;

    private Integer type;

    private String path;

    private String component;

    private String icon;

    private String method;

    private String apiPath;

    private Integer sort;

    private Integer visible;

    private Integer status;

    private String remark;

    private List<PermissionVO> children = new ArrayList<>();
}
