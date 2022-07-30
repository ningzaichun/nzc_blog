//package com.nzc.redis.utils;
//
//import cn.hutool.core.lang.Assert;
//import com.alibaba.fastjson.JSON;
//import com.alibaba.fastjson.parser.ParserConfig;
//import com.alibaba.fastjson.serializer.SerializerFeature;
//import com.fasterxml.jackson.annotation.JsonAutoDetect;
//import com.fasterxml.jackson.annotation.PropertyAccessor;
//import com.fasterxml.jackson.databind.JavaType;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
//import com.fasterxml.jackson.databind.type.TypeFactory;
//import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
//import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
//import org.springframework.boot.context.properties.EnableConfigurationProperties;
//import org.springframework.cache.annotation.EnableCaching;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.data.redis.cache.RedisCacheConfiguration;
//import org.springframework.data.redis.cache.RedisCacheManager;
//import org.springframework.data.redis.cache.RedisCacheWriter;
//import org.springframework.data.redis.connection.RedisConnectionFactory;
//import org.springframework.data.redis.core.RedisOperations;
//import org.springframework.data.redis.core.RedisTemplate;
//import org.springframework.data.redis.core.StringRedisTemplate;
//import org.springframework.data.redis.serializer.RedisSerializer;
//import org.springframework.data.redis.serializer.SerializationException;
//import org.springframework.data.redis.serializer.StringRedisSerializer;
//
//import java.nio.charset.Charset;
//import java.nio.charset.StandardCharsets;
//
///**
// * @description:
// * @author: Yihui Wang
// * @date: 2022年07月16日 14:30
// */
//@EnableCaching
//@Configuration
//@ConditionalOnClass(RedisOperations.class)
//@EnableConfigurationProperties(RedisProperties.class)
//public class RedisConfig {
//
//    private final FastJson2JsonRedisSerializer<Object> serializer = new FastJson2JsonRedisSerializer<>(Object.class);
//    {
//        ObjectMapper mapper = new ObjectMapper();
//        mapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
//        mapper.activateDefaultTyping(LaissezFaireSubTypeValidator.instance, ObjectMapper.DefaultTyping.NON_FINAL);
//        serializer.setObjectMapper(mapper);
//    }
//
//    /**
//     * redis 命名空间
//     */
//    public static final String REDIS_KEY_DATABASE = "SIAEStudio";
//
////    @Bean
////    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
////        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
////
////        StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();
////
////        //设置value hashValue值的序列化
////        redisTemplate.setValueSerializer(serializer);
////        redisTemplate.setHashValueSerializer(serializer);
////        //key hasKey的序列化
////        redisTemplate.setKeySerializer(stringRedisSerializer);
////        redisTemplate.setHashKeySerializer(stringRedisSerializer);
////
////        redisTemplate.setConnectionFactory(redisConnectionFactory);
////        redisTemplate.afterPropertiesSet();
////        return redisTemplate;
////    }
//
////    @Bean
////    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory redisConnectionFactory) {
////        StringRedisTemplate stringRedisTemplate = new StringRedisTemplate();
////        stringRedisTemplate.setConnectionFactory(redisConnectionFactory);
////        return stringRedisTemplate;
////    }
//
//    private static class FastJson2JsonRedisSerializer<T> implements RedisSerializer<T> {
//        private ObjectMapper objectMapper = new ObjectMapper();
//        private static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;
//
//        private Class<T> clazz;
//
//        static {
//            //如果遇到反序列化autoType is not support错误，请添加并修改一下包名到bean文件路径
//            // ParserConfig.getGlobalInstance().addAccept("com.xxxxx.xxx");
//            ParserConfig.getGlobalInstance().setAutoTypeSupport(true);
//        }
//
//        private FastJson2JsonRedisSerializer(Class<T> clazz) {
//            super();
//            this.clazz = clazz;
//        }
//
//        @Override
//        public byte[] serialize(T t) throws SerializationException {
//            if (t == null) {
//                return new byte[0];
//            }
//            return JSON.toJSONString(t, SerializerFeature.WriteClassName).getBytes(DEFAULT_CHARSET);
//        }
//
//        @Override
//        public T deserialize(byte[] bytes) throws SerializationException {
//            if (bytes == null || bytes.length <= 0) {
//                return null;
//            }
//            String str = new String(bytes, DEFAULT_CHARSET);
//
//            return JSON.parseObject(str, clazz);
//        }
//
//        protected JavaType getJavaType(Class<?> clazz) {
//            return TypeFactory.defaultInstance().constructType(clazz);
//        }
//
//        public void setObjectMapper(ObjectMapper objectMapper) {
//            Assert.notNull(objectMapper, "'objectMapper' must not be null");
//            this.objectMapper = objectMapper;
//        }
//    }
//}
