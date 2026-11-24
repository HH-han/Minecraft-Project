package com.minecraft.controller;

import com.minecraft.annotation.OperationLog;
import com.minecraft.dto.request.PageRequest;
import com.minecraft.dto.response.ApiResponse;
import com.minecraft.dto.response.PageResponse;
import com.minecraft.entity.SysRole;
import com.minecraft.enums.OperationType;
import com.minecraft.service.SysRoleService;
import com.minecraft.vo.UserVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 角色管理（管理端）。
 */
@Tag(name = "系统管理-角色")
@RestController
@RequestMapping("/api/system/role")
@RequiredArgsConstructor
public class SysRoleController {

    private final SysRoleService sysRoleService;

    @Operation(summary = "角色分页列表")
    @GetMapping("/list")
    @PreAuthorize("@perm.has('sys:role:list')")
    public ApiResponse<PageResponse<SysRole>> list(PageRequest request) {
        return ApiResponse.success(sysRoleService.page(request));
    }

    @Operation(summary = "全部角色（下拉选择）")
    @GetMapping("/all")
    @PreAuthorize("@perm.hasAny('sys:role:list','sys:user:list','sys:user:assign')")
    public ApiResponse<List<SysRole>> all() {
        return ApiResponse.success(sysRoleService.listAll());
    }

    @Operation(summary = "角色详情")
    @GetMapping("/{id}")
    @PreAuthorize("@perm.has('sys:role:query')")
    public ApiResponse<SysRole> detail(@PathVariable Long id) {
        return ApiResponse.success(sysRoleService.getById(id));
    }

    @Operation(summary = "新增角色")
    @OperationLog(module = "ROLE", action = "新增角色", type = OperationType.ADD, detail = "'新增角色：' + #role.name")
    @PostMapping
    @PreAuthorize("@perm.has('sys:role:add')")
    public ApiResponse<SysRole> create(@RequestBody SysRole role) {
        return ApiResponse.success("创建成功", sysRoleService.create(role));
    }

    @Operation(summary = "修改角色")
    @OperationLog(module = "ROLE", action = "修改角色", type = OperationType.UPDATE, bizId = "#id",
            detail = "'修改角色：' + #role.name")
    @PutMapping("/{id}")
    @PreAuthorize("@perm.has('sys:role:edit')")
    public ApiResponse<SysRole> update(@PathVariable Long id, @RequestBody SysRole role) {
        return ApiResponse.success("更新成功", sysRoleService.update(id, role));
    }

    @Operation(summary = "删除角色")
    @OperationLog(module = "ROLE", action = "删除角色", type = OperationType.DELETE, bizId = "#id")
    @DeleteMapping("/{id}")
    @PreAuthorize("@perm.has('sys:role:delete')")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        sysRoleService.delete(id);
        return ApiResponse.success("删除成功", null);
    }

    @Operation(summary = "角色已有权限ID集合")
    @GetMapping("/{id}/permissions")
    @PreAuthorize("@perm.has('sys:role:query')")
    public ApiResponse<List<Long>> permissions(@PathVariable Long id) {
        return ApiResponse.success(sysRoleService.getPermissionIds(id));
    }

    @Operation(summary = "分配角色权限")
    @OperationLog(module = "ROLE", action = "分配角色权限", type = OperationType.UPDATE, bizId = "#id",
            detail = "'权限数量：' + (#permissionIds != null ? #permissionIds.size() : 0)")
    @PutMapping("/{id}/permissions")
    @PreAuthorize("@perm.has('sys:role:assign')")
    public ApiResponse<Void> assignPermissions(@PathVariable Long id, @RequestBody List<Long> permissionIds) {
        sysRoleService.assignPermissions(id, permissionIds);
        return ApiResponse.success("分配成功", null);
    }

    @Operation(summary = "角色下用户列表")
    @GetMapping("/{id}/users")
    @PreAuthorize("@perm.has('sys:role:query')")
    public ApiResponse<List<UserVO>> users(@PathVariable Long id) {
        return ApiResponse.success(sysRoleService.getUsers(id));
    }
}
