package com.zhuaer.learning.mq.rabbit.handler;

import com.zhuaer.learning.mq.rabbit.config.DelayTypeEnum;
import com.zhuaer.learning.mq.rabbit.config.RabbitMQConfig;
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
public class DelayMessageSender {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    public void sendMsg(String msg, DelayTypeEnum type){
        switch (type){
            case DELAY_10s:
                rabbitTemplate.convertAndSend(RabbitMQConfig.DELAY_EXCHANGE_NAME, RabbitMQConfig.DELAY_QUEUEA_ROUTING_KEY, msg);
                break;
            case DELAY_60s:
                rabbitTemplate.convertAndSend(RabbitMQConfig.DELAY_EXCHANGE_NAME, RabbitMQConfig.DELAY_QUEUEB_ROUTING_KEY, msg);
                break;
        }
    }
}
