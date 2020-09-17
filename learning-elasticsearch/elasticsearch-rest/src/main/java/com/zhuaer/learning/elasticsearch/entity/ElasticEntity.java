package com.zhuaer.learning.elasticsearch.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Map;

/**
 * @ClassName ElasticEntity
 * @Description TODO
 * @Author zhua
 * @Date 2020/9/10 17:27
 * @Version 1.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ElasticEntity<T> implements Serializable {

    /**
     * 主键标识，用户ES持久化
     */
    private String id;

    /**
     * JSON对象，实际存储数据
     */
    private Map data;
}
