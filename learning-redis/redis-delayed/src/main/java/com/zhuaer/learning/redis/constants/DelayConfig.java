package com.zhuaer.learning.redis.constants;

/**
 * @ClassName DelayConfig
 * @Description TODO
 * @Author zhua
 * @Date 2020/8/21 16:51
 * @Version 1.0
 */
public class DelayConfig {
    /**
     * 睡眠时间
     */
    public static Long SLEEP_TIME = 1000L;

    /**
     * 重试次数
     */
    public static Integer RETRY_COUNT = 5;

    /**
     * 默认超时时间
     */
    public static Long PROCESS_TIME = 5000L;
}
