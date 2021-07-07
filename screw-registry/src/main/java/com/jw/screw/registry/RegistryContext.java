package com.jw.screw.registry;

import com.jw.screw.common.metadata.RegisterMetadata;
import com.jw.screw.common.metadata.ServiceMetadata;
import com.jw.screw.common.model.MessageNonAck;
import com.jw.screw.common.transport.UnresolvedAddress;
import io.netty.channel.Channel;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * 存放注册中心一些需要切换上下文的对象
 * 避免循环引用
 * @author jiangw
 * @date 2020/11/30 16:13
 * @since 1.0
 */
public class RegistryContext {

    /**
     * 服务提供者通道列表
     * key: 服务提供者的地址
     * value: 通道
     */
    private final ConcurrentHashMap<UnresolvedAddress, Channel> registerChannels = new ConcurrentHashMap<>();

    /**
     * 消费者订阅的服务
     * key：某个服务提供者的名字
     * value：订阅这个服务消费者的通道
     */
    private final ConcurrentHashMap<String, CopyOnWriteArraySet<Channel>> subscribeChannels = new ConcurrentHashMap<>();

    /**
     * 服务提供者注册实例列表，存放多个服务实例
     * key: providerKey
     * value：注册的多个服务元数据
     */
    private final ConcurrentHashMap<String, ConcurrentHashMap<UnresolvedAddress, RegisterMetadata>> globalRegisterInfo =
            new ConcurrentHashMap<>();

    /**
     * 快速获取服务提供者提供的服务列表
     * key: providerKey
     * value: 注册的多个服务地址
     */
    private final ConcurrentHashMap<ServiceMetadata, CopyOnWriteArraySet<UnresolvedAddress>> globalServiceInfo =
            new ConcurrentHashMap<>();

    /**
     * 未发送成功的消息
     */
    private final ConcurrentHashMap<Long, MessageNonAck> nonAck = new ConcurrentHashMap<>();

    public ConcurrentHashMap<UnresolvedAddress, Channel> getRegisterChannels() {
        return registerChannels;
    }

    public ConcurrentHashMap<String, CopyOnWriteArraySet<Channel>> getSubscribeChannels() {
        return subscribeChannels;
    }

    public ConcurrentHashMap<Long, MessageNonAck> getNonAck() {
        return nonAck;
    }

    public ConcurrentHashMap<String, ConcurrentHashMap<UnresolvedAddress, RegisterMetadata>> getGlobalRegisterInfo() {
        return globalRegisterInfo;
    }

    public ConcurrentHashMap<ServiceMetadata, CopyOnWriteArraySet<UnresolvedAddress>> getGlobalServiceInfo() {
        return globalServiceInfo;
    }

    public synchronized ConcurrentHashMap<UnresolvedAddress, RegisterMetadata> getRegisterInfo(String registerName) {
        ConcurrentHashMap<UnresolvedAddress, RegisterMetadata> registerInfo = globalRegisterInfo.get(registerName);
        if (registerInfo == null) {
            ConcurrentHashMap<UnresolvedAddress, RegisterMetadata> newRegister = new ConcurrentHashMap<>();
            if (globalRegisterInfo.putIfAbsent(registerName, newRegister) == null) {
                registerInfo = newRegister;
            }
        }
        return registerInfo;
    }

    public synchronized CopyOnWriteArraySet<UnresolvedAddress> getServiceInfo(ServiceMetadata serviceMetadata) {
        CopyOnWriteArraySet<UnresolvedAddress> unresolvedAddresses = globalServiceInfo.get(serviceMetadata);
        if (unresolvedAddresses == null) {
            unresolvedAddresses = new CopyOnWriteArraySet<>();
        }
        return unresolvedAddresses;
    }
}
