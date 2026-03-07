package com.example.demo.aspect;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

@Component
public class DurableRegistry {
  private final Map<String, Method> registry = new ConcurrentHashMap<>();

  public void register(String key, Method method) {
    registry.put(key, method);
  }

  public Method get(String key) {
    return registry.get(key);
  }

  public Map<String, Method> getAll() {
    return registry;
  }
}
