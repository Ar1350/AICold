package com.aicold.game;

import com.aicold.game.engine.GameEngine;
import com.aicold.game.server.GameServer;

import java.nio.file.Path;

/**
 * Entry point: starts the HTTP server that hosts the browser game.
 *
 * <pre>
 *   javac -d out $(find src/main/java -name '*.java')
 *   java -cp out com.aicold.game.Main
 * </pre>
 */
public final class Main {

    private static final int DEFAULT_PORT = 8080;

    private Main() {
    }

    public static void main(String[] args) throws Exception {
        int port = DEFAULT_PORT;
        Path webRoot = Path.of("web");

        for (int i = 0; i < args.length; i++) {
            if ("--port".equals(args[i]) && i + 1 < args.length) {
                port = Integer.parseInt(args[++i]);
            } else if ("--web".equals(args[i]) && i + 1 < args.length) {
                webRoot = Path.of(args[++i]);
            }
        }

        GameEngine engine = new GameEngine();
        GameServer server = new GameServer(port, webRoot, engine);
        server.start();

        Runtime.getRuntime().addShutdownHook(new Thread(server::stop));
        Thread.currentThread().join();
    }
}
