package com.ycom.event;

public record PlayerHitEvent(long playerId, long sourceId, String hitType) implements GameEvent {
}
