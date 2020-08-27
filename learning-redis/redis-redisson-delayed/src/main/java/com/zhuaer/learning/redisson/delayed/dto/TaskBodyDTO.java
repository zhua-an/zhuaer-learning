package com.zhuaer.learning.redisson.delayed.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * @ClassName TaskBodyDTO
 * @Description TODO
 * @Author zhua
 * @Date 2020/8/21 15:41
 * @Version 1.0
 */
@Data
public class TaskBodyDTO implements Serializable {

    private String name;

    private String body;
}
