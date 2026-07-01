package com.ycom.system;

import com.ycom.account.Account;
import com.ycom.account.Session;
import com.ycom.core.Config;
import com.ycom.core.TimeManager;
import com.ycom.entity.AnimatedObject;
import com.ycom.entity.GameObject;
import com.ycom.entity.Obstacle;
import com.ycom.entity.Player;
import com.ycom.resource.AssetManager;
import com.ycom.world.GameWorld;
import com.ycom.render.Camera;
import com.ycom.render.Projector;
import com.ycom.render.Projection;
import com.ycom.render.ObstacleRenderer;
import java.util.ArrayList;
import java.util.List;
import com.ycom.system.EffectSystem;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.shape.ArcType;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import com.ycom.state.UIUtils;

public class RenderSystem {
    private final Canvas canvas;
    private final double cx;
    private final double horizonY;
    private final Projector projector;
    private final ObstacleRenderer obstacleRenderer;
    private final com.ycom.render.HudRenderer hudRenderer = new com.ycom.render.HudRenderer();

    public RenderSystem(Canvas canvas) {
        this.canvas = canvas;
        this.cx = Config.LOGICAL_WIDTH / 2.0;
        this.horizonY = Config.LOGICAL_HEIGHT * 0.38;
        this.projector = new Projector(this.cx, this.horizonY);
        this.obstacleRenderer = new ObstacleRenderer(this.projector, this.horizonY);
    }

    public void render(GameWorld world, ScoreSystem scoreSystem, ParticleSystem particles, EffectSystem effectSystem) {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        Player player = world.getPlayer();

        drawBackground(gc);

        double camX = player.getX();
        double camY = Config.CAMERA_Y + player.getY();
        double camZ = player.getZ() + Config.CAMERA_OFFSET_Z;
        Camera cam = new Camera(camX, camY, camZ);

        drawTrack(gc, camX, camY, camZ);
        drawScenery(gc, camX, camY, camZ);
        drawObjects(gc, world, player, camX, camY, camZ);
        particles.draw(gc, camX, camY, camZ);
        hudRenderer.draw(gc, player, scoreSystem, effectSystem);
    }

    private void drawBackground(GraphicsContext gc) {
        Image background = AssetManager.background();
        if (background != null && background.getWidth() > 0.0) {
            drawCover(gc, background, 0.0, 0.0, Config.LOGICAL_WIDTH, Config.LOGICAL_HEIGHT);
        } else {
            gc.setFill(UIUtils.CYAN);
            gc.fillRect(0.0, 0.0, Config.LOGICAL_WIDTH, Config.LOGICAL_HEIGHT);
        }

        // 绘制雪峰背景 (Neo-Brutalist Style)
        gc.setLineWidth(5.0);
        gc.setStroke(UIUtils.BORDER);
        
        gc.setFill(UIUtils.WHITE); // 左侧矮雪峰
        double[] xPoints = { -50, Config.LOGICAL_WIDTH * 0.25, Config.LOGICAL_WIDTH * 0.6 };
        double[] yPoints = { horizonY, horizonY - 120, horizonY };
        gc.fillPolygon(xPoints, yPoints, 3);
        gc.strokePolygon(xPoints, yPoints, 3);

        gc.setFill(Color.rgb(220, 220, 220)); // 右侧高雪峰
        double[] xPoints2 = { Config.LOGICAL_WIDTH * 0.35, Config.LOGICAL_WIDTH * 0.75, Config.LOGICAL_WIDTH + 100 };
        double[] yPoints2 = { horizonY, horizonY - 180, horizonY };
        gc.fillPolygon(xPoints2, yPoints2, 3);
        gc.strokePolygon(xPoints2, yPoints2, 3);

        // Solid light sand ground (Ukiyo-e paper feel)
        gc.setFill(Color.web("#F3E5AB"));
        gc.fillRect(0.0, horizonY, Config.LOGICAL_WIDTH, Config.LOGICAL_HEIGHT - horizonY);
        
        // Thick horizon line
        gc.setLineWidth(8.0);
        gc.setStroke(UIUtils.BORDER);
        gc.strokeLine(0.0, horizonY, Config.LOGICAL_WIDTH, horizonY);
    }

