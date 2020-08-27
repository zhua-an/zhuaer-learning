package com.zhuaer.learning.mq.rabbit.config;

import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @ClassName TopicRabbitMqConfig
 * @Description Topic 简单配置
 * @Author zhua
 * @Date 2020/8/11 15:06
 * @Version 1.0
 */
@Configuration
public class TopicRabbitMqConfig {
    public static final String TOPIC_EXCHANGE_NAME = "topic_exchange";
    public static final String TOPIC_QUEUE_NAME = "topic_queue";

    /**
     * 创建 交换机
     * @return
     */
    @Bean
    public Exchange itemTopicExchange(){
        return ExchangeBuilder.topicExchange(TOPIC_EXCHANGE_NAME).build();
    }

    /**
     * 创建 队列
     * @return
     */
    @Bean
    public Queue itemQueue(){
        return QueueBuilder.durable(TOPIC_QUEUE_NAME).build();
    }

    /**
     * 绑定 交换机与队列
     * @param exchange
     * @param queue
     * @return
     */
    @Bean
    public Binding itemQueueExchange(@Qualifier("itemTopicExchange") Exchange exchange, @Qualifier("itemQueue") Queue queue){
        return BindingBuilder.bind(queue).to(exchange).with("item.#").noargs();
    }
}
