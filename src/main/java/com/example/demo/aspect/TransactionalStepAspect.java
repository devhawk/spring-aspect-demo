package com.example.demo.aspect;

import dev.dbos.transact.DBOS;
import dev.dbos.transact.database.SystemDatabase;
import dev.dbos.transact.json.JSONUtil;
import dev.dbos.transact.workflow.StepOptions;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

@Aspect
@Component("dev.dbos.transact.transactionalStepAspect")
public class TransactionalStepAspect {
  private final DBOS.Instance dbos;
  private final PlatformTransactionManager txManager;
  private final DataSource dataSource;

  public TransactionalStepAspect(
      DBOS.Instance dbos, PlatformTransactionManager txManager, DataSource dataSource) {
    this.dbos = dbos;
    this.txManager = txManager;
    this.dataSource = dataSource;
  }

  public record TxResult(String output, String error) {}

  @Around("@annotation(com.example.demo.aspect.TransactionalStep)")
  public Object interceptTransactionalStepMethod(
      ProceedingJoinPoint joinPoint, TransactionalStep transactionalStep) throws Throwable {

    var sig = (MethodSignature) joinPoint.getSignature();
    var method = sig.getMethod();

    var stepName = transactionalStep.name().isEmpty() ? method.getName() : transactionalStep.name();
    var stepOptions = new StepOptions(stepName);

    return dbos.runStep(() -> this.runStep(joinPoint), stepOptions);
  }

  private static class StepInternalException extends RuntimeException {
    public StepInternalException(Throwable cause) {
      super(cause);
    }
  }

  private Object runStep(ProceedingJoinPoint joinPoint) throws Exception {

    var txTemplate = new TransactionTemplate(txManager);
    txTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);

    if (!DBOS.inWorkflow()) {
      return txTemplate.execute(
          status -> {
            try {
              return joinPoint.proceed();
            } catch (Throwable throwable) {
              throw new RuntimeException(throwable);
            }
          });
    }

    final var wfid = DBOS.workflowId();
    final var stepid = DBOS.stepId();

    try (var connection = DataSourceUtils.getConnection(dataSource)) {
      var prevResult = getResult("dbos", wfid, stepid);
      if (prevResult != null) {
        if (prevResult.error != null) {
          var throwable = JSONUtil.deserializeAppException(prevResult.error);
          if (throwable instanceof Exception) {
            throw (Exception) throwable;
          } else {
            throw new RuntimeException(throwable.getMessage(), throwable);
          }
        }

        if (prevResult.output != null) {
          var array = JSONUtil.deserializeToArray(prevResult.output);
          if (array != null && array.length > 0) {
            return array[0];
          }
        }

        return null;
      }

      try {
        return txTemplate.execute(
            status -> {
              try {
                var result = joinPoint.proceed();
                saveStepOutcome("dbos", wfid, stepid, result);
                return result;
              } catch (Throwable t) {
                status.setRollbackOnly();
                throw new StepInternalException(t);
              }
            });
      } catch (StepInternalException e) {
        var cause = e.getCause();
        saveStepError(connection, "dbos", wfid, stepid, cause);
        throw e;
      }
    }
  }

  private TxResult getResult(String schema, String workflowId, int stepId) {
    var sanitizedSchema = SystemDatabase.sanitizeSchema(schema);
    var sql =
        "SELECT output, error FROM \"%s\".tx_results WHERE workflow_id = ? AND step_id = ?"
            .formatted(sanitizedSchema);
    try (var conn = DataSourceUtils.getConnection(dataSource);
        PreparedStatement stmt = conn.prepareStatement(sql)) {
      stmt.setString(1, workflowId);
      stmt.setInt(2, stepId);
      try (var rs = stmt.executeQuery()) {
        if (rs.next()) {
          return new TxResult(rs.getString("output"), rs.getString("error"));
        }
        return null;
      }
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  private void saveStepOutcome(String schema, String workflowId, int stepId, Object result) {
    var output = JSONUtil.serialize(result);
    saveStep(schema, workflowId, stepId, output, null);
  }

  private void saveStepError(
      Connection conn, String schema, String workflowId, int stepId, Throwable throwable) {
    TransactionTemplate ttFailure = new TransactionTemplate(txManager);
    ttFailure.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);

    ttFailure.executeWithoutResult(
        status -> {
          var error = JSONUtil.serializeAppException(throwable);
          saveStep(schema, workflowId, stepId, null, error);
        });
  }

  private void saveStep(String schema, String workflowId, int stepId, String output, String error) {
    var sanitizedSchema = SystemDatabase.sanitizeSchema(schema);
    var sql =
        "INSERT INTO \"%s\".tx_results (workflow_id, step_id, output, error) VALUES (?, ?, ?, ?)"
            .formatted(sanitizedSchema);
    try (var conn = DataSourceUtils.getConnection(dataSource);
        PreparedStatement stmt = conn.prepareStatement(sql)) {
      stmt.setString(1, workflowId);
      stmt.setInt(2, stepId);
      stmt.setString(3, output);
      stmt.setString(4, error);
      stmt.executeUpdate();
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }
}
