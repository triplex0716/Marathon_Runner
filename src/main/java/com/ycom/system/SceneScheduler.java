package com.ycom.system;

import com.ycom.core.Config;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class SceneScheduler {
    private static final List<Chunk> FILLER_POOL = List.of(
            ChunkLibrary.MAGNET_PICKUP,
            ChunkLibrary.ENERGY_DRINK_PICKUP,
            ChunkLibrary.MIXED_SAFE_ROW,
            ChunkLibrary.SINGLE_LANE_COIN_STRING,
            ChunkLibrary.JUMP_COIN_REWARD,
            ChunkLibrary.SLIDE_COIN_REWARD,
            ChunkLibrary.COIN_CURVE,
            ChunkLibrary.CONTAINER_CLIMB,
            ChunkLibrary.TRAIN_CENTER_DOUBLE_JUMP
    );

    private static final List<Chunk> POWERUP_CHUNKS = List.of(
            ChunkLibrary.ENERGY_DRINK_PICKUP,
            ChunkLibrary.MAGNET_PICKUP,
            ChunkLibrary.TREADMILL_PICKUP,
            ChunkLibrary.REVIVAL_PICKUP,
            ChunkLibrary.RANDOM_BOX_PICKUP
    );

    private final Config.Difficulty difficulty;
    private final ChunkPicker fillerPicker;
    private final ChunkPicker powerupPicker;
    private final Random rand = new Random();
    private final List<Scene> cycleQueue = new ArrayList<>();

    private Scene currentScene = null;
    private List<Chunk> currentSceneChunks = null;
    private int chunkInSceneIdx = 0;
    private int fillerRemaining = 0;
    private boolean tutorialDone = false;
    private double lastPowerupElapsedSeconds = 0.0;

    public SceneScheduler(Config.Difficulty difficulty) {
        this.difficulty = difficulty == null ? Config.DEFAULT_DIFFICULTY : difficulty;
        this.fillerPicker = new ChunkPicker(this.difficulty, FILLER_POOL);
        this.powerupPicker = new ChunkPicker(this.difficulty, POWERUP_CHUNKS);
    }

    public Chunk next(double elapsedSeconds) {
        if (elapsedSeconds - lastPowerupElapsedSeconds > Config.MAX_POWERUP_GAP_SECONDS) {
            Chunk p = powerupPicker.pick(elapsedSeconds);
            lastPowerupElapsedSeconds = elapsedSeconds;
            return p;
        }
        Chunk c = nextNormal(elapsedSeconds);
        if (c.isPowerup) {
            lastPowerupElapsedSeconds = elapsedSeconds;
        }
        return c;
    }

    private Chunk nextNormal(double elapsedSeconds) {
        if (currentScene != null) {
            Chunk c = currentSceneChunks.get(chunkInSceneIdx);
            chunkInSceneIdx++;
            if (chunkInSceneIdx >= currentSceneChunks.size()) {
                currentScene = null;
                currentSceneChunks = null;
                fillerRemaining = Config.FILLER_CHUNKS_PER_SCENE_BLOCK;
            }
            return c;
        }

        if (fillerRemaining > 0) {
            fillerRemaining--;
            return fillerPicker.pick(elapsedSeconds);
        }

        Scene nextScene = pickNextScene(elapsedSeconds);
        if (nextScene == null) {
            return fillerPicker.pick(elapsedSeconds);
        }
        currentScene = nextScene;
        if (nextScene == SceneLibrary.TUTORIAL_START) {
            currentSceneChunks = nextScene.chunks;
        } else {
            currentSceneChunks = new ArrayList<>(nextScene.chunks);
            Collections.shuffle(currentSceneChunks, rand);
        }
        chunkInSceneIdx = 0;
        return nextNormal(elapsedSeconds);
    }

    private Scene pickNextScene(double elapsedSeconds) {
        if (!tutorialDone) {
            tutorialDone = true;
            return SceneLibrary.TUTORIAL_START;
        }
        if (cycleQueue.isEmpty()) {
            List<Scene> eligible = new ArrayList<>();
            for (Scene s : SceneLibrary.ALL) {
                if (s == SceneLibrary.TUTORIAL_START) continue;
                if (!s.allowed.contains(difficulty)) continue;
                if (elapsedSeconds < s.unlockAfterSeconds) continue;
                eligible.add(s);
            }
            if (eligible.isEmpty()) {
                return null;
            }
            Collections.shuffle(eligible, rand);
            cycleQueue.addAll(eligible);
        }
        return cycleQueue.remove(0);
    }
}
