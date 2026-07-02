package com.ycom.render;

import com.ycom.entity.Obstacle;
import com.ycom.resource.AssetManager;
import com.ycom.state.UIUtils;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

public class ObstacleRenderer implements ObjectRenderer {
    private final Projector projector;
    private final double horizonY;
    private static final javafx.scene.effect.PerspectiveTransform PT = new javafx.scene.effect.PerspectiveTransform();

    public ObstacleRenderer(Projector projector, double horizonY) {
        this.projector = projector;
        this.horizonY = horizonY;
    }

    public void drawPerspectiveImage(GraphicsContext gc, String img, 
                                     double ulx, double uly, 
                                     double urx, double ury, 
                                     double lrx, double lry, 
                                     double llx, double lly) {
        if (img == null || !com.ycom.resource.AssetManager.exists(img)) return;
        
        double minX = Math.min(Math.min(ulx, urx), Math.min(llx, lrx));
        double maxX = Math.max(Math.max(ulx, urx), Math.max(llx, lrx));
        double minY = Math.min(Math.min(uly, ury), Math.min(lly, lry));
        double maxY = Math.max(Math.max(uly, ury), Math.max(lly, lry));
        
        if (maxX < 0 || minX > 1920.0 || maxY < 0 || minY > 1080.0) {
            return;
        }
        
        // Near-plane hardware culling: prevents 16384 texture allocation crashes
        if (maxX - minX > 15000.0 || maxY - minY > 15000.0) {
            return;
        }

        PT.setUlx(ulx); PT.setUly(uly);
        PT.setUrx(urx); PT.setUry(ury);
        PT.setLrx(lrx); PT.setLry(lry);
        PT.setLlx(llx); PT.setLly(lly);
        
        gc.setEffect(PT);
        com.ycom.resource.TextureRegion r = com.ycom.resource.AssetManager.getRegion(img);
            if (r != null) com.ycom.resource.AssetManager.draw(gc, img, 0, 0, r.sw(), r.sh());
        gc.setEffect(null);
    }

    @Override
    public void render(GraphicsContext gc, RenderSnapshot obstacle, Projection p, Camera cam) {
        if (obstacle.avoidMethod() == Obstacle.AvoidMethod.CONTAINER) {
            drawTrainObstacleBody(gc, obstacle, p, cam);
            return;
        } else if (obstacle.avoidMethod() == Obstacle.AvoidMethod.RAMP) {
            drawRamp(gc, obstacle, p, cam);
            return;
        }

        String obstacleImage = switch (obstacle.avoidMethod()) {
            case CHANGE_LANE -> "obs_train";
            case SLIDE -> "obs_slide";
            case JUMP -> "obs_jump";
            default -> null;
        };

        if (obstacleImage != null) {
            double drawHeight = p.height();
            if (obstacle.avoidMethod() == Obstacle.AvoidMethod.SLIDE) {
                drawHeight = p.height() * 2.5;
            }
            double groundY = horizonY - (-cam.y) * p.scale();
            com.ycom.resource.AssetManager.draw(gc, obstacleImage, p.x() - p.width() / 2.0, groundY - drawHeight, p.width(), drawHeight);
        }
    }

