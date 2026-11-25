# Minecraft 旅游系统 · 手机 APP 扫码登录 Web 端 技术路线

> 版本：v1.0
> 关联文档：《用户权限管理开发文档》v1.0
> 适用范围：minecraft-ui（用户端 Web）、minecraft-admin-main（管理端）、minecraft-App（uni-app）、Minecraft（后端）
> 设计目标：在**不改造现有 JWT / 权限体系**的前提下，新增「APP 已登录状态下，扫码授权 Web 端登录」的能力，与账号密码登录、滑块验证、现有二维码登录形成互补。

---

## 一、需求与场景

### 1.1 与现有能力的关系

| 能力 | 当前状态 | 本方案定位 |
|------|---------|-----------|
| 账号密码登录 | 已实现 | 保持 |
| 手机验证码登录 | 已实现 | 保持 |
| **二维码登录（现状）** | md 描述为「账号/手机/二维码/滑块验证码」——即 Web 生成二维码，**由另一台设备扫码或扫码后填账号**，本质仍是"凭据登录" | 保留，不动 |
| **APP 扫码登录 Web（本方案）** | 未实现 | **新增**：APP 作为**已登录授权端**，扫码后手动确认，Web 自动登录 |

> 核心区别：现有"二维码登录"是**二维码承载凭据**；本方案是**二维码承载 ticket**，APP 用自身登录态**授权**签发 Web 新 token，**不传递 APP 的 token**，安全性更高。

### 1.2 用户旅程

```
PC 浏览器（Web）                 手机（minecraft-App）
      │                                   │
      │ 1. 打开登录页，默认展示"扫码登录"      │
      │ 2. 二维码 2 分钟有效                 │
      │                                   │ 3. 用户已登录 APP
      │                                   │ 4. 打开"扫一扫"
      │                                   │ 5. 扫 PC 上二维码
      │◀── 6. Web 提示"已扫描" ──────────────┤
      │                                   │ 7. APP 弹窗："正在登录 PC Chrome
      │                                   │    / 杭州 / 192.168.x.x，确认？"
      │                                   │ 8. 用户点"确认登录"
      │◀── 9. Web 自动跳转首页 ──────────────┤
      │                                   │ 10. APP 提示"登录成功"
```

### 1.3 非功能需求

| 项 | 目标 |
|----|------|
| 二维码有效期 | 120 秒（可配置） |
| 扫码到 Web 登录 | ≤ 3 秒（WebSocket 推送，端到端） |
| 并发 | 单机 ≥ 1000 ticket/s 生成、≥ 500 次扫码确认/s |
| 可用性 | WebSocket 不可用时自动降级为轮询 |
| 安全 | ticket 一次性、防重放、防钓鱼、登录可审计 |

---

## 二、现状复用分析

> 本方案**最大化复用**项目已有能力，不引入新中间件。

| 现有能力 | 复用方式 |
|---------|---------|
| **Redis（Lettuce）** | 存储 ticket 状态与 WebSocket 订阅关系 |
| **WebSocket（IM/信令通道）** | 新增 `qr_login` 消息类型，推送 ticket 状态变更 |
| **JWT（jjwt 0.12.5）** | 扫码确认后，走现有 Token 签发逻辑生成 Web 端 token |
| **LoginUser / 权限缓存** | 沿用《权限管理文档》的 `loadUserAuth(userId)`，扫码登录后 Web 端权限加载零改动 |
| **login_log 表** | 新增登录类型 `LOGIN_TYPE_QR`，记录扫码登录 |
| **TIANAI-CAPTCHA** | 与扫码登录无冲突；可保留给"扫码失败兜底切换到密码登录" |
| **BCrypt** | 无需改动 |
| **Druid SQL 监控** | 新增接口全部纳入监控 |

> **无需新增数据库表**。所有状态存 Redis，仅在需要审计时复用 `login_log`；如未来需查询历史，可异步落 `qr_login_log`（扩展位预留）。

---

## 三、核心设计

### 3.1 状态机

```
                    ┌─────────────┐
     generate ────▶ │  WAITING    │  等待扫码
                    └──────┬──────┘
                           │ APP scan
                           ▼
                    ┌─────────────┐
                    │  SCANNED    │  已扫描，等待 APP 确认
                    └──────┬──────┘
              APP confirm  │  或 APP cancel / 超时
                           ▼              │
                    ┌─────────────┐       │
                    │  CONFIRMED  │       │
                    └──────┬──────┘       │
                           │ Web consume  │
                           ▼              │
                    ┌─────────────┐       │
                    │  CONSUMED   │       │
                    └─────────────┘       │
                                          ▼
                    ┌─────────────┐  ┌─────────────┐
                    │  EXPIRED    │◀─│  CANCELED   │
                    └─────────────┘  └─────────────┘
```

