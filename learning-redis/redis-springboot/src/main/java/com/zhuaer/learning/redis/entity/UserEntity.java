package com.zhuaer.learning.redis.entity;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @ClassName UserEntity
 * @Description TODO
 * @Author zhua
 * @Date 2020/7/17 16:49
 * @Version 1.0
 */
@Data
public class UserEntity implements Serializable {
    private Long id;
    private String guid;
    private String name;
    private String age;
    private Date createTime;
}
