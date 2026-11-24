package com.minecraft.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.minecraft.dto.request.PageRequest;
import com.minecraft.dto.response.PageResponse;
import com.minecraft.entity.SysRole;
import com.minecraft.entity.SysRolePermission;
import com.minecraft.entity.SysUserRole;
import com.minecraft.entity.User;
import com.minecraft.exception.BusinessException;
import com.minecraft.mapper.SysRoleMapper;
import com.minecraft.mapper.SysRolePermissionMapper;
import com.minecraft.mapper.SysUserRoleMapper;
import com.minecraft.mapper.UserMapper;
import com.minecraft.utils.SecurityUtils;
import com.minecraft.vo.UserVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 角色管理服务。
 */
@Service
@RequiredArgsConstructor
public class SysRoleService {

    private static final int BUILT_IN_YES = 1;

    private final SysRoleMapper roleMapper;
    private final SysRolePermissionMapper rolePermissionMapper;
    private final SysUserRoleMapper userRoleMapper;
    private final UserMapper userMapper;
    private final UserPermissionService userPermissionService;

    public PageResponse<SysRole> page(PageRequest request) {
        LambdaQueryWrapper<SysRole> wrapper = new LambdaQueryWrapper<>();
        if (request != null && StringUtils.hasText(request.getKeyword())) {
            String keyword = request.getKeyword().trim();
            wrapper.and(w -> w.like(SysRole::getName, keyword).or().like(SysRole::getCode, keyword));
        }
        wrapper.orderByAsc(SysRole::getSort).orderByAsc(SysRole::getId);
        PageRequest pageRequest = request == null ? new PageRequest() : request;
        Page<SysRole> page = roleMapper.selectPage(
                new Page<>(pageRequest.getPageNum(), pageRequest.getPageSize()), wrapper);
        return new PageResponse<>(page.getRecords(), page.getTotal(),
                pageRequest.getPageNum(), pageRequest.getPageSize());
    }

    public List<SysRole> listAll() {
        return roleMapper.selectList(new LambdaQueryWrapper<SysRole>()
                .orderByAsc(SysRole::getSort).orderByAsc(SysRole::getId));
    }

    public SysRole getById(Long id) {
        SysRole role = roleMapper.selectById(id);
        if (role == null) {
            throw new BusinessException(404, "角色不存在");
        }
        return role;
    }

    @Transactional(rollbackFor = Exception.class)
    public SysRole create(SysRole role) {
        validateCode(role.getCode(), null);
        validateParent(null, role.getParentId());
        role.setId(null);
        if (role.getTenantId() == null) {
            role.setTenantId(0L);
        }
        if (role.getParentId() == null) {
            role.setParentId(0L);
        }
        if (role.getDataScope() == null) {
            role.setDataScope(4);
        }
        if (role.getStatus() == null) {
            role.setStatus(1);
        }
        if (role.getBuiltIn() == null) {
            role.setBuiltIn(0);
        }
        if (role.getSort() == null) {
            role.setSort(0);
        }
        role.setCreateBy(SecurityUtils.getCurrentUserId());
        roleMapper.insert(role);
        return role;
    }

