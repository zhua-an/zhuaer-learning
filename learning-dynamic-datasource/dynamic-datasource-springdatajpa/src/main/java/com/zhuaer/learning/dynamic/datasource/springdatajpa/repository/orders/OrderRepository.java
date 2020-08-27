package com.zhuaer.learning.dynamic.datasource.springdatajpa.repository.orders;

import com.zhuaer.learning.dynamic.datasource.springdatajpa.dataobject.OrderDO;
import org.springframework.data.repository.CrudRepository;

public interface OrderRepository extends CrudRepository<OrderDO, Integer> {

}
