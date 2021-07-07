package com.jw.screw.test.monitor.agent;

import com.jw.screw.registry.DefaultRegistry;

public class Registry {

    public static void main(String[] args) {
        DefaultRegistry defaultRegistry = new DefaultRegistry(8080);
        defaultRegistry.start();
    }
}