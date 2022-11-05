package com.nzc.controller;

import com.nzc.entity.MenuEntity;
import com.nzc.service.ILocalMenuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @description: 本地缓存模拟
 * @author: Ning Zaichun
 * @date: 2022年09月06日 22:16
 */
@RestController
@RequestMapping("/local/cache")
public class LocalCacheController {

    @Autowired
    private ILocalMenuService localMenuService;

    @GetMapping
    public List<MenuEntity> getList() {
        return localMenuService.getLocalList();
    }

    @PostMapping
    public String updateMenu(MenuEntity menuEntity) {
        return "是否成功==>" + localMenuService.updateLocalMenuById(menuEntity);
    }

}
