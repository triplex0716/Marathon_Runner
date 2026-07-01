package com.ycom.entity;

import com.ycom.core.Config;
import com.ycom.event.BoostActivatedEvent;
import com.ycom.event.MagnetActivatedEvent;
import com.ycom.event.RevivalCollectedEvent;
import com.ycom.event.ScoreMultiplierActivatedEvent;
import javafx.scene.paint.Color;

import java.util.concurrent.ThreadLocalRandom;

public final class RandomItem extends Collectible {
    public RandomItem(double x, double y, double z) {
        super(ObjectKind.RANDOM_ITEM, x, y, z,
                Config.RANDOM_ITEM_WIDTH, Config.RANDOM_ITEM_HEIGHT, Config.RANDOM_ITEM_DEPTH,
                Color.DARKSLATEGRAY);
    }

    @Override
    protected void onPickedUpBy(GameObject player, EntityUpdateContext context) {
        long playerId = player.id;
        double roll = ThreadLocalRandom.current().nextDouble();
        double magnetLimit = Config.RANDOM_ITEM_MAGNET_CHANCE;
        double boostLimit = magnetLimit + Config.RANDOM_ITEM_BOOST_CHANCE;
        double revivalLimit = boostLimit + Config.RANDOM_ITEM_REVIVAL_CHANCE;
        if (roll < magnetLimit) {
            context.eventBus().publish(new MagnetActivatedEvent(id, playerId, Config.MAGNET_DURATION));
        } else if (roll < boostLimit) {
            context.eventBus().publish(new BoostActivatedEvent(id, playerId, Config.BOOST_DURATION, Config.BOOST_WORLD_RATE, Config.BOOST_BGM_RATE));
        } else if (roll < revivalLimit) {
            context.eventBus().publish(new RevivalCollectedEvent(id, playerId));
        } else {
            context.eventBus().publish(new ScoreMultiplierActivatedEvent(id, playerId, Config.TREADMILL_DURATION, Config.SCORE_MULTIPLIER));
        }
    }
}
