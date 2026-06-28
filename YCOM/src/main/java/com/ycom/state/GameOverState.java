package com.ycom.state;

import com.ycom.account.Account;
import com.ycom.account.AccountStore;
import com.ycom.account.Session;
import com.ycom.core.Config;
import com.ycom.resource.AssetManager;
import com.ycom.system.InputSystem;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;

import java.util.Random;

public class GameOverState implements GameState {
    private final GameStateManager gsm;
    private final Canvas canvas;
    private final InputSystem input;
    public static int finalScore = 0;
    private boolean newHighScore = false;

    private static final double PANEL_W = 1080.0;
    private static final double PANEL_H = 800.0;

    private static final Color GOLD = Color.web("#FFC73B");
    private static final Color GREEN_ACCENT = Color.web("#7DE36A");
    private static final Color SCORE_BORDER = Color.web("#FF8A2E");

    private double[] starX;
    private double[] starY;
    private double[] starSize;

    public GameOverState(GameStateManager gsm, Canvas canvas, InputSystem input) {
        this.gsm = gsm;
        this.canvas = canvas;
        this.input = input;
        initStars();
    }

    private void initStars() {
        Random rng = new Random(4242L);
        int count = 90;
        starX = new double[count];
        starY = new double[count];
        starSize = new double[count];
        for (int i = 0; i < count; i++) {
            starX[i] = rng.nextDouble() * Config.LOGICAL_WIDTH;
            starY[i] = rng.nextDouble() * 700.0;
            starSize[i] = 1.0 + rng.nextDouble() * 2.0;
        }
    }

