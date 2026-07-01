package com.ycom.event;

public record MagnetActivatedEvent(long sourceId, long collectorId, double duration) implements GameEvent {
}
