package com.nzc.config;

/**
 * @description:
 * @author: Ning Zaichun
 * @date: 2022年09月26日 20:09
 */
public class MyConstants {

    private String name;

    public MyConstants(String name) {
        this.name = name;
    }

    static {
        System.out.println("我是测试多次new对象是否会加载多次的代码");
    }

    public  static Integer CONSTANTS=1;
}
