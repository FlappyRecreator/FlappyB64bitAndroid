package com.flappybird.recreation;

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
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.view.Choreographer;
import android.view.Display;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import static com.flappybird.recreation.SettingsActivity.*;

public class MainActivity extends AppCompatActivity {

    private GameView gameView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        EdgeToEdge.enable(this);
        super.onCreate(savedInstanceState);
        hideSystemBars();

        setContentView(R.layout.main);
        FrameLayout gameContainer = findViewById(R.id.game_container);
        gameView = new GameView(this);
        gameContainer.addView(gameView);
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        requestHighestRefreshRate();
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
    private static final float DEATH_GRAVITY_MULTIPLIER = 1.5f;
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
    private static final float PLATINUM_OFFSET_X = -1.0f;
    private static final float PLATINUM_OFFSET_Y = 1.0f;
    private static final float GOLD_OFFSET_X = -1.0f;
    private static final float GOLD_OFFSET_Y = 1.0f;
    private static final int MIN_PLAYABLE_HEIGHT_PX = 48;

    private static final float BUTTON_PRESS_SPEED = 25f;
    private static final float TRANSITION_FADE_SPEED_PER_SEC = 800.0f;
    private static final float FLASH_DECAY_PER_SEC = 1500f;
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
    private static final float DAY_NIGHT_CYCLE_SEC = 120.0f;
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
    private final Bitmap[] unscaledMedalsBitmaps = new Bitmap[4];
    private final Bitmap[] unscaledNumberBitmaps = new Bitmap[10];

    private Bitmap bgDayBitmap, bgNightBitmap, groundBitmap;
    private Bitmap pipeUpBitmap, pipeDownBitmap;
    private final Bitmap[][] scaledAllBirdBitmaps = new Bitmap[3][3];
    private final Bitmap[] birdBitmaps = new Bitmap[3];
    private Bitmap getReadyBitmap, gameOverBitmap, scorePanelBitmap, playButtonBitmap;
    private Bitmap scoreButtonBitmap, titleBitmap, copyrightBitmap, settingsButtonBitmap;
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
    private final Set<Integer> settingLightningScores = new HashSet<>();
    private int settingMegaStrikeScore = DEFAULT_MEGA_STRIKE_SCORE;

