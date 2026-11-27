package com.aicold.game.model;

/**
 * Record of who eliminated whom.
 */
public record EliminationEvent(PlayerId eliminator, PlayerId victim, long tick) {

    public String toMessage() {
        return eliminator.getDisplayName() + " eliminated " + victim.getDisplayName();
    }
}
