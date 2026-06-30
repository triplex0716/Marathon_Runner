package com.ycom.system;

public final class EntitySpec {
    public final double relZ;
    public final int lane;
    public final double y;
    public final EntityType type;

    public EntitySpec(double relZ, int lane, EntityType type) {
        this(relZ, lane, 0.0, type);
    }

    public EntitySpec(double relZ, int lane, double y, EntityType type) {
        this.relZ = relZ;
        this.lane = lane;
        this.y = y;
        this.type = type;
    }
}