    @Override
    public void onEnter() {
        newHighScore = false;
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
    public void onExit() {
        Scene s = canvas.getScene();
        if (s != null) s.setCursor(Cursor.DEFAULT);
    }

    @Override
    public void update(double dt) {
        Scene scene = canvas.getScene();
        if (scene != null) {
            boolean overBtn = menuButton().contains(input.getMouseX(), input.getMouseY());
            scene.setCursor(overBtn ? Cursor.HAND : Cursor.DEFAULT);
        }
        boolean keyTrigger = input.isKeyJustPressed(KeyCode.SPACE)
                || input.isKeyJustPressed(KeyCode.ENTER)
                || input.isKeyJustPressed(KeyCode.Q);
        boolean clickTrigger = input.isMouseJustClicked()
                && menuButton().contains(input.getMouseX(), input.getMouseY());
        if (keyTrigger || clickTrigger) {
            handleBackToMenu();
        }
    }

    private void handleBackToMenu() {
        Scene s = canvas.getScene();
        if (s != null) s.setCursor(Cursor.DEFAULT);
        gsm.setState("MENU");
    }

    @Override
    public void render() {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        drawBackdrop(gc);
        drawResultPanel(gc);
        drawTitle(gc);
        if (newHighScore) drawNewHighScore(gc);
        drawScoreCard(gc);
        drawStatCards(gc);
        drawBackToMenuButton(gc);
        drawBottomHint(gc);
        gc.setTextAlign(TextAlignment.LEFT);
    }

    // ---------- background ----------
    private void drawBackdrop(GraphicsContext gc) {
        LinearGradient sky = new LinearGradient(
                0, 0, 0, Config.LOGICAL_HEIGHT, false, CycleMethod.NO_CYCLE,
                new Stop(0.0, Color.web("#060c1c")),
                new Stop(0.55, Color.web("#0f1a30")),
                new Stop(1.0, Color.web("#1a1830")));
        gc.setFill(sky);
        gc.fillRect(0, 0, Config.LOGICAL_WIDTH, Config.LOGICAL_HEIGHT);

        gc.setFill(Color.rgb(255, 80, 40, 0.10));
        gc.fillOval(-200, Config.LOGICAL_HEIGHT - 200, 600, 600);
        gc.fillOval(Config.LOGICAL_WIDTH - 400, Config.LOGICAL_HEIGHT - 300, 600, 600);

        gc.setFill(Color.rgb(255, 255, 255, 0.18));
        for (int i = 0; i < starX.length; i++) {
            gc.fillOval(starX[i], starY[i], starSize[i], starSize[i]);
        }

        double horizon = 760.0;
        gc.setFill(Color.web("#02050f"));
        gc.fillRect(0, horizon, Config.LOGICAL_WIDTH, Config.LOGICAL_HEIGHT - horizon);
        gc.setFill(Color.rgb(8, 12, 28, 0.97));
        double[] heights = {120, 60, 180, 90, 150, 70, 200, 110, 140, 80, 160, 100, 130, 70, 190, 90, 150, 100, 120, 80};
        double[] widths = {120, 90, 140, 110, 130, 80, 150, 100, 110, 90, 130, 100, 120, 80, 140, 100, 120, 100, 110, 90};
        double x = -40;
        for (int i = 0; i < heights.length; i++) {
            gc.fillRect(x, horizon - heights[i], widths[i], heights[i]);
            x += widths[i] - 10;
            if (x > Config.LOGICAL_WIDTH) break;
        }

        Image ascension = AssetManager.ascensionImage();
        if (ascension != null) {
            gc.save();
            gc.setGlobalAlpha(0.22);
            double targetH = 540.0;
            double aspect = ascension.getWidth() / Math.max(1.0, ascension.getHeight());
            double targetW = targetH * aspect;
            gc.drawImage(ascension, -targetW * 0.10, 340, targetW, targetH);
            gc.restore();
        }

        gc.setFill(Color.rgb(255, 60, 40, 0.30));
        gc.fillOval(Config.LOGICAL_WIDTH - 130, 660, 80, 80);
        gc.setFill(Color.rgb(255, 80, 50, 0.95));
        gc.fillOval(Config.LOGICAL_WIDTH - 100, 690, 20, 20);

        double vanishX = Config.LOGICAL_WIDTH / 2.0;
        double vanishY = horizon - 8.0;
        gc.setStroke(Color.rgb(255, 255, 255, 0.10));
        gc.setLineWidth(2.0);
        for (double tx : new double[]{-360, -180, 180, 360}) {
            gc.strokeLine(vanishX + tx * 0.10, vanishY, vanishX + tx, Config.LOGICAL_HEIGHT);
        }
        gc.setStroke(Color.rgb(255, 220, 90, 0.12));
        gc.setLineWidth(4.0);
        gc.setLineDashes(36, 28);
        gc.strokeLine(vanishX, vanishY, vanishX, Config.LOGICAL_HEIGHT);
        gc.setLineDashes(0);

        gc.setFill(Color.rgb(0, 0, 0, 0.46));
        gc.fillRect(0, 0, Config.LOGICAL_WIDTH, Config.LOGICAL_HEIGHT);
    }

    // ---------- panel ----------
    private double panelX() { return (Config.LOGICAL_WIDTH - PANEL_W) / 2.0; }
    private double panelY() { return (Config.LOGICAL_HEIGHT - PANEL_H) / 2.0; }

    private void drawResultPanel(GraphicsContext gc) {
        double x = panelX(), y = panelY();
        gc.setFill(Color.rgb(255, 100, 60, 0.10));
        gc.fillRoundRect(x - 18, y - 18, PANEL_W + 36, PANEL_H + 36, 36, 36);
        gc.setFill(Color.rgb(80, 120, 220, 0.10));
        gc.fillRoundRect(x - 8, y - 8, PANEL_W + 16, PANEL_H + 16, 32, 32);
        LinearGradient panelFill = new LinearGradient(
                0, y, 0, y + PANEL_H, false, CycleMethod.NO_CYCLE,
                new Stop(0.0, Color.rgb(14, 22, 42, 0.92)),
                new Stop(1.0, Color.rgb(8, 12, 28, 0.96)));
        gc.setFill(panelFill);
        gc.fillRoundRect(x, y, PANEL_W, PANEL_H, 26, 26);
        LinearGradient borderGrad = new LinearGradient(
                x, y, x + PANEL_W, y + PANEL_H, false, CycleMethod.NO_CYCLE,
                new Stop(0.0, Color.web("#FF6E3A")),
                new Stop(1.0, Color.rgb(100, 150, 220)));
        gc.setStroke(borderGrad);
        gc.setLineWidth(2.0);
        gc.strokeRoundRect(x, y, PANEL_W, PANEL_H, 26, 26);
        gc.setStroke(Color.rgb(255, 255, 255, 0.18));
        gc.setLineWidth(1.0);
        gc.strokeRoundRect(x + 6, y + 6, PANEL_W - 12, PANEL_H - 12, 22, 22);
    }

    // ---------- title ----------
    private void drawTitle(GraphicsContext gc) {
        double cx = Config.LOGICAL_WIDTH / 2.0;
        double baseY = panelY() + 150;
        Font font = Font.font("Arial Black", FontWeight.BOLD, FontPosture.ITALIC, 100);

        double titleW = textWidth("GAME OVER", font);
        double sideLeft = cx - titleW / 2.0 - 30;
        double sideRight = cx + titleW / 2.0 + 30;
        gc.setLineCap(StrokeLineCap.ROUND);
        Color red = Color.web("#E63838");
        for (int i = 0; i < 3; i++) {
            double yOff = baseY - 50 + i * 22;
            double len = 120 - i * 28;
            double alpha = 1.0 - i * 0.22;
            gc.setStroke(new Color(red.getRed(), red.getGreen(), red.getBlue(), alpha));
            gc.setLineWidth(Math.max(4.0, 10 - i * 1.6));
            gc.strokeLine(sideLeft - len, yOff, sideLeft, yOff);
            gc.strokeLine(sideRight, yOff, sideRight + len, yOff);
        }
        gc.setLineCap(StrokeLineCap.BUTT);

        gc.setFont(font);
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setFill(Color.rgb(0, 0, 0, 0.78));
        gc.fillText("GAME OVER", cx + 5, baseY + 6);
        gc.setStroke(Color.rgb(40, 10, 0, 0.85));
        gc.setLineWidth(6.0);
        gc.strokeText("GAME OVER", cx, baseY);
        LinearGradient metalGrad = new LinearGradient(
                0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0.0, Color.web("#FFFFFF")),
                new Stop(0.45, Color.web("#E0E0E5")),
                new Stop(0.55, Color.web("#FFFFFF")),
                new Stop(1.0, Color.web("#B0B0B8")));
        gc.setFill(metalGrad);
        gc.fillText("GAME OVER", cx, baseY);
    }

