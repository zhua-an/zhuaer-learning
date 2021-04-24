# Spring集成MQTT（spring5.4.6）

Spring我用的是5.4.6，在与MQTT集成上4和5有些不同，个人感觉5稍微简单一些，先来看看官方的说明：https://docs.spring.io/spring-integration/reference/html/mqtt.html

## 集成需要的jar包

```xml
<!-- Mqtt -->
<dependency>
	<groupId>org.springframework.integration</groupId>
	<artifactId>spring-integration-stream</artifactId>
	<version>5.4.6</version>
</dependency>

<dependency>
	<groupId>org.springframework.integration</groupId>
	<artifactId>spring-integration-core</artifactId>
	<version>5.4.6</version>
</dependency>

<dependency>
	<groupId>org.springframework.integration</groupId>
	<artifactId>spring-integration-mqtt</artifactId>
	<version>5.4.6</version>
</dependency>
```

## spring-mqtt.xml配置

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:int="http://www.springframework.org/schema/integration"
       xmlns:context="http://www.springframework.org/schema/context"
       xmlns:int-mqtt="http://www.springframework.org/schema/integration/mqtt"
       xsi:schemaLocation="
        http://www.springframework.org/schema/integration
        http://www.springframework.org/schema/integration/spring-integration-4.1.xsd
        http://www.springframework.org/schema/beans
        http://www.springframework.org/schema/beans/spring-beans-3.1.xsd
        http://www.springframework.org/schema/integration/mqtt
        http://www.springframework.org/schema/integration/mqtt/spring-integration-mqtt-4.1.xsd
        http://www.springframework.org/schema/context
        http://www.springframework.org/schema/context/spring-context-3.1.xsd ">

    <context:property-placeholder location="classpath:mqtt.properties" ignore-unresolvable="true"/>

    <!-- clientFactory 新版本登录信息需要设置到 org.eclipse.paho.client.mqttv3.MqttConnectOptions -->
    <bean id="clientFactory" class="org.springframework.integration.mqtt.core.DefaultMqttPahoClientFactory">
        <property name="connectionOptions">
            <bean class="org.eclipse.paho.client.mqttv3.MqttConnectOptions">
                <property name="userName" value="${mqtt.username}"/>
                <property name="password" value="${mqtt.password}"/>
                <property name="cleanSession" value="${mqtt.cleanSession}"/>
                <property name="keepAliveInterval" value="${mqtt.keepAliveInterval}"/>
                <property name="serverURIs">
                    <array>
                        <value>${mqtt.serverURI1}</value>
                    </array>
                </property>
            </bean>
        </property>
    </bean>

    <!-- 入站（消息驱动）通道适配器-->
    <int:channel id="mqttInputChannel"/>
    <!-- 出站（消息驱动）通道适配器-->
    <int:channel id="mqttOutputChannel"/>

    <!-- 消费者 -->
    <int-mqtt:message-driven-channel-adapter
            id="mqttInbound"
            client-id="mqtt_bound_in"
            qos="1"
            client-factory="clientFactory"
            auto-startup="true"
            send-timeout="10"
            channel="mqttInputChannel"
            topics="TOPIC_TEST#"/>

    <!--生产者 -->
    <int-mqtt:outbound-channel-adapter
            id="mqttOutbound"
            client-id="mqtt_bound_out"
            client-factory="clientFactory"
            auto-startup="true"
            default-qos="1"
            default-topic="TOPIC_TEST"
            channel="mqttOutputChannel"/>

    <!--消息发送方式一-->
    <!-- 配置出站网关，用于发送消息 -->
    <int:gateway service-interface="com.zhuaer.learning.mqtt.service.handler.MqttMessageSender" id="mqttMessageSender" default-request-channel="mqttOutputChannel"/>

    <!--消息发送方式二-->
    <bean id="mqttHandler" class="org.springframework.integration.mqtt.outbound.MqttPahoMessageHandler">
        <constructor-arg name="clientId" value="${mqtt.clientId}_two"/>
        <constructor-arg name="clientFactory" ref="clientFactory"/>
        <property name="async" value="${mqtt.async}"/>
        <property name="defaultQos" value="${mqtt.defaultQos}"/>
        <property name="completionTimeout" value="${mqtt.completionTimeout}"/>
    </bean>

    <!--将自定义消息接收类装配到入站适配器, 用于接收消息-->
    <int:service-activator id="startCaseService"
                           input-channel="mqttInputChannel" ref="mqttMessageHandle" method="handleMessage"/>

    <!-- 自定义消息接收类 -->
    <bean id="mqttMessageHandle" class="com.zhuaer.learning.mqtt.service.handler.MqttMessageHandle"></bean>

    <!-- 消息发送处理 -->
    <bean id="mqttCaseService" class="com.zhuaer.learning.mqtt.service.impl.MqttServiceImpl"/>

</beans>
```

生产者与消费者中配置说明见官方说明：https://docs.spring.io/spring-integration/reference/html/mqtt.html#mqtt-inbound

**注：topics中的"#"用于模糊匹配，如上配置中的"/TOPIC_TEST/"主题相关的消息都会被MqttMessageHandle接收**

## 定义消息接收类

```java
import org.springframework.integration.mqtt.support.MqttHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessagingException;

/**
 * @ClassName MqttMessageHandle
 * @Description 入站消息接收类
 * @Author zhua
 * @Date 2021/4/24 18:03
 * @Version 1.0
 */
public class MqttMessageHandle {

    public void handleMessage(Message<String> message) throws MessagingException {
        String topic = message.getHeaders().get(MqttHeaders.RECEIVED_TOPIC).toString();
        String content = message.getPayload();
        System.out.println("收到消息");
        System.out.println("主题:" + topic);
        System.out.println("内容:" + content);
    }
}
```

## 定义消息发送接口（出站网关：gateway）

```java
import org.springframework.integration.mqtt.support.MqttHeaders;
import org.springframework.messaging.handler.annotation.Header;

public interface MqttMessageSender {
    void sendMessage(@Header(MqttHeaders.TOPIC) String topic, String message);
}
```

## 发送消息

在cotroller或者service中注入出站网关MqttMessageSender ，调用sendMessage方法即可发送消息

```java
@RestController
@RequestMapping("/mqttController")
@Slf4j
public class MqttController {

    @Autowired
    private MqttService mqttService;

    @Autowired
    private MqttMessageSender mqttMessageSender;

    @RequestMapping(params = "testSend", method = RequestMethod.POST)
    @ResponseBody
    public String testSend(HttpServletRequest request, HttpServletResponse response) {
        try {
            String topic = "topic";
            String content = "content";
            this.mqttService.send(topic,content);
            this.mqttMessageSender.sendMessage(topic, content);
        } catch (Exception ex) {
            ex.printStackTrace();
            log.error("发送失败", ex);
        }
        return "发送成功";
    }
}
```

## 接收订阅消息

项目启动成功后，配置中订阅主题的相关消息都会被节点 **定义消息接收类** 接收，可根据不同topic执行相应的业务处理。

