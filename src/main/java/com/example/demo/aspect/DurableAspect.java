package com.example.demo.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.jdbi.v3.core.Jdbi;
import org.springframework.stereotype.Component;

// this is the demo's proxy interceptor, similar to DBOSInvocationHandler

@Aspect
@Component("dev.dbos.transact.durableAspect")
public class DurableAspect {

  // dbos instance would be injected like this
  private final Jdbi jdbi;

  public DurableAspect(Jdbi jdbi) {
    this.jdbi = jdbi;
  }

  @Around("@annotation(com.example.demo.aspect.Durable)")
  public Object interceptDurableMethod(ProceedingJoinPoint joinPoint) throws Throwable {
    // var sig = joinPoint.getSignature();
    // var args = joinPoint.getArgs();
    // var target = joinPoint.getTarget();
    // var klass = joinPoint.getClass();

    return joinPoint.proceed();
  }
}
