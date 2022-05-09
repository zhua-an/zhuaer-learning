# learning-dynamic-datasource

## 概述

在项目中，我们可能会碰到需要多数据源的场景。例如说：

- 读写分离：数据库主节点压力比较大，需要增加从节点提供读操作，以减少压力。
- 多数据源：一个复杂的单体项目，因为没有拆分成不同的服务，需要连接多个业务的数据源。

本质上，读写分离，仅仅是多数据源的一个场景，从节点是只提供读操作的数据源。所以只要实现了多数据源的功能，也就能够提供读写分离。

## 实现方式

### 方案一

**基于 Spring AbstractRoutingDataSource 做拓展**

简单来说，通过继承 AbstractRoutingDataSource 抽象类，实现一个管理项目中多个 DataSource 的动态 DynamicRoutingDataSource 实现类。这样，Spring 在获取数据源时，可以通过 DynamicRoutingDataSource 返回实际的 DataSource 。

然后，我们可以自定义一个 `@DS` 注解，可以添加在 Service 方法、Dao 方法上，表示其实际对应的 DataSource 。

如此，整个过程就变成，执行数据操作时，通过“配置”的 @DS 注解，使用 DynamicRoutingDataSource 获得对应的实际的 DataSource 。之后，在通过该 DataSource 获得 Connection 连接，最后发起数据库操作。

### 方案二

**不同操作类，固定数据源**

关于这个方案，解释起来略有点晦涩。以 MyBatis 举例子，假设有 orders 和 users 两个数据源。那么我们可以创建两个 SqlSessionTemplate ordersSqlSessionTemplate 和 usersSqlSessionTemplate ，分别使用这两个数据源。

然后，配置不同的 Mapper 使用不同的 `SqlSessionTemplate` 。

如此，整个过程就变成，执行数据操作时，通过 Mapper 可以对应到其  SqlSessionTemplate ，使用 SqlSessionTemplate 获得对应的实际的 DataSource 。之后，在通过该 DataSource 获得 Connection 连接，最后发起数据库操作。

### 方案三

**分库分表中间件**

对于分库分表的中间件，会解析我们编写的 SQL ，路由操作到对应的数据源。那么，它们天然就支持多数据源。如此，我们仅需配置好每个表对应的数据源，中间件就可以透明的实现多数据源或者读写分离。

目前，Java 最好用的分库分表中间件，就是 Apache ShardingSphere ，没有之一。

## baomidou 多数据源

> 示例代码对应仓库：dynamic-datasource-baomidou-01

1. 引入相关依赖

```xml
<!-- 实现对 dynamic-datasource 的自动化配置 -->
<dependency>
	<groupId>com.baomidou</groupId>
	<artifactId>dynamic-datasource-spring-boot-starter</artifactId>
	<version>2.5.7</version>
</dependency>
```
2. 启动类配置扫描路径

```java
@SpringBootApplication
@MapperScan(basePackages = "com.zhuaer.learning.dynamic.datasource.mapper")
@EnableAspectJAutoProxy(exposeProxy = true)
public class DynamicDatasourceApplication {

    public static void main(String[] args) {
        SpringApplication.run(DynamicDatasourceApplication.class, args);
    }

}
```

- 添加 @MapperScan 注解，com.zhuaer.learning.dynamic.datasource.mapper 包路径下，就是我们 Mapper 接口所在的包路径。
- 添加 @EnableAspectJAutoProxy 注解，重点是配置 exposeProxy = true ，因为我们希望 Spring AOP 能将当前代理对象设置到 AopContext 中。

3. 应用配置文件

在 resources 目录下，创建 application.yaml 配置文件。配置如下：

