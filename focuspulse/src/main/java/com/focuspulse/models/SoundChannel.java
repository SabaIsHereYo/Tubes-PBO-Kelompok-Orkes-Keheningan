package com.focuspulse.models;

import javafx.scene.control.Button;
import javafx.scene.control.Slider;
import javafx.scene.media.MediaPlayer;

/**
 * Model PBO untuk membungkus satu saluran suara (MediaPlayer + Slider + tombol kontrol opsional).
 */
public class SoundChannel {
    private final String name;
    private final MediaPlayer player;
    private final Slider slider;
    private final boolean customTrack;
    private Button playPauseBtn;
    private boolean pausedByUser = false;

    public SoundChannel(String name, MediaPlayer player, Slider slider, boolean customTrack) {
        this.name = name;
        this.player = player;
        this.slider = slider;
        this.customTrack = customTrack;
    }

    public String getName() {
        return name;
    }

    public MediaPlayer getPlayer() {
        return player;
    }

    public Slider getSlider() {
        return slider;
    }

    public boolean isCustomTrack() {
        return customTrack;
    }

    public void setPlayPauseBtn(Button playPauseBtn) {
        this.playPauseBtn = playPauseBtn;
    }

    public boolean isPausedByUser() {
        return pausedByUser;
    }

    public void setPausedByUser(boolean pausedByUser) {
        this.pausedByUser = pausedByUser;
        if (playPauseBtn != null) {
            playPauseBtn.setText(pausedByUser ? "▶" : "⏸");
        }
    }

    /**
     * Sinkronisasi status putar & volume berdasarkan slider dan status Mute All.
     */
    public void syncPlayback(boolean isGlobalMuted) {
        if (player == null || slider == null) return;

        double vol = slider.getValue();
        if (isGlobalMuted || pausedByUser || vol <= 0.0) {
            player.setVolume(0.0);
            if (player.getStatus() == MediaPlayer.Status.PLAYING) {
                player.pause();
            }
        } else {
            player.setVolume(vol);
            if (player.getStatus() != MediaPlayer.Status.PLAYING) {
                player.play();
            }
        }
    }

    /**
     * Menghentikan suara dan membersihkan memori saat track dihapus.
     */
    public void dispose() {
        if (player != null) {
            player.stop();
            player.dispose();
        }
    }
}
