package com.zhuaer.learning.dynamic.datasource.jdbctemplate.dao;

import com.zhuaer.learning.dynamic.datasource.jdbctemplate.dataobject.OrderDO;
import com.zhuaer.learning.dynamic.datasource.jdbctemplate.constant.DBConstants;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;

@Repository
public class OrderDao {

    @Resource(name = DBConstants.JDBC_TEMPLATE_ORDERS)
    private JdbcTemplate template;

    public OrderDO selectById(Integer id) {
        return template.queryForObject("SELECT id, user_id FROM orders WHERE id = ?",
                new BeanPropertyRowMapper<>(OrderDO.class), // 结果转换成对应的对象
                id);
    }

}
