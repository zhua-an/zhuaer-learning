package com.zhuaer.learning.mq.rabbit.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Date;
import java.util.UUID;

/**
 * @ClassName HelloSender
 * @Description 生产者
 * @Author zhua
 * @Date 2020/8/21 16:02
 * @Version 1.0
 */
@Slf4j
@Controller
public class HelloSender implements RabbitTemplate.ConfirmCallback,RabbitTemplate.ReturnCallback{

    private RabbitTemplate rabbitTemplate;

    //构造方法注入
    @Autowired
    public HelloSender(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
        //这是是设置回调能收到发送到响应
        rabbitTemplate.setConfirmCallback(this);
        //如果设置备份队列则不起作用
        rabbitTemplate.setMandatory(true);
        rabbitTemplate.setReturnCallback(this);
    }

    @RequestMapping("/send")
    @ResponseBody
    public void send() {
        CorrelationData correlationId = new CorrelationData(UUID.randomUUID().toString());
        String sendMsg = "hello1 " + new Date();
        System.out.println("Sender : " + sendMsg);
        //convertAndSend(exchange:交换机名称,routingKey:路由关键字,object:发送的消息内容,correlationData:消息ID)
        rabbitTemplate.convertAndSend("exchange.hello","helloKey", sendMsg,correlationId);
    }


    //回调确认
    @Override
    public void confirm(CorrelationData correlationData, boolean ack, String cause) {
        if(ack){
            log.info("消息发送成功:correlationData({}),ack({}),cause({})",correlationData,ack,cause);
        }else{
            log.info("消息发送失败:correlationData({}),ack({}),cause({})",correlationData,ack,cause);
        }
    }

    //消息发送到转换器的时候没有对列,配置了备份对列该回调则不生效
    @Override
    public void returnedMessage(Message message, int replyCode, String replyText, String exchange, String routingKey) {
        log.info("消息丢失:exchange({}),route({}),replyCode({}),replyText({}),message:{}",exchange,routingKey,replyCode,replyText,message);
    }
}
