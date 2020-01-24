package cc.univie.goya.manager.launcher;

import cc.univie.goya.manager.rest.VManagerHttp;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Promise;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class VManagerRoot extends AbstractVerticle {
    @Override
    public void start(Promise<Void> startPromise) throws Exception {
        log.info("Managaer started");

        vertx.deployVerticle(()->new VManagerHttp(), new DeploymentOptions().setInstances(1), asyncResult ->{
            if(asyncResult.succeeded()){
                startPromise.complete();
            } else {
                log.error("failed to deploy manager", asyncResult.cause());
            }
        });
    }

}
