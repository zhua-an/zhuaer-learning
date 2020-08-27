package com.zhuaer.learning.redis.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @ClassName JobTopic
 * @Description 任务类别
 * @Author zhua
 * @Date 2020/8/21 16:51
 * @Version 1.0
 */
@AllArgsConstructor
@Getter
public enum  JobTopic {

    TOPIC_ONE("one"),
    TOPIC_TWO("two");

    private String topic;
}
