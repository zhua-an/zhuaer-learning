package dynamicdatasource.service;

import com.zhuaer.learning.dynamic.datasource.DynamicDatasourceApplication;
import com.zhuaer.learning.dynamic.datasource.dataobject.OrderDO;
import com.zhuaer.learning.dynamic.datasource.service.OrderService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @ClassName OrderServiceTest
 * @Description TODO
 * @Author zhua
 * @Date 2020/7/21 11:33
 * @Version 1.0
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = DynamicDatasourceApplication.class)
public class OrderServiceTest {

    @Autowired
    private OrderService orderService;

    @Test
    public void testAdd() {
        OrderDO order = new OrderDO();
        order.setUserId(20);
        orderService.add(order);
    }

    @Test
    public void testFindById() {
        OrderDO order = orderService.findById(1);
        System.out.println(order);
    }
}
