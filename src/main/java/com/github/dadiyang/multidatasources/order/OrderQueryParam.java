package com.github.dadiyang.multidatasources.order;

import com.github.dadiyang.multidatasources.annotation.Table;

import java.util.Date;

/**
 * 订单查询条件
 *
 * @author dadiyang
 * @since 2019/4/20
 */
public class OrderQueryParam {
    /**
     * 表号，带 @Table 注解用于多数据源判断
     */
    @Table
    private int table;
    private long lastId;
    private int pageSize;
    private Date beginTime;
    private Date endTime;

    public int getTable() {
        return table;
    }

    public void setTable(int table) {
        this.table = table;
    }

    public long getLastId() {
        return lastId;
    }

    public void setLastId(long lastId) {
        this.lastId = lastId;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public Date getBeginTime() {
        return beginTime;
    }

    public void setBeginTime(Date beginTime) {
        this.beginTime = beginTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }
}
