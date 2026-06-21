package com.ycom.entity;

import com.ycom.event.CoinCollectedEvent;
import javafx.scene.paint.Color;

public final class Coin extends Collectible {
    public Coin(double x, double y, double z) {
        super(ObjectKind.COIN, x, y, z, 0.7, 0.7, 0.7, Color.GOLD, 10);
    }

    @Override
    public void onCollision(CollisionEvent event, EntityUpdateContext context) {
        if (event.other().kind() == ObjectKind.PLAYER) {
            super.onCollision(event, context);
            context.eventBus().publish(new CoinCollectedEvent(id, event.other().id, value()));
        }
    }
}
