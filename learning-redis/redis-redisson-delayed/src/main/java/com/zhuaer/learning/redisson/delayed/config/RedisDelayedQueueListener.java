package com.zhuaer.learning.redisson.delayed.config;

/**
 * @ClassName RedisDelayedQueueListener
 * @Description 队列事件监听接口，需要实现这个方法
 * @Author zhua
 * @Date 2020/8/21 15:39
 * @Version 1.0
 */
public interface RedisDelayedQueueListener <T> {
    /**
     * 执行方法
     *
     * @param t
     */
    void invoke(T t);
}
