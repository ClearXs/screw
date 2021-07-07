package com.jw.screw.common.util;

import java.util.*;

/**
 * 集合一些相关操作
 * @author jiangw
 * @date 2020/12/10 17:29
 * @since 1.0
 */
public class Collections {

    public static <E> ArrayList<E> newArrayList(Iterable<? extends E> elements) {
        if (elements == null) {
            throw new NullPointerException("to array list error");
        }
        return elements instanceof Collections
                ? new ArrayList<E>((Collection<? extends E>) elements)
                : newArrayList(elements.iterator());
    }

    public static <E> ArrayList<E> newArrayList(Iterator<? extends E> elements) {
        if (elements == null) {
            throw new NullPointerException("to array list error");
        }
        ArrayList<E> arrayList = new ArrayList<>();
        while (elements.hasNext()) {
            arrayList.add(elements.next());
        }
        return arrayList;
    }

    /**
     * map -> list
     */
    public static <K, V> List<V> toList(Map<K, V> map) {
        if (isEmpty(map)) {
            return null;
        }
        Collection<V> values = map.values();
        return newArrayList(values.iterator());
    }

    public static <T> List<String> toList(T[] array) {
        List<String> list = new ArrayList<>();
        if (array == null || array.length == 0) {
            return list;
        }
        for (T t : array) {
            list.add(t.toString());
        }
        return list;
    }

    public static <E> boolean isEmpty(Collection<E> collection) {
        if (collection == null) {
            return true;
        }
        return collection.isEmpty();
    }

    public static <E> boolean isNotEmpty(Collection<E> collection) {
        return !isEmpty(collection);
    }

    public static <K, V> boolean isEmpty(Map<K, V> map) {
        if (map == null) {
            return true;
        }
        return map.isEmpty();
    }

    public static <K, V> boolean isNotEmpty(Map<K, V> map) {
        return !isEmpty(map);
    }

    /**
     * 根据对象的equals过滤数据
     */
    @SafeVarargs
    public static <V> Collection<V> filter(Collection<V> collection, V... filters) throws IllegalAccessException, InstantiationException {
        Requires.isNull(collection, "collection");
        Requires.isNull(filters, "filters");
        Collection<V> copyCollection = collection.getClass().newInstance();
        copyCollection.addAll(collection);
        Collection<V> filterCollection = collection.getClass().newInstance();
        for (V filter : filters) {
            if (collection.contains(filter)) {
                filterCollection.add(filter);
            }
        }
        copyCollection.removeAll(filterCollection);
        return copyCollection;
    }

    public static Properties mapToProperties(Map<?, ?> map) {
        Properties properties = new Properties();
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            properties.setProperty(entry.getKey().toString(), entry.getValue().toString());
        }
        return properties;
    }

    /**
     * 两个集合求并集
     */
    public static <V> boolean union(Collection<V> source, Collection<V> target) {
        if (isEmpty(target) || isEmpty(source)) {
            return false;
        }
        boolean changed = false;
        for (V o : target) {
            boolean contains = source.contains(o);
            if (!contains) {
                source.add(o);
                changed = true;
            }
        }
        return changed;
    }
}