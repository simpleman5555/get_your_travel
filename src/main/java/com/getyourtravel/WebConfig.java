package com.getyourtravel;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {

        if (!registry.hasMappingForPattern("/uploads/**")) {
            registry
                    .addResourceHandler("/uploads/**")
                    .addResourceLocations("file:/var/lib/uploads/");
        }

        if (!registry.hasMappingForPattern("/media/**")) {
            registry
                    .addResourceHandler("/media/**")
                    .addResourceLocations("classpath:/static/media/");
        }
    }
}