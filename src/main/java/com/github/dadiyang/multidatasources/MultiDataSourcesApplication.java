package com.github.dadiyang.multidatasources;

import com.github.dadiyang.multidatasources.order.Order;
import com.github.dadiyang.multidatasources.order.OrderMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 用于演示多数据源的应用
 *
 * @author dadiyang
 * @since 2019/4/19
 */
@SpringBootApplication
public class MultiDataSourcesApplication {
    private static final Logger log = LoggerFactory.getLogger(MultiDataSourcesApplication.class);
    private static final int TABLE_COUNT = 32;

    public static void main(String[] args) {
        SpringApplication.run(MultiDataSourcesApplication.class, args);
    }

    /**
     * 这里只为演示方便，我们直接注册一个 ApplicationRunner Bean，此 Bean 的 run 方法将会在程序启动之后自动执行
     * <p>
     * 此方法用于扫表统计订单总数
     */
    @Bean
    public ApplicationRunner runner(OrderMapper orderMapper) {
        return args -> {
            AtomicLong count = new AtomicLong();
            final int pageSize = 500;
            ExecutorService executorService = Executors.newFixedThreadPool(TABLE_COUNT);
            // 假设有 32 张表，我们要遍历这 32 张表的所有数据
            for (int i = 0; i < TABLE_COUNT; i++) {
                final int table = i;
                executorService.submit(() -> {
                    try {
                        List<Order> orders = null;
                        do {
                            long lastId = 0;
                            if (orders != null) {
                                // 获取最后一条记录的id
                                lastId = orders.get(orders.size() - 1).getId();
                            }
                            orders = orderMapper.getOrders(table, lastId, pageSize);
                            count.getAndAdd(orders.size());
                            // 如果获取到的数据 >= 分页大小，说明还有下一页，继续遍历
                        } while (orders.size() >= pageSize);
                    } catch (Exception e) {
                        log.error("遍历数据出错", e);
                    }
                });
            }
            executorService.shutdown();
            executorService.awaitTermination(1, TimeUnit.HOURS);
            log.info("数据库的详单表总量为: {}", count.get());
        };
    }
}
