package com.nzc.service;

import com.nzc.entity.MenuEntity;

import java.util.List;

/**
 * @description: setnxex 实现分布式锁
 * @author: Ning Zaichun
 * @date: 2022年09月20日 20:55
 */
public interface ISetNxExLockService {


    List<MenuEntity> getList();


    List<MenuEntity> getList2();

    List<MenuEntity> getList3();

    Boolean updateMenuById(MenuEntity menu);
    Boolean updateMenuById2(MenuEntity menu);
    Boolean updateMenuById3(MenuEntity menu);


}
