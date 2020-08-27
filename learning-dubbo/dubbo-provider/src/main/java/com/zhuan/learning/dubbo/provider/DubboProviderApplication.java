package com.zhuaer.learning.dubbo.provider;

import org.apache.dubbo.config.spring.context.annotation.DubboComponentScan;
import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @ClassName DubboProviderApplication
 * @Description TODO
 * @Author zhua
 * @Date 2020/7/17 9:42
 * @Version 1.0
 */
@SpringBootApplication
//@EnableDubbo  //会扫描所有的包，从中找出dubbo的@Service标注的类
//@DubboComponentScan(basePackages = "com.zhuaer.learning.dubbo.provider.service")  //只扫描指定的包
public class DubboProviderApplication {

    public static void main(String[] args) {
        SpringApplication.run(DubboProviderApplication.class, args);
    }
}
