package com.minecraft.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.minecraft.entity.SysPermission;
import com.minecraft.exception.BusinessException;
import com.minecraft.mapper.SysPermissionMapper;
import com.minecraft.mapper.SysRolePermissionMapper;
import com.minecraft.vo.PermissionScanResult;
import com.minecraft.vo.PermissionVO;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationContext;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 权限管理服务：权限树、增删改、接口权限扫描。
 */
@Service
@RequiredArgsConstructor
public class SysPermissionService {

    private final SysPermissionMapper permissionMapper;
    private final SysRolePermissionMapper rolePermissionMapper;
    private final UserPermissionService userPermissionService;
    private final ApplicationContext applicationContext;

    /** 完整权限树（含按钮/接口），供权限管理与角色授权使用 */
    public List<PermissionVO> tree() {
        List<SysPermission> list = permissionMapper.selectList(
                new LambdaQueryWrapper<SysPermission>()
                        .orderByAsc(SysPermission::getSort)
                        .orderByAsc(SysPermission::getId));
        return buildTree(list);
    }

    /** 仅目录 + 菜单 */
    public List<PermissionVO> menuTree() {
        List<SysPermission> list = permissionMapper.selectList(
                new LambdaQueryWrapper<SysPermission>()
                        .in(SysPermission::getType, List.of(1, 2))
                        .orderByAsc(SysPermission::getSort)
                        .orderByAsc(SysPermission::getId));
        return buildTree(list);
    }

    public SysPermission getById(Long id) {
        SysPermission permission = permissionMapper.selectById(id);
        if (permission == null) {
            throw new BusinessException(404, "权限不存在");
        }
        return permission;
    }

    @Transactional(rollbackFor = Exception.class)
    public SysPermission create(SysPermission permission) {
        validateCode(permission.getCode(), null);
        validateParent(permission.getParentId());
        permission.setId(null);
        if (permission.getParentId() == null) {
            permission.setParentId(0L);
        }
        if (permission.getType() == null) {
            permission.setType(2);
        }
        if (permission.getSort() == null) {
            permission.setSort(0);
        }
        if (permission.getVisible() == null) {
            permission.setVisible(1);
        }
        if (permission.getStatus() == null) {
            permission.setStatus(1);
        }
        permissionMapper.insert(permission);
        userPermissionService.evictAll();
        return permission;
    }

