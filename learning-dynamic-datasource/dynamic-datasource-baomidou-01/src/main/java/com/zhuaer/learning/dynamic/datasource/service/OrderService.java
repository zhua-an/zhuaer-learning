package com.zhuaer.learning.dynamic.datasource.service;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.zhuaer.learning.dynamic.datasource.dataobject.OrderDO;
import com.zhuaer.learning.dynamic.datasource.dataobject.UserDO;
import com.zhuaer.learning.dynamic.datasource.mapper.OrderMapper;
import com.zhuaer.learning.dynamic.datasource.mapper.UserMapper;
import com.zhuaer.learning.dynamic.datasource.constant.DBConstants;
import org.springframework.aop.framework.AopContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * @ClassName OrderService
 * @Description TODO
 * @Author zhua
 * @Date 2020/7/21 11:05
 * @Version 1.0
 */
@Service
public class OrderService {

    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private UserMapper userMapper;

    private OrderService self() {
        return (OrderService) AopContext.currentProxy();
    }

    public void method01() {
        // 查询订单
        OrderDO order = orderMapper.selectById(1);
        System.out.println(order);
        // 查询用户
        UserDO user = userMapper.selectById(1);
        System.out.println(user);
    }

    @Transactional
    public void method02() {
        // 查询订单
        OrderDO order = orderMapper.selectById(1);
        System.out.println(order);
        // 查询用户
        UserDO user = userMapper.selectById(1);
        System.out.println(user);
    }

    public void method03() {
        // 查询订单
        self().method031();
        // 查询用户
        self().method032();
    }

    @Transactional // 报错，因为此时获取的是 primary 对应的 DataSource ，即 users 。
    public void method031() {
        OrderDO order = orderMapper.selectById(1);
        System.out.println(order);
    }

    @Transactional
    public void method032() {
        UserDO user = userMapper.selectById(1);
        System.out.println(user);
    }

    public void method04() {
        // 查询订单
        self().method041();
        // 查询用户
        self().method042();
    }

    @Transactional
    @DS(DBConstants.DATASOURCE_ORDERS)
    public void method041() {
        OrderDO order = orderMapper.selectById(1);
        System.out.println(order);
    }

    @Transactional
    @DS(DBConstants.DATASOURCE_USERS)
    public void method042() {
        UserDO user = userMapper.selectById(1);
        System.out.println(user);
    }

    @Transactional
    @DS(DBConstants.DATASOURCE_ORDERS)
    public void method05() {
        // 查询订单
        OrderDO order = orderMapper.selectById(1);
        System.out.println(order);
        // 查询用户
        self().method052();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @DS(DBConstants.DATASOURCE_USERS)
    public void method052() {
        UserDO user = userMapper.selectById(1);
        System.out.println(user);
    }
}
