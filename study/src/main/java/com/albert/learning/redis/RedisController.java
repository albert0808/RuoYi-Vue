package com.albert.learning.redis;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.redisson.config.SentinelServersConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/test/redis")
public class RedisController {
    @Autowired
    private RedisTemplate<String,Object> redisTemplate;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private BasicMethod basicMethod;

    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private RateLimitingService rateLimitingService;
    @GetMapping("/api/profile/view")
    public String viewProfile(@RequestParam String userId) {
//        if (!rateLimitingService.isAllowed(userId, 10, 60_000)) {
//            return "您访问太频繁啦，请稍后重试";
//        }
        if (!rateLimitingService.isAllowedByBucket(userId)) {
            return "您访问太频繁啦，请稍后重试";
        }
        return "个人信息详情";
    }
    @GetMapping("/string")
    public void doString() throws IOException {
        redisTemplate.opsForValue().set("test1","test1");
        redisTemplate.opsForValue().set("test2","test2",10l, TimeUnit.MINUTES);


        // 获取 JSON 字符串
        String json = redissonClient.getConfig().toJSON();

        // 用 Jackson 转为 Map
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> map = mapper.readValue(json, new TypeReference<Map<String, Object>>() {});

        // 取 sentinelServersConfig
        Map<String, Object> sentinelConfig = (Map<String, Object>) map.get("sentinelServersConfig");
        if (sentinelConfig != null) {
            System.out.println("Master: " + sentinelConfig.get("masterName"));
            System.out.println("Master Address: " + sentinelConfig.get("masterAddress"));
            System.out.println("ReadMode: " + sentinelConfig.get("readMode"));
            System.out.println("Nodes: " + sentinelConfig.get("sentinelAddresses"));
        }
        System.out.println("RedissonClient class: " + redissonClient.getClass().getName());
        System.out.println("RedisTemplate: " + redisTemplate);
        System.out.println("ConnectionFactory class: " + redisTemplate.getConnectionFactory().getClass().getName());
        System.out.println("ConnectionFactory object: " + redisTemplate.getConnectionFactory());
        System.out.println(redisTemplate.opsForValue().get("test1"));
        System.out.println(redisTemplate.opsForValue().get("test2"));
    }

    @GetMapping("/redisson")
    public String testRedisson() {
        basicMethod.testLock();
        return "Redisson test OK";
    }
    @GetMapping("/testLock")
    public String testLock() throws InterruptedException {
        String key = "LockTest";
        Thread a = new Thread(()->{
            String valueA = "A-" + UUID.randomUUID();
            boolean locked  = basicMethod.tryLock(key,valueA,3);
            if(locked){
                System.out.println("线程A 获得锁");
                try{
                    Thread.sleep(5000);
                }catch (InterruptedException e){

                }
                basicMethod.unlock(key,valueA);
                System.out.println("线程A 释放锁");
            }
        });
        Thread b = new Thread(()->{
            try { Thread.sleep(4000); } catch (InterruptedException ignored) {}
            String valueB = "B-" + UUID.randomUUID();
            boolean locked = basicMethod.tryLock(key, valueB, 5);
            if (locked) {
                System.out.println("线程B 获得锁");
                basicMethod.unlock(key, valueB);
                System.out.println("线程B 释放锁");
            }
        });
        a.start();
        b.start();
        a.join();
        b.join();
        return "test OK";
    }
}
