#几个基本的命令

| 函数                | 说明                                                |
| --------------------- | ----------------------------------------------------- |
| keys *                |  获得当前数据库的所有键                    |
| exists key [key ...] |  判断键是否存在，返回个数，如果key有一样的也是叠加数 |
| del key [key ...]     |  删除键，返回删除的个数                    |
| type key           | 获取减值的数据类型（string，hash，list，set，zset |
| flushall              | 清空所有数据库                              |
| config [get、set]    |  redis配置                                          |

#字符串类型（string）

字符串类型是Redis的最基本类型，它可以存储任何形式的字符串。其它的四种类型都是字符串类型的不同形式。

| 函数                                                                                                         | 语法                                                                                                         |
| -------------------------------------------------------------------------------------------------------------- | -------------------------------------------------------------------------------------------------------------- |
| 最基本的命令：GET、SET                                                                                 | GET key，SET key value   value如果有空格需要双引号以示区分                                      |
| 整数递增：INCR                                                                                            | INCR key    默认值为0，所以首先执行命令得到 1 ，不是整型提示错误                       |
| 增加指定的整数：INCRBY                                                                                 | INCRBY key increment                                                                                           |
| 整数递减：DECR                                                                                            | DECR key   默认值为0，所以首先执行命令得到 -1，不是整型提示错误                        |
| 减少指定的整数：DECRBY                                                                                 | DECRBY key increment                                                                                           |
| 增加指定浮点数：INCRBYFLOAT                                                                            | INCRBYFLOAT key increment  与INCR命令类似，只不过可以递增一个双精度浮点数                 |
| 向尾部追加值：APPEND                                                                                    | APPEND key value   redis客户端并不是输出追加后的字符串，而是输出字符串总长度         |
| 获取字符串长度：STRLEN                                                                                 | STRLEN key  如果键不存在返回0，注意如果有中文时，一个中文长度是3，redis是使用UTF-8编码中文的 |
| 获取多个键值：MGET                                                                                      | MGET key [key ...]  例如：MGET key1 key2                                                                    |
| 设置多个键值：MSET                                                                                      | MSET key value [key value ...]  例如：MSET key1 1 key2 "hello redis"                                        |
| 二进制指定位置值：GETBIT                                                                              |                                                                                                                |
| GETBIT key offset   例如：GETBIT key1 2 ，key1为hello 返回 1，返回的值只有0或1，当key不存在或超出实际长度时为0 | GETBIT key offset   例如：GETBIT key1 2 ，key1为hello 返回 1，返回的值只有0或1，当key不存在或超出实际长度时为0 |
|                                                                                                                |                                                                                                                |
| 设置二进制位置值：SETBIT                                                                              | SETBIT key offset value ，返回该位置的旧值                                                            |
| 二进制是1的个数：BITCOUNT                                                                              | BITCOUNT key [start end] ，start 、end为开始和结束字节                                               |
| 位运算：BITOP                                                                                              | BITOP operation destkey key [key ...]  ，operation支持AND、OR、XOR、NOT                                  |
| 偏移：BITPOS                                                                                                | BITPOS key bit [start] [end]                                                                                   |


#散列类型（hash）

| 函数                    | 语法                                                               |
| ------------------------- | -------------------------------------------------------------------- |
| 设置单个：HSET       | HSET key field value，不存在时返回1，存在时返回0，没有更新和插入之分 |
| 设置多个：HMSET      | HMSET key field value [field value ...]                              |
| 读取单个：HGET       | HGET key field，不存在是返回nil                               |
| 读取多个：HMGET      | HMGET key field [field ...]                                          |
| 读取全部：HGETALL    | HGETALL key，返回时字段和字段值的列表                   |
| 判断字段是否存在：HEXISTS | HEXISTS key field，存在返回1 ，不存在返回0                |
| 字段不存在时赋值：HSETNX  | HSETNX key field value，与hset命令不同，hsetnx是键不存在时设置值 |
| 增加数字：HINCRBY    | HINCRBY key field increment ，返回增加后的数，不是整数时会提示错误 |
| 删除字段：HDEL       | HDEL key field [field ...] ，返回被删除字段的个数         |
| 只获取字段名：HKEYS  | HKEYS key ，返回键的所有字段名                             |
| 只获取字段值：HVALS | HVALS key  ，返回键的所有字段值                            |
| 字段数量：HLEN       | HLEN key ，返回字段总数                                       |

