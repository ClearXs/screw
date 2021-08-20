package com.jw.screw.common;

import org.junit.Test;

import java.util.concurrent.locks.ReentrantLock;

public class LockTest {

    @Test
    public void testReentrantLock() {
        ReentrantLock reentrantLock = new ReentrantLock();
        System.out.println(reentrantLock);
    }
}
