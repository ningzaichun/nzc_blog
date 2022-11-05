package com.nzc.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.nzc.entity.MenuEntity;
import com.nzc.mapper.MenuMapper;
import com.nzc.service.ILocalMenuService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * <p>
 * 商品三级分类 服务实现类
 * </p>
 *
 * @author Ning Zaichun
 * @since 2022-09-07
 */
@Slf4j
@Service
public class LocalMenuServiceImpl implements ILocalMenuService {

    private Map<String, Object> localCacheMap = new HashMap<String, Object>();

    private static final String LOCAL_MENU_CACHE_KEY = "local:menu:list";

    @Autowired
    private MenuMapper menuMapper;

    @Override
    public List<MenuEntity> getLocalList() {
        //1、判断本地缓存中是否存在
        List<MenuEntity> menuEntityList = (List<MenuEntity>) localCacheMap.get(LOCAL_MENU_CACHE_KEY);
        //2、本地缓存中有，就从缓存中拿
        if (menuEntityList == null) {
            //3、如果缓存中没有，就重新查询数据库
            log.info("缓存中没有，查询数据库，重新构建缓存");
            menuEntityList = menuMapper.selectList(new QueryWrapper<MenuEntity>());
            //4、从数据库查询到结果后，重新放入缓存中
            localCacheMap.put(LOCAL_MENU_CACHE_KEY, menuEntityList);
            return menuEntityList;
        }
        log.info("缓存中有直接返回");
        //5、将结果返回
        return menuEntityList;
    }

    /**
     * 更新操作
     *
     * @param menu
     * @return
     */
    @Override
    public Boolean updateLocalMenuById(MenuEntity menu) {
        //1、删除本地缓存数据
        localCacheMap.remove(LOCAL_MENU_CACHE_KEY);
        System.out.println("清空本地缓存===>");
        //2、更新数据库,根据id更新数据库中实体信息
        return menuMapper.updateById(menu) > 0;
    }

}
