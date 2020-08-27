package com.zhuaer.learning.sharding.jdbc.mapper;

import com.zhuaer.learning.sharding.jdbc.dataobject.UserDO;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserMapper {

    UserDO selectById(@Param("id") Integer id);

}
