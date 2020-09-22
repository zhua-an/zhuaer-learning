# sentinel-nocas

Sentinel 当资源定义成功后可以动态增加各种流控降级规则。Sentinel 提供两种方式修改规则：
- 通过 API 直接修改 (`loadRules`)
- 通过 DataSource 适配不同数据源修改

手动通过 API 修改比较直观，可以通过以下几个 API 修改不同的规则：

    FlowRuleManager.loadRules(List<FlowRule> rules); // 修改流控规则
    DegradeRuleManager.loadRules(List<DegradeRule> rules); // 修改降级规则

手动修改规则（硬编码方式）一般仅用于测试和演示，生产上一般通过动态规则源的方式来动态管理规则。


## DataSource 扩展
上述 `loadRules()` 方法只接受内存态的规则对象，但更多时候规则存储在文件、数据库或者配置中心当中。`DataSource` 接口给我们提供了对接任意配置源的能力。相比直接通过 API 修改规则，实现 `DataSource` 接口是更加可靠的做法。

我们推荐**通过控制台设置规则后将规则推送到统一的规则中心，客户端实现 `ReadableDataSource` 接口端监听规则中心实时获取变更**，流程如下：
![sentinel-datasource](../../file/sentinel/sentinel-datasource.png "sentinel-datasource")

DataSource 扩展常见的实现方式有:
- 拉模式：客户端主动向某个规则管理中心定期轮询拉取规则，这个规则中心可以是 RDBMS、文件，甚至是 VCS 等。这样做的方式是简单，缺点是无法及时获取变更；
- 推模式：规则中心统一推送，客户端通过注册监听器的方式时刻监听变化，比如使用 Nacos、Zookeeper 等配置中心。这种方式有更好的实时性和一致性保证。

Sentinel 目前支持以下数据源扩展：
- Pull-based: 动态文件数据源、Consul, Eureka
- Push-based: ZooKeeper, Redis, Nacos, Apollo, etcd

## 拉模式拓展
实现拉模式的数据源最简单的方式是继承 `AutoRefreshDataSource` 抽象类，然后实现 `readSource()` 方法，在该方法里从指定数据源读取字符串格式的配置数据。比如 基于文件的数据源。

## 推模式拓展
实现推模式的数据源最简单的方式是继承 `AbstractDataSource` 抽象类，在其构造方法中添加监听器，并实现` readSource()` 从指定数据源读取字符串格式的配置数据。比如 基于 Nacos 的数据源

## 注册数据源
通常需要调用以下方法将数据源注册至指定的规则管理器中：

    ReadableDataSource<String, List<FlowRule>> flowRuleDataSource = new NacosDataSource<>(remoteAddress, groupId, dataId, parser);
    FlowRuleManager.register2Property(flowRuleDataSource.getProperty());

若不希望手动注册数据源，可以借助 Sentinel 的 `InitFunc` SPI 扩展接口。只需要实现自己的 `InitFunc` 接口，在 init 方法中编写注册数据源的逻辑。比如：

    public class DataSourceInitFunc implements InitFunc {
    
        @Override
        public void init() throws Exception {
            final String remoteAddress = "localhost";
            final String groupId = "Sentinel:Demo";
            final String dataId = "com.alibaba.csp.sentinel.demo.flow.rule";
    
            ReadableDataSource<String, List<FlowRule>> flowRuleDataSource = new NacosDataSource<>(remoteAddress, groupId, dataId,
                source -> JSON.parseObject(source, new TypeReference<List<FlowRule>>() {}));
            FlowRuleManager.register2Property(flowRuleDataSource.getProperty());
        }
    }

接着将对应的类名添加到位于资源目录（通常是 resource 目录）下的 META-INF/services 目录下的 com.alibaba.csp.sentinel.init.InitFunc 文件中

这样，当初次访问任意资源的时候，Sentinel 就可以自动去注册对应的数据源了。


## 示例
### API 模式：使用客户端规则 API 配置规则
Sentinel Dashboard 通过客户端自带的规则 API来实时查询和更改内存中的规则。
注意: 要使客户端具备规则 API，需在客户端引入以下依赖：

    <dependency>
        <groupId>com.alibaba.csp</groupId>
        <artifactId>sentinel-transport-simple-http</artifactId>
        <version>x.y.z</version>
    </dependency>

### 拉模式：使用文件配置规则
这个示例展示 Sentinel 是如何从文件获取规则信息的。FileRefreshableDataSource 会周期性的读取文件以获取规则，当文件有更新时会及时发现，并将规则更新到内存中。使用时只需添加以下依赖：

    <dependency>
        <groupId>com.alibaba.csp</groupId>
        <artifactId>sentinel-datasource-extension</artifactId>
        <version>x.y.z</version>
    </dependency>

