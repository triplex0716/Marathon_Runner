package com.ycom.state;

import com.ycom.account.Account;
import com.ycom.account.AccountStore;
import com.ycom.account.Session;
import com.ycom.core.Config;
import com.ycom.core.TimeManager;
import com.ycom.resource.AssetManager;
import com.ycom.system.InputSystem;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Paint;
import javafx.scene.paint.Stop;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;

import java.util.Random;

public class LoginState implements GameState {
    private enum Mode { LOGIN, REGISTER }
    private enum Focus { USERNAME, PASSWORD, CONFIRM }
    private enum FieldIcon { NONE, PERSON, LOCK }

    private static final double PANEL_W = 900.0;
    private static final double FIELD_H = 76.0;
    private static final double PRIMARY_BTN_H = 96.0;
    private static final double SECONDARY_BTN_W = 300.0;
    private static final double SECONDARY_BTN_H = 78.0;
    private static final double FIELD_ROW_GAP = 115.0;

    private static final Color YELLOW_ACCENT = Color.web("#FFC73B");
    private static final Color BORDER_IDLE = Color.rgb(200, 210, 235, 0.55);
    private static final Color LABEL_COLOR = Color.rgb(210, 222, 245, 0.92);

    private final GameStateManager gsm;
    private final Canvas canvas;
    private final InputSystem input;

    private final StringBuilder username = new StringBuilder();
    private final StringBuilder password = new StringBuilder();
    private final StringBuilder confirm = new StringBuilder();
    private Mode mode = Mode.LOGIN;
    private Focus focus = Focus.USERNAME;
    private String errorMessage = "";
    private boolean passwordVisible = false;

    private final javafx.event.EventHandler<KeyEvent> typedHandler = this::onKeyTyped;
    private Scene attachedScene;

    private double[] starX;
    private double[] starY;
    private double[] starSize;

    public LoginState(GameStateManager gsm, Canvas canvas, InputSystem input) {
        this.gsm = gsm;
        this.canvas = canvas;
        this.input = input;
        initStars();
    }

    private void initStars() {
        Random rng = new Random(1337L);
        int count = 110;
        starX = new double[count];
        starY = new double[count];
        starSize = new double[count];
        for (int i = 0; i < count; i++) {
            starX[i] = rng.nextDouble() * Config.LOGICAL_WIDTH;
            starY[i] = rng.nextDouble() * 640.0;
            starSize[i] = 1.0 + rng.nextDouble() * 2.4;
        }
    }

    @Override
    public void onEnter() {
        AccountStore.load();
        username.setLength(0);
        password.setLength(0);
        confirm.setLength(0);
        mode = Mode.LOGIN;
        errorMessage = "";
        passwordVisible = false;

        String last = AccountStore.loadLastUsername();
        if (last != null && !last.isEmpty() && last.length() <= AccountStore.MAX_USERNAME) {
            username.append(last);
            focus = Focus.PASSWORD;
        } else {
            focus = Focus.USERNAME;
        }

        attachedScene = canvas.getScene();
        if (attachedScene != null) {
            attachedScene.addEventFilter(KeyEvent.KEY_TYPED, typedHandler);
        }
    }

    @Override
    public void onExit() {
        if (attachedScene != null) {
            attachedScene.removeEventFilter(KeyEvent.KEY_TYPED, typedHandler);
            attachedScene = null;
        }
    }

    @Override
    public void update(double dt) {
        if (input.isKeyJustPressed(KeyCode.BACK_SPACE)) {
            StringBuilder target = focusedBuffer();
            if (target.length() > 0) target.deleteCharAt(target.length() - 1);
        }
        if (input.isKeyJustPressed(KeyCode.TAB)) {
            cycleFocus();
        }
        if (input.isKeyJustPressed(KeyCode.ENTER)) {
            submitCurrentMode();
            return;
        }
        if (!input.isMouseJustClicked()) {
            return;
        }
        double mx = input.getMouseX();
        double my = input.getMouseY();

        // Eye toggles checked BEFORE the field-focus hit-tests so the field doesn't swallow the click.
        if (passwordEyeRect().contains(mx, my)) {
            passwordVisible = !passwordVisible;
            return;
        }
        if (mode == Mode.REGISTER && confirmEyeRect().contains(mx, my)) {
            passwordVisible = !passwordVisible;
            return;
        }

        if (usernameField().contains(mx, my)) { focus = Focus.USERNAME; return; }
        if (passwordField().contains(mx, my)) { focus = Focus.PASSWORD; return; }
        if (mode == Mode.REGISTER && confirmField().contains(mx, my)) { focus = Focus.CONFIRM; return; }
        if (loginButton().contains(mx, my)) { onLoginButton(); return; }
        if (registerButton().contains(mx, my)) { onRegisterButton(); return; }
        if (guestButton().contains(mx, my)) {
            Session.enterAsGuest();
            gsm.setState("MENU");
        }
    }

