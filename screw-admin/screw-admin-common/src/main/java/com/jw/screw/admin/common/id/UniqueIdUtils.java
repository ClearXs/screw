package com.jw.screw.admin.common.id;

import com.jw.screw.admin.common.util.AppUtils;
import org.springframework.util.ObjectUtils;

/**
 * id工具类
 * @author jiangw
 * @date 2020/8/2 0:07
 * @since 1.0
 */
public class UniqueIdUtils {

    private static DefaultIdGenerator idGenerator;

    public static String getId() {
        if (ObjectUtils.isEmpty(idGenerator)) {
            init();
        }
        return idGenerator.getId();
    }

    public static long getUid() {
        return idGenerator.getUid();
    }

    public static void init() {
        idGenerator = AppUtils.getBean(DefaultIdGenerator.class);
    }
}
