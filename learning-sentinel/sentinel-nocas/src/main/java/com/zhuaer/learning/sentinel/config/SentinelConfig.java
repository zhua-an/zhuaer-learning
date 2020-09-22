package com.zhuaer.learning.sentinel.config;

import com.alibaba.csp.sentinel.annotation.aspectj.SentinelResourceAspect;
import com.alibaba.csp.sentinel.datasource.ReadableDataSource;
import com.alibaba.csp.sentinel.datasource.nacos.NacosDataSource;
import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.alibaba.nacos.api.PropertyKeyConst;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * @ClassName SentinelConfig
 * @Description TODO
 * @Author zhua
 * @Date 2020/9/2 18:41
 * @Version 1.0
 */
@Configuration
public class SentinelConfig {
    // nacos server ip
    private static final String remoteAddress = "localhost:8848";
    // nacos group
    private static final String groupId = "DEFAULT_GROUP";
    // nacos dataId
    private static final String dataId = "Sentinel:Demo";
    // fill your namespace id,if you want to use namespace. for example: 0f5c7314-4983-4022-ad5a-347de1d1057d,you can get it on nacos's console
    private static final String NACOS_NAMESPACE_ID = "sentinel";

    /**
     * 添加注解支持的配置
     * @return
     */
    @Bean
    public SentinelResourceAspect sentinelResourceAspect(){
        return new SentinelResourceAspect();
    }

    /**
     * 配置自定义限流
     * @throws Exception
     */
//    @PostConstruct
//    private void initRules() throws Exception {
//        FlowRule rule1 = new FlowRule();
//        rule1.setResource("/test/hello");
//        rule1.setGrade(RuleConstant.FLOW_GRADE_QPS);
//        rule1.setCount(1);   // 每秒调用最大次数为 1 次
//
//        List<FlowRule> rules = new ArrayList<>();
//        rules.add(rule1);
//
//        // 将控制规则载入到 Sentinel
//        com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager.loadRules(rules);
//    }

    /**
     * 动态配置
     */
    @PostConstruct
    private void loadMyNamespaceRules() {
        Properties properties = new Properties();
        properties.put(PropertyKeyConst.SERVER_ADDR, remoteAddress);
        properties.put(PropertyKeyConst.NAMESPACE, NACOS_NAMESPACE_ID);

        ReadableDataSource<String, List<FlowRule>> flowRuleDataSource = new NacosDataSource<>(properties, groupId, dataId,
                source -> JSON.parseObject(source, new TypeReference<List<FlowRule>>() {
                }));
        FlowRuleManager.register2Property(flowRuleDataSource.getProperty());
    }

}
