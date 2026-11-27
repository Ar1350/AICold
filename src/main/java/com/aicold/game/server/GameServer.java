package com.aicold.game.server;

import com.aicold.game.engine.GameEngine;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.Executors;

/**
 * Lightweight JDK HTTP server: static web assets + game API.
 */
public class GameServer {

    private final int port;
    private final Path webRoot;
    private final GameEngine engine;
    private HttpServer server;

    public GameServer(int port, Path webRoot, GameEngine engine) {
        this.port = port;
        this.webRoot = webRoot;
        this.engine = engine;
    }

    public void start() throws IOException {
        if (!Files.isDirectory(webRoot)) {
            throw new IOException("Web root not found: " + webRoot.toAbsolutePath()
                    + " (run from the project root so ./web exists)");
        }

        server = HttpServer.create(new InetSocketAddress(port), 0);
        GameApiHandler api = new GameApiHandler(engine);
        StaticFileHandler files = new StaticFileHandler(webRoot);

        server.createContext("/api", api);
        server.createContext("/", files);
        server.setExecutor(Executors.newCachedThreadPool());
        server.start();

        System.out.println("Elimination Arena listening on http://localhost:" + port);
        System.out.println("Serving static files from " + webRoot.toAbsolutePath());
        System.out.println("Open the URL in your browser. Ctrl+C to stop.");
    }

    public void stop() {
        if (server != null) {
            server.stop(0);
        }
    }

    public int getPort() {
        return port;
    }
}
