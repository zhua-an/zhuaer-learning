package com.zhuaer.learning.dynamic.datasource.mybatis.mapper.users;

import com.zhuaer.learning.dynamic.datasource.mybatis.dataobject.UserDO;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserMapper {

    UserDO selectById(@Param("id") Integer id);

}
