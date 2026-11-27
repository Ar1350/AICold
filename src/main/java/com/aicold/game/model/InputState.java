package com.aicold.game.model;

/**
 * Keyboard input for the human-controlled player.
 */
public final class InputState {
    public boolean up;
    public boolean down;
    public boolean left;
    public boolean right;

    public InputState() {
    }

    public InputState(boolean up, boolean down, boolean left, boolean right) {
        this.up = up;
        this.down = down;
        this.left = left;
        this.right = right;
    }

    public Vec2 asDirection() {
        double dx = 0;
        double dy = 0;
        if (left) {
            dx -= 1;
        }
        if (right) {
            dx += 1;
        }
        if (up) {
            dy -= 1;
        }
        if (down) {
            dy += 1;
        }
        return new Vec2(dx, dy).normalized();
    }
}
