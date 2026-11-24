package com.minecraft.dto.response;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 菜单树节点。
 */
@Data
public class MenuVO {

    private Long id;

    private Long parentId;

    private String code;

    private String name;

    /** 1目录 2菜单 3按钮 4接口 5数据 */
    private Integer type;

    private String path;

    private String component;

    private String icon;

    private Integer sort;

    private List<MenuVO> children = new ArrayList<>();
}
