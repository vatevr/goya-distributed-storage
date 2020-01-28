package cc.univie.goya.scenarios;

import io.vertx.core.Future;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

public interface Scenario {
  Future<Void> run();

  @Slf4j
  class SequentialScenario implements Scenario {
    private final List<Scenario> scenarios = new ArrayList<>();
    private Iterator<Scenario> iterator = null;
    @Getter
    private final String name;

    public SequentialScenario(String name) {
      this.name = name;
    }

    public SequentialScenario fromBeginning() {
      this.iterator = Collections.unmodifiableList(this.scenarios).iterator();
      return this;
    }

    @Override
    public Future<Void> run() {
      if (Objects.isNull(this.iterator)) {
        return Future.failedFuture("Iterator not specified");
      }

      if (!this.iterator.hasNext()) {
        return Future.succeededFuture();
      }

      Scenario current = iterator.next();

      return current.run().compose(res -> this.run().recover(err -> {
        log.error("failed to run the scenario", err);
        return Future.failedFuture(err);
      }));
    }

    public SequentialScenario add(Scenario scenario) {
      this.scenarios.add(scenario);
      return this;
    }

  }
}