    private void drawTrainObstacleBody(GraphicsContext gc, RenderSnapshot obstacle, Projection dummyFront, Camera cam) {
        double fZ = obstacle.z() - obstacle.depth() / 2.0;
        double bZ = obstacle.z() + obstacle.depth() / 2.0;
        
        Projection front = projector.project(obstacle.x(), obstacle.y(), fZ, obstacle.width() * 0.95, obstacle.height() * 0.95, cam);
        Projection back = projector.project(obstacle.x(), obstacle.y() + 0.12, bZ, obstacle.width() * 0.95, obstacle.height() * 0.95, cam);

        double frontLeft = front.x() - front.width() * 0.45;
        double frontRight = front.x() + front.width() * 0.45;
        double frontTop = front.y() - front.height() * 0.48;
        double frontBottom = front.y() + front.height() * 0.32;

        double backLeft = back.x() - back.width() * 0.45;
        double backRight = back.x() + back.width() * 0.45;
        double backTop = back.y() - back.height() * 0.48;
        double backBottom = back.y() + back.height() * 0.32;

        String topImg, sideImg, frontImg;
        boolean isFar = (obstacle.z() - cam.z) > 80.0;
        if (obstacle.avoidMethod() == Obstacle.AvoidMethod.CONTAINER) {
            topImg = isFar ? null : AssetManager.containerTop();
            sideImg = isFar ? null : AssetManager.containerSide();
            frontImg = AssetManager.containerFront();
        } else {
            topImg = isFar ? null : AssetManager.obstacleTrainSideIcon();
            sideImg = isFar ? null : AssetManager.obstacleTrainSideIcon();
            frontImg = AssetManager.obstacleTrainIcon();
        }

        if (frontTop > backTop) {
            double[] xPoints = {frontLeft, backLeft, backRight, frontRight};
            double[] yPoints = {frontTop, backTop, backTop, frontTop};
            if (topImg != null) {
                drawPerspectiveImage(gc, topImg, backLeft, backTop, backRight, backTop, frontRight, frontTop, frontLeft, frontTop);
            } else {
                gc.setFill(Color.rgb(23, 104, 160));
                gc.fillPolygon(xPoints, yPoints, 4);
            }
        }

        if (frontLeft > backLeft) {
            double[] xPoints = {frontLeft, backLeft, backLeft, frontLeft};
            double[] yPoints = {frontTop, backTop, backBottom, frontBottom};
            if (sideImg != null) {
                drawPerspectiveImage(gc, sideImg, backLeft, backTop, frontLeft, frontTop, frontLeft, frontBottom, backLeft, backBottom);
            } else {
                gc.setFill(Color.rgb(31, 136, 198));
                gc.fillPolygon(xPoints, yPoints, 4);
            }
        }

        if (frontRight < backRight) {
            double[] xPoints = {frontRight, backRight, backRight, frontRight};
            double[] yPoints = {frontTop, backTop, backBottom, frontBottom};
            if (sideImg != null) {
                drawPerspectiveImage(gc, sideImg, frontRight, frontTop, backRight, backTop, backRight, backBottom, frontRight, frontBottom);
            } else {
                gc.setFill(Color.rgb(18, 92, 142));
                gc.fillPolygon(xPoints, yPoints, 4);
            }
        }

        if (frontBottom < backBottom) {
            gc.setFill(Color.rgb(18, 74, 117));
            gc.fillPolygon(
                    new double[] {frontLeft, backLeft, backRight, frontRight},
                    new double[] {frontBottom, backBottom, backBottom, frontBottom},
                    4
            );
        }

        double[] frontX = {frontLeft, frontRight, frontRight, frontLeft};
        double[] frontY = {frontTop, frontTop, frontBottom, frontBottom};
        if (frontImg != null) {
            com.ycom.resource.AssetManager.draw(gc, frontImg, frontLeft, frontTop, frontRight - frontLeft, frontBottom - frontTop);
        } else {
            gc.setFill(Color.rgb(31, 136, 198));
            gc.fillRect(frontLeft, frontTop, frontRight - frontLeft, frontBottom - frontTop);
        }
    }

