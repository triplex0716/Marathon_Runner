package com.ycom.state;

import com.ycom.core.Config;
import com.ycom.resource.AssetManager;
import com.ycom.system.InputSystem;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;

public class InstructionState implements GameState {
    private static final double CARD_RADIUS = 8.0;

    private final GameStateManager gsm;
    private final Canvas canvas;
    private final InputSystem input;

    public InstructionState(GameStateManager gsm, Canvas canvas, InputSystem input) {
        this.gsm = gsm;
        this.canvas = canvas;
        this.input = input;
    }

    @Override
    public void onEnter() {}

    @Override
    public void onExit() {}

    @Override
    public void update(double dt) {
        if (input.isKeyJustPressed(KeyCode.ESCAPE)
                || input.isKeyJustPressed(KeyCode.Q)
                || (input.isMouseJustClicked() && backButton().contains(input.getMouseX(), input.getMouseY()))) {
            gsm.setState("MENU");
        }
    }

    @Override
    public void render() {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        drawBackground(gc);

        gc.setTextAlign(TextAlignment.CENTER);
        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 70.0));
        gc.fillText("INSTRUCTION", Config.LOGICAL_WIDTH / 2.0, 118.0);

        drawControls(gc);
        drawItems(gc);
        drawObstacles(gc);
        drawBackButton(gc);

        gc.setTextAlign(TextAlignment.LEFT);
    }

    private void drawBackground(GraphicsContext gc) {
        if (AssetManager.background() != null) {
            gc.drawImage(AssetManager.background(), 0, 0, Config.LOGICAL_WIDTH, Config.LOGICAL_HEIGHT);
            gc.setFill(Color.rgb(18, 38, 50, 0.72));
            gc.fillRect(0, 0, Config.LOGICAL_WIDTH, Config.LOGICAL_HEIGHT);
        } else {
            gc.setFill(Color.web("#101820"));
            gc.fillRect(0, 0, Config.LOGICAL_WIDTH, Config.LOGICAL_HEIGHT);
        }
    }

    private void drawControls(GraphicsContext gc) {
        double x = 96.0;
        double y = 176.0;
        double w = 470.0;
        double h = 684.0;
        drawPanel(gc, x, y, w, h, Color.rgb(14, 31, 42, 0.72));

        gc.setFill(Color.WHITE);
        gc.setTextAlign(TextAlignment.LEFT);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 36.0));
        gc.fillText("Controls", x + 36.0, y + 58.0);

        drawControlLine(gc, x + 36.0, y + 130.0, "A / Left", "Move left");
        drawControlLine(gc, x + 36.0, y + 206.0, "D / Right", "Move right");
        drawControlLine(gc, x + 36.0, y + 282.0, "W / Up", "Jump");
        drawControlLine(gc, x + 36.0, y + 358.0, "S / Down", "Slide");
        drawControlLine(gc, x + 36.0, y + 434.0, "Space", "Pause");
        drawControlLine(gc, x + 36.0, y + 510.0, "Esc / Q", "Back");

        gc.setFill(Color.rgb(255, 255, 255, 0.62));
        gc.setFont(Font.font("Arial", FontWeight.NORMAL, 24.0));
        gc.fillText("Collect items, dodge obstacles,", x + 36.0, y + 610.0);
        gc.fillText("and survive as long as you can.", x + 36.0, y + 646.0);
    }

    private void drawControlLine(GraphicsContext gc, double x, double y, String key, String action) {
        gc.setFill(Color.rgb(255, 255, 255, 0.12));
        gc.fillRoundRect(x, y - 42.0, 150.0, 54.0, CARD_RADIUS, CARD_RADIUS);
        gc.setStroke(Color.rgb(255, 255, 255, 0.42));
        gc.setLineWidth(2.0);
        gc.strokeRoundRect(x, y - 42.0, 150.0, 54.0, CARD_RADIUS, CARD_RADIUS);

        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 22.0));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText(key, x + 75.0, y - 8.0);

        gc.setTextAlign(TextAlignment.LEFT);
        gc.setFont(Font.font("Arial", FontWeight.NORMAL, 28.0));
        gc.fillText(action, x + 180.0, y - 6.0);
    }

    private void drawItems(GraphicsContext gc) {
        double x = 616.0;
        double y = 176.0;
        double w = 600.0;
        double h = 684.0;
        drawPanel(gc, x, y, w, h, Color.rgb(20, 42, 45, 0.70));

        gc.setFill(Color.WHITE);
        gc.setTextAlign(TextAlignment.LEFT);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 36.0));
        gc.fillText("Items", x + 36.0, y + 58.0);

        drawIconRow(gc, x + 36.0, y + 126.0, AssetManager.coinIcon(), "Coin", "+10 score");
        drawIconRow(gc, x + 36.0, y + 216.0, AssetManager.magnetIcon(), "Magnet", "Pulls nearby coins for 10s");
        drawIconRow(gc, x + 36.0, y + 306.0, AssetManager.spriteIcon(), "Energy Drink", "Boost speed and break obstacles");
        drawIconRow(gc, x + 36.0, y + 396.0, AssetManager.treadmillIcon(), "Book", "Doubles score for 10s");
        drawIconRow(gc, x + 36.0, y + 486.0, AssetManager.revivalIcon(), "Capsule", "Grants one revival chance");
        drawIconRow(gc, x + 36.0, y + 576.0, AssetManager.randomIcon(), "Random Box", "Gives a random power-up");
    }

    private void drawObstacles(GraphicsContext gc) {
        double x = 1266.0;
        double y = 176.0;
        double w = 558.0;
        double h = 684.0;
        drawPanel(gc, x, y, w, h, Color.rgb(45, 26, 32, 0.70));

        gc.setFill(Color.WHITE);
        gc.setTextAlign(TextAlignment.LEFT);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 36.0));
        gc.fillText("Obstacles", x + 36.0, y + 58.0);

        drawIconRow(gc, x + 36.0, y + 138.0, AssetManager.obstacleJumpIcon(), "Low Block", "Jump over it");
        drawIconRow(gc, x + 36.0, y + 270.0, AssetManager.obstacleSlideIcon(), "High Block", "Slide under it");
        drawIconRow(gc, x + 36.0, y + 402.0, AssetManager.obstacleTrainIcon(), "Train", "Change lane to avoid it");

        gc.setFill(Color.rgb(255, 255, 255, 0.62));
        gc.setFont(Font.font("Arial", FontWeight.NORMAL, 24.0));
        gc.fillText("Hard mode adds pressure earlier", x + 36.0, y + 564.0);
        gc.fillText("with more frequent lane threats.", x + 36.0, y + 600.0);
    }

    private void drawIconRow(GraphicsContext gc, double x, double y, Image image, String title, String body) {
        gc.setFill(Color.rgb(255, 255, 255, 0.11));
        gc.fillRoundRect(x, y - 54.0, 104.0, 104.0, CARD_RADIUS, CARD_RADIUS);
        gc.setStroke(Color.rgb(255, 255, 255, 0.32));
        gc.setLineWidth(2.0);
        gc.strokeRoundRect(x, y - 54.0, 104.0, 104.0, CARD_RADIUS, CARD_RADIUS);

        if (image != null && image.getWidth() > 0.0 && image.getHeight() > 0.0) {
            double iw = image.getWidth();
            double ih = image.getHeight();
            double size = 78.0;
            double scale = Math.min(size / iw, size / ih);
            double dw = iw * scale;
            double dh = ih * scale;
            gc.drawImage(image, x + 52.0 - dw / 2.0, y - dh / 2.0, dw, dh);
        }

        gc.setTextAlign(TextAlignment.LEFT);
        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 28.0));
        gc.fillText(title, x + 132.0, y - 12.0);
        gc.setFill(Color.rgb(255, 255, 255, 0.72));
        gc.setFont(Font.font("Arial", FontWeight.NORMAL, 24.0));
        gc.fillText(body, x + 132.0, y + 24.0);
    }

    private void drawPanel(GraphicsContext gc, double x, double y, double w, double h, Color color) {
        gc.setFill(color);
        gc.fillRoundRect(x, y, w, h, CARD_RADIUS, CARD_RADIUS);
        gc.setStroke(Color.rgb(255, 255, 255, 0.28));
        gc.setLineWidth(2.0);
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

    private ButtonRect backButton() {
        return new ButtonRect(80.0, 928.0, 180.0, 72.0);
    }

    private record ButtonRect(double x, double y, double w, double h) {
        boolean contains(double px, double py) {
            return px >= x && px <= x + w && py >= y && py <= y + h;
        }
    }
}
