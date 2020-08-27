package com.zhuaer.learning.mq.rabbit.mvc.handler;


import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.UUID;

/**
 * @ClassName RmqProducer
 * @Description TODO
 * @Author zhua
 * @Date 2020/8/18 19:23
 * @Version 1.0
 */
public class RmqProducer implements RabbitTemplate.ConfirmCallback, RabbitTemplate.ReturnCallback {

    private RabbitTemplate template;

    @Autowired
    public RmqProducer(RabbitTemplate template){
        this.template = template;
    }

    public void sendMessage(String content) {
        try {
            CorrelationData correlationId = new CorrelationData(UUID.randomUUID().toString());
            template.convertAndSend("spring-boot-exchange", "spring-boot-routingKey",content,correlationId);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void confirm(CorrelationData correlationData, boolean b, String s) {
        System.out.println(" 回调id:" + correlationData+"ack:"+s);
    }

    @Override
    public void returnedMessage(Message message, int replyCode, String replyText, String exchange, String routingKey) {
//        log.info("消息丢失:exchange({}),route({}),replyCode({}),replyText({}),message:{}",exchange,routingKey,replyCode,replyText,message);
    }
}
