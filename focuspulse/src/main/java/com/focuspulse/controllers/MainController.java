package com.focuspulse.controllers;

import com.focuspulse.models.SoundChannel;
import java.awt.Toolkit;
import java.io.File;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.FileChooser;
import javafx.util.Duration;

public class MainController implements Initializable {

    // 1. Root, Tema & Custom Wallpaper (JPG / PNG / GIF)
    @FXML private StackPane rootStack;
    @FXML private ImageView wallpaperView;
    @FXML private Button themeBtn;
    @FXML private Button wallpaperBtn;

    // 2. Jam & Tanggal Real-Time
    @FXML private Label clockLabel;
    @FXML private Label dateLabel;

    // 3. Timer Universal & 3 CheckBox
    @FXML private Button btn15m;
    @FXML private Button btn25m;
    @FXML private Button btn50m;
    @FXML private Spinner<Integer> minuteSpinner;
    @FXML private Label timerLabel;
    @FXML private Button startBtn;
    @FXML private Button pauseBtn;
    @FXML private Button resetBtn;
    @FXML private CheckBox stopSoundOnFinishCheck;
    @FXML private CheckBox alarmOnFinishCheck;
    @FXML private CheckBox autoRepeatCheck;

    // 4. Unified Ambient Sound Mixer
    @FXML private Button addTrackBtn;
    @FXML private Button muteAllBtn;
    @FXML private VBox mixerContainer;
    @FXML private Slider rainSlider;
    @FXML private Slider fireSlider;
    @FXML private Slider windSlider;

    // State Tema & Timer
    private boolean isLightTheme = false;
    private int durasiMenit = 25;
    private int sisaDetik = durasiMenit * 60;
    private Timeline timerTimeline;
    private boolean isTimerRunning = false;

