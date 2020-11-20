package com.zhuaer.learning.mq.rabbit.handler;

import com.zhuaer.learning.mq.rabbit.config.RabbitMQConfig2;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @ClassName DelayMessageSender
 * @Description TODO
 * @Author zhua
 * @Date 2020/8/20 14:26
 * @Version 1.0
 */
@Component
public class DelayMessageSender2 {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    public void sendMsg(String msg, Integer delayTime){
        rabbitTemplate.convertAndSend(RabbitMQConfig2.DELAY_EXCHANGE_NAME, RabbitMQConfig2.DELAY_QUEUEC_ROUTING_KEY, msg, a ->{
            a.getMessageProperties().setDelay(delayTime);
            return a;
        });
    }
}
