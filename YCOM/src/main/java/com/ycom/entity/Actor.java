package com.ycom.entity;

import javafx.scene.paint.Color;

public abstract class Actor extends AnimatedObject {
    protected int lane;
    protected double targetX;

    protected Actor(String spriteKey, double x, double y, double z, double width, double height, double depth, Color color) {
        super(ObjectKind.PLAYER, spriteKey, x, y, z, width, height, depth, color);
    }

    public int lane() {
        return lane;
    }
}
