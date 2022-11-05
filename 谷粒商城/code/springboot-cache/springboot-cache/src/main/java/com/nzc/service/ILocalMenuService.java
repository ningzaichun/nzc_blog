package com.nzc.service;

import com.nzc.entity.MenuEntity;
import com.baomidou.mybatisplus.extension.service.IService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;


/**
 * <p>
 * 菜单Service
 * </p>
 *
 * @author Ning Zaichun
 * @since 2022-09-07
 */
public interface ILocalMenuService{


    /**
     * 获取全部菜单，并组装成树形
     * @return
     */
    List<MenuEntity> getLocalList();


    /**
     * 根据ID修改信息
     * @param menu
     * @return
     */
    Boolean updateLocalMenuById(MenuEntity menu);
}
