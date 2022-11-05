package com.nzc.service;

import com.nzc.entity.MenuEntity;

import java.util.List;

/**
 * @description:
 * @author: Ning Zaichun
 * @date: 2022年09月06日 23:20
 */
public interface IRedisCacheService  {


    /**
     * 获取全部的数据
     * @return
     */
    List<MenuEntity> getList();


    /**
     * 更新 数据
     * @return
     */
    Boolean updateMenuById(MenuEntity menu);




}
