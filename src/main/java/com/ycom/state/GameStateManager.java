package com.ycom.state;

import javafx.scene.canvas.Canvas;
import com.ycom.system.InputSystem;
import java.util.EnumMap;
import java.util.Map;

public class GameStateManager {
    private Map<StateId, GameState> states = new EnumMap<>(StateId.class);
    private volatile GameState currentState;
    private Canvas canvas;
    private InputSystem input;
    
    public GameStateManager(Canvas canvas, InputSystem input) {
        this.canvas = canvas;
        this.input = input;
        
        states.put(StateId.LOGIN, new LoginState(this, canvas, input));
        states.put(StateId.MENU, new MainMenuState(this, canvas, input));
        states.put(StateId.INSTRUCTION, new InstructionState(this, canvas, input));
        states.put(StateId.SHOP, new ShopState(this, canvas, input));
        states.put(StateId.PLAYING, new PlayingState(this, canvas, input));
        states.put(StateId.GAMEOVER, new GameOverState(this, canvas, input));
        states.put(StateId.PAUSED, new PausedState(this, canvas, input));

        setState(StateId.LOGIN);
    }
    
    public void setState(StateId stateId, Object payload) {
        if (currentState != null) currentState.onExit();
        currentState = states.get(stateId);
        if (currentState != null) currentState.onEnter(payload);
    }
    public void setState(StateId stateId) {
        if (currentState != null) currentState.onExit();
        currentState = states.get(stateId);
        if (currentState != null) currentState.onEnter();
    }
    
    public GameState getState(StateId stateId) {
        return states.get(stateId);
    }
    
    public GameState getCurrentState() {
        return currentState;
    }
    
    public void update(double dt) {
        if (currentState != null) currentState.update(dt);
    }
    
    public void render() {
        if (currentState != null) currentState.render();
    }
}