    // ---------- new-high-score banner ----------
    private void drawNewHighScore(GraphicsContext gc) {
        double cx = Config.LOGICAL_WIDTH / 2.0;
        double baseY = panelY() + 240;
        Font font = Font.font("Arial", FontWeight.BOLD, FontPosture.ITALIC, 38);

        gc.setFill(Color.rgb(255, 220, 90, 0.22));
        gc.fillOval(cx - 280, baseY - 40, 560, 65);

        gc.setFont(font);
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setStroke(Color.rgb(80, 50, 0, 0.85));
        gc.setLineWidth(4.0);
        gc.strokeText("NEW HIGH SCORE!", cx, baseY);
        LinearGradient goldGrad = new LinearGradient(
                0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0.0, Color.web("#FFE26A")),
                new Stop(1.0, Color.web("#F08A1A")));
        gc.setFill(goldGrad);
        gc.fillText("NEW HIGH SCORE!", cx, baseY);

        double textW = textWidth("NEW HIGH SCORE!", font);
        drawLaurelBranch(gc, cx - textW / 2.0 - 50, baseY - 6, 44, true);
        drawLaurelBranch(gc, cx + textW / 2.0 + 6, baseY - 6, 44, false);
    }

    private void drawLaurelBranch(GraphicsContext gc, double x, double y, double size, boolean leftSide) {
        gc.setFill(GOLD);
        double dir = leftSide ? -1.0 : 1.0;
        for (int i = 0; i < 4; i++) {
            double t = i / 3.0;
            double cx = x + dir * t * size * 0.85;
            gc.save();
            gc.translate(cx, y - size * 0.18);
            gc.rotate(dir * (35 - i * 8));
            gc.fillOval(-size * 0.18, -size * 0.07, size * 0.36, size * 0.14);
            gc.restore();
            gc.save();
            gc.translate(cx, y + size * 0.12);
            gc.rotate(dir * (-30 + i * 8));
            gc.fillOval(-size * 0.18, -size * 0.07, size * 0.36, size * 0.14);
            gc.restore();
        }
    }

    // ---------- score card ----------
    private void drawScoreCard(GraphicsContext gc) {
        double w = 860, h = 100;
        double x = (Config.LOGICAL_WIDTH - w) / 2.0;
        double y = panelY() + 310;

        gc.setFill(Color.rgb(255, 138, 46, 0.16));
        gc.fillRoundRect(x - 10, y - 10, w + 20, h + 20, 24, 24);

        LinearGradient bg = new LinearGradient(
                0, y, 0, y + h, false, CycleMethod.NO_CYCLE,
                new Stop(0.0, Color.rgb(20, 30, 56, 0.92)),
                new Stop(1.0, Color.rgb(14, 22, 40, 0.96)));
        gc.setFill(bg);
        gc.fillRoundRect(x, y, w, h, 18, 18);
        gc.setStroke(SCORE_BORDER);
        gc.setLineWidth(2.0);
        gc.strokeRoundRect(x, y, w, h, 18, 18);

        double iconSize = 64;
        double iconX = x + 28;
        double iconY = y + (h - iconSize) / 2.0;
        drawTrophyIcon(gc, iconX, iconY, iconSize);

        gc.setFill(Color.WHITE);
        Font labelFont = Font.font("Arial", FontWeight.BOLD, 36);
        gc.setFont(labelFont);
        gc.setTextAlign(TextAlignment.LEFT);
        gc.fillText("Score:", iconX + iconSize + 30, y + h / 2.0 + 14);

        double labelW = textWidth("Score:", labelFont);
        gc.setFill(GOLD);
        gc.setFont(Font.font("Arial Black", FontWeight.BOLD, 64));
        gc.fillText(String.valueOf(finalScore),
                iconX + iconSize + 30 + labelW + 28, y + h / 2.0 + 22);
    }

    // ---------- stat cards ----------
    private void drawStatCards(GraphicsContext gc) {
        boolean guest = Session.isGuest();
        Account acc = guest ? null : Session.current();
        String coins = guest ? "—" : String.valueOf(acc.coins);
        String caps = guest ? "—" : String.valueOf(acc.capsules);
        String best = guest ? "—" : String.valueOf(acc.highScore);

        double cardW = 300, cardH = 120, gap = 24;
        double totalW = cardW * 3 + gap * 2;
        double startX = (Config.LOGICAL_WIDTH - totalW) / 2.0;
        double y = panelY() + 440;

        drawStatCard(gc, startX, y, cardW, cardH, "Wallet", coins, GOLD,
                AssetManager.coinIcon(), 'C');
        drawStatCard(gc, startX + cardW + gap, y, cardW, cardH, "Capsules", caps, GREEN_ACCENT,
                AssetManager.revivalIcon(), 'P');
        drawStatCard(gc, startX + (cardW + gap) * 2, y, cardW, cardH, "Best Score", best, GOLD,
                null, 'K');
    }

    private void drawStatCard(GraphicsContext gc, double x, double y, double w, double h,
                              String label, String value, Color valueColor,
                              Image icon, char fallbackKind) {
        gc.setFill(Color.rgb(18, 26, 48, 0.88));
        gc.fillRoundRect(x, y, w, h, 18, 18);
        gc.setStroke(Color.rgb(100, 130, 200, 0.55));
        gc.setLineWidth(1.6);
        gc.strokeRoundRect(x, y, w, h, 18, 18);

        double iconSize = 60;
        double iconX = x + 20;
        double iconY = y + (h - iconSize) / 2.0;
        if (icon != null) {
            gc.drawImage(icon, iconX, iconY, iconSize, iconSize);
        } else if (fallbackKind == 'K') {
            drawCrownIcon(gc, iconX, iconY, iconSize);
        }

        gc.setFill(Color.rgb(210, 222, 245, 0.88));
        gc.setFont(Font.font("Arial", FontWeight.NORMAL, 22));
        gc.setTextAlign(TextAlignment.LEFT);
        gc.fillText(label, iconX + iconSize + 16, y + 52);

        gc.setFill(valueColor);
        gc.setFont(Font.font("Arial Black", FontWeight.BOLD, 40));
        gc.fillText(value, iconX + iconSize + 16, y + 94);
    }

    // ---------- back-to-menu button ----------
    private ButtonRect menuButton() {
        double w = 700, h = 110;
        double x = (Config.LOGICAL_WIDTH - w) / 2.0;
        double y = panelY() + 600;
        return new ButtonRect(x, y, w, h);
    }

    private void drawBackToMenuButton(GraphicsContext gc) {
        ButtonRect r = menuButton();
        boolean hovered = r.contains(input.getMouseX(), input.getMouseY());

        double scale = 1.0 + (hovered ? 0.02 : 0.0);
        double dw = r.w * scale;
        double dh = r.h * scale;
        double dx = r.x - (dw - r.w) / 2.0;
        double dy = r.y - (dh - r.h) / 2.0;

        gc.setFill(Color.rgb(255, 80, 40, hovered ? 0.45 : 0.30));
        gc.fillRoundRect(dx - 14, dy - 10, dw + 28, dh + 24, 30, 30);
        gc.setFill(Color.rgb(255, 120, 70, hovered ? 0.32 : 0.22));
        gc.fillRoundRect(dx - 4, dy - 4, dw + 8, dh + 14, 26, 26);

        LinearGradient fill = new LinearGradient(
                0, dy, 0, dy + dh, false, CycleMethod.NO_CYCLE,
                new Stop(0.0, hovered ? Color.web("#FF6464") : Color.web("#E04848")),
                new Stop(1.0, hovered ? Color.web("#B82020") : Color.web("#8A1818")));
        gc.setFill(fill);
        gc.fillRoundRect(dx, dy, dw, dh, 22, 22);
        gc.setStroke(Color.rgb(255, 220, 200, 0.85));
        gc.setLineWidth(2.0);
        gc.strokeRoundRect(dx, dy, dw, dh, 22, 22);
        gc.setStroke(Color.rgb(255, 255, 255, 0.30));
        gc.setLineWidth(1.0);
        gc.strokeRoundRect(dx + 5, dy + 5, dw - 10, dh - 10, 18, 18);

        gc.setFill(Color.rgb(255, 200, 100, 0.78));
        double glowW = dw * 0.62;
        gc.fillRoundRect(dx + (dw - glowW) / 2.0, dy + dh - 4, glowW, 4, 2, 2);

        double iconSize = 52;
        double iconX = dx + 48;
        double iconY = dy + (dh - iconSize) / 2.0;
        drawDoorIcon(gc, iconX, iconY, iconSize);

        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 38));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText("Back to Menu", dx + dw / 2.0 + 26, dy + dh / 2.0 + 14);

        drawChevron(gc, dx - 60, dy + dh / 2.0, 22, true);
        drawChevron(gc, dx - 90, dy + dh / 2.0, 22, true);
        drawChevron(gc, dx + dw + 30, dy + dh / 2.0, 22, false);
        drawChevron(gc, dx + dw + 60, dy + dh / 2.0, 22, false);
    }

    private void drawBottomHint(GraphicsContext gc) {
        double y = panelY() + PANEL_H - 38;
        double cx = Config.LOGICAL_WIDTH / 2.0;
        String text = "Choose a difficulty again from the menu";
        Font font = Font.font("Arial", FontWeight.NORMAL, 22);
        gc.setFont(font);
        gc.setFill(Color.rgb(170, 185, 215, 0.85));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText(text, cx, y);
        double tw = textWidth(text, font);
        drawTinyStar(gc, cx - tw / 2.0 - 32, y - 18, 16);
        drawTinyStar(gc, cx + tw / 2.0 + 16, y - 18, 16);
    }

    // ---------- icons ----------
    private void drawTrophyIcon(GraphicsContext gc, double x, double y, double size) {
        LinearGradient cupGrad = new LinearGradient(
                0, y, 0, y + size, false, CycleMethod.NO_CYCLE,
                new Stop(0.0, Color.web("#FFE26A")),
                new Stop(1.0, Color.web("#F08A1A")));
        gc.setFill(cupGrad);
        gc.fillRoundRect(x + size * 0.20, y + size * 0.10, size * 0.60, size * 0.50, 8, 8);
        gc.setStroke(GOLD);
        gc.setLineWidth(4.0);
        gc.strokeArc(x + size * 0.02, y + size * 0.16, size * 0.30, size * 0.32,
                60, 180, ArcType.OPEN);
        gc.strokeArc(x + size * 0.68, y + size * 0.16, size * 0.30, size * 0.32,
                -60, -180, ArcType.OPEN);
        gc.setFill(cupGrad);
        gc.fillRect(x + size * 0.40, y + size * 0.60, size * 0.20, size * 0.20);
        gc.fillRoundRect(x + size * 0.20, y + size * 0.78, size * 0.60, size * 0.14, 4, 4);
        gc.setFill(Color.WHITE);
        gc.fillOval(x + size * 0.42, y + size * 0.24, size * 0.16, size * 0.16);
    }

    private void drawCrownIcon(GraphicsContext gc, double x, double y, double size) {
        LinearGradient gold = new LinearGradient(
                0, y, 0, y + size, false, CycleMethod.NO_CYCLE,
                new Stop(0.0, Color.web("#FFE26A")),
                new Stop(1.0, Color.web("#E8A218")));
        gc.setFill(gold);
        gc.fillRoundRect(x + size * 0.08, y + size * 0.55, size * 0.84, size * 0.25, 6, 6);
        double[] xs = {
                x + size * 0.08, x + size * 0.25, x + size * 0.40,
                x + size * 0.50, x + size * 0.60, x + size * 0.75,
                x + size * 0.92, x + size * 0.92, x + size * 0.08
        };
        double[] ys = {
                y + size * 0.55, y + size * 0.15, y + size * 0.42,
                y + size * 0.06, y + size * 0.42, y + size * 0.15,
                y + size * 0.55, y + size * 0.55, y + size * 0.55
        };
        gc.fillPolygon(xs, ys, 9);
        gc.setFill(Color.web("#FF4747"));
        gc.fillOval(x + size * 0.42, y + size * 0.30, size * 0.16, size * 0.16);
        gc.setFill(Color.WHITE);
        gc.fillOval(x + size * 0.18, y + size * 0.60, size * 0.10, size * 0.10);
        gc.fillOval(x + size * 0.72, y + size * 0.60, size * 0.10, size * 0.10);
    }

    private void drawDoorIcon(GraphicsContext gc, double x, double y, double size) {
        gc.setStroke(Color.WHITE);
        gc.setLineWidth(4.5);
        gc.setLineCap(StrokeLineCap.ROUND);
        double doorW = size * 0.55;
        gc.strokeLine(x, y + 4, x, y + size - 4);
        gc.strokeLine(x, y + 4, x + doorW, y + 4);
        gc.strokeLine(x, y + size - 4, x + doorW, y + size - 4);
        gc.strokeLine(x + doorW, y + 4, x + doorW, y + size - 4);
        gc.strokeLine(x + doorW * 0.65, y + size / 2.0, x + size, y + size / 2.0);
        gc.strokeLine(x + size, y + size / 2.0, x + size * 0.78, y + size * 0.28);
        gc.strokeLine(x + size, y + size / 2.0, x + size * 0.78, y + size * 0.72);
        gc.setLineCap(StrokeLineCap.BUTT);
    }

    private void drawChevron(GraphicsContext gc, double cx, double cy, double size, boolean pointLeft) {
        gc.setStroke(Color.web("#FF8A4A"));
        gc.setLineWidth(5.0);
        gc.setLineCap(StrokeLineCap.ROUND);
        double tip = pointLeft ? cx - size * 0.5 : cx + size * 0.5;
        double back = pointLeft ? cx + size * 0.5 : cx - size * 0.5;
        gc.strokeLine(back, cy - size * 0.5, tip, cy);
        gc.strokeLine(back, cy + size * 0.5, tip, cy);
        gc.setLineCap(StrokeLineCap.BUTT);
    }

    private void drawTinyStar(GraphicsContext gc, double x, double y, double size) {
        gc.setFill(Color.rgb(170, 185, 215, 0.85));
        double[] xs = new double[8];
        double[] ys = new double[8];
        double cx = x + size / 2.0, cy = y + size / 2.0;
        double outer = size / 2.0, inner = size / 5.0;
        for (int i = 0; i < 8; i++) {
            double rr = (i % 2 == 0) ? outer : inner;
            double a = Math.PI * 2.0 * i / 8.0 - Math.PI / 2.0;
            xs[i] = cx + rr * Math.cos(a);
            ys[i] = cy + rr * Math.sin(a);
        }
        gc.fillPolygon(xs, ys, 8);
    }

    private double textWidth(String s, Font f) {
        Text helper = new Text(s);
        helper.setFont(f);
        return helper.getLayoutBounds().getWidth();
    }

    private record ButtonRect(double x, double y, double w, double h) {
        boolean contains(double px, double py) {
            return px >= x && px <= x + w && py >= y && py <= y + h;
        }
    }
}
