package com.zhuaer.learning.redis.controller;

import com.zhuaer.learning.redis.util.RedisLock;
import com.zhuaer.learning.redis.util.RedisUtil;
import lombok.SneakyThrows;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @ClassName RedisLockController
 * @Description TODO
 * @Author zhua
 * @Date 2020/7/17 17:24
 * @Version 1.0
 */
@RestController
@RequestMapping("/lock")
public class RedisLockController {

    @Resource
    private RedisUtil redisUtil;
    @Resource
    private RedisLock redisLock;


    @RequestMapping("lock")
    public String expire(String key){
        String LOCK_ID = "LOCK_ID";
        for(int i=0; i < 10; i++) {
            new Thread(new Runnable() {
                @SneakyThrows
                @Override
                public void run() {
                    boolean lock = redisUtil.getLock(LOCK_ID, 5 * 1000);
                    if (lock) {
                        System.out.println("执行任务");
                        redisUtil.del(LOCK_ID);
                    } else {
                        System.out.println("没有抢到锁");
                    }
                }
            }).start();

        }

        for(int i=0; i < 10; i++) {
            new Thread(new Runnable() {
                @SneakyThrows
                @Override
                public void run() {
                    String token = null;
                    try{
                        token = redisLock.lock("lock_name", 10000, 11000);
                        if(token != null) {
                            System.out.println("我拿到了锁哦");
                            // 执行业务代码
                        } else {
                            System.out.println("我没有拿到锁唉");
                        }
                    } finally {
                        if(token!=null) {
                            redisLock.unlock("lock_name", token);
                        }
                    }
                }
            }).start();

        }
        return "success";
    }

}
