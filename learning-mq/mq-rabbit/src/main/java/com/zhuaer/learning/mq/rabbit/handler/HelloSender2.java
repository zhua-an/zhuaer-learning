package com.zhuaer.learning.mq.rabbit.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.InitializingBean;
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
public class HelloSender2 implements InitializingBean {
    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Override
    public void afterPropertiesSet() throws Exception {
        rabbitTemplate.setConfirmCallback(new RabbitConfirmCallBack());
        rabbitTemplate.setReturnCallback(new RabbitReturnCallback());
    }

    @RequestMapping("/send")
    @ResponseBody
    public void send() {
        CorrelationData correlationId = new CorrelationData(UUID.randomUUID().toString());
        String sendMsg = "hello1 " + new Date();
        System.out.println("Sender : " + sendMsg);
        //convertAndSend(exchange:交换机名称,routingKey:路由关键字,object:发送的消息内容,correlationData:消息ID)
        rabbitTemplate.convertAndSend("exchange.hello","topic.message", sendMsg,correlationId);
    }

    /**
     * 发送确认
     */
    private class RabbitConfirmCallBack implements RabbitTemplate.ConfirmCallback {
        @Override
        public void confirm(CorrelationData correlationData, boolean ack, String cause) {
            if(ack){
                log.info("消息发送成功:correlationData({}),ack({}),cause({})",correlationData,ack,cause);
            }else{
                log.info("消息发送失败:correlationData({}),ack({}),cause({})",correlationData,ack,cause);
            }
        }
    }

    /**
     * 发送失败退回
     */
    private class RabbitReturnCallback implements RabbitTemplate.ReturnCallback {
        @Override
        public void returnedMessage(Message message, int replyCode, String replyText, String exchange, String routingKey) {
            log.info("消息丢失:exchange({}),route({}),replyCode({}),replyText({}),message:{}",exchange,routingKey,replyCode,replyText,message);
        }
    }
}
