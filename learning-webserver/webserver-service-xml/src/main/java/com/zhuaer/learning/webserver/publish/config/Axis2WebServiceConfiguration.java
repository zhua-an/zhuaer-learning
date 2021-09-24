package com.zhuaer.learning.webserver.publish.config;

import com.zhuaer.learning.webserver.publish.util.FileCopyUtils;
import org.apache.axis2.transport.http.AxisServlet;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

/**
 * @ClassName Axis2WebServiceConfiguration
 * @Description TODO
 * @Author zhua
 * @Date 2021/9/24 9:02
 * @Version 1.0
 */
@Configuration
public class Axis2WebServiceConfiguration {

    @Bean
    public ServletRegistrationBean axis2Servlet() {
        ServletRegistrationBean servletRegistrationBean = new ServletRegistrationBean();
        servletRegistrationBean.setServlet(new AxisServlet());
        servletRegistrationBean.addUrlMappings("/services/*");
        // 通过默认路径无法找到services.xml，这里需要指定一下路径，且必须是绝对路径
        String path = this.getClass().getResource("/WEB-INF").getPath().toString();
        if (path.toLowerCase().startsWith("file:")) {
            path = path.substring(5);
        }
        if (path.indexOf("!") != -1) {
            try {
                FileCopyUtils.copy("WEB-INF/services/axis2/META-INF/services.xml");
            } catch (IOException e) {
                e.printStackTrace();
            }
            path = path.substring(0, path.lastIndexOf("/", path.indexOf("!"))) + "/WEB-INF";
        }
        //System.out.println("xml配置文件,path={ "+path+" }");
        servletRegistrationBean.addInitParameter("axis2.repository.path", path);
        servletRegistrationBean.setLoadOnStartup(1);
        return servletRegistrationBean;
    }
}
