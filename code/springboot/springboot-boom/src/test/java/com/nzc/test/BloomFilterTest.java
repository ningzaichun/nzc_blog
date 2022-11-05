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
        long capacity = 1000000L;
        // 错误比率
        double errorRate = 0.01;
        RBloomFilter<Long> bloomFilter = bloomFilterService.create("NZC:BOOM-FILTER2", capacity, errorRate);
        // 布隆过滤器增加元素
        for (long i = 0; i < capacity; i++) {
            bloomFilter.add(i);
        }
        long elementCount = bloomFilter.count();
        log.info("布隆过滤器中含有元素个数 = {}.", elementCount);

        // 统计误判次数
        int count = 0;
        // 我在数据范围之外的数据，测试相同量的数据，判断错误率是不是符合我们当时设定的错误率
        for (long i = capacity; i < capacity * 2; i++) {
            if (bloomFilter.contains(i)) {
                count++;
            }
        }
        log.info("误判次数 = {}.", count);

        // 清空布隆过滤器 内部实现是个异步线程在执行  我只是为了方便测试
        bloomFilter.delete();
    }
}