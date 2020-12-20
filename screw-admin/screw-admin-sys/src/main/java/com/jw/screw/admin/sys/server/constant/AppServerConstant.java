package com.jw.screw.admin.sys.server.constant;

/**
 * 应用服务状态
 * @author jiangw
 * @date 2020/11/11 15:33
 * @since 1.0
 */
public interface AppServerConstant {

    interface State {

        /**
         * 开启
         */
        int OPEN = 0;

        /**
         * 关闭
         */
        int CLOSED = 1;

        /**
         * 异常
         */
        int EXCEPTION = 2;
    }

    interface DataOperate {

        /**
         * 获取所有的服务及其配置
         */
        String ALL = "ALL";

        /**
         * 获取默认服务即配置文件
         */
        String DEFAULT = "DEFAULT";
    }

}
