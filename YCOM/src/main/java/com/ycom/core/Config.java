package com.ycom.core;

public class Config {
    // 逻辑屏幕宽度，用于统一缩放比例和UI布局
    public static final double LOGICAL_WIDTH = 1920.0;
    // 逻辑屏幕高度，用于统一缩放比例和UI布局
    public static final double LOGICAL_HEIGHT = 1080.0;

    // 固定时间步长（秒），目前为1/60秒，即60帧每秒，保证物理和逻辑更新的稳定性
    public static final double FIXED_TIMESTEP_SECONDS = 1.0 / 60.0;
    // 游戏基础速度，代表玩家向前的初始移动速度
    public static final double BASE_SPEED = 20.0;
    // 游戏基础时间缩放倍率，默认为1倍速
    public static final double BASE_TIME_SCALE = 1.0;
    // 游戏最大时间缩放倍率，限制游戏不会无限制变快
    public static final double MAX_TIME_SCALE = 1.75;
    // 难度随时间增加的时间间隔（秒），即每隔多久增加一次游戏速度
    public static final double TIME_SCALE_STEP_INTERVAL = 8.0;
    // 每次难度增加时，时间缩放倍率的增量
    public static final double TIME_SCALE_STEP_AMOUNT = 0.1;
    // 游戏胜利所需的分数，达到此分数将触发胜利结局
    public static final int GAME_WIN_SCORE = 10000;

    // 拾取每个金币增加的分数
    public static final int COIN_SCORE_VALUE = 10;
    // 在冲刺状态下撞碎障碍物所获得的额外分数
    public static final int BOOST_OBSTACLE_BREAK_SCORE = 25;
    // 冲刺道具生效时的世界时间倍率，使游戏整体运行变快
    public static final double BOOST_WORLD_RATE = 1.8;
    // 冲刺道具生效时的背景音乐播放倍率，让音乐节奏随之变快
    public static final double BOOST_BGM_RATE = 1.8;
    // 冲刺道具（能量饮料）的持续时间（秒）
    public static final double BOOST_DURATION = 3.0;
    // 磁铁道具的持续时间（秒），持续吸引周围的金币
    public static final double MAGNET_DURATION = 10.0;
    // 跑步机（分数倍增）道具的持续时间（秒）
    public static final double TREADMILL_DURATION = 10.0;
    // 激活分数倍增道具后的分数乘数倍率
    public static final double SCORE_MULTIPLIER = 2.0;
    // 玩家复活后获得的无敌时间（秒）
    public static final double REVIVE_INVINCIBLE_DURATION = 5.0;
    // 玩家复活时，清空前方障碍物的半径范围
    public static final double REVIVE_CLEAR_RADIUS = 20.0;
    // 玩家复活时，清空后方障碍物的距离范围
    public static final double REVIVE_CLEAR_BEHIND_DISTANCE = 1.5;
    // 使用金币复活的成本数组，依次为第一次、第二次、第三次复活的花费
    public static final int[] COIN_REVIVE_COSTS = { 300, 600, 1200 };
    // 全局主音量控制
    public static final double MASTER_AUDIO_VOLUME = 0.50;
    // 背景音乐音量，相对于主音量的比例
    public static final double BGM_VOLUME = MASTER_AUDIO_VOLUME * 0.80;
    // 音效混合播放时的音量上限，防止音效叠加过响
    public static final double SFX_MIX_VOLUME_CAP = BGM_VOLUME * 0.75;
    // 基础音效音量大小
    public static final double SFX_VOLUME = SFX_MIX_VOLUME_CAP * 0.60;
    // 音效播放的最小音量阈值，低于此值将不再播放
    public static final double SFX_MIN_PLAY_VOLUME = 0.06;
    // 音效爆发检测的时间窗口（秒），用于限制短时间内的音效播放数量
    public static final double SFX_BURST_WINDOW_SECONDS = 0.08;
    // 同类音效播放的最小间隔时间（秒），避免重音
    public static final double SFX_MIN_INTERVAL_SECONDS = 0.025;
    // 金币拾取音效的最小间隔时间（秒），专门针对金币密集拾取情况
    public static final double SFX_COIN_MIN_INTERVAL_SECONDS = 0.04;
    // 在爆发时间窗口内，同一音效允许的最大播放次数
    public static final int SFX_MAX_PLAYS_PER_BURST_WINDOW = 4;

