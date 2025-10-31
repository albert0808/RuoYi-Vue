package com.albert.learning.lock.service;

import com.albert.learning.lock.Account;
import com.albert.learning.lock.manager.LockManager;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * 转账方法
 * <p>分别包含不带锁的、单体应用适用锁、分布式适用的锁的方法</p>
 */
@Service
@Slf4j
public class BankService {
    //这里之所以又用@Qualifier，又用直接的lockManager，只是为了调试方便
    @Autowired
    @Qualifier("localLockManager")
    private LockManager localLockManager;

    @Autowired
    private LockManager lockManager;

    @Autowired
    private RedisTemplate<String,Object> redisTemplate;

    /*@Autowired
    private TransactionTemplate transactionTemplate;*/

    @Autowired
    private RedissonClient redissonClient;
    /**
     * 不带锁的版本
     * 经测试，一定概率下会有跑不完的情况
     * 10000次转账，耗时6-10秒
     * @param from
     * @param to
     * @param amount
     */
    @Transactional
    public void transferWithNoLock(Account from, Account to, int amount){
        if(from.getBalance()>=amount){
            from.setBalance(from.getBalance()-amount);
            to.setBalance(to.getBalance()+amount);
        }
        log.info("不加锁的一次转账行为结束，转账金额："+amount+",当前"+from.getName()+"余额："+from.getBalance()+",当前账户"+to.getName()+"余额："+to.getBalance()+";");
    }

    /**
     * 带锁版本（单体应用）
     * @param from
     * @param to
     * @param amount
     */
    @Transactional
    public void transferWithLockForMonolith(Account from, Account to, int amount){
        //比较重量的加锁方式
        //synchronizedThis(from,to,amount);
        //比较轻量的加锁方式
        synchronizedTwo(from,to,amount);
    }

    @Transactional
    public void transferWithLockForMonolithB(String from, String to, int amount){
        Object lockA = localLockManager.getLock(from);
        Object lockB = localLockManager.getLock(to);



        //之所以要排序，是因为防止死锁。如果两个线程同时到，一个拿了lockA 一个拿了 lockB，再去申请另一个，会死锁
        Object firstLock = from.compareTo(to) < 0 ? lockA : lockB;
        Object secondLock = firstLock == lockA ? lockB : lockA;

        synchronized (firstLock){
            synchronized(secondLock){
                Account fromAcc = (Account)localLockManager.getLock(from);
                Account toAcc = (Account)localLockManager.getLock(to);
                if(fromAcc.getBalance()>=amount){
                    fromAcc.setBalance(fromAcc.getBalance()-amount);
                    toAcc.setBalance(toAcc.getBalance()+amount);

                    // 回写 Redis
                    redisTemplate.opsForValue().set("account:" + from, fromAcc.getBalance());
                    redisTemplate.opsForValue().set("account:" + to, toAcc.getBalance());
                }
                log.info("加锁的一次转账行为结束，转账金额："+amount+",当前"+from+"余额："+fromAcc.getBalance()+",当前账户"+to+"余额："+toAcc.getBalance()+";");
            }
        }
    }


    /**
     * 10000次 1-10秒 不会失败，按预期运行
     * @param from
     * @param to
     * @param amount
     */
    void synchronizedThis(Account from, Account to, int amount){
        // this指的是当前BankService对象的实例
        // 如果账户金额的增减只发生在这里，直接锁住这个BankService即可
        // 如果阻塞式的锁住这个对象，会降低效率
        synchronized (this){
            if(from.getBalance()>=amount){
                from.setBalance(from.getBalance()-amount);
                to.setBalance(to.getBalance()+amount);
            }
            log.info("加锁的一次转账行为结束，转账金额："+amount+",当前"+from.getName()+"余额："+from.getBalance()+",当前账户"+to.getName()+"余额："+to.getBalance()+";");
        }
    }

    /**
     * 10000次 1-10秒 不会失败，按预期运行
     * @param from
     * @param to
     * @param amount
     */
    void synchronizedTwo(Account from, Account to, int amount){
        Object lockA = localLockManager.getLock(from.getName());
        Object lockB = localLockManager.getLock(to.getName());



        //之所以要排序，是因为防止死锁。如果两个线程同时到，一个拿了lockA 一个拿了 lockB，再去申请另一个，会死锁
        Object firstLock = from.getName().compareTo(to.getName()) < 0 ? lockA : lockB;
        Object secondLock = firstLock == lockA ? lockB : lockA;

        synchronized (firstLock){
            synchronized(secondLock){
                if(from.getBalance()>=amount){
                    from.setBalance(from.getBalance()-amount);
                    to.setBalance(to.getBalance()+amount);
                }
                log.info("加锁的一次转账行为结束，转账金额："+amount+",当前"+from.getName()+"余额："+from.getBalance()+",当前账户"+to.getName()+"余额："+to.getBalance()+";");
            }
        }
    }