    public GameView(Context context) {
        super(context);
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        highScore = prefs.getInt(PREF_HIGH_SCORE, 0);
        vibrator = ContextCompat.getSystemService(context, Vibrator.class);
        weatherSystem = new WeatherSystem(random);

        loadSettings();
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
            Insets bars = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            Insets safe = windowInsets.getInsets(
                    WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.displayCutout());
            windowInsetTop = bars.top;
            windowInsetBottom = bars.bottom;
            windowInsetLeft = safe.left;
            windowInsetRight = safe.right;
            applyLayout();
            return WindowInsetsCompat.CONSUMED;
        });
    }

    private void loadSettings() {
        settingBirdColor = prefs.getInt(PREF_BIRD_COLOR, DEFAULT_BIRD_COLOR);
        settingBackground = prefs.getInt(PREF_BACKGROUND, DEFAULT_BACKGROUND);
        settingSoundEnabled = prefs.getBoolean(PREF_SOUND_ENABLED, DEFAULT_SOUND_ENABLED);
        settingWingAnimationEnabled = prefs.getBoolean(PREF_WING_ANIMATION_ENABLED, DEFAULT_WING_ANIMATION_ENABLED);
        settingPipeColor = prefs.getInt(PREF_PIPE_COLOR, DEFAULT_PIPE_COLOR);
        settingHideSettingsIcon = prefs.getBoolean(PREF_HIDE_SETTINGS_ICON, DEFAULT_HIDE_SETTINGS_ICON);
        settingGameOverOpacity = prefs.getInt(PREF_GAMEOVER_OPACITY, DEFAULT_GAMEOVER_OPACITY);
        settingGameSpeed = prefs.getFloat(PREF_GAME_SPEED, DEFAULT_GAME_SPEED);
        settingGravity = prefs.getFloat(PREF_GRAVITY, DEFAULT_GRAVITY);
        settingJumpStrength = prefs.getFloat(PREF_JUMP_STRENGTH, DEFAULT_JUMP_STRENGTH);
        settingPipeGap = prefs.getFloat(PREF_PIPE_GAP, DEFAULT_PIPE_GAP);
        settingBirdHangDelay = prefs.getFloat(PREF_BIRD_HANG_DELAY, DEFAULT_BIRD_HANG_DELAY);
        settingPipeSpacing = prefs.getFloat(PREF_PIPE_SPACING, DEFAULT_PIPE_SPACING);
        settingBirdHitbox = prefs.getFloat(PREF_BIRD_HITBOX, DEFAULT_BIRD_HITBOX);
        settingMovingPipesEnabled = prefs.getBoolean(PREF_MOVING_PIPES_ENABLED, DEFAULT_MOVING_PIPES_ENABLED);
        settingPipeMoveTier1 = prefs.getInt(PREF_PIPE_MOVE_TIER_1_SCORE, DEFAULT_PIPE_MOVE_TIER_1_SCORE);
        settingPipeMoveTier2 = prefs.getInt(PREF_PIPE_MOVE_TIER_2_SCORE, DEFAULT_PIPE_MOVE_TIER_2_SCORE);
        settingNoClipEnabled = prefs.getBoolean(PREF_NO_CLIP_ENABLED, DEFAULT_NO_CLIP_ENABLED);
        settingRainbowBirdEnabled = prefs.getBoolean(PREF_RAINBOW_BIRD_ENABLED, DEFAULT_RAINBOW_BIRD_ENABLED);
        settingUpsideDownEnabled = prefs.getBoolean(PREF_UPSIDE_DOWN_ENABLED, DEFAULT_UPSIDE_DOWN_ENABLED);
        settingReversePipesEnabled = prefs.getBoolean(PREF_REVERSE_PIPES_ENABLED, DEFAULT_REVERSE_PIPES_ENABLED);
        settingPipeVariation = prefs.getFloat(PREF_PIPE_VARIATION, DEFAULT_PIPE_VARIATION);
        settingScoreMultiplier = prefs.getInt(PREF_SCORE_MULTIPLIER, DEFAULT_SCORE_MULTIPLIER);
        settingHapticFeedbackEnabled = prefs.getBoolean(PREF_HAPTIC_FEEDBACK_ENABLED, DEFAULT_HAPTIC_FEEDBACK_ENABLED);
        settingBirdTrailEnabled = prefs.getBoolean(PREF_BIRD_TRAIL_ENABLED, DEFAULT_BIRD_TRAIL_ENABLED);
        settingGhostModeEnabled = prefs.getBoolean(PREF_GHOST_MODE_ENABLED, DEFAULT_GHOST_MODE_ENABLED);
        settingPipeSpeedVariation = prefs.getFloat(PREF_PIPE_SPEED_VARIATION, DEFAULT_PIPE_SPEED_VARIATION);
        settingBirdSize = prefs.getFloat(PREF_BIRD_SIZE, DEFAULT_BIRD_SIZE);
        settingPipeWidth = prefs.getFloat(PREF_PIPE_WIDTH, DEFAULT_PIPE_WIDTH);
        settingBgScrollSpeed = prefs.getFloat(PREF_BG_SCROLL_SPEED, DEFAULT_BG_SCROLL_SPEED);
        settingGroundScrollSpeed = prefs.getFloat(PREF_GROUND_SCROLL_SPEED, DEFAULT_GROUND_SCROLL_SPEED);
        settingRandomPipeColorsEnabled = prefs.getBoolean(PREF_RANDOM_PIPE_COLORS_ENABLED, DEFAULT_RANDOM_PIPE_COLORS_ENABLED);
        settingInfiniteFlapEnabled = prefs.getBoolean(PREF_INFINITE_FLAP_ENABLED, DEFAULT_INFINITE_FLAP_ENABLED);
        settingJetpackModeEnabled = prefs.getBoolean(PREF_JETPACK_MODE_ENABLED, DEFAULT_JETPACK_MODE_ENABLED);
        settingDrunkModeEnabled = prefs.getBoolean(PREF_DRUNK_MODE_ENABLED, DEFAULT_DRUNK_MODE_ENABLED);
        settingDrunkModeStrength = prefs.getFloat(PREF_DRUNK_MODE_STRENGTH, DEFAULT_DRUNK_MODE_STRENGTH);
        settingWeatherEffect = prefs.getInt(PREF_WEATHER_EFFECT, DEFAULT_WEATHER_EFFECT);
        settingDynamicDayNightEnabled = prefs.getBoolean(PREF_DYNAMIC_DAY_NIGHT_ENABLED, DEFAULT_DYNAMIC_DAY_NIGHT_ENABLED);
        settingScreenShakeEnabled = prefs.getBoolean(PREF_SCREEN_SHAKE_ENABLED, DEFAULT_SCREEN_SHAKE_ENABLED);
        settingScreenShakeStrength = prefs.getFloat(PREF_SCREEN_SHAKE_STRENGTH, DEFAULT_SCREEN_SHAKE_STRENGTH);
        settingGoldenPipeEnabled = prefs.getBoolean(PREF_GOLDEN_PIPE_ENABLED, DEFAULT_GOLDEN_PIPE_ENABLED);
        settingGoldenPipeChance = prefs.getInt(PREF_GOLDEN_PIPE_CHANCE, DEFAULT_GOLDEN_PIPE_CHANCE);
        settingGoldenPipeBonus = prefs.getInt(PREF_GOLDEN_PIPE_BONUS, DEFAULT_GOLDEN_PIPE_BONUS);
        settingMegaStrikeScore = prefs.getInt(PREF_MEGA_STRIKE_SCORE, DEFAULT_MEGA_STRIKE_SCORE);
        settingMotionBlurEnabled = prefs.getBoolean(PREF_MOTION_BLUR_ENABLED, DEFAULT_MOTION_BLUR_ENABLED);
        settingMotionBlurStrength = prefs.getFloat(PREF_MOTION_BLUR_STRENGTH, DEFAULT_MOTION_BLUR_STRENGTH);

        parseLightningScores(prefs.getString(PREF_LIGHTNING_SCORES, DEFAULT_LIGHTNING_SCORES));
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
        if (unscaledButtonSettings == null) Log.w(TAG, "Could not load 'settingsbutton.png'.");

        for (int i = 0; i < 10; i++) {
            int resId = getResources().getIdentifier("number_" + i, "drawable", packageName);
            if (resId != 0) unscaledNumberBitmaps[i] = BitmapFactory.decodeResource(getResources(), resId, options);
            if (unscaledNumberBitmaps[i] == null) {
                Log.e(TAG, "Missing number resource: number_" + i);
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
        screenWidth = w;
        screenHeight = h;
        applyLayout();
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
        applyTuning();

        baseBirdX = gameplayBirdX;
        isRestarting = false;
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
        birdFrame = 0;

        spawnPipes();

        score = 0;
        displayedScore = 0;
        lastScoreTickTime = -1;
        currentMedal = MEDAL_NONE;
        darkenPaint.setAlpha(0);
    }

    private void applyTuning() {
        float gravityDirection = settingUpsideDownEnabled ? -1 : 1;
        float pipeDirection = settingReversePipesEnabled ? -1 : 1;
        gravityPerSec = baseGravityPerSec * settingGravity * gravityDirection;
        flapVelocityPerSec = baseFlapVelocityPerSec * settingJumpStrength * gravityDirection;
        pipeSpeedPerSec = basePipeSpeedPerSec * settingGameSpeed * speedVariation * pipeDirection;
        rotationDelayThresholdPerSec = baseRotationDelayThresholdPerSec * settingBirdHangDelay;

        pipeGap = (int) (UNSCALED_BIRD_HEIGHT_FOR_GAP * scale * PIPE_GAP_BIRD_HEIGHT_MULTIPLIER * settingPipeGap);
        pipeSpacing = Math.max(1, (int) (unscaledBgDay.getWidth() * PIPE_SPACING_FACTOR * scale * settingPipeSpacing));
        gameplayBirdX = settingReversePipesEnabled ? screenWidth * 2 / 3f : screenWidth / 3f;
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
        pipe.resetHeight(pipeGap, getPlayableHeight() - groundHeight, systemBarTop, settingPipeVariation);
        pipe.isScored = false;

        int color = settingRandomPipeColorsEnabled ? random.nextInt(PIPE_COLOR_COUNT) : settingPipeColor;
        pipe.setColorFilter(color >= 0 && color < PIPE_COLOR_COUNT ? pipeColorFilters[color] : null);

        pipe.isGolden = settingGoldenPipeEnabled && random.nextInt(100) < settingGoldenPipeChance;
    }

    private void update(float realDeltaTime) {
        clock += realDeltaTime;
        float gameDeltaTime = realDeltaTime * timeDilation;
        GameState stateBefore = gameState;
        float birdXBefore = birdX, birdYBefore = birdY, birdRotationBefore = birdRotation;

        if (flashAlpha > 0) flashAlpha = Math.max(0f, flashAlpha - FLASH_DECAY_PER_SEC * realDeltaTime);
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
                birdY = idleBirdY();
                if (settingUpsideDownEnabled) {
                    birdY += getPlayableHeight() * 0.18f;
                    birdY = screenHeight - birdY;
                }
                birdRotation = 0;
                break;
            case TRANSITION_TO_WAITING:
                updateTransitionToWaiting(gameDeltaTime);
                break;
            case WAITING:
                birdX = baseBirdX;
                birdY = idleBirdY();
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
        settingsBtnProgress += (settingsBtnTarget - settingsBtnProgress) * pressSpeed;
        okBtnProgress += (okBtnTarget - okBtnProgress) * pressSpeed;
        gameOverSettingsBtnProgress += (gameOverSettingsBtnTarget - gameOverSettingsBtnProgress) * pressSpeed;
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
            birdFrame = flapping ? (int) (clock / WING_FRAME_SEC_PLAYING) % birdBitmaps.length : 1;
        } else if (gameState == GameState.HOME || gameState == GameState.TRANSITION_TO_WAITING
                || gameState == GameState.WAITING) {
            birdFrame = settingWingAnimationEnabled ? (int) (clock / WING_FRAME_SEC_IDLE) % birdBitmaps.length : 1;
        }
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
            dayNightCycleProgress = (dayNightCycleProgress + gameDeltaTime / DAY_NIGHT_CYCLE_SEC) % 1.0f;
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
                settingRainbowBirdEnabled ? rainbowHue : -1);
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

    private float birdHalfHeight(int frame) {
        return (birdBitmaps[frame].getHeight() * settingBirdSize) / 2f;
    }

    private void updatePlaying(float realDeltaTime, float gameDeltaTime) {
        if (settingJetpackModeEnabled && isScreenPressed) {
            float thrust = jetpackThrustPerSec * settingJumpStrength * (settingUpsideDownEnabled ? -1 : 1);
            birdVelocityY -= thrust * gameDeltaTime;
        }

        birdVelocityY += gravityPerSec * gameDeltaTime;
        birdY += birdVelocityY * gameDeltaTime;

        if (!settingNoClipEnabled && !settingUpsideDownEnabled) {
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

        if (settingUpsideDownEnabled) {
            if (birdVelocityY > 0) {
                birdRotation = Math.min(25f, birdRotation + ROTATION_UP_SPEED_PER_SEC * gameDeltaTime);
            } else if (birdVelocityY < -rotationDelayThresholdPerSec) {
                birdRotation = Math.max(-90f, birdRotation - ROTATION_DOWN_SPEED_PER_SEC * gameDeltaTime);
            }
        } else {
            if (birdVelocityY < 0) {
                birdRotation = Math.max(-25f, birdRotation - ROTATION_UP_SPEED_PER_SEC * gameDeltaTime);
            } else if (birdVelocityY > rotationDelayThresholdPerSec) {
                birdRotation = Math.min(90f, birdRotation + ROTATION_DOWN_SPEED_PER_SEC * gameDeltaTime);
            }
        }
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
        birdY = idleBirdY();
        if (settingUpsideDownEnabled) birdY = screenHeight - birdY;
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
            birdVelocityY += (gravityPerSec * DEATH_GRAVITY_MULTIPLIER) * dt;
            birdY += birdVelocityY * dt;
            if (settingUpsideDownEnabled) {
                if (birdRotation > -90) birdRotation -= DEATH_ROTATION_SPEED_PER_SEC * dt;
            } else {
                if (birdRotation < 90) birdRotation += DEATH_ROTATION_SPEED_PER_SEC * dt;
            }
            birdHitGroundTime = -1;
            return;
        }

        if (isOnGround) {
            if (settingUpsideDownEnabled) {
                birdY = systemBarTop + halfHeight;
                birdRotation = -90f;
            } else {
                birdY = groundTop - halfHeight;
                birdRotation = 90f;
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

        if (isRestarting) {
            transitionAlpha += TRANSITION_FADE_SPEED_PER_SEC * realDeltaTime;
            if (transitionAlpha >= 255) {
                transitionAlpha = 255;
                resetGame();
                gameState = GameState.TRANSITION_TO_WAITING;
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

            boolean passedBird = settingReversePipesEnabled ? pipe.x > birdX : pipe.x + pipe.width < birdX;
            if (!pipe.isScored && passedBird) {
                pipe.isScored = true;
                score += settingScoreMultiplier;
                if (pipe.isGolden) score += settingGoldenPipeBonus;
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
        float halfWidth = baseHitboxWidth * hitboxMultiplier * settingBirdSize / 2f;
        float halfHeight = baseHitboxHeight * hitboxMultiplier * settingBirdSize / 2f;
        birdRect.set((int) (birdX - halfWidth), (int) (birdY - halfHeight),
                (int) (birdX + halfWidth), (int) (birdY + halfHeight));
    }

    private void checkCollisions() {
        if (settingNoClipEnabled) return;
        float halfHeight = birdHalfHeight(birdFrame);
        float groundTop = groundTopY();

        if (settingUpsideDownEnabled) {
            if (birdRect.bottom >= groundTop || birdY - halfHeight <= systemBarTop) {
                gameOver();
                return;
            }
        } else if (birdY + halfHeight >= groundTop) {
            gameOver();
            return;
        }

        for (int i = 0; i < pipes.size(); i++) {
            Pipe pipe = pipes.get(i);
            if (Rect.intersects(birdRect, pipe.getTopHeadRect(settingPipeWidth))
                    || Rect.intersects(birdRect, pipe.getTopBodyRect(settingPipeWidth))
                    || Rect.intersects(birdRect, pipe.getBottomHeadRect(pipeGap, settingPipeWidth))
                    || Rect.intersects(birdRect, pipe.getBottomBodyRect(pipeGap, settingPipeWidth))) {
                gameOver();
                return;
            }
        }
    }

    private void gameOver() {
        if (gameState != GameState.PLAYING) return;
        gameState = GameState.GAME_OVER;
        gameOverTime = clock;

        cinematicPhase = 0;
        cameraZoom = 1.0f;
        timeDilation = 1.0f;

        flashAlpha = 255f;
        stopWeatherSounds();
        playSound(soundHit);
        triggerShake(5.0f, 0.4f);
        postDelayed(playDieSound, 300);

        if (score > highScore) {
            highScore = score;
            prefs.edit().putInt(PREF_HIGH_SCORE, highScore).apply();
        }

        if (score >= 40) currentMedal = MEDAL_PLATINUM;
        else if (score >= 30) currentMedal = MEDAL_GOLD;
        else if (score >= 20) currentMedal = MEDAL_SILVER;
        else if (score >= 10) currentMedal = MEDAL_BRONZE;
        else currentMedal = MEDAL_NONE;

        displayedScore = 0;
        lastScoreTickTime = -1;
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
        flashAlpha = 255;
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
        flashAlpha = 255;
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

        if (transitionAlpha > 0) {
            transitionPaint.setAlpha((int) transitionAlpha);
            canvas.drawRect(0, 0, screenWidth, screenHeight, transitionPaint);
        }
        if (flashAlpha > 0) {
            flashPaint.setAlpha((int) flashAlpha);
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

        for (int i = 0; i < sparkCount; i++) {
            sparkParticles[i].draw(canvas, sparkPaint);
        }

        if (settingWeatherEffect > 0) {
            weatherSystem.drawFront(canvas);
            weatherSystem.drawLightingOverlay(canvas);
        }
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
        birdPaint.setColorFilter(filter);

        int alpha = settingGhostModeEnabled ? 128 : 255;
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
                drawScoreWithImages(canvas, score, screenWidth / 2, systemBarTop + (int) (getPlayableHeight() * 0.15));
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
        float titleY = getPlayableHeight() * 0.22f + systemBarTop;
        drawFitted(canvas, titleBitmap, titleY);

        if (settingsButtonBitmap != null) {
            layoutSettingsButton();
            if (!settingHideSettingsIcon) {
                float currentScale = 1.0f - (settingsBtnProgress * 0.15f);
                canvas.save();
                canvas.scale(currentScale, currentScale, settingsButtonDestRect.centerX(), settingsButtonDestRect.centerY());
                canvas.drawBitmap(settingsButtonBitmap, settingsButtonDestRect.left, settingsButtonDestRect.top, pixelPaint);
                canvas.restore();
            }
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
        float buttonsY = titleY + titleBitmap.getHeight() + getPlayableHeight() * 0.18f;

        playButtonRect.set((int) playBtnX, (int) buttonsY, (int) (playBtnX + newPlayWidth), (int) (buttonsY + newPlayHeight));
        scoreButtonRect.set((int) scoreBtnX, (int) buttonsY, (int) (scoreBtnX + newScoreWidth), (int) (buttonsY + newScoreHeight));
        playButtonDestRect.set(playButtonRect);
        scoreButtonDestRect.set(scoreButtonRect);

        float scaledPressOffsetY = pressOffsetY * scaleCorrection;
        drawButton(canvas, playButtonBitmap, playButtonDestRect, playBtnProgress * scaledPressOffsetY, pixelPaint);
        drawButton(canvas, scoreButtonBitmap, scoreButtonDestRect, scoreBtnProgress * scaledPressOffsetY, pixelPaint);

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
        drawScoreWithImagesRightAligned(canvas, highScore, panelRightEdge, highScoreY, smallNumberBitmaps, scaleCorrection);

        boolean countUpFinished = displayedScore == score;

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
            playButtonRect.set((int) btnX, (int) btnY, (int) (btnX + playButtonWidth), (int) (btnY + playButtonHeight));

            restartBtnDestRect.set(playButtonRect);
            drawButton(canvas, playButtonBitmap, restartBtnDestRect, okBtnProgress * (pressOffsetY * scaleCorrection), pixelPaint);
        }

        if (settingsButtonBitmap != null && gameOverSettingsAnimProgress > 0) {
            layoutSettingsButton();
            if (!settingHideSettingsIcon) {
                float baseScale = 1.0f - (gameOverSettingsBtnProgress * 0.15f);
                float popScale = (float) (Math.sin(gameOverSettingsAnimProgress * Math.PI / 2));
                float finalScale = baseScale * popScale;

                pixelPaint.setAlpha((int) (255 * gameOverSettingsAnimProgress));
                canvas.save();
                canvas.scale(finalScale, finalScale, settingsButtonDestRect.centerX(), settingsButtonDestRect.centerY());
                canvas.drawBitmap(settingsButtonBitmap, settingsButtonDestRect.left, settingsButtonDestRect.top, pixelPaint);
                canvas.restore();
                pixelPaint.setAlpha(255);
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
        int count = splitDigits(number);
        float currentX = rightX;
        for (int i = 0; i < count; i++) {
            Bitmap digitBitmap = numberSet[digitBuffer[i]];
            float digitWidth = digitBitmap.getWidth() * scaleCorrection;
            float digitHeight = digitBitmap.getHeight() * scaleCorrection;
            currentX -= digitWidth;
            scoreDigitDestRect.set(currentX, y - digitHeight, currentX + digitWidth, y);
            canvas.drawBitmap(digitBitmap, null, scoreDigitDestRect, pixelPaint);
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
    public boolean onTouchEvent(MotionEvent event) {
        if (!isReady) return true;
        int touchX = (int) event.getX();
        int touchY = (int) event.getY();

        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                isScreenPressed = true;
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
                isScreenPressed = false;
                onFingerUp(touchX, touchY);
                break;
            case MotionEvent.ACTION_CANCEL:
                isScreenPressed = false;
                cancelButtonPress();
                break;
        }
        return true;
    }

    private void onFingerDown(int x, int y) {
        pressedButtonRect = null;

        if (gameState == GameState.HOME) {
            if (playButtonRect.contains(x, y)) pressedButtonRect = playButtonRect;
            else if (scoreButtonRect.contains(x, y)) pressedButtonRect = scoreButtonRect;
            else if (settingsButtonRect.contains(x, y)) pressedButtonRect = settingsButtonRect;
        } else if (gameState == GameState.PANEL_SLIDING) {
            if (gameOverElementsY == gameOverElementsTargetY && !isRestarting) {
                if (displayedScore == score && playButtonRect.contains(x, y)) pressedButtonRect = playButtonRect;
                if (isGameOverIconAnimationDone && settingsButtonRect.contains(x, y)) pressedButtonRect = settingsButtonRect;
            }
        } else {
            handleGameplayTap();
        }

        boolean onHome = gameState == GameState.HOME;
        boolean onPanel = gameState == GameState.PANEL_SLIDING;
        playBtnTarget = (pressedButtonRect == playButtonRect && onHome) ? 1f : 0f;
        scoreBtnTarget = (pressedButtonRect == scoreButtonRect) ? 1f : 0f;
        settingsBtnTarget = (pressedButtonRect == settingsButtonRect && onHome) ? 1f : 0f;
        okBtnTarget = (pressedButtonRect == playButtonRect && onPanel) ? 1f : 0f;
        gameOverSettingsBtnTarget = (pressedButtonRect == settingsButtonRect && onPanel) ? 1f : 0f;

        if (pressedButtonRect != null) invalidate();
    }

    private void handleGameplayTap() {
        if (gameState == GameState.WAITING) {
            gameState = GameState.PLAYING;
            updateWeatherSounds();
            if (!settingJetpackModeEnabled) flap();
        } else if (gameState == GameState.PLAYING) {
            if (!settingJetpackModeEnabled) flap();
        }
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
                    Toast.makeText(getContext(), "Leaderboard unavailable. Try again later.", Toast.LENGTH_SHORT).show();
                } else if (button == settingsButtonRect) {
                    openSettings();
                }
            } else if (gameState == GameState.PANEL_SLIDING) {
                playSound(soundSwooshing);
                if (button == playButtonRect) {
                    isRestarting = true;
                } else if (button == settingsButtonRect) {
                    openSettings();
                }
            }
        }
        cancelButtonPress();
    }

    private void cancelButtonPress() {
        playBtnTarget = 0f;
        scoreBtnTarget = 0f;
        settingsBtnTarget = 0f;
        okBtnTarget = 0f;
        gameOverSettingsBtnTarget = 0f;
        if (pressedButtonRect != null) {
            pressedButtonRect = null;
            invalidate();
        }
    }

    private void openSettings() {
        getContext().startActivity(new Intent(getContext(), SettingsActivity.class));
    }

    private void flap() {
        birdVelocityY = flapVelocityPerSec;
        playSound(soundWing);
        if (settingHapticFeedbackEnabled) {
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
        boolean inRound = gameState == GameState.PLAYING || gameState == GameState.WAITING;
        boolean audible = settingSoundEnabled && hostResumed && inRound;
        boolean rain = audible && (settingWeatherEffect == WEATHER_RAIN || settingWeatherEffect == WEATHER_STORM);
        boolean storm = audible && settingWeatherEffect == WEATHER_STORM;
        try {
            setLoopPlaying(rainSoundPlayer, rain);
            setLoopPlaying(stormSoundPlayer, storm);
        } catch (IllegalStateException e) {
            Log.e(TAG, "MediaPlayer state error in updateWeatherSounds", e);
        }
    }

    private void stopWeatherSounds() {
        try {
            setLoopPlaying(rainSoundPlayer, false);
            setLoopPlaying(stormSoundPlayer, false);
        } catch (IllegalStateException e) {
            Log.e(TAG, "MediaPlayer state error in stopWeatherSounds", e);
        }
    }

    private void playSound(final int soundId) {
        final SoundPool pool = soundPool;
        final ExecutorService executor = soundExecutor;
        if (!settingSoundEnabled || !hostResumed || soundId == 0) return;
        if (pool == null || executor == null || executor.isShutdown()) return;
        executor.execute(() -> pool.play(soundId, 1, 1, 0, 0, 1));
    }

    public void resume() {
        hostResumed = true;
        if (isRunning || !isReady) return;

        loadSettings();
        if (gameState == GameState.HOME || gameState == GameState.PANEL_SLIDING) {
            resetGame();
            gameState = GameState.HOME;
            transitionAlpha = 0;
            isFadingOut = false;
        } else {
            updateWeatherSounds();
        }
        startLoop();
    }

    public void pause() {
        hostResumed = false;
        isScreenPressed = false;
        cancelButtonPress();
        stopLoop();
        removeCallbacks(playDieSound);
        stopWeatherSounds();
    }

    private void startLoop() {
        if (isRunning) return;
        isRunning = true;
        lastFrameTimeNanos = 0;
        choreographer.postFrameCallback(this);
    }

    private void stopLoop() {
        isRunning = false;
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
        update(Math.min(deltaTime, MAX_FRAME_DELTA_SEC));
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
    private static final float BASE_MARGIN_PERCENT = 0.08f;
    private static final float FAR_OFF_SCREEN = 20000f;

    private static final Random random = new Random();
    private static float pipeHeadWidth, pipeHeadHeight;
    private static float pipeBodyWidth, pipeBodyOffsetX;

    float x;
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
