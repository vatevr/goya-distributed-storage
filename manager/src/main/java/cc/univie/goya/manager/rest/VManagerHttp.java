package cc.univie.goya.manager.rest;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Random;

@Slf4j
@RequiredArgsConstructor
public class VManagerHttp extends AbstractVerticle {
  private final int nodes;
  private WebClient webclient;

  @Override
  public void start(Promise<Void> startPromise) {
    this.webclient = WebClient.create(vertx, new WebClientOptions().setUserAgent("goya/manager"));

    log.info("Starting manager http");

    Router application = Router.router(vertx);

    application.get("/ping-file").handler(this::handlePing);

    Gateway gateway = new Gateway(WebClient.create(vertx), nodes);
    DictionaryApi api = new DictionaryApi(gateway);
    api.attach(application);

    application.route().failureHandler(context -> {
      log.error("failed", context.failure());
      context.response().end("internal server err");
    });

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

  private void handlePing(RoutingContext context) {
    int hash = hash();

    this.webclient.get(Gateway.port(hash), Gateway.host(hash), "/ping").send(asyncResult -> {
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
}
