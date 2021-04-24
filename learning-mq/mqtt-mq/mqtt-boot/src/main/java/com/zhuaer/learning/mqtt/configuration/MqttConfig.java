package com.zhuaer.learning.mqtt.configuration;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.IntegrationComponentScan;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.core.MessageProducer;
import org.springframework.integration.mqtt.core.DefaultMqttPahoClientFactory;
import org.springframework.integration.mqtt.core.MqttPahoClientFactory;
import org.springframework.integration.mqtt.inbound.MqttPahoMessageDrivenChannelAdapter;
import org.springframework.integration.mqtt.outbound.MqttPahoMessageHandler;
import org.springframework.integration.mqtt.support.DefaultPahoMessageConverter;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;

import javax.annotation.PostConstruct;
import java.util.Objects;

/**
 * @ClassName MqttConfig
 * @Description TODO
 * @Author zhua
 * @Date 2021/4/24 15:54
 * @Version 1.0
 */
@Slf4j
@Configuration
@IntegrationComponentScan
public class MqttConfig {

    public static final String OUTBOUND_CHANNEL = "mqttOutboundChannel";

    public static final String INPUT_CHANNEL = "mqttInputChannel";

    public static final String SUB_TOPICS = "PSimulation,Pressure,PSimulationPump,PSimulationPressure," +
            "PSimulationValve,PSimulationFlow,FSimulation,FSimulationPump,FSimulationPressure," +
            "FSimulationValve,FSimulationFlow,leak,blast,test";

    @Autowired
    private MqttProperties mqttProperties;

    @PostConstruct
    public void init() {
        log.debug("username:{} password:{} hostUrl:{} clientId :{} ",
                mqttProperties.getUsername(), mqttProperties.getPassword(), mqttProperties.getHostUrl(), mqttProperties.getClientId());
    }

    /**
     * 配置DefaultMqttPahoClientFactory
     * 1. 配置基本的链接信息
     * 2. 配置maxInflight，在mqtt消息量比较大的情况下将值设大
     */
    @Bean
    public MqttPahoClientFactory clientFactory() {

        final MqttConnectOptions options = new MqttConnectOptions();
        options.setServerURIs(new String[]{mqttProperties.getHostUrl()});
        options.setUserName(mqttProperties.getUsername());
        options.setPassword(mqttProperties.getPassword().toCharArray());
        options.setKeepAliveInterval(mqttProperties.getKeepalive());
        options.setAutomaticReconnect(true);
        // 配置最大不确定接收消息数量，默认值10，qos!=0 时生效
        options.setMaxInflight(10);
        final DefaultMqttPahoClientFactory factory = new DefaultMqttPahoClientFactory();
        factory.setConnectionOptions(options);
        return factory;
    }

    /**
     * 配置Outbound出站，发布者发送的消息通道
     */
    @Bean(value = OUTBOUND_CHANNEL)
    public MessageChannel mqttOutboundChannel() {
        return new DirectChannel();
    }

    /**
     * 配置Outbound出站，出站通道适配器
     * 1. 通过MqttPahoMessageHandler 初始化出站通道适配器
     * 2. 配置异步发送
     * 3. 配置默认的服务质量
     */
    @Bean
    @ServiceActivator(inputChannel = OUTBOUND_CHANNEL)
    public MessageHandler mqttOutbound() {
        final MqttPahoMessageHandler handler = new MqttPahoMessageHandler(mqttProperties.getClientId() + "_outbound", clientFactory());
        handler.setDefaultQos(1);
        handler.setDefaultRetained(false);
        handler.setDefaultTopic(mqttProperties.getDefaultTopic());
        // 设置异步发送，默认是false(发送时阻塞)
        handler.setAsync(false);
        handler.setAsyncEvents(false);
        return handler;
    }

    /********************************接收处理**************************************/

    /**
     * MQTT消息接收处理
     * 配置Inbound入站，消费者订阅的消息通道
     */
    @Bean
    public MessageChannel mqttInputChannel() {
        return new DirectChannel();
    }

    /**
     * 配置Inbound入站，消费者基本连接配置
     * 1. 通过DefaultMqttPahoClientFactory 初始化入站通道适配器
     * 2. 配置超时时长，默认30000毫秒
     * 3. 配置Paho消息转换器
     * 4. 配置发送数据的服务质量 0~2
     * 5. 配置订阅通道
     */
    //配置client,监听的topic
    @Bean
    public MessageProducer inbound() {
        MqttPahoMessageDrivenChannelAdapter adapter =
                new MqttPahoMessageDrivenChannelAdapter(
                        mqttProperties.getClientId() + "_inbound", clientFactory(), SUB_TOPICS.split(","));
        // 设置连接超时时长(默认30000毫秒)
        adapter.setCompletionTimeout(3000);
        // 配置默认Paho消息转换器(qos=0, retain=false, charset=UTF-8)
        adapter.setConverter(new DefaultPahoMessageConverter());
        // 设置服务质量
        // 0 最多一次，数据可能丢失;
        // 1 至少一次，数据可能重复;
        // 2 只有一次，有且只有一次;最耗性能
        adapter.setQos(1);
        // 设置订阅通道
        adapter.setOutputChannel(mqttInputChannel());
        return adapter;
    }

    /**
     * 配置Inbound入站，消费者的消息处理器
     * 1. 使用@ServiceActivator注解，表明所修饰的方法用于消息处理
     * 2. 使用inputChannel值，表明从指定通道中取值
     * 3. 利用函数式编程的思路，解耦MessageHandler的业务逻辑
     */
    //通过通道获取数据
    @Bean
    @ServiceActivator(inputChannel = INPUT_CHANNEL)
    public MessageHandler handler() {
        return message -> {
            String topic = Objects.requireNonNull(message.getHeaders().get("mqtt_receivedTopic")).toString();
            log.info("topic: {}", topic);
            String[] topics = SUB_TOPICS.split(",");
            for (String t : topics) {
                if (t.equals(topic)) {
                    log.info("payload: {}", message.getPayload().toString());
                }
            }
        };
    }
}
