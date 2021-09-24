package com.zhuaer.learning.webserver.publish.client;

import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.rpc.client.RPCServiceClient;

import javax.xml.namespace.QName;

/**
 * @ClassName Axis2WsClient
 * @Description TODO
 * @Author zhua
 * @Date 2021/9/24 9:15
 * @Version 1.0
 */
public class Axis2WsClient {

    public static void callWs() throws AxisFault {
        try {
            EndpointReference targetEPR = new EndpointReference("http://127.0.0.1:8080/services/helloService?wsdl");

            RPCServiceClient sender = new RPCServiceClient();
            Options options = sender.getOptions();
            options.setTimeOutInMilliSeconds(2 * 20000L); //超时时间20s
            options.setTo(targetEPR);
            QName qName = new QName("http://service.publish.webserver.learning.zhuaer.com", "sayHello");
            String name = "i am zhua";
            Object[] param = new Object[]{name};
//            Object[] param = new Object[]{};
            //这是针对返值类型的
            Class<?>[] types = new Class[]{String.class};
            Object[] response = sender.invokeBlocking(qName, param, types);
            System.out.println(response[0]);
        } catch (AxisFault e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        try {
            callWs();
        } catch (AxisFault axisFault) {
            axisFault.printStackTrace();
        }
    }

}
