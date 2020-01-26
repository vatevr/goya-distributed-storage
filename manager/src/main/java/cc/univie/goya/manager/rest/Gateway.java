package cc.univie.goya.manager.rest;

import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.HttpRequest;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.codec.BodyCodec;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@RequiredArgsConstructor
public class Gateway {
  private static final int DEFAULT_PORT = 8500;
  private final WebClient webclient;
  private final int nodes;

  static int port(int hash) {
    return DEFAULT_PORT + hash;
  }

  static String host(int hash) {
    return "node" + hash;
  }

  /**
   * Hash function to hash request to node
   */
  private int hashCode(String objectKey) {
    return objectKey.hashCode() % nodes;
  }

  Future<String> uploadToNode(@NonNull String objectKey, @NonNull Buffer buffer) {
    Promise<HttpResponse<Buffer>> whenResponded = Promise.promise();

    int hash = hashCode(objectKey);

    this.webclient
        .post(port(hash), host(hash), "/object/" + objectKey)
        .sendBuffer(buffer, whenResponded);

    return whenResponded.future().map(HttpResponse::bodyAsString);
  }

  public Future<JsonObject> deleteFromNode(String objectKey) {
    Promise<HttpResponse<Buffer>> whenResponded = Promise.promise();

    int hash = hashCode(objectKey);

    this.webclient
        .delete(port(hash), host(hash), "/object/" + objectKey)
        .send(whenResponded);

    return whenResponded.future().map(HttpResponse::bodyAsJsonObject);
  }

  public Future<Buffer> getFromNode(String objectKey) {
    Promise<HttpResponse<Buffer>> whenResponded = Promise.promise();

    int hash = hashCode(objectKey);

    buildGet(hash, "object/" + objectKey)
        .send(whenResponded);

    return whenResponded.future().map(HttpResponse::bodyAsBuffer);
  }

  public Future<JsonObject> keysFromNodes() {
    List<Future> requests = IntStream.range(0, nodes)
        .mapToObj(node -> buildGet(node, "/keys").as(BodyCodec.jsonObject()))
        .map(request -> {
          Promise<HttpResponse<JsonObject>> whenSent = Promise.promise();
          request.send(whenSent);
          return whenSent.future();
        })
        .collect(Collectors.toList());

    return CompositeFuture.join(requests)
        .map(CompositeFuture::<HttpResponse<JsonObject>>list)
        .map(responses -> responses.stream().map(response -> response.bodyAsJsonObject().getJsonArray("keys")))
        .map(keysStream -> {
          JsonObject jsonObject = new JsonObject();
          JsonArray aggregate = new JsonArray();
          keysStream.forEach(aggregate::addAll);

          return jsonObject.put("kes", aggregate);
        });
  }

  private HttpRequest<Buffer> buildGet(int node, String s) {
    return this.webclient.get(port(node), host(node), s);
  }
}
