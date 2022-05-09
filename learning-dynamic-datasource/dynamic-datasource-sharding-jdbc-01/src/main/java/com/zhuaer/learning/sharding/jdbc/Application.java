package com.zhuaer.learning.sharding.jdbc;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@SpringBootApplication
@MapperScan(basePackages = "com.zhuaer.learning.sharding.jdbc.mapper")
@EnableAspectJAutoProxy(exposeProxy = true)
public class Application {
}
