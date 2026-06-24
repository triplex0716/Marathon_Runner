package com.ycom.state;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import com.ycom.core.Config;
import com.ycom.entity.GameObject;
import com.ycom.event.BoostActivatedEvent;
import com.ycom.event.CoinCollectedEvent;
import com.ycom.event.EventBus;
import com.ycom.event.GameOverEvent;
import com.ycom.event.MagnetActivatedEvent;
import com.ycom.event.ObstacleDestroyedEvent;
import com.ycom.event.PlayerHitEvent;
import com.ycom.event.RevivalCollectedEvent;
import com.ycom.event.ScoreAddEvent;
import com.ycom.event.ScoreMultiplierActivatedEvent;
import com.ycom.core.TimeManager;
import com.ycom.system.InputSystem;
import com.ycom.world.GameWorld;
import com.ycom.system.ParticleSystem;
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
    private ParticleSystem particleSystem;
    private Config.Difficulty currentDifficulty = Config.DEFAULT_DIFFICULTY;
    private boolean awaitingRevival = false;

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
        scoreSystem = new ScoreSystem(eventBus, world.getPlayer());
        collisionSystem = new CollisionSystem(world, eventBus);
        particleSystem = new ParticleSystem();
        awaitingRevival = false;
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
        if (awaitingRevival) {
            handleRevivalPrompt();
            return;
        }
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
        particleSystem.update(worldDt, world.getPlayer().z);
        AudioManager.setBgmRate(TimeManager.getAudioRate());
    }

    @Override
    public void render() {
        if (renderSystem != null && world != null && scoreSystem != null) {
            renderSystem.render(world, scoreSystem, particleSystem);
        }
        if (awaitingRevival) {
            drawRevivalPrompt();
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
            AudioManager.playSfx("invincible");
        });
        eventBus.subscribe(RevivalCollectedEvent.class, event -> {
            world.getPlayer().addRevival();
            AudioManager.playSfx("win");
        });
        eventBus.subscribe(ScoreMultiplierActivatedEvent.class, event -> {
            world.getPlayer().activateScoreMultiplier(event.duration(), event.multiplier());
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
        eventBus.subscribe(GameOverEvent.class, event -> {
            if (canOfferRevive()) {
                awaitingRevival = true;
                AudioManager.pauseBGM();
                return;
            }
            doGameOver();
        });
    }

    private boolean canOfferRevive() {
        return world.getPlayer().hasRevival() || scoreSystem.getCoins() >= currentCoinReviveCost();
    }

    private int currentCoinReviveCost() {
        int used = world.getPlayer().coinRevivesUsed();
        int[] costs = Config.COIN_REVIVE_COSTS;
        if (used >= costs.length) {
            return Integer.MAX_VALUE;
        }
        return costs[used];
    }

    private void doGameOver() {
        GameOverState.finalScore = scoreSystem.getScore();
        AudioManager.stopBGM();
        AudioManager.playSfx("fail");
        gsm.setState("GAMEOVER");
    }

    private void doRevive() {
        clearNearbyObstacles();
        world.getPlayer().revive();
        AudioManager.playSfx("invincible");
        AudioManager.playBGM();
        awaitingRevival = false;
    }

    private void clearNearbyObstacles() {
        double playerZ = world.getPlayer().z;
        double radius = Config.REVIVE_CLEAR_RADIUS;
        for (GameObject obj : world.getObjects()) {
            if (obj.kind() == GameObject.ObjectKind.OBSTACLE
                    && Math.abs(obj.z - playerZ) <= radius) {
                obj.active = false;
            }
        }
    }

    private void handleRevivalPrompt() {
        if (input.isKeyJustPressed(KeyCode.Y) && world.getPlayer().hasRevival()) {
            world.getPlayer().consumeRevival();
            doRevive();
            return;
        }
        if (input.isKeyJustPressed(KeyCode.C)) {
            int cost = currentCoinReviveCost();
            if (scoreSystem.trySpendCoins(cost)) {
                world.getPlayer().onCoinRevive();
                doRevive();
            }
            return;
        }
        if (input.isKeyJustPressed(KeyCode.N) || input.isKeyJustPressed(KeyCode.ESCAPE)) {
            awaitingRevival = false;
            doGameOver();
        }
    }

    private void drawRevivalPrompt() {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.setFill(Color.rgb(0, 0, 0, 0.72));
        gc.fillRect(0, 0, Config.LOGICAL_WIDTH, Config.LOGICAL_HEIGHT);

        gc.setFill(Color.WHITE);
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 84));
        gc.fillText("REVIVE?", Config.LOGICAL_WIDTH / 2.0, Config.LOGICAL_HEIGHT / 2.0 - 240.0);

        gc.setFont(Font.font("Arial", FontWeight.NORMAL, 40));
        gc.fillText("Score " + scoreSystem.getScore() + "    Coins " + scoreSystem.getCoins(),
                Config.LOGICAL_WIDTH / 2.0, Config.LOGICAL_HEIGHT / 2.0 - 140.0);

        double lineY = Config.LOGICAL_HEIGHT / 2.0 - 40.0;
        if (world.getPlayer().hasRevival()) {
            gc.fillText("(Y) Use Revival Capsule  x" + world.getPlayer().revivalCount(),
                    Config.LOGICAL_WIDTH / 2.0, lineY);
            lineY += 70.0;
        }
        int cost = currentCoinReviveCost();
        if (cost != Integer.MAX_VALUE && scoreSystem.getCoins() >= cost) {
            gc.fillText("(C) Spend " + cost + " coins",
                    Config.LOGICAL_WIDTH / 2.0, lineY);
            lineY += 70.0;
        }
        gc.fillText("(N) Quit to Menu", Config.LOGICAL_WIDTH / 2.0, lineY);

        gc.setTextAlign(TextAlignment.LEFT);
    }
}