    // 摄像机在Y轴上的高度位置
    public static final double CAMERA_Y = 4;
    // 摄像机在Z轴上相对于玩家的偏移距离，负数表示在玩家后方
    public static final double CAMERA_OFFSET_Z = -8.0;
    // 摄像机焦距，影响3D投影的透视程度
    public static final double FOCAL_LENGTH = 1000.0;

    // 跑道的最左侧车道索引
    public static final int MIN_LANE = -1;
    // 跑道的最右侧车道索引
    public static final int MAX_LANE = 1;
    // 单个车道的物理宽度
    public static final double LANE_WIDTH = 4.0;
    // 玩家切换车道时的横向移动速度
    public static final double LATERAL_SPEED = 15.0;
    // 世界重力加速度，值为负表示向下
    public static final double GRAVITY = -60.0;
    // 玩家起跳时的初速度
    public static final double JUMP_VELOCITY = 20.0;
    // 玩家滑铲动作的持续时间（秒）
    public static final double SLIDE_DURATION = 0.75;
    // 玩家的物理碰撞盒宽度
    public static final double PLAYER_WIDTH = 1.25;
    // 玩家站立时的物理碰撞盒高度
    public static final double PLAYER_STANDING_HEIGHT = 2.0;
    // 玩家滑铲时的物理碰撞盒高度，变矮以通过障碍
    public static final double PLAYER_SLIDING_HEIGHT = 1.0;
    // 玩家的物理碰撞盒深度（Z轴厚度）
    public static final double PLAYER_DEPTH = 1.0;
    // 玩家跑步动画的播放帧率（FPS）
    public static final double PLAYER_ANIMATION_FPS = 12.0;

    // 金币的物理碰撞盒宽度
    public static final double COIN_WIDTH = 0.7;
    // 金币的物理碰撞盒高度
    public static final double COIN_HEIGHT = 0.7;
    // 金币的物理碰撞盒深度
    public static final double COIN_DEPTH = 0.7;
    // 金币在世界中默认生成的Y轴高度
    public static final double DEFAULT_COIN_Y = 0.65;
    // 磁铁道具默认生成的Y轴高度
    public static final double DEFAULT_MAGNET_Y = 0.60;
    // 能量饮料（冲刺道具）默认生成的Y轴高度
    public static final double DEFAULT_ENERGY_DRINK_Y = 0.40;
    // 跑步机道具默认生成的Y轴高度
    public static final double DEFAULT_TREADMILL_Y = 0.45;
    // 复活胶囊道具默认生成的Y轴高度
    public static final double DEFAULT_REVIVAL_CAPSULE_Y = 0.55;
    // 随机道具盒子默认生成的Y轴高度
    public static final double DEFAULT_RANDOM_ITEM_Y = 0.50;
    
    // 磁铁道具的宽度
    public static final double MAGNET_WIDTH = 1.0;
    // 磁铁道具的高度
    public static final double MAGNET_HEIGHT = 1.0;
    // 磁铁道具的深度
    public static final double MAGNET_DEPTH = 1.0;
    // 能量饮料的宽度
    public static final double ENERGY_DRINK_WIDTH = 0.9;
    // 能量饮料的高度
    public static final double ENERGY_DRINK_HEIGHT = 1.2;
    // 能量饮料的深度
    public static final double ENERGY_DRINK_DEPTH = 0.9;
    // 复活胶囊的宽度
    public static final double REVIVAL_CAPSULE_WIDTH = 0.9;
    // 复活胶囊的高度
    public static final double REVIVAL_CAPSULE_HEIGHT = 1.1;
    // 复活胶囊的深度
    public static final double REVIVAL_CAPSULE_DEPTH = 0.9;
    // 跑步机的宽度
    public static final double TREADMILL_WIDTH = 1.1;
    // 跑步机的高度
    public static final double TREADMILL_HEIGHT = 0.9;
    // 跑步机的深度
    public static final double TREADMILL_DEPTH = 1.1;
    // 随机道具盒子的宽度
    public static final double RANDOM_ITEM_WIDTH = 1.0;
    // 随机道具盒子的高度
    public static final double RANDOM_ITEM_HEIGHT = 1.0;
    // 随机道具盒子的深度
    public static final double RANDOM_ITEM_DEPTH = 1.0;

