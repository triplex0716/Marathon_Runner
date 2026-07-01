package com.ycom.system;

import com.ycom.core.Config;
import java.util.ArrayList;
import java.util.List;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class ParticleSystem {
    private static final double GRAVITY = -25.0;
    private static final int SHARDS_PER_BREAK = 10;

    private final List<Shard> shards = new ArrayList<>();
    private final double cx;
    private final double horizonY;

    public ParticleSystem() {
        this.cx = Config.LOGICAL_WIDTH / 2.0;
        this.horizonY = Config.LOGICAL_HEIGHT * 0.38;
    }

    public void spawnBreak(double x, double y, double z, double w, double h, double d, Color color) {
        double size = Math.max(0.18, Math.min(w, h) * 0.22);
        synchronized (shards) {
            for (int i = 0; i < SHARDS_PER_BREAK; i++) {
                double dx = (Math.random() - 0.5) * w;
                double dy = Math.random() * h;
                double dz = (Math.random() - 0.5) * d * 0.6;
                double vx = (Math.random() - 0.5) * 6.0;
                double vy = 3.5 + Math.random() * 4.5;
                double vz = -2.0 - Math.random() * 4.0;
                double life = 0.45 + Math.random() * 0.25;
                shards.add(new Shard(x + dx, y + dy, z + dz, vx, vy, vz, life, color, size));
            }
        }
    }

    public void update(double dt, double playerZ) {
        synchronized (shards) {
            for (Shard s : shards) {
                s.vy += GRAVITY * dt;
                s.x += s.vx * dt;
                s.y += s.vy * dt;
                s.z += s.vz * dt;
                s.life -= dt;
            }
            shards.removeIf(s -> s.life <= 0.0 || s.y < 0.0 || s.z < playerZ - 35.0);
        }
    }

    public void draw(GraphicsContext gc, double camX, double camY, double camZ) {
        List<Shard> snapshot;
        synchronized (shards) {
            snapshot = List.copyOf(shards);
        }
        for (Shard s : snapshot) {
            double distZ = s.z - camZ;
            if (distZ < 0.5) {
                continue;
            }
            double scale = Config.FOCAL_LENGTH / distZ;
            double screenX = cx + (s.x - camX) * scale;
            double screenY = horizonY - (s.y - camY) * scale;
            double w = Math.max(2.0, s.size * scale);
            double alpha = Math.min(1.0, Math.max(0.0, s.life / 0.5));
            Color c = s.color;
            gc.setGlobalAlpha(alpha);
            gc.setFill(c);
            gc.fillRect(screenX - w / 2.0, screenY - w / 2.0, w, w);
        }
        gc.setGlobalAlpha(1.0); // Reset alpha when done
    }

    public void clear() {
        synchronized (shards) {
            shards.clear();
        }
    }

    private static final class Shard {
        double x;
        double y;
        double z;
        double vx;
        double vy;
        double vz;
        double life;
        final Color color;
        final double size;

        Shard(double x, double y, double z, double vx, double vy, double vz, double life, Color color, double size) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.vx = vx;
            this.vy = vy;
            this.vz = vz;
            this.life = life;
            this.color = color;
            this.size = size;
        }
    }
}
