package com.sistema.sistema.infrastructure.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${app.upload-dir}")
    private String uploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Esto expone la carpeta de uploads para que sea accesible via HTTP
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + uploadDir + "/uploads/")
                .setCachePeriod(3600)
                .resourceChain(true);
    }
}
