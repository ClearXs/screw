package com.jw.screw.admin.common;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jw.screw.admin.common.model.OrderColumn;
import com.jw.screw.admin.common.model.PageParams;
import com.jw.screw.admin.common.model.PageResult;

import java.util.ArrayList;
import java.util.List;

/**
 * 一个用于构建分页的工厂
 * @author jiangw
 * @date 2020/11/13 15:07
 * @since 1.0
 */
public class PageFactoryBuilder<T> {

    private PageParams pageParams;

    public PageFactoryBuilder<T> setPageParams(PageParams pageParams) {
        this.pageParams = pageParams;
        return this;
    }

    public Page<T> buildPage() {
        if (BeanUtil.isEmpty(pageParams)) {
            throw new NullPointerException("分页参数为空");
        }
        Page<T> page = new Page<>();
        // 1.设置当前页
        page.setCurrent(pageParams.getPageNum());
        // 2.设置页面大小
        page.setSize(pageParams.getPageSize());
        // 3.判断是否要查询所有的数目
        if (!BeanUtil.isEmpty(pageParams.getSearchCount())) {
            page.setSearchCount(pageParams.getSearchCount());
        }
        // 4.设置排序的字段
        if (!BeanUtil.isEmpty(pageParams.getOrders())) {
            List<OrderItem> orderItems = new ArrayList<>();
            List<OrderColumn> orders = pageParams.getOrders();
            for (OrderColumn order : orders) {
                // true asc
                if (order.getAsc()) {
                    orderItems.add(OrderItem.asc(order.getOrderColumn()));
                    // false desc
                } else{
                    orderItems.add(OrderItem.desc(order.getOrderColumn()));
                }
            }
            page.setOrders(orderItems);
        }
        return page;
    }

    /**
     * 在查询完之后build
     */
    public PageResult<T> buildPageResult(Page<T> page) {
        if (BeanUtil.isEmpty(page)) {
            throw new NullPointerException("分页对象为空");
        }
        PageResult<T> pageResult = new PageResult<>();
        // 设置分页数
        pageResult.setPageNum(page.getCurrent());
        // 设置分页大小
        pageResult.setPageSize(page.getSize());
        // 设置分页总数
        pageResult.setTotal(page.getTotal());
        // 设置数据
        pageResult.setList(page.getRecords());
        return pageResult;
    }
}
