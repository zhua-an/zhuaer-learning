package com.zhuaer.learning.mq.rabbit.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

/**
 * @ClassName TopicRabbitConfig
 * @Description RabbitMQ 简单配置
 * @Author zhua
 * @Date 2020/8/19 12:01
 * @Version 1.0
 */
@Configuration
public class SimpleRabbitMqConfig {

    final static String message = "topic.message";
    final static String messages = "topic.messages";

    /**
     * 队列
     * @return
     */
    @Bean
    public Queue queueMessage() {
        // durable:是否持久化,默认是false,持久化队列：会被存储在磁盘上，当消息代理重启时仍然存在，暂存队列：当前连接有效
        // exclusive:默认也是false，只能被当前创建的连接使用，而且当连接关闭后队列即被删除。此参考优先级高于durable
        // autoDelete:是否自动删除，当没有生产者或者消费者使用此队列，该队列会自动删除。
        //   return new Queue(SimpleRabbitMqConfig.message,true,true,false);

        return new Queue(SimpleRabbitMqConfig.message);
    }

    @Bean
    public Queue queueMessages() {
        return new Queue(SimpleRabbitMqConfig.messages);
    }

    /**
     * topic交换机
     * @return
     */
    @Bean
    TopicExchange topicExchange() {
        return new TopicExchange("topicExchange.hello");
    }

    /**
     * direct交换机
     * @return
     */
    @Bean
    DirectExchange directExchange() {
        return new DirectExchange("directExchange.hello",true,false);
    }

    /**
     * 绑定  将队列和交换机绑定, 并设置用于匹配键
     * @param queueMessage
     * @param topicExchange
     * @return
     */
    @Bean
    Binding bindingExchangeMessage(Queue queueMessage, TopicExchange topicExchange) {
        return BindingBuilder.bind(queueMessage).to(topicExchange).with("topic.message");
    }

    /**
     * 绑定  将队列和交换机绑定, 并设置用于匹配键
     * @param queueMessages
     * @param topicExchange
     * @return
     */
    @Bean
    Binding bindingExchangeMessages(Queue queueMessages, TopicExchange topicExchange) {
        return BindingBuilder.bind(queueMessages).to(topicExchange).with("topic.#");
    }

    /**
     * 如果需要在生产者需要消息发送后的回调，
     * 需要对rabbitTemplate设置ConfirmCallback对象，
     * 由于不同的生产者需要对应不同的ConfirmCallback，
     * 如果rabbitTemplate设置为单例bean，
     * 则所有的rabbitTemplate实际的ConfirmCallback为最后一次申明的ConfirmCallback。
     * @return
     */
    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public RabbitTemplate rabbitTemplate(CachingConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        return template;
    }

}
