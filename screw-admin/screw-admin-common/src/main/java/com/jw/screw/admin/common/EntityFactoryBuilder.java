package com.jw.screw.admin.common;

import org.springframework.beans.BeanUtils;
import org.springframework.util.ObjectUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 从VO构建成实体对象，T是实体的泛型
 * @author jiangw
 * @date 2020/11/12 16:41
 * @since 1.0
 */
public class EntityFactoryBuilder<T> {

    private Class<T> entityClass;

    private Object vo;

    public EntityFactoryBuilder<T> setEntityClass(Class<T> entityClass) {
        this.entityClass = entityClass;
        return this;
    }

    public EntityFactoryBuilder<T> setVo(Object vo) {
        this.vo = vo;
        return this;
    }

    public T build() throws IllegalAccessException, InstantiationException {
        if (vo == null) {
            throw new NullPointerException("未设置vo对象");
        }
        if (entityClass == null) {
            throw new NullPointerException("未设置实体对象Class");
        }
        // 构建实体对象
        T entity = entityClass.newInstance();
        BeanUtils.copyProperties(vo, entity);
        return entity;
    }

    public List<T> build(Object[] vos) throws InstantiationException, IllegalAccessException {
        if (ObjectUtils.isEmpty(vos)) {
            throw new NullPointerException("未设置vo对象");
        }
        List<T> entities = new ArrayList<>();
        for (Object vo : vos) {
            setVo(vo);
            entities.add(build());
        }
        return entities;
    }
}
