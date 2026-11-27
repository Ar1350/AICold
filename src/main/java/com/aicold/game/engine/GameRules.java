package com.aicold.game.engine;

import com.aicold.game.model.PlayerId;

/**
 * Rock-paper-scissors elimination chain:
 * <ul>
 *   <li>1 touches 2 → 2 is eliminated</li>
 *   <li>2 touches 3 → 3 is eliminated</li>
 *   <li>3 touches 1 → 1 is eliminated</li>
 * </ul>
 */
public final class GameRules {

    private GameRules() {
    }

    /**
     * Returns who gets eliminated when two players touch, or null if neither
     * eliminates the other (should not happen for distinct P1/P2/P3 pairs).
     */
    public static PlayerId victimOf(PlayerId a, PlayerId b) {
        if (a == b) {
            return null;
        }
        if (beats(a, b)) {
            return b;
        }
        if (beats(b, a)) {
            return a;
        }
        return null;
    }

    /**
     * True if {@code attacker} eliminates {@code target} on contact.
     */
    public static boolean beats(PlayerId attacker, PlayerId target) {
        return switch (attacker) {
            case P1 -> target == PlayerId.P2;
            case P2 -> target == PlayerId.P3;
            case P3 -> target == PlayerId.P1;
        };
    }

    /**
     * Prey that this player should chase (who they eliminate on contact).
     */
    public static PlayerId preyOf(PlayerId hunter) {
        return switch (hunter) {
            case P1 -> PlayerId.P2;
            case P2 -> PlayerId.P3;
            case P3 -> PlayerId.P1;
        };
    }

    /**
     * Predator that eliminates this player on contact (who they should flee).
     */
    public static PlayerId predatorOf(PlayerId prey) {
        return switch (prey) {
            case P1 -> PlayerId.P3;
            case P2 -> PlayerId.P1;
            case P3 -> PlayerId.P2;
        };
    }
}
