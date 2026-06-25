package com.ycom.entity;

import com.ycom.event.CoinCollectedEvent;
import javafx.scene.paint.Color;

public final class Coin extends Collectible {
    private static final int VALUE = 10;

    public boolean attracted = false;

    public Coin(double x, double y, double z) {
        super(ObjectKind.COIN, x, y, z, 0.7, 0.7, 0.7, Color.GOLD);
    }

    public int value() {
        return VALUE;
    }

    @Override
    protected void onPickedUpBy(GameObject player, EntityUpdateContext context) {
        context.eventBus().publish(new CoinCollectedEvent(id, player.id, VALUE));
    }
}
