package com.corp.concepts.quic.sender.handlers;

import com.corp.concepts.quic.common.QuicLogger;
import io.vertx.core.Handler;
import io.vertx.core.MultiMap;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import net.luminis.quic.QuicClientConnectionImpl;
import net.luminis.quic.QuicStream;
import net.luminis.quic.Version;
import net.luminis.quic.log.Logger;
import org.apache.commons.io.IOUtils;
import org.apache.http.client.utils.URIBuilder;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

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
            String port = queryParams.contains("port") ? queryParams.get("port") : "4433";
            String path = queryParams.contains("path") ? queryParams.get("path") : "/index.html";

            URIBuilder uriBuilder = new URIBuilder();
            uriBuilder.setHost(host).setPort(Integer.parseInt(port)).setScheme("https");

            QuicClientConnectionImpl.Builder builder = QuicClientConnectionImpl.newBuilder();
            QuicClientConnectionImpl connection =
                    builder.version(Version.IETF_draft_32)
                            .uri(uriBuilder.build())
                            .noServerCertificateCheck()
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
                    context.json(
                            new JsonObject()
                                    .put("status", "completed")
                                    .put("message", streamResponse)
                    );
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
