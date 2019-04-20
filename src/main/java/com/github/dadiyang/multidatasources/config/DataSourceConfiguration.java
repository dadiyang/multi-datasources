package com.github.dadiyang.multidatasources.config;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 数据源配置
 *
 * @author dadiyang
 * @since 2019/4/19
 */
@Configuration
public class DataSourceConfiguration {
    /**
     * 用配置 DataSourceProperties 的方式可以避免不同数据源配置字段名称不同导致的配置项错误，如: url 和 jdbcUrl，下同
     */
    @Bean
    @Primary
    @ConfigurationProperties("datasource.ds-0")
    public DataSourceProperties firstDataSourceProperties() {
        return new DataSourceProperties();
    }

    /**
     * 配置数据源
     */
    @Bean
    @ConfigurationProperties("datasource.ds-0")
    public HikariDataSource firstDataSource() {
        return firstDataSourceProperties().initializeDataSourceBuilder()
                .type(HikariDataSource.class).build();
    }

    @Bean
    @ConfigurationProperties("datasource.ds-1")
    public DataSourceProperties secondDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    @ConfigurationProperties("datasource.ds-1")
    public HikariDataSource secondDataSource() {
        return secondDataSourceProperties()
                .initializeDataSourceBuilder()
                .type(HikariDataSource.class).build();
    }

    /**
     * 使用 DependsOn 注解，以让其他数据源先初始化完毕，否则可能出现 Bean 初始化异常
     */
    @Bean
    @Primary
    @DependsOn({"firstDataSource", "secondDataSource"})
    public RoutingDataSource routingDataSource() {
        RoutingDataSource dataSource = new RoutingDataSource();
        Map<Object, Object> dataSourceMap = new HashMap<>(8);
        // 我们自定义一个 key，这个 key 将在 RoutingDataSource 中使用
        dataSourceMap.put("ds-0", firstDataSource());
        dataSourceMap.put("ds-1", secondDataSource());
        dataSource.setTargetDataSources(dataSourceMap);
        // 可以根据需要设置默认数据源
        // 本项目中，我们不设置默认数据源，以便当出现错误时能尽快暴露出来
        dataSource.setDefaultTargetDataSource(null);
        return dataSource;
    }
}
