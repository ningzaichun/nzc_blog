package com.nzc.config;

import org.apache.catalina.User;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @description:
 * @author: Ning Zaichun
 * @date: 2022年09月26日 20:11
 */
public class ConstantsTest {


    public static void main(String[] args) {

//        List<String> list = Arrays.asList("hello1:world1", "hello2:world2", "hello3:world3", "hello4:world4");
//
//        Map<String, String> collect = list.stream().map(l -> l.split(":")).collect(Collectors.toMap(s -> s[0], s -> s[1]));
//        System.out.println(collect);
        List<String> list = new ArrayList<>();
        List<String> list2 =null;
        System.out.println(list);
        System.out.println(list2);
    }

    public void test1() {
        MyConstants constants1 = new MyConstants("nzc1");
        MyConstants.CONSTANTS++;
        MyConstants constants2 = new MyConstants("nzc2");
        MyConstants.CONSTANTS++;
        MyConstants constants3 = new MyConstants("nzc3");
        MyConstants.CONSTANTS++;
        System.out.println(MyConstants.CONSTANTS);

        Integer aaa = Integer.valueOf("1111");
        System.out.println(aaa);
    }
}
