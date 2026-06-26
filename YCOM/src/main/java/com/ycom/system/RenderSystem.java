package com.ycom.system;

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

        // 把固定的逻辑画面(1080x1920)等比缩放居中到实际窗口，避免内容超出窗口看不见
        double scale = Math.min(canvas.getWidth() / Config.LOGICAL_WIDTH, canvas.getHeight() / Config.LOGICAL_HEIGHT);
        double offsetX = (canvas.getWidth() - Config.LOGICAL_WIDTH * scale) / 2.0;
        double offsetY = (canvas.getHeight() - Config.LOGICAL_HEIGHT * scale) / 2.0;

        // 先把整块画布刷成黑色(letterbox 黑边)，再进入逻辑坐标系绘制
        gc.setTransform(1, 0, 0, 1, 0, 0);
        gc.setFill(Color.BLACK);
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
        gc.setTransform(scale, 0, 0, scale, offsetX, offsetY);

        drawBackground(gc);

        double camX = player.x;
        double camY = Config.CAMERA_Y;
        double camZ = player.z + Config.CAMERA_OFFSET_Z;

        drawTrack(gc, camX, camY, camZ);
        drawObjects(gc, world, player, camX, camY, camZ);
        particles.draw(gc, camX, camY, camZ);
        drawHud(gc, player, scoreSystem);

        gc.setTransform(1, 0, 0, 1, 0, 0);
    }

    private void drawBackground(GraphicsContext gc) {
        Image background = AssetManager.background();
        if (background != null && background.getWidth() > 0.0) {
            drawCover(gc, background, 0.0, 0.0, Config.LOGICAL_WIDTH, Config.LOGICAL_HEIGHT);
            gc.setFill(Color.rgb(5, 12, 18, 0.45));
            gc.fillRect(0.0, 0.0, Config.LOGICAL_WIDTH, Config.LOGICAL_HEIGHT);
        } else {
            gc.setFill(Color.rgb(22, 36, 44));
            gc.fillRect(0.0, 0.0, Config.LOGICAL_WIDTH, Config.LOGICAL_HEIGHT);
        }

        // 绘制雪峰背景
        gc.setFill(Color.rgb(200, 220, 230, 0.9)); // 左侧矮雪峰
        gc.fillPolygon(
                new double[]{ -50, Config.LOGICAL_WIDTH * 0.25, Config.LOGICAL_WIDTH * 0.6 },
                new double[]{ horizonY, horizonY - 120, horizonY },
                3
        );
        gc.setFill(Color.rgb(180, 205, 220, 0.9)); // 右侧高雪峰
        gc.fillPolygon(
                new double[]{ Config.LOGICAL_WIDTH * 0.35, Config.LOGICAL_WIDTH * 0.75, Config.LOGICAL_WIDTH + 100 },
                new double[]{ horizonY, horizonY - 180, horizonY },
                3
        );

        gc.setFill(Color.rgb(35, 65, 45, 0.88));
        gc.fillRect(0.0, horizonY, Config.LOGICAL_WIDTH, Config.LOGICAL_HEIGHT - horizonY);
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
        if (player.isBoosted()) {
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
        Image obstacleImage = switch (obstacle.avoidMethod()) {
            case SLIDE -> AssetManager.obstacleSlideIcon();
            case JUMP -> AssetManager.obstacleJumpIcon();
            case CHANGE_LANE -> AssetManager.obstacleTrainIcon();
        };

        if (obstacle.avoidMethod() == Obstacle.AvoidMethod.CHANGE_LANE) {
            drawTrainObstacleBody(gc, obstacle, p, camX, camY, camZ);
        }
        gc.drawImage(obstacleImage, p.x - p.width / 2.0, p.y - p.height / 2.0, p.width, p.height);
    }

    private void drawTrainObstacleBody(GraphicsContext gc, Obstacle obstacle, Projection front, double camX, double camY, double camZ) {
        Projection back = project(
                obstacle.x,
                obstacle.y + 0.12,
                obstacle.z + obstacle.depth * 0.72,
                obstacle.width * 0.95,
                obstacle.height * 0.95,
                camX,
                camY,
                camZ
        );

        double frontLeft = front.x - front.width * 0.45;
        double frontRight = front.x + front.width * 0.45;
        double frontTop = front.y - front.height * 0.48;
        double frontBottom = front.y + front.height * 0.32;

        double backLeft = back.x - back.width * 0.45;
        double backRight = back.x + back.width * 0.45;
        double backTop = back.y - back.height * 0.48;
        double backBottom = back.y + back.height * 0.32;

        gc.setFill(Color.rgb(23, 104, 160));
        gc.fillPolygon(
                new double[] {frontLeft, backLeft, backRight, frontRight},
                new double[] {frontTop, backTop, backTop, frontTop},
                4
        );

        gc.setFill(Color.rgb(31, 136, 198));
        gc.fillPolygon(
                new double[] {frontLeft, backLeft, backLeft, frontLeft},
                new double[] {frontTop, backTop, backBottom, frontBottom},
                4
        );
        gc.fillPolygon(
                new double[] {frontRight, backRight, backRight, frontRight},
                new double[] {frontTop, backTop, backBottom, frontBottom},
                4
        );

        gc.setFill(Color.rgb(18, 74, 117));
        gc.fillPolygon(
                new double[] {frontLeft, backLeft, backRight, frontRight},
                new double[] {frontBottom, backBottom, backBottom, frontBottom},
                4
        );

        gc.setFill(Color.rgb(31, 136, 198));
        gc.fillRect(frontLeft, frontTop, frontRight - frontLeft, frontBottom - frontTop);
    }

    private void drawTrack(GraphicsContext gc, double camX, double camY, double camZ) {
        gc.setStroke(Color.rgb(245, 245, 245, 0.75));
        gc.setLineWidth(2.5);

        double roadHalfWidth = Config.LANE_WIDTH * 1.5;
        for (int i = -3; i <= 3; i++) {
            double lineX = i * Config.LANE_WIDTH / 2.0;
            drawGroundLine(gc, lineX, camX, camY);
        }

        gc.setStroke(Color.rgb(230, 230, 230, 0.42));
        double zOffset = positiveMod(camZ, 5.0);
        for (double z = 5.0 - zOffset; z < 220.0; z += 5.0) {
            if (z < 1.0) {
                continue;
            }
            double scale = Config.FOCAL_LENGTH / z;
            double y = horizonY - (-camY) * scale;
            double shiftX = cx + (0.0 - camX) * scale;
            double halfWidth = roadHalfWidth * scale;
            gc.strokeLine(shiftX - halfWidth, y, shiftX + halfWidth, y);
        }
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
        gc.setFill(Color.rgb(0, 0, 0, 0.45));
        gc.fillRoundRect(24.0, 24.0, 392.0, 250.0, 10.0, 10.0);

        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 34.0));
        gc.fillText("Score " + scoreSystem.getScore(), 48.0, 72.0);
        gc.setFont(Font.font("Arial", 28.0));
        gc.fillText("Coins " + scoreSystem.getCoins(), 48.0, 114.0);
        gc.fillText("Best " + scoreSystem.getHighScore(), 48.0, 156.0);
        gc.fillText("Speed " + String.format("%.2fx", TimeManager.getWorldRate()), 48.0, 198.0);
        gc.fillText("Revives " + player.revivalCount(), 48.0, 240.0);

        double size = 84.0;
        double stride = 104.0;
        double bcx = 32.0 + size / 2.0;
        double bcy = Config.LOGICAL_HEIGHT - 32.0 - size / 2.0;

        if (player.hasMagnet()) {
            drawBuffTimer(gc, AssetManager.magnetIcon(), player.magnetTimer(), player.magnetMaxDuration(),
                    Color.rgb(155, 89, 182), bcx, bcy, size);
            bcy -= stride;
        }
        if (player.isBoosted()) {
            drawBuffTimer(gc, AssetManager.spriteIcon(), player.boostTimer(), player.boostMaxDuration(),
                    Color.rgb(155, 89, 182), bcx, bcy, size);
            bcy -= stride;
        }
        if (player.hasScoreMultiplier()) {
            drawBuffTimer(gc, AssetManager.treadmillIcon(), player.scoreMultiplierTimer(), player.scoreMultiplierMaxDuration(),
                    Color.rgb(255, 170, 60), bcx, bcy, size);
        }
    }

    private void drawBuffTimer(GraphicsContext gc, Image icon, double timer, double max, Color ringColor, double cx, double cy, double size) {
        double r = size / 2.0;

        gc.setFill(Color.rgb(0, 0, 0, 0.55));
        gc.fillOval(cx - r, cy - r, size, size);

        if (icon != null && icon.getWidth() > 0.0) {
            double iconSize = size * 0.66;
            gc.drawImage(icon, cx - iconSize / 2.0, cy - iconSize / 2.0, iconSize, iconSize);
        }

        double ratio = max <= 0.0 ? 0.0 : Math.max(0.0, Math.min(1.0, timer / max));
        double ringR = r - 3.0;
        gc.setStroke(Color.rgb(255, 255, 255, 0.25));
        gc.setLineWidth(5.0);
        gc.strokeOval(cx - ringR, cy - ringR, ringR * 2.0, ringR * 2.0);
        gc.setStroke(ringColor);
        gc.setLineWidth(5.0);
        gc.strokeArc(cx - ringR, cy - ringR, ringR * 2.0, ringR * 2.0, 90.0, -ratio * 360.0, ArcType.OPEN);

        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 18.0));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText(String.format("%.1fs", timer), cx, cy + r + 20.0);
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
