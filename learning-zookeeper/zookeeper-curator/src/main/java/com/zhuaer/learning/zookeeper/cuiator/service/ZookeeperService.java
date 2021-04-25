package com.zhuaer.learning.zookeeper.cuiator.service;

import org.apache.curator.framework.recipes.cache.TreeCacheListener;
import org.apache.curator.framework.recipes.locks.InterProcessReadWriteLock;
import org.apache.zookeeper.CreateMode;

import java.util.List;

/**
 * @ClassName ZookeeperService
 * @Description TODO
 * @Author zhua
 * @Date 2021/4/25 11:35
 * @Version 1.0
 */
public interface ZookeeperService {

    /**
     * 判断节点是否存在
     */
    boolean isExistNode (final String path) ;

    /**
     * 创建节点
     * @param mode       节点类型
     *                   1、PERSISTENT 持久化目录节点，存储的数据不会丢失。
     *                   2、PERSISTENT_SEQUENTIAL顺序自动编号的持久化目录节点，存储的数据不会丢失
     *                   3、EPHEMERAL临时目录节点，一旦创建这个节点的客户端与服务器端口也就是session 超时，这种节点会被自动删除
     *                   4、EPHEMERAL_SEQUENTIAL临时自动编号节点，一旦创建这个节点的客户端与服务器端口也就是session 超时，这种节点会被自动删除，并且根据当前已近存在的节点数自动加 1，然后返回给客户端已经成功创建的目录节点名。
     * @param path  节点名称
     */
    void createNode (CreateMode mode, String path ) ;

    /**
     * 创建节点
     * @param mode       节点类型
     * 1、PERSISTENT 持久化目录节点，存储的数据不会丢失。
     * 2、PERSISTENT_SEQUENTIAL顺序自动编号的持久化目录节点，存储的数据不会丢失
     * 3、EPHEMERAL临时目录节点，一旦创建这个节点的客户端与服务器端口也就是session 超时，这种节点会被自动删除
     *4、EPHEMERAL_SEQUENTIAL临时自动编号节点，一旦创建这个节点的客户端与服务器端口也就是session 超时，这种节点会被自动删除，并且根据当前已近存在的节点数自动加 1，然后返回给客户端已经成功创建的目录节点名。
     * @param path  节点名称
     * @param nodeData  节点数据
     */
    void createNode (CreateMode mode, String path, String nodeData ) ;

    /**
     * 设置节点数据
     */
    void setNodeData (String path, String nodeData) ;

    /**
     * 获取节点数据
     */
    String getNodeData (String path) ;

    /**
     * 获取节点下数据
     */
    List<String> getNodeChild (String path) ;

    /**
     * 是否递归删除节点
     * @param path
     * @param recursive     是否删除子节点
     */
    void deleteNode (String path,Boolean recursive) ;

    /**
     * 获取读写锁
     */
    InterProcessReadWriteLock getReadWriteLock (String path) ;

    /**
     * 获取数据时先同步
     * @param path
     * @return
     */
    String synNodeData(String path);

    /**
     * 随机读取一个path子路径, "/"为根节点对应该namespace
     * 先从cache中读取，如果没有，再从zookeeper中查询
     * @param path
     * @return
     */
    String getRandomData(String path);

//    /**
//     * 可重入共享锁  -- Shared Reentrant Lock
//     * @param lockPath
//     * @param time
//     * @param dealWork      获取
//     * @return
//     */
//    Object getSRLock(String lockPath,long time, SRLockDealCallback<?> dealWork);

    /**
     * 监听数据节点的变化情况
     * @param watchPath
     * @param listener
     */
    void watchPath(String watchPath, TreeCacheListener listener);
}
