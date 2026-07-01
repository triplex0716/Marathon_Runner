package com.ycom.render;

import com.ycom.entity.AnimatedObject;
import com.ycom.entity.GameObject;
import com.ycom.entity.Obstacle;
import com.ycom.entity.Player;
import javafx.scene.paint.Color;

public record RenderSnapshot(
        long id,
        GameObject.ObjectKind kind,
        double x,
        double y,
        double z,
        double width,
        double height,
        double depth,
        Color color,
        double animationTime,
        Player.PlayerState playerState,
        boolean boosted,
        boolean reviveInvincible,
        Obstacle.AvoidMethod avoidMethod
) {
    public static RenderSnapshot from(GameObject obj) {
        double animationTime = obj instanceof AnimatedObject animated ? animated.animationTime() : 0.0;
        Player.PlayerState playerState = obj instanceof Player player ? player.state() : null;
        boolean boosted = obj instanceof Player player && player.isBoosted();
        boolean reviveInvincible = obj instanceof Player player && player.isReviveInvincible();
        Obstacle.AvoidMethod avoidMethod = obj instanceof Obstacle obstacle ? obstacle.avoidMethod() : null;

        return new RenderSnapshot(
                obj.id,
                obj.kind(),
                obj.getX(),
                obj.getY(),
                obj.getZ(),
                obj.getWidth(),
                obj.getHeight(),
                obj.getDepth(),
                obj.getColor(),
                animationTime,
                playerState,
                boosted,
                reviveInvincible,
                avoidMethod
        );
    }

    public RenderSnapshot interpolateFrom(RenderSnapshot previous, double alpha) {
        if (previous == null || previous.kind != kind) {
            return this;
        }
        double t = clamp01(alpha);
        return new RenderSnapshot(
                id,
                kind,
                lerp(previous.x, x, t),
                lerp(previous.y, y, t),
                lerp(previous.z, z, t),
                lerp(previous.width, width, t),
                lerp(previous.height, height, t),
                lerp(previous.depth, depth, t),
                color,
                lerp(previous.animationTime, animationTime, t),
                playerState,
                boosted,
                reviveInvincible,
                avoidMethod
        );
    }

    private static double lerp(double a, double b, double t) {
        return a + (b - a) * t;
    }

    private static double clamp01(double value) {
        return Math.max(0.0, Math.min(1.0, value));
    }
}
