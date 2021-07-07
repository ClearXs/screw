package com.jw.screw.common.model;

import lombok.Data;

/**
 * 元组
 * @author jiangw
 * @date 2020/12/10 17:33
 * @since 1.0
 */
@Data
public class Tuple<K, V> {

    private K key;

    private V value;

    public Tuple(K key, V value) {
        this.key = key;
        this.value = value;
    }
}
