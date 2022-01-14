package com.lianwenhong.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 绑定视图注解
 */
@Target(ElementType.FIELD) // 声明注解的作用范围是变量
@Retention(RetentionPolicy.CLASS) // 声明注解保留的声明周期为保留到编译期
public @interface BindView {
    int value();
}
