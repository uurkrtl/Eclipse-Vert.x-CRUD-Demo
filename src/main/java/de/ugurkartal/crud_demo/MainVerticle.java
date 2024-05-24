package de.ugurkartal.crud_demo;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;

public class MainVerticle extends AbstractVerticle {
  private MongoClient mongoClient;

  @Override
  public void start(Promise<Void> startPromise) throws Exception {

    // MongoClient configuration
    JsonObject config = new JsonObject()
      .put("connection_string", System.getenv("MONGODB_URI"))
      .put("db_name", System.getenv("MONGODB_NAME"));

    mongoClient = MongoClient.createShared(vertx, config);

    // Create a Router
    Router router = Router.router(vertx);

    // BodyHandler for handling request bodies
    router.route().handler(BodyHandler.create());

    // Routes
    router.post("/api/customers").handler(this::createCustomer);
    router.get("/api/customers/:id").handler(this::getCustomer);
    router.put("/api/customers/:id").handler(this::updateCustomer);
    router.delete("/api/customers/:id").handler(this::deleteCustomer);
    router.get("/api/customers").handler(this::getAllCustomers);

    // Start HTTP server
    vertx.createHttpServer()
      .requestHandler(router)
      .listen(8080, http -> {
        if (http.succeeded()) {
          startPromise.complete();
          System.out.println("HTTP server started on port 8080");
        } else {
          startPromise.fail(http.cause());
        }
      });

  }

  private void getAllCustomers(RoutingContext context) {
    mongoClient.find("customer", new JsonObject(), res -> {
      if (res.succeeded()) {
        JsonArray jsonArray = new JsonArray();
        res.result().forEach(jsonArray::add);
        context.response()
          .setStatusCode(200)
          .end(jsonArray.encodePrettily());
      } else {
        context.response()
          .setStatusCode(500)
          .end();
      }
    });
  }

  private void deleteCustomer(RoutingContext context) {
    String id = context.pathParam("id");
    JsonObject query = new JsonObject().put("_id", id);
    mongoClient.removeDocument("customer", query , res -> {
      if (res.succeeded()) {
        context.response()
          .setStatusCode(200)
          .end("Deletion successful");
      } else {
        context.response()
          .setStatusCode(500)
          .end();
      }
    });
  }

  private void updateCustomer(RoutingContext context) {
    String id = context.pathParam("id");
    JsonObject json = context.getBodyAsJson();
    JsonObject query = new JsonObject().put("_id", id);
    mongoClient.updateCollection("customer", query, new JsonObject().put("$set", json), res -> {
      if (res.succeeded()) {
        context.response()
          .setStatusCode(200)
          .end(context.getBodyAsJson().encodePrettily());
      } else {
        context.response()
          .setStatusCode(500)
          .end();
      }
    });
  }

  private void getCustomer(RoutingContext context) {
    String id = context.pathParam("id");
    JsonObject query = new JsonObject().put("_id", id);
    mongoClient.findOne("customer", query, null, res -> {
      if (res.succeeded()) {
        if (res.result() != null) {
          context.response()
            .setStatusCode(200)
            .end(res.result().encodePrettily());
        } else {
            context.response()
              .setStatusCode(404)
              .end();
        }
      } else {
        context.response()
          .setStatusCode(500)
          .end();
      }
    });
  }

  private void createCustomer(RoutingContext context) {
    JsonObject json = context.getBodyAsJson();
    mongoClient.save("customer", json, res -> {
      if (res.succeeded()) {
        context.response()
          .setStatusCode(201)
          .end(json.encodePrettily());
      } else {
        context.response()
          .setStatusCode(500)
          .end();
      }
    });
  }
}
