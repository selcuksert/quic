package com.corp.concepts.quic.common;

import org.apache.http.client.utils.URIBuilder;

import java.net.URI;
import java.net.URISyntaxException;

public class UriBuilder {

    private static UriBuilder instance;
    private URIBuilder builder;

    private UriBuilder() {
        builder = new URIBuilder();
    }

    public static UriBuilder getInstance() {
        if (instance == null) {
            instance = new UriBuilder();
        }

        return instance;
    }

    public URI build(String scheme, String host, int port, String path) throws URISyntaxException {
        builder.setHost(host).setPort(port).setScheme(scheme).setPath(path);
        return builder.build();
    }
}