```yaml
spring:
  datasource:
    # dynamic-datasource-spring-boot-starter 动态数据源的配置内容
    dynamic:
      primary: users # 设置默认的数据源或者数据源组，默认值即为 master
      datasource:
        # 订单 orders 数据源配置
        orders:
          url: jdbc:mysql://127.0.0.1:3306/dynamic_datasource01?useSSL=false&useUnicode=true&characterEncoding=UTF-8
          driver-class-name: com.mysql.jdbc.Driver
          username: root
          password:
        # 用户 users 数据源配置
        users:
          url: jdbc:mysql://127.0.0.1:3306/dynamic_datasource02?useSSL=false&useUnicode=true&characterEncoding=UTF-8
          driver-class-name: com.mysql.jdbc.Driver
          username: root
          password:

# mybatis 配置内容
mybatis:
  config-location: classpath:mybatis-config.xml # 配置 MyBatis 配置文件路径
  mapper-locations: classpath:mapper/*.xml # 配置 Mapper XML 地址
  type-aliases-package: com.zhuaer.learning.dynamic.datasource.dataobject # 配置数据库实体包路径
```

- spring.datasource.dynamic 配置项，设置 dynamic-datasource-spring-boot-starter 动态数据源的配置内容。
  - primary 配置项，设置默认的数据源或者数据源组，默认值即为 master 。 
  - datasource 配置项，配置每个动态数据源。这里，我们配置了 orders、users 两个动态数据源。
- mybatis 配置项，设置 mybatis-spring-boot-starter MyBatis 的配置内容。

4. mapper 上使用 @DS 注解

```java
// OrderMapper.java
@Repository
@DS(DBConstants.DATASOURCE_ORDERS)
public interface OrderMapper {

    OrderDO selectById(@Param("id") Integer id);

}

// UserMapper.java
@Repository
@DS(DBConstants.DATASOURCE_USERS)
public interface UserMapper {

    UserDO selectById(@Param("id") Integer id);

}
```

- DBConstants.java 类，枚举了 DATASOURCE_ORDERS 和 DATASOURCE_USERS 两个数据源。
- @DS 注解，是 dynamic-datasource-spring-boot-starter 提供，可添加在 Service 或 Mapper 的类/接口上，或者方法上。在其 value 属性种，填写数据源的名字。
    - OrderMapper 接口上，我们添加了 @DS(DBConstants.DATASOURCE_ORDERS) 注解，访问 orders 数据源。
    - UserMapper 接口上，我们添加了 @DS(DBConstants.DATASOURCE_USERS) 注解，访问 users 数据源。

5. 简单测试


## baomidou 读写分离

> 示例代码对应仓库：dynamic-datasource-baomidou-02

1. 引入相关依赖

与 [baomidou 多数据源 1] 一致

2. 启动类配置扫描路径

与 [baomidou 多数据源 2] 一致

3. 应用配置文件

在 resources 目录下，创建 application.yaml 配置文件。配置如下：

```yaml
spring:
  datasource:
    # dynamic-datasource-spring-boot-starter 动态数据源的配置内容
    dynamic:
      primary: master # 设置默认的数据源或者数据源组，默认值即为 master
      datasource:
        # 订单 orders 主库的数据源配置
        master:
          url: jdbc:mysql://127.0.0.1:3306/test_orders?useSSL=false&useUnicode=true&characterEncoding=UTF-8
          driver-class-name: com.mysql.jdbc.Driver
          username: root
          password:
        # 订单 orders 从库数据源配置
        slave_1:
          url: jdbc:mysql://127.0.0.1:3306/test_orders_01?useSSL=false&useUnicode=true&characterEncoding=UTF-8
          driver-class-name: com.mysql.jdbc.Driver
          username: root
          password:
        # 订单 orders 从库数据源配置
        slave_2:
          url: jdbc:mysql://127.0.0.1:3306/test_orders_02?useSSL=false&useUnicode=true&characterEncoding=UTF-8
          driver-class-name: com.mysql.jdbc.Driver
          username: root
          password:

# mybatis 配置内容
mybatis:
  config-location: classpath:mybatis-config.xml # 配置 MyBatis 配置文件路径
  mapper-locations: classpath:mapper/*.xml # 配置 Mapper XML 地址
  type-aliases-package: com.zhuaer.learning.dynamic.datasource.dataobject # 配置数据库实体包路径
```