**约束**：

- 状态**只能**单向前进（`WAITING → SCANNED → CONFIRMED → CONSUMED`），`CANCELED` / `EXPIRED` 为终态。
- `CONSUMED` 后 ticket 立即删除，**不可重复消费**。
- `CONFIRMED` 状态有独立短 TTL（30 秒），Web 未在 30 秒内 consume 则重置为 `WAITING`（防止 APP 确认后 Web 已关闭，ticket 被劫持）。

### 3.2 时序图

```
 Web(浏览器)                  Server                          APP
     │                          │                              │
     │ ① POST /api/auth/qr/generate                            │
     │─────────────────────────▶│                              │
     │                          │ 生成 ticket + TTL=120s        │
     │                          │ 写 Redis: qr:{ticket}=WAITING │
     │◀─ {ticket, qrCode, ttl} ─│                              │
     │                          │                              │
     │ ② WebSocket: /ws/qr/{ticket}                            │
     │═════════════════════════▶│ 订阅 ticket                   │
     │                          │                              │
     │                          │ ◀── ③ POST /qr/scan ──────────│
     │                          │      (Authorization: APP JWT) │
     │                          │ 校验 APP 登录态；              │
     │                          │ 若 ticket=WAITING 则置 SCANNED │
     │                          │ 记录 scannerUserId / IP / UA   │
     │◀═ WS: {status:SCANNED} ══│                              │
     │                          │ ══▶ 返回 Web 端设备信息 ────────│
     │                          │                              │
     │                          │ ◀── ④ POST /qr/confirm ───────│
     │                          │      (Authorization: APP JWT) │
     │                          │ 校验 scannerUserId == APP user │
     │                          │ 走现有 token 签发逻辑签发 Web JWT│
     │                          │ 写 Redis: qr:{ticket}=CONFIRMED│
     │                          │          {token, userInfo}    │
     │◀═ WS: {status:CONFIRMED} │                              │
     │                          │                              │
     │ ⑤ POST /qr/consume       │                              │
     │─────────────────────────▶│ 原子取 token，删除 ticket      │
     │◀──── {token, userInfo} ──│                              │
     │                          │                              │
     │ ⑥ 写 login_log(type=QR)  │ 异步                        │
     │                          │                              │
```

### 3.3 为什么"确认"与"消费"分离

- **确认（APP 侧）**：APP 是**授权方**，只表态"我允许这次登录"。
- **消费（Web 侧）**：Web 是**受益方**，拿到最终凭据。
- 分离后，Web 端的 token **只落地在 Web 端浏览器**，不经过 APP，也不在 WS 推送中直接给出（WS 只推送状态，token 由 Web 主动拉取），**避免长连接泄露敏感凭据**。

---

## 四、接口设计

### 4.1 接口清单

| 方法 | 路径 | 调用方 | 说明 | 鉴权 |
|------|------|-------|------|------|
| POST | `/api/auth/qr/generate` | Web | 生成登录二维码 | 公开（限流） |
| GET | `/api/auth/qr/status/{ticket}` | Web | 轮询查询状态（WS 兜底） | 公开 |
| POST | `/api/auth/qr/consume` | Web | 消费 ticket 换取 JWT | 公开（ticket 即凭据） |
| WS | `/ws/qr/{ticket}` | Web | 订阅状态推送 | 公开（ticket 即凭据） |
| GET | `/api/auth/qr/detail/{ticket}` | APP | 扫码后获取 Web 端设备信息 | **APP 登录态** |
| POST | `/api/auth/qr/scan` | APP | 标记已扫描（展示确认弹窗前置） | **APP 登录态** |
| POST | `/api/auth/qr/confirm` | APP | 确认登录 | **APP 登录态** |
| POST | `/api/auth/qr/cancel` | APP | 取消登录 | **APP 登录态** |

> **限流**：`/qr/generate`、`/qr/scan` 用 Redis 计数器限流（IP + userId 维度），防止 ticket 洪水与暴力扫码。项目已有 Redis 基础设施，直接复用。

### 4.2 关键接口定义

#### ① `POST /api/auth/qr/generate`

