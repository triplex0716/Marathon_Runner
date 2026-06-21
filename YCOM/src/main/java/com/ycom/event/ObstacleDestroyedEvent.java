package com.ycom.event;

public record ObstacleDestroyedEvent(long obstacleId, long byPlayerId) implements GameEvent {
}
