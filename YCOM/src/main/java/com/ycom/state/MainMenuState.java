package com.ycom.state;

import com.ycom.account.Account;
import com.ycom.account.Session;
import com.ycom.core.Config;
import com.ycom.resource.AssetManager;
import com.ycom.resource.AudioManager;
import com.ycom.system.InputSystem;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
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

public class MainMenuState implements GameState {
    private static final double CARD_W = 820.0;
    private static final double CARD_H = 124.0;
    private static final double CARD_GAP = 22.0;
    private static final double FIRST_CARD_Y = 380.0;
    private static final double EXPANDED_EXTRA_H = 126.0;
    private static final double INSTRUCTION_BTN_W = 540.0;
    private static final double INSTRUCTION_BTN_H = 84.0;
    private static final double HOVER_ANIMATION_SPEED = 6.5;
    private static final double HUD_PANEL_H = 72.0;
    private static final double HUD_PANEL_W = 760.0;
    private static final double HUD_PANEL_Y = 24.0;

    private static final Color YELLOW_ACCENT = Color.web("#FFC73B");
    private static final Color BLUE_PRIMARY = Color.web("#2E92FF");
    private static final Color BLUE_DARK = Color.web("#0F4FB8");
    private static final Color ORANGE_PRIMARY = Color.web("#FF9F2E");
    private static final Color ORANGE_DARK = Color.web("#B05E10");
    private static final Color RED_PRIMARY = Color.web("#FF4747");
    private static final Color RED_DARK = Color.web("#8A1818");

    private final GameStateManager gsm;
    private final Canvas canvas;
    private final InputSystem input;

    private final double[] difficultyHover = new double[Config.Difficulty.values().length];
    private double instructionHover = 0.0;

    private double[] starX;
    private double[] starY;
    private double[] starSize;

    public MainMenuState(GameStateManager gsm, Canvas canvas, InputSystem input) {
        this.gsm = gsm;
        this.canvas = canvas;
        this.input = input;
        initStars();
    }

    private void initStars() {
        Random rng = new Random(7331L);
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
        AudioManager.stopBGM();
    }

    @Override
    public void onExit() {}

    @Override
    public void update(double dt) {
        int hoveredDifficulty = hoveredDifficultyIndex();
        updateDifficultyHover(dt, hoveredDifficulty);
        updateInstructionHover(dt);

        if (!input.isMouseJustClicked()) return;
        double mx = input.getMouseX();
        double my = input.getMouseY();

        if (identityButton().contains(mx, my)) {
            if (!Session.isGuest()) Session.logout();
            gsm.setState("LOGIN");
            return;
        }
        if (Session.isLoggedIn() && shopButton().contains(mx, my)) {
            gsm.setState("SHOP");
            return;
        }
        if (instructionButton().contains(mx, my)) {
            gsm.setState("INSTRUCTION");
            return;
        }
        for (int i = 0; i < Config.Difficulty.values().length; i++) {
            if (cardRect(i).contains(mx, my)) {
                startGame(Config.Difficulty.values()[i]);
                return;
            }
        }
    }

    @Override
    public void render() {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        drawBackdrop(gc);
        drawSubwayTrain(gc);
        drawCornerDecorations(gc);
        drawTitle(gc);

        for (int i = 0; i < Config.Difficulty.values().length; i++) {
            drawDifficultyCard(gc, i);
        }
        drawInstructionButton(gc);

        drawTagline(gc);

        drawHudPanel(gc);
        if (Session.isLoggedIn()) {
            drawShopButton(gc);
        }
        drawIdentityButton(gc);

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
        gc.drawImage(train, -50, 460, 540, 420);
        gc.restore();
    }

