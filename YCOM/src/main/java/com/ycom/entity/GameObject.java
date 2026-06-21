package com.ycom.entity;

import java.util.concurrent.atomic.AtomicLong;
import javafx.scene.paint.Color;

public abstract class GameObject {
    public enum ObjectKind {
        PLAYER,
        OBSTACLE,
        COIN,
        MAGNET,
        ENERGY_DRINK
    }

    private static final AtomicLong IDS = new AtomicLong(1L);

    public final long id;
    public double x;
    public double y;
    public double z;
    public double width;
    public double height;
    public double depth;
    public Color color;
    public boolean active = true;

    private final ObjectKind kind;

    protected GameObject(ObjectKind kind, double x, double y, double z, double width, double height, double depth, Color color) {
        this.id = IDS.getAndIncrement();
        this.kind = kind;
        this.x = x;
        this.y = y;
        this.z = z;
        this.width = width;
        this.height = height;
        this.depth = depth;
        this.color = color;
    }

    public ObjectKind kind() {
        return kind;
    }

    public void update(double worldDt, EntityUpdateContext context) {
    }

    public void onCollision(CollisionEvent event, EntityUpdateContext context) {
    }

    public boolean collidesWith(GameObject other) {
        if (!active || !other.active) {
            return false;
        }

        double thisCenterY = y + height / 2.0;
        double otherCenterY = other.y + other.height / 2.0;

        return Math.abs(x - other.x) < (width / 2.0 + other.width / 2.0)
                && Math.abs(thisCenterY - otherCenterY) < (height / 2.0 + other.height / 2.0)
                && Math.abs(z - other.z) < (depth / 2.0 + other.depth / 2.0);
    }
}
