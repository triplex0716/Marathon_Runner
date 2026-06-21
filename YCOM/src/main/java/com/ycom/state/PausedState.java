package com.ycom.state;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
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
        }
        if (input.isKeyJustPressed(KeyCode.Q)) {
            AudioManager.stopBGM();
            gsm.setState("MENU");
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
        
        gc.setFill(Color.WHITE);
        gc.setFont(new Font("Arial", 100));
        gc.fillText("PAUSED", Config.LOGICAL_WIDTH/2 - 200, Config.LOGICAL_HEIGHT/2);
        
        gc.setFont(new Font("Arial", 35));
        gc.fillText("Press SPACE to Resume | Q to Quit", Config.LOGICAL_WIDTH/2 - 280, Config.LOGICAL_HEIGHT/2 + 100);
    }
}
