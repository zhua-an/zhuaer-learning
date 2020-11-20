import com.zhuaer.learning.mq.active.Application;
import com.zhuaer.learning.mq.active.handler.ActiveMqProducer;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @ClassName ActiveMqTests
 * @Description TODO
 * @Author zhua
 * @Date 2020/11/20 15:04
 * @Version 1.0
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {Application.class})
public class ActiveMqTests {

    @Autowired
    private ActiveMqProducer activeMqProducer;

    @Test
    public void sendSimpleQueueMessage() {
        activeMqProducer.sendMsg("提现200.00元");
    }

    @Test
    public void sendSimpleTopicMessage() {
        activeMqProducer.sendTopic("提现200.00元");
    }
}
