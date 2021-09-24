# webserver-service

springboot 整合axis2 webserver

# 依赖包引用

```xml
<dependency>
	<groupId>org.springframework.boot</groupId>
	<artifactId>spring-boot-starter-web-services</artifactId>
</dependency>

<!-- webservice 发布 -->
<dependency>
	<groupId>org.apache.cxf</groupId>
	<artifactId>cxf-rt-frontend-jaxws</artifactId>
	<version>3.1.12</version>
</dependency>
<dependency>
	<groupId>org.apache.cxf</groupId>
	<artifactId>cxf-rt-transports-http</artifactId>
	<version>3.1.12</version>
</dependency>

<!--有两种发布方式，第一种直接在springboot启动类中使用Endpoint发布服务，
   需要导入下面的jetty依赖，否则会报错：
   Cannot find any registered HttpDestinationFactory from the Bus.
-->
<dependency>
	<groupId>org.apache.cxf</groupId>
	<artifactId>cxf-rt-transports-http-jetty</artifactId>
	<version>3.1.6</version>
</dependency>
```

# 发布服务接口

> 需要注意的是：使用axis2客户端连接接口时，发布服务的接口不需要加 @WebParam 主键，否则会报 org.apache.axis2.AxisFault: Unmarshalling Error: 意外的元素 (uri:"", local:"arg0") 异常

```java
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
//    String sayHello(@WebParam(name = "username", targetNamespace = "http://service.publish.webserver.learning.zhuaer.com") String username);//不使用此注解 webservice 页面看到参数全是arg0
// 	axis2 接口的参数不需要加 @WebParam 主键，否则会报 org.apache.axis2.AxisFault: Unmarshalling Error: 意外的元素 (uri:"", local:"arg0") 异常
    String sayHello(String username);
}
```

# 发布服务实现类

```java
/**
 * WebService涉及到的有这些 "四解三类 ", 即四个注解，三个类
 * @WebMethod
 * @WebService
 * @WebResult
 * @WebParam
 * SpringBus
 * Endpoint
 * EndpointImpl
 *
 * 一般我们都会写一个接口，然后再写一个实现接口的实现类，但是这不是强制性的
 * @WebService 注解表明是一个webservice服务。
 *      name：对外发布的服务名, 对应于<wsdl:portType name="ServerServiceDemo"></wsdl:portType>
 *      targetNamespace：命名空间,一般是接口的包名倒序, 实现类与接口类的这个配置一定要一致这种错误
 *              Exception in thread "main" org.apache.cxf.common.i18n.UncheckedException: No operation was found with the name xxxx
 *              对应于targetNamespace="http://server.webservice.example.com"
 *      endpointInterface：服务接口全路径（如果是没有接口，直接写实现类的，该属性不用配置）, 指定做SEI（Service EndPoint Interface）服务端点接口
 *      serviceName：对应于<wsdl:service name="ServerServiceDemoImplService"></wsdl:service>
 *      portName：对应于<wsdl:port binding="tns:ServerServiceDemoImplServiceSoapBinding" name="ServerServiceDemoPort"></wsdl:port>
 *
 * @WebMethod 表示暴露的服务方法, 这里有接口ServerServiceDemo存在，在接口方法已加上@WebMethod, 所以在实现类中不用再加上，否则就要加上
 *      operationName: 接口的方法名
 *      action: 没发现又什么用处
 *      exclude: 默认是false, 用于阻止将某一继承方法公开为web服务
 *
 * @WebResult 表示方法的返回值
 *      name：返回值的名称
 *      partName：
 *      targetNamespace:
 *      header: 默认是false, 是否将参数放到头信息中，用于保护参数，默认在body中
 *
 * @WebParam
 *       name：接口的参数
 *       partName：
 *       targetNamespace:
 *       header: 默认是false, 是否将参数放到头信息中，用于保护参数，默认在body中
 *       model：WebParam.Mode.IN/OUT/INOUT
 */
```

```java
import com.zhuaer.learning.webserver.publish.service.MyWebService;
import org.springframework.stereotype.Component;

import javax.jws.WebMethod;
import javax.jws.WebService;
/**
 * @ClassName MyWebServiceImpl
 * @Description TODO
 * @Author zhua
 * @Date 2021/9/23 10:59
 * @Version 1.0
 */
@WebService(endpointInterface = "com.zhuaer.learning.webserver.publish.service.MyWebService",
        targetNamespace = "http://service.publish.webserver.learning.zhuaer.com", serviceName = "webService")
@Component
public class MyWebServiceImpl implements MyWebService {

    @WebMethod(operationName = "sayHello")
    @Override
    public String sayHello(String username) {
        return "hello,"+username;
    }
}
```

