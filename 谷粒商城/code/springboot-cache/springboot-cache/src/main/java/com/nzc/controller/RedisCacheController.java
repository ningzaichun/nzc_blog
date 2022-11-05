package com.nzc.controller;

import com.nzc.entity.MenuEntity;
import com.nzc.service.ILuaLockService;
import com.nzc.service.IRedisCacheService;
import com.nzc.service.IRedissonService;
import com.nzc.service.ISetNxExLockService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @description:
 * @author: Ning Zaichun
 * @date: 2022年09月06日 22:16
 */
@RestController
@RequestMapping("/redis/cache")
public class RedisCacheController {

    @Autowired
    private IRedisCacheService redisCacheService;

    @Autowired
    private ISetNxExLockService setNxExLockService;

    @Autowired
    private ILuaLockService luaLockService;

    @Autowired
    private IRedissonService redissonService;


    @GetMapping
    public List<MenuEntity> getList() {
        return redisCacheService.getList();
    }

    @PostMapping
    public String updateMenu(MenuEntity menuEntity) {
        return "是否成功==>" + redisCacheService.updateMenuById(menuEntity);
    }


    @GetMapping("/lock")
    public List<MenuEntity> getListLock() {
        return setNxExLockService.getList();
    }

    @PostMapping("/lock")
    public String updateMenuLock(MenuEntity menuEntity) {
        return "是否成功==>" + setNxExLockService.updateMenuById(menuEntity);
    }

    @GetMapping("/lock1")
    public List<MenuEntity> getListLock1() {
        return setNxExLockService.getList();
    }

    @PostMapping("/lock1")
    public String updateMenuLock1(MenuEntity menuEntity) {
        return "是否成功==>" + setNxExLockService.updateMenuById(menuEntity);
    }


    @GetMapping("/lock2")
    public List<MenuEntity> getListLock2() {
        return luaLockService.getList();
    }

    @PostMapping("/lock2")
    public String updateMenuLock2(MenuEntity menuEntity) {
        return "是否成功==>" + luaLockService.updateMenuById(menuEntity);
    }

    @GetMapping("/lock3")
    public List<MenuEntity> getListLock3() {
        return redissonService.getList();
    }

    @PostMapping("/lock3")
    public String updateMenuLock3(MenuEntity menuEntity) {
        return "是否成功==>" + redissonService.updateMenuById(menuEntity);
    }

}