### 推模式：使用 Nacos 配置规则
Nacos 是阿里中间件团队开源的服务发现和动态配置中心。Sentinel 针对 Nacos 作了适配，底层可以采用 Nacos 作为规则配置数据源。使用时只需添加以下依赖：

    <dependency>
        <groupId>com.alibaba.csp</groupId>
        <artifactId>sentinel-datasource-nacos</artifactId>
        <version>x.y.z</version>
    </dependency>

然后创建 NacosDataSource 并将其注册至对应的 RuleManager 上即可。比如：

    // remoteAddress 代表 Nacos 服务端的地址
    // groupId 和 dataId 对应 Nacos 中相应配置
    ReadableDataSource<String, List<FlowRule>> flowRuleDataSource = new NacosDataSource<>(remoteAddress, groupId, dataId,
        source -> JSON.parseObject(source, new TypeReference<List<FlowRule>>() {}));
    FlowRuleManager.register2Property(flowRuleDataSource.getProperty());

### 推模式：使用 ZooKeeper 配置规则
Sentinel 针对 ZooKeeper 作了相应适配，底层可以采用 ZooKeeper 作为规则配置数据源。使用时只需添加以下依赖：

    <dependency>
        <groupId>com.alibaba.csp</groupId>
        <artifactId>sentinel-datasource-zookeeper</artifactId>
        <version>x.y.z</version>
    </dependency>

然后创建 ZookeeperDataSource 并将其注册至对应的 RuleManager 上即可。比如：

    // remoteAddress 代表 ZooKeeper 服务端的地址
    // path 对应 ZK 中的数据路径
    ReadableDataSource<String, List<FlowRule>> flowRuleDataSource = new ZookeeperDataSource<>(remoteAddress, path, source -> JSON.parseObject(source, new TypeReference<List<FlowRule>>() {}));
    FlowRuleManager.register2Property(flowRuleDataSource.getProperty());

### 推模式：使用 Apollo 配置规则
Sentinel 针对 Apollo 作了相应适配，底层可以采用 Apollo 作为规则配置数据源。使用时只需添加以下依赖：

    <dependency>
        <groupId>com.alibaba.csp</groupId>
        <artifactId>sentinel-datasource-apollo</artifactId>
        <version>x.y.z</version>
    </dependency>

然后创建 ApolloDataSource 并将其注册至对应的 RuleManager 上即可。比如：

    // namespaceName 对应 Apollo 的命名空间名称
    // ruleKey 对应规则存储的 key
    // defaultRules 对应连接不上 Apollo 时的默认规则
    ReadableDataSource<String, List<FlowRule>> flowRuleDataSource = new ApolloDataSource<>(namespaceName, ruleKey, defaultRules, source -> JSON.parseObject(source, new TypeReference<List<FlowRule>>() {}));
    FlowRuleManager.register2Property(flowRuleDataSource.getProperty());

### 推模式：使用 Redis 配置规则
Sentinel 针对 Redis 作了相应适配，底层可以采用 Redis 作为规则配置数据源。使用时只需添加以下依赖：

    <!-- 仅支持 JDK 1.8+ -->
    <dependency>
        <groupId>com.alibaba.csp</groupId>
        <artifactId>sentinel-datasource-redis</artifactId>
        <version>x.y.z</version>
    </dependency>

## 实战

### 方式一

#### 引入依赖

    <dependency>
    	<groupId>org.springframework.boot</groupId>
    	<artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    <dependency>
    	<groupId>com.alibaba.csp</groupId>
    	<artifactId>sentinel-datasource-nacos</artifactId>
    	<version>1.8.0</version>
    </dependency>
    <dependency>
    	<groupId>org.springframework.cloud</groupId>
    	<artifactId>spring-cloud-starter-alibaba-sentinel</artifactId>
    </dependency>
    
### 修改application.yml

    spring:
      cloud:
        sentinel:
          transport:
            dashboard: localhost:8849
          datasource:
            ds:
              nacos:
                server-addr: 127.0.0.1:8848
                data-id: Sentinel:Demo
                namespace: sentinel
                group-id: DEFAULT_GROUP
                data-type: json
                rule-type: flow

或者

    spring.cloud.sentinel.datasource.ds.nacos.server-addr=127.0.0.1:8848
    spring.cloud.sentinel.datasource.ds.nacos.data-id=gateway-sentinel-degrade.properties
    spring.cloud.sentinel.datasource.ds.nacos.namespace=0e152861-1efa-62ea-9125-e569abc29691
    spring.cloud.sentinel.datasource.ds.nacos.group-id=DEFAULT_GROUP
    spring.cloud.sentinel.datasource.ds.nacos.data-type=json
    spring.cloud.sentinel.datasource.ds.nacos.rule-type=flow
    
