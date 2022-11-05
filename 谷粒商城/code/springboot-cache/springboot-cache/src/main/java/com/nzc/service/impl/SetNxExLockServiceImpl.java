package com.nzc.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.nzc.entity.MenuEntity;
import com.nzc.mapper.MenuMapper;
import com.nzc.service.ISetNxExLockService;
import org.redisson.api.RLock;
import org.redisson.api.RReadWriteLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * @description:
 * @author: Ning Zaichun
 * @date: 2022年09月20日 20:59
 */
@Service
public class SetNxExLockServiceImpl implements ISetNxExLockService {

    @Autowired
    StringRedisTemplate stringRedisTemplate;

    @Autowired
    private MenuMapper menuMapper;

    private static final String SET_NX_EX_MENU_LIST = "set:nx:ex:menu:list";
    private static final String SET_NX_EX_MENU_LIST_LOCK = "set:nx:ex:menu:list:lock";
    private static final String SET_NX_EX_MENU_LIST_LOCK_VALUE = "lock";


    @Override
    public List<MenuEntity> getList() {
        // 判断缓存是否有数据
        String menuJson = stringRedisTemplate.opsForValue().get(SET_NX_EX_MENU_LIST);
        if (menuJson != null) {
            System.out.println("缓存中有，直接返回缓存中的数据");
            List<MenuEntity> menuEntityList = JSON.parseObject(menuJson, new TypeReference<List<MenuEntity>>() {
            });
            return menuEntityList;
        }

        // 从数据库中查询
        List<MenuEntity> result = getMenuJsonFormDbWithLock();

        return result;
    }

    @Override
    public List<MenuEntity> getList2() {
        // 判断缓存是否有数据
        String menuJson = stringRedisTemplate.opsForValue().get(SET_NX_EX_MENU_LIST);
        if (menuJson != null) {
            System.out.println("缓存中有，直接返回缓存中的数据");
            List<MenuEntity> menuEntityList = JSON.parseObject(menuJson, new TypeReference<List<MenuEntity>>() {
            });
            return menuEntityList;
        }

        // 从数据库中查询
        List<MenuEntity> result = getMenuJsonFormDbWithExpireLock();

        return result;
    }

    @Override
    public List<MenuEntity> getList3() {
        // 判断缓存是否有数据
        String menuJson = stringRedisTemplate.opsForValue().get(SET_NX_EX_MENU_LIST);
        if (menuJson != null) {
            System.out.println("缓存中有，直接返回缓存中的数据");
            List<MenuEntity> menuEntityList = JSON.parseObject(menuJson, new TypeReference<List<MenuEntity>>() {
            });
            return menuEntityList;
        }

        // 从数据库中查询
        List<MenuEntity> result = getMenuJsonFromDbWithRedisLock();

        return result;
    }

