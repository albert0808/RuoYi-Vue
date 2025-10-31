package com.albert.learning.lock.distributedLock;

import com.albert.learning.lock.manager.LockManager;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Component
@Primary
public class RedisLockManager implements LockManager {
    @Autowired
    private RedissonClient redissonClient;

    @Override
    public RLock getLock(String key) {
        return redissonClient.getLock("lock:"+key);
    }
}
