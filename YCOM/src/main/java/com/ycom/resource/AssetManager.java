package com.ycom.resource;

import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
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
        loadImage("background", "ukiyoe_bg.jpg");
        loadImage("player", "zxf.png");
        loadSprite("run", "run.png", 8);
        loadSprite("jump", "jump.png", 8);
        loadSprite("boost", "boost.png", 8);
        loadSprite("slide", "slide.png", 8);
        loadImage("magnet", "magnet.png");
        loadImage("ramp_slope", "ramp_slope.png");
        loadImage("ramp_side", "ramp_side.png");
        loadImage("sprite", "energy_drink.png");
        loadImage("revival", "capsule.png");
        loadImage("treadmill", "book.png");
        loadImage("random", "random_box.png");
        loadImage("coin","coin.png");
        loadImage("obstacle_slide", "obstacle_slide.png");
        loadImage("obstacle_jump", "obstacle_jump.png");
        loadImage("obstacle_train", "obstacle_train.png");
        loadImage("obstacle_train_side", "obstacle_train_side.jpg");
        loadImage("ascension", "ascension.png");
        loadImage("shopping", "shopping.png");
        loadImage("container_front", "container_front.jpg");
        loadImage("container_side", "container_side.jpg");
        loadImage("container_top", "container_top.jpg");
        loadImage("menu_bg", "主界面背景.png");
        loadImage("game_over_bg", "终界面.png");
        loadImage("road_texture", "road_texture.jpg");
        loadImage("ukiyo_building", "ukiyo_building.jpg");
        loadImage("pagoda", "pagoda.jpg");
        loadImage("sakura", "sakura.jpg");
        loadImage("bldg_torii", "bldg_torii.jpg");
        loadImage("bldg_castle", "bldg_castle.jpg");
        loadImage("bldg_lantern", "bldg_lantern.jpg");
        loadImage("bldg_shop", "bldg_shop.jpg");
        loadImage("bldg_bamboo", "bldg_bamboo.jpg");
        loadImage("bldg_pine", "bldg_pine.jpg");
        loadImage("bldg_kitsune", "bldg_kitsune.jpg");
        loadImage("bldg_bell", "bldg_bell.jpg");
        
        String[] sceneries = {"pagoda", "sakura", "bldg_torii", "bldg_castle", "bldg_lantern", "bldg_shop", "bldg_bamboo", "bldg_pine", "bldg_kitsune", "bldg_bell"};
        for (String s : sceneries) {
            IMAGES.put(s, removeWhiteBackground(IMAGES.get(s)));
        }
        
        loadImage("obs_train", "obs_train.jpg");
        loadImage("obs_ramp_tex", "obs_ramp_tex.jpg");
        loadImage("obs_jump", "obs_jump.jpg");
        loadImage("obs_slide", "obs_slide.jpg");
        loadImage("item_coin", "item_coin.jpg");
        
        String[] obstacles = {"obs_train", "obs_jump", "obs_slide", "item_coin"};
        for (String o : obstacles) {
            IMAGES.put(o, removeWhiteBackground(IMAGES.get(o)));
        }
        
        loadImage("cheryl_run_1", "runners/cheryl_run-1.png");
        loadImage("cheryl_run_2", "runners/cheryl_run-2.png");
        loadImage("dave_run_1", "runners/dave_run-1.png");
        loadImage("dave_run_2", "runners/dave_run-2.png");
        loadImage("matt_run_1", "runners/matt_run-1.png");
        loadImage("matt_run_2", "runners/matt_run-2.png");
        loadImage("mazz_run_1", "runners/mazz_run-1.png");
        loadImage("mazz_run_2", "runners/mazz_run-2.png");
    }

    public static Image ascensionImage() {
        return getImage("ascension");
    }

    public static Image shoppingIcon() {
        return getImage("shopping");
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

    public static Image gameOverBg() {
        return getImage("game_over_bg");
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

    public static Image randomItemIcon() { return getImage("random_item"); }
    public static Image rampSlope() { return getImage("ramp_slope"); }
    public static Image rampSide() { return getImage("ramp_side"); }

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

    public static Image obstacleTrainSideIcon() {
        return getImage("obstacle_train_side");
    }

    public static Image containerFront() {
        return getImage("container_front");
    }

    public static Image containerSide() {
        return getImage("container_side");
    }

    public static Image containerTop() {
        return getImage("container_top");
    }

    public static Image menuBg() {
        return getImage("menu_bg");
    }

    public static Image runnerFrame(String character, int frame) {
        return getImage(character + "_run_" + frame);
    }

    public static Image removeWhiteBackground(Image img) {
        if (img == null || img.getWidth() <= 0) return img;
        int width = (int) img.getWidth();
        int height = (int) img.getHeight();
        WritableImage wImg = new WritableImage(width, height);
        PixelReader reader = img.getPixelReader();
        PixelWriter writer = wImg.getPixelWriter();
        
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Color c = reader.getColor(x, y);
                if (c.getRed() > 0.85 && c.getGreen() > 0.85 && c.getBlue() > 0.85) {
                    writer.setColor(x, y, Color.TRANSPARENT);
                } else {
                    writer.setColor(x, y, c);
                }
            }
        }
        return wImg;
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