    private void drawRamp(GraphicsContext gc, RenderSnapshot obstacle, Projection front, Camera cam) {
        double fZ = obstacle.z() - obstacle.depth() / 2.0;
        double bZ = obstacle.z() + obstacle.depth() / 2.0;
        
        Projection pFrontBottomLeft = projector.project(obstacle.x() - obstacle.width() / 2.0, obstacle.y(), fZ, 0, 0, cam);
        Projection pFrontBottomRight = projector.project(obstacle.x() + obstacle.width() / 2.0, obstacle.y(), fZ, 0, 0, cam);
        
        Projection pBackTopLeft = projector.project(obstacle.x() - obstacle.width() / 2.0, obstacle.y() + obstacle.height(), bZ, 0, 0, cam);
        Projection pBackTopRight = projector.project(obstacle.x() + obstacle.width() / 2.0, obstacle.y() + obstacle.height(), bZ, 0, 0, cam);
        
        Projection pBackBottomLeft = projector.project(obstacle.x() - obstacle.width() / 2.0, obstacle.y(), bZ, 0, 0, cam);
        Projection pBackBottomRight = projector.project(obstacle.x() + obstacle.width() / 2.0, obstacle.y(), bZ, 0, 0, cam);
        
        boolean isFar = (obstacle.z() - cam.z) > 80.0;
        String rampTex = isFar ? null : "obs_ramp_tex";

        double[] slopeX = {pBackTopLeft.x(), pBackTopRight.x(), pFrontBottomRight.x(), pFrontBottomLeft.x()};
        double[] slopeY = {pBackTopLeft.y(), pBackTopRight.y(), pFrontBottomRight.y(), pFrontBottomLeft.y()};
        if (rampTex != null) {
            drawPerspectiveImage(gc, rampTex, 
                pBackTopLeft.x(), pBackTopLeft.y(), 
                pBackTopRight.x(), pBackTopRight.y(), 
                pFrontBottomRight.x(), pFrontBottomRight.y(), 
                pFrontBottomLeft.x(), pFrontBottomLeft.y());
        } else {
            gc.setFill(Color.rgb(150, 150, 150));
            gc.fillPolygon(slopeX, slopeY, 4);
        }

        if (pFrontBottomLeft.x() > pBackBottomLeft.x()) {
            double[] leftX = {pBackTopLeft.x(), pFrontBottomLeft.x(), pBackBottomLeft.x()};
            double[] leftY = {pBackTopLeft.y(), pFrontBottomLeft.y(), pBackBottomLeft.y()};
            if (rampTex != null) {
                gc.save();
                gc.beginPath();
                gc.moveTo(pBackTopLeft.x(), pBackTopLeft.y());
                gc.lineTo(pFrontBottomLeft.x(), pFrontBottomLeft.y());
                gc.lineTo(pBackBottomLeft.x(), pBackBottomLeft.y());
                gc.closePath();
                gc.clip();
                
                Projection pFrontTopLeft = projector.project(obstacle.x() - obstacle.width() / 2.0, obstacle.y() + obstacle.height(), fZ, 0, 0, cam);
                drawPerspectiveImage(gc, rampTex,
                    pBackTopLeft.x(), pBackTopLeft.y(),
                    pFrontTopLeft.x(), pFrontTopLeft.y(),
                    pFrontBottomLeft.x(), pFrontBottomLeft.y(),
                    pBackBottomLeft.x(), pBackBottomLeft.y()
                );
                gc.restore();
            } else {
                gc.setFill(Color.rgb(100, 100, 100));
                gc.fillPolygon(leftX, leftY, 3);
            }
        }
        
        if (pFrontBottomRight.x() < pBackBottomRight.x()) {
            double[] rightX = {pBackTopRight.x(), pFrontBottomRight.x(), pBackBottomRight.x()};
            double[] rightY = {pBackTopRight.y(), pFrontBottomRight.y(), pBackBottomRight.y()};
            if (rampTex != null) {
                gc.save();
                gc.beginPath();
                gc.moveTo(pBackTopRight.x(), pBackTopRight.y());
                gc.lineTo(pFrontBottomRight.x(), pFrontBottomRight.y());
                gc.lineTo(pBackBottomRight.x(), pBackBottomRight.y());
                gc.closePath();
                gc.clip();
                
                Projection pFrontTopRight = projector.project(obstacle.x() + obstacle.width() / 2.0, obstacle.y() + obstacle.height(), fZ, 0, 0, cam);
                drawPerspectiveImage(gc, rampTex,
                    pFrontTopRight.x(), pFrontTopRight.y(),
                    pBackTopRight.x(), pBackTopRight.y(),
                    pBackBottomRight.x(), pBackBottomRight.y(),
                    pFrontBottomRight.x(), pFrontBottomRight.y()
                );
                gc.restore();
            } else {
                gc.setFill(Color.rgb(100, 100, 100));
                gc.fillPolygon(rightX, rightY, 3);
            }
        }
    }
}
