package com.nzc.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.nzc.entity.MenuEntity;
import com.nzc.entity.Student;
import com.nzc.mapper.MenuMapper;
import com.nzc.service.IUseSpringCache;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * @description:
 * @author: Ning Zaichun
 * @date: 2022年09月21日 20:30
 */
@Service
@RequiredArgsConstructor
public class UseSpringCacheImpl implements IUseSpringCache {

    private final MenuMapper menuMapper;


    private static List<Student> students=new ArrayList<>();

    static {
        students.add(new Student("1", "nzc1", "xxx", 18));
        students.add(new Student("2", "nzc2", "xxx", 18));
        students.add(new Student("3", "nzc3", "xxx", 18));
        students.add(new Student("4", "nzc4", "xxx", 18));
        students.add(new Student("5", "nzc5", "xxx", 18));
        students.add(new Student("6", "nzc6", "xxx", 18));
        students.add(new Student("7", "nzc7", "xxx", 18));
        students.add(new Student("8", "nzc8", "xxx", 18));
        students.add(new Student("9", "nzc9", "xxx", 18));
    }

    @Cacheable(value = "students",key = "'pageStudent'")
    @Override
    public List<Student> page(int pageSize, int pageNumber) {
        System.out.println("查询数据库");
        List<Student> list = new ArrayList<>();
        for (int i = pageSize; i <= pageNumber; i++) {
            list.add(students.get(i));
        }
        return list;
    }

    @Cacheable(value = {"menu"}, key = "'getMenuList'")
    @Override
    public List<MenuEntity> getMenuList() {
        System.out.println("查询数据库======");
        List<MenuEntity> menuEntityList = menuMapper.selectList(new QueryWrapper<>());
        return menuEntityList;
    }

    /**
     * 级联更新所有关联的数据
     *
     * @param menuEntity
     * @CacheEvict:失效模式
     * @CachePut:双写模式，需要有返回值 1、同时进行多种缓存操作：@Caching
     * 2、指定删除某个分区下的所有数据 @CacheEvict(value = "menu",allEntries = true)
     * 3、存储同一类型的数据，都可以指定为同一分区
     */
    // @Caching(evict = {
    //         @CacheEvict(value = "category",key = "'getLevel1Categorys'"),
    //         @CacheEvict(value = "category",key = "'getCatalogJson'")
    // })
    @CacheEvict(value = "menu", allEntries = true)       //删除某个分区下的所有数据
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateMenuById(MenuEntity menuEntity) {
        System.out.println("清空缓存======");
        menuMapper.updateById(menuEntity);
    }


    @Cacheable(value = {"test"}, key = "#root.methodName")
    @Override
    public String getTest() {
        System.out.println("测试查询了数据库");
        return "我是测试缓存数据";
    }

    @Cacheable(value = {"test"}, key = "'getTest2'")
    @Override
    public String getTest2() {
        System.out.println("测试查询了数据库2");
        return "我是测试缓存数据2";
    }

    @Cacheable(value = {"test"}, key = "'getTest3'", condition = "#number>12", unless = "#number<12")
    @Override
    public String getTest3(Integer number) {
        System.out.println("测试查询了数据库2");
        return "我是测试缓存数据" + number;
    }

    @CachePut(value = {"test"}, key = "'getTest3'", condition = "#number>12", unless = "#number<12")
    @Override
    public String updateTest3(Integer number) {
        System.out.println("更新" + number + "的数据");
        return "更新缓存数据" + number;
    }

    /**
     * studentCache
     * 缓存键值key未指定默认为userNumber+userName组合字符串
     */
    @Cacheable(cacheNames = "studentCache")
    @Override
    public Student getStudentById(String id) {
        // 方法内部实现不考虑缓存逻辑，直接实现业务
        return getFromDB(id);
    }

    /**
     * 注解@CachePut:确保方法体内方法一定执行,执行完之后更新缓存;
     * 相同的缓存userCache和key(缓存键值使用spEl表达式指定为userId字符串)以实现对该缓存更新;
     *
     * @param student
     * @return 返回
     */
    @CachePut(cacheNames = "studentCache", key = "(#student.id)")
    @Override
    public Student updateStudent(Student student) {
        return updateData(student);
    }

    private Student updateData(Student student) {
        System.out.println("real updating db..." + student.getId());
        return student;
    }

    private Student getFromDB(String id) {
        System.out.println("querying id from db..." + id);
        return new Student(id, "宁在春", "社会", 19);
    }


    @Caching(evict = {
            @CacheEvict(value = "test", key = "'getTest'"),
            @CacheEvict(value = "test", key = "'getTest2'"),
            @CacheEvict(value = "test", key = "'getTest3'"),
    })
    @Override
    public void clearTest() {
        System.out.println("清空了test缓存");
    }
}
