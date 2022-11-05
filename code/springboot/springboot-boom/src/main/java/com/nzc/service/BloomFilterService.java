package com.nzc.service;

import org.redisson.api.RBloomFilter;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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