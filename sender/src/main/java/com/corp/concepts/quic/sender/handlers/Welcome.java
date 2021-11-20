package com.corp.concepts.quic.sender.handlers;

import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

public class Welcome implements Handler<RoutingContext> {
    @Override
    public void handle(RoutingContext context) {
        // Get the address of the request
        String address = context.request().connection().remoteAddress().toString();
        // Write a json response
        context.json(
                new JsonObject()
                        .put("address", address)
                        .put("message", "QUIC App")
        );
    }
}
