package com.ycom.entity;

import javafx.scene.paint.Color;

public abstract class Collectible extends AnimatedObject {
    protected Collectible(ObjectKind kind, double x, double y, double z, double width, double height, double depth, Color color) {
        super(kind, null, x, y, z, width, height, depth, color);
    }

    @Override
    public final void onCollision(CollisionEvent event, EntityUpdateContext context) {
        if (event.other().kind() != ObjectKind.PLAYER) {
            return;
        }
        active = false;
        onPickedUpBy(event.other(), context);
    }

    protected abstract void onPickedUpBy(GameObject player, EntityUpdateContext context);
}
