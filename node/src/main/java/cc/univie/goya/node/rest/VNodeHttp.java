package cc.univie.goya.node.rest;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;

@Slf4j
@RequiredArgsConstructor
public class VNodeHttp extends AbstractVerticle {
    private static final int DEFAULT_PORT = 8500;
    private final int instance;
    private final String storagePath = "/storage";

    @Override
    public void start(Promise<Void> startPromise) {
        log.info("starting http");

        Router application = Router.router(vertx);

        application.get("/ping").handler(this::handlePing);
        application.post("/object/:key")
                .handler(BodyHandler.create(true).setBodyLimit(-1))
                .handler(this::storeObject);
        application.delete("/object/:key").handler(this::deleteObject);
        application.get("/object/:key").handler(this::searchObject);
        application.get("/keys").handler(this::storageKeys);

        int port = DEFAULT_PORT + instance;

        vertx.createHttpServer()
                .requestHandler(application)
                .listen(port, asyncResult -> {
                    if (asyncResult.succeeded()) {
                        startPromise.complete();
                    } else {
                        log.error("failed to deploy http", asyncResult.cause());
                    }
                });
    }

    private void handlePing(RoutingContext context) {
        log.info("hello request received");

        context.response().setStatusCode(200).end(String.format("pong from %d", instance));
    }

    private void storeObject(RoutingContext context) {
        log.info("storing the object");
        String key = context.pathParam("key");
        Buffer body = Objects.requireNonNull(context.getBody());

        vertx.fileSystem().writeFile(String.format("%s/%s", storagePath, key), body, asyncResult -> {
            if (asyncResult.succeeded()) {
                context.response().setStatusCode(201).end("saved file");
            } else {
                log.error("failed to save the file", asyncResult.cause());
                context.response()
                    .setStatusCode(500)
                    .end("internal server error");
            }
        });

    }

    private void deleteObject(RoutingContext context){
        log.info("deleting the object");
        String key = context.pathParam("key");
        vertx.fileSystem().delete(String.format("%s%s%s",storagePath, "/", key), asyncResult ->{
            if(asyncResult.succeeded()){
                context.response()
                    .setStatusCode(202)
                    .end(new JsonObject().put("message", key + " successfully deleted").encode());
            } else{
                log.error("failed to delete", asyncResult.cause());
                context.response()
                    .setStatusCode(400)
                    .end(new JsonObject().put("message", key + " doesn't exist").encode());
            }
        });
    }

    private void searchObject(RoutingContext context) {
        log.info("seraching for the object");
        String key = context.pathParam("key");
        vertx.fileSystem().readFile(String.format("%s%s%s", storagePath, "/", key), asyncResponse -> {
            if (asyncResponse.succeeded()) {
                log.info("file {} found writing the response", key);
                context.response().setStatusCode(200).end(asyncResponse.result());
            } else {
                log.error("file not found");
                context.response()
                    .setStatusCode(400)
                    .end(new JsonObject().put("message", key + " doesn't exist").encode());
            }
        });
    }

    private void storageKeys(RoutingContext context) {
        log.info("scanning the storage");
        vertx.fileSystem().readDir(storagePath, asyncResult -> {
            if (asyncResult.succeeded()) {
                final JsonArray keys = new JsonArray();
                Function<List<String>, String> lastElement = list -> list.get(list.size() - 1);
                Predicate<List<String>> empty = List::isEmpty;

                asyncResult.result().stream()
                        .map(key -> key.split("/"))
                        .map(Arrays::asList)
                        .filter(empty.negate())
                        .map(lastElement)
                        .forEach(keys::add);

                context.response().setStatusCode(200).end(new JsonObject().put("keys", keys).encode());
            } else {
                log.error("could not find files");
                context.fail(asyncResult.cause());
            }

        });
    }

    public static void main(String[] args) {
        String lala = "/storage/lalala";
        String lala1 = "/storage/lalal1";
        List<String> strings = Arrays.asList(lala, lala1);

        Function<List<String>, String> lastElement = list -> list.get(list.size() - 1);

        strings.stream()
                .map(key -> key.split("/"))
                .map(Arrays::asList)
                .filter(list -> !list.isEmpty())
                .map(lastElement)
                .forEach(System.out::println);
    }
}