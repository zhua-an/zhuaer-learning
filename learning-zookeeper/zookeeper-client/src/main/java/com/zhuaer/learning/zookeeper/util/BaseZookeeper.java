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

    /**
     * 建立连接
     * @param host
     * @throws IOException
     * @throws InterruptedException
     */
    public void connectZookeeper(String host) throws IOException,
            InterruptedException {
        this.zooKeeper = new ZooKeeper(host, 2000, this);
        this.countDownLatch.await();
        System.out.println("zookeeper connect ok");
    }

    //监听事件处理方法
    public void process(WatchedEvent event) {
        if (event.getState() == Watcher.Event.KeeperState.SyncConnected) {
            System.out.println("watcher received event");
            this.countDownLatch.countDown();
        }
    }

    /**
     * 创建节点
     * @param path
     * @param data
     * CreateMode
     *    PERSISTENT : 持久节点
     *    PERSISTENT_SEQUENTIAL : 持久顺序节点
     *    EPHEMERAL : 临时节点
     *    EPHEMERAL_SEQUENTIAL : 临时顺序节点
     * @return
     * @throws KeeperException
     * @throws InterruptedException
     */
    public String createNode(String path, byte[] data) throws KeeperException,
            InterruptedException {
        return this.zooKeeper.create(path, data, ZooDefs.Ids.OPEN_ACL_UNSAFE,
                CreateMode.PERSISTENT);
    }

    /**
     * 获取孩子节点
     * @param path
     * @return
     * @throws KeeperException
     * @throws InterruptedException
     */
    public List<String> getChildren(String path) throws KeeperException,
            InterruptedException {
        return this.zooKeeper.getChildren(path, false);
    }

    /**
     * 设置节点数据
     * @param path
     * @param data
     * @param version
     * @return
     * @throws KeeperException
     * @throws InterruptedException
     */
    public Stat setData(String path, byte[] data, int version)
            throws KeeperException, InterruptedException {
        return this.zooKeeper.setData(path, data, version);
    }

    /**
     * 获取节点数据
     * @param path
     * @return
     * @throws KeeperException
     * @throws InterruptedException
     */
    public byte[] getData(String path) throws KeeperException,
            InterruptedException {
        return this.zooKeeper.getData(path, false, null);
    }

    /**
     * 删除节点
     * @param path
     * @param version
     * @throws InterruptedException
     * @throws KeeperException
     */
    public void deletNode(String path, int version)
            throws InterruptedException, KeeperException {
        this.zooKeeper.delete(path, version);
    }

    /**
     * 断开连接
     * @throws InterruptedException
     */
    public void closeConnect() throws InterruptedException {
        if (null != this.zooKeeper) {
            this.zooKeeper.close();
        }
    }

    /**
     * 判断节点是否存在
     * @param path
     * @param tag
     * @return
     */
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
