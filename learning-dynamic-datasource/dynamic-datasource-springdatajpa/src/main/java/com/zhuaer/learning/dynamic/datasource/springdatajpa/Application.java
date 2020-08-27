package com.zhuaer.learning.dynamic.datasource.springdatajpa;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@SpringBootApplication
@EnableAspectJAutoProxy(proxyTargetClass = true, exposeProxy = true) // http://www.voidcn.com/article/p-zddcuyii-bpt.html
public class Application {
}
