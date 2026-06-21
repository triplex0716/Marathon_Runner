package com.ycom.event;

public record CoinCollectedEvent(long coinId, long collectorId, int value) implements GameEvent {
}
