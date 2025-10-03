package com.library.sportshop.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
         //Images sẽ được xử lý bởi ImageController
        registry.addResourceHandler("/images/**")
                 .addResourceLocations("file:/D:/image/");
    }
}
