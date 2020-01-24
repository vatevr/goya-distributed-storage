package cc.univie.goya.node.launcher;

import cc.univie.goya.node.rest.VNodeHttp;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Promise;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class VNodeRoot extends AbstractVerticle {
  @Override
  public void start(Promise<Void> promise) throws Exception {
    log.info("Root verticle starting");

    vertx.deployVerticle(() -> new VNodeHttp(), new DeploymentOptions().setInstances(1), asyncResult -> {
      if (asyncResult.succeeded()) {
        promise.complete();
      } else {
        log.error("failed to deploy node", asyncResult.cause());
      }
    });

  }
}
