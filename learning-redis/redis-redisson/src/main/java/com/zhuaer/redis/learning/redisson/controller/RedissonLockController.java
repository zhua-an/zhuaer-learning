package com.zhuaer.redis.learning.redisson.controller;

import com.zhuaer.redis.learning.redisson.util.RedissLockUtil;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @ClassName RedissonLockController
 * @Description TODO
 * @Author zhua
 * @Date 2020/7/17 19:14
 * @Version 1.0
 */
@RestController
public class RedissonLockController {

    static final String KEY = "LOCK_KEY";

    @GetMapping("/test")
    public Object test(){
        //加锁
        RedissLockUtil.lock(KEY);
        try {
            //TODO 处理业务
            System.out.println(" 处理业务。。。");
            Thread.sleep(1000);
        } catch (Exception e) {
            //异常处理
        }finally{
            //释放锁
            RedissLockUtil.unlock(KEY);
        }

        return "SUCCESS";
    }
}
