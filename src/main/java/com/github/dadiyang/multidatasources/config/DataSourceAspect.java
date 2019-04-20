package com.github.dadiyang.multidatasources.config;

import com.github.dadiyang.multidatasources.annotation.Table;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

/**
 * 数据源切面，用于根据指定的表号定位具体的数据库和表号
 *
 * @author dadiyang
 * @since 2019/4/19
 */
@Component
@Aspect
public class DataSourceAspect {
    private static final Logger log = LoggerFactory.getLogger(DataSourceAspect.class);
    /**
     * 每个库的表数量
     */
    @Value("${datasource.table-per-db:16}")
    private int tablePerDb = 16;

    /**
     * 定义切点：类或方法上打了 MultiDataSource 注解
     */
    @Pointcut("@within(com.github.dadiyang.multidatasources.annotation.MultiDataSource) || @annotation(com.github.dadiyang.multidatasources.annotation.MultiDataSource)")
    public void aspect() {
    }

    @Around("aspect()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        // 通过连接点信息获取数据源 key，若获取报错，则应视为方法执行失败
        String key = getDataSourceKey(joinPoint);
        // 设置数据源，若 key 为空则会选择默认的
        RoutingDataSource.setDataSourceKey(key);
        try {
            // 执行切点方法
            return joinPoint.proceed(joinPoint.getArgs());
        } finally {
            // 移除数据源选择
            RoutingDataSource.removeDataSourceKey();
        }
    }

    private String getDataSourceKey(ProceedingJoinPoint joinPoint) throws IllegalAccessException {
        Signature signature = joinPoint.getSignature();
        String key = null;
        if (signature instanceof MethodSignature) {
            Method method = ((MethodSignature) signature).getMethod();
            Parameter[] parameters = method.getParameters();
            int table = -1;
            for (int i = 0; i < parameters.length; i++) {
                Parameter parameter = parameters[i];
                Object arg = joinPoint.getArgs()[i];
                // 找到带有 Table 注解的参数
                Integer tableIndex = null;
                if (parameter.isAnnotationPresent(Table.class)) {
                    // 根据指定的表号定位具体的数据库和表号
                    table = (int) arg;
                    if (table >= 0) {
                        // 定位表号
                        tableIndex = locateTableIndex(table);
                        // 使用定位到的表号
                        joinPoint.getArgs()[i] = tableIndex;
                    }
                } else {
                    // 参数没有带 Table 注解，则解析参数对象的字段是否包含该注解
                    table = getTableFromObjectParam(parameter.getType(), arg);
                    if (table >= 0) {
                        // 定位到的表号
                        tableIndex = locateTableIndex(table);
                    }
                }
                // 指定表号则指定数据源
                if (tableIndex != null) {
                    // 根据定位的库选择 key
                    key = "ds-" + table / tablePerDb;
                    log.info("指定表号: {}, 定位到数据库: {}, 定位到表号: {}", table, key, tableIndex);
                    break;
                }
            }
        }
        return key;
    }

    private int getTableFromObjectParam(Class<?> paramType, Object arg) throws IllegalAccessException {
        if (!paramType.isPrimitive()) {
            // 检查字段是否包含带有 table 注解
            Field[] fields = paramType.getDeclaredFields();
            for (Field field : fields) {
                // 获取当前参数带有 Table 注解字段的值
                if (field.isAnnotationPresent(Table.class)) {
                    field.setAccessible(true);
                    // 获取表号
                    int table = (int) field.get(arg);
                    int tableIndex = locateTableIndex(table);
                    // 使用定位到的表号
                    field.set(arg, tableIndex);
                    return table;
                }
            }
        }
        return -1;
    }

    /**
     * 定位表号
     */
    private int locateTableIndex(int table) {
        return table % tablePerDb;
    }

    public void setTablePerDb(int tablePerDb) {
        this.tablePerDb = tablePerDb;
    }
}
