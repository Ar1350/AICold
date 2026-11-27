package com.aicold.game.engine;

import com.aicold.game.model.EliminationEvent;
import com.aicold.game.model.GameStatus;
import com.aicold.game.model.InputState;
import com.aicold.game.model.Player;
import com.aicold.game.model.PlayerId;
import com.aicold.game.model.Vec2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Owns match state, advances simulation, applies rules and AI.
 */
public class GameEngine {

    public static final double ARENA_WIDTH = 800;
    public static final double ARENA_HEIGHT = 560;
    public static final double PLAYER_RADIUS = 22;
    public static final double PLAYER_SPEED = 180;
    public static final double DT = 1.0 / 60.0;

    private final CollisionDetector collisionDetector = new CollisionDetector();
    private final AiController aiController = new AiController();

    private final Map<PlayerId, Player> players = new EnumMap<>(PlayerId.class);
    private final List<EliminationEvent> eliminations = new ArrayList<>();
    private PlayerId humanControlled = PlayerId.P1;
    private GameStatus status = GameStatus.RUNNING;
    private PlayerId winner;
    private long tick;

    public GameEngine() {
        reset();
    }

    public synchronized void reset() {
        players.clear();
        players.put(PlayerId.P1, new Player(PlayerId.P1, new Vec2(140, 280), PLAYER_RADIUS, PLAYER_SPEED));
        players.put(PlayerId.P2, new Player(PlayerId.P2, new Vec2(400, 120), PLAYER_RADIUS, PLAYER_SPEED));
        players.put(PlayerId.P3, new Player(PlayerId.P3, new Vec2(660, 420), PLAYER_RADIUS, PLAYER_SPEED * 0.95));
        eliminations.clear();
        status = GameStatus.RUNNING;
        winner = null;
        tick = 0;
    }

    public synchronized void setHumanControlled(PlayerId id) {
        this.humanControlled = id;
    }

    public synchronized void tick(InputState humanInput) {
        if (status != GameStatus.RUNNING) {
            return;
        }

        tick++;
        applyMovement(humanInput);
        resolveCollisions();
        evaluateWinner();
    }

    private void applyMovement(InputState humanInput) {
        List<Player> all = new ArrayList<>(players.values());
        for (Player player : all) {
            if (!player.isAlive()) {
                continue;
            }
            if (player.getId() == humanControlled) {
                Vec2 dir = humanInput.asDirection();
                player.setVelocity(dir.x * player.getSpeed(), dir.y * player.getSpeed());
            } else {
                aiController.apply(player, all);
            }
            player.move(DT, ARENA_WIDTH, ARENA_HEIGHT);
        }
    }

    private void resolveCollisions() {
        List<Player> all = new ArrayList<>(players.values());
        for (CollisionDetector.CollisionPair pair : collisionDetector.findCollisions(all)) {
            if (!pair.a().isAlive() || !pair.b().isAlive()) {
                continue;
            }
            PlayerId victimId = GameRules.victimOf(pair.a().getId(), pair.b().getId());
            if (victimId == null) {
                continue;
            }
            Player victim = players.get(victimId);
            Player eliminator = victimId == pair.a().getId() ? pair.b() : pair.a();
            if (victim != null && victim.isAlive()) {
                victim.eliminate();
                eliminations.add(new EliminationEvent(eliminator.getId(), victimId, tick));
            }
        }
    }

    private void evaluateWinner() {
        List<Player> alive = players.values().stream().filter(Player::isAlive).toList();
        if (alive.size() == 1) {
            status = GameStatus.FINISHED;
            winner = alive.get(0).getId();
        } else if (alive.isEmpty()) {
            status = GameStatus.FINISHED;
            winner = null;
        }
        // With two or three alive, valid eliminations remain possible — keep playing.
    }

    public synchronized List<Player> getPlayers() {
        return Collections.unmodifiableList(new ArrayList<>(players.values()));
    }

    public synchronized List<EliminationEvent> getEliminations() {
        return Collections.unmodifiableList(new ArrayList<>(eliminations));
    }

    public synchronized GameStatus getStatus() {
        return status;
    }

    public synchronized PlayerId getWinner() {
        return winner;
    }

    public synchronized PlayerId getHumanControlled() {
        return humanControlled;
    }

    public synchronized long getTick() {
        return tick;
    }

    public synchronized String toJson() {
        StringBuilder sb = new StringBuilder();
        sb.append('{');
        sb.append("\"status\":\"").append(status.name()).append("\",");
        sb.append("\"tick\":").append(tick).append(',');
        sb.append("\"arena\":{\"width\":").append(ARENA_WIDTH)
                .append(",\"height\":").append(ARENA_HEIGHT).append("},");
        sb.append("\"humanControlled\":\"").append(humanControlled.name()).append("\",");
        if (winner != null) {
            sb.append("\"winner\":{")
                    .append("\"id\":\"").append(winner.name()).append("\",")
                    .append("\"name\":\"").append(escape(winner.getDisplayName())).append("\",")
                    .append("\"color\":\"").append(winner.getColor()).append("\"},");
        } else {
            sb.append("\"winner\":null,");
        }
        sb.append("\"players\":[");
        boolean first = true;
        for (Player p : players.values()) {
            if (!first) {
                sb.append(',');
            }
            first = false;
            sb.append('{')
                    .append("\"id\":\"").append(p.getId().name()).append("\",")
                    .append("\"number\":").append(p.getId().getNumber()).append(',')
                    .append("\"name\":\"").append(escape(p.getId().getDisplayName())).append("\",")
                    .append("\"color\":\"").append(p.getId().getColor()).append("\",")
                    .append("\"x\":").append(round(p.getPosition().x)).append(',')
                    .append("\"y\":").append(round(p.getPosition().y)).append(',')
                    .append("\"radius\":").append(p.getRadius()).append(',')
                    .append("\"alive\":").append(p.isAlive())
                    .append('}');
        }
        sb.append("],");
        sb.append("\"eliminations\":[");
        first = true;
        for (EliminationEvent e : eliminations) {
            if (!first) {
                sb.append(',');
            }
            first = false;
            sb.append('{')
                    .append("\"eliminator\":\"").append(e.eliminator().name()).append("\",")
                    .append("\"victim\":\"").append(e.victim().name()).append("\",")
                    .append("\"message\":\"").append(escape(e.toMessage())).append("\",")
                    .append("\"tick\":").append(e.tick())
                    .append('}');
        }
        sb.append("],");
        sb.append("\"rules\":[")
                .append("\"Player 1 eliminates Player 2\",")
                .append("\"Player 2 eliminates Player 3\",")
                .append("\"Player 3 eliminates Player 1\"")
                .append(']');
        sb.append('}');
        return sb.toString();
    }

    private static double round(double v) {
        return Math.round(v * 100.0) / 100.0;
    }

    private static String escape(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
