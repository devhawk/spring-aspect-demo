package com.example.demo;

import dev.dbos.transact.DBOS;

import java.util.List;

import com.example.demo.aspect.Durable;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.jdbi.v3.core.Jdbi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class DemoAppService {

  private final Jdbi jdbi;
  private final DBOS.Instance dbos;
  private final RestTemplate restTemplate;
  private DemoAppService self;

  @JsonIgnoreProperties(ignoreUnknown = true)
  public record InsultResponse(
      @JsonProperty("number") String number, @JsonProperty("insult") String insult) {}

  public DemoAppService(Jdbi jdbi, DBOS.Instance dbos) {
    this.jdbi = jdbi;
    this.dbos = dbos;
    this.restTemplate = new RestTemplate();
  }

  @Autowired
  public void setSelf(@Lazy DemoAppService self) {
    this.self = self;
  }

  public record TablesWorkflowResponse(String insult, List<String> tables) {}

  @Durable
  public TablesWorkflowResponse tablesWorkflow() {
    var tables = dbos.runStep(() -> getTables(), "getTables");
    var insult = self.insultWorkflow();
    return new TablesWorkflowResponse(insult, tables);
  }

  @Durable
  public String insultWorkflow() {
    var result = dbos.runStep(() -> getRandomInsult(), "getRandomInsult");
    return result.insult();
  }

  private InsultResponse getRandomInsult() {
    String url = "https://evilinsult.com/generate_insult.php?lang=en&type=json";

    try {
      InsultResponse response = restTemplate.getForObject(url, InsultResponse.class);
      if (response != null) {
        return response;
      }
      return new InsultResponse("0", "Failed to get insult");
    } catch (Exception e) {
      return new InsultResponse("0", "Error: " + e.getMessage());
    }
  }

  private List<String> getTables() {
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
