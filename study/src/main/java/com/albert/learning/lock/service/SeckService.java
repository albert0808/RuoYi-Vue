package com.albert.learning.lock.service;

import com.albert.learning.lock.Account;
import com.albert.learning.lock.manager.LockManager;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RReadWriteLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 转账方法
 * <p>分别包含不带锁的、单体应用适用锁、分布式适用的锁的方法</p>
 */
@Service
@Slf4j
public class SeckService {

    @Autowired
    private RedisTemplate<String,Object> redisTemplate;

    @Autowired
    private RedissonClient redissonClient;
    public void seckillNoLock(String productId) {
        String key = "stock:" + productId;
        Integer stock = (Integer) redisTemplate.opsForValue().get(key);
        if (stock != null && stock > 0) {
            // 模拟创建订单
            //createOrder(productId);
            redisTemplate.opsForValue().decrement(key);
        } else {
            System.out.println("库存不足！");
        }
    }


    public void seckillWithLock(String productId) {
        RLock lock = redissonClient.getLock("lock:product:" + productId);
        lock.lock();
        try {
            String key = "stock:" + productId;
            Integer stock = (Integer) redisTemplate.opsForValue().get(key);
            if (stock != null && stock > 0) {
                redisTemplate.opsForValue().decrement(key);
                System.out.println(Thread.currentThread().getName() + " 下单成功，剩余库存：" + (stock - 1));
            } else {
                System.out.println(Thread.currentThread().getName() + " 库存不足！");
            }
        } finally {
            lock.unlock();
        }
    }
    public int queryStock(String productId) {
        RReadWriteLock rwLock = redissonClient.getReadWriteLock("rw:product:" + productId);
        RLock readLock = rwLock.readLock();

        readLock.lock();
        try {
            Integer stock = (Integer) redisTemplate.opsForValue().get("stock:" + productId);
            return stock != null ? stock : 0;
        } finally {
            readLock.unlock();
        }
    }

    public void seckillWithReadWriteLock(String productId) {
        RReadWriteLock rwLock = redissonClient.getReadWriteLock("rw:product:" + productId);
        RLock writeLock = rwLock.writeLock();

        writeLock.lock();
        try {
            String key = "stock:" + productId;
            Integer stock = (Integer) redisTemplate.opsForValue().get(key);
            if (stock != null && stock > 0) {
                redisTemplate.opsForValue().decrement(key);
                System.out.println(Thread.currentThread().getName() + " 下单成功，剩余库存：" + (stock - 1));
            } else {
                System.out.println(Thread.currentThread().getName() + " 库存不足！");
            }
        } finally {
            writeLock.unlock();
        }
    }

}
