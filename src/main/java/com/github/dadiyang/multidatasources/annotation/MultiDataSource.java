package com.github.dadiyang.multidatasources.annotation;

import java.lang.annotation.*;

/**
 * 指定某个类或方法使用商机数据源，并可以根据给定的带有 Table 注解的表号参数定位具体的库和表
 *
 * @author dadiyang
 * @since 2019/4/19
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface MultiDataSource {
}
