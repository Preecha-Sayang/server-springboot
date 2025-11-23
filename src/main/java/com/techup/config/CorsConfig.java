package com.techup.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.lang.NonNull;

@Configuration
public class CorsConfig {

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(@NonNull CorsRegistry registry) {
                registry.addMapping("/**")               // ทุก endpoint
                        .allowedOrigins("*")             // อนุญาตทุกโดเมน
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // method ที่อนุญาต
                        .allowedHeaders("*")             // headers ทั้งหมด
                        .allowCredentials(false);        // ถ้าไม่ใช้ cookie / auth
            }
        };
    }
}