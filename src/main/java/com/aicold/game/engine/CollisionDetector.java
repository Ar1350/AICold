package com.aicold.game.engine;

import com.aicold.game.model.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * Detects overlapping pairs of alive players (circle-circle).
 */
public final class CollisionDetector {

    public record CollisionPair(Player a, Player b) {
    }

    /**
     * Returns every unique pair of living players whose circles overlap.
     */
    public List<CollisionPair> findCollisions(List<Player> players) {
        List<CollisionPair> hits = new ArrayList<>();
        for (int i = 0; i < players.size(); i++) {
            Player a = players.get(i);
            if (!a.isAlive()) {
                continue;
            }
            for (int j = i + 1; j < players.size(); j++) {
                Player b = players.get(j);
                if (!b.isAlive()) {
                    continue;
                }
                if (circlesOverlap(a, b)) {
                    hits.add(new CollisionPair(a, b));
                }
            }
        }
        return hits;
    }

    private boolean circlesOverlap(Player a, Player b) {
        double minDist = a.getRadius() + b.getRadius();
        return a.getPosition().distanceTo(b.getPosition()) < minDist;
    }
}
