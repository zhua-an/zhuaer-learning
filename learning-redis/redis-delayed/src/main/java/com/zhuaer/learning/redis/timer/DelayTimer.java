package com.zhuaer.learning.redis.timer;

import com.zhuaer.learning.redis.container.DelayBucket;
import com.zhuaer.learning.redis.container.JobPool;
import com.zhuaer.learning.redis.container.ReadyQueue;
import com.zhuaer.learning.redis.handler.DelayJobHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import java.util.concurrent.*;

/**
 * @ClassName DelayConfig
 * @Description 轮询处理
 * @Author zhua
 * @Date 2020/8/21 17:01
 * @Version 1.0
 */
@Component
public class DelayTimer implements ApplicationListener <ContextRefreshedEvent> {

    @Autowired
    private DelayBucket delayBucket;
    @Autowired
    private JobPool jobPool;
    @Autowired
    private ReadyQueue readyQueue;
    
    @Value("${thread.size}")
    private int length;
    
    @Override 
    public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
        ExecutorService executorService = new ThreadPoolExecutor(
                length, 
                length,
                0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue <Runnable>());

        for (int i = 0; i < length; i++) {
            executorService.execute(
                    new DelayJobHandler(
                            delayBucket,
                            jobPool,
                            readyQueue,
                            i));
        }
        
    }
}
