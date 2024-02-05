package com.watson.order.aop;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// 修改原有的注解，让它们实现共同的接口
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface EmpLog {
}