- 相比 「baomidou 多数据源 3」 来说，我们配置了订单库的多个数据源：
    - master ：订单库的主库。
    - slave_1 和 slave_2 ：订单库的两个从库。
- 在 dynamic-datasource-spring-boot-starter 中，多个相同角色的数据源可以形成一个数据源组。判断标准是，数据源名以下划线 _ 分隔后的首部即为组名。例如说，slave_1 和  slave_2 形成了 slave 组。
    - 我们可以使用 @DS("slave_1") 或 @DS("slave_2") 注解，明确访问数据源组的指定数据源。
    - 也可以使用 @DS("slave") 注解，此时会负载均衡，选择分组中的某个数据源进行访问。目前，负载均衡默认采用轮询的方式。
    
## MyBatis 多数据源

> 示例代码对应仓库：dynamic-datasource-mybatis

1. 引入依赖

```xml
<!-- MyBatis 相关依赖 -->
<dependency>
	<groupId>org.mybatis.spring.boot</groupId>
	<artifactId>mybatis-spring-boot-starter</artifactId>
	<version>2.1.1</version>
</dependency>

<!-- 保证 Spring AOP 相关的依赖包 -->
<dependency>
	<groupId>org.springframework</groupId>
	<artifactId>spring-aspects</artifactId>
</dependency>
```

2. 启动类配置

```java
@SpringBootApplication
@EnableAspectJAutoProxy(exposeProxy = true)
public class Application {
}
```

3. 应用配置文件

在 resources 目录下，创建 application.yaml 配置文件。配置如下：

```yaml
spring:
  # datasource 数据源配置内容
  datasource:
    # 订单数据源配置
    orders:
      jdbc-url: jdbc:mysql://127.0.0.1:3306/test_orders?useSSL=false&useUnicode=true&characterEncoding=UTF-8
      driver-class-name: com.mysql.jdbc.Driver
      username: root
      password:
    # 用户数据源配置
    users:
      jdbc-url: jdbc:mysql://127.0.0.1:3306/test_users?useSSL=false&useUnicode=true&characterEncoding=UTF-8
      driver-class-name: com.mysql.jdbc.Driver
      username: root
      password:

# mybatis 配置内容
#mybatis:
#  config-location: classpath:mybatis-config.xml # 配置 MyBatis 配置文件路径
#  type-aliases-package: cn.iocoder.springboot.lab17.dynamicdatasource.dataobject # 配置数据库实体包路径

```

- 在 spring.datasource 配置项中，我们设置了 orders 和 users 两个数据源。
- 注释掉 mybatis 配置项，因为我们不使用 mybatis-spring-boot-starter 自动化配置 MyBatis ，而是自己写配置类，自定义配置 MyBatis 。

4. MyBatis 配置类

在 config 包路径下，我们会分别创建：

- MyBatisOrdersConfig 配置类，配置使用 orders 数据源的 MyBatis 配置。
- MyBatisUsersConfig 配置类，配置使用 users 数据源的 MyBatis 配置。
两个 MyBatis 配置类代码是一致的，只是部分配置项的值不同。所以我们仅仅来看下 MyBatisOrdersConfig 配置类，而 MyBatisUsersConfig 配置类胖友自己看看即可。代码如下：

