package com.albert.learning.lock;


import com.albert.learning.lock.service.BankService;
import com.albert.learning.redis.BasicMethod;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/test/lock")
public class LockController {

    @Autowired
    private BankService bankService;

    @Autowired
    private RedisTemplate<String,Object> redisTemplate;

    @GetMapping("/setredis")
    public String setredis() {
        Account a = new Account("A", 10000);
        Account b = new Account("B", 10000);
        redisTemplate.opsForValue().set("account:A",a);
        redisTemplate.opsForValue().set("account:B",b);
        return "OK";
    }

    @GetMapping("/balance")
    public Map<String, Object> balance() {
        Map<String, Object> map = new HashMap<>();
        map.put("A", ((Account) redisTemplate.opsForValue().get("account:A")).getBalance());
        map.put("B", ((Account) redisTemplate.opsForValue().get("account:B")).getBalance());
        return map;
    }
    @GetMapping("/test")
    public String doTest(@RequestParam String from,
                       @RequestParam String to,
                       @RequestParam int amount,
                       @RequestParam int type) throws InterruptedException {

        if(type==1){
            bankService.transfer(from,to,amount);
        }else{
            bankService.transferWithLockForMonolithB(from,to,amount);
        }
        return "success";
    }
    @GetMapping("/test5")
    public void doTest5() throws InterruptedException {
        long start = System.currentTimeMillis(); // 开始时间

        Account a = new Account("A", 10000);
        Account b = new Account("B", 10000);

        Runnable task = () -> bankService.transferWithLockForDistributed(a,b,1);

        // 模拟1000个线程同时转账
        Thread[] threads = new Thread[10000];
        for (int i = 0; i < 10000; i++) {
            threads[i] = new Thread(task);
            threads[i].start();
        }
        for (Thread t : threads) t.join();

        System.out.println("A余额：" + a.getBalance());
        System.out.println("B余额：" + b.getBalance());
        System.out.println("总余额：" + (a.getBalance() + b.getBalance()));

        long end = System.currentTimeMillis(); // 结束时间
        System.out.println("耗时：" + (end - start) + " ms");

    }

}
