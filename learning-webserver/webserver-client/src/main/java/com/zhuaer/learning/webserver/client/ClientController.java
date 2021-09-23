package com.zhuaer.learning.webserver.client;

import com.alibaba.fastjson.JSONObject;
import org.apache.axis.client.Call;
import org.apache.axis.client.Service;
import org.apache.axis.utils.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.xml.namespace.QName;
import java.util.Set;
import java.util.Vector;

/**
 * @author zhua
 */
@RestController
public class ClientController {

    private static  String ASMX_URL = "http://127.0.0.1:11008/webService/myWebService?wsdl";
    private static  String SOAPACTION="http://service.publish.webserver.learning.zhuaer.com";

    /**
     * 调用webserver样例
     * @param method
     * @param data
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/toWebServer")
    public String toWebServer(String method, String data) throws Exception{
        String returnValue = "";

        Service service=new Service();
        try{
            Call call=(Call)service.createCall();
            call.setTargetEndpointAddress(ASMX_URL);
            //new QName的URL是要指向的命名空间的名称，这个URL地址在你的wsdl打开后可以看到的，
            //上面有写着targetNamespace="http://*.*.*/",这个就是你的命名空间值了;
            call.setOperationName(new QName(SOAPACTION, method));

//            call.setReturnType(new QName(SOAPACTION, method), Vector.class);

            call.setReturnType(org.apache.axis.encoding.XMLType.XSD_STRING);

            //跨平台调用加上这个
            call.setUseSOAPAction(true);
            call.setSOAPActionURI(SOAPACTION + "/" + method);
            //传递参数获取返回值
            if(StringUtils.isEmpty(data)) {
                returnValue = (String)call.invoke(new Object[0]);
            } else {
                JSONObject jsonObj = JSONObject.parseObject(data);
                Set<String> keySet  = jsonObj.keySet();
                Object[] Objarr = new Object[keySet.size()];
                int i = 0;
                for(String key : keySet){
                    Objarr[i++] = jsonObj.get(key);
                    call.addParameter(new QName(SOAPACTION,key),
                            org.apache.axis.encoding.XMLType.XSD_STRING,
                            javax.xml.rpc.ParameterMode.IN);
                }
                //调用方法并传递参数
                returnValue = (String)call.invoke(Objarr);
            }
            System.out.println("调用结果："+returnValue);
        }catch(Exception ex) {
            ex.printStackTrace();
            JSONObject result = new JSONObject();
            result.put("result", false);
            result.put("message", ex.getMessage());
            return result.toString();
        }
        return returnValue;
    }

    public static void main(String[] args) throws Exception {
        ClientController clent = new ClientController();
        JSONObject json = new JSONObject();
        json.put("username","admin");
        clent.toWebServer("sayHello", json.toJSONString());
    }
}
