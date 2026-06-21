package com.ycom.entity;

import com.ycom.event.EventBus;
import com.ycom.system.InputSystem;

public final class EntityUpdateContext {
    private final InputSystem input;
    private final EventBus eventBus;
    private final Player player;
    private final double fixedDt;

    public EntityUpdateContext(InputSystem input, EventBus eventBus, Player player, double fixedDt) {
        this.input = input;
        this.eventBus = eventBus;
        this.player = player;
        this.fixedDt = fixedDt;
    }

    public InputSystem input() {
        return input;
    }

    public EventBus eventBus() {
        return eventBus;
    }

    public Player player() {
        return player;
    }

    public double fixedDt() {
        return fixedDt;
    }
}
