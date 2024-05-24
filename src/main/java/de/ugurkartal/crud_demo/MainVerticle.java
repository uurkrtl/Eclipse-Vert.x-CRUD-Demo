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
  public static final String MONGODB_COLLECTION = "customer";
  public static final String API_URI = "/api/customers";
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
    router.post(API_URI).handler(this::createCustomer);
    router.get(API_URI + "/:id").handler(this::getCustomer);
    router.put(API_URI + "/:id").handler(this::updateCustomer);
    router.delete(API_URI + "/:id").handler(this::deleteCustomer);
    router.get(API_URI).handler(this::getAllCustomers);

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
    mongoClient.find(MONGODB_COLLECTION, new JsonObject(), res -> {
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
    mongoClient.removeDocument(MONGODB_COLLECTION, query , res -> {
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
    mongoClient.updateCollection(MONGODB_COLLECTION, query, new JsonObject().put("$set", json), res -> {
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
    mongoClient.findOne(MONGODB_COLLECTION, query, null, res -> {
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
    mongoClient.save(MONGODB_COLLECTION, json, res -> {
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
