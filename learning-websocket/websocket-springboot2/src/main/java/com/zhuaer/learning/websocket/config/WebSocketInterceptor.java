package com.zhuaer.learning.websocket.config;

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import javax.servlet.http.HttpSession;
import java.util.Map;

/**
 * @ClassName WebSocketInterceptor
 * @Description TODO
 * @Author zhua
 * @Date 2020/11/12 14:09
 * @Version 1.0
 */
public class WebSocketInterceptor implements HandshakeInterceptor {

    /**
     * 拦截器主要是用于用户登录标识（userId）的记录，便于后面获取指定用户的会话标识并向指定用户发送消息，在下面的拦截器中，
     * 我在session中获取会话标识（这个标识是在登录时setAttribute进去的，后面代码会说到），你也可以通过H5在new WebSocket(url)中，在url传入标识参数，
     * 然后通过serverHttpRequest.getServletRequest().getParameterMap()来获取标识信息
     */


    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler webSocketHandler, Map<String, Object> map) throws Exception {
        if (request instanceof ServletServerHttpRequest) {
            ServletServerHttpRequest serverHttpRequest = (ServletServerHttpRequest) request;
            HttpSession session = serverHttpRequest.getServletRequest().getSession();
//            Map parameterMap = serverHttpRequest.getServletRequest().getParameterMap();
//            System.out.println(parameterMap);
            if (session != null) {
                map.put("userId", session.getAttribute("userId"));
            }

        }
        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest serverHttpRequest, ServerHttpResponse serverHttpResponse, WebSocketHandler webSocketHandler, Exception e) {

    }
}
