package com.zhuaer.learning.zookeeper;

import org.apache.zookeeper.server.ZooKeeperServerMain;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.File;

/**
 * @Description: zookeeper服务类
 * @Author: zhua
 * @Date: 2019/9/22 11:32
 */
@SpringBootApplication
public class ZookeeperApplication {

    /**
     * 启动单例zk server
     * @param tickTime Zookeeper中最小时间单元的长度
     * @param dataDir  Zookeeper服务器存储快照文件的目录
     * @param clientPort 当前服务器对外的服务端口
     * @param maxcnxn 客户端最大连接数，通过 IP 来区分不同的客户端
     */
    public static void startStandaloneServer(String tickTime, String dataDir, String clientPort, String maxcnxn) {
        ZooKeeperServerMain.main(new String[]{clientPort, dataDir, tickTime, maxcnxn}); // port datadir [ticktime] [maxcnxns]"
    }


    public static void main(String[] args) throws Exception {
//        startStandaloneServer("2000", new File(System.getProperty("java.io.tmpdir"), "zookeeper").getAbsolutePath(), "2181", "44");
        startStandaloneServer("2000", new File("logs", "zookeeper").getAbsolutePath(), "2181", "44");
    }

}
