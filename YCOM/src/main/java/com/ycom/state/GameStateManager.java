package com.ycom.state;

import javafx.scene.canvas.Canvas;
import com.ycom.system.InputSystem;
import java.util.HashMap;
import java.util.Map;

public class GameStateManager {
    private Map<String, GameState> states = new HashMap<>();
    private GameState currentState;
    private Canvas canvas;
    private InputSystem input;
    
    public GameStateManager(Canvas canvas, InputSystem input) {
        this.canvas = canvas;
        this.input = input;
        
        states.put("LOGIN", new LoginState(this, canvas, input));
        states.put("MENU", new MainMenuState(this, canvas, input));
        states.put("INSTRUCTION", new InstructionState(this, canvas, input));
        states.put("SHOP", new ShopState(this, canvas, input));
        states.put("PLAYING", new PlayingState(this, canvas, input));
        states.put("GAMEOVER", new GameOverState(this, canvas, input));
        states.put("PAUSED", new PausedState(this, canvas, input));

        setState("LOGIN");
    }
    
    public void setState(String stateName) {
        if (currentState != null) currentState.onExit();
        currentState = states.get(stateName);
        if (currentState != null) currentState.onEnter();
    }
    
    public GameState getState(String stateName) {
        return states.get(stateName);
    }
    
    public void update(double dt) {
        if (currentState != null) currentState.update(dt);
    }
    
    public void render() {
        if (currentState != null) currentState.render();
    }
}
