package com.corp.concepts.quic.sender;

import com.corp.concepts.quic.sender.handlers.HttpQuic;
import com.corp.concepts.quic.sender.handlers.Stream;
import com.corp.concepts.quic.sender.handlers.Welcome;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.ext.web.Router;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MainVerticle extends AbstractVerticle {
    Logger logger = LogManager.getLogger(MainVerticle.class);

    @Override
    public void start(Promise<Void> startPromise) throws Exception {
        // Create a Router
        Router router = Router.router(vertx);

        router.route("/").handler(new Welcome());

        router.route("/stream").handler(new Stream());

        router.route("/http").handler(new HttpQuic());

        vertx.createHttpServer().requestHandler(router)
                .listen(8888, http -> {
                    if (http.succeeded()) {
                        startPromise.complete();
                        logger.info("HTTP server started on port 8888");
                    } else {
                        startPromise.fail(http.cause());
                    }
                });
    }

}
