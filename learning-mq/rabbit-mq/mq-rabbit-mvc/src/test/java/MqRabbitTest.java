import org.junit.runner.RunWith;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @ClassName MqRabbitTest
 * @Description TODO
 * @Author zhua
 * @Date 2020/8/17 19:38
 * @Version 1.0
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath:conf/applicationContext.xml"})
public class MqRabbitTest {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @org.junit.Test
    public void runTest() {
        for(int i = 0; i < 10; i++) {
            System.out.println("rabbit send message:" + i);
            rabbitTemplate.convertAndSend("queuekey", "rabbit send message:" + i);
        }

    }
}
