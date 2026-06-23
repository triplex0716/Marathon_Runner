package com.ycom.entity;

import com.ycom.core.Config;
import com.ycom.event.ScoreMultiplierActivatedEvent;
import javafx.scene.paint.Color;

public final class Treadmill extends Collectible {
    public Treadmill(double x, double y, double z) {
        super(ObjectKind.TREADMILL, x, y, z, 1.1, 0.9, 1.1, Color.DARKORANGE, 0);
    }

    @Override
    public void onCollision(CollisionEvent event, EntityUpdateContext context) {
        if (event.other().kind() == ObjectKind.PLAYER) {
            super.onCollision(event, context);
            context.eventBus().publish(new ScoreMultiplierActivatedEvent(id, event.other().id, Config.TREADMILL_DURATION, Config.SCORE_MULTIPLIER));
        }
    }
}
