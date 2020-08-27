package com.zhuaer.redis.learning.redisson.boot.controller;

import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.xml.ws.Response;

/**
 * @ClassName TestController
 * @Description TODO
 * @Author zhua
 * @Date 2020/7/20 12:07
 * @Version 1.0
 */
@Slf4j
@RestController
public class TestController {

    @Autowired
    RedissonClient redissonClient;

    @RequestMapping(value = "test", method = RequestMethod.GET)
    public String test() {
        RLock rLock = redissonClient.getLock("test1");
        rLock.lock();
        try {
            Thread.sleep(1000l);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (log.isDebugEnabled()) {
            log.debug("test welcome.........");
        }
        rLock.unlock();
        return "响应成功";
    }
}
