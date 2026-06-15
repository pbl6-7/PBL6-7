package com.campus.core.validation.annotation;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * ApplicationContext 提供者
 * 用于在非 Spring 管理的类中获取 ApplicationContext
 */
@Component
public class ApplicationContextProvider implements ApplicationContextAware {

    private static ApplicationContext context;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        context = applicationContext;
    }

    /**
     * 获取 ApplicationContext 实例
     * @return ApplicationContext
     */
    public static ApplicationContext getContext() {
        return context;
    }
}
