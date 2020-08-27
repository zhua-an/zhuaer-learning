package com.zhuaer.learning.dynamic.datasource.mapper;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.zhuaer.learning.dynamic.datasource.constant.DBConstants;
import com.zhuaer.learning.dynamic.datasource.dataobject.OrderDO;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

/**
 * @ClassName OrderMapper
 * @Description TODO
 * @Author zhua
 * @Date 2020/7/21 11:07
 * @Version 1.0
 */
@Repository
public interface OrderMapper {

    @DS(DBConstants.DATASOURCE_SLAVE)
    OrderDO selectById(@Param("id") Integer id);

    @DS(DBConstants.DATASOURCE_MASTER)
    int insert(OrderDO entity);

}
