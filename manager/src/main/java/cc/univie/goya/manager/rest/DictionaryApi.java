package cc.univie.goya.manager.rest;

import io.vertx.core.Future;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class DictionaryApi {
  private final Gateway gateway;

  public void attach(@NonNull Router router) {
    router.post("/storage/:objectKey").handler(this::handlePutObject);
    router.delete("/storage/:objectKey").handler(this::deleteObject);
    router.get("/storage/:objectKey").handler(this::searchObject);
  }

  private void searchObject(RoutingContext context) {
    String objectKey = context.pathParam("objectKey");
    log.info("handling search for {}", objectKey);

    this.gateway.getFromNode(objectKey).compose(binary -> {
      context.response().setStatusCode(200).end(binary);
      return Future.succeededFuture();
    }).recover(err -> {
      log.error("failed to search for object from node {}", objectKey, err);
      return Future.failedFuture(err);
    });
  }

  private void deleteObject(RoutingContext context) {
    String objectKey = context.pathParam("objectKey");
    log.info("handling delete for {}", objectKey);
    this.gateway.deleteFromNode(objectKey).compose(response -> {
      context.response().setStatusCode(200).end(response.encode());
      return Future.succeededFuture();
    }).recover(err -> {
      log.error("failed to delete object from node {}", objectKey, err);
      return Future.failedFuture(err);
    });
  }

  private void handlePutObject(RoutingContext context) {
    String objectKey = context.pathParam("objectKey");

    this.gateway.uploadToNode(objectKey).compose(response -> {
      context.response().setStatusCode(200).end(response.encode());
      return Future.succeededFuture();
    }).recover(err -> {
      log.error("failed to put object to node {}", objectKey, err);
      return Future.failedFuture(err);
    });
  }
}
