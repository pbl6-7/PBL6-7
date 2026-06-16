package com.campus.activity.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web配置类
 * 负责静态资源映射，CORS配置统一在WebMvcConfig中管理
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${file.upload.path:uploads}")
    private String uploadPath;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String absolutePath = new java.io.File(uploadPath).getAbsolutePath();
        String pathWithSlash = absolutePath.replace("\\", "/") + "/";

        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + pathWithSlash);
    }
}
