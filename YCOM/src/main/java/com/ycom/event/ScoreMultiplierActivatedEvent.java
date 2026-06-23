package com.ycom.event;

public record ScoreMultiplierActivatedEvent(long sourceId, long collectorId, double duration, double multiplier) implements GameEvent {
}
