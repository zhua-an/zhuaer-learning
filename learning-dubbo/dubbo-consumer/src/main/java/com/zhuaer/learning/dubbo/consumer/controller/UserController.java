package com.zhuaer.learning.dubbo.consumer.controller;

import com.zhuaer.learning.dubbo.api.service.UserService;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @ClassName UserController
 * @Description TODO
 * @Author zhua
 * @Date 2020/7/17 9:56
 * @Version 1.0
 */
@RestController
public class UserController {

//    @Reference(version = "1.0.0")
    @Reference(url = "dubbo://127.0.0.1:20880", version = "1.0.0")
    private UserService userService;

    @GetMapping("user")
    public String user() {
        return userService.queryUser();
    }

}
