package com.albert.learning.redis;

import org.redisson.api.RRateLimiter;
import org.redisson.api.RateIntervalUnit;
import org.redisson.api.RateType;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

@Component
public class Init {
    @Autowired
    private RedissonClient redissonClient;
    @PostConstruct
    public void initRateLimiters() throws InterruptedException {
        RRateLimiter rateLimiter = redissonClient.getRateLimiter("rate1:user:1");
        // 每分钟最多100次，类型为每个时间段自动生成固定数量令牌
        // RateType.OVERALL 为全局限流 RateType.PER_CLIENT是每个客户端限流
        rateLimiter.trySetRate(RateType.OVERALL, 20, 1, RateIntervalUnit.MINUTES);

        // 消耗 10 个令牌，让桶不是满的
        rateLimiter.tryAcquire(10);

//        for (int i = 0; i < 30; i++) {
//            long available = rateLimiter.availablePermits(); // 查看可用令牌
//            System.out.println(LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")) +
//                    " 可用令牌: " + available);
//            Thread.sleep(1000);
//        }
    }
}
