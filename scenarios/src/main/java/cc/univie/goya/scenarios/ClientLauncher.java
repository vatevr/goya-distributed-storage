package cc.univie.goya.scenarios;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ClientLauncher {
  public static void main(String[] args) {
    Vertx vertx = Vertx.vertx();

    vertx.deployVerticle(new ClientVerticle(), new DeploymentOptions(), ar -> {
      if (ar.succeeded()) {
        String deploymentId = ar.result();
        log.info("deployed {}", deploymentId);

        vertx.undeploy(deploymentId, res -> {
          log.info("gracefully shutting down the client launcher");
          vertx.close();
        });
      } else {
        log.error("failed to deploy due to", ar.cause());
      }
    });
  }
}
