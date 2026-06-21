package com.ycom.event;

@FunctionalInterface
public interface EventListener<T extends GameEvent> {
    void onEvent(T event);
}
