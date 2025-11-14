package com.library.sportshop.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${app.image.directory}")
    private String imageDirectory;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
         //Images sẽ được xử lý bởi ImageController
        registry.addResourceHandler("/images/**")
                 .addResourceLocations("file:" + imageDirectory);
    }
}
