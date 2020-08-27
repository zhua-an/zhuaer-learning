package com.zhuaer.learning.redis.container;

import com.alibaba.fastjson.JSON;
import com.zhuaer.learning.redis.bean.Job;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

/**
 * @ClassName JobPool
 * @Description job任务池
 * @Author zhua
 * @Date 2020/8/21 16:57
 * @Version 1.0
 */
@Component
@Slf4j
public class JobPool {

    @Autowired
    private RedisTemplate redisTemplate;

    private String NAME = "job.pool";

    private BoundHashOperations getPool () {
        BoundHashOperations ops = redisTemplate.boundHashOps(NAME);
        return ops;
    }

    /**
     * 添加任务
     * @param job
     */
    public void addJob (Job job) {
        log.info("任务池添加任务：{}", JSON.toJSONString(job));
        getPool().put(job.getId(),job);
        return ;
    }

    /**
     * 获得任务
     * @param jobId
     * @return
     */
    public Job getJob(Long jobId) {
        Object o = getPool().get(jobId);
        if (o instanceof Job) {
            return (Job) o;
        }
        return null;
    }

    /**
     * 移除任务
     * @param jobId
     */
    public void removeDelayJob (Long jobId) {
        log.info("任务池移除任务：{}",jobId);
        // 移除任务
        getPool().delete(jobId);
    }
}
