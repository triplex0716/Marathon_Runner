package com.ycom.resource;

import javafx.scene.image.Image;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class AssetManager {
    private static final Map<String, Image> IMAGES = new HashMap<>();
    private static final Map<String, Integer> FRAME_COUNTS = new HashMap<>();

    public static void init() {
        IMAGES.clear();
        FRAME_COUNTS.clear();
        loadImage("background", "T-Pose.jpg");
        loadImage("player", "zxf.png");
        loadSprite("run", "run.png", 8);
        loadSprite("jump", "jump.png", 8);
        loadSprite("boost", "boost.png", 8);
        loadSprite("slide", "slide.png", 8);
        loadImage("magnet", "magnet.png");
        loadImage("sprite", "energy_drink.png");
        loadImage("revival", "capsule.png");
        loadImage("treadmill", "book.png");
        loadImage("random", "random_box.png");
        loadImage("coin","coin.png");
        loadImage("obstacle_slide", "obstacle_slide.png");
        loadImage("obstacle_jump", "obstacle_jump.png");
        loadImage("obstacle_train", "obstacle_train.png");
    }

    private static void loadSprite(String key, String fileName, int frameCount) {
        loadImage(key, fileName);
        if (IMAGES.containsKey(key)) {
            FRAME_COUNTS.put(key, frameCount);
        }
    }

    public static Image getImage(String key) {
        return IMAGES.get(key);
    }

    public static int frameCount(String key) {
        return FRAME_COUNTS.getOrDefault(key, 1);
    }

    public static Image background() {
        return getImage("background");
    }

    public static Image playerImage() {
        return getImage("player");
    }

    public static Image runSheet() {
        return getImage("run");
    }

    public static Image jumpSheet() {
        return getImage("jump");
    }

    public static Image boostSheet() {
        return getImage("boost");
    }

    public static Image slideSheet() {
        return getImage("slide");
    }

    public static Image magnetIcon() {
        return getImage("magnet");
    }

    public static Image spriteIcon() {
        return getImage("sprite");
    }

    public static Image revivalIcon() {
        return getImage("revival");
    }

    public static Image treadmillIcon() {
        return getImage("treadmill");
    }

    public static Image randomIcon() {
        return getImage("random");
    }

    public static Image coinIcon() {
        return getImage("coin");
    }

    public static Image obstacleSlideIcon() {
        return getImage("obstacle_slide");
    }

    public static Image obstacleJumpIcon() {
        return getImage("obstacle_jump");
    }

    public static Image obstacleTrainIcon() {
        return getImage("obstacle_train");
    }


    private static void loadImage(String key, String fileName) {
        Path path = resolve(fileName);
        if (path == null) {
            System.err.println("Missing image asset: " + fileName);
            return;
        }
        IMAGES.put(key, new Image(path.toUri().toString(), false));
    }

    public static Path resolve(String fileName) {
        Path cwd = Path.of("").toAbsolutePath();
        Path[] candidates = new Path[] {
                cwd.resolve(fileName),
                cwd.resolve("..").resolve(fileName),
                cwd.resolve("Audio").resolve(fileName),
                cwd.resolve("..").resolve("Audio").resolve(fileName)
        };
        for (Path candidate : candidates) {
            Path normalized = candidate.normalize();
            if (Files.exists(normalized)) {
                return normalized;
            }
        }
        return null;
    }
}