**请求**：空体，Header 可带 `X-Client-Info`（用于展示）

**响应**：

```json
{
  "code": 200,
  "data": {
    "ticket": "a1b2c3d4e5f67890abcdef1234567890",
    "qrContent": "minecraft://qrLogin?t=a1b2c3d4e5f67890abcdef1234567890",
    "expireAt": 1734500000000,
    "ttl": 120
  }
}
```

**实现要点**：

- `ticket` = `UUID.randomUUID().toString().replace("-","") + randomSuffix(16)`，48 字符，防猜测。
- `qrContent` 用 `minecraft://qrLogin?t={ticket}` 协议头；若二维码需被通用扫码器识别，可同时下发 H5 短链（扩展位）。
- 二维码由**前端**用 `qrcode` 库渲染，后端只下发 ticket，减少后端压力。

#### ② `POST /api/auth/qr/scan`（APP）

**Header**：`Authorization: Bearer {APP_TOKEN}`

**请求体**：

```json
{ "ticket": "a1b2...", "appVersion": "1.2.0", "deviceModel": "iPhone 15" }
```

**响应**：

```json
{
  "code": 200,
  "data": {
    "webClient": {
      "ip": "203.0.113.12",
      "region": "浙江省杭州市",
      "userAgent": "Mozilla/5.0 (Windows NT 10.0) Chrome/120",
      "browser": "Chrome 120",
      "os": "Windows 10",
      "genAt": 1734499900000
    },
    "expireAt": 1734500000000
  }
}
```

**实现要点**：

- 校验 APP token 有效且 `loadUserAuth(userId)` 存在（沿用权限管理文档缓存）。
- **幂等**：若 ticket 已是 `SCANNED` 且 `scannerUserId` 相同，直接返回 detail（避免 APP 抖动重试）。
- 若 ticket 已是 `CONFIRMED` / `CONSUMED` / 过期，返回明确错误码（前端提示"二维码已失效"）。

#### ③ `POST /api/auth/qr/confirm`（APP）

**请求体**：`{ "ticket": "a1b2..." }`

**响应**：

```json
{ "code": 200, "data": { "status": "CONFIRMED" } }
```

**实现要点**：

- **必须**校验 `scannerUserId == 当前 APP userId`（防止 B 扫了 A 的 ticket 又用 A 的 token 确认）。
- 走**现有** Web JWT 签发逻辑（`jwtUtils.createToken(loginUser)`）生成 token，`login_log` 记录 `type=QR`、`login_source=APP_SCAN`。
- **不**把 token 通过 WS 推给 Web，只写 Redis 供 Web 主动 consume。
- 敏感操作：若用户从未开启过扫码登录，可选**二次确认**（扩展）。

#### ④ `POST /api/auth/qr/consume`（Web）

**请求体**：`{ "ticket": "a1b2..." }`

**响应**：

```json
{
  "code": 200,
  "data": {
    "token": "eyJhbGciOi...",
    "userInfo": { "userId": 1001, "username": "tom", "nickname": "汤姆", "avatar": "..." },
    "roles": ["normal_user"],
    "permissions": ["app:community:post", "travel:attraction:list"],
    "menus": [ ... ]
  }
}
```

**实现要点**：

- **原子性**：Redis `GETDEL qr:{ticket}`（或 Lua 脚本 `GET + DEL`）取出 ticket 数据，保证一次性消费。
- 消费成功后**立即**删除 `scannerUserId` 等敏感字段，只保留必要的审计信息。
- 返回的 `permissions/menus` 直接来自 `loadUserAuth`，与账号密码登录完全一致。

### 4.3 WebSocket 协议

**连接**：`ws://host:8080/ws/qr/{ticket}`（复用现有 `/ws` 前缀）

**服务端推送**（均为 JSON）：

```json
{ "type": "qr_status", "ticket": "a1b2...", "status": "SCANNED", "ts": 1734499910000 }
{ "type": "qr_status", "ticket": "a1b2...", "status": "CONFIRMED", "ts": 1734499920000 }
{ "type": "qr_status", "ticket": "a1b2...", "status": "EXPIRED", "ts": 1734500020000 }
{ "type": "qr_status", "ticket": "a1b2...", "status": "CANCELED", "ts": 1734499930000 }
{ "type": "qr_status", "ticket": "a1b2...", "status": "CONSUMED", "ts": 1734499940000 }
```

