package com.zhuaer.learning.zookeeper.cuiator.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @ClassName ZookeeperProperties
 * @Description TODO
 * @Author zhua
 * @Date 2021/4/25 10:57
 * @Version 1.0
 */
@Data
@Component
@ConfigurationProperties(prefix = "zoo.keeper")
public class ZookeeperProperties {

    private String server;
    private String namespace;
    private String digest;
    private int sessionTimeoutMs;
    private int connectionTimeoutMs;
    private int maxRetries;
    private int baseSleepTimeMs;

}
