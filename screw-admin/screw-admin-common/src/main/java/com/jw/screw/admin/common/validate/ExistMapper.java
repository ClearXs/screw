package com.jw.screw.admin.common.validate;

/**
 * 对于所有需要判断存在的都需要继承这个接口
 * @author jiangw
 * @date 2020/11/20 10:52
 * @since 1.0
 */
@FunctionalInterface
public interface ExistMapper {

    /**
     * 由mapper继承，传动态的where sql语句
     * @param whereSql 如: where name = '21'
     * @return >0 存在，否则不存在
     */
    int isExist(String whereSql);
}
