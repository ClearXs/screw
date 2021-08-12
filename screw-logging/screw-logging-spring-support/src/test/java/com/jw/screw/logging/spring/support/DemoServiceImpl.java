package com.jw.screw.logging.spring.support;

import com.jw.screw.logging.core.annotation.ScrewLog;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
@ScrewLog
public class DemoServiceImpl implements DemoService {

    @Override
    @ScrewLog(type = "demo服务")
    public String hello() {
        return "hello world";
    }

    @Override
    public List<String> message() {
        return Arrays.asList("aaaa");
    }

    @Override
    public void exception() {
        int i = 1 / 0;
    }
}
