package com.github.dadiyang.multidatasources.order;

import com.github.dadiyang.multidatasources.annotation.MultiDataSource;
import com.github.dadiyang.multidatasources.annotation.Table;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 用于演示的订单 Mapper
 *
 * @author dadiyang
 * @since 2019/4/20
 */
@Mapper
@MultiDataSource
public interface OrderMapper {
    /**
     * 分页查询指定表中的订单数据
     *
     * @param table    表号，带 @Table 注解用于多数据源判断
     * @param lastId   上一页的最后一条记录的id
     * @param pageSize 分页大小
     * @return 订单记录
     */
    List<Order> getOrders(@Table @Param("table") int table,
                          @Param("lastId") long lastId,
                          @Param("pageSize") int pageSize);

    /**
     * 分页查询指定创建时间区间的订单数据
     * <p>
     * 用于演示对象中的字段加上 @Table 注解也可以用于多数据源判断
     *
     * @param param 参数
     * @return 订单记录
     */
    List<Order> getOrdersByCreatedTime(OrderQueryParam param);
}
