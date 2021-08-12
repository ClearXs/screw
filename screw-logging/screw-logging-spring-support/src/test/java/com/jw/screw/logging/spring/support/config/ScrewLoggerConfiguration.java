package com.jw.screw.logging.spring.support.config;

import com.jw.screw.logging.spring.support.ScrewLogger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.sql.SQLException;

@Configuration
public class ScrewLoggerConfiguration {

    @Bean
    public ScrewLogger screwLogger() throws SQLException, IOException, ClassNotFoundException {
        return new ScrewLogger();
    }
}
