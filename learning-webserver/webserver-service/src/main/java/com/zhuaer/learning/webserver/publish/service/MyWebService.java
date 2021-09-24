package com.zhuaer.learning.webserver.publish.service;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;

/**
 * @ClassName MyWebService
 * @Description TODO
 * @Author zhua
 * @Date 2021/9/23 10:56
 * @Version 1.0
 */
@WebService(targetNamespace = "http://service.publish.webserver.learning.zhuaer.com")// 命名空间,一般是接口的包名倒序
//@SOAPBinding(style = SOAPBinding.Style.RPC,use = SOAPBinding.Use.LITERAL)
//@SOAPBinding(style= SOAPBinding.Style.DOCUMENT, parameterStyle = SOAPBinding.ParameterStyle.WRAPPED)
public interface MyWebService {

    @WebMethod(operationName = "sayHello", action="http://service.publish.webserver.learning.zhuaer.com/sayHello")
//    String sayHello(@WebParam(name = "username", targetNamespace = "http://service.publish.webserver.learning.zhuaer.com") String username);//不使用此注解 webservice 页面看到参数全是arg0
// axis2 接口的参数不需要加 @WebParam 主键，否则回报 org.apache.axis2.AxisFault: Unmarshalling Error: 意外的元素 (uri:"", local:"arg0") 异常
    String sayHello(String username);

}
