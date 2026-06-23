package com.ycom.entity;

import com.ycom.event.RevivalCollectedEvent;
import javafx.scene.paint.Color;

public final class RevivalCapsule extends Collectible {
    public RevivalCapsule(double x, double y, double z) {
        super(ObjectKind.REVIVAL_CAPSULE, x, y, z, 0.9, 1.1, 0.9, Color.CRIMSON, 0);
    }

    @Override
    public void onCollision(CollisionEvent event, EntityUpdateContext context) {
        if (event.other().kind() == ObjectKind.PLAYER) {
            super.onCollision(event, context);
            context.eventBus().publish(new RevivalCollectedEvent(id, event.other().id));
        }
    }
}