**注意**：**WS 消息中绝不包含 token / userId**，只含状态，防止中间人窃取长连接即可拿凭据。

---

## 五、存储设计（Redis）

> 所有状态存 Redis，不需要新表。Key 前缀与权限缓存保持一致。

| Key | 类型 | Value | TTL | 说明 |
|-----|------|-------|-----|------|
| `mc:qr:login:{ticket}` | String (JSON) | 见下 | 120s（可配） | 主状态 |
| `mc:qr:login:ws:{ticket}` | Set | WebSocket sessionId 集合 | 120s | 用于定向推送 |
| `mc:qr:login:user:{userId}:{ticket}` | String | ticket | 30s | 反向索引，防同一用户扫多张导致状态错乱（扩展） |

**`mc:qr:login:{ticket}` 的 JSON 结构**：

```json
{
  "status": "WAITING",
  "ticket": "a1b2...",
  "genIp": "203.0.113.12",
  "genRegion": "浙江省杭州市",
  "genUserAgent": "Mozilla/5.0 ...",
  "genAt": 1734499900000,
  "expireAt": 1734500020000,
  "scannerUserId": null,
  "scannerIp": null,
  "scannedAt": null,
  "webToken": null,
  "webUserInfo": null,
  "confirmedAt": null
}
```

**状态变更用 Lua 保证原子性**（防并发扫描/确认）：

```lua
-- KEYS[1] = mc:qr:login:{ticket}
-- ARGV[1] = expectedStatus, ARGV[2] = newStatusJson
local cur = redis.call('GET', KEYS[1])
if not cur then return 0 end
local ok, obj = pcall(cjson.decode, cur)
if not ok or obj['status'] ~= ARGV[1] then return -1 end
redis.call('SET', KEYS[1], ARGV[2], 'KEEPTTL')
return 1
```

**`CONSUMED` 后**：直接 `DEL` Key，且服务端再写一条 `mc:qr:used:{ticket}` = "1"（TTL 60s）用于防重放（ticket 被截获后短暂重试会被拒绝，而不是误判为无效 ticket 而误导用户）。

---

## 六、安全设计

| 威胁 | 对策 |
|------|------|
| **ticket 猜测** | 48 位 UUID+随机串；限流：单 IP 每分钟 10 次 generate；命中不存在 ticket 的查询同样计入限流 |
| **ticket 重放** | 一次性消费 + `mc:qr:used:{ticket}` 短时黑名单 |
| **中间人** | 全链路 HTTPS/WSS；ticket 不含任何用户信息 |
| **扫码钓鱼** | APP 端展示 Web 端 IP / 归属地 / 浏览器 / OS，用户手动确认；**首次登录**或**异地登录**强制二次确认 |
| **B 扫 A 的 ticket** | `confirm` 强校验 `scannerUserId == APP userId`；`consume` 校验 `webToken` 已生成 |
| **APP 端被占（他人手机）** | APP 端扫码本身需要 APP 登录态，属于 APP 侧安全；可选：高风险操作要求重新验证 APP 密码/生物识别（扩展） |
| **WS 泄露 token** | WS 只推状态；token 由 Web 主动 `consume` |
| **XSS 窃取 token** | Web 端 token 存储策略沿用现有实现（建议 HttpOnly Cookie 或 localStorage + CSP，属于既有决策） |
| **登录审计** | `login_log` 记录 `type=QR`、`genIp`、`scannerIp`、`scannerUserId`、`userAgent`、`region` |
| **异常封禁** | 同一 IP 5 分钟内 20 次无效 ticket 查询 → 封 10 分钟（复用现有 Redis 限流组件） |

---

## 七、各端实现要点

### 7.1 后端（Minecraft）

**新增包结构**（在现有基础上）：

```
com.minecraft
├── controller/
│   └── auth/ QrLoginController.java
├── service/
│   └── qr/QrLoginService.java
│   └── qr/QrWsNotifier.java
├── entity/vo/
│   └── QrGenerateVO / QrStatusVO / QrDetailVO
├── enums/
│   └── QrLoginStatus.java  (WAITING/SCANNED/CONFIRMED/CONSUMED/EXPIRED/CANCELED)
└── config/
    └── QrLoginProperties.java  (@ConfigurationProperties)
```

**`QrLoginService` 核心方法骨架**：

