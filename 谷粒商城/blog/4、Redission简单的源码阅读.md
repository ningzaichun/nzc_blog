# Redisson 分析

上一篇文章：[用万字长文来讲讲本地锁至分布式锁的演进和Redis实现](https://juejin.cn/post/7156623634327404557)

在上一篇中挖了这个坑，今天就来把它填上哈~ 

昨天聊到了`Redisson`实现了锁的自动续期，但是就简单提了一嘴就完事了。

今天就来多说一点点里面的源码中的实现和使用。

文章大纲：

第一部分说了 `Redisson `简单使用

第二部分才是说`Redisson`**底层源码**如何实现分布式锁

1. 如何加锁
2. 如何实现锁自动续期，靠什么实现的？
3. 如何实现解锁

## 一、Redisson 简单使用

在 `SpringBoot` 中，因为自动装配的存在，使用某个封装好的轮子，就那么几步~

- 导包
- 编写配置
- 编写`xxxConfig`
- 准备开始使用它

### 1.1、导包

`Redisson` 也不例外

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-redis</artifactId>
</dependency>

<dependency>
    <groupId>org.redisson</groupId>
    <artifactId>redisson-spring-boot-starter</artifactId>
    <version>3.17.6</version>
</dependency>
```

### 1.2、配置

这里的配置就是`Redis`的配置

```yml
spring:
  redis:
    host: IP地址
    password: xxxx
```

### 1.3、编写配置类

那些什么最大连接数，连接超时等等就没配了，偷个懒~ 

```java
@Configuration
public class MyRedissonConfig {

    /**
     * 所有对Redisson的使用都是通过RedissonClient
     * @return
     * @throws IOException
     */
    @Bean(destroyMethod="shutdown")
    public RedissonClient redissonClient() throws IOException {
        //1、创建配置
        Config config = new Config();
        config.useSingleServer().setAddress("redis://4IP地址:6379").setPassword("xxxx");
//       config.setLockWatchdogTimeout();
        //2、根据Config创建出RedissonClient实例
        //Redis url should start with redis:// or rediss://
        RedissonClient redissonClient = Redisson.create(config);
        return redissonClient;
    }
}
```

注意：这里面的`setAddress()`中的地址必须以`redis://`开头~

另外我这里只是设置了这么几个属性，很多我没去写了而已，不是它不能设置哈。

![image-20221021221300948](C:\Users\ASUS\Desktop\nzc_blog\img\image-20221021221300948.png)

就比如修改锁自动需求的默认时间，就是

`config.setLockWatchdogTimeout();` 锁自动续期默认时间是30s，这是可以被修改的。

其他的还需靠大家自己探索啦~

### 1.4、简单上手

最直接的使用：

```java
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {主启动类.class})
public class RedisTest {

    @Autowired
    private RedissonClient redissonClient;

    @Test
    public void testRedisson() {
        // 获取锁实例
        RLock lock = redissonClient.getLock("redisson:lock");
        try {
            lock.lock();
            //执行需要加锁的业务~ 
        } finally {
            lock.unlock();
        }
    }
}
```

这个`lock()`方法，它是可以填写参数，也可以不填的，

```java
public void lock()   
public void lock(long leaseTime, TimeUnit unit) 
```

不填写参数，`Redisson`它帮助我们实现了锁的自动续期，

如果我们自己填写了锁的时间，`Redisson`则不会帮我们实现锁的自动续期。

这一点在后面分析源码的有详细的说明。

另外`Redisson`它作为分布式锁的实现，更好的方便`Java`开发者，它实现了`Java`中`JUC`包下的诸多接口，可以说使用起来完全没啥学习成本~

比如`JUC`下的读写锁，

```java
@Test
public void testRedissonReadAndWriteLock() {
    RReadWriteLock readWriteLock = redissonClient.getReadWriteLock("redisson:lock");= redissonClient.getReadWriteLock("redisson:lock");
    // 获取写锁
    RLock writeLock = readWriteLock.writeLock();
    // 获取读锁
    RLock readLock = readWriteLock.readLock();
    try {
        //上锁
        writeLock.lock();
        // 这种方式，使用我们自己定义的
        writeLock.lock(10,TimeUnit.SECONDS);
        //执行需要加锁的业务~
    } finally {
        writeLock.unlock();
    }
}
```

还有信号量`RSemaphore semaphore = redissonClient.getSemaphore("semaphore");`

更有其他的不少：

![image-20221021224337371](C:\Users\ASUS\Desktop\nzc_blog\img\image-20221021224337371.png)

详细的案例，其实在它的文档中都有说明的，有中文版本的，很简单的~

[官方文档](https://github.com/redisson/redisson/wiki/8.-%E5%88%86%E5%B8%83%E5%BC%8F%E9%94%81%E5%92%8C%E5%90%8C%E6%AD%A5%E5%99%A8)

这些该如何使用，其实就和用JUC下面的工具一样，懂了那一块的知识，就知道这里该如何使用啦，不懂的话，我说了也还是一样的哈哈~ 

让我偷个懒，可以去学一学JUC，蛮好玩的~

## 二、锁自动续期源码-看门狗机制分析 

在分析之前，我们先把上一篇中分布式锁演进过程中所产生的问题都一一抛出来，之后再针对性的看看 Redisson 它又是如何解决的。

### 2.1、分布式锁演进的问题

为了方便其余没有看过看一篇文章的朋友，我把分布式锁中存在的几个问题，递推式的抽离出来了。

我们都知道要正确使用分布式锁，一定要**注意原子性操作、死锁问题和被他人释放锁的问题和锁是否需要自动续期问题**。

1. **死锁问题**：说的是锁没有被动过期时间，即拿到了锁，但是在执行业务过程中，程序崩溃了，锁没有被正常释放，导致其他线程无法获取到锁，从而产生死锁问题。

   之前的解决方式：`set nx ex`命令

2. **锁被其他人释放问题**：第一条线程抢到锁，业务执行超时，第一条线程所持有的锁被**自动释放**；此时第二条线程拿到锁，准备执行业务，刚好第一条线程业务执行完成，照常执行了释放锁的过程，**导致第二条线程持有的锁被第一条线程所释放，锁被其他人释放**。

   之前的解决方式：给锁一个唯一值（UUID），每次解锁前进行判断

3. **原子性操作**：原子性的意思就是要么都成功，要么都失败。像我们获取锁，设定值和设定时间是两步操作，让他们变成原子性操作就是设定值和设定时间成为一体，一起成功或者一起失败。另外解锁操作的获取锁，判断锁是否为当前线程所拥有，也必须是一个原子性操作。

   之前的解决方式：利用`Redis`中的`lua`脚本实现。

4. **锁自动续期问题**：这个就是利用了我们今天的`Redisson`来实现的。

### 2.2、分析的开端

我们就以最简单的流程

```java
@Test
public void testRedisson() {
    RLock lock = redissonClient.getLock("redisson:lock");
    try {
        lock.lock();
        //执行需要加锁的业务~
    } finally {
        lock.unlock();
    }
}
```

来分析以下几个问题：

1. Redisson 是如何实现加锁的
2. Redisson 是如何实现锁自动续期的
3. Redisson 是如何解锁的

以及在这个过程中，去看看 `Redisson `它是如何解决我们之前出现的问题的。

顺着看下去~

```java
RLock lock = redissonClient.getLock("redisson:lock");
lock.lock();
```

看似加锁操作只调用了一个`lock()`方法，但实际上流程走的可多了~

不多说，直接在Idea中点击跳转到`RedissonLock`中的类中

![image-20221021232236227](C:\Users\ASUS\Desktop\nzc_blog\img\image-20221021232236227.png)

![image-20221021232757569](C:\Users\ASUS\Desktop\nzc_blog\img\image-20221021232757569.png)

补充说明：这下面还有一些实现自旋的操作，就是写了一个`while(true)`来实现等待获取锁的代码，没有继续往下分析了。中间还有一些判断锁是否可以被打断、订阅和退订等操作，没有去仔细研究啦，大家感兴趣可以再往下多看看。

---

接着往下走👇

![image-20221021232845385](C:\Users\ASUS\Desktop\nzc_blog\img\image-20221021232845385.png)

![image-20221021233535410](C:\Users\ASUS\Desktop\nzc_blog\img\image-20221021233535410.png)

### 2.3、Redisson 如何实现加锁-tryLockInnerAsync()

漏分析了一步，不是从`scheduleExpirationRenewal(threadId)`前进到下一步，这说的是锁续期问题~

忘记说加锁啦~ 加锁就是`tryLockInnerAsync()`方法

点进去看看~

![image-20221022010701365](C:\Users\ASUS\Desktop\nzc_blog\img\image-20221022010701365.png)

补充说明：

在这里可以看到几个方面：Redisson底层也是使用Lua脚本来实现的，这段 lua 脚本分为三个部分：

1. 第一部分：加锁

   首先用 exists 判断了 KEYS[1] （即 `redisson:lock`）是否存在。
   如果不存在，则进入第 5 行，使用 `hincrby` 命令创建一个新的哈希表，如果域`field`不存在，那么在执行命令前会被初始化为0，此命令的返回值就是执行`hincrby`命令后，哈希表`key`中域`field`的值，此时进行`increment`，也就是返回1，之后进入第6行，对KEY[1]设置过期时间，30000ms然后返回nil。

2. 第二部分：重入

   首先判断`KEY[1]`是否存在，因为`KEY[1]`是一个`hash`结构，所以13行意思是获取这个`KEYS[1]`中字段为`ARGV[2]`也就是`UUID：thredId`这个值是否存在。

   如果存在进入14行代码对其进行加1操作(锁重入)
   然后进入15行重新设置过期时间30s
   然后返回nil

3. 第三部分：返回：作用就是返回 KEY[1] 的剩余存活时间

此处的`getRawName()`方法就是我们获取到我们设定的锁名。如此处就是获取到`Redisson:lock`

` getLockName(threadId)`就是获取一个`UUID:threadId`的字符

串。

![image-20221022011424392](C:\Users\ASUS\Desktop\nzc_blog\img\image-20221022011424392.png)

至于说此处的`UUID`，是如何来的，我稍微浅看了一下，这是初始化的时候给每个连接管理器都传了一个UUID的类，更具体的使用，没去追啦。

咱们知道这是 Redisson 也是采取一样的方式，它的值也是存了一个UUID，这点同样也可以在连接工具上查看到的。

![image-20221022012535622](C:\Users\ASUS\Desktop\nzc_blog\img\image-20221022012535622.png)

看到这里就已经可以看出Redisson已经解决我们昨天出现的全部问题了。

首先是加锁操作的原子性是满足的了，因为 `Redisson `使用的Lua脚本将设置值和设置时间的操作变为一步；其次是之前的锁被其他人释放的问题，在这里`Redisson`也采用了唯一`key`值`UUID`来解决此问题。

那么剩下的就只有锁续期问题了，我们接着往下看👇

### 2.4 、Redisson 如何实现锁续期

从这个`scheduleExpirationRenewal(threadId);`方法开始继续往下探索~

![image-20221022013531966](C:\Users\ASUS\Desktop\nzc_blog\img\image-20221022013531966.png)

![image-20221021234512295](C:\Users\ASUS\Desktop\nzc_blog\img\image-20221021234512295.png)

![image-20221021235042546](C:\Users\ASUS\Desktop\nzc_blog\img\image-20221021235042546.png)

我们回过头来接着看 续期方法`renewExpiration()`

看它底层是如何实现的。

![image-20221022014232573](C:\Users\ASUS\Desktop\nzc_blog\img\image-20221022014232573.png)

可以从这段代码中很明显的看出，是启动了一个定时任务，该任务每 `internalLockLeaseTime/3ms` 后执行一次。而 `internalLockLeaseTime `默认为 30000。所以该任务每 10s 执行一次。

---

`renewExpirationAsync(long threadId) `方法，就是实现锁重新续期的lua脚本的执行

![image-20221022014642168](C:\Users\ASUS\Desktop\nzc_blog\img\image-20221022014642168.png)

上面的定时任务在不修改看门狗的默认时间时，就是每10s执行一次，意思就是每次在锁还剩下`20s`时，就会执行这段重新续期的代码，让锁重新续期到30s。

不知道说到这里，大伙有听懂吗~  

### 2.5、Redisson 如何实现解锁操作

其实看懂了`Redisson`是如何加锁的，其实看这解锁操作也是特别容易的。

不过我们这次要回到开始分析的地方去啦。

![image-20221022015402249](C:\Users\ASUS\Desktop\nzc_blog\img\image-20221022015402249.png)

![image-20221022015825854](C:\Users\ASUS\Desktop\nzc_blog\img\image-20221022015825854.png)

看的出来，解锁操作并不复杂，我们先去看看`unlockInnerAsync(threadId);`方法吧。

![image-20221022020123497](C:\Users\ASUS\Desktop\nzc_blog\img\image-20221022020123497.png)

其实看完加锁，再看这个其实都能理解啦吧

- 首先判断KEYS[1]是否存在
- 存在将值减1，如果counter还大于0，就重新设置过期时间30000ms，否则就删除操作
- `redis.call('publish', KEYS[2], ARGV[1]);`同时发布了一个事件，这个是干嘛的？是去通知正在等待获取锁其他的线程们，可以使用这把锁了。

![image-20221022020653066](C:\Users\ASUS\Desktop\nzc_blog\img\image-20221022020653066.png)



另外`cancelExpirationRenewal(threadId);`方法就是一些取消和删除操作。

![image-20221022020513662](C:\Users\ASUS\Desktop\nzc_blog\img\image-20221022020513662.png)

## 总结

`Redisson` 相比较我们自己的实现如何？

首先可以说的是，这个轮子考虑的比我们周到的多，在上篇说的各种问题都是解决了的。

在各种加锁或者解锁操作上都实现了原子性~

几个点：

1. 在使用 `Redisson` 获取锁的过程，你主动设定了锁的过期时间，``Redisson` 将不会开启看门狗机制。
2. `Redisson` 在 Redis 中保存的结构是一个 Hash的数据结构，`key` 的名称是我们的锁名称，如案例中使用的 `redisson:lock`，存储的字段名称为 `UUID：threadId`，值的话就是 `1`。
3. 结构如下图：![image-20221022130718606](C:\Users\ASUS\Desktop\nzc_blog\img\image-20221022130718606.png)
4. `Redisson`实现锁自动续期的底层是开启了一个线程，异步的执行定时任务，在锁还剩下`20s`，自动续期为`30s`，此定时任务是采用`Netty`框架中的**时间轮算法**实现。

今天就看到这里啦，周四挖的坑，周五熬夜终于写出来了。

## 后记

> 不知道这篇文章有没有帮助到你，希望看完的你，对于`Redisson`已经没有那么恐惧~，当然它其实也不难。

![yijiansanlian](C:\Users\ASUS\Desktop\nzc_blog\img\yijiansanlian.webp)

写于 2022 年 10 月 21 日晚，作者：[宁在春](https://juejin.cn/user/2859142558267559/posts)

今天是好累的一天啊~