    // State Audio Mixer
    private final List<SoundChannel> allChannels = new ArrayList<>();
    private boolean isGlobalMuted = false;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupThemeToggle();
        setupWallpaperPicker();
        setupClock();
        setupTimer();
        setupSoundMixer();
    }

    // =========================================================
    // 0. TOGGLE TEMA GELAP / TERANG (CUKUP TAMBAH/HAPUS 1 CLASS CSS)
    // =========================================================
    private void setupThemeToggle() {
        themeBtn.setOnAction(e -> {
            isLightTheme = !isLightTheme;
            if (isLightTheme) {
                rootStack.getStyleClass().add("light-theme");
                themeBtn.setText("🌙");
            } else {
                rootStack.getStyleClass().remove("light-theme");
                themeBtn.setText("☀");
            }
        });
    }

    // =========================================================
    // 1. CUSTOM WALLPAPER (MENDUKUNG JPG, PNG, & ANIMASI GIF)
    // =========================================================
    private void setupWallpaperPicker() {
        rootStack.widthProperty().addListener((obs, oldVal, newVal) -> updateWallpaperCoverSize());
        rootStack.heightProperty().addListener((obs, oldVal, newVal) -> updateWallpaperCoverSize());

        wallpaperBtn.setOnAction(e -> {
            FileChooser chooser = new FileChooser();
            chooser.setTitle("Pilih Wallpaper (JPG / PNG / GIF)");
            chooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("Image & GIF Files", "*.jpg", "*.jpeg", "*.png", "*.gif")
            );

            File file = chooser.showOpenDialog(rootStack.getScene().getWindow());
            if (file != null) {
                Image image = new Image(file.toURI().toString());
                wallpaperView.setImage(image);
                updateWallpaperCoverSize();
            }
        });
    }

    private void updateWallpaperCoverSize() {
        Image img = wallpaperView.getImage();
        if (img == null || img.getWidth() <= 0 || img.getHeight() <= 0) return;

        double paneW = rootStack.getWidth();
        double paneH = rootStack.getHeight();
        double scale = Math.max(paneW / img.getWidth(), paneH / img.getHeight());

        double fitW = img.getWidth() * scale;
        double fitH = img.getHeight() * scale;

        wallpaperView.setFitWidth(fitW);
        wallpaperView.setFitHeight(fitH);
        wallpaperView.setLayoutX((paneW - fitW) / 2.0);
        wallpaperView.setLayoutY((paneH - fitH) / 2.0);
    }

    // =========================================================
    // 2. JAM & TANGGAL BAHASA INDONESIA
    // =========================================================
    private void setupClock() {
        DateTimeFormatter timeFmt = DateTimeFormatter.ofPattern("HH:mm:ss");
        DateTimeFormatter dateFmt = DateTimeFormatter.ofPattern("EEEE, dd MMMM yyyy", Locale.forLanguageTag("id-ID"));

        Runnable updateClock = () -> {
            clockLabel.setText(LocalTime.now().format(timeFmt));
            dateLabel.setText(LocalDate.now().format(dateFmt));
        };

        updateClock.run();
        Timeline clockTimeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> updateClock.run()));
        clockTimeline.setCycleCount(Timeline.INDEFINITE);
        clockTimeline.play();
    }

    // =========================================================
    // 3. TIMER UNIVERSAL + 3 CHECKBOX AKSI OTOMATIS
    // =========================================================
    private void setupTimer() {
        SpinnerValueFactory<Integer> factory =
                new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 180, durasiMenit);
        minuteSpinner.setValueFactory(factory);

        minuteSpinner.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && !isTimerRunning) {
                setDuration(newVal);
            }
        });

        btn15m.setOnAction(e -> { minuteSpinner.getValueFactory().setValue(15); setDuration(15); });
        btn25m.setOnAction(e -> { minuteSpinner.getValueFactory().setValue(25); setDuration(25); });
        btn50m.setOnAction(e -> { minuteSpinner.getValueFactory().setValue(50); setDuration(50); });

        timerTimeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            if (sisaDetik > 0) {
                sisaDetik--;
                updateTimerLabel();
            } else {
                handleTimerFinished();
            }
        }));
        timerTimeline.setCycleCount(Timeline.INDEFINITE);

        startBtn.setOnAction(e -> {
            if (sisaDetik <= 0) {
                sisaDetik = durasiMenit * 60;
            }
            timerTimeline.play();
            isTimerRunning = true;
        });

        pauseBtn.setOnAction(e -> {
            timerTimeline.pause();
            isTimerRunning = false;
        });

        resetBtn.setOnAction(e -> {
            timerTimeline.stop();
            isTimerRunning = false;
            sisaDetik = durasiMenit * 60;
            updateTimerLabel();
        });
    }

    private void handleTimerFinished() {
        if (stopSoundOnFinishCheck.isSelected()) {
            stopAllSounds();
        }

        if (alarmOnFinishCheck.isSelected()) {
            playAlarmChime();
        }

        if (autoRepeatCheck.isSelected()) {
            sisaDetik = durasiMenit * 60;
            updateTimerLabel();
        } else {
            timerTimeline.stop();
            isTimerRunning = false;
            timerLabel.setText("00:00");
        }
    }

    private void playAlarmChime() {
        try {
            URL alarmUrl = getClass().getResource("/audio/alarm.wav");
            if (alarmUrl != null) {
                Media alarmMedia = new Media(alarmUrl.toExternalForm());
                MediaPlayer alarmPlayer = new MediaPlayer(alarmMedia);
                alarmPlayer.setVolume(0.85);
                alarmPlayer.setOnEndOfMedia(alarmPlayer::dispose);
                alarmPlayer.play();
                return;
            }
        } catch (Exception ignored) {
        }
        Toolkit.getDefaultToolkit().beep();
    }

    private void setDuration(int menit) {
        durasiMenit = menit;
        sisaDetik = durasiMenit * 60;
        updateTimerLabel();
    }

    private void updateTimerLabel() {
        int menit = sisaDetik / 60;
        int detik = sisaDetik % 60;
        timerLabel.setText(String.format("%02d:%02d", menit, detik));
    }

    // =========================================================
    // 4. UNIFIED AMBIENT SOUND MIXER (DEFAULT + CUSTOM '+')
    // =========================================================
    private void setupSoundMixer() {
        registerDefaultAudio("Hujan", "/audio/rain.mp3", rainSlider);
        registerDefaultAudio("Api Unggun", "/audio/fire.mp3", fireSlider);
        registerDefaultAudio("Angin", "/audio/wind.mp3", windSlider);

        muteAllBtn.setOnAction(e -> {
            isGlobalMuted = !isGlobalMuted;
            updateMuteAllIcon();
            for (SoundChannel channel : allChannels) {
                channel.syncPlayback(isGlobalMuted);
            }
        });

        addTrackBtn.setOnAction(e -> {
            FileChooser chooser = new FileChooser();
            chooser.setTitle("Tambah Audio Lokal (MP3 / WAV)");
            chooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("Audio Files", "*.mp3", "*.wav")
            );

            File file = chooser.showOpenDialog(rootStack.getScene().getWindow());
            if (file != null) {
                addCustomTrackRow(file);
            }
        });
    }

    private void registerDefaultAudio(String name, String resourcePath, Slider slider) {
        try {
            URL resource = getClass().getResource(resourcePath);
            if (resource == null) {
                System.err.println("File audio tidak ditemukan: " + resourcePath);
                return;
            }

            Media media = new Media(resource.toExternalForm());
            MediaPlayer player = new MediaPlayer(media);
            player.setCycleCount(MediaPlayer.INDEFINITE);

            SoundChannel channel = new SoundChannel(name, player, slider, false);
            allChannels.add(channel);

            slider.valueProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal.doubleValue() > 0 && isGlobalMuted) {
                    isGlobalMuted = false;
                    updateMuteAllIcon();
                }
                channel.syncPlayback(isGlobalMuted);
            });
        } catch (Exception e) {
            System.err.println("Gagal memuat " + resourcePath + ": " + e.getMessage());
        }
    }

    private void addCustomTrackRow(File file) {
        try {
            Media media = new Media(file.toURI().toString());
            MediaPlayer player = new MediaPlayer(media);
            player.setCycleCount(MediaPlayer.INDEFINITE);

            HBox row = new HBox(8);
            row.setAlignment(Pos.CENTER_LEFT);
            row.getStyleClass().add("mixer-row");

            String fullName = file.getName();
            String shortName = fullName.length() > 14 ? fullName.substring(0, 13) + "..." : fullName;

            Label nameLabel = new Label("🎵 " + shortName);
            nameLabel.setPrefWidth(115);
            nameLabel.getStyleClass().add("sound-label");
            nameLabel.setTooltip(new Tooltip(fullName));

            Button playPauseBtn = new Button("⏸");
            playPauseBtn.getStyleClass().add("btn-row-icon");
            playPauseBtn.setTooltip(new Tooltip("Play / Pause"));

            Slider volumeSlider = new Slider(0.0, 1.0, 0.7);
            HBox.setHgrow(volumeSlider, Priority.ALWAYS);

            Button deleteBtn = new Button("✕");
            deleteBtn.getStyleClass().add("btn-row-delete");
            deleteBtn.setTooltip(new Tooltip("Hapus audio"));

            SoundChannel channel = new SoundChannel(fullName, player, volumeSlider, true);
            channel.setPlayPauseBtn(playPauseBtn);
            allChannels.add(channel);

            channel.syncPlayback(isGlobalMuted);

            playPauseBtn.setOnAction(ev -> {
                boolean nextPaused = !channel.isPausedByUser();
                channel.setPausedByUser(nextPaused);
                if (!nextPaused && isGlobalMuted) {
                    isGlobalMuted = false;
                    updateMuteAllIcon();
                }
                channel.syncPlayback(isGlobalMuted);
            });

            volumeSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal.doubleValue() > 0 && isGlobalMuted) {
                    isGlobalMuted = false;
                    updateMuteAllIcon();
                }
                channel.syncPlayback(isGlobalMuted);
            });

            deleteBtn.setOnAction(ev -> {
                channel.dispose();
                allChannels.remove(channel);
                mixerContainer.getChildren().remove(row);
            });

            row.getChildren().addAll(nameLabel, playPauseBtn, volumeSlider, deleteBtn);
            mixerContainer.getChildren().add(row);
        } catch (Exception e) {
            System.err.println("Gagal menambah audio lokal: " + e.getMessage());
        }
    }

    private void stopAllSounds() {
        for (SoundChannel channel : allChannels) {
            if (channel.isCustomTrack()) {
                channel.setPausedByUser(true);
            } else {
                channel.getSlider().setValue(0.0);
            }
            channel.syncPlayback(isGlobalMuted);
        }
    }

    private void updateMuteAllIcon() {
        muteAllBtn.setText(isGlobalMuted ? "🔇" : "🔊");
    }
}
