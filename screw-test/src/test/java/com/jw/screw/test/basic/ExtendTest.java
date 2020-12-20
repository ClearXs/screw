package com.jw.screw.test.basic;

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
}
