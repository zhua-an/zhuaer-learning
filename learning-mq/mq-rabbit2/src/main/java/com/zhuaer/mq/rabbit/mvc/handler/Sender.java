package com.zhuaer.mq.rabbit.mvc.handler;

import com.zhuaer.mq.rabbit.mvc.config.RabbitmqConfig;
import com.zhuaer.mq.rabbit.mvc.entity.Persion;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * @ClassName Sender
 * @Description TODO
 * @Author zhua
 * @Date 2020/8/17 14:56
 * @Version 1.0
 */
@Component
public class Sender {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    public void send() {
        for (int i = 0; i <20 ; i++) {
            this.rabbitTemplate.convertAndSend(RabbitmqConfig.EXCHANGE_NAME ,RabbitmqConfig.ROUTING_KEY, new Persion("zhangsn"+i,new Date()));
        }
    }
}
