package com.example.demo.aspect;

import java.lang.reflect.Method;

import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class DurableScanner implements SmartInitializingSingleton {
  private final ApplicationContext applicationContext;
  private final DurableRegistry durableRegistry;

  @Autowired
  public DurableScanner(ApplicationContext applicationContext, DurableRegistry durableRegistry) {
    this.applicationContext = applicationContext;
    this.durableRegistry = durableRegistry;
  }

  @Override
  public void afterSingletonsInstantiated() {
    String[] beanNames = applicationContext.getBeanDefinitionNames();
    for (String beanName : beanNames) {
      // Skip infrastructure beans to avoid circular dependencies
      if (beanName.equals("durableScanner") || beanName.equals("durableRegistry")) {
        continue;
      }
      Object bean = applicationContext.getBean(beanName);
      Class<?> targetClass = AopUtils.getTargetClass(bean);
      if (targetClass == null) {
        continue;
      }
      Method[] methods = targetClass.getDeclaredMethods();
      for (Method method : methods) {
        if (method.isAnnotationPresent(Durable.class)) {
          String key = method.getAnnotation(Durable.class).name();
          if (key.isEmpty()) {
            key = targetClass.getName() + "." + method.getName();
          }
          System.out.println("Registering @Durable method: " + key);
          durableRegistry.register(key, method);
        }
      }
    }
  }
}
