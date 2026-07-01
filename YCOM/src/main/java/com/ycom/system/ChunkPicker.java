package com.ycom.system;

import com.ycom.core.Config;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Random;

public class ChunkPicker {
    private static final int COOLDOWN_WINDOW = 2;
    private static final double LATE_GAME_THRESHOLD_SECONDS = 30.0;
    private static final int MIXED_SAFE_ROW_LATE_WEIGHT = 15;

    private final Config.Difficulty difficulty;
    private final List<Chunk> pool;
    private final Random rand = new Random();
    private final Deque<Chunk> recent = new ArrayDeque<>();

    public ChunkPicker(Config.Difficulty difficulty, List<Chunk> pool) {
        this.difficulty = difficulty == null ? Config.DEFAULT_DIFFICULTY : difficulty;
        this.pool = pool;
    }

    public Chunk pick(double elapsedSeconds) {
        List<Chunk> avail = new ArrayList<>();
        List<Integer> weights = new ArrayList<>();
        int totalWeight = 0;
        for (Chunk c : pool) {
            if (!c.allowed.contains(difficulty)) continue;
            if (elapsedSeconds < c.unlockAfterSeconds) continue;
            if (recent.contains(c)) continue;
            int w = effectiveWeight(c, elapsedSeconds);
            if (w <= 0) continue;
            avail.add(c);
            weights.add(w);
            totalWeight += w;
        }

        if (avail.isEmpty()) {
            Chunk fallback = pool.get(0);
            remember(fallback);
            return fallback;
        }

        int r = rand.nextInt(totalWeight);
        for (int i = 0; i < avail.size(); i++) {
            r -= weights.get(i);
            if (r < 0) {
                Chunk picked = avail.get(i);
                remember(picked);
                return picked;
            }
        }
        Chunk fallback = avail.get(avail.size() - 1);
        remember(fallback);
        return fallback;
    }

    private int effectiveWeight(Chunk c, double elapsedSeconds) {
        if (c == ChunkLibrary.MIXED_SAFE_ROW && elapsedSeconds > LATE_GAME_THRESHOLD_SECONDS) {
            return MIXED_SAFE_ROW_LATE_WEIGHT;
        }
        return c.weight;
    }

    private void remember(Chunk c) {
        recent.addLast(c);
        while (recent.size() > COOLDOWN_WINDOW) {
            recent.removeFirst();
        }
    }
}
