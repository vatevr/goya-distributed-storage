package cc.univie.goya.manager.rest;

import io.vertx.core.Future;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;

@Slf4j
@RequiredArgsConstructor
public class DictionaryApi {
  private final Gateway gateway;

  public void attach(@NonNull Router router) {
    router.post("/storage/:objectKey")
        .handler(BodyHandler.create(true).setBodyLimit(-1))
        .handler(this::handlePutObject);

    router.delete("/storage/:objectKey").handler(this::handleDeleteObject);
    router.get("/storage/:objectKey").handler(this::handleSearchObject);
    router.get("/storage").handler(this::handleRangeSearch);

    {
      router.post("/test/upload")
        .handler(BodyHandler.create().setUploadsDirectory("./").setBodyLimit(-1))
        .handler(context -> {
          context.fileUploads().forEach(f -> log.info("filename {}", f.fileName()));
          context.response().end();
        });
    }
  }

  private void handleRangeSearch(RoutingContext context) {
    String from = Objects.requireNonNull(context.queryParams().get("from"));
    String to = Objects.requireNonNull(context.queryParams().get("to"));

    this.gateway.keysFromNodes().compose(keys -> {
      context.response().setStatusCode(200).end(keys.encode());
      return Future.succeededFuture();
    }).recover(err -> {
      log.error("failed to search keys from {} to {}", from, to, err);

      context.response()
          .setStatusCode(500)
          .end(internalServerERror().encode());

      return Future.failedFuture(err);
    });
  }

  private void handleSearchObject(RoutingContext context) {
    String objectKey = context.pathParam("objectKey");
    log.info("handling search for {}", objectKey);

    this.gateway.getFromNode(objectKey).compose(binary -> {
      context.response()
          .setStatusCode(200)
          .end(binary);

      return Future.succeededFuture();
    }).recover(err -> {
      log.error("failed to search for object from node {}", objectKey, err);

      context.response()
          .setStatusCode(500)
          .end(internalServerERror().encode());

      return Future.failedFuture(err);
    });
  }

  private void handleDeleteObject(RoutingContext context) {
    String objectKey = context.pathParam("objectKey");
    log.info("handling delete for {}", objectKey);
    this.gateway.deleteFromNode(objectKey).compose(response -> {
      context.response().setStatusCode(200).end(response.encode());
      return Future.succeededFuture();
    }).recover(err -> {
      log.error("failed to delete object from node {}", objectKey, err);
      context.response()
          .setStatusCode(500)
          .end(internalServerERror().encode());

      return Future.failedFuture(err);
    });
  }

  private void handlePutObject(RoutingContext context) {
    String objectKey = context.pathParam("objectKey");
    Buffer binary = context.getBody();

    this.gateway.uploadToNode(objectKey, binary).compose(ignoredResponse -> {
      log.info("response from node");
      context.response().setStatusCode(200).end(success(objectKey).encode());
      return Future.succeededFuture();
    }).recover(err -> {
      log.error("failed to put object to node {}", objectKey, err);
      context.response()
          .setStatusCode(500)
          .end(internalServerERror().encode());

      return Future.failedFuture(err);
    });
  }

  private static JsonObject internalServerERror() {
    return new JsonObject().put("message", "internal server error");
  }

  private static JsonObject success(@NonNull String objectKey) {
    return new JsonObject().put("message", "success").put("objectKey", objectKey);
  }
}
