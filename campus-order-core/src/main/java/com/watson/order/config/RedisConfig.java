package com.watson.order.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/*
    序列化器
 */
@Configuration
public class RedisConfig {

    @Bean
    public GenericJackson2JsonRedisSerializer genericJackson2JsonRedisSerializer() {
        ObjectMapper om = new ObjectMapper();
        // 解决查询缓存转换异常的问题
        om.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);  // 不将日期以时间戳的形式写入
        om.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);  // 允许访问所有属性
//        om.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);  // 启用默认类型推断
        om.activateDefaultTyping(LaissezFaireSubTypeValidator.instance, ObjectMapper.DefaultTyping.NON_FINAL); // 使用LaissezFaireSubTypeValidator
        // 支持 jdk 1.8 日期   ---- start ---
        om.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        om.registerModule(new Jdk8Module())
                .registerModule(new JavaTimeModule())
                .registerModule(new ParameterNamesModule());
        // --end --
        return new GenericJackson2JsonRedisSerializer(om);
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory);

//        //LocalDatetime序列化
//        JavaTimeModule timeModule = new JavaTimeModule();
//        timeModule.addDeserializer(LocalDate.class,
//                new LocalDateDeserializer(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
//        timeModule.addDeserializer(LocalDateTime.class,
//                new LocalDateTimeDeserializer(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
//        timeModule.addSerializer(LocalDate.class,
//                new LocalDateSerializer(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
//        timeModule.addSerializer(LocalDateTime.class,
//                new LocalDateTimeSerializer(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));


        // 使用StringRedisSerializer来序列化和反序列化redis的key值
        template.setKeySerializer(new StringRedisSerializer());

        // 使用Jackson2JsonRedisSerializer来序列化和反序列化redis的value值
        // 将对象序列化成JSON字符串存储到Redis中，同时配置了一个ObjectMapper来进行自定义的序列化设置
        template.setValueSerializer(genericJackson2JsonRedisSerializer());

        // Hash的key也采用String的序列化方式
        template.setHashKeySerializer(new StringRedisSerializer());

        // Hash的value序列化方式同value，采用Jackson2JsonRedisSerializer
//        template.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());
        template.setHashValueSerializer(genericJackson2JsonRedisSerializer());

        // 设置完序列化策略后，需要调用afterPropertiesSet()方法，否则序列化策略不会生效
        // 但是在Spring Boot中，这一步是自动完成的，所以不需要显式调用
        // template.afterPropertiesSet();

        return template;
    }
}