package com.zhuaer.mq.rabbit.mvc.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;


/**
 * @ClassName Persion
 * @Description TODO
 * @Author zhua
 * @Date 2020/8/17 14:51
 * @Version 1.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class Persion implements Serializable {
    private String name;
    private java.util.Date today;

}
