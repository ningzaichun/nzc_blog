package com.nzc.service.impl;

import com.nzc.entity.Student;
import org.springframework.cache.annotation.*;
import org.springframework.stereotype.Service;

/**
 * @description:
 * @author: Ning Zaichun
 * @date: 2022年10月23日 13:35
 */
@CacheConfig(cacheNames = "students")
@Service
public class StudentServiceImpl {

    @Cacheable
    public Student getStudents() {
        return new Student("1","1","1",12);
    }
    @Cacheable(key = "#id")
    public Student getStudentById(String id) {
        return new Student("1","1","1",12);
    }
    @CachePut( key = "(#student.id)")
    public Student updateStudent(Student student) {
        return new Student("1","1","1",19);
    }
    @CacheEvict
    public void  delete() {
    }
}
