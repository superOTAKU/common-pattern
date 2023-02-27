package org.pattern.ratelimit;

public interface RateLimiter {

    boolean tryAcquire();

}
