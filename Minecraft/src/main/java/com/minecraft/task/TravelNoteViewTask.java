package com.minecraft.task;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.minecraft.entity.TravelNote;
import com.minecraft.mapper.TravelNoteMapper;
import com.minecraft.service.impl.TravelNoteServiceImpl;
import com.minecraft.utils.RedisUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 旅游笔记浏览量回写定时任务
 * Redis Hash note:view:pending（field=noteId, value=增量）→ 每5分钟回写数据库
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TravelNoteViewTask {

    /** 单批回写的笔记数量，避免单条 SQL 过长 */
    private static final int BATCH_SIZE = 200;

    private final RedisUtil redisUtil;
    private final TravelNoteMapper travelNoteMapper;

    @Scheduled(cron = "0 */5 * * * ?")
    public void flushViewCounts() {
        Map<Object, Object> entries = redisUtil.hgetall(TravelNoteServiceImpl.VIEW_PENDING_KEY);
        if (entries.isEmpty()) {
            return;
        }

        List<long[]> pending = new ArrayList<>(entries.size());
        for (Map.Entry<Object, Object> entry : entries.entrySet()) {
            long noteId;
            try {
                noteId = Long.parseLong(String.valueOf(entry.getKey()).trim());
            } catch (NumberFormatException e) {
                log.warn("[旅游笔记] 非法浏览量缓存键: {}", entry.getKey());
                continue;
            }

            Object value = entry.getValue();
            long delta;
            if (value instanceof Number number) {
                delta = number.longValue();
            } else {
                try {
                    delta = Long.parseLong(String.valueOf(value).trim());
                } catch (NumberFormatException e) {
                    continue;
                }
            }
            if (delta > 0) {
                pending.add(new long[]{noteId, delta});
            }
        }

        long total = 0;
        int notes = 0;
        // 按批次用 CASE WHEN 合并为一次 UPDATE，避免每篇笔记一次数据库往返
        for (int start = 0; start < pending.size(); start += BATCH_SIZE) {
            List<long[]> batch = pending.subList(start, Math.min(start + BATCH_SIZE, pending.size()));
            StringBuilder caseSql = new StringBuilder("view_count = view_count + CASE id");
            List<Long> ids = new ArrayList<>(batch.size());
            for (long[] row : batch) {
                caseSql.append(" WHEN ").append(row[0]).append(" THEN ").append(row[1]);
                ids.add(row[0]);
                total += row[1];
                notes++;
            }
            caseSql.append(" ELSE 0 END");
            travelNoteMapper.update(null, new LambdaUpdateWrapper<TravelNote>()
                    .setSql(caseSql.toString())
                    .in(TravelNote::getId, ids));
        }

        redisUtil.hdel(TravelNoteServiceImpl.VIEW_PENDING_KEY, entries.keySet().toArray());
        if (notes > 0) {
            log.info("[旅游笔记] 浏览量回写完成: {} 篇笔记, 共 {} 次", notes, total);
        }
    }
}
