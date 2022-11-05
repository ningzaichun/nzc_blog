package com.nzc.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.nzc.entity.MenuEntity;
import com.nzc.mapper.MenuMapper;
import com.nzc.service.IRedisCacheService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;


/**
 * @description: 单机redis下的操作
 * @author: Ning Zaichun
 * @date: 2022年09月06日 23:20
 */
@Slf4j
@Service
public class RedisCacheServiceImpl implements IRedisCacheService {


    @Autowired
    private MenuMapper menuMapper;

    @Autowired
    StringRedisTemplate redisTemplate;

    private static final String REDIS_MENU_CACHE_KEY = "menu:list";

    private ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock();


    @Override
    public List<MenuEntity> getList() {
        //1、从缓存中获取
        String menuJson = redisTemplate.opsForValue().get(REDIS_MENU_CACHE_KEY);
        if (!StringUtils.isEmpty(menuJson)) {
            System.out.println("缓存中有，直接从缓存中获取");
            //2、缓存不为空直接返回
            List<MenuEntity> menuEntityList = JSON.parseObject(menuJson, new TypeReference<List<MenuEntity>>() {
            });
            return menuEntityList;
        }
        //3、查询数据库
        //不加锁情况下
        // List<MenuEntity> noLockList = getMenuJsonFormDb();
        // 加锁情况下
        List<MenuEntity> menuEntityList = getMenuJsonFormDbWithReentrantReadWriteLock();
        return menuEntityList;
    }

    public List<MenuEntity> getMenuJsonFormDb() {
        System.out.println("缓存中没有，重新从数据中查询~==>");
        //缓存为空，查询数据库，重新构建缓存
        List<MenuEntity> result = menuMapper.selectList(new QueryWrapper<MenuEntity>());
        //4、将查询的结果，重新放入缓存中
        redisTemplate.opsForValue().set(REDIS_MENU_CACHE_KEY, JSON.toJSONString(result));
        return result;
    }

    public List<MenuEntity> getMenuJsonFormDbWithReentrantReadWriteLock() {
        List<MenuEntity> result = null;
        System.out.println("缓存中没有，加锁，重新从数据中查询~==>");
        // synchronized 是同步锁，所以当多个线程同时执行到这里时，会阻塞式等待
        ReentrantReadWriteLock.ReadLock readLock = readWriteLock.readLock();
        readLock.lock();
        try {
            String menuJson = redisTemplate.opsForValue().get(REDIS_MENU_CACHE_KEY);
            //加锁成功... 再次判断缓存是否为空
            if (!StringUtils.isEmpty(menuJson)) {
                System.out.println("缓存中，直接从缓存中获取");
                //2、缓存不为空直接返回
                List<MenuEntity> menuEntityList = JSON.parseObject(menuJson, new TypeReference<List<MenuEntity>>() {
                });
                return menuEntityList;
            }
            //缓存为空，查询数据库，重新构建缓存
            result = menuMapper.selectList(new QueryWrapper<MenuEntity>());
            //4、将查询的结果，重新放入缓存中
            redisTemplate.opsForValue().set(REDIS_MENU_CACHE_KEY, JSON.toJSONString(result));
            return result;
        } finally {
            readLock.unlock();
        }
    }


    @Override
    public Boolean updateMenuById(MenuEntity menu) {
        //return updateMenuByIdNoWithLock(menu);
        return updateMenuByIdWithReentrantReadWriteLock(menu);
    }


    public Boolean updateMenuByIdNoWithLock(MenuEntity menu) {
        // 1、删除缓存
        redisTemplate.delete(REDIS_MENU_CACHE_KEY);
        System.out.println("清空单机Redis缓存");
        // 2、更新数据库
        return menuMapper.updateById(menu) > 0;
    }

    public Boolean updateMenuByIdWithReentrantReadWriteLock(MenuEntity menu) {
        ReentrantReadWriteLock.WriteLock writeLock = readWriteLock.writeLock();
        writeLock.lock();
        try {
            // 1、删除缓存
            System.out.println("清除缓存");
            redisTemplate.delete(REDIS_MENU_CACHE_KEY);
            // 2、更新数据库
            return menuMapper.updateById(menu) > 0;
        }finally {
            writeLock.unlock();
        }
    }

}
