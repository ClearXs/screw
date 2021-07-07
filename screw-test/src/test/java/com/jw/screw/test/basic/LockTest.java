package com.jw.screw.test.basic;

import org.junit.Test;

import java.util.concurrent.locks.ReentrantLock;

public class LockTest {

    @Test
    public void testReentrantLock() {
        ReentrantLock reentrantLock = new ReentrantLock();
        System.out.println(reentrantLock);
    }
}
