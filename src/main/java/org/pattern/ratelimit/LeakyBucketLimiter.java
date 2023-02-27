package org.pattern.ratelimit;

import java.util.concurrent.TimeUnit;

/**
 * 漏桶算法
 */
public class LeakyBucketLimiter implements RateLimiter {
    //上次通行时间
    private long lastPassNona;
    //允许通行的间隔
    private final double permitInterval;

    public LeakyBucketLimiter(int permitsPerSecond) {
        this.permitInterval = permitsPerSecond * 1.0 / TimeUnit.SECONDS.toNanos(1);
    }

    @Override
    public synchronized boolean tryAcquire() {
        //漏桶看当前时间是否符合间隔，如果符合则更新漏出时间
        long now = System.nanoTime();
        if (now - lastPassNona >= permitInterval) {
            lastPassNona = now;
            return true;
        }
        return false;
    }

}
