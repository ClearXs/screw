package com.jw.screw.storage;

/**
 * 查询过滤
 * @author jiangw
 * @date 2021/7/22 13:55
 * @since 1.1
 */
public interface QueryFilter<T> {

    /**
     * 实体查询对象
     */
    T getEntity();

    /**
     * 获取页面
     */
    default int getPage() {
        return 1;
    }

    /**
     * 页面大小
     */
    default int getPageSize() {
        return 1000;
    }

    /**
     * 页的偏移位置
     */
    default int getOffset() {
        return (getPage() -1 ) * getPageSize();
    }
}
