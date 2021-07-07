package com.jw.screw.spring;

import com.jw.screw.common.exception.RemoteException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContextAware;

import java.io.IOException;

/**
 * 一个screw适配spring的类型
 * @author jiangw
 * @date 2020/12/31 15:02
 * @since 1.0
 */
public interface ScrewSpring extends InitializingBean, ApplicationContextAware, DisposableBean {

    /**
     * 初始化配置
     * @throws IOException
     * @throws InterruptedException
     * @throws RemoteException
     */
    void initConfig() throws IOException, InterruptedException, RemoteException;

    /**
     * 验证参数
     */
    void validateParams();
}
