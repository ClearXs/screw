package com.jw.screw.provider.annotations;

import java.lang.annotation.*;

/**
 * 标识要发布服务的注解
 * @author jiangw
 * @date 2020/11/26 20:59
 * @since 1.0
*/
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE })
@Documented
@Inherited
public @interface ProviderService {

    /**
     * 对应实现的接口
     * @return
     */
    Class<?> publishService();
}
