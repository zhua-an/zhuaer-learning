package com.zhuaer.learning.webserver.publish.config;

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
