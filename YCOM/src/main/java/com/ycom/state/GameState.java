package com.ycom.state;

public interface GameState {
    void onEnter();
    default void onEnter(Object payload) { onEnter(); }
    void update(double dt);
    void render();
    void onExit();
}