    /**
     * 问题：死锁问题， 如果在执行解锁操作之前，服务突然宕机，那么锁就永远无法被释放，从而造成死锁问题
     * 解决方案: 因为 Redis 的更新，现在 setIfAbsent 支持同时设置过期时间，而无需分成两步操作。
     *
     * @return
     */
    public List<MenuEntity> getMenuJsonFormDbWithLock() {
        List<MenuEntity> result = null;
        System.out.println("缓存中没有，加锁，重新从数据中查询~==>");
        Boolean lock = stringRedisTemplate.opsForValue().setIfAbsent(SET_NX_EX_MENU_LIST_LOCK, SET_NX_EX_MENU_LIST_LOCK_VALUE);
        if (lock) {
            System.out.println("获取分布式锁成功...");
            try {
                //加锁成功...执行业务
                result = menuMapper.selectList(new QueryWrapper<MenuEntity>());
                stringRedisTemplate.opsForValue().set(SET_NX_EX_MENU_LIST, JSON.toJSONString(result));
            } finally {
                // 释放锁
                stringRedisTemplate.delete(SET_NX_EX_MENU_LIST_LOCK);
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
            // 手动实现自旋
            return getMenuJsonFormDbWithLock();
        }
    }

    /**
     * 问题： 因为锁不会自动过期，如果服务在执行到解锁代码之前，突然宕机，那么则会导致死锁的发生。
     * 解决方案：我们在设置锁时，给锁加上自动过期时间。
     * 并且现在 Redis 的版本更新，set 命令已经可以加 多个参数，实现设置值和加过期时间，在一条命令中完成，让设置值和设置过期时间成为原子性操作
     * <p>
     * 但使用上述方案解决之前的死锁问题后，又继而出现下面的几个问题
     * 1、如果我的业务执行时间超过了锁的时间,也称为锁的自动续期问题，这一点我暂时是提出解决思路，但是代码实现，市面上已经有轮子（Redission）,
     * 我也实现不了那么好，虽说不给自己受限，但是技术没到位，挺难的。
     * 2、由第一个问题还会继而引出第二个问题，在第一个线程还在执行业务的时候，锁的时间已经过期，第二个线程重新获取锁，在第二个线程获取到锁后，第一个线程正好执行完了，
     * 这个时候执行释放锁操作，那么就会造成第二个线程所获得到的锁被释放。也就是锁被其他人释放，这该如何处理。
     *
     * @return
     */
    public List<MenuEntity> getMenuJsonFormDbWithExpireLock() {
        List<MenuEntity> result = new ArrayList<>();
        System.out.println("缓存中没有，加锁，重新从数据中查询~==>");
        // 给锁设定一个时间
        Boolean lock = stringRedisTemplate.opsForValue().setIfAbsent(SET_NX_EX_MENU_LIST_LOCK, SET_NX_EX_MENU_LIST_LOCK_VALUE, 5L, TimeUnit.SECONDS);
        if (lock) {
            System.out.println("获取分布式锁成功...");
            try {
                //加锁成功...执行业务
                result = menuMapper.selectList(new QueryWrapper<MenuEntity>());
                stringRedisTemplate.opsForValue().set(SET_NX_EX_MENU_LIST, JSON.toJSONString(result));
            } finally {
                // 释放锁
                stringRedisTemplate.delete(SET_NX_EX_MENU_LIST_LOCK);
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
            return getMenuJsonFormDbWithExpireLock();
        }

    }


    /**
     * 问题：锁被其他人释放，这该如何处理。
     * 加锁的时候，将值给定位一个唯一的标识符（我这里使用的是 UUID ）
     * 1、解锁之前，先判断是不是自己获取的那把锁，
     * 2、确定是一把锁就执行 解锁锁操作
     *
     * @return
     */
    public List<MenuEntity> getMenuJsonFromDbWithRedisLock() {
        List<MenuEntity> result = new ArrayList<>();
        System.out.println("缓存中没有，加锁，重新从数据中查询~==>");
        // 给锁设定一个时间
        String uuid = UUID.randomUUID().toString();
        Boolean lock = stringRedisTemplate.opsForValue().setIfAbsent(SET_NX_EX_MENU_LIST_LOCK, uuid, 5L, TimeUnit.SECONDS);
        if (lock) {
            System.out.println("获取分布式锁成功...");
            try {
                //加锁成功...执行业务
                result = menuMapper.selectList(new QueryWrapper<MenuEntity>());
                stringRedisTemplate.opsForValue().set(SET_NX_EX_MENU_LIST, JSON.toJSONString(result));
            } finally {
                // 获取锁，判断是不是当前线程的锁
                String token = stringRedisTemplate.opsForValue().get(SET_NX_EX_MENU_LIST_LOCK);
                if (uuid.equals(token)) {
                    // 确定是同一把锁， 才释放锁
                    stringRedisTemplate.delete(SET_NX_EX_MENU_LIST_LOCK);
                }
            }
            return result;
            /**
             * 那这样就没有问题了吗？
             * 并不是。
             * 这里存在的问题也很明显，删除操作已经不在是一个原子性操作了。
             * 1、一个是查询判断
             * 2、第二个才是解锁操作
             * 那么又会拆分成，如果我第一步执行成功，第二步执行失败的场景，所以我们要把它变成原子操作才行。
             */
        } else {
            System.out.println("获取分布式锁失败...等待重试...");
            //加锁失败...重试机制
            //休眠一百毫秒
            try {
                TimeUnit.MILLISECONDS.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return getMenuJsonFromDbWithRedisLock();
        }

    }




    @Override
    public Boolean updateMenuById(MenuEntity menu) {
      return updateMenuByIdWithLock(menu);
    }

    @Override
    public Boolean updateMenuById2(MenuEntity menu) {
        return updateMenuWithExpireLock(menu);
    }

    @Override
    public Boolean updateMenuById3(MenuEntity menu) {
        return updateMenuWithRedisLock(menu);
    }


    public Boolean updateMenuByIdWithLock(MenuEntity menu) {
        Boolean lock = stringRedisTemplate.opsForValue().setIfAbsent(SET_NX_EX_MENU_LIST_LOCK, SET_NX_EX_MENU_LIST_LOCK_VALUE);
        Boolean update = false;
        if (lock) {
            System.out.println("更新操作：获取分布式锁成功===>");
            // 删除缓存
            try {
                stringRedisTemplate.delete(SET_NX_EX_MENU_LIST);
                // 更新数据库
                update = menuMapper.updateById(menu) > 0;
            } finally {
                // 一定要释放锁，以免造成死锁问题
                stringRedisTemplate.delete(SET_NX_EX_MENU_LIST_LOCK);
            }
            return update;
        }else{
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            return updateMenuByIdWithLock(menu);
        }
    }

    public Boolean updateMenuWithExpireLock(MenuEntity menu) {
        // 给锁设定一个时间
        Boolean lock = stringRedisTemplate.opsForValue().setIfAbsent(SET_NX_EX_MENU_LIST_LOCK, SET_NX_EX_MENU_LIST_LOCK_VALUE, 5L, TimeUnit.SECONDS);
        Boolean update = false;
        if (lock) {
            System.out.println("更新操作：获取分布式锁成功===> 清除缓存");
            try {
                // 删除缓存
                stringRedisTemplate.delete(SET_NX_EX_MENU_LIST);
                // 更新数据库
                update = menuMapper.updateById(menu) > 0;
            } finally {
                // 一定要释放锁，以免造成死锁问题
                stringRedisTemplate.delete(SET_NX_EX_MENU_LIST_LOCK);
            }
            return update;
        }  else{
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            return updateMenuWithExpireLock(menu);
        }
    }


    public Boolean updateMenuWithRedisLock(MenuEntity menu) {
        String uuid = UUID.randomUUID().toString();
        Boolean lock = stringRedisTemplate.opsForValue().setIfAbsent(SET_NX_EX_MENU_LIST_LOCK, uuid, 5L, TimeUnit.SECONDS);
        Boolean update = false;
        if (lock) {
            System.out.println("获取分布式锁成功...");
            try {
                //加锁成功...执行业务
                stringRedisTemplate.delete(SET_NX_EX_MENU_LIST);
                // 更新数据库
                update = menuMapper.updateById(menu) > 0;
            } finally {
                // 获取锁，判断是不是当前线程的锁
                String token = stringRedisTemplate.opsForValue().get(SET_NX_EX_MENU_LIST_LOCK);
                if (uuid.equals(token)) {
                    // 确定是同一把锁， 才释放锁
                    stringRedisTemplate.delete(SET_NX_EX_MENU_LIST_LOCK);
                }
            }
            return update;
        } else{
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            return updateMenuWithRedisLock(menu);
        }
    }




}
