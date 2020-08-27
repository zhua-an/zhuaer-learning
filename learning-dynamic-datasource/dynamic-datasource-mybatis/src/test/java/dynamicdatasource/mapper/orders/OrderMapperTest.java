package dynamicdatasource.mapper.orders;

import com.zhuaer.learning.dynamic.datasource.mybatis.Application;
import com.zhuaer.learning.dynamic.datasource.mybatis.dataobject.OrderDO;
import com.zhuaer.learning.dynamic.datasource.mybatis.mapper.orders.OrderMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
public class OrderMapperTest {

    @Autowired
    private OrderMapper orderMapper;

    @Test
    public void testSelectById() {
        OrderDO order = orderMapper.selectById(1);
        System.out.println(order);
    }

}
