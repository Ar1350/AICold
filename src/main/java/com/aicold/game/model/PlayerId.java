package com.aicold.game.model;

/**
 * The three arena fighters in the elimination chain.
 */
public enum PlayerId {
    P1(1, "#e74c3c", "Player 1"),
    P2(2, "#3498db", "Player 2"),
    P3(3, "#2ecc71", "Player 3");

    private final int number;
    private final String color;
    private final String displayName;

    PlayerId(int number, String color, String displayName) {
        this.number = number;
        this.color = color;
        this.displayName = displayName;
    }

    public int getNumber() {
        return number;
    }

    public String getColor() {
        return color;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static PlayerId fromNumber(int number) {
        for (PlayerId id : values()) {
            if (id.number == number) {
                return id;
            }
        }
        throw new IllegalArgumentException("Unknown player number: " + number);
    }
}