    @Override
    public void render() {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        drawBackdrop(gc);
        drawSubwayTrain(gc);
        drawCornerDecorations(gc);
        drawTitle(gc);
        drawPanel(gc);
        drawFormContent(gc);
        drawBottomButtons(gc);
        drawTagline(gc);
        drawErrorMessage(gc);
        gc.setTextAlign(TextAlignment.LEFT);
    }

    // ---------- background ----------
    private void drawBackdrop(GraphicsContext gc) {
        LinearGradient sky = new LinearGradient(
                0, 0, 0, Config.LOGICAL_HEIGHT, false, CycleMethod.NO_CYCLE,
                new Stop(0.0, Color.web("#060c1c")),
                new Stop(0.55, Color.web("#0f1d3a")),
                new Stop(1.0, Color.web("#1a2c54")));
        gc.setFill(sky);
        gc.fillRect(0, 0, Config.LOGICAL_WIDTH, Config.LOGICAL_HEIGHT);

        gc.setFill(Color.rgb(255, 255, 255, 0.20));
        for (int i = 0; i < starX.length; i++) {
            gc.fillOval(starX[i], starY[i], starSize[i], starSize[i]);
        }

        double horizon = 760.0;
        gc.setFill(Color.web("#04081a"));
        gc.fillRect(0, horizon, Config.LOGICAL_WIDTH, Config.LOGICAL_HEIGHT - horizon);

        gc.setFill(Color.rgb(8, 14, 36, 0.97));
        double[] heights = {120, 60, 180, 90, 150, 70, 200, 110, 140, 80, 160, 100, 130, 70, 190, 90, 150, 100, 120, 80};
        double[] widths = {120, 90, 140, 110, 130, 80, 150, 100, 110, 90, 130, 100, 120, 80, 140, 100, 120, 100, 110, 90};
        double x = -40.0;
        for (int i = 0; i < heights.length; i++) {
            gc.fillRect(x, horizon - heights[i], widths[i], heights[i]);
            if (i % 3 == 0) {
                gc.setFill(Color.rgb(255, 220, 140, 0.55));
                gc.fillRect(x + widths[i] * 0.45, horizon - heights[i] * 0.65, 5, 5);
                gc.fillRect(x + widths[i] * 0.65, horizon - heights[i] * 0.40, 5, 5);
                gc.setFill(Color.rgb(8, 14, 36, 0.97));
            }
            x += widths[i] - 10;
            if (x > Config.LOGICAL_WIDTH) break;
        }

        double vanishX = Config.LOGICAL_WIDTH / 2.0;
        double vanishY = horizon - 8.0;
        gc.setStroke(Color.rgb(255, 255, 255, 0.10));
        gc.setLineWidth(2.0);
        for (double tx : new double[]{-360, -180, 180, 360}) {
            gc.strokeLine(vanishX + tx * 0.10, vanishY, vanishX + tx, Config.LOGICAL_HEIGHT);
        }
        gc.setStroke(Color.rgb(255, 220, 90, 0.18));
        gc.setLineWidth(4.0);
        gc.setLineDashes(36, 28);
        gc.strokeLine(vanishX, vanishY, vanishX, Config.LOGICAL_HEIGHT);
        gc.setLineDashes(0);

        gc.setFill(Color.rgb(0, 0, 0, 0.34));
        gc.fillRect(0, 0, Config.LOGICAL_WIDTH, Config.LOGICAL_HEIGHT);
    }

    private void drawSubwayTrain(GraphicsContext gc) {
        Image train = AssetManager.obstacleTrainIcon();
        if (train == null) return;
        gc.save();
        gc.setGlobalAlpha(0.22);
        gc.drawImage(train, 1430, 520, 480, 360);
        gc.restore();
    }

