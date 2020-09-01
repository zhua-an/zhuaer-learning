package com.zhuan.learning.dubbo.provider.service;

import com.zhuaer.learning.dubbo.api.service.UserService;
import org.apache.dubbo.config.annotation.Service;

/**
 * @ClassName UserServiceImpl
 * @Description TODO
 * @Author zhua
 * @Date 2020/7/17 9:56
 * @Version 1.0
 */
@Service(version = "1.0.0")
public class UserServiceImpl implements UserService {
    @Override
    public String queryUser() {
        return "root";
    }
}
