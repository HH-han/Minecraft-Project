package com.minecraft.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.minecraft.dto.response.MenuVO;
import com.minecraft.entity.SysPermission;
import com.minecraft.entity.SysPermissionGrant;
import com.minecraft.entity.SysRole;
import com.minecraft.entity.SysRoleDept;
import com.minecraft.entity.User;
import com.minecraft.exception.BusinessException;
import com.minecraft.mapper.SysPermissionGrantMapper;
import com.minecraft.mapper.SysPermissionMapper;
import com.minecraft.mapper.SysRoleDeptMapper;
import com.minecraft.mapper.SysRoleMapper;
import com.minecraft.mapper.UserMapper;
import com.minecraft.security.cache.PermissionCacheManager;
import com.minecraft.security.core.LoginUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 用户权限加载服务：负责把「用户 → 角色 → 权限」解析为 {@link LoginUser}，并写入 Redis 缓存。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserPermissionService {

    /** 超级权限通配符 */
    public static final String WILDCARD_ALL = "*:*:*";

    private final UserMapper userMapper;
    private final SysRoleMapper roleMapper;
    private final SysPermissionMapper permissionMapper;
    private final SysRoleDeptMapper roleDeptMapper;
    private final SysPermissionGrantMapper grantMapper;
    private final PermissionCacheManager cacheManager;

    /**
     * 加载用户完整权限：缓存优先，未命中回源数据库。
     *
     * @return 用户不存在或已停用时返回 null
     */
    public LoginUser loadUserAuth(Long userId) {
        if (userId == null) {
            return null;
        }
        LoginUser cached = cacheManager.getUserAuth(userId);
        if (cached != null) {
            return cached.isEnabled() ? cached : null;
        }
        LoginUser user = buildFromDb(userId);
        if (user != null) {
            cacheManager.putUserAuth(user, collectRoleIds(userId));
        }
        return user;
    }

    /** 仅从数据库加载（不走缓存） */
    public LoginUser buildFromDb(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null || user.getStatus() == null || user.getStatus() != 1) {
            return null;
        }

        LoginUser loginUser = new LoginUser();
        loginUser.setUserId(user.getId());
        loginUser.setUsername(StringUtils.hasText(user.getAccount()) ? user.getAccount() : user.getUsername());
        loginUser.setNickname(user.getNickname());
        loginUser.setStatus(user.getStatus());
        loginUser.setDeptId(user.getDeptId() == null ? 0L : user.getDeptId());
        loginUser.setTenantId(user.getTenantId() == null ? 0L : user.getTenantId());

        // 兼容旧数据：permissions='0' 视为超级管理员
        boolean superAdmin = (user.getSuperAdmin() != null && user.getSuperAdmin() == 1)
                || "0".equals(user.getPermissions());
        loginUser.setSuperAdmin(superAdmin);

        List<SysRole> roles = roleMapper.selectEnabledRolesByUserId(userId);
        List<SysRole> allRoles = collectRolesWithParents(roles);
        Set<Long> roleIds = new LinkedHashSet<>();
        Set<String> roleCodes = new LinkedHashSet<>();
        for (SysRole role : allRoles) {
            roleIds.add(role.getId());
            if (StringUtils.hasText(role.getCode())) {
                roleCodes.add(role.getCode());
            }
        }
        loginUser.setRoleCodes(roleCodes);

        // 数据范围取最宽松（数值最小）的一个
        int dataScope = allRoles.stream()
                .map(SysRole::getDataScope)
                .filter(java.util.Objects::nonNull)
                .min(Integer::compareTo)
                .orElse(4);
        loginUser.setDataScope(dataScope);

        if (dataScope == 5 && !roleIds.isEmpty()) {
            List<SysRoleDept> roleDepts = roleDeptMapper.selectList(
                    new LambdaQueryWrapper<SysRoleDept>().in(SysRoleDept::getRoleId, roleIds));
            Set<Long> deptIds = new HashSet<>();
            roleDepts.forEach(rd -> deptIds.add(rd.getDeptId()));
            loginUser.setScopeDeptIds(deptIds);
        }

        Set<String> permissions = new HashSet<>();
        if (superAdmin) {
            permissions.add(WILDCARD_ALL);
        } else if (!roleIds.isEmpty()) {
            permissions.addAll(permissionMapper.selectPermCodesByRoleIds(roleIds));
        }

        // 临时授权：授予叠加，拒绝优先
        List<String> granted = grantMapper.selectActiveGrantCodes(userId);
        List<String> denied = grantMapper.selectActiveDenyCodes(userId);
        if (!CollectionUtils.isEmpty(granted)) {
            permissions.addAll(granted);
        }
        if (!CollectionUtils.isEmpty(denied)) {
            permissions.removeAll(denied);
        }
        loginUser.setPermissions(permissions);
        return loginUser;
    }

    /** 角色继承：递归收集父角色（带环检测，出现环时中断并告警） */
    private List<SysRole> collectRolesWithParents(List<SysRole> baseRoles) {
        List<SysRole> result = new ArrayList<>(baseRoles);
        Set<Long> visited = new HashSet<>();
        for (SysRole role : baseRoles) {
            visited.add(role.getId());
        }
        for (SysRole role : baseRoles) {
            collectParent(role, visited, result);
        }
        return result;
    }

    private void collectParent(SysRole role, Set<Long> visited, List<SysRole> result) {
        Long parentId = role.getParentId();
        if (parentId == null || parentId <= 0) {
            return;
        }
        if (!visited.add(parentId)) {
            log.warn("角色继承存在环或重复，已忽略 parentId={}", parentId);
            return;
        }
        SysRole parent = roleMapper.selectById(parentId);
        if (parent == null || parent.getStatus() == null || parent.getStatus() != 1) {
            return;
        }
        result.add(parent);
        collectParent(parent, visited, result);
    }

    /** 用户实际拥有的角色ID（含继承），用于缓存角色-用户映射 */
    public List<Long> collectRoleIds(Long userId) {
        List<SysRole> roles = roleMapper.selectEnabledRolesByUserId(userId);
        return collectRolesWithParents(roles).stream().map(SysRole::getId).distinct().toList();
    }

    /** 当前用户可见菜单树 */
    public List<MenuVO> getMenus(Long userId) {
        LoginUser loginUser = loadUserAuth(userId);
        if (loginUser == null) {
            throw new BusinessException(401, "登录状态已失效");
        }
        List<SysPermission> permissions;
        if (loginUser.isSuperAdmin()) {
            permissions = permissionMapper.selectAllMenus();
        } else {
            List<Long> roleIds = collectRoleIds(userId);
            permissions = roleIds.isEmpty()
                    ? List.of()
                    : permissionMapper.selectMenusByRoleIds(roleIds);
        }
        return buildTree(permissions);
    }

    /** 把权限列表构建为菜单树 */
    public List<MenuVO> buildTree(List<SysPermission> permissions) {
        if (CollectionUtils.isEmpty(permissions)) {
            return List.of();
        }
        Map<Long, MenuVO> nodeMap = new HashMap<>();
        for (SysPermission p : permissions) {
            MenuVO vo = new MenuVO();
            vo.setId(p.getId());
            vo.setParentId(p.getParentId());
            vo.setCode(p.getCode());
            vo.setName(p.getName());
            vo.setType(p.getType());
            vo.setPath(p.getPath());
            vo.setComponent(p.getComponent());
            vo.setIcon(p.getIcon());
            vo.setSort(p.getSort());
            nodeMap.put(p.getId(), vo);
        }
        List<MenuVO> roots = new ArrayList<>();
        for (MenuVO vo : nodeMap.values()) {
            MenuVO parent = nodeMap.get(vo.getParentId());
            if (parent == null) {
                roots.add(vo);
            } else {
                parent.getChildren().add(vo);
            }
        }
        sortTree(roots);
        return roots;
    }

    private void sortTree(List<MenuVO> nodes) {
        if (CollectionUtils.isEmpty(nodes)) {
            return;
        }
        nodes.sort(Comparator.comparing(n -> n.getSort() == null ? 0 : n.getSort()));
        for (MenuVO node : nodes) {
            sortTree(node.getChildren());
        }
    }

    public void evictUser(Long userId) {
        cacheManager.evictUser(userId);
    }

    public void evictByRole(Long roleId) {
        cacheManager.evictByRole(roleId);
    }

    public void evictAll() {
        cacheManager.evictAll();
    }
}
