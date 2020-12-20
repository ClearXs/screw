package com.jw.screw.spring;

import java.util.List;

public class ConsumerWrapper {

    private String registryAddress;

    private int registryPort;

    private List<ServiceWrapper> serviceWrappers;

    private String loadbalance;

    public String getRegistryAddress() {
        return registryAddress;
    }

    public void setRegistryAddress(String registryAddress) {
        this.registryAddress = registryAddress;
    }

    public int getRegistryPort() {
        return registryPort;
    }

    public void setRegistryPort(int registryPort) {
        this.registryPort = registryPort;
    }

    public String getLoadbalance() {
        return loadbalance;
    }

    public void setLoadbalance(String loadbalance) {
        this.loadbalance = loadbalance;
    }

    public List<ServiceWrapper> getServiceWrappers() {
        return serviceWrappers;
    }

    public void setServiceWrappers(List<ServiceWrapper> serviceWrappers) {
        this.serviceWrappers = serviceWrappers;
    }

    public static class ServiceWrapper {

        private String serverKey;

        private String invokeType;

        private List<Class<?>> services;

        public String getServerKey() {
            return serverKey;
        }

        public void setServerKey(String serverKey) {
            this.serverKey = serverKey;
        }

        public String getInvokeType() {
            return invokeType;
        }

        public void setInvokeType(String invokeType) {
            this.invokeType = invokeType;
        }

        public List<Class<?>> getServices() {
            return services;
        }

        public void setServices(List<Class<?>> services) {
            this.services = services;
        }
    }
}
