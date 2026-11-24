package com.minecraft.controller;

import com.minecraft.annotation.OperationLog;
import com.minecraft.dto.request.PageRequest;
import com.minecraft.dto.response.ApiResponse;
import com.minecraft.dto.response.PageResponse;
import com.minecraft.enums.OperationType;
import com.minecraft.service.SysUserService;
import com.minecraft.vo.UserVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 系统用户与角色分配（管理端）。
 */
@Tag(name = "系统管理-用户授权")
@RestController
@RequestMapping("/api/system/user")
@RequiredArgsConstructor
public class SysUserController {

    private final SysUserService sysUserService;

    @Operation(summary = "用户分页列表")
    @GetMapping("/page")
    @PreAuthorize("@perm.has('sys:user:list')")
    public ApiResponse<PageResponse<UserVO>> page(PageRequest request,
                                                 @RequestParam(required = false) Integer status) {
        return ApiResponse.success(sysUserService.page(request, status));
    }

    @Operation(summary = "用户已有角色ID集合")
    @GetMapping("/{userId}/roles")
    @PreAuthorize("@perm.hasAny('sys:user:list','sys:user:query','sys:user:edit','sys:user:assign')")
    public ApiResponse<List<Long>> roles(@PathVariable Long userId) {
        return ApiResponse.success(sysUserService.getRoleIds(userId));
    }

    @Operation(summary = "分配用户角色")
    @OperationLog(module = "USER", action = "分配用户角色", type = OperationType.UPDATE, bizId = "#userId",
            detail = "'角色数量：' + (#roleIds != null ? #roleIds.size() : 0)")
    @PutMapping("/{userId}/roles")
    @PreAuthorize("@perm.has('sys:user:assign')")
    public ApiResponse<Void> assignRoles(@PathVariable Long userId, @RequestBody List<Long> roleIds) {
        sysUserService.assignRoles(userId, roleIds);
        return ApiResponse.success("分配成功", null);
    }

    @Operation(summary = "启用/停用用户")
    @OperationLog(module = "USER", action = "修改用户状态", type = OperationType.UPDATE, bizId = "#userId",
            detail = "'状态：' + #status")
    @PutMapping("/{userId}/status")
    @PreAuthorize("@perm.has('sys:user:edit')")
    public ApiResponse<Void> updateStatus(@PathVariable Long userId, @RequestParam Integer status) {
        sysUserService.updateStatus(userId, status);
        return ApiResponse.success("操作成功", null);
    }
}
