package com.albert.learning.redis;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

    @GetMapping("/string")
    public void doString(){
        redisTemplate.opsForValue().set("test1","test1");
        redisTemplate.opsForValue().set("test2","test2",10l, TimeUnit.MINUTES);

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
