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

    public void render(GameWorld world, ScoreSystem scoreSystem) {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        Player player = world.getPlayer();

        drawBackground(gc);

        double camX = player.x;
        double camY = Config.CAMERA_Y;
        double camZ = player.z + Config.CAMERA_OFFSET_Z;

        drawTrack(gc, camX, camY, camZ);
        drawObjects(gc, world, player, camX, camY, camZ);
        drawHud(gc, player, scoreSystem);
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
                case REVIVAL_CAPSULE -> drawIconOrPickup(gc, p, AssetManager.revivalIcon(), Color.CRIMSON, "+");
                case TREADMILL -> {
                    double phase = ((AnimatedObject) obj).animationTime() * 4.0;
                    drawCoinFlipIcon(gc, p, AssetManager.treadmillIcon(), Color.DARKORANGE, "x2", phase);
                }
                case RANDOM_ITEM -> drawIconOrPickup(gc, p, AssetManager.randomIcon(), Color.DARKSLATEGRAY, "?");
                case OBSTACLE -> drawObstacle(gc, (Obstacle) obj, p);
            }
        }
    }

    private Projection project(GameObject obj, double camX, double camY, double camZ) {
        double distZ = Math.max(0.5, obj.z - camZ);
        double scale = Config.FOCAL_LENGTH / distZ;
        double screenX = cx + (obj.x - camX) * scale;
        double screenY = horizonY - (obj.y + obj.height / 2.0 - camY) * scale;
        return new Projection(screenX, screenY, obj.width * scale, obj.height * scale, scale);
    }

    private void drawPlayer(GraphicsContext gc, Player player, Projection p) {
        if (player.isBoosted()) {
            gc.setFill(Color.rgb(155, 89, 182, 0.25));
            gc.fillOval(p.x - p.width * 0.95, p.y - p.height * 0.9, p.width * 1.9, p.height * 1.8);
        }

        Image sheet = AssetManager.runSheet();
        if (sheet != null && sheet.getWidth() > 0.0) {
            int frame = player.currentFrame(AssetManager.runFrameCount());
            double sw = sheet.getWidth() / AssetManager.runFrameCount();
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

    private void drawCoinFlipIcon(GraphicsContext gc, Projection p, Image icon, Color color, String label, double phase) {
        if (icon == null || icon.getWidth() <= 0.0) {
            drawPickup(gc, p, color, label);
            return;
        }
        double widthScale = Math.max(0.1, Math.abs(Math.cos(phase)));
        double h = Math.max(12.0, p.height);
        double w = Math.max(12.0, p.width) * widthScale;
        gc.drawImage(icon, p.x - w / 2.0, p.y - h / 2.0, w, h);
    }

    private void drawBobbingIcon(GraphicsContext gc, Projection p, Image icon, Color color, String label, double phase) {
        double w = Math.max(12.0, p.width);
        double h = Math.max(12.0, p.height);
        double yOffset = -h * 0.30 + Math.sin(phase) * h * 0.12;
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

    private void drawObstacle(GraphicsContext gc, Obstacle obstacle, Projection p) {
        gc.setFill(obstacle.color);
        gc.fillRect(p.x - p.width / 2.0, p.y - p.height / 2.0, p.width, p.height);
        gc.setStroke(Color.rgb(25, 12, 10));
        gc.setLineWidth(Math.max(2.0, p.width * 0.03));
        gc.strokeRect(p.x - p.width / 2.0, p.y - p.height / 2.0, p.width, p.height);
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

        double y = 82.0;
        if (player.hasMagnet()) {
            drawStatus(gc, "MAGNET", player.magnetTimer(), y);
            y += 48.0;
        }
        if (player.isBoosted()) {
            drawStatus(gc, "INVINCIBLE", player.boostTimer(), y);
            y += 48.0;
        }
        if (player.hasScoreMultiplier()) {
            drawStatus(gc, "SCORE x2", player.scoreMultiplierTimer(), y);
        }
    }

    private void drawStatus(GraphicsContext gc, String name, double seconds, double y) {
        gc.setFill(Color.rgb(0, 0, 0, 0.45));
        gc.fillRoundRect(Config.LOGICAL_WIDTH - 286.0, y - 34.0, 238.0, 42.0, 8.0, 8.0);
        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 24.0));
        gc.fillText(name + " " + String.format("%.1fs", seconds), Config.LOGICAL_WIDTH - 264.0, y - 6.0);
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
