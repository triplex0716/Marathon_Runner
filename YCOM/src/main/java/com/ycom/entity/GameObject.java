package com.ycom.entity;

import java.util.concurrent.atomic.AtomicLong;
import javafx.scene.paint.Color;

public abstract class GameObject {
    public enum ObjectKind {
        PLAYER,
        OBSTACLE,
        COIN,
        MAGNET,
        ENERGY_DRINK,
        REVIVAL_CAPSULE,
        TREADMILL,
        RANDOM_ITEM
    }

    private static final AtomicLong IDS = new AtomicLong(1L);

    public final long id;
    private volatile double x;
    private volatile double y;
    private volatile double z;
    private volatile double width;
    private volatile double height;
    private volatile double depth;
    private volatile Color color;
    private volatile boolean active = true;

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


    public double getX() { return x; }
    public void setX(double x) { this.x = x; }
    public double getY() { return y; }
    public void setY(double y) { this.y = y; }
    public double getZ() { return z; }
    public void setZ(double z) { this.z = z; }
    public double getWidth() { return width; }
    public void setWidth(double width) { this.width = width; }
    public double getHeight() { return height; }
    public void setHeight(double height) { this.height = height; }
    public double getDepth() { return depth; }
    public void setDepth(double depth) { this.depth = depth; }
    public Color getColor() { return color; }
    public void setColor(Color color) { this.color = color; }
    public boolean isActive() { return active; }
    public void deactivate() { this.active = false; }

    public ObjectKind kind() {
        return kind;
    }

    public void update(double worldDt, EntityUpdateContext context) {
    }

    public void onCollision(CollisionEvent event, EntityUpdateContext context) {
    }

    public boolean collidesWith(GameObject other) {
        if (!this.active || !other.isActive()) {
            return false;
        }

        double thisCenterY = y + height / 2.0;
        double otherCenterY = other.getY() + other.getHeight() / 2.0;

        return Math.abs(x - other.getX()) < (width / 2.0 + other.getWidth() / 2.0)
                && Math.abs(thisCenterY - otherCenterY) < (height / 2.0 + other.getHeight() / 2.0)
                && Math.abs(z - other.getZ()) < (depth / 2.0 + other.getDepth() / 2.0);
    }
}
