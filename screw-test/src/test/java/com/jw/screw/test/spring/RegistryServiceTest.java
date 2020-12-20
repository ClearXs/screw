package com.jw.screw.test.spring;

import com.jw.screw.registry.DefaultRegistry;
import org.junit.Test;

public class RegistryServiceTest {

    @Test
    public void registry() {
        DefaultRegistry defaultRegistry = new DefaultRegistry(8080);
        defaultRegistry.start();
    }
}
