package com.corp.concepts.quic.client.handlers;

import com.corp.concepts.quic.common.QuicLogger;
import com.corp.concepts.quic.common.UriBuilder;
import io.vertx.core.Handler;
import io.vertx.core.MultiMap;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import net.luminis.quic.QuicClientConnectionImpl;
import net.luminis.quic.QuicStream;
import net.luminis.quic.Version;
import net.luminis.quic.log.Logger;
import org.apache.commons.io.IOUtils;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

public class Stream implements Handler<RoutingContext> {
    @Override
    public void handle(RoutingContext context) {
        Logger logger = new QuicLogger();
        logger.logPackets(true);
        logger.logInfo(true);

        try {
            // Get the query parameters
            MultiMap queryParams = context.queryParams();
            String host = queryParams.contains("host") ? queryParams.get("host") : "localhost";
            int port = queryParams.contains("port") ? Integer.parseInt(queryParams.get("port")) : 4433;
            String path = queryParams.contains("path") ? queryParams.get("path") : "/index.html";

            QuicClientConnectionImpl.Builder builder = QuicClientConnectionImpl.newBuilder();
            QuicClientConnectionImpl connection =
                    builder.version(Version.IETF_draft_32)
                            .uri(UriBuilder.getInstance().build("https", host, port, path))
                            .noServerCertificateCheck()
                            .secrets(Path.of("./quic.secret"))
                            .logger(logger)
                            .build();

            connection.connect(10_000, "hq-32");

            QuicStream stream = connection.createStream(true);

            BufferedOutputStream outputStream = new BufferedOutputStream(stream.getOutputStream());
            outputStream.write(("GET " + path + " \r\n").getBytes(StandardCharsets.UTF_8));
            outputStream.flush();

            context.vertx().executeBlocking(handler -> {
                try {
                    String streamResponse = IOUtils.toString(stream.getInputStream(), StandardCharsets.UTF_8);
                    handler.complete();

                    if (path.contains(".html")) {
                        context.response()
                                .putHeader("content-type", "text/html; charset=utf-8")
                                .end(streamResponse);
                    } else {
                        context.json(
                                new JsonObject()
                                        .put("status", "completed")
                                        .put("message", streamResponse));
                    }
                } catch (IOException e) {
                    logger.error("Error:", e);
                    handler.fail(e);
                    context.json(
                            new JsonObject()
                                    .put("status", "failed")
                                    .put("message", e.getMessage())
                    );
                }
            });
        } catch (Exception e) {
            logger.error("Error:", e);
        }
    }
}
