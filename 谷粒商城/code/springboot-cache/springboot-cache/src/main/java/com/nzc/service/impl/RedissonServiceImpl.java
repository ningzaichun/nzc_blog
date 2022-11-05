package com.nzc.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.nzc.entity.MenuEntity;
import com.nzc.mapper.MenuMapper;
import com.nzc.service.IRedissonService;
import com.nzc.service.ISetNxExLockService;
import org.redisson.api.RLock;
import org.redisson.api.RReadWriteLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * @description:
 * @author: Ning Zaichun
 * @date: 2022年09月20日 20:59
 */
@Service
public class RedissonServiceImpl implements IRedissonService {

    @Autowired
    StringRedisTemplate stringRedisTemplate;

    @Autowired
    private MenuMapper menuMapper;

    private static final String REDISSON_MENU_LIST = "redisson:menu:list";
    private static final String REDISSON_MENU_LIST_LOCK_VALUE = "redisson:lock";
    @Autowired
    private RedissonClient redissonClient;


    @Override
    public List<MenuEntity> getList() {
        // 判断缓存是否有数据
        String menuJson = stringRedisTemplate.opsForValue().get(REDISSON_MENU_LIST);
        if (menuJson != null) {


            System.out.println("缓存中有，直接返回缓存中的数据");
            List<MenuEntity> menuEntityList = JSON.parseObject(menuJson, new TypeReference<List<MenuEntity>>() {
            });
            return menuEntityList;
        }

        // 从数据库中查询
        List<MenuEntity> result = getMenuJsonFromDbWithRedissonLock();

        return result;
    }



    /**
     * 问题：其实写成上面那种模样，相对来说，也能解决很多时候的问题了，从头到尾看过来的话，其实也能发现就一个锁自动过期问题没有解决了。
     * 但在实现这个之前，我还是说明一下，为什么说在没有解决锁自动过期问题时，就已经能应付大多数场景了。
     * 重点在于如何评估 锁自动过期时间，锁自动过期时间到底设置多少合适呢？
     * 其实如果对于业务理解较为透彻，对于这一部分的业务代码执行时间能有一个较清晰的估算，给定一个合适的时间，在不出现极端情况，基本都能应付过来了。
     * <p>
     * 但是呢，很多时候，还是会怕这个万一的，万一真出现了，可能造成的损失就不止 一万了，哈哈。
     * 解决方案
     * 1、在市场主流的 Redission 中，针对这样的问题，已经有了解决方案。这也是Redission中常说的看门狗机制。
     * <p>
     * 如果需要自己实现的思路：
     * 1、这方面的问题，也做了十分浅显的思考，我觉得应该还是依赖于定时任务去实现，但到底该如何实现这个定时任务，我还没法给出一个合适的解决方案。 或许我应该会尝试一下。
     *
     * @return
     */
    public List<MenuEntity> getMenuJsonFromDbWithRedissonLock() {
        System.out.println("从数据库中查询");
        //1、占分布式锁。去redis占坑
        //（锁的粒度，越细越快:具体缓存的是某个数据，11号商品） product-11-lock
        //RLock catalogJsonLock = redissonClient.getLock("catalogJson-lock");
        //创建读锁
        RReadWriteLock readWriteLock = redissonClient.getReadWriteLock(REDISSON_MENU_LIST_LOCK_VALUE);
        RLock rLock = readWriteLock.readLock();
        List<MenuEntity> result = null;
        try {
            rLock.lock();
            String menuJson = stringRedisTemplate.opsForValue().get(REDISSON_MENU_LIST);
            if (menuJson != null) {
                System.out.println("缓存中有，直接返回缓存中的数据");
                List<MenuEntity> menuEntityList = JSON.parseObject(menuJson, new TypeReference<List<MenuEntity>>() {
                });
                return menuEntityList;
            }
            try {
                Thread.sleep(50000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            //加锁成功...执行业务
            //加锁成功...执行业务
            result = menuMapper.selectList(new QueryWrapper<MenuEntity>());
            // 构建缓存
            stringRedisTemplate.opsForValue().set(REDISSON_MENU_LIST, JSON.toJSONString(result));
        } finally {
            rLock.unlock();
        }
        return result;
    }


    @Override
    public Boolean updateMenuById(MenuEntity menu) {
        return updateMenuWithRedissonLock(menu);
    }

    public Boolean updateMenuWithRedissonLock(MenuEntity menu) {
        RReadWriteLock readWriteLock = redissonClient.getReadWriteLock(REDISSON_MENU_LIST_LOCK_VALUE);
        RLock writeLock = readWriteLock.writeLock();
        Boolean update = false;
        try {
            writeLock.lock();
            //加锁成功...执行业务
            //加锁成功...执行业务
            update = menuMapper.updateById(menu) > 0;
        } finally {
            writeLock.unlock();
        }
        return update;
    }


}
