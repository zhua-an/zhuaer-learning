package com.zhuaer.learning.redis.contants;

import java.util.concurrent.TimeUnit;

/**
 * @ClassName Status
 * @Description 状态枚举
 * @Author zhua
 * @Date 2020/7/17 16:14
 * @Version 1.0
 */
public abstract class Status {

    /**
     * 过期时间相关枚举
     */
    public static enum ExpireEnum{
        //未读消息的有效期为30天
        UNREAD_MSG(30L, TimeUnit.DAYS)
        ;

        /**
         * 过期时间
         */
        private Long time;
        /**
         * 时间单位
         */
        private TimeUnit timeUnit;

        ExpireEnum(Long time, TimeUnit timeUnit) {
            this.time = time;
            this.timeUnit = timeUnit;
        }

        public Long getTime() {
            return time;
        }

        public TimeUnit getTimeUnit() {
            return timeUnit;
        }
    }
}
