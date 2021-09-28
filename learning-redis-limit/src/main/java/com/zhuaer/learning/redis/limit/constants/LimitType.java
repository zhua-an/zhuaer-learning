package com.zhuaer.learning.redis.limit.constants;

/**
 * @ClassName LimitType
 * @Description TODO
 * @Author zhua
 * @Date 2021/9/28 11:16
 * @Version 1.0
 */
public enum LimitType {
    /**
     * 自定义key
     */
    CUSTOMER,
    /**
     * 根据请求者IP
     */
    IP;
}
