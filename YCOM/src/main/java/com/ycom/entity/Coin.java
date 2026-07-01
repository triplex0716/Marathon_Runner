package com.ycom.entity;

import com.ycom.core.Config;
import com.ycom.event.CoinCollectedEvent;
import javafx.scene.paint.Color;

public final class Coin extends Collectible {
    public boolean attracted = false;

    public Coin(double x, double y, double z) {
        super(ObjectKind.COIN, x, y, z, Config.COIN_WIDTH, Config.COIN_HEIGHT, Config.COIN_DEPTH, Color.GOLD);
    }

    public int value() {
        return Config.COIN_SCORE_VALUE;
    }

    @Override
    protected void onPickedUpBy(GameObject player, EntityUpdateContext context) {
        context.eventBus().publish(new CoinCollectedEvent(id, player.id, Config.COIN_SCORE_VALUE));
    }
}
