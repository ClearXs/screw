package com.jw.screw.common.transport;

import lombok.Data;

import java.util.Objects;

/**
 * 远程地址model
 * @author jiangw
 * @date 2020/12/10 17:31
 * @since 1.0
 */
@Data
public class RemoteAddress implements UnresolvedAddress {

    private final String host;

    private final int port;

}