- spring.cloud.sentinel.transport.dashboard：sentinel dashboard的访问地址，根据上面准备工作中启动的实例配置
- spring.cloud.sentinel.datasource.ds.nacos.server-addr：nacos的访问地址，，根据上面准备工作中启动的实例配置
- spring.cloud.sentinel.datasource.ds.nacos.groupId：nacos中存储规则的groupId
- spring.cloud.sentinel.datasource.ds.nacos.dataId：nacos中存储规则的dataId
- spring.cloud.sentinel.datasource.ds.nacos.rule-type：该参数是spring cloud alibaba升级到0.2.2之后增加的配置，用来定义存储的规则类型。所有的规则类型可查看枚举类：org.springframework.cloud.alibaba.sentinel.datasource.RuleType，每种规则的定义格式可以通过各枚举值中定义的规则对象来查看，比如限流规则可查看：com.alibaba.csp.sentinel.slots.block.flow.FlowRule

#### nacos发布配置

    [
      {
        "resource": "/test/test",
        "limitApp": "default",
        "grade": 1,
        "count": 5,
        "strategy": 0,
        "controlBehavior": 0,
        "clusterMode": false
      }
    ]

- `resource`：资源名，即限流规则的作用对象
- `limitApp`：流控针对的调用来源，若为 default 则不区分调用来源
- `grade`：限流阈值类型（QPS 或并发线程数）；0代表根据并发数量来限流，1代表根据QPS来进行流量控制
- `count`：限流阈值
- `strategy`：调用关系限流策略
- `controlBehavior`：流量控制效果（直接拒绝、Warm Up、匀速排队）
- `clusterMode`：是否为集群模式

### 方式二

#### 引入依赖

    <dependency>
    	<groupId>org.springframework.boot</groupId>
    	<artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    <dependency>
    	<groupId>com.alibaba.csp</groupId>
    	<artifactId>sentinel-datasource-nacos</artifactId>
    	<version>1.8.0</version>
    </dependency>
    <dependency>
    	<groupId>com.alibaba.csp</groupId>
    	<artifactId>sentinel-core</artifactId>
    	<version>1.8.0</version>
    </dependency>
    <dependency>
    	<groupId>com.alibaba.csp</groupId>
    	<artifactId>sentinel-transport-simple-http</artifactId>
    	<version>1.8.0</version>
    </dependency>
    <dependency>
    	<groupId>com.alibaba.csp</groupId>
    	<artifactId>sentinel-annotation-aspectj</artifactId>
    	<version>1.8.0</version>
    </dependency>
    
### 修改application.yml

    spring:
      cloud:
        sentinel:
          transport:
            dashboard: localhost:8849
            
### 添加配置文件**SentinelConfig.java**

    @Configuration
    public class SentinelConfig {
        // nacos server ip
        private static final String remoteAddress = "localhost:8848";
        // nacos group
        private static final String groupId = "DEFAULT_GROUP";
        // nacos dataId
        private static final String dataId = "Sentinel:Demo";
        // fill your namespace id,if you want to use namespace. for example: 0f5c7314-4983-4022-ad5a-347de1d1057d,you can get it on nacos's console
        private static final String NACOS_NAMESPACE_ID = "sentinel";
    
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
    //    @PostConstruct
    //    private void initRules() throws Exception {
    //        FlowRule rule1 = new FlowRule();
    //        rule1.setResource("/test/hello");
    //        rule1.setGrade(RuleConstant.FLOW_GRADE_QPS);
    //        rule1.setCount(1);   // 每秒调用最大次数为 1 次
    //
    //        List<FlowRule> rules = new ArrayList<>();
    //        rules.add(rule1);
    //
    //        // 将控制规则载入到 Sentinel
    //        com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager.loadRules(rules);
    //    }
    
        /**
         * 动态配置
         */
        @PostConstruct
        private void loadMyNamespaceRules() {
            Properties properties = new Properties();
            properties.put(PropertyKeyConst.SERVER_ADDR, remoteAddress);
            properties.put(PropertyKeyConst.NAMESPACE, NACOS_NAMESPACE_ID);
    
            ReadableDataSource<String, List<FlowRule>> flowRuleDataSource = new NacosDataSource<>(properties, groupId, dataId,
                    source -> JSON.parseObject(source, new TypeReference<List<FlowRule>>() {
                    }));
            FlowRuleManager.register2Property(flowRuleDataSource.getProperty());
        }
    
    }
    