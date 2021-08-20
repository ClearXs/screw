package com.jw.screw.common;

import io.netty.util.HashedWheelTimer;

public class ExtendTest {


    abstract static class Test {

        protected abstract int weight();
    }

    public static void main(String[] args) {
        final int weight = 3;
        Test test = new Test() {

            @Override
            public int weight() {
                return weight;
            }
        };
        int weight1 = test.weight();
        System.out.println(weight1);
    }

    @org.junit.Test
    public void hashWheel() {
        for (int i = 0; i < 100; i++) {
            new HashedWheelTimer();
        }
    }
}
