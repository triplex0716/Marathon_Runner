package com.ycom.entity;

import com.ycom.core.Config;
import com.ycom.event.RevivalCollectedEvent;
import javafx.scene.paint.Color;

public final class RevivalCapsule extends Collectible {
    public RevivalCapsule(double x, double y, double z) {
        super(ObjectKind.REVIVAL_CAPSULE, x, y, z,
                Config.REVIVAL_CAPSULE_WIDTH, Config.REVIVAL_CAPSULE_HEIGHT, Config.REVIVAL_CAPSULE_DEPTH,
                Color.CRIMSON);
    }

    @Override
    protected void onPickedUpBy(GameObject player, EntityUpdateContext context) {
        context.eventBus().publish(new RevivalCollectedEvent(id, player.id));
    }
}
