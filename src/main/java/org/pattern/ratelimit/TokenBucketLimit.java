package org.pattern.ratelimit;

import java.util.concurrent.TimeUnit;

/**
 * 令牌桶
 */
public class TokenBucketLimit implements RateLimiter {
    //当前存储的令牌数
    private double storedPermits;
    //最大令牌数
    private final double maxPermits;
    //发放令牌时间间隔
    private final double permitInterval;
    //下次可通行时间
    private long nextPassableNano;

    public TokenBucketLimit(int permitsPerSecond) {
        this.maxPermits = permitsPerSecond;
        this.permitInterval = permitsPerSecond * 1.0 / TimeUnit.SECONDS.toNanos(1);
        //预放置1个令牌
        this.storedPermits = 1;
        this.nextPassableNano = System.nanoTime();
    }

    @Override
    public synchronized boolean tryAcquire() {
        long now = System.nanoTime();
        if (now < nextPassableNano) {
            //令牌不足
            return false;
        }
        if (now > nextPassableNano) {
            //计算这堆时间存储的令牌数
            double permits = now - nextPassableNano / permitInterval;
            storedPermits = Math.min(maxPermits, permits + storedPermits);
            this.nextPassableNano = now;
        }
        if (this.storedPermits >= 1) {
            //如果桶中有令牌，消耗令牌
            this.storedPermits--;
        } else {
            //令牌不足，计算下次令牌发放时间
            this.nextPassableNano = (long)(this.nextPassableNano + (1 - this.storedPermits) * permitInterval);
            this.storedPermits = 0;
        }
        return true;
    }

}
