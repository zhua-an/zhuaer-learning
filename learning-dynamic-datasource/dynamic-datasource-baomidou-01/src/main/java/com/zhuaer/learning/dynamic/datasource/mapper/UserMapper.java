package com.zhuaer.learning.dynamic.datasource.mapper;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.zhuaer.learning.dynamic.datasource.constant.DBConstants;
import com.zhuaer.learning.dynamic.datasource.dataobject.UserDO;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

/**
 * @ClassName UserMapper
 * @Description TODO
 * @Author zhua
 * @Date 2020/7/21 11:08
 * @Version 1.0
 */
@Repository
@DS(DBConstants.DATASOURCE_USERS)
public interface UserMapper {

    UserDO selectById(@Param("id") Integer id);

}
