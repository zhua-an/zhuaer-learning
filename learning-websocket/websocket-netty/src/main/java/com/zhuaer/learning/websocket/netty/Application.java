package com.zhuaer.learning.websocket.netty;

import com.zhuaer.learning.websocket.netty.config.NettyServer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @ClassName Application
 * @Description TODO
 * @Author zhua
 * @Date 2020/10/27 16:00
 * @Version 1.0
 */
@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
        //在SpringBoot启动类中加入以下内容
        try {
            new NettyServer(8000).start();
        } catch (Exception e) {
            System.out.println("NettyServerError:" + e.getMessage());
        }
    }
}
