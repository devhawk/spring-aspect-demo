package com.example.demo.aspect;

import java.lang.reflect.Method;

import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import dev.dbos.transact.DBOS;
import dev.dbos.transact.workflow.SerializationStrategy;

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
      Method[] methods = targetClass.getDeclaredMethods();
      for (Method method : methods) {
        Durable wfTag = method.getAnnotation(Durable.class);
        if (wfTag == null) {
          continue;
        }

        var workflowName = wfTag.name().isEmpty() ? method.getName() : wfTag.name();
        dbos.registerWorkflow(workflowName,targetClass.getName(), null, bean, method, -1, SerializationStrategy.DEFAULT);
      }
    }
  }
}
