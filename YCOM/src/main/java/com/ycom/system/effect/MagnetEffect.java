package com.ycom.system.effect;
import com.ycom.entity.EntityUpdateContext;

public class MagnetEffect implements PowerUpEffect {
    private volatile double timer;
    private volatile double max;

    public MagnetEffect(double duration) {
        this.timer = duration;
        this.max = duration;
    }
    public String id() { return "magnet"; }
    public double duration() { return timer; }
    public double maxDuration() { return max; }
    public void extendDuration(double duration) {
        this.timer = Math.max(this.timer, duration);
        this.max = Math.max(this.max, duration);
    }
    public void onStart(EntityUpdateContext ctx) { ctx.player().setHasMagnet(true); }
    public void onTick(EntityUpdateContext ctx, double dt) { timer -= dt; }
    public void onEnd(EntityUpdateContext ctx) { ctx.player().setHasMagnet(false); }
    public boolean isFinished() { return timer <= 0; }
}
