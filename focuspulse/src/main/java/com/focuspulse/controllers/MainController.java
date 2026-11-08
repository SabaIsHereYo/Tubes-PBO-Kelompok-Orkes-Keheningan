package com.focuspulse.controllers;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.FileChooser;
import javafx.util.Duration;

public class MainController implements Initializable {

    @FXML private Label timerLabel;
    @FXML private Button startBtn;
    @FXML private Button pauseBtn;
    @FXML private Button resetBtn;

    @FXML private Slider rainSlider;
    @FXML private Slider fireSlider;
    @FXML private Slider windSlider;

    @FXML private Button browseBtn;
    @FXML private Slider customSlider;

    private int sisaDetik = 25 * 60;
    private Timeline timeline;

    private MediaPlayer rainPlayer;
    private MediaPlayer firePlayer;
    private MediaPlayer windPlayer;
    private MediaPlayer customPlayer;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTimer();
        setupAudio();
        setupCustomAudioPicker();
    }

    private void setupTimer() {
        timeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            if (sisaDetik > 0) {
                sisaDetik--;
                updateTimerLabel();
            } else {
                timeline.stop();
                timerLabel.setText("00:00");
            }
        }));
        timeline.setCycleCount(Timeline.INDEFINITE);

        startBtn.setOnAction(e -> timeline.play());
        pauseBtn.setOnAction(e -> timeline.pause());
        resetBtn.setOnAction(e -> {
            timeline.stop();
            sisaDetik = 25 * 60;
            updateTimerLabel();
        });
    }

    private void updateTimerLabel() {
        int menit = sisaDetik / 60;
        int detik = sisaDetik % 60;
        timerLabel.setText(String.format("%02d:%02d", menit, detik));
    }

    private void setupAudio() {
        rainPlayer = createLocalAudio("/audio/rain.mp3", rainSlider);
        firePlayer = createLocalAudio("/audio/fire.mp3", fireSlider);
        windPlayer = createLocalAudio("/audio/wind.mp3", windSlider);
    }

    private MediaPlayer createLocalAudio(String resourcePath, Slider slider) {
        try {
            URL resource = getClass().getResource(resourcePath);
            if (resource == null) {
                System.err.println("File audio belum ada di: " + resourcePath);
                return null;
            }

            Media media = new Media(resource.toExternalForm());
            MediaPlayer player = new MediaPlayer(media);
            player.setCycleCount(MediaPlayer.INDEFINITE);

            slider.valueProperty().addListener((obs, oldVal, newVal) -> {
                double vol = newVal.doubleValue();
                player.setVolume(vol);
                if (vol > 0 && player.getStatus() != MediaPlayer.Status.PLAYING) {
                    player.play();
                } else if (vol == 0 && player.getStatus() == MediaPlayer.Status.PLAYING) {
                    player.pause();
                }
            });
            return player;
        } catch (Exception e) {
            System.err.println("Gagal memuat audio " + resourcePath + ": " + e.getMessage());
            return null;
        }
    }

    private void setupCustomAudioPicker() {
        browseBtn.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Pilih File Audio (MP3 / WAV)");
            fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Audio Files", "*.mp3", "*.wav")
            );

            File file = fileChooser.showOpenDialog(browseBtn.getScene().getWindow());
            if (file != null) {
                if (customPlayer != null) {
                    customPlayer.stop();
                    customPlayer.dispose();
                }

                Media media = new Media(file.toURI().toString());
                customPlayer = new MediaPlayer(media);
                customPlayer.setCycleCount(MediaPlayer.INDEFINITE);

                customSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
                    double vol = newVal.doubleValue();
                    customPlayer.setVolume(vol);
                    if (vol > 0 && customPlayer.getStatus() != MediaPlayer.Status.PLAYING) {
                        customPlayer.play();
                    } else if (vol == 0 && customPlayer.getStatus() == MediaPlayer.Status.PLAYING) {
                        customPlayer.pause();
                    }
                });

                String fileName = file.getName();
                browseBtn.setText(fileName.length() > 10 ? fileName.substring(0, 10) + "..." : fileName);
            }
        });
    }
}