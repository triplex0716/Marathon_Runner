package com.ycom.render;

import com.ycom.core.Config;

public class Projector {
    private final double cx;
    private final double horizonY;

    public Projector(double cx, double horizonY) {
        this.cx = cx;
        this.horizonY = horizonY;
    }

    public Projection project(double x, double y, double z, double width, double height, Camera cam) {
        double distZ = Math.max(0.5, z - cam.z);
        double scale = Config.FOCAL_LENGTH / distZ;
        double screenX = cx + (x - cam.x) * scale;
        double screenY = horizonY - (y + height / 2.0 - cam.y) * scale;
        return new Projection(screenX, screenY, width * scale, height * scale, scale);
    }
    
    private double alpha = 1.0;

    public void setAlpha(double alpha) {
        this.alpha = alpha;
    }

    public double getAlpha() {
        return alpha;
    }
}
