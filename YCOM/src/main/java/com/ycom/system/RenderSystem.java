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
import java.util.ArrayList;
import java.util.List;
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

    public RenderSystem(Canvas canvas) {
        this.canvas = canvas;
        this.cx = Config.LOGICAL_WIDTH / 2.0;
        this.horizonY = Config.LOGICAL_HEIGHT * 0.38;
    }

    public void render(GameWorld world, ScoreSystem scoreSystem, ParticleSystem particles) {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        Player player = world.getPlayer();

        drawBackground(gc);

        double camX = player.x;
        double camY = Config.CAMERA_Y + player.y;
        double camZ = player.z + Config.CAMERA_OFFSET_Z;

        drawTrack(gc, camX, camY, camZ);
        drawScenery(gc, camX, camY, camZ);
        drawObjects(gc, world, player, camX, camY, camZ);
        particles.draw(gc, camX, camY, camZ);
        drawHud(gc, player, scoreSystem);
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
        List<GameObject> objects = new ArrayList<>(world.getObjects());
        objects.add(player);
        objects.sort((a, b) -> Double.compare(b.z, a.z));

        for (GameObject obj : objects) {
            if (!obj.active || obj.z < camZ + 0.8) {
                continue;
            }

            Projection p = project(obj, camX, camY, camZ);
            if (p.scale <= 0.0 || p.x + p.width < -200.0 || p.x - p.width > Config.LOGICAL_WIDTH + 200.0) {
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
                case OBSTACLE -> drawObstacle(gc, (Obstacle) obj, p, camX, camY, camZ);
            }
        }
    }

    private Projection project(GameObject obj, double camX, double camY, double camZ) {
        return project(obj.x, obj.y, obj.z, obj.width, obj.height, camX, camY, camZ);
    }

    private Projection project(double x, double y, double z, double width, double height, double camX, double camY, double camZ) {
        double distZ = Math.max(0.5, z - camZ);
        double scale = Config.FOCAL_LENGTH / distZ;
        double screenX = cx + (x - camX) * scale;
        double screenY = horizonY - (y + height / 2.0 - camY) * scale;
        return new Projection(screenX, screenY, width * scale, height * scale, scale);
    }

    private void drawPlayer(GraphicsContext gc, Player player, Projection p) {
        if (player.isReviveInvincible()) {
            gc.setFill(Color.rgb(255, 200, 50, 0.32));
            gc.fillOval(p.x - p.width * 0.95, p.y - p.height * 0.9, p.width * 1.9, p.height * 1.8);
        } else if (player.isBoosted()) {
            gc.setFill(Color.rgb(155, 89, 182, 0.25));
            gc.fillOval(p.x - p.width * 0.95, p.y - p.height * 0.9, p.width * 1.9, p.height * 1.8);
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
            gc.drawImage(sheet, sx, 0.0, sw, sheet.getHeight(), p.x - p.width / 2.0, p.y - p.height / 2.0, p.width, p.height);
            return;
        }

        Image playerImage = AssetManager.playerImage();
        if (playerImage != null && playerImage.getWidth() > 0.0) {
            gc.drawImage(playerImage, p.x - p.width / 2.0, p.y - p.height / 2.0, p.width, p.height);
            return;
        }

        gc.setFill(Color.CORNFLOWERBLUE);
        gc.fillRoundRect(p.x - p.width / 2.0, p.y - p.height / 2.0, p.width, p.height, 12.0, 12.0);
    }

    private void drawCoin(GraphicsContext gc, Projection p) {
        double size = Math.max(6.0, Math.min(p.width, p.height));
        Image coinImage = AssetManager.getImage("coin");

        if (coinImage != null && coinImage.getWidth() > 0.0) {
            gc.drawImage(coinImage, p.x - size / 2.0, p.y - size / 2.0, size, size);
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
        double h = Math.max(12.0, p.height) * POWERUP_ICON_SCALE;
        double w = Math.max(12.0, p.width) * POWERUP_ICON_SCALE * widthScale;
        gc.drawImage(icon, p.x - w / 2.0, p.y - h / 2.0, w, h);
    }

    private void drawBobbingIcon(GraphicsContext gc, Projection p, Image icon, Color color, String label, double phase) {
        double w = Math.max(12.0, p.width) * POWERUP_ICON_SCALE;
        double h = Math.max(12.0, p.height) * POWERUP_ICON_SCALE;
        double yOffset = -h * 0.45 + Math.sin(phase) * h * 0.22;
        if (icon != null && icon.getWidth() > 0.0) {
            gc.drawImage(icon, p.x - w / 2.0, p.y - h / 2.0 + yOffset, w, h);
            return;
        }
        gc.setFill(color);
        gc.fillOval(p.x - w / 2.0, p.y - h / 2.0 + yOffset, w, h);
        gc.setFill(Color.WHITE);
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, Math.max(10.0, h * 0.48)));
        gc.fillText(label, p.x, p.y + h * 0.16 + yOffset);
        gc.setTextAlign(TextAlignment.LEFT);
    }

    private void drawIconOrPickup(GraphicsContext gc, Projection p, Image icon, Color color, String label) {
        if (icon != null && icon.getWidth() > 0.0) {
            double w = Math.max(12.0, p.width);
            double h = Math.max(12.0, p.height);
            gc.drawImage(icon, p.x - w / 2.0, p.y - h / 2.0, w, h);
            return;
        }
        drawPickup(gc, p, color, label);
    }

    private void drawPickup(GraphicsContext gc, Projection p, Color color, String label) {
        double w = Math.max(12.0, p.width);
        double h = Math.max(12.0, p.height);
        gc.setFill(color);
        gc.fillOval(p.x - w / 2.0, p.y - h / 2.0, w, h);
        gc.setFill(Color.WHITE);
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, Math.max(10.0, h * 0.48)));
        gc.fillText(label, p.x, p.y + h * 0.16);
        gc.setTextAlign(TextAlignment.LEFT);
    }
    private void drawObstacle(GraphicsContext gc, Obstacle obstacle, Projection p, double camX, double camY, double camZ) {
        if (obstacle.avoidMethod() == Obstacle.AvoidMethod.CHANGE_LANE || obstacle.avoidMethod() == Obstacle.AvoidMethod.CONTAINER) {
            drawTrainObstacleBody(gc, obstacle, p, camX, camY, camZ);
            return;
        } else if (obstacle.avoidMethod() == Obstacle.AvoidMethod.RAMP) {
            drawRamp(gc, obstacle, p, camX, camY, camZ);
            return;
        }

        Image obstacleImage = switch (obstacle.avoidMethod()) {
            case SLIDE -> AssetManager.obstacleSlideIcon();
            case JUMP -> AssetManager.obstacleJumpIcon();
            default -> null;
        };

        if (obstacleImage != null) {
            gc.drawImage(obstacleImage, p.x - p.width / 2.0, p.y - p.height / 2.0, p.width, p.height);
        }
    }

    private void drawTrainObstacleBody(GraphicsContext gc, Obstacle obstacle, Projection dummyFront, double camX, double camY, double camZ) {
        double fZ = obstacle.z - obstacle.depth / 2.0;
        double bZ = obstacle.z + obstacle.depth / 2.0;
        
        Projection front = project(obstacle.x, obstacle.y, fZ, obstacle.width * 0.95, obstacle.height * 0.95, camX, camY, camZ);
        Projection back = project(obstacle.x, obstacle.y + 0.12, bZ, obstacle.width * 0.95, obstacle.height * 0.95, camX, camY, camZ);

        double frontLeft = front.x - front.width * 0.45;
        double frontRight = front.x + front.width * 0.45;
        double frontTop = front.y - front.height * 0.48;
        double frontBottom = front.y + front.height * 0.32;

        double backLeft = back.x - back.width * 0.45;
        double backRight = back.x + back.width * 0.45;
        double backTop = back.y - back.height * 0.48;
        double backBottom = back.y + back.height * 0.32;

        Image topImg, sideImg, frontImg;
        if (obstacle.avoidMethod() == Obstacle.AvoidMethod.CONTAINER) {
            topImg = AssetManager.containerTop();
            sideImg = AssetManager.containerSide();
            frontImg = AssetManager.containerFront();
        } else {
            topImg = AssetManager.obstacleTrainSideIcon();
            sideImg = AssetManager.obstacleTrainSideIcon();
            frontImg = AssetManager.obstacleTrainIcon();
        }

        // Top
        if (frontTop > backTop) {
            double[] xPoints = {frontLeft, backLeft, backRight, frontRight};
            double[] yPoints = {frontTop, backTop, backTop, frontTop};
            if (topImg != null && topImg.getWidth() > 0) {
                drawPerspectiveImage(gc, topImg, backLeft, backTop, backRight, backTop, frontRight, frontTop, frontLeft, frontTop);
            } else {
                gc.setFill(Color.rgb(23, 104, 160));
                gc.fillPolygon(xPoints, yPoints, 4);
            }
            gc.setStroke(UIUtils.BORDER);
            gc.setLineWidth(4.0);
            gc.strokePolygon(xPoints, yPoints, 4);
        }

        // Left
        if (frontLeft > backLeft) {
            double[] xPoints = {frontLeft, backLeft, backLeft, frontLeft};
            double[] yPoints = {frontTop, backTop, backBottom, frontBottom};
            if (sideImg != null && sideImg.getWidth() > 0) {
                drawPerspectiveImage(gc, sideImg, backLeft, backTop, frontLeft, frontTop, frontLeft, frontBottom, backLeft, backBottom);
            } else {
                gc.setFill(Color.rgb(31, 136, 198));
                gc.fillPolygon(xPoints, yPoints, 4);
            }
            gc.setStroke(UIUtils.BORDER);
            gc.setLineWidth(4.0);
            gc.strokePolygon(xPoints, yPoints, 4);
        }

        // Right
        if (frontRight < backRight) {
            double[] xPoints = {frontRight, backRight, backRight, frontRight};
            double[] yPoints = {frontTop, backTop, backBottom, frontBottom};
            if (sideImg != null && sideImg.getWidth() > 0) {
                drawPerspectiveImage(gc, sideImg, frontRight, frontTop, backRight, backTop, backRight, backBottom, frontRight, frontBottom);
            } else {
                gc.setFill(Color.rgb(18, 92, 142));
                gc.fillPolygon(xPoints, yPoints, 4);
            }
            gc.setStroke(UIUtils.BORDER);
            gc.setLineWidth(4.0);
            gc.strokePolygon(xPoints, yPoints, 4);
        }

        // Bottom
        if (frontBottom < backBottom) {
            gc.setFill(Color.rgb(18, 74, 117));
            gc.fillPolygon(
                    new double[] {frontLeft, backLeft, backRight, frontRight},
                    new double[] {frontBottom, backBottom, backBottom, frontBottom},
                    4
            );
        }

        // Front
        double[] frontX = {frontLeft, frontRight, frontRight, frontLeft};
        double[] frontY = {frontTop, frontTop, frontBottom, frontBottom};
        if (frontImg != null && frontImg.getWidth() > 0) {
            gc.drawImage(frontImg, frontLeft, frontTop, frontRight - frontLeft, frontBottom - frontTop);
        } else {
            gc.setFill(Color.rgb(31, 136, 198));
            gc.fillRect(frontLeft, frontTop, frontRight - frontLeft, frontBottom - frontTop);
        }
        gc.setStroke(UIUtils.BORDER);
        gc.setLineWidth(5.0);
        gc.strokePolygon(frontX, frontY, 4);
    }

    private void drawRamp(GraphicsContext gc, Obstacle obstacle, Projection front, double camX, double camY, double camZ) {
        double fZ = obstacle.z - obstacle.depth / 2.0;
        double bZ = obstacle.z + obstacle.depth / 2.0;
        
        Projection pFrontBottomLeft = project(obstacle.x - obstacle.width / 2.0, obstacle.y, fZ, 0, 0, camX, camY, camZ);
        Projection pFrontBottomRight = project(obstacle.x + obstacle.width / 2.0, obstacle.y, fZ, 0, 0, camX, camY, camZ);
        
        Projection pBackTopLeft = project(obstacle.x - obstacle.width / 2.0, obstacle.y + obstacle.height, bZ, 0, 0, camX, camY, camZ);
        Projection pBackTopRight = project(obstacle.x + obstacle.width / 2.0, obstacle.y + obstacle.height, bZ, 0, 0, camX, camY, camZ);
        
        Projection pBackBottomLeft = project(obstacle.x - obstacle.width / 2.0, obstacle.y, bZ, 0, 0, camX, camY, camZ);
        Projection pBackBottomRight = project(obstacle.x + obstacle.width / 2.0, obstacle.y, bZ, 0, 0, camX, camY, camZ);
        
        Image rampSlopeImg = AssetManager.rampSlope();
        Image rampSideImg = AssetManager.rampSide();

        // Slope
        // Slope
        double[] slopeX = {pBackTopLeft.x, pBackTopRight.x, pFrontBottomRight.x, pFrontBottomLeft.x};
        double[] slopeY = {pBackTopLeft.y, pBackTopRight.y, pFrontBottomRight.y, pFrontBottomLeft.y};
        if (rampSlopeImg != null && rampSlopeImg.getWidth() > 0) {
            drawPerspectiveImage(gc, rampSlopeImg, 
                pBackTopLeft.x, pBackTopLeft.y, 
                pBackTopRight.x, pBackTopRight.y, 
                pFrontBottomRight.x, pFrontBottomRight.y, 
                pFrontBottomLeft.x, pFrontBottomLeft.y);
        } else {
            gc.setFill(Color.rgb(150, 150, 150));
            gc.fillPolygon(slopeX, slopeY, 4);
        }
        gc.setStroke(UIUtils.BORDER);
        gc.setLineWidth(4.0);
        gc.strokePolygon(slopeX, slopeY, 4);

        // Left face
        if (pFrontBottomLeft.x > pBackBottomLeft.x) {
            double[] leftX = {pBackTopLeft.x, pFrontBottomLeft.x, pBackBottomLeft.x};
            double[] leftY = {pBackTopLeft.y, pFrontBottomLeft.y, pBackBottomLeft.y};
            if (rampSideImg != null && rampSideImg.getWidth() > 0) {
                gc.save();
                gc.beginPath();
                gc.moveTo(pBackTopLeft.x, pBackTopLeft.y);
                gc.lineTo(pFrontBottomLeft.x, pFrontBottomLeft.y);
                gc.lineTo(pBackBottomLeft.x, pBackBottomLeft.y);
                gc.closePath();
                gc.clip();
                
                Projection pFrontTopLeft = project(obstacle.x - obstacle.width / 2.0, obstacle.y + obstacle.height, fZ, 0, 0, camX, camY, camZ);
                drawPerspectiveImage(gc, rampSideImg,
                    pBackTopLeft.x, pBackTopLeft.y,
                    pFrontTopLeft.x, pFrontTopLeft.y,
                    pFrontBottomLeft.x, pFrontBottomLeft.y,
                    pBackBottomLeft.x, pBackBottomLeft.y
                );
                gc.restore();
            } else {
                gc.setFill(Color.rgb(100, 100, 100));
                gc.fillPolygon(leftX, leftY, 3);
            }
            gc.setStroke(UIUtils.BORDER);
            gc.setLineWidth(4.0);
            gc.strokePolygon(leftX, leftY, 3);
        }
        
        // Right face
        if (pFrontBottomRight.x < pBackBottomRight.x) {
            double[] rightX = {pBackTopRight.x, pFrontBottomRight.x, pBackBottomRight.x};
            double[] rightY = {pBackTopRight.y, pFrontBottomRight.y, pBackBottomRight.y};
            if (rampSideImg != null && rampSideImg.getWidth() > 0) {
                gc.save();
                gc.beginPath();
                gc.moveTo(pBackTopRight.x, pBackTopRight.y);
                gc.lineTo(pFrontBottomRight.x, pFrontBottomRight.y);
                gc.lineTo(pBackBottomRight.x, pBackBottomRight.y);
                gc.closePath();
                gc.clip();
                
                Projection pFrontTopRight = project(obstacle.x + obstacle.width / 2.0, obstacle.y + obstacle.height, fZ, 0, 0, camX, camY, camZ);
                drawPerspectiveImage(gc, rampSideImg,
                    pFrontTopRight.x, pFrontTopRight.y,
                    pBackTopRight.x, pBackTopRight.y,
                    pBackBottomRight.x, pBackBottomRight.y,
                    pFrontBottomRight.x, pFrontBottomRight.y
                );
                gc.restore();
            } else {
                gc.setFill(Color.rgb(100, 100, 100));
                gc.fillPolygon(rightX, rightY, 3);
            }
            gc.setStroke(UIUtils.BORDER);
            gc.setLineWidth(4.0);
            gc.strokePolygon(rightX, rightY, 3);
        }
    }

    private void drawPerspectiveImage(GraphicsContext gc, Image img, 
                                      double ulx, double uly, 
                                      double urx, double ury, 
                                      double lrx, double lry, 
                                      double llx, double lly) {
        if (img == null || img.getWidth() <= 0) return;
        javafx.scene.effect.PerspectiveTransform pt = new javafx.scene.effect.PerspectiveTransform();
        pt.setUlx(ulx); pt.setUly(uly);
        pt.setUrx(urx); pt.setUry(ury);
        pt.setLrx(lrx); pt.setLry(lry);
        pt.setLlx(llx); pt.setLly(lly);
        gc.setEffect(pt);
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
        double segmentLength = 20.0;
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
        Projection p = project(bX, 0, bZ, bW, bH, camX, camY, camZ);
        if (p.scale <= 0.0) return;
        
        // Pin the bottom edge of the sprite exactly to the 3D ground level
        double groundY = horizonY - (-camY) * p.scale;
        gc.drawImage(img, p.x - p.width / 2.0, groundY - p.height, p.width, p.height);
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

    private void drawHud(GraphicsContext gc, Player player, ScoreSystem scoreSystem) {
        gc.setTextAlign(TextAlignment.LEFT);

        boolean topRunVisible = Session.isLoggedIn() && Session.current().highScore > 0;
        double panelW = 440.0;
        double panelH = topRunVisible ? 240.0 : 190.0;
        double startX = 24.0;
        double startY = 24.0;

        // HUD panel shadow
        gc.setFill(UIUtils.BORDER);
        gc.fillRect(startX + 8, startY + 8, panelW, panelH);
        
        // HUD panel fill
        gc.setFill(UIUtils.WHITE);
        gc.fillRect(startX, startY, panelW, panelH);
        
        // HUD panel border
        gc.setStroke(UIUtils.BORDER);
        gc.setLineWidth(6.0);
        gc.strokeRect(startX, startY, panelW, panelH);

        gc.setFill(UIUtils.BORDER);
        gc.setFont(Font.font("Arial Black", FontWeight.BOLD, 30.0));
        double y = startY + 50.0;
        gc.fillText("SCORE: " + scoreSystem.getScore(), startX + 24.0, y);
        y += 40.0;

        gc.setFont(Font.font("Arial Black", FontWeight.BOLD, 22.0));
        if (topRunVisible) {
            Account acc = Session.current();
            int gap = acc.highScore - scoreSystem.getScore();
            if (gap > 0) {
                gc.setFill(UIUtils.BORDER);
                gc.fillText("TOP RUN: " + gap, startX + 24.0, y);
            } else {
                gc.setFill(UIUtils.RED);
                gc.fillText("NEW BEST! +" + (-gap), startX + 24.0, y);
                gc.setFill(UIUtils.BORDER);
            }
            y += 40.0;
        }

        int totalCoins = Session.current() != null ? Session.current().coins : scoreSystem.getCoins();
        gc.fillText("COINS: " + scoreSystem.getRunCoinsEarned() + " (ALL: " + totalCoins + ")", startX + 24.0, y); y += 40.0;
        gc.fillText("SPEED: " + String.format("%.2fx", TimeManager.getWorldRate()), startX + 24.0, y); y += 40.0;
        gc.fillText("CAPSULES: " + player.revivalCount(), startX + 24.0, y);

        double size = 84.0;
        double stride = 104.0;
        double bcx = 32.0;
        double bcy = Config.LOGICAL_HEIGHT - 32.0 - size;

        if (player.hasMagnet()) {
            drawBuffTimer(gc, AssetManager.magnetIcon(), player.magnetTimer(), player.magnetMaxDuration(),
                    UIUtils.PINK, bcx, bcy, size);
            bcy -= stride;
        }
        if (player.isReviveInvincible()) {
            drawBuffTimer(gc, AssetManager.revivalIcon(),
                    player.reviveInvincibleTimer(), player.reviveInvincibleMaxDuration(),
                    Color.rgb(255, 200, 50), bcx, bcy, size);
            bcy -= stride;
        }
        if (player.isBoosted()) {
            drawBuffTimer(gc, AssetManager.spriteIcon(), player.boostTimer(), player.boostMaxDuration(),
                    UIUtils.PINK, bcx, bcy, size);
            bcy -= stride;
        }
        if (player.hasScoreMultiplier()) {
            drawBuffTimer(gc, AssetManager.treadmillIcon(), player.scoreMultiplierTimer(), player.scoreMultiplierMaxDuration(),
                    UIUtils.YELLOW, bcx, bcy, size);
        }
    }

    private void drawBuffTimer(GraphicsContext gc, Image icon, double timer, double max, Color ringColor, double x, double y, double size) {
        // Flat solid background shadow
        gc.setFill(UIUtils.BORDER);
        gc.fillRect(x + 6, y + 6, size, size);

        // Flat solid color background
        gc.setFill(ringColor);
        gc.fillRect(x, y, size, size);
        
        // Border
        gc.setStroke(UIUtils.BORDER);
        gc.setLineWidth(4.0);
        gc.strokeRect(x, y, size, size);

        if (icon != null && icon.getWidth() > 0.0) {
            double iconSize = size * 0.66;
            gc.drawImage(icon, x + (size - iconSize) / 2.0, y + (size - iconSize) / 2.0, iconSize, iconSize);
        }

        // Timer text tag
        gc.setFill(UIUtils.WHITE);
        gc.fillRect(x, y + size - 14, size, 24);
        gc.strokeRect(x, y + size - 14, size, 24);

        gc.setFill(UIUtils.BORDER);
        gc.setFont(Font.font("Arial Black", FontWeight.BOLD, 16.0));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText(String.format("%.1fs", timer), x + size / 2.0, y + size + 3);
        gc.setTextAlign(TextAlignment.LEFT);
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

    private record Projection(double x, double y, double width, double height, double scale) {
    }
}
