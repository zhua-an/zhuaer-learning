# Springboot 整合 redis 限流

**巧用SpringBoot优雅解决分布式限流**

## 分布式限流

单机版中我们了解到 AtomicInteger、RateLimiter、Semaphore 这几种解决方案，但它们也仅仅是单机的解决手段，在集群环境下就透心凉了，后面又讲述了 Nginx 的限流手段，可它又属于网关层面的策略之一，并不能解决所有问题。例如供短信接口，你无法保证消费方是否会做好限流控制，所以自己在应用层实现限流还是很有必要的。

利用 自定义注解、Spring Aop、Redis Cache 实现分布式限流

## 导入依赖

```xml
<dependencies>
    <!-- 默认就内嵌了Tomcat 容器，如需要更换容器也极其简单-->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-aop</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-redis</artifactId>
    </dependency>
    <dependency>
        <groupId>com.google.guava</groupId>
        <artifactId>guava</artifactId>
        <version>21.0</version>
    </dependency>
    <dependency>
        <groupId>org.apache.commons</groupId>
        <artifactId>commons-lang3</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-test</artifactId>
    </dependency>
</dependencies>
```

## 属性配置

在 `application.properites` 资源文件中添加 redis 相关的配置项

```yaml
spring:
  application:
    name: @artifactId@
  redis:
    #Redis数据库索引（默认为0）
    database: 5
    host: 127.0.0.1
    password: 
    port: 6379
    jedis:
      pool:
        # 连接池最大连接数（使用负值表示没有限制）
        max-active: 32
        # 连接池中的最大空闲连接
        max-idle: 20
        # 连接池中的最小空闲连接
        min-idle: 5
        # 连接池最大阻塞等待时间（使用负值表示没有限制）
        max-wait: 100ms
    # 连接超时时间（毫秒）默认是2000ms
    timeout: 10000ms
  main:
    allow-bean-definition-overriding: true
```

## Limit 注解

创建一个 Limit 注解

```java

import java.lang.annotation.*;

/**
 * @ClassName Limit
 * @Description 限流
 * @Author zhua
 * @Date 2021/9/28 11:15
 * @Version 1.0
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface Limit {

    /**
     * 资源的名字
     *
     * @return String
     */
    String name() default "";

    /**
     * 资源的key
     *
     * @return String
     */
    String key() default "";

    /**
     * Key的prefix
     *
     * @return String
     */
    String prefix() default "";

    /**
     * 给定的时间段
     * 单位秒
     *
     * @return int
     */
    int period();

    /**
     * 最多的访问限制次数
     *
     * @return int
     */
    int count();

    /**
     * 类型
     *
     * @return LimitType
     */
    LimitType limitType() default LimitType.CUSTOMER;
}
```

## 限流类型

定义限流类型

```java
public enum LimitType {
    /**
     * 自定义key
     */
    CUSTOMER,
    /**
     * 根据请求者IP
     */
    IP;
}
```

## RedisTemplate

默认情况下 `spring-boot-data-redis` 为我们提供了`StringRedisTemplate` 但是满足不了其它类型的转换，所以还是得自己去定义其它类型的模板….

```java
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.io.Serializable;

/**
 * @ClassName RedisLimiterConfig
 * @Description TODO
 * @Author zhua
 * @Date 2021/9/28 11:17
 * @Version 1.0
 */
@Configuration
public class RedisLimiterConfiguration {

    @Bean
    public RedisTemplate<String, Serializable> limitRedisTemplate(LettuceConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, Serializable> template = new RedisTemplate<>();
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        template.setConnectionFactory(redisConnectionFactory);
        return template;
    }
}

```

## Limit 拦截器（AOP）

熟悉 Redis 的朋友都知道它是线程安全的，我们利用它的特性可以实现分布式锁、分布式限流等组件，限流相比分布式锁稍微复杂一点，官方虽然没有提供相应的API，但却提供了支持 Lua 脚本的功能，我们可以通过编写 Lua 脚本实现自己的API，同时他是满足原子性的….

下面核心就是调用 execute 方法传入我们的 Lua 脚本内容，然后通过返回值判断是否超出我们预期的范围，超出则给出错误提示。

