package com.zhuaer.learning.redisson.delayed.config;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBlockingQueue;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.util.Map;


/**
 * @ClassName RedisDelayedQueueInit
 * @Description 初始化队列监听
 * @Author zhua
 * @Date 2020/8/21 15:38
 * @Version 1.0
 */
@Component
@Slf4j
public class RedisDelayedQueueInit implements ApplicationContextAware {

    @Autowired
    RedissonClient redissonClient;

    /**
     * 获取应用上下文并获取相应的接口实现类
     *
     * @param applicationContext
     * @throws BeansException
     */
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        Map<String, RedisDelayedQueueListener> map = applicationContext.getBeansOfType(RedisDelayedQueueListener.class);
        map.entrySet().stream().forEach(taskEventListenerEntry->{
            String listenerName = taskEventListenerEntry.getValue().getClass().getName();
            startThread(listenerName, taskEventListenerEntry.getValue());
        });
    }

    /**
     * 启动线程获取队列*
     *
     * @param queueName                 queueName
     * @param redisDelayedQueueListener 任务回调监听
     * @param <T>                       泛型
     * @return
     */
    private <T> void startThread(String queueName, RedisDelayedQueueListener redisDelayedQueueListener) {
        RBlockingQueue<T> blockingFairQueue = redissonClient.getBlockingQueue(queueName);
        //由于此线程需要常驻，可以新建线程，不用交给线程池管理
        Thread thread = new Thread(() -> {
            log.info("启动监听队列线程" + queueName);
            while (true) {
                try {
                    T t = blockingFairQueue.take();
                    log.info("监听队列线程{},获取到值:{}", queueName, JSON.toJSONString(t));
                    new Thread(() -> redisDelayedQueueListener.invoke(t)).start();
                } catch (Exception e) {
                    log.info("监听队列线程错误,", e);
                    try {
                        Thread.sleep(10000);
                    } catch (InterruptedException ex) {
                        log.error("线程出现异常,", ex);
                    }
                }
            }
        });
        thread.setName(queueName);
        thread.start();
    }

}
