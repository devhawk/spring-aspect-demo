package com.example.demo.aspect;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

// This is the demo's version of WorkflowRegistry

@Component("dev.dbos.transact.durableRegistry")
public class DurableRegistry {
  private final Map<String, RegisteredDurableMethod> registry = new ConcurrentHashMap<>();

  public void register(String key, Object bean, Method method) {
    registry.put(key, new RegisteredDurableMethod(bean, method));
  }

  public RegisteredDurableMethod get(String key) {
    return registry.get(key);
  }

  public Map<String, RegisteredDurableMethod> getAll() {
    return registry;
  }
}
