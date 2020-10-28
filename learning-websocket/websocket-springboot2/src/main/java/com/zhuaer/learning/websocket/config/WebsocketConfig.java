package com.zhuaer.learning.websocket.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

import java.security.Principal;
import java.util.Map;

/**
 * @ClassName WebsocketConfig
 * @Description WebsocketConfig 类进行了websocket的配置
 * @Author zhua
 * @Date 2020/10/27 16:01
 * @Version 1.0
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebsocketConfig implements WebSocketMessageBrokerConfigurer {

    /**
     * 配置了一个简单的消息代理，如果不重载，默认情况下回自动配置一个简单的内存消息代理，用来处理以"/topic"为前缀的消息。这里重载configureMessageBroker()方法，
     * 消息代理将会处理前缀为"/userTest"和"/topicTest"的消息。
     * @param config
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        //启用/userTest，/topicTest,两个消息前缀
        config.enableSimpleBroker("/userTest","/topicTest");
        //如果不设置下面这一句，用convertAndSendToUser来发送消息，前端订阅只能用/user开头。
        config.setUserDestinationPrefix("/userTest");
        //客户端（html等）向服务端发送消息的前缀
        config.setApplicationDestinationPrefixes("/app");
        //可以已“.”来分割路径，看看类级别的@messageMapping和方法级别的@messageMapping  例如：/app/zhua.sendToUser
//        config.setPathMatcher(new AntPathMatcher("."));
    }

    /**
     * 将"/websocket-endpoint"路径注册为STOMP端点，这个路径与发送和接收消息的目的路径有所不同，这是一个端点，客户端在订阅或发布消息到目的地址前，要连接该端点，
     * 即用户发送请求url="/applicationName/websocket-endpoint"与STOMP server进行连接。之后再转发到订阅url；
     * PS：端点的作用——客户端在订阅或发布消息到目的地址前，要连接该端点。
     * @param stompEndpointRegistry
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry stompEndpointRegistry) {
        //客户端和服务端进行连接的endpoint
        stompEndpointRegistry.addEndpoint("/websocket-endpoint").setHandshakeHandler(new  DefaultHandshakeHandler(){
            @Override
            protected Principal determineUser(ServerHttpRequest request, WebSocketHandler wsHandler, Map<String, Object> attributes) {
                //key就是服务器和客户端保持一致的标记，一般可以用账户名称，或者是用户ID。
                return new MyPrincipal("zhua");
            }
        }).setAllowedOrigins("*").withSockJS();
    }


    /**
     * 自定义的Principal
     */
    class MyPrincipal implements Principal{

        private String key;

        public MyPrincipal(String key) {
            this.key = key;
        }

        @Override
        public String getName() {
            return key;
        }

    }
}

