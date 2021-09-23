package com.zhuaer.learning.webserver.client;

import com.zhuaer.learning.webserver.service.MyWebService;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import org.apache.cxf.jaxws.endpoint.dynamic.JaxWsDynamicClientFactory;

import javax.xml.namespace.QName;

/**
 * @ClassName CxfWsClient
 * @Description cxf类库 两种调用方式
 * @Author zhua
 * @Date 2020/8/13 12:57
 * @Version 1.0
 */
public class CxfWsClient {

    /**
     * 方式一
     */
    public static void call1() throws Exception {
        JaxWsProxyFactoryBean factory = new JaxWsProxyFactoryBean();
        factory.setServiceClass(MyWebService.class);
        factory.setAddress("http://127.0.0.1:11008/webService?wsdl");
        // 需要服务接口文件
        MyWebService client = (MyWebService) factory.create();
        String result = client.sayHello("admin");
        System.out.println(result);
    }

    /**
     * 方式二
     */
    public static void call2() throws Exception {
        String method = "sayHello";
        //采用动态工厂方式 不需要指定服务接口
        JaxWsDynamicClientFactory dcf = JaxWsDynamicClientFactory.newInstance();
        Client client = dcf
                .createClient("http://127.0.0.1:11008/webService?wsdl");
        QName qName = new QName("http://service.publish.webserver.learning.zhuaer.com", method);
        Object[] result = client.invoke(qName,
                new Object[] { "admin" });
        System.out.println(result[0]);
    }

    /**
     * cxf类库 两种调用方式
     * 启动报错就注释调 pom.xml 的 axis jar包引用
     * @param args
     */
    public static void main(String[] args) throws Exception{
        call1();
        System.out.println("===================================");
        call2();
    }


}
