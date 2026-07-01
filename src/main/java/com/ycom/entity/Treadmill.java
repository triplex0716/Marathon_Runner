package com.ycom.entity;

import com.ycom.core.Config;
import com.ycom.event.ScoreMultiplierActivatedEvent;
import javafx.scene.paint.Color;

public final class Treadmill extends Collectible {
    public Treadmill(double x, double y, double z) {
        super(ObjectKind.TREADMILL, x, y, z,
                Config.TREADMILL_WIDTH, Config.TREADMILL_HEIGHT, Config.TREADMILL_DEPTH,
                Color.DARKORANGE);
    }

    @Override
    protected void onPickedUpBy(GameObject player, EntityUpdateContext context) {
        context.eventBus().publish(new ScoreMultiplierActivatedEvent(id, player.id, Config.TREADMILL_DURATION, Config.SCORE_MULTIPLIER));
    }
}
