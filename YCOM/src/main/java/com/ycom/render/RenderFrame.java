package com.ycom.render;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class RenderFrame {
    private static final long DEFAULT_FIXED_NANOS = 16_666_667L;

    private final RenderSnapshot previousPlayer;
    private final RenderSnapshot currentPlayer;
    private final List<RenderSnapshot> currentObjects;
    private final Map<Long, RenderSnapshot> previousObjectsById;
    private final long tickNanos;
    private final long fixedNanos;

    public RenderFrame(
            RenderSnapshot previousPlayer,
            RenderSnapshot currentPlayer,
            List<RenderSnapshot> previousObjects,
            List<RenderSnapshot> currentObjects,
            long tickNanos,
            long fixedNanos
    ) {
        this.previousPlayer = previousPlayer == null ? currentPlayer : previousPlayer;
        this.currentPlayer = currentPlayer;
        this.currentObjects = List.copyOf(currentObjects);
        this.previousObjectsById = indexById(previousObjects);
        this.tickNanos = tickNanos;
        this.fixedNanos = fixedNanos > 0L ? fixedNanos : DEFAULT_FIXED_NANOS;
    }

    public static RenderFrame initial(RenderSnapshot player) {
        return new RenderFrame(player, player, List.of(), List.of(), System.nanoTime(), DEFAULT_FIXED_NANOS);
    }

    public RenderSnapshot player(double alpha) {
        return currentPlayer.interpolateFrom(previousPlayer, alpha);
    }

    public List<RenderSnapshot> objects(double alpha) {
        List<RenderSnapshot> snapshots = new ArrayList<>(currentObjects.size());
        writeObjects(snapshots, alpha);
        return snapshots;
    }

    public void writeObjects(List<RenderSnapshot> snapshots, double alpha) {
        snapshots.clear();
        for (RenderSnapshot current : currentObjects) {
            snapshots.add(current.interpolateFrom(previousObjectsById.get(current.id()), alpha));
        }
    }

    public double alpha(long nowNanos) {
        return Math.max(0.0, Math.min(1.0, (nowNanos - tickNanos) / (double) fixedNanos));
    }

    public List<RenderSnapshot> currentObjects() {
        return currentObjects;
    }

    public Map<Long, RenderSnapshot> previousObjectsById() {
        return previousObjectsById;
    }

    private static Map<Long, RenderSnapshot> indexById(List<RenderSnapshot> snapshots) {
        Map<Long, RenderSnapshot> indexed = new HashMap<>();
        for (RenderSnapshot snapshot : snapshots) {
            indexed.put(snapshot.id(), snapshot);
        }
        return Map.copyOf(indexed);
    }
}
