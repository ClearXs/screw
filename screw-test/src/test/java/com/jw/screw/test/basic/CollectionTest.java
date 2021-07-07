package com.jw.screw.test.basic;

import com.jw.screw.common.util.Collections;
import org.junit.Test;

import java.util.*;
import java.util.concurrent.CopyOnWriteArraySet;

public class CollectionTest {

    @Test
    public void testToArray() {
        List<String> list = Arrays.asList("21", "2321");
        String[] strings = list.toArray(new String[0]);
        for (String string : strings) {
            System.out.println(string);
        }
    }

    @Test
    public void testRemove() {
        List<String> strings = Arrays.asList("21", "33232");

        for (String string : strings) {
            System.out.println(string);
            strings.remove(string);
        }

        System.out.println(strings);
    }

    @Test
    public void testToList() {
        Map<String, Object> map = new HashMap<>();
        map.put("213", "212");
        map.put("21223", "212");
        List<Object> objects = Collections.toList(map);
        System.out.println(objects);
    }

    @Test
    public void testFilter() throws InstantiationException, IllegalAccessException {
        String s1 = "s1";
        String s2 = "s2";
        String s3 = "s3";
        List<String> strings1 = new ArrayList<>(Arrays.asList(s1, s2));
        Collection<String> filter = Collections.filter(strings1, s1, s3);
        System.out.println(filter);

    }

    @Test
    public void testCopyOnWriteSet() {
        CopyOnWriteArraySet<String> strings = new CopyOnWriteArraySet<>();
        strings.add("2");
        boolean add = strings.add("2");
        int size = strings.size();
        System.out.println(size);
    }
}
