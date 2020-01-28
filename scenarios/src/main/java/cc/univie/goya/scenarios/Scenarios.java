package cc.univie.goya.scenarios;

import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.core.spi.json.JsonCodec;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.codec.BodyCodec;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

@Slf4j
class ScenariosFactory {
  private final Vertx vertx;
  private final WebClient webclient;
  private final String path;

  ScenariosFactory(Vertx vertx) {
    this.vertx = vertx;
    this.webclient = WebClient.create(this.vertx);
    this.path = "scenarios/fixtures/";
  }

  Scenario upload(String key) {
    return () -> {
      log.info("uploading {}", key);
      Promise<HttpResponse<JsonObject>> promise = Promise.promise();

      String host = Optional.ofNullable(System.getenv("HOST")).orElse("localhost");
      int port = Optional.ofNullable(System.getenv("PORT")).map(Integer::valueOf).orElse(7500);

      vertx.fileSystem().readFile(this.path + key, ar -> {
        if (ar.succeeded()) {
          webclient.post(port, host, "/storage/" + key)
              .as(BodyCodec.jsonObject())
              .sendBuffer(ar.result(), promise);
        } else {
          log.error("failed to fetch file", ar.cause());
          promise.fail(ar.cause());
        }
      });


      return promise.future().compose(response -> {
        log.info("received response {}", response.body());
        return Future.succeededFuture(response);
      }).mapEmpty();
    };
  }

  Scenario download(String key) {
    return () -> {
      log.info("downloading {}", key);
      return Future.succeededFuture();
    };
  }
}