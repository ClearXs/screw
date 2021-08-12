package com.jw.screw.common.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;

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
     * @param host 主机ip
     * @param port 端口
     * @param timeout 连接的超时时间
     * @return 是否连接成功
     * @throws IOException
     */
    public static boolean connectable(String host, int port, int timeout) throws IOException {
        boolean isConnected = true;
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(host, port), timeout);
        } catch (IOException e) {
            logger.warn("connect host:[{}] port:[{}] failed", host, port);
            isConnected = false;
        }
        return isConnected;
    }

    /**
     * 获取当前机器的地址
     * @return address
     */
    public static String getHost() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        return "localhost";
    }
}
