package com.ycom.entity;

import com.ycom.event.ObstacleDestroyedEvent;
import com.ycom.event.PlayerHitEvent;
import javafx.scene.paint.Color;

public class Obstacle extends GameObject {
    public enum AvoidMethod {
        JUMP,
        SLIDE,
        CHANGE_LANE,
        CONTAINER,
        RAMP
    }

    private final AvoidMethod avoidMethod;

    public Obstacle(double x, double y, double z, double width, double height, double depth, Color color, AvoidMethod avoidMethod) {
        super(ObjectKind.OBSTACLE, x, y, z, width, height, depth, color);
        this.avoidMethod = avoidMethod;
    }

    @Override
    public void onCollision(CollisionEvent event, EntityUpdateContext context) {
        if (event.other() instanceof Player player) {
            if (avoidMethod == AvoidMethod.RAMP) {
                return;
            }
            if (avoidMethod == AvoidMethod.CONTAINER && player.floorY() > 0.0) {
                return;
            }
            if (player.isInvulnerable()) {
                deactivate();
                context.eventBus().publish(new ObstacleDestroyedEvent(
                        id, player.id,
                        getX(), getY(), getZ(), getWidth(), getHeight(), getDepth(),
                        getColor().getRed(), getColor().getGreen(), getColor().getBlue()
                ));
            } else {
                context.eventBus().publish(new PlayerHitEvent(player.id, id, avoidMethod.name()));
            }
        }
    }

    public AvoidMethod avoidMethod() {
        return avoidMethod;
    }
}
