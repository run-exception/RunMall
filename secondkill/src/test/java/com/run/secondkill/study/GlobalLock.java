package com.run.secondkill.study;

import com.run.secondkill.BaseTestBean;
import com.run.secondkill.util.RedisUtil;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 分布式锁
 */
public class GlobalLock extends BaseTestBean {

    @Autowired
    private RedisUtil redisUtil;

    private ExecutorService executorService;

    private CountDownLatch countDownLatch;

    @PostConstruct
    public void init(){
        executorService = Executors.newFixedThreadPool(4);
        countDownLatch = new CountDownLatch(4);
    }

    private static final Logger logger = LoggerFactory.getLogger(GlobalLock.class);

    @Test
    public void test() throws InterruptedException {
        final String lockKey = "lock";

        Thread t1 = new Thread(()->{
            String v = "t1-lock-value";
            try {
                int count = 0;
                while (!redisUtil.setNX(lockKey,v,10)){
                    Thread.sleep(10);
                     if (count++ > 100){
                         logger.info("t1拿锁失败");
                         return;
                     }
                }
                logger.info("t1拿锁成功");
                Thread.sleep(2000);
                logger.info("t1任务结束");
            }catch (Exception e){
                e.printStackTrace();
            }finally {
                if (redisUtil.casRemove(lockKey,v)){
                    logger.info("t1解锁成功");
                }else {
                    logger.info("t1解锁失败");
                }
            }
        });

        Thread t2 = new Thread(()->{
            String v = "t2-lock-value";
            try {
                int count = 0;
                while (!redisUtil.setNX(lockKey,v,10)){
                    Thread.sleep(10);
                    if (count++ > 100){
                        logger.info("t2拿锁失败");
                        return;
                    }
                }
                logger.info("t2拿锁成功");
                Thread.sleep(2000);
                logger.info("t2任务结束");
            }catch (Exception e){
                e.printStackTrace();
            }finally {
                if (redisUtil.casRemove(lockKey,v)){
                    logger.info("t2解锁成功");
                }else {
                    logger.info("t2解锁失败");
                }
            }
        });
        t1.start();
        t2.start();

        //主线程一直等待
        countDownLatch.await();


    }

    /**
     * t1拿锁后任务时间超过了锁超时的时间，导致t1任务还没结束锁就被t2拿到了
     * @throws Exception
     */
    @Test
    public void test2() throws Exception{
        final String lockKey = "lock";

        Thread t1 = new Thread(()->{
            String v = "t1-lock-value";
            try {
                int count = 0;
                while (!redisUtil.setNX(lockKey,v,1)){
                    Thread.sleep(10);
                    if (count++ > 200){
                        logger.info("t1拿锁失败");
                        return;
                    }
                }
                logger.info("t1拿锁成功");
                Thread.sleep(2000);
                logger.info("t1任务结束");
            }catch (Exception e){
                e.printStackTrace();
            }finally {
                if (redisUtil.casRemove(lockKey,v)){
                    logger.info("t1解锁成功");
                }else {
                    logger.info("t1解锁失败");
                }
            }
        });

        Thread t2 = new Thread(()->{
            String v = "t2-lock-value";
            try {
                int count = 0;
                while (!redisUtil.setNX(lockKey,v,1)){
                    Thread.sleep(10);
                    if (count++ > 200){
                        logger.info("t2拿锁失败");
                        return;
                    }
                }
                logger.info("t2拿锁成功");
                Thread.sleep(2000);
                logger.info("t2任务结束");
            }catch (Exception e){
                e.printStackTrace();
            }finally {
                if (redisUtil.casRemove(lockKey,v)){
                    logger.info("t2解锁成功");
                }else {
                    logger.info("t2解锁失败");
                }
            }
        });
        t1.start();
        t2.start();

        //主线程一直等待
        countDownLatch.await();
    }

    /**
     * t1 拿到1秒的锁，任务为 2秒
     * t1 开始后，由t1启动守护线程，每隔0.8秒为t1续命1秒
     * t1 任务结束后解锁
     * t2 连续20秒尝试获取锁，此时获取到锁
     * 守护线程cas失败退出线程
     * t2 结束后解锁
     * @throws Exception
     */
    @Test
    public void test3() throws Exception{
        final String lockKey = "lock";
        Thread t1Dem = new Thread(new Demo(redisUtil,lockKey,"t1-lock-value",1));
        t1Dem.setDaemon(true);


        Thread t1 = new Thread(()->{
            String v = "t1-lock-value";
            try {
                int count = 0;
                while (!redisUtil.setNX(lockKey,v,1)){
                    Thread.sleep(10);
                    if (count++ > 200){
                        logger.info("t1拿锁失败");
                        return;
                    }
                }
                logger.info("t1拿锁成功");
                t1Dem.start();
                Thread.sleep(2000);
                logger.info("t1任务结束");
            }catch (Exception e){
                e.printStackTrace();
            }finally {
                if (redisUtil.casRemove(lockKey,v)){
                    logger.info("t1解锁成功");
                }else {
                    logger.info("t1解锁失败");
                }
            }
        });

        Thread t2 = new Thread(()->{
            String v = "t2-lock-value";
            try {
                int count = 0;
                while (!redisUtil.setNX(lockKey,v,3)){
                    Thread.sleep(10);
                    if (count++ > 2000){
                        logger.info("t2拿锁失败");
                        return;
                    }
                }
                logger.info("t2拿锁成功");
                Thread.sleep(2000);
                logger.info("t2任务结束");
            }catch (Exception e){
                e.printStackTrace();
            }finally {
                if (redisUtil.casRemove(lockKey,v)){
                    logger.info("t2解锁成功");
                }else {
                    logger.info("t2解锁失败");
                }
            }
        });


        t1.start();
        Thread.sleep(10);
        t2.start();

        //主线程一直等待
        countDownLatch.await();
    }

    @Test
    public void testScript(){
        redisUtil.set("k1","v1",10);
        redisUtil.casExpireLock("k1","v1",20);
    }

}

class Demo implements Runnable{
    private RedisUtil redisUtil;
    private String key;
    private String value;
    private long second;

    public Demo(RedisUtil redisUtil, String key, String value, long second) {
        this.redisUtil = redisUtil;
        this.key = key;
        this.value = value;
        this.second = second;
    }

    @Override
    public void run() {
        try {
            Thread.sleep(800);
            System.out.println("续命苏醒，准备干活");
            while (redisUtil.casExpireLock(key,value,second)){
                System.out.println("续命成功");
                Thread.sleep(800);
            }

        }catch (Exception e){
            e.printStackTrace();
        }finally {
            System.out.println("续命线程销毁");
        }
    }
}
