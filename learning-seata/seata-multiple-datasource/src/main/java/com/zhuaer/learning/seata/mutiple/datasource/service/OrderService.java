package com.zhuaer.learning.seata.mutiple.datasource.service;

import com.zhuaer.learning.seata.mutiple.datasource.common.OperationResponse;
import com.zhuaer.learning.seata.mutiple.datasource.common.order.PlaceOrderRequestVO;

/**
 * @author HelloWoodes
 */
public interface OrderService {

    /**
     * 下单
     *
     * @param placeOrderRequestVO 请求参数
     * @return 下单结果
     */
    OperationResponse placeOrder(PlaceOrderRequestVO placeOrderRequestVO) throws Exception;
}
