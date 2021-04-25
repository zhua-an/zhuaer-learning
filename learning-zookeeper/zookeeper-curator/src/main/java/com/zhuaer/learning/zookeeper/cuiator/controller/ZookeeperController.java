package com.zhuaer.learning.zookeeper.cuiator.controller;

import com.zhuaer.learning.zookeeper.cuiator.configuration.ZookeeperConfig;
import com.zhuaer.learning.zookeeper.cuiator.service.ZookeeperService;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.atomic.AtomicValue;
import org.apache.curator.framework.recipes.atomic.DistributedAtomicLong;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.apache.curator.framework.recipes.locks.InterProcessReadWriteLock;
import org.apache.curator.framework.recipes.shared.SharedCount;
import org.apache.curator.framework.recipes.shared.VersionedValue;
import org.apache.curator.retry.RetryNTimes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.concurrent.*;
import java.util.function.Consumer;

/**
 * @ClassName ZookeeperController
 * @Description TODO
 * @Author zhua
 * @Date 2021/4/25 14:27
 * @Version 1.0
 */
@RequestMapping("/zk")
@RestController
public class ZookeeperController {

    @Autowired
    private ZookeeperService zookeeperService;

    @GetMapping("lockTest")
    public void lockTest() {
        final CuratorFramework client = ZookeeperConfig.getClient();
        //client.start();
        final InterProcessMutex lock = new InterProcessMutex(client, "/curator/lock");
        final CountDownLatch down = new CountDownLatch(1);
        for (int i = 0; i < 10; i++) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        down.await();
                        lock.acquire();
                    } catch (Exception e) {
                    }
                    SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss.SSS");
                    String orderNo = format.format(new Date());
                    System.out.println(">>>>>>生成的订单号是: " + orderNo);
                    try {
                        lock.release();
                    } catch (Exception e) {
                    }
                }
            }).start();
        }
        down.countDown();
        ZookeeperConfig.closeClient();
    }

    /**
     * 给节点添加读写锁
     * @param lockType
     * @param znode
     * @return
     */
    @RequestMapping(value = "/writeLock",method= RequestMethod.GET)
    public String readLock(@RequestParam Integer lockType,@RequestParam String znode){
        znode = "/" + znode;
        InterProcessReadWriteLock readWriteLock = zookeeperService.getReadWriteLock(znode);
        InterProcessMutex writeLock = readWriteLock.writeLock();
        InterProcessMutex readLock = readWriteLock.readLock();
        Runnable writeRunnable = ()->{
            try {
                System.out.println("------write lock-----------");
                writeLock.acquire();
                System.out.println("write acquire");
                Thread.sleep(10_000);
                System.out.println("write release");
                writeLock.release();

            } catch (Exception e) {
                e.printStackTrace();
            }
        };
        Runnable readRunnable = ()->{
            try {
                System.out.println("-------read lock----------");
                readLock.acquire();
                System.out.println("read acquire");
                Thread.sleep(20_000);
                System.out.println("read release");
                readLock.release();
            } catch (Exception e) {
                e.printStackTrace();
            }
        };

        if(lockType == 0 ){
            new Thread(writeRunnable).start();
        }else if(lockType == 1){
            new Thread(readRunnable).start();
        }
        return "success";
    }

    /**
     * 测试计算器
     * 并发越高耗时越长
     * 要自己实现获取锁失败重试
     * @param znode
     * @return
     */
    @RequestMapping(value="/counter",method=RequestMethod.POST)
    public String counter(@RequestBody  String znode){
        SharedCount baseCount = new SharedCount(ZookeeperConfig.getClient(), znode, 0);
        try {
            baseCount.start();
            //生成线程池
            ExecutorService executor = Executors.newCachedThreadPool();
            Consumer<SharedCount> consumer = (SharedCount count) -> {
                try {
                    List<Callable<Boolean>> callList = new ArrayList<>();
                    Callable<Boolean> call = () -> {
                        boolean result = false;
                        try {
                            Long time = System.currentTimeMillis();
                            while(!result){
                                VersionedValue<Integer> oldVersion = baseCount.getVersionedValue();
                                int newCnt = oldVersion.getValue() + 1;
                                result = baseCount.trySetCount(oldVersion, newCnt);
                                if(System.currentTimeMillis()-time>10_000||result){
                                    break;
                                }
                                TimeUnit.MILLISECONDS.sleep(new Random().nextInt(100)+1);
                            }
                        } catch (Exception e) {
                        }
                        return result;
                    };
                    //5个线程
                    for (int i = 0; i < 100; i++) {
                        callList.add(call);
                    }
                    List<Future<Boolean>> futures = executor.invokeAll(callList);
                } catch (Exception e) {

                }
            };
            //测试分布式int类型的计数器
            consumer.accept(baseCount);
            System.out.println("final cnt : " + baseCount.getCount());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "success："+baseCount.getCount();
    }

    /**
     * DistributedAtomicLong计数器可以自己设置重试的次数与间隔
     * 并发越高耗时越长
     * 要自己实现获取锁失败重试
     * @param znode
     * @return
     * @throws Exception
     */
    @RequestMapping(value="/counter2",method=RequestMethod.POST)
    public String distributedCount(@RequestBody  String znode) throws Exception {
        DistributedAtomicLong distributedAtomicLong = new DistributedAtomicLong(
                ZookeeperConfig.getClient(), znode, new RetryNTimes(10, 30));
        //生成线程池
        ExecutorService executor = Executors.newCachedThreadPool();
        Consumer<DistributedAtomicLong> consumer = (DistributedAtomicLong count) -> {
            try {
                List<Callable<Boolean>> callList = new ArrayList<>();
                Callable<Boolean> call = () -> {
                    boolean result = false;
                    try {
                        AtomicValue<Long> val = count.increment();
                        System.out.println("old cnt: "+val.preValue()+"   new cnt : "+ val.postValue()+"  result:"+val.succeeded());
                        result = val.succeeded();
                    } catch (Exception e) {
                    } finally {
                    }
                    return result;
                };
                //5个线程
                for (int i = 0; i < 500; i++) {
                    callList.add(call);
                }
                List<Future<Boolean>> futures = executor.invokeAll(callList);
            } catch (Exception e) {

            }
        };
        consumer.accept(distributedAtomicLong);
        return "success："+distributedAtomicLong.get().postValue();
    }

}
