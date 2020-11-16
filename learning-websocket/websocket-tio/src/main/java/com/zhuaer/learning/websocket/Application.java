package com.zhuaer.learning.websocket;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.tio.websocket.starter.EnableTioWebSocketServer;

/**
 * @ClassName Application
 * @Description TODO
 * @Author zhua
 * @Date 2020/11/12 15:12
 * @Version 1.0
 */
@SpringBootApplication
@EnableTioWebSocketServer
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