```java
@Service
@RequiredArgsConstructor
public class QrLoginService {

    private static final String KEY = "mc:qr:login:";
    private static final String USED_KEY = "mc:qr:used:";
    private static final long TTL_SEC = 120;

    private final StringRedisTemplate redis;
    private final JwtUtils jwtUtils;
    private final PermissionService permissionService;
    private final LoginLogService loginLogService;
    private final QrWsNotifier wsNotifier;

    /** 生成 ticket */
    public QrGenerateVO generate(QrGenContext ctx) {
        String ticket = randomTicket();
        QrTicket data = QrTicket.waiting(ticket, ctx, TTL_SEC);
        redis.opsForValue().set(KEY + ticket, JsonUtils.toJson(data), Duration.ofSeconds(TTL_SEC));
        return QrGenerateVO.of(ticket, data.getExpireAt(), TTL_SEC);
    }

    /** APP 扫码：WAITING → SCANNED，幂等 */
    public QrDetailVO scan(String ticket, Long appUserId, QrScanContext ctx) {
        QrTicket t = requireTicket(ticket);
        if (t.status().isFinal()) throw new BizException("二维码已失效");

        if (t.status() == QrLoginStatus.WAITING) {
            t = t.toScanned(appUserId, ctx);
            casUpdate(ticket, QrLoginStatus.WAITING, t);
            wsNotifier.push(ticket, QrLoginStatus.SCANNED);
        } else if (t.status() == QrLoginStatus.SCANNED) {
            if (!Objects.equals(t.scannerUserId(), appUserId)) throw new BizException("二维码已被占用");
            // 幂等：直接返回 detail
        } else {
            throw new BizException("二维码状态异常");
        }
        return QrDetailVO.from(t);
    }

    /** APP 确认：SCANNED → CONFIRMED，生成 Web token 落库（不推送） */
    public void confirm(String ticket, Long appUserId) {
        QrTicket t = requireTicket(ticket);
        if (t.status() != QrLoginStatus.SCANNED || !Objects.equals(t.scannerUserId(), appUserId)) {
            throw new BizException("无权确认或状态异常");
        }
        LoginUser loginUser = permissionService.loadUserAuth(appUserId);
        String webToken = jwtUtils.createToken(loginUser);
        QrTicket confirmed = t.toConfirmed(webToken, UserInfoVO.from(loginUser));
        casUpdate(ticket, QrLoginStatus.SCANNED, confirmed);
        // CONFIRMED 状态单独短 TTL，防止 Web 迟迟不来消费
        redis.expire(KEY + ticket, Duration.ofSeconds(30));
        wsNotifier.push(ticket, QrLoginStatus.CONFIRMED);

        loginLogService.asyncRecord(appUserId, LoginType.QR, t.genIp(), "APP_SCAN_CONFIRM");
    }

    /** Web 消费：CONFIRMED → CONSUMED，原子取走 */
    public QrConsumeVO consume(String ticket) {
        String json = redis.opsForValue().get(KEY + ticket);
        if (json == null) {
            if (Boolean.TRUE.equals(redis.hasKey(USED_KEY + ticket))) throw new BizException("二维码已使用");
            throw new BizException("二维码已失效");
        }
        QrTicket t = JsonUtils.parse(json, QrTicket.class);
        if (t.status() != QrLoginStatus.CONFIRMED) throw new BizException("尚未确认登录");

        // 原子 DELETE
        Boolean ok = redis.delete(KEY + ticket);
        if (!Boolean.TRUE.equals(ok)) throw new BizException("二维码已被消费");
        redis.opsForValue().set(USED_KEY + ticket, "1", Duration.ofSeconds(60));

        wsNotifier.push(ticket, QrLoginStatus.CONSUMED);
        return QrConsumeVO.of(t.webToken(), t.webUserInfo(),
                              permissionService.loadUserAuth(t.scannerUserId()));
    }
}
```

**`QrWsNotifier`**：直接复用项目现有 WebSocket 实现（`WebSocketHandler` / `@ServerEndpoint` 均可），新增 `qr_login` 频道，按 `ticket` 找到 sessionId 定向推送。若项目 WebSocket 是 STOMP，则定义 `/topic/qr/{ticket}`；若是原生 `@ServerEndpoint`，则用 `ConcurrentHashMap<ticket, Set<Session>>` + Redis Set 兜底（多实例场景）。

**多实例部署**：WS 会话有状态，需用 Redis Pub/Sub 广播状态变更 → 各实例收到后推自己本地的 WS 会话。项目 Redis 已有，加个 `RedisMessageListenerContainer` 即可。

