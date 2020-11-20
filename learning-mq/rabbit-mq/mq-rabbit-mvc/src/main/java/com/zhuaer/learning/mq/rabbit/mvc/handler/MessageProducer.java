package com.zhuaer.learning.mq.rabbit.mvc.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @ClassName MessageProducer
 * @Description TODO
 * @Author zhua
 * @Date 2020/8/17 19:06
 * @Version 1.0
 */
@RestController
public class MessageProducer {

    private Logger logger = LoggerFactory.getLogger(MessageProducer.class);

    @Autowired
//    private AmqpTemplate amqpTemplate;
    private RabbitTemplate rabbitTemplate;

    @RequestMapping("sendMessage")
    public void sendMessage(String message) {
        logger.info("to send message:{}", message);
        System.out.println("to send message:" + message);
//        amqpTemplate.convertAndSend("queuekey", message);
        rabbitTemplate.convertAndSend("queuekey", message);

    }
}
