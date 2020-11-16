package com.zhuaer.learning.websocket.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.tio.core.Tio;
import org.tio.websocket.common.WsResponse;
import org.tio.websocket.starter.TioWebSocketServerBootstrap;

/**
 * @ClassName TestController
 * @Description TODO
 * @Author zhua
 * @Date 2020/11/12 15:21
 * @Version 1.0
 */
@Controller
public class TestController {

    @Autowired
    private TioWebSocketServerBootstrap bootstrap;

    @GetMapping("/send")
    public @ResponseBody Object sendMsg(String msg) {
        Tio.sendToAll(bootstrap.getServerTioConfig(), WsResponse.fromText(msg, "utf-8"));
        return "success";
    }
}
