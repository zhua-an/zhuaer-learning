package com.zhuaer.learning.webserver.client;

import javax.xml.namespace.QName;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.rpc.client.RPCServiceClient;
import org.apache.axis2.transport.http.HTTPConstants;

import java.util.Map;

/**
 * @ClassName Axis2WsClient
 * @Description TODO
 * @Author zhua
 * @Date 2021/9/23 15:01
 * @Version 1.0
 */
public class Axis2WsClient {

    private static  String ASMX_URL = "http://127.0.0.1:11008/webService/webService?wsdl";
    private static  String SOAPACTION="http://service.publish.webserver.learning.zhuaer.com";
    public static final String METHOD = "sayHello";

    /**
     * 方法一： 应用rpc的方式调用 这种方式就等于远程调用， 即通过url定位告诉远程服务器，告知方法名称，参数等， 调用远程服务，得到结果。
     * 使用org.apache.axis2.rpc.client.RPCServiceClient类调用WebService；
     *
     * 【注】：
     * 如果被调用的WebService方法有返回值 应使用 invokeBlocking 方法 该方法有三个参数
     * 第一个参数的类型是QName对象，表示要调用的方法名；
     * 第二个参数表示要调用的WebService方法的参数值，参数类型为Object[]，
     * 当方法没有参数时，invokeBlocking方法的第二个参数值不能是null，而要使用new Object[]{}；
     * 第三个参数表示WebService方法的 返回值类型的Class对象，参数类型为Class[]。
     *
     * 如果被调用的WebService方法没有返回值 应使用 invokeRobust方法,
     * 该方法只有两个参数，它们的含义与invokeBlocking方法的前两个参数的含义相同。
     *
     */
    //调用失败
    public void testRPCClient() {
        RPCServiceClient serviceClient = null;
        try {
            // 使用RPC方式调用WebService
            serviceClient = new RPCServiceClient();
            // 创建WSDL的URL，注意不是服务地址
            // 指定调用WebService的URL
            EndpointReference targetEPR = new EndpointReference(ASMX_URL);
            Options options = serviceClient.getOptions();
            // 确定目标服务地址
            options.setTo(targetEPR);
            //解决高并发链接超时问题
            options.setManageSession(true);
            options.setProperty(HTTPConstants.REUSE_HTTP_CLIENT, true);
//            //设置响应超时，默认5s
//            options.setProperty(HTTPConstants.SO_TIMEOUT, TIMEOUT_MILLISECONDS);
//            //设置连接超时，默认5s
//            options.setProperty(HTTPConstants.CONNECTION_TIMEOUT, TIMEOUT_MILLISECONDS);
            // 设置超时时间
            options.setTimeOutInMilliSeconds(1000 * 60 * 5);// 毫秒单位

            /**
             * 指定要调用的getPrice方法及WSDL文件的命名空间
             * 如果 webservice 服务端由axis2编写
             * 命名空间 不一致导致的问题
             * org.apache.axis2.AxisFault: java.lang.RuntimeException: Unexpected subelement arg0
             */
            // 确定调用方法（wsdl 命名空间地址 (wsdl文档中的targetNamespace) 和 方法名称 的组合）
            options.setAction(SOAPACTION + "/" + METHOD);
            // 指定方法的参数值
            Object[] parameters = new Object[] {"admin"};

            // 创建服务名称
            // 1.namespaceURI - 命名空间地址 (wsdl文档中的targetNamespace)
            // 2.localPart - 服务视图名 (wsdl文档中operation的方法名称，例如<wsdl:operation name="sayHello">)
            QName qname = new QName(SOAPACTION, METHOD);

            try {
                // 调用方法一 传递参数，调用服务，获取服务返回结果集
                OMElement element = serviceClient.invokeBlocking(qname, parameters);
                System.out.println(element);
                /*
                 * 值得注意的是，返回结果就是一段由OMElement对象封装的xml字符串。
                 * 我们可以对之灵活应用,下面我取第一个元素值，并打印之。因为调用的方法返回一个结果
                 */
                String result = element.getFirstElement().getText();
                System.out.println(result);
            } catch (AxisFault axisFault) {
                axisFault.printStackTrace();
            }


            // 调用方法二 getPrice方法并输出该方法的返回值
            // 指定方法返回值的数据类型的Class对象
            Class[] returnTypes = new Class[] {String.class};
            Object[] response = serviceClient.invokeBlocking(qname, parameters, returnTypes);
            String r = (String) response[0];
            System.out.println(r);

        } catch (AxisFault e) {
            e.printStackTrace();
        }finally {
            try {
                if(serviceClient != null){
                    serviceClient.cleanupTransport();
                }
            } catch (org.apache.axis2.AxisFault e) {
//                log.error("第三方接口异常 finally",e);
            }
        }
    }

    /** * 返回xml数据 * @param response * @param map */
    public static String getXmlData(String bodyKey, Map<String, String> map) {
        StringBuffer sb = new StringBuffer();
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        sb.append("<"+bodyKey+">");
        for(String key: map.keySet()) {
            sb.append("<"+key+">" + map.get(key) + "</"+key+">");
        }
        sb.append("</"+bodyKey+">");

        return sb.toString();
    }


    /**
    * 方法二： 应用document方式调用 用ducument方式应用现对繁琐而灵活。现在用的比较多。因为真正摆脱了我们不想要的耦合
    * 即使用org.apache.axis2.client.ServiceClient类进行远程调用web服务，不生成客户端
    */
    //调用失败
    public void Weather() throws AxisFault{
        ServiceClient serviceClient = new ServiceClient();
        //创建服务地址WebService的URL,注意不是WSDL的URL
        String url = "http://127.0.0.1:11008/webService/webService.asmx";
        EndpointReference targetEPR = new EndpointReference(url);
        Options options = serviceClient.getOptions();
        options.setTo(targetEPR);
        //确定调用方法（wsdl 命名空间地址 (wsdl文档中的targetNamespace) 和 方法名称 的组合）
        options.setAction(SOAPACTION + "/" + METHOD);

        OMFactory fac = OMAbstractFactory.getOMFactory();
        /*
         * 指定命名空间，参数：
         * uri--即为wsdl文档的targetNamespace，命名空间
         * perfix--可不填
         */
        OMNamespace omNs = fac.createOMNamespace(SOAPACTION, "");
        // 指定方法
        OMElement method = fac.createOMElement(METHOD, omNs);
        // 指定方法的参数
        OMElement username = fac.createOMElement("username", omNs);
        username.setText("admin");
        method.addChild(username);
        //多个参数
        //OMElement username1 = fac.createOMElement("username", omNs);
        //username1.setText("admin");
        //method.addChild(username1);

        method.build();

        //远程调用web服务
        OMElement result = serviceClient.sendReceive(method);
        System.out.println(result);
    }


    public static void main(String[] args) throws AxisFault {
        Axis2WsClient axis2WsClient = new Axis2WsClient();
//        axis2WsClient.testRPCClient();
        axis2WsClient.Weather();
    }

}
