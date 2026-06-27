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
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;

public class ShopState implements GameState {
    public static final int CAPSULE_PRICE = 300;
    private static final double CARD_RADIUS = 12.0;

    private static final double CARD_W = 540.0;
    private static final double CARD_H = 360.0;
    private static final double ROW1_Y = 244.0;
    private static final double ROW2_Y = ROW1_Y + CARD_H + 32.0;
    private static final double ROW1_X1 = 96.0;
    private static final double ROW1_X2 = ROW1_X1 + CARD_W + 32.0;
    private static final double ROW1_X3 = ROW1_X2 + CARD_W + 32.0;
    private static final double ROW2_X1 = (Config.LOGICAL_WIDTH - (2 * CARD_W + 32.0)) / 2.0;
    private static final double ROW2_X2 = ROW2_X1 + CARD_W + 32.0;

    private static final double BUY_BTN_W = 220.0;
    private static final double BUY_BTN_H = 72.0;

    private final GameStateManager gsm;
    private final Canvas canvas;
    private final InputSystem input;
    private String flashMessage = "";
    private double flashTimer = 0.0;

    public ShopState(GameStateManager gsm, Canvas canvas, InputSystem input) {
        this.gsm = gsm;
        this.canvas = canvas;
        this.input = input;
    }

    @Override
    public void onEnter() {
        flashMessage = "";
        flashTimer = 0.0;
    }

    @Override public void onExit() {}

    @Override
    public void update(double dt) {
        if (flashTimer > 0.0) flashTimer = Math.max(0.0, flashTimer - dt);

        if (input.isKeyJustPressed(KeyCode.ESCAPE) || input.isKeyJustPressed(KeyCode.Q)) {
            gsm.setState("MENU");
            return;
        }
        if (!input.isMouseJustClicked()) return;

        double mx = input.getMouseX();
        double my = input.getMouseY();

        if (backButton().contains(mx, my)) {
            gsm.setState("MENU");
            return;
        }
        if (capsuleBuyButton().contains(mx, my)) {
            tryBuyCapsule();
        }
    }

