package com.ycom.entity;

import com.ycom.core.Config;
import com.ycom.event.BoostActivatedEvent;
import javafx.scene.paint.Color;

public final class EnergyDrink extends Collectible {
    public EnergyDrink(double x, double y, double z) {
        super(ObjectKind.ENERGY_DRINK, x, y, z, 0.9, 1.2, 0.9, Color.DEEPSKYBLUE);
    }

    @Override
    protected void onPickedUpBy(GameObject player, EntityUpdateContext context) {
        context.eventBus().publish(new BoostActivatedEvent(id, player.id, Config.BOOST_DURATION, Config.BOOST_WORLD_RATE, Config.BOOST_BGM_RATE));
    }
}
