package com.minecraft.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.minecraft.entity.SysPermissionGrant;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SysPermissionGrantMapper extends BaseMapper<SysPermissionGrant> {

    /** 当前生效的临时授予权限编码 */
    @Select("SELECT p.code FROM sys_permission_grant g "
            + "INNER JOIN sys_permission p ON p.id = g.permission_id "
            + "WHERE g.user_id = #{userId} AND g.effect_type = 1 "
            + "AND g.start_time <= NOW() AND g.end_time >= NOW() "
            + "AND p.deleted = 0 AND p.status = 1")
    List<String> selectActiveGrantCodes(@Param("userId") Long userId);

    /** 当前生效的临时拒绝权限编码（拒绝优先） */
    @Select("SELECT p.code FROM sys_permission_grant g "
            + "INNER JOIN sys_permission p ON p.id = g.permission_id "
            + "WHERE g.user_id = #{userId} AND g.effect_type = 2 "
            + "AND g.start_time <= NOW() AND g.end_time >= NOW() "
            + "AND p.deleted = 0 AND p.status = 1")
    List<String> selectActiveDenyCodes(@Param("userId") Long userId);
}
