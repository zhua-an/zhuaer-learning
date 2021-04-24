package com.zhuaer.learning.mqtt.handler;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @ClassName MqttConsumer
 * @Description 消费者
 * @Author zhua
 * @Date 2021/4/24 15:59
 * @Version 1.0
 */
@Slf4j
public class MqttConsumer {

    public String HOST = "tcp://127.0.0.1:61613";
    private String clientId = "server";
    private String topic = "v1/Gps";
    private MqttClient client;
    private MqttConnectOptions options;
    private String userName = "admin";
    private String passWord = "password";

    private ScheduledExecutorService scheduler;

    public MqttConsumer(String propertiesName) throws MqttException {
        this.initProperties(propertiesName);
        client = new MqttClient(HOST, clientId, new MemoryPersistence());
        init();
    }

    public Properties getProperties(String propName) {
        Properties prop = new Properties();
        InputStream in = null;
        try {
            File file = ResourceUtils.getFile("classpath:" + propName);
            ;
            in = new FileInputStream(file);
            prop.load(in);
        } catch (Exception e) {
            log.error(e.toString());
        } finally {
            try {
                if (null != in) {
                    in.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } return prop;
    }

    private void initProperties(String propertiesName) {
        try {
            Properties mqttProperties = getProperties(propertiesName);
            HOST = String.format("tcp://%s:%s",
                    mqttProperties.getProperty("mqtt.server"),
                    mqttProperties.getProperty("mqtt.port"));
            this.userName = mqttProperties.getProperty("mqtt.username");
            this.passWord = mqttProperties.getProperty("mqtt.password");
            this.clientId = mqttProperties.getProperty("mqtt.clientid") + UUID.randomUUID().toString();
            this.topic = mqttProperties.getProperty("mqtt.topic");
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    private void init() {
        try {
            // MQTT的连接设置
            options = new MqttConnectOptions();
            // 设置是否清空session,这里如果设置为false表示服务器会保留客户端的连接记录，这里设置为true表示每次连接到服务器都以新的身份连接
            options.setCleanSession(true);
            // 设置连接的用户名
            options.setUserName(userName);
            // 设置连接的密码
            options.setPassword(passWord.toCharArray());
            // 设置超时时间 单位为秒
            options.setConnectionTimeout(100);
            // 设置会话心跳时间 单位为秒 服务器会每隔1.5*20秒的时间向客户端发送个消息判断客户端是否在线，但这个方法并没有重连的机制
            options.setKeepAliveInterval(20);
            // MqttTopic topic = client.getTopic(TOPIC);
            // setWill方法，如果项目中需要知道客户端是否掉线可以调用该方法。设置最终端口的通知消息
            // options.setWill(topic, "close".getBytes(), 2, true);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 订阅消息
     */
    public void connect() {
        try {
            client.connect(options);
            // 订阅消息
            int[] Qos = {1};
            String[] topic1 = {this.topic};
            client.subscribe(topic1, Qos);

        } catch (MqttSecurityException e) {
            e.printStackTrace();
        } catch (MqttException e) {
            e.printStackTrace();
        }

    }

    public void setCallback(MqttCallback callback) {
        // 设置回调
        client.setCallback(callback);
    }

    /**
     * 通过定时器检查断线重连重连
     */
    public void startReconnect() {
        if (scheduler == null) {
            scheduler = Executors.newSingleThreadScheduledExecutor();
            scheduler.scheduleAtFixedRate(new Runnable() {
                @Override
                public void run() {
                    if (!client.isConnected()) {
                        log.info("mqtt client start reconnect...");
                        connect();
                    }
                }
            }, 0 * 1000, 10 * 1000, TimeUnit.MILLISECONDS);
        }
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public static void main(String[] args) throws MqttException {
        MqttConsumer client = new MqttConsumer("mqtt.properties");
        client.setCallback(new PushCallback());
        client.connect();
    }
}