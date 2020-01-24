package cc.univie.goya.manager.rest;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class VManagerHttp extends AbstractVerticle {
    @Override
    public void start(Promise<Void> startPromise) throws Exception {
        log.info("Starting manager http");

        Router application = Router.router(vertx);

        application.get("/hello").handler(context->this.handleHello(context));

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

    private void handleHello(RoutingContext context){
        log.info("hey there");

        context.response().setStatusCode(200).end("holla");
    }
}
