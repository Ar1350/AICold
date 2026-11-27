package com.aicold.game.model;

/**
 * A single mover in the arena.
 */
public class Player {
    private final PlayerId id;
    private final Vec2 position;
    private final Vec2 velocity;
    private final double radius;
    private final double speed;
    private boolean alive;

    public Player(PlayerId id, Vec2 position, double radius, double speed) {
        this.id = id;
        this.position = position.copy();
        this.velocity = new Vec2(0, 0);
        this.radius = radius;
        this.speed = speed;
        this.alive = true;
    }

    public PlayerId getId() {
        return id;
    }

    public Vec2 getPosition() {
        return position;
    }

    public Vec2 getVelocity() {
        return velocity;
    }

    public double getRadius() {
        return radius;
    }

    public double getSpeed() {
        return speed;
    }

    public boolean isAlive() {
        return alive;
    }

    public void eliminate() {
        alive = false;
        velocity.x = 0;
        velocity.y = 0;
    }

    public void setAlive(boolean alive) {
        this.alive = alive;
    }

    public void setVelocity(double vx, double vy) {
        velocity.x = vx;
        velocity.y = vy;
    }

    public void move(double dt, double arenaWidth, double arenaHeight) {
        if (!alive) {
            return;
        }
        position.x += velocity.x * dt;
        position.y += velocity.y * dt;

        // Bounce off walls
        if (position.x < radius) {
            position.x = radius;
            velocity.x = Math.abs(velocity.x);
        } else if (position.x > arenaWidth - radius) {
            position.x = arenaWidth - radius;
            velocity.x = -Math.abs(velocity.x);
        }
        if (position.y < radius) {
            position.y = radius;
            velocity.y = Math.abs(velocity.y);
        } else if (position.y > arenaHeight - radius) {
            position.y = arenaHeight - radius;
            velocity.y = -Math.abs(velocity.y);
        }
    }

    public void reset(Vec2 newPosition) {
        position.x = newPosition.x;
        position.y = newPosition.y;
        velocity.x = 0;
        velocity.y = 0;
        alive = true;
    }
}
