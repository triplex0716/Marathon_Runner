package com.ycom.entity;

import javafx.scene.paint.Color;

public abstract class AnimatedObject extends GameObject {
    private volatile double animationTime;
    private double framesPerSecond = 10.0;

    protected AnimatedObject(ObjectKind kind, double x, double y, double z, double width, double height, double depth, Color color) {
        super(kind, x, y, z, width, height, depth, color);
    }

    @Override
    public void update(double worldDt, EntityUpdateContext context) {
        animationTime += context.fixedDt();
    }

    public int currentFrame(int frameCount) {
        if (frameCount <= 1) {
            return 0;
        }
        return (int) Math.floor(animationTime * framesPerSecond) % frameCount;
    }

    public double animationTime() {
        return animationTime;
    }

    public void setFramesPerSecond(double framesPerSecond) {
        this.framesPerSecond = Math.max(1.0, framesPerSecond);
    }
}
