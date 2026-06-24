package com.ycom.entity;

import com.ycom.event.ObstacleDestroyedEvent;
import com.ycom.event.PlayerHitEvent;
import javafx.scene.paint.Color;

public class Obstacle extends RenderableObject {
    public enum AvoidMethod {
        JUMP,
        SLIDE,
        CHANGE_LANE
    }

    private final AvoidMethod avoidMethod;

    public Obstacle(double x, double y, double z, double width, double height, double depth, Color color, AvoidMethod avoidMethod) {
        super(ObjectKind.OBSTACLE, null, x, y, z, width, height, depth, color);
        this.avoidMethod = avoidMethod;
    }

    @Override
    public void onCollision(CollisionEvent event, EntityUpdateContext context) {
        if (event.other() instanceof Player player) {
            if (player.isBoosted()) {
                active = false;
                context.eventBus().publish(new ObstacleDestroyedEvent(
                        id, player.id,
                        x, y, z, width, height, depth,
                        color.getRed(), color.getGreen(), color.getBlue()
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
