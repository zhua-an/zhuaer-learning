package dynamicdatasource.mapper;

import com.zhuaer.learning.dynamic.datasource.DynamicDatasourceApplication;
import com.zhuaer.learning.dynamic.datasource.dataobject.OrderDO;
import com.zhuaer.learning.dynamic.datasource.mapper.OrderMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @ClassName OrderMapperTest
 * @Description TODO
 * @Author zhua
 * @Date 2020/7/21 11:32
 * @Version 1.0
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = DynamicDatasourceApplication.class)
public class OrderMapperTest {

    @Autowired
    private OrderMapper orderMapper;

    @Test
    public void testSelectById() {
        for (int i = 0; i < 10; i++) {
            OrderDO order = orderMapper.selectById(1);
            System.out.println(order);
        }
    }

    @Test
    public void testInsert() {
        OrderDO order = new OrderDO();
        order.setUserId(10);
        orderMapper.insert(order);
    }
}