    @Transactional(rollbackFor = Exception.class)
    public SysPermission update(Long id, SysPermission permission) {
        getById(id);
        validateCode(permission.getCode(), id);
        validateParent(permission.getParentId());
        permission.setId(id);
        permissionMapper.updateById(permission);
        userPermissionService.evictAll();
        return getById(id);
    }

    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        getById(id);
        Long children = permissionMapper.selectCount(
                new LambdaQueryWrapper<SysPermission>().eq(SysPermission::getParentId, id));
        if (children != null && children > 0) {
            throw new BusinessException("存在子权限，请先删除子权限");
        }
        if (permissionMapper.countRoleRefs(id) > 0) {
            throw new BusinessException("该权限已被角色引用，禁止删除");
        }
        permissionMapper.deleteById(id);
        userPermissionService.evictAll();
    }

    /**
     * 扫描 Controller 上的 @PreAuthorize 权限码，与数据库做 diff。
     */
    public PermissionScanResult scan() {
        PermissionScanResult result = new PermissionScanResult();
        Set<String> dbCodes = new HashSet<>();
        for (SysPermission p : permissionMapper.selectList(new LambdaQueryWrapper<SysPermission>()
                .in(SysPermission::getType, List.of(3, 4)))) {
            if (StringUtils.hasText(p.getCode()) && !"*:*:*".equals(p.getCode())) {
                dbCodes.add(p.getCode());
            }
        }

        Set<String> codeCodes = new LinkedHashSet<>();
        List<String> unprotected = new ArrayList<>();
        Pattern quoted = Pattern.compile("'([^']+)'");

        // Actuator 也注册了 RequestMappingHandlerMapping（controllerEndpointHandlerMapping），
        // 必须按名称获取 MVC 的映射器，否则会因存在多个同类型 Bean 抛 NoUniqueBeanDefinitionException
        RequestMappingHandlerMapping mapping = applicationContext.getBean(
                "requestMappingHandlerMapping", RequestMappingHandlerMapping.class);
        for (Map.Entry<RequestMappingInfo, HandlerMethod> entry : mapping.getHandlerMethods().entrySet()) {
            HandlerMethod handlerMethod = entry.getValue();
            if (!handlerMethod.getBeanType().getName().startsWith("com.minecraft.controller")) {
                continue;
            }
            Set<String> paths = entry.getKey().getPathPatternsCondition() == null
                    ? Set.of()
                    : entry.getKey().getPathPatternsCondition().getPatternValues();
            boolean managed = paths.stream()
                    .anyMatch(path -> path.startsWith("/api/admin") || path.startsWith("/api/system"));
            if (!managed) {
                continue;
            }
            PreAuthorize preAuthorize = handlerMethod.getMethodAnnotation(PreAuthorize.class);
            if (preAuthorize == null) {
                String methods = entry.getKey().getMethodsCondition().getMethods().isEmpty()
                        ? "ANY"
                        : entry.getKey().getMethodsCondition().getMethods().stream()
                        .map(RequestMethod::name).sorted().toList().toString();
                unprotected.add(methods + " " + String.join(",", paths));
                continue;
            }
            Matcher matcher = quoted.matcher(preAuthorize.value());
            while (matcher.find()) {
                String code = matcher.group(1);
                if (code.contains(":") || "*:*:*".equals(code)) {
                    codeCodes.add(code);
                }
            }
        }

        Set<String> missingInDb = new LinkedHashSet<>(codeCodes);
        missingInDb.removeAll(dbCodes);
        Set<String> missingInCode = new LinkedHashSet<>(dbCodes);
        missingInCode.removeAll(codeCodes);

        result.setMissingInDb(new ArrayList<>(missingInDb));
        result.setMissingInCode(new ArrayList<>(missingInCode));
        result.setUnprotected(unprotected);
        return result;
    }

    private List<PermissionVO> buildTree(List<SysPermission> list) {
        if (CollectionUtils.isEmpty(list)) {
            return List.of();
        }
        Map<Long, PermissionVO> nodeMap = new HashMap<>();
        for (SysPermission p : list) {
            PermissionVO vo = new PermissionVO();
            vo.setId(p.getId());
            vo.setParentId(p.getParentId());
            vo.setCode(p.getCode());
            vo.setName(p.getName());
            vo.setType(p.getType());
            vo.setPath(p.getPath());
            vo.setComponent(p.getComponent());
            vo.setIcon(p.getIcon());
            vo.setMethod(p.getMethod());
            vo.setApiPath(p.getApiPath());
            vo.setSort(p.getSort());
            vo.setVisible(p.getVisible());
            vo.setStatus(p.getStatus());
            vo.setRemark(p.getRemark());
            nodeMap.put(p.getId(), vo);
        }
        List<PermissionVO> roots = new ArrayList<>();
        for (PermissionVO vo : nodeMap.values()) {
            PermissionVO parent = nodeMap.get(vo.getParentId());
            if (parent == null) {
                roots.add(vo);
            } else {
                parent.getChildren().add(vo);
            }
        }
        sort(roots);
        return roots;
    }

    private void sort(List<PermissionVO> nodes) {
        if (CollectionUtils.isEmpty(nodes)) {
            return;
        }
        nodes.sort(Comparator.comparing(n -> n.getSort() == null ? 0 : n.getSort()));
        nodes.forEach(n -> sort(n.getChildren()));
    }

    private void validateCode(String code, Long excludeId) {
        if (!StringUtils.hasText(code)) {
            throw new BusinessException("权限编码不能为空");
        }
        LambdaQueryWrapper<SysPermission> wrapper = new LambdaQueryWrapper<SysPermission>()
                .eq(SysPermission::getCode, code);
        if (excludeId != null) {
            wrapper.ne(SysPermission::getId, excludeId);
        }
        Long count = permissionMapper.selectCount(wrapper);
        if (count != null && count > 0) {
            throw new BusinessException("权限编码已存在：" + code);
        }
    }

    private void validateParent(Long parentId) {
        if (parentId == null || parentId <= 0) {
            return;
        }
        if (permissionMapper.selectById(parentId) == null) {
            throw new BusinessException("父权限不存在");
        }
    }
}
