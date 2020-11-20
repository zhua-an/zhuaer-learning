import com.zhuaer.learning.mq.rabbit.RabbitApplication;
import com.zhuaer.learning.mq.rabbit.config.TopicRabbitMqConfig;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;

/**
 * @ClassName RabbitMQTest
 * @Description TODO
 * @Author zhua
 * @Date 2020/8/11 15:10
 * @Version 1.0
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = RabbitApplication.class)
@Slf4j
public class RabbitMQTest {
    @Resource
    private RabbitTemplate rabbitTemplate;

    @Test
    public void sendTest(){
        rabbitTemplate.convertAndSend(TopicRabbitMqConfig.TOPIC_EXCHANGE_NAME,"item.insert","topic通配符模式,RoutingKey:item.insert");
        rabbitTemplate.convertAndSend(TopicRabbitMqConfig.TOPIC_EXCHANGE_NAME,"item.delete.yes","topic通配符模式,RoutingKey:item.delete.yes");
        rabbitTemplate.convertAndSend(TopicRabbitMqConfig.TOPIC_EXCHANGE_NAME,"null.null","topic通配符模式,RoutingKey:null.null");
    }
}
