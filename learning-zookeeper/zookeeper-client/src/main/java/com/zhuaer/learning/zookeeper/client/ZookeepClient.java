package com.zhuaer.learning.zookeeper.client;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import com.zhuaer.learning.zookeeper.util.BaseZookeeper;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.data.Stat;

/**
 * @ClassName ZookeepClient
 * @Description TODO
 * @Author zhua
 * @Date 2020/11/17 16:23
 * @Version 1.0
 */
public class ZookeepClient {
    public static void main(String[] args) throws IOException,
            InterruptedException, KeeperException {
        BaseZookeeper baseZookeeper = new BaseZookeeper();

        String host = "192.168.1.199:2181";

        baseZookeeper.connectZookeeper(host);
        System.out.println("--------connect zookeeper ok-----------");

        byte[] data = { 1, 2, 3, 4, 5 };

        System.out.println("--------create node ok-----------");

        List<String> children = baseZookeeper.getChildren("/");
        for (String child : children) {
            System.out.println(child);
            System.out.println("--------get children ok-----------");

            byte[] nodeData = baseZookeeper.getData("/master");
            System.out.println(new String(nodeData));
            System.out.println("--------get node data ok-----------");

            data = "test".getBytes();
            Stat stat = baseZookeeper.exists("/master", false);

            baseZookeeper.setData("/master", data, stat.getVersion());
            System.out.println("--------set node data ok-----------");

            nodeData = baseZookeeper.getData("/master");
            System.out.println(Arrays.toString(nodeData));
            System.out.println("--------get node new data ok-----------");
            Thread.currentThread();
            Thread.sleep(60000L);
            baseZookeeper.closeConnect();
            System.out.println("--------close zookeeper ok-----------");
        }
    }
}
