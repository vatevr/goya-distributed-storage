package cc.univie.goya.scenarios;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ClientVerticle extends AbstractVerticle {
  @Override
  public void start(Promise<Void> promise) {
    this.runScenarios().setHandler(promise);
  }

  private Future<Void> runScenarios() {
    Scenario.SequentialScenario scenario = new Scenario.SequentialScenario("2 uploads and 2 downloads");
    ScenariosFactory factory = new ScenariosFactory(vertx);

    scenario.add(factory.upload("baz.jpeg"))
        .add(factory.upload("foo.jpeg"))
        .add(factory.download("foo.jpeg"))
        .add(factory.download("bar.jpeg"));

    return scenario.fromBeginning().run().compose(ignored -> {
      log.info("scenario {} complete", scenario.getName());
      return Future.succeededFuture();
    });
  }
}
