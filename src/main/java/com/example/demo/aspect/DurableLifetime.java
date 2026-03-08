package com.example.demo.aspect;

import org.jdbi.v3.core.Jdbi;
import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Component;

// this helper automatically launch and shutdown DBOS

@Component("dev.dbos.transact.durableLifetime")
public class DurableLifetime implements SmartLifecycle {

  private final Jdbi jdbi;
  private boolean running = false;

  // DBOS Instance would be injected like this Jdbi instance
  public DurableLifetime(Jdbi jdbi) {
    this.jdbi = jdbi;
  }

  @Override
  public void start() {
    running = true;
    System.out.println("DurableLifetime started");
    // dbos.launch would be called here
  }

  @Override
  public void stop() {
    running = false;
    System.out.println("DurableLifetime stopped");
    // dbos.shutdown would be called here
  }

  @Override
  public boolean isRunning() {
    return running;
  }
}
