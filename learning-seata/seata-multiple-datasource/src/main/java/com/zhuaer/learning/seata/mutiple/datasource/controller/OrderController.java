package com.zhuaer.learning.seata.mutiple.datasource.controller;

import com.zhuaer.learning.seata.mutiple.datasource.common.order.PlaceOrderRequestVO;
import com.zhuaer.learning.seata.mutiple.datasource.common.OperationResponse;
import com.zhuaer.learning.seata.mutiple.datasource.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @author HelloWoodes
 */
@RestController
@RequestMapping("/order")
@Slf4j
public class OrderController {

    @Autowired
    private OrderService orderService;

    @PostMapping("/placeOrder")
    @ResponseBody
    public OperationResponse placeOrder(@RequestBody PlaceOrderRequestVO placeOrderRequestVO) throws Exception {
        log.info("收到下单请求,用户:{}, 商品:{}", placeOrderRequestVO.getUserId(), placeOrderRequestVO.getProductId());
        return orderService.placeOrder(placeOrderRequestVO);
    }
}
