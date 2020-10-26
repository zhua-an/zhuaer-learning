package com.zhuaer.learning.redis.controller;

import com.alibaba.fastjson.JSON;
import com.zhuaer.learning.redis.config.DelayJob;
import com.zhuaer.learning.redis.config.Job;
import com.zhuaer.learning.redis.service.JobService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * @ClassName DelayController
 * @Description 测试
 * @Author zhua
 * @Date 2020/8/21 17:08
 * @Version 1.0
 */
@RestController
@RequestMapping("delay")
public class DelayController {

    @Autowired
    private JobService jobService;
    /**
     * 添加
     * @param request
     * @return
     */
    @RequestMapping(value = "add",method = RequestMethod.POST)
    public String addDefJob(Job request) {
        DelayJob delayJob = jobService.addDefJob(request);
        return JSON.toJSONString(delayJob);
    }

    /**
     * 获取
     * @return
     */
    @RequestMapping(value = "pop",method = RequestMethod.GET)
    public String getProcessJob(String topic) {
        Job process = jobService.getProcessJob(topic);
        return JSON.toJSONString(process);
    }

    /**
     * 完成一个执行的任务
     * @param jobId
     * @return
     */
    @RequestMapping(value = "finish",method = RequestMethod.DELETE)
    public String finishJob(Long jobId) {
        jobService.finishJob(jobId);
        return "success";
    }

    /**
     * 任务的删除/消费
     * @param jobId
     * @return
     */
    @RequestMapping(value = "delete",method = RequestMethod.DELETE)
    public String deleteJob(Long jobId) {
        jobService.deleteJob(jobId);
        return "success";
    }
}
