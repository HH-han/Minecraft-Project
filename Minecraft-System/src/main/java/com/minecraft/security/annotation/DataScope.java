package com.minecraft.security.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 数据范围过滤注解，配合 {@code DataScopeHelper.apply(wrapper)} 使用。
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DataScope {

    /** 表中用于匹配的字段名，如 dept_id / create_by / user_id */
    String field() default "dept_id";

    /** 实体别名，多表 join 时使用，如 "o" → o.dept_id */
    String tableAlias() default "";
}
