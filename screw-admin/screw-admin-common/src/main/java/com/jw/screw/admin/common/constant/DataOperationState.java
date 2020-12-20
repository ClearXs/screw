package com.jw.screw.admin.common.constant;

/**
 * 针对数据库操作的状态值
 * @author jiangw
 * @date 2020/11/13 14:19
 * @since 1.0
 */
public interface DataOperationState {

    /**
     * 只要数据操作大于0，就是成功的状态
     */
    Integer SUCCESSFUL = 1;

    /**
     * 相反小于的0的状态
     */
    Integer FAILURE = 0;
}
