package com.ycom.entity;

import com.ycom.event.EventBus;
import com.ycom.system.InputSystem;

public final class EntityUpdateContext {
    private final InputSystem input;
    private final EventBus eventBus;
    private final Player player;
    private final double fixedDt;
    private final com.ycom.world.GameWorld world;

    public EntityUpdateContext(InputSystem input, EventBus eventBus, Player player, double fixedDt, com.ycom.world.GameWorld world) {
        this.input = input;
        this.eventBus = eventBus;
        this.player = player;
        this.fixedDt = fixedDt;
        this.world = world;
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

    public com.ycom.world.GameWorld world() {
        return world;
    }
}
