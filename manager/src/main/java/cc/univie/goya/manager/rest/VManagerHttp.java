package cc.univie.goya.manager.rest;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Random;

@Slf4j
@RequiredArgsConstructor
public class VManagerHttp extends AbstractVerticle {
  private static final int DEFAULT_PORT = 8500;

  private static int port(int hash) {
    return DEFAULT_PORT + hash;
  }

  private static String hostname(int hash) {
    return "node" + hash;
  }

  private final int nodes;
  private WebClient webclient;

  @Override
  public void start(Promise<Void> startPromise) {
    this.webclient = WebClient.create(vertx, new WebClientOptions().setUserAgent("goya/manager"));

    log.info("Starting manager http");

    Router application = Router.router(vertx);

    application.get("/hello").handler(this::handleHello);
    application.get("/ping-file").handler(this::handlePing);
    application.post("/put/:objectKey").handler(this::handlePutObject);

    vertx.createHttpServer()
        .requestHandler(application)
        .listen(7500, asyncResult -> {
          if (asyncResult.succeeded()) {
            startPromise.complete();
          } else {
            log.error("failed to deploy http", asyncResult.cause());
          }
        });
  }

  private void handlePutObject(RoutingContext context) {
    String objectKey = context.pathParam("objectKey");
    int hash = hashCode(objectKey);

    this.sendToNode(hash).compose(response -> {
      context.response().setStatusCode(200).end(response.encode());
      return Future.succeededFuture();
    })
    .recover(err -> {
      log.error("failed to put object to node {}", hash, err);
      return Future.failedFuture(err);
    });
  }

  private Future<JsonObject> sendToNode(int hash) {
    Promise<HttpResponse<Buffer>> whenResponded = Promise.promise();

    this.webclient
        .post(port(hash), hostname(hash), "/ping")
        .send(whenResponded);

    return whenResponded.future().map(HttpResponse::bodyAsJsonObject);
  }

  private int hashCode(String objectKey) {
    return objectKey.hashCode() % nodes;
  }

  private void handlePing(RoutingContext context) {
    int hash = hash();

    this.webclient.get(port(hash), hostname(hash), "/ping").send(asyncResult -> {
      if (asyncResult.succeeded()) {
        context.response().setStatusCode(200).end(asyncResult.result().body());
      } else {
        log.error("failed to establish connection to node", asyncResult.cause());
      }
    });
  }

  /**
   * Hash function to hash request to node
   */
  private int hash() {
    Random random = new Random();
    return random.nextInt(nodes);
  }

  private void handleHello(RoutingContext context) {
    log.info("hey there");

    context.response().setStatusCode(200).end("holla");
  }
}
