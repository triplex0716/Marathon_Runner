package com.ycom.entity;

import javafx.scene.paint.Color;

public abstract class WorldObject extends GameObject {
    protected WorldObject(ObjectKind kind, double x, double y, double z, double width, double height, double depth, Color color) {
        super(kind, x, y, z, width, height, depth, color);
    }
}