    private void drawObjects(GraphicsContext gc, GameWorld world, Player player, double camX, double camY, double camZ) {
        Camera cam = new Camera(camX, camY, camZ);
        List<GameObject> objects = new ArrayList<>();
        for (GameObject obj : world.getObjects()) {
            if (obj.isActive() && obj.getZ() >= camZ + 0.8) {
                objects.add(obj);
            }
        }
        objects.add(player);
        objects.sort((a, b) -> Double.compare(b.getZ(), a.getZ()));

        for (GameObject obj : objects) {
            Projection p = projector.project(obj.getX(), obj.getY(), obj.getZ(), obj.getWidth(), obj.getHeight(), cam);
            if (p.scale() <= 0.0 || p.x() + p.width() < -200.0 || p.x() - p.width() > Config.LOGICAL_WIDTH + 200.0) {
                continue;
            }

            switch (obj.kind()) {
                case PLAYER -> drawPlayer(gc, (Player) obj, p);
                case COIN -> drawCoin(gc, p);
                case MAGNET -> {
                    double phase = ((AnimatedObject) obj).animationTime() * 4.0;
                    drawCoinFlipIcon(gc, p, AssetManager.magnetIcon(), Color.MEDIUMPURPLE, "M", phase);
                }
                case ENERGY_DRINK -> {
                    double phase = ((AnimatedObject) obj).animationTime() * 4.0;
                    drawCoinFlipIcon(gc, p, AssetManager.spriteIcon(), Color.MEDIUMPURPLE, "S", phase);
                }
                case REVIVAL_CAPSULE -> {
                    double phase = ((AnimatedObject) obj).animationTime() * 6.0;
                    drawBobbingIcon(gc, p, AssetManager.revivalIcon(), Color.CRIMSON, "+", phase);
                }
                case TREADMILL -> {
                    double phase = ((AnimatedObject) obj).animationTime() * 4.0;
                    drawCoinFlipIcon(gc, p, AssetManager.treadmillIcon(), Color.DARKORANGE, "x2", phase);
                }
                case RANDOM_ITEM -> {
                    double phase = ((AnimatedObject) obj).animationTime() * 6.0;
                    drawBobbingIcon(gc, p, AssetManager.randomIcon(), Color.DARKSLATEGRAY, "?", phase);
                }
                case OBSTACLE -> obstacleRenderer.drawObstacle(gc, (Obstacle) obj, p, cam);
            }
        }
    }

    
    
    private void drawPlayer(GraphicsContext gc, Player player, Projection p) {
        if (player.isReviveInvincible()) {
            gc.setFill(Color.rgb(255, 200, 50, 0.32));
            gc.fillOval(p.x() - p.width() * 0.95, p.y() - p.height() * 0.9, p.width() * 1.9, p.height() * 1.8);
        } else if (player.isBoosted()) {
            gc.setFill(Color.rgb(155, 89, 182, 0.25));
            gc.fillOval(p.x() - p.width() * 0.95, p.y() - p.height() * 0.9, p.width() * 1.9, p.height() * 1.8);
        }

        // 根据玩家状态选择精灵图
        Image sheet = null;
        int frameCount = 1;
        switch (player.state()) {
            case BOOSTED_INVINCIBLE -> {
                sheet = AssetManager.boostSheet();
                frameCount = AssetManager.frameCount("boost");
            }
            case JUMPING -> {
                sheet = AssetManager.jumpSheet();
                frameCount = AssetManager.frameCount("jump");
            }
            case SLIDING -> {
                sheet = AssetManager.slideSheet();
                frameCount = AssetManager.frameCount("slide");
            }
            default -> {
                sheet = AssetManager.runSheet();
                frameCount = AssetManager.frameCount("run");
            }
        }

        if (sheet != null && sheet.getWidth() > 0.0) {
            int frame = player.currentFrame(frameCount);
            double sw = sheet.getWidth() / frameCount;
            double sx = frame * sw;
            gc.drawImage(sheet, sx, 0.0, sw, sheet.getHeight(), p.x() - p.width() / 2.0, p.y() - p.height() / 2.0, p.width(), p.height());
            return;
        }

        Image playerImage = AssetManager.playerImage();
        if (playerImage != null && playerImage.getWidth() > 0.0) {
            gc.drawImage(playerImage, p.x() - p.width() / 2.0, p.y() - p.height() / 2.0, p.width(), p.height());
            return;
        }

        gc.setFill(Color.CORNFLOWERBLUE);
        gc.fillRoundRect(p.x() - p.width() / 2.0, p.y() - p.height() / 2.0, p.width(), p.height(), 12.0, 12.0);
    }

    private void drawCoin(GraphicsContext gc, Projection p) {
        double size = Math.max(6.0, Math.min(p.width(), p.height()));
        Image coinImage = AssetManager.getImage("coin");

        if (coinImage != null && coinImage.getWidth() > 0.0) {
            gc.drawImage(coinImage, p.x() - size / 2.0, p.y() - size / 2.0, size, size);
            return;
        }

    }