### 7.2 用户端 Web（minecraft-ui，Vue3 + Element Plus）

**安装依赖**：

```bash
npm i qrcode
```

**页面/组件**：

```
src/views/auth/components/
├── QrLoginPanel.vue        # 二维码面板（生成、倒计时、状态机渲染）
└── useQrLogin.js           # 组合式函数：生成 + WS 订阅 + 轮询兜底 + consume
```

**`useQrLogin.js` 核心逻辑**：

```js
import { ref, onUnmounted } from 'vue'
import QRCode from 'qrcode'
import { generateQr, consumeQr, getQrStatus } from '@/api/auth'
import { useUserStore } from '@/store/modules/user'
import { usePermissionStore } from '@/store/modules/permission'

export function useQrLogin({ onSuccess }) {
  const status = ref('WAITING')   // WAITING / SCANNED / CONFIRMED / CONSUMED / EXPIRED / CANCELED
  const qrDataUrl = ref('')
  const countdown = ref(0)
  let ticket = null, ws = null, timer = null, pollTimer = null

  async function start() {
    reset()
    const { data } = await generateQr()
    ticket = data.ticket
    qrDataUrl.value = await QRCode.toDataURL(data.qrContent, { width: 220, margin: 1 })
    countdown.value = data.ttl
    tickCountdown()

    tryConnectWs()
    startPollFallback()  // 双保险：WS 断了轮询能兜
  }

  function tryConnectWs() {
    const url = `${location.protocol === 'https:' ? 'wss' : 'ws'}://${location.host}/ws/qr/${ticket}`
    ws = new WebSocket(url)
    ws.onmessage = (e) => {
      const msg = JSON.parse(e.data)
      if (msg.type !== 'qr_status') return
      status.value = msg.status
      if (msg.status === 'CONFIRMED') onConfirmed()
      if (['EXPIRED', 'CANCELED', 'CONSUMED'].includes(msg.status)) stopAll()
    }
    ws.onclose = () => { if (!['CONSUMED','EXPIRED','CANCELED'].includes(status.value)) setTimeout(tryConnectWs, 2000) }
    ws.onerror = () => ws.close()
  }

  async function onConfirmed() {
    const { data } = await consumeQr(ticket)
    useUserStore().setToken(data.token)
    useUserStore().setUserInfo(data.userInfo)
    usePermissionStore().$patch({ permissions: data.permissions, roles: data.roles, menus: data.menus, loaded: true })
    stopAll()
    onSuccess?.(data)
  }

  function startPollFallback() {
    pollTimer = setInterval(async () => {
      if (ws && ws.readyState === WebSocket.OPEN && status.value !== 'WAITING') return
      const { data } = await getQrStatus(ticket)
      status.value = data.status
      if (data.status === 'CONFIRMED') onConfirmed()
      if (['EXPIRED','CANCELED','CONSUMED'].includes(data.status)) stopAll()
    }, 2000)
  }

  // ... tickCountdown / stopAll / reset 略
  onUnmounted(stopAll)
  return { status, qrDataUrl, countdown, start, refresh: start }
}
```

**UI 状态渲染**：

| status | 二维码面板 |
|--------|-----------|
| `WAITING` | 显示二维码 + 倒计时，附文字"请使用 Minecraft App 扫一扫" |
| `SCANNED` | 二维码上方浮层"扫描成功，请在手机端确认登录" |
| `CONFIRMED` | 浮层"登录中…"，1s 内跳转 |
| `EXPIRED` | 二维码蒙层 + "二维码已失效，点击刷新" 按钮 |
| `CANCELED` | 蒙层 + "已取消，请重新扫码" |

**登录页整合**：现有登录页已有「账号/手机/二维码」Tab，本方案**只是**给"二维码" Tab 增加 APP 扫码授权能力（若现有二维码是凭据式，可共存：默认展示 APP 扫码，失败回落到原方式）。

### 7.3 管理端（minecraft-admin-main，Vben Admin 5.7）

管理端同样可支持 APP 扫码登录（尤其适合运营人员）：

- `accessStore.setAccessCodes(...)` 依赖 `consume` 接口返回的 `permissions`，与账号登录一致。
- WS 订阅逻辑可复用 `minecraft-ui` 中的 `useQrLogin.js`（抽成 `@minecraft/shared-qr` 公共包，三端共享）。
- 新增 `LoginQrPanel.vue`，路由无需动态注册（登录页固定）。

### 7.4 移动端 APP（minecraft-App，uni-app）

**新增页面**：`pages/login/scan.vue`

```vue
<template>
  <view class="scan-page">
    <!-- 扫描结果弹窗 -->
    <uni-popup ref="confirmPopup" type="dialog">
      <view class="confirm-box">
        <text class="title">确认登录 PC 端？</text>
        <view class="device-info">
          <text>设备：{{ detail.browser }} / {{ detail.os }}</text>
          <text>位置：{{ detail.region }}（{{ detail.ip }}）</text>
          <text>若非本人操作，请点击取消</text>
        </view>
        <view class="actions">
          <button @click="onCancel">取消</button>
          <button type="primary" @click="onConfirm">确认登录</button>
        </view>
      </view>
    </uni-popup>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { scanQr, confirmQr, cancelQr } from '@/api/auth'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
