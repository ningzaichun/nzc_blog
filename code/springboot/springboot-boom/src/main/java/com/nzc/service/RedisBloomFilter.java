package com.nzc.service;

import com.google.common.base.Preconditions;
import com.google.common.hash.BloomFilter;
import com.nzc.boom.BloomFilterHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

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
            log.info("key :{} ", key, "value : {}", i);
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
            log.info("key :{} ", key, "value : {}", i);
            if (!redisTemplate.opsForValue().getBit(key, i)) {
                return false;
            }
        }
        return true;
    }

}
