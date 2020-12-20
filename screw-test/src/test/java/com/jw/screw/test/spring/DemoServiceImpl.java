package com.jw.screw.test.spring;

import com.jw.screw.provider.annotations.ProviderService;

@ProviderService(publishService = DemoService.class)
public class DemoServiceImpl implements DemoService {

    @Override
    public String hello(String msg) {
        System.out.println(msg);
        return msg;
    }
}