```java
import com.google.common.collect.ImmutableList;
import com.zhuaer.learning.redis.limit.annotation.Limit;
import com.zhuaer.learning.redis.limit.constants.LimitType;
import com.zhuaer.learning.redis.limit.exception.RedisLimitException;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.io.Serializable;
import java.lang.reflect.Method;

/**
 * @ClassName RedisLimitInterceptor
 * @Description TODO
 * @Author zhua
 * @Date 2021/9/28 11:19
 * @Version 1.0
 */
@Aspect
@Configuration
public class RedisLimitInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(RedisLimitInterceptor.class);

    private final RedisTemplate<String, Serializable> limitRedisTemplate;

    @Autowired
    public RedisLimitInterceptor(RedisTemplate<String, Serializable> limitRedisTemplate) {
        this.limitRedisTemplate = limitRedisTemplate;
    }


    @Around("execution(public * *(..)) && @annotation(com.zhuaer.learning.redis.limit.annotation.Limit)")
    public Object interceptor(ProceedingJoinPoint pjp) {
        MethodSignature signature = (MethodSignature) pjp.getSignature();
        Method method = signature.getMethod();
        Limit limitAnnotation = method.getAnnotation(Limit.class);
        LimitType limitType = limitAnnotation.limitType();
        String name = limitAnnotation.name();
        String key;
        int limitPeriod = limitAnnotation.period();
        int limitCount = limitAnnotation.count();
        switch (limitType) {
            case IP:
                key = getIpAddress();
                break;
            case CUSTOMER:
                key = limitAnnotation.key();
                break;
            default:
                key = StringUtils.upperCase(method.getName());
        }
        ImmutableList<String> keys = ImmutableList.of(StringUtils.join(limitAnnotation.prefix(), key));
        try {
            String luaScript = buildLuaScript();
            RedisScript<Number> redisScript = new DefaultRedisScript<>(luaScript, Number.class);
            Number count = limitRedisTemplate.execute(redisScript, keys, limitCount, limitPeriod);
            logger.info("Access try count is {} for name={} and key = {}", count, name, key);
            if (count != null && count.intValue() <= limitCount) {
                return pjp.proceed();
            } else {
                throw new RedisLimitException("You have been dragged into the blacklist");
            }
        } catch (Throwable e) {
            if (e instanceof RuntimeException) {
                throw new RedisLimitException(e.getLocalizedMessage());
            }
            throw new RedisLimitException("server exception");
        }
    }

    /**
     * 限流 脚本
     *
     * @return lua脚本
     */
    public String buildLuaScript() {
        StringBuilder lua = new StringBuilder();
        lua.append("local c");
        lua.append("\nc = redis.call('get',KEYS[1])");
        // 调用不超过最大值，则直接返回
        lua.append("\nif c and tonumber(c) > tonumber(ARGV[1]) then");
        lua.append("\nreturn c;");
        lua.append("\nend");
        // 执行计算器自加
        lua.append("\nc = redis.call('incr',KEYS[1])");
        lua.append("\nif tonumber(c) == 1 then");
        // 从第一次调用开始限流，设置对应键值的过期
        lua.append("\nredis.call('expire',KEYS[1],ARGV[2])");
        lua.append("\nend");
        lua.append("\nreturn c;");
        return lua.toString();
    }

    private static final String UNKNOWN = "unknown";

    public String getIpAddress() {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        String ip = request.getHeader("x-forwarded-for");
        if (ip == null || ip.length() == 0 || UNKNOWN.equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || UNKNOWN.equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || UNKNOWN.equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}

```

## 自定义异常

在应用开发过程中，除系统自身的异常外，不同业务场景中用到的异常也不一样，为了与标题 轻松搞定全局异常 更加的贴切，定义个自己的异常，看看如何捕获…

```java
/**
 * @ClassName RedisLimitException
 * @Description TODO
 * @Author zhua
 * @Date 2021/9/28 11:24
 * @Version 1.0
 */
public class RedisLimitException extends RuntimeException{

    private int code;

    public RedisLimitException() {
        super();
    }

    public RedisLimitException(String message) {
        super(message);
        this.code = 500;
    }

    public RedisLimitException(int code, String message) {
        super(message);
        this.setCode(code);
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }
}
```

## 异常信息模板

定义返回的异常信息的格式，这样异常信息风格更为统一

```java
/**
 * @ClassName ErrorResponseEntity
 * @Description 异常信息模板
 * @Author zhua
 * @Date 2021/9/28 11:28
 * @Version 1.0
 */
public class ErrorResponseEntity {

    private int code;
    private String message;

    public ErrorResponseEntity(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}

```

