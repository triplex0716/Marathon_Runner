package com.ycom.state;

public interface GameState {
    void onEnter();
    void update(double dt);
    void render();
    void onExit();
}
