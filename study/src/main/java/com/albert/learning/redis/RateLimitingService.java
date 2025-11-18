package com.albert.learning.redis;

import org.redisson.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;

@Service
public class RateLimitingService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private RedissonClient redissonClient;

    public boolean isAllowed(String userId, int limit, long windowMillis) {
        String key = "rate:user:" + userId;
        long now = System.currentTimeMillis();
        long windowStart = now - windowMillis;
        System.out.println(now);
        System.out.println(windowStart);
        // 清除过期请求记录
        redisTemplate.opsForZSet().removeRangeByScore(key, 0, windowStart);

        // 获取当前窗口请求数量
        Long count = redisTemplate.opsForZSet().zCard(key);

        if (count != null && count >= limit) {
            return false;
        }

        // 添加当前请求时间戳
        redisTemplate.opsForZSet().add(key, String.valueOf(now), now);
        redisTemplate.expire(key, 70, TimeUnit.SECONDS);
        return true;
    }

    public boolean isAllowedByBucket(String userId) {
        RRateLimiter rateLimiter = redissonClient.getRateLimiter("rate1:user:" + userId);

        // 初始化速率，仅第一次生效
        rateLimiter.trySetRate(RateType.OVERALL, 20, 1, RateIntervalUnit.MINUTES);

        // 打印当前时间
        LocalDateTime now = LocalDateTime.now();
        String formatted = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        System.out.println("当前时间: " + formatted);

        // 打印桶配置
        RateLimiterConfig config = rateLimiter.getConfig();
        System.out.println("RateType: " + config.getRateType());
        System.out.println("Rate (每分钟令牌总数): " + config.getRate());

        // 打印当前可用令牌
        long availableTokens = rateLimiter.availablePermits();
        System.out.println("Available tokens: " + availableTokens);
        System.out.println("____________________________________________");

        // 尝试获取一个令牌
        boolean allowed = rateLimiter.tryAcquire(1);
        if (!allowed) {
            System.out.println("当前请求被限流！");
        }

        return allowed;
    }


}
