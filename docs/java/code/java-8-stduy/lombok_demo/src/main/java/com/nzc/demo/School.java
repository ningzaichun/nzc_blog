package com.nzc.demo;

import com.nzc.my_annotation.MyGetter;
import com.nzc.my_annotation.MySetter;

/**
 * @description:
 * @author: nzc
 * @date: 2022年07月09日 11:40
 */
@MyGetter
@MySetter
public class School {

    private String name;
    private String address;

//    public static void main(String[] args) {
//        School school = new School();
//        school.setName("wuj");
//        System.out.println(school.getName());
//
//    }
//    java: java.lang.ClassCastException:
//    com.sun.proxy.$Proxy24 cannot be cast to com.sun.tools.javac.processing.JavacProcessingEnvironment
}
