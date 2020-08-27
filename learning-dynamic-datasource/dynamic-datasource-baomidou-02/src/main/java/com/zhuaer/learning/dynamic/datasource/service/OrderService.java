package com.zhuaer.learning.dynamic.datasource.service;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.zhuaer.learning.dynamic.datasource.constant.DBConstants;
import com.zhuaer.learning.dynamic.datasource.dataobject.OrderDO;
import com.zhuaer.learning.dynamic.datasource.mapper.OrderMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
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

    @Transactional
    @DS(DBConstants.DATASOURCE_MASTER)
    public void add(OrderDO order) {
        // 这里先假模假样的读取一下，
        OrderDO exists = orderMapper.selectById(order.getId());
        System.out.println(exists);

        // 插入订单
        orderMapper.insert(order);
    }

    public OrderDO findById(Integer id) {
        return orderMapper.selectById(id);
    }
}
