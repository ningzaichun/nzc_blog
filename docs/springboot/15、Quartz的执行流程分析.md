# Quartz 的执行流程分析

>

## 前言

昨天很简单的阐述了一下Quartz的启动流程，但是里面最重要的`QuartzSchedulerThread`的`run`方法还木有讲，今天的话，就是来简单的看一看。

## 一、QuartzSchedulerThread 的 run方法大致阐述

先说一下run方法的执行时机：

> 当**Quartzscheduler执行start方法**时，方法体中有一句
>
> `schedThread.togglePause(false);`，接着就会调用`QuartzSchedulerThread`下的 `togglePause` 方法，将 `paused`置为 `false`，在此之后，`QuartzSchedulerThread`下的 run 方法开始真正运行

```java
/**通知主处理循环在下一个可能的点暂停 */
void togglePause(boolean pause) {
    synchronized (sigLock) {
        paused = pause;
        if (paused) {
            signalSchedulingChange(0);
        } else {
            sigLock.notifyAll();
        }
    }
}
```





```java
public void run() {
    int acquiresFailed = 0;

    // 这里就是判断调度器是否该停止，如果没有收到信号的话，这个调度器是一直处于循环之中的
    while (!halted.get()) {
        try {
            // 这里是检查我们是否应该暂停
            synchronized (sigLock) {
                // 我们在初始化的时候，paused 是置为 true的，
                // 因此在上文中，我们才说
                // 当 Quartzscheduler 执行 start方法时调用togglePause,
                // 将 paused 置为false，run 方法才开始运行
                // 也是因为此处的判断
                while (paused && !halted.get()) {
                    try {
                        sigLock.wait(1000L);
                    }catch (InterruptedException ignore) {}
                    acquiresFailed = 0;
                }
                if (halted.get()) { break;}
            }

            // 如果从作业存储中读取一直失败（例如数据库关闭或重新启动）
            // 就会等待一段时间~
            if (acquiresFailed > 1) {
                try {
                    //这里就是计算延迟时间
                    long delay = computeDelayForRepeatedErrors(qsRsrcs.getJobStore(), acquiresFailed);
                    Thread.sleep(delay);
                } catch (Exception ignore) {}
            }

            // 从线程池拿出空闲可利用的线程数量
            // 这里多谈一嘴 blockForAvailableThreads()方法
            // 它是一个阻塞式方法，直到至少有一个可用线程。
            int availThreadCount = qsRsrcs.getThreadPool().blockForAvailableThreads();
            if(availThreadCount > 0) { 
                List<OperableTrigger> triggers;

                long now = System.currentTimeMillis();

                // 清除信号调度变更
                clearSignaledSchedulingChange();
                try {
                    //如果可用线程数量足够那么就是30后再次扫描，
                    //acquireNextTriggers方法的三个参数的意思分别是：
                    //idleWaitTime ：为如果没有的再次扫描的时间，默认是
                    //   private static long DEFAULT_IDLE_WAIT_TIME = 30L * 1000L; 30秒
                    //Math.min(availThreadCount, qsRsrcs.getMaxBatchSize()) ：这里的意思就是一次最多能取几个出来
                    //batchTimeWindow ：默认是0，同样是一个时间范围，
                    //如果有两个任务只差一两秒，而执行线程数量满足及batchTimeWindow时间也满足的情况下就会两个都取出来
                    // 具体的方法的执行，后文再看~
                    triggers = qsRsrcs.getJobStore().acquireNextTriggers(
                        now + idleWaitTime, Math.min(availThreadCount, qsRsrcs.getMaxBatchSize()), qsRsrcs.getBatchTimeWindow());
                    acquiresFailed = 0;
                    if (log.isDebugEnabled()){
                        //...
                    }

                    //在获取到 triggers 触发器不为空后，
                    //trigger列表是以下次执行时间排序查出来的
                    if (triggers != null && !triggers.isEmpty()) {

                        now = System.currentTimeMillis();
                        //取出集合中最早执行的触发器
                        //获取它的下一个触发时间
                        long triggerTime = triggers.get(0).getNextFireTime().getTime();

                        long timeUntilTrigger = triggerTime - now;
                        // 判断距离执行时间是否大于2 毫秒
                        while(timeUntilTrigger > 2) {
                            synchronized (sigLock) {
                                if (halted.get()) {
                                    break;
                                }
                                //判断是不是距离触发事件最近的，
                                if (!isCandidateNewTimeEarlierWithinReason(triggerTime, false)) {
                                    try {
                                        // 没有的话，就进行阻塞，稍后进行执行
                                        now = System.currentTimeMillis();
                                        timeUntilTrigger = triggerTime - now;
                                        if(timeUntilTrigger >= 1)
                                            sigLock.wait(timeUntilTrigger);
                                    } catch (InterruptedException ignore) {}
                                }
                            }
                            if(releaseIfScheduleChangedSignificantly(triggers, triggerTime)) { break; }
                            now = System.currentTimeMillis();
                            timeUntilTrigger = triggerTime - now;
                        }

                        // this happens if releaseIfScheduleChangedSignificantly decided to release triggers
                        if(triggers.isEmpty()) continue;

                        // set triggers to 'executing'
                        List<TriggerFiredResult> bndles = new ArrayList<TriggerFiredResult>();

                        boolean goAhead = true;
                        synchronized(sigLock) {
                            goAhead = !halted.get();
                        }

                        if(goAhead) {
                            try {
                                //开始根据需要执行的trigger从数据库中获取相应的JobDetail  同时这一步也更新了 triggers 的状态，稍后会讲到~
                                List<TriggerFiredResult> res = qsRsrcs.getJobStore().triggersFired(triggers);
                                if(res != null)
                                    bndles = res;
                            } catch (SchedulerException se) {
                                qs.notifySchedulerListenersError(
                                    "An error occurred while firing triggers '"
                                    + triggers + "'", se);
                                for (int i = 0; i < triggers.size(); i++) {
                                    qsRsrcs.getJobStore().releaseAcquiredTrigger(triggers.get(i));
                                }
                                continue;
                            }

                        }
                        //将查询到的结果封装成为 TriggerFiredResult
                        for (int i = 0; i < bndles.size(); i++) {
                            TriggerFiredResult result =  bndles.get(i);
                            //TriggerFiredBundle用于将执行时数据从 JobStore 返回到QuartzSchedulerThread 。  
                            TriggerFiredBundle bndle =  result.getTriggerFiredBundle();
                            Exception exception = result.getException();
                            if (exception instanceof RuntimeException) {
                                getLog().error("RuntimeException while firing trigger " + triggers.get(i), exception);
                                qsRsrcs.getJobStore().releaseAcquiredTrigger(triggers.get(i));
                                continue;
                            }
                            if (bndle == null) {
                                qsRsrcs.getJobStore().releaseAcquiredTrigger(triggers.get(i));
                                continue;
                            }

                            JobRunShell shell = null;
                            try {
                                //把任务封装成JobRunShell线程任务，
                                //JobRunShell extends SchedulerListenerSupport implements Runnable  是实现了 Runnable 接口的
                                //然后放到线程池中跑动。
                                shell = qsRsrcs.getJobRunShellFactory().createJobRunShell(bndle);
                                shell.initialize(qs);
                            } catch (SchedulerException se) {
                                qsRsrcs.getJobStore().triggeredJobComplete(triggers.get(i), bndle.getJobDetail(), CompletedExecutionInstruction.SET_ALL_JOB_TRIGGERS_ERROR);
                                continue;
                            }

                            // 别看这里是个if判断
                            // 但是这里就是将 obshell 放进线程池执行的地方
                            // 利用的就是boolean runInThread(Runnable runnable); 方法
                            // 这个方法的作用就是 在下一个可用的Thread中执行给定Runnable 
                            if (qsRsrcs.getThreadPool().runInThread(shell) == false) {
                                getLog().error("ThreadPool.runInThread() return false!");
                                qsRsrcs.getJobStore().triggeredJobComplete(triggers.get(i), bndle.getJobDetail(), CompletedExecutionInstruction.SET_ALL_JOB_TRIGGERS_ERROR);
                            }

                        }

                        continue; // while (!halted)
                    }
                } else { // if(availThreadCount > 0)
                    // should never happen, if threadPool.blockForAvailableThreads() follows contract
                    continue; // while (!halted)
                }

                long now = System.currentTimeMillis();
                long waitTime = now + getRandomizedIdleWaitTime();
                long timeUntilContinue = waitTime - now;
                synchronized(sigLock) {
                   	// ....
                }
            } 	// ....
        } // while (!halted)
	// ....
    }
```



