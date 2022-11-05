package com.nzc.service;

import com.nzc.entity.MenuEntity;
import com.nzc.entity.Student;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;

import java.util.List;

/**
 * @description:
 * @author: Ning Zaichun
 * @date: 2022年09月21日 20:30
 */
public interface IUseSpringCache {
    List<Student> page(int pageSize, int pageNumber);

    String getTest();

    String getTest2();

     String getTest3(Integer number);

    String updateTest3(Integer number);
    Student getStudentById(String id);


     Student updateStudent(Student student) ;


    void clearTest();

    List<MenuEntity> getMenuList();

    void updateMenuById(MenuEntity menuEntity);


}
