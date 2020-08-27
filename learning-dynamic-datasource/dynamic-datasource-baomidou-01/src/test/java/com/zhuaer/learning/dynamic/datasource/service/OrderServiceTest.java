package com.zhuaer.learning.dynamic.datasource.service;

import com.zhuaer.learning.dynamic.datasource.DynamicDatasourceApplication;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @ClassName OrderServiceTest
 * @Description TODO
 * @Author zhua
 * @Date 2020/7/21 11:11
 * @Version 1.0
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = DynamicDatasourceApplication.class)
public class OrderServiceTest {
    @Autowired
    private OrderService orderService;

    @Test
    public void testMethod01() {
        orderService.method01();
    }

    @Test
    public void testMethod02() {
        orderService.method02();
    }

    @Test
    public void testMethod03() {
        orderService.method03();
    }

    @Test
    public void testMethod04() {
        orderService.method04();
    }

    @Test
    public void testMethod05() {
        orderService.method05();
    }
}
