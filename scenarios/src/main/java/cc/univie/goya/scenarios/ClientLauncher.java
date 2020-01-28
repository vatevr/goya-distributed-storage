package cc.univie.goya.scenarios;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.impl.launcher.VertxCommandLauncher;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ClientLauncher {
  public static void main(String[] args) {
    Vertx vertx = Vertx.vertx();

    vertx.deployVerticle(new ClientVerticle(), new DeploymentOptions(), whenDeployed -> {
      String deploymentId = whenDeployed.result();
      log.info("deployed {}", deploymentId);

      vertx.undeploy(deploymentId, res -> {
        log.info("gracefully shutting down the client launcher");
        vertx.close();
      });
    });
  }
}
