package com.ycom.event;

public record ObstacleDestroyedEvent(
        long obstacleId,
        long byPlayerId,
        double x,
        double y,
        double z,
        double width,
        double height,
        double depth,
        double red,
        double green,
        double blue
) implements GameEvent {
}
