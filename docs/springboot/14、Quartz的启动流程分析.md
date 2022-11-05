# Quartz 的启动流程分析

>昨天写了篇[SpringBoot整合Quartz](https://juejin.cn/post/7131019144463564814)的文章，然后也正巧不知道写什么了，就带着好奇写了这篇文章，明天的话再谈一谈执行流程。

## 一、简要

### 1.1、quartz 的核心元素

1.  **Scheduler**为调度器负责整个定时系统的调度，内部通过线程池进行调度，下文阐述。

1.  **Trigger**为触发器记录着调度任务的时间规则，主要有四种类型：SimpleTrigger、CronTrigger、DataIntervalTrigger、NthIncludedTrigger，在项目中常用的为：SimpleTrigger和CronTrigger。

1.  **JobDetail**为定时任务的信息载体，可以记录Job的名字、组及任务执行的具体类和任务执行所需要的参数

1.  Job为任务的真正执行体，承载着具体的业务逻辑。

    （像我们昨天的小案例中，简单定时任务就是实现了 job 接口）

大致流程就是：

-   先由`SchedulerFactory`创建`Scheduler`调度器
-   再由`Scheduler`调度器去调取即将执行的`Trigger`，
-   执行时获取到对于的`JobDetail`信息，
-   最后找到对应的`Job`类执行业务逻辑。

### 1.2、quartz中的线程

-   执行线程主要由一个线程池维护，在需要执行定时的时候使用，

-   这里使用的线程池是`org.quartz.simpl.SimpleThreadPool`，默认线程数为 10

    （这些在程序启动过程中的打印信息处就能看到）


![image.png](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/5301e3e726794b0fb9d8b9e22995def9~tplv-k3u1fbpfcp-watermark.image?)

<!---->

-   调度线程主要分为`Regular Scheduler Thread`和 `Misfire Scheduler Thread`；
-   其中**Regular Thread 轮询Trigger**，如果有将要触发的Trigger，则从任务线程池中获取一个空闲线程，然后执行与改Trigger关联的job；
-   `Misfire Thraed`则是扫描所有的trigger，查看是否有错失的，如果有的话，根据一定的策略进行处理。

## 二、quartz 启动流程

通常是使用 `StdSchedulerFactory` 创建 `Scheduler`调度器。

在 `SchedulerFactoryBean`配置类中配了相关的配置及配置文件参数，所以会读取配置文件参数，初始化各个组件。

启动流程的大致步骤如下：

1.  先读取配置文件

<!---->

2.  初始化 SchedulerFactoryBean

<!---->

3.  初始化SchedulerFactory

<!---->

4.  实例化执行线程池（TheadPool）

<!---->

5.  实例化数据存储

<!---->

6.  初始化 `QuartzScheduler`(为Scheduler的简单实现，包括调度作业、注册JobListener实例等方法。)

<!---->

7.  new一个`QuartzSchedulerThread`调度线程（负责执行在QuartzScheduler中注册的触发触发器的线程。），并开始运行

<!---->

8.  调度开始，注册监听器，注册Job和Trigger

<!---->

9.  `SchedulerFactoryBean`初始化完成后执行start()方法

<!---->

10. 创建ClusterManager线程并启动线程

<!---->

11. 创建MisfireHandler线程并启动线程

<!---->

12. 置`QuartzSchedulerThread`的paused=false，调度线程真正开始调度，开始执行run方法

***

### 2.1、读取配置文件

这里的读取，没有去debug，配置文件的位置是在

`org.quartz`的包下，文件名为`quartz.properties`

其内容为：

![image-20220813222519096](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/91529476a7b2436489405bf8bb42661a~tplv-k3u1fbpfcp-zoom-1.image)

### 2.2、初始化SchedulerFactoryBean

当服务器启动时，Spring就加载相关的bean。

SchedulerFactoryBean实现了InitializingBean接口，因此在初始化bean的时候，会执行afterPropertiesSet方法，

![image-20220813201023574](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/8a668df3da534087b50d64122639667c~tplv-k3u1fbpfcp-zoom-1.image)

该方法将会调用SchedulerFactory，通常是调用StdSchedulerFactory 来创建Scheduler调度器

### 2.3、初始化SchedulerFactory

这一步是在 `SchedulerFactoryBean`的`prepareSchedulerFactory`方法中进行初始化，在上一步也已经看到~

![image-20220813201254004](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/3ebde5be80624b6aabf962e7ddeb5741~tplv-k3u1fbpfcp-zoom-1.image)
我们接着往下Debug

![image-20220813201512934](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/4fcbf6f89d344f8cb580713209eafc12~tplv-k3u1fbpfcp-zoom-1.image)

这一大段都是在获取配置文件信息，我们直接将断点放到方法的末尾，看一下里面的值，和接下来要debug的方法

![image-20220813201907132](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/43dd0ee24e74434a8f64d50acf47f27b~tplv-k3u1fbpfcp-zoom-1.image)

![image-20220813202205912](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/ce43d0cdcc5d4546a545eb36b9c8a16a~tplv-k3u1fbpfcp-zoom-1.image)

到这之后，我们会回到

`this.scheduler = prepareScheduler(prepareSchedulerFactory());`这一步，拿到

`prepareSchedulerFactory()`返回的，接着往下走。

![image-20220813202832119](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/65e19fed5a1042579399fed2b8bb120c~tplv-k3u1fbpfcp-zoom-1.image)

上面的if就不看了，直接看create方法做了什么

![image-20220813202913061](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/ad02d3dbed75401a9ff607fcae03dc8a~tplv-k3u1fbpfcp-zoom-1.image)

这里的`this.schedulerName` 是 `quartzScheduler`

![image-20220813203920836](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/12a19690fb6c47aaa30e9170df042a83~tplv-k3u1fbpfcp-zoom-1.image)

这里的`schedulerFactory.getScheduler()`是值得我们注意的，点进去看，我们看看`StdSchedulerFactory`的`getScheduler()`方法做了什么呢？

![image-20220813205120681](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/d0a6b1da2621433e962d343046442f9a~tplv-k3u1fbpfcp-zoom-1.image)

引来袭来的就是quartz中的重要组件，而且都是null，这说明我们要到下一步啦

![image-20220813205347014](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/060372c7f8ee46cab97bd33ddb118023~tplv-k3u1fbpfcp-zoom-1.image)

### 2.4、实例化执行线程池（TheadPool）

![image-20220813205927945](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/504389c23366426cb5be08a0e60ad192~tplv-k3u1fbpfcp-zoom-1.image)

#### 2.5、实例化数据存储

![image-20220813205954227](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/c1c0ba9a80d84027be2e9e20058e661d~tplv-k3u1fbpfcp-zoom-1.image)

### 2.6、初始化QuartzScheduler

`QuartzScheduler`为Scheduler的简单实现，包括调度作业、注册JobListener实例等方法。

![image-20220813210414784](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/36e726bac835448296c62212790e1250~tplv-k3u1fbpfcp-zoom-1.image)
![image-20220813210603648](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/594fad05f2e34449a8d7e4a76e586a47~tplv-k3u1fbpfcp-zoom-1.image)

### 2.7、new一个`QuartzSchedulerThread`调度线程

`QuartzSchedulerThread`负责执行在QuartzScheduler中注册的触发触发器的线程，并开始运行

![image-20220813211616049](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/7291b8dd9cc045a5a5deff51f15e126f~tplv-k3u1fbpfcp-zoom-1.image)

走到就不在继续逗留了，我们直接回到方法调用处去。

### 2.8、注册监听器，注册Job和Trigger

![image-20220813210940705](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/f07bb1a99a064058b087239a2c03a75f~tplv-k3u1fbpfcp-zoom-1.image)

里面不再点进去看如何注册啦，想看的xdm，继续去Debug就可以了~

到这里`SchedulerFactoryBean`初始化就算是完成了。接下来就是执行 start（）方法

### 2.9、`SchedulerFactoryBean`执行start()方法

![image-20220813213112166](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/cb961a7b40e84b8588a39a684a7cfb17~tplv-k3u1fbpfcp-zoom-1.image)

这里的start方法是bean的生命周期中的，我们关注的不是这里，

而是方法内部的`startScheduler(this.scheduler,this.startupDelay)`方法

![image-20220813213258472](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/ac3e5a6c5c7142b3bfb16aa53733ed6e~tplv-k3u1fbpfcp-zoom-1.image)

![image-20220813213844522](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/b23de7c050d848aba00163ab46a326aa~tplv-k3u1fbpfcp-zoom-1.image)

这两个不接着往下看啦~，疲啦 ...

添上红字的这两个就不继续看，我们关注一下`this.resources.getJobStore().schedulerStarted();`，这也是下一步骤的开始~

### 2.10、创建Cluszaizaianager线程、创建MisfireHandler线程

![image-20220813214745842](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/b7d1e681c1314198ad46341281100054~tplv-k3u1fbpfcp-zoom-1.image)

我原本想关注一下`void recoverJobs()`方法

![image-20220813214928709](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/cfd644d99976425894b95c7d17c61a6c~tplv-k3u1fbpfcp-zoom-1.image)

原本我是还想点进`executeInNonManagedTXLock()`方法继续看一眼，直接看麻了，知道这里是恢复任务就行了，看起来很难受，里面更加庞大啦，主要是没有思绪~

### 2.11、QuartzSchedulerThread开始执行run方法

置`QuartzSchedulerThread`的paused=false，调度线程真正开始调度，开始执行run方法

这才是调度的真正开始，前面都只能算是准备工作吧。

什么时候触发，获取线程执行任务，怎么查询数据库等等，可以说是从这里开始的。不过，咱们今天跑路了，明天或下次再来~

![image-20220813221511748](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/d326b760d3664730bb50ec5fc5686fe3~tplv-k3u1fbpfcp-zoom-1.image)

## 后记

> 昨天写了一个Quartz 的小案例，就顺带Debug了一下，简单的走了流程，里面的代码太多值得分析的啦，**明天再来看看run方法，这里才是调度的重点**~

明天继续~

写于 2022 年 8 月 13 日晚，作者：[宁在春](https://juejin.cn/user/2859142558267559)