```java
@Configuration
@MapperScan(basePackages = "com.zhuaer.learning.dynamic.datasource.mybatis.mapper.orders", sqlSessionTemplateRef = "ordersSqlSessionTemplate")
public class MyBatisOrdersConfig {

    /**
     * 创建 orders 数据源
     */
    @Bean(name = "ordersDataSource")
    @ConfigurationProperties(prefix = "spring.datasource.orders")
    public DataSource dataSource() {
        return DataSourceBuilder.create().build();
    }

    /**
     * 创建 MyBatis SqlSessionFactory
     */
    @Bean(name = "ordersSqlSessionFactory")
    public SqlSessionFactory sqlSessionFactory() throws Exception {
        SqlSessionFactoryBean bean = new SqlSessionFactoryBean();
        // 设置 orders 数据源
        bean.setDataSource(this.dataSource());
        // 设置 entity 所在包
        bean.setTypeAliasesPackage("com.zhuaer.learning.dynamic.datasource.mybatis.dataobject");
        // 设置 config 路径
        bean.setConfigLocation(new PathMatchingResourcePatternResolver().getResource("classpath:mybatis-config.xml"));
        // 设置 mapper 路径
        bean.setMapperLocations(new PathMatchingResourcePatternResolver().getResources("classpath:mapper/orders/*.xml"));
        return bean.getObject();
    }

    /**
     * 创建 MyBatis SqlSessionTemplate
     */
    @Bean(name = "ordersSqlSessionTemplate")
    public SqlSessionTemplate sqlSessionTemplate() throws Exception {
        return new SqlSessionTemplate(this.sqlSessionFactory());
    }

    /**
     * 创建 orders 数据源的 TransactionManager 事务管理器
     */
    @Bean(name = DBConstants.TX_MANAGER_ORDERS)
    public PlatformTransactionManager transactionManager() {
        return new DataSourceTransactionManager(this.dataSource());
    }

}
```

- dataSource() 方法，创建 orders 数据源。
- sqlSessionFactory() 方法，创建 MyBatis SqlSessionFactory Bean 。
- sqlSessionTemplate() 方法，创建 MyBatis SqlSessionTemplate Bean 。其内部的 sqlSessionFactory 使用的就是对应 orders 数据源的 SqlSessionFactory 对象。
- 在类上，有 @MapperScan 注解：
    - 配置 basePackages 属性，它会扫描 cn.iocoder.springboot.lab17.dynamicdatasource.mapper 包下的 orders 包下的 Mapper 接口。和 resource/mapper 路径一样，我们也将 mapper 包路径，拆分为 orders 包下的 Mapper 接口用于 orders 数据源，users 包下的 Mapper 接口用于 users 数据源。
    - 配置 sqlSessionTemplateRef 属性，它会使用 #sqlSessionTemplate() 方法创建的 SqlSessionTemplate Bean 对象。
    - 这样，我们就能保证 cn.iocoder.springboot.lab17.dynamicdatasource.mapper.orders 下的 Mapper 使用的是操作 orders 数据源的 SqlSessionFactory ，从而操作 orders 数据源。
- transactionManager() 方法，创建 orders 数据源的 Spring 事务管理器。因为，我们项目中，一般使用 Spring 管理事务。另外，我们在 DBConstants.java 枚举了 TX_MANAGER_ORDERS 和 TX_MANAGER_USERS 两个事务管理器的名字。


## Spring Data JPA 多数据源

> 示例代码对应仓库：dynamic-datasource-springdatajpa

1. 引入依赖

```xml
<!-- 实现对数据库连接池的自动化配置 -->
<dependency>
	<groupId>org.springframework.boot</groupId>
	<artifactId>spring-boot-starter-jdbc</artifactId>
</dependency>
<dependency> <!-- 本示例，我们使用 MySQL -->
	<groupId>mysql</groupId>
	<artifactId>mysql-connector-java</artifactId>
	<version>5.1.48</version>
</dependency>

<!-- JPA 相关依赖 -->
<dependency>
	<groupId>org.springframework.boot</groupId>
	<artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>
```

2. 启动类配置

```java
@SpringBootApplication
@EnableAspectJAutoProxy(proxyTargetClass = true, exposeProxy = true) 
public class Application {
}
```

3. 应用配置文件

在 resources 目录下，创建 application.yaml 配置文件。配置如下：

