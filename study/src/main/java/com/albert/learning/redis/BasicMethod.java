package com.albert.learning.redis;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Service
public class BasicMethod {
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /*第一部分：基础功能*/
    //String应用：用户登录缓存记录、取出
    public void saveLoginToken(String userId,String token){
        redisTemplate.opsForValue().set("login:token:"+userId,token,30, TimeUnit.MINUTES);
    }
    public String getLoginToken(String userId){
        return (String)redisTemplate.opsForValue().get("login:token:"+userId);
    };
    //Hash应用：存商品详情.适合存储结构化对象，如用户、商品、配置等。
    public void cacheProductInfo(String productId, Map<String,Object> info){
        redisTemplate.opsForHash().putAll("product:"+productId,info);
    }

    public Map<Object,Object> getProductInfo(String productId){
        return redisTemplate.opsForHash().entries("product:"+productId);
    }

    //List应用: 购物车.适合队列、栈、时间线、消息列表等。
    public void addCart(String userId, String productId) {
        redisTemplate.opsForList().leftPush("cart:" + userId, productId);
    }
    public List<Object> getCart(String userId) {
        return redisTemplate.opsForList().range("cart:" + userId, 0, -1);
    }
    //Set应用:商品标签.适合好友关系、标签集合、权限标识等。
    public void addProductTags(String productId, String... tags) {
        redisTemplate.opsForSet().add("product:tags:" + productId, (Object[]) tags);
    }

    public Set<Object> getProductTags(String productId) {
        return redisTemplate.opsForSet().members("product:tags:" + productId);
    }
    //Zset应用：商品热度排行榜（ZSet）.用于排行榜、延迟队列、打分排序。
    public void incrementProductScore(String productId) {
        redisTemplate.opsForZSet().incrementScore("product:rank", productId, 1);
    }

    public Set<Object> getTopProducts(int topN) {
        return redisTemplate.opsForZSet().reverseRange("product:rank", 0, topN - 1);
    }
    /*第二部分：进阶功能*/
    //应用：分布式锁（基于 setnx）场景：防止重复下单或超卖。
}
