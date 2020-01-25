package cc.univie.goya.node.rest;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.buffer.Buffer;
import io.vertx.ext.web.FileUpload;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import sun.reflect.generics.tree.VoidDescriptor;

import java.util.Objects;
import java.util.Set;

@Slf4j
@RequiredArgsConstructor
public class VNodeHttp extends AbstractVerticle {
    private static final int DEFAULT_PORT = 8500;
    private final int instance;
//    private final String storagePath = "/Users/tamarakhachaturyan/Documents/Workspace/01577205/goya-distributed-storage/node/storage";
    private final String storagePath = "/storage";

    @Override
    public void start(Promise<Void> startPromise) {
        log.info("starting http");

        Router application = Router.router(vertx);

        application.get("/ping").handler(this::handlePing);
        application.post("/object/:key")
                .handler(BodyHandler.create(true).setBodyLimit(-1))
                .handler(this::storeObject);

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

        Set<FileUpload> fileUploads = context.fileUploads();

        vertx.fileSystem().writeFile(String.format("%s/%s", storagePath, key), body, asyncResult -> {
            if (asyncResult.succeeded()) {
                context.response().setStatusCode(201).end("save file");
            } else {
                log.error("failed to save the file", asyncResult.cause());
                context.fail(asyncResult.cause());
            }
        });

    }
}