## 异常处理（关键）

> 注解概述

- @ControllerAdvice 捕获 Controller 层抛出的异常，如果添加 @ResponseBody 返回信息则为JSON 格式。
- @RestControllerAdvice 相当于 @ControllerAdvice 与 @ResponseBody 的结合体。
- @ExceptionHandler 统一处理一种类的异常，减少代码重复率，降低复杂度。

创建一个 `GlobalExceptionHandler` 类，并添加上 `@RestControllerAdvice` 注解就可以定义出异常通知类了，然后在定义的方法中添加上 `@ExceptionHandler` 即可实现异常的捕捉…

```java
import com.zhuaer.learning.redis.limit.exception.ErrorResponseEntity;
import com.zhuaer.learning.redis.limit.exception.RedisLimitException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @ClassName GlobalExceptionHandler
 * @Description 全局异常处理
 * @Author zhua
 * @Date 2021/9/28 11:26
 * @Version 1.0
 */
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    /**
     * 定义要捕获的异常 可以多个 @ExceptionHandler({})
     *
     * @param request  request
     * @param e        exception
     * @param response response
     * @return 响应结果
     */
    @ExceptionHandler(RedisLimitException.class)
    public ErrorResponseEntity redisLimitExceptionHandler(HttpServletRequest request, final Exception e, HttpServletResponse response) {
        response.setStatus(HttpStatus.BAD_REQUEST.value());
        RedisLimitException exception = (RedisLimitException) e;
        return new ErrorResponseEntity(exception.getCode(), exception.getMessage());
    }

    /**
     * 捕获  RuntimeException 异常
     * TODO  如果你觉得在一个 exceptionHandler 通过  if (e instanceof xxxException) 太麻烦
     * TODO  那么你还可以自己写多个不同的 exceptionHandler 处理不同异常
     *
     * @param request  request
     * @param e        exception
     * @param response response
     * @return 响应结果
     */
    @ExceptionHandler(RuntimeException.class)
    public ErrorResponseEntity runtimeExceptionHandler(HttpServletRequest request, final Exception e, HttpServletResponse response) {
        response.setStatus(HttpStatus.BAD_REQUEST.value());
        RuntimeException exception = (RuntimeException) e;
        return new ErrorResponseEntity(400, exception.getMessage());
    }

    /**
     * 通用的接口映射异常处理方
     */
    @Override
    protected ResponseEntity<Object> handleExceptionInternal(Exception ex, Object body, HttpHeaders headers,
                                                             HttpStatus status, WebRequest request) {
        if (ex instanceof MethodArgumentNotValidException) {
            MethodArgumentNotValidException exception = (MethodArgumentNotValidException) ex;
            return new ResponseEntity<>(new ErrorResponseEntity(status.value(), exception.getBindingResult().getAllErrors().get(0).getDefaultMessage()), status);
        }
        if (ex instanceof MethodArgumentTypeMismatchException) {
            MethodArgumentTypeMismatchException exception = (MethodArgumentTypeMismatchException) ex;
            logger.error("参数转换失败，方法：" + exception.getParameter().getMethod().getName() + "，参数：" + exception.getName()
                    + ",信息：" + exception.getLocalizedMessage());
            return new ResponseEntity<>(new ErrorResponseEntity(status.value(), "参数转换失败"), status);
        }
        return new ResponseEntity<>(new ErrorResponseEntity(status.value(), "参数转换失败"), status);
    }

}
```

## 控制层

在接口上添加 `@Limit()` 注解，如下代码会在 Redis 中生成过期时间为 100s 的 key = test 的记录，特意定义了一个 `AtomicInteger` 用作测试…

```java
import com.zhuaer.learning.redis.limit.annotation.Limit;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @ClassName LimiterController
 * @Description TODO
 * @Author zhua
 * @Date 2021/9/28 11:23
 * @Version 1.0
 */
@RestController
public class LimiterController {

    private static final AtomicInteger ATOMIC_INTEGER = new AtomicInteger();

    @Limit(key = "test", period = 100, count = 10)
    @GetMapping("/test")
    public int testLimiter() {
        // 意味著 100S 内最多允許訪問10次
        return ATOMIC_INTEGER.incrementAndGet();
    }
}
```


