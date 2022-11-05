package com.nzc;

import com.nzc.demo.HandlerApplication;
import com.nzc.demo.entity.HandlerTest;
import com.nzc.demo.service.IHandlerTestService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;
import java.util.List;

/**
 * @description:
 * @author: Yihui Wang
 * @date: 2022年08月08日 21:42
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = HandlerApplication.class)
public class HandlerServiceTest {


    @Autowired
    IHandlerTestService handlerTestService;

    @Test
    public void test1(){
        List<HandlerTest> list = handlerTestService.list();
        list.forEach(System.out::println);
    }

    @Test
    public void test2(){
        HandlerTest handlerTest = new HandlerTest();
        handlerTest.setDate(new Date());
        handlerTest.setName("测试插入数据");
        handlerTestService.save(handlerTest);
        List<HandlerTest> list = handlerTestService.list();
        list.forEach(System.out::println);
    }

}
