package com.jw.screw.common.model;

/**
 * 元组
 * @author jiangw
 * @date 2020/12/10 17:33
 * @since 1.0
 */
public class Tuple<K, V> {

    private K key;

    private V value;

    public Tuple(K key, V value) {
        this.key = key;
        this.value = value;
    }

    public K getKey() {
        return key;
    }

    public void setKey(K key) {
        this.key = key;
    }

    public V getValue() {
        return value;
    }

    public void setValue(V value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "Tuple{" +
                "key=" + key +
                ", value=" + value +
                '}';
    }
}
