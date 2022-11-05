package com.nzc.service;

import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.AsyncListenableTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.SchedulingTaskExecutor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ExecutorConfigurationSupport;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * @description:
 * @author: Ning Zaichun
 * @date: 2022年09月06日 0:02
 */
@Slf4j
@Component
@EnableAsync
@EnableScheduling
public class ScheduleService {


    @Autowired
    TaskExecutor taskExecutor;

    @Autowired
    RedissonClient redissonClient;

    private final String SCHEDULE_LOCK = "schedule:lock";

    /**
     * 1）cron = "0/5 * * * * ? " 每五秒执行一次
     * 按照我现在的 cron 表达式，最近的五次执行时间应当如下：
     * 2022-09-06 00:21:10
     * 2022-09-06 00:21:15
     * 2022-09-06 00:21:20
     * 2022-09-06 00:21:25
     * 2022-09-06 00:21:30
     * 2）实际业务中，每次的执行是需要时间的，因此使用线程睡眠来模拟业务的执行
     * 实际测试所得结果：
     * 2022-09-06 00:24:46.010  INFO 8340 --- [scheduling-1] com.nzc.service.ScheduleService          : 当前执行任务的线程号ID===>29
     * 2022-09-06 00:24:51.003  INFO 8340 --- [scheduling-1] com.nzc.service.ScheduleService          : 当前执行任务的线程号ID===>29
     * 2022-09-06 00:24:56.006  INFO 8340 --- [scheduling-1] com.nzc.service.ScheduleService          : 当前执行任务的线程号ID===>29
     * 2022-09-06 00:25:01.013  INFO 8340 --- [scheduling-1] com.nzc.service.ScheduleService          : 当前执行任务的线程号ID===>29
     * 2022-09-06 00:25:06.027  INFO 8340 --- [scheduling-1] com.nzc.service.ScheduleService          : 当前执行任务的线程号ID===>29
     * 3）问题
     * 1. 首先注意执行时间 它并不是按照我们预想的每五秒执行一次，业务代码执行超时，之后随之而来的定时任务也因此延时
     * 2. 注意执行线程，从头到尾都只有一个线程在执行任务，从而导致前面的任务。因为执行超时时，导致后面的任务延迟执行
     * 3. 因为只有一个线程在执行任务，箱单于整体就是一个串行化的任务，每执行一个定时，就再从任务队列中再取出一个执行
     * <p>
     * 4) 总结问题
     * 1. 线程过少
     * 2. 任务串行化执行
     * 5）为什么会产生这样的问题（每个@Enablexxxx注解，都会伴随着一个 xxxxAutoConfiguration 的自动装配的类，@EnableScheduling 也不例外）
     * 我们来看看 TaskSchedulingAutoConfiguration 自动装配类中，是如何的，到底为我们自动装配了些什么？
     * 1. 首先可以看到，它也是注入了一个线程池
     *
     * @Bean
     * @ConditionalOnBean(name = TaskManagementConfigUtils.SCHEDULED_ANNOTATION_PROCESSOR_BEAN_NAME)
     * @ConditionalOnMissingBean({ SchedulingConfigurer.class, TaskScheduler.class, ScheduledExecutorService.class })
     * public ThreadPoolTaskScheduler taskScheduler(TaskSchedulerBuilder builder) {
     * return builder.build();
     * }
     * 2. 我们接着点进去看，它是怎么配置这个线程池的
     * public ThreadPoolTaskScheduler build() {
     * return configure(new ThreadPoolTaskScheduler());
     * }
     * 3、在往下查看
     * @SuppressWarnings("serial") public class ThreadPoolTaskScheduler extends ExecutorConfigurationSupport
     * implements AsyncListenableTaskExecutor, SchedulingTaskExecutor, TaskScheduler {
     * <p>
     * private volatile int poolSize = 1;
     * @Nullable private ScheduledExecutorService scheduledExecutor;
     * <p>
     * public void setPoolSize(int poolSize) {
     * Assert.isTrue(poolSize > 0, "'poolSize' must be 1 or higher");
     * this.poolSize = poolSize;
     * if (this.scheduledExecutor instanceof ScheduledThreadPoolExecutor) {
     * ((ScheduledThreadPoolExecutor) this.scheduledExecutor).setCorePoolSize(poolSize);
     * }
     * 此处的 ScheduledExecutorService 实际上也是继承 jdk 中的ExecutorService ，所以究其根本还是 ExecutorService
     * 除此之外，这里默认的线程池中的配置参数，它的最大线程数和队列中最大任务数量，都为  Integer.MAX_VALUE;大家都知道这是非常不妥的。
     * 另外，这里的参数 poolSize=1，也表明如果使用的配置，那么初始化的线程池中的线程数即为1，也可以证明之前为什么一直是一个线程在执行任务了。
     * 6） 解决问题的方式：
     * 1. 修改配置文件 关于可以配置的所有参数，在 TaskSchedulingProperties 配置类中全部都可以查明
     * spring:
     * task:
     * scheduling:
     * pool:
     * size: 10
     * ... 以及其他相关的参数的配置
     * 修改后的测试结果：
     * 2022-09-06 00:56:01.018  INFO 10032 --- [   scheduling-1] com.nzc.service.ScheduleService          : 当前执行任务的线程号ID===>29
     * 2022-09-06 00:56:06.026  INFO 10032 --- [   scheduling-1] com.nzc.service.ScheduleService          : 当前执行任务的线程号ID===>29
     * 2022-09-06 00:56:11.012  INFO 10032 --- [   scheduling-2] com.nzc.service.ScheduleService          : 当前执行任务的线程号ID===>31
     * 2022-09-06 00:56:16.016  INFO 10032 --- [   scheduling-1] com.nzc.service.ScheduleService          : 当前执行任务的线程号ID===>29
     * 2022-09-06 00:56:21.017  INFO 10032 --- [   scheduling-3] com.nzc.service.ScheduleService          : 当前执行任务的线程号ID===>32
     * 2022-09-06 00:56:26.022  INFO 10032 --- [   scheduling-2] com.nzc.service.ScheduleService          : 当前执行任务的线程号ID===>31
     * 2022-09-06 00:56:31.030  INFO 10032 --- [   scheduling-4] com.nzc.service.ScheduleService          : 当前执行任务的线程号ID===>33
     * 可以看到时间的已经不再被延时，基本上都是每5秒执行一次，线程数也不再是孤单单的一个，出现了1,2,3,4等
     * 2、第二种方式：我们可以自己配置一个线程池，交给Spring管理，并且采用异步的方式来执行定时任务的方法
     * 2022-09-06 01:06:26.033  INFO 22128 --- [   scheduling-1] com.nzc.service.ScheduleService          : 当前执行任务的线程号ID===>31
     * 2022-09-06 01:06:31.016  INFO 22128 --- [   scheduling-2] com.nzc.service.ScheduleService          : 当前执行任务的线程号ID===>32
     * 2022-09-06 01:06:36.018  INFO 22128 --- [   scheduling-3] com.nzc.service.ScheduleService          : 当前执行任务的线程号ID===>33
     * 2022-09-06 01:06:41.012  INFO 22128 --- [   scheduling-4] com.nzc.service.ScheduleService          : 当前执行任务的线程号ID===>34
     * 2022-09-06 01:06:46.016  INFO 22128 --- [   scheduling-5] com.nzc.service.ScheduleService          : 当前执行任务的线程号ID===>35
     * 2022-09-06 01:06:51.021  INFO 22128 --- [   scheduling-6] com.nzc.service.ScheduleService          : 当前执行任务的线程号ID===>36
     * 2022-09-06 01:06:56.016  INFO 22128 --- [   scheduling-7] com.nzc.service.ScheduleService          : 当前执行任务的线程号ID===>37
     * 2022-09-06 01:07:01.003  INFO 22128 --- [   scheduling-8] com.nzc.service.ScheduleService          : 当前执行任务的线程号ID===>38
     * 2022-09-06 01:07:06.013  INFO 22128 --- [   scheduling-9] com.nzc.service.ScheduleService          : 当前执行任务的线程号ID===>39
     * 2022-09-06 01:07:11.018  INFO 22128 --- [  scheduling-10] com.nzc.service.ScheduleService          : 当前执行任务的线程号ID===>40
     * 2022-09-06 01:07:16.003  INFO 22128 --- [   scheduling-1] com.nzc.service.ScheduleService          : 当前执行任务的线程号ID===>31
     * <p>
     * 7） 到这里就没有任何问题了吗？
     * 如果只是针对定时任务量不大的单体项目而言，这样的解决方式，一定程度上来说是够用的，可以满足大部分的需求了。
     * 但如果在分布式调度中这样使用还可行吗？
     * 答案是不可行的。
     * 原因：
     * 1）、多台机器部署，则定时任务在同一时刻中会在多台机器上同时运行，
     * 这个时候，就牵扯到大家常说的，高并发操作，多个线程，操作同一个数据，那么就是可能存在问题的拉。
     * 2）、其次还需要保证定时任务中所执行的方法的幂等性，排除如果真的多台机器执行完同一段代码，也不会产生第二结果。
     * <p>
     * 8） 解决方式
     * 加分布式锁
     */
    @Async(value = "taskExecutor")
    @Scheduled(cron = "0/5 * * * * ? ")
    public void testSchedule() {
        //分布式锁
        RLock lock = redissonClient.getLock(SCHEDULE_LOCK);
            try {
                //加锁 10 为时间，加上时间 默认会去掉 redisson 的看门狗机制（即自动续机制）
            lock.lock(10, TimeUnit.SECONDS);
                Thread.sleep(10000);
                // 此处的测试应当要针对某一个操作执行，才能更好的看出区别
                log.info("当前执行任务的线程号ID===>{}", Thread.currentThread().getId());
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                // 一定要记得解锁~
            lock.unlock();
            }
    }
}