    private void drawCornerDecorations(GraphicsContext gc) {
        Image player = AssetManager.playerImage();
        Image magnet = AssetManager.magnetIcon();
        Image sprite = AssetManager.spriteIcon();
        Image coin = AssetManager.coinIcon();
        Image random = AssetManager.randomIcon();

        if (player != null) {
            gc.save();
            gc.setGlobalAlpha(0.95);
            gc.drawImage(player, 20, 660, 300, 400);
            gc.restore();
        }

        // magnet glow + icon, bottom-left
        gc.setFill(Color.rgb(80, 180, 255, 0.20));
        gc.fillOval(-40, 940, 280, 130);
        gc.setFill(Color.rgb(80, 180, 255, 0.30));
        gc.fillOval(-10, 960, 200, 90);
        if (magnet != null) {
            gc.save();
            gc.setGlobalAlpha(0.95);
            gc.drawImage(magnet, 0, 880, 190, 190);
            gc.restore();
        }

        // random box sitting next to the magnet, sharing the same ground line, with a purple glow.
        gc.setFill(Color.rgb(170, 90, 255, 0.20));
        gc.fillOval(180, 1010, 220, 90);
        gc.setFill(Color.rgb(170, 90, 255, 0.32));
        gc.fillOval(210, 1025, 160, 60);
        if (random != null) {
            gc.save();
            gc.setGlobalAlpha(0.95);
            gc.drawImage(random, 200, 905, 170, 170);
            gc.restore();
        }

        // energy drink + coin, bottom-right
        gc.setFill(Color.rgb(170, 70, 255, 0.18));
        gc.fillOval(1700, 950, 230, 110);
        if (sprite != null) {
            gc.save();
            gc.setGlobalAlpha(0.95);
            gc.drawImage(sprite, 1720, 770, 200, 260);
            gc.restore();
        }
        gc.setFill(Color.rgb(255, 200, 80, 0.18));
        gc.fillOval(1520, 950, 200, 100);
        if (coin != null) {
            gc.save();
            gc.setGlobalAlpha(0.95);
            gc.drawImage(coin, 1540, 850, 170, 170);
            gc.restore();
        }
    }

