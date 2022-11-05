package com.nzc.controller;

import com.nzc.entity.MenuEntity;
import com.nzc.entity.Student;
import com.nzc.service.*;
import lombok.RequiredArgsConstructor;
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
@RequestMapping("/cache")
@RequiredArgsConstructor
public class CacheController {

    private final IUseSpringCache useSpringCache;

    @GetMapping("/page")
    public List<Student> page(Integer pageSize,Integer pageNumber){
        return useSpringCache.page(pageSize,pageNumber);
    }


    @GetMapping("/student")
    public Student getStudent() {
        return useSpringCache.getStudentById("1");
    }


    @GetMapping("/updateStu")
    public Student updateStu(){
         return useSpringCache.updateStudent(new Student("1","我是宁在春","xxx",22));
    }

    @GetMapping("/test")
    public String getTest() {
        return useSpringCache.getTest();
    }


    @GetMapping("/test2")
    public String getTest2() {
        return useSpringCache.getTest2();
    }

    @GetMapping("/test3")
    public String getTest2(Integer number) {
        return useSpringCache.getTest3(number);
    }


    @GetMapping("/update3")
    public String update3(Integer number) {
        return useSpringCache.updateTest3(number);
    }



    @GetMapping("/test/clear")
    public String clearTest() {
        useSpringCache.clearTest();
        return "clearTest";
    }

    @GetMapping
    public List<MenuEntity> getMenuList() {
        return useSpringCache.getMenuList();
    }

    @GetMapping("/clear")
    public String updateMenu() {
        MenuEntity menuEntity = new MenuEntity();
        menuEntity.setCatId(33L);
        menuEntity.setName("其他测试数据");
        useSpringCache.updateMenuById(menuEntity);
        return "成功清空缓存";
    }


}
