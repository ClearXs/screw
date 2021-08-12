package com.jw.screw.logging.spring.boot;

import com.jw.screw.logging.spring.support.ScrewLogger;
import com.jw.screw.storage.properties.StorageProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.sql.SQLException;

@Configuration
public class ScrewLoggerConfiguration {

    @Bean
    @ConfigurationProperties(prefix = "screw.storage")
    public StorageProperties loggingProperties() {
        return new StorageProperties();
    }

    @Bean
    public ScrewLogger screwLogger(StorageProperties storageProperties) throws SQLException, IOException, ClassNotFoundException {
        return new ScrewLogger(storageProperties);
    }

}