const confirmPopup = ref()
const detail = ref({})
let ticket = ''

async function startScan() {
  const res = await uni.scanCode({ scanType: ['qrCode'] })
  const url = new URL(res.result)   // minecraft://qrLogin?t=xxx 需先替换协议头
  ticket = url.searchParams.get('t')
  // 校验 APP 登录态
  if (!userStore.token) {
    uni.navigateTo({ url: `/pages/login/login?redirect=${encodeURIComponent(`/pages/login/scan?t=${ticket}`)}` })
    return
  }
  // 调后端校验并拿 Web 设备信息
  const { data } = await scanQr({ ticket, appVersion: uni.getSystemInfoSync().appVersion })
  detail.value = data.webClient
  confirmPopup.value.open()
}

async function onConfirm() {
  await confirmQr({ ticket })
  confirmPopup.value.close()
  uni.showToast({ title: '登录成功', icon: 'success' })
}

async function onCancel() {
  await cancelQr({ ticket }).catch(() => {})
  confirmPopup.value.close()
}
</script>
```

**导航入口**：

- APP 首页右上角「扫一扫」图标
- 个人中心「扫一扫登录」入口
- uni-app 已支持 `uni.scanCode`（无需额外 HBuilderX 配置，iOS/Android 需在 manifest 中声明相机权限）

**降级**：若扫出的二维码**不是** `minecraft://qrLogin` 协议（例如是普通 Web 链接），引导用户用内置浏览器打开，不阻断。

---

## 八、与现有系统的融合点

| 融合点 | 做法 |
|--------|------|
| **权限加载** | 消费 `consume` 时直接调 `permissionService.loadUserAuth`，与账号登录共享缓存 |
| **JWT 签发** | 复用 `jwtUtils.createToken(loginUser)`，Token 结构、有效期完全一致 |
| **WS 通道** | 新增消息 `type=qr_status`，与 IM/信令并存，按路由前缀分流 |
| **登录审计** | 复用 `login_log`，新增 `login_type=QR` 枚举值；`login_source=APP_SCAN` |
| **限流组件** | 复用 Redis 计数器；如已有 `@RateLimit` 注解直接复用 |
| **滑块验证** | 与扫码登录解耦，用户可随时在 Tab 间切换；扫码失败回落到密码+滑块 |
| **踢下线** | 沿用现有 JWT `jti` 黑名单；管理员踢人时，`mc:qr:login:*` 无需处理（扫码完成后已是普通 token） |
| **Druid / Knife4j** | 新增 8 个接口自动纳入监控与文档 |

---

## 九、配置

`application.yml` 新增：

```yaml
qr-login:
  enabled: true
  ttl-seconds: 120
  confirmed-ttl-seconds: 30
  poll-fallback: true
  ws-heartbeat-seconds: 25
  rate-limit:
    generate-per-ip-per-min: 10
    scan-per-user-per-min: 20
  qr-protocol: minecraft://qrLogin
```

**开关**：`qr-login.enabled=false` 时，`/qr/generate` 返回业务错误"暂不支持扫码登录"，前端自动隐藏 Tab（配置可由后端 `/api/system/config` 下发，管理端可动态开关，无需重启）。

---

## 十、测试用例