    private void drawCornerDecorations(GraphicsContext gc) {
        Image magnet = AssetManager.magnetIcon();
        Image sprite = AssetManager.spriteIcon();
        Image cap = AssetManager.revivalIcon();
        Image coin = AssetManager.coinIcon();

        // bottom-left: magnet + random box on the same ground line
        gc.setFill(Color.rgb(80, 180, 255, 0.20));
        gc.fillOval(-30, 960, 240, 110);
        gc.setFill(Color.rgb(80, 180, 255, 0.32));
        gc.fillOval(0, 980, 180, 70);
        drawDecorImage(gc, magnet, 30, 880, 180, 180, 0.95);

        Image random = AssetManager.randomIcon();
        gc.setFill(Color.rgb(170, 90, 255, 0.20));
        gc.fillOval(180, 1010, 220, 90);
        gc.setFill(Color.rgb(170, 90, 255, 0.32));
        gc.fillOval(210, 1025, 160, 60);
        drawDecorImage(gc, random, 200, 905, 170, 170, 0.95);

        // bottom-right cluster: energy_drink + capsule + coin
        gc.setFill(Color.rgb(170, 90, 255, 0.20));
        gc.fillOval(1640, 950, 240, 110);
        drawDecorImage(gc, sprite, 1660, 770, 200, 260, 0.95);
        gc.setFill(Color.rgb(255, 180, 80, 0.18));
        gc.fillOval(1530, 950, 170, 90);
        drawDecorImage(gc, cap, 1530, 920, 150, 140, 0.92);
        gc.setFill(Color.rgb(255, 200, 80, 0.20));
        gc.fillOval(1820, 940, 170, 90);
        drawDecorImage(gc, coin, 1830, 850, 160, 160, 0.95);
    }

    private void drawDecorImage(GraphicsContext gc, Image img,
                                double x, double y, double w, double h, double alpha) {
        if (img == null) return;
        gc.save();
        gc.setGlobalAlpha(alpha);
        gc.drawImage(img, x, y, w, h);
        gc.restore();
    }

