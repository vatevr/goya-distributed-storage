package cc.univie.goya.scenarios;

import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.codec.BodyCodec;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

@Slf4j
class Scenarios {
  private final Vertx vertx;
  private final WebClient webclient;
  private final String path;
  private final String host;
  private final int port;

  Scenarios(Vertx vertx) {
    this.vertx = vertx;
    this.webclient = WebClient.create(this.vertx);
    this.path = "scenarios/fixtures/";


    this.host = Optional.ofNullable(System.getenv("HOST")).orElse("localhost");
    this.port = Optional.ofNullable(System.getenv("PORT")).map(Integer::valueOf).orElse(7500);

    log.info("connecting to {}:{}", host, port);
  }

  Scenario upload(String key) {
    return () -> {
      log.info("uploading {}", key);
      Promise<HttpResponse<JsonObject>> promise = Promise.promise();

      vertx.fileSystem().readFile(this.path + "/uploads/" + key, ar -> {
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

      Promise<HttpResponse<Buffer>> promise = Promise.promise();

      webclient.get(port, host, "/storage/" + key)
          .as(BodyCodec.buffer())
          .send(promise);

      return promise.future().compose(response -> {
        Promise<Void> whenWritten = Promise.promise();
        log.info("received response");
        vertx.fileSystem().writeFile(this.path + "/downloads/" + key, response.body(), whenWritten);
        return whenWritten.future();
      })
      .compose(ignores -> {
        log.info("download complete");
        return Future.succeededFuture(ignores);
      })
      .recover(err -> {
        log.error("failed to download or save file", err);
        return Future.failedFuture(err);
      })
      .mapEmpty();
    };
  }

  Scenario delete(String key) {
    return () -> {
      Promise<HttpResponse<JsonObject>> promise = Promise.promise();
      this.webclient.delete(port, host, "/storage/" + key)
          .as(BodyCodec.jsonObject())
          .send(promise);

      return promise.future().compose(response -> {
        log.info("received response from delete {}", response.body());
        return Future.succeededFuture();
      }).mapEmpty();
    };
  }

  Scenario rangeQuery(String from, String to) {
    return () -> {
      Promise<HttpResponse<JsonObject>> promise = Promise.promise();
      this.webclient.get(port, host, "/storage")
          .addQueryParam("from", from)
          .addQueryParam("to", to)
          .as(BodyCodec.jsonObject())
          .send(promise);

      return promise.future().compose(response -> {
        log.info("followings keys are between {} and {} {}", from, to, response.body().getJsonArray("keys"));
        return Future.succeededFuture();
      }).mapEmpty();
    };
  }
}