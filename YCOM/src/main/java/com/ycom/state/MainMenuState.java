package com.ycom.state;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import com.ycom.account.Account;
import com.ycom.account.Session;
import com.ycom.core.Config;
import com.ycom.resource.AssetManager;
import com.ycom.resource.AudioManager;
import com.ycom.system.InputSystem;

public class MainMenuState implements GameState {
    private static final double DIFFICULTY_X = 650.0;
    private static final double DIFFICULTY_Y = 278.0;
    private static final double DIFFICULTY_W = 620.0;
    private static final double DIFFICULTY_H = 96.0;
    private static final double DIFFICULTY_SPACING = 128.0;
    private static final double EXPANDED_EXTRA_H = 126.0;
    private static final double HOVER_ANIMATION_SPEED = 5.8;

    private final GameStateManager gsm;
    private final Canvas canvas;
    private final InputSystem input;
    private final double[] difficultyHover = new double[Config.Difficulty.values().length];

    public MainMenuState(GameStateManager gsm, Canvas canvas, InputSystem input) {
        this.gsm = gsm;
        this.canvas = canvas;
        this.input = input;
    }
    
    @Override
    public void onEnter() {
        AudioManager.stopBGM();
    }

    @Override public void onExit() {}

    @Override
    public void update(double dt) {
        int hoveredDifficulty = hoveredDifficultyIndex();
        updateDifficultyHover(dt, hoveredDifficulty);

        if (!input.isMouseJustClicked()) {
            return;
        }

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
            ButtonRect rect = buttonRect(i);
            if (rect.contains(mx, my)) {
                startGame(Config.Difficulty.values()[i]);
                return;
            }
        }
    }

    @Override
    public void render() {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        if (AssetManager.background() != null) {
            gc.drawImage(AssetManager.background(), 0, 0, Config.LOGICAL_WIDTH, Config.LOGICAL_HEIGHT);
//            gc.setFill(Color.rgb(0, 0, 0, 0.58));
            gc.setFill(Color.rgb(124,183,213,0.58));
            gc.fillRect(0, 0, Config.LOGICAL_WIDTH, Config.LOGICAL_HEIGHT);
        } else {
            gc.setFill(Color.web("#101820"));
            gc.fillRect(0, 0, Config.LOGICAL_WIDTH, Config.LOGICAL_HEIGHT);
        }

        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 78));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText("YOU CAN'T OUTRUN ME!", Config.LOGICAL_WIDTH / 2.0, 168.0);

        for (int i = 0; i < Config.Difficulty.values().length; i++) {
            Config.Difficulty difficulty = Config.Difficulty.values()[i];
            ButtonRect rect = buttonRect(i);
            drawDifficultyButton(gc, rect, difficulty, difficultyHover[i]);
        }

        drawInstructionButton(gc);
        gc.setTextAlign(TextAlignment.LEFT);

        drawIdentityBar(gc);
        if (Session.isLoggedIn()) {
            drawShopButton(gc);
        }
    }

    private void drawIdentityBar(GraphicsContext gc) {
        ButtonRect btn = identityButton();
        String info;
        String label;
        if (Session.isGuest()) {
            info = "GUEST MODE";
            label = "LOGIN";
        } else {
            Account acc = Session.current();
            info = acc.username + "   coins: " + acc.coins + "   caps: " + acc.capsules + "   HI: " + acc.highScore;
            label = "LOGOUT";
        }

        boolean shopVisible = Session.isLoggedIn();
        double infoBoxW = shopVisible ? 672.0 : 740.0;
        double infoTextRight = shopVisible ? btn.x - 96.0 : btn.x - 30.0;
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 26));
        gc.setFill(Color.rgb(0, 0, 0, 0.45));
        gc.fillRoundRect(btn.x - 760.0, btn.y, infoBoxW, btn.h, 8.0, 8.0);
        gc.setFill(Color.WHITE);
        gc.setTextAlign(TextAlignment.RIGHT);
        gc.fillText(info, infoTextRight, btn.y + 40.0);

        boolean hovered = btn.contains(input.getMouseX(), input.getMouseY());
        if (hovered) {
            gc.setFill(Color.rgb(255, 255, 255, 0.22));
            gc.fillRoundRect(btn.x, btn.y, btn.w, btn.h, 8.0, 8.0);
        }
        gc.setStroke(Color.WHITE);
        gc.setLineWidth(2.5);
        gc.strokeRoundRect(btn.x, btn.y, btn.w, btn.h, 8.0, 8.0);
        gc.setFill(Color.WHITE);
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 26));
        gc.fillText(label, btn.x + btn.w / 2.0, btn.y + 40.0);

        gc.setTextAlign(TextAlignment.LEFT);
    }

    private ButtonRect identityButton() {
        double w = 180.0;
        double h = 60.0;
        return new ButtonRect(Config.LOGICAL_WIDTH - w - 30.0, 30.0, w, h);
    }

    private ButtonRect shopButton() {
        ButtonRect id = identityButton();
        double size = 60.0;
        return new ButtonRect(id.x - size - 14.0, id.y, size, size);
    }

    private void drawShopButton(GraphicsContext gc) {
        ButtonRect btn = shopButton();
        boolean hovered = btn.contains(input.getMouseX(), input.getMouseY());
        gc.setFill(hovered ? Color.rgb(255, 255, 255, 0.28) : Color.rgb(0, 0, 0, 0.42));
        gc.fillRoundRect(btn.x, btn.y, btn.w, btn.h, 8.0, 8.0);
        gc.setStroke(Color.WHITE);
        gc.setLineWidth(2.5);
        gc.strokeRoundRect(btn.x, btn.y, btn.w, btn.h, 8.0, 8.0);

        Image icon = AssetManager.shoppingIcon();
        if (icon != null && icon.getWidth() > 0.0) {
            double pad = 8.0;
            double iw = icon.getWidth();
            double ih = icon.getHeight();
            double scale = Math.min((btn.w - pad * 2) / iw, (btn.h - pad * 2) / ih);
            double dw = iw * scale;
            double dh = ih * scale;
            gc.drawImage(icon, btn.x + (btn.w - dw) / 2.0, btn.y + (btn.h - dh) / 2.0, dw, dh);
        } else {
            gc.setFill(Color.WHITE);
            gc.setTextAlign(TextAlignment.CENTER);
            gc.setFont(Font.font("Arial", FontWeight.BOLD, 26));
            gc.fillText("S", btn.x + btn.w / 2.0, btn.y + 42.0);
            gc.setTextAlign(TextAlignment.LEFT);
        }
    }

    private void startGame(Config.Difficulty difficulty) {
        GameState state = gsm.getState("PLAYING");
        if (state instanceof PlayingState playingState) {
            playingState.resetGame(difficulty);
        }
        gsm.setState("PLAYING");
    }

    private void drawDifficultyButton(GraphicsContext gc, ButtonRect rect, Config.Difficulty difficulty, double progress) {
        double eased = easeOut(progress);
        double expandedH = rect.h + EXPANDED_EXTRA_H * eased;
        Color fill = switch (difficulty) {
            case EASY -> Color.rgb(41, 120, 82, 0.88);
            case MEDIUM -> Color.rgb(42, 86, 145, 0.88);
            case HARD -> Color.rgb(150, 50, 46, 0.88);
        };

        gc.setFill(Color.rgb(0, 0, 0, 0.26 + 0.12 * eased));
        gc.fillRoundRect(rect.x + 12.0 * eased, rect.y + 12.0 * eased, rect.w, expandedH, 8.0, 8.0);

        gc.setFill(Color.rgb(
                (int) Math.round(255 * (1.0 - eased) + fill.getRed() * 255 * eased),
                (int) Math.round(255 * (1.0 - eased) + fill.getGreen() * 255 * eased),
                (int) Math.round(255 * (1.0 - eased) + fill.getBlue() * 255 * eased),
                0.10 + 0.78 * eased
        ));
        gc.fillRoundRect(rect.x, rect.y, rect.w, expandedH, 8.0, 8.0);

        if (eased > 0.03) {
            gc.setFill(Color.rgb(255, 255, 255, 0.12 * eased));
            gc.fillRoundRect(rect.x + 18.0, rect.y + 86.0, rect.w - 36.0, expandedH - 104.0, 8.0, 8.0);
        }

        gc.setStroke(Color.rgb(255, 255, 255, 0.85));
        gc.setLineWidth(3.0 + 1.0 * eased);
        gc.strokeRoundRect(rect.x, rect.y, rect.w, expandedH, 8.0, 8.0);

        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 42.0 + 4.0 * eased));
        gc.fillText(difficulty.label.toUpperCase(), rect.x + rect.w / 2.0, rect.y + 64.0);

        if (eased > 0.02) {
            drawDifficultyInfo(gc, rect, difficulty, eased);
        }
    }

    private void drawDifficultyInfo(GraphicsContext gc, ButtonRect rect, Config.Difficulty difficulty, double alpha) {
        String[] lines = switch (difficulty) {
            case EASY -> new String[] {
                    "Fewer obstacles and wider spawn gaps",
                    "More time before lane blocks appear",
                    "Best for learning items and movement"
            };
            case MEDIUM -> new String[] {
                    "Balanced obstacles and item chances",
                    "Normal speed growth and pressure",
                    "Recommended mode for a fair run"
            };
            case HARD -> new String[] {
                    "More obstacles with shorter gaps",
                    "Lane blocks appear much earlier",
                    "Fewer safety items and faster pressure"
            };
        };

        gc.setGlobalAlpha(alpha);
        gc.setTextAlign(TextAlignment.LEFT);
        gc.setFill(Color.rgb(255, 255, 255, 0.88));
        gc.setFont(Font.font("Arial", FontWeight.NORMAL, 26.0));
        double textX = rect.x + 72.0;
        double textY = rect.y + 120.0;
        for (int i = 0; i < lines.length; i++) {
            gc.fillText(lines[i], textX, textY + i * 36.0);
        }

        gc.setFill(Color.rgb(255, 255, 255, 0.46));
        for (int i = 0; i < lines.length; i++) {
            gc.fillOval(rect.x + 40.0, textY - 14.0 + i * 36.0, 10.0, 10.0);
        }
        gc.setGlobalAlpha(1.0);
        gc.setTextAlign(TextAlignment.CENTER);
    }

    private ButtonRect buttonRect(int index) {
        double y = DIFFICULTY_Y + index * DIFFICULTY_SPACING;
        for (int i = 0; i < index; i++) {
            y += EXPANDED_EXTRA_H * easeOut(difficultyHover[i]);
        }
        return new ButtonRect(DIFFICULTY_X, y, DIFFICULTY_W, DIFFICULTY_H);
    }

    private ButtonRect instructionButton() {
        double y = DIFFICULTY_Y + Config.Difficulty.values().length * DIFFICULTY_SPACING + 36.0;
        for (double progress : difficultyHover) {
            y += EXPANDED_EXTRA_H * easeOut(progress);
        }
        return new ButtonRect((Config.LOGICAL_WIDTH - 420.0) / 2.0, y, 420.0, 82.0);
    }

    private void drawInstructionButton(GraphicsContext gc) {
        ButtonRect rect = instructionButton();
        boolean hovered = rect.contains(input.getMouseX(), input.getMouseY());
        gc.setFill(hovered ? Color.rgb(255, 255, 255, 0.24) : Color.rgb(0, 0, 0, 0.24));
        gc.fillRoundRect(rect.x, rect.y, rect.w, rect.h, 8.0, 8.0);
        gc.setStroke(Color.rgb(255, 255, 255, hovered ? 0.95 : 0.72));
        gc.setLineWidth(3.0);
        gc.strokeRoundRect(rect.x, rect.y, rect.w, rect.h, 8.0, 8.0);

        gc.setFill(Color.WHITE);
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 34.0));
        gc.fillText("Instruction", rect.x + rect.w / 2.0, rect.y + 54.0);
    }

    private int hoveredDifficultyIndex() {
        double mx = input.getMouseX();
        double my = input.getMouseY();
        for (int i = 0; i < Config.Difficulty.values().length; i++) {
            if (buttonRect(i).contains(mx, my)) {
                return i;
            }
        }
        return -1;
    }

    private void updateDifficultyHover(double dt, int hoveredDifficulty) {
        double step = Math.min(1.0, Math.max(0.0, dt * HOVER_ANIMATION_SPEED));
        for (int i = 0; i < difficultyHover.length; i++) {
            double target = i == hoveredDifficulty ? 1.0 : 0.0;
            difficultyHover[i] += (target - difficultyHover[i]) * step;
            if (Math.abs(target - difficultyHover[i]) < 0.002) {
                difficultyHover[i] = target;
            }
        }
    }

    private double easeOut(double t) {
        double clamped = Math.max(0.0, Math.min(1.0, t));
        return 1.0 - Math.pow(1.0 - clamped, 3.0);
    }

    private record ButtonRect(double x, double y, double w, double h) {
        boolean contains(double px, double py) {
            return px >= x && px <= x + w && py >= y && py <= y + h;
        }
    }
}
