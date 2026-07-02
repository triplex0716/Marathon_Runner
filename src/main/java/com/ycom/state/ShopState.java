package com.ycom.state;

import com.ycom.account.Account;
import com.ycom.account.AccountStore;
import com.ycom.account.Session;
import com.ycom.core.Config;
import com.ycom.resource.AssetManager;
import com.ycom.resource.AudioManager;
import com.ycom.system.InputSystem;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;

public class ShopState implements GameState {
    public static final int CAPSULE_PRICE = 300;
    
    private final GameStateManager gsm;
    private final Canvas canvas;
    private final InputSystem input;
    private String flashMessage = "";
    private double flashTimer = 0.0;
    private double time = 0.0;

    public ShopState(GameStateManager gsm, Canvas canvas, InputSystem input) {
        this.gsm = gsm;
        this.canvas = canvas;
        this.input = input;
    }

    @Override
    public void onEnter() {
        flashMessage = "";
        flashTimer = 0.0;
        time = 0.0;
    }

    @Override public void onExit() {}

    @Override
    public void update(double dt) {
        time += dt;
        if (flashTimer > 0.0) flashTimer = Math.max(0.0, flashTimer - dt);

        if (input.isKeyJustPressed(KeyCode.ESCAPE) || input.isKeyJustPressed(KeyCode.Q)) {
            gsm.setState(StateId.MENU);
            return;
        }
        if (!input.isMouseJustClicked()) return;

        if (btnBack().contains(input.getMouseX(), input.getMouseY())) {
            gsm.setState(StateId.MENU);
            return;
        }
        if (btnBuyCapsule().contains(input.getMouseX(), input.getMouseY())) {
            tryBuyCapsule();
        }
    }

    private void tryBuyCapsule() {
        if (Session.isGuest() || !Session.isLoggedIn()) {
            flash("Login required");
            return;
        }
        Account acc = Session.current();
        if (acc.getCoins() < CAPSULE_PRICE) {
            flash("Not enough coins");
            return;
        }
        acc.trySpendCoins(CAPSULE_PRICE);
        acc.addCapsules(1);
        AccountStore.save();
        AudioManager.playSfx("coin");
        flash("Capsule purchased!");
    }

    private void flash(String msg) {
        flashMessage = msg;
        flashTimer = 2.0;
    }

    @Override
    public void render() {
        GraphicsContext gc = canvas.getGraphicsContext2D();

        UIUtils.drawBackgroundAndTrack(gc, time);

        gc.setTextAlign(TextAlignment.CENTER);
        gc.setStroke(UIUtils.BORDER);
        gc.setLineWidth(10.0);
        gc.setFont(Font.font("Arial Black", FontWeight.EXTRA_BOLD, 76.0));
        gc.strokeText("SHOP", Config.LOGICAL_WIDTH / 2.0, 110.0);
        gc.setFill(UIUtils.YELLOW);
        gc.fillText("SHOP", Config.LOGICAL_WIDTH / 2.0, 110.0);

        drawWalletBar(gc);

        double w = 540, h = 360, gap = 40;
        double startX = (Config.LOGICAL_WIDTH - (3 * w + 2 * gap)) / 2.0;
        double startY = 395;
        
        drawCapsuleCard(gc, startX, startY);
        drawComingSoonCard(gc, startX + w + gap, startY, "Skins", AssetManager.playerImage(), UIUtils.CYAN);
        drawComingSoonCard(gc, startX + 2 * (w + gap), startY, "Boosts", AssetManager.spriteIcon(), UIUtils.PINK);

        UIUtils.drawNeoButton(gc, input, btnBack());
        drawFlash(gc);

        gc.setTextAlign(TextAlignment.LEFT);
    }

    private void drawWalletBar(GraphicsContext gc) {
        String text;
        if (Session.isGuest() || !Session.isLoggedIn()) {
            text = "GUEST MODE (LOGIN TO BUY)";
        } else {
            Account acc = Session.current();
            text = "COINS: " + acc.getCoins() + "    CAPSULES: " + acc.getCapsules();
        }
        
        double w = 900, h = 68;
        double x = (Config.LOGICAL_WIDTH - w) / 2.0;
        double y = 150;

        gc.setFill(UIUtils.BORDER);
        gc.fillRect(x + 6, y + 6, w, h);
        gc.setFill(UIUtils.WHITE);
        gc.fillRect(x, y, w, h);
        gc.setStroke(UIUtils.BORDER);
        gc.setLineWidth(4.0);
        gc.strokeRect(x, y, w, h);

        gc.setFill(UIUtils.BORDER);
        gc.setFont(Font.font("Arial Black", FontWeight.BOLD, 30.0));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText(text, Config.LOGICAL_WIDTH / 2.0, y + 46.0);
    }

