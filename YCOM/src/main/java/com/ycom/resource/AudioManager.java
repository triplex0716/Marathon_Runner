package com.ycom.resource;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import com.ycom.core.Config;
import javafx.application.Platform;
import javafx.scene.media.AudioClip;
import javafx.scene.media.Media;
import javafx.scene.media.MediaException;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;

public class AudioManager {
    private static final Map<String, AudioClip> SFX = new HashMap<>();

    private static MediaPlayer bgmPlayer;
    private static boolean bgmPlayRequested;
    private static double currentRate = 1.0;

    public static void init() {
        stopBGM();
        SFX.clear();

        bgmPlayer = loadBgm("BackgroundMusic.wav");
        loadSfx("coin", "coin.wav");
        loadSfx("fail", "fail.wav");
        loadSfx("invincible", "invincible.wav");
        loadSfx("win", "win.wav");
        loadSfx("obstacle_break", "obstacle_break.wav");
        loadSfx("ascension", "ascension.wav");
    }

    public static void playBGM() {
        if (bgmPlayer == null) {
            return;
        }
        bgmPlayRequested = true;
        runOnFxThread(() -> {
            bgmPlayer.setVolume(Config.BGM_VOLUME);
            bgmPlayer.setRate(currentRate);
            if (bgmPlayer.getStatus() == MediaPlayer.Status.READY
                    || bgmPlayer.getStatus() == MediaPlayer.Status.PAUSED
                    || bgmPlayer.getStatus() == MediaPlayer.Status.STOPPED) {
                bgmPlayer.play();
            }
        });
    }

    public static void stopBGM() {
        bgmPlayRequested = false;
        if (bgmPlayer != null) {
            runOnFxThread(() -> {
                bgmPlayer.stop();
                bgmPlayer.seek(Duration.ZERO);
            });
        }
    }

    public static void pauseBGM() {
        bgmPlayRequested = false;
        if (bgmPlayer != null) {
            runOnFxThread(bgmPlayer::pause);
        }
    }

    public static void setBgmRate(double rate) {
        double nextRate = Math.max(0.1, rate);
        if (Math.abs(nextRate - currentRate) < 0.001) {
            return;
        }
        currentRate = nextRate;
        if (bgmPlayer != null) {
            runOnFxThread(() -> bgmPlayer.setRate(currentRate));
        }
    }

    public static boolean hasSfx(String key) {
        return SFX.containsKey(key);
    }

    public static void playSfx(String key) {
        AudioClip clip = SFX.get(key);
        if (clip == null) {
            System.err.println("Missing SFX key: " + key);
            return;
        }
        clip.play(Config.SFX_VOLUME);
    }

    private static MediaPlayer loadBgm(String... fileNames) {
        Media media = loadMedia("BGM", fileNames);
        if (media == null) {
            return null;
        }

        try {
            MediaPlayer player = new MediaPlayer(media);
            player.setCycleCount(MediaPlayer.INDEFINITE);
            player.setVolume(Config.BGM_VOLUME);
            player.setRate(currentRate);
            player.setOnReady(() -> {
                if (bgmPlayRequested) {
                    player.play();
                }
            });
            player.setOnError(() -> System.err.println("BGM playback error: " + player.getError()));
            return player;
        } catch (MediaException ex) {
            System.err.println("Failed to create BGM player: " + ex.getMessage());
            return null;
        }
    }

    private static void loadSfx(String key, String... fileNames) {
        Path path = resolveFirst(fileNames);
        if (path == null) {
            System.err.println("Missing SFX " + key + " asset: " + String.join(", ", fileNames));
            return;
        }
        try {
            AudioClip clip = new AudioClip(path.toUri().toString());
            clip.setVolume(Config.SFX_VOLUME);
            SFX.put(key, clip);
        } catch (MediaException ex) {
            System.err.println("Failed to load SFX [" + key + "]: " + ex.getMessage());
        }
    }

    private static Media loadMedia(String label, String... fileNames) {
        Path path = resolveFirst(fileNames);
        if (path == null) {
            System.err.println("Missing " + label + " asset: " + String.join(", ", fileNames));
            return null;
        }

        try {
            Media media = new Media(path.toUri().toString());
            media.setOnError(() -> System.err.println(label + " media error [" + path + "]: " + media.getError()));
            return media;
        } catch (MediaException ex) {
            System.err.println("Failed to load " + label + " [" + path + "]: " + ex.getMessage());
            return null;
        }
    }

    private static Path resolveFirst(String... fileNames) {
        for (String fileName : fileNames) {
            Path path = AssetManager.resolve(fileName);
            if (path != null) {
                return path;
            }
        }
        return null;
    }

    private static void runOnFxThread(Runnable action) {
        if (Platform.isFxApplicationThread()) {
            action.run();
        } else {
            Platform.runLater(action);
        }
    }
}
