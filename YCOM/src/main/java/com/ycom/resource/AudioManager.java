package com.ycom.resource;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import com.ycom.core.Config;
import javafx.application.Platform;
import javafx.scene.media.Media;
import javafx.scene.media.MediaException;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;

public class AudioManager {
    private static final Map<String, Media> SFX = new HashMap<>();
    private static final List<MediaPlayer> ACTIVE_SFX = new ArrayList<>();

    private static MediaPlayer bgmPlayer;
    private static boolean bgmPlayRequested;
    private static double currentRate = 1.0;

    public static void init() {
        stopBGM();
        disposeSfxPlayers();
        SFX.clear();

        bgmPlayer = loadBgm("BackgroundMusic.wav", "BackgroundMusic.mp3");
        loadSfx("coin", "coin.wav", "coin.mp3");
        loadSfx("fail", "fail.wav", "fail.mp3");
        loadSfx("invincible", "invincible.wav", "invincible.mp3");
        loadSfx("win", "win.wav", "win.mp3");
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

    public static void playSfx(String key) {
        Media media = SFX.get(key);
        if (media == null) {
            System.err.println("Missing SFX key: " + key);
            return;
        }

        runOnFxThread(() -> {
            try {
                cleanupFinishedSfx();
                MediaPlayer player = new MediaPlayer(media);
                player.setVolume(Config.SFX_VOLUME);
                player.setOnEndOfMedia(() -> disposeSfx(player));
                player.setOnError(() -> {
                    System.err.println("SFX playback error [" + key + "]: " + player.getError());
                    disposeSfx(player);
                });
                ACTIVE_SFX.add(player);
                player.play();
            } catch (MediaException ex) {
                System.err.println("Failed to play SFX [" + key + "]: " + ex.getMessage());
            }
        });
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
        Media media = loadMedia("SFX " + key, fileNames);
        if (media != null) {
            SFX.put(key, media);
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

    private static void cleanupFinishedSfx() {
        Iterator<MediaPlayer> iterator = ACTIVE_SFX.iterator();
        while (iterator.hasNext()) {
            MediaPlayer player = iterator.next();
            MediaPlayer.Status status = player.getStatus();
            if (status == MediaPlayer.Status.DISPOSED || status == MediaPlayer.Status.STOPPED) {
                player.dispose();
                iterator.remove();
            }
        }
    }

    private static void disposeSfx(MediaPlayer player) {
        player.stop();
        player.dispose();
        ACTIVE_SFX.remove(player);
    }

    private static void disposeSfxPlayers() {
        for (MediaPlayer player : ACTIVE_SFX) {
            player.stop();
            player.dispose();
        }
        ACTIVE_SFX.clear();
    }

    private static void runOnFxThread(Runnable action) {
        if (Platform.isFxApplicationThread()) {
            action.run();
        } else {
            Platform.runLater(action);
        }
    }
}
