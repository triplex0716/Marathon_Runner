package com.ycom.system.effect;
import com.ycom.entity.EntityUpdateContext;

public class ScoreMultiplierEffect implements PowerUpEffect {
    private double timer;
    private double max;
    private double multiplier;

    public ScoreMultiplierEffect(double duration, double multiplier) {
        this.timer = duration;
        this.max = duration;
        this.multiplier = multiplier;
    }
    public String id() { return "score_multiplier"; }
    public double duration() { return timer; }
    public double maxDuration() { return max; }
    public double multiplier() { return multiplier; }
    public void extendDuration(double duration) {
        this.timer = Math.max(this.timer, duration);
        this.max = Math.max(this.max, duration);
    }
    public void onStart(EntityUpdateContext ctx) { ctx.player().setScoreMultiplier(multiplier); }
    public void onTick(EntityUpdateContext ctx, double dt) { timer -= dt; }
    public void onEnd(EntityUpdateContext ctx) { ctx.player().setScoreMultiplier(1.0); }
    public boolean isFinished() { return timer <= 0; }
}
