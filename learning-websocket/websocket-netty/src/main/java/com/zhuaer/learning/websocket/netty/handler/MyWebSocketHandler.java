package com.zhuaer.learning.websocket.netty.handler;

import com.alibaba.fastjson.JSON;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.GlobalEventExecutor;

import java.util.HashMap;
import java.util.Map;

/**
 * @ClassName MyWebSocketHandler
 * @Description 初始化器
 * @Author zhua
 * @Date 2020/10/28 11:28
 * @Version 1.0
 */
public class MyWebSocketHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {
    //TextWebSocketFrame是netty用于处理websocket发来的文本对象

    //通道组池，管理所有websocket连接
    //GlobalEventExecutor.INSTANCE 是全局的事件执行器，是一个单例
    public static ChannelGroup channelGroup = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    //客户端建立连接
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("与客户端建立连接，通道开启！");

        //添加到channelGroup通道组
        this.channelGroup.add(ctx.channel());
        System.out.println("[新用户] - " + ctx.name() + " -  " + ctx.channel().id() + " 加入");
        sendAllMessage("[新用户] - " + ctx.name() + " -  " + ctx.channel().id() + " 加入");
    }

    //关闭连接
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("与客户端断开连接，通道关闭！");
        //添加到channelGroup 通道组
        this.channelGroup.remove(ctx.channel());
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        //首次连接是FullHttpRequest，处理参数
        if (null != msg && msg instanceof FullHttpRequest) {
            FullHttpRequest request = (FullHttpRequest) msg;
            String uri = request.uri();

            Map paramMap=getUrlParams(uri);
            System.out.println("接收到的参数是："+JSON.toJSONString(paramMap));
            //如果url包含参数，需要处理
            if(uri.contains("?")){
                String newUri=uri.substring(0,uri.indexOf("?"));
                System.out.println(newUri);
                request.setUri(newUri);
            }

        }else if(msg instanceof TextWebSocketFrame){
            //正常的TEXT消息类型
            TextWebSocketFrame frame=(TextWebSocketFrame)msg;
            System.out.println("客户端收到服务器数据：" +frame.text());
            sendAllMessage(frame.text());
        }
        super.channelRead(ctx, msg);
    }

    //接收到客户都发送的消息，需要编解码的才会去用messageReceived
    @Override
    protected void messageReceived(ChannelHandlerContext channelHandlerContext, TextWebSocketFrame textWebSocketFrame) throws Exception {

    }

    //出现异常
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        Channel incoming = ctx.channel();
        System.out.println("SimpleChatClient:" + incoming.remoteAddress()+"异常");
        //异常出现就关闭连接
        cause.printStackTrace();
        ctx.close();
    }

    private void sendAllMessage(String message){
        //收到信息后，群发给所有channel
        this.channelGroup.writeAndFlush( new TextWebSocketFrame(message));
    }

    //给某个人发送消息
    private void sendMessage(ChannelHandlerContext ctx, String message) {
        ctx.channel().writeAndFlush(new TextWebSocketFrame(message));
    }

    //给每个人发送消息,除发消息人外
    private void sendAllMessages(ChannelHandlerContext ctx,String message) {
        for(Channel channel:this.channelGroup){
            if(!channel.id().asLongText().equals(ctx.channel().id().asLongText())){
                channel.writeAndFlush(new TextWebSocketFrame(message));
            }
        }
    }

    //删除用户与channel的对应关系
    private void removeUserId(ChannelHandlerContext ctx){
        AttributeKey<String> key = AttributeKey.valueOf("userId");
        String userId = ctx.channel().attr(key).get();
        this.channelGroup.remove(userId);
    }

    private static Map getUrlParams(String url){
        Map<String,String> map = new HashMap<>();
        url = url.replace("?",";");
        if (!url.contains(";")){
            return map;
        }
        if (url.split(";").length > 0){
            String[] arr = url.split(";")[1].split("&");
            for (String s : arr){
                String key = s.split("=")[0];
                String value = s.split("=")[1];
                map.put(key,value);
            }
            return  map;
        }else{
            return map;
        }
    }

}
