package com.zhuaer.learning.mq.active.handler;

import org.springframework.jms.core.JmsMessagingTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.jms.Queue;
import javax.jms.Topic;

/**
 * @ClassName ActiveMqProducer
 * @Description TODO
 * @Author zhua
 * @Date 2020/11/20 15:00
 * @Version 1.0
 */
@Component
public class ActiveMqProducer {

    @Resource
    private JmsMessagingTemplate jmsMessagingTemplate;
    @Resource
    private Queue queue;

    @Resource
    private Topic topic;

    public void sendTopic(String msg) {
        System.out.println("发送Topic消息内容 :"+msg);
        this.jmsMessagingTemplate.convertAndSend(this.topic, msg);
    }

    public void sendMsg(String msg) {
        System.out.println("发送消息内容 :" + msg);
        this.jmsMessagingTemplate.convertAndSend(this.queue, msg);
    }
}
