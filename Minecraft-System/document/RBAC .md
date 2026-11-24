# Minecraft 旅游系统 · 用户权限管理开发文档

> 版本：v1.0
> 适用项目：Minecraft-system（Spring Boot 3 + Vue 3 + uni-app）
> 文档定位：在现有 JWT 认证体系上，构建一套 **可扩展、可审计、可平滑迁移** 的统一权限管理方案。

---

## 一、文档说明

### 1.1 背景

项目当前已完成：

- JWT Token 认证（`jjwt 0.12.5`）
- Spring Security 基础安全配置
- 滑块验证码（TIANAI-CAPTCHA）
- BCrypt 密码加密
- Token 拦截器（`interceptor` 包）

但**尚未建立统一的权限模型**，导致：

| 现存问题 | 影响 |
|---------|------|
| 用户端 / 管理端鉴权逻辑分散 | 新增功能需重复开发鉴权代码 |
| 无角色概念 | 无法批量授权、无法区分运营/审核/超级管理员 |
| 无菜单/按钮级权限 | 管理端菜单对所有账号可见，前端无法按权限渲染 |
| 无数据权限 | 运营人员可查看全部订单，存在越权风险 |
| 无权限缓存 | 每次请求均需查库，性能瓶颈 |
| 无授权审计 | 授权变更无法追溯 |

### 1.2 设计目标

| 目标 | 说明 |
|------|------|
| **统一** | 用户端、管理端、移动端共用一套权限模型与接口 |
| **细粒度** | 支持 目录 / 菜单 / 按钮 / 接口 / 数据 五级权限 |
| **高性能** | 权限信息 Redis 缓存，常规请求零查库 |
| **可扩展** | 预留多租户、数据权限、临时授权、ABAC 扩展位 |
| **可审计** | 所有授权变更、越权访问均落日志 |
| **向后兼容** | 不破坏现有 `user` 表与 JWT 流程，增量迁移 |

### 1.3 设计原则

1. **RBAC 为主干，ABAC 为扩展**：当前使用 RBAC（用户-角色-权限），预留条件权限表支持未来属性化授权。
2. **权限编码即契约**：前后端、各端统一使用 `module:resource:action` 编码。
3. **默认拒绝**：未显式授权的接口一律拒绝（`deny by default`）。
4. **权限可组合**：角色可继承，权限可分组，数据范围可叠加。

---

## 二、权限模型设计

### 2.1 整体模型

```
┌────────┐     ┌───────────────┐     ┌──────────────┐
│  User  │────▶│  sys_user_role│◀────│   sys_role   │
└────────┘     └───────────────┘     └──────┬───────┘
                                            │ 继承(parent_id)
                                            │
                              ┌─────────────▼──────────────┐
                              │   sys_role_permission      │
                              └─────────────┬──────────────┘
                                            │
                                            ▼
                                  ┌──────────────────┐
                                  │  sys_permission  │
                                  │  目录/菜单/按钮   │
                                  │  /接口/数据       │
                                  └──────────────────┘

                 数据权限扩展：sys_role.data_scope + sys_role_dept
                 临时授权扩展：sys_permission_grant
                 条件权限扩展：sys_permission_condition (预留)
```

### 2.2 核心概念

| 概念 | 说明 |
|------|------|
| **权限（Permission）** | 一个可被授权的原子单元，用唯一编码标识 |
| **角色（Role）** | 权限的集合，可继承父角色权限 |
| **数据范围（DataScope）** | 角色对业务数据的可见范围（行级） |
| **权限编码（Code）** | `模块:资源:操作`，如 `travel:hotel:edit` |
| **通配符** | `*:*:*` 表示超级权限（仅超管角色拥有） |

### 2.3 权限类型定义

| type | 名称 | 用途 | 示例 |
|------|------|------|------|
| 1 | 目录 | 菜单分组，可折叠 | 旅行管理 |
| 2 | 菜单 | 对应一个前端路由页面 | 景点管理 |
| 3 | 按钮 | 页面内的操作点 | 新增景点 / 导出 |
| 4 | 接口 | 纯后端 API 权限（无前端入口） | 推荐引擎调度 |
| 5 | 数据 | 数据范围标识（配合 DataScope） | 订单全局查看 |

### 2.4 权限编码规范

**格式**：`{模块}:{资源}:{操作}`，全小写，冒号分隔。

| 模块 | 前缀 | 示例 |
|------|------|------|
| 系统管理 | `sys` | `sys:user:list`、`sys:role:assign` |
| 内容管理 | `content` | `content:announcement:publish` |
| 旅行管理 | `travel` | `travel:attraction:edit` |
| 推荐管理 | `reco` | `reco:rule:update` |
| 用户管理 | `member` | `member:login-log:export` |
| 服务管理 | `ops` | `ops:cache:clear` |
| 移动端 | `app` | `app:community:post` |

**操作词汇表（固定）**：

`list` 列表 / `query` 详情 / `add` 新增 / `edit` 修改 / `delete` 删除 / `export` 导出 / `import` 导入 / `audit` 审核 / `publish` 发布 / `assign` 分配 / `grant` 授权 / `clear` 清理

**通配符规则**：

| 写法 | 含义 |
|------|------|
| `*:*:*` | 全部权限（仅 `super_admin`） |
| `travel:*:*` | 旅行模块全部权限 |
| `travel:hotel:*` | 酒店资源全部操作 |

匹配逻辑：**请求权限码** 与 **用户持有码** 按段比对，持有码中的 `*` 匹配任意段。

---

## 三、数据库设计

> 约定：沿用项目现有规范 —— `utf8mb4`、逻辑删除 `deleted`、审计字段 `create_by/create_time/update_by/update_time`、主键 `BIGINT AUTO_INCREMENT`。所有表均预留 `tenant_id` 用于未来多租户。

### 3.1 角色表 `sys_role`

```sql
CREATE TABLE `sys_role` (
  `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '角色ID',
  `tenant_id`   BIGINT       NOT NULL DEFAULT 0      COMMENT '租户ID（预留，0=默认租户）',
  `parent_id`   BIGINT       NOT NULL DEFAULT 0      COMMENT '父角色ID（0=顶级，用于角色继承）',
  `code`        VARCHAR(64)  NOT NULL                COMMENT '角色编码，如 super_admin',
  `name`        VARCHAR(64)  NOT NULL                COMMENT '角色名称',
  `data_scope`  TINYINT      NOT NULL DEFAULT 4      COMMENT '数据范围 1全部 2本部门 3本部门及以下 4仅本人 5自定义',
  `built_in`    TINYINT      NOT NULL DEFAULT 0      COMMENT '是否内置角色 1是（不可删除）',
  `sort`        INT          NOT NULL DEFAULT 0      COMMENT '排序',
  `status`      TINYINT      NOT NULL DEFAULT 1      COMMENT '状态 1启用 0停用',
  `remark`      VARCHAR(255) DEFAULT NULL            COMMENT '备注',
  `create_by`   BIGINT       DEFAULT NULL,
  `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_by`   BIGINT       DEFAULT NULL,
  `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted`     TINYINT      NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_tenant_code` (`tenant_id`, `code`, `deleted`),
  KEY `idx_parent` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';
```

