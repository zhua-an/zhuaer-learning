package com.zhuaer.learning.zookeeper.cuiator.service.impl;

import com.zhuaer.learning.zookeeper.cuiator.configuration.ZookeeperConfig;
import com.zhuaer.learning.zookeeper.cuiator.service.ZookeeperService;
import org.apache.commons.lang3.StringUtils;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.TreeCache;
import org.apache.curator.framework.recipes.cache.TreeCacheListener;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.apache.curator.framework.recipes.locks.InterProcessReadWriteLock;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @ClassName ZookeeperServiceImpl
 * @Description TODO
 * @Author zhua
 * @Date 2021/4/25 11:36
 * @Version 1.0
 */
@Service
public class ZookeeperServiceImpl implements ZookeeperService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ZookeeperServiceImpl.class);

    @Override
    public boolean isExistNode(String path) {
        CuratorFramework client = ZookeeperConfig.getClient();
        client.sync() ;
        try {
            Stat stat = client.checkExists().forPath(path);
            return client.checkExists().forPath(path) != null;
        } catch (Exception e) {
            LOGGER.error("isExistNode error...", e);
            e.printStackTrace();
        }
        return false;
    }


    @Override
    public void createNode(CreateMode mode, String path) {
        CuratorFramework client = ZookeeperConfig.getClient() ;
        try {
            // 递归创建所需父节点
            //使用creatingParentContainersIfNeeded()之后Curator能够自动递归创建所有所需的父节点
            client.create().creatingParentsIfNeeded().withMode(mode).forPath(path);
        } catch (Exception e) {
            LOGGER.error("createNode error...", e);
            e.printStackTrace();
        }
    }

    @Override
    public void createNode(CreateMode mode, String path, String nodeData) {
        CuratorFramework client = ZookeeperConfig.getClient() ;
        try {
            // 创建节点，关联数据
            //使用creatingParentContainersIfNeeded()之后Curator能够自动递归创建所有所需的父节点
            client.create().creatingParentsIfNeeded().withMode(mode).forPath(path,nodeData.getBytes("UTF-8"));
        } catch (Exception e) {
            LOGGER.error("createNode error...", e);
            e.printStackTrace();
        }
    }

    @Override
    public void setNodeData(String path, String nodeData) {
        CuratorFramework client = ZookeeperConfig.getClient() ;
        try {
            // 设置节点数据
            client.setData().forPath(path, nodeData.getBytes("UTF-8"));
        } catch (Exception e) {
            LOGGER.error("setNodeData error...", e);
            e.printStackTrace();
        }
    }
    @Override
    public String getNodeData(String path) {
        CuratorFramework client = ZookeeperConfig.getClient() ;
        try {
            TreeCache cache = ZookeeperConfig.getCache();
            if(cache != null){
                ChildData data = cache.getCurrentData(path);
                if(data != null){
                    return data.getData().toString();
                }
            }
            // 数据读取和转换
            byte[] dataByte = client.getData().forPath(path) ;
            String data = new String(dataByte,"UTF-8") ;
            if (StringUtils.isNotEmpty(data)){
                return data ;
            }
        }catch (Exception e) {
            LOGGER.error("getNodeData error...", e);
            e.printStackTrace();
        }
        return null;
    }
    @Override
    public List<String> getNodeChild(String path) {
        CuratorFramework client = ZookeeperConfig.getClient() ;
        List<String> nodeChildDataList = new ArrayList<>();
        try {
            // 节点下数据集
            nodeChildDataList = client.getChildren().forPath(path);
        } catch (Exception e) {
            LOGGER.error("getNodeChild error...", e);
            e.printStackTrace();
        }
        return nodeChildDataList;
    }
    @Override
    public void deleteNode(String path, Boolean recursive) {
        CuratorFramework client = ZookeeperConfig.getClient() ;
        try {
            if(recursive) {
                // 递归删除节点
                //guaranteed()删除一个节点，强制保证删除,
                // 只要客户端会话有效，那么Curator会在后台持续进行删除操作，直到删除节点成功
                client.delete().guaranteed().deletingChildrenIfNeeded().forPath(path);
            } else {
                // 删除单个节点
                client.delete().guaranteed().forPath(path);
            }
        } catch (Exception e) {
            LOGGER.error("deleteNode error...", e);
            e.printStackTrace();
        }
    }
    @Override
    public InterProcessReadWriteLock getReadWriteLock(String path) {
        CuratorFramework client = ZookeeperConfig.getClient() ;
        // 写锁互斥、读写互斥
        InterProcessReadWriteLock readWriteLock = new InterProcessReadWriteLock(client, path);
        return readWriteLock ;
    }

    @Override
    public String synNodeData(String path) {
        CuratorFramework client = ZookeeperConfig.getClient() ;
        client.sync();
        return getNodeData(path);
    }

    /**
     * 随机读取一个path子路径, "/"为根节点对应该namespace
     * 先从cache中读取，如果没有，再从zookeeper中查询
     * @param path
     * @return
     * @throws Exception
     */
    public String getRandomData(String path)  {
        try{
            TreeCache cache = ZookeeperConfig.getCache();
            Map<String,ChildData> cacheMap = cache.getCurrentChildren(path);
            if(cacheMap != null && cacheMap.size() > 0) {
                LOGGER.debug("get random value from cache,path="+path);
                Collection<ChildData> values = cacheMap.values();
                List<ChildData> list = new ArrayList<>(values);
                Random rand = new Random();
                byte[] b = list.get(rand.nextInt(list.size())).getData();
                return new String(b,"utf-8");
            }
            if(isExistNode(path)) {
                LOGGER.debug("path [{}] is not exists,return null",path);
                return null;
            } else {
                CuratorFramework client = ZookeeperConfig.getClient() ;
                LOGGER.debug("read random from zookeeper,path="+path);
                List<String> list = client.getChildren().forPath(path);
                if(list == null || list.size() == 0) {
                    LOGGER.debug("path [{}] has no children return null",path);
                    return null;
                }
                Random rand = new Random();
                String child = list.get(rand.nextInt(list.size()));
                path = path + "/" + child;
                byte[] b = client.getData().forPath(path);
                String value = new String(b,"utf-8");
                return value;
            }
        }catch(Exception e){
            LOGGER.error("{}",e);
        }
        return null;
    }

    @Override
    public void watchPath(String watchPath, TreeCacheListener listener) {
        /**
         * 在注册监听器的时候，如果传入此参数，当事件触发时，逻辑由线程池处理
         */
        ExecutorService pool = Executors.newFixedThreadPool(2);

        TreeCache cache = new TreeCache(ZookeeperConfig.getClient(), watchPath);
        cache.getListenable().addListener(listener,pool);
        try {
            cache.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

//    @Override
//    public Object getSRLock(String lockPath, long time, SRLockDealCallback<?> dealWork) {
//        InterProcessMutex lock = new InterProcessMutex(ZookeeperConfig.getClient(), lockPath);
//        try {
//            if (!lock.acquire(time, TimeUnit.SECONDS)) {
//                LOGGER.error("get lock fail:{}", " could not acquire the lock");
//                return null;
//            }
//            LOGGER.debug("{} get the lock",lockPath);
//            Object b = dealWork.deal();
//            return b;
//        }catch(Exception e){
//            LOGGER.error("{}", e);
//        }finally{
//            try {
//                lock.release();
//            } catch (Exception e) {
//                //log.error("{}",e);
//            }
//        }
//        return null;
//    }


}
