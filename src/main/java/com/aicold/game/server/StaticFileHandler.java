package com.aicold.game.server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

/**
 * Serves files from the web/ directory (or classpath fallback).
 */
public class StaticFileHandler implements HttpHandler {

    private static final Map<String, String> CONTENT_TYPES = Map.of(
            ".html", "text/html; charset=utf-8",
            ".css", "text/css; charset=utf-8",
            ".js", "application/javascript; charset=utf-8",
            ".png", "image/png",
            ".svg", "image/svg+xml",
            ".ico", "image/x-icon"
    );

    private final Path webRoot;

    public StaticFileHandler(Path webRoot) {
        this.webRoot = webRoot;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            send(exchange, 405, "text/plain; charset=utf-8", "Method Not Allowed".getBytes());
            return;
        }

        String path = exchange.getRequestURI().getPath();
        if (path == null || path.isBlank() || "/".equals(path)) {
            path = "/index.html";
        }

        // Prevent path traversal
        if (path.contains("..")) {
            send(exchange, 400, "text/plain; charset=utf-8", "Bad Request".getBytes());
            return;
        }

        Path file = webRoot.resolve(path.substring(1)).normalize();
        if (!file.startsWith(webRoot.normalize()) || !Files.isRegularFile(file)) {
            send(exchange, 404, "text/plain; charset=utf-8", "Not Found".getBytes());
            return;
        }

        String contentType = contentTypeFor(file.getFileName().toString());
        byte[] body = Files.readAllBytes(file);
        send(exchange, 200, contentType, body);
    }

    private static String contentTypeFor(String name) {
        int dot = name.lastIndexOf('.');
        if (dot >= 0) {
            String ext = name.substring(dot).toLowerCase();
            String type = CONTENT_TYPES.get(ext);
            if (type != null) {
                return type;
            }
        }
        return "application/octet-stream";
    }

    static void send(HttpExchange exchange, int status, String contentType, byte[] body) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", contentType);
        exchange.getResponseHeaders().set("Cache-Control", "no-cache");
        exchange.sendResponseHeaders(status, body.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(body);
        }
    }

    /** Unused helper kept for classpath experiments. */
    @SuppressWarnings("unused")
    private byte[] readClasspath(String resource) throws IOException {
        try (InputStream in = getClass().getResourceAsStream(resource)) {
            if (in == null) {
                return null;
            }
            return in.readAllBytes();
        }
    }
}
