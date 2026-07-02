package com.ycom.core;

import com.ycom.render.RenderFrame;
import java.util.List;
import com.ycom.system.ParticleSystem;
import com.ycom.system.effect.PowerUpEffect;

public record PhysicsSnapshot(
    RenderFrame renderFrame,
    int score,
    int coins,
    double playerZ,
    int revivalCount,
    double worldRate,
    boolean dyingAnimation,
    boolean awaitingRevival,
    double deathTimer,
    List<ParticleSystem.Shard> particles,
    List<PowerUpEffect> effects
) {}
