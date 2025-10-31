package com.albert.learning.lock.manager;

public interface LockManager {
    Object getLock(String key);
}
