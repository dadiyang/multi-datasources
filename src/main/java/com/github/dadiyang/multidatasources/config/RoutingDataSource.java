package com.github.dadiyang.multidatasources.config;

import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

/**
 * 数据源路由
 * <p>
 * 根据 DATA_SOURCE_KEY 中保存的当前线程的数据源配置来选择当前数据源
 *
 * @author dadiyang
 * @since 2019/4/18
 */
public class RoutingDataSource extends AbstractRoutingDataSource {
    private static final ThreadLocal<String> DATA_SOURCE_KEY = new ThreadLocal<>();

    /**
     * 设置当前线程数据源的 key
     */
    public static void setDataSourceKey(String key) {
        DATA_SOURCE_KEY.set(key);
    }

    /**
     * 清理 key，在访问结束后记得
     */
    public static void removeDataSourceKey() {
        DATA_SOURCE_KEY.remove();
    }

    /**
     * AbstractRoutingDataSource 会根据此方法来选择指定的数据源
     */
    @Override
    protected Object determineCurrentLookupKey() {
        return DATA_SOURCE_KEY.get();
    }
}