    /**
     * 带锁版本（分布式应用）
     * 在分布式中
     * @param from
     * @param to
     * @param amount
     */
    public void transfer(String from, String to, int amount){

        RLock lockA = (RLock)lockManager.getLock("lock:"+from);
        RLock lockB = (RLock)lockManager.getLock("lock:"+to);

        //之所以要排序，是因为防止死锁。如果两个线程同时到，一个拿了lockA 一个拿了 lockB，再去申请另一个，会死锁
        RLock firstLock = from.compareTo(to) < 0 ? lockA : lockB;
        RLock secondLock = firstLock == lockA ? lockB : lockA;

        try{
            firstLock.lock();
            secondLock.lock();
            Account fromAcc = (Account) redisTemplate.opsForValue().get("account:" + from);
            Account toAcc = (Account) redisTemplate.opsForValue().get("account:" + to);
            if(fromAcc.getBalance()>=amount){
                fromAcc.setBalance(fromAcc.getBalance()-amount);
                toAcc.setBalance(toAcc.getBalance()+amount);

                // 回写 Redis
                redisTemplate.opsForValue().set("account:" + from, fromAcc.getBalance());
                redisTemplate.opsForValue().set("account:" + to, toAcc.getBalance());
            }
            log.info("加锁的一次转账行为结束，转账金额："+amount+",当前"+fromAcc.getName()+"余额："+fromAcc.getBalance()+",当前账户"+toAcc.getName()+"余额："+toAcc.getBalance()+";");

        }finally {
            if (secondLock.isHeldByCurrentThread()) {
                secondLock.unlock();
            }
            if (firstLock.isHeldByCurrentThread()) {
                firstLock.unlock();
            }
        }
        //或者直接使用联合锁
        /*RLock multiLock = redissonClient.getMultiLock(lockA, lockB);
        try{
            multiLock.lock();
            if(from.getBalance()>=amount){
                from.setBalance(from.getBalance()-amount);
                to.setBalance(to.getBalance()+amount);
            }
            log.info("加锁的一次转账行为结束，转账金额："+amount+",当前"+from.getName()+"余额："+from.getBalance()+",当前账户"+to.getName()+"余额："+to.getBalance()+";");

        }finally {
            if(multiLock.isHeldByCurrentThread()){
                multiLock.unlock();
            }
        }*/
    }

    public void transferWithLockForDistributed(Account from, Account to, int amount){
        RLock lockA = (RLock)lockManager.getLock(from.getName());
        RLock lockB = (RLock)lockManager.getLock(to.getName());

        //之所以要排序，是因为防止死锁。如果两个线程同时到，一个拿了lockA 一个拿了 lockB，再去申请另一个，会死锁
        RLock firstLock = from.getName().compareTo(to.getName()) < 0 ? lockA : lockB;
        RLock secondLock = firstLock == lockA ? lockB : lockA;

        try{
            firstLock.lock();
            secondLock.lock();
            if(from.getBalance()>=amount){
                from.setBalance(from.getBalance()-amount);
                to.setBalance(to.getBalance()+amount);
            }
            log.info("加锁的一次转账行为结束，转账金额："+amount+",当前"+from.getName()+"余额："+from.getBalance()+",当前账户"+to.getName()+"余额："+to.getBalance()+";");

        }finally {
            if (secondLock.isHeldByCurrentThread()) {
                secondLock.unlock();
            }
            if (firstLock.isHeldByCurrentThread()) {
                firstLock.unlock();
            }
        }
        //或者直接使用联合锁
        /*RLock multiLock = redissonClient.getMultiLock(lockA, lockB);
        try{
            multiLock.lock();
            if(from.getBalance()>=amount){
                from.setBalance(from.getBalance()-amount);
                to.setBalance(to.getBalance()+amount);
            }
            log.info("加锁的一次转账行为结束，转账金额："+amount+",当前"+from.getName()+"余额："+from.getBalance()+",当前账户"+to.getName()+"余额："+to.getBalance()+";");

        }finally {
            if(multiLock.isHeldByCurrentThread()){
                multiLock.unlock();
            }
        }*/
    }
}
