package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin(origins = "*")
public class DemoAppController {
  private final DemoAppService demoAppService;

  @Autowired
  public DemoAppController(DemoAppService demoAppService) {
    this.demoAppService = demoAppService;
  }

  @GetMapping("/")
  @ResponseBody
  public DemoAppService.TablesWorkflowResponse getTables() {
    return demoAppService.tablesWorkflow();
  }
}
