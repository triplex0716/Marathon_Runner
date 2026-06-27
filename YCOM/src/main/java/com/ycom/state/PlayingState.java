package com.ycom.state;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import com.ycom.account.Account;
import com.ycom.account.AccountStore;
import com.ycom.account.Session;
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
import com.ycom.resource.AssetManager;
import javafx.scene.image.Image;

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

    // 死亡升天动画
    private static final double DEATH_DURATION = 3.5;
    private boolean dyingAnimation = false;
    private double deathTimer = 0.0;

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
        dyingAnimation = false;
        deathTimer = 0.0;
        registerEventHandlers();

        Account acc = Session.current();
        if (Session.isGuest()) {
            acc.coins = 0;
            acc.capsules = 0;
        }
        scoreSystem.setCoins(acc.coins);
        world.getPlayer().setRevivalCount(acc.capsules);
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
        if (dyingAnimation) {
            deathTimer += dt;
            if (deathTimer >= DEATH_DURATION) {
                dyingAnimation = false;
                gsm.setState("GAMEOVER");
            }
            return;
        }
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

        int curCoins = scoreSystem.getCoins();
        int curCapsules = world.getPlayer().revivalCount();
        Account acc = Session.current();
        if (acc.coins != curCoins || acc.capsules != curCapsules) {
            acc.coins = curCoins;
            acc.capsules = curCapsules;
            if (!Session.isGuest()) AccountStore.save();
        }
    }

    @Override
    public void render() {
        if (renderSystem != null && world != null && scoreSystem != null) {
            renderSystem.render(world, scoreSystem, particleSystem);
        }
        if (dyingAnimation) {
            drawDeathAnimation();
        }
        if (awaitingRevival) {
            drawRevivalPrompt();
        }
    }

    private void drawDeathAnimation() {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        double t = Math.min(1.0, deathTimer / DEATH_DURATION);
        double W = Config.LOGICAL_WIDTH;
        double H = Config.LOGICAL_HEIGHT;

        // 渐暗的红黑背景
        gc.setGlobalAlpha(Math.min(0.82, t * 1.6));
        gc.setFill(Color.rgb(20, 0, 6));
        gc.fillRect(0, 0, W, H);
        gc.setGlobalAlpha(1.0);

        // 升天的张雪峰老师：从下往上飘 + 旋转 + 后段淡出
        Image img = AssetManager.ascensionImage();
        if (img != null && img.getWidth() > 0.0) {
            double ease = 1.0 - Math.pow(1.0 - t, 2.0);
            double cx = W / 2.0;
            double cy = (0.72 - 0.52 * ease) * H;
            double imgScale = 1.0 - 0.25 * t;
            double drawH = H * 0.55 * imgScale;
            double drawW = drawH * (img.getWidth() / img.getHeight());
            double angle = t * 150.0;
            double alpha = t < 0.65 ? 1.0 : Math.max(0.0, 1.0 - (t - 0.65) / 0.35);

            // 升天光晕
            gc.setGlobalAlpha(alpha * 0.45);
            gc.setFill(Color.rgb(255, 244, 200));
            double halo = drawH * (0.62 + 0.12 * Math.sin(t * Math.PI * 4.0));
            gc.fillOval(cx - halo, cy - halo, halo * 2.0, halo * 2.0);

            // 旋转绘制角色
            gc.setGlobalAlpha(alpha);
            gc.save();
            gc.translate(cx, cy);
            gc.rotate(angle);
            gc.drawImage(img, -drawW / 2.0, -drawH / 2.0, drawW, drawH);
            gc.restore();
            gc.setGlobalAlpha(1.0);
        }

        // 大字标语（横屏一行）
        double textAlpha = Math.max(0.0, Math.min(1.0, (t - 0.3) / 0.4));
        gc.setGlobalAlpha(textAlpha);
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 120));
        gc.setLineWidth(8.0);
        gc.setStroke(Color.rgb(110, 0, 0));
        gc.setFill(Color.rgb(255, 215, 0));
        gc.strokeText("YOU CAN'T OUTRUN ME!", W / 2.0, H * 0.22);
        gc.fillText("YOU CAN'T OUTRUN ME!", W / 2.0, H * 0.22);
        gc.setGlobalAlpha(1.0);
        gc.setTextAlign(TextAlignment.LEFT);
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
        AudioManager.playSfx("ascension");
        dyingAnimation = true;
        deathTimer = 0.0;
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
