import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.transform.Rotate;
import javafx.stage.Stage;

public class Demo extends Application {

    // 用于鼠标控制视角的变量
    private double mousePosX, mousePosY;
    private double mouseOldX, mouseOldY;

    @Override
    public void start(Stage stage) {
        // 1. 创建根节点和 3D 世界节点
        Group root = new Group();
        Group world = new Group(); // 所有的 3D 物体都放在 world 里
        root.getChildren().add(world);

        // 2. 初始化透视摄像机
        PerspectiveCamera camera = new PerspectiveCamera(true);
        camera.setTranslateZ(-1500); // 把摄像机往后拉，以便看清全貌
        camera.setNearClip(0.1);
        camera.setFarClip(10000.0);

        // 初始化旋转轴 (用于鼠标拖拽时旋转整个世界)
        Rotate rotateX = new Rotate(30, Rotate.X_AXIS); // 初始俯角 30度
        Rotate rotateY = new Rotate(45, Rotate.Y_AXIS); // 初始侧角 45度
        world.getTransforms().addAll(rotateX, rotateY);

        // 3. 构建 3D 方块矩阵
        int gridSize = 20;       // 20x20 的矩阵
        double boxSize = 30;     // 方块的边长
        double spacing = 40;     // 方块之间的间距
        Box[][] boxes = new Box[gridSize][gridSize];

        for (int x = 0; x < gridSize; x++) {
            for (int z = 0; z < gridSize; z++) {
                Box box = new Box(boxSize, boxSize, boxSize);
                // 将矩阵居中计算坐标
                box.setTranslateX((x - gridSize / 2.0) * spacing);
                box.setTranslateZ((z - gridSize / 2.0) * spacing);

                // 创建基于位置的彩色材质
                PhongMaterial material = new PhongMaterial();
                // 使用 HSB 色彩模型，基于 X 和 Z 坐标生成渐变色
                material.setDiffuseColor(Color.hsb((x + z) * 10, 0.8, 0.9));
                material.setSpecularColor(Color.WHITE); // 高光反光
                box.setMaterial(material);

                world.getChildren().add(box);
                boxes[x][z] = box;
            }
        }

        // 4. 添加灯光 (没有灯光的话 3D 物体会看起来像平面的)
        PointLight pointLight = new PointLight(Color.WHITE);
        pointLight.setTranslateX(200);
        pointLight.setTranslateY(-800);
        pointLight.setTranslateZ(-500);
        world.getChildren().add(pointLight);

        AmbientLight ambientLight = new AmbientLight(Color.color(0.4, 0.4, 0.4));
        world.getChildren().add(ambientLight);

        // 5. 创建 Scene 并开启深度缓冲 (depthBuffer = true 极其重要！)
        Scene scene = new Scene(root, 1024, 768, true, SceneAntialiasing.BALANCED);
        scene.setFill(Color.web("#1e1e1e")); // 深色背景
        scene.setCamera(camera);

        // 6. 实现鼠标拖拽旋转视角的交互逻辑
        scene.setOnMousePressed((MouseEvent me) -> {
            mouseOldX = me.getSceneX();
            mouseOldY = me.getSceneY();
        });
        scene.setOnMouseDragged((MouseEvent me) -> {
            mousePosX = me.getSceneX();
            mousePosY = me.getSceneY();
            // 根据鼠标位移修改 X 轴和 Y 轴的旋转角度
            rotateX.setAngle(rotateX.getAngle() - (mousePosY - mouseOldY) * 0.5);
            rotateY.setAngle(rotateY.getAngle() + (mousePosX - mouseOldX) * 0.5);
            mouseOldX = mousePosX;
            mouseOldY = mousePosY;
        });

        // 7. 开启主循环，利用正弦波公式驱动方块产生波浪动画
        AnimationTimer timer = new AnimationTimer() {
            private long startTime = -1;

            @Override
            public void handle(long now) {
                if (startTime < 0) startTime = now;
                double time = (now - startTime) / 1_000_000_000.0; // 转换为秒

                for (int x = 0; x < gridSize; x++) {
                    for (int z = 0; z < gridSize; z++) {
                        // 计算当前方块距离中心点的距离
                        double dx = x - gridSize / 2.0;
                        double dz = z - gridSize / 2.0;
                        double distance = Math.sqrt(dx * dx + dz * dz);

                        // 核心数学公式：Y = sin(时间 - 距离) * 振幅
                        double height = Math.sin(time * 3.0 - distance * 0.5) * 50;

                        // 更新方块的 Y 轴高度
                        boxes[x][z].setTranslateY(height);
                    }
                }
            }
        };
        timer.start();

        // 8. 显示窗口
        stage.setTitle("JavaFX 3D Kinetic Wave Demo");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}