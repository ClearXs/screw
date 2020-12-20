package com.jw.screw.admin.sys.config.constant;

public interface ConfigDataConstant {

    interface DataOperation {

        /**
         * 发布配置数据
         */
        String DEPLOY = "DEPLOY";

        /**
         * 保存配置数据
         */
        String SAVE = "SAVE";

        /**
         * 修改配置数据
         */
        String EDIT = "EDIT";
    }

    /**
     * 存储的数据类型
     */
    interface DataType {

        /**
         * 从hbp获取
         */
        String HBP = "HBP";

        /**
         * 文件中获取
         */
        String FILE = "FILE";

        /**
         * 自定义设置
         */
        String CUSTOM = "CUSTOM";
    }

    /**
     * 数据的存储状态
     */
    interface DataStoreState {

        /**
         * 发布
         */
        String DEPLOY = "DEPLOY";

        /**
         * 存储
         */
        String SAVE = "SAVE";

        /**
         * 暂存
         */
        String SAVE_TEMP = "SAVE_TEMP";
    }
}
