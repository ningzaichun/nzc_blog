package com.nzc.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.nzc.entity.MenuEntity;
import com.nzc.mapper.MenuMapper;
import com.nzc.service.ILuaLockService;
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
public class LuaLockServiceImpl implements ILuaLockService {

    @Autowired
    StringRedisTemplate stringRedisTemplate;

    @Autowired
    private MenuMapper menuMapper;

    private static final String LUA_MENU_LIST = "lua:menu:list";
    private static final String LUA_MENU_LIST_LOCK = "lua:menu:list:lock";

    @Override
    public List<MenuEntity> getList() {
        // 1、判断缓存是否有数据
        String menuJson = stringRedisTemplate.opsForValue().get(LUA_MENU_LIST);
        if (menuJson != null) {
            System.out.println("缓存中有，直接返回缓存中的数据");
            List<MenuEntity> menuEntityList = JSON.parseObject(menuJson, new TypeReference<List<MenuEntity>>() {
            });
            return menuEntityList;
        }
        // 从数据库中查询
        List<MenuEntity> result = getMenuJsonFormDbWithLuaLock();

        return result;
    }

    /**
     * 问题：释放锁操作 丢失 原子性
     * 解决方案: lua 脚本
     * 既然删除操作变成了两步，失去了原子性，那么我们就把它改成一步就行啦呀，
     * 此时就得用上我们的 lua 脚本了
     *
     * @return
     */
    public List<MenuEntity> getMenuJsonFormDbWithLuaLock() {
        List<MenuEntity> result = null;
        System.out.println("缓存中没有，加锁，重新从数据中查询~==>");
        // 给锁设定一个时间
        String uuid = UUID.randomUUID().toString();
        Boolean lock = stringRedisTemplate.opsForValue().setIfAbsent(LUA_MENU_LIST_LOCK, uuid, 5L, TimeUnit.SECONDS);
        if (lock) {
            System.out.println("获取分布式锁成功...");
            try {
                //加锁成功...执行业务
                result = menuMapper.selectList(new QueryWrapper<MenuEntity>());
                stringRedisTemplate.opsForValue().set(LUA_MENU_LIST, JSON.toJSONString(result));
            } finally {
                // 编写 lua 脚本
                String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
                //删除锁  execute 这个放番中的参数可能还需要提一提
                stringRedisTemplate.execute(new DefaultRedisScript<Long>(script, Long.class), Arrays.asList(LUA_MENU_LIST_LOCK), uuid);
            }
            return result;
        } else {
            System.out.println("获取分布式锁失败...等待重试...");
            //加锁失败...重试机制
            //休眠一百毫秒
            try {
                TimeUnit.MILLISECONDS.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return getMenuJsonFormDbWithLuaLock();
        }
    }

    @Override
    public Boolean updateMenuById(MenuEntity menu) {
        String uuid = UUID.randomUUID().toString();
        Boolean lock = stringRedisTemplate.opsForValue().setIfAbsent(LUA_MENU_LIST_LOCK, uuid, 5L, TimeUnit.SECONDS);
        Boolean update = false;
        if (lock) {
            System.out.println("更新操作：获取分布式锁成功===>");
            // 删除缓存
            try {
                stringRedisTemplate.delete(LUA_MENU_LIST);
                // 更新数据库
                update = menuMapper.updateById(menu) > 0;
            } finally {
                // 编写 lua 脚本
                String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
                //删除锁  execute 这个放番中的参数可能还需要提一提
                stringRedisTemplate.execute(new DefaultRedisScript<Long>(script, Long.class), Arrays.asList(LUA_MENU_LIST_LOCK), uuid);
            }
        }
        return update;
    }


}
