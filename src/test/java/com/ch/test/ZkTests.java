package com.ch.test;

import org.I0Itec.zkclient.ZkClient;
import org.junit.Test;

/**
 * decs:
 *
 * @author zhimin.ma
 * @date 2020/4/30
 */
public class ZkTests {

    private static final String ZK_PATH_SPLIT_CHAR = "/";
    private static final String FILE_ROOT_REGISTRY = "registry";
    private static final String REGISTRY_TYPE = "zk";

    private static final String ROOT_PATH_WITHOUT_SUFFIX = ZK_PATH_SPLIT_CHAR + FILE_ROOT_REGISTRY + ZK_PATH_SPLIT_CHAR
            + REGISTRY_TYPE;
    @Test
    public void testZkClient(){

        ZkClient zkClient = new ZkClient("10.202.107.137:2181", 6000, 2000);
        if (!zkClient.exists(ROOT_PATH_WITHOUT_SUFFIX)) {
            zkClient.createPersistent(ROOT_PATH_WITHOUT_SUFFIX, true);
        }
        System.out.println(zkClient.exists(ROOT_PATH_WITHOUT_SUFFIX));
    }

}
