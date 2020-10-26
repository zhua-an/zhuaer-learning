package com.zhuaer.learning.redisson.delayed.config2;

import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RMapCache;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * @ClassName RedisDelayedQueue
 * @Description 监听类
 * @Author zhua
 * @Date 2020/10/22 19:12
 * @Version 1.0
 */
@Slf4j
@Component
public class RedisDelayedQueue2 {

    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private MessageListener messageListener;

    public void redissonDelay(String num, String msg, Long miute) {
        //设置缓存映射的标识 这个标识自定义
        RMapCache<String, String> rMapCache = redissonClient.getMapCache("redisMessage");
        //设置缓存的时间和数据信息 TimeUnit.MILLISECONDS这个是 设置时间单位
//        rMapCache.put(num, msg, miute, TimeUnit.MILLISECONDS);
        rMapCache.put(num, msg, miute, TimeUnit.SECONDS);
        //监听方法触发类 redis过期就执行这个 messageListene类里的方法
        rMapCache.addListener(messageListener);
    }
}