## 二、一些细节

### 2.1、先获取线程池中的可用线程数量

（若没有可用的会阻塞，直到有可用的）；

```java
 int availThreadCount = qsRsrcs.getThreadPool().blockForAvailableThreads();

```



### 2.2、获取 30m 内要执行的 trigger 

(即 acquireNextTriggers )

我们来看一看 `acquireNextTriggers `方法

首先说`acquireNextTriggers`具体实现是在 `JobStoreSupport`中，同时 `quartz` 与数据库关联的实现大都在`JobStoreSupport`中，当然更具体的SQL执行还是在`DriverDelegate`接口下的。

acquireNextTriggers 做了哪些事情呢？

![image-20220814171203377](C:\Users\ASUS\Desktop\nzc_blog\img\image-20220814171203377.png)

我们看看这两个方法：

首先看第一个 ` acquireNextTrigger(conn, noLaterThan, maxCount, timeWindow);`

主要就是获取下一个 30m内可执行的triggers的触发器，在里面`JobStoreSupport`从数据库取出`triggers`时是按照`nextFireTime`排序的

![image-20220814172913342](C:\Users\ASUS\Desktop\nzc_blog\img\image-20220814172913342.png)

![image-20220814172936649](C:\Users\ASUS\Desktop\nzc_blog\img\image-20220814172936649.png)

