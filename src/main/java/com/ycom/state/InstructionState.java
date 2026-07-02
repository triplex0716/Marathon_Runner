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
import javafx.scene.text.Text;
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
            gsm.setState(StateId.MENU);
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
        gc.setFont(Font.font("Arial Black", FontWeight.BOLD, 32.0));
        gc.fillText("STORY", x + 30.0, y + 50.0);

        Font bodyFont = Font.font("Arial Black", FontWeight.NORMAL, 17.0);
        String story = "You are a runner trapped on a neon city track, chased by pressure, speed, and whatever waits beyond the next stretch of road. Streets, trains, barricades, and power-ups rush toward you without warning, so survival depends on rhythm, quick lane choices, and staying calm as the run keeps getting faster.";
        String howToPlay = "Use A/Left and D/Right to move between the three lanes.\n"
                + "Press W/Up to jump over low obstacles,\n"
                + "press S/Down to slide under high obstacles,\n"
                + "press Space to pause, and press Esc or Q to return to the menu.\n"
                + "Collect coins to raise your score and grab useful items whenever you can. Your goal is to collect as many coins as possible and reach the goal!";

        double textX = x + 30.0;
        double textW = w - 60.0;
        double nextY = drawWrappedParagraph(gc, story, textX, y + 88.0, textW, bodyFont, 27.0);

        gc.setFont(Font.font("Arial Black", FontWeight.BOLD, 32.0));
        gc.setFill(UIUtils.BORDER);
        gc.fillText("HOW TO PLAY", x + 30.0, nextY + 44.0);

        drawWrappedParagraph(gc, howToPlay, textX, nextY + 78.0, textW, bodyFont, 27.0);
    }

    private double drawWrappedParagraph(GraphicsContext gc, String paragraph, double x, double y,
                                        double maxWidth, Font font, double lineHeight) {
        gc.setFill(UIUtils.BORDER);
        gc.setFont(font);
        gc.setTextAlign(TextAlignment.LEFT);

        double drawY = y;
        String[] explicitLines = paragraph.split("\\R", -1);
        for (String explicitLine : explicitLines) {
            StringBuilder line = new StringBuilder();
            for (int i = 0; i < explicitLine.length(); i++) {
                char ch = explicitLine.charAt(i);
                String candidate = line.toString() + ch;
                if (line.length() > 0 && textWidth(candidate, font) > maxWidth) {
                    gc.fillText(line.toString().stripTrailing(), x, drawY);
                    drawY += lineHeight;
                    line.setLength(0);
                    if (!Character.isWhitespace(ch)) {
                        line.append(ch);
                    }
                } else {
                    line.append(ch);
                }
            }

            if (line.length() > 0) {
                gc.fillText(line.toString().stripTrailing(), x, drawY);
            }
            drawY += lineHeight;
        }
        return drawY;
    }

    private double textWidth(String text, Font font) {
        Text helper = new Text(text);
        helper.setFont(font);
        return helper.getLayoutBounds().getWidth();
    }

    private void drawItems(GraphicsContext gc) {
        double x = 620, y = 140, w = 620, h = 700;
        drawNeoPanel(gc, x, y, w, h, UIUtils.PINK);

        gc.setFill(UIUtils.BORDER);
        gc.setTextAlign(TextAlignment.LEFT);
        gc.setFont(Font.font("Arial Black", FontWeight.BOLD, 36.0));
        gc.fillText("ITEMS", x + 30.0, y + 50.0);

        drawIconRow(gc, x + 30.0, y + 130.0, "coin", "Coin", "+10 score");
        drawIconRow(gc, x + 30.0, y + 230.0, "magnet", "Magnet", "Pulls nearby coins");
        drawIconRow(gc, x + 30.0, y + 330.0, "sprite", "Energy Drink", "Boost speed / smash");
        drawIconRow(gc, x + 30.0, y + 430.0, "treadmill", "Book", "Double score");
        drawIconRow(gc, x + 30.0, y + 530.0, "revival", "Capsule", "Revive once");
        drawIconRow(gc, x + 30.0, y + 630.0, "random", "Random Box", "Random power-up");
    }

    private void drawObstacles(GraphicsContext gc) {
        double x = 1280, y = 140, w = 550, h = 700;
        drawNeoPanel(gc, x, y, w, h, UIUtils.YELLOW);

        gc.setFill(UIUtils.BORDER);
        gc.setTextAlign(TextAlignment.LEFT);
        gc.setFont(Font.font("Arial Black", FontWeight.BOLD, 36.0));
        gc.fillText("OBSTACLES", x + 30.0, y + 50.0);

        drawIconRow(gc, x + 30.0, y + 160.0, "obstacle_jump", "Low Block", "Jump over it");
        drawIconRow(gc, x + 30.0, y + 320.0, "obstacle_slide", "High Block", "Slide under it");
        drawIconRow(gc, x + 30.0, y + 480.0, "obstacle_train", "Train", "Dodge it");
    }

    private void drawIconRow(GraphicsContext gc, double x, double y, String image, String title, String body) {
        gc.setFill(UIUtils.BORDER);
        gc.fillRect(x + 4, y - 50 + 4, 80, 80);
        gc.setFill(UIUtils.WHITE);
        gc.fillRect(x, y - 50, 80, 80);
        gc.setStroke(UIUtils.BORDER);
        gc.setLineWidth(3.0);
        gc.strokeRect(x, y - 50, 80, 80);

        if (com.ycom.resource.AssetManager.exists(image)) {
            com.ycom.resource.TextureRegion r = com.ycom.resource.AssetManager.getRegion(image);
            double aspect = r != null ? r.sw() / r.sh() : 1.0;
            double dh = 60.0;
            double dw = dh * aspect;
            com.ycom.resource.AssetManager.draw(gc, image, x + 40.0 - dw / 2.0, y - 10 - dh / 2.0, dw, dh);
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
