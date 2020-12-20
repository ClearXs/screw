package com.jw.screw.remote;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * 一些关于网络的操作
 * @author jiangw
 * @date 2020/12/9 19:52
 * @since 1.0
 */
public class Remotes {

    private static Logger logger = LoggerFactory.getLogger(Remotes.class);

    /**
     * 判断目标是否可以连接
     * @param host
     * @param port
     * @param timeout
     * @return
     * @throws IOException
     */
    public static boolean connectable(String host, int port, int timeout) throws IOException {
        boolean isConnected = true;
        Socket socket = new Socket();
        try {
            socket.connect(new InetSocketAddress(host, port), timeout);
        } catch (IOException e) {
            logger.warn("connect host:[{}] port:[{}] failed", host, port);
            isConnected = false;
        } finally {
            socket.close();
        }
        return isConnected;
    }
}
