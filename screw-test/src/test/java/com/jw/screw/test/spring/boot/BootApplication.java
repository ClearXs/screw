package com.jw.screw.test.spring.boot;

import com.jw.screw.spring.boot.EnableScrewMonitor;
import com.jw.screw.spring.boot.EnableScrewProvider;
import com.jw.screw.spring.boot.EnableScrewRegistry;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@EnableScrewRegistry
@EnableScrewMonitor
@EnableScrewProvider(serverKey = "2121", serverPort = 8085)
@SpringBootApplication
public class BootApplication {

    public static void main(String[] args) {
        SpringApplication.run(BootApplication.class, args);
    }
}
