# Vert.x
image:https://img.shields.io/badge/vert.x-4.5.7-purple.svg[link="https://vertx.io"]

This repository shows how to use the `Vert.x core` and `web` modules also `mongodb` and `web` client on a web application which contains RESTful Web Services.

## What you will learn in this repository?
* How to deploy a Verticle
* How to create and use an HTTP server
* How to create a router for a web service
* How to use mongo client and handle CRUD operations

### Deploy a Verticle
```java
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
```

### Creating a Server and adding a Router to it
```java
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
    LOGGER.info("HTTP server started on port 8080");
  } else {
    startPromise.fail(http.cause());
  }
});
```

### Creating a Mongo Client
```java
// MongoClient configuration
JsonObject config = new JsonObject()
  .put("connection_string", System.getenv("MONGODB_URI"))
  .put("db_name", System.getenv("MONGODB_NAME"));

mongoClient = MongoClient.createShared(vertx, config);
```
### GetAll
```java
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
```

### GetById
```java
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
```

### Create
```java
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
```

### Update
```java
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
```

### Delete
```java
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
```
