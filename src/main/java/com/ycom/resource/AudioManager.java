package com.ycom.resource;

import java.util.ArrayDeque;
import java.util.Deque;
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
    private static final Map<String, SfxState> SFX = new HashMap<>();
    private static final Deque<SfxPlayStamp> recentSfxPlays = new ArrayDeque<>();

    private static MediaPlayer bgmPlayer;
    private static boolean bgmPlayRequested;
    private static double currentRate = 1.0;

    public static void init() {
        stopBGM();
        SFX.clear();
        recentSfxPlays.clear();

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

    public static synchronized boolean hasSfx(String key) {
        return SFX.containsKey(key);
    }

    public static void playSfx(String key) {
        SfxPlay play = prepareSfxPlay(key);
        if (play == null) {
            return;
        }
        play.clip().play(play.volume());
    }

    private static SfxPlay prepareSfxPlay(String key) {
        long now = System.nanoTime();
        synchronized (AudioManager.class) {
            SfxState state = SFX.get(key);
            if (state == null) {
                System.err.println("Missing SFX key: " + key);
                return null;
            }

            if (now < state.nextAllowedNanos) {
                return null;
            }

            pruneRecentSfx(now);
            if (recentSfxPlays.size() >= Config.SFX_MAX_PLAYS_PER_BURST_WINDOW) {
                return null;
            }

            int overlapping = recentSfxPlays.size();
            double activeVolume = activeSfxVolume();
            double remainingMixBudget = Config.SFX_MIX_VOLUME_CAP - activeVolume;
            if (remainingMixBudget < Config.SFX_MIN_PLAY_VOLUME) {
                return null;
            }

            state.nextAllowedNanos = now + secondsToNanos(minIntervalFor(key));

            double volume = Math.min(Config.SFX_VOLUME / Math.sqrt(overlapping + 1.0), remainingMixBudget);
            recentSfxPlays.addLast(new SfxPlayStamp(now, volume));
            return new SfxPlay(state.clip, volume);
        }
    }

    private static void pruneRecentSfx(long now) {
        long windowNanos = secondsToNanos(Config.SFX_BURST_WINDOW_SECONDS);
        while (!recentSfxPlays.isEmpty() && now - recentSfxPlays.peekFirst().startedAtNanos() > windowNanos) {
            recentSfxPlays.removeFirst();
        }
    }

    private static double activeSfxVolume() {
        double volume = 0.0;
        for (SfxPlayStamp stamp : recentSfxPlays) {
            volume += stamp.volume();
        }
        return volume;
    }

    private static double minIntervalFor(String key) {
        return "coin".equals(key) ? Config.SFX_COIN_MIN_INTERVAL_SECONDS : Config.SFX_MIN_INTERVAL_SECONDS;
    }

    private static long secondsToNanos(double seconds) {
        return Math.max(0L, Math.round(seconds * 1_000_000_000.0));
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
        String url = resolveFirst(fileNames);
        if (url == null) {
            System.err.println("Missing SFX " + key + " asset: " + String.join(", ", fileNames));
            return;
        }
        try {
            AudioClip clip = new AudioClip(url);
            clip.setVolume(Config.SFX_VOLUME);
            SFX.put(key, new SfxState(clip));
        } catch (MediaException ex) {
            System.err.println("Failed to load SFX [" + key + "]: " + ex.getMessage());
        }
    }

    private static Media loadMedia(String label, String... fileNames) {
        String url = resolveFirst(fileNames);
        if (url == null) {
            System.err.println("Missing " + label + " asset: " + String.join(", ", fileNames));
            return null;
        }

        try {
            Media media = new Media(url);
            media.setOnError(() -> System.err.println(label + " media error [" + url + "]: " + media.getError()));
            return media;
        } catch (MediaException ex) {
            System.err.println("Failed to load " + label + " [" + url + "]: " + ex.getMessage());
            return null;
        }
    }

    private static String resolveFirst(String... fileNames) {
        for (String fileName : fileNames) {
            String url = AssetManager.resolve(fileName);
            if (url != null) {
                return url;
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

    private static final class SfxState {
        private final AudioClip clip;
        private long nextAllowedNanos;

        private SfxState(AudioClip clip) {
            this.clip = clip;
        }
    }

    private record SfxPlay(AudioClip clip, double volume) {
    }

    private record SfxPlayStamp(long startedAtNanos, double volume) {
    }
}
