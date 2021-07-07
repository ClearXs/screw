package com.jw.screw.common.transport.body;

import com.jw.screw.common.metadata.RegisterMetadata;
import lombok.Data;

import java.util.List;

/**
 * 服务注册body
 * @author jiangw
 * @date 2020/12/10 17:30
 * @since 1.0
 */
@Data
public class RegisterBody implements Body {

    private final String serviceProviderName;

    private final List<RegisterMetadata> registerMetadata;

}