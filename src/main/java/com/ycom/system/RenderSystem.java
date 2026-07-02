package com.ycom.system;

import com.ycom.core.Config;
import com.ycom.entity.Player;
import com.ycom.resource.AssetManager;
import com.ycom.world.GameWorld;
import com.ycom.render.Camera;
import com.ycom.render.Projector;
import com.ycom.render.Projection;
import com.ycom.render.ObstacleRenderer;
import com.ycom.render.RenderFrame;
import com.ycom.render.RenderSnapshot;
import com.ycom.entity.GameObject;
import java.util.ArrayList;
import java.util.List;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
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
    private final Camera camera = new Camera(0.0, 0.0, 0.0);
    private final List<RenderSnapshot> objectBuffer = new ArrayList<>();
    private final com.ycom.render.RendererRegistry registry = new com.ycom.render.RendererRegistry();
    private WritableImage backgroundCache;

    public RenderSystem(Canvas canvas) {
        this.canvas = canvas;
        this.cx = Config.LOGICAL_WIDTH / 2.0;
        this.horizonY = Config.LOGICAL_HEIGHT * 0.38;
        this.projector = new Projector(this.cx, this.horizonY);
        this.obstacleRenderer = new ObstacleRenderer(this.projector, this.horizonY);
        
        registry.register(GameObject.ObjectKind.PLAYER, (gc, obj, p, cam) -> drawPlayer(gc, obj, p));
        registry.register(GameObject.ObjectKind.COIN, (gc, obj, p, cam) -> drawCoin(gc, p));
        registry.register(GameObject.ObjectKind.MAGNET, (gc, obj, p, cam) -> {
            double phase = obj.animationTime() * 4.0;
            drawCoinFlipIcon(gc, p, AssetManager.magnetIcon(), Color.MEDIUMPURPLE, "M", phase);
        });
        registry.register(GameObject.ObjectKind.ENERGY_DRINK, (gc, obj, p, cam) -> {
            double phase = obj.animationTime() * 4.0;
            drawCoinFlipIcon(gc, p, AssetManager.spriteIcon(), Color.MEDIUMPURPLE, "S", phase);
        });
        registry.register(GameObject.ObjectKind.REVIVAL_CAPSULE, (gc, obj, p, cam) -> {
            double phase = obj.animationTime() * 6.0;
            drawBobbingIcon(gc, p, "revival", Color.CRIMSON, "+", phase);
        });
        registry.register(GameObject.ObjectKind.TREADMILL, (gc, obj, p, cam) -> {
            double phase = obj.animationTime() * 4.0;
            drawCoinFlipIcon(gc, p, AssetManager.treadmillIcon(), Color.DARKORANGE, "x2", phase);
        });
        registry.register(GameObject.ObjectKind.RANDOM_ITEM, (gc, obj, p, cam) -> {
            double phase = obj.animationTime() * 6.0;
            drawBobbingIcon(gc, p, "random", Color.DARKSLATEGRAY, "?", phase);
        });
        registry.register(GameObject.ObjectKind.OBSTACLE, this.obstacleRenderer);
    }

    public void render(com.ycom.core.PhysicsSnapshot snap, com.ycom.system.ParticleSystem particles) {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        RenderFrame frame = snap.renderFrame();
        if (frame == null) return;
        double alpha = frame.alpha(System.nanoTime());
        RenderSnapshot playerSnapshot = frame.player(alpha);

        drawBackground(gc);

        double camX = playerSnapshot.x();
        double camY = Config.CAMERA_Y + playerSnapshot.y();
        double camZ = playerSnapshot.z() + Config.CAMERA_OFFSET_Z;
        camera.x = camX;
        camera.y = camY;
        camera.z = camZ;

        drawTrack(gc, camera);
        drawScenery(gc, camera);
        drawObjects(gc, frame, alpha, playerSnapshot, camera);
        particles.draw(gc, camX, camY, camZ, snap.particles());
        hudRenderer.draw(gc, snap);
    }

    private void drawBackground(GraphicsContext gc) {
        if (backgroundCache == null) {
            backgroundCache = createBackgroundCache();
        }
        gc.drawImage(backgroundCache, 0.0, 0.0);
    }

    private WritableImage createBackgroundCache() {
        Canvas bgCanvas = new Canvas(Config.LOGICAL_WIDTH, Config.LOGICAL_HEIGHT);
        GraphicsContext gc = bgCanvas.getGraphicsContext2D();
        drawBackgroundLayer(gc);
        return bgCanvas.snapshot(null, null);
    }

    private void drawBackgroundLayer(GraphicsContext gc) {
        String background = AssetManager.background();
        if (com.ycom.resource.AssetManager.exists(background)) {
            com.ycom.resource.AssetManager.draw(gc, background, 0.0, 0.0, Config.LOGICAL_WIDTH, Config.LOGICAL_HEIGHT);
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

    private void drawObjects(GraphicsContext gc, RenderFrame frame, double alpha, RenderSnapshot player, Camera cam) {
        objectBuffer.clear();
        frame.writeObjects(objectBuffer, alpha);
        objectBuffer.removeIf(snapshot -> snapshot.z() < cam.z + 0.8);
        objectBuffer.add(player);
        objectBuffer.sort((a, b) -> Double.compare(b.z(), a.z()));

        for (RenderSnapshot obj : objectBuffer) {
            Projection p = projector.project(obj.x(), obj.y(), obj.z(), obj.width(), obj.height(), cam);
            if (p.scale() <= 0.0 || p.x() + p.width() < -200.0 || p.x() - p.width() > Config.LOGICAL_WIDTH + 200.0) {
                continue;
            }

            com.ycom.render.ObjectRenderer renderer = registry.getRenderer(obj.kind());
            if (renderer != null) {
                renderer.render(gc, obj, p, cam);
            }
        }
    }
    
    private void drawPlayer(GraphicsContext gc, RenderSnapshot player, Projection p) {
        if (player.reviveInvincible()) {
            gc.setFill(Color.rgb(255, 200, 50, 0.32));
            gc.fillOval(p.x() - p.width() * 0.95, p.y() - p.height() * 0.9, p.width() * 1.9, p.height() * 1.8);
        } else if (player.boosted()) {
            gc.setFill(Color.rgb(155, 89, 182, 0.25));
            gc.fillOval(p.x() - p.width() * 0.95, p.y() - p.height() * 0.9, p.width() * 1.9, p.height() * 1.8);
        }

        // 根据玩家状态选择精灵图
        String sheet = null;
        int frameCount = 1;
        switch (player.playerState()) {
            case BOOSTED_INVINCIBLE -> {
                sheet = "boost";
                frameCount = AssetManager.frameCount("boost");
            }
            case JUMPING -> {
                sheet = "jump";
                frameCount = AssetManager.frameCount("jump");
            }
            case SLIDING -> {
                sheet = "slide";
                frameCount = AssetManager.frameCount("slide");
            }
            default -> {
                sheet = "run";
                frameCount = AssetManager.frameCount("run");
            }
        }

        if (sheet != null) {
            int frame = currentFrame(player.animationTime(), frameCount, 12.0);
            com.ycom.resource.AssetManager.drawSpriteFrame(gc, sheet, frame, p.x() - p.width() / 2.0, p.y() - p.height() / 2.0, p.width(), p.height());
            return;
        }

        String playerImage = "player";
        if (playerImage != null) {
            com.ycom.resource.AssetManager.draw(gc, playerImage, p.x() - p.width() / 2.0, p.y() - p.height() / 2.0, p.width(), p.height());
            return;
        }

        gc.setFill(Color.CORNFLOWERBLUE);
        gc.fillRoundRect(p.x() - p.width() / 2.0, p.y() - p.height() / 2.0, p.width(), p.height(), 12.0, 12.0);
    }

    private int currentFrame(double animationTime, int frameCount, double framesPerSecond) {
        if (frameCount <= 1) {
            return 0;
        }
        return (int) Math.floor(animationTime * framesPerSecond) % frameCount;
    }

    private void drawCoin(GraphicsContext gc, Projection p) {
        double size = Math.max(6.0, Math.min(p.width(), p.height()));
        String coinImage = "coin";

        if (coinImage != null) {
            com.ycom.resource.AssetManager.draw(gc, coinImage, p.x() - size / 2.0, p.y() - size / 2.0, size, size);
            return;
        }

    }

    private static final double POWERUP_ICON_SCALE = 1.3;

    private void drawCoinFlipIcon(GraphicsContext gc, Projection p, String icon, Color color, String label, double phase) {
        if (!com.ycom.resource.AssetManager.exists(icon)) {
            drawPickup(gc, p, color, label);
            return;
        }
        double widthScale = Math.max(0.1, Math.abs(Math.cos(phase)));
        double h = Math.max(12.0, p.height()) * POWERUP_ICON_SCALE;
        double w = Math.max(12.0, p.width()) * POWERUP_ICON_SCALE * widthScale;
        com.ycom.resource.AssetManager.draw(gc, icon, p.x() - w / 2.0, p.y() - h / 2.0, w, h);
    }

    private void drawBobbingIcon(GraphicsContext gc, Projection p, String icon, Color color, String label, double phase) {
        double w = Math.max(12.0, p.width()) * POWERUP_ICON_SCALE;
        double h = Math.max(12.0, p.height()) * POWERUP_ICON_SCALE;
        double yOffset = -h * 0.45 + Math.sin(phase) * h * 0.22;
        if (icon != null) {
            com.ycom.resource.AssetManager.draw(gc, icon, p.x() - w / 2.0, p.y() - h / 2.0 + yOffset, w, h);
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

    private void drawIconOrPickup(GraphicsContext gc, Projection p, String icon, Color color, String label) {
        if (icon != null) {
            double w = Math.max(12.0, p.width());
            double h = Math.max(12.0, p.height());
            com.ycom.resource.AssetManager.draw(gc, icon, p.x() - w / 2.0, p.y() - h / 2.0, w, h);
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

    private void drawPerspectiveImage(GraphicsContext gc, String img, 
                                      double ulx, double uly, 
                                      double urx, double ury, 
                                      double lrx, double lry, 
                                      double llx, double lly) {
        if (img == null || !com.ycom.resource.AssetManager.exists(img)) return;
        
        double minX = Math.min(Math.min(ulx, urx), Math.min(llx, lrx));
        double maxX = Math.max(Math.max(ulx, urx), Math.max(llx, lrx));
        double minY = Math.min(Math.min(uly, ury), Math.min(lly, lry));
        double maxY = Math.max(Math.max(uly, ury), Math.max(lly, lry));
        
        if (maxX < 0 || minX > Config.LOGICAL_WIDTH || maxY < 0 || minY > Config.LOGICAL_HEIGHT) {
            return;
        }
        
        // Near-plane hardware culling
        if (maxX - minX > 15000.0 || maxY - minY > 15000.0) {
            return;
        }

        PT.setUlx(ulx); PT.setUly(uly);
        PT.setUrx(urx); PT.setUry(ury);
        PT.setLrx(lrx); PT.setLry(lry);
        PT.setLlx(llx); PT.setLly(lly);
        
        gc.setEffect(PT);
        com.ycom.resource.TextureRegion r = com.ycom.resource.AssetManager.getRegion(img);
        if (r != null) {
            com.ycom.resource.AssetManager.draw(gc, img, 0, 0, r.sw(), r.sh());
        }
        gc.setEffect(null);
    }

    private void drawTrack(GraphicsContext gc, Camera cam) {
        String roadTex = "road_texture";
        if (roadTex == null || !com.ycom.resource.AssetManager.exists(roadTex)) {
            // Fallback if texture fails
            gc.setStroke(UIUtils.BORDER);
            gc.setLineWidth(5.0);
            double roadHalfWidth = Config.LANE_WIDTH * 1.5;
            for (int i = -3; i <= 3; i++) {
                double lineX = i * Config.LANE_WIDTH / 2.0;
                drawGroundLine(gc, lineX, cam.x, cam.y);
            }
            return;
        }
        
        double roadHalfWidth = Config.LANE_WIDTH * 1.6;
        double segmentLength = 40.0; // Optimized: doubled length halves Shader calls and adds speed blur
        double zOffset = positiveMod(cam.z, segmentLength);
        
        // Draw back to front to avoid bleeding
        for (double z = 220.0 - zOffset; z > 1.0; z -= segmentLength) {
            double farZ = z;
            double nearZ = Math.max(1.0, z - segmentLength);
            
            double farScale = Config.FOCAL_LENGTH / farZ;
            double nearScale = Config.FOCAL_LENGTH / nearZ;
            
            double farY = horizonY - (-cam.y) * farScale;
            double nearY = horizonY - (-cam.y) * nearScale;
            
            double farShiftX = cx + (0.0 - cam.x) * farScale;
            double nearShiftX = cx + (0.0 - cam.x) * nearScale;
            
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

    private void drawScenery(GraphicsContext gc, Camera cam) {
        double spacing = 50.0;
        double zOffset = positiveMod(cam.z, spacing);
        
        for (double dZ = 220.0 - zOffset; dZ > 1.0; dZ -= spacing) {
            double worldZ = cam.z + dZ;
            int absZ = (int) Math.round(worldZ / spacing);
            
            String keyLeft = SCENERY_KEYS[Math.abs(absZ * 7) % SCENERY_KEYS.length];
            String keyRight = SCENERY_KEYS[Math.abs(absZ * 11) % SCENERY_KEYS.length];
            
            String imgLeft = keyLeft;
            String imgRight = keyRight;
            
            // Keep sizes uniform for a neat row
            double wLeft = 45.0 + (Math.abs(absZ * 13) % 10);
            double hLeft = wLeft * 1.5; 
            
            double wRight = 45.0 + (Math.abs(absZ * 17) % 10);
            double hRight = wRight * 1.5;
            
            // Align perfectly with the road edge so they don't clip into the track
            double roadEdge = 35.0;
            double xLeft = -roadEdge - (wLeft / 2.0);
            double xRight = roadEdge + (wRight / 2.0);
            
            if (imgLeft != null) drawScenerySprite(gc, imgLeft, xLeft, worldZ, wLeft, hLeft, cam);
            if (imgRight != null) drawScenerySprite(gc, imgRight, xRight, worldZ, wRight, hRight, cam);
        }
    }

    private void drawScenerySprite(GraphicsContext gc, String img, double bX, double bZ, double bW, double bH, Camera cam) {
        if (bZ < cam.z + 1.0) return;
        Projection p = projector.project(bX, 0, bZ, bW, bH, cam);
        if (p.scale() <= 0.0) return;
        
        // Pin the bottom edge of the sprite exactly to the 3D ground level
        double groundY = horizonY - (-cam.y) * p.scale();
        com.ycom.resource.AssetManager.draw(gc, img, p.x() - p.width() / 2.0, groundY - p.height(), p.width(), p.height());
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


    private void drawCover(GraphicsContext gc, String image, double x, double y, double w, double h) {
        com.ycom.resource.TextureRegion r = com.ycom.resource.AssetManager.getRegion(image);
        if (r == null) return;
        double scale = Math.max(w / r.sw(), h / r.sh());
        double sw = w / scale;
        double sh = h / scale;
        double sx = (r.sw() - sw) / 2.0;
        double sy = (r.sh() - sh) / 2.0;
        gc.drawImage(com.ycom.resource.AssetManager.ATLAS, r.sx() + sx, r.sy() + sy, sw, sh, x, y, w, h);
    }

    private double positiveMod(double value, double mod) {
        double result = value % mod;
        return result < 0.0 ? result + mod : result;
    }

    public void setAlpha(double alpha) {
        this.projector.setAlpha(alpha);
    }
}
