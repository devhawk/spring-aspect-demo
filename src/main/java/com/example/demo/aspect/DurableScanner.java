package com.example.demo.aspect;

import dev.dbos.transact.DBOS;
import dev.dbos.transact.workflow.SerializationStrategy;

import java.lang.reflect.Method;
import java.util.Objects;

import org.springframework.aop.framework.Advised;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

// this class populates the workflow registry

@Component("dev.dbos.transact.durableScanner")
public class DurableScanner implements SmartInitializingSingleton {
  private final ApplicationContext applicationContext;
  private final DBOS.Instance dbos;

  @Autowired
  public DurableScanner(ApplicationContext applicationContext, DBOS.Instance dbos) {
    this.applicationContext = applicationContext;
    this.dbos = dbos;
  }

  @Override
  public void afterSingletonsInstantiated() {
    String[] beanNames = applicationContext.getBeanDefinitionNames();
    for (String beanName : beanNames) {
      if (beanName.startsWith("dev.dbos.transact.")) {
        continue;
      }

      Object bean = applicationContext.getBean(beanName);
      Class<?> targetClass = AopUtils.getTargetClass(bean);
      if (targetClass == null) {
        continue;
      }

      Object targetObject = null;

      Method[] methods = targetClass.getDeclaredMethods();
      for (Method method : methods) {
        Durable durableTag = method.getAnnotation(Durable.class);
        if (durableTag == null) {
          continue;
        }

        // Extract the actual target object (non-intercepted version)
        if (targetObject == null) {
          if (AopUtils.isAopProxy(bean) && bean instanceof Advised) {
            try {
              targetObject = ((Advised) bean).getTargetSource().getTarget();
            } catch (Exception e) {
              throw new RuntimeException(e);
            }
          }

          Objects.requireNonNull(targetObject);
        }

        var workflowName = durableTag.name().isEmpty() ? method.getName() : durableTag.name();

        dbos.registerWorkflow(
            workflowName,
            targetClass.getName(),
            null,
            targetObject,
            method,
            -1,
            SerializationStrategy.DEFAULT);
      }
    }
  }
}
