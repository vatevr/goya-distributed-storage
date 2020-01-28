package cc.univie.goya.scenarios;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ClientVerticle extends AbstractVerticle {
  @Override
  public void start(Promise<Void> promise) {
    promise.complete();
    this.runUploadsAndDownloads();
  }

  private Future<Void> runUploadsAndDownloads() {
    Scenario.SequentialScenario sequential = new Scenario.SequentialScenario("3 uploads and 3 downloads");
    ScenariosFactory scenario = new ScenariosFactory(vertx);

    sequential.add(scenario.upload("baz.jpeg"))
        .add(scenario.upload("foo.jpeg"))
        .add(scenario.upload("baz.jpeg"))
        .add(scenario.download("foo.jpeg"))
        .add(scenario.download("baz.jpeg"));

    return sequential.fromBeginning().run().compose(ignored -> {
      log.info("scenario {} complete", sequential.getName());
      return Future.succeededFuture();
    });
  }
}