| 编号 | 用例 | 预期 |
|------|------|------|
| TC-01 | 生成二维码，120s 内不扫码 | 到期后 Web 收到 EXPIRED，二维码蒙层 |
| TC-02 | APP 扫码 → 确认 → Web 跳转 | 端到端 ≤ 3s，login_log 记录 type=QR |
| TC-03 | APP 扫码后，Web 关闭页面再重开（同 ticket 未过期） | WS 重连成功，状态同步 |
| TC-04 | APP 扫码后取消 | Web 收到 CANCELED，二维码需刷新 |
| TC-05 | Web 未在 30s 内 consume（CONFIRMED 后） | ticket 过期，Web 提示"登录超时" |
| TC-06 | 同一 ticket 被两个 Web 同时 consume | 仅一个成功，另一个报"已使用" |
| TC-07 | A 用户 APP 扫码，B 用户 APP 尝试 confirm 同一 ticket | 拒绝，返回"无权确认" |
| TC-08 | 伪造 48 位随机 ticket 调 scan | 拒绝 + 限流计数 |
| TC-09 | 同 IP 1 分钟内 generate 11 次 | 第 11 次被限流 |
| TC-10 | WS 断开 | 自动降级到轮询，扫码后仍能完成登录 |
| TC-11 | 多实例部署（2 实例） | WS 跨实例推送正常（Redis Pub/Sub） |
| TC-12 | APP 未登录状态扫码 | 引导跳转 APP 登录页，登录后续扫 |
| TC-13 | 扫码登录后，Web 端权限与账号登录一致 | permissions/menus 完全一致 |
| TC-14 | 管理端踢下线该 token | Web 端失效（走现有逻辑） |

---

## 十一、上线与灰度

| 阶段 | 动作 |
|------|------|
| 阶段一 | 后端接口 + Web 端「扫码登录」Tab 灰度 10% 用户 |
| 阶段二 | APP 发版加入「扫一扫」入口，监控扫码成功率 |
| 阶段三 | 灰度 50% → 100% |
| 阶段四 | 观察 1 周：生成量、扫码量、确认率、consume 成功率、异常率 |

**回滚**：`qr-login.enabled=false` 秒级关闭，不影响账号密码登录。

---

## 十二、扩展能力（预留）

| 扩展需求 | 当前预留位置 |
|---------|-------------|
| **扫码授权第三方 OAuth** | ticket 结构增加 `scope` / `clientId` 字段，`consume` 时按 scope 签发受限 token |
| **扫码绑定账号**（新设备首次登录） | ticket 状态增加 `BINDING`，`confirm` 前要求 APP 端二次验证密码 |
| **扫码支付 / 扫码开门** | 同一 ticket 框架复用，改 `qrContent` 协议头（`minecraft://qrPay?t=`）即可 |
| **H5 扫一扫（无 APP）** | 落库历史 + 唯一短链跳转，浏览器扫码后登录后自动回调 |
| **多 Web 端同时扫同一 ticket** | WS Set 已支持多订阅；`consume` 原子性保证仅一个成功 |
| **风控**（异地登录二次确认） | `confirm` 前查 `login_log` 最近 N 条，命中异地则要求 APP 弹二次确认 |
| **登录设备管理** | 已登录设备列表 = `login_log` + JWT `jti`；踢下线复用现有 |

---

## 十三、工作量与排期

| 模块 | 工作项 | 人日 |
|------|--------|------|
| 后端 | 接口 + Redis 状态机 + WS 推送 + 限流 + 日志 | 2.0 |
| 后端 | 单测 + 联调 | 0.5 |
| Web | `useQrLogin` + 二维码组件 + 登录页整合 | 1.0 |
| 管理端 | 复用 Web 组件 + 接入 Vben | 0.5 |
| APP | 扫码页 + 确认弹窗 + 入口 | 0.5 |
| 测试 | 功能 + 安全 + 性能 | 1.0 |
| **合计** | | **≈ 5.5 人日** |

> 若项目内已有"二维码登录"的 Web 端 UI 与 WS 通道，工作量可压缩至 **3 人日**。

---

## 附录：核心错误码

| code | 含义 | 处理建议 |
|------|------|---------|
| `QR_4001` | ticket 不存在 | 前端提示"二维码已失效，请刷新" |
| `QR_4002` | ticket 已过期 | 同上 |
| `QR_4003` | ticket 已被使用 | 前端提示"已登录，请重新生成" |
| `QR_4004` | ticket 已被他人扫描 | APP 端提示"二维码已被占用" |
| `QR_4005` | 无权确认（scannerUserId 不匹配） | APP 端提示"非本人扫码" |
| `QR_4006` | 状态流转非法 | 前端自动刷新二维码 |
| `QR_4007` | 扫码频率超限 | 提示稍后再试 |
| `QR_4008` | 扫码登录已关闭 | 前端隐藏 Tab |

---