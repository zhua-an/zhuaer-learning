package com.zhuaer.mq.rabbit.mvc.handler;

import com.alibaba.fastjson.JSONObject;
import com.rabbitmq.client.Channel;
import com.zhuaer.mq.rabbit.mvc.config.RabbitmqConfig;
import com.zhuaer.mq.rabbit.mvc.config.TextMessageConverter;
import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.amqp.rabbit.listener.api.ChannelAwareMessageListener;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @ClassName RecvHandler
 * @Description TODO
 * @Author zhua
 * @Date 2020/8/19 11:57
 * @Version 1.0
 */
@Configuration
public class RecvHandler {

    // 消费者 监听某个队列 方式一
    @Bean
    public SimpleMessageListenerContainer messageContainer(ConnectionFactory connectionFactory) {
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer(connectionFactory);
        container.setQueues(new Queue(RabbitmqConfig.QUEUE_NAME));
        container.setExposeListenerChannel(true);
        container.setMaxConcurrentConsumers(1);
        container.setConcurrentConsumers(1);
        // 设置确认模式手工确认
        container.setAcknowledgeMode(AcknowledgeMode.MANUAL);
        container.setMessageListener(new ChannelAwareMessageListener() {
            @Override
            public void onMessage(Message message, Channel channel) throws Exception {
                byte[] body = message.getBody();
                System.out.println("1  receive msg : " + JSONObject.parseObject(new String(body)));
                //不读取消息并且将当前消息抛弃掉，消息队列中删除当前消息
                //channel.basicReject(message.getMessageProperties().getDeliveryTag(), false);
                //不读取消息，消息队列中保留当前消息未被查看状态
                //channel.basicReject(message.getMessageProperties().getDeliveryTag(), true);

                //确认消息成功消费，删除消息队列中的消息
                // channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
                //确认消息成功消费，删除消息队列中的消息，他跟上面貌似一样
                channel.basicAck(message.getMessageProperties().getDeliveryTag(), true);
            }
        });
        return container;
    }

    @Bean
    public SimpleMessageListenerContainer messageContainer2(ConnectionFactory connectionFactory) {
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer(connectionFactory);
        container.setQueues(new Queue(RabbitmqConfig.QUEUE_NAME));
        container.setExposeListenerChannel(true);
        container.setMaxConcurrentConsumers(1);
        container.setConcurrentConsumers(1);
        // 设置确认模式手工确认
        container.setAcknowledgeMode(AcknowledgeMode.MANUAL);
        container.setMessageListener(new ChannelAwareMessageListener() {
            @Override
            public void onMessage(Message message, Channel channel) throws Exception {
                byte[] body = message.getBody();
                System.out.println("  2  receive msg : " + JSONObject.parseObject(new String(body)));
                //不读取消息并且将当前消息抛弃掉，消息队列中删除当前消息
                //channel.basicReject(message.getMessageProperties().getDeliveryTag(), false);
                //不读取消息，消息队列中保留当前消息未被查看状态
                //channel.basicReject(message.getMessageProperties().getDeliveryTag(), true);

                //确认消息成功消费，删除消息队列中的消息
                // channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
                //确认消息成功消费，删除消息队列中的消息，他跟上面貌似一样
                channel.basicAck(message.getMessageProperties().getDeliveryTag(), true);
            }
        });
        return container;
    }

    // 消费者 监听某个队列 方式二
    @Bean
    public SimpleMessageListenerContainer messageListenerContainer(ConnectionFactory connectionFactory){
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.setQueueNames(RabbitmqConfig.QUEUE_NAME);

        MessageListenerAdapter adapter = new MessageListenerAdapter(new MessageHandler());
        //指定Json转换器
        adapter.setMessageConverter(new TextMessageConverter());
        //设置处理器的消费消息的默认方法
        adapter.setDefaultListenerMethod("onMessage");
        container.setMessageListener(adapter);

        return container;
    }
}
