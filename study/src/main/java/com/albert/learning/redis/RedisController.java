package com.albert.learning.redis;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/test/redis")
public class RedisController {
    @Autowired
    private RedisTemplate<String,Object> redisTemplate;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;


    @GetMapping("/string")
    public void doString(){
        redisTemplate.opsForValue().set("test1","test1");
        redisTemplate.opsForValue().set("test2","test2",10l, TimeUnit.MINUTES);

        System.out.println(redisTemplate.opsForValue().get("test1"));
        System.out.println(redisTemplate.opsForValue().get("test2"));
    }
}
