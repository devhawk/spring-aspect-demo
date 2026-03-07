package com.example.demo.aspect;

import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Component;

@Component
public class DurableLifetime implements SmartLifecycle {
  private boolean running = false;

  @Override
  public void start() {
    running = true;
    System.out.println("DurableLifetime started");
    // Add custom startup logic here
  }

  @Override
  public void stop() {
    running = false;
    System.out.println("DurableLifetime stopped");
    // Add custom shutdown logic here
  }

  @Override
  public boolean isRunning() {
    return running;
  }
}
