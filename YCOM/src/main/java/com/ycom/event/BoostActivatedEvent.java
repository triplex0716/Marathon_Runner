package com.ycom.event;

public record BoostActivatedEvent(long sourceId, long collectorId, double duration, double worldRate, double bgmRate) implements GameEvent {
}
