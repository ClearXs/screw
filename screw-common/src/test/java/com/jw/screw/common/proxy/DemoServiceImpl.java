package com.jw.screw.common.proxy;

import java.util.Arrays;

public class DemoServiceImpl implements DemoService {
    @Override
    public String hello() {
        return "hello world";
    }

    @Override
    public void noResult(Object... arg) {
        System.out.println(Arrays.toString(arg));
    }

}
