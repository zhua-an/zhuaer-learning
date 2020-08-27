package com.zhuaer.learning.mq.rabbit.config;

import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;

import java.util.HashMap;
import java.util.Map;

/**
 * @ClassName RabbitMQConfig2
 * @Description TODO
 * @Author zhua
 * @Date 2020/8/20 14:30
 * @Version 1.0
 */
public class RabbitMQConfig2 {

    public static final String DELAY_EXCHANGE_NAME = "delay.queue.demo.business.exchange";
    public static final String DELAY_QUEUEC_NAME = "delay.queue.demo.business.queuec";
    public static final String DELAY_QUEUEC_ROUTING_KEY = "delay.queue.demo.business.queuec.routingkey";
    public static final String DEAD_LETTER_EXCHANGE = "delay.queue.demo.deadletter.exchange";
    public static final String DEAD_LETTER_QUEUEC_ROUTING_KEY = "delay.queue.demo.deadletter.delay_anytime.routingkey";
    public static final String DEAD_LETTER_QUEUEC_NAME = "delay.queue.demo.deadletter.queuec";

    // 声明延时Exchange
    @Bean("delayExchange")
    public DirectExchange delayExchange(){
        return new DirectExchange(DELAY_EXCHANGE_NAME);
    }

    // 声明死信Exchange
    @Bean("deadLetterExchange")
    public DirectExchange deadLetterExchange(){
        return new DirectExchange(DEAD_LETTER_EXCHANGE);
    }

    // 声明延时队列C 不设置TTL
    // 并绑定到对应的死信交换机
    @Bean("delayQueueC")
    public Queue delayQueueC(){
        Map<String, Object> args = new HashMap<>(3);
        // x-dead-letter-exchange    这里声明当前队列绑定的死信交换机
        args.put("x-dead-letter-exchange", DEAD_LETTER_EXCHANGE);
        // x-dead-letter-routing-key  这里声明当前队列的死信路由key
        args.put("x-dead-letter-routing-key", DEAD_LETTER_QUEUEC_ROUTING_KEY);
        return QueueBuilder.durable(DELAY_QUEUEC_NAME).withArguments(args).build();
    }

    // 声明死信队列C 用于接收延时任意时长处理的消息
    @Bean("deadLetterQueueC")
    public Queue deadLetterQueueC(){
        return new Queue(DEAD_LETTER_QUEUEC_NAME);
    }

    // 声明延时列C绑定关系
    @Bean
    public Binding delayBindingC(@Qualifier("delayQueueC") Queue queue,
                                 @Qualifier("delayExchange") DirectExchange exchange){
        return BindingBuilder.bind(queue).to(exchange).with(DELAY_QUEUEC_ROUTING_KEY);
    }

    // 声明死信队列C绑定关系
    @Bean
    public Binding deadLetterBindingC(@Qualifier("deadLetterQueueC") Queue queue,
                                      @Qualifier("deadLetterExchange") DirectExchange exchange){
        return BindingBuilder.bind(queue).to(exchange).with(DEAD_LETTER_QUEUEC_ROUTING_KEY);
    }
}
