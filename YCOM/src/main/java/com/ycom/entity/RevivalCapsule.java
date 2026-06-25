package com.ycom.entity;

import com.ycom.event.RevivalCollectedEvent;
import javafx.scene.paint.Color;

public final class RevivalCapsule extends Collectible {
    public RevivalCapsule(double x, double y, double z) {
        super(ObjectKind.REVIVAL_CAPSULE, x, y, z, 0.9, 1.1, 0.9, Color.CRIMSON);
    }

    @Override
    protected void onPickedUpBy(GameObject player, EntityUpdateContext context) {
        context.eventBus().publish(new RevivalCollectedEvent(id, player.id));
    }
}
