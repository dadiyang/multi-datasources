package com.github.dadiyang.multidatasources.annotation;

import java.lang.annotation.*;

/**
 * 指定某个参数或字段为具体表号，仅限 int 类型
 *
 * @author dadiyang
 * @since 2019/4/19
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PARAMETER, ElementType.FIELD})
public @interface Table {
}
