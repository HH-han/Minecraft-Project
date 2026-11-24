package com.minecraft.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Component
public class RedisUtil {

    /** SCAN 单批处理数量 */
    private static final int SCAN_BATCH = 500;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    public void set(String key, Object value) {
        redisTemplate.opsForValue().set(key, value);
    }

    public void set(String key, Object value, long timeout, TimeUnit unit) {
        redisTemplate.opsForValue().set(key, value, timeout, unit);
    }

    public Object get(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    /**
     * 批量获取值，将 N 次网络往返压缩为 1 次（如好友列表的在线状态/未读数）。
     */
    public List<Object> multiGet(Collection<String> keys) {
        if (keys == null || keys.isEmpty()) {
            return List.of();
        }
        return redisTemplate.opsForValue().multiGet(keys);
    }

    public Boolean delete(String key) {
        return redisTemplate.delete(key);
    }

    public Long delete(Collection<String> keys) {
        return redisTemplate.delete(keys);
    }

    public Boolean hasKey(String key) {
        return redisTemplate.hasKey(key);
    }

    public Boolean expire(String key, long timeout, TimeUnit unit) {
        return redisTemplate.expire(key, timeout, unit);
    }

    public Long getExpire(String key) {
        return redisTemplate.getExpire(key);
    }

    public void hset(String key, String field, Object value) {
        redisTemplate.opsForHash().put(key, field, value);
    }

    public Long hincr(String key, String field, long delta) {
        return redisTemplate.opsForHash().increment(key, field, delta);
    }

    public Object hget(String key, String field) {
        return redisTemplate.opsForHash().get(key, field);
    }

    public Map<Object, Object> hgetall(String key) {
        return redisTemplate.opsForHash().entries(key);
    }

    public void hdel(String key, Object... fields) {
        redisTemplate.opsForHash().delete(key, fields);
    }

    public Boolean hHasKey(String key, Object field) {
        return redisTemplate.opsForHash().hasKey(key, field);
    }

    public Long hSize(String key) {
        return redisTemplate.opsForHash().size(key);
    }

    public void lpush(String key, Object value) {
        redisTemplate.opsForList().leftPush(key, value);
    }

    public void rpush(String key, Object value) {
        redisTemplate.opsForList().rightPush(key, value);
    }

    public Object lpop(String key) {
        return redisTemplate.opsForList().leftPop(key);
    }

    public Object rpop(String key) {
        return redisTemplate.opsForList().rightPop(key);
    }

    public List<Object> lrange(String key, long start, long end) {
        return redisTemplate.opsForList().range(key, start, end);
    }

    public Long lsize(String key) {
        return redisTemplate.opsForList().size(key);
    }

    public void sadd(String key, Object... values) {
        redisTemplate.opsForSet().add(key, values);
    }

    public Set<Object> smembers(String key) {
        return redisTemplate.opsForSet().members(key);
    }

    public Boolean sismember(String key, Object value) {
        return redisTemplate.opsForSet().isMember(key, value);
    }

    public Long sremove(String key, Object... values) {
        return redisTemplate.opsForSet().remove(key, values);
    }

    public Long ssize(String key) {
        return redisTemplate.opsForSet().size(key);
    }

    public void zadd(String key, double score, Object value) {
        redisTemplate.opsForZSet().add(key, value, score);
    }

    public Set<Object> zrange(String key, long start, long end) {
        return redisTemplate.opsForZSet().range(key, start, end);
    }

    public Set<Object> zreverseRange(String key, long start, long end) {
        return redisTemplate.opsForZSet().reverseRange(key, start, end);
    }

    public Long zremove(String key, Object... values) {
        return redisTemplate.opsForZSet().remove(key, values);
    }

    public Long zsize(String key) {
        return redisTemplate.opsForZSet().size(key);
    }
    
    /**
     * 根据模式获取Redis键
     * @param pattern 键模式，例如：login_info:page:*
     * @return 匹配的键集合
     */
    public Set<String> keys(String pattern) {
        return redisTemplate.keys(pattern);
    }

    /**
     * 按模式分批删除键。
     * <p>使用 SCAN 游标逐批处理，避免 KEYS 一次性返回海量键造成 Redis 阻塞与应用堆内存尖峰。</p>
     */
    public long deleteByPattern(String pattern) {
        Long deleted = redisTemplate.execute((RedisCallback<Long>) connection -> {
            long count = 0;
            List<byte[]> batch = new ArrayList<>(SCAN_BATCH);
            ScanOptions options = ScanOptions.scanOptions().match(pattern).count(SCAN_BATCH).build();
            try (Cursor<byte[]> cursor = connection.keyCommands().scan(options)) {
                while (cursor.hasNext()) {
                    batch.add(cursor.next());
                    if (batch.size() >= SCAN_BATCH) {
                        count += connection.keyCommands().del(batch.toArray(new byte[0][]));
                        batch.clear();
                    }
                }
            }
            if (!batch.isEmpty()) {
                count += connection.keyCommands().del(batch.toArray(new byte[0][]));
            }
            return count;
        });
        return deleted == null ? 0L : deleted;
    }

    public boolean exists(String blockKey) {
        return redisTemplate.hasKey(blockKey);
    }
    
    public Boolean setIfAbsent(String key, Object value, long timeout, TimeUnit unit) {
        return redisTemplate.opsForValue().setIfAbsent(key, value, timeout, unit);
    }
    
    public Boolean setIfAbsent(String key, Object value) {
        return redisTemplate.opsForValue().setIfAbsent(key, value);
    }

    /**
     * 自增计数（用于固定窗口限流）。
     * 注意：底层使用 Redis INCR，要求键值为整数；首次自增后可配合 expire 设置窗口。
     */
    public Long increment(String key, long delta) {
        return redisTemplate.opsForValue().increment(key, delta);
    }
}