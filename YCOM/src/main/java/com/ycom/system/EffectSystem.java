package com.ycom.system;
import com.ycom.entity.EntityUpdateContext;
import com.ycom.system.effect.PowerUpEffect;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class EffectSystem {
    private final List<PowerUpEffect> effects = new ArrayList<>();

    public void addEffect(PowerUpEffect newEffect, EntityUpdateContext ctx) {
        for (PowerUpEffect e : effects) {
            if (e.id().equals(newEffect.id())) {
                e.extendDuration(newEffect.duration());
                return;
            }
        }
        effects.add(newEffect);
        newEffect.onStart(ctx);
    }

    public void update(double dt, EntityUpdateContext ctx) {
        Iterator<PowerUpEffect> it = effects.iterator();
        while (it.hasNext()) {
            PowerUpEffect e = it.next();
            e.onTick(ctx, dt);
            if (e.isFinished()) {
                e.onEnd(ctx);
                it.remove();
            }
        }
    }
    
    public void clear(EntityUpdateContext ctx) {
        for (PowerUpEffect e : effects) {
            e.onEnd(ctx);
        }
        effects.clear();
    }

    public boolean hasEffect(String id) {
        for (PowerUpEffect e : effects) {
            if (e.id().equals(id)) {
                return true;
            }
        }
        return false;
    }
    
    public PowerUpEffect getEffect(String id) {
        for (PowerUpEffect e : effects) {
            if (e.id().equals(id)) {
                return e;
            }
        }
        return null;
    }

    public List<PowerUpEffect> activeEffects() {
        return effects;
    }
}
