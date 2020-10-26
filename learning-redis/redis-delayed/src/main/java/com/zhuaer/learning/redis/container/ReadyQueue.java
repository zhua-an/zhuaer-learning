package com.zhuaer.learning.redis.container;

import com.alibaba.fastjson.JSON;
import com.zhuaer.learning.redis.config.DelayJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

/**
 * @ClassName ReadyQueue
 * @Description 待完成任务
 * @Author zhua
 * @Date 2020/8/21 16:59
 * @Version 1.0
 */
@Component
@Slf4j
public class ReadyQueue {

    @Autowired
    private RedisTemplate redisTemplate;

    private String NAME = "process.queue";

    private String getKey(String topic) {
        return NAME + topic;
    }

    /**
     * 获得队列
     * @param topic
     * @return
     */
    private BoundListOperations getQueue (String topic) {
        BoundListOperations ops = redisTemplate.boundListOps(getKey(topic));
        return ops;
    }

    /**
     * 设置任务
     * @param delayJob
     */
    public void pushJob(DelayJob delayJob) {
        log.info("执行队列添加任务:{}",delayJob);
        BoundListOperations listOperations = getQueue(delayJob.getTopic());
        listOperations.leftPush(delayJob);
    }

    /**
     * 移除并获得任务
     * @param topic
     * @return
     */
    public DelayJob popJob(String topic) {
        BoundListOperations listOperations = getQueue(topic);
        Object o = listOperations.leftPop();
        if (o instanceof DelayJob) {
            log.info("执行队列取出任务:{}", JSON.toJSONString((DelayJob) o));
            return (DelayJob) o;
        }
        return null;
    }
}
