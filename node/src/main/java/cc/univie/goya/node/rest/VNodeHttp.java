package cc.univie.goya.node.rest;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class VNodeHttp extends AbstractVerticle {
  @Override
  public void start(Promise<Void> startPromise) {
    log.info("starting http");

    Router application = Router.router(vertx);

    application.get("/ping").handler(context -> this.handlePing(context));

    vertx.createHttpServer()
        .requestHandler(application)
        .listen(8500, asyncResult -> {
          if (asyncResult.succeeded()) {
            startPromise.complete();
          } else {
            log.error("failed to deploy http", asyncResult.cause());
          }
        });
  }

  private void handlePing(RoutingContext context) {
    log.info("hello request received");

    context.response().setStatusCode(200).end("pong");
  }
}
