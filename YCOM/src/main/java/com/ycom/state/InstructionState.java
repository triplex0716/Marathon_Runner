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
    private final GameStateManager gsm;
    private final Canvas canvas;
    private final InputSystem input;
    private double time = 0.0;

    public InstructionState(GameStateManager gsm, Canvas canvas, InputSystem input) {
        this.gsm = gsm;
        this.canvas = canvas;
        this.input = input;
    }

    @Override
    public void onEnter() {
        time = 0.0;
    }

    @Override
    public void onExit() {}

    @Override
    public void update(double dt) {
        time += dt;
        if (input.isKeyJustPressed(KeyCode.ESCAPE)
                || input.isKeyJustPressed(KeyCode.Q)
                || (input.isMouseJustClicked() && btnBack().contains(input.getMouseX(), input.getMouseY()))) {
            gsm.setState("MENU");
        }
    }

    @Override
    public void render() {
        GraphicsContext gc = canvas.getGraphicsContext2D();

        UIUtils.drawBackgroundAndTrack(gc, time);

        gc.setTextAlign(TextAlignment.CENTER);
        gc.setStroke(UIUtils.BORDER);
        gc.setLineWidth(10.0);
        gc.setFont(Font.font("Arial Black", FontWeight.EXTRA_BOLD, 70.0));
        gc.strokeText("HOW TO PLAY", Config.LOGICAL_WIDTH / 2.0, 100.0);
        gc.setFill(UIUtils.YELLOW);
        gc.fillText("HOW TO PLAY", Config.LOGICAL_WIDTH / 2.0, 100.0);

        drawControls(gc);
        drawItems(gc);
        drawObstacles(gc);
        
        UIUtils.drawNeoButton(gc, input, btnBack());

        gc.setTextAlign(TextAlignment.LEFT);
    }

    private void drawControls(GraphicsContext gc) {
        double x = 80, y = 140, w = 500, h = 700;
        drawNeoPanel(gc, x, y, w, h, UIUtils.CYAN);

        gc.setFill(UIUtils.BORDER);
        gc.setTextAlign(TextAlignment.LEFT);
        gc.setFont(Font.font("Arial Black", FontWeight.BOLD, 36.0));
        gc.fillText("CONTROLS", x + 30.0, y + 50.0);

        drawControlLine(gc, x + 30.0, y + 120.0, "A / Left", "Move left");
        drawControlLine(gc, x + 30.0, y + 200.0, "D / Right", "Move right");
        drawControlLine(gc, x + 30.0, y + 280.0, "W / Up", "Jump");
        drawControlLine(gc, x + 30.0, y + 360.0, "S / Down", "Slide");
        drawControlLine(gc, x + 30.0, y + 440.0, "Space", "Pause");
        drawControlLine(gc, x + 30.0, y + 520.0, "Esc / Q", "Back");
    }

    private void drawControlLine(GraphicsContext gc, double x, double y, String key, String action) {
        gc.setFill(UIUtils.BORDER);
        gc.fillRect(x + 4, y - 40 + 4, 150, 54);
        gc.setFill(UIUtils.WHITE);
        gc.fillRect(x, y - 40, 150, 54);
        gc.setStroke(UIUtils.BORDER);
        gc.setLineWidth(3.0);
        gc.strokeRect(x, y - 40, 150, 54);

        gc.setFill(UIUtils.BORDER);
        gc.setFont(Font.font("Arial Black", FontWeight.BOLD, 22.0));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText(key, x + 75.0, y - 4.0);

        gc.setTextAlign(TextAlignment.LEFT);
        gc.setFont(Font.font("Arial Black", FontWeight.NORMAL, 28.0));
        gc.fillText(action, x + 170.0, y - 2.0);
    }

    private void drawItems(GraphicsContext gc) {
        double x = 620, y = 140, w = 620, h = 700;
        drawNeoPanel(gc, x, y, w, h, UIUtils.PINK);

        gc.setFill(UIUtils.BORDER);
        gc.setTextAlign(TextAlignment.LEFT);
        gc.setFont(Font.font("Arial Black", FontWeight.BOLD, 36.0));
        gc.fillText("ITEMS", x + 30.0, y + 50.0);

        drawIconRow(gc, x + 30.0, y + 130.0, AssetManager.coinIcon(), "Coin", "+10 score");
        drawIconRow(gc, x + 30.0, y + 230.0, AssetManager.magnetIcon(), "Magnet", "Pulls nearby coins");
        drawIconRow(gc, x + 30.0, y + 330.0, AssetManager.spriteIcon(), "Energy Drink", "Boost speed / smash");
        drawIconRow(gc, x + 30.0, y + 430.0, AssetManager.treadmillIcon(), "Book", "Double score");
        drawIconRow(gc, x + 30.0, y + 530.0, AssetManager.revivalIcon(), "Capsule", "Revive once");
        drawIconRow(gc, x + 30.0, y + 630.0, AssetManager.randomIcon(), "Random Box", "Random power-up");
    }

    private void drawObstacles(GraphicsContext gc) {
        double x = 1280, y = 140, w = 550, h = 700;
        drawNeoPanel(gc, x, y, w, h, UIUtils.YELLOW);

        gc.setFill(UIUtils.BORDER);
        gc.setTextAlign(TextAlignment.LEFT);
        gc.setFont(Font.font("Arial Black", FontWeight.BOLD, 36.0));
        gc.fillText("OBSTACLES", x + 30.0, y + 50.0);

        drawIconRow(gc, x + 30.0, y + 160.0, AssetManager.obstacleJumpIcon(), "Low Block", "Jump over it");
        drawIconRow(gc, x + 30.0, y + 320.0, AssetManager.obstacleSlideIcon(), "High Block", "Slide under it");
        drawIconRow(gc, x + 30.0, y + 480.0, AssetManager.obstacleTrainIcon(), "Train", "Dodge it");
    }

    private void drawIconRow(GraphicsContext gc, double x, double y, Image image, String title, String body) {
        gc.setFill(UIUtils.BORDER);
        gc.fillRect(x + 4, y - 50 + 4, 80, 80);
        gc.setFill(UIUtils.WHITE);
        gc.fillRect(x, y - 50, 80, 80);
        gc.setStroke(UIUtils.BORDER);
        gc.setLineWidth(3.0);
        gc.strokeRect(x, y - 50, 80, 80);

        if (image != null && image.getWidth() > 0.0 && image.getHeight() > 0.0) {
            double iw = image.getWidth();
            double ih = image.getHeight();
            double size = 60.0;
            double scale = Math.min(size / iw, size / ih);
            double dw = iw * scale;
            double dh = ih * scale;
            gc.drawImage(image, x + 40.0 - dw / 2.0, y - 10 - dh / 2.0, dw, dh);
        }

        gc.setTextAlign(TextAlignment.LEFT);
        gc.setFill(UIUtils.BORDER);
        gc.setFont(Font.font("Arial Black", FontWeight.BOLD, 26.0));
        gc.fillText(title, x + 100.0, y - 12.0);
        gc.setFont(Font.font("Arial Black", FontWeight.NORMAL, 20.0));
        gc.fillText(body, x + 100.0, y + 16.0);
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

    private UIUtils.NeoBtn btnBack() {
        return new UIUtils.NeoBtn(80.0, 880.0, 240.0, 72.0, UIUtils.WHITE, "BACK", 30);
    }
}
