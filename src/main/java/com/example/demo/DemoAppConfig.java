package com.example.demo;

import dev.dbos.transact.DBOS;
import dev.dbos.transact.config.DBOSConfig;

import java.util.Objects;

import javax.sql.DataSource;

import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DemoAppConfig {
  @Bean
  public Jdbi jdbi(DataSource dataSource) {
    var jdbi = Jdbi.create(dataSource);
    jdbi.installPlugin(new SqlObjectPlugin());
    return jdbi;
  }

  // eventually, this will be in the dbos spring boot package
  @Bean
  public DBOS.Instance dbos(DBOSConfig config) {
    return new DBOS.Instance(config);
  }

  // eventually, we will read these out of app properties,
  // similar to how the DataSource bean that gets passed to
  // jdbi method above is created
  @Bean
  public DBOSConfig dbosConfig() {
    String databaseUrl = System.getenv("DBOS_SYSTEM_JDBC_URL");
    if (databaseUrl == null || databaseUrl.isEmpty()) {
      databaseUrl = "jdbc:postgresql://localhost:5432/spring_aspect_demo";
    }
    return DBOSConfig.defaults("spring-aspect-demo")
        .withDatabaseUrl(databaseUrl)
        .withDbUser(Objects.requireNonNullElse(System.getenv("PGUSER"), "postgres"))
        .withDbPassword(Objects.requireNonNullElse(System.getenv("PGPASSWORD"), "dbos"))
        .withAppVersion("0.1.0");
  }
}
