package com.jw.screw.common;

import org.junit.Test;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class PortTest {

    @Test
    public void testPort() throws UnknownHostException {
        String hostAddress = InetAddress.getLocalHost().getHostAddress();
        System.out.println(hostAddress);
    }
}
