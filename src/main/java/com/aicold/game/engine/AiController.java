package com.aicold.game.engine;

import com.aicold.game.model.Player;
import com.aicold.game.model.PlayerId;
import com.aicold.game.model.Vec2;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Simple chase/flee AI for computer-controlled fighters.
 * Chases prey when safe; flees predator when close.
 */
public final class AiController {

    private static final double FLEE_DISTANCE = 160.0;

    public void apply(Player self, List<Player> all) {
        if (!self.isAlive()) {
            self.setVelocity(0, 0);
            return;
        }

        Map<PlayerId, Player> byId = all.stream()
                .collect(Collectors.toMap(Player::getId, Function.identity()));

        Player prey = byId.get(GameRules.preyOf(self.getId()));
        Player predator = byId.get(GameRules.predatorOf(self.getId()));

        Vec2 desire = new Vec2(0, 0);
        boolean preyAlive = prey != null && prey.isAlive();
        boolean predatorAlive = predator != null && predator.isAlive();

        // With both other fighters alive: flee a close predator, otherwise chase prey.
        // With only one opponent left: always chase them so the match can resolve.
        if (preyAlive && predatorAlive) {
            double dist = self.getPosition().distanceTo(predator.getPosition());
            if (dist < FLEE_DISTANCE) {
                desire = self.getPosition().minus(predator.getPosition()).normalized();
            } else {
                desire = prey.getPosition().minus(self.getPosition()).normalized();
            }
        } else if (preyAlive) {
            desire = prey.getPosition().minus(self.getPosition()).normalized();
        } else if (predatorAlive) {
            desire = predator.getPosition().minus(self.getPosition()).normalized();
        }

        if (desire.length() < 1e-6) {
            // Wander slightly so they don't freeze when alone mid-frame
            desire = new Vec2(Math.sin(System.nanoTime() * 1e-9 + self.getId().ordinal()),
                    Math.cos(System.nanoTime() * 1e-9 + self.getId().ordinal())).normalized();
        }

        Vec2 vel = desire.scaled(self.getSpeed());
        self.setVelocity(vel.x, vel.y);
    }
}
