package com.nzc.service;

import com.nzc.entity.MenuEntity;

import java.util.List;

/**
 * @description: setnxex 实现分布式锁
 * @author: Ning Zaichun
 * @date: 2022年09月20日 20:55
 */
public interface ILuaLockService {


    List<MenuEntity> getList();


    Boolean updateMenuById(MenuEntity menu);



}
