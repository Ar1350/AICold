# Elimination Arena

A browser-playable three-player chase game. Game rules, collisions, AI, and elimination logic run on a **Java** HTTP server; the browser renders a canvas client and sends keyboard input.

## Rules

Three fighters move in a 2D arena:

| Touch | Result |
|-------|--------|
| Player **1** touches **2** | **2** is eliminated |
| Player **2** touches **3** | **3** is eliminated |
| Player **3** touches **1** | **1** is eliminated |

**Win condition:** last survivor wins. With two players left, one still eliminates the other on contact, so the match continues until one remains.

## Requirements

- **Java 17+** (developed and tested with OpenJDK 21)
- A modern browser

No Maven/Gradle required — the JDK alone is enough.

## How to run

From the repository root:

```bash
# Compile
mkdir -p out
javac -d out $(find src/main/java -name '*.java')

# Start server (serves web/ and the game API on port 8080)
java -cp out com.aicold.game.Main
```

Optional flags:

```bash
java -cp out com.aicold.game.Main --port 9090
java -cp out com.aicold.game.Main --web web
```

Open **http://localhost:8080** in your browser.

Or use the helper script:

```bash
./run.sh
```

## How to play

- You control one fighter (**Player 1** by default) with **W A S D** or **arrow keys**.
- The other two are AI (chase prey, flee predators).
- Switch who you control with the buttons in the sidebar.
- When someone is eliminated, it appears in the log.
- When one fighter remains, use **Play again** / **Restart**.

## Project structure

```
src/main/java/com/aicold/game/
  Main.java                 # Entry point
  model/                    # Player, Vec2, InputState, events, enums
  engine/
    GameRules.java          # Who eliminates whom
    CollisionDetector.java  # Circle-circle hits
    AiController.java       # Chase / flee AI
    GameEngine.java         # Simulation loop & JSON state
  server/
    GameServer.java         # JDK HttpServer wiring
    GameApiHandler.java     # /api/state, /api/tick, /api/restart, /api/control
    StaticFileHandler.java  # Serves web/
web/
  index.html
  css/style.css
  js/                       # api, input, render, ui, main
```

## API (for reference)

| Method | Path | Purpose |
|--------|------|---------|
| `GET` | `/api/state` | Current match JSON |
| `POST` | `/api/tick` | Advance one frame with `{up,down,left,right}` |
| `POST` | `/api/restart` | Reset the match |
| `POST` | `/api/control` | Choose human player `{ "player": "P1" }` |