```yaml
spring:
  # datasource 数据源配置内容
  datasource:
    # 订单数据源配置
    orders:
      jdbc-url: jdbc:mysql://127.0.0.1:3306/test_orders?useSSL=false&useUnicode=true&characterEncoding=UTF-8
      driver-class-name: com.mysql.jdbc.Driver
      username: root
      password:
    # 用户数据源配置
    users:
      jdbc-url: jdbc:mysql://127.0.0.1:3306/test_users?useSSL=false&useUnicode=true&characterEncoding=UTF-8
      driver-class-name: com.mysql.jdbc.Driver
      username: root
      password:
  jpa:
    show-sql: true # 打印 SQL 。生产环境，建议关闭
    # Hibernate 配置内容，对应 HibernateProperties 类
    hibernate:
      ddl-auto: none
```

4. Spring Data JPA 配置类

在 config 包路径下，创建 HibernateConfig.java 配置类。代码如下：

```java
// HibernateConfig.java

@Configuration
public class HibernateConfig {

    @Autowired
    private JpaProperties jpaProperties;
    @Autowired
    private HibernateProperties hibernateProperties;

    /**
     * 获取 Hibernate Vendor 相关配置
     */
    @Bean(name = "hibernateVendorProperties")
    public Map<String, Object> hibernateVendorProperties() {
        return hibernateProperties.determineHibernateProperties(
                jpaProperties.getProperties(), new HibernateSettings());
    }
}
```

- 目的是获得 Hibernate Vendor 相关配置。

在 config 包路径下，我们会分别创建：

- JpaOrdersConfig 配置类，配置使用 orders 数据源的 Spring Data JPA 配置。
- JpaUsersConfig 配置类，配置使用 users 数据源的 Spring Data JPA 配置。

两个 Spring Data JPA 配置类代码是一致的，只是部分配置项的值不同。所以我们仅仅来看下 JpaOrdersConfig 配置类，而 JpaUsersConfig 配置类胖友自己看看即可。代码如下：

```java
@Configuration
@EnableJpaRepositories(
        entityManagerFactoryRef = DBConstants.ENTITY_MANAGER_FACTORY_ORDERS,
        transactionManagerRef = DBConstants.TX_MANAGER_ORDERS,
        basePackages = {"com.zhuaer.learning.dynamic.datasource.springdatajpa.repository.orders"}) // 设置 Repository 接口所在包
public class JpaOrdersConfig {

    @Resource(name = "hibernateVendorProperties")
    private Map<String, Object> hibernateVendorProperties;

    /**
     * 创建 orders 数据源
     */
    @Bean(name = "ordersDataSource")
    @ConfigurationProperties(prefix = "spring.datasource.orders")
    @Primary // 需要特殊添加，否则初始化会有问题
    public DataSource dataSource() {
        return DataSourceBuilder.create().build();
    }

    /**
     * 创建 LocalContainerEntityManagerFactoryBean
     */
    @Bean(name = DBConstants.ENTITY_MANAGER_FACTORY_ORDERS)
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(EntityManagerFactoryBuilder builder) {
        return builder
                .dataSource(this.dataSource()) // 数据源
                .properties(hibernateVendorProperties) // 获取并注入 Hibernate Vendor 相关配置
                .packages("com.zhuaer.learning.dynamic.datasource.springdatajpa.dataobject") // 数据库实体 entity 所在包
                .persistenceUnit("ordersPersistenceUnit") // 设置持久单元的名字，需要唯一
                .build();
    }

    /**
     * 创建 PlatformTransactionManager
     */
    @Bean(name = DBConstants.TX_MANAGER_ORDERS)
    public PlatformTransactionManager transactionManager(EntityManagerFactoryBuilder builder) {
        return new JpaTransactionManager(entityManagerFactory(builder).getObject());
    }

}
```

