package com.aicold.game.model;

/**
 * Simple 2D vector for position and velocity.
 */
public final class Vec2 {
    public double x;
    public double y;

    public Vec2(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public Vec2 copy() {
        return new Vec2(x, y);
    }

    public double length() {
        return Math.hypot(x, y);
    }

    public Vec2 normalized() {
        double len = length();
        if (len < 1e-9) {
            return new Vec2(0, 0);
        }
        return new Vec2(x / len, y / len);
    }

    public Vec2 scaled(double s) {
        return new Vec2(x * s, y * s);
    }

    public Vec2 plus(Vec2 other) {
        return new Vec2(x + other.x, y + other.y);
    }

    public Vec2 minus(Vec2 other) {
        return new Vec2(x - other.x, y - other.y);
    }

    public double distanceTo(Vec2 other) {
        return Math.hypot(x - other.x, y - other.y);
    }
}
