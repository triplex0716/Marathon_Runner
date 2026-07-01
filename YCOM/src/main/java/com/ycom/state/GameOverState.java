package com.ycom.state;

import com.ycom.account.Account;
import com.ycom.account.AccountStore;
import com.ycom.account.Session;
import com.ycom.core.Config;
import com.ycom.system.InputSystem;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;

public class GameOverState implements GameState {
    private final GameStateManager gsm;
    private final Canvas canvas;
    private final InputSystem input;
    public static int finalScore = 0;
    public static boolean isWin = false;
    private boolean newHighScore = false;
    private double time = 0.0;

    public GameOverState(GameStateManager gsm, Canvas canvas, InputSystem input) {
        this.gsm = gsm;
        this.canvas = canvas;
        this.input = input;
    }

    @Override
    public void onEnter() {
        newHighScore = false;
        time = 0.0;
        if (Session.isLoggedIn()) {
            Account acc = Session.current();
            if (finalScore > acc.highScore) {
                acc.highScore = finalScore;
                AccountStore.save();
                newHighScore = true;
            }
        }
    }

    @Override
    public void onExit() {}

    @Override
    public void update(double dt) {
        time += dt;
        boolean keyTrigger = input.isKeyJustPressed(KeyCode.SPACE)
                || input.isKeyJustPressed(KeyCode.ENTER)
                || input.isKeyJustPressed(KeyCode.Q);
        boolean clickTrigger = input.isMouseJustClicked()
                && btnMenu().contains(input.getMouseX(), input.getMouseY());
        if (keyTrigger || clickTrigger) {
            gsm.setState("MENU");
        }
    }

    @Override
    public void render() {
        GraphicsContext gc = canvas.getGraphicsContext2D();

        javafx.scene.image.Image bg = com.ycom.resource.AssetManager.gameOverBg();
        if (bg != null) {
            gc.drawImage(bg, 0, 0, Config.LOGICAL_WIDTH, Config.LOGICAL_HEIGHT);
        } else {
            UIUtils.drawBackgroundAndTrack(gc, time);
        }
        
        drawTitle(gc);
        drawScoreCard(gc);
        drawStatCards(gc);
        
        UIUtils.drawNeoButton(gc, input, btnMenu());
        
        gc.setTextAlign(TextAlignment.LEFT);
    }

    private void drawTitle(GraphicsContext gc) {
        String text = isWin ? "YOU WIN!" : "GAME OVER";
        gc.setFont(Font.font("Arial Black", FontWeight.EXTRA_BOLD, 90));
        gc.setTextAlign(TextAlignment.CENTER);
        double cx = Config.LOGICAL_WIDTH * 0.3;
        double cy = 240;
        
        gc.setStroke(UIUtils.BORDER);
        gc.setLineWidth(12.0);
        gc.strokeText(text, cx, cy);
        
        gc.setFill(isWin ? UIUtils.YELLOW : UIUtils.RED);
        gc.fillText(text, cx, cy);

        if (newHighScore) {
            gc.setFont(Font.font("Arial Black", FontWeight.BOLD, 40));
            gc.setStroke(UIUtils.BORDER);
            gc.setLineWidth(6.0);
            gc.strokeText("NEW HIGH SCORE!", cx, cy + 60);
            gc.setFill(UIUtils.YELLOW);
            gc.fillText("NEW HIGH SCORE!", cx, cy + 60);
        }
    }

    private void drawScoreCard(GraphicsContext gc) {
        double w = 600, h = 120;
        double x = Config.LOGICAL_WIDTH * 0.3 - w / 2.0;
        double y = 350;

        // Shadow
        gc.setFill(UIUtils.BORDER);
        gc.fillRect(x + 6, y + 6, w, h);

        gc.setFill(UIUtils.WHITE);
        gc.fillRect(x, y, w, h);
        gc.setStroke(UIUtils.BORDER);
        gc.setLineWidth(4.0);
        gc.strokeRect(x, y, w, h);

        gc.setFill(UIUtils.BORDER);
        gc.setFont(Font.font("Arial Black", FontWeight.BOLD, 48));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText("SCORE: " + finalScore, x + w / 2.0, y + h / 2.0 + 16);
    }

    private void drawStatCards(GraphicsContext gc) {
        boolean guest = Session.isGuest();
        Account acc = guest ? null : Session.current();
        String coins = guest ? "---" : String.valueOf(acc.coins);
        String caps = guest ? "---" : String.valueOf(acc.capsules);
        String best = guest ? "---" : String.valueOf(acc.highScore);

        double w = 860, h = 80;
        double x = Config.LOGICAL_WIDTH * 0.3 - w / 2.0;
        double y = 510;

        // Shadow
        gc.setFill(UIUtils.BORDER);
        gc.fillRect(x + 4, y + 4, w, h);

        gc.setFill(UIUtils.CYAN);
        gc.fillRect(x, y, w, h);
        gc.setStroke(UIUtils.BORDER);
        gc.setLineWidth(4.0);
        gc.strokeRect(x, y, w, h);

        gc.setFill(UIUtils.BORDER);
        gc.setFont(Font.font("Arial Black", FontWeight.BOLD, 30));
        gc.setTextAlign(TextAlignment.CENTER);
        
        String stats = String.format("COINS: %s   |   CAPSULES: %s   |   BEST: %s", coins, caps, best);
        gc.fillText(stats, x + w / 2.0, y + h / 2.0 + 10);
    }

    private UIUtils.NeoBtn btnMenu() {
        double w = 400, h = 80;
        double x = Config.LOGICAL_WIDTH * 0.3 - w / 2.0;
        double y = 650;
        return new UIUtils.NeoBtn(x, y, w, h, UIUtils.YELLOW, "BACK TO MENU", 28);
    }
}
