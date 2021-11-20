package com.corp.concepts.quic.client.handlers;

import com.corp.concepts.quic.common.QuicLogger;
import com.corp.concepts.quic.common.UriBuilder;
import io.vertx.core.Handler;
import io.vertx.core.MultiMap;
import io.vertx.ext.web.RoutingContext;
import net.luminis.quic.QuicClientConnection;
import net.luminis.quic.QuicClientConnectionImpl;
import net.luminis.quic.QuicConnection;
import net.luminis.quic.Version;
import net.luminis.quic.client.h09.Http09Client;
import net.luminis.quic.log.Logger;
import net.luminis.quic.run.KwikCli;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Path;
import java.time.Duration;

public class HttpQuic implements Handler<RoutingContext> {
    private static HttpClient createHttpClient(QuicClientConnection quicConnection, boolean useZeroRtt) {
        HttpVersion httpVersion = loadHttp3ClientClass() ? HttpVersion.HTTP3 : HttpVersion.HTTP09;

        if (httpVersion == HttpVersion.HTTP3) {
            try {
                Class http3ClientClass = KwikCli.class.getClassLoader().loadClass("net.luminis.http3.Http3SingleConnectionClient");
                Constructor constructor = http3ClientClass.getConstructor(QuicConnection.class, Duration.class, Long.class);
                // Connection timeout and receive buffer size are not used when client is using an existing quic connection

                long maxReceiveBufferSize = 50_000_000L;
                Duration connectionTimeout = Duration.ofSeconds(60);
                HttpClient http3Client = (HttpClient) constructor.newInstance(quicConnection, connectionTimeout, maxReceiveBufferSize);
                return http3Client;
            } catch (ClassNotFoundException | NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        } else {
            return new Http09Client(quicConnection, useZeroRtt);
        }
    }

    private static boolean loadHttp3ClientClass() {
        try {
            KwikCli.class.getClassLoader().loadClass("net.luminis.http3.Http3SingleConnectionClient");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }

    }

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
            QuicClientConnectionImpl quicConnection =
                    builder.version(Version.IETF_draft_32)
                            .uri(UriBuilder.getInstance().build("https", host, port, path))
                            .logger(logger)
                            .secrets(Path.of("./quic.secret"))
                            .noServerCertificateCheck()
                            .build();

            quicConnection.connect(10_000, "hq-32");

            HttpClient httpClient = createHttpClient(quicConnection, false);
            InetSocketAddress serverAddress = quicConnection.getServerAddress();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI("https", null, serverAddress.getHostName(), serverAddress.getPort(), path, null, null))
                    .build();

            HttpResponse<String> httpResponse = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            context.response()
                    .putHeader("content-type", "text/html; charset=utf-8")
                    .end(httpResponse.body());

            quicConnection.close();
        } catch (IOException | URISyntaxException | InterruptedException exc) {
            logger.error("Error:", exc);
        }
    }

    enum HttpVersion {
        HTTP09,
        HTTP3
    }

}
