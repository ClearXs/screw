package com.jw.screw.admin.web;

import com.jw.screw.spring.boot.EnableScrewMonitor;
import com.jw.screw.spring.boot.EnableScrewProvider;
import com.jw.screw.spring.boot.EnableScrewRegistry;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * application
 * @author jiangw
 * @date 2020/12/20 20:49
 * @since 1.0
 */
@EnableTransactionManagement
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class}, scanBasePackages = {"com.jw.screw.admin", "com.jw.screw.admin.api" })
@EnableScrewMonitor
@EnableScrewProvider(serverKey = "config_center", serverPort = 8601, monitorKey = "monitor_center", isConfigCenter = true)
@EnableScrewRegistry
public class AdminApplication extends SpringBootServletInitializer {

	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
		return builder.sources(AdminApplication.class);
	}

	public static void main(String[] args) {
		SpringApplication.run(AdminApplication.class, args);
	}

}
