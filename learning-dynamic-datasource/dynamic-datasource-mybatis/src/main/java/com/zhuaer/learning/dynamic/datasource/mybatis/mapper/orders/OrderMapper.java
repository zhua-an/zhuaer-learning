package com.zhuaer.learning.dynamic.datasource.mybatis.mapper.orders;

import com.zhuaer.learning.dynamic.datasource.mybatis.dataobject.OrderDO;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderMapper {

    OrderDO selectById(@Param("id") Integer id);

}
