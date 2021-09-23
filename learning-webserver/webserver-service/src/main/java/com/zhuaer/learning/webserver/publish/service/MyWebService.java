package com.zhuaer.learning.webserver.publish.service;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;

/**
 * @ClassName MyWebService
 * @Description TODO
 * @Author zhua
 * @Date 2021/9/23 10:56
 * @Version 1.0
 */
@WebService(targetNamespace = "http://service.publish.webserver.learning.zhuaer.com")// 命名空间,一般是接口的包名倒序
public interface MyWebService {

    @WebMethod(operationName = "sayHello", action="http://service.publish.webserver.learning.zhuaer.com/sayHello")
    String sayHello(@WebParam(name = "username", targetNamespace = "http://service.publish.webserver.learning.zhuaer.com") String username);//不使用此注解 webservice 页面看到参数全是arg0

}
