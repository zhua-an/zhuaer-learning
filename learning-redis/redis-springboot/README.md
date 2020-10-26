# redis-springboot

redis是键值对的数据库，有5中主要数据类型：

字符串类型（string），散列类型（hash），列表类型（list），集合类型（set），有序集合类型（zset）

常用命令：[redis常用指令](./redis常用指令.md "redis常用指令")


## 添加依赖

    <!--springboot中的redis依赖-->
    <dependency>
    	<groupId>org.springframework.boot</groupId>
    	<artifactId>spring-boot-starter-data-redis</artifactId>
    </dependency>
    
## application.yml 中加入redis相关配置

    spring:
      application:
        name: @artifactId@
      redis:
        #Redis数据库索引（默认为0）
        database: 5
        host: 127.0.0.1
        password:
        port: 16379
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

## 配置一个RedisTemplate

其实SpringBoot自动帮我们在容器中生成了一个`RedisTemplate`和一个`StringRedisTemplate`。但是，这个RedisTemplate的泛型是`<Object,Object>`，
写代码不方便，需要写好多类型转换的代码；我们需要一个泛型为`<String,Object>`形式的`RedisTemplate`。并且，这个`RedisTemplate`没有设置数据存在Redis时，key及value的序列化方式。

自己手动配置`RedisTemplate`：

    /**
     * retemplate相关配置
     * @param factory
     * @return
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory) {
    
    	RedisTemplate<String, Object> template = new RedisTemplate<>();
    	// 配置连接工厂
    	template.setConnectionFactory(factory);
    
    	//使用Jackson2JsonRedisSerializer来序列化和反序列化redis的value值（默认使用JDK的序列化方式）
    	Jackson2JsonRedisSerializer jacksonSeial = new Jackson2JsonRedisSerializer(Object.class);
    
    	ObjectMapper om = new ObjectMapper();
    	// 指定要序列化的域，field,get和set,以及修饰符范围，ANY是都有包括private和public
    	om.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
    	// 指定序列化输入的类型，类必须是非final修饰的，final修饰的类，比如String,Integer等会跑出异常
    	om.activateDefaultTyping(LaissezFaireSubTypeValidator.instance ,
    			ObjectMapper.DefaultTyping.NON_FINAL, JsonTypeInfo.As.PROPERTY);
    	jacksonSeial.setObjectMapper(om);
    
    	// 值采用json序列化
    	template.setValueSerializer(jacksonSeial);
    	//使用StringRedisSerializer来序列化和反序列化redis的key值
    	template.setKeySerializer(new StringRedisSerializer());
    
    	// 设置hash key 和value序列化模式
    	template.setHashKeySerializer(new StringRedisSerializer());
    	template.setHashValueSerializer(jacksonSeial);
    	template.afterPropertiesSet();
    
    	return template;
    }
    
    
    /**
     * 对hash类型的数据操作
     *
     * @param redisTemplate
     * @return
     */
    @Bean
    public HashOperations<String, String, Object> hashOperations(RedisTemplate<String, Object> redisTemplate) {
    	return redisTemplate.opsForHash();
    }
    
    /**
     * 对redis字符串类型数据操作
     *
     * @param redisTemplate
     * @return
     */
    @Bean
    public ValueOperations<String, Object> valueOperations(RedisTemplate<String, Object> redisTemplate) {
    	return redisTemplate.opsForValue();
    }
    
    /**
     * 对链表类型的数据操作
     *
     * @param redisTemplate
     * @return
     */
    @Bean
    public ListOperations<String, Object> listOperations(RedisTemplate<String, Object> redisTemplate) {
    	return redisTemplate.opsForList();
    }
    
    /**
     * 对无序集合类型的数据操作
     *
     * @param redisTemplate
     * @return
     */
    @Bean
    public SetOperations<String, Object> setOperations(RedisTemplate<String, Object> redisTemplate) {
    	return redisTemplate.opsForSet();
    }
    
    /**
     * 对有序集合类型的数据操作
     *
     * @param redisTemplate
     * @return
     */
    @Bean
    public ZSetOperations<String, Object> zSetOperations(RedisTemplate<String, Object> redisTemplate) {
    	return redisTemplate.opsForZSet();
    }
    
