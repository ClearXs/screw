package com.jw.screw.monitor.opentracing;

import com.jw.screw.common.serialization.SerializerHolders;
import org.junit.Test;

public class SerializerTest {

    @Test
    public void complex() {
        ScrewTracer tracer = new ScrewTracer();
        ScrewSpan test = tracer.buildSpan("test").start();
        tracer.scopeManager().activate(test, true);
        byte[] serialization = SerializerHolders.serializer().serialization(tracer);

        ScrewTracer deserialization = SerializerHolders.serializer().deserialization(serialization, ScrewTracer.class);

    }
}
