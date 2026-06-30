package com.ycom.state;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import javafx.scene.input.KeyCode;
import com.ycom.core.Config;
import com.ycom.resource.AudioManager;
import com.ycom.system.InputSystem;

public class PausedState implements GameState {
    private final GameStateManager gsm;
    private final Canvas canvas;
    private final InputSystem input;

    public PausedState(GameStateManager gsm, Canvas canvas, InputSystem input) {
        this.gsm = gsm;
        this.canvas = canvas;
        this.input = input;
    }
    
    @Override public void onEnter() {}
    @Override public void onExit() {}

    @Override
    public void update(double dt) {
        if (input.isKeyJustPressed(KeyCode.SPACE) || input.isKeyJustPressed(KeyCode.ESCAPE)) {
            gsm.setState("PLAYING");
            return;
        }
        if (input.isKeyJustPressed(KeyCode.Q)) {
            AudioManager.stopBGM();
            gsm.setState("MENU");
            return;
        }

        if (input.isMouseJustClicked()) {
            if (btnResume().contains(input.getMouseX(), input.getMouseY())) {
                gsm.setState("PLAYING");
            } else if (btnQuit().contains(input.getMouseX(), input.getMouseY())) {
                AudioManager.stopBGM();
                gsm.setState("MENU");
            }
        }
    }

    @Override
    public void render() {
        GameState playing = gsm.getState("PLAYING");
        if (playing != null) {
            playing.render();
        }

        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.setFill(Color.rgb(0, 0, 0, 0.6));
        gc.fillRect(0, 0, Config.LOGICAL_WIDTH, Config.LOGICAL_HEIGHT);
        
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setStroke(UIUtils.BORDER);
        gc.setLineWidth(10.0);
        gc.setFont(Font.font("Arial Black", FontWeight.EXTRA_BOLD, 100.0));
        gc.strokeText("PAUSED", Config.LOGICAL_WIDTH / 2.0, Config.LOGICAL_HEIGHT / 2.0 - 50.0);
        gc.setFill(UIUtils.YELLOW);
        gc.fillText("PAUSED", Config.LOGICAL_WIDTH / 2.0, Config.LOGICAL_HEIGHT / 2.0 - 50.0);
        
        UIUtils.drawNeoButton(gc, input, btnResume());
        UIUtils.drawNeoButton(gc, input, btnQuit());

        gc.setTextAlign(TextAlignment.LEFT);
    }

    private UIUtils.NeoBtn btnResume() {
        double w = 300, h = 80;
        double x = Config.LOGICAL_WIDTH / 2.0 - w - 20;
        double y = Config.LOGICAL_HEIGHT / 2.0 + 50;
        return new UIUtils.NeoBtn(x, y, w, h, UIUtils.CYAN, "RESUME", 30);
    }

    private UIUtils.NeoBtn btnQuit() {
        double w = 300, h = 80;
        double x = Config.LOGICAL_WIDTH / 2.0 + 20;
        double y = Config.LOGICAL_HEIGHT / 2.0 + 50;
        return new UIUtils.NeoBtn(x, y, w, h, UIUtils.PINK, "QUIT", 30);
    }
}
