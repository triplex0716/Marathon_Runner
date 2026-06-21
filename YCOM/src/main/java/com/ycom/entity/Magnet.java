package com.ycom.entity;

import com.ycom.core.Config;
import com.ycom.event.MagnetActivatedEvent;
import javafx.scene.paint.Color;

public final class Magnet extends Collectible {
    public Magnet(double x, double y, double z) {
        super(ObjectKind.MAGNET, x, y, z, 1.0, 1.0, 1.0, Color.MEDIUMPURPLE, 0);
    }

    @Override
    public void onCollision(CollisionEvent event, EntityUpdateContext context) {
        if (event.other().kind() == ObjectKind.PLAYER) {
            super.onCollision(event, context);
            context.eventBus().publish(new MagnetActivatedEvent(id, event.other().id, Config.MAGNET_DURATION));
        }
    }
}