    private void drawCapsuleCard(GraphicsContext gc, double x, double y) {
        drawNeoPanel(gc, x, y, 540, 360, UIUtils.YELLOW);

        String icon = AssetManager.revivalIcon();
        if (icon != null) {
            com.ycom.resource.AssetManager.draw(gc, icon, x + 200, y + 40, 140, 140);
        }

        gc.setFill(UIUtils.BORDER);
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setFont(Font.font("Arial Black", FontWeight.BOLD, 34.0));
        gc.fillText("REVIVAL CAPSULE", x + 270, y + 210);

        gc.setFont(Font.font("Arial Black", FontWeight.NORMAL, 22.0));
        gc.fillText("COST: " + CAPSULE_PRICE + " COINS", x + 270, y + 250);

        UIUtils.drawNeoButton(gc, input, btnBuyCapsule());
    }

    private void drawComingSoonCard(GraphicsContext gc, double x, double y, String title, String icon, Color c) {
        drawNeoPanel(gc, x, y, 540, 360, c);

        if (icon != null) {
            gc.setGlobalAlpha(0.4);
            com.ycom.resource.AssetManager.draw(gc, icon, x + 200, y + 40, 140, 140);
            gc.setGlobalAlpha(1.0);
        }

        gc.setFill(UIUtils.BORDER);
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setFont(Font.font("Arial Black", FontWeight.BOLD, 34.0));
        gc.fillText(title.toUpperCase(), x + 270, y + 210);

        gc.setFill(UIUtils.BORDER);
        gc.fillRect(x + 130 + 4, y + 260 + 4, 280, 60);
        gc.setFill(UIUtils.WHITE);
        gc.fillRect(x + 130, y + 260, 280, 60);
        gc.setStroke(UIUtils.BORDER);
        gc.setLineWidth(3.0);
        gc.strokeRect(x + 130, y + 260, 280, 60);
        
        gc.setFill(UIUtils.BORDER);
        gc.setFont(Font.font("Arial Black", FontWeight.BOLD, 26.0));
        gc.fillText("COMING SOON", x + 270, y + 300);
    }

    private void drawNeoPanel(GraphicsContext gc, double x, double y, double w, double h, Color c) {
        gc.setFill(UIUtils.BORDER);
        gc.fillRect(x + 10, y + 10, w, h);
        gc.setFill(c);
        gc.fillRect(x, y, w, h);
        gc.setStroke(UIUtils.BORDER);
        gc.setLineWidth(6.0);
        gc.strokeRect(x, y, w, h);
    }

    private void drawFlash(GraphicsContext gc) {
        if (flashTimer <= 0.0 || flashMessage.isEmpty()) return;
        gc.setGlobalAlpha(Math.min(1.0, flashTimer));
        
        gc.setStroke(UIUtils.BORDER);
        gc.setLineWidth(6.0);
        gc.setFont(Font.font("Arial Black", FontWeight.BOLD, 46.0));
        gc.strokeText(flashMessage, Config.LOGICAL_WIDTH / 2.0, 720);
        
        gc.setFill(UIUtils.RED);
        gc.fillText(flashMessage, Config.LOGICAL_WIDTH / 2.0, 720);
        gc.setGlobalAlpha(1.0);
    }

    private UIUtils.NeoBtn btnBuyCapsule() {
        double w = 540, h = 360, gap = 40;
        double startX = (Config.LOGICAL_WIDTH - (3 * w + 2 * gap)) / 2.0;
        return new UIUtils.NeoBtn(startX + (w - 240) / 2.0, 395 + h - 80 - 15, 240, 70, UIUtils.WHITE, "BUY", 24);
    }

    private UIUtils.NeoBtn btnBack() {
        return new UIUtils.NeoBtn(80.0, 880.0, 240.0, 72.0, UIUtils.WHITE, "BACK", 30);
    }
}
