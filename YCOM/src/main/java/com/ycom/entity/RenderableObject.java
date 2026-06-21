package com.ycom.entity;

import javafx.scene.paint.Color;

public abstract class RenderableObject extends WorldObject {
    private final String spriteKey;

    protected RenderableObject(ObjectKind kind, String spriteKey, double x, double y, double z, double width, double height, double depth, Color color) {
        super(kind, x, y, z, width, height, depth, color);
        this.spriteKey = spriteKey;
    }

    public String spriteKey() {
        return spriteKey;
    }
}