- dataSource() 方法，创建 orders 数据源。
- entityManagerFactoryPrimary(EntityManagerFactoryBuilder builder) 方法，创建 LocalContainerEntityManagerFactoryBean Bean ，它是创建 EntityManager 实体管理器的工厂 Bean ，最终会创建对应的 EntityManager Bean 。
- transactionManager(EntityManagerFactoryBuilder builder) 方法，创建使用上述 EntityManager 的 JpaTransactionManager Bean 对象。这样，该事务管理器使用的也是 orders 数据源。
- 最终，通过 @EnableJpaRepositories 注解，串联在一起：
    - entityManagerFactoryRef 属性，保证了使用 orders 数据源的 EntityManager 实体管理器的工厂 Bean 。
    - transactionManagerRef 属性，保证了使用 orders 数据源的 PlatformTransactionManager 事务管理器 Bean 。
    - basePackages 属性，它会扫描 cn.iocoder.springboot.lab17.dynamicdatasource.repository 包下的 orders 包下的 Repository 接口。我们将 repository 包路径，拆分为 orders 包下的 Repository 接口用于 orders 数据源，users 包下的 Repository 接口用于 users 数据源。
- 另外，我们在 DBConstants.java 类中，枚举了：
    - TX_MANAGER_ORDERS 和 TX_MANAGER_USERS 两个事务管理器的名字，方便代码中使用。
    - ENTITY_MANAGER_FACTORY_ORDERS 和 ENTITY_MANAGER_FACTORY_USERS 两个实体管理器的名字。


## JdbcTemplate 多数据源

> 示例代码对应仓库：dynamic-datasource-jdbctemplate

1. 引入依赖

```xml
<!-- 实现对数据库连接池的自动化配置 -->
<dependency>
	<groupId>org.springframework.boot</groupId>
	<artifactId>spring-boot-starter-jdbc</artifactId>
</dependency>
<dependency> <!-- 本示例，我们使用 MySQL -->
	<groupId>mysql</groupId>
	<artifactId>mysql-connector-java</artifactId>
	<version>5.1.48</version>
</dependency>

<!-- 保证 Spring AOP 相关的依赖包 -->
<dependency>
	<groupId>org.springframework</groupId>
	<artifactId>spring-aspects</artifactId>
</dependency>
```

2. 启动类配置

```java
@SpringBootApplication
@EnableAspectJAutoProxy(proxyTargetClass = true, exposeProxy = true) 
public class Application {
}
```

3. 应用配置文件

在 resources 目录下，创建 application.yaml 配置文件。配置如下：

```yaml
spring:
  # datasource 数据源配置内容
  datasource:
    # 订单数据源配置
    orders:
      jdbc-url: jdbc:mysql://127.0.0.1:3306/dynamic_datasource01?useSSL=false&useUnicode=true&characterEncoding=UTF-8
      driver-class-name: com.mysql.jdbc.Driver
      username: root
      password:
    # 用户数据源配置
    users:
      jdbc-url: jdbc:mysql://127.0.0.1:3306/dynamic_datasource02?useSSL=false&useUnicode=true&characterEncoding=UTF-8
      driver-class-name: com.mysql.jdbc.Driver
      username: root
      password:
```

4. JdbcTemplate 配置类

在 config 包路径下，我们会分别创建：

- JdbcTemplateOrdersConfig 配置类，配置使用 orders 数据源的 MyBatis 配置。
- JdbcTemplateUsersConfig 配置类，配置使用 users 数据源的 MyBatis 配置。
两个 JdbcTemplate 配置类代码是一致的，只是部分配置项的值不同。所以我们仅仅来看下 JdbcTemplateOrdersConfig 配置类，而 JdbcTemplateUsersConfig 配置类胖友自己看看即可。代码如下：

