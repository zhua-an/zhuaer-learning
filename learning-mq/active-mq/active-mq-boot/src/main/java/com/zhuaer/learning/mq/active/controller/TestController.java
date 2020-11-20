package com.zhuaer.learning.mq.active.controller;

import com.zhuaer.learning.mq.active.handler.ActiveMqProducer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @ClassName TestController
 * @Description TODO
 * @Author zhua
 * @Date 2020/11/20 15:15
 * @Version 1.0
 */
@RestController
public class TestController {

    @Autowired
    private ActiveMqProducer queryProducer;

    /**
     * 发送字符串消息
     */
    @RequestMapping("/sendMessage")
    public void sendMessage() {
        queryProducer.sendMsg("提现200.00元");
    }


    /**
     * 发送字符串消息
     */
    @RequestMapping("/sendTopic")
    public void sendTopic() {
        queryProducer.sendTopic("提现200.00元");
    }
}