    private void tryBuyCapsule() {
        if (Session.isGuest() || !Session.isLoggedIn()) {
            flash("Login required to purchase");
            return;
        }
        Account acc = Session.current();
        if (acc.coins < CAPSULE_PRICE) {
            flash("Not enough coins");
            return;
        }
        acc.coins -= CAPSULE_PRICE;
        acc.capsules += 1;
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
        drawBackground(gc);

        gc.setTextAlign(TextAlignment.CENTER);
        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 76.0));
        gc.fillText("SHOP", Config.LOGICAL_WIDTH / 2.0, 130.0);

        drawWalletBar(gc);

        drawCapsuleCard(gc, ROW1_X1, ROW1_Y);
        drawComingSoonCard(gc, ROW1_X2, ROW1_Y, "Skins", AssetManager.playerImage());
        drawComingSoonCard(gc, ROW1_X3, ROW1_Y, "Boosts", AssetManager.spriteIcon());
        drawComingSoonCard(gc, ROW2_X1, ROW2_Y, "Sound Packs", null);
        drawComingSoonCard(gc, ROW2_X2, ROW2_Y, "Daily Mission", AssetManager.coinIcon());

        drawBackButton(gc);
        drawFlash(gc);

        gc.setTextAlign(TextAlignment.LEFT);
    }

    private void drawBackground(GraphicsContext gc) {
        if (AssetManager.background() != null) {
            gc.drawImage(AssetManager.background(), 0, 0, Config.LOGICAL_WIDTH, Config.LOGICAL_HEIGHT);
            gc.setFill(Color.rgb(14, 26, 42, 0.78));
            gc.fillRect(0, 0, Config.LOGICAL_WIDTH, Config.LOGICAL_HEIGHT);
        } else {
            gc.setFill(Color.web("#0e1a2a"));
            gc.fillRect(0, 0, Config.LOGICAL_WIDTH, Config.LOGICAL_HEIGHT);
        }
    }

    private void drawWalletBar(GraphicsContext gc) {
        String text;
        if (Session.isGuest() || !Session.isLoggedIn()) {
            text = "Guest mode  (login to purchase)";
        } else {
            Account acc = Session.current();
            text = "Wallet: " + acc.coins + "  coins    Capsules: " + acc.capsules;
        }
        double w = 900.0;
        double h = 68.0;
        double x = (Config.LOGICAL_WIDTH - w) / 2.0;
        double y = 160.0;
        gc.setFill(Color.rgb(0, 0, 0, 0.46));
        gc.fillRoundRect(x, y, w, h, CARD_RADIUS, CARD_RADIUS);
        gc.setStroke(Color.rgb(255, 255, 255, 0.55));
        gc.setLineWidth(2.0);
        gc.strokeRoundRect(x, y, w, h, CARD_RADIUS, CARD_RADIUS);
        gc.setFill(Color.WHITE);
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 30.0));
        gc.fillText(text, Config.LOGICAL_WIDTH / 2.0, y + 46.0);
    }

    private void drawCapsuleCard(GraphicsContext gc, double x, double y) {
        boolean canBuy = !Session.isGuest() && Session.isLoggedIn() && Session.current().coins >= CAPSULE_PRICE;
        drawPanel(gc, x, y, CARD_W, CARD_H, Color.rgb(28, 56, 92, 0.78), Color.rgb(255, 255, 255, 0.45));

        Image icon = AssetManager.revivalIcon();
        drawCardIcon(gc, x, y, icon);

        gc.setFill(Color.WHITE);
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 34.0));
        gc.fillText("Revival Capsule", x + CARD_W / 2.0, y + 200.0);

        gc.setFill(Color.rgb(255, 255, 255, 0.72));
        gc.setFont(Font.font("Arial", FontWeight.NORMAL, 22.0));
        gc.fillText("+1 capsule per purchase", x + CARD_W / 2.0, y + 232.0);

        gc.setFill(Color.rgb(255, 215, 90));
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 26.0));
        gc.fillText(CAPSULE_PRICE + " coins", x + CARD_W / 2.0, y + 272.0);

        ButtonRect btn = capsuleBuyButton();
        boolean hovered = btn.contains(input.getMouseX(), input.getMouseY());
        Color fill;
        if (!canBuy) fill = Color.rgb(120, 120, 120, 0.45);
        else if (hovered) fill = Color.rgb(255, 215, 90, 0.85);
        else fill = Color.rgb(255, 195, 50, 0.78);

        gc.setFill(fill);
        gc.fillRoundRect(btn.x, btn.y, btn.w, btn.h, CARD_RADIUS, CARD_RADIUS);
        gc.setStroke(Color.WHITE);
        gc.setLineWidth(2.5);
        gc.strokeRoundRect(btn.x, btn.y, btn.w, btn.h, CARD_RADIUS, CARD_RADIUS);

        gc.setFill(canBuy ? Color.rgb(20, 20, 20) : Color.rgb(220, 220, 220));
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 30.0));
        gc.fillText("BUY", btn.x + btn.w / 2.0, btn.y + 48.0);
    }

    private void drawComingSoonCard(GraphicsContext gc, double x, double y, String title, Image icon) {
        drawPanel(gc, x, y, CARD_W, CARD_H, Color.rgb(28, 34, 44, 0.66), Color.rgb(255, 255, 255, 0.22));

        if (icon != null) {
            gc.setGlobalAlpha(0.35);
            drawCardIcon(gc, x, y, icon);
            gc.setGlobalAlpha(1.0);
        } else {
            gc.setFill(Color.rgb(255, 255, 255, 0.18));
            gc.setFont(Font.font("Arial", FontWeight.BOLD, 130.0));
            gc.setTextAlign(TextAlignment.CENTER);
            gc.fillText("?", x + CARD_W / 2.0, y + 150.0);
        }

        gc.setFill(Color.rgb(255, 255, 255, 0.85));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 34.0));
        gc.fillText(title, x + CARD_W / 2.0, y + 220.0);

        double tagW = 240.0;
        double tagH = 56.0;
        double tagX = x + (CARD_W - tagW) / 2.0;
        double tagY = y + 260.0;
        gc.setFill(Color.rgb(0, 0, 0, 0.5));
        gc.fillRoundRect(tagX, tagY, tagW, tagH, CARD_RADIUS, CARD_RADIUS);
        gc.setStroke(Color.rgb(255, 255, 255, 0.45));
        gc.setLineWidth(2.0);
        gc.strokeRoundRect(tagX, tagY, tagW, tagH, CARD_RADIUS, CARD_RADIUS);
        gc.setFill(Color.rgb(255, 230, 160));
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 26.0));
        gc.fillText("Coming Soon", tagX + tagW / 2.0, tagY + 38.0);
    }

    private void drawCardIcon(GraphicsContext gc, double x, double y, Image image) {
        if (image == null || image.getWidth() <= 0.0) return;
        double maxSize = 130.0;
        double iw = image.getWidth();
        double ih = image.getHeight();
        double scale = Math.min(maxSize / iw, maxSize / ih);
        double dw = iw * scale;
        double dh = ih * scale;
        gc.drawImage(image, x + CARD_W / 2.0 - dw / 2.0, y + 30.0, dw, dh);
    }

    private void drawPanel(GraphicsContext gc, double x, double y, double w, double h, Color fill, Color stroke) {
        gc.setFill(fill);
        gc.fillRoundRect(x, y, w, h, CARD_RADIUS, CARD_RADIUS);
        gc.setStroke(stroke);
        gc.setLineWidth(2.5);
        gc.strokeRoundRect(x, y, w, h, CARD_RADIUS, CARD_RADIUS);
    }

    private void drawBackButton(GraphicsContext gc) {
        ButtonRect btn = backButton();
        boolean hovered = btn.contains(input.getMouseX(), input.getMouseY());
        gc.setFill(hovered ? Color.rgb(255, 255, 255, 0.24) : Color.rgb(255, 255, 255, 0.13));
        gc.fillRoundRect(btn.x, btn.y, btn.w, btn.h, CARD_RADIUS, CARD_RADIUS);
        gc.setStroke(Color.WHITE);
        gc.setLineWidth(2.5);
        gc.strokeRoundRect(btn.x, btn.y, btn.w, btn.h, CARD_RADIUS, CARD_RADIUS);

        gc.setFill(Color.WHITE);
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 30.0));
        gc.fillText("Back", btn.x + btn.w / 2.0, btn.y + 48.0);
    }

    private void drawFlash(GraphicsContext gc) {
        if (flashTimer <= 0.0 || flashMessage.isEmpty()) return;
        double alpha = Math.min(1.0, flashTimer);
        gc.setGlobalAlpha(alpha);
        gc.setFill(Color.rgb(255, 235, 120));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 36.0));
        gc.fillText(flashMessage, Config.LOGICAL_WIDTH / 2.0, Config.LOGICAL_HEIGHT - 120.0);
        gc.setGlobalAlpha(1.0);
    }

    private ButtonRect capsuleBuyButton() {
        double x = ROW1_X1 + (CARD_W - BUY_BTN_W) / 2.0;
        double y = ROW1_Y + CARD_H - BUY_BTN_H - 14.0;
        return new ButtonRect(x, y, BUY_BTN_W, BUY_BTN_H);
    }

    private ButtonRect backButton() {
        return new ButtonRect(80.0, 960.0, 180.0, 72.0);
    }

    private record ButtonRect(double x, double y, double w, double h) {
        boolean contains(double px, double py) {
            return px >= x && px <= x + w && py >= y && py <= y + h;
        }
    }
}