    // ---------- title ----------
    private void drawTitle(GraphicsContext gc) {
        Font font = Font.font("Arial Black", FontWeight.EXTRA_BOLD, 108);
        LinearGradient outrunGrad = new LinearGradient(
                0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0.0, Color.web("#FFE26A")),
                new Stop(0.55, Color.web("#FFC23B")),
                new Stop(1.0, Color.web("#F08A1A")));
        drawSlantedTitleLine(gc, Config.LOGICAL_WIDTH / 2.0, 130,
                new String[]{"YOU CAN'T"},
                new Paint[]{Color.WHITE},
                font);
        drawSlantedTitleLine(gc, Config.LOGICAL_WIDTH / 2.0, 240,
                new String[]{"OUTRUN", " ME!"},
                new Paint[]{outrunGrad, Color.WHITE},
                font);
    }

    private void drawSlantedTitleLine(GraphicsContext gc, double centerX, double baseY,
                                      String[] parts, Paint[] fills, Font font) {
        double[] widths = new double[parts.length];
        double total = 0;
        for (int i = 0; i < parts.length; i++) {
            widths[i] = textWidth(parts[i], font);
            total += widths[i];
        }

        gc.save();
        gc.translate(centerX, baseY);
        gc.rotate(-6.0);
        gc.setFont(font);
        gc.setTextAlign(TextAlignment.LEFT);

        double textStartX = -total / 2.0;

        // speed streaks behind the text (trailing on the left)
        gc.setLineCap(StrokeLineCap.ROUND);
        for (int s = 0; s < 5; s++) {
            double yOff = -78.0 + s * 22.0;
            double width = 10.0 - s * 1.0;
            double alpha = 0.65 - s * 0.10;
            gc.setStroke(Color.rgb(110, 175, 255, alpha));
            gc.setLineWidth(Math.max(width, 3.0));
            double x1 = textStartX - 300.0 + s * 22.0;
            double x2 = textStartX - 22.0 - s * 6.0;
            gc.strokeLine(x1, yOff, x2, yOff);
        }
        // shorter streaks on the right
        for (int s = 0; s < 4; s++) {
            double yOff = -60.0 + s * 26.0;
            double width = 9.0 - s * 1.0;
            double alpha = 0.45 - s * 0.08;
            gc.setStroke(Color.rgb(110, 175, 255, alpha));
            gc.setLineWidth(Math.max(width, 3.0));
            double x1 = textStartX + total + 26.0 + s * 8.0;
            double x2 = textStartX + total + 170.0 - s * 12.0;
            gc.strokeLine(x1, yOff, x2, yOff);
        }
        gc.setLineCap(StrokeLineCap.BUTT);

        // shadow
        gc.setFill(Color.rgb(2, 6, 22, 0.85));
        double sx = textStartX;
        for (int i = 0; i < parts.length; i++) {
            gc.fillText(parts[i], sx + 6, 8);
            sx += widths[i];
        }
        // dark blue outline
        gc.setStroke(Color.web("#0a1a3c"));
        gc.setLineWidth(7.0);
        sx = textStartX;
        for (int i = 0; i < parts.length; i++) {
            gc.strokeText(parts[i], sx, 0);
            sx += widths[i];
        }
        // colored fill (per-part paint)
        sx = textStartX;
        for (int i = 0; i < parts.length; i++) {
            gc.setFill(fills[i]);
            gc.fillText(parts[i], sx, 0);
            sx += widths[i];
        }
        gc.restore();
    }

    private double textWidth(String s, Font f) {
        Text helper = new Text(s);
        helper.setFont(f);
        return helper.getLayoutBounds().getWidth();
    }

    // ---------- panel ----------
    private double panelX() { return (Config.LOGICAL_WIDTH - PANEL_W) / 2.0; }
    private double panelY() { return mode == Mode.REGISTER ? 275.0 : 290.0; }
    private double panelH() { return mode == Mode.REGISTER ? 680.0 : 560.0; }

    private void drawPanel(GraphicsContext gc) {
        double x = panelX(), y = panelY(), w = PANEL_W, h = panelH();
        gc.setFill(Color.rgb(70, 140, 255, 0.07));
        gc.fillRoundRect(x - 18, y - 18, w + 36, h + 36, 38, 38);
        gc.setFill(Color.rgb(70, 140, 255, 0.13));
        gc.fillRoundRect(x - 8, y - 8, w + 16, h + 16, 32, 32);
        LinearGradient panelFill = new LinearGradient(
                0, y, 0, y + h, false, CycleMethod.NO_CYCLE,
                new Stop(0.0, Color.rgb(14, 22, 42, 0.88)),
                new Stop(1.0, Color.rgb(8, 14, 30, 0.94)));
        gc.setFill(panelFill);
        gc.fillRoundRect(x, y, w, h, 26, 26);
        gc.setStroke(Color.rgb(160, 200, 255, 0.55));
        gc.setLineWidth(1.6);
        gc.strokeRoundRect(x, y, w, h, 26, 26);
        gc.setStroke(Color.rgb(255, 255, 255, 0.16));
        gc.setLineWidth(1.0);
        gc.strokeRoundRect(x + 6, y + 6, w - 12, h - 12, 22, 22);
    }

    private void drawPanelHeaderRule(GraphicsContext gc, double centerX, double y, double halfWidth) {
        Color line = Color.rgb(160, 200, 255, 0.55);
        gc.setStroke(line);
        gc.setLineWidth(2.0);
        gc.strokeLine(centerX - halfWidth - 50, y, centerX - halfWidth - 14, y);
        gc.strokeLine(centerX + halfWidth + 14, y, centerX + halfWidth + 50, y);
        gc.setFill(line);
        gc.fillOval(centerX - halfWidth - 12, y - 4, 8, 8);
        gc.fillOval(centerX + halfWidth + 4, y - 4, 8, 8);
    }

    // ---------- form ----------
    private void drawFormContent(GraphicsContext gc) {
        double x = panelX();
        double y = panelY();

        String header = mode == Mode.REGISTER ? "REGISTER MODE" : "LOGIN MODE";
        Font headerFont = Font.font("Arial", FontWeight.BOLD, 38);
        double hw = textWidth(header, headerFont) / 2.0;

        drawPanelHeaderRule(gc, x + PANEL_W / 2.0, y + 50, hw + 12);

        gc.setFont(headerFont);
        gc.setFill(YELLOW_ACCENT);
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText(header, x + PANEL_W / 2.0, y + 62);

        gc.setFont(Font.font("Arial", FontWeight.NORMAL, 22));
        gc.setFill(Color.rgb(200, 215, 240, 0.85));
        gc.fillText(mode == Mode.REGISTER
                        ? "Use A-Z, a-z, and 0-9 only  |  username >= 3, password >= 6"
                        : "Use A-Z, a-z, and 0-9 only",
                x + PANEL_W / 2.0, y + 100);

        gc.setTextAlign(TextAlignment.LEFT);

        drawLabeledField(gc, "Username", usernameField(), username.toString(),
                focus == Focus.USERNAME, false, FieldIcon.PERSON, false);
        drawLabeledField(gc, "Password", passwordField(), password.toString(),
                focus == Focus.PASSWORD, !passwordVisible, FieldIcon.LOCK, true);
        if (mode == Mode.REGISTER) {
            drawLabeledField(gc, "Confirm Password", confirmField(), confirm.toString(),
                    focus == Focus.CONFIRM, !passwordVisible, FieldIcon.LOCK, true);
        }

        drawPrimaryButton(gc, loginButton(), mode == Mode.REGISTER ? "REGISTER" : "LOGIN");
    }

    private void drawLabeledField(GraphicsContext gc, String label, ButtonRect r,
                                  String text, boolean focused, boolean mask,
                                  FieldIcon icon, boolean showEyeToggle) {
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 22));
        gc.setFill(LABEL_COLOR);
        gc.setTextAlign(TextAlignment.LEFT);
        gc.fillText(label, r.x + 4, r.y - 12);

        gc.setFill(focused ? Color.rgb(255, 255, 255, 0.10) : Color.rgb(0, 0, 0, 0.35));
        gc.fillRoundRect(r.x, r.y, r.w, r.h, 18, 18);
        gc.setStroke(focused ? YELLOW_ACCENT : BORDER_IDLE);
        gc.setLineWidth(focused ? 3.0 : 1.8);
        gc.strokeRoundRect(r.x, r.y, r.w, r.h, 18, 18);

        double textPadLeft = 26.0;
        if (icon == FieldIcon.PERSON) {
            drawPersonIcon(gc, r.x + 18, r.y + r.h / 2.0 - 19, 38);
            textPadLeft = 72.0;
        } else if (icon == FieldIcon.LOCK) {
            drawLockIcon(gc, r.x + 20, r.y + r.h / 2.0 - 18, 32);
            textPadLeft = 70.0;
        }

        String display = mask ? "*".repeat(text.length()) : text;
        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Consolas", FontWeight.NORMAL, 34));
        gc.setTextAlign(TextAlignment.LEFT);
        double textY = r.y + r.h / 2.0 + 12.0;
        gc.fillText(display, r.x + textPadLeft, textY);

        if (focused) {
            boolean caretOn = ((long) (TimeManager.getElapsedTime() * 2)) % 2 == 0;
            if (caretOn) {
                double caretX = r.x + textPadLeft + gc.getFont().getSize() * 0.52 * display.length();
                gc.setStroke(YELLOW_ACCENT);
                gc.setLineWidth(2.5);
                gc.strokeLine(caretX, r.y + 14.0, caretX, r.y + r.h - 14.0);
            }
        }

        if (showEyeToggle) {
            ButtonRect eye = eyeRectFor(r);
            boolean hovered = eye.contains(input.getMouseX(), input.getMouseY());
            drawEyeIcon(gc, eye.x + 6, eye.y + 14, 32, !mask, hovered);
        }
    }

    private void drawPersonIcon(GraphicsContext gc, double x, double y, double size) {
        Color base = Color.web("#A8C8FF");
        gc.setFill(base);
        gc.fillOval(x + size * 0.30, y, size * 0.40, size * 0.40);
        gc.fillRoundRect(x + size * 0.12, y + size * 0.50, size * 0.76, size * 0.45, 18, 18);
        gc.setFill(Color.web("#5790E0"));
        gc.fillRoundRect(x + size * 0.20, y + size * 0.62, size * 0.60, size * 0.10, 8, 8);
    }

    private void drawLockIcon(GraphicsContext gc, double x, double y, double size) {
        double bodyY = y + size * 0.42;
        double bodyH = size * 0.58;
        gc.setFill(Color.rgb(255, 220, 120, 0.92));
        gc.fillRoundRect(x, bodyY, size, bodyH, 6, 6);
        gc.setStroke(Color.rgb(255, 220, 120, 0.92));
        gc.setLineWidth(3.0);
        gc.strokeArc(x + size * 0.18, y, size * 0.64, size * 0.66, 0, 180, ArcType.OPEN);
        gc.setFill(Color.rgb(40, 30, 10));
        gc.fillOval(x + size * 0.40, bodyY + bodyH * 0.30, size * 0.20, size * 0.20);
    }

    private void drawEyeIcon(GraphicsContext gc, double x, double y, double size,
                             boolean open, boolean hovered) {
        Color c = hovered ? Color.WHITE : Color.web("#A8C6F0");
        gc.setStroke(c);
        gc.setLineWidth(2.5);
        gc.strokeArc(x, y, size, size * 0.70, 20, 140, ArcType.OPEN);
        gc.strokeArc(x, y - size * 0.20, size, size * 0.70, -20, -140, ArcType.OPEN);
        if (open) {
            gc.setFill(c);
            gc.fillOval(x + size * 0.36, y + size * 0.18, size * 0.28, size * 0.28);
        } else {
            gc.setStroke(c);
            gc.setLineWidth(3.0);
            gc.strokeLine(x + size * 0.10, y + size * 0.05, x + size * 0.90, y + size * 0.75);
        }
    }

    // ---------- buttons ----------
    private void drawPrimaryButton(GraphicsContext gc, ButtonRect r, String label) {
        boolean hovered = r.contains(input.getMouseX(), input.getMouseY());
        double sx = r.x, sy = r.y, sw = r.w, sh = r.h;

        if (hovered) {
            gc.setFill(Color.rgb(80, 160, 255, 0.32));
            gc.fillRoundRect(sx - 12, sy - 10, sw + 24, sh + 20, 24, 24);
        }
        LinearGradient fill = new LinearGradient(
                0, sy, 0, sy + sh, false, CycleMethod.NO_CYCLE,
                new Stop(0.0, hovered ? Color.web("#52A8FF") : Color.web("#2E7EE6")),
                new Stop(1.0, hovered ? Color.web("#1F66C7") : Color.web("#1450A8")));
        gc.setFill(fill);
        gc.fillRoundRect(sx, sy, sw, sh, 20, 20);
        gc.setStroke(Color.rgb(255, 255, 255, 0.55));
        gc.setLineWidth(2.0);
        gc.strokeRoundRect(sx, sy, sw, sh, 20, 20);
        gc.setStroke(Color.rgb(255, 255, 255, 0.28));
        gc.setLineWidth(1.0);
        gc.strokeRoundRect(sx + 5, sy + 5, sw - 10, sh - 10, 16, 16);

        // running figure on the left
        double runSize = sh - 18;
        double runX = sx + 80;
        double runY = sy + 9;
        drawRunIcon(gc, runX, runY, runSize);

        // speed dashes on the right
        drawSpeedDashes(gc, sx + sw - 105, sy + sh / 2.0, 4);

        // label
        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 40));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText(label, sx + sw / 2.0 + 35, sy + sh / 2.0 + 15.0);
    }

    private void drawRunIcon(GraphicsContext gc, double x, double y, double size) {
        Image sheet = AssetManager.runSheet();
        if (sheet != null && sheet.getWidth() > 0) {
            int frames = AssetManager.frameCount("run");
            if (frames < 1) frames = 8;
            double sw = sheet.getWidth() / frames;
            double sh = sheet.getHeight();
            int idx = Math.min(2, frames - 1);
            gc.drawImage(sheet, sw * idx, 0, sw, sh, x, y, size, size);
            return;
        }
        gc.setFill(Color.WHITE);
        gc.fillOval(x + size * 0.40, y + size * 0.05, size * 0.22, size * 0.22);
        gc.setStroke(Color.WHITE);
        gc.setLineWidth(4.0);
        gc.strokeLine(x + size * 0.51, y + size * 0.28, x + size * 0.51, y + size * 0.62);
        gc.strokeLine(x + size * 0.51, y + size * 0.62, x + size * 0.30, y + size * 0.92);
        gc.strokeLine(x + size * 0.51, y + size * 0.62, x + size * 0.72, y + size * 0.88);
        gc.strokeLine(x + size * 0.51, y + size * 0.40, x + size * 0.74, y + size * 0.55);
        gc.strokeLine(x + size * 0.51, y + size * 0.40, x + size * 0.30, y + size * 0.50);
    }

    private void drawSpeedDashes(GraphicsContext gc, double x, double y, int count) {
        gc.setStroke(Color.rgb(255, 255, 255, 0.55));
        gc.setLineWidth(5.0);
        gc.setLineCap(StrokeLineCap.ROUND);
        for (int i = 0; i < count; i++) {
            double dy = -28 + i * 16;
            double len = 28 + (i % 2 == 0 ? 8 : 0);
            gc.strokeLine(x, y + dy, x + len, y + dy);
        }
        gc.setLineCap(StrokeLineCap.BUTT);
    }

    private void drawSecondaryButton(GraphicsContext gc, ButtonRect r, String label,
                                     boolean isGuest) {
        boolean hovered = r.contains(input.getMouseX(), input.getMouseY());
        double sx = r.x, sy = r.y, sw = r.w, sh = r.h;

        gc.setFill(hovered ? Color.rgb(60, 130, 230, 0.22) : Color.rgb(18, 30, 56, 0.55));
        gc.fillRoundRect(sx, sy, sw, sh, 18, 18);
        gc.setStroke(hovered ? Color.web("#7DB8FF") : Color.rgb(190, 210, 245, 0.85));
        gc.setLineWidth(hovered ? 2.6 : 2.0);
        gc.strokeRoundRect(sx, sy, sw, sh, 18, 18);

        double iconSize = 38;
        double iconX = sx + 32;
        double iconY = sy + (sh - iconSize) / 2.0;
        if (isGuest) drawGuestIcon(gc, iconX, iconY, iconSize, hovered);
        else drawRegisterIcon(gc, iconX, iconY, iconSize, hovered);

        gc.setFill(hovered ? Color.WHITE : Color.rgb(220, 232, 250));
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 30));
        gc.setTextAlign(TextAlignment.LEFT);
        gc.fillText(label, iconX + iconSize + 22, sy + sh / 2.0 + 11.0);
    }

    private void drawRegisterIcon(GraphicsContext gc, double x, double y, double size, boolean hovered) {
        Color blue = hovered ? Color.web("#BFDDFF") : Color.web("#7DB1F0");
        gc.setFill(blue);
        gc.fillOval(x + size * 0.18, y, size * 0.36, size * 0.36);
        gc.fillRoundRect(x + size * 0.02, y + size * 0.46, size * 0.66, size * 0.40, 14, 14);
        double cx = x + size * 0.82;
        double cy = y + size * 0.74;
        double p = size * 0.30;
        gc.setStroke(blue);
        gc.setLineWidth(4.0);
        gc.setLineCap(StrokeLineCap.ROUND);
        gc.strokeLine(cx - p / 2, cy, cx + p / 2, cy);
        gc.strokeLine(cx, cy - p / 2, cx, cy + p / 2);
        gc.setLineCap(StrokeLineCap.BUTT);
    }

    private void drawGuestIcon(GraphicsContext gc, double x, double y, double size, boolean hovered) {
        Color yellow = hovered ? Color.web("#FFE26A") : YELLOW_ACCENT;
        gc.setFill(yellow);
        gc.fillArc(x, y, size, size, 0, 180, ArcType.ROUND);
        gc.fillRoundRect(x + size * 0.15, y + size * 0.50, size * 0.70, size * 0.42, 12, 12);
        gc.setFill(Color.web("#1A2542"));
        gc.fillRoundRect(x + size * 0.20, y + size * 0.42, size * 0.60, size * 0.18, 6, 6);
        gc.setFill(yellow);
        gc.fillOval(x + size * 0.28, y + size * 0.45, size * 0.12, size * 0.12);
        gc.fillOval(x + size * 0.60, y + size * 0.45, size * 0.12, size * 0.12);
    }

    private void drawBottomButtons(GraphicsContext gc) {
        drawSecondaryButton(gc, registerButton(), mode == Mode.REGISTER ? "LOGIN" : "REGISTER", false);
        drawSecondaryButton(gc, guestButton(), "GUEST", true);
    }

    private void drawTagline(GraphicsContext gc) {
        if (mode == Mode.REGISTER) return;
        double y = panelY() + panelH() + 25 + SECONDARY_BTN_H + 60;
        if (y > Config.LOGICAL_HEIGHT - 22) y = Config.LOGICAL_HEIGHT - 22;
        double cx = Config.LOGICAL_WIDTH / 2.0;
        String text = "Run fast.  Collect more.  Never stop.";
        Font font = Font.font("Arial", FontPosture.ITALIC, 26);
        gc.setFont(font);
        gc.setFill(Color.rgb(215, 225, 245, 0.85));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText(text, cx, y);
        double tw = textWidth(text, font);
        drawLightningIcon(gc, cx - tw / 2.0 - 50, y - 26, 30);
        drawLightningIcon(gc, cx + tw / 2.0 + 20, y - 26, 30);
    }

    private void drawLightningIcon(GraphicsContext gc, double x, double y, double size) {
        double[] xs = {x + size * 0.55, x + size * 0.20, x + size * 0.45,
                       x + size * 0.30, x + size * 0.80, x + size * 0.55};
        double[] ys = {y, y + size * 0.55, y + size * 0.55,
                       y + size, y + size * 0.45, y + size * 0.45};
        gc.setFill(YELLOW_ACCENT);
        gc.fillPolygon(xs, ys, 6);
    }

    private void drawErrorMessage(GraphicsContext gc) {
        if (errorMessage.isEmpty()) return;
        gc.setFill(Color.rgb(255, 110, 110));
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        gc.setTextAlign(TextAlignment.CENTER);
        double y = loginButton().y - 14.0;
        gc.fillText(errorMessage, Config.LOGICAL_WIDTH / 2.0, y);
    }

    // ---------- key/typed handling (unchanged behavior) ----------
    private void onKeyTyped(KeyEvent e) {
        String ch = e.getCharacter();
        if (ch == null || ch.isEmpty()) return;
        char c = ch.charAt(0);

        if (c < 0x20 || c == 0x7F) return;
        if (!Character.isLetterOrDigit(c)) return;
        if (c > 0x7A) return;

        StringBuilder target = focusedBuffer();
        int max = (focus == Focus.USERNAME) ? AccountStore.MAX_USERNAME : AccountStore.MAX_PASSWORD;
        if (target.length() >= max) return;
        target.append(c);
    }

    private StringBuilder focusedBuffer() {
        return switch (focus) {
            case USERNAME -> username;
            case PASSWORD -> password;
            case CONFIRM -> confirm;
        };
    }

    private void cycleFocus() {
        if (mode == Mode.LOGIN) {
            focus = (focus == Focus.USERNAME) ? Focus.PASSWORD : Focus.USERNAME;
        } else {
            focus = switch (focus) {
                case USERNAME -> Focus.PASSWORD;
                case PASSWORD -> Focus.CONFIRM;
                case CONFIRM -> Focus.USERNAME;
            };
        }
    }

    private void submitCurrentMode() {
        if (mode == Mode.LOGIN) tryLogin();
        else tryRegister();
    }

    private void onLoginButton() {
        submitCurrentMode();
    }

    private void onRegisterButton() {
        switchMode(mode == Mode.REGISTER ? Mode.LOGIN : Mode.REGISTER);
    }

    private void switchMode(Mode m) {
        mode = m;
        errorMessage = "";
        username.setLength(0);
        password.setLength(0);
        confirm.setLength(0);
        focus = Focus.USERNAME;
    }

    private void tryLogin() {
        if (username.length() == 0 || password.length() == 0) {
            errorMessage = "Username and password required.";
            return;
        }
        Account acc = AccountStore.authenticate(username.toString(), password.toString());
        if (acc == null) {
            errorMessage = "Wrong username or password.";
            password.setLength(0);
            return;
        }
        Session.login(acc);
        gsm.setState("MENU");
    }

    private void tryRegister() {
        if (username.length() == 0 || password.length() == 0 || confirm.length() == 0) {
            errorMessage = "All three fields are required.";
            return;
        }
        if (!password.toString().equals(confirm.toString())) {
            errorMessage = "Passwords do not match.";
            confirm.setLength(0);
            focus = Focus.CONFIRM;
            return;
        }
        try {
            Account acc = AccountStore.register(username.toString(), password.toString());
            Session.login(acc);
            gsm.setState("MENU");
        } catch (IllegalStateException dup) {
            errorMessage = "Username already taken.";
        } catch (IllegalArgumentException bad) {
            switch (bad.getMessage()) {
                case "SHORT" -> errorMessage = "Password must be at least 6 characters.";
                case "USER_SHORT" -> errorMessage = "Username must be at least 3 characters.";
                case "TOO_LONG" -> errorMessage = "Username/password too long.";
                case "BAD_CHAR" -> errorMessage = "Username has invalid characters.";
                default -> errorMessage = "Invalid username or password.";
            }
        }
    }

    // ---------- layout rects ----------
    private ButtonRect usernameField() { return fieldRect(0); }
    private ButtonRect passwordField() { return fieldRect(1); }
    private ButtonRect confirmField() { return fieldRect(2); }

    private ButtonRect fieldRect(int row) {
        double fx = panelX() + 40.0;
        double fw = PANEL_W - 80.0;
        double fy = panelY() + 155.0 + row * FIELD_ROW_GAP;
        return new ButtonRect(fx, fy, fw, FIELD_H);
    }

    private ButtonRect eyeRectFor(ButtonRect field) {
        double w = 44, h = 52;
        return new ButtonRect(field.x + field.w - w - 14, field.y + (field.h - h) / 2.0, w, h);
    }

    private ButtonRect passwordEyeRect() { return eyeRectFor(passwordField()); }
    private ButtonRect confirmEyeRect() { return eyeRectFor(confirmField()); }

    private ButtonRect loginButton() {
        double bx = panelX() + 90.0;
        double bw = PANEL_W - 180.0;
        double by = panelY() + panelH() - 120.0;
        return new ButtonRect(bx, by, bw, PRIMARY_BTN_H);
    }

    private ButtonRect registerButton() { return secondaryRow(0); }
    private ButtonRect guestButton() { return secondaryRow(1); }

    private ButtonRect secondaryRow(int idx) {
        double gap = 40.0;
        double totalW = SECONDARY_BTN_W * 2 + gap;
        double startX = (Config.LOGICAL_WIDTH - totalW) / 2.0;
        double y = panelY() + panelH() + 25.0;
        return new ButtonRect(startX + (SECONDARY_BTN_W + gap) * idx, y,
                SECONDARY_BTN_W, SECONDARY_BTN_H);
    }

    private record ButtonRect(double x, double y, double w, double h) {
        boolean contains(double px, double py) {
            return px >= x && px <= x + w && py >= y && py <= y + h;
        }
    }
}
