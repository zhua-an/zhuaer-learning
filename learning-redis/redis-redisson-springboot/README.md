# redis-redisson-springboot

springboot 整合redisson

## 添加依赖

    <dependency>
    	<groupId>org.redisson</groupId>
    	<artifactId>redisson-spring-boot-starter</artifactId>
    	<version>3.13.6</version>
    </dependency>
    
## RedissonConfig 配置文件

官方文档：[https://github.com/redisson/redisson/wiki/%E7%9B%AE%E5%BD%95](https://github.com/redisson/redisson/wiki/%E7%9B%AE%E5%BD%95 "https://github.com/redisson/redisson/wiki/%E7%9B%AE%E5%BD%95")

redisson官方发布了redisson-spring-boot-starter，具体可以参考：[https://github.com/redisson/redisson/tree/master/redisson-spring-boot-starter#spring-boot-starter](https://github.com/redisson/redisson/tree/master/redisson-spring-boot-starter#spring-boot-starter "https://github.com/redisson/redisson/tree/master/redisson-spring-boot-starter#spring-boot-starter")
    
    方式一：
    在resource目录新建redisson.yml文件
    clusterServersConfig:
      # 连接空闲超时，单位：毫秒 默认10000
      idleConnectionTimeout: 10000
      pingTimeout: 1000
      # 同任何节点建立连接时的等待超时。时间单位是毫秒 默认10000
      connectTimeout: 10000
      ......
    
    配置redisson
    #path to config - redisson.yaml
    spring.redis.redisson.file=classpath:redisson.yaml
    或者
    @Bean
    public RedissonClient redisson() throws IOException {
        Config config = Config.fromYAML(new ClassPathResource("redisson.yml").getInputStream());
        RedissonClient redisson = Redisson.create(config);
        return redisson;
    }
    
    
    方式二：
    也可以直接把全部配置写到springboot的配置文件application.yml里（不推荐）
    spring:
      redis:
        redisson:
          config:
            clusterServersConfig:
              idleConnectionTimeout: 10000
              connectTimeout: 10000
              .....

代码如下：

    /**
     * @ClassName RedissonConfig
     * @Description TODO
     * @Author zhua
     * @Date 2020/7/20 10:25
     * @Version 1.0
     */
    @Configuration
    public class RedissonConfig {
        @Bean
        public RedissonClient redisson() throws IOException {
            Config config = Config.fromYAML(new ClassPathResource("redisson.yaml").getInputStream());
            RedissonClient redisson = Redisson.create(config);
            return redisson;
        }
    
        @Bean("redisTemplate")
        public RedisTemplate getRedisTemplate(RedisConnectionFactory redissonConnectionFactory) {
            RedisTemplate<Object, Object> redisTemplate = new RedisTemplate();
            redisTemplate.setConnectionFactory(redissonConnectionFactory);
            redisTemplate.setValueSerializer(valueSerializer());
            redisTemplate.setKeySerializer(keySerializer());
            redisTemplate.setHashKeySerializer(keySerializer());
            redisTemplate.setHashValueSerializer(valueSerializer());
            return redisTemplate;
        }
    
        @Bean
        public RedisSerializer keySerializer() {
            return new StringRedisSerializer();
        }
    
        @Bean
        public RedisSerializer valueSerializer() {
            Jackson2JsonRedisSerializer jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer(Object.class);
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
            objectMapper.activateDefaultTyping(LaissezFaireSubTypeValidator.instance ,
                    ObjectMapper.DefaultTyping.NON_FINAL, JsonTypeInfo.As.PROPERTY);
            jackson2JsonRedisSerializer.setObjectMapper(objectMapper);
            return jackson2JsonRedisSerializer;
        }
    
    }


## 使用用例

    @RequestMapping(value = "test", method = RequestMethod.GET)
    public String test() {
    	RLock rLock = redissonClient.getLock("test1");
    	rLock.lock();
    	try {
    		Thread.sleep(1000l);
    	} catch (InterruptedException e) {
    		e.printStackTrace();
    	}
    	if (log.isDebugEnabled()) {
    		log.debug("test welcome.........");
    	}
    	rLock.unlock();
    	return "响应成功";
    }
