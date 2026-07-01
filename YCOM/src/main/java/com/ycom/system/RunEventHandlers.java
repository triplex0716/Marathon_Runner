package com.ycom.system;

import com.ycom.event.EventBus;
import com.ycom.event.CoinCollectedEvent;
import com.ycom.event.MagnetActivatedEvent;
import com.ycom.event.BoostActivatedEvent;
import com.ycom.event.RevivalCollectedEvent;
import com.ycom.event.ScoreMultiplierActivatedEvent;
import com.ycom.event.ObstacleDestroyedEvent;
import com.ycom.event.PlayerHitEvent;
import com.ycom.event.GameOverEvent;
import com.ycom.event.ScoreAddEvent;
import com.ycom.world.GameWorld;
import com.ycom.resource.AudioManager;
import com.ycom.core.TimeManager;
import javafx.scene.paint.Color;

public class RunEventHandlers {
    public static void register(EventBus eventBus, GameWorld world, ParticleSystem particleSystem, EffectSystem effectSystem, Runnable onGameOver) {
        eventBus.subscribe(CoinCollectedEvent.class, event -> AudioManager.playSfx("coin"));
        eventBus.subscribe(MagnetActivatedEvent.class, event -> {
            effectSystem.addEffect(new com.ycom.system.effect.MagnetEffect(event.duration()), new com.ycom.entity.EntityUpdateContext(null, eventBus, world.getPlayer(), 0, world));
            AudioManager.playSfx("win");
        });
        eventBus.subscribe(BoostActivatedEvent.class, event -> {
            effectSystem.addEffect(new com.ycom.system.effect.BoostEffect(event.duration(), event.worldRate(), event.bgmRate()), new com.ycom.entity.EntityUpdateContext(null, eventBus, world.getPlayer(), 0, world));
                        AudioManager.playSfx("invincible");
        });
        eventBus.subscribe(RevivalCollectedEvent.class, event -> {
            world.getPlayer().addRevival();
            AudioManager.playSfx("win");
        });
        eventBus.subscribe(ScoreMultiplierActivatedEvent.class, event -> {
            effectSystem.addEffect(new com.ycom.system.effect.ScoreMultiplierEffect(event.duration(), event.multiplier()), new com.ycom.entity.EntityUpdateContext(null, eventBus, world.getPlayer(), 0, world));
            AudioManager.playSfx("win");
        });
        eventBus.subscribe(ObstacleDestroyedEvent.class, event -> {
            eventBus.publish(new ScoreAddEvent(25, "BOOST_BREAK"));
            particleSystem.spawnBreak(
                    event.x(), event.y(), event.z(),
                    event.width(), event.height(), event.depth(),
                    Color.color(event.red(), event.green(), event.blue())
            );
            if (AudioManager.hasSfx("obstacle_break")) {
                AudioManager.playSfx("obstacle_break");
            } else {
                AudioManager.playSfx("win");
            }
        });
        eventBus.subscribe(PlayerHitEvent.class, event -> eventBus.publish(new GameOverEvent(event.hitType())));
        eventBus.subscribe(GameOverEvent.class, event -> onGameOver.run());
    }
}
