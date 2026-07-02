package com.ycom.render;

import java.util.ArrayList;
import java.util.List;

public final class RenderFrame {
    private static final long DEFAULT_FIXED_NANOS = 16_666_667L;

    private final RenderSnapshot previousPlayer;
    private final RenderSnapshot currentPlayer;
    private final List<RenderSnapshot> currentObjects;
    private final List<RenderSnapshot> previousObjects;
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
        this.currentObjects = currentObjects;
        this.previousObjects = previousObjects;
        this.tickNanos = tickNanos;
        this.fixedNanos = fixedNanos > 0L ? fixedNanos : DEFAULT_FIXED_NANOS;
    }

    public static RenderFrame initial(RenderSnapshot player) {
        return new RenderFrame(player, player, List.of(), List.of(), System.nanoTime(), DEFAULT_FIXED_NANOS);
    }

    public RenderSnapshot currentPlayer() {
        return currentPlayer;
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
            snapshots.add(current.interpolateFrom(findPrevious(current.id()), alpha));
        }
    }

    public double alpha(long nowNanos) {
        return Math.max(0.0, Math.min(1.0, (nowNanos - tickNanos) / (double) fixedNanos));
    }

    public List<RenderSnapshot> currentObjects() {
        return currentObjects;
    }

    private RenderSnapshot findPrevious(long id) {
        if (previousObjects == null || previousObjects.isEmpty()) return null;
        int low = 0;
        int high = previousObjects.size() - 1;

        while (low <= high) {
            int mid = (low + high) >>> 1;
            RenderSnapshot midVal = previousObjects.get(mid);
            long midId = midVal.id();

            if (midId < id)
                low = mid + 1;
            else if (midId > id)
                high = mid - 1;
            else
                return midVal;
        }
        return null;
    }
}
