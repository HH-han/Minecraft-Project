package com.minecraft.security.cache;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.minecraft.security.core.LoginUser;
import com.minecraft.security.core.UserAuthCache;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * 权限 Redis 缓存。
 *
 * <p>Key 设计：</p>
 * <ul>
 *   <li>{@code mc:perm:v{version}:user:{userId}} —— 用户完整权限（30 分钟）</li>
 *   <li>{@code mc:perm:roleUsers:{roleId}} —— 角色下用户集合（30 分钟）</li>
 *   <li>{@code mc:perm:version} —— 全局版本号，权限表变更时 +1，实现整批失效</li>
 * </ul>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PermissionCacheManager {

    private static final String USER_KEY_PREFIX = "mc:perm:v";
    private static final String USER_KEY_SUFFIX = ":user:";
    private static final String ROLE_USERS_KEY = "mc:perm:roleUsers:";
    private static final String VERSION_KEY = "mc:perm:version";
    private static final Duration TTL = Duration.ofMinutes(30);

    private final StringRedisTemplate redis;
    private final ObjectMapper objectMapper;

    public long currentVersion() {
        try {
            String value = redis.opsForValue().get(VERSION_KEY);
            return value == null ? 0L : Long.parseLong(value);
        } catch (Exception e) {
            log.warn("读取权限缓存版本失败，降级为 0: {}", e.getMessage());
            return 0L;
        }
    }

    /** 权限表 / 菜单变更后整批失效 */
    public void bumpVersion() {
        try {
            redis.opsForValue().increment(VERSION_KEY);
        } catch (Exception e) {
            log.warn("权限缓存版本自增失败: {}", e.getMessage());
        }
    }

    public LoginUser getUserAuth(Long userId) {
        try {
            String json = redis.opsForValue().get(userKey(userId));
            if (json == null) {
                return null;
            }
            return objectMapper.readValue(json, UserAuthCache.class).toLoginUser();
        } catch (Exception e) {
            log.warn("读取用户权限缓存失败 userId={}: {}", userId, e.getMessage());
            return null;
        }
    }

    public void putUserAuth(LoginUser user, Collection<Long> roleIds) {
        if (user == null || user.getUserId() == null) {
            return;
        }
        try {
            String json = objectMapper.writeValueAsString(UserAuthCache.from(user));
            redis.opsForValue().set(userKey(user.getUserId()), json, TTL);
            if (roleIds != null) {
                for (Long roleId : roleIds) {
                    String key = ROLE_USERS_KEY + roleId;
                    redis.opsForSet().add(key, String.valueOf(user.getUserId()));
                    redis.expire(key, TTL);
                }
            }
        } catch (Exception e) {
            log.warn("写入用户权限缓存失败 userId={}: {}", user.getUserId(), e.getMessage());
        }
    }

    public void evictUser(Long userId) {
        if (userId == null) {
            return;
        }
        try {
            redis.delete(userKey(userId));
        } catch (Exception e) {
            log.warn("清理用户权限缓存失败 userId={}: {}", userId, e.getMessage());
        }
    }

    public void evictByRole(Long roleId) {
        if (roleId == null) {
            return;
        }
        try {
            String key = ROLE_USERS_KEY + roleId;
            Set<String> userIds = redis.opsForSet().members(key);
            if (userIds != null && !userIds.isEmpty()) {
                List<String> keys = userIds.stream().map(id -> userKey(Long.valueOf(id))).toList();
                redis.delete(keys);
            }
            redis.delete(key);
        } catch (Exception e) {
            log.warn("按角色清理权限缓存失败 roleId={}: {}", roleId, e.getMessage());
        }
    }

    /** 全局失效（版本号 +1） */
    public void evictAll() {
        bumpVersion();
    }

    private String userKey(Long userId) {
        return USER_KEY_PREFIX + currentVersion() + USER_KEY_SUFFIX + userId;
    }
}
