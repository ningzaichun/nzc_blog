import com.alibaba.fastjson.JSON;
import com.nzc.ApplicationCache;
import com.nzc.entity.Student;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.redisson.api.RLock;
import org.redisson.api.RReadWriteLock;
import org.redisson.api.RSemaphore;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.*;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * @description:
 * @author: Ning Zaichun
 * @date: 2022年09月21日 22:01
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {ApplicationCache.class})
public class RedisTest {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;


    @Autowired
    private RedissonClient redissonClient;


    @Test
    public void testRedissonReadAndWriteLock() {
        RSemaphore semaphore = redissonClient.getSemaphore("semaphore");
        RReadWriteLock readWriteLock = redissonClient.getReadWriteLock("redisson:lock");
        redissonClient.getReadWriteLock("redisson:lock");
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


    @Test
    public void testRedisson() {
        RLock lock = redissonClient.getLock("redisson:lock");
        try {
            lock.tryLock();
            try {
                Thread.sleep(100000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            //执行需要加锁的业务~
        } finally {
            lock.unlock();
        }
    }


    /**
     * set key value 命令 使用
     */
    @Test
    public void test1() {
        // set key value 往redis 中set 一个值
        stringRedisTemplate.opsForValue().set("username", "宁在春");
        // get key : 从redis中根据key 获取一个值
        System.out.println(stringRedisTemplate.opsForValue().get("username"));
        //out：宁在春

        // del key: 从redis 中删除某个key
        stringRedisTemplate.delete("username");
        System.out.println(stringRedisTemplate.opsForValue().get("username"));
    }

    /**
     * setnx  key value : 如果 key 不存在，则设置成功 返回 1 ;否则失败 返回 0
     */
    @Test
    public void test2() {
        Boolean lock = stringRedisTemplate.opsForValue().setIfAbsent("lock", "1");
        System.out.println(lock);
        Boolean lock2 = stringRedisTemplate.opsForValue().setIfAbsent("lock", "1");
        System.out.println(lock2);
        //true
        //false
    }

    /**
     * set  key value nx ex : 如果 key 不存在，则设置值和过期时间 返回 1 ;否则失败 返回 0
     * nx、ex 都为命令后面的参数
     * 更为详细命令的解释：https://redis.io/commands/set/
     * 这个命令也常常在分布式锁中出现，悄悄为后文铺垫一下
     */
    @Test
    public void test3() {
        Boolean lock = stringRedisTemplate.opsForValue().setIfAbsent("lock:nx:ex", "lock", 30L, TimeUnit.SECONDS);
        System.out.println(lock);
        Boolean lock2 = stringRedisTemplate.opsForValue().setIfAbsent("lock:nx:ex", "lock", 30L, TimeUnit.SECONDS);
        System.out.println(lock2);
        //true
        //false
    }


    /**
     * 上述的三个测试，都是基础的 set 命令的，但 redis 中也有很多其他的数据结构，如 list、set、map 等等
     * list测试，可将列表视为队列（先进先出）
     * 想要详细了解，请查看：https://redis.io/docs/data-types/lists/
     * 【注意注意注意：还有很多方法没有测试，大家私下可以多用用】
     */
    @Test
    public void test4() {
        ListOperations<String, String> opsForList = stringRedisTemplate.opsForList();
        //从列表左边进行插入
        opsForList.leftPush("left:key", "1");
        opsForList.leftPush("left:key", "2");
        opsForList.leftPush("left:key", "3");
        opsForList.leftPush("left:key", "4");
        opsForList.leftPush("left:key", "5");
        //从列表右边进行插入
        opsForList.rightPush("left:key", "10");
        opsForList.rightPush("left:key", "9");
        opsForList.rightPush("left:key", "8");
        opsForList.rightPush("left:key", "7");
        opsForList.rightPush("left:key", "6");

        //按规定范围取出数据 ，也可以取出单个数据 leftPop rightPop ,按这样的方式取出来，也就代表从列表中删除了。
        List<String> stringList = opsForList.range("left:key", 1, 10);

        stringList.forEach(System.out::print);
        //out::4321109876
    }

    /**
     * set 测试，set 数据结构是唯一字符串（成员）的无序集合
     * 想要详细了解，请查看：https://redis.io/docs/data-types/sets/
     * 【注意注意注意：还有很多方法没有测试，大家私下可以多用用】
     */
    @Test
    public void test5() {
        SetOperations<String, String> opsForSet = stringRedisTemplate.opsForSet();

        opsForSet.add("set:key", "宁在春", "1", "1", "2", "3", "开始学习");

        Set<String> members = opsForSet.members("set:key");
        members.forEach(System.out::print);
        // 3宁在春21开始学习

        System.out.println("");
        // 从 set 集合中删除 value 为 1 的值
        opsForSet.remove("set:key", "1");

        Set<String> members2 = opsForSet.members("set:key");
        members2.forEach(System.out::print);
        //2宁在春3开始学习
        // 注意：取出来的时候，不一定是插入顺序
    }

    /**
     * Redis 哈希 测试，
     * Redis 哈希是结构为字段值对集合的记录类型。您可以使用散列来表示基本对象并存储计数器分组等。
     * 想要详细了解，请查看：https://redis.io/docs/data-types/hashes/
     * 【注意注意注意：还有很多方法没有测试，大家私下可以多用用】
     * 应用场景：可以存储登录用户的相关信息
     */
    @Test
    public void test6() {
        HashOperations<String, Object, Object> opsForHash = stringRedisTemplate.opsForHash();
        opsForHash.put("hash:key", "username", "宁在春");
        opsForHash.put("hash:key", "school", "xxxx学校");
        opsForHash.put("hash:key", "age", "3");

        Object username = opsForHash.get("hash:key", "username");
        System.out.println(username);
        //宁在春
        username = "宁在春写的这篇文章还不错，值得一赞";
        // 更新某一个值数据
        opsForHash.put("hash:key", "username", username);
        Object username2 = opsForHash.get("hash:key", "username");
        System.out.println(username2);
        //宁在春写的这篇文章还不错，值得一赞

        // 删除某一条数据
        opsForHash.delete("hash:key", "age");

        // 展示某个key下所有的 hashKey
        Set<Object> keys = opsForHash.keys("hash:key");
        keys.forEach(System.out::println);
        //username
        //school

        // 展示某个key下所有 hashKey 对应 Value值
        List<Object> hashKeyValues = opsForHash.values("hash:key");
        hashKeyValues.forEach(System.out::println);
        //宁在春写的这篇文章还不错，值得一赞
        //xxxx学校


        HashMap<String, String> map = new HashMap<>();
        map.put("name", "nzc");
        map.put("address", "china");
        opsForHash.putAll("hash:key2", map);

        List<Object> values = opsForHash.values("hash:key2");
        values.forEach(System.out::println);
        //china
        //nzc

    }

    @Autowired
    private RedisTemplate<Object, Object> redisTemplate;


    /**
     * 上面的操作都是操作字符串,但是大家在使用的过程中都知道,我们大都是存放到Redis中的数据,都是将某个对象直接放入Redis中
     * <p>
     * redisTemplate 的操作和 stringRedisTemplate 的操作都是一样的，只是内部存放的不同罢了
     */
    @Test
    public void test7() {
        Map<String, Object> map = new HashMap<>();

        Student s1 = new Student();
        s1.setSchool("xxxx1");
        s1.setUsername("ningzaichun1号");
        s1.setAge(3);

        map.put("username", s1.getUsername());
        map.put("school", s1.getSchool());
        map.put("age", s1.getAge());
        redisTemplate.opsForHash().putAll("student:key1", map);

        List<Object> values = redisTemplate.opsForHash().values("student:key1");
        values.forEach(System.out::println);
        //
    }

    /**
     * 虽然大家可以在Java 程序中看到取出来的值是正常的,
     * 但是在平时开发和测试的时候,我们还是需要借助 Redis 的可视化工具来查看的,
     * 你会发现,我们采用默认的序列化机制(JDK序列化机制),在Redis可视化软件中,会无法直接查看的,
     */
    @Test
    public void test8() {
        Student s1 = new Student();
        s1.setSchool("xxxx1");
        s1.setUsername("ningzaichun1号");
        s1.setAge(3);
        redisTemplate.opsForValue().set("nzc", JSON.toJSONString(s1));
    }

    /**
     * 虽然大家可以在Java 程序中看到取出来的值是正常的,
     * 但是在平时开发和测试的时候,我们还是需要借助 Redis 的可视化工具来查看的,
     * 你会发现,我们采用默认的序列化机制(JDK序列化机制),在Redis可视化软件中,会无法直接查看的,
     */
    @Test
    public void test9() {
        Map<String, Object> map = new HashMap<>();

        Student s1 = new Student();
        s1.setSchool("xxxx1");
        s1.setUsername("ningzaichun1号");
        s1.setAge(3);
        map.put("user:1", s1);
        redisTemplate.opsForHash().putAll("student:key1", map);

        List<Object> values = redisTemplate.opsForHash().values("student:key2");
        values.forEach(System.out::println);
        //
    }

    /**
     * 虽然大家可以在Java 程序中看到取出来的值是正常的,
     * 但是在平时开发和测试的时候,我们还是需要借助 Redis 的可视化工具来查看的,
     * 你会发现,我们采用默认的序列化机制(JDK序列化机制),在Redis可视化软件中,会无法直接查看的,
     * 都是转码之后的数据: \xac\xed\x00\x05t\x00\x06user:2
     * 图片见下面第二张图
     */
    @Test
    public void test10() {
        Map<String, Student> map = new HashMap<>();
        Student s1 = new Student();
        s1.setSchool("xxxx1");
        s1.setUsername("ningzaichun1号");
        s1.setAge(3);
        Student s2 = new Student();
        s2.setSchool("xxxx2");
        s2.setUsername("ningzaichun2号");
        s2.setAge(5);
        map.put("user:1", s1);
        map.put("user:2", s2);
        redisTemplate.opsForHash().putAll("student:key", map);

        List<Object> values = redisTemplate.opsForHash().values("student:key");
        values.forEach(System.out::println);
        //Student(username=ningzaichun1号, school=xxxx1, age=3)
        //Student(username=ningzaichun2号, school=xxxx2, age=5)
    }

}
