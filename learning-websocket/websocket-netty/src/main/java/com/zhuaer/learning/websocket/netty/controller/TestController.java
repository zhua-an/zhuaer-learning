package com.zhuaer.learning.websocket.netty.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

/**
 * @ClassName TestController
 * @Description TODO
 * @Author zhua
 * @Date 2020/10/28 11:49
 * @Version 1.0
 */
@RestController
public class TestController {

    @GetMapping("/index")
    public ModelAndView index(){
        ModelAndView mav=new ModelAndView("index");
        mav.addObject("uid", System.currentTimeMillis());
        return mav;
    }
}
