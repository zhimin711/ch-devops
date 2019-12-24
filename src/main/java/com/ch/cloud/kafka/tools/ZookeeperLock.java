package com.ch.cloud.kafka.tools;

import org.I0Itec.zkclient.IZkDataListener;
import org.I0Itec.zkclient.ZkClient;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;

/**
 * decs:
 *
 * @author 01370603
 * @date 2019/12/24
 */
@Component
public class ZookeeperLock implements ILock {

    private ZkClient zkClient;

    CountDownLatch countDownLatch = null;

    public String lockPath = "/lockPath";

    //获取锁
    public void getLock() {
        //1、连接zkClient 创建一个/lock的临时节点
        // 2、 如果节点创建成果，直接执行业务逻辑，如果节点创建失败，进行等待
        if (tryLock()) {
            System.out.println("#####成功获取锁######");
        } else {
            //进行等待
            waitLock();
        }
        //3、使用事件通知监听该节点是否被删除    ，如果是，重新进入获取锁的资源
    }

    @Value("${dubbo.registry.address}")
    private String zkUrl;

    public void init() throws IOException {
        zkClient = new ZkClient(zkUrl);
    }

    //创建失败 进行等待
    private void waitLock() {

        IZkDataListener iZkDataListener = new IZkDataListener() {

            // 节点被删除
            public void handleDataDeleted(String arg0) {
                if (countDownLatch != null) {
                    countDownLatch.countDown(); // 计数器为0的情况，await 后面的继续执行
                }

            }

            // 节点被修改
            public void handleDataChange(String arg0, Object arg1) {

            }
        };
        // 监听事件通知
        zkClient.subscribeDataChanges(lockPath, iZkDataListener);
        // 控制程序的等待
        if (zkClient.exists(lockPath)) {  //如果 检查出 已经被创建了 就new 然后进行等待
            countDownLatch = new CountDownLatch(1);
            try {
                countDownLatch.wait(); //等待时候 就不往下走了   当为0 时候 后面的继续执行
            } catch (Exception e) {
                // TODO: handle exception
            }
        }
        //后面代码继续执行
        //为了不影响程序的执行 建议删除该事件监听 监听完了就删除掉
        zkClient.unsubscribeDataChanges(lockPath, iZkDataListener);

    }

    private boolean tryLock() {
        try {
            init();
            zkClient.createEphemeral(lockPath);
            System.out.println("#########获取锁######");
            return true;
        } catch (Exception e) {
            // 如果失败 直接catch
            System.out.println("##########获取锁失败########");
            return false;
        }
    }

    //释放锁
    public void unLock() {
        //执行完毕 直接连接
        if (zkClient != null) {
            zkClient.close();
            System.out.println("######释放锁完毕######");
        }

    }

}
