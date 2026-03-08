package com.example.demo.aspect;

import dev.dbos.transact.DBOS;

import java.util.Objects;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

// this is the demo's proxy interceptor, similar to DBOSInvocationHandler

@Aspect
@Component("dev.dbos.transact.durableAspect")
public class DurableAspect {

  private final DBOS.Instance dbos;

  public DurableAspect(DBOS.Instance dbos) {
    this.dbos = dbos;
  }

  @Around("@annotation(com.example.demo.aspect.Durable)")
  public Object interceptDurableMethod(ProceedingJoinPoint joinPoint) throws Throwable {

    var sig = (MethodSignature)joinPoint.getSignature();
    var method = sig.getMethod();
    var durableTag = Objects.requireNonNull(method.getAnnotation(Durable.class));

    var workflowName = durableTag.name().isEmpty() ? method.getName() : durableTag.name();
    var className = joinPoint.getTarget().getClass().getName();

    return dbos.handleWorkflow(workflowName,className, null, joinPoint.getArgs(), sig.getReturnType());
  }
}
