package com.nzc.test;

import cn.hutool.bloomfilter.BitMapBloomFilter;
import com.google.common.base.Charsets;
import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnels;
import com.nzc.WebApplication;
import com.nzc.boom.BloomFilterHelper;
import com.nzc.service.RedisBloomFilter;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.nio.charset.Charset;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * @description:
 * @author: Yihui Wang
 * @date: 2022年08月26日 22:16
 */
@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest(classes = WebApplication.class)
public class RedisBloomFilterTest {


    @Autowired
    RedisBloomFilter redisBloomFilter;

    @Autowired
    BloomFilterHelper<Long> bloomFilterHelper;

    @Test
    public void test1() {
        // 预期插入数量
        long capacity = 1000L;
        // 错误比率
        double errorRate = 0.01;
        for (long i = 0; i < capacity; i++) {
            redisBloomFilter.addByBloomFilter(bloomFilterHelper, "nzc:bloomFilter1", i);
        }

        // 统计误判次数
        int count = 0;
        // 我在数据范围之外的数据，测试相同量的数据，判断错误率是不是符合我们当时设定的错误率
        for (long i = capacity; i < capacity * 2; i++) {
            if (redisBloomFilter.includeByBloomFilter(bloomFilterHelper, "nzc:bloomFilter1", i)) {
                count++;
            }
        }
        log.info("存入元素为=={}", capacity);
        System.out.println("误判个数为==>" + count);
    }

    @Test
    public void test2() {
        // 预期插入数量
        long capacity = 1000000L;
        // 错误比率
        double errorRate = 0.01;
        //创建BloomFilter对象，需要传入Funnel对象，预估的元素个数，错误率
        BloomFilter<Long> filter = BloomFilter.create(Funnels.longFunnel(), capacity, errorRate);
//        BloomFilter<String> filter = BloomFilter.create(Funnels.stringFunnel(Charset.forName("utf-8")), 10000, 0.0001);
        //put值进去
        for (long i = 0; i < capacity; i++) {
            filter.put(i);
        }

        log.info("存入元素为=={}", capacity);
        // 统计误判次数
        int count = 0;
        // 我在数据范围之外的数据，测试相同量的数据，判断错误率是不是符合我们当时设定的错误率
        for (long i = capacity; i < capacity * 2; i++) {
            if (filter.mightContain(i)) {
                count++;
            }
        }
        System.out.println("误判个数为==>" + count);

    }

    @Test
    public void test3() {
        BloomFilter<String> bloomFilter = BloomFilter.create(Funnels.stringFunnel(Charsets.UTF_8), 50000, 0.01);
        List<String> list = new ArrayList<>(50000);
        for (int i = 0; i < 50000; i++) {
            String uuid = UUID.randomUUID().toString();
            bloomFilter.put(uuid);
            list.add(uuid);
        }
        int mightContainNumber2 = 0;
        for (int i = 0; i < 5000000; i++) {
            String key = UUID.randomUUID().toString();
            if (bloomFilter.mightContain(key)) {
                mightContainNumber2++;
            }
        }
        NumberFormat percentFormat = NumberFormat.getPercentInstance();
        System.out.println("【key不存在的情况】布隆过滤器认为存在的key值数：" + mightContainNumber2);
        System.out.println("【key不存在的情况】布隆过滤器的误判率为：" + percentFormat.format((float) mightContainNumber2 / 5000000));

    }


    @Test
    public void test4() {
        int capacity = 100;
        // 错误比率
        double errorRate = 0.01;
        // 初始化
        BitMapBloomFilter filter = new BitMapBloomFilter(capacity);
        for (int i = 0; i < capacity; i++) {
            filter.add(String.valueOf(i));
        }

        log.info("存入元素为=={}", capacity);
        // 统计误判次数
        int count = 0;
        // 我在数据范围之外的数据，测试相同量的数据，判断错误率是不是符合我们当时设定的错误率
        for (int i = capacity; i < capacity * 2; i++) {
            if (filter.contains(String.valueOf(i))) {
                count++;
            }
        }
        log.info("误判元素为==={}", count);
    }
}