    // 只能跳跃通过的障碍物宽度
    public static final double JUMP_OBSTACLE_WIDTH = 2.1;
    // 只能跳跃通过的障碍物高度
    public static final double JUMP_OBSTACLE_HEIGHT = 1.0;
    // 只能跳跃通过的障碍物深度
    public static final double JUMP_OBSTACLE_DEPTH = 1.2;
    // 只能滑铲通过的障碍物生成的Y轴高度
    public static final double SLIDE_OBSTACLE_Y = 1.25;
    // 只能滑铲通过的障碍物宽度
    public static final double SLIDE_OBSTACLE_WIDTH = 2.2;
    // 只能滑铲通过的障碍物高度
    public static final double SLIDE_OBSTACLE_HEIGHT = 1.0;
    // 只能滑铲通过的障碍物深度
    public static final double SLIDE_OBSTACLE_DEPTH = 1.1;
    // 阻塞整个车道（需要换道）的障碍物宽度
    public static final double LANE_BLOCK_WIDTH = 2.4;
    // 阻塞整个车道（需要换道）的障碍物高度
    public static final double LANE_BLOCK_HEIGHT = 3.0;
    // 阻塞整个车道（需要换道）的障碍物深度
    public static final double LANE_BLOCK_DEPTH = 7.0;
    // 斜坡障碍物底部中心的Z轴偏移量
    public static final double RAMP_Z_OFFSET = 5.5;
    // 斜坡障碍物宽度
    public static final double RAMP_WIDTH = 2.4;
    // 斜坡障碍物高度
    public static final double RAMP_HEIGHT = 3.0;
    // 斜坡障碍物深度
    public static final double RAMP_DEPTH = 4.0;

    // 游戏对象在落后玩家多远距离后会被销毁（优化性能）
    public static final double OBJECT_DESPAWN_BEHIND_DISTANCE = 35.0;
    // 磁铁对落后于玩家的金币的吸引距离
    public static final double MAGNET_RANGE_BEHIND_DISTANCE = 1.0;
    // 磁铁对前方金币的最大吸引探测距离
    public static final double MAGNET_RANGE_AHEAD_DISTANCE = 38.0;
    // 金币被磁铁吸引时，向玩家移动的目标Y轴高度偏移
    public static final double MAGNET_TARGET_Y_OFFSET = 0.8;
    // 磁铁的实际吸引生效半径
    public static final double MAGNET_ATTRACT_RADIUS = 45.0;
    // 金币飞向玩家时的最小基础速度
    public static final double MAGNET_PULL_MIN_SPEED = 36.0;
    // 金币飞向玩家速度随距离的乘数因子
    public static final double MAGNET_PULL_SPEED_MULTIPLIER = 2.0;

    // 生成各个关卡Chunk块之间的缓冲间隙距离
    public static final double CHUNK_BUFFER_GAP = 6.0;
    // 场景生成器提前在玩家前方多少距离生成对象
    public static final double SPAWN_LOOKAHEAD = 230.0;
    // 游戏开始时第一个对象的生成Z轴坐标
    public static final double INITIAL_SPAWN_Z = 50.0;
    // 两个复杂场景块(Scene Block)之间填充的普通Chunk块数量
    public static final int FILLER_CHUNKS_PER_SCENE_BLOCK = 7;
    // 两次生成强力道具之间的最大间隔时间（秒）
    public static final double MAX_POWERUP_GAP_SECONDS = 10.0;
    // 场景生成选择器的冷却窗口，防止连续生成相同类型的难点块
    public static final int CHUNK_PICKER_COOLDOWN_WINDOW = 2;
    // 进入游戏后期的阈值时间（秒），后期难度会增加
    public static final double LATE_GAME_THRESHOLD_SECONDS = 30.0;
    // 在游戏后期混合模式下，安全车道生成的随机权重，控制难度
    public static final int MIXED_SAFE_ROW_LATE_WEIGHT = 15;