    @Transactional(rollbackFor = Exception.class)
    public SysRole update(Long id, SysRole role) {
        SysRole exist = getById(id);
        if (exist.getBuiltIn() != null && exist.getBuiltIn() == BUILT_IN_YES
                && StringUtils.hasText(role.getCode()) && !exist.getCode().equals(role.getCode())) {
            throw new BusinessException("内置角色不允许修改编码");
        }
        validateCode(role.getCode(), id);
        validateParent(id, role.getParentId());
        role.setId(id);
        role.setBuiltIn(exist.getBuiltIn());
        role.setUpdateBy(SecurityUtils.getCurrentUserId());
        roleMapper.updateById(role);
        userPermissionService.evictAll();
        return getById(id);
    }

    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        SysRole role = getById(id);
        if (role.getBuiltIn() != null && role.getBuiltIn() == BUILT_IN_YES) {
            throw new BusinessException("内置角色禁止删除");
        }
        if (roleMapper.countUsersByRole(id) > 0) {
            throw new BusinessException("该角色下仍有用户，请先解除绑定");
        }
        Long childCount = roleMapper.selectCount(new LambdaQueryWrapper<SysRole>().eq(SysRole::getParentId, id));
        if (childCount != null && childCount > 0) {
            throw new BusinessException("该角色存在子角色，请先删除子角色或调整继承关系");
        }
        rolePermissionMapper.delete(new LambdaQueryWrapper<SysRolePermission>().eq(SysRolePermission::getRoleId, id));
        roleMapper.deleteById(id);
        userPermissionService.evictAll();
    }

    public List<Long> getPermissionIds(Long roleId) {
        getById(roleId);
        return rolePermissionMapper.selectList(
                        new LambdaQueryWrapper<SysRolePermission>().eq(SysRolePermission::getRoleId, roleId))
                .stream().map(SysRolePermission::getPermissionId).toList();
    }

    @Transactional(rollbackFor = Exception.class)
    public void assignPermissions(Long roleId, List<Long> permissionIds) {
        getById(roleId);
        rolePermissionMapper.delete(new LambdaQueryWrapper<SysRolePermission>()
                .eq(SysRolePermission::getRoleId, roleId));
        if (!CollectionUtils.isEmpty(permissionIds)) {
            Long operatorId = SecurityUtils.getCurrentUserId();
            Set<Long> distinct = new HashSet<>(permissionIds);
            for (Long permissionId : distinct) {
                if (permissionId == null) {
                    continue;
                }
                SysRolePermission rp = new SysRolePermission();
                rp.setRoleId(roleId);
                rp.setPermissionId(permissionId);
                rp.setCreateBy(operatorId);
                rolePermissionMapper.insert(rp);
            }
        }
        userPermissionService.evictAll();
    }

    public List<UserVO> getUsers(Long roleId) {
        getById(roleId);
        List<Long> userIds = roleMapper.selectUserIdsByRole(roleId);
        if (CollectionUtils.isEmpty(userIds)) {
            return List.of();
        }
        List<User> users = userMapper.selectBatchIds(userIds);
        List<UserVO> result = new ArrayList<>(users.size());
        for (User user : users) {
            UserVO vo = new UserVO();
            BeanUtils.copyProperties(user, vo);
            vo.setUserId(user.getId());
            vo.setRealName(StringUtils.hasText(user.getNickname()) ? user.getNickname() : user.getUsername());
            result.add(vo);
        }
        return result;
    }

    private void validateCode(String code, Long excludeId) {
        if (!StringUtils.hasText(code)) {
            throw new BusinessException("角色编码不能为空");
        }
        LambdaQueryWrapper<SysRole> wrapper = new LambdaQueryWrapper<SysRole>().eq(SysRole::getCode, code);
        if (excludeId != null) {
            wrapper.ne(SysRole::getId, excludeId);
        }
        Long count = roleMapper.selectCount(wrapper);
        if (count != null && count > 0) {
            throw new BusinessException("角色编码已存在：" + code);
        }
    }

    /** 校验角色继承不成环 */
    private void validateParent(Long roleId, Long parentId) {
        if (parentId == null || parentId <= 0) {
            return;
        }
        if (roleId != null && roleId.equals(parentId)) {
            throw new BusinessException("父角色不能是自身");
        }
        Set<Long> visited = new HashSet<>();
        Long current = parentId;
        while (current != null && current > 0) {
            if (!visited.add(current)) {
                throw new BusinessException("角色继承存在环");
            }
            if (roleId != null && roleId.equals(current)) {
                throw new BusinessException("角色继承存在环");
            }
            SysRole parent = roleMapper.selectById(current);
            if (parent == null) {
                break;
            }
            current = parent.getParentId();
        }
    }
}
