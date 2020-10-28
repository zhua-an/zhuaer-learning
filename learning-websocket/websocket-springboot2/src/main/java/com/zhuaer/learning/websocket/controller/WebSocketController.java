package com.zhuaer.learning.websocket.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.security.Principal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @ClassName WebSocketController
 * @Description TODO
 * @Author zhua
 * @Date 2020/10/27 16:08
 * @Version 1.0
 */
@EnableScheduling
@Controller
public class WebSocketController {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @GetMapping("/")
    public String index() {
        return "index";
    }

    /**
     * index.html将message发送给后端，后端再将消息重组后发送到/topicTest/web-to-server-to-web
     * @param message
     * @return
     * @throws Exception
     */
    @MessageMapping("/send")
    @SendTo("/topicTest/web-to-server-to-web")
    public String send(String message) throws Exception {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return "服务器将原消息返回: "+df.format(new Date())+" :" + message;
    }

    /**
     * 这里用的是@SendToUser，这就是发送给单一客户端的标志。本例中，
     * 客户端接收一对一消息的主题应该是“/userTest/” + 用户Id + “/message” ,这里的用户id可以是一个普通的字符串，只要每个用户端都使用自己的id并且服务端知道每个用户的id就行。
     * @SendToUser 此外该注解还有个broadcast属性，表明是否广播。就是当有同一个用户登录多个session时，是否都能收到。取值true/false.
     * @return
     */
    @MessageMapping("/sendToUser")
    @SendToUser("/message")
    public String handleSubscribe(String message) {
        System.out.println(message);
        System.out.println("I am a msg from SubscribeMapping('/sendToUser').");
        return "I am a msg from SubscribeMapping('/sendToUser').";
    }

    /**
     * 最基本的服务器端主动推送消息给前端
     * @return
     * @throws Exception
     */
    @Scheduled(fixedRate = 1000)
    public String serverTime() throws Exception {
        // 发现消息
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        messagingTemplate.convertAndSend("/topicTest/servertime", df.format(new Date()));
        return "servertime";
    }

    /**
     * 以下面这种方式发送消息，前端订阅消息的方式为： stompClient.subscribe('/userTest/hzb/info'
     * @return
     * @throws Exception
     */
    @Scheduled(fixedRate = 1000)
    public String serverTimeToUser() throws Exception {
        // 发现消息
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        //这里虽然没有指定发送前缀为/userTest，但是在WebsocketConfig.java中设置了config.setUserDestinationPrefix("/userTest")，
        //否则默认为/user
        messagingTemplate.convertAndSendToUser("zhua","/info", df.format(new Date()));
        return "serverTimeToUser";
    }
}
