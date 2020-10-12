package com.zhuaer.learning.mq.rabbit.config;

import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessagePostProcessor;

/**
 * @ClassName ExpirationMessagePostProcessor
 * @Description TODO
 * @Author zhua
 * @Date 2020/9/24 14:57
 * @Version 1.0
 */
public class ExpirationMessagePostProcessor implements MessagePostProcessor {

    private final String ttl;

    public ExpirationMessagePostProcessor(String ttl) {
        this.ttl = ttl;
    }

    @Override
    public Message postProcessMessage(Message message) throws AmqpException {
        message.getMessageProperties().setExpiration(ttl);
        return message;
    }
}