### 3.2 权限表 `sys_permission`

```sql
CREATE TABLE `sys_permission` (
  `id`         BIGINT       NOT NULL AUTO_INCREMENT COMMENT '权限ID',
  `parent_id`  BIGINT       NOT NULL DEFAULT 0      COMMENT '父权限ID（构建树）',
  `code`       VARCHAR(128) NOT NULL                COMMENT '权限编码 module:resource:action',
  `name`       VARCHAR(64)  NOT NULL                COMMENT '权限名称',
  `type`       TINYINT      NOT NULL DEFAULT 2      COMMENT '1目录 2菜单 3按钮 4接口 5数据',
  `path`       VARCHAR(255) DEFAULT NULL            COMMENT '路由路径（目录/菜单）',
  `component`  VARCHAR(255) DEFAULT NULL            COMMENT '前端组件路径（菜单）',
  `icon`       VARCHAR(64)  DEFAULT NULL            COMMENT '图标',
  `method`     VARCHAR(10)  DEFAULT NULL            COMMENT 'HTTP方法（type=4 时可选，用于生成API资源表）',
  `api_path`   VARCHAR(255) DEFAULT NULL            COMMENT 'API路径（type=4）',
  `sort`       INT          NOT NULL DEFAULT 0,
  `visible`    TINYINT      NOT NULL DEFAULT 1      COMMENT '是否显示 1是 0否',
  `status`     TINYINT      NOT NULL DEFAULT 1      COMMENT '状态 1启用 0停用',
  `remark`     VARCHAR(255) DEFAULT NULL,
  `create_time` DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted`    TINYINT      NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code` (`code`, `deleted`),
  KEY `idx_parent` (`parent_id`),
  KEY `idx_type` (`type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='权限表';
