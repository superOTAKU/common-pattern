package org.pattern.ratelimit;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.Transaction;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 基于redis sorted set实现的滑动窗口限流算法
 */
public class RedisRateLimiter implements RateLimiter {
    private final int permitsPerSecond;
    private final Jedis jedis;
    private final String key;

    public RedisRateLimiter(int permitsPerSecond, Jedis jedis, String key) {
        this.permitsPerSecond = permitsPerSecond;
        this.jedis = jedis;
        this.key = key;
    }

    @Override
    public boolean tryAcquire() {
        String val = jedis.get(key);
        if (val != null && Integer.parseInt(val) > permitsPerSecond) {
            return false;
        }
        long now = System.currentTimeMillis();
        long before = now - TimeUnit.SECONDS.toMillis(1);
        Transaction transaction = jedis.multi();
        //删除旧值
        transaction.zremrangeByScore(key, 0, before);
        transaction.zadd(key, now, UUID.randomUUID().toString());
        //统计时间窗口内的请求数
        transaction.zcount(key, before, now);
        List<Object> responseList = transaction.exec();
        Long count = (Long)responseList.get(2);
        return count <= permitsPerSecond;
    }

}
