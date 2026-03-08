package com.example.demo.aspect;

import dev.dbos.transact.DBOS;

import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Component;

// this helper automatically launch and shutdown DBOS

@Component("dev.dbos.transact.durableLifetime")
public class DurableLifetime implements SmartLifecycle {

  private final DBOS.Instance dbos;
  private boolean running = false;

  // DBOS Instance would be injected like this Jdbi instance
  public DurableLifetime(DBOS.Instance dbos) {
    this.dbos = dbos;
  }

  @Override
  public void start() {
    running = true;
    System.out.println("DurableLifetime started");
    dbos.launch();
  }

  @Override
  public void stop() {
    running = false;
    System.out.println("DurableLifetime stopped");
    dbos.shutdown();
  }

  @Override
  public boolean isRunning() {
    return running;
  }
}
