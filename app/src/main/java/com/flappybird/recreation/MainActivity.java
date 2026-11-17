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

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
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
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

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
        else if (gameView != null) gameView.releaseHeldInput();
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
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return;
        Display display = getWindow().getDecorView().getDisplay();
        if (display == null) return;

        Display.Mode current = display.getMode();
        Display.Mode best = current;
        for (Display.Mode mode : display.getSupportedModes()) {
            boolean sameResolution = mode.getPhysicalWidth() == current.getPhysicalWidth()
                    && mode.getPhysicalHeight() == current.getPhysicalHeight();
            if (sameResolution && mode.getRefreshRate() > best.getRefreshRate()) best = mode;
        }
        if (best.getModeId() == current.getModeId()) return;

        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.preferredDisplayModeId = best.getModeId();
        getWindow().setAttributes(params);
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
    private static final float STATUS_FADE_IN_SEC = 0.18f;
    private static final float STATUS_HOLD_SEC = 2f;
    private static final float STATUS_FADE_OUT_SEC = 0.4f;
    private static final float RETURN_HOME_HOLD_SEC = 0.3f;
    private static final float BOARD_OPEN_SEC = 0.32f;
    private static final float BOARD_CLOSE_SEC = 0.18f;
    private static final int BOARD_TAB_IDLE = 0xFFC9C27E;
    private static final int BOARD_DIVIDER = 0x55543847;
    private static final int BOARD_PANEL = 0xFFDED895;
    private static final int BOARD_PANEL_EDGE = 0xFFF8F0B0;
    private static final int BOARD_DARK = 0xFF543847;
    private static final int BOARD_ORANGE = 0xFFFC7858;
    private static final int BOARD_OWN_ROW = 0x40FC7858;
    private static final int BOARD_VERIFIED = 0xFF8E5CF7;
    private static final String BOARD_SHOW_ALL_KEY = "boardShowUnverified";
    private static final int BOARD_DIM = 0x99000000;

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
    private static final int PIPE_COLOR_COUNT = 9;
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
    private static final float FIXED_STEP_SEC = 1.0f / 60.0f;
    private static final int MAX_FIXED_STEPS = 8;
    private static final float STEP_SNAP = 0.03f;
    private static final float STEP_EPSILON = 0.00002f;
    private static final float PIXEL_STEP_EPSILON = 0.001f;
    private static final float CLASSIC_TILT_KICK_PER_SEC = -600f;
    private static final float CLASSIC_TILT_PULL_PER_SEC2 = 1440f;
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

    private final Choreographer choreographer = Choreographer.getInstance();
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
    private boolean touchHeld, flapKeyHeld, flapAltKeyHeld, secondaryClickHeld;
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
    static final int REPLAY_RULES = 2;
    static final int REPLAY_RULES_OLDEST = 1;
    static final int REPLAY_FLAP = 0xFFFFFFFF;
    static final int REPLAY_MAX_EVENTS = 4000000;
    private final Random seedSource = new Random();
    private long replaySeed;
    private boolean replayLive;
    private byte[] replayHeader;
    private int[] replayEvents;
    private int replayCount, replayLastBits;
    private Map<String, Object> replayPrefs;
    private double roundActiveSeconds;
    private long roundRealStart, roundUptimeStart, roundPausedMs, pausedSince = -1L;
    private boolean settingBoardAutoPost = true;
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
    private float boardAnim, boardAlpha = 1f;
    private int boardTab;
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
    private final Runnable playDieSound = () -> playSound(soundDie);

    private final SharedPreferences prefs;
    private final Random random = new Random();
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
    private boolean settingUpsideDownEnabled, settingReversePipesEnabled, settingHapticFeedbackEnabled;
    private boolean settingBirdTrailEnabled, settingGhostModeEnabled, settingRandomPipeColorsEnabled;
    private boolean settingInfiniteFlapEnabled, settingJetpackModeEnabled, settingDrunkModeEnabled;
    private boolean settingDynamicDayNightEnabled, settingScreenShakeEnabled, settingGoldenPipeEnabled;
    private boolean settingMotionBlurEnabled;
    private boolean settingCrashFlashEnabled, settingFixedStepEnabled, settingCeilingEnabled, settingBirdTiltEnabled;
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
        super(context);
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        highScore = intPref(prefs.getAll(), PREF_HIGH_SCORE, 0);
        adoptOldHighScore(prefs);
        vibrator = ContextCompat.getSystemService(context, Vibrator.class);
        weatherSystem = new WeatherSystem(random);

        loadSettings();
        SettingsActivity.ActivityLog.add(context, "GAME", "Game opened on " + SettingsActivity.deviceSummary(context)
                + " (high score " + highScore + ", best earned by playing " + earnedHighScore(prefs) + ")");
        createPaints();
        createParticlePools();
        extractBitmapsFromAtlas();
        sparkleBitmap = createSparkleBitmap();
        smokeBitmap = createSmokeBitmap();
        loadSounds();
        listenForWindowInsets();
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

        pipeColorFilters[1] = new PorterDuffColorFilter(0xFFD05050, PorterDuff.Mode.MULTIPLY);
        pipeColorFilters[2] = new PorterDuffColorFilter(0xFF6080E0, PorterDuff.Mode.MULTIPLY);
        pipeColorFilters[3] = new PorterDuffColorFilter(0xFFE0E060, PorterDuff.Mode.MULTIPLY);
        pipeColorFilters[4] = new PorterDuffColorFilter(0xFFFFFFFF, PorterDuff.Mode.SRC_ATOP);
        pipeColorFilters[5] = new PorterDuffColorFilter(0xFFE879E8, PorterDuff.Mode.MULTIPLY);
        pipeColorFilters[6] = new PorterDuffColorFilter(0xFF505050, PorterDuff.Mode.MULTIPLY);
        pipeColorFilters[7] = new PorterDuffColorFilter(0xFF9040D0, PorterDuff.Mode.MULTIPLY);
        pipeColorFilters[8] = new PorterDuffColorFilter(0xFFE89030, PorterDuff.Mode.MULTIPLY);
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
        Map<String, ?> stored = prefs.getAll();
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
        settingMaxFallSpeed = floatPref(stored, PREF_MAX_FALL_SPEED, DEFAULT_MAX_FALL_SPEED);
        settingCeilingEnabled = flagPref(stored, PREF_CEILING_ENABLED, DEFAULT_CEILING_ENABLED);
        settingBirdTiltEnabled = flagPref(stored, PREF_BIRD_TILT_ENABLED, DEFAULT_BIRD_TILT_ENABLED);
        settingMaxTiltUp = intPref(stored, PREF_MAX_TILT_UP, DEFAULT_MAX_TILT_UP);
        settingMaxTiltDown = intPref(stored, PREF_MAX_TILT_DOWN, DEFAULT_MAX_TILT_DOWN);
        settingTiltSpeed = floatPref(stored, PREF_TILT_SPEED, DEFAULT_TILT_SPEED);
        settingScorePoint = intPref(stored, PREF_SCORE_POINT, DEFAULT_SCORE_POINT);
        settingPixelStepsEnabled = flagPref(stored, PREF_PIXEL_STEPS_ENABLED, DEFAULT_PIXEL_STEPS_ENABLED);
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
        settingBoardShowStatus = flagPref(stored, PREF_BOARD_SHOW_STATUS, DEFAULT_BOARD_SHOW_STATUS);
        settingBoardOpenOnBest = flagPref(stored, PREF_BOARD_OPEN_ON_BEST, DEFAULT_BOARD_OPEN_ON_BEST);
        settingBoardTab = intPref(stored, PREF_BOARD_TAB, DEFAULT_BOARD_TAB) == 1 ? 1 : 0;
        settingBoardSize = Leaderboard.boardSize(intPref(stored, PREF_BOARD_SIZE, DEFAULT_BOARD_SIZE));
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
        }
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
        if (soundPool == null) loadSounds();
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
        if (w > 0 && h > 0 && (w != loggedWidth || h != loggedHeight)) {
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
        Pipe.initHitboxDimensions(scale);

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
        Pipe pipe = new Pipe(x, pipeUpBitmap.getWidth(), pipeUpBitmap.getHeight());
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

        int color = settingRandomPipeColorsEnabled ? random.nextInt(PIPE_COLOR_COUNT) : settingPipeColor;
        pipe.setColorFilter(color >= 0 && color < PIPE_COLOR_COUNT ? pipeColorFilters[color] : null);

        pipe.isGolden = settingGoldenPipeEnabled && random.nextInt(100) < settingGoldenPipeChance;
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
                    spawnSpark(birdX, birdY, random.nextBoolean() ? SparkParticle.TYPE_CRACKLE : SparkParticle.TYPE_FALLING);
                }
            }
        }

        if (apocalypse) {
            apocalypseSparkEmission += APOCALYPSE_SPARKS_PER_SEC * realDeltaTime;
            while (apocalypseSparkEmission >= 1f) {
                apocalypseSparkEmission -= 1f;
                spawnSpark(random.nextFloat() * screenWidth, -50 * fxScale, SparkParticle.TYPE_FALLING);
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
        if (smokeCount < MAX_SMOKE_PARTICLES) smokeParticles[smokeCount++].init(x, y, scale, random);
    }

    private void spawnSpark(float x, float y, int type) {
        if (sparkCount < MAX_SPARK_PARTICLES) sparkParticles[sparkCount++].init(x, y, scale, type, random);
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

        birdVelocityY = capFall(birdVelocityY + gravityPerSec * gameDeltaTime);
        birdY = moveBird(birdY, birdVelocityY * gameDeltaTime);

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
            float angle = birdRotation * side + birdTiltVelocity * dt;
            birdTiltVelocity += CLASSIC_TILT_PULL_PER_SEC2 * speed * speed * dt;
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
        if (random.nextFloat() < strikesPerSec * realDeltaTime) triggerLightning();

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
            birdVelocityY = capFall(birdVelocityY + (gravityPerSec * Math.max(0f, settingCrashGravity)) * dt);
            birdY = moveBird(birdY, birdVelocityY * dt);
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
            if (isOnGround) triggerShake(5.0f, 0.4f);
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
                sparkleXOffset = 0.2f + random.nextFloat() * 0.6f;
                sparkleYOffset = 0.2f + random.nextFloat() * 0.6f;
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

    private void updateScoreCountUp() {
        if (lastScoreTickTime < 0) lastScoreTickTime = clock;
        if (displayedScore >= score) return;

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
        triggerShake(5.0f, 0.4f);
        postDelayed(playDieSound, 300);

        previousBest = highScore;
        isNewBest = score > highScore;
        if (isNewBest) {
            highScore = score;
            prefs.edit().putInt(PREF_HIGH_SCORE, highScore).apply();
        }
        recordRound();

        if (score >= settingMedalPlatinum) currentMedal = MEDAL_PLATINUM;
        else if (score >= settingMedalGold) currentMedal = MEDAL_GOLD;
        else if (score >= settingMedalSilver) currentMedal = MEDAL_SILVER;
        else if (score >= settingMedalBronze) currentMedal = MEDAL_BRONZE;
        else currentMedal = MEDAL_NONE;

        displayedScore = 0;
        lastScoreTickTime = -1;
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
        round.append("; ").append(judgeRound(assists));
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
        h = fingerprint(h, settingSpeedRampPercent);
        h = fingerprint(h, settingGapShrinkPercent);
        h = fingerprint(h, settingFixedStepEnabled ? 1 : 0);
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

    private void startRoundChecks() {
        roundFairMode = currentFairMode;
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
        if (roundFairMode != FAIR_NONE && settingBoardAutoPost) Leaderboard.startSession(getContext(), replaySeed);
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
        replaySeed = seedSource.nextLong() ^ System.nanoTime();
        Pipe.reseed(replaySeed);
        replayLive = false;
        replayHeader = null;
        replayEvents = null;
        replayCount = 0;
        replayLastBits = 0;
        replayPrefs = null;
        if (roundFairMode == FAIR_NONE || !settingBoardAutoPost || !Leaderboard.configured()) return;
        try {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream(4096);
            DataOutputStream out = new DataOutputStream(bytes);
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
            replayPrefs = replayPrefs(prefs.getAll());
            writePrefs(out, replayPrefs);
            out.flush();
            replayHeader = bytes.toByteArray();
            replayEvents = new int[16384];
            replayLive = true;
        } catch (IOException | RuntimeException e) {
            replayLive = false;
        }
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
        if (replayLive && gameState == GameState.PLAYING && !isPaused) recordStep(dt);
        update(dt);
    }

    private void holdMotion() {
        pastBirdX = birdX;
        pastBirdY = birdY;
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
        float amount = stepAccumulator / FIXED_STEP_SEC;
        if (!(amount > 0f) || amount >= 1f) return false;
        keptBirdX = birdX;
        keptBirdY = birdY;
        keptGroundX = groundX;
        keptBackgroundX = backgroundX;
        birdX += (birdX - pastBirdX) * amount;
        birdY += (birdY - pastBirdY) * amount;
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
        groundX = keptGroundX;
        backgroundX = keptBackgroundX;
        for (int i = 0; i < pipes.size(); i++) {
            Pipe pipe = pipes.get(i);
            pipe.x = pipe.keptX;
        }
    }

    private Leaderboard.Replay takeReplay() {
        boolean usable = replayLive && replayHeader != null && replayEvents != null && replayCount > 0
                && replayPrefs != null && replayPrefs.equals(replayPrefs(prefs.getAll()));
        Leaderboard.Replay replay = usable ? new Leaderboard.Replay(replayHeader, replayEvents, replayCount, replaySeed, REPLAY_RULES) : null;
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

    private String judgeRound(String assists) {
        Leaderboard.Replay replay = takeReplay();
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
        } else if (score > 0 && (score > Leaderboard.posted(prefs) || score > Leaderboard.weekPosted(prefs)
                || (replay != null && Leaderboard.wantsReplay(prefs, score)))) {
            final boolean allTime = score > Leaderboard.posted(prefs);
            final boolean newBest = allTime || score > Leaderboard.weekPosted(prefs);
            boardStatusText = newBest ? "Posting..." : "Sending for verification...";
            Leaderboard.submit(getContext(), score, classic, whole, roundFlaps, paused, replay, this::post, (status, error) -> {
                if (Leaderboard.VERIFYING.equals(status)) {
                    boardStatusText = newBest ? "Posted! Verifying your run..." : "Verifying your run...";
                    if (boardOpen) refreshBoard();
                    else if (settingBoardOpenOnBest && newBest) postDelayed(this::openBoardAfterBest, 900);
                } else if (Leaderboard.POSTED.equals(status)) {
                    boardStatusText = allTime ? "Posted! New all-time best" : "Posted! New weekly best";
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
        if (boardHost == null || !Leaderboard.configured() || nameOverlayOpen) return;
        boolean taken = Leaderboard.nameTaken(prefs);
        if (!taken && (Leaderboard.asked(prefs) || !Leaderboard.name(prefs).isEmpty())) return;
        boardHost.askName(taken ? Leaderboard.name(prefs) : "", true, name -> boardRefreshPending = true);
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
        boardTab = settingBoardTab;
        boardShowAll = Boolean.TRUE.equals(prefs.getAll().get(BOARD_SHOW_ALL_KEY));
        boardOpen = true;
        boardClosing = false;
        boardAnim = 0f;
        boardPressed = null;
        if (boardHost != null) boardHost.boardVisible(true);
        refreshBoard();
    }

    private void closeBoard() {
        if (!boardOpen || boardClosing) return;
        boardClosing = true;
        boardPressed = null;
    }

    private void finishClosingBoard() {
        boardOpen = false;
        boardClosing = false;
        boardAnim = 0f;
        releaseBoardCache();
        if (boardHost != null) boardHost.boardVisible(false);
    }

    private void animateBoard(float deltaTime) {
        float step = Math.min(0.1f, Math.max(0f, deltaTime));
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
        }
        if (boardRefreshPending && !nameOverlayOpen && nameSpring[0] < 0.02f) {
            boardRefreshPending = false;
            if (boardOpen) refreshBoard();
        }
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
        if (boardClosing) {
            boardAnim -= step / BOARD_CLOSE_SEC;
            if (boardAnim <= 0f) finishClosingBoard();
        } else if (boardAnim < 1f) {
            boardAnim = Math.min(1f, boardAnim + step / BOARD_OPEN_SEC);
        }
    }

    private String boardHint() {
        if (Leaderboard.nameTaken(prefs)) return "Your name is taken. Tap NAME to choose another";
        if (Leaderboard.name(prefs).isEmpty()) return "Tap NAME to choose a name";
        if (!settingBoardAutoPost) return "Posting is off in Settings > System";
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
        if (!Leaderboard.configured()) {
            boardLoading = false;
            boardNote = "The leaderboard is not set up in this build";
            return;
        }
        boardLoading = true;
        boardNote = boardOffline ? BOARD_OFFLINE_NOTE : "Loading...";
        Leaderboard.fetch(getContext(), settingBoardSize, this::post, (board, error) -> {
            boardLoading = false;
            boardRetrySec = 0f;
            if (board != null) {
                boardOffline = false;
                boardData = board;
                boardNote = boardHint();
            } else {
                boardOffline = error == null || Leaderboard.NO_CONNECTION.equals(error);
                boardNote = boardOffline ? BOARD_OFFLINE_NOTE : error;
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
            if (ready && pressed != null && pressed.contains(x, y)) {
                playSound(soundSwooshing);
                if (pressed == boardCloseRect) closeBoard();
                else if (pressed == boardJoinRect) askBoardName();
                else if (pressed == boardYouRect) scrollBoardToMine();
                else if (pressed == boardToggleRect) {
                    boardShowAll = !boardShowAll;
                    resetBoardScroll();
                    prefs.edit().putBoolean(BOARD_SHOW_ALL_KEY, boardShowAll).apply();
                    if (!boardLoading && boardData != null) boardNote = boardHint();
                } else {
                    int tab = pressed == boardTabAllRect ? 0 : 1;
                    if (tab != boardTab) resetBoardScroll();
                    boardTab = tab;
                    if (!boardLoading && boardData != null) boardNote = boardHint();
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

    private void boardTab(Canvas canvas, Rect rect, String label, boolean selected, float unit) {
        boardShape.set(rect);
        boardPaint.setColor(faded(selected ? BOARD_ORANGE : BOARD_TAB_IDLE));
        canvas.drawRoundRect(boardShape, unit * 3f, unit * 3f, boardPaint);
        if (selected) {
            boardPaint.setColor(faded(BOARD_DARK));
            boardShape.set(rect.left + unit * 2f, rect.bottom - unit * 1.5f, rect.right - unit * 2f, rect.bottom);
            canvas.drawRect(boardShape, boardPaint);
        }
        boardText(canvas, label, rect.exactCenterX(), rect.exactCenterY() + unit * 3f, unit * 8.5f,
                selected ? Color.WHITE : BOARD_DARK, Paint.Align.CENTER, rect.width() - unit * 6f);
    }

    private void boardRow(Canvas canvas, Leaderboard.Entry entry, String rank, float left, float width, float rowTop, float unit, boolean mine) {
        float rowHeight = BOARD_ROW_HEIGHT * unit;
        float baseline = rowTop + 13.5f * unit;
        if (mine) {
            boardPaint.setColor(faded(BOARD_OWN_ROW));
            boardShape.set(left + 7f * unit, rowTop + unit, left + width - 7f * unit, rowTop + rowHeight - unit);
            canvas.drawRoundRect(boardShape, unit * 2f, unit * 2f, boardPaint);
        }
        boardText(canvas, rank, left + 32f * unit, baseline, 10f * unit, BOARD_ORANGE, Paint.Align.RIGHT, 26f * unit);
        String score = String.valueOf(entry.score);
        boardText(canvas, score, left + width - 13f * unit, baseline, 11f * unit, BOARD_DARK, Paint.Align.RIGHT, 0f);
        float scoreWidth = boardTextPaint.measureText(score);
        float badgeRoom = 0f;
        if (entry.verified) {
            badgeRoom = 13f * unit;
            verifiedBadge(canvas, left + width - 13f * unit - scoreWidth - 7.5f * unit, rowTop + 9.5f * unit, unit);
        }
        float nameLeft = left + 37f * unit;
        float nameRoom = width - 37f * unit - 13f * unit - scoreWidth - badgeRoom - 6f * unit - (entry.classic ? 40f * unit : 0f);
        boardText(canvas, entry.name, nameLeft, baseline, 10f * unit, BOARD_DARK, Paint.Align.LEFT, nameRoom);
        if (entry.classic) {
            float nameWidth = Math.min(nameRoom, boardTextPaint.measureText(entry.name));
            float tagLeft = nameLeft + nameWidth + 4f * unit;
            boardPaint.setColor(faded(BOARD_ORANGE));
            boardShape.set(tagLeft, rowTop + 4.5f * unit, tagLeft + 36f * unit, rowTop + 14.5f * unit);
            canvas.drawRoundRect(boardShape, unit * 2f, unit * 2f, boardPaint);
            boardText(canvas, "CLASSIC", tagLeft + 18f * unit, rowTop + 12f * unit, 6.5f * unit, Color.WHITE, Paint.Align.CENTER, 32f * unit);
        }
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
        if (still && reshaped && drawBoardCached(canvas, fade)) return;
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
        boardTabAllRect.set((int) tabsLeft, (int) tabTop, (int) (tabsLeft + tabWidth), (int) (tabTop + 18f * unit));
        boardTabWeekRect.set((int) (tabsLeft + tabWidth + tabGap), (int) tabTop, (int) (tabsLeft + tabWidth * 2f + tabGap), (int) (tabTop + 18f * unit));
        boardTab(canvas, boardTabAllRect, "ALL TIME", boardTab == 0, unit);
        boardTab(canvas, boardTabWeekRect, "WEEKLY", boardTab == 1, unit);

        String own = Leaderboard.uid(prefs);
        List<Leaderboard.Entry> entries = boardEntries();
        Leaderboard.Entry mine = boardMine();
        int rank = boardRank();
        float rowsTop = tabTop + 24f * unit;
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
        int first = Math.max(0, (int) (boardScroll / rowHeight));
        int last = Math.min(count, first + BOARD_ROWS + 1);
        for (int row = first; row < last; row++) {
            boardRow(canvas, entries.get(row), String.valueOf(row + 1), left, width, rowsTop + row * rowHeight - boardScroll, unit,
                    row == boardMineIndex);
        }
        canvas.restore();
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
        if (!boardLoading) {
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
                place = rank > 0 ? "#" + rank : rank < 0 ? Leaderboard.RANK_LIMIT + "+" : "-";
            }
            if (pinned != null) {
                boardPaint.setColor(faded(BOARD_DIVIDER));
                boardShape.set(left + 12f * unit, youTop + unit, left + width - 12f * unit, youTop + unit * 1.8f);
                canvas.drawRect(boardShape, boardPaint);
                float drop = boardPressed == boardYouRect ? unit : 0f;
                boardRow(canvas, pinned, place, left, width, youTop + 2f * unit + drop, unit, true);
            }
        }

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

    private void triggerThunderHaptic() {
        if (!settingHapticFeedbackEnabled || vibrator == null) return;
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
        boolean shaking = shakeTimer > 0;
        if (shaking) canvas.drawColor(Color.BLACK);

        canvas.save();
        if (shaking) {
            float magnitude = currentShakeMagnitude * (shakeTimer / 0.5f);
            float xOffset = (random.nextFloat() - 0.5f) * 2 * magnitude;
            float yOffset = (random.nextFloat() - 0.5f) * 2 * magnitude;
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
        if (settingShowFps) drawFps(canvas);
        if (isPaused) drawPauseOverlay(canvas);
        if (boardStatusAlpha > 0f && boardStatusPlace()) drawBoardStatus(canvas);
        if (boardOpen) drawBoard(canvas);
        if (!boardOpen && (nameOverlayOpen || dimSpring[0] > 0f)) drawNameBackdrop(canvas);

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

    private void updateFpsColor() {
        float target = displayRefreshRate();
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
            float cx = x + (random.nextFloat() * w);
            float cy = startY;
            while (cy < endY && count + 4 <= electricityPoints.length) {
                float nextY = cy + (random.nextInt(40) + 10) * fxScale;
                float nextX = Math.max(x, Math.min(x + w, cx + (random.nextInt(30) - 15) * fxScale));
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
                else drawCenteredBitmap(canvas, getReadyBitmap, -(int) (getPlayableHeight() * 0.15));
                break;
            case WAITING:
                drawCenteredBitmap(canvas, getReadyBitmap, -(int) (getPlayableHeight() * 0.15));
                break;
            case PLAYING:
                int scoreCenterY = systemBarTop + (int) (getPlayableHeight() * 0.15);
                drawScoreWithImages(canvas, score, screenWidth / 2, scoreCenterY);
                drawLives(canvas, scoreCenterY + numberBitmaps[0].getHeight() / 2f + 6f * scale);
                if (settingPauseButtonEnabled && !isPaused) drawPauseButton(canvas);
                break;
            case GAME_OVER:
            case PANEL_SLIDING:
                if (darkenPaint.getAlpha() > 0) {
                    canvas.drawRect(0, 0, screenWidth, screenHeight, darkenPaint);
                }
                if (gameState == GameState.PANEL_SLIDING) drawGameOverScreen(canvas);
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
        drawScoreWithImagesRightAligned(canvas, displayedScore, panelRightEdge, scoreY, smallNumberBitmaps, scaleCorrection);
        boolean countUpFinished = displayedScore == score;
        int shownBest = isNewBest && !countUpFinished ? previousBest : highScore;
        drawScoreWithImagesRightAligned(canvas, shownBest, panelRightEdge, highScoreY, smallNumberBitmaps, scaleCorrection);

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
        int count = splitDigits(number);
        float totalWidth = 0;
        for (int i = 0; i < count; i++) totalWidth += numberBitmaps[digitBuffer[i]].getWidth();

        float currentX = x - (totalWidth / 2f);
        for (int i = count - 1; i >= 0; i--) {
            Bitmap digitBitmap = numberBitmaps[digitBuffer[i]];
            canvas.drawBitmap(digitBitmap, currentX, y - (digitBitmap.getHeight() / 2f), pixelPaint);
            currentX += digitBitmap.getWidth();
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
        if (boardOpen && !nameOverlayOpen && event.getActionMasked() == MotionEvent.ACTION_SCROLL) {
            float amount = event.getAxisValue(MotionEvent.AXIS_VSCROLL);
            if (amount != 0f && scrollBoard(-amount * 3f)) return true;
        }
        return super.onGenericMotionEvent(event);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!isReady) return true;
        int touchX = (int) event.getX();
        int touchY = (int) event.getY();

        int action = event.getActionMasked();
        if (event.getToolType(0) == MotionEvent.TOOL_TYPE_MOUSE) hasMouse = true;
        if (nameOverlayOpen) return true;
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
        if (event.getToolType(0) == MotionEvent.TOOL_TYPE_MOUSE) {
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
        if (boardOpen && (nameOverlayOpen || nameSpring[0] >= 0.02f)) return false;
        if (boardOpen) return boardCloseRect.contains(x, y) || boardJoinRect.contains(x, y)
                || boardTabAllRect.contains(x, y) || boardTabWeekRect.contains(x, y) || boardToggleRect.contains(x, y)
                || boardYouRect.contains(x, y);
        if (gameState == GameState.HOME) {
            return playButtonRect.contains(x, y) || scoreButtonRect.contains(x, y)
                    || rateButtonRect.contains(x, y) || settingsButtonRect.contains(x, y);
        }
        if (gameState == GameState.PANEL_SLIDING) {
            if (gameOverElementsY != gameOverElementsTargetY || isRestarting) return false;
            boolean counted = displayedScore == score;
            return (counted && playButtonRect.contains(x, y))
                    || (counted && settingClassicMenuEnabled && scoreButtonRect.contains(x, y))
                    || (isGameOverIconAnimationDone && settingsButtonRect.contains(x, y));
        }
        if (gameState == GameState.PLAYING) {
            if (isPaused) return resumeDestRect.contains(x, y);
            return settingPauseButtonEnabled && pauseButtonRect.contains(x, y);
        }
        return false;
    }

    private void refreshHeld() {
        isScreenPressed = touchHeld || flapKeyHeld || flapAltKeyHeld;
    }

    void releaseHeldInput() {
        touchHeld = false;
        flapKeyHeld = false;
        flapAltKeyHeld = false;
        secondaryClickHeld = false;
        refreshHeld();
        if (keyButtonCode != KeyEvent.KEYCODE_UNKNOWN) cancelButtonPress();
    }

    boolean onGameKey(int keyCode, KeyEvent event, boolean down) {
        if (!isReady || !settingKeyboardEnabled || keyCode == KeyEvent.KEYCODE_UNKNOWN || event == null || nameOverlayOpen) return false;
        boolean flapKey = keyCode == keyFlap || keyCode == keyFlapAlt;
        boolean playKey = keyCode == keyPlay;
        boolean settingsKey = keyCode == keySettings;
        boolean running = gameState == GameState.PLAYING;
        boolean pauseKey = keyCode == keyPause || (keyCode == KeyEvent.KEYCODE_ESCAPE && running);
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
        refreshHeld();
    }

    private void togglePause() {
        if (gameState != GameState.PLAYING) return;
        if (isPaused) resumeFromPause();
        else pauseGame();
    }

    private void resumeFromPause() {
        if (!isPaused) return;
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
        } else if (gameState == GameState.PANEL_SLIDING) {
            if (gameOverElementsY == gameOverElementsTargetY && !isRestarting) {
                if (displayedScore == score && playButtonRect.contains(x, y)) pressedButtonRect = playButtonRect;
                else if (displayedScore == score && settingClassicMenuEnabled && scoreButtonRect.contains(x, y)) pressedButtonRect = scoreButtonRect;
                if (isGameOverIconAnimationDone && settingsButtonRect.contains(x, y)) pressedButtonRect = settingsButtonRect;
            }
        } else if (isPaused) {
            resumeFromPause();
            handleGameplayTap();
        } else if (gameState == GameState.PLAYING && settingPauseButtonEnabled && pauseButtonRect.contains(x, y)) {
            pauseGame();
        } else {
            handleGameplayTap();
        }

        applyButtonTargets();
        if (pressedButtonRect != null) invalidate();
    }

    private void applyButtonTargets() {
        boolean onHome = gameState == GameState.HOME;
        boolean onPanel = gameState == GameState.PANEL_SLIDING;
        playBtnTarget = (pressedButtonRect == playButtonRect && onHome) ? 1f : 0f;
        scoreBtnTarget = (pressedButtonRect == scoreButtonRect) ? 1f : 0f;
        rateBtnTarget = (pressedButtonRect == rateButtonRect) ? 1f : 0f;
        settingsBtnTarget = (pressedButtonRect == settingsButtonRect && onHome) ? 1f : 0f;
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
            roundSlowFrameLimit = 1.5f / displayRefreshRate();
            noteAssists();
            updateWeatherSounds();
            if (playerFlaps) {
                if (replayLive) recordEvent(REPLAY_FLAP);
                flap();
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
        isPaused = true;
        trackPause();
        pauseFade = 0f;
        touchHeld = false;
        flapKeyHeld = false;
        flapAltKeyHeld = false;
        refreshHeld();
        stopWeatherSounds();
        invalidate();
    }

    private void onFingerUp(int x, int y) {
        Rect button = pressedButtonRect;
        if (button != null && button.contains(x, y)) {
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
                }
            } else if (gameState == GameState.PANEL_SLIDING) {
                playSound(soundSwooshing);
                if (button == playButtonRect) {
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
        playSound(soundWing);
        if (settingHapticFeedbackEnabled && !settingAutopilotEnabled) {
            performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
        }
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
        boolean audible = settingSoundEnabled && hostResumed && inRound;
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
        try {
            setLoopPlaying(rainSoundPlayer, false);
            setLoopPlaying(stormSoundPlayer, false);
        } catch (IllegalStateException e) {
            Log.e(TAG, "MediaPlayer state error in stopWeatherSounds", e);
            logProblem("Weather sound error while stopping: " + e);
        }
    }

    private void playSound(final int soundId) {
        final SoundPool pool = soundPool;
        final ExecutorService executor = soundExecutor;
        if (!settingSoundEnabled || !hostResumed || soundId == 0) return;
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
        choreographer.postFrameCallback(this);
    }

    private void stopLoop() {
        isRunning = false;
        trackPause();
        choreographer.removeFrameCallback(this);
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
        choreographer.postFrameCallback(this);
        if (lastFrameTimeNanos == 0) {
            lastFrameTimeNanos = frameTimeNanos;
            return;
        }
        float deltaTime = (frameTimeNanos - lastFrameTimeNanos) / 1_000_000_000.0f;
        lastFrameTimeNanos = frameTimeNanos;
        if (deltaTime <= 0) return;
        fpsFrames++;
        fpsTimer += deltaTime;
        animateBoard(deltaTime);
        if (gameState == GameState.PLAYING && !isPaused) {
            roundActiveSeconds += deltaTime;
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
        if (settingFixedStepEnabled) {
            float advance = Math.min(deltaTime, FIXED_STEP_SEC * MAX_FIXED_STEPS);
            if (advance > 0f) {
                float framesPerStep = FIXED_STEP_SEC / advance;
                int whole = Math.round(framesPerStep);
                if (whole >= 1 && whole <= 4 && Math.abs(framesPerStep - whole) < STEP_SNAP * whole) advance = FIXED_STEP_SEC / whole;
            }
            stepAccumulator += advance;
            int steps = 0;
            while (stepAccumulator >= FIXED_STEP_SEC - STEP_EPSILON && steps < MAX_FIXED_STEPS) {
                holdMotion();
                step(FIXED_STEP_SEC);
                if (gameState != motionState) motionValid = false;
                stepAccumulator = Math.max(0f, stepAccumulator - FIXED_STEP_SEC);
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
        final float z;
        final float bandDepth;
        float x, y;
        float ySpeed, size, length;
        float offset;
        float bounceX = 0, bounceY = 0;

        WeatherParticle(float z, float bandDepth) {
            this.z = z;
            this.bandDepth = bandDepth;
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

    private static final Random random = new Random();
    private static float pipeHeadWidth, pipeHeadHeight;

    static void reseed(long seed) {
        random.setSeed(seed);
    }
    private static float pipeBodyWidth, pipeBodyOffsetX;

    float x, pastX = Float.NaN, keptX;
    int topPipeY;
    boolean isScored;
    int width, height;
    public final Paint pipePaint;
    public boolean isGolden;

    private final Rect topHeadRect = new Rect(), topBodyRect = new Rect();
    private final Rect bottomHeadRect = new Rect(), bottomBodyRect = new Rect();
    private float currentYOffset = 0;

    public Pipe(float x, int width, int height) {
        this.x = x;
        this.width = width;
        this.height = height;
        this.isScored = false;
        this.isGolden = false;
        this.pipePaint = new Paint();
        this.pipePaint.setFilterBitmap(false);
        this.pipePaint.setAntiAlias(false);
    }

    public static void initHitboxDimensions(float scale) {
        pipeHeadWidth = UNSCALED_HEAD_WIDTH * scale;
        pipeHeadHeight = UNSCALED_HEAD_HEIGHT * scale;
        pipeBodyWidth = UNSCALED_BODY_WIDTH * scale;
        pipeBodyOffsetX = (pipeHeadWidth - pipeBodyWidth) / 2.0f;
    }

    public void resetHeight(int pipeGap, int playAreaHeight, int topOffset, float variationMultiplier) {
        float effectiveVariation = Math.max(0.001f, variationMultiplier);
        float marginPercent = Math.max(0.0f, Math.min(0.49f, BASE_MARGIN_PERCENT / effectiveVariation));

        int margin = (int) (playAreaHeight * marginPercent);
        int availableRange = playAreaHeight - (2 * margin) - pipeGap;
        if (availableRange <= 0) {
            this.topPipeY = (playAreaHeight / 2) + topOffset - pipeGap / 2;
        } else {
            this.topPipeY = random.nextInt(availableRange) + margin + topOffset;
        }
        this.currentYOffset = 0;
    }

    public void resetHeightClassic(int pipeGap, int playAreaHeight, int topOffset, float variationMultiplier, float scale) {
        float spread = CLASSIC_GAP_BOTTOM_SPREAD * Math.max(0f, variationMultiplier);
        float bottom = (CLASSIC_GAP_BOTTOM_CENTER + (random.nextFloat() - 0.5f) * spread) * scale;
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
        float top = getCurrentTopPipeY() - pipeHeadHeight;
        float bottom = getCurrentTopPipeY();
        float visualWidth = this.width * widthMultiplier;
        float xPos = x + (this.width - visualWidth) / 2.0f;
        topHeadRect.set((int) xPos, (int) top, (int) (xPos + visualWidth), (int) bottom);
        return topHeadRect;
    }

    public Rect getTopBodyRect(float widthMultiplier) {
        float bottom = getCurrentTopPipeY() - pipeHeadHeight;
        float visualWidth = pipeBodyWidth * widthMultiplier;
        float xPos = x + pipeBodyOffsetX + (pipeBodyWidth - visualWidth) / 2.0f;
        topBodyRect.set((int) xPos, (int) -FAR_OFF_SCREEN, (int) (xPos + visualWidth), (int) bottom);
        return topBodyRect;
    }

    public Rect getBottomHeadRect(int pipeGap, float widthMultiplier) {
        float top = getBottomPipeY(pipeGap);
        float bottom = top + pipeHeadHeight;
        float visualWidth = this.width * widthMultiplier;
        float xPos = x + (this.width - visualWidth) / 2.0f;
        bottomHeadRect.set((int) xPos, (int) top, (int) (xPos + visualWidth), (int) bottom);
        return bottomHeadRect;
    }

    public Rect getBottomBodyRect(int pipeGap, float widthMultiplier) {
        float top = getBottomPipeY(pipeGap) + pipeHeadHeight;
        float visualWidth = pipeBodyWidth * widthMultiplier;
        float xPos = x + pipeBodyOffsetX + (pipeBodyWidth - visualWidth) / 2.0f;
        bottomBodyRect.set((int) xPos, (int) top, (int) (xPos + visualWidth), (int) (top + FAR_OFF_SCREEN));
        return bottomBodyRect;
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
            float drop = down ? unit * 1.5f : 0f;
            int alpha = isEnabled() ? 255 : 120;
            paint.setColor(DARK);
            paint.setAlpha(alpha);
            shape.set(0, unit * 1.5f, getWidth(), getHeight());
            canvas.drawRoundRect(shape, unit * 3f, unit * 3f, paint);
            shape.set(0, drop, getWidth(), getHeight() - unit * 1.5f + drop);
            canvas.drawRoundRect(shape, unit * 3f, unit * 3f, paint);
            paint.setColor(Color.WHITE);
            paint.setAlpha(alpha);
            shape.inset(unit * 1.5f, unit * 1.5f);
            canvas.drawRoundRect(shape, unit * 2f, unit * 2f, paint);
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
        message.setText(firstTime ? "Shown on the online leaderboard. Fair rounds post automatically; turn that off in Settings > System." : "");
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