#列表类型（list）

内部使用双向链表实现，所以获取越接近两端的元素速度越快，但通过索引访问时会比较慢

| 函数                            | 语法                                                                                                                                                                                                |
| --------------------------------- | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| 添加左边元素：LPUSH        | LPUSH key value [value ...]  ，返回添加后的列表元素的总个数                                                                                                                            |
| 添加右边元素：RPUSH        | RPUSH key value [value ...]  ，返回添加后的列表元素的总个数                                                                                                                            |
| 移除左边第一个元素：LPOP  | LPOP key  ，返回被移除的元素值                                                                                                                                                              |
| 移除右边第一个元素：RPOP | RPOP key ，返回被移除的元素值                                                                                                                                                               |
| 列表元素个数：LLEN         | LLEN key， 不存在时返回0，redis是直接读取现成的值，并不是统计个数                                                                                                            |
| 获取列表片段：LRANGE       | LRANGE key start stop，如果start比stop靠后时返回空列表，0 -1 返回整个列表正数时：start 开始索引值，stop结束索引值（索引从0开始）负数时：例如 lrange num -2 -1，-2表示最右边第二个，-1表示最右边第一个 |
| 删除指定值：LREM            | LREM key count value，返回被删除的个数，count>0，从左边开始删除前count个值为value的元素，count<0，从右边开始删除前|count|个值为value的元素，count=0，删除所有值为value的元素 |
| 索引元素值：LINDEX          | LINDEX key index ，返回索引的元素值，-1表示从最右边的第一位                                                                                                                       |
| 设置元素值：LSET            | LSET key index value                                                                                                                                                                                  |
| 保留列表片段：LTRIM        | LTRIM key start stop，start、top 参考lrange命令                                                                                                                                                 |
| 一个列表转移另一个列表：RPOPLPUSH | RPOPLPUSH source desctination ，从source列表转移到desctination列表，该命令分两步看，首先source列表RPOP右移除，再desctination列表LPUSH                                    |

#集合类型（set）

集合类型值具有唯一性，常用操作是向集合添加、删除、判断某个值是否存在，集合内部是使用值为空的散列表实现的。

| 函数                                                                                                                                                                                                                   | 语法                                                                                                                                                                                                                   |
| ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ |
| 添加元素：SADD                                                                                                                                                                                                      |                                                                                                                                                                                                                          |
| SADD key member [member ...] ，向一个集合添加一个或多个元素，因为集合的唯一性，所以添加相同值时会被忽略。返回成功添加元素的数量。                                      | SADD key member [member ...] ，向一个集合添加一个或多个元素，因为集合的唯一性，所以添加相同值时会被忽略。返回成功添加元素的数量。                                      |
|                                                                                                                                                                                                                          |                                                                                                                                                                                                                          |
| 删除元素：SREM                                                                                                                                                                                                      | SREM key member [member ...] 删除集合中一个或多个元素，返回成功删除的个数。                                                                                                                       |
| 获取全部元素：SMEMBERS                                                                                                                                                                                            | SMEMBERS key ，返回集合全部元素                                                                                                                                                                                 |
| 值是否存在：SISMEMBER                                                                                                                                                                                              | 值是否存在：SISMEMBER                                                                                                                                                                                              |
| SISMEMBER key member ，如果存在返回1，不存在返回0                                                                                                                                                           | SISMEMBER key member ，如果存在返回1，不存在返回0                                                                                                                                                           |
| 差运算：SDIFF                                                                                                                                                                                                        | SDIFF key [key ...] ，例如：集合A和集合B，差集表示A-B，在A里有的元素B里没有，返回差集合；多个集合(A-B)-C                                                                              |
| 交运算：SINTER                                                                                                                                                                                                       | SINTER key [key ...]，返回交集集合，每个集合都有的元素                                                                                                                                                  |
| 并运算：SUNION　                                                                                                                                                                                                    | SUNION key [key ...]，返回并集集合，所有集合的元素                                                                                                                                                        |
| 集合元素个数：SCARD                                                                                                                                                                                               | SCARD key ，返回集合元素个数                                                                                                                                                                                    |
| 集合运算后存储结果                                                                                                                                                                                              |                                                                                                                                                                                                                          |
| SDIFFSTROE destination key [key ...] ，差运算并存储到destination新集合中SINTERSTROE destination key [key ...]，交运算并存储到destination新集合中SUNIONSTROE destination key [key ...]，并运算并存储到destination新集合中 | SDIFFSTROE destination key [key ...] ，差运算并存储到destination新集合中SINTERSTROE destination key [key ...]，交运算并存储到destination新集合中SUNIONSTROE destination key [key ...]，并运算并存储到destination新集合中 |
|                                                                                                                                                                                                                          |                                                                                                                                                                                                                          |
| 随机获取元素：SRANDMEMGER                                                                                                                                                                                         |                                                                                                                                                                                                                          |
| SRANDMEMBER key [count]，根据count不同有不同结果，count大于元素总数时返回全部元素count>0 ，返回集合中count不重复的元素count<0，返回集合中count的绝对值个元素，但元素可能会重复 | SRANDMEMBER key [count]，根据count不同有不同结果，count大于元素总数时返回全部元素count>0 ，返回集合中count不重复的元素count<0，返回集合中count的绝对值个元素，但元素可能会重复 |
|                                                                                                                                                                                                                          |                                                                                                                                                                                                                          |
| 弹出元素：SPOP                                                                                                                                                                                                      | SPOP key [count] ，因为集合是无序的，所以spop会随机弹出一个元素                                                                                                                                     |


