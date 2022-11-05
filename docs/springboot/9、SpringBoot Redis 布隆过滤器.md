# SpringBoot 中使用布隆过滤器

> 昨天写了一篇[Redis布隆过滤器相关的命令](https://juejin.cn/post/7135840044547309598)的文章，今天来说一说springboot中如何简单在代码中使用布隆过滤器吧。

目前市面上也有好几种实现方式，如果你需要高度定制化，可以完全从零实现，当然这不是一个简单的工程。

如果只是想快速开始的话，那么市面上现成的实现，无疑是最快的。

## 前言

今天说到的实现方式有以下几种：

- 引入 Guava 实现
- 引入 hutool 实现
- 引入 Redission 实现
- Guava 布隆过滤器结合 Redis （重点）

项目工程的搭建，就在这里先写明啦~

boot项目就是四步走~ 导包->写配置->编写配置类->使用

`补充说明`：我使用的 redis 是用docker下载的一个集成redis和布隆过滤器的镜像。安装方式：[Docker安装Redis布隆过滤器](https://juejin.cn/post/7135840044547309598)

如果你是在windows上安装的redis 是3.0版本的，是无法集成布隆过滤器。

如果是在liunx版本上的redis，需要再额外下载一个布隆过滤器的模块。需要自行百度啦~



---

我将要用到的所有jar都放在这里啦~

```xml
<parent>
    <artifactId>spring-boot-dependencies</artifactId>
    <groupId>org.springframework.boot</groupId>
    <version>2.5.2</version>
</parent>
<dependencies>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-redis</artifactId>
    </dependency>
    <!-- https://mvnrepository.com/artifact/org.redisson/redisson-spring-boot-starter -->
    <dependency>
        <groupId>org.redisson</groupId>
        <artifactId>redisson-spring-boot-starter</artifactId>
        <version>3.17.6</version>
    </dependency>

    <dependency>
        <groupId>com.google.guava</groupId>
        <artifactId>guava</artifactId>
        <version>30.0-jre</version>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-test</artifactId>
    </dependency>
    <dependency>
        <groupId>junit</groupId>
        <artifactId>junit</artifactId>
        <scope>test</scope>
    </dependency>
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
    </dependency>
    <dependency>
        <groupId>cn.hutool</groupId>
        <artifactId>hutool-all</artifactId>
        <version>5.7.22</version>
    </dependency>
</dependencies>

```

yml 配置文件：

```yml
server:
  port: 8081
spring:
  redis:
    port: 6379
    host: 192.xxx
```





## 一、Guava 实现布隆过滤器

这个方式非常快捷：

直接用一个Demo来说明吧

```java
    @Test
    public void test2() {
        // 预期插入数量
        long capacity = 10000L;
        // 错误比率
        double errorRate = 0.01;
        //创建BloomFilter对象，需要传入Funnel对象，预估的元素个数，错误率
        BloomFilter<Long> filter = BloomFilter.create(Funnels.longFunnel(), capacity, errorRate);
//        BloomFilter<String> filter = BloomFilter.create(Funnels.stringFunnel(Charset.forName("utf-8")), 10000, 0.0001);
        //put值进去
        for (long i = 0; i < capacity; i++) {
            filter.put(i);
        }
        // 统计误判次数
        int count = 0;
        // 我在数据范围之外的数据，测试相同量的数据，判断错误率是不是符合我们当时设定的错误率
        for (long i = capacity; i < capacity * 2; i++) {
            if (filter.mightContain(i)) {
                count++;
            }
        }
        System.out.println(count);
    }

```





当容量为1k，误判率为  0.01时

```bash
2022-08-26 23:50:01.028  INFO 14748 --- [           main] com.nzc.test.RedisBloomFilterTest        : 存入元素为==1000
误判个数为==>10
```

当容量为1w，误判率为  0.01时

```bash
2022-08-26 23:49:23.618  INFO 21796 --- [           main] com.nzc.test.RedisBloomFilterTest        : 存入元素为==10000
误判个数为==>87

```

当容量为100w，误判率为  0.01时

```java
2022-08-26 23:50:45.167  INFO 8964 --- [           main] com.nzc.test.RedisBloomFilterTest        : 存入元素为==1000000
误判个数为==>9946
```



`   BloomFilter<Long> filter = BloomFilter.create(Funnels.longFunnel(), capacity, errorRate);`  

create方法实际上调用的方法是：

```
public static <T> BloomFilter<T> create(
    Funnel<? super T> funnel, int expectedInsertions, double fpp) {
  return create(funnel, (long) expectedInsertions, fpp);
}
```

- funnel 用来对参数做转化,方便生成hash值
- expectedInsertions 预期插入的数据量大小
- fpp 误判率

里面具体的实现，相对我来说，数学能力有限，没法说清楚。希望大家多多包含。



## 二、Hutool 布隆过滤器

Hutool 工具中的布隆过滤器，内存占用太高了，并且功能相比于guava也弱了很多，个人不建议使用。

```java
@Test
public void test4(){
    int capacity = 100;
    // 错误比率
    double errorRate = 0.01;
    // 初始化
    BitMapBloomFilter filter = new BitMapBloomFilter(capacity);
    for (int i = 0; i < capacity; i++) {
        filter.add(String.valueOf(i));
    }

    log.info("存入元素为=={}",capacity);
    // 统计误判次数
    int count = 0;
    // 我在数据范围之外的数据，测试相同量的数据，判断错误率是不是符合我们当时设定的错误率
    for (int i = capacity; i < capacity * 2; i++) {
        if (filter.contains(String.valueOf(i))) {
            count++;
        }
    }
    log.info("误判元素为==={}",count);
}
```





## 三、Redission 布隆过滤器

redission的使用其实也很简单，官方也有非常好的教程。

引入jar，然后编写一个config类即可

```xml

<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-redis</artifactId>
</dependency>
<!-- https://mvnrepository.com/artifact/org.redisson/redisson-spring-boot-starter -->
<dependency>
    <groupId>org.redisson</groupId>
    <artifactId>redisson-spring-boot-starter</artifactId>
    <version>3.17.6</version>
</dependency>
```



出了注入 redissionclient，还注入了一些redis相关的东西，都是历史包裹~

```java
/**
 * @description:
 * @author: Yihui Wang
 * @date: 2022年08月26日 22:06
 */
@Configuration
@EnableCaching
public class RedisConfig {

    @Bean
    public RedissonClient redissonClient(){
        Config config = new Config();
        config.useSingleServer().setAddress("redis://47.113.227.254:6379");
        RedissonClient redissonClient = Redisson.create(config);
        return  redissonClient;
    }

    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        RedisCacheManager rcm=RedisCacheManager.create(connectionFactory);
        return rcm;
    }
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory) {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<String, Object>();
        redisTemplate.setConnectionFactory(factory);
 
        Jackson2JsonRedisSerializer jackson2JsonRedisSerializer = new
                Jackson2JsonRedisSerializer(Object.class);
        ObjectMapper om = new ObjectMapper();
        om.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        om.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
        jackson2JsonRedisSerializer.setObjectMapper(om);
        //序列化设置 ，这样计算是正常显示的数据，也能正常存储和获取
        redisTemplate.setKeySerializer(jackson2JsonRedisSerializer);
        redisTemplate.setValueSerializer(jackson2JsonRedisSerializer);
        redisTemplate.setHashKeySerializer(jackson2JsonRedisSerializer);
        redisTemplate.setHashValueSerializer(jackson2JsonRedisSerializer);
 
        return redisTemplate;
    }
    @Bean
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory factory) {
        StringRedisTemplate stringRedisTemplate = new StringRedisTemplate();
        stringRedisTemplate.setConnectionFactory(factory);
        return stringRedisTemplate;
    }
}
```



我们在中间再编写一个Service，

```java
@Service
public class BloomFilterService {

    @Autowired
    private RedissonClient redissonClient;

    /**
     * 创建布隆过滤器
     * @param filterName 布隆过滤器名称
     * @param capacity 预测插入数量
     * @param errorRate 误判率
     * @param <T>
     * @return
     */
    public <T> RBloomFilter<T> create(String filterName, long capacity, double errorRate) {
        RBloomFilter<T> bloomFilter = redissonClient.getBloomFilter(filterName);
        bloomFilter.tryInit(capacity, errorRate);
        return bloomFilter;
    }
}
```

测试：

```java
package com.nzc.test;

import com.nzc.WebApplication;
import com.nzc.service.BloomFilterService;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.redisson.api.RBloomFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest(classes = WebApplication.class)
public class BloomFilterTest {

    @Autowired
    private BloomFilterService bloomFilterService;

    @Test
    public void testBloomFilter() {
        // 预期插入数量
        long expectedInsertions = 1000L;
        // 错误比率
        double falseProbability = 0.01;
        RBloomFilter<Long> bloomFilter = bloomFilterService.create("NZC:BOOM-FILTER", expectedInsertions, falseProbability);
        // 布隆过滤器增加元素
        for (long i = 0; i < expectedInsertions; i++) {
            bloomFilter.add(i);
        }
        long elementCount = bloomFilter.count();
        log.info("布隆过滤器中含有元素个数 = {}.", elementCount);

        // 统计误判次数
        int count = 0;
        // 我在数据范围之外的数据，测试相同量的数据，判断错误率是不是符合我们当时设定的错误率
        for (long i = expectedInsertions; i < expectedInsertions * 2; i++) {
            if (bloomFilter.contains(i)) {
                count++;
            }
        }
        log.info("误判次数 = {}.", count);

        // 清空布隆过滤器 内部实现是个异步线程在执行  我只是为了方便测试
        bloomFilter.delete();
    }
}
```

当容量为1k，误判率为0.01时的输出情况

```bash
2022-08-26 23:37:04.903  INFO 1472 --- [           main] com.nzc.test.BloomFilterTest             : 布隆过滤器中含有元素个数 = 993.
2022-08-26 23:37:38.549  INFO 1472 --- [           main] com.nzc.test.BloomFilterTest             : 误判次数 = 36.
```



当容量为1w，误判率为0.01时的输出情况

```bash
2022-08-26 23:50:54.478  INFO 17088 --- [           main] com.nzc.test.BloomFilterTest             : 布隆过滤器中含有元素个数 = 9895.
2022-08-26 23:56:56.171  INFO 17088 --- [           main] com.nzc.test.BloomFilterTest             : 误判次数 = 259.
```





## 四、小结

> 我实际测试的时候，Guava 的效果应该是最好的，Redission 虽然是直接集成了Redis，但实际效果比起Guava较差一些，我这里没有贴上时间，Redission所创建出来的布隆过滤器，速度较慢。

当然我的测试范围是有限的，并且只是循环测试，另外服务器也并非在本地，这都有影响。

但是仅目前看来是这样的。

还有就是将 Guava 结合 Redis 一起使用。

## 五、Guava 布隆过滤器结合 Redis 使用

> 仅限于测试，一切效果还是需看实测。

---

我是以 Guava 中创建 布隆过滤器为基础，利用它构造的方法，来进行修改，功能相比于 guava 还是少了很多的。

```java
package com.nzc.boom;
 
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.hash.Funnel;
import com.google.common.hash.Hashing;
import com.google.common.primitives.Longs;

public class BloomFilterHelper<T> {
 
    private int numHashFunctions;
 
    private int bitSize;
 
    private Funnel<T> funnel;
 
    public BloomFilterHelper(Funnel<T> funnel, int expectedInsertions, double fpp) {
        Preconditions.checkArgument(funnel != null, "funnel不能为空");
        this.funnel = funnel;
        // 计算bit数组长度
        bitSize = optimalNumOfBits(expectedInsertions, fpp);
        // 计算hash方法执行次数
        numHashFunctions = optimalNumOfHashFunctions(expectedInsertions, bitSize);
    }


    /** 源码
     *public <T> boolean mightContain(
     *         T object, Funnel<? super T> funnel, int numHashFunctions, LockFreeBitArray bits) {
     *       long bitSize = bits.bitSize();
     *       byte[] bytes = Hashing.murmur3_128().hashObject(object, funnel).getBytesInternal();
     *       long hash1 = lowerEight(bytes);
     *       long hash2 = upperEight(bytes);
     *
     *       long combinedHash = hash1;
     *       for (int i = 0; i < numHashFunctions; i++) {
     *         // Make the combined hash positive and indexable
     *         if (!bits.get((combinedHash & Long.MAX_VALUE) % bitSize)) {
     *           return false;
     *         }
     *         combinedHash += hash2;
     *       }
     *       return true;
     *     }
     * @param value
     * @return
     */
    public long[] murmurHashOffset(T value) {
        long[] offset = new long[numHashFunctions];
        byte[] bytes = Hashing.murmur3_128().hashObject(value, funnel).asBytes();
        long hash1 = lowerEight(bytes);
        long hash2 = upperEight(bytes);
        long combinedHash = hash1;
        for (int i = 1; i <= numHashFunctions; i++) {
            long nextHash = hash1 + i * hash2;
            if (nextHash < 0) {
                nextHash = ~nextHash;
            }
            offset[i - 1] = nextHash % bitSize;
        }
        return offset;


    }
    private /* static */ long lowerEight(byte[] bytes) {
        return Longs.fromBytes(
                bytes[7], bytes[6], bytes[5], bytes[4], bytes[3], bytes[2], bytes[1], bytes[0]);
    }

    private /* static */ long upperEight(byte[] bytes) {
        return Longs.fromBytes(
                bytes[15], bytes[14], bytes[13], bytes[12], bytes[11], bytes[10], bytes[9], bytes[8]);
    }
    /**
     * 计算bit数组长度
     * 同样是guava创建布隆过滤器中的计算bit数组长度方法
     */
    private int optimalNumOfBits(long n, double p) {
        if (p == 0) {
            // 设定最小期望长度
            p = Double.MIN_VALUE;
        }
        return (int) (-n * Math.log(p) / (Math.log(2) * Math.log(2)));
    }
 
    /**
     * 这里是从guava 中 copy 出来的
     * 就是guava 创建一个 布隆过滤器时，
     * 计算hash方法执行次数的方法
     */
    private int optimalNumOfHashFunctions(long n, long m) {
        int countOfHash = Math.max(1, (int) Math.round((double) m / n * Math.log(2)));
        return countOfHash;
    }

}
```

以上的这些代码，在guava包都可以找到的。



在redisConfig中注入布隆过滤器

```java

/**
 * @description:
 * @author: Yihui Wang
 * @date: 2022年08月26日 22:06
 */
@Configuration
@EnableCaching
public class RedisConfig {

    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        RedisCacheManager rcm=RedisCacheManager.create(connectionFactory);
        return rcm;
    }
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory) {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<String, Object>();
        redisTemplate.setConnectionFactory(factory);
 
        Jackson2JsonRedisSerializer jackson2JsonRedisSerializer = new
                Jackson2JsonRedisSerializer(Object.class);
        ObjectMapper om = new ObjectMapper();
        om.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        om.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
        jackson2JsonRedisSerializer.setObjectMapper(om);
        //序列化设置 ，这样计算是正常显示的数据，也能正常存储和获取
        redisTemplate.setKeySerializer(jackson2JsonRedisSerializer);
        redisTemplate.setValueSerializer(jackson2JsonRedisSerializer);
        redisTemplate.setHashKeySerializer(jackson2JsonRedisSerializer);
        redisTemplate.setHashValueSerializer(jackson2JsonRedisSerializer);
 
        return redisTemplate;
    }
    @Bean
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory factory) {
        StringRedisTemplate stringRedisTemplate = new StringRedisTemplate();
        stringRedisTemplate.setConnectionFactory(factory);
        return stringRedisTemplate;
    }
 
    
    //初始化布隆过滤器，放入到spring容器里面
    @Bean
    public BloomFilterHelper<String> initBloomFilterHelper() {
        return new BloomFilterHelper<String>((Funnel<String>) (from, into) -> into.putString(from, Charsets.UTF_8).putString(from, Charsets.UTF_8), 1000, 0.01);
    }

    @Bean
    public BloomFilterHelper<Long> initLongBloomFilterHelper() {
        return new BloomFilterHelper<Long>((Funnel<Long>) (from, into) -> into.putLong(from),1000, 0.01);
    }


}
```

也就是注入我们刚刚编写的那个布隆过滤器。



然后再编写一个Service 层

```java

/**
 * @description:
 * @author: Yihui Wang
 */
@Slf4j
@Service
public class RedisBloomFilter {

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 根据给定的布隆过滤器添加值
     */
    public <T> void addByBloomFilter(BloomFilterHelper<T> bloomFilterHelper, String key, T value) {
        Preconditions.checkArgument(bloomFilterHelper != null, "bloomFilterHelper不能为空");
        long[] offset = bloomFilterHelper.murmurHashOffset(value);
        for (long i : offset) {
            log.info("key :{} ，value : {}", key,  i);
            redisTemplate.opsForValue().setBit(key, i, true);
        }
    }

    /**
     * 根据给定的布隆过滤器判断值是否存在
     */
    public <T> boolean includeByBloomFilter(BloomFilterHelper<T> bloomFilterHelper, String key, T value) {
        Preconditions.checkArgument(bloomFilterHelper != null, "bloomFilterHelper不能为空");
        long[] offset = bloomFilterHelper.murmurHashOffset(value);
        for (long i : offset) {
            log.info("key :{} ，value : {}", key,  i);
            if (!redisTemplate.opsForValue().getBit(key, i)) {
                return false;
            }
        }
        return true;
    }
}
```





测试：

```java
    @Test
    public void test1() {
        // 预期插入数量
        long capacity = 1000L;
        // 错误比率
        double errorRate = 0.01;
        for (long i = 0; i < capacity; i++) {
            redisBloomFilter.addByBloomFilter(bloomFilterHelper, "nzc:bloomFilter1", i);
        }
        log.info("存入元素为=={}", capacity);
        // 统计误判次数
        int count = 0;
        // 我在数据范围之外的数据，测试相同量的数据，判断错误率是不是符合我们当时设定的错误率
        for (long i = capacity; i < capacity * 2; i++) {
            if (redisBloomFilter.includeByBloomFilter(bloomFilterHelper, "nzc:bloomFilter1", i)) {
                count++;
            }
        }
        System.out.println("误判个数为==>" + count);
    }

```

输出：

```
存入元素为==1000
误判个数为==>12
```



## 后记

> 终于又到周六周日啦，这次一定要好好整点东西出来，完成自己的flag~



写于2022年 8 月 26日晚，作者：[宁在春](https://juejin.cn/user/2859142558267559)