    // ---------- title ----------
    private void drawTitle(GraphicsContext gc) {
        Font font = Font.font("Arial Black", FontWeight.EXTRA_BOLD, 92);
        LinearGradient outrunGrad = new LinearGradient(
                0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0.0, Color.web("#FFE26A")),
                new Stop(0.55, Color.web("#FFC23B")),
                new Stop(1.0, Color.web("#F08A1A")));
        drawSlantedTitleLine(gc, Config.LOGICAL_WIDTH / 2.0, 195,
                new String[]{"YOU CAN'T"},
                new Paint[]{Color.WHITE},
                font);
        drawSlantedTitleLine(gc, Config.LOGICAL_WIDTH / 2.0, 295,
                new String[]{"OUTRUN", " ME!"},
                new Paint[]{outrunGrad, Color.WHITE},
                font);
        // small running figure in upper-right corner of the title
        drawTinyRunner(gc, Config.LOGICAL_WIDTH / 2.0 + 360, 145, 70);
    }

    private void drawTinyRunner(GraphicsContext gc, double cx, double cy, double size) {
        gc.save();
        gc.translate(cx, cy);
        gc.rotate(-6.0);
        Color c = Color.WHITE;
        gc.setFill(c);
        gc.fillOval(-size * 0.10, -size * 0.55, size * 0.22, size * 0.22);
        gc.setStroke(c);
        gc.setLineWidth(5.0);
        gc.setLineCap(StrokeLineCap.ROUND);
        gc.strokeLine(0, -size * 0.30, 0, size * 0.05);
        gc.strokeLine(0, size * 0.05, -size * 0.30, size * 0.36);
        gc.strokeLine(0, size * 0.05, size * 0.30, size * 0.30);
        gc.strokeLine(0, -size * 0.18, size * 0.30, -size * 0.04);
        gc.strokeLine(0, -size * 0.18, -size * 0.30, -size * 0.10);
        // speed dashes behind it
        gc.setStroke(Color.rgb(110, 175, 255, 0.55));
        gc.setLineWidth(5.0);
        gc.strokeLine(-size * 0.90, -size * 0.40, -size * 0.45, -size * 0.40);
        gc.strokeLine(-size * 1.00, -size * 0.20, -size * 0.55, -size * 0.20);
        gc.strokeLine(-size * 0.95, 0, -size * 0.55, 0);
        gc.setLineCap(StrokeLineCap.BUTT);
        gc.restore();
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

        gc.setLineCap(StrokeLineCap.ROUND);
        for (int s = 0; s < 5; s++) {
            double yOff = -70.0 + s * 22.0;
            double width = 10.0 - s * 1.0;
            double alpha = 0.65 - s * 0.10;
            gc.setStroke(Color.rgb(110, 175, 255, alpha));
            gc.setLineWidth(Math.max(width, 3.0));
            double x1 = textStartX - 280.0 + s * 22.0;
            double x2 = textStartX - 22.0 - s * 6.0;
            gc.strokeLine(x1, yOff, x2, yOff);
        }
        for (int s = 0; s < 4; s++) {
            double yOff = -55.0 + s * 24.0;
            double width = 9.0 - s * 1.0;
            double alpha = 0.45 - s * 0.08;
            gc.setStroke(Color.rgb(110, 175, 255, alpha));
            gc.setLineWidth(Math.max(width, 3.0));
            double x1 = textStartX + total + 26.0 + s * 8.0;
            double x2 = textStartX + total + 160.0 - s * 12.0;
            gc.strokeLine(x1, yOff, x2, yOff);
        }
        gc.setLineCap(StrokeLineCap.BUTT);

        gc.setFill(Color.rgb(2, 6, 22, 0.85));
        double sx = textStartX;
        for (int i = 0; i < parts.length; i++) {
            gc.fillText(parts[i], sx + 6, 8);
            sx += widths[i];
        }
        gc.setStroke(Color.web("#0a1a3c"));
        gc.setLineWidth(7.0);
        sx = textStartX;
        for (int i = 0; i < parts.length; i++) {
            gc.strokeText(parts[i], sx, 0);
            sx += widths[i];
        }
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

    // ---------- difficulty cards ----------
    private void drawDifficultyCard(GraphicsContext gc, int idx) {
        Config.Difficulty d = Config.Difficulty.values()[idx];
        ButtonRect r = cardRect(idx);
        double eased = easeOut(difficultyHover[idx]);
        // Rect height already grows with hover via cardRect; draw at native rect size.
        double dw = r.w;
        double dh = r.h;
        double dx = r.x;
        double dy = r.y;

        Color primary = primaryColor(d);
        Color dark = darkColor(d);

        // outer glow on hover
        if (eased > 0) {
            gc.setFill(brighten(primary, 0.2, 0.30 * eased));
            gc.fillRoundRect(dx - 10, dy - 10, dw + 20, dh + 20, 26, 26);
        }

        // soft drop shadow
        gc.setFill(Color.rgb(0, 0, 0, 0.50));
        gc.fillRoundRect(dx + 6, dy + 10, dw, dh, 22, 22);

        // dark translucent fill with subtle colored tint
        LinearGradient bgFill = new LinearGradient(
                0, dy, 0, dy + dh, false, CycleMethod.NO_CYCLE,
                new Stop(0.0, Color.rgb(16, 26, 50, 0.90)),
                new Stop(1.0, Color.rgb(10, 18, 38, 0.94)));
        gc.setFill(bgFill);
        gc.fillRoundRect(dx, dy, dw, dh, 22, 22);

        // colored inner tint
        gc.setFill(tintAlpha(primary, 0.10 + 0.10 * eased));
        gc.fillRoundRect(dx, dy, dw, dh, 22, 22);

        // halftone dot pattern on the right side
        drawHalftone(gc, dx + dw * 0.55, dy + 8, dw * 0.40, dh - 16, primary, 0.35);

        // bright outlined border
        LinearGradient borderGrad = new LinearGradient(
                dx, dy, dx + dw, dy + dh, false, CycleMethod.NO_CYCLE,
                new Stop(0.0, brighten(primary, 0.10, 1.0)),
                new Stop(1.0, brighten(dark, 0.05, 1.0)));
        gc.setStroke(borderGrad);
        gc.setLineWidth(3.0 + 1.5 * eased);
        gc.strokeRoundRect(dx, dy, dw, dh, 22, 22);

        // inner edge highlight
        gc.setStroke(Color.rgb(255, 255, 255, 0.18));
        gc.setLineWidth(1.0);
        gc.strokeRoundRect(dx + 5, dy + 5, dw - 10, dh - 10, 18, 18);

        // icon — pinned to the collapsed-header area so it stays put while the card expands.
        double headerH = CARD_H;
        double iconSize = 92;
        double iconX = dx + 30;
        double iconY = dy + (headerH - iconSize) / 2.0;
        gc.setFill(Color.rgb(0, 0, 0, 0.25));
        gc.fillRoundRect(iconX - 4, iconY - 4, iconSize + 8, iconSize + 8, 16, 16);
        drawDifficultyIcon(gc, d, iconX, iconY, iconSize);

        // name + subtitle
        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Arial Black", FontWeight.BOLD, 46));
        gc.setTextAlign(TextAlignment.LEFT);
        gc.fillText(d.label.toUpperCase(), dx + 150, dy + 60);

        gc.setFont(Font.font("Arial", FontWeight.NORMAL, 22));
        gc.setFill(primary);
        gc.fillText(difficultySubtitle(d), dx + 150, dy + 93);

        // expanded description (3 bullets) — fades in with hover
        if (eased > 0.02) {
            drawDifficultyDescription(gc, d, dx, dy, dw, eased, primary);
        }

        // right arrow — kept in the header area so it stays vertically aligned with the icon/name.
        drawArrow(gc, dx + dw - 60, dy + headerH / 2.0, 24 + 6 * eased);
    }

    private void drawDifficultyDescription(GraphicsContext gc, Config.Difficulty d,
                                           double dx, double dy, double dw,
                                           double alpha, Color accent) {
        String[] lines = switch (d) {
            case EASY -> new String[]{
                    "Fewer obstacles and wider spawn gaps",
                    "More time before lane blocks appear",
                    "Best for learning items and movement"
            };
            case MEDIUM -> new String[]{
                    "Balanced obstacles and item chances",
                    "Normal speed growth and pressure",
                    "Recommended mode for a fair run"
            };
            case HARD -> new String[]{
                    "More obstacles with shorter gaps",
                    "Lane blocks appear much earlier",
                    "Fewer safety items and faster pressure"
            };
        };

        double a = Math.max(0.0, Math.min(1.0, alpha));
        gc.setGlobalAlpha(a);
        // separator line
        gc.setStroke(Color.rgb(255, 255, 255, 0.22));
        gc.setLineWidth(1.2);
        gc.strokeLine(dx + 40, dy + CARD_H - 4, dx + dw - 40, dy + CARD_H - 4);

        gc.setFont(Font.font("Arial", FontWeight.NORMAL, 24));
        gc.setFill(Color.rgb(230, 240, 255, 0.92));
        gc.setTextAlign(TextAlignment.LEFT);
        double textX = dx + 72.0;
        double textY = dy + CARD_H + 30.0;
        for (int i = 0; i < lines.length; i++) {
            gc.fillText(lines[i], textX, textY + i * 32.0);
        }

        gc.setFill(tintAlpha(accent, 0.85));
        for (int i = 0; i < lines.length; i++) {
            gc.fillOval(dx + 44.0, textY - 12.0 + i * 32.0, 10.0, 10.0);
        }
        gc.setGlobalAlpha(1.0);
        gc.setTextAlign(TextAlignment.CENTER);
    }

    private void drawHalftone(GraphicsContext gc, double x, double y, double w, double h,
                              Color color, double maxAlpha) {
        double dotSize = 6.0;
        double gap = 14.0;
        for (double yy = y; yy < y + h; yy += gap) {
            for (double xx = x; xx < x + w; xx += gap) {
                double t = (xx - x) / w;
                double alpha = maxAlpha * t;
                if (alpha <= 0.02) continue;
                gc.setFill(new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha));
                gc.fillOval(xx, yy, dotSize, dotSize);
            }
        }
    }

    private Color primaryColor(Config.Difficulty d) {
        return switch (d) {
            case EASY -> BLUE_PRIMARY;
            case MEDIUM -> ORANGE_PRIMARY;
            case HARD -> RED_PRIMARY;
        };
    }

    private Color darkColor(Config.Difficulty d) {
        return switch (d) {
            case EASY -> BLUE_DARK;
            case MEDIUM -> ORANGE_DARK;
            case HARD -> RED_DARK;
        };
    }

    private Color brighten(Color base, double amount, double opacity) {
        double a = Math.max(0.0, Math.min(1.0, amount));
        double r = base.getRed() + (1.0 - base.getRed()) * a;
        double g = base.getGreen() + (1.0 - base.getGreen()) * a;
        double b = base.getBlue() + (1.0 - base.getBlue()) * a;
        return new Color(r, g, b, opacity);
    }

    private Color tintAlpha(Color base, double opacity) {
        return new Color(base.getRed(), base.getGreen(), base.getBlue(), opacity);
    }

    private String difficultySubtitle(Config.Difficulty d) {
        return switch (d) {
            case EASY -> "For new runners";
            case MEDIUM -> "Challenge yourself";
            case HARD -> "Only for pros";
        };
    }

    private void drawDifficultyIcon(GraphicsContext gc, Config.Difficulty d,
                                    double x, double y, double size) {
        switch (d) {
            case EASY -> {
                Image sheet = AssetManager.runSheet();
                if (sheet != null && sheet.getWidth() > 0) {
                    int frames = AssetManager.frameCount("run");
                    if (frames < 1) frames = 8;
                    double sw = sheet.getWidth() / frames;
                    double sh = sheet.getHeight();
                    int idx = Math.min(2, frames - 1);
                    gc.drawImage(sheet, sw * idx, 0, sw, sh, x, y, size, size);
                } else drawFallbackLetter(gc, "E", x, y, size);
            }
            case MEDIUM -> {
                Image img = AssetManager.spriteIcon();
                if (img != null) gc.drawImage(img, x, y, size, size);
                else drawFallbackLetter(gc, "M", x, y, size);
            }
            case HARD -> {
                Image img = AssetManager.obstacleTrainIcon();
                if (img != null) gc.drawImage(img, x, y, size, size);
                else drawFallbackLetter(gc, "H", x, y, size);
            }
        }
    }

    private void drawFallbackLetter(GraphicsContext gc, String s, double x, double y, double size) {
        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Arial Black", FontWeight.BOLD, size * 0.7));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText(s, x + size / 2.0, y + size * 0.78);
    }

    private void drawArrow(GraphicsContext gc, double cx, double cy, double size) {
        gc.setStroke(Color.WHITE);
        gc.setLineWidth(5.0);
        gc.setLineCap(StrokeLineCap.ROUND);
        double half = size * 0.5;
        gc.strokeLine(cx - half, cy - half * 0.8, cx + half * 0.4, cy);
        gc.strokeLine(cx - half, cy + half * 0.8, cx + half * 0.4, cy);
        gc.setLineCap(StrokeLineCap.BUTT);
    }

    // ---------- instruction button ----------
    private void drawInstructionButton(GraphicsContext gc) {
        ButtonRect r = instructionButton();
        double eased = easeOut(instructionHover);
        double scale = 1.0 + 0.02 * eased;
        double dw = r.w * scale;
        double dh = r.h * scale;
        double dx = r.x - (dw - r.w) / 2.0;
        double dy = r.y - (dh - r.h) / 2.0;

        gc.setFill(Color.rgb(0, 0, 0, 0.40));
        gc.fillRoundRect(dx + 4, dy + 6, dw, dh, 18, 18);
        gc.setFill(Color.rgb(20, 32, 60, 0.70 + 0.15 * eased));
        gc.fillRoundRect(dx, dy, dw, dh, 18, 18);
        gc.setStroke(eased > 0.5 ? YELLOW_ACCENT : Color.rgb(190, 210, 245, 0.85));
        gc.setLineWidth(2.0 + 1.2 * eased);
        gc.strokeRoundRect(dx, dy, dw, dh, 18, 18);

        double iconSize = 48;
        double iconX = dx + 26;
        double iconY = dy + (dh - iconSize) / 2.0;
        drawInfoIcon(gc, iconX, iconY, iconSize);

        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Arial Black", FontWeight.BOLD, 30));
        gc.setTextAlign(TextAlignment.LEFT);
        gc.fillText("INSTRUCTION", iconX + iconSize + 18, dy + 40);
        gc.setFont(Font.font("Arial", FontWeight.NORMAL, 18));
        gc.setFill(Color.rgb(180, 210, 245, 0.90));
        gc.fillText("Learn how to play", iconX + iconSize + 18, dy + 66);

        drawArrow(gc, dx + dw - 42, dy + dh / 2.0, 18);
    }

    private void drawInfoIcon(GraphicsContext gc, double x, double y, double size) {
        gc.setStroke(YELLOW_ACCENT);
        gc.setLineWidth(3.0);
        gc.strokeOval(x, y, size, size);
        gc.setFill(YELLOW_ACCENT);
        gc.setFont(Font.font("Arial Black", FontWeight.BOLD, size * 0.62));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText("i", x + size / 2.0 + 1, y + size * 0.74);
    }

    // ---------- tagline ----------
    private void drawTagline(GraphicsContext gc) {
        // hide the tagline while any difficulty card is expanding so the bullets get full vertical space.
        for (double h : difficultyHover) {
            if (h > 0.05) return;
        }
        double y = instructionButton().y + INSTRUCTION_BTN_H + 78;
        if (y > Config.LOGICAL_HEIGHT - 30) y = Config.LOGICAL_HEIGHT - 30;
        double cx = Config.LOGICAL_WIDTH / 2.0;
        String text = "Run fast.  Collect more.  Never stop.";
        Font font = Font.font("Arial", FontPosture.ITALIC, 28);
        gc.setFont(font);
        gc.setFill(YELLOW_ACCENT);
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText(text, cx, y);
        double tw = textWidth(text, font);
        drawSpeedChevron(gc, cx - tw / 2.0 - 56, y - 10, 40, true);
        drawSpeedChevron(gc, cx + tw / 2.0 + 16, y - 10, 40, false);
    }

    private void drawSpeedChevron(GraphicsContext gc, double x, double y, double size, boolean pointLeft) {
        gc.setStroke(YELLOW_ACCENT);
        gc.setLineWidth(4.5);
        gc.setLineCap(StrokeLineCap.ROUND);
        double tip = pointLeft ? x : x + size;
        double back = pointLeft ? x + size : x;
        gc.strokeLine(back, y, tip, y + size * 0.25);
        gc.strokeLine(back, y + size * 0.5, tip, y + size * 0.75);
        gc.setLineCap(StrokeLineCap.BUTT);
    }

    // ---------- HUD (top-center) ----------
    private void drawHudPanel(GraphicsContext gc) {
        double panelX = (Config.LOGICAL_WIDTH - HUD_PANEL_W) / 2.0;

        gc.setFill(Color.rgb(14, 22, 42, 0.82));
        gc.fillRoundRect(panelX, HUD_PANEL_Y, HUD_PANEL_W, HUD_PANEL_H, 18, 18);
        gc.setStroke(Color.rgb(160, 200, 255, 0.55));
        gc.setLineWidth(1.8);
        gc.strokeRoundRect(panelX, HUD_PANEL_Y, HUD_PANEL_W, HUD_PANEL_H, 18, 18);

        Font font = Font.font("Arial", FontWeight.BOLD, 22);
        gc.setFont(font);
        gc.setTextAlign(TextAlignment.LEFT);
        double textY = HUD_PANEL_Y + HUD_PANEL_H / 2.0 + 8.0;
        double cx = panelX + 14.0;

        // avatar (player image cropped into a rounded square)
        double avatarSize = HUD_PANEL_H - 16.0;
        double avatarY = HUD_PANEL_Y + 8.0;
        gc.setFill(Color.rgb(70, 140, 255, 0.30));
        gc.fillRoundRect(cx, avatarY, avatarSize, avatarSize, 14, 14);
        Image avatar = AssetManager.playerImage();
        if (avatar != null) {
            gc.save();
            gc.beginPath();
            gc.moveTo(cx + 14, avatarY);
            gc.lineTo(cx + avatarSize - 14, avatarY);
            gc.arc(cx + avatarSize - 14, avatarY + 14, 14, 14, 90, -90);
            gc.lineTo(cx + avatarSize, avatarY + avatarSize - 14);
            gc.arc(cx + avatarSize - 14, avatarY + avatarSize - 14, 14, 14, 0, -90);
            gc.lineTo(cx + 14, avatarY + avatarSize);
            gc.arc(cx + 14, avatarY + avatarSize - 14, 14, 14, 270, -90);
            gc.lineTo(cx, avatarY + 14);
            gc.arc(cx + 14, avatarY + 14, 14, 14, 180, -90);
            gc.closePath();
            gc.clip();
            // draw character with head visible: scale up + shift up so face shows
            double drawW = avatarSize * 1.6;
            double drawH = avatarSize * 2.4;
            gc.drawImage(avatar, cx - (drawW - avatarSize) / 2.0,
                    avatarY - drawH * 0.10, drawW, drawH);
            gc.restore();
        }
        gc.setStroke(Color.rgb(160, 200, 255, 0.55));
        gc.setLineWidth(1.4);
        gc.strokeRoundRect(cx, avatarY, avatarSize, avatarSize, 14, 14);
        cx += avatarSize + 14.0;

        if (Session.isGuest()) {
            gc.setFill(YELLOW_ACCENT);
            gc.fillText("GUEST MODE", cx, textY);
            return;
        }

        Account acc = Session.current();
        gc.setFill(YELLOW_ACCENT);
        gc.fillText(acc.username, cx, textY);
        cx += textWidth(acc.username, font) + 22.0;

        // separator
        gc.setStroke(Color.rgb(255, 255, 255, 0.22));
        gc.setLineWidth(1.2);
        gc.strokeLine(cx - 11, HUD_PANEL_Y + 16, cx - 11, HUD_PANEL_Y + HUD_PANEL_H - 16);

        // coins
        double iconSize = 30.0;
        double iconY = HUD_PANEL_Y + (HUD_PANEL_H - iconSize) / 2.0;
        Image coinImg = AssetManager.coinIcon();
        if (coinImg != null) gc.drawImage(coinImg, cx, iconY, iconSize, iconSize);
        cx += iconSize + 6.0;
        gc.setFill(Color.WHITE);
        gc.fillText("coins:", cx, textY);
        cx += textWidth("coins:", font) + 6.0;
        gc.setFill(YELLOW_ACCENT);
        String coinText = String.valueOf(acc.coins);
        gc.fillText(coinText, cx, textY);
        cx += textWidth(coinText, font) + 18.0;

        // caps
        Image capImg = AssetManager.revivalIcon();
        if (capImg != null) gc.drawImage(capImg, cx, iconY, iconSize, iconSize);
        cx += iconSize + 6.0;
        gc.setFill(Color.WHITE);
        gc.fillText("caps:", cx, textY);
        cx += textWidth("caps:", font) + 6.0;
        gc.setFill(YELLOW_ACCENT);
        String capText = String.valueOf(acc.capsules);
        gc.fillText(capText, cx, textY);
        cx += textWidth(capText, font) + 18.0;

        // HI
        Image hiImg = AssetManager.ascensionImage();
        if (hiImg != null) gc.drawImage(hiImg, cx, iconY, iconSize, iconSize);
        cx += iconSize + 6.0;
        gc.setFill(Color.WHITE);
        gc.fillText("HI:", cx, textY);
        cx += textWidth("HI:", font) + 6.0;
        gc.setFill(YELLOW_ACCENT);
        gc.fillText(String.valueOf(acc.highScore), cx, textY);
    }

    private void drawIdentityButton(GraphicsContext gc) {
        ButtonRect r = identityButton();
        boolean hovered = r.contains(input.getMouseX(), input.getMouseY());
        gc.setFill(hovered ? Color.rgb(70, 140, 255, 0.30) : Color.rgb(14, 22, 42, 0.82));
        gc.fillRoundRect(r.x, r.y, r.w, r.h, 14, 14);
        gc.setStroke(hovered ? Color.web("#7DB8FF") : Color.rgb(190, 210, 245, 0.85));
        gc.setLineWidth(hovered ? 2.4 : 1.8);
        gc.strokeRoundRect(r.x, r.y, r.w, r.h, 14, 14);

        String label = Session.isGuest() ? "LOGIN" : "LOGOUT";
        double iconSize = 28;
        double iconX = r.x + 14;
        double iconY = r.y + (r.h - iconSize) / 2.0;
        drawLogoutIcon(gc, iconX, iconY, iconSize);

        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 22));
        gc.setTextAlign(TextAlignment.LEFT);
        gc.fillText(label, iconX + iconSize + 12, r.y + r.h / 2.0 + 8);
    }

    private void drawLogoutIcon(GraphicsContext gc, double x, double y, double size) {
        gc.setStroke(Color.WHITE);
        gc.setLineWidth(2.8);
        gc.setLineCap(StrokeLineCap.ROUND);
        // door frame
        gc.strokeLine(x, y + 2, x, y + size - 2);
        gc.strokeLine(x, y + 2, x + size * 0.55, y + 2);
        gc.strokeLine(x, y + size - 2, x + size * 0.55, y + size - 2);
        gc.strokeLine(x + size * 0.55, y + 2, x + size * 0.55, y + size - 2);
        // arrow
        gc.strokeLine(x + size * 0.35, y + size / 2.0, x + size, y + size / 2.0);
        gc.strokeLine(x + size, y + size / 2.0, x + size * 0.75, y + size * 0.25);
        gc.strokeLine(x + size, y + size / 2.0, x + size * 0.75, y + size * 0.75);
        gc.setLineCap(StrokeLineCap.BUTT);
    }

    private void drawShopButton(GraphicsContext gc) {
        ButtonRect r = shopButton();
        boolean hovered = r.contains(input.getMouseX(), input.getMouseY());
        gc.setFill(hovered ? Color.rgb(70, 140, 255, 0.30) : Color.rgb(14, 22, 42, 0.82));
        gc.fillRoundRect(r.x, r.y, r.w, r.h, 14, 14);
        gc.setStroke(hovered ? Color.web("#7DB8FF") : Color.rgb(190, 210, 245, 0.85));
        gc.setLineWidth(hovered ? 2.4 : 1.8);
        gc.strokeRoundRect(r.x, r.y, r.w, r.h, 14, 14);

        Image icon = AssetManager.shoppingIcon();
        if (icon != null && icon.getWidth() > 0.0) {
            double pad = 10.0;
            gc.drawImage(icon, r.x + pad, r.y + pad, r.w - pad * 2, r.h - pad * 2);
        } else {
            drawCartIcon(gc, r.x + 12, r.y + 14, r.w - 24);
        }
    }

    private void drawCartIcon(GraphicsContext gc, double x, double y, double size) {
        gc.setStroke(Color.WHITE);
        gc.setLineWidth(2.8);
        gc.setLineCap(StrokeLineCap.ROUND);
        gc.strokeLine(x, y, x + size * 0.20, y);
        gc.strokeLine(x + size * 0.20, y, x + size * 0.30, y + size * 0.55);
        gc.strokeLine(x + size * 0.30, y + size * 0.55, x + size, y + size * 0.55);
        gc.strokeLine(x + size, y + size * 0.55, x + size * 0.86, y + size * 0.18);
        gc.strokeOval(x + size * 0.40, y + size * 0.75, size * 0.12, size * 0.12);
        gc.strokeOval(x + size * 0.70, y + size * 0.75, size * 0.12, size * 0.12);
        gc.setLineCap(StrokeLineCap.BUTT);
    }

    // ---------- rects ----------
    private ButtonRect cardRect(int idx) {
        double x = (Config.LOGICAL_WIDTH - CARD_W) / 2.0;
        double y = FIRST_CARD_Y;
        for (int i = 0; i < idx; i++) {
            y += CARD_H + EXPANDED_EXTRA_H * easeOut(difficultyHover[i]) + CARD_GAP;
        }
        double h = CARD_H + EXPANDED_EXTRA_H * easeOut(difficultyHover[idx]);
        return new ButtonRect(x, y, CARD_W, h);
    }

    private ButtonRect instructionButton() {
        int n = Config.Difficulty.values().length;
        double y = FIRST_CARD_Y;
        for (int i = 0; i < n; i++) {
            y += CARD_H + EXPANDED_EXTRA_H * easeOut(difficultyHover[i]) + CARD_GAP;
        }
        y += 6.0;
        double x = (Config.LOGICAL_WIDTH - INSTRUCTION_BTN_W) / 2.0;
        return new ButtonRect(x, y, INSTRUCTION_BTN_W, INSTRUCTION_BTN_H);
    }

    private ButtonRect identityButton() {
        double w = 174.0;
        double h = 64.0;
        return new ButtonRect(Config.LOGICAL_WIDTH - w - 30.0, 28.0, w, h);
    }

    private ButtonRect shopButton() {
        ButtonRect id = identityButton();
        double size = id.h;
        return new ButtonRect(id.x - size - 14.0, id.y, size, size);
    }

    // ---------- hover state ----------
    private int hoveredDifficultyIndex() {
        double mx = input.getMouseX();
        double my = input.getMouseY();
        for (int i = 0; i < Config.Difficulty.values().length; i++) {
            if (cardRect(i).contains(mx, my)) return i;
        }
        return -1;
    }

    private void updateDifficultyHover(double dt, int target) {
        double step = Math.min(1.0, Math.max(0.0, dt * HOVER_ANIMATION_SPEED));
        for (int i = 0; i < difficultyHover.length; i++) {
            double goal = i == target ? 1.0 : 0.0;
            difficultyHover[i] += (goal - difficultyHover[i]) * step;
            if (Math.abs(goal - difficultyHover[i]) < 0.002) difficultyHover[i] = goal;
        }
    }

    private void updateInstructionHover(double dt) {
        boolean h = instructionButton().contains(input.getMouseX(), input.getMouseY());
        double step = Math.min(1.0, Math.max(0.0, dt * HOVER_ANIMATION_SPEED));
        double goal = h ? 1.0 : 0.0;
        instructionHover += (goal - instructionHover) * step;
        if (Math.abs(goal - instructionHover) < 0.002) instructionHover = goal;
    }

    private double easeOut(double t) {
        double clamped = Math.max(0.0, Math.min(1.0, t));
        return 1.0 - Math.pow(1.0 - clamped, 3.0);
    }

    private void startGame(Config.Difficulty difficulty) {
        GameState state = gsm.getState("PLAYING");
        if (state instanceof PlayingState playingState) {
            playingState.resetGame(difficulty);
        }
        gsm.setState("PLAYING");
    }

    private record ButtonRect(double x, double y, double w, double h) {
        boolean contains(double px, double py) {
            return px >= x && px <= x + w && py >= y && py <= y + h;
        }
    }
}
