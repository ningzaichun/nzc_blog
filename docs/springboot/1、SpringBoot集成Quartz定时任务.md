#  SpringBoot 整合 Quartz 定时任务框架

> 在选择技术栈之前，一定要先明确一件事情，你真的需要用它吗？还有其他方式可以使用吗？
>
> 相比其他技术技术，优点在哪里呢？使用了之后的利与弊等等。

写这个主要是因为一直想写一下定时任务这个主题，这个算是写那篇文前期的铺垫和基础吧~

本文没有聊到 `Java`其他的实现定时任务的方法啥的~，只是对使用 Quartz 做了一个小实践

## 一、简单聊一聊 Quartz

Quartz 是一个完全由 Java 编写的开源作业调度框架，为在 Java 应用程序中进行作业调度提供了简单却强大的机制。

Quartz 其实就是通过一个调度线程不断的扫描数据库中的数据来获取到那些已经到点要触发的任务，然后调度执行它的。这个线程就是 QuartzSchedulerThread类。其run方法中就是quartz的调度逻辑。

另外，这是一个Demo，木有考虑并发、多任务执行等等状态的发生及处理情况，见谅。

### 1.1、Quartz 概念

Quartz 的几个核心概念

1.  Job

    表示一个工作，要执行的具体内容。此接口中只有一个方法

    ```
     void execute(JobExecutionContext context) 
    ```

1.  **JobDetail** 表示一个具体的可执行的调度程序，Job 是这个可执行程调度程序所要执行的内容，另外 JobDetail 还包含了这个任务调度的方案和策略。

1.  **Trigger** 代表一个调度参数的配置，什么时候去调。

1.  **Scheduler** 代表一个调度容器，一个调度容器中可以注册多个 JobDetail 和 Trigger。当 Trigger 与 JobDetail 组合，就可以被 Scheduler 容器调度了。

## 二、SpringBoot 使用 Quartz

### 2.1、基本步骤

基本步骤就那些，这篇也不是高大上讲原理和流程之类的，就是偏向实操，可能一些地方在代码中含有注释，就不再贴说明了~

基本：JDK 8、SpringBoot、MybatisPlus、Quartz

创建一个 SpringBoot 项目

导入相关依赖~

```
 <parent>
     <groupId>org.springframework.boot</groupId>
     <artifactId>spring-boot-starter-parent</artifactId>
     <version>2.5.2</version>
     <relativePath/>
 </parent>
 
 <properties>
     <java.version>1.8</java.version>
     <maven.compiler.source>8</maven.compiler.source>
     <maven.compiler.target>8</maven.compiler.target>
 </properties>
 
 <dependencies>
     <dependency>
         <groupId>org.springframework.boot</groupId>
         <artifactId>spring-boot-starter-web</artifactId>
     </dependency>
     <dependency>
         <groupId>org.springframework.boot</groupId>
         <artifactId>spring-boot-starter-quartz</artifactId>
     </dependency>
     <dependency>
         <groupId>org.projectlombok</groupId>
         <artifactId>lombok</artifactId>
     </dependency>
     <dependency>
         <groupId>mysql</groupId>
         <artifactId>mysql-connector-java</artifactId>
     </dependency>
     <dependency>
         <groupId>com.baomidou</groupId>
         <artifactId>mybatis-plus-boot-starter</artifactId>
         <version>3.4.1</version>
     </dependency>
 
     <dependency>
         <groupId>cn.hutool</groupId>
         <artifactId>hutool-all</artifactId>
         <version>5.1.4</version>
     </dependency>
     <dependency>
         <groupId>com.alibaba</groupId>
         <artifactId>fastjson</artifactId>
         <version>1.2.76</version>
     </dependency>
 </dependencies>
```

项目结构：


![image.png](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/93e2ad6542ab4becb092faa1f49ae208~tplv-k3u1fbpfcp-watermark.image?)

### 2.2、执行 Quartz 需要的SQL文件

找到 quartz 需要的 sql 文件，在数据库中执行，这也是Quartz持久化的基础~


![image.png](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/a47e808255d649e6bd88e8d244bf364c~tplv-k3u1fbpfcp-watermark.image?)
往下滑，找到你需要的sql文件即可。

