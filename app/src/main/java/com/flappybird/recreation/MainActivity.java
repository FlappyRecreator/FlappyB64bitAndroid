package com.flappybird.recreation;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.ColorFilter;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.LightingColorFilter;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.PorterDuffXfermode;
import android.graphics.RadialGradient;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.media.AudioAttributes;
import android.net.Uri;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.graphics.drawable.GradientDrawable;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.util.TypedValue;
import android.view.Choreographer;
import android.view.Display;
import android.view.Gravity;
import android.view.HapticFeedbackConstants;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.PointerIcon;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import static com.flappybird.recreation.SettingsActivity.*;

public class MainActivity extends AppCompatActivity implements GameView.BoardHost {

    private static boolean crashLoggerInstalled;

    private GameView gameView;
    private OnBackPressedCallback boardBack;
    private OnBackPressedCallback nameBack;
    private NamePanel namePanel;
    private PhysicsPanel physicsPanel;
    private OnBackPressedCallback physicsBack;
    private OnBackPressedCallback runBack;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        installCrashLogger();
        EdgeToEdge.enable(this);
        super.onCreate(savedInstanceState);
        hideSystemBars();

        setContentView(R.layout.main);
        FrameLayout gameContainer = findViewById(R.id.game_container);
        gameView = new GameView(this);
        gameContainer.addView(gameView);
        Typeface face = null;
        try {
            face = ResourcesCompat.getFont(this, R.font.flappy_font);
        } catch (RuntimeException ignored) {
        }
        namePanel = new NamePanel(this, face != null ? face : Typeface.DEFAULT_BOLD);
        gameContainer.addView(namePanel, new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
        physicsPanel = new PhysicsPanel(this, face != null ? face : Typeface.DEFAULT_BOLD);
        gameContainer.addView(physicsPanel, new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        runBack = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (gameView != null && gameView.pauseForBack()) return;
                setEnabled(false);
                getOnBackPressedDispatcher().onBackPressed();
                setEnabled(true);
            }
        };
        getOnBackPressedDispatcher().addCallback(this, runBack);
        boardBack = new OnBackPressedCallback(false) {
            @Override
            public void handleOnBackPressed() {
                if (gameView != null) gameView.closeBoardIfOpen();
            }
        };
        getOnBackPressedDispatcher().addCallback(this, boardBack);
        nameBack = new OnBackPressedCallback(false) {
            @Override
            public void handleOnBackPressed() {
                if (namePanel != null && namePanel.cancellable()) closeNamePanel();
            }
        };
        getOnBackPressedDispatcher().addCallback(this, nameBack);
        physicsBack = new OnBackPressedCallback(false) {
            @Override
            public void handleOnBackPressed() {
                if (physicsPanel != null) physicsPanel.choose(false);
            }
        };
        getOnBackPressedDispatcher().addCallback(this, physicsBack);
        gameView.setBoardHost(this);
        gameView.post(() -> {
            if (gameView != null && !isFinishing()) gameView.askFirstName();
        });
    }

    @Override
    public void boardVisible(boolean open) {
        if (boardBack != null) boardBack.setEnabled(open);
    }

    @Override
    public void askName(String current, boolean firstTime, final GameView.NameCallback callback) {
        if (isFinishing() || namePanel == null) return;
        if (gameView != null) gameView.setNameOverlay(true);
        namePanel.open(firstTime, current, gameView != null && gameView.nameBeside(), new NamePanel.Listener() {
            @Override
            public void submit(final String name) {
                SettingsActivity.Leaderboard.setName(MainActivity.this, name, MainActivity.this::runOnUiThread, (status, error) -> {
                    if (namePanel == null || !namePanel.isOpen()) return;
                    if (error != null) {
                        namePanel.showError(error);
                        return;
                    }
                    closeNamePanel();
                    if (SettingsActivity.Leaderboard.OFFLINE.equals(status)) {
                        Toast.makeText(MainActivity.this, "Name saved. It will be checked when you're online", Toast.LENGTH_SHORT).show();
                    }
                    callback.named(name);
                });
            }

            @Override
            public void cancel() {
                closeNamePanel();
            }
        });
        if (nameBack != null) nameBack.setEnabled(true);
    }

    @Override
    public void askPhysics() {
        if (isFinishing() || physicsPanel == null || physicsPanel.isOpen()) return;
        if (gameView != null) gameView.setNameOverlay(true);
        physicsPanel.open(classic -> {
            SharedPreferences.Editor editor = getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit();
            if (classic) SettingsActivity.writeClassicFeel(editor);
            editor.putBoolean(KEY_PHYSICS_CHOSEN, true).apply();
            if (physicsBack != null) physicsBack.setEnabled(false);
            if (gameView != null) {
                gameView.setNameOverlay(false);
                gameView.physicsChosen(classic);
            }
        });
        if (physicsBack != null) physicsBack.setEnabled(true);
    }

    private void closeNamePanel() {
        if (namePanel != null) namePanel.close(gameView != null && gameView.nameBeside());
        if (nameBack != null) nameBack.setEnabled(false);
        if (gameView != null) gameView.setNameOverlay(false);
    }

    @Override
    public int namePanelHeight(float unit) {
        return namePanel == null ? 0 : namePanel.panelHeight(unit);
    }

    @Override
    public void placeNamePanel(int left, int top, float unit) {
        if (namePanel != null) namePanel.place(left, top, unit);
    }

    @Override
    public boolean namePanelShowing() {
        return namePanel != null && namePanel.getVisibility() == View.VISIBLE;
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        requestHighestRefreshRate();
    }

    private void installCrashLogger() {
        if (crashLoggerInstalled) return;
        crashLoggerInstalled = true;
        final Context app = getApplicationContext();
        final Thread.UncaughtExceptionHandler previous = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler((thread, error) -> {
            try {
                SettingsActivity.ActivityLog.addNow(app, "CRASH", "The app crashed on thread \"" + thread.getName() + "\" ("
                        + SettingsActivity.deviceSummary(app) + "): " + Log.getStackTraceString(error));
            } catch (Throwable ignored) {
            }
            if (previous != null) previous.uncaughtException(thread, error);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        hideSystemBars();
        if (gameView != null) gameView.resume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (gameView != null) gameView.pause();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) hideSystemBars();
        else if (gameView != null) gameView.focusLost();
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (physicsPanel != null && physicsPanel.isOpen() && physicsPanel.key(event)) return true;
        return super.dispatchKeyEvent(event);
    }

    void feel(int kind) {
        if (gameView != null) gameView.feel(kind);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (gameView != null && gameView.onGameKey(keyCode, event, true)) return true;
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (gameView != null && gameView.onGameKey(keyCode, event, false)) return true;
        return super.onKeyUp(keyCode, event);
    }

    private void hideSystemBars() {
        WindowInsetsControllerCompat controller =
                WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
        controller.setSystemBarsBehavior(WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
        controller.hide(WindowInsetsCompat.Type.systemBars());
    }

    private void requestHighestRefreshRate() {
        SettingsActivity.preferSmoothDisplay(this);
    }
}

class GameView extends View implements Choreographer.FrameCallback {

    interface NameCallback {
        void named(String name);
    }

    interface BoardHost {
        void boardVisible(boolean open);

        void askName(String current, boolean firstTime, NameCallback callback);

        int namePanelHeight(float unit);

        void placeNamePanel(int left, int top, float unit);

        boolean namePanelShowing();

        void askPhysics();
    }

    private static final int BOARD_ROWS = 10;
    private static final float BOARD_ROW_HEIGHT = 19f;
    private static final float BOARD_WIDTH_UNITS = 236f;
    private static final float BOARD_HEIGHT_UNITS = 150f + (BOARD_ROWS + 1) * BOARD_ROW_HEIGHT;
    private static final float BOARD_SIDE_GAP = 10f;
    private static final float BOARD_PAIR_UNITS = BOARD_WIDTH_UNITS + BOARD_SIDE_GAP + NamePanel.WIDTH_UNITS * 0.95f + 16f;
    private static final float BOARD_SIDE_MIN_FIT = 0.8f;
    private static final float BOARD_CACHE_PIXELS = 4500000f;
    private static final int STATUS_PILL = 0xCC0D1321;
    private static final String BOARD_OFFLINE_NOTE = "No internet connection";
    private static final float BOARD_RETRY_SEC = 5f;
    private static final int BOARD_PINNED_ROW = Integer.MAX_VALUE;
    private static final long DOUBLE_TAP_MS = 350L;
    private static final float BUBBLE_FADE_IN_SEC = 0.15f;
    private static final float BUBBLE_HOLD_SEC = 2.2f;
    private static final float BUBBLE_FADE_SEC = 0.35f;
    private static final float STATUS_FADE_IN_SEC = 0.18f;
    private static final float STATUS_HOLD_SEC = 2f;
    private static final float STATUS_FADE_OUT_SEC = 0.4f;
    private static final float RETURN_HOME_HOLD_SEC = 0.3f;
    private static final float BOARD_OPEN_SEC = 0.32f;
    private static final float BOARD_SWITCH_SEC = 0.3f;
    private static final float BOARD_PILL_SEC = 0.36f;
    private static final float BOARD_CLOSE_SEC = 0.18f;
    private static final int BOARD_TAB_IDLE = 0xFFC9C27E;
    private static final int BOARD_DIVIDER = 0x55543847;
    private static final int BOARD_PANEL = 0xFFDED895;
    private static final int BOARD_PANEL_EDGE = 0xFFF8F0B0;
    private static final int BOARD_DARK = 0xFF543847;
    private static final int BOARD_ORANGE = 0xFFFC7858;
    private static final int BOARD_OWN_ROW = 0x40FC7858;
    private static final int BOARD_VERIFIED = 0xFF8E5CF7;
    private static final int[][] MEDAL_COLORS = {
            {0xFFFFE07A, 0xFFF2B01E, 0xFFB97A00, 0xFF7A4E00, 0xFFE0453A},
            {0xFFF6F8FB, 0xFFC4CBD4, 0xFF8E98A5, 0xFF505A66, 0xFF3F7BD9},
            {0xFFF6C28F, 0xFFD08443, 0xFF9C5A22, 0xFF5E3210, 0xFF2E9E62}};
    private static final int CHAMPION_TEXT = 0xFF8A5A00;
    private static final int CHAMPION_SHINE = 0xFFFFD45C;
    private static final int CHAMPION_GLOW = 0xFFFFC21A;
    private static final float CHAMPION_SHINE_SEC = 3f;
    private static final float CHAMPION_PULSE_SEC = 2f;
    private static final String BOARD_SHOW_ALL_KEY = "boardShowUnverified";
    private static final int BOARD_DIM = 0x99000000;
    static final int BOARD_MIN_SCORE = 5;
    private static final long BOARD_VERIFY_FRESH_MS = 8000L;
    private static final float BOARD_VERIFY_POLL_SEC = 8f;
    private static final long BOARD_VERIFY_WATCH_MS = 300000L;
    private static final String BOARD_MEMORY_FILE = "board_seen.bin";
    private static final Object BOARD_MEMORY_LOCK = new Object();
    private static final float BOARD_MOTION_DELAY = 0.12f;
    private static final float BOARD_MOTION_START = 0.85f;
    private static final float BOARD_SLIDE_SEC = 0.62f;
    private static final float BOARD_COUNT_DELAY = 0.06f;
    private static final float BOARD_COUNT_MIN_SEC = 0.42f;
    private static final float BOARD_COUNT_MAX_SEC = 1.15f;
    private static final float BOARD_POP_SEC = 0.3f;
    private static final float BOARD_GLOW_FADE_SEC = 0.7f;
    private static final float BOARD_GAIN_SEC = 1.3f;
    private static final float BOARD_ARROW_SEC = 2.2f;
    private static final float BOARD_ENTER_SEC = 0.3f;
    private static final float BOARD_ENTER_STAGGER = 0.035f;
    private static final long BOARD_TICK_MS = 38L;
    private static final int BOARD_GAIN = 0xFF34A853;
    private static final int BOARD_DROP = 0xFFD9534A;
    private static final int BOARD_LIFT_SHADOW = 0x40543847;
    private static final int HAPTIC_TICK = 0;
    private static final int HAPTIC_LAND = 1;
    private static final int HAPTIC_RISE = 2;
    private static BoardMemory boardMemory;

    private static final String TAG = "GameView";

    public static final String PREF_LIGHTNING_SCORES = "pref_lightning_scores";
    public static final String PREF_MEGA_STRIKE_SCORE = "pref_mega_strike_score";
    private static final String PREF_HIGH_SCORE = "highScore";
    private static final String DEFAULT_LIGHTNING_SCORES = "11,34,79,210";
    private static final int DEFAULT_MEGA_STRIKE_SCORE = 350;

    private static final float TARGET_FPS = 120.0f;
    private static final float MAX_FRAME_DELTA_SEC = 1.0f / 30.0f;

    private static final float GRAVITY_PER_FRAME = 0.10f;
    private static final float FLAP_VELOCITY_PER_FRAME = -3.2f;
    private static final float PIPE_SPEED_PER_FRAME = 1.1f;
    private static final float ROTATION_DELAY_VELOCITY_PER_FRAME = 2.7f;
    private static final float JETPACK_THRUST_FACTOR = 1.3f;
    private static final float ROTATION_UP_SPEED_PER_SEC = 6.0f * TARGET_FPS;
    private static final float ROTATION_DOWN_SPEED_PER_SEC = 2.5f * TARGET_FPS;
    private static final float DEATH_ROTATION_SPEED_PER_SEC = 2.8f * TARGET_FPS;
    private static final float BIRD_HITBOX_PADDING_X = 0.10f;
    private static final float BIRD_HITBOX_PADDING_Y = 0.10f;
    private static final float BIRD_BOB_AMPLITUDE = 6f;
    private static final double BIRD_BOB_SPEED = 5.0;
    private static final float CEILING_OVERSHOOT = 0.8f;

    private static final float PIPE_GAP_BIRD_HEIGHT_MULTIPLIER = 2.2f;
    private static final int UNSCALED_BIRD_HEIGHT_FOR_GAP = 48;
    private static final float PIPE_SPACING_FACTOR = 0.68f;
    private static final float PIPE_SPAWN_DELAY_SECONDS = 1.5f;
    private static final float FIRST_PIPE_REFERENCE_DISTANCE = 2f / 3f;
    private static final int MIN_PIPE_COUNT = 5;
    private static final int MAX_PIPE_COUNT = 64;
    private static final float PIPE_MOVE_RANGE_PERCENT = 0.08f;
    private static final float PIPE_MOVE_SPEED_TIER_1 = 0.03f;
    private static final float PIPE_MOVE_SPEED_TIER_2 = 0.06f;
    private static final float PIPE_MOVE_RAMP_PER_FRAME = 0.15f;
    private static final float PIPE_MOVE_DURATION_SEC = 30f;
    private static final float PIPE_STOP_DECAY_PER_FRAME = 0.9f;
    private static final float PIPE_WAVE_LENGTH = 500f;

    private static final float UI_MARGIN_HORIZONTAL_PERCENT = 0.025f;
    private static final float HOME_BUTTON_GAP_PERCENT = 0.05f;
    private static final float SETTINGS_BUTTON_SCALE_MULTIPLIER = 0.75f;
    private static final float SCORE_PANEL_NUMBER_SCALE_MULTIPLIER = 0.6f;
    private static final int SCORE_DIGIT_OVERLAP = 4;
    private static final int PANEL_DIGIT_GAP = 2;
    private static final int[][] ATLAS_PANEL_DIGITS = {
            {274, 612, 14, 20}, {278, 954, 10, 20}, {274, 978, 14, 20}, {262, 1002, 14, 20}, {1004, 0, 14, 20},
            {1004, 24, 14, 20}, {1010, 52, 14, 20}, {1010, 84, 14, 20}, {586, 484, 14, 20}, {622, 412, 14, 20}};
    private static final int[] ATLAS_TUTORIAL = {584, 182, 114, 98};
    private static final float TUTORIAL_ABOVE_BIRD = 22f;
    private static final float READY_FADE_SEC = 0.25f;
    private static final float READY_SPACE_ABOVE = 0.58f;
    private static final int SCORE_PANEL_CURRENT_SCORE_Y_OFFSET = 55;
    private static final int SCORE_PANEL_HIGH_SCORE_Y_OFFSET = 98;
    private static final int SCORE_PANEL_MEDAL_X_OFFSET = 32;
    private static final int SCORE_PANEL_MEDAL_Y_OFFSET = 43;
    private static final int SCORE_PANEL_NEW_X_OFFSET = 142;
    private static final int SCORE_PANEL_NEW_Y_OFFSET = 60;
    private static final int MENU_TITLE_Y = 150;
    private static final int MENU_BIRD_Y = 228;
    private static final int MENU_RATE_Y = 270;
    private static final int MENU_BUTTONS_Y = 340;
    private static final int ATLAS_RATE_X = 924, ATLAS_RATE_Y = 0, ATLAS_RATE_WIDTH = 74, ATLAS_RATE_HEIGHT = 48;
    private static final int ATLAS_RESUME_X = 668, ATLAS_RESUME_Y = 284, ATLAS_RESUME_WIDTH = 26, ATLAS_RESUME_HEIGHT = 28;
    private static final float RESUME_BUTTON_SHARE = 0.13f;
    private static final float PAUSE_FADE_SEC = 0.16f;
    private static final int PAUSE_DIM_ALPHA = 120;
    private static final int ATLAS_NEW_X = 224, ATLAS_NEW_Y = 1002, ATLAS_NEW_WIDTH = 32, ATLAS_NEW_HEIGHT = 14;
    private static final String RATE_URL = "https://github.com/FlappyRecreator/FlappyB64bitAndroid";
    private static final String RATE_MESSAGE = "No browser found to open the project page.";
    private static final int FPS_COLOR_GOOD = 0xFF5BE35B;
    private static final int FPS_COLOR_OK = 0xFFFFD23F;
    private static final int FPS_COLOR_BAD = 0xFFFF5A4F;
    private static final float FPS_GOOD_SHARE = 0.9f;
    private static final float FPS_OK_SHARE = 0.6f;
    private static final float FALLBACK_REFRESH_RATE = 60f;
    private static final float CURSOR_HIDE_DELAY_SEC = 1.2f;
    private static final float PLATINUM_OFFSET_X = -1.0f;
    private static final float PLATINUM_OFFSET_Y = 1.0f;
    private static final float GOLD_OFFSET_X = -1.0f;
    private static final float GOLD_OFFSET_Y = 1.0f;
    private static final int MIN_PLAYABLE_HEIGHT_PX = 48;

    private static final float BUTTON_PRESS_SPEED = 25f;
    private static final float SETTINGS_PRESSED_SCALE = 0.86f;
    private static final float SETTINGS_PRESS_TURN = 60f;
    private static final long SETTINGS_OPEN_DELAY_MS = 140L;
    private static final float TRANSITION_FADE_SPEED_PER_SEC = 800.0f;
    private static final float FLASH_DECAY_PER_SEC = 1500f;
    private static final int[] CRASH_FLASH_COLORS = {Color.WHITE, 0xFFFF3B30, 0xFFFFD23F, Color.BLACK};
    private static final long FRAME_LIMIT_RESET_NANOS = 250_000_000L;
    private static final int MAX_FIXED_STEPS = 8;
    private static final float STEP_SNAP = 0.03f;
    private static final float STEP_EPSILON = 0.00002f;
    private static final float PIXEL_STEP_EPSILON = 0.001f;
    private static final float CLASSIC_TILT_KICK_PER_SEC = -600f;
    private static final float CLASSIC_TILT_PULL_PER_SEC2 = 1440f;
    private static final float ORIGINAL_STEP_SEC = 1f / 60f;
    private static final float SQUARE_HITBOX_SIZE = 20f;
    private static final float FPS_REFRESH_SEC = 0.5f;
    private static final float FPS_DIGIT_SCALE = 0.8f;
    private static final int MIN_DAY_NIGHT_SEC = 5;
    private static final float FLAP_ANIMATION_TIMEOUT_SEC = 0.480f;
    private static final float WING_FRAME_SEC_PLAYING = 0.075f;
    private static final float WING_FRAME_SEC_IDLE = 0.150f;
    private static final float PANEL_SLIDE_DELAY_SEC = 0.100f;
    private static final float GAME_OVER_MAX_FALL_SEC = 4.0f;
    private static final float PANEL_SLIDE_EASING_BASE = 0.95f;
    private static final float GAMEOVER_ICON_SLIDE_DELAY_SEC = 0.025f;
    private static final float SCORE_TICK_SEC_FAST = 0.008f;
    private static final float SCORE_TICK_SEC_SLOW = 0.045f;
    private static final float SPARKLE_DURATION = 0.6f;
    private static final float SPARKLE_INTERVAL = 2.0f;

    private static final int MEDAL_NONE = -1;
    private static final int MEDAL_PLATINUM = 0;
    private static final int MEDAL_GOLD = 1;
    private static final int MEDAL_SILVER = 2;
    private static final int MEDAL_BRONZE = 3;

    private static final int WEATHER_RAIN = 1;
    private static final int WEATHER_STORM = 2;
    private static final int WEATHER_SNOW = 3;
    private static final int APOCALYPSE_SCORE = 600;
    private static final float REFERENCE_SCALE = 4.5f;
    private static final float SMOKE_PER_SEC = 0.50f * TARGET_FPS;
    private static final float BURNT_SPARKS_PER_SEC = 0.40f * TARGET_FPS;
    private static final float APOCALYPSE_SPARKS_PER_SEC = 0.30f * TARGET_FPS;
    private static final float LIGHTNING_PER_SEC = TARGET_FPS / 300f;
    private static final float LIGHTNING_PER_SEC_APOCALYPSE = TARGET_FPS / 100f;
    private static final int MAX_TRAIL_PARTICLES = 15;
    private static final int MAX_SMOKE_PARTICLES = 160;
    private static final int MAX_SPARK_PARTICLES = 192;
    private static final int MAX_ELECTRICITY_SEGMENTS = 96;
    private static final int HUE_FILTER_STEPS = 120;
    private static final int MAX_EXTRA_LIVES = 9;
    private static final float INVULNERABLE_SEC = 2.0f;
    private static final float LIFE_ICON_SCALE = 0.6f;
    private static final float PAUSE_BUTTON_SIZE = 22f;
    private static final float PILOT_STEP_SEC = 1.0f / 60.0f;
    private static final int PILOT_STEPS = 96;
    private static final float PILOT_CRASH_COST = 1000f;
    private static final float PILOT_FLAP_COOLDOWN_SEC = 0.12f;
    private static final float PILOT_MARGIN = 0.1f;
    private static final float PILOT_MARGIN_FIXED_STEP = 0.04f;
    private static final int[] PILOT_WAITS = {0, 3, 6, 9, 12, 16, 20, 25, 30, 38, 45};
    private static final float MOTION_BLUR_SHUTTER_SEC = 1.0f / 48.0f;
    private static final float MOTION_BLUR_MAX_STRENGTH = 10.0f;
    private static final float MOTION_BLUR_MAX_LENGTH = 48.0f;
    private static final float MOTION_BLUR_MAX_ROTATION = 60.0f;
    private static final float MOTION_BLUR_SAMPLE_SPACING = 1.0f;
    private static final int MOTION_BLUR_MAX_SAMPLES = 12;

    private enum GameState { HOME, TRANSITION_TO_WAITING, WAITING, PLAYING, GAME_OVER, PANEL_SLIDING }

    private Choreographer choreographer;
    private GameState gameState = GameState.HOME;
    private boolean hostResumed = false;
    private boolean isRunning = false;
    private boolean isReady = false;
    private long lastFrameTimeNanos = 0;
    private double clock = 0;

    private int screenWidth, screenHeight;
    private int laidOutWidth, laidOutHeight;
    private int windowInsetTop, windowInsetBottom, windowInsetLeft, windowInsetRight;
    private int systemBarTop, systemBarBottom, safeInsetLeft, safeInsetRight;
    private float scale;
    private float fxScale = 1.0f;
    private float uiScaleCorrection = 1.0f;
    private float uiMargin, cornerMargin, homeButtonGap, uiAvailableWidth;
    private float pressOffsetY = 0;
    private int groundHeight;
    private float bitmapScale = -1f;
    private int bitmapScreenHeight = -1;

    private Paint pixelPaint, birdPaint, smoothPaint, trailPaint, smokePaint, sparkPaint;
    private Paint nightBgPaint, goldenPipePaint, electricityPipePaint, electricityGlowPaint;
    private Paint transitionPaint, flashPaint, darkenPaint;
    private Paint hudPlatePaint, hudIconPaint, hitboxPaint;
    private final Path hudIconPath = new Path();
    private final RectF hudRect = new RectF();
    private Shader goldenPipeShader;
    private ColorFilter burntColorFilter;
    private final ColorFilter[] pipeColorFilters = new ColorFilter[PIPE_COLOR_COUNT];
    private final ColorFilter[] hueFilters = new ColorFilter[HUE_FILTER_STEPS];
    private final ColorMatrix colorMatrix = new ColorMatrix();
    private final Matrix birdMatrix = new Matrix();
    private final Matrix gradientMatrix = new Matrix();
    private final RectF groundDestRect = new RectF();
    private final RectF playButtonDestRect = new RectF();
    private final RectF scoreButtonDestRect = new RectF();
    private final RectF gameOverDestRect = new RectF();
    private final RectF panelDestRect = new RectF();
    private final RectF medalDestRect = new RectF();
    private final RectF restartBtnDestRect = new RectF();
    private final RectF scoreDigitDestRect = new RectF();
    private final RectF centeredBitmapDestRect = new RectF();
    private final RectF settingsButtonDestRect = new RectF();
    private final RectF pipeDestRect = new RectF();
    private final RectF sparkleDest = new RectF();
    private final RectF smokeDestRect = new RectF();
    private final Rect buttonSrcRect = new Rect();
    private final Rect pipeEdgeSrcRect = new Rect();
    private final float[] electricityPoints = new float[MAX_ELECTRICITY_SEGMENTS * 4];
    private final int[] digitBuffer = new int[10];

    private Bitmap unscaledBgDay, unscaledBgNight, unscaledLand;
    private Bitmap unscaledPipeGreen;
    private final Bitmap[][] unscaledAllBirdBitmaps = new Bitmap[3][3];
    private Bitmap unscaledTextReady, unscaledTextGameOver, unscaledScorePanel, unscaledButtonPlay;
    private Bitmap unscaledButtonScore, unscaledTitle, unscaledCopyright, unscaledButtonSettings;
    private Bitmap unscaledButtonRate, unscaledNewLabel, unscaledButtonResume;
    private final Bitmap[] unscaledMedalsBitmaps = new Bitmap[4];
    private final Bitmap[] unscaledNumberBitmaps = new Bitmap[10];

    private Bitmap bgDayBitmap, bgNightBitmap, groundBitmap;
    private Bitmap pipeUpBitmap, pipeDownBitmap;
    private final Bitmap[][] scaledAllBirdBitmaps = new Bitmap[3][3];
    private final Bitmap[] birdBitmaps = new Bitmap[3];
    private Bitmap getReadyBitmap, gameOverBitmap, scorePanelBitmap, playButtonBitmap;
    private Bitmap scoreButtonBitmap, titleBitmap, copyrightBitmap, settingsButtonBitmap;
    private Bitmap rateButtonBitmap, newLabelBitmap;
    private final Bitmap[] medalBitmaps = new Bitmap[4];
    private final Bitmap[] numberBitmaps = new Bitmap[10];
    private final Bitmap[] smallNumberBitmaps = new Bitmap[10];
    private final Bitmap[] panelNumberBitmaps = new Bitmap[10];
    private final Bitmap[] unscaledPanelNumbers = new Bitmap[10];
    private Bitmap unscaledTutorial, tutorialBitmap;
    private final Paint readyPaint = newPixelPaint();
    private final RectF readyDestRect = new RectF();
    private Bitmap sparkleBitmap, smokeBitmap;

    private int birdFrame = 0;
    private int currentBirdColor;
    private float birdX, birdY, birdVelocityY, birdRotation;
    private float baseBirdX;
    private float gameplayBirdX;
    private double lastFlapTime = -1000;
    private final Rect birdRect = new Rect();
    private boolean isScreenPressed = false;
    private float drunkModePhase = 0f;

    private final List<Pipe> pipes = new ArrayList<>();
    private int pipeSpacing, pipeGap;
    private float groundX = 0, backgroundX = 0;
    private boolean nightBackground = false;
    private float dayNightCycleProgress = 0f;
    private float speedVariation = 1.0f;

    private float baseGravityPerSec, baseFlapVelocityPerSec, basePipeSpeedPerSec;
    private float baseRotationDelayThresholdPerSec, jetpackThrustPerSec;
    private float gravityPerSec, flapVelocityPerSec, pipeSpeedPerSec, rotationDelayThresholdPerSec;

    private boolean hasTier1Triggered = false, hasTier2Triggered = false;
    private boolean pipesAreMoving = false;
    private boolean pipesAreStopping = false;
    private float pipeAnimationCounter = 0f;
    private float pipeAnimationSpeed = PIPE_MOVE_SPEED_TIER_1;
    private float maxPipeMoveRange = 0f;
    private float currentPipeMoveRange = 0f;
    private double pipeMoveStartTime = -1;

    private int score = 0, highScore = 0;
    private int displayedScore = 0;
    private double lastScoreTickTime = -1;
    private int currentMedal = MEDAL_NONE;
    private int lastStormCheckedScore = -1;

    private float gameOverElementsY;
    private int gameOverElementsTargetY;
    private double gameOverTime = 0;
    private double birdHitGroundTime = -1;
    private double panelFinishedSlidingTime = -1;
    private float gameOverSettingsAnimProgress = 0f;
    private boolean isGameOverIconAnimationDone = false;
    private float sparkleTimer = 0f;
    private float sparkleXOffset = 0f;
    private float sparkleYOffset = 0f;
    private float sparkleScaleCurrent = 0f;
    private boolean isSparkleAnimating = false;

    private final Rect playButtonRect = new Rect();
    private final Rect scoreButtonRect = new Rect();
    private final Rect settingsButtonRect = new Rect();
    private final Rect rateButtonRect = new Rect();
    private final RectF rateButtonDestRect = new RectF();
    private final RectF newLabelDestRect = new RectF();
    private final RectF resumeDestRect = new RectF();
    private float pauseFade = 0f;
    private float rateBtnProgress = 0, rateBtnTarget = 0;
    private boolean isNewBest = false;
    private int previousBest = 0;
    private boolean settingClassicMenuEnabled;
    private final Rect pauseButtonRect = new Rect();
    private Rect pressedButtonRect = null;
    private float playBtnProgress = 0, playBtnTarget = 0;
    private float scoreBtnProgress = 0, scoreBtnTarget = 0;
    private float settingsBtnProgress = 0, settingsBtnTarget = 0;
    private float okBtnProgress = 0, okBtnTarget = 0;
    private float gameOverSettingsBtnProgress = 0, gameOverSettingsBtnTarget = 0;

    private float transitionAlpha = 0;
    private boolean isFadingOut = false;
    private boolean isRestarting = false;
    private float flashAlpha = 0;
    private int flashColor = Color.WHITE;
    private float flashPeak = 0f, flashElapsed = 0f, flashLengthSec = 1f;
    private boolean flashSharp = false;
    private float birdTiltVelocity = 0f;
    private float fpsTimer = 0f;
    private Paint fpsPaint;
    private ColorFilter fpsGoodFilter, fpsOkFilter, fpsBadFilter;
    private int fpsColor = FPS_COLOR_GOOD;
    private boolean settingKeyboardEnabled, settingRightClickPauses, settingHideCursor;
    private int keyFlap, keyFlapAlt, keyPause, keyPlay, keySettings;
    private boolean touchHeld, flapKeyHeld, flapAltKeyHeld, padKeyHeld, secondaryClickHeld;
    private int keyButtonCode = KeyEvent.KEYCODE_UNKNOWN;
    private boolean hasMouse;
    private float hoverX = -1f, hoverY = -1f, cursorIdleSec = 0f;
    private int cursorType = Integer.MIN_VALUE;
    private int fpsFrames = 0, shownFps = 0;
    private float stepAccumulator = 0f;
    private float shakeTimer = 0f;
    private float currentShakeMagnitude = 0f;
    private float rainbowHue = 0f;
    private float goldenShineOffset = 0f;
    private float goldenShinePeriod = 200f;

    private final PorterDuffXfermode additiveXfermode = new PorterDuffXfermode(PorterDuff.Mode.ADD);
    private float backgroundBlurLength = 0f, groundBlurLength = 0f, pipeBlurLength = 0f;
    private float birdBlurX = 0f, birdBlurY = 0f, birdBlurRotation = 0f;

    private float timeDilation = 1.0f;
    private float cameraZoom = 1.0f;
    private float cinematicTimer = 0f;
    private int cinematicPhase = 0;
    private boolean isMegaStrike = false;
    private boolean hasTriggeredMegaStrike = false;
    private boolean isBirdBurnt = false;

    private int livesLeft = 0;
    private double roundStartClock;
    private int currentFairMode, roundFairMode;
    private String fairBlocker;
    private int scoreKey, scoreCheck;
    private boolean roundTampered;
    private final int sealKey = (int) (System.nanoTime() * 0x9E3779B1L) | 1;
    private int settingsSeal, tuningSeal, roundPipes, pipesCheck;
    static final int REPLAY_FORMAT = 1;
    static final int REPLAY_RULES = 3;
    static final int REPLAY_RULES_OLDEST = 3;
    static final int REPLAY_MARK = 0x80000000;
    static final int MARK_PLAY = 1;
    static final int MARK_PAUSE = 2;
    static final int MARK_RESUME = 3;
    static final int MARK_END = 4;
    static final float HEARTBEAT_SEC = 3f;
    static final int REPLAY_FLAP = 0xFFFFFFFF;
    static final int REPLAY_MAX_EVENTS = 4000000;
    static final int MARK_HOLD_ON = 5;
    static final int MARK_HOLD_OFF = 6;
    static final int MARK_START = 7;
    static final int FILM_FORMAT = 1;
    static final String FILM_DIR = "replays";
    static final String FILM_SUFFIX = ".fbr";
    static final String FILM_PREFIX = "film_";
    static final String BEST_PREFIX = "best_";
    static final String FILM_PREFS = "FlappyReplayPlayback";
    static final float FILM_LEAD_SEC = 0.6f;
    static final float FILM_TAIL_MAX_SEC = 8f;
    static final float FILM_TAIL_STEP_SEC = 1f / 120f;
    static final float FILM_AFTER_PANEL_SEC = 1.6f;
    private static ExecutorService filmSaver;
    private final Pipe.Kit pipeKit = new Pipe.Kit();
    private final Random seedSource = new Random();
    private long replaySeed;
    private boolean replayLive;
    private byte[] replayHeader;
    private int[] replayEvents;
    private int replayCount, replayLastBits;
    private Map<String, Object> replayPrefs;
    private MessageDigest replayDigest;
    private final byte[] replayChain = new byte[32];
    private int replayHashed, replayMarks;
    private float heartbeatTimer;
    private float boardShine;
    private final Shader[] medalFaces = new Shader[3];
    private final Path medalPath = new Path();
    private final Matrix championMatrix = new Matrix();
    private LinearGradient championShine;
    private double roundActiveSeconds;
    private long roundRealStart, roundUptimeStart, roundPausedMs, pausedSince = -1L;
    private boolean settingBoardAutoPost = true;
    private boolean settingDailyChallenges = true;
    private int challengeTwist = -1;
    private long challengeTwistDay = -1L;
    private boolean roundChallenge;
    private Leaderboard.Wallet wallet;
    private boolean walletLoading, walletOffline, walletTried;
    private int pendingMask;
    private long pendingSince, pendingServerTime;
    private float walletPollSec;
    private String buyingItem;
    private long buyingSince;
    private String toastTitle, toastText;
    private float toastAge = 99f;
    private boolean toastCoin;
    private final java.util.ArrayDeque<String[]> toastQueue = new java.util.ArrayDeque<>();
    private boolean coinOpen, coinClosing;
    private float coinAnim, coinTime, coinAlpha = 1f;
    private int coinTab, coinKind;
    private float coinScroll, coinScrollMax, coinFling, coinDragStartY, coinDragLastY;
    private long coinDragLastTime;
    private boolean coinDragArmed, coinDragging;
    private final RectF coinPanel = new RectF();
    private final RectF coinListRect = new RectF();
    private final List<float[]> coinHits = new ArrayList<>();
    private int coinHitCount;
    private int coinPressAction, coinPressArg = -1;
    private boolean coinGhost;
    private final RectF coinRect = new RectF();
    private float coinSwitchAge = 99f, coinSwitchScroll, coinKindAge = 99f, coinOpenAge = 99f, coinPill, coinKindPill;
    private int coinSwitchFrom, coinKindFrom;
    private final float[] coinBars = new float[3];
    private final float[] coinBarTargets = new float[3];
    private final float[] coinDonePop = {99f, 99f, 99f};
    private int coinDoneSeen;
    private float coinShown = -1f, coinBump = 99f;
    private boolean coinCacheLoaded;
    private long coinSeenDayCache = Long.MIN_VALUE, coinDoneDayCache = Long.MIN_VALUE;
    private int coinBalanceCache, coinDoneCache;
    private float twistCheckSec;
    private Leaderboard.Entry coinSample;
    private String coinPreview, coinConfirm, coinNote = "";
    private float coinConfirmAge = 99f, coinNoteAge = 99f;
    private final Rect coinButtonRect = new Rect();
    private float coinBtnProgress, coinBtnTarget;
    private final Rect twistChipRect = new Rect();
    private Paint lookPaint;
    private final Path lookPath = new Path();
    private final Matrix lookMatrix = new Matrix();
    private final Shader[] lookShaders = new Shader[Shop.ITEMS.length];
    private final String[] coinBlocked = new String[3];
    private float coinBlockedAge = 99f;
    private long coinBlockedDay = -1L;
    private int coinLabelValue = Integer.MIN_VALUE;
    private String coinLabel = "";
    private boolean settingBoardShowStatus = true, settingBoardOpenOnBest;
    private int settingBoardTab;
    private int settingBoardSize = 10;
    private String boardStatusText;
    private String boardStatusSeen;
    private float boardStatusAge, boardStatusAlpha;
    private boolean returningHome;
    private boolean motionValid;
    private GameState motionState;
    private float pastBirdX, pastBirdY, pastGroundX, pastBackgroundX;
    private float keptBirdX, keptBirdY, keptGroundX, keptBackgroundX;
    private float pastBirdRotation, keptBirdRotation;
    private float returnHoldSec;
    private final RectF boardStatusPill = new RectF();
    private Paint boardStatusPaint;
    private boolean nameOverlayOpen, boardSqueezed;
    private int imeInset;
    private float boardReserve;
    private float keyboardCut;
    private final float[] nameSpring = new float[2];
    private final float[] keyboardSpring = new float[2];
    private final float[] hideSpring = new float[2];
    private final float[] dimSpring = new float[2];
    private boolean nameSide, boardCached, boardRefreshPending, boardOffline;
    private float boardRetrySec;
    private float sideFit = 1f, sidePanelWidth, boardUnit, boardLeft, boardTop;
    private Bitmap boardCache;
    private Canvas boardCacheCanvas;
    private Paint boardCachePaint;
    private long boardCacheKey;
    private int boardCacheMargin;
    private float boardCacheUnit;
    private final RectF boardCacheDest = new RectF();
    private final int[] boardRectScratch = new int[28];
    private float settingsScale = 1f, settingsScaleVelocity, settingsSpin, settingsSpinTarget;
    private boolean settingsOpening;
    private Paint spinPaint;
    private boolean boardOpen, boardClosing, boardLoading;
    private int boardLoadingMask;
    private int boardStaleMask = 0xF;
    private int boardSizeLoaded = -1;
    private String boardUidLoaded = "";
    private int boardDataVersion;
    private int boardTapIndex = -1;
    private long boardTapTime;
    private int boardBubbleIndex = -1;
    private Leaderboard.Entry boardBubbleEntry;
    private String boardBubbleText;
    private float boardBubbleAge = 99f;
    private final RectF boardBubbleShape = new RectF();
    private Leaderboard.Entry boardPinned;
    private float boardAnim, boardAlpha = 1f;
    private int boardTab;
    private float boardSwitchAge = 99f;
    private int boardSwitchFromTab;
    private boolean boardSwitchFromAll;
    private float boardSwitchScroll;
    private int boardSwitchDir;
    private float boardPillFrom = -1f;
    private float boardPillTo;
    private String boardNote = "";
    private Leaderboard.Board boardData = new Leaderboard.Board();
    private final RectF boardPanel = new RectF();
    private final RectF boardShape = new RectF();
    private final Rect boardRowsRect = new Rect();
    private final Rect boardYouRect = new Rect();
    private float boardScroll, boardScrollMax, boardFling, boardRowStep = 1f;
    private float boardScrollTarget = Float.NaN;
    private boolean boardDragArmed, boardDragging;
    private float boardDragStartY, boardDragLastY;
    private long boardDragLastTime;
    private List<Leaderboard.Entry> boardIndexed;
    private final BoardMotion[] boardMotions = new BoardMotion[4];
    private long boardVerifyUntil, boardVerifySince;
    private boolean boardVerifyArmed;
    private float boardVerifyPoll;
    private boolean boardMemoryDirty;
    private int boardHapticMode = -1;
    private boolean boardHapticRise;
    private VibrationEffect[] boardHapticEffects;
    private long boardTickAt;
    private int boardRowShown = -1;
    private int boardRowArrow;
    private int boardRowGain;
    private float boardRowGlow, boardRowPop, boardRowLift, boardRowArrowAlpha, boardRowHot;
    private float boardRowGainAge = -1f;
    private int boardMineIndex = -1;
    private final Rect boardCloseRect = new Rect();
    private final Rect boardJoinRect = new Rect();
    private final Rect boardTabAllRect = new Rect();
    private final Rect boardToggleRect = new Rect();
    private boolean boardShowAll;
    private final Rect boardTabWeekRect = new Rect();
    private Rect boardPressed;
    private Paint boardPaint, boardTextPaint;
    private BoardHost boardHost;
    private boolean settingGodFallEnabled;
    private float settingGodFallSeconds;
    private float offScreenSeconds;
    private String roundEndCause = "";
    private int roundFrames, roundSlowFrames;
    private float roundFrameSeconds, roundWorstFrame, roundSlowFrameLimit;
    private int roundFlaps, roundGoldenPoints, roundLivesUsed;
    private final Set<String> roundAssists = new LinkedHashSet<>();
    private int loggedWidth, loggedHeight;
    private float invulnerableTimer = 0f;
    private boolean isPaused = false;
    private int difficultyScore = -1;
    private float pilotFrameDelta = 1.0f / TARGET_FPS;
    private float burntTimer = 0f;
    private float pipeElectrificationTimer = 0f;

    private final TrailParticle[] birdTrail = new TrailParticle[MAX_TRAIL_PARTICLES];
    private int trailStart = 0, trailCount = 0;
    private final SmokeParticle[] smokeParticles = new SmokeParticle[MAX_SMOKE_PARTICLES];
    private int smokeCount = 0;
    private final SparkParticle[] sparkParticles = new SparkParticle[MAX_SPARK_PARTICLES];
    private int sparkCount = 0;
    private float smokeEmission = 0f, burntSparkEmission = 0f, apocalypseSparkEmission = 0f;
    private final WeatherSystem weatherSystem;

    private SoundPool soundPool;
    private int soundWing, soundPoint, soundHit, soundDie, soundSwooshing, soundThunder;
    private ExecutorService soundExecutor;
    private MediaPlayer rainSoundPlayer, stormSoundPlayer;
    private final Vibrator vibrator;
    private final Feel feel;
    private double scoreFeltAt = -1;
    private boolean panelFelt;
    private final Runnable playDieSound = () -> playSound(soundDie);

    private final SharedPreferences prefs;
    private final Random random = new Random();
    private final Random fxRandom = new Random();
    private final Random drawRandom = new Random();
    final boolean filmMode;
    private final boolean filmSounds;
    private long roundRandomSeed;
    private boolean replayForBoard;
    private boolean filmRecording;
    private boolean recordedHold;
    private byte[] filmExtras;
    private int filmBestBefore;
    private boolean settingReplayEnabled = DEFAULT_REPLAY_ENABLED;
    private int settingReplayKeep = DEFAULT_REPLAY_KEEP;
    private boolean settingReplayBest;
    private Film film;
    private int filmIndex;
    private int filmLastBits;
    private float filmLeadLeft;
    private float filmTail;
    private float filmDieIn = -1f;
    private double filmTime;
    private boolean filmAudible;
    private boolean filmFeel;
    private final FastBytes headerBytes = new FastBytes(4096);
    private final DataOutputStream headerOut = new DataOutputStream(headerBytes);
    private final FastBytes extrasBytes = new FastBytes(16384);
    private final DataOutputStream extrasOut = new DataOutputStream(extrasBytes);
    private Map<String, Object> packedPrefs;
    private byte[] packedPrefsBytes;
    private static final java.util.concurrent.ConcurrentLinkedQueue<int[]> SPARE_EVENTS = new java.util.concurrent.ConcurrentLinkedQueue<>();
    private static final int SPARE_EVENT_BUFFERS = 2;
    private static final int SPARE_EVENT_MAX = 1 << 20;
    private FilmSink filmSink;

    interface FilmSink {
        void sound(int rawId, double atSec, float volume);

        void loop(int rawId, boolean playing, double atSec, float volume);
    }
    private int settingBirdColor, settingBackground, settingPipeColor, settingGameOverOpacity;
    private int settingPipeMoveTier1, settingPipeMoveTier2, settingScoreMultiplier;
    private int settingWeatherEffect, settingGoldenPipeChance, settingGoldenPipeBonus;
    private float settingGameSpeed, settingGravity, settingJumpStrength, settingPipeGap;
    private float settingBirdHangDelay, settingPipeSpacing, settingBirdHitbox, settingPipeVariation;
    private float settingPipeSpeedVariation, settingBirdSize, settingPipeWidth;
    private float settingBgScrollSpeed, settingGroundScrollSpeed, settingDrunkModeStrength;
    private float settingScreenShakeStrength, settingMotionBlurStrength;
    private boolean settingSoundEnabled, settingNoClipEnabled, settingWingAnimationEnabled;
    private boolean settingMovingPipesEnabled, settingHideSettingsIcon, settingRainbowBirdEnabled;
    private boolean settingShowTapGuide = true, settingShowGetReady = true, settingShowScore = true;
    private boolean settingUpsideDownEnabled, settingReversePipesEnabled, settingHapticFeedbackEnabled;
    private boolean settingBirdTrailEnabled, settingGhostModeEnabled, settingRandomPipeColorsEnabled;
    private boolean settingInfiniteFlapEnabled, settingJetpackModeEnabled, settingDrunkModeEnabled;
    private boolean settingDynamicDayNightEnabled, settingScreenShakeEnabled, settingGoldenPipeEnabled;
    private boolean settingMotionBlurEnabled;
    private boolean settingCrashFlashEnabled, settingFixedStepEnabled, settingCeilingEnabled, settingBirdTiltEnabled;
    private boolean settingStepOriginal;
    private boolean settingOriginalMotion;
    private int settingStepRate = DEFAULT_FIXED_STEP_RATE, settingFrameLimit;
    private float fixedStepSec = 1f / DEFAULT_FIXED_STEP_RATE;
    private long lastVsyncNanos, frameCreditNanos;
    private int settingCrashFlashColor, settingCrashFlashStrength, settingCrashFlashLengthMs;
    private int settingMaxTiltUp, settingMaxTiltDown, settingScorePoint;
    private float settingMaxFallSpeed, settingTiltSpeed;
    private boolean settingPixelStepsEnabled, settingFlapLimitEnabled, settingShowHitboxes, settingShowFps;
    private int settingTiltStyle, settingHitboxShape, settingGapPlacement, settingCrashFlashFade;
    private int settingBirdPosition, settingStartHeight, settingSoundVolume, settingWeatherVolume;
    private int settingDayNightSeconds, settingRampMinGap;
    private float settingCrashGravity, settingWingSpeed, settingRampTopSpeed;
    private boolean settingAutopilotEnabled, settingPauseButtonEnabled, settingAutoPauseEnabled;
    private int settingExtraLives, settingSpeedRampPercent, settingGapShrinkPercent, settingBirdHue;
    private int settingMedalBronze, settingMedalSilver, settingMedalGold, settingMedalPlatinum;
    private final Set<Integer> settingLightningScores = new HashSet<>();
    private int settingMegaStrikeScore = DEFAULT_MEGA_STRIKE_SCORE;

    public GameView(Context context) {
        this(context, null, true);
    }

    GameView(Context context, SharedPreferences playback, boolean withSounds) {
        super(context);
        filmMode = playback != null;
        filmSounds = withSounds;
        prefs = filmMode ? playback : context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        highScore = intPref(prefs.getAll(), PREF_HIGH_SCORE, 0);
        if (!filmMode) adoptOldHighScore(prefs);
        vibrator = filmMode && !withSounds ? null : ContextCompat.getSystemService(context, Vibrator.class);
        feel = new Feel(vibrator, this);
        weatherSystem = new WeatherSystem(fxRandom);

        loadSettings();
        if (!filmMode) {
            SettingsActivity.ActivityLog.add(context, "GAME", "Game opened on " + SettingsActivity.deviceSummary(context)
                    + " (high score " + highScore + ", best earned by playing " + earnedHighScore(prefs) + ")");
            preloadBoardMemory();
        }
        createPaints();
        createParticlePools();
        extractBitmapsFromAtlas();
        sparkleBitmap = createSparkleBitmap();
        smokeBitmap = createSmokeBitmap();
        if (filmSounds) {
            loadSounds();
        } else if (filmMode) {
            soundWing = 1;
            soundPoint = 2;
            soundHit = 3;
            soundDie = 4;
            soundSwooshing = 5;
            soundThunder = 6;
        }
        if (!filmMode) listenForWindowInsets();
    }

    private Choreographer choreographer() {
        if (choreographer == null) choreographer = Choreographer.getInstance();
        return choreographer;
    }

    private static Paint newPixelPaint() {
        Paint paint = new Paint();
        paint.setFilterBitmap(false);
        paint.setAntiAlias(false);
        return paint;
    }

    private static Paint newOverlayPaint(int color) {
        Paint paint = new Paint();
        paint.setColor(color);
        paint.setAlpha(0);
        return paint;
    }

    private static Paint newStrokePaint(int color) {
        Paint paint = new Paint();
        paint.setColor(color);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setAntiAlias(true);
        return paint;
    }

    private void createPaints() {
        pixelPaint = newPixelPaint();
        birdPaint = newPixelPaint();
        nightBgPaint = newPixelPaint();
        trailPaint = new Paint();

        smoothPaint = new Paint();
        smoothPaint.setFilterBitmap(true);

        smokePaint = new Paint();
        smokePaint.setFilterBitmap(true);
        smokePaint.setAntiAlias(true);

        transitionPaint = newOverlayPaint(Color.BLACK);
        flashPaint = newOverlayPaint(Color.WHITE);
        darkenPaint = newOverlayPaint(Color.BLACK);

        hudPlatePaint = new Paint();
        hudPlatePaint.setColor(Color.BLACK);
        hudIconPaint = new Paint();
        hudIconPaint.setColor(Color.WHITE);
        hudIconPaint.setAntiAlias(true);
        hitboxPaint = newStrokePaint(0xFFFF3B30);
        fpsPaint = new Paint(pixelPaint);
        fpsGoodFilter = new LightingColorFilter(FPS_COLOR_GOOD, 0);
        fpsOkFilter = new LightingColorFilter(FPS_COLOR_OK, 0);
        fpsBadFilter = new LightingColorFilter(FPS_COLOR_BAD, 0);
        fpsPaint.setColorFilter(fpsGoodFilter);

        goldenPipePaint = newPixelPaint();
        goldenPipePaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_ATOP));

        sparkPaint = newStrokePaint(Color.WHITE);
        electricityPipePaint = newStrokePaint(Color.CYAN);
        electricityGlowPaint = newStrokePaint(Color.CYAN);
        electricityGlowPaint.setAlpha(70);

        burntColorFilter = new PorterDuffColorFilter(Color.rgb(40, 40, 40), PorterDuff.Mode.MULTIPLY);

        for (int i = 0; i < PIPE_COLOR_COUNT; i++) pipeColorFilters[i] = pipeColorFilter(i);
    }

    private void createParticlePools() {
        for (int i = 0; i < birdTrail.length; i++) birdTrail[i] = new TrailParticle();
        for (int i = 0; i < smokeParticles.length; i++) smokeParticles[i] = new SmokeParticle();
        for (int i = 0; i < sparkParticles.length; i++) sparkParticles[i] = new SparkParticle();
    }

    private Bitmap createSparkleBitmap() {
        int size = 64;
        Bitmap bmp = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bmp);
        Paint p = new Paint();
        p.setColor(Color.WHITE);
        p.setAntiAlias(true);
        p.setMaskFilter(new BlurMaskFilter(size / 4f, BlurMaskFilter.Blur.NORMAL));
        c.drawCircle(size / 2f, size / 2f, size / 6f, p);
        p.setMaskFilter(null);
        float cx = size / 2f;
        float cy = size / 2f;
        float halfLen = size / 2.5f;
        float thickness = size / 12f;
        c.drawRect(cx - halfLen, cy - thickness / 2, cx + halfLen, cy + thickness / 2, p);
        c.drawRect(cx - thickness / 2, cy - halfLen, cx + thickness / 2, cy + halfLen, p);
        return bmp;
    }

    private Bitmap createSmokeBitmap() {
        int size = 128;
        Bitmap bmp = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bmp);
        Paint p = new Paint();
        p.setShader(new RadialGradient(size / 2f, size / 2f, size / 2f,
                Color.rgb(60, 60, 60), Color.TRANSPARENT, Shader.TileMode.CLAMP));
        c.drawCircle(size / 2f, size / 2f, size / 2f, p);
        return bmp;
    }

    private void listenForWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(this, (v, windowInsets) -> {
            imeInset = Math.max(0, windowInsets.getInsets(WindowInsetsCompat.Type.ime()).bottom);
            invalidate();
            Insets bars = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            Insets safe = windowInsets.getInsets(
                    WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.displayCutout());
            boolean changed = windowInsetTop != bars.top || windowInsetBottom != bars.bottom
                    || windowInsetLeft != safe.left || windowInsetRight != safe.right || !isReady;
            windowInsetTop = bars.top;
            windowInsetBottom = bars.bottom;
            windowInsetLeft = safe.left;
            windowInsetRight = safe.right;
            if (changed) applyLayout();
            return WindowInsetsCompat.CONSUMED;
        });
    }

    private void loadSettings() {
        if (!filmMode) readTwist();
        Map<String, ?> stored = playPrefs();
        int storedBest = Math.max(0, intPref(stored, PREF_HIGH_SCORE, 0));
        if (storedBest != highScore) {
            highScore = storedBest;
            previousBest = storedBest;
            isNewBest = false;
        }
        settingBirdColor = intPref(stored, PREF_BIRD_COLOR, DEFAULT_BIRD_COLOR);
        settingBackground = intPref(stored, PREF_BACKGROUND, DEFAULT_BACKGROUND);
        settingSoundEnabled = flagPref(stored, PREF_SOUND_ENABLED, DEFAULT_SOUND_ENABLED);
        settingWingAnimationEnabled = flagPref(stored, PREF_WING_ANIMATION_ENABLED, DEFAULT_WING_ANIMATION_ENABLED);
        settingPipeColor = intPref(stored, PREF_PIPE_COLOR, DEFAULT_PIPE_COLOR);
        settingHideSettingsIcon = flagPref(stored, PREF_HIDE_SETTINGS_ICON, DEFAULT_HIDE_SETTINGS_ICON);
        settingShowTapGuide = flagPref(stored, PREF_SHOW_TAP_GUIDE, DEFAULT_SHOW_TAP_GUIDE);
        settingShowGetReady = flagPref(stored, PREF_SHOW_GET_READY, DEFAULT_SHOW_GET_READY);
        settingShowScore = flagPref(stored, PREF_SHOW_SCORE, DEFAULT_SHOW_SCORE);
        settingClassicMenuEnabled = flagPref(stored, PREF_CLASSIC_MENU_ENABLED, DEFAULT_CLASSIC_MENU_ENABLED);
        settingKeyboardEnabled = flagPref(stored, PREF_KEYBOARD_ENABLED, DEFAULT_KEYBOARD_ENABLED);
        settingRightClickPauses = flagPref(stored, PREF_RIGHT_CLICK_PAUSES, DEFAULT_RIGHT_CLICK_PAUSES);
        settingHideCursor = flagPref(stored, PREF_HIDE_CURSOR, DEFAULT_HIDE_CURSOR);
        keyFlap = keyPref(stored, PREF_KEY_FLAP, DEFAULT_KEY_FLAP);
        keyFlapAlt = keyPref(stored, PREF_KEY_FLAP_ALT, DEFAULT_KEY_FLAP_ALT);
        keyPause = keyPref(stored, PREF_KEY_PAUSE, DEFAULT_KEY_PAUSE);
        keyPlay = keyPref(stored, PREF_KEY_PLAY, DEFAULT_KEY_PLAY);
        keySettings = keyPref(stored, PREF_KEY_SETTINGS, DEFAULT_KEY_SETTINGS);
        settingGameOverOpacity = intPref(stored, PREF_GAMEOVER_OPACITY, DEFAULT_GAMEOVER_OPACITY);
        settingGameSpeed = floatPref(stored, PREF_GAME_SPEED, DEFAULT_GAME_SPEED);
        settingGravity = floatPref(stored, PREF_GRAVITY, DEFAULT_GRAVITY);
        settingJumpStrength = floatPref(stored, PREF_JUMP_STRENGTH, DEFAULT_JUMP_STRENGTH);
        settingPipeGap = floatPref(stored, PREF_PIPE_GAP, DEFAULT_PIPE_GAP);
        settingBirdHangDelay = floatPref(stored, PREF_BIRD_HANG_DELAY, DEFAULT_BIRD_HANG_DELAY);
        settingPipeSpacing = floatPref(stored, PREF_PIPE_SPACING, DEFAULT_PIPE_SPACING);
        settingBirdHitbox = floatPref(stored, PREF_BIRD_HITBOX, DEFAULT_BIRD_HITBOX);
        settingMovingPipesEnabled = flagPref(stored, PREF_MOVING_PIPES_ENABLED, DEFAULT_MOVING_PIPES_ENABLED);
        settingPipeMoveTier1 = intPref(stored, PREF_PIPE_MOVE_TIER_1_SCORE, DEFAULT_PIPE_MOVE_TIER_1_SCORE);
        settingPipeMoveTier2 = intPref(stored, PREF_PIPE_MOVE_TIER_2_SCORE, DEFAULT_PIPE_MOVE_TIER_2_SCORE);
        settingNoClipEnabled = flagPref(stored, PREF_NO_CLIP_ENABLED, DEFAULT_NO_CLIP_ENABLED);
        settingGodFallEnabled = flagPref(stored, PREF_GOD_FALL_ENABLED, DEFAULT_GOD_FALL_ENABLED);
        settingGodFallSeconds = Math.max(0.1f, Math.min(60f, floatPref(stored, PREF_GOD_FALL_SECONDS, DEFAULT_GOD_FALL_SECONDS)));
        SettingsActivity.ActivityLog.setEnabled(flagPref(stored, PREF_ACTIVITY_LOG_ENABLED, DEFAULT_ACTIVITY_LOG_ENABLED));
        settingRainbowBirdEnabled = flagPref(stored, PREF_RAINBOW_BIRD_ENABLED, DEFAULT_RAINBOW_BIRD_ENABLED);
        settingUpsideDownEnabled = flagPref(stored, PREF_UPSIDE_DOWN_ENABLED, DEFAULT_UPSIDE_DOWN_ENABLED);
        settingReversePipesEnabled = flagPref(stored, PREF_REVERSE_PIPES_ENABLED, DEFAULT_REVERSE_PIPES_ENABLED);
        settingPipeVariation = floatPref(stored, PREF_PIPE_VARIATION, DEFAULT_PIPE_VARIATION);
        settingScoreMultiplier = intPref(stored, PREF_SCORE_MULTIPLIER, DEFAULT_SCORE_MULTIPLIER);
        settingHapticFeedbackEnabled = flagPref(stored, PREF_HAPTIC_FEEDBACK_ENABLED, DEFAULT_HAPTIC_FEEDBACK_ENABLED);
        if (feel != null) feel.load(stored);
        settingBirdTrailEnabled = flagPref(stored, PREF_BIRD_TRAIL_ENABLED, DEFAULT_BIRD_TRAIL_ENABLED);
        settingGhostModeEnabled = flagPref(stored, PREF_GHOST_MODE_ENABLED, DEFAULT_GHOST_MODE_ENABLED);
        settingPipeSpeedVariation = floatPref(stored, PREF_PIPE_SPEED_VARIATION, DEFAULT_PIPE_SPEED_VARIATION);
        settingBirdSize = floatPref(stored, PREF_BIRD_SIZE, DEFAULT_BIRD_SIZE);
        settingPipeWidth = floatPref(stored, PREF_PIPE_WIDTH, DEFAULT_PIPE_WIDTH);
        settingBgScrollSpeed = floatPref(stored, PREF_BG_SCROLL_SPEED, DEFAULT_BG_SCROLL_SPEED);
        settingGroundScrollSpeed = floatPref(stored, PREF_GROUND_SCROLL_SPEED, DEFAULT_GROUND_SCROLL_SPEED);
        settingRandomPipeColorsEnabled = flagPref(stored, PREF_RANDOM_PIPE_COLORS_ENABLED, DEFAULT_RANDOM_PIPE_COLORS_ENABLED);
        settingInfiniteFlapEnabled = flagPref(stored, PREF_INFINITE_FLAP_ENABLED, DEFAULT_INFINITE_FLAP_ENABLED);
        settingJetpackModeEnabled = flagPref(stored, PREF_JETPACK_MODE_ENABLED, DEFAULT_JETPACK_MODE_ENABLED);
        settingDrunkModeEnabled = flagPref(stored, PREF_DRUNK_MODE_ENABLED, DEFAULT_DRUNK_MODE_ENABLED);
        settingDrunkModeStrength = floatPref(stored, PREF_DRUNK_MODE_STRENGTH, DEFAULT_DRUNK_MODE_STRENGTH);
        settingWeatherEffect = intPref(stored, PREF_WEATHER_EFFECT, DEFAULT_WEATHER_EFFECT);
        settingDynamicDayNightEnabled = flagPref(stored, PREF_DYNAMIC_DAY_NIGHT_ENABLED, DEFAULT_DYNAMIC_DAY_NIGHT_ENABLED);
        settingScreenShakeEnabled = flagPref(stored, PREF_SCREEN_SHAKE_ENABLED, DEFAULT_SCREEN_SHAKE_ENABLED);
        settingScreenShakeStrength = floatPref(stored, PREF_SCREEN_SHAKE_STRENGTH, DEFAULT_SCREEN_SHAKE_STRENGTH);
        settingGoldenPipeEnabled = flagPref(stored, PREF_GOLDEN_PIPE_ENABLED, DEFAULT_GOLDEN_PIPE_ENABLED);
        settingGoldenPipeChance = intPref(stored, PREF_GOLDEN_PIPE_CHANCE, DEFAULT_GOLDEN_PIPE_CHANCE);
        settingGoldenPipeBonus = intPref(stored, PREF_GOLDEN_PIPE_BONUS, DEFAULT_GOLDEN_PIPE_BONUS);
        settingMegaStrikeScore = intPref(stored, PREF_MEGA_STRIKE_SCORE, DEFAULT_MEGA_STRIKE_SCORE);
        settingExtraLives = intPref(stored, PREF_EXTRA_LIVES, DEFAULT_EXTRA_LIVES);
        settingAutopilotEnabled = flagPref(stored, PREF_AUTOPILOT_ENABLED, DEFAULT_AUTOPILOT_ENABLED);
        settingPauseButtonEnabled = flagPref(stored, PREF_PAUSE_BUTTON_ENABLED, DEFAULT_PAUSE_BUTTON_ENABLED);
        settingAutoPauseEnabled = flagPref(stored, PREF_AUTO_PAUSE_ENABLED, DEFAULT_AUTO_PAUSE_ENABLED);
        settingSpeedRampPercent = intPref(stored, PREF_SPEED_RAMP, DEFAULT_SPEED_RAMP);
        settingGapShrinkPercent = intPref(stored, PREF_GAP_SHRINK, DEFAULT_GAP_SHRINK);
        settingBirdHue = intPref(stored, PREF_BIRD_HUE, DEFAULT_BIRD_HUE);
        settingMedalBronze = intPref(stored, PREF_MEDAL_BRONZE_SCORE, DEFAULT_MEDAL_BRONZE_SCORE);
        settingMedalSilver = intPref(stored, PREF_MEDAL_SILVER_SCORE, DEFAULT_MEDAL_SILVER_SCORE);
        settingMedalGold = intPref(stored, PREF_MEDAL_GOLD_SCORE, DEFAULT_MEDAL_GOLD_SCORE);
        settingMedalPlatinum = intPref(stored, PREF_MEDAL_PLATINUM_SCORE, DEFAULT_MEDAL_PLATINUM_SCORE);
        settingMotionBlurEnabled = flagPref(stored, PREF_MOTION_BLUR_ENABLED, DEFAULT_MOTION_BLUR_ENABLED);
        settingMotionBlurStrength = floatPref(stored, PREF_MOTION_BLUR_STRENGTH, DEFAULT_MOTION_BLUR_STRENGTH);
        settingCrashFlashEnabled = flagPref(stored, PREF_CRASH_FLASH_ENABLED, DEFAULT_CRASH_FLASH_ENABLED);
        settingCrashFlashColor = intPref(stored, PREF_CRASH_FLASH_COLOR, DEFAULT_CRASH_FLASH_COLOR);
        settingCrashFlashStrength = intPref(stored, PREF_CRASH_FLASH_STRENGTH, DEFAULT_CRASH_FLASH_STRENGTH);
        settingCrashFlashLengthMs = intPref(stored, PREF_CRASH_FLASH_LENGTH, DEFAULT_CRASH_FLASH_LENGTH);
        settingFixedStepEnabled = flagPref(stored, PREF_FIXED_STEP_ENABLED, DEFAULT_FIXED_STEP_ENABLED);
        settingStepRate = SettingsActivity.stepRate(intPref(stored, PREF_FIXED_STEP_RATE, DEFAULT_FIXED_STEP_RATE));
        fixedStepSec = 1f / settingStepRate;
        settingStepOriginal = intPref(stored, PREF_FIXED_STEP_TIMING, DEFAULT_FIXED_STEP_TIMING) == STEP_TIMING_ORIGINAL;
        settingFrameLimit = SettingsActivity.frameLimit(intPref(stored, PREF_FRAME_RATE_LIMIT, DEFAULT_FRAME_RATE_LIMIT));
        settingMaxFallSpeed = floatPref(stored, PREF_MAX_FALL_SPEED, DEFAULT_MAX_FALL_SPEED);
        settingCeilingEnabled = flagPref(stored, PREF_CEILING_ENABLED, DEFAULT_CEILING_ENABLED);
        settingBirdTiltEnabled = flagPref(stored, PREF_BIRD_TILT_ENABLED, DEFAULT_BIRD_TILT_ENABLED);
        settingMaxTiltUp = intPref(stored, PREF_MAX_TILT_UP, DEFAULT_MAX_TILT_UP);
        settingMaxTiltDown = intPref(stored, PREF_MAX_TILT_DOWN, DEFAULT_MAX_TILT_DOWN);
        settingTiltSpeed = floatPref(stored, PREF_TILT_SPEED, DEFAULT_TILT_SPEED);
        settingScorePoint = intPref(stored, PREF_SCORE_POINT, DEFAULT_SCORE_POINT);
        settingPixelStepsEnabled = flagPref(stored, PREF_PIXEL_STEPS_ENABLED, DEFAULT_PIXEL_STEPS_ENABLED);
        settingOriginalMotion = flagPref(stored, PREF_ORIGINAL_MOTION_ENABLED, DEFAULT_ORIGINAL_MOTION_ENABLED);
        settingTiltStyle = intPref(stored, PREF_TILT_STYLE, DEFAULT_TILT_STYLE);
        settingFlapLimitEnabled = flagPref(stored, PREF_FLAP_LIMIT_ENABLED, DEFAULT_FLAP_LIMIT_ENABLED);
        settingHitboxShape = intPref(stored, PREF_HITBOX_SHAPE, DEFAULT_HITBOX_SHAPE);
        settingGapPlacement = intPref(stored, PREF_GAP_PLACEMENT, DEFAULT_GAP_PLACEMENT);
        settingCrashFlashFade = intPref(stored, PREF_CRASH_FLASH_FADE, DEFAULT_CRASH_FLASH_FADE);
        settingCrashGravity = floatPref(stored, PREF_CRASH_GRAVITY, DEFAULT_CRASH_GRAVITY);
        settingBirdPosition = intPref(stored, PREF_BIRD_POSITION, DEFAULT_BIRD_POSITION);
        settingStartHeight = intPref(stored, PREF_START_HEIGHT, DEFAULT_START_HEIGHT);
        settingWingSpeed = floatPref(stored, PREF_WING_SPEED, DEFAULT_WING_SPEED);
        settingSoundVolume = intPref(stored, PREF_SOUND_VOLUME, DEFAULT_SOUND_VOLUME);
        settingWeatherVolume = intPref(stored, PREF_WEATHER_VOLUME, DEFAULT_WEATHER_VOLUME);
        settingDayNightSeconds = intPref(stored, PREF_DAY_NIGHT_LENGTH, DEFAULT_DAY_NIGHT_LENGTH);
        settingShowHitboxes = flagPref(stored, PREF_SHOW_HITBOXES, DEFAULT_SHOW_HITBOXES);
        settingShowFps = flagPref(stored, PREF_SHOW_FPS, DEFAULT_SHOW_FPS);
        settingRampTopSpeed = floatPref(stored, PREF_RAMP_TOP_SPEED, DEFAULT_RAMP_TOP_SPEED);
        settingRampMinGap = intPref(stored, PREF_RAMP_MIN_GAP, DEFAULT_RAMP_MIN_GAP);

        parseLightningScores(textPref(stored, PREF_LIGHTNING_SCORES, DEFAULT_LIGHTNING_SCORES));
        fairBlocker = fairPlayBlocker(stored);
        currentFairMode = fairBlocker == null ? fairPlayMode(stored) : FAIR_NONE;
        settingBoardAutoPost = flagPref(stored, PREF_BOARD_AUTO_POST, DEFAULT_BOARD_AUTO_POST);
        settingDailyChallenges = flagPref(stored, PREF_DAILY_CHALLENGES, DEFAULT_DAILY_CHALLENGES);
        settingBoardShowStatus = flagPref(stored, PREF_BOARD_SHOW_STATUS, DEFAULT_BOARD_SHOW_STATUS);
        settingBoardOpenOnBest = flagPref(stored, PREF_BOARD_OPEN_ON_BEST, DEFAULT_BOARD_OPEN_ON_BEST);
        settingBoardTab = intPref(stored, PREF_BOARD_TAB, DEFAULT_BOARD_TAB) == 1 ? 1 : 0;
        settingBoardSize = Leaderboard.boardSize(intPref(stored, PREF_BOARD_SIZE, DEFAULT_BOARD_SIZE));
        settingReplayEnabled = flagPref(stored, PREF_REPLAY_ENABLED, DEFAULT_REPLAY_ENABLED);
        settingReplayKeep = replayKeep(intPref(stored, PREF_REPLAY_KEEP, DEFAULT_REPLAY_KEEP));
        settingReplayBest = intPref(stored, PREF_REPLAY_MODE, DEFAULT_REPLAY_MODE) == REPLAY_MODE_BEST;
        sealSettings();
        if (gameState == GameState.PLAYING) {
            noteAssists();
            if (roundFairMode != currentFairMode) roundFairMode = FAIR_NONE;
        }
    }

    private void guardScore() {
        scoreKey = (int) (System.nanoTime() * 0x9E3779B1L) | 1;
        scoreCheck = score ^ scoreKey;
    }

    private void countPipe() {
        if ((pipesCheck ^ scoreKey) != roundPipes) roundTampered = true;
        roundPipes++;
        pipesCheck = roundPipes ^ scoreKey;
        checkSeals();
    }

    private void addScore(int points) {
        if ((scoreCheck ^ scoreKey) != score) roundTampered = true;
        score += points;
        scoreCheck = score ^ scoreKey;
    }

    private void noteAssists() {
        if (settingAutopilotEnabled) roundAssists.add("autopilot");
        if (settingNoClipEnabled) roundAssists.add("God mode");
        if (settingScoreMultiplier > 1) roundAssists.add(settingScoreMultiplier + " points per pipe");
        if (settingPipeGap > 1.001f) roundAssists.add("wider gaps (" + Math.round(settingPipeGap * 100) + "%)");
        if (settingBirdHitbox < 0.999f) roundAssists.add("smaller hitbox (" + Math.round(settingBirdHitbox * 100) + "%)");
        if (settingGameSpeed < 0.999f && currentFairMode != FAIR_CLASSIC) roundAssists.add("slower game (" + Math.round(settingGameSpeed * 100) + "%)");
    }

    private String roundAssistText() {
        List<String> parts = new ArrayList<>(roundAssists);
        int livesUsed = roundLivesUsed;
        if (livesUsed > 0) parts.add(livesUsed == 1 ? "1 extra life used" : livesUsed + " extra lives used");
        if (roundGoldenPoints > 0) parts.add("golden pipes (+" + roundGoldenPoints + ")");
        StringBuilder text = new StringBuilder();
        for (int i = 0; i < parts.size(); i++) {
            if (i > 0) text.append(", ");
            text.append(parts.get(i));
        }
        return text.toString();
    }

    private void logProblem(String message) {
        if (filmMode) return;
        SettingsActivity.ActivityLog.add(getContext(), "WARNING", message);
    }

    private static int intPref(Map<String, ?> stored, String key, int fallback) {
        Object value = stored.get(key);
        if (!(value instanceof Number)) return fallback;
        float number = ((Number) value).floatValue();
        return Float.isNaN(number) || Float.isInfinite(number) ? fallback : ((Number) value).intValue();
    }

    private static float floatPref(Map<String, ?> stored, String key, float fallback) {
        Object value = stored.get(key);
        if (!(value instanceof Number)) return fallback;
        float number = ((Number) value).floatValue();
        return Float.isNaN(number) || Float.isInfinite(number) ? fallback : number;
    }

    private static int keyPref(Map<String, ?> stored, String key, int fallback) {
        int code = intPref(stored, key, fallback);
        return isUsableKey(code) ? code : fallback;
    }

    private static boolean flagPref(Map<String, ?> stored, String key, boolean fallback) {
        Object value = stored.get(key);
        return value instanceof Boolean ? (Boolean) value : fallback;
    }

    private static String textPref(Map<String, ?> stored, String key, String fallback) {
        Object value = stored.get(key);
        return value instanceof String ? (String) value : fallback;
    }

    private void parseLightningScores(String list) {
        settingLightningScores.clear();
        if (list == null) return;
        for (String entry : list.split(",")) {
            try {
                settingLightningScores.add(Integer.parseInt(entry.trim()));
            } catch (NumberFormatException ignored) {
            }
        }
    }

    private Bitmap extract(Bitmap source, int x, int y, int w, int h) {
        return Bitmap.createBitmap(source, x, y, w, h);
    }

    private Bitmap extractOptional(Bitmap source, int x, int y, int w, int h) {
        if (x + w > source.getWidth() || y + h > source.getHeight()) return null;
        try {
            return Bitmap.createBitmap(source, x, y, w, h);
        } catch (IllegalArgumentException e) {
            Log.w(TAG, "Atlas sprite missing at " + x + "," + y);
            logProblem("Atlas sprite missing at " + x + "," + y);
            return null;
        }
    }

    private int loc(double coord, int dim) {
        return (int) (coord * dim);
    }

    private BitmapFactory.Options rawPixelOptions() {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inScaled = false;
        return options;
    }

    private void extractBitmapsFromAtlas() {
        BitmapFactory.Options options = rawPixelOptions();
        Bitmap atlas = BitmapFactory.decodeResource(getResources(), R.drawable.atlas, options);
        if (atlas == null) throw new IllegalStateException("res/drawable/atlas could not be decoded");
        final int ATLAS_WIDTH = 1024, ATLAS_HEIGHT = 1024;

        unscaledBgDay = extract(atlas, loc(0.0, ATLAS_WIDTH), loc(0.0, ATLAS_HEIGHT), 288, 512);
        unscaledBgNight = extract(atlas, loc(0.28515625, ATLAS_WIDTH), loc(0.0, ATLAS_HEIGHT), 288, 512);
        unscaledLand = extract(atlas, loc(0.5703125, ATLAS_WIDTH), loc(0.0, ATLAS_HEIGHT), 336, 112);
        unscaledTitle = extract(atlas, loc(0.6855469, ATLAS_WIDTH), loc(0.17773438, ATLAS_HEIGHT), 178, 48);
        unscaledCopyright = extract(atlas, loc(0.86328125, ATLAS_WIDTH), loc(0.17773438, ATLAS_HEIGHT), 126, 14);
        unscaledButtonScore = extract(atlas, loc(0.8027344, ATLAS_WIDTH), loc(0.22851562, ATLAS_HEIGHT), 116, 70);
        unscaledTextReady = extract(atlas, loc(0.5703125, ATLAS_WIDTH), loc(0.11328125, ATLAS_HEIGHT), 196, 62);
        unscaledTutorial = extractOptional(atlas, ATLAS_TUTORIAL[0], ATLAS_TUTORIAL[1], ATLAS_TUTORIAL[2], ATLAS_TUTORIAL[3]);
        boolean panelDigits = true;
        for (int i = 0; i < 10; i++) {
            int[] at = ATLAS_PANEL_DIGITS[i];
            unscaledPanelNumbers[i] = extractOptional(atlas, at[0], at[1], at[2], at[3]);
            if (unscaledPanelNumbers[i] == null) panelDigits = false;
        }
        if (!panelDigits) Arrays.fill(unscaledPanelNumbers, null);
        unscaledTextGameOver = extract(atlas, loc(0.765625, ATLAS_WIDTH), loc(0.11328125, ATLAS_HEIGHT), 204, 54);
        unscaledScorePanel = extract(atlas, loc(0.0, ATLAS_WIDTH), loc(0.50390625, ATLAS_HEIGHT), 238, 126);
        unscaledButtonPlay = extract(atlas, loc(0.6855469, ATLAS_WIDTH), loc(0.22851562, ATLAS_HEIGHT), 116, 70);
        unscaledMedalsBitmaps[MEDAL_PLATINUM] = extract(atlas, loc(0.23632812, ATLAS_WIDTH), loc(0.50390625, ATLAS_HEIGHT), 48, 48);
        unscaledMedalsBitmaps[MEDAL_GOLD] = extract(atlas, loc(0.23632812, ATLAS_WIDTH), loc(0.55078125, ATLAS_HEIGHT), 48, 48);
        unscaledMedalsBitmaps[MEDAL_SILVER] = extract(atlas, loc(0.21875, ATLAS_WIDTH), loc(0.8847656, ATLAS_HEIGHT), 44, 48);
        unscaledMedalsBitmaps[MEDAL_BRONZE] = extract(atlas, loc(0.21875, ATLAS_WIDTH), loc(0.9316406, ATLAS_HEIGHT), 44, 48);
        unscaledButtonRate = extractOptional(atlas, ATLAS_RATE_X, ATLAS_RATE_Y, ATLAS_RATE_WIDTH, ATLAS_RATE_HEIGHT);
        unscaledNewLabel = extractOptional(atlas, ATLAS_NEW_X, ATLAS_NEW_Y, ATLAS_NEW_WIDTH, ATLAS_NEW_HEIGHT);
        unscaledButtonResume = extractOptional(atlas, ATLAS_RESUME_X, ATLAS_RESUME_Y, ATLAS_RESUME_WIDTH, ATLAS_RESUME_HEIGHT);
        atlas.recycle();

        unscaledPipeGreen = BitmapFactory.decodeResource(getResources(), R.drawable.pipe_green, options);

        unscaledAllBirdBitmaps[0][0] = BitmapFactory.decodeResource(getResources(), R.drawable.yellowbird_upflap, options);
        unscaledAllBirdBitmaps[0][1] = BitmapFactory.decodeResource(getResources(), R.drawable.yellowbird_midflap, options);
        unscaledAllBirdBitmaps[0][2] = BitmapFactory.decodeResource(getResources(), R.drawable.yellowbird_downflap, options);
        unscaledAllBirdBitmaps[1][0] = BitmapFactory.decodeResource(getResources(), R.drawable.bluebird_upflap, options);
        unscaledAllBirdBitmaps[1][1] = BitmapFactory.decodeResource(getResources(), R.drawable.bluebird_midflap, options);
        unscaledAllBirdBitmaps[1][2] = BitmapFactory.decodeResource(getResources(), R.drawable.bluebird_downflap, options);
        unscaledAllBirdBitmaps[2][0] = BitmapFactory.decodeResource(getResources(), R.drawable.redbird_upflap, options);
        unscaledAllBirdBitmaps[2][1] = BitmapFactory.decodeResource(getResources(), R.drawable.redbird_midflap, options);
        unscaledAllBirdBitmaps[2][2] = BitmapFactory.decodeResource(getResources(), R.drawable.redbird_downflap, options);

        String packageName = getContext().getPackageName();
        int settingsResId = getResources().getIdentifier("settingsbutton", "drawable", packageName);
        if (settingsResId != 0) unscaledButtonSettings = BitmapFactory.decodeResource(getResources(), settingsResId, options);
        if (unscaledButtonSettings == null) {
            Log.w(TAG, "Could not load 'settingsbutton.png'.");
            logProblem("Could not load settingsbutton.png");
        }

        for (int i = 0; i < 10; i++) {
            int resId = getResources().getIdentifier("number_" + i, "drawable", packageName);
            if (resId != 0) unscaledNumberBitmaps[i] = BitmapFactory.decodeResource(getResources(), resId, options);
            if (unscaledNumberBitmaps[i] == null) {
                Log.e(TAG, "Missing number resource: number_" + i);
                logProblem("Missing number resource: number_" + i);
                unscaledNumberBitmaps[i] = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);
            }
        }
    }

    private Bitmap rescale(Bitmap previous, Bitmap source, float width, float height) {
        if (source == null) return null;
        Bitmap scaled = Bitmap.createScaledBitmap(source, Math.max(1, (int) width), Math.max(1, (int) height), false);
        if (previous != null && previous != source && previous != scaled) previous.recycle();
        return scaled;
    }

    private Bitmap rescale(Bitmap previous, Bitmap source, float factor) {
        if (source == null) return null;
        return rescale(previous, source, source.getWidth() * factor, source.getHeight() * factor);
    }

    private void scaleAllBitmaps() {
        float bgScale = (float) screenHeight / unscaledBgDay.getHeight();
        float bgWidth = unscaledBgDay.getWidth() * bgScale;
        bgDayBitmap = rescale(bgDayBitmap, unscaledBgDay, bgWidth, screenHeight);
        bgNightBitmap = rescale(bgNightBitmap, unscaledBgNight, bgWidth, screenHeight);
        bgDayBitmap.setHasAlpha(false);
        bgNightBitmap.setHasAlpha(false);
        groundBitmap = rescale(groundBitmap, unscaledLand, scale);

        pipeUpBitmap = rescale(pipeUpBitmap, unscaledPipeGreen, scale);
        Matrix flipMatrix = new Matrix();
        flipMatrix.setScale(1, -1);
        Bitmap previousPipeDown = pipeDownBitmap;
        pipeDownBitmap = Bitmap.createBitmap(pipeUpBitmap, 0, 0, pipeUpBitmap.getWidth(), pipeUpBitmap.getHeight(), flipMatrix, true);
        if (previousPipeDown != null && previousPipeDown != pipeDownBitmap) previousPipeDown.recycle();

        for (int color = 0; color < 3; color++) {
            for (int frame = 0; frame < 3; frame++) {
                scaledAllBirdBitmaps[color][frame] = rescale(scaledAllBirdBitmaps[color][frame], unscaledAllBirdBitmaps[color][frame], scale);
            }
        }
        for (int i = 0; i < 4; i++) medalBitmaps[i] = rescale(medalBitmaps[i], unscaledMedalsBitmaps[i], scale);
        for (int i = 0; i < 10; i++) {
            numberBitmaps[i] = rescale(numberBitmaps[i], unscaledNumberBitmaps[i], scale);
            smallNumberBitmaps[i] = rescale(smallNumberBitmaps[i], unscaledNumberBitmaps[i], scale * SCORE_PANEL_NUMBER_SCALE_MULTIPLIER);
            panelNumberBitmaps[i] = rescale(panelNumberBitmaps[i], unscaledPanelNumbers[i], scale);
        }
        tutorialBitmap = rescale(tutorialBitmap, unscaledTutorial, scale);
        titleBitmap = rescale(titleBitmap, unscaledTitle, scale);
        copyrightBitmap = rescale(copyrightBitmap, unscaledCopyright, scale);
        scoreButtonBitmap = rescale(scoreButtonBitmap, unscaledButtonScore, scale);
        rateButtonBitmap = rescale(rateButtonBitmap, unscaledButtonRate, scale);
        newLabelBitmap = rescale(newLabelBitmap, unscaledNewLabel, scale);
        getReadyBitmap = rescale(getReadyBitmap, unscaledTextReady, scale);
        gameOverBitmap = rescale(gameOverBitmap, unscaledTextGameOver, scale);
        scorePanelBitmap = rescale(scorePanelBitmap, unscaledScorePanel, scale);
        playButtonBitmap = rescale(playButtonBitmap, unscaledButtonPlay, scale);
        settingsButtonBitmap = rescale(settingsButtonBitmap, unscaledButtonSettings, scale * SETTINGS_BUTTON_SCALE_MULTIPLIER);

        bgDayBitmap.prepareToDraw();
        bgNightBitmap.prepareToDraw();
        groundBitmap.prepareToDraw();

        bitmapScale = scale;
        bitmapScreenHeight = screenHeight;
    }

    private void assignBirdBitmaps() {
        for (int i = 0; i < 3; i++) birdBitmaps[i] = scaledAllBirdBitmaps[currentBirdColor][i];
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (soundPool == null && filmSounds) loadSounds();
        ViewCompat.requestApplyInsets(this);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (isReady && screenWidth > 0 && w == screenWidth && h > 0) {
            if (h < screenHeight && (nameOverlayOpen || keyboardCut > 0f)) {
                keyboardCut = screenHeight - h;
                invalidate();
                return;
            }
            if (h == screenHeight) {
                keyboardCut = 0f;
                invalidate();
                return;
            }
        }
        keyboardCut = 0f;
        screenWidth = w;
        screenHeight = h;
        applyLayout();
        if (!filmMode && w > 0 && h > 0 && (w != loggedWidth || h != loggedHeight)) {
            loggedWidth = w;
            loggedHeight = h;
            SettingsActivity.ActivityLog.add(getContext(), "SCREEN", "Game size " + w + " x " + h
                    + (w > h ? " (landscape)" : " (portrait)"));
        }
    }

    private int getPlayableHeight() {
        return screenHeight - systemBarTop - systemBarBottom;
    }

    private float groundTopY() {
        return screenHeight - systemBarBottom - groundHeight;
    }

    private float playAreaCenterY() {
        return systemBarTop + (getPlayableHeight() - groundHeight) / 2f;
    }

    private float referenceWidth() {
        return unscaledBgDay.getWidth() * scale;
    }

    private float fitToWidth(float contentWidth) {
        return contentWidth > uiAvailableWidth ? uiAvailableWidth / contentWidth : 1.0f;
    }

    private void applyLayout() {
        int playableHeight = screenHeight - windowInsetTop - windowInsetBottom;
        if (screenWidth <= 0 || playableHeight < MIN_PLAYABLE_HEIGHT_PX) return;

        boolean unchanged = isReady
                && screenWidth == laidOutWidth && screenHeight == laidOutHeight
                && windowInsetTop == systemBarTop && windowInsetBottom == systemBarBottom
                && windowInsetLeft == safeInsetLeft && windowInsetRight == safeInsetRight;
        if (unchanged) return;

        final float previousScale = scale;
        final int previousTop = systemBarTop;
        final float previousBirdAnchorX = gameplayBirdX;
        final float previousBgWidth = bgDayBitmap != null ? bgDayBitmap.getWidth() : 0f;
        final int previousPanelTargetY = gameOverElementsTargetY;

        laidOutWidth = screenWidth;
        laidOutHeight = screenHeight;
        systemBarTop = windowInsetTop;
        systemBarBottom = windowInsetBottom;
        safeInsetLeft = windowInsetLeft;
        safeInsetRight = windowInsetRight;

        scale = (float) playableHeight / unscaledBgDay.getHeight();
        fxScale = scale / REFERENCE_SCALE;

        baseGravityPerSec = (GRAVITY_PER_FRAME * scale) * TARGET_FPS * TARGET_FPS;
        baseFlapVelocityPerSec = (FLAP_VELOCITY_PER_FRAME * scale) * TARGET_FPS;
        basePipeSpeedPerSec = (PIPE_SPEED_PER_FRAME * scale) * TARGET_FPS;
        baseRotationDelayThresholdPerSec = (ROTATION_DELAY_VELOCITY_PER_FRAME * scale) * TARGET_FPS;
        jetpackThrustPerSec = baseGravityPerSec * JETPACK_THRUST_FACTOR;
        pressOffsetY = 4 * scale;

        if (scale != bitmapScale || screenHeight != bitmapScreenHeight) scaleAllBitmaps();
        groundHeight = groundBitmap.getHeight();
        maxPipeMoveRange = playableHeight * PIPE_MOVE_RANGE_PERCENT;
        pipeKit.size(scale);

        float reference = referenceWidth();
        uiMargin = Math.min(screenWidth, reference) * UI_MARGIN_HORIZONTAL_PERCENT;
        homeButtonGap = Math.min(screenWidth, reference) * HOME_BUTTON_GAP_PERCENT;
        cornerMargin = Math.min(screenWidth, screenHeight) * UI_MARGIN_HORIZONTAL_PERCENT;
        uiAvailableWidth = Math.max(1f, screenWidth - 2 * Math.max(safeInsetLeft, safeInsetRight) - 2 * uiMargin);
        uiScaleCorrection = fitToWidth(scorePanelBitmap.getWidth());

        updateEffectScale();
        weatherSystem.init(screenWidth, screenHeight, fxScale);

        if (!isReady) {
            resetGame();
            gameState = GameState.HOME;
            isReady = true;
            if (hostResumed) startLoop();
        } else {
            rescaleWorld(previousScale, previousTop, previousBirdAnchorX, previousBgWidth, previousPanelTargetY);
        }
        invalidate();
    }

    private void updateEffectScale() {
        sparkPaint.setStrokeWidth(Math.max(2f, 5f * fxScale));
        electricityPipePaint.setStrokeWidth(Math.max(1.5f, 3f * fxScale));
        electricityGlowPaint.setStrokeWidth(Math.max(4f, 10f * fxScale));

        float shineWidth = Math.max(1f, 100f * fxScale);
        int[] colors = {Color.argb(0, 255, 215, 0), Color.argb(150, 255, 255, 100), Color.argb(0, 255, 215, 0)};
        float[] positions = {0, 0.5f, 1};
        goldenPipeShader = new LinearGradient(0, 0, shineWidth, 0, colors, positions, Shader.TileMode.MIRROR);
        goldenPipePaint.setShader(goldenPipeShader);
        goldenShinePeriod = shineWidth * 2;
    }

    private void rescaleWorld(float previousScale, int previousTop, float previousBirdAnchorX,
                              float previousBgWidth, int previousPanelTargetY) {
        float ratio = scale / previousScale;
        if (gameState == GameState.PLAYING) replayLive = false;

        cancelButtonPress();
        applyTuning();
        assignBirdBitmaps();

        boolean birdAtHome = gameState == GameState.HOME
                || (gameState == GameState.TRANSITION_TO_WAITING && isFadingOut);
        baseBirdX = birdAtHome ? screenWidth / 2f : gameplayBirdX;
        birdX = baseBirdX;
        birdY = systemBarTop + (birdY - previousTop) * ratio;
        birdVelocityY *= ratio;

        groundX *= ratio;
        if (previousBgWidth > 0) backgroundX *= bgDayBitmap.getWidth() / previousBgWidth;
        currentPipeMoveRange = Math.min(currentPipeMoveRange * ratio, maxPipeMoveRange);
        currentShakeMagnitude *= ratio;

        trailCount = 0;
        smokeCount = 0;
        sparkCount = 0;

        boolean pipesOnTheMove = gameState == GameState.PLAYING
                || gameState == GameState.GAME_OVER || gameState == GameState.PANEL_SLIDING;
        if (pipesOnTheMove) {
            int pipeWidth = pipeUpBitmap.getWidth(), pipeHeight = pipeUpBitmap.getHeight();
            for (int i = 0; i < pipes.size(); i++) {
                pipes.get(i).rescale(ratio, previousTop, systemBarTop, previousBirdAnchorX, gameplayBirdX, pipeWidth, pipeHeight);
            }
            extendPipeChain();
        } else {
            spawnPipes();
        }

        if (gameState == GameState.PANEL_SLIDING) {
            gameOverElementsTargetY = computeGameOverPanelTargetY();
            boolean arrived = gameOverElementsY <= previousPanelTargetY;
            gameOverElementsY = arrived
                    ? gameOverElementsTargetY
                    : gameOverElementsTargetY + (gameOverElementsY - previousPanelTargetY) * ratio;
        }
        updateBirdRect();
    }

    private void resetGame() {
        loadSettings();

        updateWeatherSounds();
        weatherSystem.setWeatherType(settingWeatherEffect);
        weatherSystem.setHeavyRain(false);

        speedVariation = 1.0f + (random.nextFloat() - 0.5f) * 2 * settingPipeSpeedVariation;
        score = 0;
        guardScore();
        applyTuning();

        baseBirdX = gameplayBirdX;
        isRestarting = false;
        isPaused = false;
        livesLeft = Math.max(0, Math.min(MAX_EXTRA_LIVES, settingExtraLives));
        invulnerableTimer = 0f;
        hasTier1Triggered = false;
        hasTier2Triggered = false;
        pipeMoveStartTime = -1;
        pipesAreMoving = false;
        pipesAreStopping = false;
        pipeAnimationCounter = 0f;
        currentPipeMoveRange = 0f;
        birdHitGroundTime = -1;
        gameOverSettingsAnimProgress = 0f;
        isGameOverIconAnimationDone = false;
        panelFinishedSlidingTime = -1;
        rainbowHue = 0f;
        drunkModePhase = 0f;
        dayNightCycleProgress = 0f;
        shakeTimer = 0f;
        flashAlpha = 0f;

        isBirdBurnt = false;
        burntTimer = 0f;
        pipeElectrificationTimer = 0f;
        hasTriggeredMegaStrike = false;
        lastStormCheckedScore = -1;

        trailStart = 0;
        trailCount = 0;
        smokeCount = 0;
        sparkCount = 0;
        smokeEmission = 0f;
        burntSparkEmission = 0f;
        apocalypseSparkEmission = 0f;

        timeDilation = 1.0f;
        cameraZoom = 1.0f;
        cinematicPhase = 0;

        sparkleTimer = 0f;
        isSparkleAnimating = false;

        playBtnProgress = 0;
        scoreBtnProgress = 0;
        rateBtnProgress = 0;
        isNewBest = false;
        settingsBtnProgress = 0;
        okBtnProgress = 0;
        gameOverSettingsBtnProgress = 0;

        if (!settingDynamicDayNightEnabled) {
            if (settingBackground == 0) nightBackground = false;
            else if (settingBackground == 1) nightBackground = true;
            else nightBackground = random.nextBoolean();
        }

        currentBirdColor = (settingBirdColor >= 0 && settingBirdColor <= 2) ? settingBirdColor : random.nextInt(3);
        assignBirdBitmaps();

        birdX = baseBirdX;
        birdY = playAreaCenterY();
        if (settingUpsideDownEnabled) birdY = screenHeight - birdY;
        birdVelocityY = 0;
        birdRotation = 0;
        birdTiltVelocity = 0f;
        birdFrame = 0;

        spawnPipes();

        score = 0;
        guardScore();
        displayedScore = 0;
        lastScoreTickTime = -1;
        panelFelt = false;
        scoreFeltAt = -1;
        currentMedal = MEDAL_NONE;
        darkenPaint.setAlpha(0);
    }

    private void applyTuning() {
        if (gameState == GameState.PLAYING && (settingsFingerprint() ^ sealKey) != settingsSeal) roundTampered = true;
        float gravityDirection = settingUpsideDownEnabled ? -1 : 1;
        float pipeDirection = settingReversePipesEnabled ? -1 : 1;
        gravityPerSec = baseGravityPerSec * settingGravity * gravityDirection;
        flapVelocityPerSec = baseFlapVelocityPerSec * settingJumpStrength * gravityDirection;
        float tensOfPoints = Math.max(0, score) / 10f;
        float topSpeed = Math.max(1f, settingRampTopSpeed);
        float smallestGap = Math.max(10, Math.min(100, settingRampMinGap)) / 100f;
        float speedRamp = Math.min(topSpeed, 1f + Math.max(0, settingSpeedRampPercent) / 100f * tensOfPoints);
        float gapSqueeze = Math.max(smallestGap, 1f - Math.max(0, settingGapShrinkPercent) / 100f * tensOfPoints);
        difficultyScore = score;

        pipeSpeedPerSec = basePipeSpeedPerSec * settingGameSpeed * speedVariation * pipeDirection * speedRamp;
        rotationDelayThresholdPerSec = baseRotationDelayThresholdPerSec * settingBirdHangDelay;

        pipeGap = (int) (UNSCALED_BIRD_HEIGHT_FOR_GAP * scale * PIPE_GAP_BIRD_HEIGHT_MULTIPLIER * settingPipeGap * gapSqueeze);
        pipeSpacing = Math.max(1, (int) (unscaledBgDay.getWidth() * PIPE_SPACING_FACTOR * scale * settingPipeSpacing));
        if (settingBirdPosition == DEFAULT_BIRD_POSITION) {
            gameplayBirdX = settingReversePipesEnabled ? screenWidth * 2 / 3f : screenWidth / 3f;
        } else {
            float share = Math.max(10, Math.min(90, settingBirdPosition)) / 100f;
            gameplayBirdX = screenWidth * (settingReversePipesEnabled ? 1f - share : share);
        }
        sealTuning();
    }

    private int requiredPipeCount() {
        int needed = (int) Math.ceil((float) screenWidth / pipeSpacing) + 2;
        return Math.min(MAX_PIPE_COUNT, Math.max(MIN_PIPE_COUNT, needed));
    }

    private float firstPipeX() {
        float pipeWidth = pipeUpBitmap.getWidth();
        float edgeDistance = settingReversePipesEnabled ? gameplayBirdX + pipeWidth : screenWidth - gameplayBirdX;
        float referenceEdgeDistance = referenceWidth() * FIRST_PIPE_REFERENCE_DISTANCE
                + (settingReversePipesEnabled ? pipeWidth : 0);
        float delayDistance = Math.abs(pipeSpeedPerSec) * PIPE_SPAWN_DELAY_SECONDS;
        float lead = Math.max(edgeDistance, Math.min(edgeDistance, referenceEdgeDistance) + delayDistance);
        return settingReversePipesEnabled ? gameplayBirdX - lead : gameplayBirdX + lead;
    }

    private void spawnPipes() {
        spawnPipes(seedSource.nextLong() ^ System.nanoTime());
    }

    private void spawnPipes(long seed) {
        replaySeed = seed;
        pipeKit.random.setSeed(seed);
        motionValid = false;
        pipes.clear();
        float direction = settingReversePipesEnabled ? -1 : 1;
        float firstX = firstPipeX();
        int count = requiredPipeCount();
        for (int i = 0; i < count; i++) {
            pipes.add(createPipe(firstX + i * pipeSpacing * direction));
        }
    }

    private void extendPipeChain() {
        if (pipes.isEmpty()) return;
        float direction = settingReversePipesEnabled ? -1 : 1;
        int count = requiredPipeCount();
        while (pipes.size() < count) {
            float lastX = pipes.get(0).x;
            for (int i = 1; i < pipes.size(); i++) {
                float x = pipes.get(i).x;
                if ((x - lastX) * direction > 0) lastX = x;
            }
            pipes.add(createPipe(lastX + pipeSpacing * direction));
        }
    }

    private Pipe createPipe(float x) {
        Pipe pipe = new Pipe(pipeKit, x, pipeUpBitmap.getWidth(), pipeUpBitmap.getHeight());
        randomizePipe(pipe);
        return pipe;
    }

    private void randomizePipe(Pipe pipe) {
        int playHeight = getPlayableHeight() - groundHeight;
        if (settingGapPlacement == GAP_PLACEMENT_CLASSIC) {
            pipe.resetHeightClassic(pipeGap, playHeight, systemBarTop, settingPipeVariation, scale);
        } else {
            pipe.resetHeight(pipeGap, playHeight, systemBarTop, settingPipeVariation);
        }
        pipe.isScored = false;

        int color = settingPipeColor;
        if (settingRandomPipeColorsEnabled) {
            random.nextInt(CLASSIC_PIPE_COLOR_COUNT);
            color = fxRandom.nextInt(PIPE_COLOR_COUNT);
        }
        paintPipe(pipe, color);

        pipe.isGolden = settingGoldenPipeEnabled && random.nextInt(100) < settingGoldenPipeChance;
    }

    private void paintPipe(Pipe pipe, int color) {
        pipe.colorIndex = color;
        pipe.setColorFilter(color >= 0 && color < PIPE_COLOR_COUNT ? pipeColorFilters[color] : null);
    }

    private void update(float realDeltaTime) {
        if (isPaused) {
            pauseFade = Math.min(1f, pauseFade + realDeltaTime / PAUSE_FADE_SEC);
            return;
        }
        clock += realDeltaTime;
        float gameDeltaTime = realDeltaTime * timeDilation;
        GameState stateBefore = gameState;
        float birdXBefore = birdX, birdYBefore = birdY, birdRotationBefore = birdRotation;

        if (flashAlpha > 0) {
            flashElapsed += realDeltaTime;
            float left = Math.max(0f, 1f - flashElapsed / flashLengthSec);
            flashAlpha = flashPeak * (flashSharp ? left * left * left * left * left : left);
        }
        updateButtonAnimations(realDeltaTime);
        updateCinematicSequence(realDeltaTime);
        updateScrolling(gameDeltaTime);
        updateWingAnimation();
        updateEffects(realDeltaTime, gameDeltaTime);

        switch (gameState) {
            case PLAYING:
                updatePlaying(realDeltaTime, gameDeltaTime);
                break;
            case HOME:
                baseBirdX = screenWidth / 2f;
                birdX = baseBirdX;
                birdY = homeBirdY();
                birdRotation = 0;
                if (transitionAlpha > 0f) transitionAlpha = Math.max(0f, transitionAlpha - TRANSITION_FADE_SPEED_PER_SEC * realDeltaTime);
                break;
            case TRANSITION_TO_WAITING:
                updateTransitionToWaiting(gameDeltaTime);
                break;
            case WAITING:
                birdX = baseBirdX;
                birdY = waitingBirdY();
                if (settingUpsideDownEnabled) birdY = screenHeight - birdY;
                birdRotation = 0;
                break;
            case GAME_OVER:
                updateGameOver(gameDeltaTime);
                break;
            case PANEL_SLIDING:
                updatePanel(realDeltaTime);
                break;
        }

        updateBirdRect();
        if (settingWeatherEffect > 0) {
            weatherSystem.update(gameDeltaTime, pipeSpeedPerSec * 0.1f, birdRect);
        }

        if (gameState == GameState.PLAYING) {
            updatePipes(gameDeltaTime);
            checkCollisions();
            if (settingNoClipEnabled) checkOffScreen(gameDeltaTime);
        }

        updateMotionBlur(realDeltaTime, stateBefore, birdXBefore, birdYBefore, birdRotationBefore);
    }

    private void updateMotionBlur(float realDeltaTime, GameState stateBefore,
                                  float birdXBefore, float birdYBefore, float birdRotationBefore) {
        if (!settingMotionBlurEnabled) {
            backgroundBlurLength = 0f;
            groundBlurLength = 0f;
            pipeBlurLength = 0f;
            birdBlurX = 0f;
            birdBlurY = 0f;
            birdBlurRotation = 0f;
            return;
        }

        float strength = Math.max(1.0f, Math.min(MOTION_BLUR_MAX_STRENGTH, settingMotionBlurStrength));
        float shutter = MOTION_BLUR_SHUTTER_SEC * strength;
        float maxLength = MOTION_BLUR_MAX_LENGTH * scale;

        boolean worldScrolling = gameState != GameState.GAME_OVER && gameState != GameState.PANEL_SLIDING;
        float worldTravel = worldScrolling ? Math.abs(pipeSpeedPerSec) * timeDilation * shutter : 0f;
        backgroundBlurLength = Math.min(maxLength, worldTravel * Math.abs(settingBgScrollSpeed));
        groundBlurLength = Math.min(maxLength, worldTravel * Math.abs(settingGroundScrollSpeed));
        pipeBlurLength = gameState == GameState.PLAYING ? Math.min(maxLength, worldTravel) : 0f;

        boolean birdInFlight = gameState == stateBefore
                && (gameState == GameState.PLAYING || gameState == GameState.GAME_OVER);
        if (!birdInFlight || realDeltaTime <= 0) {
            birdBlurX = 0f;
            birdBlurY = 0f;
            birdBlurRotation = 0f;
            return;
        }

        float exposure = shutter / realDeltaTime;
        birdBlurX = (birdX - birdXBefore) * exposure;
        birdBlurY = (birdY - birdYBefore) * exposure;
        birdBlurRotation = (birdRotation - birdRotationBefore) * exposure;

        float travel = (float) Math.hypot(birdBlurX, birdBlurY);
        if (travel > maxLength) {
            float shrink = maxLength / travel;
            birdBlurX *= shrink;
            birdBlurY *= shrink;
            birdBlurRotation *= shrink;
        }
        birdBlurRotation = Math.max(-MOTION_BLUR_MAX_ROTATION, Math.min(MOTION_BLUR_MAX_ROTATION, birdBlurRotation));
    }

    private int motionSamples(float blurLength) {
        if (blurLength < 1.0f) return 1;
        int samples = (int) Math.ceil(blurLength / (MOTION_BLUR_SAMPLE_SPACING * scale)) + 1;
        return Math.max(2, Math.min(MOTION_BLUR_MAX_SAMPLES, samples));
    }

    private static float samplePosition(int index, int samples) {
        return (index + 0.5f) / samples - 0.5f;
    }

    private static int sampleAlpha(int totalAlpha, int index, int samples) {
        return Math.round(totalAlpha * (index + 1) / (float) samples) - Math.round(totalAlpha * index / (float) samples);
    }

    private void updateButtonAnimations(float dt) {
        float pressSpeed = BUTTON_PRESS_SPEED * dt;
        playBtnProgress += (playBtnTarget - playBtnProgress) * pressSpeed;
        scoreBtnProgress += (scoreBtnTarget - scoreBtnProgress) * pressSpeed;
        rateBtnProgress += (rateBtnTarget - rateBtnProgress) * pressSpeed;
        okBtnProgress += (okBtnTarget - okBtnProgress) * pressSpeed;
        float press = 1f - (float) Math.exp(-Math.max(0f, dt) * 18f);
        settingsBtnProgress += (settingsBtnTarget - settingsBtnProgress) * press;
        gameOverSettingsBtnProgress += (gameOverSettingsBtnTarget - gameOverSettingsBtnProgress) * press;
        float scaleTarget = settingsBtnTarget > 0f || gameOverSettingsBtnTarget > 0f ? SETTINGS_PRESSED_SCALE : 1f;
        float remaining = Math.min(0.1f, Math.max(0f, dt));
        while (remaining > 0f) {
            float h = Math.min(remaining, 1f / 240f);
            settingsScaleVelocity += ((scaleTarget - settingsScale) * 700f - settingsScaleVelocity * 30f) * h;
            settingsScale += settingsScaleVelocity * h;
            remaining -= h;
        }
        if (Math.abs(scaleTarget - settingsScale) < 0.0005f && Math.abs(settingsScaleVelocity) < 0.01f) {
            settingsScale = scaleTarget;
            settingsScaleVelocity = 0f;
        }
        settingsSpin += (settingsSpinTarget - settingsSpin) * (1f - (float) Math.exp(-Math.max(0f, dt) * 9f));
        if (Math.abs(settingsSpinTarget - settingsSpin) < 0.05f) {
            settingsSpin = settingsSpinTarget % 360f;
            settingsSpinTarget = settingsSpin;
        }
    }

    private float spinBase() {
        return (float) Math.floor(settingsSpin / 360f) * 360f;
    }

    private void drawSettingsGear(Canvas canvas, int alpha, float extraScale) {
        float drawScale = settingsScale * extraScale;
        boolean turning = settingsSpin != 0f;
        Paint paint = pixelPaint;
        if (turning) {
            if (spinPaint == null) {
                spinPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
                spinPaint.setFilterBitmap(true);
            }
            paint = spinPaint;
        }
        paint.setAlpha(alpha);
        canvas.save();
        canvas.scale(drawScale, drawScale, settingsButtonDestRect.centerX(), settingsButtonDestRect.centerY());
        if (turning) canvas.rotate(settingsSpin, settingsButtonDestRect.centerX(), settingsButtonDestRect.centerY());
        canvas.drawBitmap(settingsButtonBitmap, settingsButtonDestRect.left, settingsButtonDestRect.top, paint);
        canvas.restore();
        paint.setAlpha(255);
    }

    private void updateScrolling(float dt) {
        if (gameState == GameState.GAME_OVER || gameState == GameState.PANEL_SLIDING) return;
        backgroundX = (backgroundX - pipeSpeedPerSec * settingBgScrollSpeed * dt) % bgDayBitmap.getWidth();
        groundX = (groundX - pipeSpeedPerSec * settingGroundScrollSpeed * dt) % groundBitmap.getWidth();
    }

    private void updateWingAnimation() {
        if (gameState == GameState.PLAYING) {
            boolean recentlyFlapped = clock - lastFlapTime < FLAP_ANIMATION_TIMEOUT_SEC;
            boolean flapping = (settingWingAnimationEnabled && (settingInfiniteFlapEnabled || recentlyFlapped))
                    || (settingJetpackModeEnabled && isScreenPressed);
            birdFrame = flapping ? (int) (clock / (WING_FRAME_SEC_PLAYING / wingSpeed())) % birdBitmaps.length : 1;
        } else if (gameState == GameState.HOME || gameState == GameState.TRANSITION_TO_WAITING
                || gameState == GameState.WAITING) {
            birdFrame = settingWingAnimationEnabled ? (int) (clock / (WING_FRAME_SEC_IDLE / wingSpeed())) % birdBitmaps.length : 1;
        }
    }

    private float wingSpeed() {
        return Math.max(0.1f, Math.min(10f, settingWingSpeed));
    }

    private boolean isApocalypse() {
        return score >= APOCALYPSE_SCORE && settingWeatherEffect == WEATHER_STORM;
    }

    private void updateEffects(float realDeltaTime, float gameDeltaTime) {
        if (settingRainbowBirdEnabled) {
            rainbowHue = (rainbowHue + 150 * gameDeltaTime) % 360;
        }
        goldenShineOffset = (goldenShineOffset + 200 * fxScale * gameDeltaTime) % goldenShinePeriod;

        if (settingBirdTrailEnabled && (gameState == GameState.PLAYING || gameState == GameState.GAME_OVER)) {
            recordTrail();
        }

        if (shakeTimer > 0) shakeTimer -= realDeltaTime;
        if (pipeElectrificationTimer > 0) {
            pipeElectrificationTimer = Math.max(0f, pipeElectrificationTimer - gameDeltaTime);
        }

        boolean apocalypse = isApocalypse();
        if (apocalypse) weatherSystem.setHeavyRain(true);

        if (isBirdBurnt) {
            burntTimer -= gameDeltaTime;
            if (burntTimer <= 0) {
                isBirdBurnt = false;
                sparkCount = 0;
            } else {
                smokeEmission += SMOKE_PER_SEC * realDeltaTime;
                while (smokeEmission >= 1f) {
                    smokeEmission -= 1f;
                    spawnSmoke(birdX, birdY);
                }
                burntSparkEmission += BURNT_SPARKS_PER_SEC * realDeltaTime;
                while (burntSparkEmission >= 1f) {
                    burntSparkEmission -= 1f;
                    spawnSpark(birdX, birdY, fxRandom.nextBoolean() ? SparkParticle.TYPE_CRACKLE : SparkParticle.TYPE_FALLING);
                }
            }
        }

        if (apocalypse) {
            apocalypseSparkEmission += APOCALYPSE_SPARKS_PER_SEC * realDeltaTime;
            while (apocalypseSparkEmission >= 1f) {
                apocalypseSparkEmission -= 1f;
                spawnSpark(fxRandom.nextFloat() * screenWidth, -50 * fxScale, SparkParticle.TYPE_FALLING);
            }
        }

        updateParticles(gameDeltaTime);

        if (settingDynamicDayNightEnabled && (gameState == GameState.PLAYING || gameState == GameState.WAITING)) {
            float cycleSec = Math.max(MIN_DAY_NIGHT_SEC, settingDayNightSeconds);
            dayNightCycleProgress = (dayNightCycleProgress + gameDeltaTime / cycleSec) % 1.0f;
        }
    }

    private void recordTrail() {
        int index;
        if (trailCount < MAX_TRAIL_PARTICLES) {
            index = (trailStart + trailCount) % MAX_TRAIL_PARTICLES;
            trailCount++;
        } else {
            index = trailStart;
            trailStart = (trailStart + 1) % MAX_TRAIL_PARTICLES;
        }
        birdTrail[index].set(birdX, birdY, birdRotation, currentBirdColor, birdFrame,
                settingRainbowBirdEnabled ? rainbowHue : (settingBirdHue != 0 ? settingBirdHue : -1));
    }

    private void spawnSmoke(float x, float y) {
        if (smokeCount < MAX_SMOKE_PARTICLES) smokeParticles[smokeCount++].init(x, y, scale, fxRandom);
    }

    private void spawnSpark(float x, float y, int type) {
        if (sparkCount < MAX_SPARK_PARTICLES) sparkParticles[sparkCount++].init(x, y, scale, type, fxRandom);
    }

    private void updateParticles(float dt) {
        float smokeWind = pipeSpeedPerSec * 0.5f;
        for (int i = smokeCount - 1; i >= 0; i--) {
            SmokeParticle particle = smokeParticles[i];
            particle.update(dt, smokeWind);
            if (particle.isDead()) {
                smokeCount--;
                smokeParticles[i] = smokeParticles[smokeCount];
                smokeParticles[smokeCount] = particle;
            }
        }

        float sparkWind = pipeSpeedPerSec * 0.8f;
        float sparkGravity = 800f * fxScale;
        for (int i = sparkCount - 1; i >= 0; i--) {
            SparkParticle particle = sparkParticles[i];
            particle.update(dt, sparkWind, sparkGravity);
            if (particle.isDead()) {
                sparkCount--;
                sparkParticles[i] = sparkParticles[sparkCount];
                sparkParticles[sparkCount] = particle;
            }
        }
    }

    private float idleBirdY() {
        return playAreaCenterY() + (float) (Math.sin(clock * BIRD_BOB_SPEED) * BIRD_BOB_AMPLITUDE * scale);
    }

    private boolean classicMenu() {
        return settingClassicMenuEnabled && rateButtonBitmap != null;
    }

    private float homeBirdY() {
        float bob = (float) (Math.sin(clock * BIRD_BOB_SPEED) * BIRD_BOB_AMPLITUDE * scale);
        if (classicMenu()) {
            float onScreen = systemBarTop + MENU_BIRD_Y * scale + bob;
            return settingUpsideDownEnabled ? screenHeight - onScreen : onScreen;
        }
        float y = idleBirdY();
        if (settingUpsideDownEnabled) {
            y += getPlayableHeight() * 0.18f;
            y = screenHeight - y;
        }
        return y;
    }

    private float waitingBirdY() {
        if (settingStartHeight == DEFAULT_START_HEIGHT) return idleBirdY();
        float share = 1f - Math.max(10, Math.min(90, settingStartHeight)) / 100f;
        float anchor = systemBarTop + (getPlayableHeight() - groundHeight) * share;
        return anchor + (float) (Math.sin(clock * BIRD_BOB_SPEED) * BIRD_BOB_AMPLITUDE * scale);
    }

    private boolean pixelSteps() {
        return settingPixelStepsEnabled && settingFixedStepEnabled && timeDilation == 1.0f;
    }

    private float moveBird(float y, float travel) {
        if (!pixelSteps()) return y + travel;
        float source = (y - systemBarTop + travel) / scale;
        float snapped = settingUpsideDownEnabled
                ? (float) Math.ceil(source - PIXEL_STEP_EPSILON)
                : (float) Math.floor(source + PIXEL_STEP_EPSILON);
        return systemBarTop + snapped * scale;
    }

    private float hitboxHalfHeight() {
        float multiplier = Math.max(0.0f, settingBirdHitbox) * settingBirdSize / 2f;
        if (settingHitboxShape == HITBOX_SHAPE_SQUARE) return SQUARE_HITBOX_SIZE * scale * multiplier;
        return birdBitmaps[1].getHeight() * (1.0f - 2.0f * BIRD_HITBOX_PADDING_Y) * multiplier;
    }

    private float hitboxHalfWidth() {
        float multiplier = Math.max(0.0f, settingBirdHitbox) * settingBirdSize / 2f;
        if (settingHitboxShape == HITBOX_SHAPE_SQUARE) return SQUARE_HITBOX_SIZE * scale * multiplier;
        return birdBitmaps[1].getWidth() * (1.0f - 2.0f * BIRD_HITBOX_PADDING_X) * multiplier;
    }

    private boolean canFlapAt(float y) {
        if (!settingFlapLimitEnabled || settingUpsideDownEnabled) return true;
        return y - hitboxHalfHeight() >= systemBarTop;
    }

    private void kickTilt() {
        birdTiltVelocity = CLASSIC_TILT_KICK_PER_SEC * Math.max(0.05f, settingTiltSpeed);
    }

    private float birdHalfHeight(int frame) {
        return (birdBitmaps[frame].getHeight() * settingBirdSize) / 2f;
    }

    private void updatePlaying(float realDeltaTime, float gameDeltaTime) {
        if (invulnerableTimer > 0) invulnerableTimer -= gameDeltaTime;
        if (settingAutopilotEnabled) runAutopilot(gameDeltaTime);

        if (settingJetpackModeEnabled && isScreenPressed) {
            float thrust = jetpackThrustPerSec * settingJumpStrength * (settingUpsideDownEnabled ? -1 : 1);
            birdVelocityY -= thrust * gameDeltaTime;
            kickTilt();
        }

        if (originalMotion()) {
            birdY = moveBird(birdY, originalTravel(birdVelocityY, gravityPerSec, gameDeltaTime));
            birdVelocityY = capFall(birdVelocityY + gravityPerSec * gameDeltaTime);
        } else {
            birdVelocityY = capFall(birdVelocityY + gravityPerSec * gameDeltaTime);
            birdY = moveBird(birdY, birdVelocityY * gameDeltaTime);
        }

        if (!settingNoClipEnabled && !settingUpsideDownEnabled && settingCeilingEnabled) {
            float halfHeight = birdHalfHeight(birdFrame);
            float ceiling = systemBarTop - halfHeight - halfHeight * CEILING_OVERSHOOT;
            if (birdY < ceiling) {
                birdY = ceiling;
                if (birdVelocityY < 0) birdVelocityY = 0;
            }
        }

        if (settingDrunkModeEnabled) {
            drunkModePhase += gameDeltaTime * 2.0f;
            float maxDrift = Math.min(screenWidth, referenceWidth()) * 0.15f * settingDrunkModeStrength;
            birdX = baseBirdX + ((float) Math.sin(drunkModePhase) * maxDrift);
        } else {
            birdX = baseBirdX;
        }

        if (settingWeatherEffect == WEATHER_STORM) updateStorm(realDeltaTime);

        updateTilt(gameDeltaTime);
    }

    private boolean originalMotion() {
        return settingOriginalMotion && !settingUpsideDownEnabled && !settingJetpackModeEnabled && gravityPerSec > 0f;
    }

    private float originalTravel(float velocity, float gravity, float dt) {
        float lead = velocity + gravity * ORIGINAL_STEP_SEC * 0.5f;
        if (settingMaxFallSpeed <= 0f) return lead * dt + 0.5f * gravity * dt * dt;
        float cap = fallLimit();
        if (lead >= cap) return cap * dt;
        float reach = (cap - lead) / gravity;
        if (reach >= dt) return lead * dt + 0.5f * gravity * dt * dt;
        return lead * reach + 0.5f * gravity * reach * reach + cap * (dt - reach);
    }

    private float fallLimit() {
        return Math.abs(baseFlapVelocityPerSec) * settingMaxFallSpeed;
    }

    private float capFall(float velocity) {
        if (settingMaxFallSpeed <= 0f) return velocity;
        float limit = fallLimit();
        return settingUpsideDownEnabled ? Math.max(-limit, velocity) : Math.min(limit, velocity);
    }

    private float tiltUpLimit() {
        return settingBirdTiltEnabled ? Math.max(0, Math.min(90, settingMaxTiltUp)) : 0f;
    }

    private float tiltDownLimit() {
        return settingBirdTiltEnabled ? Math.max(0, Math.min(90, settingMaxTiltDown)) : 0f;
    }

    private void updateTilt(float dt) {
        if (!settingBirdTiltEnabled) {
            birdRotation = 0f;
            return;
        }
        float speed = Math.max(0.05f, settingTiltSpeed);
        float up = tiltUpLimit();
        float down = tiltDownLimit();
        if (settingTiltStyle == TILT_STYLE_CLASSIC) {
            float side = settingUpsideDownEnabled ? -1f : 1f;
            float pull = CLASSIC_TILT_PULL_PER_SEC2 * speed * speed;
            float angle = settingOriginalMotion
                    ? birdRotation * side + (birdTiltVelocity - pull * ORIGINAL_STEP_SEC * 0.5f) * dt + 0.5f * pull * dt * dt
                    : birdRotation * side + birdTiltVelocity * dt;
            birdTiltVelocity += pull * dt;
            birdRotation = Math.max(-up, Math.min(down, angle)) * side;
            return;
        }
        if (settingUpsideDownEnabled) {
            if (birdVelocityY > 0) {
                birdRotation = Math.min(up, birdRotation + ROTATION_UP_SPEED_PER_SEC * speed * dt);
            } else if (birdVelocityY < -rotationDelayThresholdPerSec) {
                birdRotation = Math.max(-down, birdRotation - ROTATION_DOWN_SPEED_PER_SEC * speed * dt);
            }
        } else {
            if (birdVelocityY < 0) {
                birdRotation = Math.max(-up, birdRotation - ROTATION_UP_SPEED_PER_SEC * speed * dt);
            } else if (birdVelocityY > rotationDelayThresholdPerSec) {
                birdRotation = Math.min(down, birdRotation + ROTATION_DOWN_SPEED_PER_SEC * speed * dt);
            }
        }
    }

    private void startFlash(int color, float alpha, float lengthSec, boolean sharp) {
        if (alpha <= 0f) return;
        if (flashAlpha > 0f && flashColor != color && flashAlpha > alpha) return;
        flashColor = color;
        flashPeak = Math.min(255f, alpha);
        flashAlpha = flashPeak;
        flashLengthSec = Math.max(0.01f, lengthSec);
        flashElapsed = 0f;
        flashSharp = sharp;
    }

    private void startCrashFlash(float share) {
        if (!settingCrashFlashEnabled) return;
        int index = settingCrashFlashColor >= 0 && settingCrashFlashColor < CRASH_FLASH_COLORS.length ? settingCrashFlashColor : 0;
        float strength = Math.max(0, Math.min(100, settingCrashFlashStrength)) / 100f;
        startFlash(CRASH_FLASH_COLORS[index], 255f * strength * share, Math.max(10, settingCrashFlashLengthMs) / 1000f,
                settingCrashFlashFade == FLASH_FADE_SHARP);
    }

    private void startLightningFlash() {
        startFlash(Color.WHITE, 255f, 255f / FLASH_DECAY_PER_SEC, false);
    }

    private void updateStorm(float realDeltaTime) {
        float strikesPerSec = score >= APOCALYPSE_SCORE ? LIGHTNING_PER_SEC_APOCALYPSE : LIGHTNING_PER_SEC;
        if (fxRandom.nextFloat() < strikesPerSec * realDeltaTime) triggerLightning();

        if (score == lastStormCheckedScore) return;
        lastStormCheckedScore = score;
        if (settingLightningScores.contains(score)) {
            startCinematicStrikeSequence(false);
        } else if (score == settingMegaStrikeScore && !hasTriggeredMegaStrike) {
            hasTriggeredMegaStrike = true;
            startCinematicStrikeSequence(true);
        }
    }

    private void updateTransitionToWaiting(float dt) {
        if (isFadingOut && classicMenu()) {
            birdY = homeBirdY();
        } else {
            birdY = isFadingOut ? idleBirdY() : waitingBirdY();
            if (settingUpsideDownEnabled) birdY = screenHeight - birdY;
        }
        birdRotation = 0;

        if (isFadingOut) {
            transitionAlpha += TRANSITION_FADE_SPEED_PER_SEC * dt;
            if (transitionAlpha >= 255) {
                transitionAlpha = 255;
                isFadingOut = false;
                baseBirdX = gameplayBirdX;
                birdX = baseBirdX;
            }
        } else {
            transitionAlpha -= TRANSITION_FADE_SPEED_PER_SEC * dt;
            if (transitionAlpha <= 0) {
                transitionAlpha = 0;
                gameState = GameState.WAITING;
            }
        }
    }

    private void updateGameOver(float dt) {
        float halfHeight = birdHalfHeight(1);
        float groundTop = groundTopY();
        boolean isOnGround = settingUpsideDownEnabled
                ? birdY - halfHeight <= systemBarTop
                : birdY + halfHeight >= groundTop;
        boolean gaveUpWaiting = !isOnGround && clock - gameOverTime > GAME_OVER_MAX_FALL_SEC;

        if (!isOnGround && !gaveUpWaiting) {
            float fallGravity = gravityPerSec * Math.max(0f, settingCrashGravity);
            if (originalMotion() && fallGravity > 0f) {
                birdY = moveBird(birdY, originalTravel(birdVelocityY, fallGravity, dt));
                birdVelocityY = capFall(birdVelocityY + fallGravity * dt);
            } else {
                birdVelocityY = capFall(birdVelocityY + fallGravity * dt);
                birdY = moveBird(birdY, birdVelocityY * dt);
            }
            float deathAngle = tiltDownLimit();
            if (settingTiltStyle == TILT_STYLE_CLASSIC) {
                updateTilt(dt);
            } else if (settingUpsideDownEnabled) {
                birdRotation = Math.max(-deathAngle, birdRotation - DEATH_ROTATION_SPEED_PER_SEC * dt);
            } else {
                birdRotation = Math.min(deathAngle, birdRotation + DEATH_ROTATION_SPEED_PER_SEC * dt);
            }
            birdHitGroundTime = -1;
            return;
        }

        if (isOnGround) {
            if (settingUpsideDownEnabled) {
                birdY = systemBarTop + halfHeight;
                birdRotation = -tiltDownLimit();
            } else {
                birdY = groundTop - halfHeight;
                birdRotation = tiltDownLimit();
            }
        }
        birdFrame = 1;
        if (birdHitGroundTime < 0) {
            birdHitGroundTime = clock;
            if (isOnGround) {
                triggerShake(5.0f, 0.4f);
                if (clock - gameOverTime > 0.15f) feel(Feel.LAND);
            }
        }

        if (clock - birdHitGroundTime > PANEL_SLIDE_DELAY_SEC) {
            gameOverElementsTargetY = computeGameOverPanelTargetY();
            gameOverElementsY = screenHeight;
            gameState = GameState.PANEL_SLIDING;
            birdHitGroundTime = -1;
        }
    }

    private int computeGameOverPanelTargetY() {
        float textHeight = gameOverBitmap.getHeight() * uiScaleCorrection;
        float gap = (20 * scale) * uiScaleCorrection;
        float panelHeight = scorePanelBitmap.getHeight() * uiScaleCorrection;
        float totalHeight = textHeight + gap + panelHeight;
        int playAreaCenter = systemBarTop + (getPlayableHeight() - groundHeight) / 2;
        return (int) (playAreaCenter - (totalHeight / 2));
    }

    private void updatePanel(float realDeltaTime) {
        if (gameOverElementsY > gameOverElementsTargetY) {
            float deltaMultiplier = TARGET_FPS * realDeltaTime;
            float portionToMove = 1.0f - (float) Math.pow(PANEL_SLIDE_EASING_BASE, deltaMultiplier);
            gameOverElementsY -= (gameOverElementsY - gameOverElementsTargetY) * portionToMove;
            if (gameOverElementsY - gameOverElementsTargetY < 1.0f) {
                gameOverElementsY = gameOverElementsTargetY;
            }
        } else {
            gameOverElementsY = gameOverElementsTargetY;
            if (currentMedal != MEDAL_NONE && displayedScore == score) updateMedalSparkle(realDeltaTime);
            updateScoreCountUp();

            if (panelFinishedSlidingTime < 0) panelFinishedSlidingTime = clock;
            if (!isGameOverIconAnimationDone && clock - panelFinishedSlidingTime > GAMEOVER_ICON_SLIDE_DELAY_SEC) {
                gameOverSettingsAnimProgress += 5f * realDeltaTime;
                if (gameOverSettingsAnimProgress >= 1.0f) {
                    gameOverSettingsAnimProgress = 1.0f;
                    isGameOverIconAnimationDone = true;
                }
            }
        }

        int maxAlphaFromSettings = (int) (settingGameOverOpacity / 100.0f * 255.0f);
        if (screenHeight > gameOverElementsTargetY) {
            float progress = 1.0f - ((gameOverElementsY - gameOverElementsTargetY) / (screenHeight - gameOverElementsTargetY));
            progress = Math.max(0.0f, Math.min(1.0f, progress));
            darkenPaint.setAlpha(Math.max(0, Math.min(255, (int) (maxAlphaFromSettings * progress))));
        }

        if (isRestarting && returningHome && returnHoldSec > 0f) {
            returnHoldSec -= realDeltaTime;
        } else if (isRestarting) {
            transitionAlpha += TRANSITION_FADE_SPEED_PER_SEC * realDeltaTime;
            if (transitionAlpha >= 255) {
                transitionAlpha = 255;
                if (returningHome) {
                    returningHome = false;
                    loadSettings();
                    resetGame();
                    gameState = GameState.HOME;
                } else {
                    resetGame();
                    gameState = GameState.TRANSITION_TO_WAITING;
                }
                isFadingOut = false;
            }
        }
    }

    private void updateMedalSparkle(float dt) {
        sparkleTimer += dt;
        if (!isSparkleAnimating) {
            if (sparkleTimer >= SPARKLE_INTERVAL) {
                isSparkleAnimating = true;
                sparkleTimer = 0;
                sparkleXOffset = 0.2f + fxRandom.nextFloat() * 0.6f;
                sparkleYOffset = 0.2f + fxRandom.nextFloat() * 0.6f;
            }
            return;
        }
        float progress = sparkleTimer / SPARKLE_DURATION;
        if (progress >= 1.0f) {
            isSparkleAnimating = false;
            sparkleTimer = 0;
            sparkleScaleCurrent = 0;
        } else {
            sparkleScaleCurrent = (float) Math.sin(progress * Math.PI);
        }
    }

    private void feelPanel() {
        if (panelFelt) return;
        panelFelt = true;
        if (isNewBest) feel(Feel.BEST);
        else if (currentMedal != MEDAL_NONE) feel(Feel.MEDAL);
    }

    private void updateScoreCountUp() {
        if (lastScoreTickTime < 0) lastScoreTickTime = clock;
        if (displayedScore >= score) {
            if (score > 0) feelPanel();
            return;
        }

        int remaining = score - displayedScore;
        float interval;
        int increment;
        if (remaining > 100) {
            interval = SCORE_TICK_SEC_FAST;
            increment = 11;
        } else if (remaining > 10) {
            interval = SCORE_TICK_SEC_FAST;
            increment = 3;
        } else {
            interval = SCORE_TICK_SEC_SLOW;
            increment = 1;
        }
        if (clock - lastScoreTickTime > interval) {
            displayedScore = Math.min(score, displayedScore + increment);
            lastScoreTickTime = clock;
            if (displayedScore >= score) {
                feelPanel();
            } else if (scoreFeltAt < 0 || clock - scoreFeltAt >= 0.05) {
                scoreFeltAt = clock;
                feel(Feel.TICK);
            }
        }
    }

    private void updatePipes(float dt) {
        if (settingMovingPipesEnabled) updatePipeMotion(dt);

        float stopDecay = (float) Math.pow(PIPE_STOP_DECAY_PER_FRAME, dt * TARGET_FPS);
        float waveLength = PIPE_WAVE_LENGTH * fxScale;
        float direction = settingReversePipesEnabled ? -1 : 1;

        for (int i = 0; i < pipes.size(); i++) {
            Pipe pipe = pipes.get(i);
            pipe.x -= pipeSpeedPerSec * dt;
            if (settingMovingPipesEnabled) {
                pipe.updateAnimation(pipesAreMoving, pipeAnimationCounter, currentPipeMoveRange,
                        pipesAreStopping, stopDecay, waveLength);
            }

            float scoreShare = settingScorePoint == SCORE_POINT_START ? 0f : settingScorePoint == SCORE_POINT_MIDDLE ? 0.5f : 1f;
            boolean passedBird;
            if (settingScorePoint == SCORE_POINT_CLASSIC) {
                passedBird = settingReversePipesEnabled ? pipe.x + pipe.width >= birdRect.right : pipe.x <= birdRect.left;
            } else {
                passedBird = settingReversePipesEnabled
                        ? pipe.x + pipe.width * (1f - scoreShare) > birdX
                        : pipe.x + pipe.width * scoreShare < birdX;
            }
            if (!pipe.isScored && passedBird) {
                pipe.isScored = true;
                countPipe();
                addScore(settingScoreMultiplier);
                if (pipe.isGolden) {
                    addScore(settingGoldenPipeBonus);
                    roundGoldenPoints += settingGoldenPipeBonus;
                }
                playSound(soundPoint);
                feel(pipe.isGolden ? Feel.GOLDEN : Feel.SCORE);
            }

            boolean leftTheScreen = settingReversePipesEnabled
                    ? pipe.x - (pipe.width * settingPipeWidth) > screenWidth
                    : pipe.x + (pipe.width * settingPipeWidth) < 0;
            if (leftTheScreen) {
                pipe.x += pipes.size() * pipeSpacing * direction;
                randomizePipe(pipe);
            }
        }

        boolean difficultyGrows = settingSpeedRampPercent > 0 || settingGapShrinkPercent > 0;
        if (difficultyGrows && score != difficultyScore) applyTuning();
    }

    private void updatePipeMotion(float dt) {
        if (score >= settingPipeMoveTier1 && !hasTier1Triggered) {
            hasTier1Triggered = true;
            startPipeMotion(PIPE_MOVE_SPEED_TIER_1);
        }
        if (score >= settingPipeMoveTier2 && !hasTier2Triggered) {
            hasTier2Triggered = true;
            startPipeMotion(PIPE_MOVE_SPEED_TIER_2);
        }
        if (pipeMoveStartTime >= 0 && !pipesAreStopping && clock - pipeMoveStartTime > PIPE_MOVE_DURATION_SEC) {
            pipesAreStopping = true;
            pipeMoveStartTime = -1;
        }
        if (pipesAreStopping) {
            boolean allPipesAtRest = true;
            for (int i = 0; i < pipes.size(); i++) {
                if (!pipes.get(i).isAtRest()) {
                    allPipesAtRest = false;
                    break;
                }
            }
            if (allPipesAtRest) {
                pipesAreMoving = false;
                pipesAreStopping = false;
            }
        }
        if (pipesAreMoving || pipesAreStopping) {
            pipeAnimationCounter += (pipeAnimationSpeed * TARGET_FPS) * dt;
            if (currentPipeMoveRange < maxPipeMoveRange && !pipesAreStopping) {
                float rampSpeed = PIPE_MOVE_RAMP_PER_FRAME * scale * TARGET_FPS;
                currentPipeMoveRange = Math.min(currentPipeMoveRange + rampSpeed * dt, maxPipeMoveRange);
            }
        }
    }

    private void startPipeMotion(float speed) {
        pipesAreMoving = true;
        pipesAreStopping = false;
        pipeAnimationSpeed = speed;
        pipeMoveStartTime = clock;
    }

    private void updateBirdRect() {
        Bitmap currentBirdBitmap = birdBitmaps[birdFrame];
        float baseHitboxWidth = currentBirdBitmap.getWidth() * (1.0f - 2.0f * BIRD_HITBOX_PADDING_X);
        float baseHitboxHeight = currentBirdBitmap.getHeight() * (1.0f - 2.0f * BIRD_HITBOX_PADDING_Y);
        float hitboxMultiplier = Math.max(0.0f, settingBirdHitbox);
        if (settingHitboxShape == HITBOX_SHAPE_SQUARE) {
            baseHitboxWidth = SQUARE_HITBOX_SIZE * scale;
            baseHitboxHeight = SQUARE_HITBOX_SIZE * scale;
        }
        float halfWidth = baseHitboxWidth * hitboxMultiplier * settingBirdSize / 2f;
        float halfHeight = baseHitboxHeight * hitboxMultiplier * settingBirdSize / 2f;
        birdRect.set((int) (birdX - halfWidth), (int) (birdY - halfHeight),
                (int) (birdX + halfWidth), (int) (birdY + halfHeight));
    }

    private void checkOffScreen(float deltaTime) {
        if (!settingGodFallEnabled) {
            offScreenSeconds = 0f;
            return;
        }
        float halfHeight = birdHalfHeight(birdFrame);
        boolean gone = birdY - halfHeight > screenHeight || birdY + halfHeight < 0f;
        offScreenSeconds = gone ? offScreenSeconds + deltaTime : 0f;
        if (offScreenSeconds >= settingGodFallSeconds) {
            roundEndCause = "off screen too long in God mode";
            gameOver();
        }
    }

    private void checkCollisions() {
        if (settingNoClipEnabled) return;
        float halfHeight = birdHalfHeight(birdFrame);
        float groundTop = groundTopY();

        boolean hitFloor = settingUpsideDownEnabled ? birdRect.bottom >= groundTop : birdY + halfHeight >= groundTop;
        boolean hitRoof = settingUpsideDownEnabled && birdY - halfHeight <= systemBarTop;
        if (hitFloor || hitRoof) {
            if (!surviveHit()) {
                roundEndCause = hitFloor ? "hit the ground" : "hit the ceiling";
                gameOver();
                return;
            }
            float bounce = Math.max(Math.abs(flapVelocityPerSec), Math.abs(baseFlapVelocityPerSec) * 0.5f);
            if (hitFloor) {
                birdY = Math.min(birdY, groundTop - halfHeight - 1f);
                birdVelocityY = -bounce;
            } else {
                birdY = Math.max(birdY, systemBarTop + halfHeight + 1f);
                birdVelocityY = bounce;
            }
            kickTilt();
            return;
        }

        if (invulnerableTimer > 0) return;
        for (int i = 0; i < pipes.size(); i++) {
            Pipe pipe = pipes.get(i);
            if (Rect.intersects(birdRect, pipe.getTopHeadRect(settingPipeWidth))
                    || Rect.intersects(birdRect, pipe.getTopBodyRect(settingPipeWidth))
                    || Rect.intersects(birdRect, pipe.getBottomHeadRect(pipeGap, settingPipeWidth))
                    || Rect.intersects(birdRect, pipe.getBottomBodyRect(pipeGap, settingPipeWidth))) {
                if (!surviveHit()) {
                    roundEndCause = "hit a pipe";
                    gameOver();
                }
                return;
            }
        }
    }

    private boolean surviveHit() {
        if (invulnerableTimer > 0) return true;
        if (livesLeft <= 0) return false;
        livesLeft--;
        roundLivesUsed++;
        invulnerableTimer = INVULNERABLE_SEC;
        startCrashFlash(150f / 255f);
        playSound(soundHit);
        feel(Feel.LIFE);
        triggerShake(5.0f, 0.3f);
        return true;
    }

    private float gapTopOf(Pipe pipe) {
        return pipe.getTopPipeY() + pipe.height;
    }

    private float gapTopAt(Pipe pipe, float shift, float time) {
        if (!settingMovingPipesEnabled || !pipesAreMoving || pipesAreStopping) return gapTopOf(pipe);
        float counter = pipeAnimationCounter + pipeAnimationSpeed * TARGET_FPS * time;
        float phase = ((pipe.x - shift) / (PIPE_WAVE_LENGTH * fxScale)) * (float) Math.PI;
        return pipe.topPipeY + (float) Math.sin(counter + phase) * currentPipeMoveRange;
    }

    private Pipe nextPipeAhead(float shift, float halfWidth, float pilotX) {
        Pipe best = null;
        float bestDistance = Float.MAX_VALUE;
        for (int i = 0; i < pipes.size(); i++) {
            Pipe pipe = pipes.get(i);
            float visualWidth = pipe.width * settingPipeWidth;
            float left = pipe.x - shift - (visualWidth - pipe.width) / 2f;
            float distance = settingReversePipesEnabled
                    ? (pilotX + halfWidth) - left
                    : (left + visualWidth) - (pilotX - halfWidth);
            if (distance >= 0f && distance < bestDistance) {
                bestDistance = distance;
                best = pipe;
            }
        }
        return best;
    }

    private boolean pilotWantsLift(float y, float velocity, float top, float halfHeight) {
        if (settingJetpackModeEnabled) {
            float side = settingUpsideDownEnabled ? -1f : 1f;
            float aim = (top + pipeGap / 2f) * side;
            float position = y * side;
            float speed = velocity * side;
            float pull = Math.abs(gravityPerSec);
            float push = Math.abs(jetpackThrustPerSec * settingJumpStrength) - pull;
            float rest;
            if (speed < 0f) rest = pull > 0f ? position - speed * speed / (2f * pull) : -Float.MAX_VALUE;
            else rest = push > 0f ? position + speed * speed / (2f * push) : Float.MAX_VALUE;
            return rest > aim;
        }
        float margin = pipeGap * 0.14f;
        return settingUpsideDownEnabled
                ? y < top + halfHeight + margin
                : y > top + pipeGap - halfHeight - margin;
    }

    private float flightCost(boolean liftNow, int waitSteps, float halfWidth, float halfHeight) {
        boolean flipped = settingUpsideDownEnabled;
        float groundTop = groundTopY();
        float thrust = jetpackThrustPerSec * settingJumpStrength * (flipped ? -1 : 1);
        float y = birdY;
        float velocity = birdVelocityY;
        float sinceFlap = (float) (clock - lastFlapTime);
        float drift = Math.min(screenWidth, referenceWidth()) * 0.15f * settingDrunkModeStrength;
        float bodyHalf = Math.max(halfHeight, birdHalfHeight(1));
        float cost = 0f;

        for (int step = 0; step < PILOT_STEPS; step++) {
            float time = PILOT_STEP_SEC * step;
            float shift = pipeSpeedPerSec * (settingFixedStepEnabled ? time + PILOT_STEP_SEC : time);
            float pilotX = settingDrunkModeEnabled
                    ? baseBirdX + (float) Math.sin(drunkModePhase + 2.0f * (time + PILOT_STEP_SEC)) * drift
                    : birdX;
            Pipe next = nextPipeAhead(shift, halfWidth, pilotX);
            float top = next != null ? gapTopAt(next, shift, time) : playAreaCenterY() - pipeGap / 2f;
            float bottom = top + pipeGap;

            boolean lift;
            if (step == 0) lift = liftNow;
            else if (step <= waitSteps) lift = settingJetpackModeEnabled && liftNow;
            else lift = pilotWantsLift(y, velocity, top, halfHeight);
            if (settingJetpackModeEnabled) {
                if (lift) velocity -= thrust * PILOT_STEP_SEC;
            } else if (lift && sinceFlap > PILOT_FLAP_COOLDOWN_SEC && canFlapAt(y)) {
                velocity = flapVelocityPerSec;
                sinceFlap = 0f;
            }
            float nextVelocity = velocity + gravityPerSec * PILOT_STEP_SEC;
            float cappedVelocity = capFall(nextVelocity);
            if (pixelSteps()) y = moveBird(y, cappedVelocity * PILOT_STEP_SEC);
            else if (cappedVelocity != nextVelocity) y += cappedVelocity * PILOT_STEP_SEC;
            else y += velocity * PILOT_STEP_SEC + gravityPerSec * PILOT_STEP_SEC * (PILOT_STEP_SEC + pilotFrameDelta) / 2f;
            velocity = cappedVelocity;
            sinceFlap += PILOT_STEP_SEC;

            boolean crashed = y + bodyHalf > groundTop || (flipped && y - bodyHalf < systemBarTop);
            float ceiling = systemBarTop - bodyHalf * (1f + CEILING_OVERSHOOT);
            if (!flipped && settingCeilingEnabled && y < ceiling) {
                y = ceiling;
                if (velocity < 0f) velocity = 0f;
            }
            if (next != null && !crashed) {
                float visualWidth = next.width * settingPipeWidth;
                float left = next.x - shift - (visualWidth - next.width) / 2f;
                boolean inside = pilotX + halfWidth > left && pilotX - halfWidth < left + visualWidth;
                crashed = inside && (y - halfHeight < top || y + halfHeight > bottom);
            }
            if (crashed) return cost + PILOT_CRASH_COST - step;
            cost += Math.abs(y - (top + bottom) / 2f) / Math.max(1, pipeGap) * 0.01f;
        }
        return cost;
    }

    private float bestFlightCost(boolean liftNow, float halfWidth, float halfHeight) {
        float best = Float.MAX_VALUE;
        for (int wait : PILOT_WAITS) {
            best = Math.min(best, flightCost(liftNow, wait, halfWidth, halfHeight));
            if (best < 1f) break;
        }
        return best;
    }

    private void runAutopilot(float frameDelta) {
        pilotFrameDelta = frameDelta;
        Bitmap sprite = birdBitmaps[1];
        float margin = settingFixedStepEnabled ? PILOT_MARGIN_FIXED_STEP : PILOT_MARGIN;
        float halfWidth = hitboxHalfWidth() + sprite.getWidth() * settingBirdSize * margin;
        float halfHeight = hitboxHalfHeight() + sprite.getHeight() * settingBirdSize * margin;
        boolean canAct = settingJetpackModeEnabled || clock - lastFlapTime > PILOT_FLAP_COOLDOWN_SEC;
        boolean lift = canAct
                && bestFlightCost(true, halfWidth, halfHeight) < bestFlightCost(false, halfWidth, halfHeight);
        if (settingJetpackModeEnabled) isScreenPressed = lift;
        else if (lift) flap();
    }

    private void gameOver() {
        if (gameState != GameState.PLAYING) return;
        commitMark(MARK_END);
        gameState = GameState.GAME_OVER;
        gameOverTime = clock;
        isPaused = false;
        invulnerableTimer = 0f;

        cinematicPhase = 0;
        cameraZoom = 1.0f;
        timeDilation = 1.0f;

        startCrashFlash(1f);
        stopWeatherSounds();
        playSound(soundHit);
        feel(Feel.CRASH);
        triggerShake(5.0f, 0.4f);
        if (filmMode) filmDieIn = 0.3f;
        else postDelayed(playDieSound, 300);

        previousBest = highScore;
        isNewBest = score > highScore;
        if (isNewBest) {
            highScore = score;
            if (!filmMode) prefs.edit().putInt(PREF_HIGH_SCORE, highScore).apply();
        }
        if (!filmMode) {
            saveFilm();
            recordRound();
        }

        if (score >= settingMedalPlatinum) currentMedal = MEDAL_PLATINUM;
        else if (score >= settingMedalGold) currentMedal = MEDAL_GOLD;
        else if (score >= settingMedalSilver) currentMedal = MEDAL_SILVER;
        else if (score >= settingMedalBronze) currentMedal = MEDAL_BRONZE;
        else currentMedal = MEDAL_NONE;

        displayedScore = 0;
        lastScoreTickTime = -1;
        panelFelt = false;
        scoreFeltAt = -1;
    }

    private void recordRound() {
        String assists = roundAssistText();
        int earned = submitRound(prefs, Math.max(score, 0), assists);
        boolean earnedBest = earned != Integer.MIN_VALUE;
        if (earned == -1) {
            SettingsActivity.ActivityLog.add(getContext(), "SCORE", "The saved earned record did not match its seal and was rebuilt from this round");
        }
        String seconds = String.format(Locale.US, "%.1f", Math.max(0.0, clock - roundStartClock));
        StringBuilder round = new StringBuilder("Score ").append(score).append(", best ").append(highScore).append(", ")
                .append(seconds).append("s, ").append(roundFlaps).append(" flaps");
        if (!roundEndCause.isEmpty()) round.append(", ").append(roundEndCause);
        if (!assists.isEmpty()) round.append(", with ").append(assists);
        if (roundFrames > 0 && roundFrameSeconds > 0f) {
            round.append(", ").append(Math.round(roundFrames / roundFrameSeconds)).append(" fps");
            if (roundSlowFrames > 0) {
                round.append(" (").append(roundSlowFrames).append(roundSlowFrames == 1 ? " slow frame" : " slow frames")
                        .append(", worst ").append(Math.round(roundWorstFrame * 1000f)).append(" ms)");
            }
        }
        roundEndCause = "";
        Leaderboard.Replay replay = takeReplay();
        int weekBest = Leaderboard.coinWeekBest(prefs);
        round.append("; ").append(judgeRound(assists, replay));
        claimRound(replay, weekBest);
        SettingsActivity.ActivityLog.add(getContext(), "ROUND", round.toString());
        if (isNewBest) {
            SettingsActivity.ActivityLog.add(getContext(), "BEST", "New high score " + score + " (was " + previousBest + ")"
                    + (assists.isEmpty() ? ", no assists" : ", with " + assists));
        }
        if (earnedBest && earned >= 0) {
            SettingsActivity.ActivityLog.add(getContext(), "BEST", "Best earned by playing is now " + score + " (was " + earned + ")");
        }
    }

    private int fingerprint(int hash, float value) {
        return (hash ^ Float.floatToIntBits(value)) * 0x01000193;
    }

    private int fingerprint(int hash, int value) {
        return (hash ^ value) * 0x01000193;
    }

    private int settingsFingerprint() {
        int h = 0x811C9DC5;
        h = fingerprint(h, settingGameSpeed);
        h = fingerprint(h, settingGravity);
        h = fingerprint(h, settingJumpStrength);
        h = fingerprint(h, settingPipeGap);
        h = fingerprint(h, settingBirdHangDelay);
        h = fingerprint(h, settingPipeSpacing);
        h = fingerprint(h, settingBirdHitbox);
        h = fingerprint(h, settingMovingPipesEnabled ? 1 : 0);
        h = fingerprint(h, settingNoClipEnabled ? 1 : 0);
        h = fingerprint(h, settingUpsideDownEnabled ? 1 : 0);
        h = fingerprint(h, settingReversePipesEnabled ? 1 : 0);
        h = fingerprint(h, settingPipeVariation);
        h = fingerprint(h, settingScoreMultiplier);
        h = fingerprint(h, settingPipeSpeedVariation);
        h = fingerprint(h, settingBirdSize);
        h = fingerprint(h, settingPipeWidth);
        h = fingerprint(h, settingJetpackModeEnabled ? 1 : 0);
        h = fingerprint(h, settingDrunkModeEnabled ? 1 : 0);
        h = fingerprint(h, settingGoldenPipeEnabled ? 1 : 0);
        h = fingerprint(h, settingExtraLives);
        h = fingerprint(h, settingAutopilotEnabled ? 1 : 0);
        h = fingerprint(h, settingFixedStepEnabled ? settingStepRate : 0);
        h = fingerprint(h, settingPixelStepsEnabled ? 1 : 0);
        h = fingerprint(h, settingSpeedRampPercent);
        h = fingerprint(h, settingGapShrinkPercent);
        h = fingerprint(h, settingFixedStepEnabled ? 1 : 0);
        h = fingerprint(h, settingOriginalMotion ? 1 : 0);
        h = fingerprint(h, settingMaxFallSpeed);
        h = fingerprint(h, settingCeilingEnabled ? 1 : 0);
        h = fingerprint(h, settingPixelStepsEnabled ? 1 : 0);
        h = fingerprint(h, settingFlapLimitEnabled ? 1 : 0);
        h = fingerprint(h, settingHitboxShape);
        h = fingerprint(h, settingGapPlacement);
        h = fingerprint(h, settingScorePoint);
        h = fingerprint(h, settingBirdPosition);
        h = fingerprint(h, settingStartHeight);
        h = fingerprint(h, settingShowHitboxes ? 1 : 0);
        h = fingerprint(h, settingWeatherEffect);
        return h;
    }

    private int tuningFingerprint() {
        int h = 0x01000193;
        h = fingerprint(h, gravityPerSec);
        h = fingerprint(h, flapVelocityPerSec);
        h = fingerprint(h, pipeSpeedPerSec);
        h = fingerprint(h, pipeGap);
        h = fingerprint(h, pipeSpacing);
        h = fingerprint(h, rotationDelayThresholdPerSec);
        h = fingerprint(h, gameplayBirdX);
        return h;
    }

    private void sealSettings() {
        settingsSeal = settingsFingerprint() ^ sealKey;
    }

    private void sealTuning() {
        tuningSeal = tuningFingerprint() ^ sealKey;
    }

    private void checkSeals() {
        if ((settingsFingerprint() ^ sealKey) != settingsSeal || (tuningFingerprint() ^ sealKey) != tuningSeal) roundTampered = true;
        if ((scoreCheck ^ scoreKey) != score || (pipesCheck ^ scoreKey) != roundPipes) roundTampered = true;
    }

    private static final String KEY_TWIST = "challengeTwist";
    private static final String KEY_TWIST_DAY = "challengeTwistDay";
    private static final String KEY_COIN_BALANCE = "coinBalance";
    private static final String KEY_COIN_SEEN_DAY = "coinSeenDay";
    private static final String KEY_COIN_EARNED = "coinEarned";
    private static final String KEY_COIN_DAY = "coinDay";
    private static final String KEY_COIN_DONE = "coinDone";
    private static final float WALLET_POLL_SEC = 6f;
    private static final long PENDING_MAX_MS = 5L * 60000L;
    private static final long BUY_MAX_MS = 4L * 60000L;
    private static final float TOAST_SEC = 3.4f;
    private static final float COIN_OPEN_SEC = 0.34f;
    private static final float COIN_CLOSE_SEC = 0.2f;
    private static final float CONFIRM_SEC = 3f;
    private static final float COIN_SWITCH_SEC = 0.32f;
    private static final float COIN_KIND_SEC = 0.24f;
    private static final int COIN_GOLD = 0xFFFFC93C;
    private static final int COIN_RIM = 0xFFE8A317;
    private static final int COIN_SHADE = 0xFFC9811A;
    private static final int[] TIER_COLORS = {0xFF5DBB4A, 0xFFF5A623, 0xFFE0533D};
    private static final int HIT_CLOSE = 1;
    private static final int HIT_TAB = 2;
    private static final int HIT_KIND = 3;
    private static final int HIT_PLAY = 4;
    private static final int HIT_STOP = 5;
    private static final int HIT_ITEM = 6;
    private static final int HIT_ACTION = 7;
    private static final int HIT_SETTINGS = 8;

    private long today() {
        return Challenges.day(Leaderboard.now());
    }

    private void readTwist() {
        Map<String, ?> all = prefs.getAll();
        Object twist = all.get(KEY_TWIST);
        Object day = all.get(KEY_TWIST_DAY);
        int value = twist instanceof Integer ? (Integer) twist : -1;
        long when = day instanceof Long ? (Long) day : day instanceof Integer ? (Integer) day : -1L;
        if (value <= Challenges.ANY || value >= Challenges.TWIST_NAMES.length || when != today()) {
            challengeTwist = -1;
            challengeTwistDay = -1L;
            if (twist != null) prefs.edit().remove(KEY_TWIST).remove(KEY_TWIST_DAY).apply();
            return;
        }
        challengeTwist = value;
        challengeTwistDay = when;
    }

    private Map<String, ?> playPrefs() {
        Map<String, ?> all = prefs.getAll();
        if (filmMode || challengeTwist <= Challenges.ANY) return all;
        Map<String, Object> merged = new HashMap<>(all);
        merged.putAll(Challenges.overlay(challengeTwist));
        return merged;
    }

    private Challenges.Goal[] todayGoals(long day) {
        Challenges.Goal[] picked = Challenges.forDay(day);
        if (wallet != null && wallet.day == day && wallet.goals != null) {
            for (int i = 0; i < 3; i++) {
                Challenges.Goal goal = Challenges.find(wallet.goals[i]);
                picked[i] = goal != null && goal.tier == i ? goal : null;
            }
        }
        return picked;
    }

    private void loadCoinCache() {
        if (coinCacheLoaded) return;
        coinCacheLoaded = true;
        Map<String, ?> all = prefs.getAll();
        Object seen = all.get(KEY_COIN_SEEN_DAY);
        Object balance = all.get(KEY_COIN_BALANCE);
        Object day = all.get(KEY_COIN_DAY);
        Object done = all.get(KEY_COIN_DONE);
        coinSeenDayCache = seen instanceof Long ? (Long) seen : Long.MIN_VALUE;
        coinBalanceCache = balance instanceof Integer ? (Integer) balance : 0;
        coinDoneDayCache = day instanceof Long ? (Long) day : Long.MIN_VALUE;
        coinDoneCache = done instanceof Integer ? (Integer) done : 0;
    }

    private boolean goalDone(long day, int slot) {
        if (wallet != null && wallet.day == day) return wallet.doneFor(day, slot);
        loadCoinCache();
        return coinDoneDayCache == day && (coinDoneCache & (1 << slot)) != 0;
    }

    private int goalProgress(long day, int slot) {
        return wallet != null ? wallet.progressFor(day, slot) : 0;
    }

    private boolean coinsOn() {
        return Leaderboard.coinsReady() && !filmMode;
    }

    private boolean challengeRoundPossible() {
        if (!coinsOn() || !settingDailyChallenges) return false;
        long day = today();
        Challenges.Goal[] goals = todayGoals(day);
        Map<String, ?> current = null;
        for (int i = 0; i < 3; i++) {
            if (goals[i] == null || goalDone(day, i)) continue;
            if (current == null) current = playPrefs();
            if (Challenges.fits(goals[i], current)) return true;
        }
        return false;
    }

    private void claimRound(Leaderboard.Replay replay, int weekBest) {
        boolean wanted = roundChallenge;
        roundChallenge = false;
        if (!wanted || filmMode) return;
        checkSeals();
        Map<String, ?> current = playPrefs();
        boolean fair = SettingsActivity.fairPlayMode(current) != FAIR_NONE;
        boolean clean = !roundTampered && score == roundPipes && roundLivesUsed == 0 && roundGoldenPoints == 0 && score >= 1;
        if (fair && clean) Leaderboard.noteCoinWeekBest(prefs, score);
        if (replay == null || !clean) return;
        long day = today();
        Challenges.Goal[] goals = todayGoals(day);
        int mask = 0;
        for (int i = 0; i < 3; i++) {
            Challenges.Goal goal = goals[i];
            if (goal == null || goalDone(day, i) || !Challenges.fits(goal, current)) continue;
            int before = goalProgress(day, i);
            int after = Challenges.advance(goal, before, score, roundActiveSeconds + 0.5, roundFlaps, weekBest);
            if (after > before) mask |= 1 << i;
        }
        if (mask == 0) return;
        int twist = Challenges.twistOf(current);
        boolean classic = twist >= 0 && Challenges.modeOf(twist, current) == FAIR_CLASSIC;
        final int sent = mask;
        pendingMask |= mask;
        pendingSince = SystemClock.uptimeMillis();
        pendingServerTime = Leaderboard.now();
        walletPollSec = 0f;
        if (roundFairMode == FAIR_NONE) boardStatusText = "Challenge run sent to be checked";
        Leaderboard.claim(getContext(), score, classic, replay, this::post, (status, error) -> {
            if (Leaderboard.VERIFYING.equals(status)) {
                pendingSince = SystemClock.uptimeMillis();
                walletPollSec = WALLET_POLL_SEC * 0.5f;
            } else {
                pendingMask &= ~sent;
                if (roundFairMode == FAIR_NONE && error != null) boardStatusText = "Challenge run not sent: " + error.substring(0, 1).toLowerCase(Locale.US) + error.substring(1);
            }
        });
    }

    private void startTwist(int twist) {
        if (twist <= Challenges.ANY) {
            endTwist(false);
            return;
        }
        if (twist == challengeTwist) return;
        prefs.edit().putInt(KEY_TWIST, twist).putLong(KEY_TWIST_DAY, today()).apply();
        coinBlockedAge = 99f;
        if (gameState == GameState.HOME) resetGame();
        else loadSettings();
        SettingsActivity.ActivityLog.add(getContext(), "SETTINGS", "Challenge setup on: " + Challenges.TWIST_NAMES[twist]);
    }

    private void endTwist(boolean announce) {
        if (challengeTwist <= Challenges.ANY && !prefs.getAll().containsKey(KEY_TWIST)) return;
        int was = challengeTwist;
        prefs.edit().remove(KEY_TWIST).remove(KEY_TWIST_DAY).apply();
        challengeTwist = -1;
        coinBlockedAge = 99f;
        if (gameState == GameState.HOME) resetGame();
        else loadSettings();
        if (was > Challenges.ANY) {
            SettingsActivity.ActivityLog.add(getContext(), "SETTINGS", "Challenge setup off: " + Challenges.TWIST_NAMES[was]);
            if (announce) showToast(Challenges.TWIST_NAMES[was] + " is off", "Your own settings are back", false);
        }
    }

    private boolean twistStillWanted() {
        if (challengeTwist <= Challenges.ANY) return false;
        long day = today();
        if (challengeTwistDay != day) return false;
        Challenges.Goal[] goals = todayGoals(day);
        for (int i = 0; i < 3; i++) {
            if (goals[i] != null && goals[i].twist == challengeTwist && !goalDone(day, i)) return true;
        }
        return wallet == null;
    }

    private void loadWallet() {
        if (!coinsOn() || walletLoading) return;
        walletLoading = true;
        walletTried = true;
        Leaderboard.fetchWallet(getContext(), this::post, (fresh, error) -> {
            walletLoading = false;
            if (fresh == null) {
                walletOffline = true;
                return;
            }
            walletOffline = false;
            takeWallet(fresh);
        });
    }

    private void takeWallet(Leaderboard.Wallet fresh) {
        Leaderboard.Wallet old = wallet;
        wallet = fresh;
        Map<String, ?> all = prefs.getAll();
        int knownEarned = all.get(KEY_COIN_EARNED) instanceof Integer ? (Integer) all.get(KEY_COIN_EARNED) : -1;
        SharedPreferences.Editor editor = prefs.edit().putInt(KEY_COIN_BALANCE, fresh.balance).putInt(KEY_COIN_EARNED, fresh.earned);
        if (fresh.day >= 0) editor.putLong(KEY_COIN_DAY, fresh.day).putInt(KEY_COIN_DONE, fresh.doneMask);
        editor.apply();
        loadCoinCache();
        coinBalanceCache = fresh.balance;
        if (fresh.day >= 0) {
            coinDoneDayCache = fresh.day;
            coinDoneCache = fresh.doneMask;
        }
        if (fresh.newsTime >= pendingServerTime - 60000L && pendingMask != 0 && fresh.newsTime > 0) pendingMask = 0;
        int gained = knownEarned >= 0 ? fresh.earned - knownEarned : 0;
        if (gained > 0) {
            boolean all3 = fresh.day == today() && fresh.doneMask == 7 && (old == null || old.doneMask != 7 || old.day != fresh.day);
            showToast("+" + gained + " FLIGHT COINS", all3 ? "All three done! " + fresh.streakFor(today()) + " day streak" : "Challenge complete!", true);
            playSound(soundPoint);
            feel(Feel.BEST);
        } else if (!fresh.newsOk && fresh.newsTime >= pendingServerTime - 60000L && fresh.newsTime > 0 && (old == null || old.newsTime != fresh.newsTime)) {
            showToast("RUN NOT COUNTED", fresh.newsWhy.isEmpty() ? "The checker couldn't confirm it" : "The checker said " + fresh.newsWhy, false);
        }
        if (buyingItem != null) {
            if (fresh.owned.contains(buyingItem)) {
                Shop.Item item = Shop.item(buyingItem);
                buyingItem = null;
                if (item != null) {
                    showToast("UNLOCKED " + item.name.toUpperCase(Locale.US), "It's on your leaderboard row now", true);
                    playSound(soundPoint);
                    feel(Feel.BEST);
                    String[] style = wallet.style.clone();
                    style[item.kind] = item.id;
                    equipStyle(style);
                }
            } else if (fresh.boughtItem.equals(buyingItem) && !fresh.boughtOk && fresh.boughtTime >= buyingSince - 5000L) {
                buyingItem = null;
                showToast("NOT BOUGHT", "You need more Flight Coins", false);
            } else if (!fresh.buying.equals(buyingItem) && fresh.boughtTime >= buyingSince - 5000L && fresh.boughtItem.equals(buyingItem)) {
                buyingItem = null;
            }
        }
        if (challengeTwist > Challenges.ANY && !twistStillWanted() && (gameState == GameState.HOME || gameState == GameState.PANEL_SLIDING)) {
            endTwist(true);
        }
    }

    private String balanceLabel() {
        int value = coinShown < 0f ? shownBalance() : Math.round(coinShown);
        if (value != coinLabelValue) {
            coinLabelValue = value;
            coinLabel = String.format(Locale.US, "%,d", value);
        }
        return coinLabel;
    }

    private int shownBalance() {
        if (wallet != null) return wallet.balance;
        loadCoinCache();
        return coinBalanceCache;
    }

    private void showToast(String title, String text, boolean coin) {
        if (toastAge < TOAST_SEC && toastTitle != null) {
            if (toastQueue.size() < 4) toastQueue.add(new String[] {title, text, coin ? "1" : ""});
            return;
        }
        toastTitle = title;
        toastText = text;
        toastCoin = coin;
        toastAge = 0f;
    }

    private void equipStyle(String[] style) {
        if (wallet == null) return;
        wallet.style = style.clone();
        boardStaleMask = 0xF;
        Leaderboard.equip(getContext(), wallet.style, this::post, (status, error) -> {
            if (error != null) setCoinNote("Couldn't save your style: " + error);
        });
    }

    private void setCoinNote(String note) {
        coinNote = note == null ? "" : note;
        coinNoteAge = 0f;
    }

    private void animateCoins(float deltaTime) {
        float step = Math.min(0.1f, Math.max(0f, deltaTime));
        coinTime = (coinTime + step) % 3600f;
        if (toastAge < TOAST_SEC) {
            if (!((gameState == GameState.PLAYING && !isPaused) || gameState == GameState.WAITING)) toastAge += step;
        } else if (!toastQueue.isEmpty()) {
            String[] next = toastQueue.poll();
            toastTitle = next[0];
            toastText = next[1];
            toastCoin = !next[2].isEmpty();
            toastAge = 0f;
        }
        coinBlockedAge += step;
        if (coinOpen) {
            coinOpenAge = Math.min(10f, coinOpenAge + step);
            coinSwitchAge = Math.min(10f, coinSwitchAge + step);
            coinKindAge = Math.min(10f, coinKindAge + step);
            coinPill += (coinTab - coinPill) * Math.min(1f, step * 16f);
            if (Math.abs(coinTab - coinPill) < 0.002f) coinPill = coinTab;
            coinKindPill += (coinKind - coinKindPill) * Math.min(1f, step * 16f);
            if (Math.abs(coinKind - coinKindPill) < 0.002f) coinKindPill = coinKind;
            for (int i = 0; i < 3; i++) {
                coinBars[i] += (coinBarTargets[i] - coinBars[i]) * Math.min(1f, step * 5f);
                if (Math.abs(coinBarTargets[i] - coinBars[i]) < 0.001f) coinBars[i] = coinBarTargets[i];
                if (coinDonePop[i] < 1f) coinDonePop[i] += step;
            }
        }
        int balanceNow = shownBalance();
        if (coinShown < 0f) {
            coinShown = balanceNow;
        } else if (coinShown != balanceNow) {
            if (balanceNow > coinShown + 0.5f && coinBump >= 0.5f) coinBump = 0f;
            coinShown += (balanceNow - coinShown) * Math.min(1f, step * 4f);
            if (Math.abs(balanceNow - coinShown) < 0.5f) coinShown = balanceNow;
        }
        if (coinBump < 1f) coinBump += step;
        twistCheckSec += step;
        if (twistCheckSec >= 0.5f) {
            twistCheckSec = 0f;
            if (challengeTwist > Challenges.ANY && wallet != null && (gameState == GameState.HOME || gameState == GameState.PANEL_SLIDING)
                    && !coinOpen && !twistStillWanted()) {
                endTwist(true);
            }
        }
        if (coinConfirmAge < CONFIRM_SEC) coinConfirmAge += step;
        else coinConfirm = null;
        if (coinNoteAge < 6f) coinNoteAge += step;
        float press = coinBtnTarget > coinBtnProgress ? 14f : 10f;
        coinBtnProgress += (coinBtnTarget - coinBtnProgress) * Math.min(1f, step * press);
        long now = SystemClock.uptimeMillis();
        if (pendingMask != 0 && now - pendingSince > PENDING_MAX_MS) pendingMask = 0;
        if (buyingItem != null && now - buyingSince > BUY_MAX_MS) {
            buyingItem = null;
            setCoinNote("The shop is slow right now. Your coins are safe, try again later");
        }
        boolean watching = pendingMask != 0 || buyingItem != null;
        if (watching && coinsOn()) {
            walletPollSec += step;
            if (walletPollSec >= WALLET_POLL_SEC && !walletLoading) {
                walletPollSec = 0f;
                loadWallet();
            }
        }
        if (challengeTwist > Challenges.ANY && (gameState == GameState.HOME || gameState == GameState.PANEL_SLIDING)
                && challengeTwistDay != today()) {
            endTwist(true);
        }
        if (coinOpen) {
            if (!coinDragging && coinFling != 0f) {
                coinScroll += coinFling * step;
                coinFling *= Math.max(0f, 1f - step * 4f);
                if (Math.abs(coinFling) < 20f || coinScroll <= 0f || coinScroll >= coinScrollMax) coinFling = 0f;
            }
            coinScroll = Math.max(0f, Math.min(coinScrollMax, coinScroll));
            if (coinClosing) {
                coinAnim -= step / COIN_CLOSE_SEC;
                if (coinAnim <= 0f) {
                    coinOpen = false;
                    coinClosing = false;
                    coinAnim = 0f;
                    coinPressAction = 0;
                    if (boardHost != null && !boardOpen) boardHost.boardVisible(false);
                }
            } else if (coinAnim < 1f) {
                coinAnim = Math.min(1f, coinAnim + step / COIN_OPEN_SEC);
            }
        }
    }

    private void openCoins() {
        if (!coinsOn()) return;
        coinOpen = true;
        coinClosing = false;
        coinAnim = 0f;
        coinScroll = 0f;
        coinFling = 0f;
        coinPreview = null;
        coinConfirm = null;
        coinNote = "";
        coinNoteAge = 99f;
        coinBlockedAge = 99f;
        long day = today();
        loadCoinCache();
        if (coinSeenDayCache != day) prefs.edit().putLong(KEY_COIN_SEEN_DAY, day).apply();
        coinSeenDayCache = day;
        coinOpenAge = 0f;
        coinSwitchAge = 99f;
        coinKindAge = 99f;
        coinPill = coinTab;
        coinKindPill = coinKind;
        for (int i = 0; i < 3; i++) {
            coinBars[i] = 0f;
            coinBarTargets[i] = 0f;
            coinDonePop[i] = 99f;
        }
        coinDoneSeen = -1;
        coinPressAction = 0;
        coinPressArg = -1;
        coinSample = new Leaderboard.Entry();
        String name = Leaderboard.name(prefs);
        coinSample.name = name.isEmpty() ? "You" : name;
        coinSample.uid = Leaderboard.uid(prefs);
        coinSample.score = Math.max(1, Math.max(highScore, Leaderboard.best(prefs)));
        loadWallet();
    }

    private void closeCoins() {
        if (!coinOpen || coinClosing) return;
        coinClosing = true;
        coinPressAction = 0;
        coinPreview = null;
    }

    boolean closeCoinsIfOpen() {
        if (!coinOpen) return false;
        closeCoins();
        return true;
    }

    private boolean coinDot() {
        if (!coinsOn()) return false;
        loadCoinCache();
        return coinSeenDayCache != today();
    }

    private void ensureBoardPaints() {
        if (boardPaint == null) boardPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        if (boardTextPaint == null) {
            boardTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            Typeface face = null;
            try {
                face = ResourcesCompat.getFont(getContext(), R.font.flappy_font);
            } catch (RuntimeException ignored) {
            }
            boardTextPaint.setTypeface(face != null ? face : Typeface.DEFAULT_BOLD);
        }
        if (lookPaint == null) lookPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    }

    private void coinHit(float left, float top, float right, float bottom, int action, int arg) {
        if (coinGhost) return;
        float[] hit;
        if (coinHitCount < coinHits.size()) {
            hit = coinHits.get(coinHitCount);
        } else {
            hit = new float[6];
            coinHits.add(hit);
        }
        hit[0] = left;
        hit[1] = top;
        hit[2] = right;
        hit[3] = bottom;
        hit[4] = action;
        hit[5] = arg;
        coinHitCount++;
    }

    private float[] coinHitAt(int x, int y) {
        for (int i = coinHitCount - 1; i >= 0; i--) {
            float[] hit = coinHits.get(i);
            if (x >= hit[0] && x <= hit[2] && y >= hit[1] && y <= hit[3]) {
                if ((hit[4] == HIT_ITEM || hit[4] == HIT_ACTION) && !coinListRect.contains(x, y)) continue;
                return hit;
            }
        }
        return null;
    }

    private boolean pressedHit(int action, int arg) {
        return coinPressAction == action && coinPressArg == arg;
    }

    private void drawCoin(Canvas canvas, float x, float y, float radius) {
        boardPaint.setColor(faded(BOARD_DARK));
        canvas.drawCircle(x, y + radius * 0.16f, radius, boardPaint);
        boardPaint.setColor(faded(COIN_SHADE));
        canvas.drawCircle(x, y, radius, boardPaint);
        boardPaint.setColor(faded(COIN_GOLD));
        canvas.drawCircle(x, y - radius * 0.06f, radius * 0.86f, boardPaint);
        boardPaint.setColor(faded(COIN_RIM));
        canvas.drawCircle(x, y - radius * 0.06f, radius * 0.6f, boardPaint);
        boardPaint.setColor(faded(COIN_GOLD));
        float arm = radius * 0.42f;
        lookPath.reset();
        lookPath.moveTo(x, y - arm - radius * 0.06f);
        lookPath.quadTo(x, y - radius * 0.06f, x + arm, y - radius * 0.06f);
        lookPath.quadTo(x, y - radius * 0.06f, x, y + arm - radius * 0.06f);
        lookPath.quadTo(x, y - radius * 0.06f, x - arm, y - radius * 0.06f);
        lookPath.quadTo(x, y - radius * 0.06f, x, y - arm - radius * 0.06f);
        lookPath.close();
        canvas.drawPath(lookPath, boardPaint);
        boardPaint.setColor(faded(0x99FFFFFF));
        boardPaint.setStyle(Paint.Style.STROKE);
        boardPaint.setStrokeWidth(radius * 0.14f);
        boardPaint.setStrokeCap(Paint.Cap.ROUND);
        boardShape.set(x - radius * 0.7f, y - radius * 0.76f, x + radius * 0.7f, y + radius * 0.64f);
        canvas.drawArc(boardShape, 200f, 60f, false, boardPaint);
        boardPaint.setStyle(Paint.Style.FILL);
        boardPaint.setStrokeCap(Paint.Cap.BUTT);
    }

    private void coinButton(Canvas canvas, RectF rect, String label, float unit, boolean pressed, int textColor, int face) {
        float drop = pressed ? unit * 1.2f : 0f;
        float radius = Math.min(unit * 3f, rect.height() / 2f);
        boardPaint.setColor(faded(BOARD_DARK));
        boardShape.set(rect.left, rect.top + unit * 1.2f, rect.right, rect.bottom + unit * 1.2f);
        canvas.drawRoundRect(boardShape, radius, radius, boardPaint);
        boardShape.set(rect.left, rect.top + drop, rect.right, rect.bottom + drop);
        canvas.drawRoundRect(boardShape, radius, radius, boardPaint);
        boardPaint.setColor(faded(face));
        boardShape.inset(unit * 1.3f, unit * 1.3f);
        canvas.drawRoundRect(boardShape, Math.max(unit, radius - unit), Math.max(unit, radius - unit), boardPaint);
        float size = Math.min(unit * 8f, rect.height() * 0.48f);
        boardText(canvas, label, rect.centerX(), rect.centerY() + drop + size * 0.36f, size, textColor, Paint.Align.CENTER, rect.width() - unit * 5f);
    }

    private void layoutCoins() {
        float unit = baseBoardUnit();
        float usableWidth = screenWidth - safeInsetLeft - safeInsetRight;
        float width = BOARD_WIDTH_UNITS * unit;
        float height = BOARD_HEIGHT_UNITS * unit;
        float left = safeInsetLeft + (usableWidth - width) / 2f;
        float top = systemBarTop + (getPlayableHeight() - height) / 2f;
        coinPanel.set(left, top, left + width, top + height);
    }

    private void drawCoins(Canvas canvas) {
        ensureBoardPaints();
        float t = Math.max(0f, Math.min(1f, coinAnim));
        float eased;
        float alpha;
        if (coinClosing) {
            eased = t * t;
            alpha = t;
        } else {
            float back = 1.70158f;
            float x = t - 1f;
            eased = 1f + (back + 1f) * x * x * x + back * x * x;
            alpha = Math.min(1f, t * 1.8f);
        }
        float keep = boardAlpha;
        boardAlpha = alpha;
        coinAlpha = alpha;
        layoutCoins();
        boardPaint.setColor(faded(BOARD_DIM));
        canvas.drawRect(0, 0, screenWidth, screenHeight, boardPaint);
        float unit = baseBoardUnit();
        float panelScale = 0.82f + 0.18f * eased;
        canvas.save();
        canvas.scale(panelScale, panelScale, coinPanel.centerX(), coinPanel.centerY());
        canvas.translate(0f, (1f - eased) * 24f * unit);
        coinHitCount = 0;
        drawCoinsContent(canvas, coinPanel.left, coinPanel.top, unit);
        canvas.restore();
        if (panelScale != 1f || eased != 1f) coinHitCount = 0;
        boardAlpha = keep;
    }

    private void drawCoinsContent(Canvas canvas, float left, float top, float unit) {
        float width = BOARD_WIDTH_UNITS * unit;
        float height = BOARD_HEIGHT_UNITS * unit;
        boardShape.set(left, top, left + width, top + height);
        boardPaint.setColor(faded(BOARD_DARK));
        canvas.drawRoundRect(boardShape, unit * 5f, unit * 5f, boardPaint);
        boardShape.inset(unit * 2f, unit * 2f);
        boardPaint.setColor(faded(BOARD_PANEL_EDGE));
        canvas.drawRoundRect(boardShape, unit * 4f, unit * 4f, boardPaint);
        boardShape.inset(unit * 1.5f, unit * 1.5f);
        boardPaint.setColor(faded(BOARD_PANEL));
        canvas.drawRoundRect(boardShape, unit * 3f, unit * 3f, boardPaint);

        boardText(canvas, "FLIGHT COINS", left + width / 2f, top + 24f * unit, 15f * unit, BOARD_ORANGE, Paint.Align.CENTER, width - 24f * unit);

        float tabTop = top + 32f * unit;
        float tabWidth = 96f * unit;
        float tabGap = 6f * unit;
        float tabsLeft = left + (width - tabWidth * 2f - tabGap) / 2f;
        String[] tabs = {"DAILY", "SHOP"};
        for (int i = 0; i < 2; i++) {
            float tabLeft = tabsLeft + i * (tabWidth + tabGap);
            boardShape.set(tabLeft, tabTop, tabLeft + tabWidth, tabTop + 18f * unit);
            boardPaint.setColor(faded(BOARD_TAB_IDLE));
            canvas.drawRoundRect(boardShape, unit * 3f, unit * 3f, boardPaint);
        }
        float pill = Math.max(0f, Math.min(1f, coinPill));
        float moving = (float) Math.sin(Math.PI * Math.min(1f, Math.abs(coinTab - coinPill) * 2f)) * 0.5f;
        float pillLeft = tabsLeft + pill * (tabWidth + tabGap) - moving * 5f * unit;
        float pillRight = pillLeft + tabWidth + moving * 10f * unit;
        float squash = moving * 0.8f * unit;
        boardShape.set(pillLeft, tabTop + squash, pillRight, tabTop + 18f * unit - squash);
        boardPaint.setColor(faded(BOARD_ORANGE));
        canvas.drawRoundRect(boardShape, unit * 3f, unit * 3f, boardPaint);
        boardPaint.setColor(faded(BOARD_DARK));
        boardShape.set(pillLeft + unit * 2f, tabTop + 16.5f * unit - squash, pillRight - unit * 2f, tabTop + 18f * unit - squash);
        canvas.drawRect(boardShape, boardPaint);
        for (int i = 0; i < 2; i++) {
            float tabLeft = tabsLeft + i * (tabWidth + tabGap);
            float covered = Math.max(0f, Math.min(pillRight, tabLeft + tabWidth) - Math.max(pillLeft, tabLeft)) / tabWidth;
            boardText(canvas, tabs[i], tabLeft + tabWidth / 2f, tabTop + 12f * unit, 8.5f * unit, mixColor(BOARD_DARK, Color.WHITE, Math.min(1f, covered)),
                    Paint.Align.CENTER, tabWidth - 6f * unit);
            coinHit(tabLeft, tabTop, tabLeft + tabWidth, tabTop + 18f * unit, HIT_TAB, i);
        }

        float stripTop = top + 55f * unit;
        drawCoin(canvas, left + 18f * unit, stripTop + 7f * unit, 6.5f * unit * coinBumpScale());
        String balance = wallet == null && walletLoading ? "..." : balanceLabel();
        boardText(canvas, balance, left + 28f * unit, stripTop + 11f * unit, 12f * unit, BOARD_DARK, Paint.Align.LEFT, 90f * unit);
        long day = today();
        int streak = wallet != null ? wallet.streakFor(day) : 0;
        if (streak > 0) {
            String label = streak + " DAY STREAK";
            boardTextPaint.setTextSize(7.5f * unit);
            float labelWidth = Math.min(80f * unit, boardTextPaint.measureText(label));
            drawBadge(canvas, 4, left + width - 14f * unit - labelWidth - 7f * unit, stripTop + 6.6f * unit, unit * 0.9f);
            boardText(canvas, label, left + width - 14f * unit, stripTop + 10.5f * unit, 7.5f * unit, BOARD_ORANGE, Paint.Align.RIGHT, 80f * unit);
        }

        float contentTop = top + 74f * unit;
        float contentBottom = top + 300f * unit;
        if (wallet == null && walletOffline) {
            float middle = (contentTop + contentBottom) / 2f;
            boardText(canvas, "NO INTERNET", left + width / 2f, middle - 4f * unit, 13f * unit, BOARD_ORANGE, Paint.Align.CENTER, width - 30f * unit);
            boardText(canvas, "Connect to earn and spend Flight Coins", left + width / 2f, middle + 10f * unit, 7.5f * unit, BOARD_DARK,
                    Paint.Align.CENTER, width - 30f * unit);
            boardText(canvas, "Trying again every few seconds", left + width / 2f, middle + 21f * unit, 6.5f * unit, BOARD_DARK,
                    Paint.Align.CENTER, width - 30f * unit);
            coinScrollMax = 0f;
            if (!walletLoading && coinTime % 5f < 0.05f) loadWallet();
        } else if (coinSwitchAge < COIN_SWITCH_SEC && coinSwitchFrom != coinTab) {
            float t = coinSwitchAge / COIN_SWITCH_SEC;
            float x = 1f - t;
            float eased = 1f - x * x * x;
            float shift = (coinTab > coinSwitchFrom ? 1f : -1f) * 34f * unit;
            float baseAlpha = boardAlpha;
            canvas.save();
            canvas.clipRect(left + 3.5f * unit, contentTop - 2f * unit, left + width - 3.5f * unit, contentBottom + 2f * unit);
            boardAlpha = baseAlpha * Math.max(0f, 1f - eased * 1.35f);
            if (boardAlpha > 0.003f) {
                float scroll = coinScroll;
                coinScroll = coinSwitchScroll;
                coinGhost = true;
                canvas.save();
                canvas.translate(-shift * eased, 0f);
                drawTab(canvas, coinSwitchFrom, left, width, contentTop, contentBottom, unit);
                canvas.restore();
                coinGhost = false;
                coinScroll = scroll;
            }
            boardAlpha = baseAlpha * Math.max(0f, Math.min(1f, (eased - 0.12f) / 0.88f));
            canvas.save();
            canvas.translate(shift * (1f - eased), 0f);
            drawTab(canvas, coinTab, left, width, contentTop, contentBottom, unit);
            canvas.restore();
            boardAlpha = baseAlpha;
            canvas.restore();
        } else {
            drawTab(canvas, coinTab, left, width, contentTop, contentBottom, unit);
        }

        String note = coinNoteAge < 6f ? coinNote : "";
        if (note.isEmpty()) note = coinFooter(day);
        boardText(canvas, note, left + width / 2f, top + 312f * unit, 7.2f * unit, coinNoteAge < 6f ? BOARD_ORANGE : BOARD_DARK,
                Paint.Align.CENTER, width - 20f * unit);
        float buttonWidth = 84f * unit;
        float buttonTop = top + 320f * unit;
        RectF ok = coinRect;
        ok.set(left + (width - buttonWidth) / 2f, buttonTop, left + (width + buttonWidth) / 2f, buttonTop + 26f * unit);
        coinButton(canvas, ok, "OK", unit, pressedHit(HIT_CLOSE, 0), BOARD_ORANGE, Color.WHITE);
        coinHit(ok.left, ok.top, ok.right, ok.bottom, HIT_CLOSE, 0);
    }

    private void drawTab(Canvas canvas, int tab, float left, float width, float contentTop, float contentBottom, float unit) {
        if (tab == 0) drawDaily(canvas, left, width, contentTop, contentBottom, unit);
        else drawShop(canvas, left, width, contentTop, contentBottom, unit);
    }

    private float coinBumpScale() {
        if (coinBump >= 0.45f) return 1f;
        float t = coinBump / 0.45f;
        return 1f + 0.22f * (float) Math.sin(t * Math.PI) * (1f - t * 0.4f);
    }

    private float staggered(int index) {
        float t = Math.max(0f, Math.min(1f, (coinOpenAge - 0.1f - index * 0.07f) / 0.34f));
        float back = 1.70158f;
        float x = t - 1f;
        return 1f + (back + 1f) * x * x * x + back * x * x;
    }

    private String coinFooter(long day) {
        if (!settingDailyChallenges) return "Daily challenges are off in Settings";
        long left = Math.max(0L, Challenges.dayEnds(day) - Leaderboard.now());
        long hours = left / 3600000L;
        long minutes = (left / 60000L) % 60L;
        if (coinTab == 1) return "Everyone sees your style on the leaderboard";
        return "New challenges in " + (hours > 0 ? hours + "h " : "") + minutes + "m";
    }

    private void drawDaily(Canvas canvas, float left, float width, float contentTop, float contentBottom, float unit) {
        coinScrollMax = 0f;
        long day = today();
        Challenges.Goal[] goals = todayGoals(day);
        if (coinBlockedAge > 0.5f || coinBlockedDay != day) {
            Map<String, ?> current = playPrefs();
            for (int i = 0; i < 3; i++) coinBlocked[i] = goals[i] == null ? null : Challenges.blocker(goals[i], withTwist(current, goals[i].twist));
            coinBlockedAge = 0f;
            coinBlockedDay = day;
        }
        float cardHeight = 58f * unit;
        float gap = 6f * unit;
        float cardLeft = left + 10f * unit;
        float cardRight = left + width - 10f * unit;
        int doneCount = 0;
        int doneMask = 0;
        for (int i = 0; i < 3; i++) if (goalDone(day, i)) doneMask |= 1 << i;
        if (!coinGhost) {
            if (coinDoneSeen >= 0) {
                for (int i = 0; i < 3; i++) if ((doneMask & (1 << i)) != 0 && (coinDoneSeen & (1 << i)) == 0) coinDonePop[i] = 0f;
            }
            coinDoneSeen = doneMask;
        }
        float baseAlpha = boardAlpha;
        for (int i = 0; i < 3; i++) {
            float enter = staggered(i);
            boardAlpha = baseAlpha * Math.max(0f, Math.min(1f, enter * 1.4f));
            float cardTop = contentTop + i * (cardHeight + gap) + (1f - enter) * 14f * unit;
            Challenges.Goal goal = goals[i];
            boolean done = (doneMask & (1 << i)) != 0;
            if (done) doneCount++;
            boardShape.set(cardLeft, cardTop + unit, cardRight, cardTop + cardHeight + unit);
            boardPaint.setColor(faded(BOARD_DIVIDER));
            canvas.drawRoundRect(boardShape, unit * 4f, unit * 4f, boardPaint);
            boardShape.set(cardLeft, cardTop, cardRight, cardTop + cardHeight);
            boardPaint.setColor(faded(done ? mixColor(BOARD_PANEL_EDGE, 0xFFD8F2C4, 0.6f) : BOARD_PANEL_EDGE));
            canvas.drawRoundRect(boardShape, unit * 4f, unit * 4f, boardPaint);
            int tierColor = TIER_COLORS[i];
            boardShape.set(cardLeft + 6f * unit, cardTop + 6f * unit, cardLeft + 46f * unit, cardTop + 17f * unit);
            boardPaint.setColor(faded(tierColor));
            canvas.drawRoundRect(boardShape, unit * 2.5f, unit * 2.5f, boardPaint);
            boardText(canvas, Challenges.TIER_NAMES[i], boardShape.centerX(), cardTop + 14.2f * unit, 6.5f * unit, Color.WHITE,
                    Paint.Align.CENTER, 36f * unit);
            String reward = "+" + Challenges.REWARD[i];
            boardTextPaint.setTextSize(9f * unit);
            float rewardWidth = boardTextPaint.measureText(reward);
            boardText(canvas, reward, cardRight - 8f * unit, cardTop + 15f * unit, 9f * unit, BOARD_DARK, Paint.Align.RIGHT, 0f);
            drawCoin(canvas, cardRight - 13f * unit - rewardWidth, cardTop + 11.5f * unit, 4.4f * unit);
            if (goal == null) {
                boardText(canvas, "Update the game to see this challenge", cardLeft + 8f * unit, cardTop + 32f * unit, 7.5f * unit, BOARD_DARK,
                        Paint.Align.LEFT, cardRight - cardLeft - 16f * unit);
                continue;
            }
            float buttonWidth = 50f * unit;
            float textRoom = cardRight - cardLeft - 16f * unit - (done ? 22f * unit : buttonWidth + 4f * unit);
            boardText(canvas, goal.title(), cardLeft + 8f * unit, cardTop + 31f * unit, 8f * unit, BOARD_DARK, Paint.Align.LEFT,
                    cardRight - cardLeft - 16f * unit - (done ? 22f * unit : 0f));
            int progress = Math.min(goal.goal(), goalProgress(day, i));
            if (done) progress = goal.goal();
            float barLeft = cardLeft + 8f * unit;
            float barRight = cardLeft + 8f * unit + textRoom;
            float barTop = cardTop + 39f * unit;
            boardShape.set(barLeft, barTop, barRight, barTop + 7f * unit);
            boardPaint.setColor(faded(BOARD_DIVIDER));
            canvas.drawRoundRect(boardShape, unit * 3.5f, unit * 3.5f, boardPaint);
            float share = goal.goal() > 0 ? Math.min(1f, progress / (float) goal.goal()) : 0f;
            if (!coinGhost) coinBarTargets[i] = share;
            float shown = coinGhost ? share : coinBars[i];
            if (shown > 0.002f) {
                boardShape.set(barLeft, barTop, barLeft + Math.max(7f * unit, (barRight - barLeft) * shown), barTop + 7f * unit);
                boardPaint.setColor(faded(tierColor));
                canvas.drawRoundRect(boardShape, unit * 3.5f, unit * 3.5f, boardPaint);
            }
            String count = goal.goal() == 1 ? (done ? "DONE" : "NOT YET") : progress + " / " + goal.goal();
            String blocked = done ? null : coinBlocked[i];
            if (blocked != null) count = blocked.isEmpty() ? "Needs fair settings" : "Turn off " + blocked + " first";
            boardText(canvas, count, barLeft, barTop + 15f * unit, 6.3f * unit, blocked != null ? BOARD_DROP : BOARD_DARK, Paint.Align.LEFT, textRoom);
            if (done) {
                float pop = coinDonePop[i] < 0.5f ? 1f + 0.45f * (float) Math.sin(coinDonePop[i] / 0.5f * Math.PI) : 1f;
                canvas.save();
                canvas.scale(pop, pop, cardRight - 16f * unit, cardTop + 40f * unit);
                drawCheck(canvas, cardRight - 16f * unit, cardTop + 40f * unit, unit);
                canvas.restore();
                continue;
            }
            RectF button = coinRect;
            button.set(cardRight - 8f * unit - buttonWidth, cardTop + 35f * unit, cardRight - 8f * unit, cardTop + 53f * unit);
            boolean checking = (pendingMask & (1 << i)) != 0;
            boolean active = goal.twist > Challenges.ANY && goal.twist == challengeTwist;
            if (checking) {
                int dots = 1 + (int) ((coinTime * 3f) % 3f);
                String label = "CHECKING" + "...".substring(0, dots);
                boardText(canvas, label, button.centerX(), button.centerY() + 2.5f * unit, 6.6f * unit, BOARD_ORANGE, Paint.Align.CENTER, buttonWidth);
            } else if (active) {
                coinButton(canvas, button, "STOP", unit, pressedHit(HIT_STOP, i), Color.WHITE, BOARD_ORANGE);
                coinHit(button.left, button.top, button.right, button.bottom, HIT_STOP, i);
            } else {
                coinButton(canvas, button, "PLAY", unit, pressedHit(HIT_PLAY, i), Color.WHITE, tierColor);
                coinHit(button.left, button.top, button.right, button.bottom, HIT_PLAY, i);
            }
        }
        float enterBonus = staggered(3);
        boardAlpha = baseAlpha * Math.max(0f, Math.min(1f, enterBonus * 1.4f));
        float bonusTop = contentTop + 3f * (cardHeight + gap) + 2f * unit + (1f - enterBonus) * 5f * unit;
        int streak = wallet != null ? wallet.streakFor(day) : 0;
        boolean allDone = doneCount == 3;
        int nextStreak = allDone ? streak : (wallet != null && wallet.lastFull == day - 1 ? wallet.streak + 1 : 1);
        int bonus = Challenges.bonus(Math.max(1, nextStreak));
        String line = allDone ? "ALL DONE! COME BACK TOMORROW" : "FINISH ALL 3 FOR +" + bonus;
        boardShape.set(left + 10f * unit, bonusTop, left + width - 10f * unit, bonusTop + 18f * unit);
        boardPaint.setColor(faded(allDone ? 0xFF5DBB4A : BOARD_DARK));
        canvas.drawRoundRect(boardShape, unit * 4f, unit * 4f, boardPaint);
        boardText(canvas, line, left + width / 2f + (allDone ? 0f : 5f * unit), bonusTop + 12.3f * unit, 8f * unit, Color.WHITE,
                Paint.Align.CENTER, width - 46f * unit);
        if (!allDone) {
            boardTextPaint.setTextSize(8f * unit);
            float lineWidth = Math.min(width - 46f * unit, boardTextPaint.measureText(line));
            drawCoin(canvas, left + width / 2f + 5f * unit - lineWidth / 2f - 8f * unit, bonusTop + 9f * unit, 4.6f * unit);
        }
        if (!allDone && nextStreak > 1) {
            boardText(canvas, "Includes a streak bonus for day " + nextStreak, left + width / 2f, bonusTop + 28f * unit, 6.5f * unit, BOARD_ORANGE,
                    Paint.Align.CENTER, width - 30f * unit);
        } else if (!allDone) {
            boardText(canvas, "Finish every day in a row for a bigger bonus", left + width / 2f, bonusTop + 28f * unit, 6.5f * unit, BOARD_DARK,
                    Paint.Align.CENTER, width - 30f * unit);
        }
        boardAlpha = baseAlpha;
    }

    private Map<String, ?> withTwist(Map<String, ?> current, int twist) {
        if (twist <= Challenges.ANY || twist == challengeTwist) return current;
        Map<String, Object> merged = new HashMap<>(prefs.getAll());
        merged.putAll(Challenges.overlay(twist));
        return merged;
    }

    private void drawCheck(Canvas canvas, float x, float y, float unit) {
        boardPaint.setColor(faded(BOARD_DARK));
        canvas.drawCircle(x, y + unit * 0.9f, 8f * unit, boardPaint);
        boardPaint.setColor(faded(0xFF5DBB4A));
        canvas.drawCircle(x, y, 8f * unit, boardPaint);
        boardPaint.setColor(faded(Color.WHITE));
        boardPaint.setStyle(Paint.Style.STROKE);
        boardPaint.setStrokeWidth(2.2f * unit);
        boardPaint.setStrokeCap(Paint.Cap.ROUND);
        canvas.drawLine(x - 3.6f * unit, y + 0.2f * unit, x - 0.9f * unit, y + 2.9f * unit, boardPaint);
        canvas.drawLine(x - 0.9f * unit, y + 2.9f * unit, x + 3.9f * unit, y - 2.8f * unit, boardPaint);
        boardPaint.setStyle(Paint.Style.FILL);
        boardPaint.setStrokeCap(Paint.Cap.BUTT);
    }

    private String[] previewStyle() {
        String[] style = wallet != null ? wallet.style.clone() : new String[5];
        Shop.Item item = Shop.item(coinPreview);
        if (item != null) style[item.kind] = item.id;
        return style;
    }

    private void drawShop(Canvas canvas, float left, float width, float contentTop, float contentBottom, float unit) {
        float boxLeft = left + 8f * unit;
        float boxRight = left + width - 8f * unit;
        boardShape.set(boxLeft, contentTop, boxRight, contentTop + 25f * unit);
        boardPaint.setColor(faded(BOARD_PANEL_EDGE));
        canvas.drawRoundRect(boardShape, unit * 4f, unit * 4f, boardPaint);
        if (coinSample == null) {
            coinSample = new Leaderboard.Entry();
            coinSample.name = "You";
            coinSample.score = 1;
        }
        Leaderboard.Entry sample = coinSample;
        String[] look = previewStyle();
        if (!Arrays.equals(look, sample.look)) {
            sample.look = look;
            sample.lookCache = null;
        }
        plainBoardRow();
        float shine = boardShine;
        boardShine = coinTime;
        boardRow(canvas, sample, "7", boxLeft - 2f * unit, boxRight - boxLeft + 6f * unit, contentTop + 3f * unit, unit, false);
        boardShine = shine;

        float chipTop = contentTop + 29f * unit;
        float chipGap = 3f * unit;
        float chipWidth = (boxRight - boxLeft - chipGap * 4f) / 5f;
        for (int k = 0; k < 5; k++) {
            float chipLeft = boxLeft + k * (chipWidth + chipGap);
            boardShape.set(chipLeft, chipTop, chipLeft + chipWidth, chipTop + 14f * unit);
            boardPaint.setColor(faded(BOARD_TAB_IDLE));
            canvas.drawRoundRect(boardShape, unit * 3f, unit * 3f, boardPaint);
        }
        float kindPill = Math.max(0f, Math.min(4f, coinGhost ? coinKind : coinKindPill));
        float pillLeft = boxLeft + kindPill * (chipWidth + chipGap);
        boardShape.set(pillLeft, chipTop, pillLeft + chipWidth, chipTop + 14f * unit);
        boardPaint.setColor(faded(BOARD_DARK));
        canvas.drawRoundRect(boardShape, unit * 3f, unit * 3f, boardPaint);
        for (int k = 0; k < 5; k++) {
            float chipLeft = boxLeft + k * (chipWidth + chipGap);
            float covered = Math.max(0f, Math.min(pillLeft + chipWidth, chipLeft + chipWidth) - Math.max(pillLeft, chipLeft)) / chipWidth;
            boardText(canvas, Shop.KIND_NAMES[k], chipLeft + chipWidth / 2f, chipTop + 9.6f * unit, 6.4f * unit, mixColor(BOARD_DARK, Color.WHITE, covered),
                    Paint.Align.CENTER, chipWidth - 3f * unit);
            coinHit(chipLeft, chipTop, chipLeft + chipWidth, chipTop + 14f * unit, HIT_KIND, k);
        }

        float listTop = chipTop + 18f * unit;
        float listBottom = contentBottom;
        List<Shop.Item> items = Shop.of(coinKind);
        float rowHeight = 22f * unit;
        float scrollMax = Math.max(0f, items.size() * rowHeight - (listBottom - listTop));
        float scroll = Math.max(0f, Math.min(scrollMax, coinScroll));
        if (!coinGhost) {
            coinListRect.set(boxLeft, listTop, boxRight, listBottom);
            coinScrollMax = scrollMax;
            coinScroll = scroll;
        }
        float slide = 0f;
        float baseAlpha = boardAlpha;
        if (!coinGhost && coinKindAge < COIN_KIND_SEC && coinKindFrom != coinKind) {
            float t = coinKindAge / COIN_KIND_SEC;
            float x = 1f - t;
            slide = (coinKind > coinKindFrom ? 1f : -1f) * 18f * unit * x * x * x;
            boardAlpha = baseAlpha * Math.min(1f, 0.2f + t);
        }
        canvas.save();
        canvas.clipRect(boxLeft - unit, listTop, boxRight + unit, listBottom);
        canvas.translate(slide, 0f);
        for (int i = 0; i < items.size(); i++) {
            float rowTop = listTop + i * rowHeight - scroll;
            if (rowTop > listBottom || rowTop + rowHeight < listTop) continue;
            drawShopRow(canvas, items.get(i), Shop.KINDS.indexOf(items.get(i).id.charAt(0)) * 100 + i, boxLeft, boxRight, rowTop, rowHeight, unit);
        }
        canvas.restore();
        boardAlpha = baseAlpha;
        if (scrollMax > 0f) {
            float track = listBottom - listTop - 4f * unit;
            float thumb = Math.max(14f * unit, track * (listBottom - listTop) / (items.size() * rowHeight));
            float thumbTop = listTop + 2f * unit + (track - thumb) * (scroll / scrollMax);
            boardPaint.setColor(faded(BOARD_DIVIDER));
            boardShape.set(boxRight + 1.5f * unit, listTop + 2f * unit, boxRight + 4f * unit, listTop + 2f * unit + track);
            canvas.drawRoundRect(boardShape, unit * 1.25f, unit * 1.25f, boardPaint);
            boardPaint.setColor(faded(BOARD_DARK));
            boardShape.set(boxRight + 1.5f * unit, thumbTop, boxRight + 4f * unit, thumbTop + thumb);
            canvas.drawRoundRect(boardShape, unit * 1.25f, unit * 1.25f, boardPaint);
        }
    }

    private void drawShopRow(Canvas canvas, Shop.Item item, int index, float left, float right, float rowTop, float rowHeight, float unit) {
        boolean owned = wallet != null && wallet.owned.contains(item.id);
        boolean worn = wallet != null && item.id.equals(wallet.style[item.kind]);
        boolean previewing = item.id.equals(coinPreview);
        boardShape.set(left, rowTop + unit, right, rowTop + rowHeight - unit);
        boardPaint.setColor(faded(previewing ? mixColor(BOARD_PANEL_EDGE, BOARD_ORANGE, 0.25f) : BOARD_PANEL_EDGE));
        canvas.drawRoundRect(boardShape, unit * 3.5f, unit * 3.5f, boardPaint);
        float swatch = left + 13f * unit;
        float middle = rowTop + rowHeight / 2f;
        drawSwatch(canvas, item, swatch, middle, unit);
        boardText(canvas, item.name, left + 27f * unit, middle + 3f * unit, 8f * unit, BOARD_DARK, Paint.Align.LEFT, 74f * unit);
        float buttonWidth = 44f * unit;
        RectF button = coinRect;
        button.set(right - 5f * unit - buttonWidth, rowTop + 4f * unit, right - 5f * unit, rowTop + rowHeight - 4f * unit);
        if (!owned) {
            String price = String.format(Locale.US, "%,d", item.price);
            boardTextPaint.setTextSize(7.5f * unit);
            float priceWidth = boardTextPaint.measureText(price);
            float priceRight = button.left - 5f * unit;
            boolean affordable = shownBalance() >= item.price;
            boardText(canvas, price, priceRight, middle + 2.8f * unit, 7.5f * unit, affordable ? BOARD_DARK : BOARD_DROP, Paint.Align.RIGHT, 0f);
            drawCoin(canvas, priceRight - priceWidth - 5f * unit, middle, 3.6f * unit);
        }
        String label;
        int face;
        int text;
        float pulse = 1f;
        if (item.id.equals(buyingItem)) {
            label = "BUYING" + "...".substring(0, 1 + (int) ((coinTime * 3f) % 3f));
            face = BOARD_TAB_IDLE;
            text = BOARD_DARK;
        } else if (worn) {
            label = "ON";
            face = 0xFF5DBB4A;
            text = Color.WHITE;
        } else if (owned) {
            label = "USE";
            face = Color.WHITE;
            text = BOARD_ORANGE;
        } else if (item.id.equals(coinConfirm)) {
            label = "SURE?";
            face = BOARD_ORANGE;
            text = Color.WHITE;
            pulse = 1f + 0.05f * (float) Math.sin(coinTime * 9f);
        } else {
            label = "BUY";
            face = Color.WHITE;
            text = BOARD_ORANGE;
        }
        float buttonLeft = button.left;
        canvas.save();
        canvas.scale(pulse, pulse, button.centerX(), button.centerY());
        coinButton(canvas, button, label, unit, pressedHit(HIT_ACTION, index), text, face);
        canvas.restore();
        coinHit(left, rowTop, buttonLeft - unit, rowTop + rowHeight, HIT_ITEM, index);
        coinHit(buttonLeft - unit, rowTop, right, rowTop + rowHeight, HIT_ACTION, index);
    }

    private Shop.Item itemAt(int index) {
        int kind = index / 100;
        List<Shop.Item> items = Shop.of(kind);
        int at = index % 100;
        return at >= 0 && at < items.size() ? items.get(at) : null;
    }

    private void drawSwatch(Canvas canvas, Shop.Item item, float x, float y, float unit) {
        switch (item.kind) {
            case Shop.NAME:
                lookText(canvas, item, "Aa", x, y + 3.2f * unit, 9f * unit, Paint.Align.CENTER, 0f, 7);
                break;
            case Shop.ROW:
                lookBackground(canvas, item, x - 9f * unit, y - 6f * unit, x + 9f * unit, y + 6f * unit, unit, 3);
                break;
            case Shop.GLOW:
                boardShape.set(x - 8f * unit, y - 5f * unit, x + 8f * unit, y + 5f * unit);
                boardPaint.setColor(faded(BOARD_PANEL));
                canvas.drawRoundRect(boardShape, unit * 2.5f, unit * 2.5f, boardPaint);
                lookGlow(canvas, item, x - 8f * unit, y - 5f * unit, x + 8f * unit, y + 5f * unit, unit);
                break;
            case Shop.MOTION:
                boardShape.set(x - 9f * unit, y - 6f * unit, x + 9f * unit, y + 6f * unit);
                boardPaint.setColor(faded(0xFF9FB8C8));
                canvas.drawRoundRect(boardShape, unit * 2.5f, unit * 2.5f, boardPaint);
                canvas.save();
                canvas.clipRect(boardShape.left, boardShape.top, boardShape.right, boardShape.bottom);
                lookMotion(canvas, item, x - 9f * unit, y - 6f * unit, x + 9f * unit, y + 6f * unit, unit, 7);
                canvas.restore();
                break;
            default:
                drawBadge(canvas, item.effect, x, y, unit * 1.15f);
                break;
        }
    }

    private boolean onCoinsTouch(int action, int x, int y) {
        boolean ready = !coinClosing && coinAnim >= 1f;
        if (action == MotionEvent.ACTION_DOWN) {
            float[] hit = ready ? coinHitAt(x, y) : null;
            coinPressAction = hit == null ? 0 : (int) hit[4];
            coinPressArg = hit == null ? -1 : (int) hit[5];
            coinDragArmed = ready && coinTab == 1 && coinScrollMax > 0f && coinListRect.contains(x, y);
            coinDragging = false;
            coinDragStartY = y;
            coinDragLastY = y;
            coinDragLastTime = SystemClock.uptimeMillis();
            if (coinDragArmed) coinFling = 0f;
            if (coinPressAction != 0) feel(Feel.PRESS);
        } else if (action == MotionEvent.ACTION_MOVE) {
            if (coinDragArmed) {
                if (!coinDragging && Math.abs(y - coinDragStartY) > 8f * baseBoardUnit()) {
                    coinDragging = true;
                    coinPressAction = 0;
                    coinDragLastY = y;
                }
                if (coinDragging) {
                    long now = SystemClock.uptimeMillis();
                    float moved = y - coinDragLastY;
                    float seconds = Math.max(0.001f, (now - coinDragLastTime) / 1000f);
                    coinScroll = Math.max(0f, Math.min(coinScrollMax, coinScroll - moved));
                    coinFling = coinFling * 0.3f + (-moved / seconds) * 0.7f;
                    coinDragLastY = y;
                    coinDragLastTime = now;
                }
            }
        } else if (action == MotionEvent.ACTION_UP) {
            int pressedAction = coinPressAction;
            int pressedArg = coinPressArg;
            coinPressAction = 0;
            coinPressArg = -1;
            boolean dragged = coinDragging;
            coinDragArmed = false;
            coinDragging = false;
            if (dragged) {
                if (SystemClock.uptimeMillis() - coinDragLastTime > 80L) coinFling = 0f;
                return true;
            }
            coinFling = 0f;
            float[] released = ready ? coinHitAt(x, y) : null;
            if (pressedAction != 0 && released != null && pressedAction == (int) released[4] && pressedArg == (int) released[5]) coinAction(pressedAction, pressedArg);
        } else if (action == MotionEvent.ACTION_CANCEL) {
            coinPressAction = 0;
            coinDragArmed = false;
            coinDragging = false;
            coinFling = 0f;
        }
        return true;
    }

    private void coinAction(int action, int arg) {
        switch (action) {
            case HIT_CLOSE:
                playSound(soundSwooshing);
                feel(Feel.BUTTON);
                closeCoins();
                break;
            case HIT_TAB:
                if (coinTab != arg) {
                    playSound(soundSwooshing);
                    feel(Feel.TICK);
                    coinSwitchFrom = coinTab;
                    coinSwitchScroll = coinScroll;
                    coinSwitchAge = 0f;
                    coinTab = arg;
                    coinFling = 0f;
                    coinScroll = 0f;
                    coinPreview = null;
                    coinConfirm = null;
                }
                break;
            case HIT_KIND:
                if (coinKind != arg) {
                    feel(Feel.TICK);
                    coinKindFrom = coinKind;
                    coinKindAge = 0f;
                    coinKind = arg;
                    coinFling = 0f;
                    coinScroll = 0f;
                    coinPreview = null;
                    coinConfirm = null;
                }
                break;
            case HIT_PLAY:
                playChallenge(arg);
                break;
            case HIT_STOP:
                feel(Feel.TOGGLE);
                endTwist(false);
                setCoinNote("Back to your own settings");
                break;
            case HIT_ITEM: {
                Shop.Item item = itemAt(arg);
                if (item == null) break;
                feel(Feel.TICK);
                coinPreview = item.id.equals(coinPreview) ? null : item.id;
                break;
            }
            case HIT_ACTION:
                shopAction(itemAt(arg));
                break;
            default:
                break;
        }
    }

    private void playChallenge(int slot) {
        long day = today();
        Challenges.Goal goal = todayGoals(day)[slot];
        if (goal == null) return;
        String blocked = Challenges.blocker(goal, withTwist(playPrefs(), goal.twist));
        if (blocked != null) {
            feel(Feel.REJECT);
            setCoinNote(blocked.isEmpty() ? "Change your settings back to fair ones first" : "Turn off " + blocked + " in Settings first");
            return;
        }
        if (!settingDailyChallenges) {
            feel(Feel.REJECT);
            setCoinNote("Turn on Daily challenges in Settings first");
            return;
        }
        feel(Feel.BUTTON);
        playSound(soundSwooshing);
        if (goal.twist > Challenges.ANY) startTwist(goal.twist);
        else if (challengeTwist > Challenges.ANY) endTwist(false);
        coinOpen = false;
        coinClosing = false;
        coinAnim = 0f;
        if (boardHost != null && !boardOpen) boardHost.boardVisible(false);
        if (gameState == GameState.HOME) {
            gameState = GameState.TRANSITION_TO_WAITING;
            isFadingOut = true;
        }
    }

    private void shopAction(Shop.Item item) {
        if (item == null || wallet == null) return;
        if (item.id.equals(buyingItem)) return;
        String[] style = wallet.style.clone();
        if (item.id.equals(style[item.kind])) {
            feel(Feel.TOGGLE);
            style[item.kind] = null;
            equipStyle(style);
            return;
        }
        if (wallet.owned.contains(item.id)) {
            feel(Feel.CONFIRM);
            playSound(soundSwooshing);
            style[item.kind] = item.id;
            equipStyle(style);
            coinPreview = null;
            return;
        }
        if (buyingItem != null) {
            setCoinNote("Wait for your last purchase to finish");
            return;
        }
        if (wallet.balance < item.price) {
            feel(Feel.REJECT);
            coinPreview = item.id;
            setCoinNote("You need " + String.format(Locale.US, "%,d", item.price - wallet.balance) + " more Flight Coins");
            return;
        }
        if (!item.id.equals(coinConfirm)) {
            feel(Feel.TICK);
            coinConfirm = item.id;
            coinConfirmAge = 0f;
            coinPreview = item.id;
            return;
        }
        coinConfirm = null;
        buyingItem = item.id;
        buyingSince = Leaderboard.now();
        walletPollSec = 0f;
        feel(Feel.CONFIRM);
        Leaderboard.buy(getContext(), item.id, this::post, (status, error) -> {
            if (error != null) {
                buyingItem = null;
                setCoinNote(error);
                feel(Feel.REJECT);
            } else {
                walletPollSec = WALLET_POLL_SEC * 0.4f;
            }
        });
    }

    private boolean onCoinsKey(int keyCode, KeyEvent event, boolean down) {
        if (!down || event.getRepeatCount() > 0) return true;
        if (keyCode == KeyEvent.KEYCODE_ESCAPE || keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_ENTER
                || keyCode == keyPlay || keyCode == keyFlap || keyCode == keyFlapAlt || keyCode == KeyEvent.KEYCODE_BUTTON_A) {
            closeCoins();
            return true;
        }
        if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT || keyCode == KeyEvent.KEYCODE_DPAD_RIGHT || keyCode == KeyEvent.KEYCODE_TAB
                || keyCode == KeyEvent.KEYCODE_BUTTON_L1 || keyCode == KeyEvent.KEYCODE_BUTTON_R1) {
            int tab = keyCode == KeyEvent.KEYCODE_DPAD_LEFT || keyCode == KeyEvent.KEYCODE_BUTTON_L1 ? 0
                    : keyCode == KeyEvent.KEYCODE_DPAD_RIGHT || keyCode == KeyEvent.KEYCODE_BUTTON_R1 ? 1 : 1 - coinTab;
            coinAction(HIT_TAB, tab);
            return true;
        }
        if (coinTab == 1 && (keyCode == KeyEvent.KEYCODE_DPAD_UP || keyCode == KeyEvent.KEYCODE_DPAD_DOWN)) {
            if (keyCode == KeyEvent.KEYCODE_DPAD_UP) coinAction(HIT_KIND, (coinKind + 4) % 5);
            else coinAction(HIT_KIND, (coinKind + 1) % 5);
            return true;
        }
        return true;
    }

    private boolean scrollCoins(float amount) {
        if (!coinOpen || coinClosing || coinScrollMax <= 0f) return false;
        coinFling = 0f;
        coinScroll = Math.max(0f, Math.min(coinScrollMax, coinScroll + amount * 22f * baseBoardUnit()));
        return true;
    }

    private void layoutCoinButton() {
        float height = settingsButtonBitmap != null ? settingsButtonBitmap.getHeight() : 28f * scale;
        float x = safeInsetLeft + cornerMargin;
        float y = systemBarTop + cornerMargin;
        ensureBoardPaints();
        boardTextPaint.setTextSize(height * 0.56f);
        String balance = balanceLabel();
        float width = height * 1.05f + boardTextPaint.measureText(balance) + height * 0.42f;
        coinButtonRect.set((int) x, (int) y, (int) (x + width), (int) (y + height));
    }

    private void drawCoinButton(Canvas canvas) {
        if (!coinsOn()) {
            coinButtonRect.setEmpty();
            return;
        }
        layoutCoinButton();
        float keep = boardAlpha;
        boardAlpha = 1f;
        float unit = coinButtonRect.height() / 28f;
        float drop = coinBtnProgress * 2f * unit;
        RectF rect = coinRect;
        rect.set(coinButtonRect.left, coinButtonRect.top, coinButtonRect.right, coinButtonRect.bottom);
        float radius = rect.height() / 2f;
        boardPaint.setColor(BOARD_DARK);
        boardShape.set(rect.left, rect.top + 2f * unit, rect.right, rect.bottom + 2f * unit);
        canvas.drawRoundRect(boardShape, radius, radius, boardPaint);
        boardShape.set(rect.left, rect.top + drop, rect.right, rect.bottom + drop);
        canvas.drawRoundRect(boardShape, radius, radius, boardPaint);
        boardShape.inset(2f * unit, 2f * unit);
        boardPaint.setColor(Color.WHITE);
        canvas.drawRoundRect(boardShape, radius - 2f * unit, radius - 2f * unit, boardPaint);
        drawCoin(canvas, rect.left + rect.height() * 0.5f, rect.centerY() + drop - unit * 0.5f, rect.height() * 0.3f * coinBumpScale());
        String balance = balanceLabel();
        boardText(canvas, balance, rect.left + rect.height() * 0.9f, rect.centerY() + drop + rect.height() * 0.19f, rect.height() * 0.56f,
                BOARD_ORANGE, Paint.Align.LEFT, 0f);
        if (coinDot()) {
            float pulse = 0.5f + 0.5f * (float) Math.sin(coinTime * 4f);
            boardPaint.setColor(BOARD_DARK);
            canvas.drawCircle(rect.right - unit * 2f, rect.top + unit * 3f + drop, unit * (5.2f + pulse * 0.6f), boardPaint);
            boardPaint.setColor(BOARD_DROP);
            canvas.drawCircle(rect.right - unit * 2f, rect.top + unit * 2.6f + drop, unit * (4f + pulse * 0.6f), boardPaint);
        }
        boardAlpha = keep;
    }

    private void drawTwistChip(Canvas canvas, boolean interactive) {
        if (challengeTwist <= Challenges.ANY) {
            twistChipRect.setEmpty();
            return;
        }
        ensureBoardPaints();
        float keep = boardAlpha;
        boardAlpha = 1f;
        float unit = scale;
        String label = Challenges.TWIST_NAMES[challengeTwist].toUpperCase(Locale.US) + " CHALLENGE";
        float size = 7.6f * unit;
        boardTextPaint.setTextSize(size);
        float textWidth = boardTextPaint.measureText(label);
        float height = 17f * unit;
        float width = textWidth + (interactive ? 26f : 14f) * unit;
        float x = safeInsetLeft + (screenWidth - safeInsetLeft - safeInsetRight - width) / 2f;
        float y = systemBarTop + cornerMargin;
        if (gameState == GameState.HOME && !coinButtonRect.isEmpty()) {
            x = coinButtonRect.left;
            y = coinButtonRect.bottom + 8f * unit;
        }
        boardShape.set(x, y, x + width, y + height);
        boardPaint.setColor(0xCC543847);
        canvas.drawRoundRect(boardShape, height / 2f, height / 2f, boardPaint);
        boardText(canvas, label, x + 7f * unit + textWidth / 2f, y + height / 2f + size * 0.36f, size, COIN_GOLD, Paint.Align.CENTER, 0f);
        if (interactive) {
            float cx = x + width - 10f * unit;
            float cy = y + height / 2f;
            boardPaint.setColor(Color.WHITE);
            boardPaint.setStyle(Paint.Style.STROKE);
            boardPaint.setStrokeWidth(1.6f * unit);
            boardPaint.setStrokeCap(Paint.Cap.ROUND);
            canvas.drawLine(cx - 2.6f * unit, cy - 2.6f * unit, cx + 2.6f * unit, cy + 2.6f * unit, boardPaint);
            canvas.drawLine(cx + 2.6f * unit, cy - 2.6f * unit, cx - 2.6f * unit, cy + 2.6f * unit, boardPaint);
            boardPaint.setStyle(Paint.Style.FILL);
            boardPaint.setStrokeCap(Paint.Cap.BUTT);
            twistChipRect.set((int) x, (int) (y - 4f * unit), (int) (x + width), (int) (y + height + 4f * unit));
        } else {
            twistChipRect.setEmpty();
        }
        boardAlpha = keep;
    }

    private void drawToast(Canvas canvas) {
        if (toastTitle == null || toastAge >= TOAST_SEC) return;
        if ((gameState == GameState.PLAYING && !isPaused) || gameState == GameState.WAITING) return;
        ensureBoardPaints();
        float keep = boardAlpha;
        float rise = Math.min(1f, toastAge / 0.25f);
        float fall = Math.max(0f, Math.min(1f, (toastAge - TOAST_SEC + 0.4f) / 0.4f));
        rise = 1f - (1f - rise) * (1f - rise) * (1f - rise);
        boardAlpha = Math.max(0f, Math.min(1f, rise * (1f - fall)));
        float unit = baseBoardUnit();
        float titleSize = 10f * unit;
        float textSize = 7f * unit;
        boardTextPaint.setTextSize(titleSize);
        float titleWidth = boardTextPaint.measureText(toastTitle);
        boardTextPaint.setTextSize(textSize);
        float textWidth = toastText == null ? 0f : boardTextPaint.measureText(toastText);
        float usable = screenWidth - safeInsetLeft - safeInsetRight;
        float iconRoom = toastCoin ? 20f * unit : 0f;
        float width = Math.min(usable - 16f * unit, Math.max(titleWidth + iconRoom, textWidth) + 22f * unit);
        float height = 34f * unit;
        float x = safeInsetLeft + (usable - width) / 2f;
        float y = systemBarTop + 10f * unit - (1f - rise) * 16f * unit;
        if (gameState == GameState.HOME && !coinButtonRect.isEmpty()) {
            y += Math.max(coinButtonRect.bottom, twistChipRect.isEmpty() ? 0 : twistChipRect.bottom) - systemBarTop;
        } else if (!twistChipRect.isEmpty()) {
            y += twistChipRect.bottom - systemBarTop;
        }
        boardShape.set(x, y + 1.5f * unit, x + width, y + height + 1.5f * unit);
        boardPaint.setColor(faded(BOARD_DARK));
        canvas.drawRoundRect(boardShape, unit * 6f, unit * 6f, boardPaint);
        boardShape.set(x, y, x + width, y + height);
        canvas.drawRoundRect(boardShape, unit * 6f, unit * 6f, boardPaint);
        boardShape.inset(unit * 2f, unit * 2f);
        boardPaint.setColor(faded(BOARD_PANEL));
        canvas.drawRoundRect(boardShape, unit * 4.5f, unit * 4.5f, boardPaint);
        float center = x + width / 2f;
        if (toastCoin) {
            float spin = (float) Math.abs(Math.cos(toastAge * 5f));
            canvas.save();
            float coinX = center - Math.min(titleWidth, width - 30f * unit) / 2f - 4f * unit;
            canvas.scale(0.35f + 0.65f * spin, 1f, coinX, y + 14f * unit);
            drawCoin(canvas, coinX, y + 14f * unit, 6f * unit);
            canvas.restore();
            center += 7f * unit;
        }
        boardText(canvas, toastTitle, center, y + 17.5f * unit, titleSize, BOARD_ORANGE, Paint.Align.CENTER, width - 16f * unit - iconRoom);
        if (toastText != null) {
            boardText(canvas, toastText, x + width / 2f, y + 28f * unit, textSize, BOARD_DARK, Paint.Align.CENTER, width - 16f * unit);
        }
        boardAlpha = keep;
    }

    private static float lookHash(int seed, int k) {
        int h = seed * 0x27D4EB2D + k * 0x165667B1;
        h ^= h >>> 15;
        h *= 0x85EBCA77;
        h ^= h >>> 13;
        h *= 0xC2B2AE3D;
        h ^= h >>> 16;
        return (h & 0xFFFFFF) / (float) 0x1000000;
    }

    private Shop.Item[] lookOf(Leaderboard.Entry entry) {
        if (entry == null || entry.look == null) return null;
        if (!(entry.lookCache instanceof Shop.Item[])) entry.lookCache = Shop.look(entry.look);
        return (Shop.Item[]) entry.lookCache;
    }

    private static int hueColor(float hue, float saturation, float value) {
        float h = ((hue % 1f) + 1f) % 1f * 6f;
        int sector = (int) Math.floor(h);
        float f = h - sector;
        float p = value * (1f - saturation);
        float q = value * (1f - saturation * f);
        float t = value * (1f - saturation * (1f - f));
        float r, g, b;
        switch (sector) {
            case 0: r = value; g = t; b = p; break;
            case 1: r = q; g = value; b = p; break;
            case 2: r = p; g = value; b = t; break;
            case 3: r = p; g = q; b = value; break;
            case 4: r = t; g = p; b = value; break;
            default: r = value; g = p; b = q; break;
        }
        return 0xFF000000 | (Math.round(r * 255f) << 16) | (Math.round(g * 255f) << 8) | Math.round(b * 255f);
    }

    private Shader lookShader(Shop.Item item) {
        int index = -1;
        for (int i = 0; i < Shop.ITEMS.length; i++) {
            if (Shop.ITEMS[i] == item) {
                index = i;
                break;
            }
        }
        if (index < 0) return null;
        if (lookShaders[index] != null) return lookShaders[index];
        Shader shader;
        int a = item.color;
        int b = item.second;
        if (item.kind == Shop.NAME) {
            if (item.effect == 1) {
                shader = new LinearGradient(0f, 0f, 1f, 0f, new int[] {a, a, b, a, a}, new float[] {0f, 0.4f, 0.5f, 0.6f, 1f}, Shader.TileMode.CLAMP);
            } else if (item.effect == 2) {
                shader = new LinearGradient(0f, 0f, 1f, 0f, new int[] {a, b, a}, null, Shader.TileMode.REPEAT);
            } else {
                int[] colors = new int[7];
                for (int i = 0; i < 7; i++) colors[i] = hueColor(i / 6f, 0.85f, 0.95f);
                shader = new LinearGradient(0f, 0f, 1f, 0f, colors, null, Shader.TileMode.REPEAT);
            }
        } else if (item.kind == Shop.ROW) {
            switch (item.effect) {
                case 1:
                    shader = new LinearGradient(0f, 0f, 1f, 0f, new int[] {a, mixColor(a, b, 0.5f), b}, null, Shader.TileMode.CLAMP);
                    break;
                case 3:
                    shader = new LinearGradient(0f, 0f, 0f, 1f, new int[] {b, a, mixColor(a, 0xFF8A5A10, 0.35f)}, new float[] {0f, 0.55f, 1f},
                            Shader.TileMode.CLAMP);
                    break;
                case 4:
                    shader = new LinearGradient(0f, 0f, 1f, 0f, new int[] {a, b, a}, null, Shader.TileMode.CLAMP);
                    break;
                case 5:
                    shader = new LinearGradient(0f, 0f, 1f, 0f, new int[] {a, b, a}, null, Shader.TileMode.REPEAT);
                    break;
                default:
                    shader = new LinearGradient(0f, 0f, 0f, 1f, new int[] {b, a}, null, Shader.TileMode.CLAMP);
                    break;
            }
        } else {
            return null;
        }
        lookShaders[index] = shader;
        return shader;
    }

    private void lookText(Canvas canvas, Shop.Item item, String text, float x, float baseline, float size, Paint.Align align, float room, int seed) {
        if (item == null || item.kind != Shop.NAME) {
            boardText(canvas, text, x, baseline, size, BOARD_DARK, align, room);
            return;
        }
        outlineText(canvas, text, x, baseline, size, align, room);
        if (item.effect == 0) {
            boardText(canvas, text, x, baseline, size, item.color, align, room);
            return;
        }
        Shader shader = lookShader(item);
        boardTextPaint.setTextSize(size);
        float width = Math.max(size, boardTextPaint.measureText(text));
        if (room > 0f) width = Math.min(width, room);
        float start = align == Paint.Align.CENTER ? x - width / 2f : align == Paint.Align.RIGHT ? x - width : x;
        float time = boardShine + lookHash(seed, 1) * 7f;
        lookMatrix.reset();
        if (item.effect == 1) {
            float sweep = Math.max(width, 30f * size / 10f) * 1.6f;
            float travel = (time % 2.6f) / 2.6f;
            lookMatrix.setScale(sweep, 1f);
            lookMatrix.postTranslate(start - sweep + (width + sweep * 1.4f) * travel, 0f);
        } else if (item.effect == 2) {
            float span = Math.max(size * 3f, width * 0.8f);
            lookMatrix.setScale(span, 1f);
            lookMatrix.postTranslate(start - time * span * 0.35f, 0f);
        } else {
            float span = Math.max(size * 4f, width * 1.2f);
            lookMatrix.setScale(span, 1f);
            lookMatrix.postTranslate(start - time * span * 0.25f, 0f);
        }
        shader.setLocalMatrix(lookMatrix);
        boardTextPaint.setShader(shader);
        boardText(canvas, text, x, baseline, size, Color.WHITE, align, room);
        boardTextPaint.setShader(null);
    }

    private void outlineText(Canvas canvas, String text, float x, float baseline, float size, Paint.Align align, float room) {
        boardTextPaint.setShader(null);
        boardTextPaint.setStyle(Paint.Style.STROKE);
        boardTextPaint.setStrokeWidth(size * 0.13f);
        boardTextPaint.setStrokeJoin(Paint.Join.ROUND);
        boardText(canvas, text, x, baseline + size * 0.04f, size, 0xCC543847, align, room);
        boardTextPaint.setStyle(Paint.Style.FILL);
        boardTextPaint.setStrokeWidth(0f);
    }

    private void lookBackground(Canvas canvas, Shop.Item item, float left, float top, float right, float bottom, float unit, int seed) {
        if (item == null) return;
        ensureBoardPaints();
        float radius = unit * 3f;
        boardShape.set(left, top, right, bottom);
        lookPaint.setStyle(Paint.Style.FILL);
        lookPaint.setShader(null);
        if (item.effect == 2) {
            lookPaint.setColor(faded(item.second));
            canvas.drawRoundRect(boardShape, radius, radius, lookPaint);
            canvas.save();
            canvas.clipRect(left + radius * 0.6f, top, right - radius * 0.6f, bottom);
            lookPaint.setColor(faded(scaleAlpha(item.color, 0.75f)));
            float band = 6f * unit;
            float shift = (boardShine * 6f * unit) % (band * 2f);
            for (float x = left - (bottom - top) - band * 2f + shift; x < right; x += band * 2f) {
                lookPath.reset();
                lookPath.moveTo(x, bottom);
                lookPath.lineTo(x + band, bottom);
                lookPath.lineTo(x + band + (bottom - top), top);
                lookPath.lineTo(x + (bottom - top), top);
                lookPath.close();
                canvas.drawPath(lookPath, lookPaint);
            }
            canvas.restore();
            return;
        }
        Shader shader = lookShader(item);
        lookMatrix.reset();
        if (item.effect == 5) {
            float span = (right - left) * 0.7f;
            lookMatrix.setScale(span, bottom - top);
            lookMatrix.postTranslate(left - boardShine * span * 0.08f, top);
        } else {
            lookMatrix.setScale(right - left, bottom - top);
            lookMatrix.postTranslate(left, top);
        }
        if (shader != null) {
            shader.setLocalMatrix(lookMatrix);
            lookPaint.setShader(shader);
            lookPaint.setColor(Color.WHITE);
            lookPaint.setAlpha(Math.round(255f * Math.max(0f, Math.min(1f, boardAlpha))));
        } else {
            lookPaint.setColor(faded(item.color));
        }
        canvas.drawRoundRect(boardShape, radius, radius, lookPaint);
        lookPaint.setShader(null);
        if (item.effect == 3) {
            canvas.save();
            canvas.clipRect(left + radius * 0.6f, top, right - radius * 0.6f, bottom);
            float cycle = (boardShine % 3.2f) / 3.2f;
            float bandX = left - 20f * unit + (right - left + 40f * unit) * cycle;
            lookPaint.setColor(faded(0x88FFFFFF));
            lookPath.reset();
            lookPath.moveTo(bandX, bottom);
            lookPath.lineTo(bandX + 6f * unit, bottom);
            lookPath.lineTo(bandX + 6f * unit + (bottom - top), top);
            lookPath.lineTo(bandX + (bottom - top), top);
            lookPath.close();
            canvas.drawPath(lookPath, lookPaint);
            canvas.restore();
        } else if (item.effect == 4) {
            for (int k = 0; k < 9; k++) {
                float sx = left + 4f * unit + (right - left - 8f * unit) * lookHash(seed, k + 11);
                float sy = top + 2.5f * unit + (bottom - top - 5f * unit) * lookHash(seed, k + 37);
                float twinkle = 0.5f + 0.5f * (float) Math.sin(boardShine * (1.6f + lookHash(seed, k) * 2f) + k * 1.7f);
                lookPaint.setColor(faded(scaleAlpha(0xFFFFFFFF, 0.25f + 0.75f * twinkle)));
                canvas.drawCircle(sx, sy, (0.45f + 0.45f * twinkle) * unit, lookPaint);
            }
        } else if (item.effect == 5) {
            canvas.save();
            canvas.clipRect(left + radius * 0.4f, top, right - radius * 0.4f, bottom);
            for (int k = 0; k < 4; k++) {
                float phase = (boardShine * (0.12f + 0.05f * lookHash(seed, k)) + lookHash(seed, k + 5)) % 1f;
                float bx = left + (right - left) * phase;
                float by = top + (bottom - top) * (0.3f + 0.4f * lookHash(seed, k + 9));
                lookPaint.setColor(faded(scaleAlpha(0xFFFFB43B, 0.35f)));
                canvas.drawCircle(bx, by, 3.2f * unit, lookPaint);
            }
            canvas.restore();
        }
    }

    private void lookGlow(Canvas canvas, Shop.Item item, float left, float top, float right, float bottom, float unit) {
        if (item == null) return;
        ensureBoardPaints();
        float pulse = 0.5f + 0.5f * (float) Math.sin(boardShine * 3.1f);
        int color = item.effect == 1 ? hueColor(boardShine * 0.25f, 0.8f, 1f) : item.color;
        lookPaint.setShader(null);
        lookPaint.setStyle(Paint.Style.STROKE);
        float[] widths = {3.6f, 2.2f, 1.1f};
        float[] alphas = {0.18f, 0.32f, 0.85f};
        for (int i = 0; i < 3; i++) {
            float w = widths[i] * unit * (0.85f + 0.3f * pulse);
            lookPaint.setStrokeWidth(w);
            lookPaint.setColor(faded(scaleAlpha(color, alphas[i] * (0.65f + 0.35f * pulse))));
            boardShape.set(left + w * 0.5f, top + w * 0.5f, right - w * 0.5f, bottom - w * 0.5f);
            canvas.drawRoundRect(boardShape, unit * 3f, unit * 3f, lookPaint);
        }
        lookPaint.setStyle(Paint.Style.FILL);
    }

    private void lookMotion(Canvas canvas, Shop.Item item, float left, float top, float right, float bottom, float unit, int seed) {
        if (item == null) return;
        ensureBoardPaints();
        float width = right - left;
        float height = bottom - top;
        float time = boardShine + lookHash(seed, 3) * 9f;
        lookPaint.setShader(null);
        lookPaint.setStyle(Paint.Style.FILL);
        switch (item.effect) {
            case 0: {
                float cycle = (time % 2.8f) / 2.8f;
                float bandX = left - 16f * unit + (width + 32f * unit) * cycle;
                lookPaint.setColor(faded(0x55FFFFFF));
                lookPath.reset();
                lookPath.moveTo(bandX, bottom);
                lookPath.lineTo(bandX + 9f * unit, bottom);
                lookPath.lineTo(bandX + 9f * unit + height * 0.6f, top);
                lookPath.lineTo(bandX + height * 0.6f, top);
                lookPath.close();
                canvas.drawPath(lookPath, lookPaint);
                break;
            }
            case 1:
                for (int k = 0; k < 6; k++) {
                    float speed = 0.7f + lookHash(seed, k + 20);
                    float life = (time * speed + lookHash(seed, k + 40)) % 1f;
                    float strength = (float) Math.sin(life * Math.PI);
                    float sx = left + 3f * unit + (width - 6f * unit) * lookHash(seed, k + 60 + (int) (time * speed + lookHash(seed, k + 40)));
                    float sy = top + 2.5f * unit + (height - 5f * unit) * lookHash(seed, k + 80 + (int) (time * speed + lookHash(seed, k + 40)));
                    lookStar(canvas, sx, sy, (1.4f + 1.2f * lookHash(seed, k)) * unit * strength, scaleAlpha(item.color, strength));
                }
                break;
            case 2:
                lookPaint.setStyle(Paint.Style.STROKE);
                lookPaint.setStrokeWidth(0.6f * unit);
                for (int k = 0; k < 7; k++) {
                    float speed = 0.25f + 0.25f * lookHash(seed, k + 3);
                    float life = (time * speed + lookHash(seed, k + 13)) % 1f;
                    float bx = left + width * lookHash(seed, k + 23) + (float) Math.sin(time * 2f + k) * 1.2f * unit;
                    float by = bottom + 2f * unit - (height + 4f * unit) * life;
                    lookPaint.setColor(faded(scaleAlpha(item.color, 0.9f * (1f - life * 0.6f))));
                    canvas.drawCircle(bx, by, (0.9f + 1.3f * lookHash(seed, k + 33)) * unit, lookPaint);
                }
                lookPaint.setStyle(Paint.Style.FILL);
                break;
            case 3:
                for (int k = 0; k < 10; k++) {
                    float speed = 0.22f + 0.2f * lookHash(seed, k + 7);
                    float life = (time * speed + lookHash(seed, k + 17)) % 1f;
                    float fx = left + width * lookHash(seed, k + 27) + (float) Math.sin(time * 1.5f + k * 2f) * 2f * unit;
                    float fy = top - 1f * unit + (height + 2f * unit) * life;
                    lookPaint.setColor(faded(scaleAlpha(0xFFFFFFFF, 0.9f)));
                    canvas.drawCircle(fx, fy, (0.5f + 0.6f * lookHash(seed, k + 47)) * unit, lookPaint);
                }
                break;
            case 4:
                for (int k = 0; k < 4; k++) {
                    float speed = 0.3f + 0.15f * lookHash(seed, k + 5);
                    float life = (time * speed + lookHash(seed, k + 15)) % 1f;
                    float hx = left + 8f * unit + (width - 16f * unit) * lookHash(seed, k + 25);
                    float hy = bottom - (height + 2f * unit) * life;
                    float fade = (float) Math.sin(life * Math.PI);
                    lookHeart(canvas, hx, hy, 1.7f * unit, scaleAlpha(item.color, 0.85f * fade));
                }
                break;
            case 5:
                for (int k = 0; k < 9; k++) {
                    float speed = 0.45f + 0.35f * lookHash(seed, k + 2);
                    float life = (time * speed + lookHash(seed, k + 12)) % 1f;
                    float ex = left + width * lookHash(seed, k + 22) + (float) Math.sin(time * 3f + k) * 1.4f * unit;
                    float ey = bottom - height * life;
                    float flicker = 0.6f + 0.4f * (float) Math.sin(time * 14f + k * 3f);
                    int color = mixColor(0xFFFFE27A, item.color, life);
                    lookPaint.setColor(faded(scaleAlpha(color, (1f - life) * flicker)));
                    canvas.drawCircle(ex, ey, (0.5f + 0.7f * (1f - life)) * unit, lookPaint);
                }
                break;
            default:
                for (int k = 0; k < 3; k++) {
                    float speed = 0.12f + 0.06f * lookHash(seed, k + 4);
                    float life = (time * speed + lookHash(seed, k + 14)) % 1f;
                    float fx = left - 6f * unit + (width + 12f * unit) * life;
                    float fy = top + height * (0.25f + 0.5f * lookHash(seed, k + 24)) + (float) Math.sin(time * 2.2f + k * 2f) * 2.5f * unit;
                    canvas.save();
                    canvas.rotate((float) Math.sin(time * 1.7f + k) * 35f + 20f, fx, fy);
                    lookPaint.setColor(faded(scaleAlpha(BOARD_DARK, 0.5f)));
                    boardShape.set(fx - 3.6f * unit, fy - 1.1f * unit + 0.4f * unit, fx + 3.6f * unit, fy + 1.1f * unit + 0.4f * unit);
                    canvas.drawRoundRect(boardShape, 1.1f * unit, 1.1f * unit, lookPaint);
                    lookPaint.setColor(faded(item.color));
                    boardShape.set(fx - 3.6f * unit, fy - 1.1f * unit, fx + 3.6f * unit, fy + 1.1f * unit);
                    canvas.drawRoundRect(boardShape, 1.1f * unit, 1.1f * unit, lookPaint);
                    lookPaint.setColor(faded(scaleAlpha(BOARD_DARK, 0.6f)));
                    lookPaint.setStyle(Paint.Style.STROKE);
                    lookPaint.setStrokeWidth(0.4f * unit);
                    canvas.drawLine(fx - 3.4f * unit, fy, fx + 3.4f * unit, fy, lookPaint);
                    lookPaint.setStyle(Paint.Style.FILL);
                    canvas.restore();
                }
                break;
        }
    }

    private void lookStar(Canvas canvas, float x, float y, float arm, int color) {
        if (arm <= 0.05f) return;
        lookPaint.setColor(faded(color));
        lookPath.reset();
        lookPath.moveTo(x, y - arm);
        lookPath.quadTo(x, y, x + arm, y);
        lookPath.quadTo(x, y, x, y + arm);
        lookPath.quadTo(x, y, x - arm, y);
        lookPath.quadTo(x, y, x, y - arm);
        lookPath.close();
        canvas.drawPath(lookPath, lookPaint);
    }

    private void lookHeart(Canvas canvas, float x, float y, float size, int color) {
        lookPaint.setColor(faded(color));
        lookPath.reset();
        lookPath.moveTo(x, y + size * 1.1f);
        lookPath.quadTo(x - size * 2.1f, y - size * 0.3f, x - size, y - size * 1.2f);
        lookPath.quadTo(x - size * 0.3f, y - size * 1.6f, x, y - size * 0.6f);
        lookPath.quadTo(x + size * 0.3f, y - size * 1.6f, x + size, y - size * 1.2f);
        lookPath.quadTo(x + size * 2.1f, y - size * 0.3f, x, y + size * 1.1f);
        lookPath.close();
        canvas.drawPath(lookPath, lookPaint);
    }

    private void drawBadge(Canvas canvas, int kind, float x, float y, float unit) {
        ensureBoardPaints();
        lookPaint.setShader(null);
        lookPaint.setStyle(Paint.Style.FILL);
        switch (kind) {
            case 0: {
                lookPath.reset();
                for (int i = 0; i < 10; i++) {
                    double angle = -Math.PI / 2 + i * Math.PI / 5;
                    float r = (i % 2 == 0 ? 4.6f : 2f) * unit;
                    float px = x + (float) Math.cos(angle) * r;
                    float py = y + 0.4f * unit + (float) Math.sin(angle) * r;
                    if (i == 0) lookPath.moveTo(px, py);
                    else lookPath.lineTo(px, py);
                }
                lookPath.close();
                lookPaint.setColor(faded(BOARD_DARK));
                canvas.save();
                canvas.translate(0f, 0.7f * unit);
                canvas.drawPath(lookPath, lookPaint);
                canvas.restore();
                lookPaint.setColor(faded(0xFFFFC93C));
                canvas.drawPath(lookPath, lookPaint);
                break;
            }
            case 1:
                lookHeart(canvas, x, y + 0.6f * unit, 2.5f * unit, BOARD_DARK);
                lookHeart(canvas, x, y, 2.5f * unit, 0xFFFF4F7B);
                lookPaint.setColor(faded(0x88FFFFFF));
                canvas.drawCircle(x - 1.6f * unit, y - 1.3f * unit, 0.8f * unit, lookPaint);
                break;
            case 2: {
                float keep = boardAlpha;
                drawCoin(canvas, x, y, 4.4f * unit);
                boardAlpha = keep;
                break;
            }
            case 3:
                lookPath.reset();
                lookPath.moveTo(x + 1.2f * unit, y - 5f * unit);
                lookPath.lineTo(x - 3.2f * unit, y + 0.8f * unit);
                lookPath.lineTo(x - 0.2f * unit, y + 0.8f * unit);
                lookPath.lineTo(x - 1.4f * unit, y + 5f * unit);
                lookPath.lineTo(x + 3.4f * unit, y - 1.2f * unit);
                lookPath.lineTo(x + 0.4f * unit, y - 1.2f * unit);
                lookPath.close();
                lookPaint.setColor(faded(BOARD_DARK));
                canvas.save();
                canvas.translate(0.5f * unit, 0.7f * unit);
                canvas.drawPath(lookPath, lookPaint);
                canvas.restore();
                lookPaint.setColor(faded(0xFFFFE04A));
                canvas.drawPath(lookPath, lookPaint);
                break;
            case 4: {
                float flick = 0.92f + 0.08f * (float) Math.sin(boardShine * 9f + x);
                lookPath.reset();
                lookPath.moveTo(x, y - 5f * unit * flick);
                lookPath.quadTo(x + 5.4f * unit, y + 0.6f * unit, x, y + 4.4f * unit);
                lookPath.quadTo(x - 5.4f * unit, y + 0.6f * unit, x, y - 5f * unit * flick);
                lookPath.close();
                lookPaint.setColor(faded(0xFFE0401F));
                canvas.drawPath(lookPath, lookPaint);
                lookPath.reset();
                lookPath.moveTo(x, y - 1.6f * unit * flick);
                lookPath.quadTo(x + 2.8f * unit, y + 1.8f * unit, x, y + 4f * unit);
                lookPath.quadTo(x - 2.8f * unit, y + 1.8f * unit, x, y - 1.6f * unit * flick);
                lookPath.close();
                lookPaint.setColor(faded(0xFFFFD45C));
                canvas.drawPath(lookPath, lookPaint);
                break;
            }
            case 5: {
                Bitmap bird = birdBitmaps != null && birdBitmaps.length > 1 ? birdBitmaps[(int) (boardShine * 8f) % birdBitmaps.length] : null;
                if (bird != null) {
                    lookPaint.setAlpha(Math.round(255f * Math.max(0f, Math.min(1f, boardAlpha))));
                    lookPaint.setFilterBitmap(false);
                    float w = 11f * unit;
                    float h = w * bird.getHeight() / Math.max(1f, bird.getWidth());
                    boardShape.set(x - w / 2f, y - h / 2f, x + w / 2f, y + h / 2f);
                    canvas.drawBitmap(bird, null, boardShape, lookPaint);
                    lookPaint.setFilterBitmap(true);
                    lookPaint.setAlpha(255);
                }
                break;
            }
            case 6:
                lookPath.reset();
                lookPath.moveTo(x - 4.6f * unit, y + 3.4f * unit);
                lookPath.lineTo(x - 4.8f * unit, y - 3f * unit);
                lookPath.lineTo(x - 2.2f * unit, y - 0.4f * unit);
                lookPath.lineTo(x, y - 4.4f * unit);
                lookPath.lineTo(x + 2.2f * unit, y - 0.4f * unit);
                lookPath.lineTo(x + 4.8f * unit, y - 3f * unit);
                lookPath.lineTo(x + 4.6f * unit, y + 3.4f * unit);
                lookPath.close();
                lookPaint.setColor(faded(BOARD_DARK));
                canvas.save();
                canvas.translate(0f, 0.8f * unit);
                canvas.drawPath(lookPath, lookPaint);
                canvas.restore();
                lookPaint.setColor(faded(0xFFFFC93C));
                canvas.drawPath(lookPath, lookPaint);
                lookPaint.setColor(faded(0xFFE0533D));
                canvas.drawCircle(x, y + 1.4f * unit, 0.95f * unit, lookPaint);
                lookPaint.setColor(faded(0xFF3FA9F5));
                canvas.drawCircle(x - 2.6f * unit, y + 1.8f * unit, 0.7f * unit, lookPaint);
                canvas.drawCircle(x + 2.6f * unit, y + 1.8f * unit, 0.7f * unit, lookPaint);
                break;
            default:
                lookPath.reset();
                lookPath.moveTo(x, y - 4.6f * unit);
                lookPath.lineTo(x + 4.4f * unit, y - 1f * unit);
                lookPath.lineTo(x, y + 4.8f * unit);
                lookPath.lineTo(x - 4.4f * unit, y - 1f * unit);
                lookPath.close();
                lookPaint.setColor(faded(BOARD_DARK));
                canvas.save();
                canvas.translate(0f, 0.7f * unit);
                canvas.drawPath(lookPath, lookPaint);
                canvas.restore();
                lookPaint.setColor(faded(0xFF7FE3FF));
                canvas.drawPath(lookPath, lookPaint);
                lookPaint.setColor(faded(0xFFD8F8FF));
                lookPath.reset();
                lookPath.moveTo(x, y - 4.6f * unit);
                lookPath.lineTo(x + 1.6f * unit, y - 1f * unit);
                lookPath.lineTo(x, y + 4.8f * unit);
                lookPath.lineTo(x - 1.6f * unit, y - 1f * unit);
                lookPath.close();
                canvas.drawPath(lookPath, lookPaint);
                float glint = 0.5f + 0.5f * (float) Math.sin(boardShine * 2.4f);
                lookStar(canvas, x + 2.6f * unit, y - 3.4f * unit, 1.6f * unit * glint, scaleAlpha(0xFFFFFFFF, glint));
                break;
        }
    }

    private void startRoundChecks() {
        roundFairMode = currentFairMode;
        roundChallenge = false;
        if (filmMode) {
            startReplay();
            return;
        }
        roundChallenge = challengeRoundPossible();
        roundTampered = false;
        roundPipes = 0;
        pipesCheck = scoreKey;
        roundActiveSeconds = 0.0;
        roundRealStart = SystemClock.elapsedRealtime();
        roundUptimeStart = SystemClock.uptimeMillis();
        roundPausedMs = 0L;
        pausedSince = -1L;
        boardStatusText = null;
        trackPause();
        startReplay();
        if (roundChallenge) Leaderboard.startSession(getContext(), replaySeed, true);
        else if (roundFairMode != FAIR_NONE && settingBoardAutoPost) Leaderboard.startSession(getContext(), replaySeed);
    }

    static Map<String, Object> replayPrefs(Map<String, ?> all) {
        Map<String, Object> picked = new TreeMap<>();
        for (Map.Entry<String, ?> entry : all.entrySet()) {
            Object value = entry.getValue();
            if (entry.getKey() == null || !entry.getKey().startsWith("pref_")) continue;
            if (value instanceof Integer || value instanceof Float || value instanceof Boolean || value instanceof String || value instanceof Long) {
                picked.put(entry.getKey(), value);
            }
        }
        return picked;
    }

    static void writePrefs(DataOutputStream out, Map<String, Object> values) throws IOException {
        out.writeInt(values.size());
        for (Map.Entry<String, Object> entry : values.entrySet()) {
            Object value = entry.getValue();
            out.writeUTF(entry.getKey());
            if (value instanceof Integer) {
                out.writeByte('I');
                out.writeInt((Integer) value);
            } else if (value instanceof Float) {
                out.writeByte('F');
                out.writeFloat((Float) value);
            } else if (value instanceof Boolean) {
                out.writeByte('Z');
                out.writeBoolean((Boolean) value);
            } else if (value instanceof Long) {
                out.writeByte('J');
                out.writeLong((Long) value);
            } else {
                out.writeByte('S');
                out.writeUTF(String.valueOf(value));
            }
        }
    }

    private void startReplay() {
        replayLive = false;
        replayMarks = 0;
        replayHashed = 0;
        heartbeatTimer = 0f;
        replayHeader = null;
        replayEvents = null;
        replayCount = 0;
        replayLastBits = 0;
        replayPrefs = null;
        replayForBoard = false;
        filmRecording = false;
        recordedHold = false;
        filmExtras = null;
        roundRandomSeed = filmMode && film != null ? film.randomSeed : seedSource.nextLong() ^ System.nanoTime();
        random.setSeed(roundRandomSeed);
        fxRandom.setSeed(roundRandomSeed ^ 0x2545F4914F6CDD1DL);
        if (filmMode) return;
        boolean forBoard = (roundFairMode != FAIR_NONE && settingBoardAutoPost && Leaderboard.configured()) || roundChallenge;
        if (!forBoard && !settingReplayEnabled) return;
        try {
            headerBytes.reset();
            DataOutputStream out = headerOut;
            out.writeInt(REPLAY_FORMAT);
            out.writeInt(REPLAY_RULES);
            out.writeInt(roundFairMode);
            out.writeLong(replaySeed);
            out.writeInt(screenWidth);
            out.writeInt(screenHeight);
            out.writeInt(systemBarTop);
            out.writeInt(systemBarBottom);
            out.writeInt(safeInsetLeft);
            out.writeInt(safeInsetRight);
            out.writeFloat(scale);
            out.writeInt(pipeGap);
            out.writeInt(pipeSpacing);
            out.writeFloat(pipeSpeedPerSec);
            out.writeFloat(gravityPerSec);
            out.writeFloat(flapVelocityPerSec);
            out.writeDouble(clock);
            out.writeDouble(lastFlapTime);
            out.writeFloat(birdX);
            out.writeFloat(baseBirdX);
            out.writeFloat(birdY);
            out.writeFloat(birdVelocityY);
            out.writeFloat(birdRotation);
            out.writeFloat(birdTiltVelocity);
            out.writeFloat(speedVariation);
            out.writeFloat(backgroundX);
            out.writeFloat(groundX);
            out.writeInt(birdFrame);
            out.writeInt(pipes.size());
            for (int i = 0; i < pipes.size(); i++) {
                Pipe pipe = pipes.get(i);
                out.writeFloat(pipe.x);
                out.writeInt(pipe.topPipeY);
                out.writeInt(pipe.width);
                out.writeInt(pipe.height);
                out.writeBoolean(pipe.isScored);
                out.writeBoolean(pipe.isGolden);
            }
            Map<String, Object> current = replayPrefs(playPrefs());
            if (packedPrefsBytes == null || !current.equals(packedPrefs)) {
                FastBytes packed = new FastBytes(2048);
                DataOutputStream packer = new DataOutputStream(packed);
                writePrefs(packer, current);
                packer.flush();
                packedPrefs = current;
                packedPrefsBytes = packed.toByteArray();
            }
            replayPrefs = packedPrefs;
            out.write(packedPrefsBytes);
            out.flush();
            replayHeader = headerBytes.toByteArray();
            if (forBoard) {
                if (replayDigest == null) replayDigest = MessageDigest.getInstance("SHA-256");
                chainStart(replayDigest, replayHeader, replayChain);
            }
            if (settingReplayEnabled) {
                filmExtras = filmExtras();
                filmBestBefore = highScore;
            }
            int[] spare = SPARE_EVENTS.poll();
            replayEvents = spare != null ? spare : new int[16384];
            replayForBoard = forBoard;
            filmRecording = settingReplayEnabled;
            replayLive = true;
        } catch (IOException | NoSuchAlgorithmException | RuntimeException e) {
            replayLive = false;
        }
    }

    static void chainStart(MessageDigest digest, byte[] header, byte[] chain) {
        digest.reset();
        digest.update(header);
        System.arraycopy(digest.digest(), 0, chain, 0, 32);
    }

    static String chainNext(MessageDigest digest, byte[] chain, int[] events, int from, int to) {
        digest.reset();
        digest.update(chain);
        byte[] words = new byte[Math.min(4096, Math.max(0, to - from)) * 4];
        for (int i = from; i < to; ) {
            int n = Math.min(words.length / 4, to - i);
            for (int k = 0; k < n; k++) {
                int value = events[i + k];
                int at = k * 4;
                words[at] = (byte) (value >>> 24);
                words[at + 1] = (byte) (value >>> 16);
                words[at + 2] = (byte) (value >>> 8);
                words[at + 3] = (byte) value;
            }
            digest.update(words, 0, n * 4);
            i += n;
        }
        byte[] next = digest.digest();
        System.arraycopy(next, 0, chain, 0, 32);
        StringBuilder code = new StringBuilder(16);
        for (int i = 0; i < 8; i++) {
            code.append(Character.forDigit((next[i] >> 4) & 15, 16)).append(Character.forDigit(next[i] & 15, 16));
        }
        return code.toString();
    }

    private void commitMark(int kind) {
        if (!replayLive || !replayForBoard || replayDigest == null) return;
        if (kind == MARK_PLAY && Leaderboard.heartbeatBusy()) return;
        heartbeatTimer = 0f;
        recordEvent(REPLAY_MARK | kind);
        if (!replayLive) return;
        String code = chainNext(replayDigest, replayChain, replayEvents, replayHashed, replayCount);
        replayHashed = replayCount;
        Leaderboard.heartbeat(getContext(), replaySeed, replayMarks++, kind, code);
    }

    private void recordEvent(int value) {
        if (replayCount == replayEvents.length) {
            if (replayCount >= REPLAY_MAX_EVENTS) {
                replayLive = false;
                replayEvents = null;
                return;
            }
            replayEvents = Arrays.copyOf(replayEvents, Math.min(REPLAY_MAX_EVENTS, replayCount * 2));
        }
        replayEvents[replayCount++] = value;
    }

    private void recordStep(float dt) {
        int bits = Float.floatToIntBits(dt);
        recordEvent(bits ^ replayLastBits);
        replayLastBits = bits;
    }

    private void step(float dt) {
        if (replayLive && gameState == GameState.PLAYING && !isPaused) {
            if (!replayForBoard && settingJetpackModeEnabled && isScreenPressed != recordedHold) {
                recordedHold = isScreenPressed;
                recordEvent(REPLAY_MARK | (recordedHold ? MARK_HOLD_ON : MARK_HOLD_OFF));
            }
            if (replayLive) recordStep(dt);
        }
        update(dt);
    }

    static float snapStep(float advance, float stepSec) {
        float ratio = advance / stepSec;
        if (ratio >= 1f) {
            int whole = Math.round(ratio);
            if (whole <= MAX_FIXED_STEPS && Math.abs(ratio - whole) < STEP_SNAP * whole) return whole * stepSec;
            return advance;
        }
        float framesPerStep = stepSec / advance;
        int whole = Math.round(framesPerStep);
        if (whole >= 1 && whole <= 4 && Math.abs(framesPerStep - whole) < STEP_SNAP * whole) return stepSec / whole;
        return advance;
    }

    private void holdMotion() {
        pastBirdX = birdX;
        pastBirdY = birdY;
        pastBirdRotation = birdRotation;
        pastGroundX = groundX;
        pastBackgroundX = backgroundX;
        for (int i = 0; i < pipes.size(); i++) {
            Pipe pipe = pipes.get(i);
            pipe.pastX = pipe.x;
        }
        motionState = gameState;
        motionValid = true;
    }

    private static float blendWrapped(float past, float now, float amount, float length) {
        float change = now - past;
        if (length > 0f && Math.abs(change) > length / 2f) change -= Math.signum(change) * length;
        float value = now + change * amount;
        return length > 0f ? value % length : value;
    }

    private boolean blendMotion() {
        if (!settingFixedStepEnabled || !motionValid || isPaused || settingShowHitboxes || gameState != motionState) return false;
        if (settingStepOriginal) return false;
        float amount = stepAccumulator / fixedStepSec;
        if (!(amount > 0f) || amount >= 1f) return false;
        keptBirdX = birdX;
        keptBirdY = birdY;
        keptBirdRotation = birdRotation;
        keptGroundX = groundX;
        keptBackgroundX = backgroundX;
        birdX += (birdX - pastBirdX) * amount;
        birdY += (birdY - pastBirdY) * amount;
        birdRotation = Math.max(-90f, Math.min(90f, birdRotation + (birdRotation - pastBirdRotation) * amount));
        groundX = blendWrapped(pastGroundX, groundX, amount, groundBitmap.getWidth());
        backgroundX = blendWrapped(pastBackgroundX, backgroundX, amount, bgDayBitmap.getWidth());
        for (int i = 0; i < pipes.size(); i++) {
            Pipe pipe = pipes.get(i);
            pipe.keptX = pipe.x;
            float change = pipe.x - pipe.pastX;
            if (Math.abs(change) < pipe.width) pipe.x += change * amount;
        }
        return true;
    }

    private void restoreMotion() {
        birdX = keptBirdX;
        birdY = keptBirdY;
        birdRotation = keptBirdRotation;
        groundX = keptGroundX;
        backgroundX = keptBackgroundX;
        for (int i = 0; i < pipes.size(); i++) {
            Pipe pipe = pipes.get(i);
            pipe.x = pipe.keptX;
        }
    }

    private byte[] filmExtras() throws IOException {
        extrasBytes.reset();
        DataOutputStream out = extrasOut;
        out.writeLong(roundRandomSeed);
        out.writeInt(currentBirdColor);
        out.writeBoolean(nightBackground);
        out.writeFloat(dayNightCycleProgress);
        out.writeFloat(rainbowHue);
        out.writeFloat(drunkModePhase);
        out.writeInt(livesLeft);
        out.writeFloat(pipeAnimationCounter);
        out.writeInt(pipes.size());
        for (int i = 0; i < pipes.size(); i++) out.writeInt(pipes.get(i).colorIndex);
        out.writeFloat(goldenShineOffset);
        out.writeInt(trailCount);
        for (int i = 0; i < trailCount; i++) birdTrail[(trailStart + i) % MAX_TRAIL_PARTICLES].save(out);
        weatherSystem.save(out);
        out.flush();
        return extrasBytes.toByteArray();
    }

    private void saveFilm() {
        if (!filmRecording || !replayLive || replayHeader == null || replayEvents == null || replayCount == 0 || filmExtras == null) return;
        if (replayPrefs == null || !replayPrefs.equals(replayPrefs(playPrefs()))) return;
        final byte[] header = replayHeader;
        final byte[] extras = filmExtras;
        final int count = replayCount;
        final boolean handOff = !replayForBoard;
        final int[] events = handOff ? replayEvents : Arrays.copyOf(replayEvents, replayCount);
        if (handOff) {
            replayEvents = null;
            replayLive = false;
        }
        final int keep = settingReplayKeep;
        final boolean best = settingReplayBest;
        final int finalScore = Math.max(0, score);
        final int bestBefore = filmBestBefore;
        final int flaps = roundFlaps;
        final float seconds = (float) Math.max(0.0, clock - roundStartClock);
        final int mode = roundFairMode;
        final Context app = getContext().getApplicationContext() != null ? getContext().getApplicationContext() : getContext();
        final long savedAt = System.currentTimeMillis();
        filmSaver().execute(() -> {
            try {
                File dir = new File(app.getFilesDir(), FILM_DIR);
                String version = Leaderboard.versionName(app);
                if (best) writeBestFilm(dir, savedAt, finalScore, bestBefore, flaps, seconds, mode, version, header, extras, events, count);
                else writeFilm(dir, FILM_PREFIX, savedAt, finalScore, bestBefore, flaps, seconds, mode, version, header, extras, events, count, keep);
            } catch (IOException | RuntimeException e) {
                Log.w(TAG, "Replay could not be saved", e);
            } finally {
                if (handOff) recycleEvents(events);
            }
        });
    }

    static void recycleEvents(int[] events) {
        if (events == null || events.length > SPARE_EVENT_MAX || SPARE_EVENTS.size() >= SPARE_EVENT_BUFFERS) return;
        SPARE_EVENTS.offer(events);
    }

    private static synchronized ExecutorService filmSaver() {
        if (filmSaver == null) {
            filmSaver = Executors.newSingleThreadExecutor(task -> {
                Thread thread = new Thread(task, "ReplaySaver");
                thread.setDaemon(true);
                thread.setPriority(Thread.MIN_PRIORITY);
                return thread;
            });
        }
        return filmSaver;
    }

    static boolean writeBestFilm(File dir, long savedAt, int score, int bestBefore, int flaps, float seconds, int mode, String version,
            byte[] header, byte[] extras, int[] events) throws IOException {
        return writeBestFilm(dir, savedAt, score, bestBefore, flaps, seconds, mode, version, header, extras, events, events.length);
    }

    static boolean writeBestFilm(File dir, long savedAt, int score, int bestBefore, int flaps, float seconds, int mode, String version,
            byte[] header, byte[] extras, int[] events, int count) throws IOException {
        List<File> previous = films(dir, BEST_PREFIX);
        for (File file : previous) {
            try {
                if (Film.read(file).score >= score) return false;
            } catch (IOException e) {
                file.delete();
            }
        }
        File saved = writeFilm(dir, BEST_PREFIX, savedAt, score, bestBefore, flaps, seconds, mode, version, header, extras, events, count, Integer.MAX_VALUE);
        for (File file : previous) {
            if (!file.equals(saved)) file.delete();
        }
        pruneTemp(dir);
        return true;
    }

    static File writeFilm(File dir, String prefix, long savedAt, int score, int bestBefore, int flaps, float seconds, int mode, String version,
            byte[] header, byte[] extras, int[] events, int keep) throws IOException {
        return writeFilm(dir, prefix, savedAt, score, bestBefore, flaps, seconds, mode, version, header, extras, events, events.length, keep);
    }

    static File writeFilm(File dir, String prefix, long savedAt, int score, int bestBefore, int flaps, float seconds, int mode, String version,
            byte[] header, byte[] extras, int[] events, int count, int keep) throws IOException {
        if (!dir.isDirectory() && !dir.mkdirs()) throw new IOException("No replay folder");
        while (new File(dir, FILM_PREFIX + savedAt + FILM_SUFFIX).exists() || new File(dir, BEST_PREFIX + savedAt + FILM_SUFFIX).exists()) savedAt++;
        File temp = new File(dir, prefix + savedAt + ".tmp");
        try (DataOutputStream out = new DataOutputStream(new java.io.BufferedOutputStream(new DeflaterOutputStream(new FileOutputStream(temp)), 16384))) {
            out.writeInt(FILM_FORMAT);
            out.writeLong(savedAt);
            out.writeInt(score);
            out.writeInt(bestBefore);
            out.writeInt(flaps);
            out.writeFloat(seconds);
            out.writeInt(mode);
            out.writeUTF(version == null ? "" : version);
            out.writeInt(header.length);
            out.write(header);
            out.writeInt(extras.length);
            out.write(extras);
            out.writeInt(count);
            java.nio.ByteBuffer packed = java.nio.ByteBuffer.allocate(4 * Math.min(count, 16384));
            for (int from = 0; from < count; from += 16384) {
                int to = Math.min(count, from + 16384);
                packed.clear();
                packed.asIntBuffer().put(events, from, to - from);
                out.write(packed.array(), 0, (to - from) * 4);
            }
        }
        File done = new File(dir, prefix + savedAt + FILM_SUFFIX);
        if (!temp.renameTo(done)) {
            temp.delete();
            throw new IOException("Replay could not be stored");
        }
        if (FILM_PREFIX.equals(prefix)) pruneFilms(dir, keep);
        return done;
    }

    static List<File> films(File dir) {
        return films(dir, FILM_PREFIX);
    }

    static List<File> films(File dir, String prefix) {
        List<File> found = new ArrayList<>();
        File[] files = dir.listFiles();
        if (files == null) return found;
        for (File file : files) {
            if (file.getName().startsWith(prefix) && file.getName().endsWith(FILM_SUFFIX)) found.add(file);
        }
        java.util.Collections.sort(found, (a, b) -> Long.compare(filmTime(b), filmTime(a)));
        return found;
    }

    static long filmTime(File file) {
        String name = file.getName();
        try {
            return Long.parseLong(name.substring(name.indexOf('_') + 1, name.length() - FILM_SUFFIX.length()));
        } catch (RuntimeException e) {
            return file.lastModified();
        }
    }

    static void pruneFilms(File dir, int keep) {
        List<File> all = films(dir);
        for (int i = Math.max(0, keep); i < all.size(); i++) all.get(i).delete();
        pruneTemp(dir);
    }

    static void pruneTemp(File dir) {
        File[] files = dir.listFiles();
        if (files == null) return;
        for (File file : files) {
            if (file.getName().endsWith(".tmp") && System.currentTimeMillis() - file.lastModified() > 60000L) file.delete();
        }
    }

    static final int SNAPSHOT_WIDTH = 320;
    static final String SNAPSHOT_DIR = "replay_thumbs";

    static double filmDeathTime(Film film) {
        double total = FILM_LEAD_SEC;
        int last = 0;
        for (int value : film.events) {
            if (value == REPLAY_FLAP || (value & REPLAY_MARK) != 0) continue;
            int bits = value ^ last;
            last = bits;
            float dt = Float.intBitsToFloat(bits);
            if (dt > 0f && dt <= 1f) total += dt;
        }
        return total;
    }

    static String snapshotKey(File film) {
        return film.getName() + "_" + film.length() + "_" + SNAPSHOT_WIDTH;
    }

    static File snapshotFolder(Context context) {
        return new File(context.getCacheDir(), SNAPSHOT_DIR);
    }

    static Bitmap loadSnapshot(Context context, File film) {
        File cached = new File(snapshotFolder(context), snapshotKey(film) + ".jpg");
        if (!cached.isFile()) return null;
        try {
            return BitmapFactory.decodeFile(cached.getPath());
        } catch (RuntimeException | OutOfMemoryError e) {
            return null;
        }
    }

    static double storedLength(Context context, File film) {
        File stored = new File(snapshotFolder(context), snapshotKey(film) + ".len");
        if (!stored.isFile()) return -1.0;
        try (DataInputStream in = new DataInputStream(new FileInputStream(stored))) {
            double value = in.readDouble();
            return value > 0.0 && value < 86400.0 ? value : -1.0;
        } catch (IOException | RuntimeException e) {
            return -1.0;
        }
    }

    static void storeLength(Context context, File film, double length) {
        File folder = snapshotFolder(context);
        if (!folder.isDirectory() && !folder.mkdirs()) return;
        File temp = new File(folder, snapshotKey(film) + ".len.tmp");
        try (DataOutputStream out = new DataOutputStream(new FileOutputStream(temp))) {
            out.writeDouble(length);
        } catch (IOException | RuntimeException e) {
            temp.delete();
            return;
        }
        if (!temp.renameTo(new File(folder, snapshotKey(film) + ".len"))) temp.delete();
    }

    static Bitmap renderSnapshot(Context context, File file, Film film) {
        Bitmap big = null;
        try {
            if (!film.complete) film = Film.read(file);
            GameView view = new GameView(context, new MemoryPrefs(filmPlaybackPrefs(film, false, false)), false);
            view.measure(MeasureSpec.makeMeasureSpec(film.width, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(film.height, MeasureSpec.EXACTLY));
            view.layout(0, 0, film.width, film.height);
            view.filmLoad(film);
            if (!view.filmReady()) return null;
            view.filmSeek(Math.max(FILM_LEAD_SEC + 0.05, filmDeathTime(film) - 0.4));
            int width = SNAPSHOT_WIDTH, height = Math.max(1, Math.round(SNAPSHOT_WIDTH * film.height / (float) film.width));
            big = Bitmap.createBitmap(width * 2, height * 2, Bitmap.Config.ARGB_8888);
            big.setDensity(Bitmap.DENSITY_NONE);
            Canvas canvas = new Canvas(big);
            canvas.setDensity(Bitmap.DENSITY_NONE);
            canvas.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.FILTER_BITMAP_FLAG));
            canvas.drawColor(Color.BLACK);
            canvas.scale(width * 2f / film.width, height * 2f / film.height);
            view.filmDraw(canvas);
            Bitmap small = Bitmap.createScaledBitmap(big, width, height, true);
            if (small != big) big.recycle();
            big = null;
            File folder = snapshotFolder(context);
            if (folder.isDirectory() || folder.mkdirs()) {
                File temp = new File(folder, snapshotKey(file) + ".jpg.tmp");
                try (FileOutputStream out = new FileOutputStream(temp)) {
                    small.compress(Bitmap.CompressFormat.JPEG, 90, out);
                } catch (IOException | RuntimeException e) {
                    temp.delete();
                }
                if (temp.isFile() && !temp.renameTo(new File(folder, snapshotKey(file) + ".jpg"))) temp.delete();
            }
            if (storedLength(context, file) < 0.0) storeLength(context, file, view.filmLength());
            return small;
        } catch (IOException | RuntimeException | OutOfMemoryError e) {
            if (big != null) big.recycle();
            return null;
        }
    }

    static Bitmap snapshot(Context context, File file) {
        Bitmap cached = loadSnapshot(context, file);
        if (cached != null) return cached;
        try {
            return renderSnapshot(context, file, Film.read(file));
        } catch (IOException e) {
            return null;
        }
    }

    static void forgetSnapshots(Context context, java.util.Collection<File> keep) {
        java.util.Set<String> wanted = new java.util.HashSet<>();
        for (File film : keep) wanted.add(snapshotKey(film));
        File[] all = snapshotFolder(context).listFiles();
        if (all == null) return;
        for (File file : all) {
            String name = file.getName();
            boolean keepIt = false;
            for (String key : wanted) {
                if (name.startsWith(key + ".")) {
                    keepIt = true;
                    break;
                }
            }
            if (!keepIt) file.delete();
        }
    }

    static List<File> newestFilms(File dir, int recentKeep) {
        List<File> all = new ArrayList<>();
        List<File> recent = films(dir, FILM_PREFIX);
        all.addAll(recent.size() > recentKeep ? recent.subList(0, recentKeep) : recent);
        List<File> best = films(dir, BEST_PREFIX);
        if (!best.isEmpty()) all.add(best.get(0));
        java.util.Collections.sort(all, (a, b) -> Long.compare(filmTime(b), filmTime(a)));
        return all;
    }

    static final class BoardMemory {
        static final int MAGIC = 0x46425346;
        static final int MAX_ROWS = 1000;
        final Object[] lists = new Object[4];
        final Leaderboard.Entry[] mine = new Leaderboard.Entry[4];
        final int[] rank = new int[4];
        String url = "";
        String uid = "";
        int week;

        @SuppressWarnings("unchecked")
        List<Leaderboard.Entry> list(int view) {
            return (List<Leaderboard.Entry>) lists[view];
        }

        void forget(int view) {
            lists[view] = null;
            mine[view] = null;
            rank[view] = 0;
        }

        byte[] bytes() throws IOException {
            ByteArrayOutputStream raw = new ByteArrayOutputStream(4096);
            DataOutputStream out = new DataOutputStream(raw);
            out.writeInt(MAGIC);
            out.writeUTF(clip(url));
            out.writeUTF(clip(uid));
            out.writeInt(week);
            for (int view = 0; view < 4; view++) {
                List<Leaderboard.Entry> list = list(view);
                int count = list == null ? -1 : Math.min(MAX_ROWS, list.size());
                out.writeInt(count);
                for (int i = 0; i < count; i++) writeEntry(out, list.get(i));
                out.writeBoolean(mine[view] != null);
                if (mine[view] != null) writeEntry(out, mine[view]);
                out.writeInt(rank[view]);
            }
            out.flush();
            return raw.toByteArray();
        }

        static BoardMemory parse(byte[] data) {
            BoardMemory memory = new BoardMemory();
            if (data == null || data.length < 8) return memory;
            try {
                DataInputStream in = new DataInputStream(new ByteArrayInputStream(data));
                if (in.readInt() != MAGIC) return memory;
                BoardMemory loaded = new BoardMemory();
                loaded.url = in.readUTF();
                loaded.uid = in.readUTF();
                loaded.week = in.readInt();
                for (int view = 0; view < 4; view++) {
                    int count = in.readInt();
                    if (count > MAX_ROWS) return memory;
                    if (count >= 0) {
                        List<Leaderboard.Entry> list = new ArrayList<>(count);
                        for (int i = 0; i < count; i++) list.add(readEntry(in));
                        loaded.lists[view] = list;
                    }
                    if (in.readBoolean()) loaded.mine[view] = readEntry(in);
                    loaded.rank[view] = in.readInt();
                }
                return loaded;
            } catch (IOException | RuntimeException e) {
                return memory;
            }
        }

        static BoardMemory read(File file) {
            if (file == null || !file.isFile() || file.length() > (1 << 20)) return new BoardMemory();
            try (FileInputStream in = new FileInputStream(file)) {
                ByteArrayOutputStream raw = new ByteArrayOutputStream((int) Math.max(64L, file.length()));
                byte[] buffer = new byte[8192];
                int read;
                while ((read = in.read(buffer)) > 0) raw.write(buffer, 0, read);
                return parse(raw.toByteArray());
            } catch (IOException | RuntimeException e) {
                return new BoardMemory();
            }
        }

        static void write(File file, byte[] bytes) {
            File temp = new File(file.getPath() + ".tmp");
            try (FileOutputStream out = new FileOutputStream(temp)) {
                out.write(bytes);
            } catch (IOException | RuntimeException e) {
                temp.delete();
                return;
            }
            if (!temp.renameTo(file)) {
                file.delete();
                if (!temp.renameTo(file)) temp.delete();
            }
        }

        private static String clip(String text) {
            if (text == null) return "";
            return text.length() > 200 ? text.substring(0, 200) : text;
        }

        private static void writeEntry(DataOutputStream out, Leaderboard.Entry entry) throws IOException {
            out.writeUTF(clip(entry.uid));
            out.writeUTF(clip(entry.name));
            out.writeInt(entry.score);
            out.writeByte((entry.classic ? 1 : 0) | (entry.verified ? 2 : 0));
            out.writeLong(entry.time);
            out.writeUTF(clip(entry.version));
            StringBuilder look = new StringBuilder();
            for (int i = 0; entry.look != null && i < entry.look.length && i < 5; i++) {
                if (i > 0) look.append(',');
                if (entry.look[i] != null) look.append(entry.look[i]);
            }
            out.writeUTF(clip(look.toString()));
        }

        private static Leaderboard.Entry readEntry(DataInputStream in) throws IOException {
            Leaderboard.Entry entry = new Leaderboard.Entry();
            entry.uid = in.readUTF();
            entry.name = in.readUTF();
            entry.score = in.readInt();
            int flags = in.readByte();
            entry.classic = (flags & 1) != 0;
            entry.verified = (flags & 2) != 0;
            entry.time = in.readLong();
            entry.version = in.readUTF();
            String look = in.readUTF();
            if (!look.isEmpty()) {
                String[] ids = (look + ",,,,,").split(",", -1);
                String[] kept = new String[5];
                boolean any = false;
                for (int i = 0; i < 5; i++) {
                    if (Shop.validId(ids[i]) && ids[i].charAt(0) == Shop.KINDS.charAt(i)) {
                        kept[i] = ids[i];
                        any = true;
                    }
                }
                if (any) entry.look = kept;
            }
            return entry;
        }
    }

    static final class BoardMotion {
        final List<Leaderboard.Entry> list;
        final float[] from;
        final int[] fromScore;
        final float[] countSec;
        final float[] enterAt;
        final boolean[] newcomer;
        final boolean entrance;
        int mineFromScore = -1;
        int mineScore;
        int mineFromRank;
        int mineToRank;
        float mineCountSec;
        int ownRow = -1;
        boolean ownClimbed;
        float age = -BOARD_MOTION_DELAY;
        float length;
        long tickSum = Long.MIN_VALUE;
        boolean rose;
        boolean landed;
        boolean felt;

        BoardMotion(List<Leaderboard.Entry> list, boolean entrance) {
            this.list = list;
            this.entrance = entrance;
            int count = list.size();
            from = new float[count];
            fromScore = new int[count];
            countSec = new float[count];
            enterAt = new float[count];
            newcomer = new boolean[count];
            Arrays.fill(from, Float.NaN);
            Arrays.fill(fromScore, -1);
            Arrays.fill(enterAt, -1f);
        }

        static BoardMotion plan(List<Leaderboard.Entry> before, Leaderboard.Entry mineBefore, int rankBefore,
                List<Leaderboard.Entry> after, Leaderboard.Entry mineAfter, int rankAfter, boolean entrance, String own) {
            if (after == null) return null;
            int count = after.size();
            BoardMotion motion = new BoardMotion(after, entrance);
            if (entrance) {
                for (int i = 0; i < count; i++) motion.enterAt[i] = Math.min(i, BOARD_ROWS + 2) * BOARD_ENTER_STAGGER;
                motion.measure();
                return motion.length > 0f ? motion : null;
            }
            if (before == null) return null;
            HashMap<String, Integer> where = new HashMap<>(before.size() * 2 + 1);
            for (int i = 0; i < before.size(); i++) {
                String uid = before.get(i).uid;
                if (uid != null && !uid.isEmpty() && !where.containsKey(uid)) where.put(uid, i);
            }
            int reach = BOARD_ROWS + 1;
            for (int i = 0; i < count; i++) {
                Leaderboard.Entry entry = after.get(i);
                boolean ownEntry = own != null && !own.isEmpty() && own.equals(entry.uid);
                if (ownEntry) motion.ownRow = i;
                Integer was = entry.uid == null ? null : where.get(entry.uid);
                int oldScore = -1;
                float oldRow = Float.NaN;
                if (was != null) {
                    oldRow = was;
                    oldScore = before.get(was).score;
                } else if (ownEntry && mineBefore != null && own.equals(mineBefore.uid)) {
                    oldRow = i + reach;
                    oldScore = mineBefore.score;
                } else if (i < before.size()) {
                    motion.newcomer[i] = true;
                    motion.enterAt[i] = 0f;
                }
                if (!Float.isNaN(oldRow) && oldRow != i) {
                    motion.from[i] = Math.max(i - reach, Math.min(i + reach, oldRow));
                    if (ownEntry && oldRow > i) motion.ownClimbed = true;
                }
                if (oldScore >= 0 && entry.score > oldScore) {
                    motion.fromScore[i] = oldScore;
                    motion.countSec[i] = countSeconds(entry.score - oldScore);
                }
            }
            if (mineAfter != null && mineBefore != null && own != null && own.equals(mineAfter.uid) && own.equals(mineBefore.uid)) {
                motion.mineScore = mineAfter.score;
                if (mineAfter.score > mineBefore.score) {
                    motion.mineFromScore = mineBefore.score;
                    motion.mineCountSec = countSeconds(mineAfter.score - mineBefore.score);
                }
                if (rankBefore > 0 && rankAfter > 0 && rankBefore != rankAfter) {
                    motion.mineFromRank = rankBefore;
                    motion.mineToRank = rankAfter;
                    if (rankAfter < rankBefore) motion.ownClimbed = true;
                }
            }
            motion.measure();
            return motion.length > 0f ? motion : null;
        }

        static float countSeconds(int gain) {
            float sec = BOARD_COUNT_MIN_SEC + 0.11f * (float) Math.log(1.0 + Math.max(0, gain));
            return Math.min(BOARD_COUNT_MAX_SEC, sec);
        }

        void measure() {
            float end = 0f;
            for (int i = 0; i < from.length; i++) {
                if (!Float.isNaN(from[i])) end = Math.max(end, Math.max(BOARD_SLIDE_SEC, BOARD_ARROW_SEC));
                if (fromScore[i] >= 0) {
                    end = Math.max(end, BOARD_COUNT_DELAY + countSec[i] + Math.max(BOARD_POP_SEC, BOARD_GLOW_FADE_SEC));
                    end = Math.max(end, BOARD_COUNT_DELAY + BOARD_GAIN_SEC);
                }
                if (enterAt[i] >= 0f) end = Math.max(end, enterAt[i] + BOARD_ENTER_SEC + (newcomer[i] ? BOARD_GLOW_FADE_SEC + 0.3f : 0f));
            }
            if (mineFromScore >= 0) {
                end = Math.max(end, BOARD_COUNT_DELAY + mineCountSec + Math.max(BOARD_POP_SEC, BOARD_GLOW_FADE_SEC));
                end = Math.max(end, BOARD_COUNT_DELAY + BOARD_GAIN_SEC);
            }
            if (mineFromRank > 0) end = Math.max(end, Math.max(BOARD_SLIDE_SEC * 1.3f, BOARD_ARROW_SEC));
            length = end;
        }

        boolean exciting() {
            for (int i = 0; i < from.length; i++) if (!Float.isNaN(from[i]) || fromScore[i] >= 0 || newcomer[i]) return true;
            return mineFromScore >= 0 || mineFromRank > 0;
        }

        static float clamp(float value) {
            return Math.max(0f, Math.min(1f, value));
        }

        static float backOut(float t) {
            float s = 1.25f;
            float x = t - 1f;
            return 1f + (s + 1f) * x * x * x + s * x * x;
        }

        float slide(int row) {
            return Float.isNaN(from[row]) ? 1f : clamp(age / BOARD_SLIDE_SEC);
        }

        float rowAt(int row) {
            if (Float.isNaN(from[row])) return row;
            float p = slide(row);
            if (p >= 1f) return row;
            return from[row] + (row - from[row]) * backOut(p);
        }

        boolean moving(int row) {
            return !Float.isNaN(from[row]) && slide(row) < 1f;
        }

        int layer(int row) {
            if (moving(row)) return from[row] > row ? 2 : 1;
            return enterAt[row] >= 0f && age < enterAt[row] + BOARD_ENTER_SEC ? 1 : 0;
        }

        float lift(int row) {
            if (!moving(row)) return 0f;
            return (float) Math.sin(Math.PI * Math.min(1f, slide(row) * 1.15f));
        }

        static int counted(int start, int end, float age, float seconds) {
            if (start < 0 || end <= start) return end;
            float t = clamp((age - BOARD_COUNT_DELAY) / Math.max(0.01f, seconds));
            if (t >= 1f) return end;
            float eased = 1f - (1f - t) * (1f - t) * (1f - t) * (1f - t);
            return start + (int) Math.floor((end - start) * eased);
        }

        int scoreAt(int row) {
            return counted(fromScore[row], list.get(row).score, age, countSec[row]);
        }

        int mineScoreAt() {
            return counted(mineFromScore, mineScore, age, mineCountSec);
        }

        int mineRankAt() {
            if (mineFromRank <= 0) return 0;
            float t = clamp(age / (BOARD_SLIDE_SEC * 1.3f));
            float eased = 1f - (1f - t) * (1f - t) * (1f - t);
            return Math.round(mineFromRank + (mineToRank - mineFromRank) * eased);
        }

        boolean counting(int row) {
            return fromScore[row] >= 0 && age < BOARD_COUNT_DELAY + countSec[row];
        }

        static float popAt(float age, float end) {
            float q = (age - end) / BOARD_POP_SEC;
            return q <= 0f || q >= 1f ? 0f : (float) Math.sin(Math.PI * q);
        }

        static float glowAt(float age, float start, float hold) {
            if (age < start) return 0f;
            float rise = clamp((age - start) / 0.12f);
            float fall = clamp((age - hold) / BOARD_GLOW_FADE_SEC);
            return rise * (1f - fall * fall);
        }

        float pop(int row) {
            return fromScore[row] < 0 ? 0f : popAt(age, BOARD_COUNT_DELAY + countSec[row]);
        }

        float glow(int row) {
            if (fromScore[row] >= 0) return glowAt(age, 0f, BOARD_COUNT_DELAY + countSec[row]);
            if (newcomer[row]) return glowAt(age, enterAt[row], enterAt[row] + BOARD_ENTER_SEC + 0.3f);
            return 0f;
        }

        float hot(int row) {
            if (fromScore[row] < 0) return 0f;
            float end = BOARD_COUNT_DELAY + countSec[row];
            if (age < end) return age >= 0f ? 1f : 0f;
            return 1f - clamp((age - end) / 0.45f);
        }

        float gainAge(int row) {
            if (fromScore[row] < 0 || age < BOARD_COUNT_DELAY) return -1f;
            float t = (age - BOARD_COUNT_DELAY) / BOARD_GAIN_SEC;
            return t >= 1f ? -1f : t;
        }

        int arrow(int row) {
            if (Float.isNaN(from[row])) return 0;
            return from[row] > row ? 1 : -1;
        }

        float arrowAlpha(boolean present) {
            if (!present || age <= 0f) return 0f;
            float in = clamp(age / 0.15f);
            float out = clamp((BOARD_ARROW_SEC - age) / 0.5f);
            return in * out;
        }

        float enter(int row) {
            if (enterAt[row] < 0f) return 1f;
            float t = clamp((age - enterAt[row]) / BOARD_ENTER_SEC);
            return 1f - (1f - t) * (1f - t) * (1f - t);
        }

        long tickSum() {
            long sum = 0L;
            for (int i = 0; i < from.length; i++) {
                if (fromScore[i] >= 0) sum = sum * 31L + scoreAt(i);
                if (!Float.isNaN(from[i])) sum = sum * 31L + Math.round(rowAt(i));
            }
            if (mineFromScore >= 0) sum = sum * 31L + mineScoreAt();
            if (mineFromRank > 0) sum = sum * 31L + mineRankAt();
            return sum;
        }

        float progress() {
            float longest = 0f;
            for (int i = 0; i < countSec.length; i++) if (fromScore[i] >= 0) longest = Math.max(longest, countSec[i]);
            if (mineFromScore >= 0) longest = Math.max(longest, mineCountSec);
            if (longest <= 0f) return clamp(age / BOARD_SLIDE_SEC);
            return clamp((age - BOARD_COUNT_DELAY) / longest);
        }

        boolean settledAll() {
            for (int i = 0; i < from.length; i++) {
                if (counting(i) || moving(i)) return false;
            }
            return !(mineFromScore >= 0 && age < BOARD_COUNT_DELAY + mineCountSec) && !(mineFromRank > 0 && age < BOARD_SLIDE_SEC * 1.3f);
        }
    }

    static final class Film {
        long savedAt;
        int score, bestBefore, flaps, mode;
        float seconds;
        String version = "";
        int format, rules, fairMode, width, height, insetTop, insetBottom, insetLeft, insetRight, pipeGap, pipeSpacing, birdFrame;
        long seed, randomSeed;
        float scale, pipeSpeed, gravity, flapVelocity, birdX, baseBirdX, birdY, birdVelocityY, birdRotation, birdTiltVelocity;
        float speedVariation, backgroundX, groundX, dayNight, rainbowHue, drunkPhase, pipeAnimation;
        double clock, lastFlapTime;
        float[] pipeX = new float[0];
        int[] pipeTop = new int[0], pipeWidth = new int[0], pipeHeight = new int[0], pipeColor = new int[0];
        boolean[] pipeScored = new boolean[0], pipeGolden = new boolean[0];
        boolean night;
        int birdColor, lives;
        byte[] visuals = new byte[0];
        Map<String, Object> prefs = new TreeMap<>();
        int[] events = new int[0];

        boolean complete;

        static Film read(File file) throws IOException {
            return read(file, true);
        }

        static Film readInfo(File file) throws IOException {
            return read(file, false);
        }

        static Film read(File file, boolean withEvents) throws IOException {
            try (DataInputStream in = new DataInputStream(new java.io.BufferedInputStream(new InflaterInputStream(new FileInputStream(file)), 16384))) {
                Film film = new Film();
                if (in.readInt() != FILM_FORMAT) throw new IOException("Unknown replay format");
                film.savedAt = in.readLong();
                film.score = in.readInt();
                film.bestBefore = in.readInt();
                film.flaps = in.readInt();
                film.seconds = in.readFloat();
                film.mode = in.readInt();
                film.version = in.readUTF();
                byte[] header = new byte[checkedLength(in.readInt(), 1 << 20)];
                in.readFully(header);
                byte[] extras = new byte[checkedLength(in.readInt(), 1 << 16)];
                in.readFully(extras);
                int count = checkedLength(in.readInt(), REPLAY_MAX_EVENTS);
                if (withEvents) {
                    film.events = new int[count];
                    byte[] block = new byte[4 * Math.min(count, 16384)];
                    for (int from = 0; from < count; from += 16384) {
                        int n = Math.min(16384, count - from);
                        in.readFully(block, 0, n * 4);
                        java.nio.ByteBuffer.wrap(block, 0, n * 4).asIntBuffer().get(film.events, from, n);
                    }
                    film.complete = true;
                }
                film.readHeader(header);
                film.readExtras(extras);
                return film;
            } catch (RuntimeException e) {
                throw new IOException("Replay is damaged", e);
            }
        }

        private static int checkedLength(int length, int max) throws IOException {
            if (length < 0 || length > max) throw new IOException("Replay is damaged");
            return length;
        }

        private void readHeader(byte[] header) throws IOException {
            DataInputStream in = new DataInputStream(new ByteArrayInputStream(header));
            format = in.readInt();
            rules = in.readInt();
            fairMode = in.readInt();
            seed = in.readLong();
            width = in.readInt();
            height = in.readInt();
            insetTop = in.readInt();
            insetBottom = in.readInt();
            insetLeft = in.readInt();
            insetRight = in.readInt();
            scale = in.readFloat();
            pipeGap = in.readInt();
            pipeSpacing = in.readInt();
            pipeSpeed = in.readFloat();
            gravity = in.readFloat();
            flapVelocity = in.readFloat();
            clock = in.readDouble();
            lastFlapTime = in.readDouble();
            birdX = in.readFloat();
            baseBirdX = in.readFloat();
            birdY = in.readFloat();
            birdVelocityY = in.readFloat();
            birdRotation = in.readFloat();
            birdTiltVelocity = in.readFloat();
            speedVariation = in.readFloat();
            backgroundX = in.readFloat();
            groundX = in.readFloat();
            birdFrame = in.readInt();
            int count = checkedLength(in.readInt(), 64);
            pipeX = new float[count];
            pipeTop = new int[count];
            pipeWidth = new int[count];
            pipeHeight = new int[count];
            pipeScored = new boolean[count];
            pipeGolden = new boolean[count];
            for (int i = 0; i < count; i++) {
                pipeX[i] = in.readFloat();
                pipeTop[i] = in.readInt();
                pipeWidth[i] = in.readInt();
                pipeHeight[i] = in.readInt();
                pipeScored[i] = in.readBoolean();
                pipeGolden[i] = in.readBoolean();
            }
            int prefCount = checkedLength(in.readInt(), 4096);
            for (int i = 0; i < prefCount; i++) {
                String key = in.readUTF();
                int type = in.readByte();
                if (type == 'I') prefs.put(key, in.readInt());
                else if (type == 'F') prefs.put(key, in.readFloat());
                else if (type == 'Z') prefs.put(key, in.readBoolean());
                else if (type == 'J') prefs.put(key, in.readLong());
                else prefs.put(key, in.readUTF());
            }
            if (width <= 0 || height <= 0 || width > 20000 || height > 20000) throw new IOException("Replay is damaged");
        }

        private void readExtras(byte[] extras) throws IOException {
            DataInputStream in = new DataInputStream(new ByteArrayInputStream(extras));
            randomSeed = in.readLong();
            birdColor = in.readInt();
            night = in.readBoolean();
            dayNight = in.readFloat();
            rainbowHue = in.readFloat();
            drunkPhase = in.readFloat();
            lives = in.readInt();
            pipeAnimation = in.readFloat();
            int count = checkedLength(in.readInt(), 64);
            pipeColor = new int[count];
            for (int i = 0; i < count; i++) pipeColor[i] = in.readInt();
            visuals = new byte[in.available()];
            in.readFully(visuals);
        }

        boolean classic() {
            return mode == FAIR_CLASSIC;
        }
    }

    static Map<String, Object> filmPlaybackPrefs(Film film, boolean sound, boolean feel) {
        Map<String, Object> values = new TreeMap<>(film.prefs);
        values.put(PREF_BOARD_AUTO_POST, false);
        values.put(PREF_ACTIVITY_LOG_ENABLED, false);
        values.put(PREF_SHOW_FPS, false);
        values.put(PREF_HAPTIC_FEEDBACK_ENABLED, feel);
        values.put(PREF_REPLAY_ENABLED, false);
        values.put(PREF_SOUND_ENABLED, sound);
        values.put(PREF_HIGH_SCORE, film.bestBefore);
        return values;
    }

    void filmLoad(Film loaded) {
        film = loaded;
        windowInsetTop = loaded.insetTop;
        windowInsetBottom = loaded.insetBottom;
        windowInsetLeft = loaded.insetLeft;
        windowInsetRight = loaded.insetRight;
        if (screenWidth == loaded.width && screenHeight == loaded.height) applyLayout();
        filmRestart();
    }

    boolean filmReady() {
        return film != null && isReady && screenWidth == film.width && screenHeight == film.height;
    }

    void filmRestart() {
        Film f = film;
        if (f == null || !isReady) return;
        resetGame();
        removeCallbacks(playDieSound);
        gameState = GameState.WAITING;
        isFadingOut = false;
        transitionAlpha = 0;
        isPaused = false;
        clock = f.clock;
        gameOverTime = 0;
        lastFlapTime = f.lastFlapTime;
        speedVariation = f.speedVariation;
        applyTuning();
        birdX = f.birdX;
        baseBirdX = f.baseBirdX;
        birdY = f.birdY;
        birdVelocityY = f.birdVelocityY;
        birdRotation = f.birdRotation;
        birdTiltVelocity = f.birdTiltVelocity;
        backgroundX = f.backgroundX;
        groundX = f.groundX;
        birdFrame = Math.max(0, Math.min(2, f.birdFrame));
        spawnPipes(f.seed);
        while (pipes.size() > f.pipeX.length && !pipes.isEmpty()) pipes.remove(pipes.size() - 1);
        while (pipes.size() < f.pipeX.length) pipes.add(createPipe(0f));
        for (int i = 0; i < f.pipeX.length; i++) {
            Pipe pipe = pipes.get(i);
            pipe.x = f.pipeX[i];
            pipe.topPipeY = f.pipeTop[i];
            pipe.width = f.pipeWidth[i];
            pipe.height = f.pipeHeight[i];
            pipe.isScored = f.pipeScored[i];
            pipe.isGolden = f.pipeGolden[i];
            paintPipe(pipe, i < f.pipeColor.length ? f.pipeColor[i] : settingPipeColor);
        }
        currentBirdColor = Math.max(0, Math.min(2, f.birdColor));
        assignBirdBitmaps();
        nightBackground = f.night;
        dayNightCycleProgress = f.dayNight;
        rainbowHue = f.rainbowHue;
        drunkModePhase = f.drunkPhase;
        livesLeft = f.lives;
        pipeAnimationCounter = f.pipeAnimation;
        highScore = f.bestBefore;
        restoreVisuals(f.visuals);
        isScreenPressed = false;
        touchHeld = false;
        flapKeyHeld = false;
        flapAltKeyHeld = false;
        padKeyHeld = false;
        stepAccumulator = 0f;
        motionValid = false;
        shakeTimer = 0f;
        flashAlpha = 0f;
        filmIndex = 0;
        filmLastBits = 0;
        filmLeadLeft = FILM_LEAD_SEC;
        filmTail = 0f;
        filmDieIn = -1f;
        filmTime = 0.0;
        updateWeatherSounds();
        invalidate();
    }

    private void restoreVisuals(byte[] visuals) {
        trailStart = 0;
        trailCount = 0;
        if (visuals == null || visuals.length == 0) return;
        try {
            DataInputStream in = new DataInputStream(new ByteArrayInputStream(visuals));
            goldenShineOffset = in.readFloat();
            int count = Math.max(0, Math.min(MAX_TRAIL_PARTICLES, in.readInt()));
            for (int i = 0; i < count; i++) birdTrail[i].load(in);
            trailCount = count;
            weatherSystem.load(in);
        } catch (IOException | RuntimeException ignored) {
        }
    }

    void filmRelease() {
        removeCallbacks(playDieSound);
        releaseSounds();
    }

    void filmSetFeel(boolean feel) {
        filmFeel = feel;
    }

    void filmSetAudible(boolean audible) {
        filmAudible = audible;
        if (!audible) {
            try {
                setLoopPlaying(rainSoundPlayer, false);
                setLoopPlaying(stormSoundPlayer, false);
            } catch (IllegalStateException ignored) {
            }
        }
        updateWeatherSounds();
    }

    void filmSetSink(FilmSink sink) {
        filmSink = sink;
    }

    double filmTime() {
        return filmTime;
    }

    int filmScore() {
        return score;
    }

    float filmStep() {
        Film f = film;
        if (f == null || !isReady) return 0f;
        if (filmLeadLeft > 0f) {
            float dt = Math.min(FILM_TAIL_STEP_SEC, filmLeadLeft);
            filmLeadLeft -= dt;
            filmTime += dt;
            return dt;
        }
        int[] events = f.events;
        while (filmIndex < events.length) {
            int value = events[filmIndex++];
            if (value == REPLAY_FLAP) {
                handleGameplayTap();
                continue;
            }
            if ((value & REPLAY_MARK) != 0) {
                int kind = value & ~REPLAY_MARK;
                if (kind == MARK_HOLD_ON) isScreenPressed = true;
                else if (kind == MARK_HOLD_OFF) isScreenPressed = false;
                else if (kind == MARK_START) handleGameplayTap();
                continue;
            }
            int bits = value ^ filmLastBits;
            filmLastBits = bits;
            float dt = Float.intBitsToFloat(bits);
            if (!(dt > 0f) || dt > 1f) continue;
            filmAdvance(dt);
            return dt;
        }
        if (filmFinished()) return 0f;
        filmTail += FILM_TAIL_STEP_SEC;
        filmAdvance(FILM_TAIL_STEP_SEC);
        return FILM_TAIL_STEP_SEC;
    }

    private void filmAdvance(float dt) {
        update(dt);
        filmTime += dt;
        if (filmDieIn >= 0f) {
            filmDieIn -= dt;
            if (filmDieIn < 0f) playSound(soundDie);
        }
    }

    private boolean filmFinished() {
        if (film == null || filmIndex < film.events.length) return false;
        if (filmTail >= FILM_TAIL_MAX_SEC) return true;
        return gameState == GameState.PANEL_SLIDING && panelFinishedSlidingTime >= 0 && clock - panelFinishedSlidingTime >= FILM_AFTER_PANEL_SEC;
    }

    boolean filmEnded() {
        return film != null && filmIndex >= film.events.length && filmFinished();
    }

    double filmSeek(double target) {
        FilmSink sink = filmSink;
        boolean audible = filmAudible;
        boolean feel = filmFeel;
        filmSink = null;
        filmAudible = false;
        filmFeel = false;
        try {
            if (target < filmTime) filmRestart();
            while (filmTime < target) {
                if (filmStep() <= 0f) break;
            }
        } finally {
            filmSink = sink;
            filmAudible = audible;
            filmFeel = feel;
        }
        updateWeatherSounds();
        invalidate();
        return filmTime;
    }

    double filmLength() {
        double length = filmSeek(Double.MAX_VALUE);
        FilmSink sink = filmSink;
        filmSink = null;
        try {
            filmRestart();
        } finally {
            filmSink = sink;
        }
        return length;
    }

    void filmDraw(Canvas canvas) {
        if (!isReady) return;
        drawFrame(canvas);
    }

    private Leaderboard.Replay takeReplay() {
        boolean usable = replayLive && replayForBoard && replayHeader != null && replayEvents != null && replayCount > 0
                && replayPrefs != null && replayPrefs.equals(replayPrefs(playPrefs()));
        Leaderboard.Replay replay = usable ? new Leaderboard.Replay(replayHeader, replayEvents, replayCount, replaySeed, REPLAY_RULES, replayMarks) : null;
        replayLive = false;
        replayHeader = null;
        replayEvents = null;
        replayPrefs = null;
        return replay;
    }

    private void trackPause() {
        boolean inactive = gameState == GameState.PLAYING && (isPaused || !isRunning);
        long now = SystemClock.elapsedRealtime();
        if (inactive && pausedSince < 0L) {
            pausedSince = now;
        } else if (!inactive && pausedSince >= 0L) {
            roundPausedMs += Math.max(0L, now - pausedSince);
            pausedSince = -1L;
        }
    }

    private double pausedSeconds() {
        long open = pausedSince >= 0L ? Math.max(0L, SystemClock.elapsedRealtime() - pausedSince) : 0L;
        return (roundPausedMs + open) / 1000.0;
    }

    private String clockProblem() {
        double real = (SystemClock.elapsedRealtime() - roundRealStart) / 1000.0;
        double activeReal = Math.max(0.0, real - pausedSeconds());
        double uptime = (SystemClock.uptimeMillis() - roundUptimeStart) / 1000.0;
        double game = Math.max(0.0, clock - roundStartClock);
        if (uptime > real + 1.0 + real * 0.03) return "the device clocks disagree";
        if (roundActiveSeconds > real + 1.0 + real * 0.03) return "the device clocks disagree";
        if (game > roundActiveSeconds * 1.05 + 1.0) return "game time ran faster than real time";
        if (roundActiveSeconds > 5.0 && game < roundActiveSeconds * 0.55) return "the game ran too slowly for a fair round";
        if (activeReal > 5.0 && activeReal > game * 1.6 + 3.0) return "the game ran slower than real time";
        return null;
    }

    private String judgeRound(String assists, Leaderboard.Replay replay) {
        if (!Leaderboard.configured()) return "leaderboard not set up";
        checkSeals();
        String reason = null;
        boolean classic = roundFairMode == FAIR_CLASSIC;
        float secondsPerPoint = classic ? Leaderboard.CLASSIC_SECONDS_PER_POINT : Leaderboard.STANDARD_SECONDS_PER_POINT;
        double seconds = Math.max(0.0, clock - roundStartClock);
        String clocks = clockProblem();
        if (roundFairMode == FAIR_NONE || roundFairMode != currentFairMode) {
            reason = fairBlocker != null ? fairKeyName(fairBlocker) + " changed" : "settings changed during the round";
        } else if (!assists.isEmpty() || roundLivesUsed > 0 || roundGoldenPoints > 0) {
            reason = "assists were used";
        } else if (roundTampered) {
            reason = "game values were changed while playing";
        } else if (score != roundPipes) {
            reason = "the score did not match the pipes passed";
        } else if (score > Leaderboard.MAX_SCORE) {
            reason = "the score is above the leaderboard limit";
        } else if (seconds < score * (double) secondsPerPoint) {
            reason = "the score was reached faster than the game allows";
        } else if (roundFlaps < score) {
            reason = "too few flaps for the score";
        } else if (clocks != null) {
            reason = clocks;
        }
        if (reason != null) {
            if (roundTampered || clocks != null || seconds < score * (double) secondsPerPoint || score != roundPipes) {
                SettingsActivity.ActivityLog.add(getContext(), "BOARD", "Round of " + score + " rejected for the leaderboard: " + reason);
            }
            boardStatusText = !settingBoardAutoPost ? "Posting is off" : "Not posted: " + reason;
            if (challengeTwist > Challenges.ANY && roundFairMode == FAIR_NONE) boardStatusText = Challenges.TWIST_NAMES[challengeTwist] + " challenge round";
            return "not eligible for the leaderboard (" + reason + ")";
        }
        int whole = (int) Math.ceil(seconds);
        int paused = (int) Math.ceil(pausedSeconds());
        if (Leaderboard.offer(prefs, score, classic, whole, roundFlaps)) {
            SettingsActivity.ActivityLog.add(getContext(), "BOARD", "New fair best " + score + (classic ? " (Classic)" : ""));
        }
        boardStatusText = null;
        if (!settingBoardAutoPost) {
            boardStatusText = "Posting is off";
        } else if (Leaderboard.name(prefs).isEmpty() || Leaderboard.nameTaken(prefs)) {
            boardStatusText = "Choose a leaderboard name to post scores";
        } else if (score < BOARD_MIN_SCORE) {
            boardStatusText = "Not posted: score below " + BOARD_MIN_SCORE;
            return "below the leaderboard minimum of " + BOARD_MIN_SCORE;
        } else if (score > 0 && (score > Leaderboard.posted(prefs) || score > Leaderboard.weekPosted(prefs)
                || (replay != null && Leaderboard.wantsReplay(prefs, score)))) {
            final boolean allTime = score > Leaderboard.posted(prefs);
            final boolean newBest = allTime || score > Leaderboard.weekPosted(prefs);
            boardStatusText = newBest ? "Posting..." : "Sending for verification...";
            Leaderboard.submit(getContext(), score, classic, whole, roundFlaps, paused, replay, this::post, (status, error) -> {
                if (Leaderboard.VERIFYING.equals(status)) {
                    watchVerdict();
                    boardStatusText = newBest ? "Posted! Verifying your run..." : "Verifying your run...";
                    boardStaleMask = 0xF;
                    if (boardOpen) refreshBoard();
                    else if (settingBoardOpenOnBest && newBest) postDelayed(this::openBoardAfterBest, 900);
                } else if (Leaderboard.POSTED.equals(status)) {
                    boardStatusText = allTime ? "Posted! New all-time best" : "Posted! New weekly best";
                    boardStaleMask = 0xF;
                    if (boardOpen) refreshBoard();
                    else if (settingBoardOpenOnBest) postDelayed(this::openBoardAfterBest, 900);
                } else if (error != null) {
                    boardStatusText = "Not posted: " + error.substring(0, 1).toLowerCase(Locale.US) + error.substring(1);
                } else {
                    boardStatusText = null;
                }
            });
        }
        return classic ? "eligible for the leaderboard (Classic)" : "eligible for the leaderboard";
    }

    void setBoardHost(BoardHost host) {
        boardHost = host;
    }

    void askFirstName() {
        if (boardHost == null || nameOverlayOpen) return;
        if (!Leaderboard.configured()) {
            if (!filmMode && highScore == 0 && !prefs.getBoolean(KEY_PHYSICS_CHOSEN, false)) boardHost.askPhysics();
            return;
        }
        boolean taken = Leaderboard.nameTaken(prefs);
        if (!taken && (Leaderboard.asked(prefs) || !Leaderboard.name(prefs).isEmpty())) return;
        final boolean fresh = !taken && Leaderboard.name(prefs).isEmpty() && !prefs.getBoolean(KEY_PHYSICS_CHOSEN, false);
        boardHost.askName(taken && !Leaderboard.nameRefused(prefs) ? Leaderboard.name(prefs) : "", true, name -> {
            boardRefreshPending = true;
            if (fresh && boardHost != null) postDelayed(() -> {
                if (boardHost != null && !prefs.getBoolean(KEY_PHYSICS_CHOSEN, false)) boardHost.askPhysics();
            }, 220);
        });
    }

    void physicsChosen(boolean classic) {
        if (gameState == GameState.HOME) resetGame();
        else loadSettings();
        SettingsActivity.ActivityLog.add(getContext(), "SETTINGS", classic ? "Chose Classic physics" : "Chose Default physics");
        invalidate();
    }

    private void openBoardAfterBest() {
        if (gameState == GameState.PANEL_SLIDING && !boardOpen && !nameOverlayOpen) openBoard();
    }

    void setNameOverlay(boolean open) {
        nameOverlayOpen = open;
        if (open) chooseNameSide();
        else postDelayed(this::syncSizeAfterKeyboard, 450);
        boardPressed = null;
        boardDragArmed = false;
        boardDragging = false;
        invalidate();
    }

    boolean nameBeside() {
        return nameSide && boardOpen;
    }

    private void chooseNameSide() {
        float usableWidth = screenWidth - safeInsetLeft - safeInsetRight;
        sideFit = usableWidth / (BOARD_PAIR_UNITS * baseBoardUnit());
        nameSide = boardOpen && usableWidth > getPlayableHeight() && sideFit >= BOARD_SIDE_MIN_FIT;
    }

    private void syncSizeAfterKeyboard() {
        if (nameOverlayOpen || !isReady || getWidth() <= 0 || getHeight() <= 0) return;
        if (getWidth() == screenWidth && getHeight() == screenHeight) return;
        keyboardCut = 0f;
        screenWidth = getWidth();
        screenHeight = getHeight();
        applyLayout();
    }

    private static void settle(float[] spring, float target, float dt, float omega, float snap) {
        float offset = spring[0] - target;
        float carry = spring[1] + omega * offset;
        float decay = (float) Math.exp(-omega * dt);
        spring[0] = target + (offset + carry * dt) * decay;
        spring[1] = (carry - omega * (offset + carry * dt)) * decay;
        if (Math.abs(spring[0] - target) < snap && Math.abs(spring[1]) < snap * omega) {
            spring[0] = target;
            spring[1] = 0f;
        }
    }

    private void releaseBoardCache() {
        if (boardCache != null) {
            boardCache.recycle();
            boardCache = null;
            boardCacheCanvas = null;
        }
        boardCacheKey = 0L;
        boardCached = false;
    }

    private void layoutBoard() {
        float usableWidth = screenWidth - safeInsetLeft - safeInsetRight;
        float playable = getPlayableHeight();
        float base = baseBoardUnit();
        float shown = nameSpring[0];
        float hide = hideSpring[0];
        float keyboardTarget = Math.max(imeInset, keyboardCut);
        float floor = systemBarTop + playable;
        float bottomNow = Math.min(floor, screenHeight - keyboardSpring[0]);
        float bottomTarget = Math.min(floor, screenHeight - keyboardTarget);
        boolean showing = boardHost != null && (nameOverlayOpen || boardHost.namePanelShowing());
        if (nameOverlayOpen) chooseNameSide();
        float pairScale = nameSide ? Math.min(1f, sideFit) : 1f;
        float panelUnit = base * 0.95f * pairScale;
        float panelHeight = 0f;
        if (showing) {
            panelHeight = boardHost.namePanelHeight(panelUnit);
            float room = bottomTarget - systemBarTop - 8f * panelUnit;
            if (panelHeight > room && room > 0f && panelHeight > 0f) {
                panelUnit *= room / panelHeight;
                panelHeight = boardHost.namePanelHeight(panelUnit);
            }
            if (nameOverlayOpen) {
                sidePanelWidth = NamePanel.WIDTH_UNITS * panelUnit;
                if (boardOpen && !nameSide) boardReserve = panelHeight + 10f * panelUnit;
            }
        }
        float unit, left, top;
        if (nameSide) {
            unit = base * (1f + (pairScale - 1f) * shown);
            float width = BOARD_WIDTH_UNITS * unit;
            float centered = safeInsetLeft + (usableWidth - width) / 2f;
            float paired = safeInsetLeft + (usableWidth - (width + BOARD_SIDE_GAP * unit + sidePanelWidth)) / 2f;
            left = centered + (paired - centered) * shown;
            top = systemBarTop + (playable - BOARD_HEIGHT_UNITS * unit) / 2f;
            boardSqueezed = false;
        } else {
            float limit = Math.min(scale, usableWidth / 252f);
            float area = Math.max(0f, playable - shown * (boardReserve + floor - bottomNow));
            unit = Math.max(0.01f, Math.min(limit, area / 360f));
            float targetArea = Math.max(0f, playable - boardReserve - (floor - bottomTarget));
            boardSqueezed = nameOverlayOpen && boardOpen && Math.min(limit, targetArea / 360f) < base * 0.45f;
            left = safeInsetLeft + (usableWidth - BOARD_WIDTH_UNITS * unit) / 2f;
            top = systemBarTop + (area - BOARD_HEIGHT_UNITS * unit) / 2f;
        }
        boardUnit = unit;
        boardLeft = left;
        boardTop = top;
        boardPanel.set(left, top, left + BOARD_WIDTH_UNITS * unit, top + BOARD_HEIGHT_UNITS * unit);
        if (!showing) return;
        float panelWidth = NamePanel.WIDTH_UNITS * panelUnit;
        float middle = systemBarTop + (bottomNow - systemBarTop - panelHeight) / 2f;
        float panelLeft = safeInsetLeft + (usableWidth - panelWidth) / 2f;
        float panelTop = middle;
        if (boardOpen && nameSide) {
            panelLeft = boardPanel.right + BOARD_SIDE_GAP * unit;
        } else if (boardOpen) {
            float under = boardPanel.bottom + 6f * panelUnit;
            panelTop = under + (middle - under) * hide;
        }
        panelTop = Math.max(systemBarTop, Math.min(panelTop, bottomNow - panelHeight - 4f * panelUnit));
        boardHost.placeNamePanel(Math.round(panelLeft), Math.round(panelTop), panelUnit);
    }

    private float baseBoardUnit() {
        float usableWidth = screenWidth - safeInsetLeft - safeInsetRight;
        return Math.max(0.01f, Math.min(scale, Math.min(usableWidth / 252f, getPlayableHeight() / 360f)));
    }

    private void drawNameBackdrop(Canvas canvas) {
        if (boardPaint == null) boardPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        int alpha = Math.round(Color.alpha(BOARD_DIM) * Math.max(0f, Math.min(1f, dimSpring[0])));
        boardPaint.setColor((BOARD_DIM & 0x00FFFFFF) | (alpha << 24));
        canvas.drawRect(0, 0, screenWidth, screenHeight, boardPaint);
        layoutBoard();
    }

    private boolean boardStatusPlace() {
        return boardStatusText != null && settingBoardShowStatus && gameState == GameState.PANEL_SLIDING && !playButtonRect.isEmpty();
    }

    private void animateBoardStatus(float step) {
        if (boardStatusText != boardStatusSeen) {
            boardStatusSeen = boardStatusText;
            boardStatusAge = boardStatusAlpha > 0f ? STATUS_FADE_IN_SEC : 0f;
        }
        if (!boardStatusPlace()) {
            boardStatusAlpha = 0f;
            return;
        }
        boardStatusAge += step;
        float rise = Math.min(1f, boardStatusAge / STATUS_FADE_IN_SEC);
        float fall = Math.max(0f, Math.min(1f, (boardStatusAge - STATUS_FADE_IN_SEC - STATUS_HOLD_SEC) / STATUS_FADE_OUT_SEC));
        rise = rise * rise * (3f - 2f * rise);
        fall = fall * fall * (3f - 2f * fall);
        boardStatusAlpha = rise * (1f - fall);
    }

    private void drawBoardStatus(Canvas canvas) {
        if (boardStatusPaint == null) {
            boardStatusPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            Typeface face = null;
            try {
                face = ResourcesCompat.getFont(getContext(), R.font.flappy_font);
            } catch (RuntimeException ignored) {
            }
            boardStatusPaint.setTypeface(face != null ? face : Typeface.DEFAULT_BOLD);
            boardStatusPaint.setTextAlign(Paint.Align.CENTER);
            boardStatusPaint.setStyle(Paint.Style.FILL);
        }
        float alpha = Math.max(0f, Math.min(1f, boardStatusAlpha));
        float size = 7f * scale;
        float padX = 7f * scale;
        float usable = screenWidth - safeInsetLeft - safeInsetRight;
        float maxWidth = usable - 12f * scale - padX * 2f;
        boardStatusPaint.setTextSize(size);
        float width = boardStatusPaint.measureText(boardStatusText);
        if (width > maxWidth && maxWidth > 0f) {
            size *= maxWidth / width;
            boardStatusPaint.setTextSize(size);
            width = maxWidth;
        }
        float height = size * 2.1f;
        float x = safeInsetLeft + usable / 2f;
        float y = playButtonRect.bottom + 13f * scale + (1f - alpha) * 3f * scale;
        boardStatusPill.set(x - width / 2f - padX, y - height / 2f, x + width / 2f + padX, y + height / 2f);
        boardStatusPaint.setColor(STATUS_PILL);
        boardStatusPaint.setAlpha(Math.round(Color.alpha(STATUS_PILL) * alpha));
        canvas.drawRoundRect(boardStatusPill, height / 2f, height / 2f, boardStatusPaint);
        boardStatusPaint.setColor(Color.WHITE);
        boardStatusPaint.setAlpha(Math.round(255f * alpha));
        canvas.drawText(boardStatusText, x, y + size * 0.37f, boardStatusPaint);
    }

    boolean closeBoardIfOpen() {
        if (coinOpen) {
            closeCoins();
            return true;
        }
        if (!boardOpen) return false;
        closeBoard();
        return true;
    }

    private void resetBoardScroll() {
        boardScroll = 0f;
        boardFling = 0f;
        boardScrollTarget = Float.NaN;
        boardDragArmed = false;
        boardDragging = false;
    }

    private void openBoard() {
        resetBoardScroll();
        boardOffline = false;
        boardRetrySec = 0f;
        boardVerifyPoll = 0f;
        boardTab = settingBoardTab;
        boardSwitchAge = BOARD_SWITCH_SEC;
        boardPillFrom = -1f;
        boardShowAll = Boolean.TRUE.equals(prefs.getAll().get(BOARD_SHOW_ALL_KEY));
        boardOpen = true;
        boardClosing = false;
        boardAnim = 0f;
        boardPressed = null;
        hideBoardBubble();
        if (boardHost != null) boardHost.boardVisible(true);
        showBoardView(false);
    }

    private int boardView() {
        return boardTab + (boardShowAll ? 0 : 2);
    }

    private void syncBoardLoading() {
        boardLoading = (boardLoadingMask & (1 << boardView())) != 0;
    }

    private void showBoardView(boolean force) {
        int view = boardView();
        String who = Leaderboard.uid(prefs);
        if (boardSizeLoaded != settingBoardSize || !who.equals(boardUidLoaded)) {
            boardSizeLoaded = settingBoardSize;
            boardUidLoaded = who;
            boardStaleMask = 0xF;
        }
        boolean stale = (boardStaleMask & (1 << view)) != 0;
        seedBoardView(view);
        syncBoardLoading();
        long now = SystemClock.uptimeMillis();
        boolean waited = awaitingVerdict() && now - boardData.loadedAt[view] >= BOARD_VERIFY_FRESH_MS;
        if (force || stale || waited || !boardData.fresh(view, now)) {
            loadBoardView(view);
        } else if (!boardLoading) {
            boardNote = boardHint();
        }
    }

    private void hideBoardBubble() {
        boardBubbleAge = 99f;
        boardBubbleIndex = -1;
        boardBubbleEntry = null;
        boardTapIndex = -1;
    }

    private void closeBoard() {
        if (!boardOpen || boardClosing) return;
        boardClosing = true;
        boardPressed = null;
    }

    private void finishClosingBoard() {
        int view = boardView();
        BoardMotion motion = boardMotions[view];
        if (motion != null && motion.age > 0f) settleBoardMotion(view);
        else if (motion == null) rememberBoardView(view);
        saveBoardMemory();
        boardOpen = false;
        boardClosing = false;
        boardAnim = 0f;
        releaseBoardCache();
        if (boardHost != null) boardHost.boardVisible(false);
    }

    private void animateBoard(float deltaTime) {
        float step = Math.min(0.1f, Math.max(0f, deltaTime));
        if (boardOpen) boardShine = (boardShine + step) % 600f;
        float glide = Math.min(step, 1f / 30f);
        animateBoardStatus(glide);
        settle(keyboardSpring, Math.max(imeInset, keyboardCut), glide, 18f, 0.5f);
        settle(nameSpring, nameOverlayOpen && boardOpen ? 1f : 0f, glide, 17f, 0.002f);
        settle(hideSpring, boardSqueezed ? 1f : 0f, glide, 20f, 0.004f);
        settle(dimSpring, nameOverlayOpen ? 1f : 0f, glide, 20f, 0.004f);
        if (nameSpring[0] == 0f && boardCache != null) releaseBoardCache();
        if (boardOpen && boardOffline && !boardLoading && !boardClosing && !nameOverlayOpen) {
            boardRetrySec += step;
            if (boardRetrySec >= BOARD_RETRY_SEC) refreshBoard();
        } else if (boardOpen && !boardLoading && !boardClosing && !nameOverlayOpen && awaitingVerdict()) {
            boardVerifyPoll += step;
            if (boardVerifyPoll >= BOARD_VERIFY_POLL_SEC) {
                boardVerifyPoll = 0f;
                refreshBoard();
            }
        }
        if (boardRefreshPending && !nameOverlayOpen && nameSpring[0] < 0.02f) {
            boardRefreshPending = false;
            boardStaleMask = 0xF;
            if (boardOpen) refreshBoard();
        }
        if (boardBubbleAge < BUBBLE_HOLD_SEC + BUBBLE_FADE_SEC) boardBubbleAge += step;
        if (boardSwitchAge < BOARD_SWITCH_SEC) boardSwitchAge += step;
        if (boardOpen || nameOverlayOpen || dimSpring[0] > 0f) layoutBoard();
        if (!boardOpen) return;
        if (!boardDragging) {
            if (!Float.isNaN(boardScrollTarget)) {
                boardScroll += (boardScrollTarget - boardScroll) * Math.min(1f, step * 12f);
                if (Math.abs(boardScrollTarget - boardScroll) < 0.5f) {
                    boardScroll = boardScrollTarget;
                    boardScrollTarget = Float.NaN;
                }
            } else if (boardFling != 0f) {
                boardScroll += boardFling * step;
                boardFling *= Math.max(0f, 1f - step * 4f);
                if (Math.abs(boardFling) < boardRowStep || boardScroll <= 0f || boardScroll >= boardScrollMax) boardFling = 0f;
            }
            boardScroll = Math.max(0f, Math.min(boardScrollMax, boardScroll));
        }
        animateBoardMotion(step);
        if (boardClosing) {
            boardAnim -= step / BOARD_CLOSE_SEC;
            if (boardAnim <= 0f) finishClosingBoard();
        } else if (boardAnim < 1f) {
            boardAnim = Math.min(1f, boardAnim + step / BOARD_OPEN_SEC);
        }
    }

    private static File boardMemoryFile(Context context) {
        Context app = context.getApplicationContext() != null ? context.getApplicationContext() : context;
        return new File(app.getFilesDir(), BOARD_MEMORY_FILE);
    }

    private void preloadBoardMemory() {
        synchronized (BOARD_MEMORY_LOCK) {
            if (boardMemory != null) return;
        }
        final File file;
        try {
            file = boardMemoryFile(getContext());
        } catch (RuntimeException e) {
            return;
        }
        filmSaver().execute(() -> {
            BoardMemory loaded = BoardMemory.read(file);
            synchronized (BOARD_MEMORY_LOCK) {
                if (boardMemory == null) boardMemory = loaded;
            }
        });
    }

    private BoardMemory boardMemory() {
        BoardMemory memory;
        synchronized (BOARD_MEMORY_LOCK) {
            if (boardMemory == null) {
                BoardMemory loaded;
                try {
                    loaded = BoardMemory.read(boardMemoryFile(getContext()));
                } catch (RuntimeException e) {
                    loaded = new BoardMemory();
                }
                boardMemory = loaded;
            }
            memory = boardMemory;
        }
        String url = String.valueOf(Leaderboard.DATABASE_URL);
        String uid = Leaderboard.uid(prefs);
        int week = Leaderboard.week(Leaderboard.now());
        if (!url.equals(memory.url)) {
            for (int view = 0; view < 4; view++) memory.forget(view);
            memory.url = url;
            memory.uid = uid;
            memory.week = week;
            boardMemoryDirty = true;
        }
        if (!uid.equals(memory.uid)) {
            for (int view = 0; view < 4; view++) {
                memory.mine[view] = null;
                memory.rank[view] = 0;
            }
            memory.uid = uid;
            boardMemoryDirty = true;
        }
        if (week != memory.week) {
            memory.forget(Leaderboard.VIEW_WEEKLY);
            memory.forget(Leaderboard.VIEW_VERIFIED_WEEKLY);
            memory.week = week;
            boardMemoryDirty = true;
        }
        return memory;
    }

    private void saveBoardMemory() {
        if (!boardMemoryDirty || filmMode) return;
        boardMemoryDirty = false;
        final byte[] bytes;
        final File file;
        try {
            bytes = boardMemory().bytes();
            file = boardMemoryFile(getContext());
        } catch (IOException | RuntimeException e) {
            return;
        }
        filmSaver().execute(() -> BoardMemory.write(file, bytes));
    }

    private static List<Leaderboard.Entry> boardList(Leaderboard.Board board, int view) {
        switch (view) {
            case Leaderboard.VIEW_ALL_TIME: return board.allTime;
            case Leaderboard.VIEW_WEEKLY: return board.weekly;
            case Leaderboard.VIEW_VERIFIED_ALL_TIME: return board.verifiedAllTime;
            default: return board.verifiedWeekly;
        }
    }

    private static Leaderboard.Entry boardMineOf(Leaderboard.Board board, int view) {
        switch (view) {
            case Leaderboard.VIEW_ALL_TIME: return board.mineAllTime;
            case Leaderboard.VIEW_WEEKLY: return board.mineWeekly;
            case Leaderboard.VIEW_VERIFIED_ALL_TIME: return board.mineVerifiedAllTime;
            default: return board.mineVerifiedWeekly;
        }
    }

    private static int boardRankOf(Leaderboard.Board board, int view) {
        switch (view) {
            case Leaderboard.VIEW_ALL_TIME: return board.rankAllTime;
            case Leaderboard.VIEW_WEEKLY: return board.rankWeekly;
            case Leaderboard.VIEW_VERIFIED_ALL_TIME: return board.rankVerifiedAllTime;
            default: return board.rankVerifiedWeekly;
        }
    }

    private void seedBoardView(int view) {
        if (boardData.loaded[view] || !boardList(boardData, view).isEmpty()) return;
        BoardMemory memory = boardMemory();
        List<Leaderboard.Entry> list = memory.list(view);
        if (list == null || list.isEmpty()) return;
        int limit = settingBoardSize;
        if (limit > 0 && list.size() > limit) {
            list = new ArrayList<>(list.subList(0, limit));
            memory.lists[view] = list;
        }
        Leaderboard.Entry mine = memory.mine[view];
        int rank = memory.rank[view];
        switch (view) {
            case Leaderboard.VIEW_ALL_TIME:
                boardData.allTime = list;
                boardData.mineAllTime = mine;
                boardData.rankAllTime = rank;
                break;
            case Leaderboard.VIEW_WEEKLY:
                boardData.weekly = list;
                boardData.mineWeekly = mine;
                boardData.rankWeekly = rank;
                break;
            case Leaderboard.VIEW_VERIFIED_ALL_TIME:
                boardData.verifiedAllTime = list;
                boardData.mineVerifiedAllTime = mine;
                boardData.rankVerifiedAllTime = rank;
                break;
            default:
                boardData.verifiedWeekly = list;
                boardData.mineVerifiedWeekly = mine;
                boardData.rankVerifiedWeekly = rank;
                break;
        }
        boardDataVersion++;
    }

    private void planBoardMotion(Leaderboard.Board board, int view) {
        List<Leaderboard.Entry> fresh = boardList(board, view);
        BoardMemory memory = boardMemory();
        BoardMotion old = boardMotions[view];
        if (old != null && old.age > 0f && old.list == boardList(boardData, view)) settleBoardMotion(view);
        boolean shownEmpty = boardList(boardData, view).isEmpty();
        boolean entrance = boardOpen && !boardClosing && view == boardView() && shownEmpty && !boardData.loaded[view];
        BoardMotion motion = entrance
                ? BoardMotion.plan(null, null, 0, fresh, null, 0, true, null)
                : BoardMotion.plan(memory.list(view), memory.mine[view], memory.rank[view], fresh, boardMineOf(board, view),
                        boardRankOf(board, view), false, Leaderboard.uid(prefs));
        boardMotions[view] = motion;
    }

    private void settleBoardMotion(int view) {
        BoardMotion motion = boardMotions[view];
        boardMotions[view] = null;
        if (motion == null || motion.list != boardList(boardData, view)) return;
        rememberBoardView(view);
    }

    private void rememberBoardView(int view) {
        if (!boardData.loaded[view]) return;
        List<Leaderboard.Entry> list = boardList(boardData, view);
        Leaderboard.Entry mine = boardMineOf(boardData, view);
        int rank = boardRankOf(boardData, view);
        BoardMemory known = boardMemory;
        if (known != null && known.list(view) == list && known.mine[view] == mine && known.rank[view] == rank) return;
        BoardMemory memory = boardMemory();
        if (memory.list(view) == list && memory.mine[view] == mine && memory.rank[view] == rank) return;
        memory.lists[view] = list;
        memory.mine[view] = mine;
        memory.rank[view] = rank;
        boardMemoryDirty = true;
    }

    private BoardMotion currentBoardMotion() {
        int view = boardView();
        BoardMotion motion = boardMotions[view];
        if (motion != null && motion.list != boardList(boardData, view)) {
            boardMotions[view] = null;
            return null;
        }
        return motion;
    }

    private void animateBoardMotion(float step) {
        if (!boardOpen) return;
        int view = boardView();
        BoardMotion motion = currentBoardMotion();
        if (motion == null) {
            if (!boardClosing && boardData.loaded[view] && boardMemory != null) rememberBoardView(view);
            return;
        }
        if (boardClosing || boardAnim < BOARD_MOTION_START) return;
        float before = motion.age;
        motion.age += step;
        if (!motion.entrance) feelBoardMotion(motion, before);
        if (motion.age >= motion.length) settleBoardMotion(view);
    }

    private void feelBoardMotion(BoardMotion motion, float before) {
        if (motion.age <= 0f) return;
        if (!motion.rose && before <= 0f) {
            motion.rose = true;
            if (motion.ownClimbed) boardHaptic(HAPTIC_RISE, 1f);
        }
        long sum = motion.tickSum();
        if (motion.tickSum == Long.MIN_VALUE) motion.tickSum = sum;
        long now = SystemClock.uptimeMillis();
        if (sum != motion.tickSum && now - boardTickAt >= BOARD_TICK_MS) {
            motion.tickSum = sum;
            boardTickAt = now;
            boardHaptic(HAPTIC_TICK, motion.progress());
            motion.felt = true;
        }
        if (!motion.landed && motion.settledAll()) {
            motion.landed = true;
            if (motion.felt || motion.exciting()) {
                boardTickAt = now;
                boardHaptic(HAPTIC_LAND, 1f);
            }
        }
    }

    private void boardHaptic(int kind, float strength) {
        if (!settingHapticFeedbackEnabled || filmMode || (feel != null && !feel.allows(Feel.TICK))) return;
        if (boardHapticMode < 0) boardHapticMode = pickBoardHaptics();
        if (boardHapticMode == 2 || (boardHapticMode == 1 && kind != HAPTIC_TICK)) {
            try {
                int level = Math.max(0, Math.min(3, (int) (strength * 4f)));
                vibrator.vibrate(boardEffect(kind, level));
                return;
            } catch (RuntimeException e) {
                boardHapticMode = 0;
            }
        }
        int feedback;
        if (kind == HAPTIC_TICK) {
            feedback = HapticFeedbackConstants.CLOCK_TICK;
        } else if (kind == HAPTIC_LAND && Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            feedback = HapticFeedbackConstants.CONFIRM;
        } else {
            feedback = HapticFeedbackConstants.VIRTUAL_KEY;
        }
        performHapticFeedback(feedback);
    }

    private int pickBoardHaptics() {
        if (vibrator == null) return 0;
        try {
            if (!vibrator.hasVibrator()) return 0;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                if (vibrator.areAllPrimitivesSupported(VibrationEffect.Composition.PRIMITIVE_TICK, VibrationEffect.Composition.PRIMITIVE_CLICK)) {
                    boardHapticRise = vibrator.areAllPrimitivesSupported(VibrationEffect.Composition.PRIMITIVE_QUICK_RISE);
                    return 2;
                }
            }
            return Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q ? 1 : 0;
        } catch (RuntimeException e) {
            return 0;
        }
    }

    private VibrationEffect boardEffect(int kind, int level) {
        if (boardHapticEffects == null) boardHapticEffects = new VibrationEffect[6];
        int slot = kind == HAPTIC_TICK ? level : 3 + kind;
        VibrationEffect effect = boardHapticEffects[slot];
        if (effect == null) {
            effect = boardHapticMode == 2 ? composedBoardEffect(kind, level, boardHapticRise) : predefinedBoardEffect(kind);
            boardHapticEffects[slot] = effect;
        }
        return effect;
    }

    private static VibrationEffect composedBoardEffect(int kind, int level, boolean rise) {
        if (kind == HAPTIC_TICK) {
            return VibrationEffect.startComposition()
                    .addPrimitive(VibrationEffect.Composition.PRIMITIVE_TICK, 0.38f + 0.16f * level)
                    .compose();
        }
        if (kind == HAPTIC_LAND) {
            return VibrationEffect.startComposition()
                    .addPrimitive(VibrationEffect.Composition.PRIMITIVE_CLICK, 0.9f)
                    .compose();
        }
        if (rise) {
            return VibrationEffect.startComposition()
                    .addPrimitive(VibrationEffect.Composition.PRIMITIVE_QUICK_RISE, 0.6f)
                    .addPrimitive(VibrationEffect.Composition.PRIMITIVE_CLICK, 1f, 25)
                    .compose();
        }
        return VibrationEffect.startComposition()
                .addPrimitive(VibrationEffect.Composition.PRIMITIVE_CLICK, 0.6f)
                .addPrimitive(VibrationEffect.Composition.PRIMITIVE_CLICK, 1f, 70)
                .compose();
    }

    private static VibrationEffect predefinedBoardEffect(int kind) {
        if (kind == HAPTIC_TICK) return VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK);
        if (kind == HAPTIC_LAND) return VibrationEffect.createPredefined(VibrationEffect.EFFECT_HEAVY_CLICK);
        return VibrationEffect.createPredefined(VibrationEffect.EFFECT_DOUBLE_CLICK);
    }

    private void watchVerdict() {
        boardVerifyArmed = true;
        boardVerifySince = Leaderboard.now() - 5000L;
        boardVerifyUntil = SystemClock.uptimeMillis() + BOARD_VERIFY_WATCH_MS;
        boardVerifyPoll = 0f;
    }

    private boolean awaitingVerdict() {
        return boardVerifyUntil != 0L && SystemClock.uptimeMillis() < boardVerifyUntil;
    }

    private void followVerdict(Leaderboard.Board board) {
        if (!board.verdictKnown) return;
        boolean judged = !boardData.verifying && boardData.verdictTime >= boardVerifySince;
        if (boardVerifyUntil != 0L && judged) {
            boardVerifyUntil = 0L;
        } else if (boardData.verifying && boardVerifyUntil == 0L && !boardVerifyArmed) {
            watchVerdict();
        }
    }

    private String boardHint() {
        if (Leaderboard.nameRefused(prefs)) return "Your name isn't allowed. Tap NAME to choose another";
        if (Leaderboard.nameTaken(prefs)) return "Your name is taken. Tap NAME to choose another";
        if (Leaderboard.name(prefs).isEmpty()) return "Tap NAME to choose a name";
        if (!settingBoardAutoPost) return "Posting is off in Settings > Leaderboard";
        if (boardData.verifying && !boardShowAll) return "Verifying your latest run...";
        long sinceVerdict = Leaderboard.now() - boardData.verdictTime;
        if (!boardShowAll && boardData.verdictTime > 0 && sinceVerdict > -600000L && sinceVerdict < 86400000L && boardData.verdictScore > 0) {
            if (boardData.verdictOk) return "Your " + boardData.verdictScore + " was verified!";
            if (!boardData.verdictWhy.isEmpty()) return "Your " + boardData.verdictScore + " was not verified: " + boardData.verdictWhy;
        }
        if (currentFairMode == FAIR_NONE) return "Not posting: " + fairKeyName(fairBlocker) + " changed";
        List<Leaderboard.Entry> entries = boardEntries();
        if (entries.isEmpty()) return boardShowAll ? "No scores yet. Be the first!" : "No verified scores yet";
        if (boardTab == 1) {
            long left = Math.max(0L, boardData.weekEnds - Leaderboard.now());
            long hours = left / 3600000L;
            return hours >= 24 ? "Resets in " + hours / 24 + "d " + hours % 24 + "h" : "Resets in " + Math.max(1L, hours) + "h";
        }
        return currentFairMode == FAIR_CLASSIC ? "Your scores post as Classic" : "";
    }

    private List<Leaderboard.Entry> boardEntries() {
        if (boardTab == 0) return boardShowAll ? boardData.allTime : boardData.verifiedAllTime;
        return boardShowAll ? boardData.weekly : boardData.verifiedWeekly;
    }

    private Leaderboard.Entry boardMine() {
        if (boardTab == 0) return boardShowAll ? boardData.mineAllTime : boardData.mineVerifiedAllTime;
        return boardShowAll ? boardData.mineWeekly : boardData.mineVerifiedWeekly;
    }

    private int boardRank() {
        if (boardTab == 0) return boardShowAll ? boardData.rankAllTime : boardData.rankVerifiedAllTime;
        return boardShowAll ? boardData.rankWeekly : boardData.rankVerifiedWeekly;
    }

    private void refreshBoard() {
        loadBoardView(boardView());
    }

    private void loadBoardView(final int view) {
        if (!Leaderboard.configured()) {
            boardLoading = false;
            boardNote = "The leaderboard is not set up in this build";
            return;
        }
        final int bit = 1 << view;
        if ((boardLoadingMask & bit) != 0) return;
        boardLoadingMask |= bit;
        syncBoardLoading();
        if (view == boardView()) boardNote = boardOffline ? BOARD_OFFLINE_NOTE : boardEntries().isEmpty() ? "Loading..." : "Updating...";
        Leaderboard.fetch(getContext(), settingBoardSize, view, this::post, (board, error) -> {
            boardLoadingMask &= ~bit;
            boolean current = view == boardView();
            if (board != null) {
                boardOffline = false;
                board.loadedAt[view] = SystemClock.uptimeMillis();
                planBoardMotion(board, view);
                boardData.absorb(board, view);
                boardStaleMask &= ~bit;
                boardDataVersion++;
                followVerdict(board);
            } else if (current) {
                boardOffline = error == null || Leaderboard.NO_CONNECTION.equals(error);
            }
            syncBoardLoading();
            if (current) boardRetrySec = 0f;
            if (!boardLoading) {
                if (board == null && current) boardNote = boardOffline ? BOARD_OFFLINE_NOTE : error;
                else if (current || boardData.loaded[boardView()]) boardNote = boardHint();
            }
        });
    }

    private void askBoardName() {
        if (boardHost == null || !Leaderboard.configured()) return;
        boardHost.askName(Leaderboard.name(prefs), false, name -> boardRefreshPending = true);
    }

    private boolean onBoardTouch(int action, int x, int y) {
        boolean ready = !boardClosing && boardAnim >= 1f && !nameOverlayOpen && nameSpring[0] < 0.02f;
        if (action == MotionEvent.ACTION_DOWN) {
            boardPressed = !ready ? null : boardCloseRect.contains(x, y) ? boardCloseRect : boardJoinRect.contains(x, y) ? boardJoinRect
                    : boardTabAllRect.contains(x, y) ? boardTabAllRect : boardTabWeekRect.contains(x, y) ? boardTabWeekRect
                    : boardToggleRect.contains(x, y) ? boardToggleRect : boardYouRect.contains(x, y) ? boardYouRect : null;
            boardDragArmed = ready && boardScrollMax > 0f && boardRowsRect.contains(x, y);
            boardDragging = false;
            boardDragStartY = y;
            boardDragLastY = y;
            boardDragLastTime = SystemClock.uptimeMillis();
            if (boardDragArmed) {
                boardFling = 0f;
                boardScrollTarget = Float.NaN;
            }
        } else if (action == MotionEvent.ACTION_MOVE) {
            if (boardDragArmed) {
                if (!boardDragging && Math.abs(y - boardDragStartY) > boardRowStep * 0.4f) {
                    boardDragging = true;
                    boardPressed = null;
                    boardDragLastY = y;
                }
                if (boardDragging) {
                    long now = SystemClock.uptimeMillis();
                    float moved = y - boardDragLastY;
                    float seconds = Math.max(0.001f, (now - boardDragLastTime) / 1000f);
                    boardScroll = Math.max(0f, Math.min(boardScrollMax, boardScroll - moved));
                    boardFling = boardFling * 0.3f + (-moved / seconds) * 0.7f;
                    boardDragLastY = y;
                    boardDragLastTime = now;
                }
            }
        } else if (action == MotionEvent.ACTION_UP) {
            Rect pressed = boardPressed;
            boardPressed = null;
            boolean dragged = boardDragging;
            boardDragArmed = false;
            boardDragging = false;
            if (dragged) {
                if (SystemClock.uptimeMillis() - boardDragLastTime > 80L) boardFling = 0f;
                return true;
            }
            boardFling = 0f;
            if (ready && boardRowsRect.contains(x, y) && (pressed == null || pressed == boardYouRect)) {
                boardRowTapped(y);
            } else if (ready && pressed == boardYouRect && boardYouRect.contains(x, y)) {
                boardRowTapped(-1);
            }
            if (ready && pressed != null && pressed.contains(x, y)) {
                playSound(soundSwooshing);
                if (pressed == boardCloseRect) closeBoard();
                else if (pressed == boardJoinRect) askBoardName();
                else if (pressed == boardYouRect) scrollBoardToMine();
                else if (pressed == boardToggleRect) {
                    switchBoard(boardTab, !boardShowAll);
                    prefs.edit().putBoolean(BOARD_SHOW_ALL_KEY, boardShowAll).apply();
                } else {
                    int tab = pressed == boardTabAllRect ? 0 : 1;
                    if (tab != boardTab) switchBoard(tab, boardShowAll);
                    else showBoardView(false);
                }
            }
        } else if (action == MotionEvent.ACTION_CANCEL) {
            boardPressed = null;
            boardDragArmed = false;
            boardDragging = false;
            boardFling = 0f;
        }
        return true;
    }

    private void boardRowTapped(int y) {
        List<Leaderboard.Entry> entries = boardEntries();
        int index;
        if (y < 0) {
            index = BOARD_PINNED_ROW;
        } else {
            index = boardRowStep > 0f ? (int) Math.floor((y - boardRowsRect.top + boardScroll) / boardRowStep) : -1;
            if (index < 0 || index >= entries.size()) {
                boardTapIndex = -1;
                return;
            }
        }
        long now = SystemClock.uptimeMillis();
        if (index == boardTapIndex && now - boardTapTime <= DOUBLE_TAP_MS) {
            Leaderboard.Entry entry = index == BOARD_PINNED_ROW ? boardPinned : entries.get(index);
            boardTapIndex = -1;
            if (entry == null) return;
            boardBubbleIndex = index;
            boardBubbleEntry = entry;
            boardBubbleText = entry.name + "  \u00B7  " + entry.versionLabel();
            boardBubbleAge = 0f;
            playSound(soundSwooshing);
            return;
        }
        boardTapIndex = index;
        boardTapTime = now;
    }

    private void drawBoardBubble(Canvas canvas, float left, float width, float rowsTop, float rowsHeight, float youTop, float unit) {
        if (boardBubbleEntry == null || boardBubbleAge >= BUBBLE_HOLD_SEC + BUBBLE_FADE_SEC || boardBubbleText == null) return;
        float rowTop;
        if (boardBubbleIndex == BOARD_PINNED_ROW) {
            if (boardPinned != boardBubbleEntry) return;
            rowTop = youTop + 2f * unit;
        } else {
            List<Leaderboard.Entry> entries = boardEntries();
            if (boardBubbleIndex < 0 || boardBubbleIndex >= entries.size() || entries.get(boardBubbleIndex) != boardBubbleEntry) return;
            rowTop = rowsTop + boardBubbleIndex * boardRowStep - boardScroll;
            if (rowTop < rowsTop - boardRowStep * 0.5f || rowTop > rowsTop + rowsHeight - boardRowStep * 0.5f) return;
        }
        float rise = Math.min(1f, boardBubbleAge / BUBBLE_FADE_IN_SEC);
        float fall = Math.max(0f, Math.min(1f, (boardBubbleAge - BUBBLE_HOLD_SEC) / BUBBLE_FADE_SEC));
        float alpha = rise * rise * (3f - 2f * rise) * (1f - fall * fall * (3f - 2f * fall));
        if (alpha <= 0f) return;
        float size = 8f * unit;
        float pad = 6f * unit;
        boardTextPaint.setTextSize(size);
        boardTextPaint.setTextAlign(Paint.Align.CENTER);
        float textWidth = Math.min(boardTextPaint.measureText(boardBubbleText), width - 30f * unit);
        float height = size * 2.1f;
        float centerX = left + width / 2f;
        boolean above = rowTop - height - 2f * unit >= rowsTop - 6f * unit;
        float centerY = above ? rowTop - height / 2f - unit + (1f - alpha) * 2f * unit
                : rowTop + boardRowStep + height / 2f + unit - (1f - alpha) * 2f * unit;
        boardBubbleShape.set(centerX - textWidth / 2f - pad, centerY - height / 2f, centerX + textWidth / 2f + pad, centerY + height / 2f);
        boardPaint.setColor(faded((Math.round(Color.alpha(STATUS_PILL) * alpha) << 24) | (STATUS_PILL & 0x00FFFFFF)));
        canvas.drawRoundRect(boardBubbleShape, height / 2f, height / 2f, boardPaint);
        boardText(canvas, boardBubbleText, centerX, centerY + size * 0.37f, size,
                (Math.round(255f * alpha) << 24) | 0x00FFFFFF, Paint.Align.CENTER, width - 30f * unit);
    }

    private void scrollBoardToMine() {
        if (boardMineIndex < 0) return;
        float view = BOARD_ROWS * boardRowStep;
        float target = boardMineIndex * boardRowStep - (view - boardRowStep) / 2f;
        boardFling = 0f;
        boardScrollTarget = Math.max(0f, Math.min(boardScrollMax, target));
    }

    boolean scrollBoard(float rows) {
        if (!boardOpen || boardClosing || boardScrollMax <= 0f) return false;
        boardFling = 0f;
        float from = Float.isNaN(boardScrollTarget) ? boardScroll : boardScrollTarget;
        boardScrollTarget = Math.max(0f, Math.min(boardScrollMax, from + rows * boardRowStep));
        return true;
    }

    private int faded(int color) {
        int alpha = Math.round(Color.alpha(color) * boardAlpha);
        return (color & 0x00FFFFFF) | (Math.max(0, Math.min(255, alpha)) << 24);
    }

    private void boardText(Canvas canvas, String text, float x, float y, float size, int color, Paint.Align align, float maxWidth) {
        boardTextPaint.setTextSize(size);
        boardTextPaint.setTextAlign(align);
        boardTextPaint.setColor(faded(color));
        float width = boardTextPaint.measureText(text);
        if (maxWidth > 0f && width > maxWidth) boardTextPaint.setTextSize(size * maxWidth / width);
        canvas.drawText(text, x, y, boardTextPaint);
    }

    private void boardButton(Canvas canvas, Rect rect, String label, float unit) {
        float drop = boardPressed == rect ? unit * 1.5f : 0f;
        boardPaint.setColor(faded(BOARD_DARK));
        boardShape.set(rect.left, rect.top + unit * 1.5f, rect.right, rect.bottom + unit * 1.5f);
        canvas.drawRoundRect(boardShape, unit * 3f, unit * 3f, boardPaint);
        boardShape.set(rect.left, rect.top + drop, rect.right, rect.bottom + drop);
        canvas.drawRoundRect(boardShape, unit * 3f, unit * 3f, boardPaint);
        boardPaint.setColor(faded(Color.WHITE));
        boardShape.inset(unit * 1.5f, unit * 1.5f);
        canvas.drawRoundRect(boardShape, unit * 2f, unit * 2f, boardPaint);
        boardText(canvas, label, rect.exactCenterX(), rect.exactCenterY() + drop + unit * 3.6f, unit * 10f, BOARD_ORANGE,
                Paint.Align.CENTER, rect.width() - unit * 8f);
    }

    private void boardRow(Canvas canvas, Leaderboard.Entry entry, String rank, float left, float width, float rowTop, float unit, boolean mine) {
        float rowHeight = BOARD_ROW_HEIGHT * unit;
        float baseline = rowTop + 13.5f * unit;
        if (boardRowLift > 0f) {
            boardPaint.setColor(faded(scaleAlpha(BOARD_LIFT_SHADOW, boardRowLift)));
            boardShape.set(left + 6f * unit, rowTop + 1.5f * unit + 1.6f * unit * boardRowLift, left + width - 9f * unit,
                    rowTop + rowHeight + 1.6f * unit * boardRowLift);
            canvas.drawRoundRect(boardShape, unit * 3f, unit * 3f, boardPaint);
            boardPaint.setColor(faded(mixColor(BOARD_PANEL, BOARD_PANEL_EDGE, Math.min(1f, boardRowLift * 1.2f))));
            boardShape.set(left + 6f * unit, rowTop + 0.5f * unit, left + width - 9f * unit, rowTop + rowHeight - 0.5f * unit);
            canvas.drawRoundRect(boardShape, unit * 3f, unit * 3f, boardPaint);
        }
        Shop.Item[] look = lookOf(entry);
        int lookSeed = entry.uid == null ? 0 : entry.uid.hashCode();
        float lookLeft = left + 6f * unit;
        float lookRight = left + width - 9f * unit;
        boolean dark = look != null && look[Shop.ROW] != null && look[Shop.ROW].dark();
        if (look != null) {
            lookBackground(canvas, look[Shop.ROW], lookLeft, rowTop + 0.5f * unit, lookRight, rowTop + rowHeight - 0.5f * unit, unit, lookSeed);
            lookGlow(canvas, look[Shop.GLOW], lookLeft, rowTop + 0.5f * unit, lookRight, rowTop + rowHeight - 0.5f * unit, unit);
        }
        if (boardRowGlow > 0f) {
            boardPaint.setColor(faded(scaleAlpha(0x66FFD45C, boardRowGlow)));
            boardShape.set(left + 6f * unit, rowTop + 0.5f * unit, left + width - 9f * unit, rowTop + rowHeight - 0.5f * unit);
            canvas.drawRoundRect(boardShape, unit * 3f, unit * 3f, boardPaint);
        }
        if (mine) {
            boardPaint.setColor(faded(BOARD_OWN_ROW));
            boardShape.set(left + 7f * unit, rowTop + unit, left + width - 7f * unit, rowTop + rowHeight - unit);
            canvas.drawRoundRect(boardShape, unit * 2f, unit * 2f, boardPaint);
        }
        if (boardRowArrow != 0 && boardRowArrowAlpha > 0f) boardArrow(canvas, left + 9.5f * unit, rowTop + 9.5f * unit, unit, boardRowArrow, boardRowArrowAlpha);
        int medal = medalFor(rank);
        if (medal > 0) {
            medal(canvas, medal, left + 23f * unit, rowTop + 9f * unit, unit);
        } else {
            boardText(canvas, rank, left + 32f * unit, baseline, 10f * unit, BOARD_ORANGE, Paint.Align.RIGHT, 26f * unit);
        }
        String score = String.valueOf(entry.score);
        String shown = boardRowShown >= 0 && boardRowShown != entry.score ? String.valueOf(boardRowShown) : score;
        float scoreRight = left + width - 13f * unit;
        boardTextPaint.setTextSize(11f * unit);
        float scoreWidth = boardTextPaint.measureText(score);
        int plainText = dark ? Color.WHITE : BOARD_DARK;
        int scoreColor = boardRowHot > 0f ? mixColor(plainText, BOARD_ORANGE, boardRowHot) : plainText;
        if (boardRowPop > 0f) {
            float grow = 1f + 0.3f * boardRowPop;
            canvas.save();
            canvas.scale(grow, grow, scoreRight - scoreWidth / 2f, baseline - 4f * unit);
            boardText(canvas, shown, scoreRight, baseline, 11f * unit, scoreColor, Paint.Align.RIGHT, 0f);
            canvas.restore();
        } else {
            boardText(canvas, shown, scoreRight, baseline, 11f * unit, scoreColor, Paint.Align.RIGHT, 0f);
        }
        float badgeRoom = 0f;
        if (entry.verified) {
            badgeRoom = 13f * unit;
            verifiedBadge(canvas, left + width - 13f * unit - scoreWidth - 7.5f * unit, rowTop + 9.5f * unit, unit);
        }
        float nameLeft = left + 37f * unit;
        float nameRoom = width - 37f * unit - 13f * unit - scoreWidth - badgeRoom - 6f * unit - (entry.classic ? 40f * unit : 0f);
        if (look != null && look[Shop.BADGE] != null) {
            drawBadge(canvas, look[Shop.BADGE].effect, nameLeft + 4.5f * unit, rowTop + 9.3f * unit, unit * 0.92f);
            nameLeft += 11f * unit;
            nameRoom -= 11f * unit;
        }
        if (look != null && look[Shop.NAME] != null) {
            lookText(canvas, look[Shop.NAME], entry.name, nameLeft, baseline, 10f * unit, Paint.Align.LEFT, nameRoom, lookSeed);
        } else if (medal == 1) {
            championName(canvas, entry.name, nameLeft, baseline, unit, nameRoom);
        } else {
            boardText(canvas, entry.name, nameLeft, baseline, 10f * unit, plainText, Paint.Align.LEFT, nameRoom);
        }
        if (entry.classic) {
            float nameWidth = Math.min(nameRoom, boardTextPaint.measureText(entry.name));
            float tagLeft = nameLeft + nameWidth + 4f * unit;
            boardPaint.setColor(faded(BOARD_ORANGE));
            boardShape.set(tagLeft, rowTop + 4.5f * unit, tagLeft + 36f * unit, rowTop + 14.5f * unit);
            canvas.drawRoundRect(boardShape, unit * 2f, unit * 2f, boardPaint);
            boardText(canvas, "CLASSIC", tagLeft + 18f * unit, rowTop + 12f * unit, 6.5f * unit, Color.WHITE, Paint.Align.CENTER, 32f * unit);
        }
        if (look != null && look[Shop.MOTION] != null) {
            canvas.save();
            canvas.clipRect(lookLeft, rowTop + 0.5f * unit, lookRight, rowTop + rowHeight - 0.5f * unit);
            lookMotion(canvas, look[Shop.MOTION], lookLeft, rowTop + 0.5f * unit, lookRight, rowTop + rowHeight - 0.5f * unit, unit, lookSeed);
            canvas.restore();
        }
        if (boardRowGainAge >= 0f && boardRowGain > 0) {
            float t = boardRowGainAge;
            float fade = Math.min(1f, t / 0.12f) * (1f - Math.max(0f, (t - 0.55f) / 0.45f));
            float rise = 1f - (1f - t) * (1f - t);
            fade = Math.max(0f, Math.min(1f, fade));
            String gain = "+" + boardRowGain;
            float gainRight = scoreRight - scoreWidth - 4f * unit - badgeRoom;
            float gainBase = baseline - 1.5f * unit - 5f * unit * rise;
            boardTextPaint.setTextSize(7.5f * unit);
            float gainWidth = boardTextPaint.measureText(gain);
            boardPaint.setColor(faded(scaleAlpha(BOARD_PANEL_EDGE, fade * 0.92f)));
            boardShape.set(gainRight - gainWidth - 2.5f * unit, gainBase - 6.8f * unit, gainRight + 2.5f * unit, gainBase + 2f * unit);
            canvas.drawRoundRect(boardShape, 4.4f * unit, 4.4f * unit, boardPaint);
            boardText(canvas, gain, gainRight, gainBase, 7.5f * unit, scaleAlpha(BOARD_GAIN, fade), Paint.Align.RIGHT, 0f);
        }
    }

    private void boardArrow(Canvas canvas, float x, float y, float unit, int direction, float alpha) {
        int color = scaleAlpha(direction > 0 ? BOARD_GAIN : BOARD_DROP, alpha);
        boardPaint.setColor(faded(color));
        medalPath.reset();
        float half = 2.6f * unit;
        float tall = 2.4f * unit;
        float bob = (1f - alpha) * 1.5f * unit * direction;
        if (direction > 0) {
            medalPath.moveTo(x, y - tall + bob);
            medalPath.lineTo(x + half, y + tall * 0.6f + bob);
            medalPath.lineTo(x - half, y + tall * 0.6f + bob);
        } else {
            medalPath.moveTo(x, y + tall + bob);
            medalPath.lineTo(x + half, y - tall * 0.6f + bob);
            medalPath.lineTo(x - half, y - tall * 0.6f + bob);
        }
        medalPath.close();
        canvas.drawPath(medalPath, boardPaint);
    }

    private static int scaleAlpha(int color, float amount) {
        int alpha = Math.round(Color.alpha(color) * Math.max(0f, Math.min(1f, amount)));
        return (color & 0x00FFFFFF) | (alpha << 24);
    }

    private static int mixColor(int from, int to, float t) {
        float k = Math.max(0f, Math.min(1f, t));
        int a = Math.round(Color.alpha(from) + (Color.alpha(to) - Color.alpha(from)) * k);
        int r = Math.round(Color.red(from) + (Color.red(to) - Color.red(from)) * k);
        int g = Math.round(Color.green(from) + (Color.green(to) - Color.green(from)) * k);
        int b = Math.round(Color.blue(from) + (Color.blue(to) - Color.blue(from)) * k);
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    private void plainBoardRow() {
        boardRowShown = -1;
        boardRowArrow = 0;
        boardRowGain = 0;
        boardRowGlow = 0f;
        boardRowPop = 0f;
        boardRowLift = 0f;
        boardRowArrowAlpha = 0f;
        boardRowHot = 0f;
        boardRowGainAge = -1f;
    }

    private void styleBoardRow(BoardMotion motion, int row) {
        boardRowShown = motion.scoreAt(row);
        boardRowGlow = motion.glow(row);
        boardRowPop = motion.pop(row);
        boardRowHot = motion.hot(row);
        boardRowGainAge = motion.gainAge(row);
        boardRowGain = motion.fromScore[row] >= 0 ? motion.list.get(row).score - motion.fromScore[row] : 0;
        boardRowArrow = motion.arrow(row);
        boardRowArrowAlpha = motion.arrowAlpha(boardRowArrow != 0);
        boardRowLift = motion.lift(row);
    }

    private void drawMovingRows(Canvas canvas, BoardMotion motion, List<Leaderboard.Entry> entries, float left, float width, float rowsTop,
            float rowsHeight, float unit) {
        float rowHeight = BOARD_ROW_HEIGHT * unit;
        int count = Math.min(entries.size(), motion.from.length);
        float savedAlpha = boardAlpha;
        for (int layer = 0; layer < 3; layer++) {
            for (int row = 0; row < count; row++) {
                if (motion.layer(row) != layer) continue;
                float at = motion.rowAt(row);
                float top = rowsTop + at * rowHeight - boardScroll;
                if (top >= rowsTop + rowsHeight || top + rowHeight <= rowsTop) continue;
                float enter = motion.enter(row);
                if (enter <= 0f) continue;
                styleBoardRow(motion, row);
                String rank = motion.moving(row) ? String.valueOf(Math.max(1, Math.round(at) + 1)) : String.valueOf(row + 1);
                boardAlpha = savedAlpha * enter;
                boolean shifted = enter < 1f || boardRowLift > 0f;
                if (shifted) {
                    canvas.save();
                    if (enter < 1f) canvas.translate((1f - enter) * (motion.newcomer[row] ? 18f : 10f) * unit, 0f);
                    if (boardRowLift > 0f) {
                        float grow = 1f + 0.035f * boardRowLift;
                        canvas.scale(grow, grow, left + width / 2f, top + rowHeight / 2f);
                    }
                }
                boardRow(canvas, entries.get(row), rank, left, width, top, unit, row == boardMineIndex);
                if (shifted) canvas.restore();
                boardAlpha = savedAlpha;
                plainBoardRow();
            }
        }
    }

    private void drawBoardSkeleton(Canvas canvas, float left, float width, float rowsTop, float unit) {
        float rowHeight = BOARD_ROW_HEIGHT * unit;
        for (int row = 0; row < BOARD_ROWS; row++) {
            float wave = 0.5f + 0.5f * (float) Math.sin(boardShine * 5.5f - row * 0.55f);
            float fade = 1f - row / (float) (BOARD_ROWS + 2);
            int color = scaleAlpha(BOARD_DARK, (0.07f + 0.08f * wave) * fade);
            boardPaint.setColor(faded(color));
            float top = rowsTop + row * rowHeight;
            canvas.drawCircle(left + 23f * unit, top + 9.5f * unit, 5f * unit, boardPaint);
            float nameWidth = (58f + ((row * 37) % 5) * 11f) * unit;
            boardShape.set(left + 37f * unit, top + 6f * unit, left + 37f * unit + nameWidth, top + 13f * unit);
            canvas.drawRoundRect(boardShape, unit * 3.5f, unit * 3.5f, boardPaint);
            float scoreWidth = (16f + ((row * 3) % 3) * 5f) * unit;
            boardShape.set(left + width - 13f * unit - scoreWidth, top + 5.5f * unit, left + width - 13f * unit, top + 13.5f * unit);
            canvas.drawRoundRect(boardShape, unit * 3.5f, unit * 3.5f, boardPaint);
        }
    }

    private void drawBoardProgress(Canvas canvas, float left, float width, float rowsTop, float unit) {
        float trackLeft = left + 14f * unit;
        float track = width - 28f * unit;
        float top = rowsTop - 3.4f * unit;
        float bottom = top + 1.6f * unit;
        boardPaint.setColor(faded(scaleAlpha(BOARD_DARK, 0.12f)));
        boardShape.set(trackLeft, top, trackLeft + track, bottom);
        canvas.drawRoundRect(boardShape, unit, unit, boardPaint);
        float cycle = (boardShine % 1.1f) / 1.1f;
        float head = cycle < 1f ? 1f - (1f - cycle) * (1f - cycle) : 1f;
        float tail = cycle * cycle;
        float from = trackLeft + track * tail;
        float to = trackLeft + track * head;
        if (to - from < 2f * unit) return;
        boardPaint.setColor(faded(BOARD_ORANGE));
        boardShape.set(from, top, to, bottom);
        canvas.drawRoundRect(boardShape, unit, unit, boardPaint);
    }

    static int medalFor(String rank) {
        if (rank == null) return 0;
        String plain = rank.startsWith("#") ? rank.substring(1) : rank;
        return plain.equals("1") ? 1 : plain.equals("2") ? 2 : plain.equals("3") ? 3 : 0;
    }

    private void medal(Canvas canvas, int place, float x, float y, float unit) {
        int[] colors = MEDAL_COLORS[place - 1];
        if (medalFaces[place - 1] == null) {
            medalFaces[place - 1] = new LinearGradient(-4f, -5f, 4f, 5f, colors[0], colors[1], Shader.TileMode.CLAMP);
        }
        canvas.save();
        canvas.translate(x, y);
        canvas.scale(unit, unit);
        if (place == 1) {
            float pulse = 0.5f - 0.5f * (float) Math.cos(boardShine * 2.0 * Math.PI / CHAMPION_PULSE_SEC);
            boardPaint.setColor(faded((Math.round(40f + 50f * pulse) << 24) | (CHAMPION_GLOW & 0x00FFFFFF)));
            canvas.drawCircle(0f, 0f, 7.6f + 0.9f * pulse, boardPaint);
        }
        medalPath.reset();
        medalPath.moveTo(-3.6f, 2f);
        medalPath.lineTo(-5f, 8.4f);
        medalPath.lineTo(-3.2f, 7.4f);
        medalPath.lineTo(-1.9f, 9f);
        medalPath.lineTo(-0.6f, 3f);
        medalPath.close();
        medalPath.moveTo(3.6f, 2f);
        medalPath.lineTo(5f, 8.4f);
        medalPath.lineTo(3.2f, 7.4f);
        medalPath.lineTo(1.9f, 9f);
        medalPath.lineTo(0.6f, 3f);
        medalPath.close();
        boardPaint.setColor(faded(colors[4]));
        canvas.drawPath(medalPath, boardPaint);
        boardPaint.setColor(faded(BOARD_DARK));
        canvas.drawCircle(0f, 0.8f, 6f, boardPaint);
        boardPaint.setColor(faded(colors[2]));
        canvas.drawCircle(0f, 0f, 6f, boardPaint);
        boardPaint.setColor(faded(Color.WHITE));
        boardPaint.setShader(medalFaces[place - 1]);
        canvas.drawCircle(0f, 0f, 4.7f, boardPaint);
        boardPaint.setShader(null);
        boardPaint.setColor(faded(0x8CFFFFFF));
        boardPaint.setStyle(Paint.Style.STROKE);
        boardPaint.setStrokeWidth(0.9f);
        boardPaint.setStrokeCap(Paint.Cap.ROUND);
        boardShape.set(-3.5f, -3.5f, 3.5f, 3.5f);
        canvas.drawArc(boardShape, 200f, 70f, false, boardPaint);
        boardPaint.setStyle(Paint.Style.FILL);
        boardPaint.setStrokeCap(Paint.Cap.BUTT);
        boardText(canvas, String.valueOf(place), 0f, 2.6f, 7.2f, colors[3], Paint.Align.CENTER, 0f);
        canvas.restore();
    }

    private void championName(Canvas canvas, String name, float x, float baseline, float unit, float room) {
        float pulse = 0.5f - 0.5f * (float) Math.cos(boardShine * 2.0 * Math.PI / CHAMPION_PULSE_SEC);
        boardTextPaint.setTextSize(10f * unit);
        float width = Math.max(unit, Math.min(room, boardTextPaint.measureText(name)));
        if (championShine == null) {
            championShine = new LinearGradient(0f, 0f, 1f, 0f, new int[] {CHAMPION_TEXT, CHAMPION_TEXT, CHAMPION_SHINE, CHAMPION_TEXT, CHAMPION_TEXT},
                    new float[] {0f, 0.38f, 0.5f, 0.62f, 1f}, Shader.TileMode.CLAMP);
        }
        float sweep = Math.max(width, 30f * unit) * 1.6f;
        float travel = (boardShine % CHAMPION_SHINE_SEC) / CHAMPION_SHINE_SEC;
        championMatrix.setScale(sweep, 1f);
        championMatrix.postTranslate(x - sweep + (width + sweep * 1.4f) * travel, 0f);
        championShine.setLocalMatrix(championMatrix);
        boardTextPaint.setShader(championShine);
        boardTextPaint.setShadowLayer((2.2f + 1.6f * pulse) * unit, 0f, 0f, faded((Math.round(150f + 90f * pulse) << 24) | (CHAMPION_GLOW & 0x00FFFFFF)));
        boardText(canvas, name, x, baseline, 10f * unit, Color.WHITE, Paint.Align.LEFT, room);
        boardTextPaint.clearShadowLayer();
        boardTextPaint.setShader(null);
        float glint = (boardShine % CHAMPION_PULSE_SEC) / CHAMPION_PULSE_SEC;
        sparkle(canvas, x + width + 2.2f * unit, baseline - 8.6f * unit, 2.4f * unit, (float) Math.sin(glint * Math.PI));
        float later = (glint + 0.5f) % 1f;
        sparkle(canvas, x - 1.6f * unit, baseline - 0.4f * unit, 1.8f * unit, (float) Math.sin(later * Math.PI));
    }

    private void sparkle(Canvas canvas, float x, float y, float size, float strength) {
        if (strength <= 0.02f) return;
        float arm = size * strength;
        boardPaint.setColor(faded((Math.round(255f * strength) << 24) | (CHAMPION_SHINE & 0x00FFFFFF)));
        medalPath.reset();
        medalPath.moveTo(x, y - arm);
        medalPath.quadTo(x, y, x + arm, y);
        medalPath.quadTo(x, y, x, y + arm);
        medalPath.quadTo(x, y, x - arm, y);
        medalPath.quadTo(x, y, x, y - arm);
        medalPath.close();
        canvas.drawPath(medalPath, boardPaint);
    }

    private void verifiedBadge(Canvas canvas, float x, float y, float unit) {
        boardPaint.setColor(faded(BOARD_DARK));
        canvas.drawCircle(x, y + unit * 0.8f, 5.2f * unit, boardPaint);
        boardPaint.setColor(faded(BOARD_VERIFIED));
        canvas.drawCircle(x, y, 5.2f * unit, boardPaint);
        boardPaint.setColor(faded(Color.WHITE));
        boardPaint.setStyle(Paint.Style.STROKE);
        boardPaint.setStrokeWidth(1.5f * unit);
        boardPaint.setStrokeCap(Paint.Cap.ROUND);
        canvas.drawLine(x - 2.4f * unit, y + 0.1f * unit, x - 0.6f * unit, y + 1.9f * unit, boardPaint);
        canvas.drawLine(x - 0.6f * unit, y + 1.9f * unit, x + 2.6f * unit, y - 1.9f * unit, boardPaint);
        boardPaint.setStyle(Paint.Style.FILL);
        boardPaint.setStrokeCap(Paint.Cap.BUTT);
    }

    private void boardToggle(Canvas canvas, Rect rect, float unit) {
        float box = 9f * unit;
        float drop = boardPressed == rect ? unit : 0f;
        float boxLeft = rect.left + unit;
        float boxTop = rect.exactCenterY() - box / 2f + drop;
        boardShape.set(boxLeft, boxTop, boxLeft + box, boxTop + box);
        boardPaint.setColor(faded(BOARD_DARK));
        canvas.drawRoundRect(boardShape, unit * 2f, unit * 2f, boardPaint);
        boardShape.inset(unit * 1.2f, unit * 1.2f);
        boardPaint.setColor(faded(boardShowAll ? BOARD_ORANGE : Color.WHITE));
        canvas.drawRoundRect(boardShape, unit * 1.2f, unit * 1.2f, boardPaint);
        if (boardShowAll) {
            float cx = boxLeft + box / 2f, cy = boxTop + box / 2f;
            boardPaint.setColor(faded(Color.WHITE));
            boardPaint.setStyle(Paint.Style.STROKE);
            boardPaint.setStrokeWidth(1.4f * unit);
            boardPaint.setStrokeCap(Paint.Cap.ROUND);
            canvas.drawLine(cx - 2.2f * unit, cy, cx - 0.6f * unit, cy + 1.7f * unit, boardPaint);
            canvas.drawLine(cx - 0.6f * unit, cy + 1.7f * unit, cx + 2.4f * unit, cy - 1.8f * unit, boardPaint);
            boardPaint.setStyle(Paint.Style.FILL);
            boardPaint.setStrokeCap(Paint.Cap.BUTT);
        }
        boardText(canvas, "SHOW UNVERIFIED", boxLeft + box + 4f * unit, rect.exactCenterY() + 2.6f * unit + drop, 7f * unit, BOARD_DARK,
                Paint.Align.LEFT, rect.right - (boxLeft + box + 4f * unit));
    }

    private void drawBoard(Canvas canvas) {
        if (boardPaint == null) boardPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        if (boardTextPaint == null) {
            boardTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            Typeface face = null;
            try {
                face = ResourcesCompat.getFont(getContext(), R.font.flappy_font);
            } catch (RuntimeException ignored) {
            }
            boardTextPaint.setTypeface(face != null ? face : Typeface.DEFAULT_BOLD);
        }
        float t = Math.max(0f, Math.min(1f, boardAnim));
        float eased;
        if (boardClosing) {
            eased = t * t;
            boardAlpha = t;
        } else {
            float back = 1.70158f;
            float x = t - 1f;
            eased = 1f + (back + 1f) * x * x * x + back * x * x;
            boardAlpha = Math.min(1f, t * 1.8f);
        }
        float panelScale = 0.82f + 0.18f * eased;

        layoutBoard();
        boardPaint.setColor(faded(BOARD_DIM));
        canvas.drawRect(0, 0, screenWidth, screenHeight, boardPaint);
        float fade = 1f - Math.max(0f, Math.min(1f, hideSpring[0]));
        if (fade <= 0f) {
            boardCached = false;
            return;
        }
        boolean still = boardAnim >= 1f && !boardClosing;
        boolean reshaped = nameSpring[0] > 0f && (!nameSide || sideFit < 1f);
        boolean idle = !boardLoading && currentBoardMotion() == null && boardSwitchAge >= BOARD_SWITCH_SEC;
        if (still && reshaped && idle && drawBoardCached(canvas, fade)) return;
        boardCached = false;
        canvas.save();
        canvas.scale(panelScale, panelScale, boardPanel.centerX(), boardPanel.centerY());
        canvas.translate(0f, (1f - eased) * 24f * boardUnit);
        drawBoardContent(canvas, boardLeft, boardTop, boardUnit);
        canvas.restore();
    }

    private Rect boardRect(int index) {
        switch (index) {
            case 0: return boardCloseRect;
            case 1: return boardJoinRect;
            case 2: return boardTabAllRect;
            case 3: return boardTabWeekRect;
            case 4: return boardToggleRect;
            case 5: return boardRowsRect;
            default: return boardYouRect;
        }
    }

    private boolean drawBoardCached(Canvas canvas, float fade) {
        float base = baseBoardUnit();
        float pixels = BOARD_WIDTH_UNITS * base * BOARD_HEIGHT_UNITS * base;
        float unit = base * Math.min(1f, (float) Math.sqrt(BOARD_CACHE_PIXELS / Math.max(1f, pixels)));
        int margin = (int) Math.ceil(2f * unit);
        int width = (int) Math.ceil(BOARD_WIDTH_UNITS * unit) + margin * 2;
        int height = (int) Math.ceil(BOARD_HEIGHT_UNITS * unit) + margin * 2;
        if (boardCache == null || boardCache.getWidth() != width || boardCache.getHeight() != height) {
            releaseBoardCache();
            try {
                boardCache = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                boardCacheCanvas = new Canvas(boardCache);
            } catch (OutOfMemoryError | RuntimeException e) {
                boardCache = null;
                boardCacheCanvas = null;
                return false;
            }
        }
        long key = 17L;
        key = key * 31L + System.identityHashCode(boardData);
        key = key * 31L + boardDataVersion;
        key = key * 31L + (boardBubbleAge < BUBBLE_HOLD_SEC + BUBBLE_FADE_SEC ? Float.floatToIntBits(boardBubbleAge) : 0);
        key = key * 31L + (boardNote == null ? 0 : boardNote.hashCode());
        key = key * 31L + (boardLoading ? 1 : 2);
        key = key * 31L + (boardOffline ? 1 : 2);
        key = key * 31L + boardTab;
        key = key * 31L + (boardShowAll ? 1 : 2);
        key = key * 31L + Float.floatToIntBits(boardScroll);
        key = key * 31L + Float.floatToIntBits(unit);
        key = key * 31L + Leaderboard.uid(prefs).hashCode();
        key = key * 31L + (boardPressed == null ? 0 : System.identityHashCode(boardPressed));
        if (key == 0L) key = 1L;
        if (key != boardCacheKey) {
            for (int i = 0; i < 7; i++) {
                Rect rect = boardRect(i);
                boardRectScratch[i * 4] = rect.left;
                boardRectScratch[i * 4 + 1] = rect.top;
                boardRectScratch[i * 4 + 2] = rect.right;
                boardRectScratch[i * 4 + 3] = rect.bottom;
            }
            float scroll = boardScroll, scrollMax = boardScrollMax, rowStep = boardRowStep, alpha = boardAlpha;
            boardScroll = scroll * unit / base;
            boardAlpha = 1f;
            boardCache.eraseColor(Color.TRANSPARENT);
            drawBoardContent(boardCacheCanvas, margin, margin, unit);
            boardScroll = scroll;
            boardScrollMax = scrollMax;
            boardRowStep = rowStep;
            boardAlpha = alpha;
            for (int i = 0; i < 7; i++) {
                boardRect(i).set(boardRectScratch[i * 4], boardRectScratch[i * 4 + 1], boardRectScratch[i * 4 + 2], boardRectScratch[i * 4 + 3]);
            }
            boardCacheKey = key;
            boardCacheUnit = unit;
            boardCacheMargin = margin;
        }
        if (boardCachePaint == null) {
            boardCachePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            boardCachePaint.setFilterBitmap(true);
        }
        float ratio = boardUnit / boardCacheUnit;
        boardCacheDest.set(boardLeft - boardCacheMargin * ratio, boardTop - boardCacheMargin * ratio,
                boardLeft + (width - boardCacheMargin) * ratio, boardTop + (height - boardCacheMargin) * ratio);
        boardCachePaint.setAlpha(Math.round(255f * fade * Math.max(0f, Math.min(1f, boardAlpha))));
        canvas.drawBitmap(boardCache, null, boardCacheDest, boardCachePaint);
        boardCached = true;
        return true;
    }

    private void drawBoardBody(Canvas canvas, float left, float width, float rowsTop, float unit) {
        float rowHeight = BOARD_ROW_HEIGHT * unit;
        String own = Leaderboard.uid(prefs);
        List<Leaderboard.Entry> entries = boardEntries();
        Leaderboard.Entry mine = boardMine();
        int rank = boardRank();
        float rowsHeight = BOARD_ROWS * rowHeight;
        int count = entries.size();
        if (entries != boardIndexed) {
            boardIndexed = entries;
            boardMineIndex = -1;
            for (int i = 0; i < count && !own.isEmpty(); i++) {
                if (own.equals(entries.get(i).uid)) {
                    boardMineIndex = i;
                    break;
                }
            }
        }
        boardRowStep = rowHeight;
        boardScrollMax = Math.max(0f, (count - BOARD_ROWS) * rowHeight);
        boardScroll = Math.max(0f, Math.min(boardScrollMax, boardScroll));
        boardRowsRect.set((int) left, (int) rowsTop, (int) (left + width), (int) (rowsTop + rowsHeight));
        canvas.save();
        canvas.clipRect(left, rowsTop, left + width, rowsTop + rowsHeight);
        BoardMotion motion = currentBoardMotion();
        if (motion == null) {
            int first = Math.max(0, (int) (boardScroll / rowHeight));
            int last = Math.min(count, first + BOARD_ROWS + 1);
            for (int row = first; row < last; row++) {
                boardRow(canvas, entries.get(row), String.valueOf(row + 1), left, width, rowsTop + row * rowHeight - boardScroll, unit,
                        row == boardMineIndex);
            }
        } else {
            drawMovingRows(canvas, motion, entries, left, width, rowsTop, rowsHeight, unit);
        }
        if (count == 0 && boardLoading && !boardOffline) drawBoardSkeleton(canvas, left, width, rowsTop, unit);
        canvas.restore();
        if (boardLoading && Leaderboard.configured()) drawBoardProgress(canvas, left, width, rowsTop, unit);
        if (boardScrollMax > 0f) {
            float trackTop = rowsTop + 2f * unit;
            float track = rowsHeight - 4f * unit;
            float thumb = Math.max(14f * unit, track * BOARD_ROWS / count);
            float thumbTop = trackTop + (track - thumb) * (boardScroll / boardScrollMax);
            float barRight = left + width - 5f * unit;
            boardPaint.setColor(faded(BOARD_DIVIDER));
            boardShape.set(barRight - 2.5f * unit, trackTop, barRight, trackTop + track);
            canvas.drawRoundRect(boardShape, unit * 1.25f, unit * 1.25f, boardPaint);
            boardPaint.setColor(faded(BOARD_DARK));
            boardShape.set(barRight - 2.5f * unit, thumbTop, barRight, thumbTop + thumb);
            canvas.drawRoundRect(boardShape, unit * 1.25f, unit * 1.25f, boardPaint);
        }
        float youTop = rowsTop + rowsHeight;
        boardYouRect.setEmpty();
        boardPinned = null;
        if (!boardLoading || count > 0) {
            float mineTop = boardMineIndex * rowHeight - boardScroll;
            boolean hidden = boardMineIndex >= 0 && (mineTop < -rowHeight * 0.5f || mineTop > rowsHeight - rowHeight * 0.5f);
            Leaderboard.Entry pinned = null;
            String place = null;
            if (hidden) {
                pinned = entries.get(boardMineIndex);
                place = "#" + (boardMineIndex + 1);
                boardYouRect.set((int) left, (int) youTop, (int) (left + width), (int) (youTop + rowHeight + 2f * unit));
            } else if (boardMineIndex < 0 && mine != null && mine.score > 0) {
                pinned = mine;
                place = rank > 0 ? "#" + rank : "-";
            }
            boardPinned = pinned;
            if (pinned != null) {
                boardPaint.setColor(faded(BOARD_DIVIDER));
                boardShape.set(left + 12f * unit, youTop + unit, left + width - 12f * unit, youTop + unit * 1.8f);
                canvas.drawRect(boardShape, boardPaint);
                float drop = boardPressed == boardYouRect ? unit : 0f;
                if (motion != null) {
                    if (hidden) {
                        styleBoardRow(motion, boardMineIndex);
                        boardRowLift = 0f;
                    } else {
                        if (motion.mineFromScore >= 0) {
                            boardRowShown = motion.mineScoreAt();
                            boardRowHot = motion.mineCountSec > 0f && motion.age < BOARD_COUNT_DELAY + motion.mineCountSec ? (motion.age >= 0f ? 1f : 0f) : 0f;
                            boardRowGlow = BoardMotion.glowAt(motion.age, 0f, BOARD_COUNT_DELAY + motion.mineCountSec);
                            boardRowPop = BoardMotion.popAt(motion.age, BOARD_COUNT_DELAY + motion.mineCountSec);
                            float gain = motion.age < BOARD_COUNT_DELAY ? -1f : (motion.age - BOARD_COUNT_DELAY) / BOARD_GAIN_SEC;
                            boardRowGainAge = gain >= 1f ? -1f : gain;
                            boardRowGain = motion.mineScore - motion.mineFromScore;
                        }
                        if (motion.mineFromRank > 0) {
                            place = "#" + motion.mineRankAt();
                            boardRowArrow = motion.mineToRank < motion.mineFromRank ? 1 : -1;
                            boardRowArrowAlpha = motion.arrowAlpha(true);
                        }
                    }
                }
                boardRow(canvas, pinned, place, left, width, youTop + 2f * unit + drop, unit, true);
                plainBoardRow();
            }
        }

        drawBoardBubble(canvas, left, width, rowsTop, rowsHeight, youTop, unit);
    }

    private boolean boardSwitching() {
        return boardSwitchAge < BOARD_SWITCH_SEC && (boardSwitchFromTab != boardTab || boardSwitchFromAll != boardShowAll);
    }

    private void drawBoardSwitch(Canvas canvas, float left, float width, float rowsTop, float unit) {
        float t = Math.max(0f, Math.min(1f, boardSwitchAge / BOARD_SWITCH_SEC));
        float x = 1f - t;
        float eased = 1f - x * x * x;
        float baseAlpha = boardAlpha;
        int tab = boardTab;
        boolean showAll = boardShowAll;
        float scroll = boardScroll;
        float shift = (boardSwitchDir == 0 ? 0f : 34f * unit) * boardSwitchDir;
        float rise = boardSwitchDir == 0 ? 8f * unit : 0f;
        float bottom = rowsTop + (BOARD_ROWS + 1) * BOARD_ROW_HEIGHT * unit + 3f * unit;
        canvas.save();
        canvas.clipRect(left + 3.5f * unit, rowsTop - unit, left + width - 3.5f * unit, bottom);
        boardTab = boardSwitchFromTab;
        boardShowAll = boardSwitchFromAll;
        boardScroll = boardSwitchScroll;
        boardAlpha = baseAlpha * Math.max(0f, 1f - eased * 1.35f);
        if (boardAlpha > 0.003f) {
            canvas.save();
            canvas.translate(-shift * eased, -rise * eased);
            drawBoardBody(canvas, left, width, rowsTop, unit);
            canvas.restore();
        }
        boardTab = tab;
        boardShowAll = showAll;
        boardScroll = scroll;
        boardAlpha = baseAlpha * Math.max(0f, Math.min(1f, (eased - 0.12f) / 0.88f));
        canvas.save();
        canvas.translate(shift * (1f - eased), rise * (1f - eased));
        drawBoardBody(canvas, left, width, rowsTop, unit);
        canvas.restore();
        boardAlpha = baseAlpha;
        canvas.restore();
    }

    private void switchBoard(int tab, boolean showAll) {
        if (tab == boardTab && showAll == boardShowAll) return;
        boardPillFrom = boardPillPosition();
        boardPillTo = pillSpot(tab);
        boardSwitchFromTab = boardTab;
        boardSwitchFromAll = boardShowAll;
        boardSwitchScroll = boardScroll;
        boardSwitchDir = tab == boardTab ? 0 : pillSpot(tab) > pillSpot(boardTab) ? 1 : -1;
        boardSwitchAge = 0f;
        boardTab = tab;
        boardShowAll = showAll;
        resetBoardScroll();
        hideBoardBubble();
        feel(Feel.TICK);
        showBoardView(false);
    }

    private static float pillSpot(int tab) {
        return tab == 1 ? 0f : 1f;
    }

    private float boardPillPosition() {
        if (boardSwitchAge >= BOARD_SWITCH_SEC || boardPillFrom < 0f) return pillSpot(boardTab);
        float t = Math.max(0f, Math.min(1f, boardSwitchAge / BOARD_PILL_SEC));
        float back = 1.2f;
        float x = t - 1f;
        float eased = 1f + (back + 1f) * x * x * x + back * x * x;
        return boardPillFrom + (boardPillTo - boardPillFrom) * eased;
    }

    private void drawBoardTabs(Canvas canvas, float unit) {
        for (Rect rect : new Rect[] {boardTabWeekRect, boardTabAllRect}) {
            boardShape.set(rect);
            boardPaint.setColor(faded(BOARD_TAB_IDLE));
            canvas.drawRoundRect(boardShape, unit * 3f, unit * 3f, boardPaint);
        }
        float pos = boardPillPosition();
        float moving = boardSwitchAge < BOARD_PILL_SEC ? (float) Math.sin(Math.PI * Math.max(0f, Math.min(1f, boardSwitchAge / BOARD_PILL_SEC))) : 0f;
        float pillLeft = boardTabWeekRect.left + (boardTabAllRect.left - boardTabWeekRect.left) * pos;
        float pillRight = boardTabWeekRect.right + (boardTabAllRect.right - boardTabWeekRect.right) * pos;
        float stretch = moving * 5f * unit;
        float squash = moving * 0.8f * unit;
        boardShape.set(pillLeft - stretch, boardTabWeekRect.top + squash, pillRight + stretch, boardTabWeekRect.bottom - squash);
        boardPaint.setColor(faded(BOARD_ORANGE));
        canvas.drawRoundRect(boardShape, unit * 3f, unit * 3f, boardPaint);
        boardPaint.setColor(faded(BOARD_DARK));
        boardShape.set(pillLeft - stretch + unit * 2f, boardTabWeekRect.bottom - squash - unit * 1.5f, pillRight + stretch - unit * 2f, boardTabWeekRect.bottom - squash);
        canvas.drawRect(boardShape, boardPaint);
        drawTabLabel(canvas, boardTabWeekRect, "WEEKLY", pillLeft - stretch, pillRight + stretch, unit);
        drawTabLabel(canvas, boardTabAllRect, "ALL TIME", pillLeft - stretch, pillRight + stretch, unit);
    }

    private void drawTabLabel(Canvas canvas, Rect rect, String label, float pillLeft, float pillRight, float unit) {
        float covered = Math.max(0f, Math.min(pillRight, rect.right) - Math.max(pillLeft, rect.left)) / Math.max(1f, rect.width());
        boardText(canvas, label, rect.exactCenterX(), rect.exactCenterY() + unit * 3f, unit * 8.5f,
                mixColor(BOARD_DARK, Color.WHITE, Math.max(0f, Math.min(1f, covered))), Paint.Align.CENTER, rect.width() - unit * 6f);
    }

    private void drawBoardContent(Canvas canvas, float left, float top, float unit) {
        float rowHeight = BOARD_ROW_HEIGHT * unit;
        float width = BOARD_WIDTH_UNITS * unit;
        float height = BOARD_HEIGHT_UNITS * unit;

        boardShape.set(left, top, left + width, top + height);
        boardPaint.setColor(faded(BOARD_DARK));
        canvas.drawRoundRect(boardShape, unit * 5f, unit * 5f, boardPaint);
        boardShape.inset(unit * 2f, unit * 2f);
        boardPaint.setColor(faded(BOARD_PANEL_EDGE));
        canvas.drawRoundRect(boardShape, unit * 4f, unit * 4f, boardPaint);
        boardShape.inset(unit * 1.5f, unit * 1.5f);
        boardPaint.setColor(faded(BOARD_PANEL));
        canvas.drawRoundRect(boardShape, unit * 3f, unit * 3f, boardPaint);

        boardText(canvas, "LEADERBOARD", left + width / 2f, top + 24f * unit, 15f * unit, BOARD_ORANGE, Paint.Align.CENTER, width - 24f * unit);

        float tabTop = top + 32f * unit;
        float tabWidth = 96f * unit;
        float tabGap = 6f * unit;
        float tabsLeft = left + (width - tabWidth * 2f - tabGap) / 2f;
        boardTabWeekRect.set((int) tabsLeft, (int) tabTop, (int) (tabsLeft + tabWidth), (int) (tabTop + 18f * unit));
        boardTabAllRect.set((int) (tabsLeft + tabWidth + tabGap), (int) tabTop, (int) (tabsLeft + tabWidth * 2f + tabGap), (int) (tabTop + 18f * unit));
        drawBoardTabs(canvas, unit);

        float rowsTop = tabTop + 24f * unit;
        float rowsHeight = BOARD_ROWS * rowHeight;
        if (boardSwitching()) drawBoardSwitch(canvas, left, width, rowsTop, unit);
        else drawBoardBody(canvas, left, width, rowsTop, unit);
        float youTop = rowsTop + rowsHeight;
        int count = boardEntries().size();
        float footerTop = youTop + rowHeight;
        boolean offline = boardOffline;
        if (offline && count == 0) {
            float middle = rowsTop + rowsHeight / 2f;
            boardText(canvas, "NO INTERNET", left + width / 2f, middle - 4f * unit, 13f * unit, BOARD_ORANGE, Paint.Align.CENTER, width - 30f * unit);
            boardText(canvas, "Connect to see the leaderboard", left + width / 2f, middle + 10f * unit, 7.5f * unit, BOARD_DARK,
                    Paint.Align.CENTER, width - 30f * unit);
            boardText(canvas, "Trying again every few seconds", left + width / 2f, middle + 21f * unit, 6.5f * unit, BOARD_DARK,
                    Paint.Align.CENTER, width - 30f * unit);
        }
        if (!boardNote.isEmpty()) {
            boardText(canvas, boardNote, left + width / 2f, footerTop + 15f * unit, 8f * unit, offline ? BOARD_ORANGE : BOARD_DARK,
                    Paint.Align.CENTER, width - 20f * unit);
        }
        float buttonTop = footerTop + 24f * unit;
        float buttonWidth = 84f * unit;
        float buttonHeight = 26f * unit;
        float gap = 16f * unit;
        boolean canName = Leaderboard.configured();
        float buttonsLeft = left + (width - (canName ? buttonWidth * 2f + gap : buttonWidth)) / 2f;
        if (canName) {
            boardJoinRect.set((int) buttonsLeft, (int) buttonTop, (int) (buttonsLeft + buttonWidth), (int) (buttonTop + buttonHeight));
            boardButton(canvas, boardJoinRect, "NAME", unit);
            buttonsLeft += buttonWidth + gap;
        } else {
            boardJoinRect.setEmpty();
        }
        boardCloseRect.set((int) buttonsLeft, (int) buttonTop, (int) (buttonsLeft + buttonWidth), (int) (buttonTop + buttonHeight));
        boardButton(canvas, boardCloseRect, "OK", unit);
        if (canName) {
            float toggleWidth = 92f * unit;
            float toggleTop = buttonTop + buttonHeight + 2f * unit;
            float toggleLeft = left + (width - toggleWidth) / 2f;
            boardToggleRect.set((int) toggleLeft, (int) toggleTop, (int) (toggleLeft + toggleWidth), (int) (toggleTop + 22f * unit));
            boardToggle(canvas, boardToggleRect, unit);
        }
        if (!canName) {
            boardTabAllRect.setEmpty();
            boardTabWeekRect.setEmpty();
            boardToggleRect.setEmpty();
        }
    }

    private void startCinematicStrikeSequence(boolean mega) {
        cinematicPhase = 1;
        cinematicTimer = 0f;
        timeDilation = 1.0f;
        cameraZoom = 1.0f;
        isMegaStrike = mega;
    }

    private void updateCinematicSequence(float realDeltaTime) {
        if (cinematicPhase == 0) return;

        cinematicTimer += realDeltaTime;
        float strikeZoom = isMegaStrike ? 1.8f : 1.5f;
        float strikeTimeScale = isMegaStrike ? 0.05f : 0.1f;

        if (cinematicPhase == 1) {
            float progress = Math.min(1.0f, cinematicTimer / 0.2f);
            cameraZoom = 1.0f + ((strikeZoom - 1.0f) * progress);
            timeDilation = 1.0f - ((1.0f - strikeTimeScale) * progress);

            if (progress >= 1.0f) {
                cinematicPhase = 2;
                cinematicTimer = 0f;
                if (isMegaStrike) {
                    triggerMegaStrike();
                } else {
                    triggerDirectLightningOnBird();
                }
            }
        } else if (cinematicPhase == 2) {
            timeDilation = strikeTimeScale;
            cameraZoom = strikeZoom;
            if (cinematicTimer >= (isMegaStrike ? 0.8f : 0.5f)) {
                cinematicPhase = 3;
                cinematicTimer = 0f;
            }
        } else if (cinematicPhase == 3) {
            float progress = Math.min(1.0f, cinematicTimer / 0.3f);
            cameraZoom = strikeZoom - ((strikeZoom - 1.0f) * progress);
            timeDilation = strikeTimeScale + ((1.0f - strikeTimeScale) * progress);

            if (progress >= 1.0f) {
                cinematicPhase = 0;
                timeDilation = 1.0f;
                cameraZoom = 1.0f;
                isMegaStrike = false;
            }
        }
    }

    private void triggerLightning() {
        weatherSystem.triggerLightning(screenWidth, getPlayableHeight() + systemBarTop);
        playSound(soundThunder);
        triggerThunderHaptic();
        triggerShake(7.0f, 0.5f);
    }

    private void triggerDirectLightningOnBird() {
        weatherSystem.triggerTargetedLightning(birdX, birdY);
        playSound(soundThunder);
        triggerThunderHaptic();
        triggerShake(15.0f, 0.8f);
        isBirdBurnt = true;
        burntTimer = 10.0f;
        startLightningFlash();
    }

    private void triggerMegaStrike() {
        weatherSystem.triggerMultiDirectionalBolts(birdX, birdY, screenWidth, getPlayableHeight() + systemBarTop);
        playSound(soundThunder);
        playSound(soundThunder);
        triggerThunderHaptic();
        triggerShake(25.0f, 1.5f);
        isBirdBurnt = true;
        burntTimer = 20.0f;
        pipeElectrificationTimer = 20.0f;
        startLightningFlash();
    }

    private void triggerShake(float magnitude, float duration) {
        if (!settingScreenShakeEnabled) return;
        currentShakeMagnitude = magnitude * scale * settingScreenShakeStrength;
        shakeTimer = duration;
    }

    void feel(int kind) {
        if (feel == null || (filmMode && !filmFeel)) return;
        if (kind == Feel.FLAP && settingAutopilotEnabled) return;
        feel.play(kind);
    }

    private void triggerThunderHaptic() {
        if (!settingHapticFeedbackEnabled || vibrator == null || (filmMode && !filmFeel)) return;
        long[] pattern = {0, 30, 40, 30, 40, 80, 20, 100};
        try {
            if (!vibrator.hasVibrator()) return;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createWaveform(pattern, -1));
            } else {
                vibrator.vibrate(pattern, -1);
            }
        } catch (RuntimeException e) {
            Log.w(TAG, "Vibration unavailable", e);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (!isReady) return;
        boolean blended = blendMotion();
        try {
            drawFrame(canvas);
        } finally {
            if (blended) restoreMotion();
        }
    }

    private void drawFrame(Canvas canvas) {
        drawRandom.setSeed(Double.doubleToLongBits(clock) * 0x9E3779B97F4A7C15L);
        boolean shaking = shakeTimer > 0;
        if (shaking) canvas.drawColor(Color.BLACK);

        canvas.save();
        if (shaking) {
            float magnitude = currentShakeMagnitude * (shakeTimer / 0.5f);
            float xOffset = (drawRandom.nextFloat() - 0.5f) * 2 * magnitude;
            float yOffset = (drawRandom.nextFloat() - 0.5f) * 2 * magnitude;
            canvas.translate(xOffset, yOffset);
        }
        if (cameraZoom > 1.0f) {
            canvas.scale(cameraZoom, cameraZoom, birdX, birdY);
        }
        if (settingUpsideDownEnabled) {
            canvas.scale(1, -1, screenWidth / 2f, screenHeight / 2f);
        }
        drawWorld(canvas, shaking);
        canvas.restore();

        drawHud(canvas);
        if (settingShowFps && !filmMode) drawFps(canvas);
        if (isPaused) drawPauseOverlay(canvas);
        if (boardStatusAlpha > 0f && boardStatusPlace()) drawBoardStatus(canvas);
        if (boardOpen) drawBoard(canvas);
        if (!boardOpen && (nameOverlayOpen || dimSpring[0] > 0f)) drawNameBackdrop(canvas);
        if (coinOpen) drawCoins(canvas);
        if (!filmMode) drawToast(canvas);

        if (transitionAlpha > 0) {
            transitionPaint.setAlpha((int) transitionAlpha);
            canvas.drawRect(0, 0, screenWidth, screenHeight, transitionPaint);
        }
        if (flashAlpha > 0) {
            flashPaint.setColor(flashColor);
            flashPaint.setAlpha(Math.max(0, Math.min(255, (int) flashAlpha)));
            canvas.drawRect(0, 0, screenWidth, screenHeight, flashPaint);
        }
    }

    private void drawWorld(Canvas canvas, boolean shaking) {
        drawBackground(canvas);

        if (settingWeatherEffect > 0) weatherSystem.drawBack(canvas);

        boolean pipesHidden = gameState == GameState.HOME
                || (gameState == GameState.TRANSITION_TO_WAITING && isFadingOut);
        if (!pipesHidden) drawPipes(canvas, shaking);

        drawGround(canvas);

        for (int i = 0; i < smokeCount; i++) {
            SmokeParticle particle = smokeParticles[i];
            smokePaint.setAlpha((int) (particle.alpha * 255));
            smokeDestRect.set(particle.x - particle.radius, particle.y - particle.radius,
                    particle.x + particle.radius, particle.y + particle.radius);
            canvas.drawBitmap(smokeBitmap, null, smokeDestRect, smokePaint);
        }

        drawBirdTrail(canvas);
        drawBird(canvas);
        if (settingShowHitboxes) drawHitboxes(canvas, !pipesHidden);

        for (int i = 0; i < sparkCount; i++) {
            sparkParticles[i].draw(canvas, sparkPaint);
        }

        if (settingWeatherEffect > 0) {
            weatherSystem.drawFront(canvas);
            weatherSystem.drawLightingOverlay(canvas);
        }
    }

    private void drawHitboxes(Canvas canvas, boolean withPipes) {
        hitboxPaint.setStrokeWidth(Math.max(2f, scale * 0.5f));
        canvas.drawRect(birdRect.left, birdRect.top, birdRect.right, birdRect.bottom, hitboxPaint);
        if (!withPipes) return;
        float groundTop = groundTopY();
        for (int i = 0; i < pipes.size(); i++) {
            Pipe pipe = pipes.get(i);
            float reach = pipe.width * Math.max(1f, settingPipeWidth);
            if (pipe.x + reach < 0 || pipe.x - reach > screenWidth) continue;
            drawHitbox(canvas, pipe.getTopHeadRect(settingPipeWidth), groundTop);
            drawHitbox(canvas, pipe.getTopBodyRect(settingPipeWidth), groundTop);
            drawHitbox(canvas, pipe.getBottomHeadRect(pipeGap, settingPipeWidth), groundTop);
            drawHitbox(canvas, pipe.getBottomBodyRect(pipeGap, settingPipeWidth), groundTop);
        }
    }

    private void drawHitbox(Canvas canvas, Rect rect, float groundTop) {
        float top = Math.max(rect.top, -screenHeight);
        float bottom = Math.min(rect.bottom, groundTop);
        if (bottom > top) canvas.drawRect(rect.left, top, rect.right, bottom, hitboxPaint);
    }

    private void drawFps(Canvas canvas) {
        float digitHeight = smallNumberBitmaps[0].getHeight() * FPS_DIGIT_SCALE;
        float digitWidth = smallNumberBitmaps[0].getWidth() * FPS_DIGIT_SCALE;
        float right = safeInsetLeft + cornerMargin + digitWidth * 3f;
        float bottom = systemBarTop + cornerMargin + digitHeight;
        drawDigitsRightAligned(canvas, shownFps, right, bottom, smallNumberBitmaps, FPS_DIGIT_SCALE, fpsPaint);
    }

    private float displayRefreshRate() {
        Display display = getDisplay();
        float rate = display != null ? display.getRefreshRate() : FALLBACK_REFRESH_RATE;
        return rate >= 1f && rate <= 1000f ? rate : FALLBACK_REFRESH_RATE;
    }

    private float targetFrameRate() {
        float refresh = displayRefreshRate();
        return settingFrameLimit > 0 ? Math.min(refresh, settingFrameLimit) : refresh;
    }

    private void updateFpsColor() {
        float target = targetFrameRate();
        int color = shownFps >= target * FPS_GOOD_SHARE ? FPS_COLOR_GOOD
                : shownFps >= target * FPS_OK_SHARE ? FPS_COLOR_OK : FPS_COLOR_BAD;
        if (color == fpsColor) return;
        fpsColor = color;
        fpsPaint.setColorFilter(color == FPS_COLOR_GOOD ? fpsGoodFilter : color == FPS_COLOR_OK ? fpsOkFilter : fpsBadFilter);
    }

    private void drawBackground(Canvas canvas) {
        int samples = motionSamples(backgroundBlurLength);
        if (!settingDynamicDayNightEnabled) {
            drawExposedBackground(canvas, nightBackground ? bgNightBitmap : bgDayBitmap, samples);
            return;
        }

        float nightAmount = (float) (1 - Math.cos(dayNightCycleProgress * 2 * Math.PI)) / 2;
        int nightAlpha = Math.max(0, Math.min(255, (int) (nightAmount * 255)));
        if (nightAlpha < 255) drawExposedBackground(canvas, bgDayBitmap, samples);
        if (nightAlpha == 255) {
            drawExposedBackground(canvas, bgNightBitmap, samples);
        } else if (nightAlpha > 0 && samples > 1) {
            canvas.saveLayerAlpha(null, nightAlpha);
            drawExposedBackground(canvas, bgNightBitmap, samples);
            canvas.restore();
        } else if (nightAlpha > 0) {
            nightBgPaint.setAlpha(nightAlpha);
            drawBackgroundTiles(canvas, bgNightBitmap, nightBgPaint, 0f);
        }
    }

    private void drawExposedBackground(Canvas canvas, Bitmap bitmap, int samples) {
        for (int sample = 0; sample < samples; sample++) {
            pixelPaint.setAlpha(255 / (sample + 1));
            drawBackgroundTiles(canvas, bitmap, pixelPaint, samplePosition(sample, samples) * backgroundBlurLength);
        }
        pixelPaint.setAlpha(255);
    }

    private void drawBackgroundTiles(Canvas canvas, Bitmap bitmap, Paint paint, float shift) {
        float tileWidth = bitmap.getWidth();
        float startX = (backgroundX + shift) % tileWidth;
        if (startX > 0) startX -= tileWidth;
        for (float x = startX - tileWidth; x < screenWidth + tileWidth; x += tileWidth) {
            canvas.drawBitmap(bitmap, x, 0, paint);
        }
    }

    private void drawPipes(Canvas canvas, boolean shaking) {
        float margin = shaking ? Math.abs(currentShakeMagnitude) * 3f : 0f;
        float worldTop = -margin - 1f;
        float groundTop = groundTopY();
        int samples = motionSamples(pipeBlurLength);
        boolean blurred = samples > 1;
        float reach = blurred ? pipeBlurLength / 2f : 0f;
        boolean shineReady = false;

        if (blurred) canvas.saveLayer(-margin, worldTop, screenWidth + margin, groundTop, null);

        for (int i = 0; i < pipes.size(); i++) {
            Pipe pipe = pipes.get(i);
            float visualWidth = pipe.width * settingPipeWidth;
            float left = pipe.x - (visualWidth - pipe.width) / 2f;
            float right = left + visualWidth;
            if (right + reach < -margin || left - reach > screenWidth + margin) continue;

            float topY = pipe.getTopPipeY();
            float bottomY = pipe.getBottomPipeY(pipeGap);
            Paint paint = pipe.pipePaint;

            if (blurred) paint.setXfermode(additiveXfermode);
            for (int sample = 0; sample < samples; sample++) {
                float shift = samplePosition(sample, samples) * pipeBlurLength;
                if (blurred) paint.setAlpha(sampleAlpha(255, sample, samples));
                drawPipePair(canvas, paint, left + shift, right + shift, topY, bottomY, pipe.height, worldTop, groundTop);
            }
            if (blurred) {
                paint.setXfermode(null);
                paint.setAlpha(255);
            }

            if (pipe.isGolden) {
                if (!shineReady) {
                    gradientMatrix.setTranslate(goldenShineOffset, 0);
                    goldenPipeShader.setLocalMatrix(gradientMatrix);
                    shineReady = true;
                }
                pipeDestRect.set(left - reach, topY, right + reach, topY + pipe.height);
                canvas.drawRect(pipeDestRect, goldenPipePaint);
                pipeDestRect.set(left - reach, bottomY, right + reach, bottomY + pipe.height);
                canvas.drawRect(pipeDestRect, goldenPipePaint);
            }
        }

        if (blurred) canvas.restore();

        if (pipeElectrificationTimer <= 0) return;
        for (int i = 0; i < pipes.size(); i++) {
            Pipe pipe = pipes.get(i);
            float visualWidth = pipe.width * settingPipeWidth;
            float left = pipe.x - (visualWidth - pipe.width) / 2f;
            if (left + visualWidth < -margin || left > screenWidth + margin) continue;
            drawPipeElectricity(canvas, left, pipe.getTopPipeY(), visualWidth, pipe.height);
            drawPipeElectricity(canvas, left, pipe.getBottomPipeY(pipeGap), visualWidth, pipe.height);
        }
    }

    private void drawPipePair(Canvas canvas, Paint paint, float left, float right, float topY, float bottomY,
                              float height, float worldTop, float groundTop) {
        int bitmapWidth = pipeUpBitmap.getWidth();
        int bitmapHeight = pipeUpBitmap.getHeight();

        pipeDestRect.set(left, topY, right, topY + height);
        canvas.drawBitmap(pipeDownBitmap, null, pipeDestRect, paint);
        if (topY > worldTop) {
            pipeEdgeSrcRect.set(0, 0, bitmapWidth, 1);
            pipeDestRect.set(left, worldTop, right, topY);
            canvas.drawBitmap(pipeDownBitmap, pipeEdgeSrcRect, pipeDestRect, paint);
        }

        pipeDestRect.set(left, bottomY, right, bottomY + height);
        canvas.drawBitmap(pipeUpBitmap, null, pipeDestRect, paint);
        if (bottomY + height < groundTop) {
            pipeEdgeSrcRect.set(0, bitmapHeight - 1, bitmapWidth, bitmapHeight);
            pipeDestRect.set(left, bottomY + height, right, groundTop + 1f);
            canvas.drawBitmap(pipeUpBitmap, pipeEdgeSrcRect, pipeDestRect, paint);
        }
    }

    private void drawPipeElectricity(Canvas canvas, float x, float y, float w, float h) {
        float startY = Math.max(y, 0f);
        float endY = Math.min(y + h, screenHeight);
        if (endY <= startY || w <= 0) return;

        for (int strand = 0; strand < 3; strand++) {
            int count = 0;
            float cx = x + (drawRandom.nextFloat() * w);
            float cy = startY;
            while (cy < endY && count + 4 <= electricityPoints.length) {
                float nextY = cy + (drawRandom.nextInt(40) + 10) * fxScale;
                float nextX = Math.max(x, Math.min(x + w, cx + (drawRandom.nextInt(30) - 15) * fxScale));
                electricityPoints[count++] = cx;
                electricityPoints[count++] = cy;
                electricityPoints[count++] = nextX;
                electricityPoints[count++] = nextY;
                cx = nextX;
                cy = nextY;
            }
            canvas.drawLines(electricityPoints, 0, count, electricityGlowPaint);
            canvas.drawLines(electricityPoints, 0, count, electricityPipePaint);
        }
    }

    private void drawGround(Canvas canvas) {
        float groundDrawWidth = groundBitmap.getWidth();
        float groundTop = groundTopY();
        int samples = motionSamples(groundBlurLength);

        for (int sample = 0; sample < samples; sample++) {
            float startGroundX = (groundX + samplePosition(sample, samples) * groundBlurLength) % groundDrawWidth;
            if (startGroundX > 0) startGroundX -= groundDrawWidth;

            pixelPaint.setAlpha(255 / (sample + 1));
            for (float x = startGroundX - groundDrawWidth; x < screenWidth + groundDrawWidth; x += groundDrawWidth) {
                groundDestRect.set(x, groundTop, x + groundDrawWidth, screenHeight);
                canvas.drawBitmap(groundBitmap, null, groundDestRect, pixelPaint);
            }
        }
        pixelPaint.setAlpha(255);
    }

    private ColorFilter hueFilter(float hue) {
        int index = (int) (hue / 360f * HUE_FILTER_STEPS) % HUE_FILTER_STEPS;
        if (index < 0) index += HUE_FILTER_STEPS;
        ColorFilter filter = hueFilters[index];
        if (filter == null) {
            colorMatrix.setRotate(2, index * (360f / HUE_FILTER_STEPS));
            filter = new ColorMatrixColorFilter(colorMatrix);
            hueFilters[index] = filter;
        }
        return filter;
    }

    private void setBirdMatrix(Bitmap bitmap, float x, float y, float rotation) {
        birdMatrix.reset();
        birdMatrix.postTranslate(-bitmap.getWidth() / 2f, -bitmap.getHeight() / 2f);
        birdMatrix.postRotate(rotation);
        birdMatrix.postScale(settingBirdSize, settingBirdSize, 0, 0);
        birdMatrix.postTranslate(x, y);
    }

    private void drawBird(Canvas canvas) {
        ColorFilter filter = null;
        if (isBirdBurnt) filter = burntColorFilter;
        else if (settingRainbowBirdEnabled) filter = hueFilter(rainbowHue);
        else if (settingBirdHue != 0) filter = hueFilter(settingBirdHue);
        birdPaint.setColorFilter(filter);

        int alpha = settingGhostModeEnabled ? 128 : 255;
        if (invulnerableTimer > 0 && ((int) (clock * 10)) % 2 == 0) alpha = alpha * 2 / 5;
        Bitmap currentBirdBitmap = birdBitmaps[birdFrame];
        float halfSpan = (currentBirdBitmap.getWidth() + currentBirdBitmap.getHeight()) * Math.abs(settingBirdSize) / 2f;
        float rotationTravel = (float) Math.toRadians(Math.abs(birdBlurRotation)) * halfSpan / 2f;
        int samples = motionSamples(Math.max((float) Math.hypot(birdBlurX, birdBlurY), rotationTravel));

        if (samples <= 1) {
            birdPaint.setAlpha(alpha);
            setBirdMatrix(currentBirdBitmap, birdX, birdY, birdRotation);
            canvas.drawBitmap(currentBirdBitmap, birdMatrix, birdPaint);
            return;
        }

        float reachX = halfSpan + Math.abs(birdBlurX) / 2f + 2f;
        float reachY = halfSpan + Math.abs(birdBlurY) / 2f + 2f;
        canvas.saveLayer(birdX - reachX, birdY - reachY, birdX + reachX, birdY + reachY, null);
        birdPaint.setXfermode(additiveXfermode);
        for (int sample = 0; sample < samples; sample++) {
            float position = samplePosition(sample, samples);
            birdPaint.setAlpha(sampleAlpha(alpha, sample, samples));
            setBirdMatrix(currentBirdBitmap, birdX + birdBlurX * position, birdY + birdBlurY * position,
                    birdRotation + birdBlurRotation * position);
            canvas.drawBitmap(currentBirdBitmap, birdMatrix, birdPaint);
        }
        birdPaint.setXfermode(null);
        birdPaint.setAlpha(alpha);
        canvas.restore();
    }

    private void drawBirdTrail(Canvas canvas) {
        if (!settingBirdTrailEnabled || trailCount == 0) return;

        for (int i = 0; i < trailCount; i++) {
            TrailParticle particle = birdTrail[(trailStart + i) % MAX_TRAIL_PARTICLES];
            float progress = (float) i / trailCount;

            trailPaint.setColorFilter(particle.rainbowHue != -1 ? hueFilter(particle.rainbowHue) : null);
            trailPaint.setAlpha((int) (progress * 100));

            Bitmap trailBitmap = scaledAllBirdBitmaps[particle.colorIndex][particle.frameIndex];
            setBirdMatrix(trailBitmap, particle.x, particle.y, particle.rotation);
            canvas.drawBitmap(trailBitmap, birdMatrix, trailPaint);
        }
    }

    private void drawHud(Canvas canvas) {
        switch (gameState) {
            case HOME:
                drawHomeScreen(canvas);
                break;
            case TRANSITION_TO_WAITING:
                if (isFadingOut) drawHomeScreen(canvas);
                else drawGetReady(canvas, 255);
                break;
            case WAITING:
                drawGetReady(canvas, 255);
                if (!filmMode) drawTwistChip(canvas, false);
                break;
            case PLAYING:
                int scoreCenterY = systemBarTop + (int) (getPlayableHeight() * 0.15);
                float readyLeft = 1f - (float) ((clock - roundStartClock) / READY_FADE_SEC);
                if (readyLeft > 0f) drawGetReady(canvas, Math.round(255f * Math.min(1f, readyLeft)));
                if (settingShowScore) drawScoreWithImages(canvas, score, screenWidth / 2, scoreCenterY);
                drawLives(canvas, scoreCenterY + numberBitmaps[0].getHeight() / 2f + 6f * scale);
                if (settingPauseButtonEnabled && !isPaused) drawPauseButton(canvas);
                break;
            case GAME_OVER:
            case PANEL_SLIDING:
                if (darkenPaint.getAlpha() > 0) {
                    canvas.drawRect(0, 0, screenWidth, screenHeight, darkenPaint);
                }
                if (gameState == GameState.PANEL_SLIDING) {
                    drawGameOverScreen(canvas);
                    if (!filmMode && gameOverElementsY == gameOverElementsTargetY) drawTwistChip(canvas, true);
                    else twistChipRect.setEmpty();
                }
                break;
        }
    }

    private void drawLives(Canvas canvas, float top) {
        if (livesLeft <= 0) return;
        Bitmap icon = birdBitmaps[1];
        float iconWidth = icon.getWidth() * LIFE_ICON_SCALE;
        float iconHeight = icon.getHeight() * LIFE_ICON_SCALE;
        float gap = iconWidth * 0.25f;
        float x = (screenWidth - (livesLeft * iconWidth + (livesLeft - 1) * gap)) / 2f;
        for (int i = 0; i < livesLeft; i++) {
            hudRect.set(x, top, x + iconWidth, top + iconHeight);
            canvas.drawBitmap(icon, null, hudRect, pixelPaint);
            x += iconWidth + gap;
        }
    }

    private void drawPauseButton(Canvas canvas) {
        int size = Math.max(1, Math.round(PAUSE_BUTTON_SIZE * scale));
        int right = (int) (screenWidth - safeInsetRight - cornerMargin);
        int top = (int) (systemBarTop + cornerMargin);
        pauseButtonRect.set(right - size, top, right, top + size);

        hudPlatePaint.setAlpha(90);
        canvas.drawRect(pauseButtonRect.left, pauseButtonRect.top, pauseButtonRect.right, pauseButtonRect.bottom, hudPlatePaint);
        float bar = size * 0.18f;
        float inset = size * 0.26f;
        hudIconPaint.setAlpha(235);
        canvas.drawRect(pauseButtonRect.left + inset, top + inset, pauseButtonRect.left + inset + bar, top + size - inset, hudIconPaint);
        canvas.drawRect(pauseButtonRect.right - inset - bar, top + inset, pauseButtonRect.right - inset, top + size - inset, hudIconPaint);
    }

    private void drawPauseOverlay(Canvas canvas) {
        float fade = 1f - (1f - pauseFade) * (1f - pauseFade);
        hudPlatePaint.setAlpha(Math.round(PAUSE_DIM_ALPHA * fade));
        canvas.drawRect(0, 0, screenWidth, screenHeight, hudPlatePaint);

        float height = Math.min(screenWidth, screenHeight) * RESUME_BUTTON_SHARE * (0.85f + 0.15f * fade);
        float width = height * ATLAS_RESUME_WIDTH / ATLAS_RESUME_HEIGHT;
        float centerX = screenWidth / 2f;
        float centerY = playAreaCenterY();
        resumeDestRect.set(centerX - width / 2f, centerY - height / 2f, centerX + width / 2f, centerY + height / 2f);
        int alpha = Math.round(255 * fade);

        if (unscaledButtonResume != null) {
            pixelPaint.setAlpha(alpha);
            canvas.drawBitmap(unscaledButtonResume, null, resumeDestRect, pixelPaint);
            pixelPaint.setAlpha(255);
            return;
        }

        float radius = width * 0.18f;
        hudPlatePaint.setAlpha(Math.round(170 * fade));
        canvas.drawRoundRect(resumeDestRect, radius, radius, hudPlatePaint);
        float side = width * 0.42f;
        float triangleHeight = side * 0.866f;
        float left = centerX - triangleHeight / 3f;
        hudIconPath.reset();
        hudIconPath.moveTo(left, centerY - side / 2f);
        hudIconPath.lineTo(left + triangleHeight, centerY);
        hudIconPath.lineTo(left, centerY + side / 2f);
        hudIconPath.close();
        hudIconPaint.setAlpha(Math.round(240 * fade));
        canvas.drawPath(hudIconPath, hudIconPaint);
    }

    private void drawButton(Canvas canvas, Bitmap bitmap, RectF destRect, float offset, Paint paint) {
        if (offset > 0.5f) {
            float scaleCorr = destRect.height() / bitmap.getHeight();
            int clipBottom = (int) (offset / scaleCorr);
            if (clipBottom >= bitmap.getHeight()) clipBottom = bitmap.getHeight() - 1;
            if (clipBottom < 0) clipBottom = 0;

            buttonSrcRect.set(0, 0, bitmap.getWidth(), bitmap.getHeight() - clipBottom);

            float originalTop = destRect.top;
            destRect.top += offset;
            canvas.drawBitmap(bitmap, buttonSrcRect, destRect, paint);
            destRect.top = originalTop;
        } else {
            canvas.drawBitmap(bitmap, null, destRect, paint);
        }
    }

    private void layoutSettingsButton() {
        float x = screenWidth - safeInsetRight - settingsButtonBitmap.getWidth() - cornerMargin;
        float y = systemBarTop + cornerMargin;
        settingsButtonRect.set((int) x, (int) y,
                (int) (x + settingsButtonBitmap.getWidth()), (int) (y + settingsButtonBitmap.getHeight()));
        settingsButtonDestRect.set(settingsButtonRect);
    }

    private void drawFitted(Canvas canvas, Bitmap bitmap, float top) {
        float fit = fitToWidth(bitmap.getWidth());
        float width = bitmap.getWidth() * fit;
        float left = (screenWidth - width) / 2f;
        centeredBitmapDestRect.set(left, top, left + width, top + bitmap.getHeight() * fit);
        canvas.drawBitmap(bitmap, null, centeredBitmapDestRect, pixelPaint);
    }

    private void drawHomeScreen(Canvas canvas) {
        boolean classic = classicMenu();
        float titleY = classic ? systemBarTop + MENU_TITLE_Y * scale : getPlayableHeight() * 0.22f + systemBarTop;
        drawFitted(canvas, titleBitmap, titleY);

        if (settingsButtonBitmap != null) {
            layoutSettingsButton();
            if (!settingHideSettingsIcon) drawSettingsGear(canvas, 255, 1f);
        }
        if (!filmMode && !boardOpen && !nameOverlayOpen) {
            drawCoinButton(canvas);
            drawTwistChip(canvas, true);
        } else {
            coinButtonRect.setEmpty();
            twistChipRect.setEmpty();
        }

        float originalPlayWidth = playButtonBitmap.getWidth();
        float originalScoreWidth = scoreButtonBitmap.getWidth();
        float scaleCorrection = fitToWidth(originalPlayWidth + originalScoreWidth + homeButtonGap);

        float newPlayWidth = originalPlayWidth * scaleCorrection;
        float newPlayHeight = playButtonBitmap.getHeight() * scaleCorrection;
        float newScoreWidth = originalScoreWidth * scaleCorrection;
        float newScoreHeight = scoreButtonBitmap.getHeight() * scaleCorrection;
        float newGap = homeButtonGap * scaleCorrection;
        float totalNewWidth = newPlayWidth + newScoreWidth + newGap;
        float playBtnX = (screenWidth - totalNewWidth) / 2f;
        float scoreBtnX = playBtnX + newPlayWidth + newGap;
        float buttonsY = classic
                ? systemBarTop + MENU_BUTTONS_Y * scale
                : titleY + titleBitmap.getHeight() + getPlayableHeight() * 0.18f;

        playButtonRect.set((int) playBtnX, (int) buttonsY, (int) (playBtnX + newPlayWidth), (int) (buttonsY + newPlayHeight));
        scoreButtonRect.set((int) scoreBtnX, (int) buttonsY, (int) (scoreBtnX + newScoreWidth), (int) (buttonsY + newScoreHeight));
        playButtonDestRect.set(playButtonRect);
        scoreButtonDestRect.set(scoreButtonRect);

        float scaledPressOffsetY = pressOffsetY * scaleCorrection;
        drawButton(canvas, playButtonBitmap, playButtonDestRect, playBtnProgress * scaledPressOffsetY, pixelPaint);
        drawButton(canvas, scoreButtonBitmap, scoreButtonDestRect, scoreBtnProgress * scaledPressOffsetY, pixelPaint);

        if (classic) {
            float rateWidth = rateButtonBitmap.getWidth() * scaleCorrection;
            float rateHeight = rateButtonBitmap.getHeight() * scaleCorrection;
            float rateX = (screenWidth - rateWidth) / 2f;
            float rateY = systemBarTop + MENU_RATE_Y * scale;
            rateButtonRect.set((int) rateX, (int) rateY, (int) (rateX + rateWidth), (int) (rateY + rateHeight));
            rateButtonDestRect.set(rateButtonRect);
            drawButton(canvas, rateButtonBitmap, rateButtonDestRect, rateBtnProgress * scaledPressOffsetY, pixelPaint);
        } else {
            rateButtonRect.setEmpty();
        }

        float copyrightHeight = copyrightBitmap.getHeight() * fitToWidth(copyrightBitmap.getWidth());
        drawFitted(canvas, copyrightBitmap, groundTopY() + (groundHeight - copyrightHeight) / 2f);
    }

    private void drawGameOverScreen(Canvas canvas) {
        float scaleCorrection = this.uiScaleCorrection;

        float gameOverTextWidth = gameOverBitmap.getWidth() * scaleCorrection;
        float gameOverTextHeight = gameOverBitmap.getHeight() * scaleCorrection;
        float gameOverTextX = (screenWidth - gameOverTextWidth) / 2f;
        float gameOverTextY = gameOverElementsY;
        gameOverDestRect.set(gameOverTextX, gameOverTextY, gameOverTextX + gameOverTextWidth, gameOverTextY + gameOverTextHeight);
        canvas.drawBitmap(gameOverBitmap, null, gameOverDestRect, pixelPaint);

        float panelWidth = scorePanelBitmap.getWidth() * scaleCorrection;
        float panelHeight = scorePanelBitmap.getHeight() * scaleCorrection;
        float gap = (20 * scale) * scaleCorrection;
        float panelX = (screenWidth - panelWidth) / 2f;
        float panelY = gameOverTextY + gameOverTextHeight + gap;
        panelDestRect.set(panelX, panelY, panelX + panelWidth, panelY + panelHeight);
        canvas.drawBitmap(scorePanelBitmap, null, panelDestRect, pixelPaint);

        float panelRightEdge = panelX + panelWidth - (22 * scale * scaleCorrection);
        float scoreY = panelY + (SCORE_PANEL_CURRENT_SCORE_Y_OFFSET * scale * scaleCorrection);
        float highScoreY = panelY + (SCORE_PANEL_HIGH_SCORE_Y_OFFSET * scale * scaleCorrection);
        drawPanelDigits(canvas, displayedScore, panelRightEdge, scoreY, scaleCorrection);
        boolean countUpFinished = displayedScore == score;
        int shownBest = isNewBest && !countUpFinished ? previousBest : highScore;
        drawPanelDigits(canvas, shownBest, panelRightEdge, highScoreY, scaleCorrection);

        if (isNewBest && countUpFinished && newLabelBitmap != null) {
            float labelX = panelX + SCORE_PANEL_NEW_X_OFFSET * scale * scaleCorrection;
            float labelY = panelY + SCORE_PANEL_NEW_Y_OFFSET * scale * scaleCorrection;
            newLabelDestRect.set(labelX, labelY, labelX + newLabelBitmap.getWidth() * scaleCorrection,
                    labelY + newLabelBitmap.getHeight() * scaleCorrection);
            canvas.drawBitmap(newLabelBitmap, null, newLabelDestRect, pixelPaint);
        }

        if (currentMedal != MEDAL_NONE && countUpFinished) {
            Bitmap medalBitmap = medalBitmaps[currentMedal];
            float medalWidth = medalBitmap.getWidth() * scaleCorrection;
            float medalHeight = medalBitmap.getHeight() * scaleCorrection;
            float medalX = panelX + (SCORE_PANEL_MEDAL_X_OFFSET * scale * scaleCorrection);
            float medalY = panelY + (SCORE_PANEL_MEDAL_Y_OFFSET * scale * scaleCorrection);

            if (currentMedal == MEDAL_PLATINUM) {
                medalX += PLATINUM_OFFSET_X * scale * scaleCorrection;
                medalY += PLATINUM_OFFSET_Y * scale * scaleCorrection;
            } else if (currentMedal == MEDAL_GOLD) {
                medalX += GOLD_OFFSET_X * scale * scaleCorrection;
                medalY += GOLD_OFFSET_Y * scale * scaleCorrection;
            }

            medalDestRect.set(medalX, medalY, medalX + medalWidth, medalY + medalHeight);
            canvas.drawBitmap(medalBitmap, null, medalDestRect, pixelPaint);

            if (isSparkleAnimating) {
                float shineSize = 30 * scale * scaleCorrection * sparkleScaleCurrent;
                float centerX = medalX + (medalWidth * sparkleXOffset);
                float centerY = medalY + (medalHeight * sparkleYOffset);
                float left = centerX - (shineSize / 2f);
                float top = centerY - (shineSize / 2f);
                sparkleDest.set(left, top, left + shineSize, top + shineSize);

                canvas.save();
                canvas.rotate(sparkleTimer * 100, centerX, centerY);
                canvas.drawBitmap(sparkleBitmap, null, sparkleDest, smoothPaint);
                canvas.restore();
            }
        }

        if (countUpFinished) {
            float playButtonWidth = playButtonBitmap.getWidth() * scaleCorrection;
            float playButtonHeight = playButtonBitmap.getHeight() * scaleCorrection;
            float btnX = (screenWidth - playButtonWidth) / 2f;
            float btnY = panelY + panelHeight + (15 * scale * scaleCorrection);
            if (settingClassicMenuEnabled) {
                float scoreButtonWidth = scoreButtonBitmap.getWidth() * scaleCorrection;
                float scoreButtonHeight = scoreButtonBitmap.getHeight() * scaleCorrection;
                float buttonGap = Math.min(homeButtonGap * scaleCorrection,
                        Math.max(0f, uiAvailableWidth - playButtonWidth - scoreButtonWidth));
                btnX = (screenWidth - playButtonWidth - scoreButtonWidth - buttonGap) / 2f;
                float scoreX = btnX + playButtonWidth + buttonGap;
                scoreButtonRect.set((int) scoreX, (int) btnY, (int) (scoreX + scoreButtonWidth), (int) (btnY + scoreButtonHeight));
                scoreButtonDestRect.set(scoreButtonRect);
                drawButton(canvas, scoreButtonBitmap, scoreButtonDestRect, scoreBtnProgress * (pressOffsetY * scaleCorrection), pixelPaint);
            } else {
                scoreButtonRect.setEmpty();
            }
            playButtonRect.set((int) btnX, (int) btnY, (int) (btnX + playButtonWidth), (int) (btnY + playButtonHeight));

            restartBtnDestRect.set(playButtonRect);
            drawButton(canvas, playButtonBitmap, restartBtnDestRect, okBtnProgress * (pressOffsetY * scaleCorrection), pixelPaint);
        }

        if (settingsButtonBitmap != null && gameOverSettingsAnimProgress > 0) {
            layoutSettingsButton();
            if (!settingHideSettingsIcon) {
                float popScale = (float) (Math.sin(gameOverSettingsAnimProgress * Math.PI / 2));
                drawSettingsGear(canvas, (int) (255 * gameOverSettingsAnimProgress), popScale);
            }
        }
    }

    private int splitDigits(int number) {
        int remaining = Math.max(0, number);
        int count = 0;
        do {
            digitBuffer[count++] = remaining % 10;
            remaining /= 10;
        } while (remaining > 0);
        return count;
    }

    private void drawScoreWithImages(Canvas canvas, int number, int x, int y) {
        drawScoreWithImages(canvas, number, x, y, pixelPaint);
    }

    private void drawScoreWithImages(Canvas canvas, int number, int x, int y, Paint paint) {
        int count = splitDigits(number);
        float overlap = Math.round(SCORE_DIGIT_OVERLAP * scale);
        float totalWidth = 0;
        for (int i = 0; i < count; i++) totalWidth += numberBitmaps[digitBuffer[i]].getWidth();
        totalWidth -= overlap * (count - 1);

        float currentX = Math.round(x - (totalWidth / 2f));
        for (int i = count - 1; i >= 0; i--) {
            Bitmap digitBitmap = numberBitmaps[digitBuffer[i]];
            canvas.drawBitmap(digitBitmap, currentX, y - (digitBitmap.getHeight() / 2f), paint);
            currentX += digitBitmap.getWidth() - overlap;
        }
    }

    private void drawPanelDigits(Canvas canvas, int number, float rightX, float bottomY, float scaleCorrection) {
        if (panelNumberBitmaps[0] == null) {
            drawScoreWithImagesRightAligned(canvas, number, rightX, bottomY, smallNumberBitmaps, scaleCorrection);
            return;
        }
        int count = splitDigits(number);
        float gap = PANEL_DIGIT_GAP * scale * scaleCorrection;
        float currentX = rightX;
        for (int i = 0; i < count; i++) {
            Bitmap digitBitmap = panelNumberBitmaps[digitBuffer[i]];
            float digitWidth = digitBitmap.getWidth() * scaleCorrection;
            float digitHeight = digitBitmap.getHeight() * scaleCorrection;
            currentX -= digitWidth;
            scoreDigitDestRect.set(currentX, bottomY - digitHeight, currentX + digitWidth, bottomY);
            canvas.drawBitmap(digitBitmap, null, scoreDigitDestRect, pixelPaint);
            currentX -= gap;
        }
    }

    private void drawScoreWithImagesRightAligned(Canvas canvas, int number, float rightX, float y, Bitmap[] numberSet, float scaleCorrection) {
        drawDigitsRightAligned(canvas, number, rightX, y, numberSet, scaleCorrection, pixelPaint);
    }

    private void drawDigitsRightAligned(Canvas canvas, int number, float rightX, float y, Bitmap[] numberSet, float scaleCorrection, Paint paint) {
        int count = splitDigits(number);
        float currentX = rightX;
        for (int i = 0; i < count; i++) {
            Bitmap digitBitmap = numberSet[digitBuffer[i]];
            float digitWidth = digitBitmap.getWidth() * scaleCorrection;
            float digitHeight = digitBitmap.getHeight() * scaleCorrection;
            currentX -= digitWidth;
            scoreDigitDestRect.set(currentX, y - digitHeight, currentX + digitWidth, y);
            canvas.drawBitmap(digitBitmap, null, scoreDigitDestRect, paint);
        }
    }

    private void drawGetReady(Canvas canvas, int alpha) {
        if (alpha <= 0) return;
        readyPaint.setAlpha(alpha);
        float scoreCenterY = systemBarTop + (int) (getPlayableHeight() * 0.15);
        float scoreBottom = scoreCenterY + numberBitmaps[0].getHeight() / 2f;
        float scaleCorrection = fitToWidth(getReadyBitmap.getWidth());
        float width = getReadyBitmap.getWidth() * scaleCorrection;
        float height = getReadyBitmap.getHeight() * scaleCorrection;
        float left = (screenWidth - width) / 2f;
        float top = (systemBarTop + (groundTopY() - systemBarTop) / 2f) - height / 2f - (int) (getPlayableHeight() * 0.15) * scaleCorrection;
        boolean guide = false;
        float tutorialLeft = 0f, tutorialTop = 0f, tutorialWidth = 0f, tutorialHeight = 0f;
        if (tutorialBitmap != null && !settingUpsideDownEnabled) {
            float fit = fitToWidth(tutorialBitmap.getWidth());
            tutorialWidth = tutorialBitmap.getWidth() * fit;
            tutorialHeight = tutorialBitmap.getHeight() * fit;
            tutorialLeft = Math.round((screenWidth - tutorialWidth) / 2f);
            tutorialTop = Math.round(waitingBirdAnchorY() - TUTORIAL_ABOVE_BIRD * scale * fit);
            guide = tutorialTop >= scoreBottom && tutorialTop + tutorialHeight <= groundTopY();
        }
        if (guide) {
            float room = tutorialTop - scoreBottom - height;
            top = room >= 0f ? scoreBottom + room * READY_SPACE_ABOVE : tutorialTop - height;
            top = Math.max(top, scoreBottom);
        } else {
            top = Math.max(top, scoreBottom);
        }
        top = Math.round(top);
        readyDestRect.set(left, top, left + width, top + height);
        if (settingShowGetReady) canvas.drawBitmap(getReadyBitmap, null, readyDestRect, readyPaint);
        if (guide && settingShowTapGuide) {
            readyDestRect.set(tutorialLeft, tutorialTop, tutorialLeft + tutorialWidth, tutorialTop + tutorialHeight);
            canvas.drawBitmap(tutorialBitmap, null, readyDestRect, readyPaint);
        }
        if (gameState != GameState.PLAYING && settingShowScore) {
            drawScoreWithImages(canvas, 0, screenWidth / 2, (int) scoreCenterY, readyPaint);
        }
    }

    private float waitingBirdAnchorY() {
        if (settingStartHeight == DEFAULT_START_HEIGHT) return playAreaCenterY();
        float share = 1f - Math.max(10, Math.min(90, settingStartHeight)) / 100f;
        return systemBarTop + (getPlayableHeight() - groundHeight) * share;
    }

    private void drawCenteredBitmap(Canvas canvas, Bitmap bitmap, int yOffset) {
        float scaleCorrection = fitToWidth(bitmap.getWidth());
        float newWidth = bitmap.getWidth() * scaleCorrection;
        float newHeight = bitmap.getHeight() * scaleCorrection;
        float left = (screenWidth - newWidth) / 2f;
        float groundY = groundTopY();
        float top = (systemBarTop + (groundY - systemBarTop) / 2f) - (newHeight / 2f) + (yOffset * scaleCorrection);
        centeredBitmapDestRect.set(left, top, left + newWidth, top + newHeight);
        canvas.drawBitmap(bitmap, null, centeredBitmapDestRect, pixelPaint);
    }

    @Override
    public boolean onGenericMotionEvent(MotionEvent event) {
        if (coinOpen && event.getActionMasked() == MotionEvent.ACTION_SCROLL) {
            float amount = event.getAxisValue(MotionEvent.AXIS_VSCROLL);
            if (amount != 0f) scrollCoins(-amount * 1.5f);
            return true;
        }
        if (boardOpen && !nameOverlayOpen && event.getActionMasked() == MotionEvent.ACTION_SCROLL) {
            float amount = event.getAxisValue(MotionEvent.AXIS_VSCROLL);
            if (amount != 0f && scrollBoard(-amount * 3f)) return true;
        }
        return super.onGenericMotionEvent(event);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (filmMode) return false;
        if (!isReady) return true;
        int touchX = (int) event.getX();
        int touchY = (int) event.getY();

        int action = event.getActionMasked();
        if (event.getToolType(0) == MotionEvent.TOOL_TYPE_MOUSE) hasMouse = true;
        if (nameOverlayOpen) return true;
        if (coinOpen) return onCoinsTouch(action, touchX, touchY);
        if (boardOpen) return onBoardTouch(action, touchX, touchY);
        if (action == MotionEvent.ACTION_DOWN && isSecondaryClick(event)) {
            secondaryClickHeld = true;
            if (settingRightClickPauses) togglePause();
            return true;
        }
        if (secondaryClickHeld) {
            if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) secondaryClickHeld = false;
            return true;
        }

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                touchHeld = true;
                refreshHeld();
                onFingerDown(touchX, touchY);
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                handleGameplayTap();
                break;
            case MotionEvent.ACTION_MOVE:
                if (pressedButtonRect != null && !pressedButtonRect.contains(touchX, touchY)) cancelButtonPress();
                break;
            case MotionEvent.ACTION_POINTER_UP:
                cancelButtonPress();
                break;
            case MotionEvent.ACTION_UP:
                touchHeld = false;
                refreshHeld();
                onFingerUp(touchX, touchY);
                break;
            case MotionEvent.ACTION_CANCEL:
                touchHeld = false;
                refreshHeld();
                cancelButtonPress();
                break;
        }
        return true;
    }

    private static boolean isSecondaryClick(MotionEvent event) {
        if (event.getToolType(0) != MotionEvent.TOOL_TYPE_MOUSE) return false;
        int buttons = event.getButtonState();
        return (buttons & MotionEvent.BUTTON_SECONDARY) != 0 && (buttons & MotionEvent.BUTTON_PRIMARY) == 0;
    }

    @Override
    public boolean onHoverEvent(MotionEvent event) {
        if (!filmMode && event.getToolType(0) == MotionEvent.TOOL_TYPE_MOUSE) {
            hasMouse = true;
            if (event.getActionMasked() == MotionEvent.ACTION_HOVER_EXIT) {
                hoverX = -1f;
                hoverY = -1f;
            } else {
                hoverX = event.getX();
                hoverY = event.getY();
                cursorIdleSec = 0f;
            }
            updateCursor();
        }
        return super.onHoverEvent(event);
    }

    private void updateCursor() {
        if (!hasMouse || Build.VERSION.SDK_INT < Build.VERSION_CODES.N) return;
        int type;
        boolean running = (gameState == GameState.PLAYING && !isPaused) || gameState == GameState.WAITING;
        if (running && settingHideCursor && cursorIdleSec >= CURSOR_HIDE_DELAY_SEC) type = PointerIcon.TYPE_NULL;
        else if (overButton((int) hoverX, (int) hoverY)) type = PointerIcon.TYPE_HAND;
        else type = PointerIcon.TYPE_ARROW;
        if (type == cursorType) return;
        cursorType = type;
        setPointerIcon(PointerIcon.getSystemIcon(getContext(), type));
    }

    private boolean overButton(int x, int y) {
        if (x < 0 || y < 0) return false;
        if (coinOpen) return coinHitAt(x, y) != null;
        if (boardOpen && (nameOverlayOpen || nameSpring[0] >= 0.02f)) return false;
        if (boardOpen) return boardCloseRect.contains(x, y) || boardJoinRect.contains(x, y)
                || boardTabAllRect.contains(x, y) || boardTabWeekRect.contains(x, y) || boardToggleRect.contains(x, y)
                || boardYouRect.contains(x, y);
        if (gameState == GameState.HOME) {
            return playButtonRect.contains(x, y) || scoreButtonRect.contains(x, y)
                    || rateButtonRect.contains(x, y) || settingsButtonRect.contains(x, y) || coinButtonRect.contains(x, y)
                    || twistChipRect.contains(x, y);
        }
        if (gameState == GameState.PANEL_SLIDING) {
            if (gameOverElementsY != gameOverElementsTargetY || isRestarting) return false;
            boolean counted = displayedScore == score;
            return (counted && playButtonRect.contains(x, y))
                    || (counted && settingClassicMenuEnabled && scoreButtonRect.contains(x, y))
                    || (isGameOverIconAnimationDone && settingsButtonRect.contains(x, y)) || twistChipRect.contains(x, y);
        }
        if (gameState == GameState.PLAYING) {
            if (isPaused) return resumeDestRect.contains(x, y);
            return settingPauseButtonEnabled && pauseButtonRect.contains(x, y);
        }
        return false;
    }

    private void refreshHeld() {
        isScreenPressed = touchHeld || flapKeyHeld || flapAltKeyHeld || padKeyHeld;
    }

    void releaseHeldInput() {
        touchHeld = false;
        flapKeyHeld = false;
        flapAltKeyHeld = false;
        padKeyHeld = false;
        secondaryClickHeld = false;
        refreshHeld();
        if (keyButtonCode != KeyEvent.KEYCODE_UNKNOWN) cancelButtonPress();
    }

    boolean onGameKey(int keyCode, KeyEvent event, boolean down) {
        if (!isReady || !settingKeyboardEnabled || keyCode == KeyEvent.KEYCODE_UNKNOWN || event == null || nameOverlayOpen) return false;
        boolean running = gameState == GameState.PLAYING;
        boolean flapKey = keyCode == keyFlap || keyCode == keyFlapAlt || keyCode == KeyEvent.KEYCODE_BUTTON_A;
        boolean playKey = keyCode == keyPlay || (keyPlay == KeyEvent.KEYCODE_ENTER && keyCode == KeyEvent.KEYCODE_NUMPAD_ENTER)
                || (keyCode == KeyEvent.KEYCODE_BUTTON_START && !running);
        boolean settingsKey = keyCode == keySettings;
        boolean pauseKey = keyCode == keyPause || ((keyCode == KeyEvent.KEYCODE_ESCAPE || keyCode == KeyEvent.KEYCODE_BUTTON_START) && running);
        if (coinOpen) return onCoinsKey(keyCode, event, down);
        if (boardOpen) {
            boolean closes = flapKey || playKey || keyCode == KeyEvent.KEYCODE_ESCAPE || keyCode == KeyEvent.KEYCODE_ENTER;
            if (closes && down && event.getRepeatCount() == 0) closeBoard();
            if (!closes) {
                float rows = keyCode == KeyEvent.KEYCODE_DPAD_UP ? -1f : keyCode == KeyEvent.KEYCODE_DPAD_DOWN ? 1f
                        : keyCode == KeyEvent.KEYCODE_PAGE_UP ? -BOARD_ROWS : keyCode == KeyEvent.KEYCODE_PAGE_DOWN ? BOARD_ROWS
                        : keyCode == KeyEvent.KEYCODE_MOVE_HOME ? -100000f : keyCode == KeyEvent.KEYCODE_MOVE_END ? 100000f : 0f;
                if (rows != 0f) {
                    if (down) scrollBoard(rows);
                    return true;
                }
                if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT || keyCode == KeyEvent.KEYCODE_DPAD_RIGHT || keyCode == KeyEvent.KEYCODE_TAB
                        || keyCode == KeyEvent.KEYCODE_BUTTON_L1 || keyCode == KeyEvent.KEYCODE_BUTTON_R1) {
                    if (down && event.getRepeatCount() == 0 && Leaderboard.configured()) {
                        int tab = keyCode == KeyEvent.KEYCODE_DPAD_LEFT || keyCode == KeyEvent.KEYCODE_BUTTON_L1 ? 1
                                : keyCode == KeyEvent.KEYCODE_DPAD_RIGHT || keyCode == KeyEvent.KEYCODE_BUTTON_R1 ? 0 : 1 - boardTab;
                        if (tab != boardTab) {
                            playSound(soundSwooshing);
                            switchBoard(tab, boardShowAll);
                        }
                    }
                    return true;
                }
            }
            return closes || settingsKey || pauseKey;
        }
        if (!flapKey && !playKey && !settingsKey && !(pauseKey && running)) return false;
        if (down && event.getRepeatCount() > 0) return true;
        if (pauseKey && running) {
            if (down) togglePause();
            return true;
        }
        return down ? keyDown(keyCode, flapKey, playKey, settingsKey) : keyUp(keyCode, flapKey);
    }

    private boolean keyDown(int keyCode, boolean flapKey, boolean playKey, boolean settingsKey) {
        if (gameState == GameState.HOME) {
            if (flapKey || playKey) return pressByKey(playButtonRect, keyCode);
            return settingsKey && pressByKey(settingsButtonRect, keyCode);
        }
        if (gameState == GameState.PANEL_SLIDING) {
            boolean ready = gameOverElementsY == gameOverElementsTargetY && !isRestarting;
            if ((flapKey || playKey) && ready && displayedScore == score) return pressByKey(playButtonRect, keyCode);
            if (settingsKey && ready && isGameOverIconAnimationDone) return pressByKey(settingsButtonRect, keyCode);
            return flapKey || playKey;
        }
        if (gameState == GameState.PLAYING && isPaused) {
            if (!flapKey && !playKey) return false;
            resumeFromPause();
            if (flapKey) {
                holdKey(keyCode, true);
                handleGameplayTap();
            }
            return true;
        }
        if (gameState == GameState.WAITING || gameState == GameState.PLAYING) {
            if (flapKey) {
                holdKey(keyCode, true);
                handleGameplayTap();
                return true;
            }
            if (playKey && gameState == GameState.WAITING) handleGameplayTap();
            return playKey;
        }
        return flapKey || playKey;
    }

    private boolean keyUp(int keyCode, boolean flapKey) {
        if (keyCode == keyButtonCode && pressedButtonRect != null) {
            Rect button = pressedButtonRect;
            keyButtonCode = KeyEvent.KEYCODE_UNKNOWN;
            onFingerUp(button.centerX(), button.centerY());
            return true;
        }
        if (flapKey) holdKey(keyCode, false);
        return true;
    }

    private boolean pressByKey(Rect button, int keyCode) {
        if (button.isEmpty()) return false;
        pressedButtonRect = button;
        keyButtonCode = keyCode;
        applyButtonTargets();
        invalidate();
        return true;
    }

    private void holdKey(int keyCode, boolean held) {
        if (keyCode == keyFlap) flapKeyHeld = held;
        else if (keyCode == keyFlapAlt) flapAltKeyHeld = held;
        else if (keyCode == KeyEvent.KEYCODE_BUTTON_A) padKeyHeld = held;
        refreshHeld();
    }

    private void togglePause() {
        if (gameState != GameState.PLAYING) return;
        if (isPaused) resumeFromPause();
        else pauseGame();
        feel(Feel.PAUSE);
    }

    boolean pauseForBack() {
        if (filmMode || !isReady || gameState != GameState.PLAYING || isPaused) return false;
        pauseGame();
        feel(Feel.PAUSE);
        return true;
    }

    private void resumeFromPause() {
        if (!isPaused) return;
        if (gameState == GameState.PLAYING) commitMark(MARK_RESUME);
        isPaused = false;
        trackPause();
        updateWeatherSounds();
        invalidate();
    }

    private void onFingerDown(int x, int y) {
        pressedButtonRect = null;
        keyButtonCode = KeyEvent.KEYCODE_UNKNOWN;

        if (gameState == GameState.HOME) {
            if (playButtonRect.contains(x, y)) pressedButtonRect = playButtonRect;
            else if (scoreButtonRect.contains(x, y)) pressedButtonRect = scoreButtonRect;
            else if (rateButtonRect.contains(x, y)) pressedButtonRect = rateButtonRect;
            else if (settingsButtonRect.contains(x, y)) pressedButtonRect = settingsButtonRect;
            else if (coinButtonRect.contains(x, y)) pressedButtonRect = coinButtonRect;
            else if (twistChipRect.contains(x, y)) pressedButtonRect = twistChipRect;
        } else if (gameState == GameState.PANEL_SLIDING) {
            if (gameOverElementsY == gameOverElementsTargetY && !isRestarting && twistChipRect.contains(x, y)) {
                pressedButtonRect = twistChipRect;
            } else if (gameOverElementsY == gameOverElementsTargetY && !isRestarting) {
                if (displayedScore == score && playButtonRect.contains(x, y)) pressedButtonRect = playButtonRect;
                else if (displayedScore == score && settingClassicMenuEnabled && scoreButtonRect.contains(x, y)) pressedButtonRect = scoreButtonRect;
                if (isGameOverIconAnimationDone && settingsButtonRect.contains(x, y)) pressedButtonRect = settingsButtonRect;
            }
        } else if (isPaused) {
            resumeFromPause();
            handleGameplayTap();
        } else if (gameState == GameState.PLAYING && settingPauseButtonEnabled && pauseButtonRect.contains(x, y)) {
            pauseGame();
            feel(Feel.PAUSE);
        } else {
            handleGameplayTap();
        }

        applyButtonTargets();
        if (pressedButtonRect != null) {
            feel(Feel.PRESS);
            invalidate();
        }
    }

    private void applyButtonTargets() {
        boolean onHome = gameState == GameState.HOME;
        boolean onPanel = gameState == GameState.PANEL_SLIDING;
        playBtnTarget = (pressedButtonRect == playButtonRect && onHome) ? 1f : 0f;
        scoreBtnTarget = (pressedButtonRect == scoreButtonRect) ? 1f : 0f;
        rateBtnTarget = (pressedButtonRect == rateButtonRect) ? 1f : 0f;
        settingsBtnTarget = (pressedButtonRect == settingsButtonRect && onHome) ? 1f : 0f;
        coinBtnTarget = (pressedButtonRect == coinButtonRect && onHome) ? 1f : 0f;
        okBtnTarget = (pressedButtonRect == playButtonRect && onPanel) ? 1f : 0f;
        gameOverSettingsBtnTarget = (pressedButtonRect == settingsButtonRect && onPanel) ? 1f : 0f;
        if (pressedButtonRect == settingsButtonRect && (onHome || onPanel) && !settingsOpening && settingsSpinTarget % 360f == 0f) {
            settingsSpinTarget = Math.max(settingsSpinTarget, settingsSpin) + SETTINGS_PRESS_TURN;
        }
    }

    private void handleGameplayTap() {
        if (isPaused) return;
        boolean playerFlaps = !settingJetpackModeEnabled && !settingAutopilotEnabled;
        if (gameState == GameState.WAITING) {
            gameState = GameState.PLAYING;
            roundStartClock = clock;
            roundFlaps = 0;
            roundGoldenPoints = 0;
            roundLivesUsed = 0;
            roundAssists.clear();
            guardScore();
            startRoundChecks();
            offScreenSeconds = 0f;
            roundEndCause = "";
            roundFrames = 0;
            roundSlowFrames = 0;
            roundFrameSeconds = 0f;
            roundWorstFrame = 0f;
            roundSlowFrameLimit = 1.5f / targetFrameRate();
            noteAssists();
            updateWeatherSounds();
            if (playerFlaps) {
                if (replayLive) recordEvent(REPLAY_FLAP);
                flap();
            } else if (replayLive && !replayForBoard) {
                recordEvent(REPLAY_MARK | MARK_START);
            }
        } else if (gameState == GameState.PLAYING) {
            if (playerFlaps) {
                if (replayLive) recordEvent(REPLAY_FLAP);
                flap();
            }
        }
    }

    private void pauseGame() {
        if (gameState != GameState.PLAYING) return;
        if (!isPaused) commitMark(MARK_PAUSE);
        isPaused = true;
        trackPause();
        pauseFade = 0f;
        touchHeld = false;
        flapKeyHeld = false;
        flapAltKeyHeld = false;
        padKeyHeld = false;
        refreshHeld();
        stopWeatherSounds();
        invalidate();
    }

    private void onFingerUp(int x, int y) {
        Rect button = pressedButtonRect;
        if (button != null && button.contains(x, y)) {
            if (gameState == GameState.HOME || gameState == GameState.PANEL_SLIDING) feel(Feel.BUTTON);
            if (gameState == GameState.HOME) {
                playSound(soundSwooshing);
                if (button == playButtonRect) {
                    gameState = GameState.TRANSITION_TO_WAITING;
                    isFadingOut = true;
                } else if (button == scoreButtonRect) {
                    openBoard();
                } else if (button == rateButtonRect) {
                    openRatePage();
                } else if (button == settingsButtonRect) {
                    openSettingsSoon();
                } else if (button == coinButtonRect) {
                    openCoins();
                    if (boardHost != null) boardHost.boardVisible(true);
                } else if (button == twistChipRect) {
                    endTwist(true);
                }
            } else if (gameState == GameState.PANEL_SLIDING) {
                playSound(soundSwooshing);
                if (button == twistChipRect) {
                    endTwist(true);
                } else if (button == playButtonRect) {
                    isRestarting = true;
                } else if (button == scoreButtonRect) {
                    openBoard();
                } else if (button == settingsButtonRect) {
                    openSettingsSoon();
                }
            }
        }
        cancelButtonPress();
    }

    private void openRatePage() {
        try {
            Intent page = new Intent(Intent.ACTION_VIEW, Uri.parse(RATE_URL));
            page.addCategory(Intent.CATEGORY_BROWSABLE);
            getContext().startActivity(page);
        } catch (ActivityNotFoundException | SecurityException e) {
            Toast.makeText(getContext(), RATE_MESSAGE, Toast.LENGTH_SHORT).show();
        }
    }

    private void cancelButtonPress() {
        keyButtonCode = KeyEvent.KEYCODE_UNKNOWN;
        playBtnTarget = 0f;
        scoreBtnTarget = 0f;
        rateBtnTarget = 0f;
        settingsBtnTarget = 0f;
        coinBtnTarget = 0f;
        okBtnTarget = 0f;
        gameOverSettingsBtnTarget = 0f;
        if (!settingsOpening && settingsSpinTarget % 360f != 0f) settingsSpinTarget = spinBase();
        if (pressedButtonRect != null) {
            pressedButtonRect = null;
            invalidate();
        }
    }

    private void openSettingsSoon() {
        if (settingsOpening) return;
        settingsOpening = true;
        settingsSpinTarget = spinBase() + 360f;
        postDelayed(() -> {
            settingsOpening = false;
            openSettings();
        }, SETTINGS_OPEN_DELAY_MS);
    }

    private void openSettings() {
        getContext().startActivity(new Intent(getContext(), SettingsActivity.class));
    }

    private void flap() {
        if (!canFlapAt(birdY)) return;
        birdVelocityY = flapVelocityPerSec;
        roundFlaps++;
        kickTilt();
        pastBirdX = birdX;
        pastBirdY = birdY;
        pastBirdRotation = birdRotation;
        playSound(soundWing);
        feel(Feel.FLAP);
        lastFlapTime = clock;
    }

    private void loadSounds() {
        AudioAttributes attributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();
        soundPool = new SoundPool.Builder().setMaxStreams(10).setAudioAttributes(attributes).build();
        soundWing = soundPool.load(getContext(), R.raw.wing, 1);
        soundPoint = soundPool.load(getContext(), R.raw.point, 1);
        soundHit = soundPool.load(getContext(), R.raw.hit, 1);
        soundDie = soundPool.load(getContext(), R.raw.die, 1);
        soundSwooshing = soundPool.load(getContext(), R.raw.swooshing, 1);
        soundThunder = soundPool.load(getContext(), R.raw.thunder, 1);
        soundExecutor = Executors.newSingleThreadExecutor();

        rainSoundPlayer = MediaPlayer.create(getContext(), R.raw.rain);
        if (rainSoundPlayer != null) rainSoundPlayer.setLooping(true);

        stormSoundPlayer = MediaPlayer.create(getContext(), R.raw.storm_backtrack);
        if (stormSoundPlayer != null) stormSoundPlayer.setLooping(true);
    }

    private void releaseSounds() {
        if (soundExecutor != null) {
            soundExecutor.shutdownNow();
            soundExecutor = null;
        }
        if (soundPool != null) {
            soundPool.release();
            soundPool = null;
        }
        if (rainSoundPlayer != null) {
            rainSoundPlayer.release();
            rainSoundPlayer = null;
        }
        if (stormSoundPlayer != null) {
            stormSoundPlayer.release();
            stormSoundPlayer = null;
        }
    }

    private static void setLoopPlaying(MediaPlayer player, boolean shouldPlay) {
        if (player == null) return;
        if (shouldPlay && !player.isPlaying()) player.start();
        else if (!shouldPlay && player.isPlaying()) player.pause();
    }

    private void updateWeatherSounds() {
        boolean inRound = (gameState == GameState.PLAYING && !isPaused) || gameState == GameState.WAITING;
        boolean audible = settingSoundEnabled && (filmMode ? filmAudible || filmSink != null : hostResumed) && inRound;
        if (filmMode && filmSink != null) {
            float level = Math.max(0, Math.min(100, settingWeatherVolume)) / 100f;
            filmSink.loop(R.raw.rain, audible && (settingWeatherEffect == WEATHER_RAIN || settingWeatherEffect == WEATHER_STORM), filmTime, level);
            filmSink.loop(R.raw.storm_backtrack, audible && settingWeatherEffect == WEATHER_STORM, filmTime, level);
            return;
        }
        boolean rain = audible && (settingWeatherEffect == WEATHER_RAIN || settingWeatherEffect == WEATHER_STORM);
        boolean storm = audible && settingWeatherEffect == WEATHER_STORM;
        float volume = Math.max(0, Math.min(100, settingWeatherVolume)) / 100f;
        try {
            if (rain && rainSoundPlayer != null) rainSoundPlayer.setVolume(volume, volume);
            if (storm && stormSoundPlayer != null) stormSoundPlayer.setVolume(volume, volume);
            setLoopPlaying(rainSoundPlayer, rain);
            setLoopPlaying(stormSoundPlayer, storm);
        } catch (IllegalStateException e) {
            Log.e(TAG, "MediaPlayer state error in updateWeatherSounds", e);
            logProblem("Weather sound error: " + e);
        }
    }

    private void stopWeatherSounds() {
        if (filmMode && filmSink != null) {
            filmSink.loop(R.raw.rain, false, filmTime, 0f);
            filmSink.loop(R.raw.storm_backtrack, false, filmTime, 0f);
            return;
        }
        try {
            setLoopPlaying(rainSoundPlayer, false);
            setLoopPlaying(stormSoundPlayer, false);
        } catch (IllegalStateException e) {
            Log.e(TAG, "MediaPlayer state error in stopWeatherSounds", e);
            logProblem("Weather sound error while stopping: " + e);
        }
    }

    private int rawSound(int soundId) {
        if (soundId == 0) return 0;
        if (soundId == soundWing) return R.raw.wing;
        if (soundId == soundPoint) return R.raw.point;
        if (soundId == soundHit) return R.raw.hit;
        if (soundId == soundDie) return R.raw.die;
        if (soundId == soundSwooshing) return R.raw.swooshing;
        if (soundId == soundThunder) return R.raw.thunder;
        return 0;
    }

    private void playSound(final int soundId) {
        if (filmMode) {
            if (!settingSoundEnabled) return;
            float level = Math.max(0, Math.min(100, settingSoundVolume)) / 100f;
            if (filmSink != null) {
                int raw = rawSound(soundId);
                if (raw != 0 && level > 0f) filmSink.sound(raw, filmTime, level);
                return;
            }
            if (!filmAudible) return;
        }
        final SoundPool pool = soundPool;
        final ExecutorService executor = soundExecutor;
        if (!settingSoundEnabled || !(filmMode ? filmAudible : hostResumed) || soundId == 0) return;
        if (pool == null || executor == null || executor.isShutdown()) return;
        final float volume = Math.max(0, Math.min(100, settingSoundVolume)) / 100f;
        if (volume <= 0f) return;
        executor.execute(() -> pool.play(soundId, volume, volume, 0, 0, 1));
    }

    public void resume() {
        hostResumed = true;
        settingsOpening = false;
        if (isRunning || !isReady) return;

        if (gameState == GameState.PANEL_SLIDING) {
            if (!returningHome) returnHoldSec = transitionAlpha > 0f ? 0f : RETURN_HOME_HOLD_SEC;
            returningHome = true;
            isRestarting = true;
            cancelButtonPress();
        } else {
            loadSettings();
            if (gameState == GameState.HOME) {
                resetGame();
                transitionAlpha = 0;
                isFadingOut = false;
            } else {
                updateWeatherSounds();
            }
        }
        startLoop();
    }

    void focusLost() {
        releaseHeldInput();
        if (!filmMode && settingAutoPauseEnabled && gameState == GameState.PLAYING && !isPaused) pauseGame();
    }

    public void pause() {
        hostResumed = false;
        releaseHeldInput();
        if (settingAutoPauseEnabled) pauseGame();
        cancelButtonPress();
        stopLoop();
        removeCallbacks(playDieSound);
        stopWeatherSounds();
    }

    private void startLoop() {
        if (isRunning) return;
        isRunning = true;
        trackPause();
        lastFrameTimeNanos = 0;
        stepAccumulator = 0f;
        choreographer().postFrameCallback(this);
    }

    private void stopLoop() {
        isRunning = false;
        trackPause();
        choreographer().removeFrameCallback(this);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        stopLoop();
        removeCallbacks(playDieSound);
        releaseSounds();
    }

    @Override
    public void doFrame(long frameTimeNanos) {
        if (!isRunning) return;
        choreographer().postFrameCallback(this);
        long vsync = lastVsyncNanos == 0 ? 0 : frameTimeNanos - lastVsyncNanos;
        lastVsyncNanos = frameTimeNanos;
        if (lastFrameTimeNanos == 0) {
            lastFrameTimeNanos = frameTimeNanos;
            frameCreditNanos = 0;
            return;
        }
        if (settingFrameLimit > 0 && vsync > 0) {
            long target = 1_000_000_000L / settingFrameLimit;
            if (vsync > FRAME_LIMIT_RESET_NANOS) {
                frameCreditNanos = target;
            } else {
                frameCreditNanos += vsync;
            }
            if (frameCreditNanos + vsync / 2 < target) return;
            frameCreditNanos = Math.max(-target, Math.min(target, frameCreditNanos - target));
        } else {
            frameCreditNanos = 0;
        }
        float deltaTime = (frameTimeNanos - lastFrameTimeNanos) / 1_000_000_000.0f;
        lastFrameTimeNanos = frameTimeNanos;
        if (deltaTime <= 0) return;
        fpsFrames++;
        fpsTimer += deltaTime;
        animateBoard(deltaTime);
        if (!filmMode) animateCoins(deltaTime);
        if (gameState == GameState.PLAYING && !isPaused) {
            roundActiveSeconds += deltaTime;
            if (replayLive) {
                heartbeatTimer += deltaTime;
                if (heartbeatTimer >= HEARTBEAT_SEC) commitMark(MARK_PLAY);
            }
            checkSeals();
            roundFrames++;
            roundFrameSeconds += deltaTime;
            if (deltaTime > roundWorstFrame) roundWorstFrame = deltaTime;
            if (deltaTime > roundSlowFrameLimit) roundSlowFrames++;
        }
        if (fpsTimer >= FPS_REFRESH_SEC) {
            shownFps = Math.min(999, Math.round(fpsFrames / fpsTimer));
            fpsFrames = 0;
            fpsTimer = 0f;
            if (settingShowFps) updateFpsColor();
        }
        if (hasMouse) {
            cursorIdleSec += deltaTime;
            updateCursor();
        }
        if (settingFixedStepEnabled && settingStepOriginal) {
            motionValid = false;
            stepAccumulator += Math.min(deltaTime, fixedStepSec * MAX_FIXED_STEPS);
            int steps = 0;
            while (stepAccumulator >= fixedStepSec && steps < MAX_FIXED_STEPS) {
                step(fixedStepSec);
                stepAccumulator -= fixedStepSec;
                steps++;
            }
            if (steps == 0 && !boardOpen && !coinOpen && !nameOverlayOpen && boardStatusAlpha <= 0f && toastAge >= TOAST_SEC) return;
        } else if (settingFixedStepEnabled) {
            float advance = Math.min(deltaTime, fixedStepSec * MAX_FIXED_STEPS);
            if (advance > 0f) advance = snapStep(advance, fixedStepSec);
            stepAccumulator += advance;
            int steps = 0;
            while (stepAccumulator >= fixedStepSec - STEP_EPSILON && steps < MAX_FIXED_STEPS) {
                holdMotion();
                step(fixedStepSec);
                if (gameState != motionState) motionValid = false;
                stepAccumulator = Math.max(0f, stepAccumulator - fixedStepSec);
                steps++;
            }
        } else {
            stepAccumulator = 0f;
            motionValid = false;
            step(Math.min(deltaTime, MAX_FRAME_DELTA_SEC));
        }
        invalidate();
    }

    private static final class TrailParticle {
        float x, y, rotation, rainbowHue;
        int colorIndex, frameIndex;

        void save(DataOutputStream out) throws IOException {
            out.writeFloat(x);
            out.writeFloat(y);
            out.writeFloat(rotation);
            out.writeFloat(rainbowHue);
            out.writeInt(colorIndex);
            out.writeInt(frameIndex);
        }

        void load(DataInputStream in) throws IOException {
            x = in.readFloat();
            y = in.readFloat();
            rotation = in.readFloat();
            rainbowHue = in.readFloat();
            colorIndex = in.readInt();
            frameIndex = in.readInt();
        }

        void set(float x, float y, float rotation, int colorIndex, int frameIndex, float rainbowHue) {
            this.x = x;
            this.y = y;
            this.rotation = rotation;
            this.colorIndex = colorIndex;
            this.frameIndex = frameIndex;
            this.rainbowHue = rainbowHue;
        }
    }

    private static final class SmokeParticle {
        float x, y, radius, alpha;
        float speedX, speedY;
        float expansionRate;
        float maxLife, life;

        void init(float startX, float startY, float scale, Random r) {
            x = startX + (r.nextFloat() - 0.5f) * 40 * scale;
            y = startY + (r.nextFloat() - 0.5f) * 40 * scale;
            radius = (10 + r.nextFloat() * 10) * scale;
            alpha = 0.8f;
            speedY = -(30 + r.nextFloat() * 40) * scale;
            speedX = -(50 + r.nextFloat() * 30) * scale;
            expansionRate = 25 * scale;
            maxLife = 1.0f + r.nextFloat() * 0.5f;
            life = maxLife;
        }

        void update(float dt, float windSpeed) {
            life -= dt;
            x -= windSpeed * dt;
            x += speedX * dt;
            y += speedY * dt;
            radius += expansionRate * dt;
            alpha = Math.max(0, Math.min(1f, life / maxLife));
        }

        boolean isDead() {
            return life <= 0;
        }
    }

    private static final class SparkParticle {
        static final int TYPE_CRACKLE = 0;
        static final int TYPE_FALLING = 1;

        float x, y;
        float vx, vy;
        float life, maxLife;
        int color;
        int type;

        void init(float startX, float startY, float scale, int type, Random r) {
            this.type = type;
            x = startX + (r.nextFloat() - 0.5f) * 60 * scale;
            y = startY + (r.nextFloat() - 0.5f) * 60 * scale;

            if (type == TYPE_CRACKLE) {
                vx = (r.nextFloat() - 0.5f) * 50 * scale;
                vy = (r.nextFloat() - 0.5f) * 50 * scale;
                maxLife = 0.1f + r.nextFloat() * 0.1f;
            } else {
                vx = (r.nextFloat() - 0.5f) * 200 * scale;
                vy = -(100 + r.nextFloat() * 100) * scale;
                maxLife = 0.5f + r.nextFloat() * 0.5f;
            }
            life = maxLife;
            color = r.nextBoolean() ? Color.WHITE : Color.CYAN;
        }

        void update(float dt, float wind, float gravity) {
            life -= dt;
            x += vx * dt;
            y += vy * dt;
            if (type == TYPE_FALLING) {
                x -= wind * dt;
                vy += gravity * dt;
            }
        }

        void draw(Canvas canvas, Paint paint) {
            int alpha = Math.max(0, Math.min(255, (int) ((life / maxLife) * 255)));
            paint.setColor(color);
            paint.setAlpha(alpha);
            canvas.drawPoint(x, y, paint);
        }

        boolean isDead() {
            return life <= 0;
        }
    }

    private static final class WeatherSystem {
        private static final int PARTICLES_PER_LAYER = 150;
        private static final int DEPTH_BANDS = 5;
        private static final int BAND_SIZE = PARTICLES_PER_LAYER / DEPTH_BANDS;
        private static final int STREAKS_PER_DROP_HEAVY = 3;
        private static final float BOUNCE_DECAY_PER_FRAME = 0.90f;
        private static final float LIGHTNING_DECAY_PER_SEC = 4.0f;

        private final Random random;
        private final WeatherParticle[] frontParticles = new WeatherParticle[PARTICLES_PER_LAYER];
        private final WeatherParticle[] backParticles = new WeatherParticle[PARTICLES_PER_LAYER];
        private final float[] rainLines = new float[BAND_SIZE * STREAKS_PER_DROP_HEAVY * 4];
        private final List<LightningBolt> bolts = new ArrayList<>();

        private final Paint rainPaint = new Paint();
        private final Paint snowPaint = new Paint();
        private final Paint lightningPaint = new Paint();
        private final Paint lightningGlowPaint;
        private final Paint lightningHaloPaint;
        private final Paint stormVignettePaint = new Paint();
        private final Paint snowVignettePaint = new Paint();
        private final Paint flashTintPaint = new Paint();

        private int weatherType = 0;
        private float lightningIntensity = 0f;
        private boolean heavyRain = false;
        private int width, height;
        private float fx = 1.0f;

        WeatherSystem(Random random) {
            this.random = random;
            for (int i = 0; i < PARTICLES_PER_LAYER; i++) {
                int band = i / BAND_SIZE;
                float bandWidth = 0.5f / DEPTH_BANDS;
                frontParticles[i] = new WeatherParticle(1.0f + (band + random.nextFloat()) * bandWidth, 1.0f + (band + 0.5f) * bandWidth);
                backParticles[i] = new WeatherParticle(0.5f + (band + random.nextFloat()) * bandWidth, 0.5f + (band + 0.5f) * bandWidth);
            }

            rainPaint.setColor(Color.WHITE);
            rainPaint.setAntiAlias(true);
            snowPaint.setColor(Color.WHITE);
            snowPaint.setAntiAlias(true);

            lightningPaint.setColor(Color.WHITE);
            lightningPaint.setStyle(Paint.Style.STROKE);
            lightningPaint.setStrokeCap(Paint.Cap.ROUND);
            lightningPaint.setStrokeJoin(Paint.Join.ROUND);
            lightningPaint.setAntiAlias(true);
            lightningGlowPaint = new Paint(lightningPaint);
            lightningGlowPaint.setColor(Color.argb(255, 200, 230, 255));
            lightningHaloPaint = new Paint(lightningGlowPaint);

            flashTintPaint.setColor(Color.WHITE);
            flashTintPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.ADD));
        }

        void init(int width, int height, float fx) {
            this.width = width;
            this.height = height;
            this.fx = fx;

            float bolt = REFERENCE_SCALE * fx;
            lightningPaint.setStrokeWidth(4 * bolt);
            lightningGlowPaint.setStrokeWidth(14 * bolt);
            lightningHaloPaint.setStrokeWidth(26 * bolt);

            for (int i = 0; i < PARTICLES_PER_LAYER; i++) {
                frontParticles[i].respawn(width, height, weatherType, fx, random);
                frontParticles[i].y = random.nextFloat() * height;
                backParticles[i].respawn(width, height, weatherType, fx, random);
                backParticles[i].y = random.nextFloat() * height;
            }
            bolts.clear();

            float radius = Math.max(1f, (float) Math.hypot(width, height) / 2f * 1.09f);
            snowVignettePaint.setShader(new RadialGradient(width / 2f, height / 2f, radius,
                    new int[]{Color.TRANSPARENT, Color.argb(200, 230, 230, 250)},
                    new float[]{0.3f, 1.0f}, Shader.TileMode.CLAMP));
            stormVignettePaint.setShader(new RadialGradient(width / 2f, height / 2f, radius,
                    new int[]{Color.TRANSPARENT, Color.argb(200, 0, 0, 20)},
                    new float[]{0.3f, 1.0f}, Shader.TileMode.CLAMP));
        }

        void setHeavyRain(boolean enable) {
            heavyRain = enable;
        }

        void save(DataOutputStream out) throws IOException {
            out.writeInt(weatherType);
            out.writeBoolean(heavyRain);
            out.writeFloat(lightningIntensity);
            for (int i = 0; i < PARTICLES_PER_LAYER; i++) {
                frontParticles[i].save(out);
                backParticles[i].save(out);
            }
        }

        void load(DataInputStream in) throws IOException {
            int type = in.readInt();
            boolean heavy = in.readBoolean();
            float intensity = in.readFloat();
            bolts.clear();
            if (type != weatherType) return;
            heavyRain = heavy;
            lightningIntensity = intensity;
            for (int i = 0; i < PARTICLES_PER_LAYER; i++) {
                frontParticles[i].load(in);
                backParticles[i].load(in);
            }
        }

        void setWeatherType(int type) {
            weatherType = type;
            for (int i = 0; i < PARTICLES_PER_LAYER; i++) {
                frontParticles[i].respawn(width, height, type, fx, random);
                backParticles[i].respawn(width, height, type, fx, random);
            }
        }

        private void strike() {
            lightningIntensity = 1.0f;
        }

        void triggerLightning(int width, int height) {
            bolts.add(LightningBolt.falling(random, fx, random.nextFloat() * width, -50 * fx, height * 0.8f));
            strike();
        }

        void triggerTargetedLightning(float targetX, float targetY) {
            bolts.add(LightningBolt.targeted(random, fx, targetX, -50 * fx, targetX, targetY));
            strike();
        }

        void triggerMultiDirectionalBolts(float targetX, float targetY, int width, int height) {
            float off = 50 * fx;
            bolts.add(LightningBolt.targeted(random, fx, targetX, -off, targetX, targetY));
            bolts.add(LightningBolt.targeted(random, fx, 0, -off, targetX, targetY));
            bolts.add(LightningBolt.targeted(random, fx, width, -off, targetX, targetY));
            bolts.add(LightningBolt.targeted(random, fx, -off, height * 0.2f, targetX, targetY));
            bolts.add(LightningBolt.targeted(random, fx, width + off, height * 0.2f, targetX, targetY));
            strike();
        }

        void update(float deltaTime, float windSpeed, Rect birdRect) {
            float bounceDecay = (float) Math.pow(BOUNCE_DECAY_PER_FRAME, deltaTime * TARGET_FPS);
            float backWind = windSpeed * 0.5f;
            for (int i = 0; i < PARTICLES_PER_LAYER; i++) {
                frontParticles[i].update(deltaTime, width, height, weatherType, windSpeed, birdRect, heavyRain, bounceDecay, fx, random);
                backParticles[i].update(deltaTime, width, height, weatherType, backWind, birdRect, heavyRain, bounceDecay, fx, random);
            }

            for (int i = bolts.size() - 1; i >= 0; i--) {
                LightningBolt bolt = bolts.get(i);
                bolt.update(deltaTime);
                if (bolt.isDead()) bolts.remove(i);
            }

            if (lightningIntensity > 0) {
                lightningIntensity = Math.max(0f, lightningIntensity - LIGHTNING_DECAY_PER_SEC * deltaTime);
            }
        }

        void drawBack(Canvas canvas) {
            drawLayer(canvas, backParticles);
        }

        void drawFront(Canvas canvas) {
            drawLayer(canvas, frontParticles);
        }

        private void drawLayer(Canvas canvas, WeatherParticle[] particles) {
            if (weatherType == WEATHER_SNOW) {
                for (int i = 0; i < PARTICLES_PER_LAYER; i++) {
                    WeatherParticle p = particles[i];
                    snowPaint.setAlpha(Math.min(255, (int) (180 * p.z)));
                    canvas.drawCircle(p.x, p.y, p.size, snowPaint);
                }
                return;
            }

            float riseFactor = heavyRain ? 1.5f : 1.0f;
            for (int band = 0; band < DEPTH_BANDS; band++) {
                int first = band * BAND_SIZE;
                int count = 0;
                for (int i = first; i < first + BAND_SIZE; i++) {
                    WeatherParticle p = particles[i];
                    float run = -20f * p.z * fx;
                    float rise = p.length * riseFactor;
                    count = addStreak(count, p.x, p.y, run, rise);
                    if (heavyRain) {
                        count = addStreak(count, p.x + 50 * fx, p.y + 50 * fx, run, rise);
                        count = addStreak(count, p.x - 30 * fx, p.y - 70 * fx, run, rise);
                    }
                }
                float bandDepth = particles[first].bandDepth;
                rainPaint.setAlpha(Math.min(255, (int) (160 * bandDepth)));
                rainPaint.setStrokeWidth(2.5f * bandDepth * fx);
                canvas.drawLines(rainLines, 0, count, rainPaint);
            }
        }

        private int addStreak(int count, float x, float y, float run, float rise) {
            rainLines[count] = x;
            rainLines[count + 1] = y;
            rainLines[count + 2] = x + run;
            rainLines[count + 3] = y + rise;
            return count + 4;
        }

        void drawLightingOverlay(Canvas canvas) {
            float overscan = Math.max(width, height) * 0.25f;
            if (weatherType == WEATHER_STORM) {
                canvas.drawRect(-overscan, -overscan, width + overscan, height + overscan, stormVignettePaint);
            } else if (weatherType == WEATHER_SNOW) {
                canvas.drawRect(-overscan, -overscan, width + overscan, height + overscan, snowVignettePaint);
            }

            for (int i = 0; i < bolts.size(); i++) {
                bolts.get(i).draw(canvas, lightningPaint, lightningGlowPaint, lightningHaloPaint);
            }

            if (lightningIntensity > 0) {
                flashTintPaint.setAlpha((int) (lightningIntensity * 100));
                canvas.drawRect(-overscan, -overscan, width + overscan, height + overscan, flashTintPaint);
            }
        }
    }

    private static final class LightningBolt {
        private static final float LIFE_SEC = 0.25f;
        private static final int TARGETED_SEGMENTS = 8;

        private final Path path = new Path();
        private float life = LIFE_SEC;

        private LightningBolt() {
        }

        static LightningBolt falling(Random r, float fx, float x, float y, float targetHeight) {
            LightningBolt bolt = new LightningBolt();
            bolt.path.moveTo(x, y);
            float currentX = x;
            float currentY = y;
            while (currentY < targetHeight) {
                currentY += (30 + r.nextFloat() * 50) * fx;
                currentX += (r.nextFloat() - 0.5f) * 80 * fx;
                bolt.path.lineTo(currentX, currentY);
                if (r.nextBoolean()) {
                    float branchX = currentX + (r.nextFloat() - 0.5f) * 100 * fx;
                    float branchY = currentY + (50 + r.nextFloat() * 50) * fx;
                    bolt.path.lineTo(branchX, branchY);
                    bolt.path.moveTo(currentX, currentY);
                }
            }
            return bolt;
        }

        static LightningBolt targeted(Random r, float fx, float startX, float startY, float endX, float endY) {
            LightningBolt bolt = new LightningBolt();
            bolt.path.moveTo(startX, startY);
            float stepX = (endX - startX) / TARGETED_SEGMENTS;
            float stepY = (endY - startY) / TARGETED_SEGMENTS;
            float currentX = startX;
            float currentY = startY;
            for (int i = 0; i < TARGETED_SEGMENTS; i++) {
                currentX += stepX;
                currentY += stepY;
                if (i == TARGETED_SEGMENTS - 1) {
                    currentX = endX;
                    currentY = endY;
                } else {
                    currentX += (r.nextFloat() - 0.5f) * 100 * fx;
                    currentY += (r.nextFloat() - 0.5f) * 50 * fx;
                }
                bolt.path.lineTo(currentX, currentY);
            }
            return bolt;
        }

        void update(float dt) {
            life -= dt;
        }

        boolean isDead() {
            return life <= 0;
        }

        void draw(Canvas canvas, Paint core, Paint glow, Paint halo) {
            int alpha = Math.max(0, Math.min(255, (int) (life / LIFE_SEC * 255)));
            halo.setAlpha((int) (alpha * 0.25f));
            glow.setAlpha((int) (alpha * 0.55f));
            core.setAlpha(alpha);
            canvas.drawPath(path, halo);
            canvas.drawPath(path, glow);
            canvas.drawPath(path, core);
        }
    }

    private static final class WeatherParticle {
        float z;
        final float bandDepth;
        float x, y;
        float ySpeed, size, length;
        float offset;
        float bounceX = 0, bounceY = 0;

        WeatherParticle(float z, float bandDepth) {
            this.z = z;
            this.bandDepth = bandDepth;
        }

        void save(DataOutputStream out) throws IOException {
            out.writeFloat(z);
            out.writeFloat(x);
            out.writeFloat(y);
            out.writeFloat(ySpeed);
            out.writeFloat(size);
            out.writeFloat(length);
            out.writeFloat(offset);
            out.writeFloat(bounceX);
            out.writeFloat(bounceY);
        }

        void load(DataInputStream in) throws IOException {
            z = in.readFloat();
            x = in.readFloat();
            y = in.readFloat();
            ySpeed = in.readFloat();
            size = in.readFloat();
            length = in.readFloat();
            offset = in.readFloat();
            bounceX = in.readFloat();
            bounceY = in.readFloat();
        }

        void respawn(int screenWidth, int screenHeight, int weatherType, float fx, Random random) {
            x = random.nextFloat() * screenWidth;
            y = (-50 - random.nextFloat() * 50) * fx;
            bounceX = 0;
            bounceY = 0;
            offset = random.nextFloat() * 100f;

            if (weatherType == WEATHER_SNOW) {
                size = (2f + random.nextFloat() * 4f) * z * fx;
                ySpeed = (30f + random.nextFloat() * 40f) * z * fx;
            } else {
                size = (2.0f + random.nextFloat()) * z * fx;
                length = (30f + random.nextFloat() * 40f) * z * fx;
                ySpeed = (700f + random.nextFloat() * 300f) * z * fx;
            }
        }

        void update(float deltaTime, int screenWidth, int screenHeight, int weatherType, float windSpeed,
                    Rect birdRect, boolean heavy, float bounceDecay, float fx, Random random) {
            float speedMult = heavy ? 2.5f : 1.0f;
            y += ySpeed * speedMult * deltaTime;
            x += bounceX * deltaTime;
            y += bounceY * deltaTime;
            bounceX *= bounceDecay;
            bounceY *= bounceDecay;

            if (weatherType == WEATHER_SNOW) {
                float drift = (float) Math.sin(y / fx * 0.01f + offset) * 50f * z * fx;
                x += (windSpeed * 0.1f * z + drift) * deltaTime;
                if (birdRect.contains((int) x, (int) y)) {
                    float dx = x - birdRect.centerX();
                    float dy = y - birdRect.centerY();
                    float dist = (float) Math.sqrt(dx * dx + dy * dy);
                    if (dist < 1) dist = 1;
                    bounceX = (dx / dist) * 200f * fx;
                    bounceY = -Math.abs(ySpeed) * 0.8f;
                    x += (dx / dist) * 5f * fx;
                    y += (dy / dist) * 5f * fx;
                }
            } else {
                x -= windSpeed * z * deltaTime;
            }

            float sideMargin = 100 * fx;
            if (y > screenHeight + length || x < -sideMargin || x > screenWidth + sideMargin) {
                respawn(screenWidth, screenHeight, weatherType, fx, random);
            }
        }
    }
}

class Pipe {
    private static final int UNSCALED_HEAD_WIDTH = 52;
    private static final int UNSCALED_HEAD_HEIGHT = 24;
    private static final int UNSCALED_BODY_WIDTH = 48;
    private static final float CLASSIC_GAP_BOTTOM_CENTER = 270f;
    private static final float CLASSIC_GAP_BOTTOM_SPREAD = 180f;
    private static final float BASE_MARGIN_PERCENT = 0.08f;
    private static final float FAR_OFF_SCREEN = 20000f;

    static final class Kit {
        final Random random = new Random();
        float headWidth, headHeight, bodyWidth, bodyOffsetX;

        void size(float scale) {
            headWidth = UNSCALED_HEAD_WIDTH * scale;
            headHeight = UNSCALED_HEAD_HEIGHT * scale;
            bodyWidth = UNSCALED_BODY_WIDTH * scale;
            bodyOffsetX = (headWidth - bodyWidth) / 2.0f;
        }
    }

    private final Kit kit;

    float x, pastX = Float.NaN, keptX;
    int colorIndex;
    int topPipeY;
    boolean isScored;
    int width, height;
    public final Paint pipePaint;
    public boolean isGolden;

    private final Rect topHeadRect = new Rect(), topBodyRect = new Rect();
    private final Rect bottomHeadRect = new Rect(), bottomBodyRect = new Rect();
    private float currentYOffset = 0;

    public Pipe(Kit kit, float x, int width, int height) {
        this.kit = kit;
        this.x = x;
        this.width = width;
        this.height = height;
        this.isScored = false;
        this.isGolden = false;
        this.pipePaint = new Paint();
        this.pipePaint.setFilterBitmap(false);
        this.pipePaint.setAntiAlias(false);
    }

    public void resetHeight(int pipeGap, int playAreaHeight, int topOffset, float variationMultiplier) {
        float effectiveVariation = Math.max(0.001f, variationMultiplier);
        float marginPercent = Math.max(0.0f, Math.min(0.49f, BASE_MARGIN_PERCENT / effectiveVariation));

        int margin = (int) (playAreaHeight * marginPercent);
        int availableRange = playAreaHeight - (2 * margin) - pipeGap;
        if (availableRange <= 0) {
            this.topPipeY = (playAreaHeight / 2) + topOffset - pipeGap / 2;
        } else {
            this.topPipeY = kit.random.nextInt(availableRange) + margin + topOffset;
        }
        this.currentYOffset = 0;
    }

    public void resetHeightClassic(int pipeGap, int playAreaHeight, int topOffset, float variationMultiplier, float scale) {
        float spread = CLASSIC_GAP_BOTTOM_SPREAD * Math.max(0f, variationMultiplier);
        float bottom = (CLASSIC_GAP_BOTTOM_CENTER + (kit.random.nextFloat() - 0.5f) * spread) * scale;
        int lowest = playAreaHeight - pipeGap;
        int top = Math.round(bottom) - pipeGap;
        this.topPipeY = (lowest <= 0 ? lowest / 2 : Math.max(0, Math.min(lowest, top))) + topOffset;
        this.currentYOffset = 0;
    }

    void rescale(float ratio, int previousTop, int newTop, float previousAnchorX, float newAnchorX,
                 int newWidth, int newHeight) {
        x = newAnchorX + (x - previousAnchorX) * ratio;
        topPipeY = newTop + Math.round((topPipeY - previousTop) * ratio);
        currentYOffset *= ratio;
        width = newWidth;
        height = newHeight;
    }

    public void updateAnimation(boolean isMoving, float animationCounter, float moveRange, boolean isStopping,
                                float stopDecay, float waveLength) {
        if (isStopping) {
            currentYOffset *= stopDecay;
            if (Math.abs(currentYOffset) < 0.1f) currentYOffset = 0;
        } else if (isMoving) {
            float phaseShift = (this.x / waveLength) * (float) Math.PI;
            this.currentYOffset = ((float) Math.sin(animationCounter + phaseShift) * moveRange);
        } else {
            currentYOffset = 0;
        }
    }

    public boolean isAtRest() {
        return currentYOffset == 0;
    }

    private float getCurrentTopPipeY() {
        return topPipeY + currentYOffset;
    }

    public float getTopPipeY() {
        return getCurrentTopPipeY() - this.height;
    }

    public float getBottomPipeY(int pipeGap) {
        return getCurrentTopPipeY() + pipeGap;
    }

    public void setColorFilter(ColorFilter filter) {
        this.pipePaint.setColorFilter(filter);
    }

    public Rect getTopHeadRect(float widthMultiplier) {
        float top = getCurrentTopPipeY() - kit.headHeight;
        float bottom = getCurrentTopPipeY();
        float visualWidth = this.width * widthMultiplier;
        float xPos = x + (this.width - visualWidth) / 2.0f;
        topHeadRect.set((int) xPos, (int) top, (int) (xPos + visualWidth), (int) bottom);
        return topHeadRect;
    }

    public Rect getTopBodyRect(float widthMultiplier) {
        float bottom = getCurrentTopPipeY() - kit.headHeight;
        float visualWidth = kit.bodyWidth * widthMultiplier;
        float xPos = x + kit.bodyOffsetX + (kit.bodyWidth - visualWidth) / 2.0f;
        topBodyRect.set((int) xPos, (int) -FAR_OFF_SCREEN, (int) (xPos + visualWidth), (int) bottom);
        return topBodyRect;
    }

    public Rect getBottomHeadRect(int pipeGap, float widthMultiplier) {
        float top = getBottomPipeY(pipeGap);
        float bottom = top + kit.headHeight;
        float visualWidth = this.width * widthMultiplier;
        float xPos = x + (this.width - visualWidth) / 2.0f;
        bottomHeadRect.set((int) xPos, (int) top, (int) (xPos + visualWidth), (int) bottom);
        return bottomHeadRect;
    }

    public Rect getBottomBodyRect(int pipeGap, float widthMultiplier) {
        float top = getBottomPipeY(pipeGap) + kit.headHeight;
        float visualWidth = kit.bodyWidth * widthMultiplier;
        float xPos = x + kit.bodyOffsetX + (kit.bodyWidth - visualWidth) / 2.0f;
        bottomBodyRect.set((int) xPos, (int) top, (int) (xPos + visualWidth), (int) (top + FAR_OFF_SCREEN));
        return bottomBodyRect;
    }
}

final class PhysicsPanel extends FrameLayout {

    interface Listener {
        void chose(boolean classic);
    }

    static final float WIDTH_UNITS = 200f;

    private int marked = -1;
    private final NamePanel.Card card;
    private final TextView title;
    private final TextView message;
    private final NamePanel.PixelButton standard;
    private final NamePanel.PixelButton classic;
    private final TextView standardNote;
    private final TextView classicNote;
    private final LinearLayout choices;
    private Listener listener;
    private float unit = -1f;
    private boolean closing;

    PhysicsPanel(Context context, Typeface face) {
        super(context);
        card = new NamePanel.Card(context);
        card.setGravity(Gravity.CENTER_HORIZONTAL);
        title = text(context, face, NamePanel.ORANGE);
        title.setText("PICK YOUR PHYSICS");
        message = text(context, face, NamePanel.DARK);
        message.setText("How should the bird fly? You can change it anytime in Settings > Classic.");
        standard = new NamePanel.PixelButton(context, "DEFAULT", face);
        classic = new NamePanel.PixelButton(context, "CLASSIC", face);
        standardNote = text(context, face, 0xB3543847);
        standardNote.setText("Smooth and modern");
        classicNote = text(context, face, 0xB3543847);
        classicNote.setText("Like the 2013 original");
        choices = new LinearLayout(context);
        choices.setOrientation(LinearLayout.HORIZONTAL);
        choices.setGravity(Gravity.CENTER);
        choices.addView(option(context, standard, standardNote), new LinearLayout.LayoutParams(1, LayoutParams.WRAP_CONTENT));
        choices.addView(option(context, classic, classicNote), new LinearLayout.LayoutParams(1, LayoutParams.WRAP_CONTENT));
        card.addView(title, new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        card.addView(message, new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        card.addView(choices, new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        addView(card, new FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, Gravity.CENTER));
        setBackgroundColor(0x33000000);
        setClickable(true);
        setVisibility(GONE);
        standard.setOnClickListener(v -> choose(false));
        classic.setOnClickListener(v -> choose(true));
    }

    private static TextView text(Context context, Typeface face, int color) {
        TextView view = new TextView(context);
        view.setTypeface(face);
        view.setTextColor(color);
        view.setGravity(Gravity.CENTER);
        view.setIncludeFontPadding(false);
        return view;
    }

    private static LinearLayout option(Context context, NamePanel.PixelButton button, TextView note) {
        LinearLayout column = new LinearLayout(context);
        column.setOrientation(LinearLayout.VERTICAL);
        column.setGravity(Gravity.CENTER_HORIZONTAL);
        column.addView(button, new LinearLayout.LayoutParams(1, 1));
        column.addView(note, new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        return column;
    }

    @Override
    protected void onMeasure(int widthSpec, int heightSpec) {
        int width = MeasureSpec.getSize(widthSpec);
        int height = MeasureSpec.getSize(heightSpec);
        if (width > 0 && height > 0) fit(width, height);
        super.onMeasure(widthSpec, heightSpec);
    }

    private void fit(int width, int height) {
        float roomWidth = width * 0.92f;
        float roomHeight = height * 0.86f;
        float value = Math.max(0.5f, Math.min(width / 252f, height / 360f) * 0.95f);
        value = Math.min(value, roomWidth / (WIDTH_UNITS + 4f));
        for (int pass = 0; pass < 3; pass++) {
            applyUnit(value);
            card.measure(MeasureSpec.makeMeasureSpec(Math.round(roomWidth), MeasureSpec.AT_MOST),
                    MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
            float shrink = Math.min(roomWidth / Math.max(1, card.getMeasuredWidth()), roomHeight / Math.max(1, card.getMeasuredHeight()));
            if (shrink >= 0.995f) return;
            value *= shrink * 0.98f;
        }
        applyUnit(value);
    }

    private static LinearLayout.LayoutParams params(View view) {
        android.view.ViewGroup.LayoutParams current = view.getLayoutParams();
        if (current instanceof LinearLayout.LayoutParams) return (LinearLayout.LayoutParams) current;
        return new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
    }

    private void applyUnit(float value) {
        if (!(value > 0f) || Math.abs(value - unit) < 0.002f * value) return;
        unit = value;
        card.unit = unit;
        int side = Math.round(10f * unit);
        card.setPadding(side, Math.round(9f * unit), side, Math.round(10f * unit));
        card.setMinimumWidth(Math.round(WIDTH_UNITS * unit));
        title.setTextSize(TypedValue.COMPLEX_UNIT_PX, 13f * unit);
        message.setTextSize(TypedValue.COMPLEX_UNIT_PX, 6.5f * unit);
        message.setLineSpacing(1.5f * unit, 1f);
        LinearLayout.LayoutParams messageParams = params(message);
        messageParams.width = Math.round((WIDTH_UNITS - 20f) * unit);
        messageParams.topMargin = Math.round(5f * unit);
        message.setLayoutParams(messageParams);
        LinearLayout.LayoutParams rowParams = params(choices);
        rowParams.topMargin = Math.round(9f * unit);
        choices.setLayoutParams(rowParams);
        int column = Math.round(82f * unit);
        int gap = Math.round(4f * unit);
        for (int i = 0; i < choices.getChildCount(); i++) {
            View child = choices.getChildAt(i);
            LinearLayout.LayoutParams params = params(child);
            params.width = column;
            params.leftMargin = gap;
            params.rightMargin = gap;
            child.setLayoutParams(params);
        }
        for (NamePanel.PixelButton button : new NamePanel.PixelButton[] {standard, classic}) {
            button.unit = unit;
            LinearLayout.LayoutParams params = params(button);
            params.width = Math.round(74f * unit);
            params.height = Math.round(24f * unit);
            button.setLayoutParams(params);
            button.invalidate();
        }
        for (TextView note : new TextView[] {standardNote, classicNote}) {
            note.setTextSize(TypedValue.COMPLEX_UNIT_PX, 5.5f * unit);
            note.setLineSpacing(1f * unit, 1f);
            LinearLayout.LayoutParams params = params(note);
            params.topMargin = Math.round(4f * unit);
            note.setLayoutParams(params);
        }
        card.invalidate();
    }

    void open(Listener listener) {
        this.listener = listener;
        closing = false;
        View parent = (View) getParent();
        if (parent != null && parent.getWidth() > 0 && parent.getHeight() > 0) fit(parent.getWidth(), parent.getHeight());
        setVisibility(VISIBLE);
        requestLayout();
        setAlpha(0f);
        animate().cancel();
        animate().alpha(1f).setDuration(180).start();
        float travel = Math.max(1f, unit) * 16f;
        card.animate().cancel();
        card.setScaleX(0.96f);
        card.setScaleY(0.96f);
        card.setTranslationY(travel);
        card.animate().scaleX(1f).scaleY(1f).translationY(0f).setStartDelay(40).setDuration(280)
                .setInterpolator(new DecelerateInterpolator(1.8f)).withLayer().start();
        mark(-1);
    }

    boolean isOpen() {
        return getVisibility() == VISIBLE && !closing;
    }

    private void mark(int which) {
        marked = which;
        standard.setMarked(which == 0);
        classic.setMarked(which == 1);
    }

    boolean key(KeyEvent event) {
        int code = event.getKeyCode();
        boolean down = event.getAction() == KeyEvent.ACTION_DOWN;
        boolean first = down && event.getRepeatCount() == 0;
        switch (code) {
            case KeyEvent.KEYCODE_DPAD_LEFT:
            case KeyEvent.KEYCODE_DPAD_RIGHT:
            case KeyEvent.KEYCODE_TAB:
            case KeyEvent.KEYCODE_DPAD_UP:
            case KeyEvent.KEYCODE_DPAD_DOWN:
                if (down) {
                    int next;
                    if (code == KeyEvent.KEYCODE_DPAD_LEFT) next = 0;
                    else if (code == KeyEvent.KEYCODE_DPAD_RIGHT) next = 1;
                    else next = marked < 0 ? (code == KeyEvent.KEYCODE_TAB && event.isShiftPressed() ? 1 : 0) : 1 - marked;
                    if (next != marked) {
                        mark(next);
                        hint();
                    }
                }
                return true;
            case KeyEvent.KEYCODE_D:
                if (first) press(false);
                return true;
            case KeyEvent.KEYCODE_C:
                if (first) press(true);
                return true;
            case KeyEvent.KEYCODE_ENTER:
            case KeyEvent.KEYCODE_NUMPAD_ENTER:
            case KeyEvent.KEYCODE_SPACE:
            case KeyEvent.KEYCODE_DPAD_CENTER:
            case KeyEvent.KEYCODE_BUTTON_A:
                if (first) {
                    if (marked < 0) {
                        mark(0);
                        hint();
                    } else {
                        press(marked == 1);
                    }
                }
                return true;
            case KeyEvent.KEYCODE_ESCAPE:
            case KeyEvent.KEYCODE_BUTTON_B:
                if (first) press(false);
                return true;
            default:
                return false;
        }
    }

    private void hint() {
        Context context = getContext();
        if (context instanceof MainActivity) ((MainActivity) context).feel(SettingsActivity.Feel.TICK);
    }

    private void press(boolean pickClassic) {
        NamePanel.PixelButton button = button(pickClassic);
        mark(pickClassic ? 1 : 0);
        button.performClick();
    }

    void choose(boolean pickClassic) {
        if (!isOpen()) return;
        Listener chosen = listener;
        listener = null;
        closing = true;
        float travel = Math.max(1f, unit) * 12f;
        card.animate().cancel();
        card.animate().scaleX(0.97f).scaleY(0.97f).translationY(travel).setStartDelay(0).setDuration(160)
                .setInterpolator(new AccelerateInterpolator(1.4f)).withLayer().start();
        animate().cancel();
        animate().alpha(0f).setDuration(180).withEndAction(() -> {
            if (!closing) return;
            closing = false;
            setVisibility(GONE);
        }).start();
        if (chosen != null) chosen.chose(pickClassic);
    }

    NamePanel.PixelButton button(boolean pickClassic) {
        return pickClassic ? classic : standard;
    }
}

final class NamePanel extends FrameLayout {

    interface Listener {
        void submit(String name);

        void cancel();
    }

    static final int PANEL = 0xFFDED895;
    static final int PANEL_EDGE = 0xFFF8F0B0;
    static final int DARK = 0xFF543847;
    static final int ORANGE = 0xFFFC7858;
    static final float WIDTH_UNITS = 200f;
    private static final String HELP = "Letters, numbers, spaces and _";

    private final FrameLayout holder;
    private final Card card;
    private final TextView title;
    private final TextView message;
    private final EditText field;
    private final TextView status;
    private final TextView counter;
    private final PixelButton cancel;
    private final PixelButton ok;
    private Listener listener;
    private float unit = -1f;
    private float measuredUnit = -1f;
    private int measuredHeight;
    private boolean measureDirty = true;
    private boolean busy;
    private boolean firstTime;
    private boolean closing;

    static final class Card extends LinearLayout {
        private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final RectF shape = new RectF();
        float unit = 1f;

        Card(Context context) {
            super(context);
            setOrientation(VERTICAL);
            setWillNotDraw(false);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            shape.set(0, 0, getWidth(), getHeight());
            paint.setColor(DARK);
            canvas.drawRoundRect(shape, unit * 5f, unit * 5f, paint);
            shape.inset(unit * 2f, unit * 2f);
            paint.setColor(PANEL_EDGE);
            canvas.drawRoundRect(shape, unit * 4f, unit * 4f, paint);
            shape.inset(unit * 1.5f, unit * 1.5f);
            paint.setColor(PANEL);
            canvas.drawRoundRect(shape, unit * 3f, unit * 3f, paint);
            super.onDraw(canvas);
        }
    }

    static final class PixelButton extends View {
        private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint text = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final RectF shape = new RectF();
        private String label;
        private boolean down;
        private boolean marked;
        float unit = 1f;

        PixelButton(Context context, String label, Typeface face) {
            super(context);
            this.label = label;
            text.setTypeface(face);
            text.setTextAlign(Paint.Align.CENTER);
            setClickable(true);
            setFocusable(true);
            setContentDescription(label);
        }

        void setLabel(String label) {
            this.label = label;
            setContentDescription(label);
            invalidate();
        }

        String label() {
            return label;
        }

        void setMarked(boolean marked) {
            if (this.marked == marked) return;
            this.marked = marked;
            invalidate();
        }

        @Override
        public boolean performClick() {
            Context context = getContext();
            if (isEnabled() && context instanceof MainActivity) ((MainActivity) context).feel(SettingsActivity.Feel.BUTTON);
            return super.performClick();
        }

        @Override
        public void onHoverChanged(boolean hovered) {
            super.onHoverChanged(hovered);
            invalidate();
        }

        @Override
        protected void onFocusChanged(boolean gainFocus, int direction, Rect previous) {
            super.onFocusChanged(gainFocus, direction, previous);
            invalidate();
        }

        @Override
        protected void drawableStateChanged() {
            super.drawableStateChanged();
            invalidate();
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            if (!isEnabled()) return false;
            int action = event.getActionMasked();
            boolean inside = event.getX() >= 0 && event.getY() >= 0 && event.getX() <= getWidth() && event.getY() <= getHeight();
            if (action == MotionEvent.ACTION_DOWN) {
                down = true;
            } else if (action == MotionEvent.ACTION_UP) {
                boolean click = down && inside;
                down = false;
                if (click) performClick();
            } else if (action == MotionEvent.ACTION_CANCEL || (action == MotionEvent.ACTION_MOVE && !inside)) {
                down = false;
            }
            invalidate();
            return true;
        }

        @Override
        protected void onDraw(Canvas canvas) {
            float drop = down || isPressed() ? unit * 1.5f : 0f;
            int alpha = isEnabled() ? 255 : 120;
            boolean lit = isEnabled() && (marked || isFocused() || isHovered());
            paint.setColor(DARK);
            paint.setAlpha(alpha);
            shape.set(0, unit * 1.5f, getWidth(), getHeight());
            canvas.drawRoundRect(shape, unit * 3f, unit * 3f, paint);
            shape.set(0, drop, getWidth(), getHeight() - unit * 1.5f + drop);
            canvas.drawRoundRect(shape, unit * 3f, unit * 3f, paint);
            paint.setColor(lit ? ORANGE : Color.WHITE);
            paint.setAlpha(alpha);
            shape.inset(unit * 1.5f, unit * 1.5f);
            canvas.drawRoundRect(shape, unit * 2f, unit * 2f, paint);
            if (lit) {
                paint.setColor(Color.WHITE);
                paint.setAlpha(alpha);
                shape.inset(unit * 1.2f, unit * 1.2f);
                canvas.drawRoundRect(shape, unit * 1.5f, unit * 1.5f, paint);
            }
            text.setColor(ORANGE);
            text.setAlpha(alpha);
            text.setTextSize(unit * 9f);
            canvas.drawText(label, getWidth() / 2f, (getHeight() - unit * 1.5f) / 2f + drop + unit * 3.2f, text);
        }
    }

    NamePanel(Context context, Typeface face) {
        super(context);
        card = new Card(context);
        title = new TextView(context);
        title.setTypeface(face);
        title.setTextColor(ORANGE);
        title.setGravity(Gravity.CENTER);
        message = new TextView(context);
        message.setTypeface(face);
        message.setTextColor(DARK);
        message.setGravity(Gravity.CENTER);
        field = new EditText(context);
        field.setTypeface(face);
        field.setTextColor(DARK);
        field.setHintTextColor(0x99543847);
        field.setHint("TYPE A NAME");
        field.setSingleLine(true);
        field.setGravity(Gravity.CENTER);
        field.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS | InputType.TYPE_TEXT_VARIATION_PERSON_NAME);
        field.setImeOptions(EditorInfo.IME_ACTION_DONE | EditorInfo.IME_FLAG_NO_EXTRACT_UI);
        field.setFilters(new InputFilter[] {new InputFilter.LengthFilter(SettingsActivity.Leaderboard.NAME_MAX)});
        field.setContentDescription("Leaderboard name");
        LinearLayout info = new LinearLayout(context);
        info.setOrientation(LinearLayout.HORIZONTAL);
        status = new TextView(context);
        status.setTypeface(face);
        status.setTextColor(0xB3543847);
        counter = new TextView(context);
        counter.setTypeface(face);
        counter.setTextColor(0xB3543847);
        counter.setGravity(Gravity.END);
        info.addView(status, new LinearLayout.LayoutParams(0, LayoutParams.WRAP_CONTENT, 1f));
        info.addView(counter, new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        LinearLayout buttons = new LinearLayout(context);
        buttons.setOrientation(LinearLayout.HORIZONTAL);
        buttons.setGravity(Gravity.CENTER);
        cancel = new PixelButton(context, "CANCEL", face);
        ok = new PixelButton(context, "OK", face);
        buttons.addView(cancel);
        buttons.addView(ok);
        card.addView(title);
        card.addView(message);
        card.addView(field);
        card.addView(info);
        card.addView(buttons);
        holder = new FrameLayout(context);
        holder.addView(card, new FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        addView(holder, new FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, Gravity.LEFT | Gravity.TOP));
        setVisibility(GONE);
        setClickable(false);

        ok.setOnClickListener(v -> submit());
        cancel.setOnClickListener(v -> {
            if (!busy && listener != null) listener.cancel();
        });
        field.setOnEditorActionListener((view, action, event) -> {
            if (action != EditorInfo.IME_ACTION_DONE) return false;
            submit();
            return true;
        });
        field.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence text, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence text, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable text) {
                if (busy) return;
                refreshState(null);
            }
        });
    }

    void open(boolean firstTime, String current, boolean beside, Listener listener) {
        this.firstTime = firstTime;
        this.listener = listener;
        busy = false;
        title.setText(firstTime ? "CHOOSE A NAME" : "YOUR NAME");
        message.setText(firstTime ? "Shown on the online leaderboard. Fair rounds post automatically; turn that off in Settings > Leaderboard." : "");
        message.setVisibility(firstTime ? VISIBLE : GONE);
        cancel.setVisibility(firstTime ? GONE : VISIBLE);
        field.setEnabled(true);
        field.setText(current == null ? "" : current);
        field.setSelection(field.getText() == null ? 0 : field.getText().length());
        refreshState(null);
        closing = false;
        measureDirty = true;
        setVisibility(VISIBLE);
        float travel = Math.max(1f, unit) * 16f;
        card.animate().cancel();
        card.setAlpha(0f);
        card.setScaleX(0.96f);
        card.setScaleY(0.96f);
        card.setTranslationX(beside ? travel : 0f);
        card.setTranslationY(beside ? 0f : travel);
        card.animate().alpha(1f).scaleX(1f).scaleY(1f).translationX(0f).translationY(0f).setStartDelay(40).setDuration(280)
                .setInterpolator(new DecelerateInterpolator(1.8f)).withLayer().start();
        field.requestFocus();
        post(() -> {
            InputMethodManager keyboard = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            if (keyboard != null) keyboard.showSoftInput(field, InputMethodManager.SHOW_IMPLICIT);
        });
    }

    void close(boolean beside) {
        if (getVisibility() != VISIBLE || closing) return;
        InputMethodManager keyboard = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (keyboard != null) keyboard.hideSoftInputFromWindow(field.getWindowToken(), 0);
        field.clearFocus();
        busy = false;
        listener = null;
        closing = true;
        float travel = Math.max(1f, unit) * 12f;
        card.animate().cancel();
        card.animate().alpha(0f).scaleX(0.97f).scaleY(0.97f).translationX(beside ? travel : 0f).translationY(beside ? 0f : travel)
                .setStartDelay(0).setDuration(160).setInterpolator(new AccelerateInterpolator(1.4f)).withLayer().withEndAction(() -> {
                    if (!closing) return;
                    closing = false;
                    setVisibility(GONE);
                }).start();
    }

    boolean isOpen() {
        return getVisibility() == VISIBLE && !closing;
    }

    boolean cancellable() {
        return !firstTime && !busy;
    }

    void setBusy(boolean busy) {
        this.busy = busy;
        field.setEnabled(!busy);
        cancel.setEnabled(!busy);
        refreshState(busy ? "Checking..." : null);
    }

    void showError(String error) {
        setBusy(false);
        refreshState(error);
    }

    private String typedName() {
        return SettingsActivity.Leaderboard.cleanName(field.getText() == null ? "" : field.getText().toString());
    }

    private void refreshState(String note) {
        String raw = field.getText() == null ? "" : field.getText().toString();
        boolean valid = !typedName().isEmpty();
        boolean error = note != null && !note.equals("Checking...");
        if (note == null && !raw.isEmpty() && !valid) {
            note = "Use at least one letter or number";
            error = true;
        }
        String shown = note != null ? note : HELP;
        if (!shown.contentEquals(status.getText())) measureDirty = true;
        status.setText(shown);
        status.setTextColor(error ? ORANGE : 0xB3543847);
        counter.setText(raw.length() + "/" + SettingsActivity.Leaderboard.NAME_MAX);
        ok.setEnabled(valid && !busy);
    }

    private void submit() {
        if (busy || listener == null) return;
        String name = typedName();
        if (name.isEmpty()) {
            refreshState("Use at least one letter or number");
            return;
        }
        setBusy(true);
        listener.submit(name);
    }

    int panelHeight(float unit) {
        applyUnit(unit);
        if (measureDirty || measuredUnit != this.unit) {
            card.measure(MeasureSpec.makeMeasureSpec(Math.round(WIDTH_UNITS * this.unit), MeasureSpec.EXACTLY),
                    MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
            measuredHeight = card.getMeasuredHeight();
            measuredUnit = this.unit;
            measureDirty = false;
        }
        return measuredHeight;
    }

    void place(int left, int top, float unit) {
        applyUnit(unit);
        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) card.getLayoutParams();
        int width = Math.round(WIDTH_UNITS * this.unit);
        if (params.width != width) {
            params.width = width;
            card.setLayoutParams(params);
        }
        if (holder.getTranslationX() != left) holder.setTranslationX(left);
        if (holder.getTranslationY() != top) holder.setTranslationY(top);
    }

    private void applyUnit(float unit) {
        if (Math.abs(unit - this.unit) < 0.01f * Math.max(0.01f, this.unit)) return;
        this.unit = unit;
        measureDirty = true;
        card.unit = unit;
        int side = Math.round(10f * unit);
        card.setPadding(side, Math.round(9f * unit), side, Math.round(9f * unit));
        title.setTextSize(TypedValue.COMPLEX_UNIT_PX, 13f * unit);
        message.setTextSize(TypedValue.COMPLEX_UNIT_PX, 6.5f * unit);
        field.setTextSize(TypedValue.COMPLEX_UNIT_PX, 10f * unit);
        status.setTextSize(TypedValue.COMPLEX_UNIT_PX, 6.5f * unit);
        counter.setTextSize(TypedValue.COMPLEX_UNIT_PX, 6.5f * unit);
        GradientDrawable box = new GradientDrawable();
        box.setColor(PANEL_EDGE);
        box.setCornerRadius(3f * unit);
        box.setStroke(Math.max(1, Math.round(1.5f * unit)), DARK);
        field.setBackground(box);
        field.setPadding(Math.round(6f * unit), 0, Math.round(6f * unit), 0);
        LinearLayout.LayoutParams messageParams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        messageParams.topMargin = Math.round(4f * unit);
        message.setLayoutParams(messageParams);
        LinearLayout.LayoutParams fieldParams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, Math.round(22f * unit));
        fieldParams.topMargin = Math.round(7f * unit);
        field.setLayoutParams(fieldParams);
        LinearLayout.LayoutParams infoParams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        infoParams.topMargin = Math.round(3f * unit);
        ((View) status.getParent()).setLayoutParams(infoParams);
        LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, Math.round(24f * unit));
        rowParams.topMargin = Math.round(7f * unit);
        ((View) ok.getParent()).setLayoutParams(rowParams);
        LinearLayout.LayoutParams cancelParams = new LinearLayout.LayoutParams(Math.round(70f * unit), LayoutParams.MATCH_PARENT);
        cancelParams.rightMargin = Math.round(10f * unit);
        cancel.setLayoutParams(cancelParams);
        ok.setLayoutParams(new LinearLayout.LayoutParams(Math.round(70f * unit), LayoutParams.MATCH_PARENT));
        cancel.unit = unit;
        ok.unit = unit;
    }

    EditText field() {
        return field;
    }

    PixelButton okButton() {
        return ok;
    }

    PixelButton cancelButton() {
        return cancel;
    }

    String statusText() {
        return String.valueOf(status.getText());
    }
}

final class FastBytes extends java.io.OutputStream {
    private byte[] data;
    private int size;

    FastBytes(int capacity) {
        data = new byte[Math.max(64, capacity)];
    }

    void reset() {
        size = 0;
    }

    private void room(int extra) {
        if (size + extra <= data.length) return;
        data = java.util.Arrays.copyOf(data, Math.max(data.length * 2, size + extra));
    }

    @Override
    public void write(int value) {
        room(1);
        data[size++] = (byte) value;
    }

    @Override
    public void write(byte[] source, int offset, int length) {
        room(length);
        System.arraycopy(source, offset, data, size, length);
        size += length;
    }

    byte[] toByteArray() {
        return java.util.Arrays.copyOf(data, size);
    }
}

final class MemoryPrefs implements SharedPreferences {
    private final java.util.Map<String, Object> values = new java.util.concurrent.ConcurrentHashMap<>();

    MemoryPrefs(java.util.Map<String, Object> initial) {
        for (java.util.Map.Entry<String, Object> entry : initial.entrySet()) {
            if (entry.getKey() != null && entry.getValue() != null) values.put(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public java.util.Map<String, ?> getAll() {
        return new java.util.HashMap<>(values);
    }

    @Override
    public String getString(String key, String fallback) {
        Object value = values.get(key);
        return value instanceof String ? (String) value : fallback;
    }

    @SuppressWarnings("unchecked")
    public java.util.Set<String> getStringSet(String key, java.util.Set<String> fallback) {
        Object value = values.get(key);
        return value instanceof java.util.Set ? new java.util.HashSet<>((java.util.Set<String>) value) : fallback;
    }

    @Override
    public int getInt(String key, int fallback) {
        Object value = values.get(key);
        return value instanceof Integer ? (Integer) value : fallback;
    }

    @Override
    public long getLong(String key, long fallback) {
        Object value = values.get(key);
        return value instanceof Long ? (Long) value : fallback;
    }

    @Override
    public float getFloat(String key, float fallback) {
        Object value = values.get(key);
        return value instanceof Float ? (Float) value : fallback;
    }

    @Override
    public boolean getBoolean(String key, boolean fallback) {
        Object value = values.get(key);
        return value instanceof Boolean ? (Boolean) value : fallback;
    }

    @Override
    public boolean contains(String key) {
        return values.containsKey(key);
    }

    @Override
    public Editor edit() {
        return new MemoryEditor();
    }

    public void registerOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener listener) {
    }

    public void unregisterOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener listener) {
    }

    final class MemoryEditor implements Editor {
        private final java.util.Map<String, Object> changes = new java.util.HashMap<>();
        private boolean clear;

        @Override
        public Editor putString(String key, String value) {
            changes.put(key, value);
            return this;
        }

        public Editor putStringSet(String key, java.util.Set<String> value) {
            changes.put(key, value == null ? null : new java.util.HashSet<>(value));
            return this;
        }

        @Override
        public Editor putInt(String key, int value) {
            changes.put(key, value);
            return this;
        }

        @Override
        public Editor putLong(String key, long value) {
            changes.put(key, value);
            return this;
        }

        @Override
        public Editor putFloat(String key, float value) {
            changes.put(key, value);
            return this;
        }

        @Override
        public Editor putBoolean(String key, boolean value) {
            changes.put(key, value);
            return this;
        }

        @Override
        public Editor remove(String key) {
            changes.put(key, null);
            return this;
        }

        @Override
        public Editor clear() {
            clear = true;
            return this;
        }

        @Override
        public boolean commit() {
            if (clear) values.clear();
            for (java.util.Map.Entry<String, Object> entry : changes.entrySet()) {
                if (entry.getValue() == null) values.remove(entry.getKey());
                else values.put(entry.getKey(), entry.getValue());
            }
            return true;
        }

        @Override
        public void apply() {
            commit();
        }
    }
}

final class Challenges {
    static final long DAY_MS = 86400000L;
    static final int EASY = 0;
    static final int MEDIUM = 1;
    static final int HARD = 2;
    static final int[] REWARD = {40, 80, 150};
    static final String[] TIER_NAMES = {"EASY", "MEDIUM", "HARD"};
    static final int BONUS = 100;
    static final int STREAK_STEP = 20;
    static final int STREAK_CAP = 5;
    static final int MAX_BALANCE = 99999999;

    static final int SCORE = 1;
    static final int TOTAL = 2;
    static final int ROUNDS = 3;
    static final int SECONDS = 4;
    static final int EXACT = 5;
    static final int THRIFTY = 6;
    static final int BEAT_WEEK = 7;
    static final int FLAPS = 8;

    static final int ANY = 0;
    static final int CLASSIC = 1;
    static final int STORM = 2;
    static final int UPSIDE = 3;
    static final int DIZZY = 4;
    static final int REVERSE = 5;
    static final int RAMP = 6;
    static final int SHRINK = 7;
    static final int GIANT = 8;
    static final int WIDE = 9;
    static final String[] TWIST_NAMES = {"", "Classic physics", "Thunderstorm", "Upside down", "Dizzy bird", "Reverse pipes",
            "Speed ramp", "Shrinking gaps", "Giant bird", "Wide pipes"};

    private static final Object[][][] TWISTS = {
            {},
            {},
            {{SettingsActivity.PREF_WEATHER_EFFECT, 2, SettingsActivity.DEFAULT_WEATHER_EFFECT},
                    {SettingsActivity.PREF_LIGHTNING_SCORES, SettingsActivity.DEFAULT_LIGHTNING_SCORES, SettingsActivity.DEFAULT_LIGHTNING_SCORES},
                    {SettingsActivity.PREF_MEGA_STRIKE_SCORE, SettingsActivity.DEFAULT_MEGA_STRIKE_SCORE, SettingsActivity.DEFAULT_MEGA_STRIKE_SCORE}},
            {{SettingsActivity.PREF_UPSIDE_DOWN_ENABLED, true, SettingsActivity.DEFAULT_UPSIDE_DOWN_ENABLED}},
            {{SettingsActivity.PREF_DRUNK_MODE_ENABLED, true, SettingsActivity.DEFAULT_DRUNK_MODE_ENABLED},
                    {SettingsActivity.PREF_DRUNK_MODE_STRENGTH, 1.0f, SettingsActivity.DEFAULT_DRUNK_MODE_STRENGTH}},
            {{SettingsActivity.PREF_REVERSE_PIPES_ENABLED, true, SettingsActivity.DEFAULT_REVERSE_PIPES_ENABLED}},
            {{SettingsActivity.PREF_SPEED_RAMP, 10, SettingsActivity.DEFAULT_SPEED_RAMP},
                    {SettingsActivity.PREF_RAMP_TOP_SPEED, SettingsActivity.DEFAULT_RAMP_TOP_SPEED, SettingsActivity.DEFAULT_RAMP_TOP_SPEED}},
            {{SettingsActivity.PREF_GAP_SHRINK, 5, SettingsActivity.DEFAULT_GAP_SHRINK},
                    {SettingsActivity.PREF_RAMP_MIN_GAP, SettingsActivity.DEFAULT_RAMP_MIN_GAP, SettingsActivity.DEFAULT_RAMP_MIN_GAP}},
            {{SettingsActivity.PREF_BIRD_SIZE, 1.5f, SettingsActivity.DEFAULT_BIRD_SIZE}},
            {{SettingsActivity.PREF_PIPE_WIDTH, 1.6f, SettingsActivity.DEFAULT_PIPE_WIDTH}},
    };

    static final class Goal {
        final String id;
        final int tier;
        final int kind;
        final int target;
        final int extra;
        final int twist;

        Goal(String id, int tier, int kind, int target, int extra, int twist) {
            this.id = id;
            this.tier = tier;
            this.kind = kind;
            this.target = target;
            this.extra = extra;
            this.twist = twist;
        }

        boolean single() {
            return kind == SCORE || kind == SECONDS || kind == EXACT || kind == THRIFTY || kind == BEAT_WEEK;
        }

        int goal() {
            return single() && kind != SCORE && kind != SECONDS ? 1 : target;
        }

        String title() {
            String where = twistPhrase(twist);
            switch (kind) {
                case SCORE: return "Score " + target + " in one round" + where;
                case TOTAL: return "Score " + target + " points in total" + where;
                case ROUNDS: return "Score " + extra + " or more in " + target + " rounds" + where;
                case SECONDS: return "Stay alive for " + target + " seconds" + where;
                case EXACT: return "Crash on exactly " + target + " points" + where;
                case THRIFTY: return "Score " + target + " using " + extra + " flaps or fewer" + where;
                case BEAT_WEEK: return "Beat your best score this week" + where;
                case FLAPS: return "Flap " + target + " times" + where;
                default: return "Challenge";
            }
        }
    }

    static String twistPhrase(int twist) {
        switch (twist) {
            case CLASSIC: return " with Classic physics";
            case STORM: return " in a thunderstorm";
            case UPSIDE: return " upside down";
            case DIZZY: return " as a dizzy bird";
            case REVERSE: return " with reversed pipes";
            case RAMP: return " as the speed ramps up";
            case SHRINK: return " as the gaps shrink";
            case GIANT: return " as a giant bird";
            case WIDE: return " with extra wide pipes";
            default: return "";
        }
    }

    static final Goal[] EASY_POOL = {
            new Goal("e1", EASY, SCORE, 10, 0, ANY),
            new Goal("e2", EASY, SCORE, 15, 0, ANY),
            new Goal("e3", EASY, TOTAL, 30, 0, ANY),
            new Goal("e4", EASY, ROUNDS, 3, 3, ANY),
            new Goal("e5", EASY, SECONDS, 20, 0, ANY),
            new Goal("e6", EASY, FLAPS, 100, 0, ANY),
            new Goal("e7", EASY, SCORE, 5, 0, UPSIDE),
            new Goal("e8", EASY, SCORE, 5, 0, STORM),
            new Goal("e9", EASY, SCORE, 5, 0, REVERSE),
            new Goal("e10", EASY, SCORE, 8, 0, CLASSIC),
            new Goal("e11", EASY, EXACT, 7, 0, ANY),
            new Goal("e12", EASY, SCORE, 5, 0, GIANT),
            new Goal("e13", EASY, SCORE, 5, 0, DIZZY),
            new Goal("e14", EASY, SCORE, 8, 0, WIDE),
            new Goal("e15", EASY, TOTAL, 20, 0, STORM),
    };

    static final Goal[] MEDIUM_POOL = {
            new Goal("m1", MEDIUM, SCORE, 25, 0, ANY),
            new Goal("m2", MEDIUM, SCORE, 40, 0, ANY),
            new Goal("m3", MEDIUM, TOTAL, 100, 0, ANY),
            new Goal("m4", MEDIUM, ROUNDS, 5, 10, ANY),
            new Goal("m5", MEDIUM, SECONDS, 45, 0, ANY),
            new Goal("m6", MEDIUM, SCORE, 15, 0, STORM),
            new Goal("m7", MEDIUM, SCORE, 12, 0, UPSIDE),
            new Goal("m8", MEDIUM, SCORE, 12, 0, REVERSE),
            new Goal("m9", MEDIUM, SCORE, 20, 0, CLASSIC),
            new Goal("m10", MEDIUM, EXACT, 13, 0, ANY),
            new Goal("m11", MEDIUM, THRIFTY, 15, 40, ANY),
            new Goal("m12", MEDIUM, SCORE, 15, 0, WIDE),
            new Goal("m13", MEDIUM, SCORE, 10, 0, DIZZY),
            new Goal("m14", MEDIUM, SCORE, 20, 0, RAMP),
            new Goal("m15", MEDIUM, FLAPS, 400, 0, ANY),
            new Goal("m16", MEDIUM, SCORE, 12, 0, GIANT),
            new Goal("m17", MEDIUM, SCORE, 20, 0, SHRINK),
            new Goal("m18", MEDIUM, TOTAL, 50, 0, STORM),
    };

    static final Goal[] HARD_POOL = {
            new Goal("h1", HARD, SCORE, 50, 0, ANY),
            new Goal("h2", HARD, SCORE, 75, 0, ANY),
            new Goal("h3", HARD, TOTAL, 300, 0, ANY),
            new Goal("h4", HARD, ROUNDS, 10, 15, ANY),
            new Goal("h5", HARD, SECONDS, 90, 0, ANY),
            new Goal("h6", HARD, TOTAL, 100, 0, STORM),
            new Goal("h7", HARD, SCORE, 30, 0, STORM),
            new Goal("h8", HARD, SCORE, 25, 0, UPSIDE),
            new Goal("h9", HARD, SCORE, 25, 0, REVERSE),
            new Goal("h10", HARD, SCORE, 40, 0, CLASSIC),
            new Goal("h11", HARD, EXACT, 42, 0, ANY),
            new Goal("h12", HARD, BEAT_WEEK, 5, 0, ANY),
            new Goal("h13", HARD, THRIFTY, 30, 75, ANY),
            new Goal("h14", HARD, SCORE, 30, 0, SHRINK),
            new Goal("h15", HARD, SCORE, 25, 0, GIANT),
            new Goal("h16", HARD, SCORE, 40, 0, RAMP),
            new Goal("h17", HARD, SCORE, 20, 0, DIZZY),
            new Goal("h18", HARD, SCORE, 30, 0, WIDE),
    };

    private Challenges() {
    }

    static long day(long time) {
        return time >= 0 ? time / DAY_MS : (time - DAY_MS + 1) / DAY_MS;
    }

    static long dayEnds(long day) {
        return (day + 1L) * DAY_MS;
    }

    static Goal find(String id) {
        if (id == null) return null;
        for (Goal[] pool : new Goal[][] {EASY_POOL, MEDIUM_POOL, HARD_POOL}) {
            for (Goal goal : pool) if (goal.id.equals(id)) return goal;
        }
        return null;
    }

    static Goal[] forDay(long day) {
        Random random = new Random(day * 0x9E3779B97F4A7C15L + 0x2545F4914F6CDD1DL);
        Goal[] picked = new Goal[3];
        Goal[][] pools = {EASY_POOL, MEDIUM_POOL, HARD_POOL};
        for (int tier = 0; tier < 3; tier++) {
            Goal[] pool = pools[tier];
            Goal choice = pool[random.nextInt(pool.length)];
            for (int tries = 0; tries < 12 && clashes(choice, picked, tier); tries++) choice = pool[random.nextInt(pool.length)];
            picked[tier] = choice;
        }
        return picked;
    }

    private static boolean clashes(Goal choice, Goal[] picked, int count) {
        for (int i = 0; i < count; i++) {
            Goal other = picked[i];
            if (choice.twist != ANY && choice.twist == other.twist) return true;
            if (choice.twist == ANY && other.twist == ANY && choice.kind == other.kind) return true;
        }
        return false;
    }

    static Object[][] twistKeys(int twist) {
        return twist >= 0 && twist < TWISTS.length ? TWISTS[twist] : new Object[0][];
    }

    static Map<String, Object> overlay(int twist) {
        Map<String, Object> values = new TreeMap<>();
        if (twist == CLASSIC) {
            values.putAll(SettingsActivity.classicRuleValues());
            return values;
        }
        for (Object[] key : twistKeys(twist)) values.put((String) key[0], key[1]);
        return values;
    }

    static int modeOf(int twist, Map<String, ?> prefs) {
        if (twist == ANY) return SettingsActivity.fairPlayMode(prefs);
        if (twist == CLASSIC) {
            return SettingsActivity.fairPlayMode(prefs) == SettingsActivity.FAIR_CLASSIC ? SettingsActivity.FAIR_CLASSIC : SettingsActivity.FAIR_NONE;
        }
        if (twist < 0 || twist >= TWISTS.length) return SettingsActivity.FAIR_NONE;
        Map<String, Object> rest = new TreeMap<>();
        for (Map.Entry<String, ?> entry : prefs.entrySet()) {
            if (entry.getKey() != null) rest.put(entry.getKey(), entry.getValue());
        }
        for (Object[] key : TWISTS[twist]) {
            String name = (String) key[0];
            Object value = prefs.containsKey(name) ? prefs.get(name) : key[2];
            if (!SettingsActivity.samePref(value, key[1])) return SettingsActivity.FAIR_NONE;
            rest.remove(name);
        }
        return SettingsActivity.fairPlayMode(rest);
    }

    static boolean fits(Goal goal, Map<String, ?> prefs) {
        return goal != null && modeOf(goal.twist, prefs) != SettingsActivity.FAIR_NONE;
    }

    static boolean fits(Goal goal, int twist, int restMode) {
        if (goal == null || restMode == SettingsActivity.FAIR_NONE) return false;
        if (goal.twist == ANY) return twist == ANY;
        if (goal.twist == CLASSIC) return twist == ANY && restMode == SettingsActivity.FAIR_CLASSIC;
        return twist == goal.twist;
    }

    static int twistOf(Map<String, ?> prefs) {
        if (SettingsActivity.fairPlayMode(prefs) != SettingsActivity.FAIR_NONE) return ANY;
        for (int twist = STORM; twist < TWISTS.length; twist++) {
            if (modeOf(twist, prefs) != SettingsActivity.FAIR_NONE) return twist;
        }
        return -1;
    }

    static String blocker(Goal goal, Map<String, ?> prefs) {
        if (goal == null || fits(goal, prefs)) return null;
        Map<String, Object> rest = new TreeMap<>();
        for (Map.Entry<String, ?> entry : prefs.entrySet()) {
            if (entry.getKey() != null) rest.put(entry.getKey(), entry.getValue());
        }
        if (goal.twist == CLASSIC) rest.putAll(SettingsActivity.classicRuleValues());
        else for (Object[] key : twistKeys(goal.twist)) rest.remove((String) key[0]);
        String key = SettingsActivity.fairPlayBlocker(rest);
        return key == null ? "" : SettingsActivity.fairKeyName(key);
    }

    static int advance(Goal goal, int progress, int score, double seconds, int flaps, int weekBest) {
        int before = Math.max(0, progress);
        if (score < 1) return before;
        switch (goal.kind) {
            case SCORE: return Math.max(before, Math.min(goal.target, score));
            case TOTAL: return Math.min(goal.target, before + score);
            case ROUNDS: return score >= goal.extra ? Math.min(goal.target, before + 1) : before;
            case SECONDS: return Math.max(before, Math.min(goal.target, (int) Math.floor(seconds)));
            case EXACT: return score == goal.target ? 1 : before;
            case THRIFTY: return score >= goal.target && flaps <= goal.extra ? 1 : before;
            case BEAT_WEEK: return score >= goal.target && score > weekBest ? 1 : before;
            case FLAPS: return Math.min(goal.target, before + Math.max(0, flaps));
            default: return before;
        }
    }

    static boolean done(Goal goal, int progress) {
        return goal != null && progress >= goal.goal();
    }

    static int bonus(int streak) {
        return BONUS + STREAK_STEP * Math.max(0, Math.min(STREAK_CAP, streak - 1));
    }
}

final class Shop {
    static final int NAME = 0;
    static final int ROW = 1;
    static final int GLOW = 2;
    static final int MOTION = 3;
    static final int BADGE = 4;
    static final String KINDS = "nbgai";
    static final String[] KIND_NAMES = {"NAME", "ROW", "GLOW", "MOTION", "BADGE"};

    static final class Item {
        final String id;
        final int kind;
        final String name;
        final int price;
        final int color;
        final int second;
        final int effect;

        Item(String id, String name, int price, int color, int second, int effect) {
            this.id = id;
            this.kind = KINDS.indexOf(id.charAt(0));
            this.name = name;
            this.price = price;
            this.color = color;
            this.second = second;
            this.effect = effect;
        }

        boolean dark() {
            return kind == ROW && effect >= 4;
        }
    }

    static final Item[] ITEMS = {
            new Item("n_gold", "Gold", 300, 0xFFE8A317, 0xFFFFD45C, 0),
            new Item("n_ruby", "Ruby", 300, 0xFFD62F4B, 0xFFFF6B81, 0),
            new Item("n_emerald", "Emerald", 300, 0xFF15955A, 0xFF4FD08E, 0),
            new Item("n_sapphire", "Sapphire", 300, 0xFF2A62D9, 0xFF6F9BFF, 0),
            new Item("n_violet", "Violet", 350, 0xFF8A3FD6, 0xFFC08BFF, 0),
            new Item("n_bubblegum", "Bubblegum", 350, 0xFFF0559D, 0xFFFF9CCB, 0),
            new Item("n_ice", "Frost", 600, 0xFF3AA7D8, 0xFFB8F1FF, 1),
            new Item("n_fire", "Fire", 900, 0xFFE0401F, 0xFFFFB43B, 2),
            new Item("n_ocean", "Ocean", 900, 0xFF1F6FD0, 0xFF2BD3C0, 2),
            new Item("n_rainbow", "Rainbow", 1500, 0xFFFF5A5A, 0xFF5AB4FF, 3),
            new Item("b_sky", "Sky", 400, 0xFF9FDDF5, 0xFFDDF6FF, 0),
            new Item("b_mint", "Mint", 400, 0xFFA8E8C4, 0xFFE4FBEC, 0),
            new Item("b_peach", "Peach", 400, 0xFFFFC2A1, 0xFFFFE7D6, 0),
            new Item("b_sunset", "Sunset", 700, 0xFFFF9A5C, 0xFFE86BB5, 1),
            new Item("b_candy", "Candy", 800, 0xFFFFB3D1, 0xFFFFF3F8, 2),
            new Item("b_gold", "Gold bar", 1200, 0xFFE8B23A, 0xFFFFE79A, 3),
            new Item("b_night", "Night sky", 900, 0xFF1C2350, 0xFF3A3F86, 4),
            new Item("b_lava", "Lava", 900, 0xFF5C1210, 0xFFC93A16, 5),
            new Item("g_gold", "Gold glow", 600, 0xFFFFC93C, 0, 0),
            new Item("g_pink", "Pink glow", 500, 0xFFFF5FA8, 0, 0),
            new Item("g_cyan", "Cyan glow", 500, 0xFF3FD8F0, 0, 0),
            new Item("g_lime", "Lime glow", 500, 0xFF8CE04A, 0, 0),
            new Item("g_rainbow", "Rainbow glow", 1200, 0xFFFF5A5A, 0, 1),
            new Item("a_shimmer", "Shimmer", 700, 0xFFFFFFFF, 0, 0),
            new Item("a_sparkle", "Sparkles", 900, 0xFFFFE27A, 0, 1),
            new Item("a_bubbles", "Bubbles", 800, 0xFF8FDBFF, 0, 2),
            new Item("a_snow", "Snowfall", 800, 0xFFFFFFFF, 0, 3),
            new Item("a_hearts", "Hearts", 900, 0xFFFF5F8A, 0, 4),
            new Item("a_embers", "Embers", 1000, 0xFFFF8A2B, 0, 5),
            new Item("a_feathers", "Feathers", 1000, 0xFFF8F0B0, 0, 6),
            new Item("i_star", "Star", 250, 0xFFFFC93C, 0, 0),
            new Item("i_heart", "Heart", 250, 0xFFFF4F7B, 0, 1),
            new Item("i_coin", "Coin", 300, 0xFFFFC93C, 0, 2),
            new Item("i_bolt", "Lightning", 350, 0xFFFFE04A, 0, 3),
            new Item("i_flame", "Flame", 400, 0xFFFF7A1F, 0, 4),
            new Item("i_bird", "Little bird", 600, 0xFFFFC93C, 0, 5),
            new Item("i_crown", "Crown", 700, 0xFFFFC93C, 0, 6),
            new Item("i_diamond", "Diamond", 900, 0xFF7FE3FF, 0, 7),
    };

    private Shop() {
    }

    static Item item(String id) {
        if (id == null) return null;
        for (Item item : ITEMS) if (item.id.equals(id)) return item;
        return null;
    }

    static boolean validId(String id) {
        return id != null && id.matches("[nbgai]_[a-z0-9]{1,20}");
    }

    private static final Object[] LISTS = new Object[5];

    @SuppressWarnings("unchecked")
    static List<Item> of(int kind) {
        if (kind < 0 || kind >= LISTS.length) return new ArrayList<>();
        if (LISTS[kind] == null) {
            List<Item> list = new ArrayList<>();
            for (Item item : ITEMS) if (item.kind == kind) list.add(item);
            LISTS[kind] = java.util.Collections.unmodifiableList(list);
        }
        return (List<Item>) LISTS[kind];
    }

    static Item[] look(String[] ids) {
        Item[] look = new Item[5];
        if (ids == null) return look;
        for (int i = 0; i < 5 && i < ids.length; i++) {
            Item item = item(ids[i]);
            if (item != null && item.kind == i) look[i] = item;
        }
        return look;
    }
}