```

### 3.3 关联表

```sql
-- 用户-角色
CREATE TABLE `sys_user_role` (
  `id`         BIGINT NOT NULL AUTO_INCREMENT,
  `user_id`    BIGINT NOT NULL,
  `role_id`    BIGINT NOT NULL,
  `create_by`  BIGINT DEFAULT NULL,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_role` (`user_id`, `role_id`),
  KEY `idx_role` (`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色关联表';

-- 角色-权限
CREATE TABLE `sys_role_permission` (
  `id`            BIGINT NOT NULL AUTO_INCREMENT,
  `role_id`       BIGINT NOT NULL,
  `permission_id` BIGINT NOT NULL,
  `create_by`     BIGINT DEFAULT NULL,
  `create_time`   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_role_perm` (`role_id`, `permission_id`),
  KEY `idx_perm` (`permission_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色权限关联表';

-- 角色自定义数据范围（data_scope=5 时生效）
CREATE TABLE `sys_role_dept` (
  `id`      BIGINT NOT NULL AUTO_INCREMENT,
  `role_id` BIGINT NOT NULL,
  `dept_id` BIGINT NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_role_dept` (`role_id`, `dept_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色部门数据范围表';
```

### 3.4 扩展预留表

```sql
-- 临时授权（带时效）：用于"临时给某运营开通导出权限 3 天"
CREATE TABLE `sys_permission_grant` (
  `id`            BIGINT      NOT NULL AUTO_INCREMENT,
  `user_id`       BIGINT      NOT NULL,
  `permission_id` BIGINT      NOT NULL,
  `effect_type`   TINYINT     NOT NULL DEFAULT 1 COMMENT '1授予 2拒绝（拒绝优先于角色授予）',
  `start_time`    DATETIME    NOT NULL,
  `end_time`      DATETIME    NOT NULL,
  `reason`        VARCHAR(255) DEFAULT NULL,
  `create_by`     BIGINT      DEFAULT NULL,
  `create_time`   DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user_time` (`user_id`, `start_time`, `end_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='临时权限授权表';

-- ABAC 条件权限（预留）：如"仅允许编辑自己创建且状态=草稿的景点"
CREATE TABLE `sys_permission_condition` (
  `id`            BIGINT       NOT NULL AUTO_INCREMENT,
  `permission_id` BIGINT       NOT NULL,
  `expr`          VARCHAR(1024) NOT NULL COMMENT '条件表达式(SpEL/自研DSL)，如 #ownerId == #currentUserId',
  `enabled`       TINYINT      NOT NULL DEFAULT 1,
  `remark`        VARCHAR(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_perm` (`permission_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='权限条件表达式表（ABAC扩展预留）';
```

### 3.5 现有 `user` 表增量改造

> 采用**增量 ALTER**，不重建表，保证线上数据安全。

```sql
ALTER TABLE `user`
  ADD COLUMN `dept_id`    BIGINT  NOT NULL DEFAULT 0 COMMENT '部门ID（数据权限用）' AFTER `id`,
  ADD COLUMN `tenant_id`  BIGINT  NOT NULL DEFAULT 0 COMMENT '租户ID（预留）'        AFTER `dept_id`,
  ADD COLUMN `super_admin` TINYINT NOT NULL DEFAULT 0 COMMENT '是否超级管理员 1是'   AFTER `tenant_id`;

CREATE INDEX `idx_dept`  ON `user` (`dept_id`);
CREATE INDEX `idx_tenant` ON `user` (`tenant_id`);
```

### 3.6 初始化数据

```sql
-- 内置角色
INSERT INTO `sys_role` (`id`,`code`,`name`,`data_scope`,`built_in`,`sort`,`remark`) VALUES
 (1,'super_admin', '超级管理员', 1, 1, 1, '系统内置，拥有全部权限'),
 (2,'content_admin','内容运营',  1, 1, 2, '负责公告/轮播/社区内容'),
 (3,'travel_admin', '旅行运营',  2, 1, 3, '负责景点/酒店/美食/商品'),
 (4,'order_admin',  '订单客服',  4, 1, 4, '仅可查看本人处理订单'),
 (5,'normal_user',  '普通用户',  4, 1, 9, 'C端注册用户默认角色');

-- 顶级菜单（节选，完整脚本见 db_permission.sql）
INSERT INTO `sys_permission` (`id`,`parent_id`,`code`,`name`,`type`,`path`,`component`,`icon`,`sort`) VALUES
 (100,0,  'sys',      '系统管理', 1,'/system',  NULL,                  'SettingOutlined', 90),
 (101,100,'sys:user', '用户管理', 2,'/system/user','system/user/index', 'UserOutlined',   1),
 (102,101,'sys:user:list','用户列表',3,NULL,NULL,NULL,1),
 (103,101,'sys:user:add', '新增用户',3,NULL,NULL,NULL,2),
 (104,101,'sys:user:edit','编辑用户',3,NULL,NULL,NULL,3),
 (105,101,'sys:user:delete','删除用户',3,NULL,NULL,NULL,4),
 (106,101,'sys:user:assign','分配角色',3,NULL,NULL,NULL,5),
 (107,100,'sys:role', '角色管理', 2,'/system/role','system/role/index', 'TeamOutlined',  2),
 ...
```

> 完整 SQL 建议独立为 `Minecraft/src/main/resources/static/sql/db_permission.sql`，与现有 `db_minecraft.sql` 分离，便于增量升级。

---

## 四、后端架构与实现

### 4.1 包结构（在现有结构上扩展）

```
com.minecraft
├── security/                       # 新增：权限核心包
│   ├── annotation/
│   │   ├── RequiresPermission.java     # 自定义权限注解（可选，辅助用）
│   │   └── DataScope.java              # 数据范围注解
│   ├── aspect/
│   │   ├── DataScopeAspect.java        # 数据范围注入切面
│   │   └── PermissionAuditAspect.java  # 权限审计切面
│   ├── core/
│   │   ├── LoginUser.java              # 扩展 UserDetails，携带 userId/perms/roles
│   │   ├── PermissionChecker.java      # @perm 表达式 Bean
│   │   ├── SecurityUtils.java          # 当前登录用户工具
│   │   └── PermissionContext.java      # ThreadLocal 上下文
│   ├── cache/
│   │   └── PermissionCacheManager.java # Redis 权限缓存
│   └── data/
│       └── DataScopeHelper.java        # 数据范围 SQL 拼接工具
├── entity/  SysRole / SysPermission / SysUserRole / SysRolePermission ...
├── mapper/  SysRoleMapper / SysPermissionMapper ...
├── service/ SysRoleService / SysPermissionService ...
└── controller/ SysRoleController / SysPermissionController ...
```

### 4.2 扩展 UserDetails

```java
package com.minecraft.security.core;

@Data
public class LoginUser implements UserDetails {

    private Long userId;
    private String username;
    private String password;
    private Integer status;
    private Long deptId;
    private Long tenantId;
    private boolean superAdmin;

    /** 角色编码集合 */
    private Set<String> roleCodes = new HashSet<>();
    /** 权限编码集合（已展开通配符） */
    private Set<String> permissions = new HashSet<>();
    /** 角色数据范围（取并集中最宽松的） */
    private Integer dataScope = 4;
    /** 自定义数据范围部门ID */
    private Set<Long> scopeDeptIds = new HashSet<>();

    @Override public Collection<? extends GrantedAuthority> getAuthorities() {
        return permissions.stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toSet());
    }
    @Override public boolean isAccountNonExpired()  { return true; }
    @Override public boolean isAccountNonLocked()   { return status != null && status == 1; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled()            { return status != null && status == 1; }
}
```

### 4.3 权限检查 Bean（供 SpEL 调用）

```java
package com.minecraft.security.core;

@Component("perm")
public class PermissionChecker {

    /** 是否拥有指定权限 */
    public boolean has(String code) {
        LoginUser user = SecurityUtils.getLoginUser();
        if (user == null || code == null || code.isBlank()) return false;
        if (user.isSuperAdmin()) return true;
        return match(user.getPermissions(), code);
    }

    /** 拥有任意一个即可 */
    public boolean hasAny(String... codes) {
        for (String c : codes) if (has(c)) return true;
        return false;
    }

    /** 拥有全部 */
    public boolean hasAll(String... codes) {
        for (String c : codes) if (!has(c)) return false;
        return true;
    }

    /** 角色判断 */
    public boolean hasRole(String roleCode) {
        LoginUser user = SecurityUtils.getLoginUser();
        return user != null && user.getRoleCodes().contains(roleCode);
    }

    /**
     * 通配符匹配：持有码的 * 可匹配请求码任意段
     * 持有 travel:*:*  请求 travel:hotel:edit  -> true
     */
    private boolean match(Set<String> owned, String required) {
        if (owned.contains(required)) return true;
        String[] req = required.split(":");
        for (String o : owned) {
            String[] own = o.split(":");
            if (own.length != req.length) continue;
            boolean ok = true;
            for (int i = 0; i < own.length; i++) {
                if (!"*".equals(own[i]) && !own[i].equals(req[i])) { ok = false; break; }
            }
            if (ok) return true;
        }
        return false;
    }
}
```

### 4.4 使用方式（推荐）

在 `SecurityConfig` 上开启方法级安全：

```java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)   // 关键：开启 @PreAuthorize
public class SecurityConfig { ... }
```

Controller 中按需标注：

```java
@RestController
@RequestMapping("/api/admin/attraction")
public class AttractionAdminController {

    @GetMapping("/list")
    @PreAuthorize("@perm.has('travel:attraction:list')")
    public Result<PageResult<AttractionVO>> list(AttractionQuery query) { ... }

    @PostMapping
    @PreAuthorize("@perm.has('travel:attraction:add')")
    public Result<Long> add(@RequestBody @Valid AttractionDTO dto) { ... }

    @DeleteMapping("/{id}")
    @PreAuthorize("@perm.has('travel:attraction:delete')")
    public Result<Void> delete(@PathVariable Long id) { ... }

    /** 多权限：导出需"导出"或"超管" */
    @GetMapping("/export")
    @PreAuthorize("@perm.has('travel:attraction:export')")
    public void export(HttpServletResponse resp) { ... }
}
```

> **不建议**自研 `@RequiresPermission` 注解 + AOP，因为 Spring Security 的 `@PreAuthorize` 已具备表达式能力（`and` / `or` / `#param` / `@bean.method()`），扩展性更强且无需维护切面。自定义注解仅在需要「收集所有接口权限自动建表」的场景下作为辅助使用（见 5.6 节）。

### 4.5 权限加载与缓存

```java
@Service
@RequiredArgsConstructor
public class PermissionService {

    private final SysPermissionMapper permissionMapper;
    private final SysRoleMapper roleMapper;
    private final PermissionCacheManager cacheManager;

    /** 加载某用户的完整权限信息（带缓存） */
    public LoginUser loadUserAuth(Long userId) {
        LoginUser cached = cacheManager.getUserAuth(userId);
        if (cached != null) return cached;

        LoginUser user = new LoginUser();
        user.setUserId(userId);
        user.setRoleCodes(new HashSet<>(roleMapper.selectRoleCodesByUserId(userId)));
        user.setPermissions(new HashSet<>(permissionMapper.selectPermCodesByUserId(userId)));
        user.setSuperAdmin(roleMapper.countSuperAdmin(userId) > 0);
        // 数据范围：取该用户所有角色中最宽松的一个
        user.setDataScope(roleMapper.selectMinDataScope(userId));
        user.setScopeDeptIds(new HashSet<>(roleMapper.selectScopeDeptIds(userId)));

        cacheManager.putUserAuth(userId, user);
        return user;
    }

    /** 角色变更后主动失效 */
    public void evictByRole(Long roleId) {
        cacheManager.evictByRole(roleId);
    }
    public void evictByUser(Long userId) {
        cacheManager.evictUser(userId);
    }
}
```

**缓存设计**：

| Key | Value | TTL |
|-----|-------|-----|
| `mc:perm:user:{userId}` | `LoginUser`（JSON） | 30 min |
| `mc:perm:roleUsers:{roleId}` | `Set<Long>` 该角色下用户 | 30 min |
| `mc:perm:version` | 全局版本号（权限表变更时 +1） | 永久 |

**失效策略**：

- 角色授权变更 → 删除该角色下所有用户的 `mc:perm:user:*`
- 用户分配角色变更 → 删除该用户的 `mc:perm:user:*`
- 权限表/菜单变更 → 全局版本号 +1，并在缓存 Key 中带版本前缀（`mc:perm:v{ver}:user:{id}`），实现整批失效

```java
@Component
@RequiredArgsConstructor
public class PermissionCacheManager {

    private static final String USER_KEY = "mc:perm:user:";
    private static final String ROLE_USERS_KEY = "mc:perm:roleUsers:";
    private static final String VERSION_KEY = "mc:perm:version";

    private final StringRedisTemplate redis;
    private final ObjectMapper om;

    public LoginUser getUserAuth(Long userId) {
        String json = redis.opsForValue().get(USER_KEY + userId);
        return json == null ? null : om.readValue(json, LoginUser.class);
    }

    public void putUserAuth(Long userId, LoginUser user) {
        redis.opsForValue().set(USER_KEY + userId, om.writeValueAsString(user), Duration.ofMinutes(30));
    }

    public void evictUser(Long userId) { redis.delete(USER_KEY + userId); }

    public void evictByRole(Long roleId) {
        Set<String> members = redis.opsForSet().members(ROLE_USERS_KEY + roleId);
        if (members != null && !members.isEmpty()) {
            redis.delete(members.stream().map(ROLE_USERS_KEY::concat).toList());
            redis.delete(ROLE_USERS_KEY + roleId);
        }
    }

    public long currentVersion() {
        String v = redis.opsForValue().get(VERSION_KEY);
        return v == null ? 0 : Long.parseLong(v);
    }
    public void bumpVersion() { redis.opsForValue().increment(VERSION_KEY); }
}
```

### 4.6 登录流程集成

在现有 JWT 认证基础上做**最小侵入**修改：

```
POST /api/auth/login
    ├─ 滑块验证码校验（TIANAI-CAPTCHA）
    ├─ 账号密码校验（BCrypt）
    ├─ 生成 JWT（仅存 userId + username + jti，不存权限，避免 Token 膨胀）
    ├─ permissionService.loadUserAuth(userId) → 写入 Redis 缓存
    └─ 返回 { token, userInfo, permissions[], roles[], menus[] }
```

**JWT Filter 修改点**（现有 `interceptor` / `filter` 中）：

```java
// 解析 Token 得到 userId
Long userId = jwtUtils.getUserId(token);
// 从缓存加载完整权限（缓存未命中则回源查库）
LoginUser loginUser = permissionService.loadUserAuth(userId);
if (loginUser == null || !loginUser.isEnabled()) {
    throw new AuthException("登录状态已失效");
}
UsernamePasswordAuthenticationToken auth =
        new UsernamePasswordAuthenticationToken(loginUser, null, loginUser.getAuthorities());
SecurityContextHolder.getContext().setAuthentication(auth);
```

> **注意**：JWT 中**不存放权限列表**。权限存服务端 Redis，可随时踢下线 / 动态调权，避免「Token 未过期但权限已收回」的安全漏洞。

### 4.7 数据权限实现

#### 4.7.1 注解定义

```java
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DataScope {
    /** 表中用于匹配的字段名，如 dept_id / create_by / user_id */
    String field() default "dept_id";
    /** 实体对应别名，多表 join 时使用，如 "o" -> o.dept_id */
    String tableAlias() default "";
}
```

#### 4.7.2 切面实现

```java
@Aspect
@Component
public class DataScopeAspect {

    @Before("@annotation(dataScope)")
    public void before(JoinPoint jp, DataScope dataScope) {
        LoginUser user = SecurityUtils.getLoginUser();
        if (user == null || user.isSuperAdmin()) return;

        String field = dataScope.tableAlias().isBlank()
                ? dataScope.field()
                : dataScope.tableAlias() + "." + dataScope.field();

        String condition = switch (user.getDataScope()) {
            case 1 -> null;                               // 全部数据
            case 2 -> String.format("%s = %d", field, user.getDeptId());
            case 3 -> String.format("%s IN (SELECT id FROM sys_dept WHERE id = %d OR FIND_IN_SET(%d, ancestors))",
                                    field, user.getDeptId(), user.getDeptId());
            case 4 -> String.format("create_by = %d", user.getUserId());
            case 5 -> user.getScopeDeptIds().isEmpty()
                        ? "1 = 0"
                        : String.format("%s IN (%s)", field,
                            user.getScopeDeptIds().stream()
                                .map(String::valueOf).collect(Collectors.joining(",")));
            default -> "1 = 0";
        };

        if (condition != null) PermissionContext.setDataScope(condition);
    }

    @AfterReturning("@annotation(dataScope)")
    public void after(DataScope dataScope) { PermissionContext.clear(); }
}
```

#### 4.7.3 Service 层使用

```java
@DataScope(field = "dept_id")
public Page<OrderVO> pageOrders(OrderQuery query) {
    QueryWrapper<Order> w = new QueryWrapper<>();
    // ... 业务条件
    DataScopeHelper.apply(w);   // 追加数据范围 SQL
    return orderMapper.selectPage(query.toPage(), w);
}
```

```java
public class DataScopeHelper {
    public static <T> void apply(QueryWrapper<T> wrapper) {
        String scope = PermissionContext.getDataScope();
        if (StringUtils.isNotBlank(scope)) wrapper.apply(scope);
    }
}
```

> **扩展位**：若未来需要更细的「列级权限」（如财务只看金额不看成本），可在 VO 序列化阶段使用 Jackson `@JsonView` + 自定义序列化器按权限裁剪字段，本方案已在 `sys_permission.type=5` 预留数据权限编码位置。

### 4.8 权限审计

```java
@Aspect
@Component
@RequiredArgsConstructor
public class PermissionAuditAspect {

    private final SysOperationLogService logService;

    /** 审计所有授权类接口：角色授权、用户分配角色、临时授权 */
    @Around("execution(* com.minecraft.controller..*.*(..)) "
          + "&& (@annotation(org.springframework.web.bind.annotation.PostMapping) "
          + "    || @annotation(org.springframework.web.bind.annotation.PutMapping) "
          + "    || @annotation(org.springframework.web.bind.annotation.DeleteMapping))")
    public Object around(ProceedingJoinPoint jp) throws Throwable {
        Object result = jp.proceed();
        // 仅记录与权限相关的路径
        String uri = ServletUtils.getRequestUri();
        if (uri.contains("/role") || uri.contains("/permission") || uri.contains("/user/role")) {
            logService.record(SecurityUtils.getUserId(), uri,
                    JsonUtils.toJson(jp.getArgs()), "SUCCESS");
        }
        return result;
    }
}
```

**越权访问记录**：在全局异常处理器中拦截 `AccessDeniedException`，写入 `system_log`（`type=SECURITY`），并触发告警（同一用户 5 分钟内越权 ≥ 5 次则临时封禁 10 分钟，可扩展）。

---

## 五、接口文档

### 5.1 认证类

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| POST | `/api/auth/login` | 登录，返回 token + 权限 + 菜单 | 公开 |
| POST | `/api/auth/logout` | 登出（清除缓存） | 登录态 |
| GET | `/api/auth/me` | 获取当前登录用户信息 + 权限码 | 登录态 |
| GET | `/api/auth/menus` | 获取当前用户可见菜单树 | 登录态 |
| PUT | `/api/auth/password` | 修改自身密码 | 登录态 |

**`GET /api/auth/me` 响应示例**：

```json
{
  "code": 200,
  "data": {
    "userInfo": { "userId": 1001, "username": "admin", "nickname": "管理员", "avatar": "..." },
    "roles": ["super_admin"],
    "permissions": ["*:*:*"],
    "dataScope": 1,
    "superAdmin": true
  }
}
```

### 5.2 角色管理

| 方法 | 路径 | 说明 | 权限码 |
|------|------|------|--------|
| GET | `/api/system/role/list` | 角色分页列表 | `sys:role:list` |
| GET | `/api/system/role/{id}` | 角色详情 | `sys:role:query` |
| POST | `/api/system/role` | 新增角色 | `sys:role:add` |
| PUT | `/api/system/role/{id}` | 修改角色 | `sys:role:edit` |
| DELETE | `/api/system/role/{id}` | 删除角色（内置角色禁止） | `sys:role:delete` |
| GET | `/api/system/role/{id}/permissions` | 查询角色已有权限ID集合 | `sys:role:query` |
| PUT | `/api/system/role/{id}/permissions` | 分配权限 | `sys:role:assign` |
| GET | `/api/system/role/{id}/users` | 角色下用户列表 | `sys:role:query` |

### 5.3 权限管理

| 方法 | 路径 | 说明 | 权限码 |
|------|------|------|--------|
| GET | `/api/system/permission/tree` | 权限树（含按钮） | `sys:permission:list` |
| GET | `/api/system/permission/menu-tree` | 仅菜单树（分配角色用） | `sys:permission:list` |
| POST | `/api/system/permission` | 新增权限 | `sys:permission:add` |
| PUT | `/api/system/permission/{id}` | 修改权限 | `sys:permission:edit` |
| DELETE | `/api/system/permission/{id}` | 删除权限（有子节点/被引用时禁止） | `sys:permission:delete` |
| POST | `/api/system/permission/scan` | 扫描 Controller 生成接口权限（开发工具） | `sys:permission:add` |

### 5.4 用户角色分配

| 方法 | 路径 | 说明 | 权限码 |
|------|------|------|--------|
| GET | `/api/system/user/{userId}/roles` | 查询用户角色ID集合 | `sys:user:query` |
| PUT | `/api/system/user/{userId}/roles` | 分配用户角色 | `sys:user:assign` |
| PUT | `/api/system/user/{userId}/data-scope` | 调整用户数据范围（扩展） | `sys:user:assign` |

### 5.5 临时授权（扩展）

| 方法 | 路径 | 说明 | 权限码 |
|------|------|------|--------|
| POST | `/api/system/grant` | 新增临时授权 | `sys:grant:add` |
| DELETE | `/api/system/grant/{id}` | 撤销临时授权 | `sys:grant:delete` |
| GET | `/api/system/grant/list` | 临时授权列表 | `sys:grant:list` |

### 5.6 接口权限自动扫描（开发辅助）

> 用于解决「新增 Controller 后忘记补权限数据」的问题。

```java
@PostMapping("/scan")
@PreAuthorize("@perm.has('sys:permission:add')")
public Result<ScanResult> scan() {
    // 通过 RequestMappingHandlerMapping 获取所有映射
    // 结合 @PreAuthorize("@perm.has('xxx')") 中的编码，与 sys_permission 表 diff
    // 输出：新增接口权限 / 已失效权限 / 未标注权限的接口
}
```

**输出示例**：

```json
{
  "missingInDb": ["travel:scenic:audit", "reco:engine:trigger"],
  "missingInCode": ["travel:old:list"],
  "unprotected": ["GET /api/debug/health"]
}
```

推荐在 CI 中加入该检查（`unprotected` 非空则构建失败），从工程上保证「默认拒绝」。

---

## 六、前端实现

### 6.1 用户端 / 旧管理端（minecraft-ui / minecraft-admin，Vue 3 + Element Plus）

#### 6.1.1 Pinia 权限 Store

```js
// src/store/modules/permission.js
import { defineStore } from 'pinia'
import { getMe, getMenus } from '@/api/auth'

export const usePermissionStore = defineStore('permission', {
  state: () => ({
    loaded: false,
    permissions: [],   // ['travel:attraction:list', ...]
    roles: [],         // ['travel_admin']
    menus: [],         // 菜单树
    superAdmin: false
  }),

  getters: {
    /** 单个权限判断 */
    hasPerm: (state) => (code) => {
      if (state.superAdmin || state.permissions.includes('*:*:*')) return true
      if (state.permissions.includes(code)) return true
      // 通配符匹配 travel:*:*
      const req = code.split(':')
      return state.permissions.some(p => {
        const own = p.split(':')
        if (own.length !== req.length) return false
        return own.every((seg, i) => seg === '*' || seg === req[i])
      })
    },
    /** 任意一个 */
    hasAnyPerm: (state) => (codes = []) => codes.some(c => state.hasPerm(c))
  },

  actions: {
    async load() {
      if (this.loaded) return
      const [{ data: me }, { data: menus }] = await Promise.all([getMe(), getMenus()])
      this.permissions = me.permissions || []
      this.roles = me.roles || []
      this.superAdmin = !!me.superAdmin
      this.menus = menus || []
      this.loaded = true
    },
    reset() {
      this.$reset()
    }
  }
})
```

#### 6.1.2 权限指令 `v-perm`

```js
// src/directives/permission.js
import { usePermissionStore } from '@/store/modules/permission'

export const perm = {
  mounted(el, binding) {
    const { value, modifiers } = binding   // v-perm="'travel:hotel:add'"
    if (!value) return
    const store = usePermissionStore()
    const codes = Array.isArray(value) ? value : [value]
    const ok = modifiers.any ? store.hasAnyPerm(codes) : codes.every(store.hasPerm)
    if (!ok) el.parentNode?.removeChild(el)
  }
}

// main.js
app.directive('perm', perm)
```

**使用**：

```vue
<el-button v-perm="'travel:hotel:add'" type="primary" @click="handleAdd">新增酒店</el-button>
<el-button v-perm.any="['travel:hotel:export','travel:hotel:import']">批量操作</el-button>
```

#### 6.1.3 路由守卫 + 动态路由

```js
router.beforeEach(async (to, from, next) => {
  const token = useUserStore().token
  if (!token) return to.path === '/login' ? next() : next('/login')

  const permStore = usePermissionStore()
  if (!permStore.loaded) {
    try {
      await permStore.load()
      // 依据 menus 动态注册路由（component 使用 import.meta.glob 懒加载）
      const routes = buildRoutesFromMenus(permStore.menus)
      routes.forEach(r => router.addRoute(r))
      next({ ...to, replace: true })
      return
    } catch (e) {
      permStore.reset()
      return next('/login')
    }
  }

  // 路由级权限（meta.permission）
  if (to.meta?.permission && !permStore.hasPerm(to.meta.permission)) {
    return next('/403')
  }
  next()
})
```

### 6.2 新版管理端（minecraft-admin-main，Vben Admin 5.7）

Vben Admin 已内置 `access` 能力，做**适配接入**即可：

```ts
// apps/web-antdv-next/src/router/guard.ts 中
import { useAccessStore } from '@vben/stores';

// 登录后写入权限码
const accessStore = useAccessStore();
accessStore.setAccessCodes(me.permissions);   // 复用 Vben 的 accessCodes

// 菜单由后端返回的 menus 转为 Vben 路由结构
accessStore.setAccessMenus(convertToVbenMenus(menus));
```

**页面内使用 Vben 提供的 `AccessControl` 组件**：

```vue
<script setup lang="ts">
import { AccessControl, useAccess } from '@vben/access';
const { hasAccessByCodes } = useAccess();
</script>

<template>
  <AccessControl type="code" :codes="['travel:attraction:add']">
    <Button type="primary">新增景点</Button>
  </AccessControl>
</template>
```

### 6.3 移动端（minecraft-App，uni-app）

```vue
<!-- 通过 store 判断，uni-app 不支持自定义指令 -->
<view v-if="permStore.hasPerm('app:community:post')" class="publish-btn">
  发布帖子
</view>
```

`permissionStore` 复用同一份 `permissions` 数组逻辑（建议抽成 `@minecraft/shared-perm` 公共包，供三端复用）。

---

## 七、安全机制

| 机制 | 实现要点 |
|------|---------|
| **默认拒绝** | 除白名单（登录/注册/验证码/公开查询）外，所有 `/api/admin/**` 接口强制鉴权 |
| **接口白名单** | 在 `SecurityConfig` 中显式配置 `permitAll` 路径，且每次新增需 Code Review |
| **越权防护** | 数据权限（4.7）+ 资源归属校验（编辑/删除前校验 `create_by == 当前用户` 或超级权限） |
| **权限缓存隔离** | Redis Key 带版本号，避免权限变更后的脏读 |
| **Token 安全** | JWT 不存权限；支持 `jti` 黑名单踢人下线 |
| **授权审计** | 所有 `POST/PUT/DELETE` 的授权类接口记录操作人与内容 |
| **内置角色保护** | `built_in=1` 的角色禁止删除、禁止修改 `code` |
| **超级管理员** | `super_admin=1` 的用户绕过权限校验，但**绕过动作也需落日志** |
| **密码安全** | 沿用 BCrypt；修改密码后强制失效该用户全部 Token（缓存版本 +1） |
| **横向越权** | 用户 ID 一律从 `SecurityContext` 取，**禁止从请求参数取**（防止改 ID 越权） |

**关键代码约束**：

```java
// ✅ 正确
Long userId = SecurityUtils.getUserId();

// ❌ 错误：可被篡改，造成横向越权
public Result<Void> update(@RequestParam Long userId, ...)
```

---

## 八、扩展能力设计（重点）

> 本章明确「未来 1–3 年可能的演进方向」及**当前已预留的扩展位**，保证届时为增量开发而非重构。

### 8.1 多租户（SaaS 化）

| 项 | 现状 | 扩展方式 |
|----|------|---------|
| 数据字段 | 所有权限表已含 `tenant_id DEFAULT 0` | 无需改表 |
| 拦截层 | 未启用 | 新增 `TenantInterceptor`，从 JWT 中取 `tenantId`，MyBatis-Plus 用 `TenantLineInnerInterceptor` 自动拼 `tenant_id = ?` |
| 缓存 Key | `mc:perm:user:{id}` | 改为 `mc:perm:{tenant}:user:{id}` |
| 角色隔离 | `uk_tenant_code` 已按租户唯一 | 直接可用 |

**改造工作量预估**：≤ 3 人日（仅加拦截器 + 缓存 Key 前缀）。

### 8.2 数据权限进阶

当前支持：全部 / 本部门 / 本部门及以下 / 仅本人 / 自定义部门。

预留扩展路径：

| 阶段 | 能力 | 实现要点 |
|------|------|---------|
| P1（当前） | 表字段级过滤 | `@DataScope` + `DataScopeHelper` |
| P2 | **规则可配置** | 新增 `sys_data_rule` 表，`rule = "create_by = ${userId} OR dept_id IN (...)"`，支持运营在管理端可视化配置 |
| P3 | **列级权限** | `@JsonView` + 自定义序列化器，按 `permission.type=5` 的码裁剪 VO 字段 |
| P4 | **行级 + 列级组合** | 引入轻量规则引擎（如 QLExpress），表达式存 `sys_data_rule.expr` |

**扩展位已就绪**：`sys_permission.type = 5`（数据权限类型）、`sys_role.data_scope = 5`（自定义）。

### 8.3 ABAC 混合模型

当业务出现「仅允许编辑自己创建的、且状态为草稿的景点」这类需求时：

- 已有 `sys_permission_condition` 表（4.4 节），字段 `expr` 存条件表达式；
- 在 `PermissionChecker.has()` 中增加**条件评估**：

```java
public boolean has(String code, Object... args) {
    if (!has(code)) return false;
    List<String> exprs = conditionService.getExprs(code);
    if (exprs.isEmpty()) return true;
    // 使用 SpEL 评估，args 通过 EvaluationContext 注入
    return exprs.stream().allMatch(expr -> spelEval(expr, args));
}
```

- Controller 中使用：`@PreAuthorize("@perm.has('travel:attraction:edit', #id)")`

**扩展位已就绪**：条件表 + `has(String, Object...)` 重载。

### 8.4 角色继承与权限组

- **角色继承**：`sys_role.parent_id` 已预留。`loadUserAuth` 时递归收集父角色权限（**需做环检测**）。

```java
private Set<Long> collectRoleWithParents(Long roleId, Set<Long> visited) {
    if (!visited.add(roleId)) throw new BizException("角色继承存在环");
    SysRole role = roleMapper.selectById(roleId);
    Set<Long> all = new HashSet<>(Set.of(roleId));
    if (role.getParentId() != null && role.getParentId() > 0) {
        all.addAll(collectRoleWithParents(role.getParentId(), visited));
    }
    return all;
}
```

- **权限组**：若未来权限数量膨胀（> 500），可新增 `sys_permission_group`，将权限按业务线分组，角色授权时整组勾选。`sys_permission` 增加 `group_id` 字段即可。

### 8.5 临时授权与审批

- 表已预留：`sys_permission_grant`（含 `effect_type`、`start_time`、`end_time`）；
- 加载权限时叠加：

```java
// 在 loadUserAuth 中
Set<String> tempGranted = grantMapper.selectActiveGrantCodes(userId, LocalDateTime.now());
Set<String> tempDenied  = grantMapper.selectActiveDenyCodes(userId, LocalDateTime.now());
user.getPermissions().addAll(tempGranted);
user.getPermissions().removeAll(tempDenied);   // 拒绝优先
```

- 审批流可对接现有工作流（或轻量实现 `sys_grant_apply`），审批通过后写 `sys_permission_grant`。

**扩展位已就绪**：临时授权表 + 加载逻辑的合并点。

### 8.6 对外开放 API 的权限（OAuth2 / 应用授权）

若未来开放 OpenAPI 给第三方（如旅行社对接）：

| 项 | 设计 |
|----|------|
| 应用表 | `sys_app`（app_id / app_secret / 状态） |
| 授权表 | `sys_app_scope`（app_id / permission_code） |
| 鉴权 | 新增 `ApiKeyAuthFilter`，认证后构造 `LoginUser`（permissions 来自 app_scope） |
| 限流 | 复用 Redis + 自定义注解 `@RateLimit` |

**扩展位**：`PermissionChecker` 与业务代码解耦，替换认证方式即可复用全部权限判断逻辑。

### 8.7 权限与业务模块的隔离

项目业务模块众多（景点/酒店/美食/商城/跟团游/推荐/IM）。建议按「**业务域前缀**」组织权限，方便后续模块独立拆分：

```
travel:*       旅行核心（景点/酒店/美食/票务）
mall:*         特产商城
group:*        跟团游
community:*    社区
im:*           即时通讯
reco:*         推荐系统
content:*      内容配置（公告/轮播/官网）
ops:*          运维（缓存/监控/在线用户）
sys:*          系统（用户/角色/权限/日志）
```

若某业务模块独立成微服务，其权限码前缀天然形成边界，鉴权可下沉到网关。

### 8.8 前端可扩展点

| 扩展需求 | 现方案支撑方式 |
|---------|--------------|
| 多端权限差异（Web/App/小程序） | 权限树中增加 `platform` 字段（预留），下发时按端过滤 |
| 动态菜单/路由 | 后端返回 `menus`，前端 `addRoute` 动态注册 |
| 按钮级细粒度 | `v-perm` 指令 + `hasPerm` 函数 |
| 权限变更实时生效 | WebSocket 推送 `permission-changed` 事件，前端 `permStore.reset()` 并重新 `load()`（复用现有 WebSocket 通道） |
| 多语言权限名称 | `sys_permission` 增加 `name_i18n` JSON 字段（预留），前端按 `locale` 取值 |

### 8.9 扩展能力清单速查

| 能力 | 是否已预留扩展位 | 预留位置 |
|------|----------------|---------|
| 多租户 | ✅ | 所有表 `tenant_id`、`uk_tenant_code` |
| 角色继承 | ✅ | `sys_role.parent_id` |
| 临时授权 | ✅ | `sys_permission_grant` 表 |
| ABAC 条件权限 | ✅ | `sys_permission_condition` 表 + `has(code, args)` |
| 数据权限自定义规则 | ✅ | `data_scope=5` + `sys_role_dept` |
| 列级权限 | ⚠️ 部分 | `permission.type=5` 保留类型 |
| 权限组 | ⚠️ 部分 | `type=1` 目录可作为天然分组 |
| 对外开放 API | ⚠️ 需新增 | 认证层可插拔 |
| 审批流授权 | ⚠️ 需新增 | 表结构支持 |
| 权限变更实时推送 | ✅ | 现有 WebSocket 通道 |

---

## 九、迁移与上线方案

### 9.1 迁移步骤（不停机）

| 步骤 | 操作 | 风险 |
|------|------|------|
| 1 | 执行 `db_permission.sql`（新建 6 张表） | 无（纯新增） |
| 2 | 执行 `user` 表 ALTER（3 个字段 + 2 个索引） | 低（大表需评估锁表时间，建议在低峰期） |
| 3 | 初始化内置角色、权限数据、`super_admin` 用户 | 无 |
| 4 | 后端发布：新增权限相关接口 + `@PreAuthorize` 标注 | 需回归测试存量接口 |
| 5 | 前端发布：接入 `permStore` + `v-perm` | 存量页面逐步替换 |
| 6 | 观察 1 周：审计日志中的越权拒绝率 | 若异常偏高说明权限配置遗漏 |

### 9.2 灰度策略

- 阶段一：仅启用**菜单权限**（不影响存量按钮）
- 阶段二：开启**按钮权限**（前端指令生效）
- 阶段三：开启**接口权限**（`@PreAuthorize` 全量生效）—— 上线前务必用接口权限扫描（5.6）核对覆盖率

### 9.3 回滚方案

- 后端：`@PreAuthorize` 可通过配置 `perm.enabled=false` 全局旁路（在 `PermissionChecker` 首行判断），无需回滚代码
- 数据库：新增表与字段不影响旧逻辑，可保留不删

```java
@Component("perm")
public class PermissionChecker {
    @Value("${perm.enabled:true}")
    private boolean enabled;

    public boolean has(String code) {
        if (!enabled) return true;   // 全局开关，应急旁路
        ...
    }
}
```

---

## 十、测试用例清单

### 10.1 功能测试

| 编号 | 用例 | 预期 |
|------|------|------|
| TC-01 | 超管访问任意接口 | 全部放行 |
| TC-02 | 普通用户访问 `sys:user:list` | 403 + 审计日志 |
| TC-03 | 拥有 `travel:*:*` 的用户访问 `travel:hotel:add` | 放行 |
| TC-04 | 角色授权后，用户下次请求立即生效 | 缓存已失效，权限生效 |
| TC-05 | 用户被移除角色后，Token 未过期 | 立即失去权限（缓存已失效） |
| TC-06 | 删除被角色引用的权限 | 返回业务错误，禁止删除 |
| TC-07 | 删除内置角色 | 返回业务错误，禁止删除 |
| TC-08 | 角色继承成环 | 保存时校验失败 |

### 10.2 数据权限测试

| 编号 | 用例 | 预期 |
|------|------|------|
| TC-10 | `data_scope=4` 的用户查询订单列表 | 仅返回自己创建的 |
| TC-11 | `data_scope=2` 的用户查询景点 | 仅返回本部门数据 |
| TC-12 | `data_scope=5` 且未配置部门 | 返回空列表 |
| TC-13 | 超管查询 | 全部数据 |

### 10.3 安全测试

| 编号 | 用例 | 预期 |
|------|------|------|
| TC-20 | 篡改请求参数 userId 越权编辑 | 拒绝（userId 从上下文取） |
| TC-21 | 无 Token 访问受保护接口 | 401 |
| TC-22 | 过期 Token | 401 |
| TC-23 | 同一账号 5 分钟内越权 5 次 | 临时封禁（扩展能力） |
| TC-24 | 权限缓存命中 | 无 SQL 查询日志 |

### 10.4 性能测试

| 指标 | 目标 |
|------|------|
| 鉴权耗时（缓存命中） | < 1 ms |
| 鉴权耗时（缓存未命中，回源） | < 20 ms |
| 权限加载 QPS（8C16G + Redis 单机） | ≥ 5000 |
| 权限变更后生效延迟 | < 1 s |

---

## 十一、上线检查清单

- [ ] `db_permission.sql` 已在生产库执行并校验数据
- [ ] `user` 表 ALTER 完成，存量用户 `dept_id` 已初始化
- [ ] 至少 1 个 `super_admin` 用户存在且可正常登录
- [ ] 所有 `/api/admin/**` 接口已完成 `@PreAuthorize` 标注（通过扫描接口确认 `unprotected` 为空）
- [ ] 白名单路径经安全评审（登录/注册/验证码/公开查询）
- [ ] Redis 权限缓存 Key 前缀与 TTL 已确认
- [ ] `perm.enabled` 应急开关已配置
- [ ] 越权审计日志与告警已接入
- [ ] 前端 `v-perm` 指令已接入至少 1 个页面并验证
- [ ] Vben Admin `accessCodes` 已接入并验证菜单渲染
- [ ] 三端（Web/管理端/App）权限码一致性已联调
- [ ] 压测报告达标（见 10.4）
- [ ] 回滚演练完成

---

## 附录 A：核心接口签名速查

```java
// 当前用户
Long   SecurityUtils.getUserId();
String SecurityUtils.getUsername();
LoginUser SecurityUtils.getLoginUser();
boolean SecurityUtils.isSuperAdmin();

// 权限判断
@PreAuthorize("@perm.has('sys:user:add')")
@PreAuthorize("@perm.hasAny('a:b:c','d:e:f')")
@PreAuthorize("@perm.hasAll('a:b:c','d:e:f')")
@PreAuthorize("@perm.hasRole('super_admin')")

// 数据权限
@DataScope(field = "dept_id")
@DataScope(field = "dept_id", tableAlias = "o")

// 缓存失效
permissionCacheManager.evictUser(userId);
permissionCacheManager.evictByRole(roleId);
permissionCacheManager.bumpVersion();
```

## 附录 B：与现有模块的对应关系

| 现有模块 | 权限前缀 | 说明 |
|---------|---------|------|
| 公告 / 轮播 / 官网内容（`officialwebsite_*`） | `content:*` | 内容运营角色 |
| 景点 / 酒店 / 美食 / 商品 / 票务 / 城市 | `travel:*` | 旅行运营角色 |
| 跟团游（`group_travel` 系列） | `group:*` | 跟团游运营 |
| 社区帖子 / 评论 / 点赞 / 收藏 | `community:*` | 社区运营 + 内容审核 |
| 即时通讯（`chat_*` / `friend`） | `im:*` | 客服角色 |
| 推荐系统（`recommendation_*`） | `reco:*` | 仅算法/超管 |
| 在线用户 / 缓存 / 服务监控 / 日志 | `ops:*` | 运维角色 |
| 用户 / 角色 / 权限 / 登录日志 | `sys:*` | 超管 |
| 移动端专属（发帖、发布游记等） | `app:*` | C 端用户角色 |

---

**文档结束**

> 本文档在保持与现有 JWT + Spring Security 体系兼容的前提下，给出了完整的 RBAC 落地方案，并通过「表字段预留 + 接口可插拔 + 前端指令化」三种手段，为多租户、数据权限进阶、ABAC、临时授权、对外开放 API 等未来演进方向预留了明确的扩展位。建议后续按第九章的灰度节奏分阶段上线，配合第十章测试用例验收。