package com.zhuaer.learning.redis.config;

import com.alibaba.fastjson.JSON;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;

import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/**
 * @ClassName RedisDelayedQueueT
 * @Description TODO
 * @Author zhua
 * @Date 2020/8/21 17:17
 * @Version 1.0
 */
public class RedisDelayedQueueT<T> {

    //原子操作类
    private static final AtomicInteger COUNTER = new AtomicInteger(1);

    private volatile boolean started = false;

    private String queueKey;

    private Consumer<T> handler;

    private Class<T> classOfT;

    private StringRedisTemplate stringRedisTemplate;

    /**
     * @param queueKey 队列键值
     * @param classOfT 元素的类型
     * @param handler  处理器，如果元素到了时间，需要做的处理
     */
    public RedisDelayedQueueT(String queueKey, Class<T> classOfT, Consumer<T> handler) {
        this.queueKey = queueKey;
        this.handler = handler;
        this.classOfT = classOfT;
    }

    /**
     * 往该延时队列中放入数据,达到指定时间时处理
     *
     * @param value    数据
     * @param deadLine 截止时间戳，单位是毫秒
     * @return 是否执行成功
     */
    public boolean putForDeadLine(T value, long deadLine) {
        if (value == null) {
            return false;
        }
        long current = System.currentTimeMillis();
        if (deadLine < current) {
            throw new IllegalArgumentException(String.format("deadline: %d 小于当前时间: %d !", deadLine, current));
        }
        if (stringRedisTemplate == null) {
            throw new IllegalStateException("请设置stringRedisTemplate!");
        }
        String json = JSON.toJSONString(value);
        Boolean flag = stringRedisTemplate.opsForZSet().add(queueKey, json, deadLine);
        return Boolean.TRUE.equals(flag);
    }

    /**
     * 往该延时队列中放入数据,指定时间后执行
     *
     * @param value       数据
     * @param delayedTime 需要延长的时间，单位是毫秒
     * @return 是否执行成功
     */
    public boolean putForDelayedTime(T value, long delayedTime) {
        return putForDeadLine(value, System.currentTimeMillis() + delayedTime);
    }

    /**
     * 清除队列中的数据
     */
    public void clear() {
        stringRedisTemplate.opsForZSet().removeRangeByScore(queueKey, Double.MIN_VALUE, Double.MAX_VALUE);
    }

    /**
     * 验证队列是否存在 true 存在  false 不存在
     */
    public Boolean verify() {
        Long value = stringRedisTemplate.opsForZSet().zCard(queueKey);
        return value != null ? value > 0 : false;
    }


    public void setStringRedisTemplate(StringRedisTemplate stringRedisTemplate) {
        if (this.stringRedisTemplate == null && !started) {
            this.stringRedisTemplate = stringRedisTemplate;
            Worker worker = new Worker();
            worker.setName("delayed-queue-task-" + queueKey + "-" + COUNTER.getAndIncrement());
            worker.start();
            started = true;
        }
    }

    class Worker extends Thread {

        @Override
        public void run() {
            while (!Thread.currentThread().isInterrupted()) {
                long current = System.currentTimeMillis();
                Set<ZSetOperations.TypedTuple<String>> typedTuples = stringRedisTemplate.opsForZSet()
                        .rangeByScoreWithScores(queueKey, 0, current, 0, 1);
                if (typedTuples != null && typedTuples.size() > 0) {
                    ZSetOperations.TypedTuple<String> next = typedTuples.iterator().next();
                    if (next.getScore() != null && next.getScore() < current) {
                        Long removedCount = stringRedisTemplate.opsForZSet().remove(queueKey, next.getValue());
                        // 只有一个线程可以删除成功，代表拿到了这个需要处理的数据
                        if (removedCount != null && removedCount > 0) {
                            handler.accept(JSON.parseObject(next.getValue(), classOfT));
                        }
                    }
                }
                try {
                    TimeUnit.MILLISECONDS.sleep(10L + ThreadLocalRandom.current().nextInt(10));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            started = false;
        }
    }
}
