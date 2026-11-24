package com.minecraft.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.minecraft.entity.SysPermission;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.Collection;
import java.util.List;

@Mapper
public interface SysPermissionMapper extends BaseMapper<SysPermission> {

    /** 按角色ID集合查询权限编码 */
    @Select("<script>"
            + "SELECT DISTINCT p.code FROM sys_permission p "
            + "INNER JOIN sys_role_permission rp ON rp.permission_id = p.id "
            + "WHERE p.deleted = 0 AND p.status = 1 AND rp.role_id IN "
            + "<foreach collection='roleIds' item='roleId' open='(' separator=',' close=')'>#{roleId}</foreach>"
            + "</script>")
    List<String> selectPermCodesByRoleIds(@Param("roleIds") Collection<Long> roleIds);

    /** 按角色ID集合查询可见菜单（目录 + 菜单） */
    @Select("<script>"
            + "SELECT DISTINCT p.* FROM sys_permission p "
            + "INNER JOIN sys_role_permission rp ON rp.permission_id = p.id "
            + "WHERE p.deleted = 0 AND p.status = 1 AND p.visible = 1 AND p.type IN (1, 2) AND rp.role_id IN "
            + "<foreach collection='roleIds' item='roleId' open='(' separator=',' close=')'>#{roleId}</foreach>"
            + " ORDER BY p.sort ASC"
            + "</script>")
    List<SysPermission> selectMenusByRoleIds(@Param("roleIds") Collection<Long> roleIds);

    /** 全部可见菜单（超级管理员） */
    @Select("SELECT * FROM sys_permission "
            + "WHERE deleted = 0 AND status = 1 AND visible = 1 AND type IN (1, 2) ORDER BY sort ASC")
    List<SysPermission> selectAllMenus();

    /** 权限被角色引用的数量 */
    @Select("SELECT COUNT(1) FROM sys_role_permission WHERE permission_id = #{permissionId}")
    long countRoleRefs(@Param("permissionId") Long permissionId);
}
