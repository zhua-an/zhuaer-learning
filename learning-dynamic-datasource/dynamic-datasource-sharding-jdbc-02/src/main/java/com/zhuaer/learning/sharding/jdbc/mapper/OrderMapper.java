package com.zhuaer.learning.sharding.jdbc.mapper;

import com.zhuaer.learning.sharding.jdbc.dataobject.OrderDO;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderMapper {

    OrderDO selectById(@Param("id") Integer id);

    int insert(OrderDO entity);

}
