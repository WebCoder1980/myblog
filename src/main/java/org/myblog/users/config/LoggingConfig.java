package org.myblog.users.config;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.myblog.users.UsersApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LoggingConfig {
    @Bean
    public Logger logger() {
        return LogManager.getLogger(UsersApplication.class);
    }
}
