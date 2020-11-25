package com.zhuaer.learning.mq.active.config;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.command.ActiveMQQueue;
import org.apache.activemq.command.ActiveMQTopic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.config.JmsListenerContainerFactory;

import javax.jms.ConnectionFactory;
import javax.jms.Queue;
import javax.jms.Topic;

/**
 * @ClassName ActiveMqConfig
 * @Description TODO
 * @Author zhua
 * @Date 2020/11/20 14:45
 * @Version 1.0
 */
@Configuration
public class ActiveMqConfig {

//    /**
//     * activeMQ 远程服务连接
//     * @return
//     */
//    @ConditionalOnMissingBean(ConnectionFactory.class)
//    @Bean
//    public ConnectionFactory connectionFactory(){
//        return new ActiveMQConnectionFactory("username", "password", "brokerUrl");
//    }

    /**
     * 队列模式监听工厂
     * @param connectionFactory
     * @return
     */
    @Bean("queueListenerFactory")
    public JmsListenerContainerFactory<?> queueListenerFactory(ConnectionFactory connectionFactory) {
        DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setPubSubDomain(false);
        return factory;
    }

    /**
     * 广播订阅模式监听工厂
     * @param connectionFactory
     * @return
     */
    @Bean("topicListenerFactory")
    public JmsListenerContainerFactory<?> topicListenerFactory(ConnectionFactory connectionFactory) {
        DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        //设置为发布订阅方式, 默认情况下使用的生产消费者方式
        //这里必须设置为true，false则表示是queue类型
        factory.setPubSubDomain(true);
        return factory;
    }

    /**
     * 点对点模式队列
     * @return
     */
    @Bean
    public Queue queue() {
        return new ActiveMQQueue("sms.queue");
    }

    /**
     * 发布/订阅模式
     * @return
     */
    @Bean
    public Topic topic() {
        return new ActiveMQTopic("sms.topic");
    }

}
