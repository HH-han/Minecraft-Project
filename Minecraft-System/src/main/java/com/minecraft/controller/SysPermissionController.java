package com.minecraft.controller;

import com.minecraft.annotation.OperationLog;
import com.minecraft.dto.response.ApiResponse;
import com.minecraft.entity.SysPermission;
import com.minecraft.enums.OperationType;
import com.minecraft.service.SysPermissionService;
import com.minecraft.vo.PermissionScanResult;
import com.minecraft.vo.PermissionVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 权限管理（管理端）。
 */
@Tag(name = "系统管理-权限")
@RestController
@RequestMapping("/api/system/permission")
@RequiredArgsConstructor
public class SysPermissionController {

    private final SysPermissionService sysPermissionService;

    @Operation(summary = "完整权限树（含按钮/接口）")
    @GetMapping("/tree")
    @PreAuthorize("@perm.has('sys:permission:list')")
    public ApiResponse<List<PermissionVO>> tree() {
        return ApiResponse.success(sysPermissionService.tree());
    }

    @Operation(summary = "菜单树（仅目录/菜单）")
    @GetMapping("/menu-tree")
    @PreAuthorize("@perm.has('sys:permission:list')")
    public ApiResponse<List<PermissionVO>> menuTree() {
        return ApiResponse.success(sysPermissionService.menuTree());
    }

    @Operation(summary = "权限详情")
    @GetMapping("/{id}")
    @PreAuthorize("@perm.has('sys:permission:list')")
    public ApiResponse<SysPermission> detail(@PathVariable Long id) {
        return ApiResponse.success(sysPermissionService.getById(id));
    }

    @Operation(summary = "新增权限")
    @OperationLog(module = "PERMISSION", action = "新增权限", type = OperationType.ADD,
            detail = "'新增权限：' + #permission.code")
    @PostMapping
    @PreAuthorize("@perm.has('sys:permission:add')")
    public ApiResponse<SysPermission> create(@RequestBody SysPermission permission) {
        return ApiResponse.success("创建成功", sysPermissionService.create(permission));
    }

    @Operation(summary = "修改权限")
    @OperationLog(module = "PERMISSION", action = "修改权限", type = OperationType.UPDATE, bizId = "#id",
            detail = "'修改权限：' + #permission.code")
    @PutMapping("/{id}")
    @PreAuthorize("@perm.has('sys:permission:edit')")
    public ApiResponse<SysPermission> update(@PathVariable Long id, @RequestBody SysPermission permission) {
        return ApiResponse.success("更新成功", sysPermissionService.update(id, permission));
    }

    @Operation(summary = "删除权限")
    @OperationLog(module = "PERMISSION", action = "删除权限", type = OperationType.DELETE, bizId = "#id")
    @DeleteMapping("/{id}")
    @PreAuthorize("@perm.has('sys:permission:delete')")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        sysPermissionService.delete(id);
        return ApiResponse.success("删除成功", null);
    }

    @Operation(summary = "扫描接口权限（开发辅助）")
    @PostMapping("/scan")
    @PreAuthorize("@perm.has('sys:permission:add')")
    public ApiResponse<PermissionScanResult> scan() {
        return ApiResponse.success(sysPermissionService.scan());
    }
}
