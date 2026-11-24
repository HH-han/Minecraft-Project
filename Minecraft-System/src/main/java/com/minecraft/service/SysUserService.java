package com.minecraft.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.minecraft.dto.request.PageRequest;
import com.minecraft.dto.response.PageResponse;
import com.minecraft.entity.SysRole;
import com.minecraft.entity.SysUserRole;
import com.minecraft.entity.User;
import com.minecraft.exception.BusinessException;
import com.minecraft.mapper.SysRoleMapper;
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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 系统用户管理（用户角色分配）。
 */
@Service
@RequiredArgsConstructor
public class SysUserService {

    private final UserMapper userMapper;
    private final SysRoleMapper roleMapper;
    private final SysUserRoleMapper userRoleMapper;
    private final UserPermissionService userPermissionService;

    public PageResponse<UserVO> page(PageRequest request, Integer status) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        if (request != null && StringUtils.hasText(request.getKeyword())) {
            String keyword = request.getKeyword().trim();
            wrapper.and(w -> w.like(User::getAccount, keyword)
                    .or().like(User::getUsername, keyword)
                    .or().like(User::getNickname, keyword)
                    .or().like(User::getEmail, keyword));
        }
        if (status != null) {
            wrapper.eq(User::getStatus, status);
        }
        wrapper.orderByDesc(User::getId);
        PageRequest pageRequest = request == null ? new PageRequest() : request;
        Page<User> page = userMapper.selectPage(
                new Page<>(pageRequest.getPageNum(), pageRequest.getPageSize()), wrapper);
        List<UserVO> records = page.getRecords().stream().map(this::toVO).toList();
        return new PageResponse<>(records, page.getTotal(), pageRequest.getPageNum(), pageRequest.getPageSize());
    }

    public List<Long> getRoleIds(Long userId) {
        requireUser(userId);
        return userRoleMapper.selectList(
                        new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, userId))
                .stream().map(SysUserRole::getRoleId).toList();
    }

    @Transactional(rollbackFor = Exception.class)
    public void assignRoles(Long userId, List<Long> roleIds) {
        requireUser(userId);
        Set<Long> distinct = CollectionUtils.isEmpty(roleIds) ? Set.of() : new HashSet<>(roleIds);
        if (!distinct.isEmpty()) {
            Long roleCount = roleMapper.selectCount(new LambdaQueryWrapper<SysRole>().in(SysRole::getId, distinct));
            if (roleCount == null || roleCount != distinct.size()) {
                throw new BusinessException("存在无效的角色ID");
            }
        }
        userRoleMapper.delete(new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, userId));
        Long operatorId = SecurityUtils.getCurrentUserId();
        for (Long roleId : distinct) {
            SysUserRole userRole = new SysUserRole();
            userRole.setUserId(userId);
            userRole.setRoleId(roleId);
            userRole.setCreateBy(operatorId);
            userRoleMapper.insert(userRole);
        }
        userPermissionService.evictUser(userId);
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(Long userId, Integer status) {
        requireUser(userId);
        if (status == null || (status != 0 && status != 1)) {
            throw new BusinessException("状态值只能为 0 或 1");
        }
        if (status == 0 && userId.equals(SecurityUtils.getCurrentUserId())) {
            throw new BusinessException("不能停用当前登录账号");
        }
        User update = new User();
        update.setId(userId);
        update.setStatus(status);
        userMapper.updateById(update);
        userPermissionService.evictUser(userId);
    }

    private User requireUser(Long userId) {
        User user = userId == null ? null : userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }
        return user;
    }

    private UserVO toVO(User user) {
        UserVO vo = new UserVO();
        BeanUtils.copyProperties(user, vo);
        vo.setUserId(user.getId());
        vo.setRealName(StringUtils.hasText(user.getNickname()) ? user.getNickname() : user.getUsername());
        return vo;
    }
}