## 写一个Redis工具类

直接用RedisTemplate操作Redis，需要很多行代码，因此直接封装好一个RedisUtils，这样写代码更方便点。这个RedisUtils交给Spring容器实例化，使用时直接注解注入。

    import com.zhuaer.learning.redis.contants.Status;
    import org.springframework.beans.factory.annotation.Autowired;
    import org.springframework.data.redis.core.BoundListOperations;
    import org.springframework.data.redis.core.RedisTemplate;
    import org.springframework.stereotype.Component;
    import org.springframework.util.CollectionUtils;
    
    import java.util.List;
    import java.util.Map;
    import java.util.Set;
    import java.util.concurrent.TimeUnit;
    
    /**
     * @ClassName RedisUtil
     * @Description TODO
     * @Author zhua
     * @Date 2020/7/17 15:23
     * @Version 1.0
     */
    @Component
    public class RedisUtil {
    
        @Autowired
        private RedisTemplate<String, Object> redisTemplate;
    
        public RedisUtil(RedisTemplate<String, Object> redisTemplate) {
            this.redisTemplate = redisTemplate;
        }
    
        //============================Lock=============================
        /**
         * 获得锁
         */
        public boolean getLock(String lockId, long millisecond) {
            Boolean success = redisTemplate.opsForValue().setIfAbsent(lockId, "lock",
                    millisecond, TimeUnit.MILLISECONDS);
            return success != null && success;
        }
    
        /**
         * 指定缓存失效时间
         * @param key 键
         * @param time 时间(秒)
         * @return
         */
        public boolean expire(String key,long time){
            try {
                if(time>0){
                    redisTemplate.expire(key, time, TimeUnit.SECONDS);
                }
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
    
        /**
         * 根据key 获取过期时间
         * @param key 键 不能为null
         * @return 时间(秒) 返回0代表为永久有效
         */
        public long getExpire(String key){
            return redisTemplate.getExpire(key,TimeUnit.SECONDS);
        }
    
        /**
         * 判断key是否存在
         * @param key 键
         * @return true 存在 false不存在
         */
        public boolean hasKey(String key){
            try {
                return redisTemplate.hasKey(key);
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
    
        /**
         * 删除缓存
         * @param key 可以传一个值 或多个
         */
        @SuppressWarnings("unchecked")
        public void del(String ... key){
            if(key!=null&&key.length>0){
                if(key.length==1){
                    redisTemplate.delete(key[0]);
                }else{
                    redisTemplate.delete(CollectionUtils.arrayToList(key));
                }
            }
        }
    
        //============================String=============================
        /**
         * 普通缓存获取
         * @param key 键
         * @return 值
         */
        public Object get(String key){
            return key==null?null:redisTemplate.opsForValue().get(key);
        }
    
        /**
         * 普通缓存放入
         * @param key 键
         * @param value 值
         * @return true成功 false失败
         */
        public boolean set(String key,Object value) {
            try {
                redisTemplate.opsForValue().set(key, value);
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
    
        /**
         * 普通缓存放入并设置时间
         * @param key 键
         * @param value 值
         * @param time 时间(秒) time要大于0 如果time小于等于0 将设置无限期
         * @return true成功 false 失败
         */
        public boolean set(String key,Object value,long time){
            try {
                if(time>0){
                    redisTemplate.opsForValue().set(key, value, time, TimeUnit.SECONDS);
                }else{
                    set(key, value);
                }
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
    
        /**
         * 递增
         * @param key 键
         * @param delta 要增加几(大于0)
         * @return
         */
        public long incr(String key, long delta){
            if(delta<0){
                throw new RuntimeException("递增因子必须大于0");
            }
            return redisTemplate.opsForValue().increment(key, delta);
        }
    
        /**
         * 递减
         * @param key 键
         * @param delta 要减少几(小于0)
         * @return
         */
        public long decr(String key, long delta){
            if(delta<0){
                throw new RuntimeException("递减因子必须大于0");
            }
            return redisTemplate.opsForValue().increment(key, -delta);
        }
    
        //================================Map=================================
        /**
         * HashGet
         * @param key 键 不能为null
         * @param item 项 不能为null
         * @return 值
         */
        public Object hget(String key,String item){
            return redisTemplate.opsForHash().get(key, item);
        }
    
        /**
         * 获取hashKey对应的所有键值
         * @param key 键
         * @return 对应的多个键值
         */
        public Map<Object,Object> hmget(String key){
            return redisTemplate.opsForHash().entries(key);
        }
    
        /**
         * HashSet
         * @param key 键
         * @param map 对应多个键值
         * @return true 成功 false 失败
         */
        public boolean hmset(String key, Map<String,Object> map){
            try {
                redisTemplate.opsForHash().putAll(key, map);
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
    
        /**
         * HashSet 并设置时间
         * @param key 键
         * @param map 对应多个键值
         * @param time 时间(秒)
         * @return true成功 false失败
         */
        public boolean hmset(String key, Map<String,Object> map, long time){
            try {
                redisTemplate.opsForHash().putAll(key, map);
                if(time>0){
                    expire(key, time);
                }
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
    
        /**
         * 向一张hash表中放入数据,如果不存在将创建
         * @param key 键
         * @param item 项
         * @param value 值
         * @return true 成功 false失败
         */
        public boolean hset(String key,String item,Object value) {
            try {
                redisTemplate.opsForHash().put(key, item, value);
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
    
        /**
         * 向一张hash表中放入数据,如果不存在将创建
         * @param key 键
         * @param item 项
         * @param value 值
         * @param time 时间(秒)  注意:如果已存在的hash表有时间,这里将会替换原有的时间
         * @return true 成功 false失败
         */
        public boolean hset(String key,String item,Object value,long time) {
            try {
                redisTemplate.opsForHash().put(key, item, value);
                if(time>0){
                    expire(key, time);
                }
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
    
        /**
         * 删除hash表中的值
         * @param key 键 不能为null
         * @param item 项 可以使多个 不能为null
         */
        public void hdel(String key, Object... item){
            redisTemplate.opsForHash().delete(key,item);
        }
    
        /**
         * 判断hash表中是否有该项的值
         * @param key 键 不能为null
         * @param item 项 不能为null
         * @return true 存在 false不存在
         */
        public boolean hHasKey(String key, String item){
            return redisTemplate.opsForHash().hasKey(key, item);
        }
    
        /**
         * hash递增 如果不存在,就会创建一个 并把新增后的值返回
         * @param key 键
         * @param item 项
         * @param by 要增加几(大于0)
         * @return
         */
        public double hincr(String key, String item,double by){
            return redisTemplate.opsForHash().increment(key, item, by);
        }
    
        /**
         * hash递减
         * @param key 键
         * @param item 项
         * @param by 要减少记(小于0)
         * @return
         */
        public double hdecr(String key, String item,double by){
            return redisTemplate.opsForHash().increment(key, item,-by);
        }
    
        //============================set=============================
        /**
         * 根据key获取Set中的所有值
         * @param key 键
         * @return
         */
        public Set<Object> sGet(String key){
            try {
                return redisTemplate.opsForSet().members(key);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
    
        /**
         * 根据value从一个set中查询,是否存在
         * @param key 键
         * @param value 值
         * @return true 存在 false不存在
         */
        public boolean sHasKey(String key,Object value){
            try {
                return redisTemplate.opsForSet().isMember(key, value);
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
    
        /**
         * 将数据放入set缓存
         * @param key 键
         * @param values 值 可以是多个
         * @return 成功个数
         */
        public long sSet(String key, Object...values) {
            try {
                return redisTemplate.opsForSet().add(key, values);
            } catch (Exception e) {
                e.printStackTrace();
                return 0;
            }
        }
    
        /**
         * 将set数据放入缓存
         * @param key 键
         * @param time 时间(秒)
         * @param values 值 可以是多个
         * @return 成功个数
         */
        public long sSetAndTime(String key,long time,Object...values) {
            try {
                Long count = redisTemplate.opsForSet().add(key, values);
                if(time>0) {
                    expire(key, time);
                }
                return count;
            } catch (Exception e) {
                e.printStackTrace();
                return 0;
            }
        }
    
        /**
         * 获取set缓存的长度
         * @param key 键
         * @return
         */
        public long sGetSetSize(String key){
            try {
                return redisTemplate.opsForSet().size(key);
            } catch (Exception e) {
                e.printStackTrace();
                return 0;
            }
        }
    
        /**
         * 移除值为value的
         * @param key 键
         * @param values 值 可以是多个
         * @return 移除的个数
         */
        public long setRemove(String key, Object ...values) {
            try {
                Long count = redisTemplate.opsForSet().remove(key, values);
                return count;
            } catch (Exception e) {
                e.printStackTrace();
                return 0;
            }
        }
        //===============================list=================================
    
        /**
         * 获取list缓存的内容
         * @param key 键
         * @param start 开始
         * @param end 结束  0 到 -1代表所有值
         * @return
         */
        public List<Object> lGet(String key, long start, long end){
            try {
                return redisTemplate.opsForList().range(key, start, end);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
    
        /**
         * 获取list缓存的长度
         * @param key 键
         * @return
         */
        public long lGetListSize(String key){
            try {
                return redisTemplate.opsForList().size(key);
            } catch (Exception e) {
                e.printStackTrace();
                return 0;
            }
        }
    
        /**
         * 通过索引 获取list中的值
         * @param key 键
         * @param index 索引  index>=0时， 0 表头，1 第二个元素，依次类推；index<0时，-1，表尾，-2倒数第二个元素，依次类推
         * @return
         */
        public Object lGetIndex(String key,long index){
            try {
                return redisTemplate.opsForList().index(key, index);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
    
        /**
         * 将list放入缓存
         * @param key 键
         * @param value 值
         * @return
         */
        public boolean lSet(String key, Object value) {
            try {
                redisTemplate.opsForList().rightPush(key, value);
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
    
        /**
         * 将list放入缓存
         * @param key 键
         * @param value 值
         * @param time 时间(秒)
         * @return
         */
        public boolean lSet(String key, Object value, long time) {
            try {
                redisTemplate.opsForList().rightPush(key, value);
                if (time > 0) {
                    expire(key, time);
                }
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
    
        /**
         * 将list放入缓存
         * @param key 键
         * @param value 值
         * @return
         */
        public boolean lSet(String key, List<Object> value) {
            try {
                redisTemplate.opsForList().rightPushAll(key, value);
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
    
        /**
         * 将list放入缓存
         * @param key 键
         * @param value 值
         * @param time 时间(秒)
         * @return
         */
        public boolean lSet(String key, List<Object> value, long time) {
            try {
                redisTemplate.opsForList().rightPushAll(key, value);
                if (time > 0) {
                    expire(key, time);
                }
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
    
        /**
         * 根据索引修改list中的某条数据
         * @param key 键
         * @param index 索引
         * @param value 值
         * @return
         */
        public boolean lUpdateIndex(String key, long index,Object value) {
            try {
                redisTemplate.opsForList().set(key, index, value);
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
    
        /**
         * 移除N个值为value
         * @param key 键
         * @param count 移除多少个
         * @param value 值
         * @return 移除的个数
         */
        public long lRemove(String key,long count,Object value) {
            try {
                Long remove = redisTemplate.opsForList().remove(key, count, value);
                return remove;
            } catch (Exception e) {
                e.printStackTrace();
                return 0;
            }
        }
    
        /**
         * 模糊查询获取key值
         * @param pattern
         * @return
         */
        public Set keys(String pattern){
            return redisTemplate.keys(pattern);
        }
    
        /**
         * 使用Redis的消息队列
         * @param channel
         * @param message 消息内容
         */
        public void convertAndSend(String channel, Object message){
            redisTemplate.convertAndSend(channel,message);
        }
    
    
        //=========BoundListOperations 用法 start============
    
        /**
         *将数据添加到Redis的list中（从右边添加）
         * @param listKey
         * @param expireEnum 有效期的枚举类
         * @param values 待添加的数据
         */
        public void addToListRight(String listKey, Status.ExpireEnum expireEnum, Object... values) {
            //绑定操作
            BoundListOperations<String, Object> boundValueOperations = redisTemplate.boundListOps(listKey);
            //插入数据
            boundValueOperations.rightPushAll(values);
            //设置过期时间
            boundValueOperations.expire(expireEnum.getTime(),expireEnum.getTimeUnit());
        }
        /**
         * 根据起始结束序号遍历Redis中的list
         * @param listKey
         * @param start  起始序号
         * @param end  结束序号
         * @return
         */
        public List<Object> rangeList(String listKey, long start, long end) {
            //绑定操作
            BoundListOperations<String, Object> boundValueOperations = redisTemplate.boundListOps(listKey);
            //查询数据
            return boundValueOperations.range(start, end);
        }
        /**
         * 弹出右边的值 --- 并且移除这个值
         * @param listKey
         */
        public Object rifhtPop(String listKey){
            //绑定操作
            BoundListOperations<String, Object> boundValueOperations = redisTemplate.boundListOps(listKey);
            return boundValueOperations.rightPop();
        }
    
        //=========BoundListOperations 用法 End============
    
    }
    
    
# 订阅发布模式

##订阅者监听配置

具体配置：[redis配置](./src/main/java/com/zhuaer/learning/redis/config/RedisConfig.java "redis配置")

    /**
     * 订阅者监听配置
     * @param factory
     * @return
     */
    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer(RedisConnectionFactory factory) {
    
    	RedisMessageListenerContainer container = new RedisMessageListenerContainer();
    	container.setConnectionFactory(factory);
    
    	/**
    	 * 添加订阅者监听类，数量不限.PatternTopic定义监听主题,这里监听topic主题
    	 */
    	container.addMessageListener(new SubscribeListener(), new PatternTopic("topic"));
    	return container;
    }


##消息监听器

    /**
     * @ClassName SubscribeListener
     * @Description 订阅监听类
     * @Author zhua
     * @Date 2020/7/17 17:05
     * @Version 1.0
     */
    public class SubscribeListener implements MessageListener {
        @Override
        public void onMessage(Message message, byte[] bytes) {
            // 缓存消息是序列化的，需要反序列化。然而new String()可以反序列化，但静态方法valueOf()不可以
            System.out.println(new String(bytes) + "主题发布：" + new String(message.getBody()));
        }
    }

## 发布方法

    /**
     * 发布方法、
     * @return
     */
    @RequestMapping("/publish/{id}")
    public String publish(@PathVariable String id) {
    	// 该方法封装的 connection.publish(rawChannel, rawMessage);
    	for(int i = 1; i <= 5; i++) {
    		redisTemplate.convertAndSend("topic", String.format("我是消息{%d}号: %tT", i, new Date()));
    	}
    	return "success";
    }
    
# redis 锁

## StringRedisTemplate 或者 RedisTemplate 常用加锁方式

### 加锁操作

    /**
     * Redis加锁的操作
     *
     * @param key
     * @param value
     * @return
     */
    public Boolean tryLock(String key, String value) {
    	if (stringRedisTemplate.opsForValue().setIfAbsent(key, value)) {
    		return true;
    	}
    	String currentValue = stringRedisTemplate.opsForValue().get(key);
    	if (StringUtils.isNotEmpty(currentValue) && Long.valueOf(currentValue) < System.currentTimeMillis()) {
    		//获取上一个锁的时间 如果高并发的情况可能会出现已经被修改的问题  所以多一次判断保证线程的安全
    		String oldValue = stringRedisTemplate.opsForValue().getAndSet(key, value);
    		if (StringUtils.isNotEmpty(oldValue) && oldValue.equals(currentValue)) {
    			return true;
    		}
    	}
    	return false;
    }

### 解锁操作

    /**
     * Redis解锁的操作
     *
     * @param key
     * @param value
     */
    public void unlock(String key, String value) {
    	String currentValue = stringRedisTemplate.opsForValue().get(key);
    	try {
    		if (StringUtils.isNotEmpty(currentValue) && currentValue.equals(value)) {
    			stringRedisTemplate.opsForValue().getOperations().delete(key);
    		}
    	} catch (Exception e) {
    	}
    }
    
    
## 使用指令解锁

    @Repository
    public class RedisLock {
    
        /**
         * 解锁脚本，原子操作
         */
        private static final String unlockScript =
                "if redis.call(\"get\",KEYS[1]) == ARGV[1]\n"
                        + "then\n"
                        + "    return redis.call(\"del\",KEYS[1])\n"
                        + "else\n"
                        + "    return 0\n"
                        + "end";
    
        private StringRedisTemplate redisTemplate;
    
        public RedisLock(StringRedisTemplate redisTemplate) {
            this.redisTemplate = redisTemplate;
        }
    
        /**
         * 加锁，有阻塞
         * @param name
         * @param expire
         * @param timeout
         * @return
         */
        public String lock(String name, long expire, long timeout){
            long startTime = System.currentTimeMillis();
            String token;
            do{
                token = tryLock(name, expire);
                if(token == null) {
                    if((System.currentTimeMillis()-startTime) > (timeout-50))
                        break;
                    try {
                        Thread.sleep(50); //try 50 per sec
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        return null;
                    }
                }
            }while(token==null);
    
            return token;
        }
    
        /**
         * 加锁，无阻塞
         * @param name
         * @param expire
         * @return
         */
        public String tryLock(String name, long expire) {
            String token = UUID.randomUUID().toString();
            RedisConnectionFactory factory = redisTemplate.getConnectionFactory();
            RedisConnection conn = factory.getConnection();
            try{
                Boolean result = conn.set(name.getBytes(Charset.forName("UTF-8")), token.getBytes(Charset.forName("UTF-8")),
                        Expiration.from(expire, TimeUnit.MILLISECONDS), RedisStringCommands.SetOption.SET_IF_ABSENT);
                if(result!=null && result)
                    return token;
            }finally {
                RedisConnectionUtils.releaseConnection(conn, factory, false);
            }
            return null;
        }
    
        /**
         * 解锁
         * @param name
         * @param token
         * @return
         */
        public boolean unlock(String name, String token) {
            byte[][] keysAndArgs = new byte[2][];
            keysAndArgs[0] = name.getBytes(Charset.forName("UTF-8"));
            keysAndArgs[1] = token.getBytes(Charset.forName("UTF-8"));
            RedisConnectionFactory factory = redisTemplate.getConnectionFactory();
            RedisConnection conn = factory.getConnection();
            try {
                Long result = (Long)conn.scriptingCommands().eval(unlockScript.getBytes(Charset.forName("UTF-8")), ReturnType.INTEGER, 1, keysAndArgs);
                if(result!=null && result>0)
                    return true;
            }finally {
                RedisConnectionUtils.releaseConnection(conn, factory, false);
            }
    
            return false;
        }
    }
    