# 发布服务

## 方式一

直接在启动类中使用Endpoint，不需编写配置类，这种方式可以自定义webservice的端口，但不要和服务器的端口冲突了。

```java
/**
 * @ClassName Application
 * @Description TODO
 * @Author zhua
 * @Date 2021/9/23 10:54
 * @Version 1.0
 */
@SpringBootApplication
public class WebServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(WebServerApplication.class, args);

        System.out.println("启动并发布webservice远程服务");
        MyWebService myWebService = new MyWebServiceImpl();
        Endpoint.publish("http://127.0.0.1:11008/webService/webService?wsdl", myWebService);
        System.out.println("启动并发布webservice远程服务，服务发布成功....");
    }
}

```

## 方式二

使用配置类，不用在启动类中加Endpoint.publish，这种接口的端口号和服务器端口号是一致的

```java

import com.zhuaer.learning.webserver.publish.service.MyWebService;
import org.apache.cxf.Bus;
import org.apache.cxf.bus.spring.SpringBus;
import org.apache.cxf.jaxws.EndpointImpl;
import org.apache.cxf.transport.servlet.CXFServlet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.xml.ws.Endpoint;

/**
 * @ClassName CxfConfig
 * @Description
 * 第一种方式：直接在启动类中使用Endpoint，不需编写配置类，这种方式可以自定义webservice的端口，但不要和服务器的端口冲突了。
 * 第二种方式：使用配置类，不用在启动类中加Endpoint.publish，这种接口的端口号和服务器端口号是一致的
 * @Author zhua
 * @Date 2020/8/13 11:15
 * @Version 1.0
 */

@Configuration
public class CxfConfig {

    @Autowired
    private Bus bus;

    @Autowired
    private MyWebService myWebService;

    /**
     * 此方法作用是改变项目中服务名的前缀名，此处127.0.0.1或者localhost不能访问时，请使用ipconfig查看本机ip来访问
     * 此方法被注释后:wsdl访问地址为http://127.0.0.1:8080/services/myWebService?wsdl
     * 去掉注释后：wsdl访问地址为：http://127.0.0.1:8080/soap/myWebService?wsdl
     *
     * @return
     */
    @Bean
    public ServletRegistrationBean disServlet() {
        ServletRegistrationBean servletRegistrationBean = new ServletRegistrationBean(new CXFServlet(), "/webService/*");
        return servletRegistrationBean;
    }

    /**
     * Apache CXF 核心架构是以BUS为核心，整合其他组件。
     * Bus是CXF的主干, 为共享资源提供一个可配置的场所，作用类似于Spring的ApplicationContext，这些共享资源包括
     * WSDl管理器、绑定工厂等。通过对BUS进行扩展，可以方便地容纳自己的资源，或者替换现有的资源。默认Bus实现基于Spring架构，
     * 通过依赖注入，在运行时将组件串联起来。BusFactory负责Bus的创建。默认的BusFactory是SpringBusFactory，对应于默认
     * 的Bus实现。在构造过程中，SpringBusFactory会搜索META-INF/cxf（包含在 CXF 的jar中）下的所有bean配置文件。
     * 根据这些配置文件构建一个ApplicationContext。开发者也可以提供自己的配置文件来定制Bus。
     */
    @Bean(name = Bus.DEFAULT_BUS_ID)
    public SpringBus springBus() {
        return new SpringBus();
    }

    /**
     * 此方法作用是改变项目中服务名的前缀名，此处127.0.0.1或者localhost不能访问时，请使用ipconfig查看本机ip来访问
     * 此方法被注释后, 即不改变前缀名(默认是services), wsdl访问地址为 http://127.0.0.1:8080/services/ws/api?wsdl
     * 去掉注释后wsdl访问地址为：http://127.0.0.1:8080/soap/ws/api?wsdl
     * http://127.0.0.1:8080/soap/列出服务列表 或 http://127.0.0.1:8080/soap/ws/api?wsdl 查看实际的服务
     * 新建Servlet记得需要在启动类添加注解：@ServletComponentScan
     *
     * 如果启动时出现错误：not loaded because DispatcherServlet Registration found non dispatcher servlet dispatcherServlet
     * 可能是springboot与cfx版本不兼容。
     * 同时在spring boot2.0.6之后的版本与xcf集成，不需要在定义以下方法，直接在application.properties配置文件中添加：
     * cxf.path=/service（默认是services）
     */
    @Bean
    public Endpoint endpoint() {
        EndpointImpl endpoint = new EndpointImpl(bus, myWebService);
        endpoint.publish("/sayHello");
        return endpoint;
    }
}
```