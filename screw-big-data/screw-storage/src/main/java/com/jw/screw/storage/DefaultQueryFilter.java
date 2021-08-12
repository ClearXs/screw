package com.jw.screw.storage;

/**
 * TDD
 * @author jiangw
 * @date 2021/7/22 13:57
 * @since 1.1
 */
public class DefaultQueryFilter<T> implements QueryFilter<T> {

    private final T entity;

    private int page = 1;

    private int pageSize = 100000;

    public DefaultQueryFilter(T entity) {
        this.entity = entity;
    }

    public DefaultQueryFilter(T entity, int page) {
        this.entity = entity;
        this.page = page;
    }

    public DefaultQueryFilter(T entity, int page, int pageSize) {
        this.entity = entity;
        this.page = page;
        this.pageSize = pageSize;
    }

    @Override
    public T getEntity() {
        return this.entity;
    }

    @Override
    public int getPage() {
        return this.page;
    }

    @Override
    public int getPageSize() {
        return this.pageSize;
    }
}
