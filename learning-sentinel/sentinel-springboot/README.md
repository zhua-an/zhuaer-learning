# learning-sentinel

## sentinel_dashboard的引入
[Sentinel 下载地址：https://github.com/alibaba/Sentinel/releases](https://github.com/alibaba/Sentinel/releases "Sentinel 下载地址：https://github.com/alibaba/Sentinel/releases")

由于dashboard是springboot的项目，在CMD模式下使用命令:

    java -Dserver.port=8080 -Dcsp.sentinel.dashboard.server=localhost:8080 -Dproject.name=sentinel-dashboard -jar sentinel-dashboard-1.8.0.jar


其中，*-Dserver.port=8080* 代表看板项目的端口号，*-Dcsp.sentinel.dashboard.server=localhost:8080* 代表向 Sentinel 接入端指定控制台的地址，本看板服务将会注册到自己的看板上，*-Dproject.name=sentinel-dashboard* 代表向 Sentinel 指定应用名称，比如上面对应的应用名称就为 sentinel-dashboard

经过上述配置，控制台启动后会自动向自己发送心跳。程序启动后浏览器访问 localhost:8080 即可访问 Sentinel 控制台。

从 Sentinel 1.6.0 开始，Sentinel 控制台支持简单的登录功能，默认用户名和密码都是 sentinel。用户可以通过如下参数进行配置：
- *-Dsentinel.dashboard.auth.username=sentinel* 用于指定控制台的登录用户名为 *sentinel*；
- *-Dsentinel.dashboard.auth.password=123456* 用于指定控制台的登录密码为 123456；如果省略这两个参数，默认用户和密码均为 sentinel；
- *-Dserver.servlet.session.timeout=7200* 用于指定 Spring Boot 服务端 session 的过期时间，如 7200 表示 7200 秒；60m 表示 60 分钟，默认为 30 分钟；

## 添加依赖
### spring cloud 的项目引入如下依赖：

    <!--<dependency>
    	<groupId>org.springframework.cloud</groupId>
    	<artifactId>spring-cloud-starter-alibaba-sentinel</artifactId>
    	<version>0.2.2.RELEASE</version>
    </dependency>-->
    <dependency>
    	<groupId>com.alibaba.cloud</groupId>
    	<artifactId>spring-cloud-starter-alibaba-sentinel</artifactId>
    </dependency>
    
### spring mvc 项目引入如下依赖：

    <dependency>
        <groupId>com.alibaba.csp</groupId>
        <artifactId>sentinel-core</artifactId>
        <version>1.8.0</version>
    </dependency>
    
使用注解的方式，还需要加入 Sentinel 的切面支持依赖

    <dependency>
        <groupId>com.alibaba.csp</groupId>
        <artifactId>sentinel-annotation-aspectj</artifactId>
        <version>x.y.z</version>
    </dependency>  
 
 Sentinel 提供对所有资源的实时监控。如果需要实时监控，客户端需引入以下依赖（以 Maven 为例）：
 
     <dependency>
         <groupId>com.alibaba.csp</groupId>
         <artifactId>sentinel-transport-simple-http</artifactId>
         <version>1.8.0</version>
     </dependency>

 
## 添加配置文件    
    spring:
      cloud:
        sentinel:
          transport:
            dashboard: localhost:8849
    #        port: 客户端端口（8719）
    #        client-ip: 客户端IP（指定部署springboot项目云服务器的外网IP）
    #     eager: true
    
 ## 编写配置类
     /**
      * 添加注解支持的配置
      * @return
      */
     @Bean
     public SentinelResourceAspect sentinelResourceAspect(){
     	return new SentinelResourceAspect();
     }
     
     /**
      * 配置自定义限流
      * @throws Exception
      */
     @PostConstruct
     private void initFlowRules() throws Exception {
     	FlowRule rule1 = new FlowRule();
     	rule1.setResource("test");
     	rule1.setGrade(RuleConstant.FLOW_GRADE_QPS);
     	rule1.setCount(1);   // 每秒调用最大次数为 1 次
     
     	List<FlowRule> rules = new ArrayList<>();
     	rules.add(rule1);
     
     	// 将控制规则载入到 Sentinel
     	com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager.loadRules(rules);
     }
     
## 限流注解使用

### java 代码中使用
资源 是 *Sentinel* 中的核心概念之一。最常用的资源是我们代码中的 Java 方法。 当然，您也可以更灵活的定义你的资源，例如，把需要控制流量的代码用 *Sentinel API SphU.entry("HelloWorld")* 和 *entry.exit()* 包围起来即可。在下面的例子中，我们将 System.out.println("hello world"); 作为资源（被保护的逻辑），用 API 包装起来。参考代码如下:

    public static void main(String[] args) {
        // 配置规则.
        initFlowRules();
    
        while (true) {
            // 1.5.0 版本开始可以直接利用 try-with-resources 特性，自动 exit entry
            try (Entry entry = SphU.entry("HelloWorld")) {
                // 被保护的逻辑
                System.out.println("hello world");
            } catch (BlockException ex) {
                    // 处理被流控的逻辑
                System.out.println("blocked!");
            }
        }
    }

SphO 提供 if-else 风格的 API。用这种方式，当资源发生了限流之后会返回 false，这个时候可以根据返回值，进行限流之后的逻辑处理。示例代码如下:

    // 资源名可使用任意有业务语义的字符串
    if (SphO.entry("自定义资源名")) {
    	// 务必保证finally会被执行
    	try {
    	  /**
    	  * 被保护的业务逻辑
    	  */
    	} finally {
    	  SphO.exit();
    	}
    } else {
    	// 资源访问阻止，被限流或被降级
    	// 进行相应的处理操作
    }

注意：SphO.entry(xxx) 需要与 SphO.exit()方法成对出现，匹配调用，位置正确，否则会导致调用链记录异常，抛出ErrorEntryFreeException` 异常。

完成以上两步后，代码端的改造就完成了。
接下来，通过流控规则来指定允许该资源通过的请求次数，例如下面的代码定义了资源 HelloWorld 每秒最多只能通过 20 个请求。

    private static void initFlowRules(){
        List<FlowRule> rules = new ArrayList<>();
        FlowRule rule = new FlowRule();
        rule.setResource("HelloWorld");
        rule.setGrade(RuleConstant.FLOW_GRADE_QPS);
        // Set limit QPS to 20.
        rule.setCount(20);
        rules.add(rule);
        FlowRuleManager.loadRules(rules);
    }
    
完成上面 3 步，Sentinel 就能够正常工作了。更多的信息可以参考 [使用文档](https://github.com/alibaba/Sentinel/wiki/%E5%A6%82%E4%BD%95%E4%BD%BF%E7%94%A8 "使用文档")。

#### 异步调用支持

Sentinel 支持异步调用链路的统计。在异步调用中，需要通过 SphU.asyncEntry(xxx) 方法定义资源，并通常需要在异步的回调函数中调用 exit 方法。以下是一个简单的示例：

    try {
        AsyncEntry entry = SphU.asyncEntry(resourceName);
    
        // 异步调用.
        doAsync(userId, result -> {
            try {
                // 在此处处理异步调用的结果.
            } finally {
                // 在回调结束后 exit.
                entry.exit();
            }
        });
    } catch (BlockException ex) {
        // Request blocked.
        // Handle the exception (e.g. retry or fallback).
    }
    
SphU.asyncEntry(xxx) 不会影响当前（调用线程）的 Context，因此以下两个 entry 在调用链上是平级关系（处于同一层），而不是嵌套关系：

    // 调用链类似于：
    // -parent
    // ---asyncResource
    // ---syncResource
    asyncEntry = SphU.asyncEntry(asyncResource);
    entry = SphU.entry(normalResource);
    
若在异步回调中需要嵌套其它的资源调用（无论是 entry 还是 asyncEntry），只需要借助 Sentinel 提供的上下文切换功能，在对应的地方通过 ContextUtil.runOnContext(context, f) 进行 Context 变换，将对应资源调用处的 Context 切换为生成的异步 Context，即可维持正确的调用链路关系。示例如下：

    public void handleResult(String result) {
        Entry entry = null;
        try {
            entry = SphU.entry("handleResultForAsync");
            // Handle your result here.
        } catch (BlockException ex) {
            // Blocked for the result handler.
        } finally {
            if (entry != null) {
                entry.exit();
            }
        }
    }
    
    public void someAsync() {
        try {
            AsyncEntry entry = SphU.asyncEntry(resourceName);
    
            // Asynchronous invocation.
            doAsync(userId, result -> {
                // 在异步回调中进行上下文变换，通过 AsyncEntry 的 getAsyncContext 方法获取异步 Context
                ContextUtil.runOnContext(entry.getAsyncContext(), () -> {
                    try {
                        // 此处嵌套正常的资源调用.
                        handleResult(result);
                    } finally {
                        entry.exit();
                    }
                });
            });
        } catch (BlockException ex) {
            // Request blocked.
            // Handle the exception (e.g. retry or fallback).
        }
    }

此时的调用链就类似于：

    -parent
    ---asyncInvocation
    -----handleResultForAsync


### 注解方式使用
在服务层定义资源，这里使用注解 @SentinelResource，@SentinelResource 注解用来标识资源是否被限流、降级。

    @GetMapping(value = "/test")
    @SentinelResource(value = "test", blockHandler = "exceptionHandler")
    public String test() {
    	return "sucessful";
    }
    
    // 限流与熔断处理
    /**
     * 降级方法，限流后应用
     * @return
     */
    public String exceptionHandler(BlockException ex){
    	System.out.println("进入熔断");
    	System.out.println("请求被限流,触发限流规则="+blockException.getRule().getResource());
    	return "handler->Exception ->success";
    }
    
 *SentinelResource* 注解源码如下：
 
     @Target({ElementType.METHOD})
     @Retention(RetentionPolicy.RUNTIME)
     @Inherited
     public @interface SentinelResource {
         String value() default "";
     
         EntryType entryType() default EntryType.OUT;
     
         int resourceType() default 0;
     
         String blockHandler() default "";
     
         Class<?>[] blockHandlerClass() default {};
     
         String fallback() default "";
     
         String defaultFallback() default "";
     
         Class<?>[] fallbackClass() default {};
     
         Class<? extends Throwable>[] exceptionsToTrace() default {Throwable.class};
     
         Class<? extends Throwable>[] exceptionsToIgnore() default {};
     }
     
#### @SentinelResource 注解
> 注意：注解方式埋点不支持 private 方法。

*@SentinelResource* 用于定义资源，并提供可选的异常处理和 `fallback` 配置项。 *@SentinelResource* 注解包含以下属性：
- `value`：资源名称，必需项（不能为空）
- `entryType`：entry 类型，可选项（默认为 `EntryType.OUT`）
- `blockHandler` / `blockHandlerClass`: `blockHandler` 对应处理 `BlockException` 的函数名称，可选项。`blockHandler` 函数访问范围需要是 public，返回类型需要与原方法相匹配，参数类型需要和原方法相匹配并且最后加一个额外的参数，类型为 `BlockException`。`blockHandler` 函数默认需要和原方法在同一个类中。若希望使用其他类的函数，则可以指定 `blockHandlerClass` 为对应的类的 Class 对象，注意对应的函数必需为 static 函数，否则无法解析。
- `fallback` / `fallbackClass`：`fallback` 函数名称，可选项，用于在抛出异常的时候提供 `fallback` 处理逻辑。`fallback` 函数可以针对所有类型的异常（除了 `exceptionsToIgnore` 里面排除掉的异常类型）进行处理。`fallback` 函数签名和位置要求：
	1. 返回值类型必须与原函数返回值类型一致；
	2. 方法参数列表需要和原函数一致，或者可以额外多一个 `Throwable` 类型的参数用于接收对应的异常。
	3. `fallback` 函数默认需要和原方法在同一个类中。若希望使用其他类的函数，则可以指定 `fallbackClass` 为对应的类的 Class 对象，注意对应的函数必需为 static 函数，否则无法解析。
- `defaultFallback`（since 1.6.0）：默认的 fallback 函数名称，可选项，通常用于通用的 fallback 逻辑（即可以用于很多服务或方法）。默认 fallback 函数可以针对所有类型的异常（除了 `exceptionsToIgnore` 里面排除掉的异常类型）进行处理。若同时配置了 fallback 和 defaultFallback，则只有 fallback 会生效。defaultFallback 函数签名要求：
	1. 返回值类型必须与原函数返回值类型一致；
	2. 方法参数列表需要为空，或者可以额外多一个 Throwable 类型的参数用于接收对应的异常。
	3. defaultFallback 函数默认需要和原方法在同一个类中。若希望使用其他类的函数，则可以指定 fallbackClass 为对应的类的 Class 对象，注意对应的函数必需为 static 函数，否则无法解析。
- `exceptionsToIgnore`（since 1.6.0）：用于指定哪些异常被排除掉，不会计入异常统计中，也不会进入 fallback 逻辑中，而是会原样抛出。
> 注：1.6.0 之前的版本 fallback 函数只针对降级异常（DegradeException）进行处理，不能针对业务异常进行处理

特别地，若 blockHandler 和 fallback 都进行了配置，则被限流降级而抛出 BlockException 时只会进入 blockHandler 处理逻辑。若未配置 blockHandler、fallback 和 defaultFallback，则被限流降级时会将 BlockException 直接抛出（若方法本身未定义 throws BlockException 则会被 JVM 包装一层 UndeclaredThrowableException）。

示例：

    public class TestService {
    
        // 对应的 `handleException` 函数需要位于 `ExceptionUtil` 类中，并且必须为 static 函数.
        @SentinelResource(value = "test", blockHandler = "handleException", blockHandlerClass = {ExceptionUtil.class})
        public void test() {
            System.out.println("Test");
        }
    
        // 原函数
        @SentinelResource(value = "hello", blockHandler = "exceptionHandler", fallback = "helloFallback")
        public String hello(long s) {
            return String.format("Hello at %d", s);
        }
        
        // Fallback 函数，函数签名与原函数一致或加一个 Throwable 类型的参数.
        public String helloFallback(long s) {
            return String.format("Halooooo %d", s);
        }
    
        // Block 异常处理函数，参数最后多一个 BlockException，其余与原函数一致.
        public String exceptionHandler(long s, BlockException ex) {
            // Do some log here.
            ex.printStackTrace();
            return "Oops, error occurred at " + s;
        }
    }

从 1.4.0 版本开始，注解方式定义资源支持自动统计业务异常，无需手动调用 `Tracer.trace(ex)` 来记录业务异常。Sentinel 1.4.0 以前的版本需要自行调用 Tracer.trace(ex) 来记录业务异常。

##### 配置

若您是通过 **Spring Cloud Alibaba** 接入的 Sentinel，则无需额外进行配置即可使用 @SentinelResource 注解。

若您的应用使用了 Spring AOP（无论是 Spring Boot 还是传统 Spring 应用），您需要通过配置的方式将 SentinelResourceAspect 注册为一个 Spring Bean：

    @Configuration
    public class SentinelAspectConfiguration {
    
        @Bean
        public SentinelResourceAspect sentinelResourceAspect() {
            return new SentinelResourceAspect();
        }
    }

#### 规则的种类
Sentinel 的所有规则都可以在内存态中动态地查询及修改，修改之后立即生效。同时 Sentinel 也提供相关 API，供您来定制自己的规则策略。

Sentinel 支持以下几种规则：**流量控制规则**、**熔断降级规则**、**系统保护规则**、**来源访问控制规则** 和 **热点参数规则**。

##### 流量控制规则 (FlowRule)
重要属性：

| Field  |  说明 | 默认值  |
| ------------ | ------------ | ------------ |
| resource  | 资源名，资源名是限流规则的作用对象  |   |
| count  |  限流阈值 |   |
| grade  |  限流阈值类型，QPS 模式（1）或并发线程数模式（0） | 	QPS 模式  |
| limitApp  | 	流控针对的调用来源  |  default，代表不区分调用来源 |
| strategy |  	调用关系限流策略：直接、链路、关联 | 根据资源本身（直接）  |
| controlBehavior  | 流控效果（直接拒绝/WarmUp/匀速+排队等待），不支持按调用关系限流  | 直接拒绝 |
| clusterMode  |  是否集群限流 | 否  |

同一个资源可以同时有多个限流规则，检查规则时会依次检查。

###### 通过代码定义流量控制规则
理解上面规则的定义之后，我们可以通过调用 FlowRuleManager.loadRules() 方法来用硬编码的方式定义流量控制规则，比如：

    private void initFlowQpsRule() {
        List<FlowRule> rules = new ArrayList<>();
        FlowRule rule = new FlowRule(resourceName);
        // set limit qps to 20
        rule.setCount(20);
        rule.setGrade(RuleConstant.FLOW_GRADE_QPS);
        rule.setLimitApp("default");
        rules.add(rule);
        FlowRuleManager.loadRules(rules);
    }

##### 熔断降级规则 (DegradeRule)

熔断降级规则包含下面几个重要的属性：

| Field  |  说明 | 默认值  |
| ------------ | ------------ | ------------ |
| resource  | 资源名，即规则的作用对象  |   |
| grade  |  熔断策略，支持慢调用比例/异常比例/异常数策略 | 慢调用比例  |
| count  |  慢调用比例模式下为慢调用临界 RT（超出该值计为慢调用）；异常比例/异常数模式下为对应的阈值 |  |
| timeWindow  | 熔断时长，单位为 s |  |
| minRequestAmount |  熔断触发的最小请求数，请求数小于该值时即使异常比率超出阈值也不会熔断（1.7.0 引入） | 5 |
| statIntervalMs  | 统计时长（单位为 ms），如 60*1000 代表分钟级（1.8.0 引入）  | 1000 ms |
| slowRatioThreshold  |  慢调用比例阈值，仅慢调用比例模式有效（1.8.0 引入） |   |

同一个资源可以同时有多个降级规则。

理解上面规则的定义之后，我们可以通过调用 `DegradeRuleManager.loadRules()` 方法来用硬编码的方式定义流量控制规则。

    private void initDegradeRule() {
        List<DegradeRule> rules = new ArrayList<>();
        DegradeRule rule = new DegradeRule();
        rule.setResource(KEY);
        // set threshold RT, 10 ms
        rule.setCount(10);
        rule.setGrade(RuleConstant.DEGRADE_GRADE_RT);
        rule.setTimeWindow(10);
        rules.add(rule);
        DegradeRuleManager.loadRules(rules);
    }

###### 熔断器事件监听

Sentinel 支持注册自定义的事件监听器监听熔断器状态变换事件（state change event）。示例：

    EventObserverRegistry.getInstance().addStateChangeObserver("logging",
        (prevState, newState, rule, snapshotValue) -> {
            if (newState == State.OPEN) {
                // 变换至 OPEN state 时会携带触发时的值
                System.err.println(String.format("%s -> OPEN at %d, snapshotValue=%.2f", prevState.name(),
                    TimeUtil.currentTimeMillis(), snapshotValue));
            } else {
                System.err.println(String.format("%s -> %s at %d", prevState.name(), newState.name(),
                    TimeUtil.currentTimeMillis()));
            }
        });

##### 系统保护规则 (SystemRule)

Sentinel 系统自适应限流从整体维度对应用入口流量进行控制，结合应用的 Load、CPU 使用率、总体平均 RT、入口 QPS 和并发线程数等几个维度的监控指标，通过自适应的流控策略，让系统的入口流量和系统的负载达到一个平衡，让系统尽可能跑在最大吞吐量的同时保证系统整体的稳定性。

系统规则包含下面几个重要的属性：

| Field  |  说明 | 默认值  |
| ------------ | ------------ | ------------ |
| highestSystemLoad  | load1 触发值，用于触发自适应控制阶段  | -1 (不生效)  |
| avgRt  |  所有入口流量的平均响应时间 | -1 (不生效)  |
| maxThread  |  入口流量的最大并发数 | -1 (不生效) |
| qps  | 所有入口资源的 QPS | -1 (不生效) |
| highestCpuUsage | 当前系统的 CPU 使用率（0.0-1.0） | -1 (不生效) |


理解上面规则的定义之后，我们可以通过调用 `SystemRuleManager.loadRules()` 方法来用硬编码的方式定义流量控制规则。

    private void initSystemRule() {
        List<SystemRule> rules = new ArrayList<>();
        SystemRule rule = new SystemRule();
        rule.setHighestSystemLoad(10);
        rules.add(rule);
        SystemRuleManager.loadRules(rules);
    }

注意系统规则只针对入口资源（EntryType=IN）生效。

## 问题
### 如果将控制台部署在公网，本机启动连接出现错误日志

    Failed to fetch metric from <http://IP:8719/metric?startTime=1599630567000&endTime=1599630573000&refetch=false> (ConnectionException: Connection timed out)

说明发送了**内网地址**，导致fetch拉取埋点信息不通

通过`System.setProperty(TransportConfig.HEARTBEAT_CLIENT_IP, split[0].trim());`设置心跳地址为外网地址解决这个问题

本质上是因为控制台主动通过接口来客户端拉信息，但若是访问不通，也是没辙，所以本地测试部在服务器上的控制台，除非外网映射

### 部署上去后发现可以访问通，且项目注册进来了，但没有任何调用信息，且没有任何规则信息

这个问题基础是因为部署到docker上的，之后debug源码，发现控制台调用客户端的地址是我根本没配过的，深入后发现如下代码段

    Runnable serverInitTask = new Runnable() {
    	int port;
    
    	{
    		try {
    			port = Integer.parseInt(TransportConfig.getPort());
    		} catch (Exception e) {
    			port = DEFAULT_PORT;
    		}
    	}
    
    	@Override
    	public void run() {
    		boolean success = false;
    		ServerSocket serverSocket = getServerSocketFromBasePort(port);
    
    		if (serverSocket != null) {
    			CommandCenterLog.info("[CommandCenter] Begin listening at port " + serverSocket.getLocalPort());
    			socketReference = serverSocket;
    			executor.submit(new ServerThread(serverSocket));
    			success = true;
    			port = serverSocket.getLocalPort();
    		} else {
    			CommandCenterLog.info("[CommandCenter] chooses port fail, http command center will not work");
    		}
    
    		if (!success) {
    			port = PORT_UNINITIALIZED;
    		}
    
    		TransportConfig.setRuntimePort(port);
    		executor.shutdown();
    	}
    
    };
    
    new Thread(serverInitTask).start();
    
该代码段的作用是为客户端在分配一个socketServer，之后的信息交互都是通过该服务提供的端口来提供；

这样一来客户端需要额外提供一个端口了，而部署的docker只暴露了1个服务端口，所以不可避免的会出现问题，以上是目前的思路，正在验证中

至于端口如何决定，它是用了一个简单的技巧，若设置了`csp.sentinel.api.port`配置项，则会取该配置端口，若没有设，则是默认端口8719；但如果你用的是官网的启动方式，那8719应该是被控制台占用了，所以进入小技巧getServerSocketFromBasePort方法，内容如下

    private static ServerSocket getServerSocketFromBasePort(int basePort) {
    	int tryCount = 0;
    	while (true) {
    		try {
    			ServerSocket server = new ServerSocket(basePort + tryCount / 3, 100);
    			server.setReuseAddress(true);
    			return server;
    		} catch (IOException e) {
    			tryCount++;
    			try {
    				TimeUnit.MILLISECONDS.sleep(30);
    			} catch (InterruptedException e1) {
    				break;
    			}
    		}
    	}
    	return null;
    }
    
它会循环尝试端口是否被占用，每个端口尝试三次，若被占用则取下一个+1端口，一直到可用的端口返回；所以如果我们的客户端应用放到了docker，而开放的端口只有一个，那就获取不了信息了

这里`csp.sentinel.api.port`配置项很容易理解成客户端的端口地址，因为启动也不会报错啥的，会误让我们误会这个参数可以不填，虽然文档上写着必填，但本地测试的时候可没影响-_-||，所有都注意了，**这个配置项是必填的**

还要注意一点，因为是socket连接，两边端口要一致，所以**docker端口号映射需要一样**