#有序集合类型 zset(sorted set：有序集合)

Redis zset 和 set 一样也是string类型元素的集合,且不允许重复的成员。

不同的是每个元素都会关联一个double类型的分数。

redis正是通过分数来为集合中的成员进行从小到大的排序。zset的成员是唯一的,但分数(score)却可以重复。

| 函数                                                                                                                                                                                                                 | 语法                                                                                                                                                                                                                 |
| ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| 添加集合元素：ZADD                                                                                                                                                                                              | ZADD key [NX|XX] [CH] [INCR] score member [score member ...]，不存在添加，存在更新。                                                                                                                       |
| 获取元素分数：ZSCORE                                                                                                                                                                                            | ZSCORE key member ，返回元素成员的score 分数                                                                                                                                                                 |
| 元素小到大：ZRANGE                                                                                                                                                                                               |                                                                                                                                                                                                                        |
| ZRANGE key start top [WITHSCORES] ，参考LRANGE ，加上withscores 返回带元素，即元素，分数当分数一样时，按元素排序                                                                         | ZRANGE key start top [WITHSCORES] ，参考LRANGE ，加上withscores 返回带元素，即元素，分数当分数一样时，按元素排序                                                                         |
|                                                                                                                                                                                                                        |                                                                                                                                                                                                                        |
| 元素大到小：ZREVRANGE                                                                                                                                                                                            | ZREVRANGE key start [WITHSCORES] ，与zrange区别在于zrevrange是从大到小排序                                                                                                                                |
| 指定分数范围元素：ZRANGEBYSCORE                                                                                                                                                                               |                                                                                                                                                                                                                        |
| ZRANGEBYSCORE key min max [WITHSCORE] [LIMIT offest count]返回从小到大的在min和max之间的元素，( 符号表示不包含，例如：80-100，(80 100，withscore返回带分数limit offest count 向左偏移offest个元素，并获取前count个元素 | ZRANGEBYSCORE key min max [WITHSCORE] [LIMIT offest count]返回从小到大的在min和max之间的元素，( 符号表示不包含，例如：80-100，(80 100，withscore返回带分数limit offest count 向左偏移offest个元素，并获取前count个元素 |
|                                                                                                                                                                                                                        |                                                                                                                                                                                                                        |
| 指定分数范围元素：ZREVRANGESCORE                                                                                                                                                                              |                                                                                                                                                                                                                        |
| ZREVRANGEBYSCORE key max  min [WITHSCORE] [LIMIT offest count]与zrangebyscore类似，只不过该命令是从大到小排序的。                                                                                   | ZREVRANGEBYSCORE key max  min [WITHSCORE] [LIMIT offest count]与zrangebyscore类似，只不过该命令是从大到小排序的。                                                                                   |
|                                                                                                                                                                                                                        |                                                                                                                                                                                                                        |
| 增加分数：ZINCRBY                                                                                                                                                                                                 | ZINCRBY key increment member ，注意是增加分数，返回增加后的分数；如果成员不存在，则添加一个为0的成员。                                                                             |

