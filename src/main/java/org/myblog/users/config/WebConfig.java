package org.myblog.users.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("http://localhost:37000", "http://localhost:37001", "http://localhost:37000", "http://localhost:37001", "http://127.0.0.1:37000", "http://127.0.0.1:37001")
                .allowedMethods("*")
                .allowedHeaders("*")
                .allowCredentials(true);
    }
}
