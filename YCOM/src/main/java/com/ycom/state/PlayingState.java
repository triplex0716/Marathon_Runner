package com.ycom.state;

import javafx.scene.canvas.Canvas;
import javafx.scene.input.KeyCode;
import com.ycom.core.Config;
import com.ycom.event.BoostActivatedEvent;
import com.ycom.event.CoinCollectedEvent;
import com.ycom.event.EventBus;
import com.ycom.event.GameOverEvent;
import com.ycom.event.MagnetActivatedEvent;
import com.ycom.event.ObstacleDestroyedEvent;
import com.ycom.event.PlayerHitEvent;
import com.ycom.event.ScoreAddEvent;
import com.ycom.core.TimeManager;
import com.ycom.system.InputSystem;
import com.ycom.world.GameWorld;
import com.ycom.system.RenderSystem;
import com.ycom.system.SpawnSystem;
import com.ycom.system.CollisionSystem;
import com.ycom.system.ScoreSystem;
import com.ycom.resource.AudioManager;

public class PlayingState implements GameState {
    private final GameStateManager gsm;
    private final Canvas canvas;
    private final InputSystem input;

    private EventBus eventBus;
    private GameWorld world;
    private RenderSystem renderSystem;
    private SpawnSystem spawnSystem;
    private CollisionSystem collisionSystem;
    private ScoreSystem scoreSystem;
    private Config.Difficulty currentDifficulty = Config.DEFAULT_DIFFICULTY;

    public PlayingState(GameStateManager gsm, Canvas canvas, InputSystem input) {
        this.gsm = gsm;
        this.canvas = canvas;
        this.input = input;
    }

    public void resetGame() {
        resetGame(currentDifficulty);
    }

    public void resetGame(Config.Difficulty difficulty) {
        currentDifficulty = difficulty == null ? Config.DEFAULT_DIFFICULTY : difficulty;
        TimeManager.reset(currentDifficulty);
        eventBus = new EventBus();
        world = new GameWorld();
        renderSystem = new RenderSystem(canvas);
        spawnSystem = new SpawnSystem(world, currentDifficulty);
        scoreSystem = new ScoreSystem(eventBus);
        collisionSystem = new CollisionSystem(world, eventBus);
        registerEventHandlers();
    }

    @Override
    public void onEnter() {
        if (world == null) {
            resetGame();
        }
        AudioManager.setBgmRate(TimeManager.getAudioRate());
        AudioManager.playBGM();
    }

    @Override
    public void onExit() {
    }

    @Override
    public void update(double dt) {
        if (input.isKeyJustPressed(KeyCode.P) || input.isKeyJustPressed(KeyCode.ESCAPE)) {
            AudioManager.pauseBGM();
            gsm.setState("PAUSED");
            return;
        }

        TimeManager.update(dt);
        double worldDt = TimeManager.getScaledDeltaTime(dt);

        world.update(worldDt, dt, input, eventBus);
        spawnSystem.update(world.getPlayer().z);
        collisionSystem.update();
        scoreSystem.update(worldDt, world.getPlayer().z);
        AudioManager.setBgmRate(TimeManager.getAudioRate());
    }

    @Override
    public void render() {
        if (renderSystem != null && world != null && scoreSystem != null) {
            renderSystem.render(world, scoreSystem);
        }
    }

    private void registerEventHandlers() {
        eventBus.subscribe(CoinCollectedEvent.class, event -> AudioManager.playSfx("coin"));
        eventBus.subscribe(MagnetActivatedEvent.class, event -> {
            world.getPlayer().activateMagnet(event.duration());
            AudioManager.playSfx("win");
        });
        eventBus.subscribe(BoostActivatedEvent.class, event -> {
            world.getPlayer().activateBoost(event.duration());
            TimeManager.activateBoost(event.duration(), event.worldRate(), event.bgmRate());
            AudioManager.playSfx("invincible");
        });
        eventBus.subscribe(ObstacleDestroyedEvent.class, event -> {
            eventBus.publish(new ScoreAddEvent(25, "BOOST_BREAK"));
            AudioManager.playSfx("win");
        });
        eventBus.subscribe(PlayerHitEvent.class, event -> eventBus.publish(new GameOverEvent(event.hitType())));
        eventBus.subscribe(GameOverEvent.class, event -> {
            GameOverState.finalScore = scoreSystem.getScore();
            AudioManager.stopBGM();
            AudioManager.playSfx("fail");
            gsm.setState("GAMEOVER");
        });
    }
}
