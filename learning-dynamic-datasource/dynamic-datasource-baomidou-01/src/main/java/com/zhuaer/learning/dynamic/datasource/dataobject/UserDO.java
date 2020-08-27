package com.zhuaer.learning.dynamic.datasource.dataobject;

/**
 * @ClassName UserDO
 * @Description 用户 DO
 * @Author zhua
 * @Date 2020/7/21 11:06
 * @Version 1.0
 */
public class UserDO {

    /**
     * 用户编号
     */
    private Integer id;
    /**
     * 账号
     */
    private String username;

    public Integer getId() {
        return id;
    }

    public UserDO setId(Integer id) {
        this.id = id;
        return this;
    }

    public String getUsername() {
        return username;
    }

    public UserDO setUsername(String username) {
        this.username = username;
        return this;
    }

    @Override
    public String toString() {
        return "UserDO{" +
                "id=" + id +
                ", username='" + username + '\'' +
                '}';
    }
}
