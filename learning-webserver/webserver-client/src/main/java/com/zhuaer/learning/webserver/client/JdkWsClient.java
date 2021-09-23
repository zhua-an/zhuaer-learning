package com.zhuaer.learning.webserver.client;

import com.zhuaer.learning.webserver.service.MyWebService;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import java.net.URL;

/**
 * @ClassName JdkWsClient
 * @Description jdk原生调用(需要获取服务接口文件)
 * @Author zhua
 * @Date 2020/8/13 12:42
 * @Version 1.0
 */
public class JdkWsClient {

    /**
     * 启动报错就注释调 pom.xml 的 axis jar包引用
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        URL url = new URL("http://127.0.0.1:11008/webService?wsdl");
        // 指定命名空间和服务名称
        QName qName = new QName("http://service.publish.webserver.learning.zhuaer.com", "webService");
        Service service = Service.create(url, qName);
        // 通过getPort方法返回指定接口
        MyWebService myServer = service.getPort(new QName("http://service.publish.webserver.learning.zhuaer.com",
                "myWebService"), MyWebService.class);
        // 调用方法 获取返回值
        String result = myServer.sayHello("admin");
        System.out.println(result);
    }
}
