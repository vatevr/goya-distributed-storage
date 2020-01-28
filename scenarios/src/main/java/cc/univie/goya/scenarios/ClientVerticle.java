package cc.univie.goya.scenarios;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.List;

@Slf4j
public class ClientVerticle extends AbstractVerticle {
  private static List<String> PAINTINGS = Arrays.asList(
      "Albrecht.jpg",
      "Bruegel.jpg",
      "Bruegel2.jpg",
      "Durer.jpg",
      "Frida.jpg",
      "Goya.jpg",
      "Magritte.jpg",
      "Raphael.jpg",
      "Raphael2.jpg",
      "Rembrandt.jpg",
      "Renoir.jpg",
      "Titan.jpg",
      "VanGogh.jpg",
      "William.jpg"
  );

  @Override
  public void start(Promise<Void> promise) {
    this.runUploadsAndDownloads()
        .compose(ignored -> runDelete())
        .compose(ignored -> runUploadAllAndRangeQuery())
        .compose(ignored -> runDeleteAll())
        .setHandler(promise);
  }

  private Future<Void> runUploadsAndDownloads() {
    Scenario.SequentialScenario sequential = new Scenario.SequentialScenario("3 uploads and 2 downloads");
    Scenarios scenarios = new Scenarios(vertx);

    sequential.add(scenarios.upload("Albrecht.jpg"))
        .add(scenarios.upload("Goya.jpg"))
        .add(scenarios.upload("Rembrandt.jpg"))
        .add(scenarios.download("Rembrandt.jpg"))
        .add(scenarios.download("Albrecht.jpg"));

    return sequential.fromBeginning().run().compose(ignored -> logAndComplete(sequential));
  }

  private Future<Void> runDelete() {
    Scenario.SequentialScenario sequential = new Scenario.SequentialScenario("3 uploads and 2 downloads");
    Scenarios scenarios = new Scenarios(vertx);

    sequential.add(scenarios.delete("Albrecht.jpg"))
        .add(scenarios.delete("Goya.jpg"))
        .add(scenarios.delete("Rembrandt.jpg"))
        .add(scenarios.download("Rembrandt.jpg"))
        .add(scenarios.download("Albrecht.jpg"));

    return sequential.fromBeginning().run().compose(ignored -> logAndComplete(sequential));
  }

  private Future<Void> runUploadAllAndRangeQuery() {
    Scenario.SequentialScenario sequential = new Scenario.SequentialScenario("Upload all, range query, delete and download some");
    Scenarios scenarios = new Scenarios(vertx);

    PAINTINGS.stream()
        .map(scenarios::upload)
        .forEach(sequential::add);

    sequential.add(scenarios.rangeQuery("Goya", "Titan"));

    return sequential.fromBeginning().run().compose(ignored -> logAndComplete(sequential));
  }

  private Future<Void> runDeleteAll() {
    Scenario.SequentialScenario sequential = new Scenario.SequentialScenario("Delete all uploads");
    Scenarios scenarios = new Scenarios(vertx);

    PAINTINGS.stream()
        .map(scenarios::delete)
        .forEach(sequential::add);

    return sequential.fromBeginning().run().compose(ignored -> logAndComplete(sequential));
  }

  private Future<Void> logAndComplete(Scenario.SequentialScenario sequential) {
    log.info("sequential scenario {} complete", sequential.getName());
    return Future.succeededFuture();
  }
}
