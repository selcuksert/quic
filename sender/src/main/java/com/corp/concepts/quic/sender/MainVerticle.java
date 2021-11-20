package com.corp.concepts.quic.sender;

import com.corp.concepts.quic.sender.handlers.HttpQuic;
import com.corp.concepts.quic.sender.handlers.Stream;
import com.corp.concepts.quic.sender.handlers.Welcome;
import io.vertx.config.ConfigRetriever;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
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

        ConfigRetriever retriever = ConfigRetriever.create(vertx);

        retriever.getConfig(json -> {
            JsonObject result = json.result();
            JsonObject server = result.getJsonObject("server");
            int port = server.getInteger("port");

            vertx.createHttpServer().requestHandler(router)
                    .listen(port, http -> {
                        if (http.succeeded()) {
                            startPromise.complete();
                            logger.info("HTTP server started on port: {}", http.result().actualPort());
                        } else {
                            startPromise.fail(http.cause());
                        }
                    });
        });
    }

}