更具体的就需要大家点进方法去看啦~另外里面还包含triggers状态的变更，属于是更加细节化的东西。

第二个就是获取到触发的触发记录~

然后在执行`executeInNonManagedTXLock`时，是需要先获得锁，之后再在提交时释放锁的。

![image-20220814175127844](C:\Users\ASUS\Desktop\nzc_blog\img\image-20220814175127844.png)

----

待直到获取的trigger中最先执行的trigger在2ms内；

```java
if (triggers != null && !triggers.isEmpty()) {    
    now = System.currentTimeMillis();
    long triggerTime = triggers.get(0).getNextFireTime().getTime();
    long timeUntilTrigger = triggerTime - now;
    while(timeUntilTrigger > 2) {
        //...
    }
}
```



### 2.3、triggersFired(triggers)

`List<TriggerFiredResult> res = qsRsrcs.getJobStore().triggersFired(triggers);`

这一步看着只是获取了`List<TriggerFiredResult>`对象，实际上在`triggersFired(triggers)`方法中隐藏了很多东西~

![image-20220814174449717](C:\Users\ASUS\Desktop\nzc_blog\img\image-20220814174449717.png)

首先查询，确保触发器没有被删除、暂停或完成...，就更新`firedTrigger`的`status=STATE_EXECUTING`;代码的注释上还说，如果没有这些就会将状态该为`deleted`

![image-20220814173657395](C:\Users\ASUS\Desktop\nzc_blog\img\image-20220814173657395.png)

另外就是更新触发触发器：

1. 更新trigger下一次触发的时间；

2. 更新trigger的状态：

   ![image-20220814174224517](C:\Users\ASUS\Desktop\nzc_blog\img\image-20220814174224517.png)



如果下一次的执行时间为空，状态则改为`STATE_COMPLETE`

![image-20220814174239367](C:\Users\ASUS\Desktop\nzc_blog\img\image-20220814174239367.png)

在执行`executeInNonManagedTXLock`方法时，提交前先获得锁，`transOwner = getLockHandler().obtainLock(conn, lockName);`

![image-20220814174859252](C:\Users\ASUS\Desktop\nzc_blog\img\image-20220814174859252.png)

最后是释放锁：`commitConnection(conn);`

### 2.4、创建JobRunShell，放进线程池执行

针对每个要执行的trigger，创建JobRunShell，并放入线程池执行：

然后由execute:执行job

![image-20220814175205074](C:\Users\ASUS\Desktop\nzc_blog\img\image-20220814175205074.png)

更详细的看不下去啦~

跑路啦跑路啦~

## 后记

> 下次继续~，这些都是debug就能看到的东西，里面的很多东西有趣是有趣，但是怎么说勒，真的很麻人。
>
> **如果不是这外面四十度的天，我感觉我应该也在外面溜达啦**~

又是荒废的两天，定下来的任务一个也没完成~

写于 2022 年 8 月 14 日黄昏，作者：[宁在春](https://juejin.cn/user/2859142558267559)