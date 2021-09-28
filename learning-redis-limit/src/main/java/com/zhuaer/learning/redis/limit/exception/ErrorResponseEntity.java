package com.zhuaer.learning.redis.limit.exception;

/**
 * @ClassName ErrorResponseEntity
 * @Description 异常信息模板
 * @Author zhua
 * @Date 2021/9/28 11:28
 * @Version 1.0
 */
public class ErrorResponseEntity {

    private int code;
    private String message;

    public ErrorResponseEntity(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
