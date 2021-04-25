package com.zhuaer.learning.zookeeper.cuiator.configuration;

import org.apache.commons.lang3.StringUtils;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.api.ACLProvider;
import org.apache.curator.framework.recipes.cache.TreeCache;
import org.apache.curator.framework.recipes.cache.TreeCacheListener;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.framework.state.ConnectionStateListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.data.ACL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.List;

/**
 * @ClassName ZookeeperConfig
 * @Description TODO
 * @Author zhua
 * @Date 2021/4/25 10:55
 * @Version 1.0
 */
@Configuration
public class ZookeeperConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger(ZookeeperConfig.class) ;
    @Resource
    private ZookeeperProperties zookeeperProperties ;
    private static CuratorFramework client = null ;
    public static TreeCache cache;
    /**
     * 初始化
     */
    @PostConstruct
    public void init (){
        try {
            //重试策略，初试时间1秒，重试10次
            RetryPolicy retryPolicy = new ExponentialBackoffRetry(
                    zookeeperProperties.getBaseSleepTimeMs(),
                    zookeeperProperties.getMaxRetries());
            //通过工厂创建Curator
            client = CuratorFrameworkFactory.builder()
                    .connectString(zookeeperProperties.getServer())
//                    .authorization("digest",zookeeperProperties.getDigest().getBytes())
                    .connectionTimeoutMs(zookeeperProperties.getConnectionTimeoutMs())
                    .sessionTimeoutMs(zookeeperProperties.getSessionTimeoutMs())
                    .retryPolicy(retryPolicy).build();

//        方式二：
//        CuratorFrameworkFactory.Builder builder   = CuratorFrameworkFactory.builder()
//                .connectString(zookeeperProperties.getServer()).retryPolicy(retryPolicy)
//                .sessionTimeoutMs( zookeeperProperties.getSessionTimeoutMs())
//                .connectionTimeoutMs( zookeeperProperties.getConnectionTimeoutMs())
//                .namespace( zookeeperProperties.getNamespace());
//        if(StringUtils.isNotEmpty( zookeeperProperties.getDigest())){
//            builder.authorization("digest", zookeeperProperties.getDigest().getBytes());
//            builder.aclProvider(new ACLProvider() {
//                @Override
//                public List<ACL> getDefaultAcl() {
//                    return ZooDefs.Ids.CREATOR_ALL_ACL;
//                }
//
//                @Override
//                public List<ACL> getAclForPath(final String path) {
//                    return ZooDefs.Ids.CREATOR_ALL_ACL;
//                }
//            });
//        }
//        client = builder.build();

            //开启连接
            client.start();
            LOGGER.info("zookeeper 初始化完成...");

            initLocalCache("/test");

            //连接监听
            client.getConnectionStateListenable().addListener(new ConnectionStateListener() {
                public void stateChanged(CuratorFramework client, ConnectionState state) {
                    if (state == ConnectionState.LOST) {
                        //连接丢失
                        LOGGER.info("lost session with zookeeper");
                    } else if (state == ConnectionState.CONNECTED) {
                        //连接新建
                        LOGGER.info("connected with zookeeper");
                    } else if (state == ConnectionState.RECONNECTED) {
                        LOGGER.info("reconnected with zookeeper");
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 初始化本地缓存
     * @param watchRootPath
     * @throws Exception
     */
    private void initLocalCache(String watchRootPath) throws Exception {
        cache = new TreeCache(client, watchRootPath);
        TreeCacheListener listener = (client1, event) ->{
            LOGGER.info("event:" + event.getType() +
                    " |path:" + (null != event.getData() ? event.getData().getPath() : null));

            if(event.getData()!=null && event.getData().getData()!=null){
                LOGGER.info("发生变化的节点内容为：" + new String(event.getData().getData()));
            }

            // client1.getData().
        };
        cache.getListenable().addListener(listener);
        cache.start();
    }

    public static CuratorFramework getClient (){
        return client ;
    }

    public static TreeCache getCache (){
        return cache ;
    }
    public static void closeClient (){
        if (client != null){
            client.close();
        }
    }
}
