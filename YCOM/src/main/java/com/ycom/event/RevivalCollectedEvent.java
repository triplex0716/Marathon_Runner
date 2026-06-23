package com.ycom.event;

public record RevivalCollectedEvent(long sourceId, long collectorId) implements GameEvent {
}
