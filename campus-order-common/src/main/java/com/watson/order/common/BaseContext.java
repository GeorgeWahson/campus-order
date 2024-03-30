package com.watson.order.common;

/**
 * 基于ThreadLocal封装工具类，用户保存和获取当前登录用户id
 */
public class BaseContext {
    /*
        什么是ThreadLocal?
        ThreadLocal并不是一个Thread，而是Thread的局部变量。
        当使用ThreadLocal维护变量时，ThreadLocal为每个使用该变量的线程提供独立的变量副本，
        所以每一个线程都可以独立地改变自己的副本，而不会影响其它线程所对应的副本。
        ThreadLocal为每个线程提供单独一份存储空间，具有线程隔离的效果，
        只有在线程内才能获取到对应的值，线程外则不能访问。
     */
    private static final ThreadLocal<Long> THREAD_LOCAL = new ThreadLocal<>();

    public static void setCurrentId(Long id) {
        THREAD_LOCAL.set(id);
    }

    public static Long getCurrentId() {
        return THREAD_LOCAL.get();
    }

    public static void clearCurrentId() {
        THREAD_LOCAL.remove();
    }
}