```java
@Configuration
public class JdbcTemplateOrdersConfig {

    /**
     * 创建 orders 数据源
     */
    @Bean(name = "ordersDataSource")
    @ConfigurationProperties(prefix = "spring.datasource.orders")
    public DataSource dataSource() {
        return DataSourceBuilder.create().build();
    }

    /**
     * 创建 orders JdbcTemplate
     */
    @Bean(name = DBConstants.JDBC_TEMPLATE_ORDERS)
    public JdbcTemplate jdbcTemplate() {
        return new JdbcTemplate(this.dataSource());
    }

    /**
     * 创建 orders 数据源的 TransactionManager 事务管理器
     */
    @Bean(name = DBConstants.TX_MANAGER_ORDERS)
    public PlatformTransactionManager transactionManager() {
        return new DataSourceTransactionManager(this.dataSource());
    }

}
```

- dataSource() 方法，创建 orders 数据源。
- jdbcTemplate() 方法，创建使用 orders 数据源的 JdbcTemplate Bean 。
- transactionManager() 方法，创建 orders 数据源的 Spring 事务管理器。因为，我们项目中，一般使用 Spring 管理事务。另外，我们在 DBConstants.java 枚举了 TX_MANAGER_ORDERS 和 TX_MANAGER_USERS 两个事务管理器的名字。

## Sharding-JDBC 多数据源

> 示例代码对应仓库：dynamic-datasource-sharding-jdbc-01

Sharding-JDBC 是 Apache ShardingSphere 下，基于 JDBC 的分库分表组件。对于 Java 语言来说，我们推荐选择 Sharding-JDBC 优于 Sharding-Proxy ，主要原因是：

- 减少一层 Proxy 的开销，性能更优。
- 去中心化，无需多考虑一次 Proxy 的高可用。

1. 引入相关依赖

```xml
<!-- 实现对 Sharding-JDBC 的自动化配置 -->
<dependency>
    <groupId>org.apache.shardingsphere</groupId>
    <artifactId>sharding-jdbc-spring-boot-starter</artifactId>
    <version>4.0.0-RC2</version>
</dependency>
```

2. 启动类配置

```java
@SpringBootApplication
@MapperScan(basePackages = "com.zhuaer.learning.sharding.jdbc.mapper")
@EnableAspectJAutoProxy(exposeProxy = true)
public class Application {
}
```

3. 应用配置文件

在 resources 目录下，创建 application.yaml 配置文件。配置如下：

```yaml
spring:
  # ShardingSphere 配置项
  shardingsphere:
    datasource:
      # 所有数据源的名字
      names: ds-orders, ds-users
      # 订单 orders 数据源配置
      ds-orders:
        type: com.zaxxer.hikari.HikariDataSource # 使用 Hikari 数据库连接池
        driver-class-name: com.mysql.jdbc.Driver
        jdbc-url: jdbc:mysql://127.0.0.1:3306/test_orders?useSSL=false&useUnicode=true&characterEncoding=UTF-8
        username: root
        password:
      # 订单 users 数据源配置
      ds-users:
        type: com.zaxxer.hikari.HikariDataSource # 使用 Hikari 数据库连接池
        driver-class-name: com.mysql.jdbc.Driver
        jdbc-url: jdbc:mysql://127.0.0.1:3306/test_users?useSSL=false&useUnicode=true&characterEncoding=UTF-8
        username: root
        password:
    # 分片规则
    sharding:
      tables:
        # orders 表配置
        orders:
          actualDataNodes: ds-orders.orders # 映射到 ds-orders 数据源的 orders 表
        # users 表配置
        users:
          actualDataNodes: ds-users.users # 映射到 ds-users 数据源的 users 表

# mybatis 配置内容
mybatis:
  config-location: classpath:mybatis-config.xml # 配置 MyBatis 配置文件路径
  mapper-locations: classpath:mapper/*.xml # 配置 Mapper XML 地址
  type-aliases-package: com.zhuaer.learning.sharding.jdbc.dataobject # 配置数据库实体包路径

```

