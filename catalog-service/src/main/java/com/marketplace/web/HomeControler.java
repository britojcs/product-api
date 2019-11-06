package com.marketplace.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/")
public class HomeControler {

  private Environment env;

  @Autowired
  public HomeControler(Environment env) {
    super();
    this.env = env;
  }



  @GetMapping("/")
  public String home() {
    return "Catalog Service running at port: " + env.getProperty("local.server.port");
  }

}
