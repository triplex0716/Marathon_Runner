package com.ycom.system.effect;
import com.ycom.entity.EntityUpdateContext;

public class ReviveInvincibleEffect implements PowerUpEffect {
    private double timer;
    private double max;

    public ReviveInvincibleEffect(double duration) {
        this.timer = duration;
        this.max = duration;
    }
    public String id() { return "revive"; }
    public double duration() { return timer; }
    public double maxDuration() { return max; }
    public void extendDuration(double duration) {
        this.timer = Math.max(this.timer, duration);
        this.max = Math.max(this.max, duration);
    }
    public void onStart(EntityUpdateContext ctx) { ctx.player().setReviveInvincible(true); }
    public void onTick(EntityUpdateContext ctx, double dt) { timer -= dt; }
    public void onEnd(EntityUpdateContext ctx) { ctx.player().setReviveInvincible(false); }
    public boolean isFinished() { return timer <= 0; }
}