- spring.shardingsphere.datasource 配置项下，我们配置了 ds_orders 和 ds_users 两个数据源。
- spring.shardingsphere.sharding 配置项下，我们配置了分片规则，将 orders 逻辑表的操作路由到 ds-orders 数据源的 orders 真实表 ，将 users 逻辑表的操作路由到 ds-users 数据源的 users 真实表 。
- mybatis 配置项，设置 mybatis-spring-boot-starter MyBatis 的配置内容。


## Sharding-JDBC 读写分离


> 示例代码对应仓库：dynamic-datasource-sharding-jdbc-02

Sharding-JDBC 已经提供了读写分离的支持，胖友可以看看如下两个文档：

- ShardingSphere > 概念 & 功能 > 读写分离
- ShardingSphere > 用户手册 > Sharding-JDBC > 使用手册 > 读写分离

1. 引入相关依赖

```xml
<!-- 实现对 Sharding-JDBC 的自动化配置 -->
<dependency>
    <groupId>org.apache.shardingsphere</groupId>
    <artifactId>sharding-jdbc-spring-boot-starter</artifactId>
    <version>4.0.0-RC2</version>
</dependency>
```

2. 启动类配置

```java
@SpringBootApplication
@MapperScan(basePackages = "com.zhuaer.learning.sharding.jdbc.mapper")
@EnableAspectJAutoProxy(exposeProxy = true)
public class Application {
}
```

3. 应用配置文件

在 resources 目录下，创建 application.yaml 配置文件。配置如下：

```yaml
spring:
  # ShardingSphere 配置项
  shardingsphere:
    # 数据源配置
    datasource:
      # 所有数据源的名字
      names: ds-master, ds-slave-1, ds-slave-2
      # 订单 orders 主库的数据源配置
      ds-master:
        type: com.zaxxer.hikari.HikariDataSource # 使用 Hikari 数据库连接池
        driver-class-name: com.mysql.jdbc.Driver
        jdbc-url: jdbc:mysql://127.0.0.1:3306/test_orders?useSSL=false&useUnicode=true&characterEncoding=UTF-8
        username: root
        password:
      # 订单 orders 从库数据源配置
      ds-slave-1:
        type: com.zaxxer.hikari.HikariDataSource # 使用 Hikari 数据库连接池
        driver-class-name: com.mysql.jdbc.Driver
        jdbc-url: jdbc:mysql://127.0.0.1:3306/test_orders_01?useSSL=false&useUnicode=true&characterEncoding=UTF-8
        username: root
        password:
      # 订单 orders 从库数据源配置
      ds-slave-2:
        type: com.zaxxer.hikari.HikariDataSource # 使用 Hikari 数据库连接池
        driver-class-name: com.mysql.jdbc.Driver
        jdbc-url: jdbc:mysql://127.0.0.1:3306/test_orders_02?useSSL=false&useUnicode=true&characterEncoding=UTF-8
        username: root
        password:
    # 读写分离配置，对应 YamlMasterSlaveRuleConfiguration 配置类
    masterslave:
      name: ms # 名字，任意，需要保证唯一
      master-data-source-name: ds-master # 主库数据源
      slave-data-source-names: ds-slave-1, ds-slave-2 # 从库数据源

# mybatis 配置内容
mybatis:
  config-location: classpath:mybatis-config.xml # 配置 MyBatis 配置文件路径
  mapper-locations: classpath:mapper/*.xml # 配置 Mapper XML 地址
  type-aliases-package: com.zhuaer.learning.sharding.jdbc.dataobject # 配置数据库实体包路径
```

- spring.shardingsphere.datasource 配置项下，我们配置了 一个主数据源 ds-master 、两个从数据源 ds-slave-1、ds-slave-2 。
- spring.shardingsphere.masterslave 配置项下，配置了读写分离。对于从库来说，Sharding-JDBC 提供了多种负载均衡策略，默认为轮询。
- mybatis 配置项，设置 mybatis-spring-boot-starter MyBatis 的配置内容。



