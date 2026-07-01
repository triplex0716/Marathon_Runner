package com.ycom.system;

import com.ycom.core.Config;
import java.util.EnumSet;
import java.util.List;

public final class Scene {
    public final String name;
    public final EnumSet<Config.Difficulty> allowed;
    public final double unlockAfterSeconds;
    public final List<Chunk> chunks;

    public Scene(String name,
                 EnumSet<Config.Difficulty> allowed,
                 double unlockAfterSeconds,
                 List<Chunk> chunks) {
        this.name = name;
        this.allowed = allowed;
        this.unlockAfterSeconds = unlockAfterSeconds;
        this.chunks = chunks;
    }
}
