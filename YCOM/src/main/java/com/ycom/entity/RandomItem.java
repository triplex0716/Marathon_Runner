package com.ycom.entity;

import com.ycom.core.Config;
import com.ycom.event.BoostActivatedEvent;
import com.ycom.event.MagnetActivatedEvent;
import com.ycom.event.RevivalCollectedEvent;
import com.ycom.event.ScoreMultiplierActivatedEvent;
import javafx.scene.paint.Color;

import java.util.Random;

public final class RandomItem extends Collectible {
    private static final Random RNG = new Random();

    public RandomItem(double x, double y, double z) {
        super(ObjectKind.RANDOM_ITEM, x, y, z, 1.0, 1.0, 1.0, Color.DARKSLATEGRAY, 0);
    }

    @Override
    public void onCollision(CollisionEvent event, EntityUpdateContext context) {
        if (event.other().kind() != ObjectKind.PLAYER) {
            return;
        }
        super.onCollision(event, context);

        long playerId = event.other().id;
        double roll = RNG.nextDouble();
        if (roll < 0.35) {
            context.eventBus().publish(new MagnetActivatedEvent(id, playerId, Config.MAGNET_DURATION));
        } else if (roll < 0.70) {
            context.eventBus().publish(new BoostActivatedEvent(id, playerId, Config.BOOST_DURATION, Config.BOOST_WORLD_RATE, Config.BOOST_BGM_RATE));
        } else if (roll < 0.80) {
            context.eventBus().publish(new RevivalCollectedEvent(id, playerId));
        } else {
            context.eventBus().publish(new ScoreMultiplierActivatedEvent(id, playerId, Config.TREADMILL_DURATION, Config.SCORE_MULTIPLIER));
        }
    }
}
