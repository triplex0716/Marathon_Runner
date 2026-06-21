package com.ycom.state;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import javafx.scene.input.KeyCode;
import com.ycom.core.Config;
import com.ycom.resource.AssetManager;
import com.ycom.system.InputSystem;

public class GameOverState implements GameState {
    private final GameStateManager gsm;
    private final Canvas canvas;
    private final InputSystem input;
    public static int finalScore = 0;

    public GameOverState(GameStateManager gsm, Canvas canvas, InputSystem input) {
        this.gsm = gsm;
        this.canvas = canvas;
        this.input = input;
    }
    
    @Override public void onEnter() {}
    @Override public void onExit() {}

    @Override
    public void update(double dt) {
        if (input.isKeyJustPressed(KeyCode.SPACE)
                || input.isKeyJustPressed(KeyCode.ENTER)
                || input.isKeyJustPressed(KeyCode.Q)
                || (input.isMouseJustClicked() && menuButton().contains(input.getMouseX(), input.getMouseY()))) {
            gsm.setState("MENU");
        }
    }

    @Override
    public void render() {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        if (AssetManager.background() != null) {
            gc.drawImage(AssetManager.background(), 0, 0, Config.LOGICAL_WIDTH, Config.LOGICAL_HEIGHT);
        }
        gc.setFill(Color.rgb(60, 0, 0, 0.78));
        gc.fillRect(0, 0, Config.LOGICAL_WIDTH, Config.LOGICAL_HEIGHT);

        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 96));
        gc.fillText("GAME OVER", Config.LOGICAL_WIDTH/2 - 300, Config.LOGICAL_HEIGHT/2 - 100);

        gc.setFont(new Font("Arial", 60));
        gc.fillText("Score: " + finalScore, Config.LOGICAL_WIDTH/2 - 150, Config.LOGICAL_HEIGHT/2 + 50);

        ButtonRect button = menuButton();
        gc.setFill(Color.rgb(255, 255, 255, 0.16));
        gc.fillRoundRect(button.x, button.y, button.w, button.h, 8.0, 8.0);
        gc.setStroke(Color.WHITE);
        gc.setLineWidth(3.0);
        gc.strokeRoundRect(button.x, button.y, button.w, button.h, 8.0, 8.0);

        gc.setFill(Color.WHITE);
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 38));
        gc.fillText("Back to Menu", button.x + button.w / 2.0, button.y + 66.0);
        gc.setTextAlign(TextAlignment.LEFT);

        gc.setFont(new Font("Arial", 28));
        gc.fillText("Choose a difficulty again from the menu", Config.LOGICAL_WIDTH/2 - 245, Config.LOGICAL_HEIGHT/2 + 280);
    }

    private ButtonRect menuButton() {
        double w = 440.0;
        double h = 104.0;
        return new ButtonRect((Config.LOGICAL_WIDTH - w) / 2.0, Config.LOGICAL_HEIGHT / 2.0 + 150.0, w, h);
    }

    private record ButtonRect(double x, double y, double w, double h) {
        boolean contains(double px, double py) {
            return px >= x && px <= x + w && py >= y && py <= y + h;
        }
    }
}
