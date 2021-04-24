package com.zhuaer.learning.mqtt.handler;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.io.UnsupportedEncodingException;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @ClassName MqttProducer
 * @Description 生产者
 * @Author zhua
 * @Date 2021/4/24 16:00
 * @Version 1.0
 */
@Slf4j
public class MqttProducer {

    private static final MqttProducer mqttProducer = new MqttProducer();

    private MqttProducer(){

    }

    public static String HOST = "tcp://127.0.0.1:61613";
    public static String TOPIC = "v1/AlertResp";
    private static String CLIENTID = "zzservertid";

    private MqttClient client;
    private MqttTopic topic;

    private String userName = "admin";
    private String passWord = "password";

    //消息体
    private MqttMessage message;

    private ScheduledExecutorService scheduler;

    public static MqttProducer getInstance(){
        return  mqttProducer;
    }

    public void init(Properties properties) {
        try {
            HOST = String.format("tcp://%s:%s", properties.getProperty("mqtt.server"),
                    properties.getProperty("mqtt.port"));
            CLIENTID = properties.getProperty("mqtt.serverid") + UUID.randomUUID().toString();
            this.userName = properties.getProperty("mqtt.username");
            this.passWord = properties.getProperty("mqtt.password");
            this.TOPIC = properties.getProperty("mqtt.topic");
        } catch (Exception e) {
            log.error(e.getMessage());
        }

        try {
            client = new MqttClient(HOST, CLIENTID, new MemoryPersistence());
        } catch (MqttException e) {
            e.printStackTrace();
        }
        connect();
    }

    /**
     * 连接服务器
     */
    private void connect() {
        MqttConnectOptions options = new MqttConnectOptions();
        options.setCleanSession(false);
        options.setUserName(userName);
        options.setPassword(passWord.toCharArray());
        // 设置超时时间
        options.setConnectionTimeout(10);
        // 设置会话心跳时间
        options.setKeepAliveInterval(20);
        try {
            client.connect(options);
            topic = client.getTopic(TOPIC);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setCallback(MqttCallback callback) {
        // 设置回调
        client.setCallback(callback);
    }

    /**
     * 消息发布
     *
     * @param topic
     * @param message
     * @throws MqttPersistenceException
     * @throws MqttException
     */
    public void publish(MqttTopic topic, MqttMessage message) throws MqttPersistenceException, MqttException {
        MqttDeliveryToken token = topic.publish(message);
        token.waitForCompletion();
        log.info("message is published completely! " + token.isComplete() + " topic:" + topic.getName() +
                " Message:" + new String(message.getPayload()));
    }

    /**
     * 消息发布
     *
     * @param topic
     * @param message
     * @throws MqttPersistenceException
     * @throws MqttException
     */
    public void publish(String topic, String message) throws MqttPersistenceException, MqttException {
        if (!client.isConnected()){
            client.connect();
        }

        MqttTopic mqttTopic = client.getTopic(topic);
        MqttMessage mqttMessage = new MqttMessage();
        mqttMessage.setQos(2);
        mqttMessage.setRetained(false);
        try {
            mqttMessage.setPayload(message.getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        MqttDeliveryToken token = mqttTopic.publish(mqttMessage);
        token.waitForCompletion();
        log.info("message is published completely! " + token.isComplete());
    }

    /**
     * 通过定时器检查断线重连重连
     */
    public void startReconnect() {
        scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(new Runnable() {

            @Override
            public void run() {
                log.info("客户端重连");
                if (!client.isConnected()) {
                    connect();
                }
            }
        }, 0 * 1000, 10 * 1000, TimeUnit.MILLISECONDS);
    }

    public static void main(String[] args) throws MqttException {
//        Properties properties = LoadProperties.getProperties("mqtt.properties");
//        MqttProducer server = MqttProducer.getInstance();
//        server.init(properties);
//
//        server.message = new MqttMessage();
//        server.message.setQos(2);
//        server.message.setRetained(true);
//        server.setCallback(new ConnectionCallback(server));
//
//        for (int i = 0; i < 10000; i++) {
//            server.message.setPayload(("给客户端124推送的信息" + i).getBytes());
//            server.publish(server.topic, server.message);
//            try {
//                Thread.sleep(1000);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        }

//        LogUtil.info(server.message.isRetained() + "------ratained状态");

//        Properties p = LoadProperties.getProperties("mqtt.properties");
//        MqttProducer server = MqttProducer.getInstance();
//        server.init(p);
//        for (int i = 0; i < 10000; i++) {
//            try {
//                server.publish(server.TOPIC, "给客户端124推送的信息" + i);
//            } catch (MqttException e) {
//                e.printStackTrace();
//            }
//            try {
//                Thread.sleep(1000);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        }
    }
}