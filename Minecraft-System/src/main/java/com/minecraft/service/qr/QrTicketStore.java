package com.minecraft.service.qr;

import com.alibaba.fastjson2.JSON;
import com.minecraft.dto.qr.QrTicket;
import com.minecraft.enums.QrLoginStatus;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 扫码登录 ticket 存储。
 *
 * <p>优先使用 Redis（支持多实例部署与进程重启后状态保留）；
 * 当 Redis 不可用时（例如本地开发未启动 Redis）自动降级为进程内内存存储，
 * 保证扫码登录仍可单机使用。降级期间每 30 秒探测一次 Redis，恢复后自动切回。</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class QrTicketStore {

    private static final String KEY_PREFIX = "mc:qr:login:";

    private static final String USED_PREFIX = "mc:qr:used:";

    private static final String BLOCK_PREFIX = "mc:qr:block:";

    private static final String PROBE_KEY = "mc:qr:probe";

    private static final long REDIS_RETRY_MILLIS = 30_000L;

    /** 状态 CAS：仅当当前状态等于期望状态时才写入新快照，保留原 TTL */
    private static final DefaultRedisScript<Long> CAS_SCRIPT = new DefaultRedisScript<>(
            "local cur = redis.call('GET', KEYS[1])\n" +
            "if not cur then return 0 end\n" +
            "local ok, obj = pcall(cjson.decode, cur)\n" +
            "if not ok or obj['status'] ~= ARGV[1] then return -1 end\n" +
            "redis.call('SET', KEYS[1], ARGV[2], 'KEEPTTL')\n" +
            "return 1",
            Long.class);

    /** 原子消费：仅 CONFIRMED 状态可被取走，取走的同时删除 key */
    private static final DefaultRedisScript<String> CONSUME_SCRIPT = new DefaultRedisScript<>(
            "local cur = redis.call('GET', KEYS[1])\n" +
            "if not cur then return nil end\n" +
            "local ok, obj = pcall(cjson.decode, cur)\n" +
            "if not ok then return '__BAD__' end\n" +
            "if obj['status'] ~= 'CONFIRMED' then return '__NOT_CONFIRMED__' end\n" +
            "redis.call('DEL', KEYS[1])\n" +
            "return cur",
            String.class);

    private final StringRedisTemplate redis;

    private final Map<String, MemoryEntry> memory = new ConcurrentHashMap<>();

    private final Map<String, Long> usedMemory = new ConcurrentHashMap<>();

    private volatile boolean redisActive = true;

    private volatile long nextRedisProbeAt = 0L;

    public enum ConsumeStatus {
        MISSING,
        NOT_CONFIRMED,
        OK
    }

    @Getter
    public static class ConsumeResult {

        private final ConsumeStatus status;

        private final QrTicket ticket;

        public ConsumeResult(ConsumeStatus status, QrTicket ticket) {
            this.status = status;
            this.ticket = ticket;
        }
    }

    private static class MemoryEntry {

        private QrTicket data;

        private long expireAt;

        MemoryEntry(QrTicket data, long expireAt) {
            this.data = data;
            this.expireAt = expireAt;
        }
    }

    public void save(String ticket, QrTicket data, Duration ttl) {
        cleanupMemory();
        if (redisReady()) {
            try {
                redis.opsForValue().set(KEY_PREFIX + ticket, JSON.toJSONString(data), ttl);
                synchronized (memory) {
                    memory.remove(ticket);
                }
                return;
            } catch (Exception e) {
                markRedisDown(e);
            }
        }
        synchronized (memory) {
            memory.put(ticket, new MemoryEntry(data, System.currentTimeMillis() + ttl.toMillis()));
        }
    }

    public QrTicket get(String ticket) {
        if (redisReady()) {
            try {
                String json = redis.opsForValue().get(KEY_PREFIX + ticket);
                if (json != null) {
                    return parse(json);
                }
            } catch (Exception e) {
                markRedisDown(e);
            }
        }
        synchronized (memory) {
            MemoryEntry entry = memory.get(ticket);
            if (entry == null) {
                return null;
            }
            if (entry.expireAt <= System.currentTimeMillis()) {
                memory.remove(ticket);
                return null;
            }
            return entry.data;
        }
    }

    /**
     * 状态 CAS 更新。
     *
     * @return true 表示更新成功；false 表示状态不匹配或 ticket 已不存在
     */
    public boolean cas(String ticket, QrLoginStatus expected, QrTicket updated) {
        String payload = JSON.toJSONString(updated);
        if (redisReady()) {
            try {
                Long result = redis.execute(CAS_SCRIPT, Collections.singletonList(KEY_PREFIX + ticket),
                        expected.name(), payload);
                if (result != null && result == 1L) {
                    synchronized (memory) {
                        memory.remove(ticket);
                    }
                    return true;
                }
                if (result != null && result == -1L) {
                    return false;
                }
            } catch (Exception e) {
                markRedisDown(e);
            }
        }
        return memoryCas(ticket, expected, updated);
    }

    public void expire(String ticket, Duration ttl) {
        if (redisReady()) {
            try {
                Boolean ok = redis.expire(KEY_PREFIX + ticket, ttl);
                if (Boolean.TRUE.equals(ok)) {
                    return;
                }
            } catch (Exception e) {
                markRedisDown(e);
            }
        }
        synchronized (memory) {
            MemoryEntry entry = memory.get(ticket);
            if (entry != null && entry.expireAt > System.currentTimeMillis()) {
                entry.expireAt = System.currentTimeMillis() + ttl.toMillis();
            }
        }
    }

    /**
     * 原子消费 CONFIRMED 状态的 ticket。
     */
    public ConsumeResult consumeConfirmed(String ticket) {
        if (redisReady()) {
            try {
                String json = redis.execute(CONSUME_SCRIPT, Collections.singletonList(KEY_PREFIX + ticket));
                if ("__NOT_CONFIRMED__".equals(json)) {
                    return new ConsumeResult(ConsumeStatus.NOT_CONFIRMED, null);
                }
                if (json != null && !"__BAD__".equals(json)) {
                    QrTicket parsed = parse(json);
                    if (parsed != null) {
                        synchronized (memory) {
                            memory.remove(ticket);
                        }
                        return new ConsumeResult(ConsumeStatus.OK, parsed);
                    }
                    return new ConsumeResult(ConsumeStatus.MISSING, null);
                }
            } catch (Exception e) {
                markRedisDown(e);
            }
        }
        return memoryConsume(ticket);
    }

    public boolean isUsed(String ticket) {
        if (redisReady()) {
            try {
                if (Boolean.TRUE.equals(redis.hasKey(USED_PREFIX + ticket))) {
                    return true;
                }
            } catch (Exception e) {
                markRedisDown(e);
            }
        }
        Long expireAt = usedMemory.get(ticket);
        if (expireAt == null) {
            return false;
        }
        if (expireAt <= System.currentTimeMillis()) {
            usedMemory.remove(ticket);
            return false;
        }
        return true;
    }

    public void markUsed(String ticket, Duration ttl) {
        usedMemory.put(ticket, System.currentTimeMillis() + ttl.toMillis());
        if (redisReady()) {
            try {
                redis.opsForValue().set(USED_PREFIX + ticket, "1", ttl);
            } catch (Exception e) {
                markRedisDown(e);
            }
        }
    }

    public boolean isBlocked(String ip) {
        if (!redisReady()) {
            return false;
        }
        try {
            return Boolean.TRUE.equals(redis.hasKey(BLOCK_PREFIX + ip));
        } catch (Exception e) {
            markRedisDown(e);
            return false;
        }
    }

    public void block(String ip, Duration ttl) {
        if (!redisReady()) {
            return;
        }
        try {
            redis.opsForValue().set(BLOCK_PREFIX + ip, "1", ttl);
        } catch (Exception e) {
            markRedisDown(e);
        }
    }

    /**
     * 固定窗口计数器；Redis 不可用时返回 null，调用方应跳过限流。
     */
    public Long increment(String key, Duration window) {
        if (!redisReady()) {
            return null;
        }
        try {
            Long value = redis.opsForValue().increment(key);
            if (value != null && value == 1L) {
                redis.expire(key, window);
            }
            return value;
        } catch (Exception e) {
            markRedisDown(e);
            return null;
        }
    }

    private QrTicket parse(String json) {
        try {
            return JSON.parseObject(json, QrTicket.class);
        } catch (Exception e) {
            log.warn("解析扫码登录 ticket 失败: {}", e.getMessage());
            return null;
        }
    }

    private boolean memoryCas(String ticket, QrLoginStatus expected, QrTicket updated) {
        synchronized (memory) {
            MemoryEntry entry = memory.get(ticket);
            if (entry == null || entry.expireAt <= System.currentTimeMillis()) {
                memory.remove(ticket);
                return false;
            }
            if (entry.data.getStatus() != expected) {
                return false;
            }
            entry.data = updated;
            return true;
        }
    }

    private ConsumeResult memoryConsume(String ticket) {
        synchronized (memory) {
            MemoryEntry entry = memory.get(ticket);
            if (entry == null || entry.expireAt <= System.currentTimeMillis()) {
                memory.remove(ticket);
                return new ConsumeResult(ConsumeStatus.MISSING, null);
            }
            if (entry.data.getStatus() != QrLoginStatus.CONFIRMED) {
                return new ConsumeResult(ConsumeStatus.NOT_CONFIRMED, null);
            }
            memory.remove(ticket);
            return new ConsumeResult(ConsumeStatus.OK, entry.data);
        }
    }

    private void cleanupMemory() {
        long now = System.currentTimeMillis();
        if (memory.size() > 256) {
            memory.entrySet().removeIf(e -> e.getValue().expireAt <= now);
        }
        usedMemory.entrySet().removeIf(e -> e.getValue() <= now);
    }

    private boolean redisReady() {
        if (redisActive) {
            return true;
        }
        long now = System.currentTimeMillis();
        if (now < nextRedisProbeAt) {
            return false;
        }
        try {
            redis.hasKey(PROBE_KEY);
            redisActive = true;
            log.info("Redis 已恢复，扫码登录状态切回 Redis 存储");
            return true;
        } catch (Exception e) {
            nextRedisProbeAt = now + REDIS_RETRY_MILLIS;
            return false;
        }
    }

    private void markRedisDown(Exception e) {
        if (redisActive) {
            log.warn("Redis 不可用，扫码登录状态自动降级为本地内存存储: {}", e.getMessage());
        }
        redisActive = false;
        nextRedisProbeAt = System.currentTimeMillis() + REDIS_RETRY_MILLIS;
    }
}