![image.png](https://p9-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/c63da712ae7c46c7beae763d1de300d2~tplv-k3u1fbpfcp-watermark.image?)

执行完的结果：


![image.png](https://p1-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/2e2e7f4e5b264ff9a56cf54e40ba9f76~tplv-k3u1fbpfcp-watermark.image?)

***

在此基础上，我们再额外增加一张表，与我们可能有业务关联的信息整合，这啥啥允许为空，是方便我写测试~，并非正例


![image.png](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/c6464048db0c4ec0af6dcaf485ef2b73~tplv-k3u1fbpfcp-watermark.image?)

```
 DROP TABLE IF EXISTS `sys_quartz_job`;
 CREATE TABLE `sys_quartz_job`  (
   `id` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
   `create_by` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '创建人',
   `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
   `del_flag` int(1) NULL DEFAULT NULL COMMENT '删除状态',
   `update_by` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '修改人',
   `update_time` datetime NULL DEFAULT NULL COMMENT '修改时间',
   `job_class_name` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '任务类名',
   `cron_expression` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT 'cron表达式',
   `parameter` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '参数',
   `description` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '描述',
   `status` int(1) NULL DEFAULT NULL COMMENT '状态 0正常 -1停止',
   PRIMARY KEY (`id`) USING BTREE
 ) ENGINE = MyISAM CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = DYNAMIC;
 
```

### 2.3、Controller

我们直接从controller说起吧，从上往下开发。

其实一旦牵扯到表的操作，我们无疑就是crud四件事。

```
 /**
  * @Description: 定时任务在线管理
  * @author nzc
  */
 @RestController
 @RequestMapping("/quartzJob")
 @Slf4j
 public class QuartzJobController {
 
     @Autowired
     private IQuartzJobService quartzJobService;
     @Autowired
     private Scheduler scheduler;
 
 
     @RequestMapping(value = "/list", method = RequestMethod.GET)
     public Result<?> queryPageList(QuartzJob quartzJob, @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                                    @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize, HttpServletRequest req) {
         Page<QuartzJob> page = new Page<QuartzJob>(pageNo, pageSize);
         IPage<QuartzJob> pageList = quartzJobService.page(page);
         return Result.ok(pageList);
 
     }
 
     @RequestMapping(value = "/add", method = RequestMethod.POST)
     public Result<?> add(@RequestBody QuartzJob quartzJob) {
         List<QuartzJob> list = quartzJobService.findByJobClassName(quartzJob.getJobClassName());
         if (list != null && list.size() > 0) {
             return Result.error("该定时任务类名已存在");
         }
         quartzJobService.saveAndScheduleJob(quartzJob);
         return Result.ok("创建定时任务成功");
     }
 
     @RequestMapping(value = "/edit", method = RequestMethod.PUT)
     public Result<?> eidt(@RequestBody QuartzJob quartzJob) {
         try {
             quartzJobService.editAndScheduleJob(quartzJob);
         } catch (SchedulerException e) {
             log.error(e.getMessage(),e);
             return Result.error("更新定时任务失败!");
         }
         return Result.ok("更新定时任务成功!");
     }
 
     @RequestMapping(value = "/delete", method = RequestMethod.DELETE)
     public Result<?> delete(@RequestParam(name = "id", required = true) String id) {
         QuartzJob quartzJob = quartzJobService.getById(id);
         if (quartzJob == null) {
             return Result.error("未找到对应实体");
         }
         quartzJobService.deleteAndStopJob(quartzJob);
         return Result.ok("删除成功!");
 
     }
 
     @RequestMapping(value = "/deleteBatch", method = RequestMethod.DELETE)
     public Result<?> deleteBatch(@RequestParam(name = "ids", required = true) String ids) {
         if (ids == null || "".equals(ids.trim())) {
             return Result.error("参数不识别！");
         }
         for (String id : Arrays.asList(ids.split(","))) {
             QuartzJob job = quartzJobService.getById(id);
             quartzJobService.deleteAndStopJob(job);
         }
         return Result.ok("删除定时任务成功!");
     }
 
     /**
      * 暂停定时任务
      * @param jobClassName
      */
     @GetMapping(value = "/pause")
     public Result<Object> pauseJob(@RequestParam(name = "jobClassName", required = true) String jobClassName) {
         QuartzJob job = null;
         try {
             job = quartzJobService.getOne(new LambdaQueryWrapper<QuartzJob>().eq(QuartzJob::getJobClassName, jobClassName));
             if (job == null) {
                 return Result.error("定时任务不存在！");
             }
             scheduler.pauseJob(JobKey.jobKey(jobClassName.trim()));
         } catch (SchedulerException e) {
             throw new MyException("暂停定时任务失败");
         }
         job.setStatus(CommonConstant.STATUS_DISABLE);
         quartzJobService.updateById(job);
         return Result.ok("暂停定时任务成功");
     }
 
     /**
      * 恢复定时任务
      * @param jobClassName
      */
     @GetMapping(value = "/resume")
     public Result<Object> resumeJob(@RequestParam(name = "jobClassName", required = true) String jobClassName) {
         QuartzJob job = quartzJobService.getOne(new LambdaQueryWrapper<QuartzJob>().eq(QuartzJob::getJobClassName, jobClassName));
         if (job == null) {
             return Result.error("定时任务不存在！");
         }
         quartzJobService.resumeJob(job);
         //scheduler.resumeJob(JobKey.jobKey(job.getJobClassName().trim()));
         return Result.ok("恢复定时任务成功");
     }
 
     /** 通过id查询*/
     @RequestMapping(value = "/queryById", method = RequestMethod.GET)
     public Result<?> queryById(@RequestParam(name = "id", required = true) String id) {
         QuartzJob quartzJob = quartzJobService.getById(id);
         return Result.ok(quartzJob);
     }
 }
 
```

### 2.4、Service 划重点

```
 public interface IQuartzJobService extends IService<QuartzJob> {
 
     List<QuartzJob> findByJobClassName(String jobClassName);
 
     boolean saveAndScheduleJob(QuartzJob quartzJob);
 
     boolean editAndScheduleJob(QuartzJob quartzJob) throws SchedulerException;
 
     boolean deleteAndStopJob(QuartzJob quartzJob);
 
     boolean resumeJob(QuartzJob quartzJob);
 }
 
```

其中最主要的实现都是在这里：

```
 @Slf4j
 @Service
 public class QuartzJobServiceImpl extends ServiceImpl<QuartzJobMapper, QuartzJob> implements IQuartzJobService {
 
     @Autowired
     private QuartzJobMapper quartzJobMapper;
 
     @Autowired
     private Scheduler scheduler;
 
     @Override
     public List<QuartzJob> findByJobClassName(String jobClassName) {
         return quartzJobMapper.findByJobClassName(jobClassName);
     }
 
     /**保存&启动定时任务*/
     @Override
     public boolean saveAndScheduleJob(QuartzJob quartzJob) {
         if (CommonConstant.STATUS_NORMAL.equals(quartzJob.getStatus())) {
             // 定时器添加
             this.schedulerAdd(quartzJob.getJobClassName().trim(), quartzJob.getCronExpression().trim(), quartzJob.getParameter());
         }
         // DB设置修改
         return this.save(quartzJob);
     }
 
     /**恢复定时任务 */
     @Override
     public boolean resumeJob(QuartzJob quartzJob) {
         schedulerDelete(quartzJob.getJobClassName().trim());
         schedulerAdd(quartzJob.getJobClassName().trim(), quartzJob.getCronExpression().trim(), quartzJob.getParameter());
         quartzJob.setStatus(CommonConstant.STATUS_NORMAL);
         return this.updateById(quartzJob);
     }
 
     /**编辑&启停定时任务 @throws SchedulerException */
     @Override
     public boolean editAndScheduleJob(QuartzJob quartzJob) throws SchedulerException {
         if (CommonConstant.STATUS_NORMAL.equals(quartzJob.getStatus())) {
             schedulerDelete(quartzJob.getJobClassName().trim());
             schedulerAdd(quartzJob.getJobClassName().trim(), quartzJob.getCronExpression().trim(), quartzJob.getParameter());
         }else{
             scheduler.pauseJob(JobKey.jobKey(quartzJob.getJobClassName().trim()));
         }
         return this.updateById(quartzJob);
     }
 
     /**删除&停止删除定时任务*/
     @Override
     public boolean deleteAndStopJob(QuartzJob job) {
         schedulerDelete(job.getJobClassName().trim());
         return this.removeById(job.getId());
     }
 
     /** 添加定时任务*/
     private void schedulerAdd(String jobClassName, String cronExpression, String parameter) {
         try {
             // 启动调度器
             scheduler.start();
 
             // 构建job信息
             JobDetail jobDetail = JobBuilder.newJob(getClass(jobClassName).getClass()).withIdentity(jobClassName).usingJobData("parameter", parameter).build();
 
             // 表达式调度构建器(即任务执行的时间)
             CronScheduleBuilder scheduleBuilder = CronScheduleBuilder.cronSchedule(cronExpression);
 
             // 按新的cronExpression表达式构建一个新的trigger
             CronTrigger trigger = TriggerBuilder.newTrigger().withIdentity(jobClassName).withSchedule(scheduleBuilder).build();
 
             scheduler.scheduleJob(jobDetail, trigger);
         } catch (SchedulerException e) {
             throw new MyException("创建定时任务失败", e);
         } catch (RuntimeException e) {
             throw new MyException(e.getMessage(), e);
         }catch (Exception e) {
             throw new MyException("后台找不到该类名：" + jobClassName, e);
         }
     }
 
     /** 删除定时任务*/
     private void schedulerDelete(String jobClassName) {
         try {
             /*使用给定的键暂停Trigger 。*/
             scheduler.pauseTrigger(TriggerKey.triggerKey(jobClassName));
             /*从调度程序中删除指示的Trigger */
             scheduler.unscheduleJob(TriggerKey.triggerKey(jobClassName));
             /*从 Scheduler 中删除已识别的Job - 以及任何关联的Trigger */
             scheduler.deleteJob(JobKey.jobKey(jobClassName));
         } catch (Exception e) {
             log.error(e.getMessage(), e);
             throw new MyException("删除定时任务失败");
         }
     }
     
     private static Job getClass(String classname) throws Exception {
         Class<?> class1 = Class.forName(classname);
         return (Job) class1.newInstance();
     }
 }
 
```

```
 @Mapper
 public interface QuartzJobMapper extends BaseMapper<QuartzJob> {
     @Select("select * from  sys_quartz_job  where job_class_name = #{jobClassName}")
     public List<QuartzJob> findByJobClassName(@Param("jobClassName") String jobClassName);
 }
```

### 2.5、实体类

```
 @Data
 @TableName("sys_quartz_job")
 public class QuartzJob implements Serializable {
 
     @TableId(type = IdType.ID_WORKER_STR)
     private String id;
 
     private String createBy;
 
     private String updateBy;
 
     /**任务类名*/
     private String jobClassName;
     
     /** cron表达式 */
     private String cronExpression;
     
     /**  参数*/
     private String parameter;
 
     private String description;
     /** 状态 0正常 -1停止*/
     private Integer status;
     
     @TableLogic
     private Integer delFlag;
 
     @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
     @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
     @TableField(fill = FieldFill.INSERT)
     private Date createTime;
 
     @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
     @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
     @TableField(fill = FieldFill.INSERT_UPDATE)
     private Date updateTime;
 }
```

另外在这里顺带补充一下，本项目在yml中配置quartz，如下：

```
spring:
  ## quartz定时任务,采用数据库方式
  quartz:
    job-store-type: jdbc
```

其他很杂的一些MybatisPlus 相关和一些公共类，从仓库中拿一下就好[github源码](https://github.com/ningzaichun/springboot-quartz)

### 2.6、简单的 Job 案例

如果调度器要执行任务，首先得要有一个任务相关滴类。

写了两个平常的案例，一个是不带参数的，一个是带参数的。

```
/**
 * 不带参的简单定时任务
 * @Author nzc
 */
@Slf4j
public class SampleJob implements Job {

	@Override
	public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {

		log.info(String.format("Ning zaichun的 普通定时任务 SampleJob !  时间:" + new Date()));
	}
}
```

带参数的

```
 /**
  * 带参数的简单的定时任务
  * @Author nzc
  */
 @Slf4j
 public class SampleParamJob implements Job {
 
     /**
      * 若参数变量名修改 QuartzJobController中也需对应修改
      */
     private String parameter;
 
     public void setParameter(String parameter) {
         this.parameter = parameter;
     }
 
     @Override
     public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
         log.info(String.format("welcome %s! Jeecg-Boot 带参数定时任务 SampleParamJob !   时间:" + LocalDateTime.now(), this.parameter));
     }
 }
 
