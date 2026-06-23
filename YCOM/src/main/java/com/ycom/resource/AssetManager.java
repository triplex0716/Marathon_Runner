package com.ycom.resource;

import javafx.scene.image.Image;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class AssetManager {
    private static final Map<String, Image> IMAGES = new HashMap<>();
    private static final int RUN_FRAME_COUNT = 4;

    public static void init() {
        IMAGES.clear();
        loadImage("background", "T-Pose.jpg");
        loadImage("player", "zxf.png");
        loadImage("run", "run.jpg");
        loadImage("magnet", "magnet.png");
        loadImage("sprite", "energy_drink.png");
        loadImage("revival", "revival.png");
        loadImage("treadmill", "book.png");
        loadImage("random", "random.png");
        loadImage("coin","coin.png");
    }

    public static Image getImage(String key) {
        return IMAGES.get(key);
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

    public static int runFrameCount() {
        return RUN_FRAME_COUNT;
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