    private static final double POWERUP_ICON_SCALE = 1.3;

    private void drawCoinFlipIcon(GraphicsContext gc, Projection p, Image icon, Color color, String label, double phase) {
        if (icon == null || icon.getWidth() <= 0.0) {
            drawPickup(gc, p, color, label);
            return;
        }
        double widthScale = Math.max(0.1, Math.abs(Math.cos(phase)));
        double h = Math.max(12.0, p.height()) * POWERUP_ICON_SCALE;
        double w = Math.max(12.0, p.width()) * POWERUP_ICON_SCALE * widthScale;
        gc.drawImage(icon, p.x() - w / 2.0, p.y() - h / 2.0, w, h);
    }

    private void drawBobbingIcon(GraphicsContext gc, Projection p, Image icon, Color color, String label, double phase) {
        double w = Math.max(12.0, p.width()) * POWERUP_ICON_SCALE;
        double h = Math.max(12.0, p.height()) * POWERUP_ICON_SCALE;
        double yOffset = -h * 0.45 + Math.sin(phase) * h * 0.22;
        if (icon != null && icon.getWidth() > 0.0) {
            gc.drawImage(icon, p.x() - w / 2.0, p.y() - h / 2.0 + yOffset, w, h);
            return;
        }
        gc.setFill(color);
        gc.fillOval(p.x() - w / 2.0, p.y() - h / 2.0 + yOffset, w, h);
        gc.setFill(Color.WHITE);
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, Math.max(10.0, h * 0.48)));
        gc.fillText(label, p.x(), p.y() + h * 0.16 + yOffset);
        gc.setTextAlign(TextAlignment.LEFT);
    }

    private void drawIconOrPickup(GraphicsContext gc, Projection p, Image icon, Color color, String label) {
        if (icon != null && icon.getWidth() > 0.0) {
            double w = Math.max(12.0, p.width());
            double h = Math.max(12.0, p.height());
            gc.drawImage(icon, p.x() - w / 2.0, p.y() - h / 2.0, w, h);
            return;
        }
        drawPickup(gc, p, color, label);
    }

    private void drawPickup(GraphicsContext gc, Projection p, Color color, String label) {
        double w = Math.max(12.0, p.width());
        double h = Math.max(12.0, p.height());
        gc.setFill(color);
        gc.fillOval(p.x() - w / 2.0, p.y() - h / 2.0, w, h);
        gc.setFill(Color.WHITE);
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, Math.max(10.0, h * 0.48)));
        gc.fillText(label, p.x(), p.y() + h * 0.16);
        gc.setTextAlign(TextAlignment.LEFT);
    }
    
    
    
    private static final javafx.scene.effect.PerspectiveTransform PT = new javafx.scene.effect.PerspectiveTransform();

    private void drawPerspectiveImage(GraphicsContext gc, Image img, 
                                      double ulx, double uly, 
                                      double urx, double ury, 
                                      double lrx, double lry, 
                                      double llx, double lly) {
        if (img == null || img.getWidth() <= 0) return;
        PT.setUlx(ulx); PT.setUly(uly);
        PT.setUrx(urx); PT.setUry(ury);
        PT.setLrx(lrx); PT.setLry(lry);
        PT.setLlx(llx); PT.setLly(lly);
        gc.setEffect(PT);
        gc.drawImage(img, 0, 0, img.getWidth(), img.getHeight());
        gc.setEffect(null);
    }

    private void drawTrack(GraphicsContext gc, double camX, double camY, double camZ) {
        Image roadTex = AssetManager.getImage("road_texture");
        if (roadTex == null || roadTex.getWidth() <= 0) {
            // Fallback if texture fails
            gc.setStroke(UIUtils.BORDER);
            gc.setLineWidth(5.0);
            double roadHalfWidth = Config.LANE_WIDTH * 1.5;
            for (int i = -3; i <= 3; i++) {
                double lineX = i * Config.LANE_WIDTH / 2.0;
                drawGroundLine(gc, lineX, camX, camY);
            }
            return;
        }
        
        double roadHalfWidth = Config.LANE_WIDTH * 1.6;
        double segmentLength = 40.0; // Optimized: doubled length halves Shader calls and adds speed blur
        double zOffset = positiveMod(camZ, segmentLength);
        
        // Draw back to front to avoid bleeding
        for (double z = 220.0 - zOffset; z > 1.0; z -= segmentLength) {
            double farZ = z;
            double nearZ = Math.max(0.5, z - segmentLength);
            
            double farScale = Config.FOCAL_LENGTH / farZ;
            double nearScale = Config.FOCAL_LENGTH / nearZ;
            
            double farY = horizonY - (-camY) * farScale;
            double nearY = horizonY - (-camY) * nearScale;
            
            double farShiftX = cx + (0.0 - camX) * farScale;
            double nearShiftX = cx + (0.0 - camX) * nearScale;
            
            double farHalf = roadHalfWidth * farScale;
            double nearHalf = roadHalfWidth * nearScale;
            
            drawPerspectiveImage(gc, roadTex,
                farShiftX - farHalf, farY,
                farShiftX + farHalf, farY,
                nearShiftX + nearHalf, nearY,
                nearShiftX - nearHalf, nearY
            );
        }
    }

    private static final String[] SCENERY_KEYS = {
        "pagoda", "sakura", "bldg_torii", "bldg_castle", "bldg_lantern", 
        "bldg_shop", "bldg_bamboo", "bldg_pine", "bldg_kitsune", "bldg_bell"
    };

    private void drawScenery(GraphicsContext gc, double camX, double camY, double camZ) {
        double spacing = 50.0;
        double zOffset = positiveMod(camZ, spacing);
        
        for (double dZ = 220.0 - zOffset; dZ > 1.0; dZ -= spacing) {
            double worldZ = camZ + dZ;
            int absZ = (int) Math.round(worldZ / spacing);
            
            String keyLeft = SCENERY_KEYS[Math.abs(absZ * 7) % SCENERY_KEYS.length];
            String keyRight = SCENERY_KEYS[Math.abs(absZ * 11) % SCENERY_KEYS.length];
            
            Image imgLeft = AssetManager.getImage(keyLeft);
            Image imgRight = AssetManager.getImage(keyRight);
            
            // Keep sizes uniform for a neat row
            double wLeft = 45.0 + (Math.abs(absZ * 13) % 10);
            double hLeft = wLeft * 1.5; 
            
            double wRight = 45.0 + (Math.abs(absZ * 17) % 10);
            double hRight = wRight * 1.5;
            
            // Align perfectly with the road edge so they don't clip into the track
            double roadEdge = 35.0;
            double xLeft = -roadEdge - (wLeft / 2.0);
            double xRight = roadEdge + (wRight / 2.0);
            
            if (imgLeft != null) drawScenerySprite(gc, imgLeft, xLeft, worldZ, wLeft, hLeft, camX, camY, camZ);
            if (imgRight != null) drawScenerySprite(gc, imgRight, xRight, worldZ, wRight, hRight, camX, camY, camZ);
        }
    }

    private void drawScenerySprite(GraphicsContext gc, Image img, double bX, double bZ, double bW, double bH, double camX, double camY, double camZ) {
        if (bZ < camZ + 1.0) return;
        Projection p = projector.project(bX, 0, bZ, bW, bH, new Camera(camX, camY, camZ));
        if (p.scale() <= 0.0) return;
        
        // Pin the bottom edge of the sprite exactly to the 3D ground level
        double groundY = horizonY - (-camY) * p.scale();
        gc.drawImage(img, p.x() - p.width() / 2.0, groundY - p.height(), p.width(), p.height());
    }


    private void drawGroundLine(GraphicsContext gc, double lineX, double camX, double camY) {
        double nearZ = 1.0;
        double farZ = 220.0;
        double nearScale = Config.FOCAL_LENGTH / nearZ;
        double farScale = Config.FOCAL_LENGTH / farZ;
        double x1 = cx + (lineX - camX) * nearScale;
        double y1 = horizonY - (-camY) * nearScale;
        double x2 = cx + (lineX - camX) * farScale;
        double y2 = horizonY - (-camY) * farScale;
        gc.strokeLine(x1, y1, x2, y2);
    }


    private void drawCover(GraphicsContext gc, Image image, double x, double y, double w, double h) {
        double scale = Math.max(w / image.getWidth(), h / image.getHeight());
        double sw = w / scale;
        double sh = h / scale;
        double sx = (image.getWidth() - sw) / 2.0;
        double sy = (image.getHeight() - sh) / 2.0;
        gc.drawImage(image, sx, sy, sw, sh, x, y, w, h);
    }

    private double positiveMod(double value, double mod) {
        double result = value % mod;
        return result < 0.0 ? result + mod : result;
    }

}
