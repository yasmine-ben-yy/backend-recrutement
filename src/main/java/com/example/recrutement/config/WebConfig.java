// com.example.recrutement.config.WebConfig.java
package com.example.recrutement.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Servir les fichiers uploadés (logos)
        registry.addResourceHandler("/uploads/**")
        .addResourceLocations("file:uploads/")
        .setCachePeriod(3600);
        
                
        registry.addResourceHandler("/uploads/**")
        .addResourceLocations("file:./uploads/")
        .setCachePeriod(3600);

        
        registry.addResourceHandler("/uploads/candidatures/**")
        .addResourceLocations("file:./uploads/candidatures/")
        .setCachePeriod(3600);
        
        // Servir tous les fichiers uploads
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:./uploads/")
                .setCachePeriod(3600);
    }
}