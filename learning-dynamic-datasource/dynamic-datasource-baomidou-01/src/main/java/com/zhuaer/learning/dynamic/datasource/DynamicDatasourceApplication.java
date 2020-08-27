package com.zhuaer.learning.dynamic.datasource;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

/**
 * @ClassName DynamicDatasourceApplication
 * @Description TODO
 * @Author zhua
 * @Date 2020/7/21 10:44
 * @Version 1.0
 */
@SpringBootApplication
@MapperScan(basePackages = "com.zhuaer.learning.dynamic.datasource.mapper")
@EnableAspectJAutoProxy(exposeProxy = true) // http://www.voidcn.com/article/p-zddcuyii-bpt.html
public class DynamicDatasourceApplication {

    public static void main(String[] args) {
        SpringApplication.run(DynamicDatasourceApplication.class, args);
    }

}
