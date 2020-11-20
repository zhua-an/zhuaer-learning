package com.zhuaer.learning.mq.rabbit.handler;

import com.rabbitmq.client.Channel;
import com.zhuaer.learning.mq.rabbit.config.RabbitMQConfig2;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Date;

/**
 * @ClassName DeadLetterQueueConsumer
 * @Description 死信队列消费者
 * @Author zhua
 * @Date 2020/8/20 14:23
 * @Version 1.0
 */
@Slf4j
@Component
public class DeadLetterQueueConsumer2 {

    @RabbitListener(queues = RabbitMQConfig2.DEAD_LETTER_QUEUEC_NAME)
    public void receiveC(Message message, Channel channel) throws IOException {
        String msg = new String(message.getBody());
        log.info("当前时间：{},死信队列C收到消息：{}", new Date().toString(), msg);
        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
    }
}
