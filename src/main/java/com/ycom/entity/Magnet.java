package com.ycom.entity;

import com.ycom.core.Config;
import com.ycom.event.MagnetActivatedEvent;
import javafx.scene.paint.Color;

public final class Magnet extends Collectible {
    public Magnet(double x, double y, double z) {
        super(ObjectKind.MAGNET, x, y, z,
                Config.MAGNET_WIDTH, Config.MAGNET_HEIGHT, Config.MAGNET_DEPTH,
                Color.MEDIUMPURPLE);
    }

    @Override
    protected void onPickedUpBy(GameObject player, EntityUpdateContext context) {
        context.eventBus().publish(new MagnetActivatedEvent(id, player.id, Config.MAGNET_DURATION));
    }
}
