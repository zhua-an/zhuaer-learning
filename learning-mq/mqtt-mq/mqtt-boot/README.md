# springboot整合mqtt

mqtt是一个轻量级的消息服务器

导入面向企业应用集成库和对应mqtt集成库,pom配置：

```xml
<dependencies>
	<dependency>
		<groupId>org.springframework.boot</groupId>
		<artifactId>spring-boot-starter-web</artifactId>
	</dependency>
	<dependency>
		<groupId>org.springframework.boot</groupId>
		<artifactId>spring-boot-configuration-processor</artifactId>
		<optional>true</optional>
	</dependency>
	<dependency>
		<groupId>org.springframework.boot</groupId>
		<artifactId>spring-boot-starter-integration</artifactId>
	</dependency>
	<dependency>
		<groupId>org.springframework.integration</groupId>
		<artifactId>spring-integration-stream</artifactId>
	</dependency>
	<dependency>
		<groupId>org.springframework.integration</groupId>
		<artifactId>spring-integration-mqtt</artifactId>
	</dependency>

	<dependency>
		<groupId>org.projectlombok</groupId>
		<artifactId>lombok</artifactId>
		<version>1.18.16</version>
	</dependency>

	<dependency>
		<groupId>org.springframework.boot</groupId>
		<artifactId>spring-boot-starter-test</artifactId>
		<scope>test</scope>
	</dependency>
</dependencies>
```

MQTT连接配置文件：

```yaml
spring:
  mqtt:
    # 账号
    username:
    # 密码
    password:
    # mqtt连接tcp地址
    host-url: tcp://broker.emqx.io:1883
    # 客户端Id，每个启动的id要不同
    client-id: test
    # 默认主题
    default-topic: test
    # 超时时间 单位为秒
    timeout: 100
    # 设置会话心跳时间 单位为秒 服务器会每隔1.5*20秒的时间向客户端发送心跳判断客户端是否在线
    keepalive: 100
```

mqtt属性配置：

```java
/**
 * @ClassName MqttProperties
 * @Description TODO
 * @Author zhua
 * @Date 2021/4/24 15:55
 * @Version 1.0
 */
@Component
@ConfigurationProperties(prefix = "spring.mqtt")
public class MqttProperties {

    private String username;
    private String password;
    private String hostUrl;
    private String clientId;
    private String defaultTopic;
    private String timeout;
    private int keepalive;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getHostUrl() {
        return hostUrl;
    }

    public void setHostUrl(String hostUrl) {
        this.hostUrl = hostUrl;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getDefaultTopic() {
        return defaultTopic;
    }

    public void setDefaultTopic(String defaultTopic) {
        this.defaultTopic = defaultTopic;
    }

    public String getTimeout() {
        return timeout;
    }

    public void setTimeout(String timeout) {
        this.timeout = timeout;
    }

    public int getKeepalive() {
        return keepalive;
    }

    public void setKeepalive(int keepalive) {
        this.keepalive = keepalive;
    }
}
```

mqtt配置：

**配置MQTT订阅者  入站消息适配器**

第一步：配置MQTT客户端工厂类DefaultMqttPahoClientFactory

第二步：配置MQTT入站消息适配器MqttPahoMessageDrivenChannelAdapter

第三步：定义MQTT入站消息通道MessageChannel

第四步：声明MQTT入站消息处理器MessageHandler

注意：
- 1）MQTT的客户端ID要唯一。
- 2）MQTT在消息量大的情况下会出现消息丢失的情况。
- 3）MessageHandler注意解耦问题。


**配置MQTT发布者  出站消息适配器**

第一步：配置Outbound出站，出站通道适配器

第二步：配置Outbound出站，发布者发送的消息通道

第三步：对外提供推送消息的接口

注意：
- 1）发布者和订阅者的客户端ID不能相同。
- 2）消息的推送建议采用异步的方式。
- 3）消息的推送方法可以只传payload消息体，但需要配置setDefaultTopic。

```java
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
```

推送接口：

注入MQTT的MessageGateway，然后推送消息。

```java
import org.springframework.integration.annotation.MessagingGateway;
import org.springframework.integration.mqtt.support.MqttHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

/**
 * @ClassName MqttGateway
 * @Description 推送接口
 * 对外提供推送消息的接口
 * 1. 使用@MessagingGateway注解，配置MQTTMessageGateway消息推送接口
 * 2. 使用defaultRequestChannel值，调用时将向其发送消息的默认通道
 * 3. 配置灵活的topic主题
 * @Author zhua
 * @Date 2021/4/24 15:57
 * @Version 1.0
 */
@Component
@MessagingGateway(defaultRequestChannel = MqttConfig.OUTBOUND_CHANNEL)
public interface MqttGateway {

    void sendToMqtt(String payload);

    void sendToMqtt(@Header(MqttHeaders.TOPIC) String topic, String payload);

    void sendToMqtt(@Header(MqttHeaders.TOPIC) String topic, @Header(MqttHeaders.QOS) int qos, String payload);

}
```

#开发常见问题

## 1 MQTT每次重连失败都会增长线程数

目上线一段时间后，客户的服务器严重卡顿。原因是客户服务断网后，MQTT在每次尝试重连的过程中一直在创建新的线程，导致一个Java服务创建了上万个线程。解决方案是更新了org.eclipse.paho.client.mqttv3的版本，也是 "3.1 导入mqtt库" 中提到的。后续就没有出现这个问题了。

## 2 MQTT消息量大存在消息丢失的情况

MQTT的消息量大的情况下，既要保障数据的完整，又要保障性能的稳定。光从MQTT本身上来说，很难做到鱼和熊掌不可兼得。

- 1）数据的完整性，主要用于能耗的统计、报警的分析
- 2）性能的稳定性，服务器不挂

在消息量大的情况下，可以将服务质量设置成0（最多一次）以减少消息确认的开销，用来保证系统的稳定性。

将消息的服务质量设置成0后，会让消息的丢失可能性变得更大，如何保证数据的完整性？其实可以在往MQTT通道推送消息之前，先将底层驱动采集的数据先异步保存到数据库中。

还有就是每次发送消息量不能太大，太大也会导致消息丢失。最直接的就是后端报错，比如：java.io.EOFException 和 too large message: xxx bytes 。但是有的场景后端没有报错，前端订阅的mqtt也没收到消息。最麻烦的是mqttbox工具因为数据量太大直接卡死。一时间真不知道把锅甩给谁。其实我们 可以将消息拆包一批批发送。可以缓解这个问题

其实采集的数据消息，若在这一批推送过程中丢失。也会在下一批推送过程中补上。命令下发也是一样，如果下发失败，再重写下发一次。毕竟消息的丢失并不是必现的情况。也是小概率事件，系统的稳定性才是最重要的。