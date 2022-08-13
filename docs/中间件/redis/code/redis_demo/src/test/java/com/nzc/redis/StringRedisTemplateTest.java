package com.nzc.redis;

import com.alibaba.fastjson.JSONObject;
import com.nzc.redis.entity.LoginUser;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.connection.stream.Record;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @description:
 * @author: Yihui Wang
 * @date: 2022年07月16日 14:29
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class StringRedisTemplateTest {


    @Autowired
    private StringRedisTemplate stringRedisTemplate;


    /**
     * set k1 v1
     * get k1 v1
     */
    @Test
    public void test1(){
        stringRedisTemplate.opsForValue().set("k1","stringRedisTemplate1");
        System.out.println(stringRedisTemplate.opsForValue().get("k1"));
    }

    /**
     * set k1 v1
     * get k1 v1
     */
    @Test
    public void test2(){
        LoginUser loginUser = new LoginUser();
        loginUser.setUsername("宁在春");
        loginUser.setPassword("admins");
        stringRedisTemplate.opsForValue().set("k1", JSONObject.toJSONString(loginUser));
        System.out.println(stringRedisTemplate.opsForValue().get("k1"));
    }


    /**
     *  HMSET nzcKey
     *   name "宁在春"
     *   description "一位普普通通的创作者"
     */
    @Test
    public void testHash(){
        stringRedisTemplate.opsForHash().put("nzcKey","name","宁在春");
        stringRedisTemplate.opsForHash().put("nzcKey","description","一位普普通通的创作者");
        System.out.println(stringRedisTemplate.opsForHash().get("nzcKey","name"));
    }

    /**
     *  LPUSH listKEY redis
     *  LPUSH listKEY mongodb
     *  LPUSH listKEY mysql
     *  LRANGE listKEY 0 10
     */
    @Test
    public void testList(){
        stringRedisTemplate.opsForList().leftPush("listKEY","redis");
        stringRedisTemplate.opsForList().leftPush("listKEY","mongodb");
        stringRedisTemplate.opsForList().leftPush("listKEY","mysql");
        List<String> listKEY = stringRedisTemplate.opsForList().range("listKEY", 0L, 10L);
        listKEY.forEach(s-> System.out.println(s));
    }


    /**
     * SADD setKey redis
     * SADD setKey mongodb
     * SADD setKey mysql
     * SADD setKey mysql
     * SMEMBERS setKey
     */
    @Test
    public void testSet(){
        stringRedisTemplate.opsForSet().add("setKey","redis","mongodb","mysql","mysql");
    }


    /**
     * redis> XADD mystream * name Sara surname OConnor
     * "1601372323627-0"
     * redis> XADD mystream * field1 value1 field2 value2 field3 value3
     * "1601372323627-1"
     * redis> XLEN mystream
     * (integer) 2
     * redis> XRANGE mystream - +
     */
    @Test
    public void testStream(){
        Map<String, Object> map = new HashMap<>();
        map.put("name","Sara");
        map.put("surname","OConnor");
        map.put("field1","value1");
        map.put("field2","value2");
        map.put("field3","value3");
        stringRedisTemplate.opsForStream().add("mystream",map);
    }

    /**
     * redis> GEOADD Sicily 13.361389 38.115556 "Palermo" 15.087269 37.502669 "Catania"
     * (integer) 2
     * redis> GEODIST Sicily Palermo Catania
     * "166274.1516"
     * redis> GEORADIUS Sicily 15 37 100 km
     * 1) "Catania"
     * redis> GEORADIUS Sicily 15 37 200 km
     * 1) "Palermo"
     * 2) "Catania"
     */
    @Test
    public void testGeo(){
        Point point1 = new Point(13.361389,38.115556);
        Point point2 = new Point(15.087269,37.502669);
        stringRedisTemplate.opsForGeo().add("geoKey",point1,"nzc1");
        stringRedisTemplate.opsForGeo().add("geoKey",point2,"nzc2");
        Distance distance = stringRedisTemplate.opsForGeo().distance("geoKey", "nzc1", "nzc2");
        System.out.println(distance.toString());
    }




}
