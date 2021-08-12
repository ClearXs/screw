package com.jw.screw.storage;

import org.junit.Test;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public class GenericType {

    @Test
    public void test() {
        Type type = AbstractMemoryGenericRecoder.class.getGenericSuperclass();
        if (type instanceof ParameterizedType) {
            for (Type actualTypeArgument : ((ParameterizedType) type).getActualTypeArguments()) {
                System.out.println(actualTypeArgument);
            }
        }
    }
}
