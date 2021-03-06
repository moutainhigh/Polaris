package com.polaris.container.dubbo.server;

import java.util.concurrent.CountDownLatch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.polaris.container.SpringContextServer;
import com.polaris.core.config.ConfClient;

public class DubboServer extends SpringContextServer{
    
    private static Logger logger = LoggerFactory.getLogger(DubboServer.class);
    
    /**
     * 私有构造方法
     */
    private DubboServer() {
    }
    
    /**
     * 获取单实例公共静态方法
     *
     * @return 单实例
     */
    public static DubboServer getInstance() {
        return Singletone.INSTANCE;
    }

    /**
     * 静态内部类实现单例
     */
    private static class Singletone {
        /**
         * 单实例
         */
        private static final DubboServer INSTANCE = new DubboServer();
    }
    
    /**
     * 启动服务器
     *
     * @throws Exception
     */
    public void start() throws Exception {

        //start
        super.start();
        
        //block
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    logger.info("Dubbo started on port(s) " + ConfClient.get("dubbo.protocol.port"));
                    new CountDownLatch(1).await();
                } catch (Exception e) {
                    logger.error("ERROR:",e);
                }
            }
        }, "Dubbo-block").start();
    }
    
}