```

### 2.7、那么该如何使用呢？

启动项目，让我们拿起来postman来测试吧，康康该如何使用，数据表在使用的时候，又有怎么样的变化~

我们直接来测试添加定时任务的接口，先来个不用参数的吧

![image.png](https://p1-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/5193ab3b534a40f897734251209ab0b6~tplv-k3u1fbpfcp-watermark.image?)

```
 {
     "createBy": "nzc",
     "jobClassName": "com.nzc.quartz.job.SampleJob",
     "cronExpression": "0/10 * * * * ? ",
     "description": "每十秒执行一次",
     "status": "0"
 }
```

添加完之后就已经在执行了。
![image.png](https://p9-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/d162e287bc364787b298c43bc6fc105d~tplv-k3u1fbpfcp-watermark.image?)


此时我们将项目停止，重新启动~

![image.png](https://p6-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/934a74cf031044019ffdae89f8e11c05~tplv-k3u1fbpfcp-watermark.image?)




调度器也会主动去检测任务信息，如果有，就会开始执行。

我们再来测测带有参数的
![image.png](https://p6-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/9c59409c694546e1a0f305fe38b0d1c0~tplv-k3u1fbpfcp-watermark.image?)

也是可以成功的，并且也是按照我们设定的时间执行的



![image.png](https://p6-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/9b7a6220292c4c8383ba7347a6b46e49~tplv-k3u1fbpfcp-watermark.image?)

其他的接口也是同样如此，根据接口的设定，将参数传入即可

[源码](https://github.com/ningzaichun/springboot-quartz)

## 后记

> 本文不牵扯到过多的内容，就是一篇普通的入门的使用教程。
>
> 后续在更文的时候，再打算说一说它的流程。
>
> 我觉得内部的执行流程和机制才是有趣的，Debug的时候，你会发现很多很多的奥妙~
>
> 不过想要探索之前，还是需要学会如何使用才行~

明天继续~，晚安，各位

写于 2022 年 8 月 12 日晚，作者：[宁在春](https://juejin.cn/user/2859142558267559)