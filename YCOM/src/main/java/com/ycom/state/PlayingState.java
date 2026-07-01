package com.ycom.state;

import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
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
    private boolean isWin = false;

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

        if (scoreSystem.getScore() >= 10000 && !dyingAnimation && !awaitingRevival) {
            doGameWin();
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
        String text = isWin ? "YOU WIN!" : "YOU CAN'T OUTRUN ME!";
        gc.strokeText(text, W / 2.0, H * 0.22);
        gc.fillText(text, W / 2.0, H * 0.22);
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
        GameOverState.isWin = false;
        isWin = false;
        AudioManager.stopBGM();
        AudioManager.playSfx("ascension");
        dyingAnimation = true;
        deathTimer = 0.0;
    }

    private void doGameWin() {
        GameOverState.finalScore = scoreSystem.getScore();
        GameOverState.isWin = true;
        isWin = true;
        AudioManager.stopBGM();
        AudioManager.playSfx("win");
        dyingAnimation = true;
        deathTimer = 0.0;
    }

    private void doRevive() {
        clearNearbyObstacles();
        world.getPlayer().revive();
        eventBus.clear();
        AudioManager.playSfx("invincible");
        AudioManager.playBGM();
        awaitingRevival = false;
    }

    private void clearNearbyObstacles() {
        double playerZ = world.getPlayer().z;
        double radius = Config.REVIVE_CLEAR_RADIUS;
        for (GameObject obj : world.getObjects()) {
            if (obj.kind() != GameObject.ObjectKind.OBSTACLE) continue;
            double dz = obj.z - playerZ;
            if (dz >= -1.5 && dz <= radius) {
                obj.active = false;
            }
        }
    }

    private void handleRevivalPrompt() {
        boolean capsuleAvail = world.getPlayer().hasRevival();
        int cost = currentCoinReviveCost();
        boolean coinAvail = cost != Integer.MAX_VALUE && scoreSystem.getCoins() >= cost;

        double mx = input.getMouseX();
        double my = input.getMouseY();
        boolean overCapsule = capsuleAvail && capsuleButton().contains(mx, my);
        boolean overCoin = coinAvail && coinButton().contains(mx, my);
        boolean overQuit = quitButton().contains(mx, my);
        Scene scene = canvas.getScene();
        if (scene != null) {
            scene.setCursor((overCapsule || overCoin || overQuit) ? Cursor.HAND : Cursor.DEFAULT);
        }

        if (input.isKeyJustPressed(KeyCode.Y) && capsuleAvail) {
            handleUseCapsuleRevive();
            return;
        }
        if (input.isKeyJustPressed(KeyCode.C)) {
            handleCoinRevive();
            return;
        }
        if (input.isKeyJustPressed(KeyCode.N) || input.isKeyJustPressed(KeyCode.ESCAPE)) {
            handleQuitToMenu();
            return;
        }
        if (input.isMouseJustClicked()) {
            if (overCapsule) { handleUseCapsuleRevive(); return; }
            if (overCoin) { handleCoinRevive(); return; }
            if (overQuit) { handleQuitToMenu(); }
        }
    }

    private void handleUseCapsuleRevive() {
        if (!world.getPlayer().hasRevival()) return;
        world.getPlayer().consumeRevival();
        resetReviveCursor();
        doRevive();
    }

    private void handleCoinRevive() {
        int cost = currentCoinReviveCost();
        if (scoreSystem.trySpendCoins(cost)) {
            world.getPlayer().onCoinRevive();
            resetReviveCursor();
            doRevive();
        }
    }

    private void handleQuitToMenu() {
        resetReviveCursor();
        awaitingRevival = false;
        doGameOver();
    }

    private void resetReviveCursor() {
        Scene s = canvas.getScene();
        if (s != null) s.setCursor(Cursor.DEFAULT);
    }

    private static final double REVIVE_PANEL_W = 760.0;
    private static final double REVIVE_PANEL_H = 820.0;
    private static final double REVIVE_BTN_W = 660.0;
    private static final double REVIVE_BTN_H = 108.0;
    private static final double REVIVE_BTN_GAP = 18.0;
    private static final double REVIVE_BTN_FIRST_Y_OFFSET = 340.0;

    private double revivePanelX() { return (Config.LOGICAL_WIDTH - REVIVE_PANEL_W) / 2.0; }
    private double revivePanelY() { return (Config.LOGICAL_HEIGHT - REVIVE_PANEL_H) / 2.0; }

    private ButtonRect capsuleButton() { return reviveButtonRect(0); }
    private ButtonRect coinButton() { return reviveButtonRect(1); }
    private ButtonRect quitButton() { return reviveButtonRect(2); }

    private ButtonRect reviveButtonRect(int idx) {
        double x = (Config.LOGICAL_WIDTH - REVIVE_BTN_W) / 2.0;
        double y = revivePanelY() + REVIVE_BTN_FIRST_Y_OFFSET + idx * (REVIVE_BTN_H + REVIVE_BTN_GAP);
        return new ButtonRect(x, y, REVIVE_BTN_W, REVIVE_BTN_H);
    }

    private void drawRevivalPrompt() {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        // solid overlay pattern instead of blurry shadow? Let's use a flat slightly transparent color, 
        // but no gradients.
        gc.setFill(Color.rgb(0, 0, 0, 0.75));
        gc.fillRect(0, 0, Config.LOGICAL_WIDTH, Config.LOGICAL_HEIGHT);

        drawRevivePanel(gc);
        drawReviveTitle(gc);
        drawHudCards(gc);

        boolean capsuleAvail = world.getPlayer().hasRevival();
        int cost = currentCoinReviveCost();
        boolean coinAvail = cost != Integer.MAX_VALUE && scoreSystem.getCoins() >= cost;
        int capsuleCount = world.getPlayer().revivalCount();

        drawCapsuleButton(gc, capsuleAvail, capsuleCount);
        drawCoinButton(gc, coinAvail, cost);
        drawQuitButton(gc);

        drawReviveFooter(gc);
        gc.setTextAlign(TextAlignment.LEFT);
    }

    // ---------- panel ----------
    private void drawRevivePanel(GraphicsContext gc) {
        double x = revivePanelX();
        double y = revivePanelY();
        
        // Solid black shadow
        gc.setFill(UIUtils.BORDER);
        gc.fillRect(x + 10, y + 10, REVIVE_PANEL_W, REVIVE_PANEL_H);
        
        // Solid panel fill
        gc.setFill(UIUtils.CYAN);
        gc.fillRect(x, y, REVIVE_PANEL_W, REVIVE_PANEL_H);
        
        // Thick black border
        gc.setStroke(UIUtils.BORDER);
        gc.setLineWidth(6.0);
        gc.strokeRect(x, y, REVIVE_PANEL_W, REVIVE_PANEL_H);
    }

    // ---------- title ----------
    private void drawReviveTitle(GraphicsContext gc) {
        double cx = Config.LOGICAL_WIDTH / 2.0;
        double y = revivePanelY() + 80.0;
        
        gc.setFont(Font.font("Arial Black", FontWeight.EXTRA_BOLD, 48));
        gc.setTextAlign(TextAlignment.CENTER);
        
        gc.setStroke(UIUtils.BORDER);
        gc.setLineWidth(8.0);
        gc.strokeText("CONTINUE?", cx, y);
        gc.setFill(UIUtils.YELLOW);
        gc.fillText("CONTINUE?", cx, y);
    }

    // ---------- HUD cards ----------
    private void drawHudCards(GraphicsContext gc) {
        double y = revivePanelY() + 220;
        double cardW = 320, cardH = 90, gap = 24;
        double totalW = cardW * 2 + gap;
        double startX = (Config.LOGICAL_WIDTH - totalW) / 2.0;

        drawHudCard(gc, startX, y, cardW, cardH, "Score",
                String.valueOf(scoreSystem.getScore()), Color.web("#5DB4FF"), null, true);
        drawHudCard(gc, startX + cardW + gap, y, cardW, cardH, "Coins",
                String.valueOf(scoreSystem.getCoins()), Color.web("#FFC73B"),
                AssetManager.coinIcon(), false);
    }

    private void drawHudCard(GraphicsContext gc, double x, double y, double w, double h,
                             String label, String value, Color valueColor,
                             Image icon, boolean trophyFallback) {
        gc.setFill(Color.rgb(20, 32, 60, 0.65));
        gc.fillRoundRect(x, y, w, h, 16, 16);
        gc.setStroke(Color.rgb(160, 200, 255, 0.45));
        gc.setLineWidth(1.6);
        gc.strokeRoundRect(x, y, w, h, 16, 16);

        double iconSize = 56;
        double iconX = x + 16;
        double iconY = y + (h - iconSize) / 2.0;
        if (icon != null) {
            gc.drawImage(icon, iconX, iconY, iconSize, iconSize);
        } else if (trophyFallback) {
            drawTrophyIcon(gc, iconX, iconY, iconSize, valueColor);
        }

        gc.setFill(Color.rgb(210, 222, 245, 0.88));
        gc.setFont(Font.font("Arial", FontWeight.NORMAL, 20));
        gc.setTextAlign(TextAlignment.LEFT);
        gc.fillText(label, x + 88, y + 36);

        gc.setFill(valueColor);
        gc.setFont(Font.font("Arial Black", FontWeight.BOLD, 36));
        gc.fillText(value, x + 88, y + 76);
    }

    private void drawTrophyIcon(GraphicsContext gc, double x, double y, double size, Color tint) {
        gc.setFill(tint);
        gc.fillRoundRect(x + size * 0.25, y + size * 0.10, size * 0.50, size * 0.45, 8, 8);
        gc.setStroke(tint);
        gc.setLineWidth(3.0);
        gc.strokeArc(x + size * 0.05, y + size * 0.18, size * 0.30, size * 0.30,
                60, 180, javafx.scene.shape.ArcType.OPEN);
        gc.strokeArc(x + size * 0.65, y + size * 0.18, size * 0.30, size * 0.30,
                -60, -180, javafx.scene.shape.ArcType.OPEN);
        gc.fillRect(x + size * 0.42, y + size * 0.55, size * 0.16, size * 0.20);
        gc.fillRoundRect(x + size * 0.20, y + size * 0.75, size * 0.60, size * 0.15, 4, 4);
        gc.setFill(Color.WHITE);
        gc.fillOval(x + size * 0.42, y + size * 0.22, size * 0.16, size * 0.16);
    }

    // ---------- buttons ----------
    private void drawCapsuleButton(GraphicsContext gc, boolean avail, int count) {
        ButtonRect r = capsuleButton();
        String text = avail ? "USE CAPSULE (" + count + ")" : "NO CAPSULES";
        Color bg = avail ? UIUtils.PINK : Color.GRAY;
        UIUtils.drawNeoButton(gc, input, new UIUtils.NeoBtn(r.x, r.y, r.w, r.h, bg, text, 28));
    }

    private void drawCoinButton(GraphicsContext gc, boolean avail, int cost) {
        ButtonRect r = coinButton();
        String text = "USE COINS";
        if (cost != Integer.MAX_VALUE) {
            text += " (" + cost + ")";
            if (!avail) text += " - NEED MORE";
        } else {
            text = "COIN REVIVE MAXED";
        }
        Color bg = avail ? UIUtils.YELLOW : Color.GRAY;
        UIUtils.drawNeoButton(gc, input, new UIUtils.NeoBtn(r.x, r.y, r.w, r.h, bg, text, 28));
    }

    private void drawQuitButton(GraphicsContext gc) {
        ButtonRect r = quitButton();
        UIUtils.drawNeoButton(gc, input, new UIUtils.NeoBtn(r.x, r.y, r.w, r.h, UIUtils.WHITE, "GIVE UP", 28));
    }

    // ---------- footer ----------
    private void drawReviveFooter(GraphicsContext gc) {
        double y = revivePanelY() + REVIVE_PANEL_H - 32;
        double cx = Config.LOGICAL_WIDTH / 2.0;
        String text = "TIP: COLLECT COINS TO RUN FURTHER!";
        gc.setFont(Font.font("Arial Black", FontWeight.BOLD, 20));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setStroke(UIUtils.BORDER);
        gc.setLineWidth(4.0);
        gc.strokeText(text, cx, y);
        gc.setFill(UIUtils.YELLOW);
        gc.fillText(text, cx, y);
    }

    private record ButtonRect(double x, double y, double w, double h) {
        boolean contains(double px, double py) {
            return px >= x && px <= x + w && py >= y && py <= y + h;
        }
    }
}