    // 随机道具开出磁铁的概率
    public static final double RANDOM_ITEM_MAGNET_CHANCE = 0.35;
    // 随机道具开出冲刺饮料的概率
    public static final double RANDOM_ITEM_BOOST_CHANCE = 0.35;
    // 随机道具开出复活胶囊的概率
    public static final double RANDOM_ITEM_REVIVAL_CHANCE = 0.10;

    // 游戏难度枚举定义
    public enum Difficulty {
        // 简单难度参数配置
        EASY("Easy", 0.90, 15.5, 5.5, 0.16, 0.12, 0.06, 0.05, 0.05, 0.10, 45.0, 0.05, 0.02, 0.04),
        // 中等难度参数配置
        MEDIUM("Medium", 1.00, 14.0, 5.0, 0.24, 0.18, 0.14, 0.04, 0.04, 0.22, 25.0, 0.04, 0.015, 0.03),
        // 困难难度参数配置
        HARD("Hard", 1.15, 12.0, 4.0, 0.30, 0.24, 0.22, 0.03, 0.03, 0.36, 8.0, 0.03, 0.01, 0.02);

        // 难度显示的名称
        public final String label;
        // 该难度下的初始时间缩放倍率
        public final double initialTimeScale;
        // 基础生成间距，决定障碍物的密度
        public final double spawnBaseGap;
        // 随机生成间距浮动值
        public final double spawnRandomGap;
        // 生成需要跳跃的障碍物的概率
        public final double jumpObstacleChance;
        // 生成需要滑铲的障碍物的概率
        public final double slideObstacleChance;
        // 生成封路障碍物的概率
        public final double laneBlockObstacleChance;
        // 生成磁铁道具的概率
        public final double magnetChance;
        // 生成冲刺道具的概率
        public final double energyDrinkChance;
        // 游戏后期生成额外障碍物的概率增量
        public final double lateExtraObstacleChance;
        // 封路障碍物解锁所需的时间（秒）
        public final double laneBlockUnlockSeconds;
        // 生成跑步机（双倍分数）的概率
        public final double treadmillChance;
        // 生成复活胶囊的概率
        public final double revivalChance;
        // 生成随机道具盲盒的概率
        public final double randomItemChance;

        Difficulty(
                String label,
                double initialTimeScale,
                double spawnBaseGap,
                double spawnRandomGap,
                double jumpObstacleChance,
                double slideObstacleChance,
                double laneBlockObstacleChance,
                double magnetChance,
                double energyDrinkChance,
                double lateExtraObstacleChance,
                double laneBlockUnlockSeconds,
                double treadmillChance,
                double revivalChance,
                double randomItemChance
        ) {
            this.label = label;
            this.initialTimeScale = initialTimeScale;
            this.spawnBaseGap = spawnBaseGap;
            this.spawnRandomGap = spawnRandomGap;
            this.jumpObstacleChance = jumpObstacleChance;
            this.slideObstacleChance = slideObstacleChance;
            this.laneBlockObstacleChance = laneBlockObstacleChance;
            this.magnetChance = magnetChance;
            this.energyDrinkChance = energyDrinkChance;
            this.lateExtraObstacleChance = lateExtraObstacleChance;
            this.laneBlockUnlockSeconds = laneBlockUnlockSeconds;
            this.treadmillChance = treadmillChance;
            this.revivalChance = revivalChance;
            this.randomItemChance = randomItemChance;
        }
    }

    // 游戏默认难度
    public static final Difficulty DEFAULT_DIFFICULTY = Difficulty.MEDIUM;
}
