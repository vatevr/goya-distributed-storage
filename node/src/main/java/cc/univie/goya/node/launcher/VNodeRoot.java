package cc.univie.goya.node.launcher;

import cc.univie.goya.node.rest.VNodeHttp;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Promise;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

@Slf4j
public class VNodeRoot extends AbstractVerticle {
  private static final String INSTANCE = "INSTANCE";
  @Override
  public void start(Promise<Void> promise) {
    int instance = Optional.ofNullable(System.getenv(INSTANCE))
        .map(Integer::valueOf)
        .orElseThrow(UnknownInstance::new);

    log.info("starting instance {} root verticle", instance);

    vertx.deployVerticle(() -> new VNodeHttp(instance ), new DeploymentOptions().setInstances(1), asyncResult -> {
      if (asyncResult.succeeded()) {
        promise.complete();
      } else {
        log.error("failed to deploy node", asyncResult.cause());
      }
    });

  }

  private static class UnknownInstance extends RuntimeException {
    UnknownInstance() {
      super("instance number was not set");
    }
  }
}
