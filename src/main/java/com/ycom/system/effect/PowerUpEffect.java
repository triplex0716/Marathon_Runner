package com.ycom.system.effect;
import com.ycom.entity.EntityUpdateContext;

public interface PowerUpEffect {
    String id();
    double duration();
    double maxDuration();
    void onStart(EntityUpdateContext ctx);
    void onTick(EntityUpdateContext ctx, double dt);
    void onEnd(EntityUpdateContext ctx);
    boolean isFinished();
    void extendDuration(double duration);
}
