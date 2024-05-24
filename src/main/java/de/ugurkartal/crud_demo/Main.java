package de.ugurkartal.crud_demo;

import io.vertx.core.Vertx;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {
  public static final Logger LOGGER = LoggerFactory.getLogger(Main.class);
  public static void main(String[] args) {
    Vertx vertx = Vertx.vertx();
    vertx.deployVerticle(new MainVerticle(), res -> {
      if (res.succeeded()) {
        LOGGER.info("Deployment id is: {}", res.result());
      } else {
        LOGGER.error("Deployment failed!");
      }
    });
  }
}
