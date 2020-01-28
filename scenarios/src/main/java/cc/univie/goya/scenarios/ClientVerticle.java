package cc.univie.goya.scenarios;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;

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
    promise.complete();
    this.runUploadsAndDownloads();
  }

  private Future<Void> runUploadsAndDownloads() {
    List<String> filesToSelect = Collections.unmodifiableList(PAINTINGS);

    Scenario.SequentialScenario sequential = new Scenario.SequentialScenario("3 uploads and 2 downloads");
    Scenarios scenarios = new Scenarios(vertx);

    sequential.add(scenarios.upload("Albrecht.jpg"))
        .add(scenarios.upload("Goya.jpg"))
        .add(scenarios.upload("Rembrandt.jpg"))
        .add(scenarios.download("Rembrandt.jpg"))
        .add(scenarios.download("Albrecht.jpg"));

    return sequential.fromBeginning().run().compose(ignored -> {
      log.info("sequential scenario {} complete", sequential.getName());
      return Future.succeededFuture();
    });
  }

  private Future<Void> runDelete() {
    List<String> filesToSelect = Collections.unmodifiableList(PAINTINGS);

    Scenario.SequentialScenario sequential = new Scenario.SequentialScenario("3 uploads and 2 downloads");
    Scenarios scenarios = new Scenarios(vertx);

    sequential.add(scenarios.delete("Albrecht.jpg"))
        .add(scenarios.delete("Goya.jpg"))
        .add(scenarios.delete("Rembrandt.jpg"))
        .add(scenarios.download("Rembrandt.jpg"))
        .add(scenarios.download("Albrecht.jpg"));

    return sequential.fromBeginning().run().compose(ignored -> {
      log.info("sequential scenario {} complete", sequential.getName());
      return Future.succeededFuture();
    });
  }


}
