package com.ycom.system;

import com.ycom.core.Config;
import java.util.EnumSet;
import java.util.List;

public final class Chunk {
    public final String name;
    public final EnumSet<Config.Difficulty> allowed;
    public final double length;
    public final int weight;
    public final double unlockAfterSeconds;
    public final List<EntitySpec> entities;
    public final boolean laneShiftable;
    public final boolean mirrorable;
    public final boolean isPowerup;

    public Chunk(String name,
                 EnumSet<Config.Difficulty> allowed,
                 double length,
                 int weight,
                 double unlockAfterSeconds,
                 List<EntitySpec> entities,
                 boolean laneShiftable,
                 boolean mirrorable,
                 boolean isPowerup) {
        this.name = name;
        this.allowed = allowed;
        this.length = length;
        this.weight = weight;
        this.unlockAfterSeconds = unlockAfterSeconds;
        this.entities = entities;
        this.laneShiftable = laneShiftable;
        this.mirrorable = mirrorable;
        this.isPowerup = isPowerup;
    }
}
