package com.aicold.game.server;

import com.aicold.game.engine.GameEngine;
import com.aicold.game.model.InputState;
import com.aicold.game.model.PlayerId;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * REST-ish JSON API for tick / state / restart / control selection.
 */
public class GameApiHandler implements HttpHandler {

    private final GameEngine engine;

    public GameApiHandler(GameEngine engine) {
        this.engine = engine;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        String method = exchange.getRequestMethod();

        // CORS for local experimentation
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");

        if ("OPTIONS".equalsIgnoreCase(method)) {
            exchange.sendResponseHeaders(204, -1);
            exchange.close();
            return;
        }

        try {
            if ("/api/state".equals(path) && "GET".equalsIgnoreCase(method)) {
                json(exchange, 200, engine.toJson());
                return;
            }
            if ("/api/tick".equals(path) && "POST".equalsIgnoreCase(method)) {
                InputState input = parseInput(readBody(exchange));
                engine.tick(input);
                json(exchange, 200, engine.toJson());
                return;
            }
            if ("/api/restart".equals(path) && "POST".equalsIgnoreCase(method)) {
                engine.reset();
                json(exchange, 200, engine.toJson());
                return;
            }
            if ("/api/control".equals(path) && "POST".equalsIgnoreCase(method)) {
                String body = readBody(exchange);
                PlayerId id = parseControl(body);
                if (id != null) {
                    engine.setHumanControlled(id);
                }
                json(exchange, 200, engine.toJson());
                return;
            }
            json(exchange, 404, "{\"error\":\"Not Found\"}");
        } catch (Exception e) {
            String msg = e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage();
            json(exchange, 400, "{\"error\":\"" + msg.replace("\"", "'") + "\"}");
        }
    }

    private static void json(HttpExchange exchange, int status, String body) throws IOException {
        StaticFileHandler.send(exchange, status, "application/json; charset=utf-8",
                body.getBytes(StandardCharsets.UTF_8));
    }

    private static String readBody(HttpExchange exchange) throws IOException {
        try (InputStream in = exchange.getRequestBody()) {
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    /**
     * Minimal JSON parse for {"up":bool,"down":bool,"left":bool,"right":bool}.
     */
    static InputState parseInput(String body) {
        InputState input = new InputState();
        if (body == null || body.isBlank()) {
            return input;
        }
        input.up = truthy(body, "up");
        input.down = truthy(body, "down");
        input.left = truthy(body, "left");
        input.right = truthy(body, "right");
        return input;
    }

    static PlayerId parseControl(String body) {
        if (body == null) {
            return null;
        }
        for (PlayerId id : PlayerId.values()) {
            if (body.contains("\"" + id.name() + "\"") || body.contains("\"player\":" + id.getNumber())) {
                return id;
            }
        }
        return null;
    }

    private static boolean truthy(String json, String key) {
        String patternTrue = "\"" + key + "\":true";
        String patternTrueSpaced = "\"" + key + "\": true";
        return json.contains(patternTrue) || json.contains(patternTrueSpaced);
    }
}
