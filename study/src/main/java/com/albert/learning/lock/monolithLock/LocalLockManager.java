package com.albert.learning.lock.monolithLock;

import com.albert.learning.lock.manager.LockManager;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;

@Component
public class LocalLockManager implements LockManager {
    private final ConcurrentHashMap<String,Object> locks = new ConcurrentHashMap<>();
    @Override
    public Object getLock(String key) {
        //“如果这个 key 没有锁对象，就创建一个新的 Object 放进去；否则返回已有对象。”
        return locks.computeIfAbsent(key,k -> new Object());
    }
}
