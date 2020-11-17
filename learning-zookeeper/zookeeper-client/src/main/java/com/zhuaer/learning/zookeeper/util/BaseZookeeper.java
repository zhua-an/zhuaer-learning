package com.zhuaer.learning.zookeeper.util;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;

/**
 * @ClassName BaseZookeeper
 * @Description TODO
 * @Author zhua
 * @Date 2020/11/16 17:55
 * @Version 1.0
 */
public class BaseZookeeper implements Watcher {

    public ZooKeeper zooKeeper;
    private static final int SESSION_TIME_OUT = 2000;
    private CountDownLatch countDownLatch;

    public BaseZookeeper() {
        this.countDownLatch = new CountDownLatch(1);
    }

    public void connectZookeeper(String host) throws IOException,
            InterruptedException {
        this.zooKeeper = new ZooKeeper(host, 2000, this);
        this.countDownLatch.await();
        System.out.println("zookeeper connect ok");
    }

    public void process(WatchedEvent event) {
        if (event.getState() == Watcher.Event.KeeperState.SyncConnected) {
            System.out.println("watcher received event");
            this.countDownLatch.countDown();
        }
    }

    public String createNode(String path, byte[] data) throws KeeperException,
            InterruptedException {
        return this.zooKeeper.create(path, data, ZooDefs.Ids.OPEN_ACL_UNSAFE,
                CreateMode.PERSISTENT);
    }

    public List<String> getChildren(String path) throws KeeperException,
            InterruptedException {
        return this.zooKeeper.getChildren(path, false);
    }

    public Stat setData(String path, byte[] data, int version)
            throws KeeperException, InterruptedException {
        return this.zooKeeper.setData(path, data, version);
    }

    public byte[] getData(String path) throws KeeperException,
            InterruptedException {
        return this.zooKeeper.getData(path, false, null);
    }

    public void deletNode(String path, int version)
            throws InterruptedException, KeeperException {
        this.zooKeeper.delete(path, version);
    }

    public void closeConnect() throws InterruptedException {
        if (null != this.zooKeeper) {
            this.zooKeeper.close();
        }
    }

    public Stat exists(String path, boolean tag) {
        Stat st = null;
        try {
            st = this.zooKeeper.exists(path, tag);
        } catch (KeeperException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return st;
    }

}
