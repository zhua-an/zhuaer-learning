package com.zhuaer.learning.dynamic.datasource.dataobject;

/**
 * @ClassName OrderDO
 * @Description 订单 DO
 * @Author zhua
 * @Date 2020/7/21 11:06
 * @Version 1.0
 */
public class OrderDO {

    /**
     * 订单编号
     */
    private Integer id;
    /**
     * 用户编号
     */
    private Integer userId;

    public Integer getId() {
        return id;
    }

    public OrderDO setId(Integer id) {
        this.id = id;
        return this;
    }

    public Integer getUserId() {
        return userId;
    }

    public OrderDO setUserId(Integer userId) {
        this.userId = userId;
        return this;
    }

    @Override
    public String toString() {
        return "OrderDO{" +
                "id=" + id +
                ", userId=" + userId +
                '}';
    }
}
