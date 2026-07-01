package com.ycom.system.effect;
import com.ycom.entity.EntityUpdateContext;
import com.ycom.core.TimeManager;

public class BoostEffect implements PowerUpEffect {
    private double timer;
    private double max;
    private double worldRate;
    private double bgmRate;

    public BoostEffect(double duration, double worldRate, double bgmRate) {
        this.timer = duration;
        this.max = duration;
        this.worldRate = worldRate;
        this.bgmRate = bgmRate;
    }
    public String id() { return "boost"; }
    public double duration() { return timer; }
    public double maxDuration() { return max; }
    public void extendDuration(double duration) {
        this.timer = Math.max(this.timer, duration);
        this.max = Math.max(this.max, duration);
        TimeManager.activateBoost(this.timer, this.worldRate, this.bgmRate);
    }
    public void onStart(EntityUpdateContext ctx) { 
        ctx.player().setBoosted(true); 
        TimeManager.activateBoost(this.timer, this.worldRate, this.bgmRate);
    }
    public void onTick(EntityUpdateContext ctx, double dt) { timer -= dt; }
    public void onEnd(EntityUpdateContext ctx) { 
        ctx.player().setBoosted(false); 
        TimeManager.clearBoost();
    }
    public boolean isFinished() { return timer <= 0; }
}
