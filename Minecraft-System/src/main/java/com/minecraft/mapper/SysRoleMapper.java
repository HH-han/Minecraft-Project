package com.minecraft.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.minecraft.entity.SysRole;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SysRoleMapper extends BaseMapper<SysRole> {

    /** 查询用户启用的角色（含继承所需的父角色信息） */
    @Select("SELECT r.* FROM sys_role r "
            + "INNER JOIN sys_user_role ur ON ur.role_id = r.id "
            + "WHERE ur.user_id = #{userId} AND r.deleted = 0 AND r.status = 1 "
            + "ORDER BY r.sort ASC")
    List<SysRole> selectEnabledRolesByUserId(@Param("userId") Long userId);

    /** 角色下用户数 */
    @Select("SELECT COUNT(1) FROM sys_user_role WHERE role_id = #{roleId}")
    long countUsersByRole(@Param("roleId") Long roleId);

    /** 角色下用户ID */
    @Select("SELECT user_id FROM sys_user_role WHERE role_id = #{roleId}")
    List<Long> selectUserIdsByRole(@Param("roleId") Long roleId);
}
