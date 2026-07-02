package com.ycom.resource;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class AssetManager {
    public static Image ATLAS;
    private static final Map<String, TextureRegion> REGIONS = new HashMap<>();
    private static final Map<String, Integer> FRAME_COUNTS = new HashMap<>();
    private static final Map<String, Image> TEMP_IMAGES = new HashMap<>();

    public static void init() {
        TEMP_IMAGES.clear();
        REGIONS.clear();
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
        
        String[] sceneries = {"pagoda", "sakura", "bldg_torii", "bldg_castle", "bldg_lantern", "bldg_shop", "bldg_bamboo", "bldg_pine", "bldg_kitsune", "bldg_bell"};
        for (String s : sceneries) {
            loadImage(s, s + ".jpg");
            TEMP_IMAGES.put(s, removeWhiteBackground(TEMP_IMAGES.get(s)));
        }
        
        loadImage("obs_train", "obs_train.jpg");
        loadImage("obs_ramp_tex", "obs_ramp_tex.jpg");
        loadImage("obs_jump", "obs_jump.jpg");
        loadImage("obs_slide", "obs_slide.jpg");
        loadImage("item_coin", "item_coin.jpg");
        
        String[] obstacles = {"obs_train", "obs_jump", "obs_slide", "item_coin"};
        for (String o : obstacles) {
            TEMP_IMAGES.put(o, removeWhiteBackground(TEMP_IMAGES.get(o)));
        }
        
        loadImage("cheryl_run_1", "runners/cheryl_run-1.png");
        loadImage("cheryl_run_2", "runners/cheryl_run-2.png");
        loadImage("dave_run_1", "runners/dave_run-1.png");
        loadImage("dave_run_2", "runners/dave_run-2.png");
        loadImage("matt_run_1", "runners/matt_run-1.png");
        loadImage("matt_run_2", "runners/matt_run-2.png");
        loadImage("mazz_run_1", "runners/mazz_run-1.png");
        loadImage("mazz_run_2", "runners/mazz_run-2.png");
        
        packAtlas();
    }
    
    private static void packAtlas() {
        int atlasSize = 8192;
        WritableImage atlas = new WritableImage(atlasSize, atlasSize);
        PixelWriter writer = atlas.getPixelWriter();
        
        int currentX = 0;
        int currentY = 0;
        int rowHeight = 0;
        
        java.util.List<Map.Entry<String, Image>> sortedEntries = new java.util.ArrayList<>(TEMP_IMAGES.entrySet());
        sortedEntries.sort((e1, e2) -> Double.compare(
            e2.getValue() == null ? 0 : e2.getValue().getHeight(),
            e1.getValue() == null ? 0 : e1.getValue().getHeight()
        ));
        
        for (Map.Entry<String, Image> entry : sortedEntries) {
            Image img = entry.getValue();
            if (img == null || img.getWidth() <= 0) continue;
            int w = (int) img.getWidth();
            int h = (int) img.getHeight();
            
            if (currentX + w > atlasSize) {
                currentX = 0;
                currentY += rowHeight;
                rowHeight = 0;
            }
            if (currentY + h > atlasSize) {
                System.err.println("Atlas too small for " + entry.getKey());
                continue;
            }
            
            PixelReader reader = img.getPixelReader();
            writer.setPixels(currentX, currentY, w, h, reader, 0, 0);
            
            REGIONS.put(entry.getKey(), new TextureRegion(atlas, currentX, currentY, w, h));
            
            currentX += w;
            rowHeight = Math.max(rowHeight, h);
        }
        ATLAS = atlas;
        TEMP_IMAGES.clear(); // Free memory
    }

    public static TextureRegion getRegion(String key) {
        return REGIONS.get(key);
    }
    
    // Legacy support for places that absolutely need an Image (if any)
    public static String background() { return "background"; }
    public static String gameOverBg() { return "game_over_bg"; }
    public static String playerImage() { return "player"; }
    public static String runSheet() { return "run"; }
    public static String jumpSheet() { return "jump"; }
    public static String boostSheet() { return "boost"; }
    public static String slideSheet() { return "slide"; }
    public static String magnetIcon() { return "magnet"; }
    public static String spriteIcon() { return "sprite"; }
    public static String revivalIcon() { return "revival"; }
    public static String treadmillIcon() { return "treadmill"; }
    public static String randomItemIcon() { return "random"; }
    public static String rampSlope() { return "ramp_slope"; }
    public static String rampSide() { return "ramp_side"; }
    public static String randomIcon() { return "random"; }
    public static String coinIcon() { return "coin"; }
    public static String obstacleSlideIcon() { return "obstacle_slide"; }
    public static String obstacleJumpIcon() { return "obstacle_jump"; }
    public static String obstacleTrainIcon() { return "obstacle_train"; }
    public static String obstacleTrainSideIcon() { return "obstacle_train_side"; }
    public static String containerFront() { return "container_front"; }
    public static String containerSide() { return "container_side"; }
    public static String containerTop() { return "container_top"; }
    public static String menuBg() { return "menu_bg"; }
    public static String ascensionImage() { return "ascension"; }
    public static String shoppingIcon() { return "shopping"; }
    public static String runnerFrame(String character, int frame) {
        return character + "_run_" + frame;
    }
    
    public static boolean exists(String key) {
        return REGIONS.containsKey(key);
    }
    public static Image getImage(String key) {
        // Warning: This should ideally not be used if we want to stick to the atlas.
        // It's left here just in case something breaks. We return ATLAS if it's there.
        // Actually, if we return ATLAS, the caller will draw the entire atlas!
        // We will remove this and fix all callers.
        return null;
    }

    public static void draw(GraphicsContext gc, String key, double dx, double dy, double dw, double dh) {
        TextureRegion r = REGIONS.get(key);
        if (r != null) {
            gc.drawImage(ATLAS, r.sx(), r.sy(), r.sw(), r.sh(), dx, dy, dw, dh);
        }
    }
    
    public static void drawSpriteFrame(GraphicsContext gc, String key, int frame, double dx, double dy, double dw, double dh) {
        TextureRegion r = REGIONS.get(key);
        if (r != null) {
            int frameCount = FRAME_COUNTS.getOrDefault(key, 1);
            double frameW = r.sw() / frameCount;
            double sx = r.sx() + frame * frameW;
            gc.drawImage(ATLAS, sx, r.sy(), frameW, r.sh(), dx, dy, dw, dh);
        }
    }

    public static int frameCount(String key) {
        return FRAME_COUNTS.getOrDefault(key, 1);
    }

    private static void loadSprite(String key, String fileName, int frameCount) {
        loadImage(key, fileName);
        if (TEMP_IMAGES.containsKey(key)) {
            FRAME_COUNTS.put(key, frameCount);
        }
    }

    public static Image removeWhiteBackground(Image img) {
        if (img == null || img.getWidth() <= 0) return img;
        int width = (int) img.getWidth();
        int height = (int) img.getHeight();
        WritableImage wImg = new WritableImage(width, height);
        PixelReader reader = img.getPixelReader();
        PixelWriter writer = wImg.getPixelWriter();
        
        int[] pixels = new int[width * height];
        reader.getPixels(0, 0, width, height, javafx.scene.image.PixelFormat.getIntArgbInstance(), pixels, 0, width);
        
        for (int i = 0; i < pixels.length; i++) {
            int argb = pixels[i];
            int r = (argb >> 16) & 0xff;
            int g = (argb >> 8) & 0xff;
            int b = argb & 0xff;
            
            if (r > 217 && g > 217 && b > 217) {
                pixels[i] = 0x00ffffff; 
            }
        }
        
        writer.setPixels(0, 0, width, height, javafx.scene.image.PixelFormat.getIntArgbInstance(), pixels, 0, width);
        return wImg;
    }

    private static void loadImage(String key, String fileName) {
        String url = resolve(fileName);
        if (url == null) {
            System.err.println("Missing image asset: " + fileName);
            return;
        }
        TEMP_IMAGES.put(key, new Image(url, false));
    }

    public static String resolve(String fileName) {
        String resourcePath = "";
        String lower = fileName.toLowerCase();
        if (lower.endsWith(".png") || lower.endsWith(".jpg") || lower.endsWith(".jpeg")) {
            resourcePath = "/assets/textures/" + fileName;
        } else if (lower.endsWith(".wav") || lower.endsWith(".mp3") || lower.endsWith(".ogg")) {
            resourcePath = "/assets/audio/" + fileName;
        } else {
            resourcePath = "/assets/" + fileName;
        }

        java.net.URL url = AssetManager.class.getResource(resourcePath);
        if (url != null) {
            return url.toExternalForm();
        }

        Path cwd = Path.of("").toAbsolutePath();
        Path[] candidates = new Path[] {
                cwd.resolve("src/main/resources" + resourcePath),
                cwd.resolve("YCOM/src/main/resources" + resourcePath)
        };
        for (Path candidate : candidates) {
            Path normalized = candidate.normalize();
            if (Files.exists(normalized)) {
                return normalized.toUri().toString();
            }
        }
        return null;
    }
}
