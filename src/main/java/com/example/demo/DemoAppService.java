package com.example.demo;

import java.util.List;

import org.jdbi.v3.core.Jdbi;
import org.springframework.stereotype.Service;

@Service
public class DemoAppService {

  private final Jdbi jdbi;

  public DemoAppService(Jdbi jdbi) {
    this.jdbi = jdbi;
  }

  public List<String> getTables() {
    return jdbi.withHandle(
        h ->
            h.createQuery(
                    """
                        SELECT tablename
                        FROM pg_catalog.pg_tables
                        ORDER BY tablename
                    """)
                .mapTo(String.class)
                .list());
  }
}
