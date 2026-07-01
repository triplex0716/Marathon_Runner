package com.ycom.event;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;

public final class EventBus {
    private final Map<Class<? extends GameEvent>, List<EventListener<? extends GameEvent>>> listeners = new HashMap<>();
    private final Queue<GameEvent> queue = new ArrayDeque<>();

    public <T extends GameEvent> void subscribe(Class<T> eventType, EventListener<T> listener) {
        listeners.computeIfAbsent(eventType, ignored -> new ArrayList<>()).add(listener);
    }

    public void publish(GameEvent event) {
        queue.add(event);
    }

    public void dispatchQueuedEvents() {
        int count = queue.size();
        for (int i = 0; i < count; i++) {
            GameEvent event = queue.poll();
            dispatch(event);
        }
    }

    @SuppressWarnings("unchecked")
    private <T extends GameEvent> void dispatch(T event) {
        List<EventListener<? extends GameEvent>> typedListeners = listeners.get(event.getClass());
        if (typedListeners == null) {
            return;
        }
        for (EventListener<? extends GameEvent> listener : typedListeners) {
            ((EventListener<T>) listener).onEvent(event);
        }
    }
}
