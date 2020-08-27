package com.zhuaer.learning.dynamic.datasource.mapper;

import com.zhuaer.learning.dynamic.datasource.DynamicDatasourceApplication;
import com.zhuaer.learning.dynamic.datasource.dataobject.UserDO;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @ClassName UserMapperTest
 * @Description TODO
 * @Author zhua
 * @Date 2020/7/21 11:36
 * @Version 1.0
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = DynamicDatasourceApplication.class)
public class UserMapperTest {

    @Autowired
    private UserMapper userMapper;

    @Test
    public void testSelectById() {
        UserDO user = userMapper.selectById(1);
        System.out.println(user);
    }

}