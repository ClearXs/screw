package com.jw.screw.test.basic;

import com.jw.screw.common.serialization.SerializerHolders;
import com.jw.screw.monitor.opentracing.ScrewSpan;
import com.jw.screw.monitor.opentracing.ScrewTracer;
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
