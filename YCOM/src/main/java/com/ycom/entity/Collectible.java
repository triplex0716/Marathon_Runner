package com.ycom.entity;

import javafx.scene.paint.Color;

public abstract class Collectible extends AnimatedObject {
    private final int value;

    protected Collectible(ObjectKind kind, double x, double y, double z, double width, double height, double depth, Color color, int value) {
        super(kind, null, x, y, z, width, height, depth, color);
        this.value = value;
        setFramesPerSecond(6.0);
    }

    public int value() {
        return value;
    }

    @Override
    public void onCollision(CollisionEvent event, EntityUpdateContext context) {
        if (event.other().kind() == ObjectKind.PLAYER) {
            active = false;
        }
    }
}
