package com.zhuaer.learning.redis.limit.exception;

/**
 * @ClassName RedisLimitException
 * @Description TODO
 * @Author zhua
 * @Date 2021/9/28 11:24
 * @Version 1.0
 */
public class RedisLimitException extends RuntimeException{

    private int code;

    public RedisLimitException() {
        super();
    }

    public RedisLimitException(String message) {
        super(message);
        this.code = 500;
    }

    public RedisLimitException(int code, String message) {
        super(message);
        this.setCode(code);
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }
}
