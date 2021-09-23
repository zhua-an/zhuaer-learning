package com.zhuaer.learning.webserver.client;

import org.apache.axis.client.Call;
import org.apache.axis.client.Service;
import org.apache.axis.encoding.XMLType;

import javax.xml.namespace.QName;
import javax.xml.rpc.ParameterMode;
import javax.xml.rpc.ServiceException;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;

/**
 * @ClassName AxisWsClient
 * @Description TODO
 * @Author zhua
 * @Date 2020/8/13 13:05
 * @Version 1.0
 */
public class AxisWsClient {
    private static  String ASMX_URL = "http://127.0.0.1:11008/webService/myWebService?wsdl";
    private static  String SOAPACTION="http://service.publish.webserver.learning.zhuaer.com";
    /**
     * 跨平台调用Web Service出现
     *  faultString: 服务器未能识别 HTTP 头 SOAPAction 的值:
     * JAX-WS规范不需要SoapAction，但是.NET需要，所以产生了这个错误。
     * options.setAction("目标的TargetNameSpace"+"调用的方法名");
     */
    public static void main(String[] args) {
        Service service = new Service();
        String method = "sayHello";
        try {
            Call call = (Call) service.createCall();
            call.setTargetEndpointAddress(new URL(ASMX_URL));
//            call.setTargetEndpointAddress(ASMX_URL);

            // WSDL里面描述的接口名称(要调用的方法)
            call.setOperationName(new QName(SOAPACTION,
                    method));

            //跨平台调用加上这个
//            call.setUseSOAPAction(true);
//            call.setSOAPActionURI(SOAPACTION + method);
            // 接口方法的参数名, 参数类型,参数模式 IN(输入), OUT(输出) or INOUT(输入输出)
            call.addParameter("username", XMLType.XSD_STRING, ParameterMode.IN);
            // 设置被调用方法的返回值类型
            call.setReturnType(XMLType.XSD_STRING);
//            call.setReturnType(new QName(SOAPACTION, method), Vector.class);

            // 设置方法中参数的值
            Object result = call.invoke(new Object[] { "admin" });

            System.out.println(result.toString());
        } catch (ServiceException | RemoteException | MalformedURLException e) {
            e.printStackTrace();
        }
    }

}
