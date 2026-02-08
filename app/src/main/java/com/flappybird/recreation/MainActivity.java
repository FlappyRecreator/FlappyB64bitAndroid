package com.flappybird.recreation;

import android.animation.TimeInterpolator;
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
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import android.widget.FrameLayout;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.flappybird.recreation.SettingsActivity.*;

public class MainActivity extends AppCompatActivity {

    private GameView gameView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        EdgeToEdge.enable(this);
        super.onCreate(savedInstanceState);
        
        WindowInsetsControllerCompat windowInsetsController =
                WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
        windowInsetsController.setSystemBarsBehavior(
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        );
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars());

        setContentView(R.layout.main);
        FrameLayout gameContainer = findViewById(R.id.game_container);
        gameView = new GameView(this);
        gameContainer.addView(gameView);
    }

    @Override
    protected void onResume() {
        super.onResume();
        WindowInsetsControllerCompat windowInsetsController =
                WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars());
        if (gameView != null) gameView.resume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (gameView != null) gameView.pause();
    }
}

class GameView extends View implements Choreographer.FrameCallback {

    // --- MEDAL ADJUSTMENTS (Change these if position is off) ---
    // Negative X = Left, Positive X = Right
    // Negative Y = Up,   Positive Y = Down
    // These values are multiplied by the screen scale, so they remain consistent across devices.
    private final float PLATINUM_OFFSET_X = -1.0f;
    private final float PLATINUM_OFFSET_Y = 1.0f;
    
    private final float GOLD_OFFSET_X = -1.0f;
    private final float GOLD_OFFSET_Y = 1.0f;

    private enum GameState { HOME, TRANSITION_TO_WAITING, WAITING, PLAYING, GAME_OVER, PANEL_SLIDING }
    private GameState gameState = GameState.HOME;
    private Choreographer choreographer;
    private boolean isRunning = false;
    private boolean isReady = false;
    private long lastFrameTimeNanos = 0;
    private int screenWidth, screenHeight;
    private float scale;
    private int systemBarTop = -1, systemBarBottom = -1;

    // Graphics & Object Pooling
    private Paint pixelPaint, birdPaint;
    private Matrix birdMatrix = new Matrix();
    private Matrix gradientMatrix = new Matrix();

    private Bitmap unscaledBgDay, unscaledBgNight, unscaledLand;
    private Bitmap unscaledPipeGreen;
    private Bitmap[][] unscaledAllBirdBitmaps = new Bitmap[3][3];
    private Bitmap unscaledTextReady, unscaledTextGameOver, unscaledScorePanel, unscaledButtonPlay, unscaledButtonScore, unscaledTitle, unscaledCopyright, unscaledButtonSettings;
    private Bitmap[] unscaledMedalsBitmaps = new Bitmap[4];
    private Bitmap[] unscaledNumberBitmaps = new Bitmap[10];
    
    private Bitmap bgDayBitmap, bgNightBitmap, groundBitmap;
    private Bitmap pipeUpBitmap, pipeDownBitmap;
    private Bitmap backgroundBitmap, currentPipeTopBitmap, currentPipeBottomBitmap;
    private Bitmap[][] scaledAllBirdBitmaps = new Bitmap[3][3];
    private Bitmap[] birdBitmaps = new Bitmap[3];
    private Bitmap getReadyBitmap, gameOverBitmap, scorePanelBitmap, playButtonBitmap, scoreButtonBitmap, titleBitmap, copyrightBitmap, settingsButtonBitmap;
    private Bitmap[] medalBitmaps = new Bitmap[4];
    private Bitmap currentMedalBitmap;
    private Bitmap[] numberBitmaps = new Bitmap[10];
    private Bitmap[] smallNumberBitmaps = new Bitmap[10];

    // Physics variables
    private int birdFrame = 0;
    private float birdX, birdY, birdVelocityY, birdRotation;
    private long lastFlapTimeMillis = 0;
    private Rect birdRect = new Rect();
    private Rect playButtonRect = new Rect(), scoreButtonRect = new Rect(), settingsButtonRect = new Rect();
    private int currentBirdColor;

    private List<Pipe> pipes;
    private int pipeSpacing, pipeGap;

    private float groundX = 0, backgroundX = 0;
    private int groundHeight;

    private int score = 0, highScore = 0;
    private SharedPreferences prefs;

    // Audio & Haptics
    private SoundPool soundPool;
    private int soundWing, soundPoint, soundHit, soundDie, soundSwooshing, soundThunder;
    private ExecutorService soundExecutor;
    private MediaPlayer rainSoundPlayer, stormSoundPlayer;
    private Vibrator vibrator;

    // Game Logic Triggers
    private boolean hasTier1Triggered = false, hasTier2Triggered = false;
    
    // Physics Constants
    private float BASE_GRAVITY_PER_SEC, BASE_FLAP_VELOCITY_PER_SEC, BASE_PIPE_SPEED_PER_SEC, JETPACK_THRUST_PER_SEC;
    private float BASE_ROTATION_DELAY_THRESHOLD_PER_SEC, BASE_ROTATION_DOWN_SPEED_PER_SEC;
    private float GRAVITY_PER_SEC, FLAP_VELOCITY_PER_SEC, PIPE_SPEED_PER_SEC;
    private float ROTATION_DELAY_THRESHOLD_PER_SEC, ROTATION_DOWN_SPEED_PER_SEC;
    private float TRANSITION_FADE_SPEED_PER_SEC;
    
    private Random random = new Random();
    
    // UI Constants
    private final float UI_MARGIN_HORIZONTAL_PERCENT = 0.025f;
    private final float HOME_BUTTON_GAP_PERCENT = 0.05f;
    private final float SETTINGS_BUTTON_SCALE_MULTIPLIER = 0.75f;
    private final float PIPE_GAP_BIRD_HEIGHT_MULTIPLIER = 2.2f;
    private final float SCORE_PANEL_NUMBER_SCALE_MULTIPLIER = 0.6f;
    private final int SCORE_PANEL_CURRENT_SCORE_Y_OFFSET = 55;
    private final int SCORE_PANEL_HIGH_SCORE_Y_OFFSET = 98;
    private final int SCORE_PANEL_MEDAL_X_OFFSET = 32;
    // UPDATED: Changed from 45 to 41 to fix "positioned too low"
    private final int SCORE_PANEL_MEDAL_Y_OFFSET = 43;
    private static final String PREF_KEY_WARNING_SHOWN = "hasSeenAspectRatioWarning";
    private final float BIRD_HITBOX_PADDING_X = 0.10f;
    private final float BIRD_HITBOX_PADDING_Y = 0.10f;
    private long FLAP_ANIMATION_TIMEOUT_MS = 480;

    // Animation & State Variables
    private float gameOverElementsY;
    private int gameOverElementsTargetY;
    private Rect pressedButtonRect = null;
    
    // Settings Button Animation
    private float settingsButtonScale = 1.0f;
    private float settingsButtonTargetScale = 1.0f;
    private float pressOffsetY = 0;
    
    private boolean pipesAreMoving = false;
    private boolean pipesAreStopping = false;
    private float pipeAnimationCounter = 0f;
    private float pipeAnimationSpeed = 0.03f;
    private float maxPipeMoveRange = 0f;
    private float currentPipeMoveRange = 0f;
    private long pipeMoveStartTime = 0;
    private final long PIPE_MOVE_DURATION_MS = 30_000;
    private final float PIPE_SPAWN_DELAY_SECONDS = 1.5f;
    private final long PANEL_SLIDE_DELAY_MS = 100;
    private final float PANEL_SLIDE_EASING_BASE = 0.95f;
    private final long GAMEOVER_ICON_SLIDE_DELAY_MS = 25;
    private final float GAMEOVER_ICON_EASING_BASE = 0.92f;
    
    private long birdHitGroundTime = 0;
    private float gameOverSettingsIconY = -1;
    private float gameOverSettingsIconTargetY;
    private boolean isGameOverIconAnimationDone = false;
    private long panelFinishedSlidingTime = 0;

    private Paint transitionPaint;
    private float transitionAlpha = 0;
    private boolean isFadingOut = false;
    private boolean isRestarting = false;

    // Flash Effect Logic
    private Paint flashPaint;
    private float flashAlpha = 0;

    private Paint darkenPaint;
    private float uiScaleCorrection = 1.0f;

    // Settings Cache
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

    private float rainbowHue = 0f;
    private Deque<TrailParticle> birdTrail;
    private Paint trailPaint;
    private static final int MAX_TRAIL_PARTICLES = 15;

    private int displayedScore = 0;
    private long lastScoreTickTime = 0;
    private final long SCORE_ANIMATION_INTERVAL_MS_FAST = 8;
    private final long SCORE_ANIMATION_INTERVAL_MS_SLOW = 45;
    
    // Allocations moved out of draw
    private RectF groundDestRect = new RectF();
    private RectF playButtonDestRect = new RectF();
    private RectF scoreButtonDestRect = new RectF();
    private RectF gameOverDestRect = new RectF();
    private RectF panelDestRect = new RectF();
    private RectF medalDestRect = new RectF();
    private RectF restartBtnDestRect = new RectF();
    private RectF scoreDigitDestRect = new RectF();
    private RectF centeredBitmapDestRect = new RectF();
    private RectF settingsButtonDestRect = new RectF();
    private RectF pipeDestRect = new RectF();

    private static final float TARGET_FPS = 120.0f;
    private boolean isScreenPressed = false;
    private float baseBirdX;
    private float drunkModePhase = 0f;
    
    // Weather System
    private WeatherSystem weatherSystem;
    private Paint nightBgPaint, goldenPipePaint, motionBlurPaint;
    private Shader goldenPipeShader;
    private float dayNightCycleProgress = 0f;
    private float shakeTimer = 0f;
    private float currentShakeMagnitude = 0f;
    private float goldenPipeHue = 0f;

    // --- BURNT BIRD & CINEMATIC VARIABLES ---
    private boolean isBirdBurnt = false;
    private float burntTimer = 0f;
    private boolean hasTriggeredStrikeForScore = false;
    private boolean hasTriggeredMegaStrike = false;
    private float pipeElectrificationTimer = 0f;
    
    private List<SmokeParticle> smokeParticles = new ArrayList<>();
    private List<SparkParticle> sparkParticles = new ArrayList<>();
    private Paint sparkPaint;
    private ColorFilter burntColorFilter;
    private Paint electricityPipePaint;

    // Cinematic Engine Variables
    private float timeDilation = 1.0f; // 1.0 = Normal, < 1.0 = Slow Motion
    private float cameraZoom = 1.0f;
    private float cinematicTimer = 0f;
    private int cinematicPhase = 0; // 0: None, 1: Zoom In, 2: Strike, 3: Zoom Out
    
    // --- MEDAL SHINE VARIABLES ---
    private Bitmap sparkleBitmap;
    private float sparkleTimer = 0f;
    private float sparkleXOffset = 0f;
    private float sparkleYOffset = 0f;
    private float sparkleScaleCurrent = 0f;
    private boolean isSparkleAnimating = false;
    private final float SPARKLE_DURATION = 0.6f; 
    private final float SPARKLE_INTERVAL = 2.0f; // Seconds between shines
    
    public GameView(Context context) {
        super(context);
        init(context);
    }

    private void init(Context context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        highScore = prefs.getInt("highScore", 0);

        loadSettings();

        pixelPaint = new Paint();
        pixelPaint.setFilterBitmap(false);
        pixelPaint.setAntiAlias(false);

        birdPaint = new Paint();
        birdPaint.setFilterBitmap(false);
        birdPaint.setAntiAlias(false);

        ViewCompat.setOnApplyWindowInsetsListener(this, (v, windowInsets) -> {
            Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            systemBarTop = insets.top;
            systemBarBottom = insets.bottom;
            tryInitializeGame();
            return WindowInsetsCompat.CONSUMED;
        });
        choreographer = Choreographer.getInstance();
        extractBitmapsFromAtlas();
        loadSounds();
        soundExecutor = Executors.newSingleThreadExecutor();
        vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);

        transitionPaint = new Paint();
        transitionPaint.setColor(Color.BLACK);
        transitionPaint.setAlpha(0);

        flashPaint = new Paint();
        flashPaint.setColor(Color.WHITE);
        flashPaint.setAlpha(0);

        darkenPaint = new Paint();
        darkenPaint.setColor(Color.BLACK);
        darkenPaint.setAlpha(0);

        nightBgPaint = new Paint();
        nightBgPaint.setFilterBitmap(false);
        nightBgPaint.setAntiAlias(false);

        goldenPipePaint = new Paint();
        goldenPipePaint.setFilterBitmap(false);
        goldenPipePaint.setAntiAlias(false);
        goldenPipePaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_ATOP));
        // Pre-create shader for golden pipe
        int[] colors = {Color.argb(0, 255, 215, 0), Color.argb(150, 255, 255, 100), Color.argb(0, 255, 215, 0)};
        float[] positions = {0, 0.5f, 1};
        goldenPipeShader = new LinearGradient(0, 0, 100, 0, colors, positions, Shader.TileMode.MIRROR);
        goldenPipePaint.setShader(goldenPipeShader);

        motionBlurPaint = new Paint();
        motionBlurPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OVER));

        birdTrail = new ArrayDeque<>(MAX_TRAIL_PARTICLES);
        trailPaint = new Paint();
        
        // --- SMOKE & SPARKS INIT ---
        sparkPaint = new Paint();
        sparkPaint.setStrokeWidth(4);
        sparkPaint.setStyle(Paint.Style.STROKE);
        sparkPaint.setStrokeCap(Paint.Cap.ROUND);
        sparkPaint.setAntiAlias(true);
        // Apply a blur to sparks to make them glow
        sparkPaint.setMaskFilter(new BlurMaskFilter(3, BlurMaskFilter.Blur.NORMAL));
        
        electricityPipePaint = new Paint();
        electricityPipePaint.setColor(Color.CYAN);
        electricityPipePaint.setStyle(Paint.Style.STROKE);
        electricityPipePaint.setStrokeWidth(3);
        electricityPipePaint.setAntiAlias(true);
        electricityPipePaint.setMaskFilter(new BlurMaskFilter(5, BlurMaskFilter.Blur.NORMAL));
        
        // Darken the bird for burnt effect
        burntColorFilter = new PorterDuffColorFilter(Color.rgb(40, 40, 40), PorterDuff.Mode.MULTIPLY);

        weatherSystem = new WeatherSystem();
        
        sparkleBitmap = createSparkleBitmap();
    }
    
    private Bitmap createSparkleBitmap() {
        int size = 64; // Base size
        Bitmap bmp = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bmp);
        Paint p = new Paint();
        p.setColor(Color.WHITE);
        p.setAntiAlias(true);
        
        // Draw a glow
        p.setMaskFilter(new BlurMaskFilter(size / 4f, BlurMaskFilter.Blur.NORMAL));
        c.drawCircle(size / 2f, size / 2f, size / 6f, p);
        
        // Draw the cross/star shape
        p.setMaskFilter(null);
        float cx = size / 2f;
        float cy = size / 2f;
        float halfLen = size / 2.5f;
        float thickness = size / 12f;
        
        // Horizontal bar
        c.drawRect(cx - halfLen, cy - thickness/2, cx + halfLen, cy + thickness/2, p);
        // Vertical bar
        c.drawRect(cx - thickness/2, cy - halfLen, cx + thickness/2, cy + halfLen, p);
        
        return bmp;
    }

    private void loadSettings() {
        if (prefs == null) return;
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
        settingMotionBlurEnabled = prefs.getBoolean(PREF_MOTION_BLUR_ENABLED, DEFAULT_MOTION_BLUR_ENABLED);
        settingMotionBlurStrength = prefs.getFloat(PREF_MOTION_BLUR_STRENGTH, DEFAULT_MOTION_BLUR_STRENGTH);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (w == 0 || h == 0) return;

        screenWidth = w;
        screenHeight = h;
        tryInitializeGame();
    }

    private void tryInitializeGame() {
        if (isReady || screenWidth == 0 || screenHeight == 0 || systemBarTop == -1) {
            return;
        }

        if (!prefs.getBoolean(PREF_KEY_WARNING_SHOWN, false)) {
            float aspectRatio = (float) screenHeight / screenWidth;
            final float minAspectRatioThreshold = 1.7f;
            if (aspectRatio < minAspectRatioThreshold) {
                showAspectRatioWarning();
            }
        }

        scale = (float) getPlayableHeight() / unscaledBgDay.getHeight();
        BASE_GRAVITY_PER_SEC = (0.10f * scale) * TARGET_FPS * TARGET_FPS;
        BASE_FLAP_VELOCITY_PER_SEC = (-3.2f * scale) * TARGET_FPS;
        BASE_PIPE_SPEED_PER_SEC = (1.1f * scale) * TARGET_FPS;
        JETPACK_THRUST_PER_SEC = BASE_GRAVITY_PER_SEC * 1.3f;
        BASE_ROTATION_DELAY_THRESHOLD_PER_SEC = (2.7f * scale) * TARGET_FPS;
        BASE_ROTATION_DOWN_SPEED_PER_SEC = (2.5f * TARGET_FPS);
        TRANSITION_FADE_SPEED_PER_SEC = 800.0f;
        pressOffsetY = 4 * scale;
        maxPipeMoveRange = (getPlayableHeight() - groundHeight) * 0.08f;

        scaleAllBitmaps();
        groundHeight = groundBitmap.getHeight();
        Pipe.initHitboxDimensions(scale);
        float margin = screenWidth * UI_MARGIN_HORIZONTAL_PERCENT;
        float availableWidth = screenWidth - (2 * margin);
        if (scorePanelBitmap != null && scorePanelBitmap.getWidth() > availableWidth) {
            uiScaleCorrection = availableWidth / scorePanelBitmap.getWidth();
        } else {
            uiScaleCorrection = 1.0f;
        }

        weatherSystem.init(screenWidth, screenHeight, scale);
        
        resetGame();
        gameState = GameState.HOME;

        isReady = true;
        resume();
    }

    private int getPlayableHeight() { return screenHeight - systemBarTop - systemBarBottom; }

    private void resetGame() {
        loadSettings();
        updateWeatherSounds();
        weatherSystem.setWeatherType(settingWeatherEffect);
        weatherSystem.setHeavyRain(false); // Reset heavy rain

        float speedVariation = 1.0f + (random.nextFloat() - 0.5f) * 2 * settingPipeSpeedVariation;
        GRAVITY_PER_SEC = BASE_GRAVITY_PER_SEC * settingGravity * (settingUpsideDownEnabled ? -1 : 1);
        FLAP_VELOCITY_PER_SEC = BASE_FLAP_VELOCITY_PER_SEC * settingJumpStrength * (settingUpsideDownEnabled ? -1 : 1);
        PIPE_SPEED_PER_SEC = BASE_PIPE_SPEED_PER_SEC * settingGameSpeed * speedVariation * (settingReversePipesEnabled ? -1 : 1);
        ROTATION_DELAY_THRESHOLD_PER_SEC = BASE_ROTATION_DELAY_THRESHOLD_PER_SEC * settingBirdHangDelay;
        ROTATION_DOWN_SPEED_PER_SEC = BASE_ROTATION_DOWN_SPEED_PER_SEC;
        final int UNSCALED_BIRD_HEIGHT_FOR_GAP = 48;
        pipeGap = (int) (UNSCALED_BIRD_HEIGHT_FOR_GAP * scale * PIPE_GAP_BIRD_HEIGHT_MULTIPLIER * settingPipeGap);

        final float VIRTUAL_SCREEN_WIDTH = unscaledBgDay.getWidth();
        final float VIRTUAL_SPACING_FACTOR = 0.68f;
        pipeSpacing = (int) (VIRTUAL_SCREEN_WIDTH * VIRTUAL_SPACING_FACTOR * scale * settingPipeSpacing);

        baseBirdX = settingReversePipesEnabled ? screenWidth * 2 / 3f : screenWidth / 3f;
        isRestarting = false;
        hasTier1Triggered = false;
        hasTier2Triggered = false;
        pipeMoveStartTime = 0;
        pipesAreMoving = false;
        pipesAreStopping = false;
        pipeAnimationCounter = 0f;
        currentPipeMoveRange = 0f;
        birdHitGroundTime = 0;
        gameOverSettingsIconY = -1;
        isGameOverIconAnimationDone = false;
        panelFinishedSlidingTime = 0;
        rainbowHue = 0f;
        drunkModePhase = 0f;
        dayNightCycleProgress = 0f;
        shakeTimer = 0f;
        flashAlpha = 0f; // Reset flash
        birdTrail.clear();
        
        // Reset Burnt State & Cinematic
        isBirdBurnt = false;
        burntTimer = 0f;
        pipeElectrificationTimer = 0f;
        hasTriggeredStrikeForScore = false;
        hasTriggeredMegaStrike = false;
        smokeParticles.clear();
        sparkParticles.clear();
        timeDilation = 1.0f;
        cameraZoom = 1.0f;
        cinematicPhase = 0;
        
        // Reset Shine
        sparkleTimer = 0f;
        isSparkleAnimating = false;

        if (!settingDynamicDayNightEnabled) {
            if (settingBackground == 0) backgroundBitmap = bgDayBitmap;
            else if (settingBackground == 1) backgroundBitmap = bgNightBitmap;
            else backgroundBitmap = random.nextBoolean() ? bgNightBitmap : bgDayBitmap;
        }

        currentPipeTopBitmap = pipeDownBitmap;
        currentPipeBottomBitmap = pipeUpBitmap;

        int birdColor;
        if (settingBirdColor >= 0 && settingBirdColor <= 2) birdColor = settingBirdColor;
        else birdColor = random.nextInt(3);
        this.currentBirdColor = birdColor;
        for (int i = 0; i < 3; i++) birdBitmaps[i] = scaledAllBirdBitmaps[birdColor][i];

        birdX = baseBirdX;
        birdY = systemBarTop + (getPlayableHeight() - groundHeight) / 2f;
        if (settingUpsideDownEnabled) {
            birdY = screenHeight - birdY;
        }
        birdVelocityY = 0; birdRotation = 0; birdFrame = 0;
        pipes = new ArrayList<>();
        int playableAreaHeight = getPlayableHeight() - groundHeight;
        float firstPipeX;
        if (settingReversePipesEnabled) {
            firstPipeX = -pipeUpBitmap.getWidth() - (Math.abs(PIPE_SPEED_PER_SEC) * PIPE_SPAWN_DELAY_SECONDS);
        } else {
            firstPipeX = screenWidth + (Math.abs(PIPE_SPEED_PER_SEC) * PIPE_SPAWN_DELAY_SECONDS);
        }

        final int NUM_PIPES = 5;
        for (int i = 0; i < NUM_PIPES; i++) {
            float pipeX = firstPipeX + (i * pipeSpacing * (settingReversePipesEnabled ? -1 : 1));
            Pipe pipe = new Pipe(pipeX, currentPipeBottomBitmap.getWidth(), currentPipeBottomBitmap.getHeight());
            pipe.resetHeight(pipeGap, playableAreaHeight, systemBarTop, settingPipeVariation);

            int colorToSet = settingPipeColor;
            if (settingRandomPipeColorsEnabled) {
                colorToSet = random.nextInt(9);
            }
            pipe.setColorFilter(createPipeColorFilter(colorToSet));

            if (settingGoldenPipeEnabled && random.nextInt(100) < settingGoldenPipeChance) {
                pipe.isGolden = true;
            }

            pipes.add(pipe);
        }

        score = 0;
        displayedScore = 0;
        currentMedalBitmap = null;
        if (darkenPaint != null) darkenPaint.setAlpha(0);
    }

    private ColorFilter createPipeColorFilter(int pipeColorSetting) {
        if(settingReversePipesEnabled && pipeColorSetting == 0) {
            return new PorterDuffColorFilter(0xFFD05050, PorterDuff.Mode.MULTIPLY);
        }
        switch (pipeColorSetting) {
            case 1: return new PorterDuffColorFilter(0xFFD05050, PorterDuff.Mode.MULTIPLY);
            case 2: return new PorterDuffColorFilter(0xFF6080E0, PorterDuff.Mode.MULTIPLY);
            case 3: return new PorterDuffColorFilter(0xFFE0E060, PorterDuff.Mode.MULTIPLY);
            case 4: return new PorterDuffColorFilter(0xFFFFFFFF, PorterDuff.Mode.SRC_ATOP);
            case 5: return new PorterDuffColorFilter(0xFFE879E8, PorterDuff.Mode.MULTIPLY);
            case 6: return new PorterDuffColorFilter(0xFF505050, PorterDuff.Mode.MULTIPLY);
            case 7: return new PorterDuffColorFilter(0xFF9040D0, PorterDuff.Mode.MULTIPLY);
            case 8: return new PorterDuffColorFilter(0xFFE89030, PorterDuff.Mode.MULTIPLY);
            default: return null;
        }
    }

    private void update(float deltaTime) {
        // --- Time Management ---
        float realDeltaTime = deltaTime; 
        float gameDeltaTime = deltaTime * timeDilation;

        // Flash decay logic (Real Time)
        if (flashAlpha > 0) {
            float decaySpeed = 1500f;
            flashAlpha -= decaySpeed * realDeltaTime;
            if (flashAlpha < 0) flashAlpha = 0;
        }

        // --- Cinematic Engine Update ---
        updateCinematicSequence(realDeltaTime);

        // Background Scrolling (Game Time)
        if (gameState != GameState.GAME_OVER && gameState != GameState.PANEL_SLIDING) {
            float effectivePipeSpeed = PIPE_SPEED_PER_SEC;
            float bgScrollSpeed = effectivePipeSpeed * settingBgScrollSpeed;
            float groundScrollSpeed = effectivePipeSpeed * settingGroundScrollSpeed;

            Bitmap bg = (backgroundBitmap != null) ? backgroundBitmap : bgDayBitmap;
            float bgWidth = bg.getWidth();

            backgroundX -= bgScrollSpeed * gameDeltaTime;
            if (bgScrollSpeed > 0 && backgroundX <= -bgWidth) {
                backgroundX += bgWidth;
            } else if (bgScrollSpeed < 0 && backgroundX >= bgWidth) {
                backgroundX -= bgWidth;
            }

            float groundWidth = groundBitmap.getWidth();
            groundX -= groundScrollSpeed * gameDeltaTime;
            if (groundScrollSpeed > 0 && groundX <= -groundWidth) {
                groundX += groundWidth;
            } else if (groundScrollSpeed < 0 && groundX >= groundWidth) {
                groundX -= groundWidth;
            }
        }
        
        // Settings Button Animation Logic (Real Time)
        if (settingsButtonScale != settingsButtonTargetScale) {
            float diff = settingsButtonTargetScale - settingsButtonScale;
            if (Math.abs(diff) < 0.01f) {
                settingsButtonScale = settingsButtonTargetScale;
            } else {
                settingsButtonScale += diff * (10f * realDeltaTime);
            }
        }

        boolean wingAnimation = settingWingAnimationEnabled && (settingInfiniteFlapEnabled || System.currentTimeMillis() - lastFlapTimeMillis < FLAP_ANIMATION_TIMEOUT_MS);
        if (gameState == GameState.PLAYING) {
            if (wingAnimation || (settingJetpackModeEnabled && isScreenPressed)) {
               long scaledTime = (long)(System.currentTimeMillis() * timeDilation); 
               birdFrame = (int) ((System.currentTimeMillis() / 75) % birdBitmaps.length);
            }
            else birdFrame = 1;
        } else if (gameState == GameState.HOME || gameState == GameState.TRANSITION_TO_WAITING || gameState == GameState.WAITING) {
            if (settingWingAnimationEnabled) birdFrame = (int) ((System.currentTimeMillis() / 150) % birdBitmaps.length);
            else birdFrame = 1;
        }

        if (settingRainbowBirdEnabled) {
            rainbowHue = (rainbowHue + 150 * gameDeltaTime) % 360;
        }

        goldenPipeHue = (goldenPipeHue + 200 * gameDeltaTime);

        if (settingBirdTrailEnabled && (gameState == GameState.PLAYING || gameState == GameState.GAME_OVER)) {
            if (birdTrail.size() >= MAX_TRAIL_PARTICLES) {
                birdTrail.pollFirst();
            }
            birdTrail.add(new TrailParticle(birdX, birdY, birdRotation, currentBirdColor, birdFrame, settingRainbowBirdEnabled ? rainbowHue : -1));
        }
        for (TrailParticle p : birdTrail) {
            p.update(gameDeltaTime);
        }

        if (shakeTimer > 0) {
            shakeTimer -= realDeltaTime; // Shake runs in real time
        }
        
        // --- PIPE ELECTRIFICATION ---
        if (pipeElectrificationTimer > 0) {
            pipeElectrificationTimer -= gameDeltaTime;
            if (pipeElectrificationTimer < 0) pipeElectrificationTimer = 0;
        }
        
        // --- APOCALYPSE MODE CHECK ---
        boolean isApocalypse = (score >= 600 && settingWeatherEffect == 2);
        
        if (isApocalypse) {
            weatherSystem.setHeavyRain(true);
        }

        // --- UPDATE BURNT & SMOKE EFFECTS (Game Time) ---
        if (isBirdBurnt) {
            burntTimer -= gameDeltaTime;
            if (burntTimer <= 0) {
                isBirdBurnt = false;
                sparkParticles.clear();
            } else {
                // Add Smoke - Frequent
                if (random.nextInt(100) < 50) { 
                   smokeParticles.add(new SmokeParticle(birdX, birdY, scale));
                }
                
                // Add Sparks - Falling and Zapping
                if (random.nextInt(100) < 40) { // 40% chance per frame
                    // Type 0: Zap (Static relative to bird)
                    // Type 1: Fall (Gravity)
                    int type = random.nextBoolean() ? 0 : 1;
                    sparkParticles.add(new SparkParticle(birdX, birdY, scale, type));
                }
            }
        }
        
        // --- AMBIENT APOCALYPSE EFFECTS ---
        if (isApocalypse) {
            // Random Ambient Sparks falling from sky (matches bird spark visual)
            if (random.nextInt(100) < 30) {
                float sparkX = random.nextFloat() * screenWidth;
                float sparkY = -50;
                // Type 1 = Falling
                sparkParticles.add(new SparkParticle(sparkX, sparkY, scale, 1));
            }
        }

        // Update existing particles
        Iterator<SmokeParticle> smokeIter = smokeParticles.iterator();
        while (smokeIter.hasNext()) {
            SmokeParticle p = smokeIter.next();
            p.update(gameDeltaTime, PIPE_SPEED_PER_SEC * 0.5f); 
            if (p.isDead()) smokeIter.remove();
        }
        
        Iterator<SparkParticle> sparkIter = sparkParticles.iterator();
        while(sparkIter.hasNext()) {
            SparkParticle sp = sparkIter.next();
            // Move sparks relative to world (wind) if they are falling
            float wind = (sp.type == 1) ? PIPE_SPEED_PER_SEC * 0.8f : 0;
            sp.update(gameDeltaTime, wind);
            if (sp.isDead()) sparkIter.remove();
        }

        updateBirdRect();

        if (settingWeatherEffect > 0) {
            weatherSystem.update(gameDeltaTime, PIPE_SPEED_PER_SEC * 0.1f, birdVelocityY, birdRect);
        }

        if (settingDynamicDayNightEnabled && (gameState == GameState.PLAYING || gameState == GameState.WAITING)) {
            float cycleDuration = 120.0f;
            dayNightCycleProgress += gameDeltaTime / cycleDuration;
            if (dayNightCycleProgress > 1.0f) {
                dayNightCycleProgress -= 1.0f;
            }
        }

        switch (gameState) {
            case PLAYING:
                if (settingJetpackModeEnabled) {
                    if (isScreenPressed) {
                        float thrust = JETPACK_THRUST_PER_SEC * settingJumpStrength * (settingUpsideDownEnabled ? -1 : 1);
                        birdVelocityY -= thrust * gameDeltaTime;
                    }
                }

                birdVelocityY += GRAVITY_PER_SEC * gameDeltaTime;
                birdY += birdVelocityY * gameDeltaTime;
                
                if (!settingNoClipEnabled) {
                    float birdHalfHeight = (birdBitmaps[birdFrame].getHeight() * settingBirdSize) / 2f;
                    float ceilingThreshold = systemBarTop - birdHalfHeight - (birdHalfHeight * 0.8f); 
                    
                    if (settingUpsideDownEnabled) {
                        float upsideDownCeiling = screenHeight + birdHalfHeight + (birdHalfHeight * 0.8f);
                        if (birdY > upsideDownCeiling) {
                            birdY = upsideDownCeiling;
                            if (birdVelocityY > 0) birdVelocityY = 0;
                        }
                    } else {
                        if (birdY < ceilingThreshold) {
                            birdY = ceilingThreshold;
                            if (birdVelocityY < 0) birdVelocityY = 0;
                        }
                    }
                }

                if(settingDrunkModeEnabled) {
                    drunkModePhase += gameDeltaTime * 2.0f;
                    float maxDrift = screenWidth * 0.15f * settingDrunkModeStrength;
                    float drift = (float)Math.sin(drunkModePhase) * maxDrift;
                    birdX = baseBirdX + drift;
                }

                if (settingWeatherEffect == 2) {
                    // Increase lightning frequency in Apocalypse Mode (Score >= 600)
                    // 100 vs 300 -> 3x more frequent
                    int lightningChance = (score >= 600) ? 100 : 300;
                    if (random.nextInt(lightningChance) == 0) {
                        triggerLightning();
                    }
                }
                
                // --- DYNAMIC LIGHTNING STRIKE LOGIC ---
                if (settingWeatherEffect == 2) {
                    // Regular Strike Scores
                    if (score == 11 || score == 34 || score == 79 || score == 210) {
                        if (!hasTriggeredStrikeForScore) {
                            startCinematicStrikeSequence(false); // Normal single bolt
                            hasTriggeredStrikeForScore = true;
                        }
                    } else if (score == 350) {
                        // MEGA STRIKE
                        if (!hasTriggeredMegaStrike) {
                            startCinematicStrikeSequence(true); // Mega Multi-bolt
                            hasTriggeredMegaStrike = true;
                        }
                    } else {
                        hasTriggeredStrikeForScore = false;
                    }
                }

                final float ROTATION_UP_SPEED = 6.0f * TARGET_FPS;
                if (settingUpsideDownEnabled) {
                    if (birdVelocityY > 0) {
                        birdRotation = Math.min(25f, birdRotation + ROTATION_UP_SPEED * gameDeltaTime);
                    } else if (birdVelocityY < -ROTATION_DELAY_THRESHOLD_PER_SEC) {
                        birdRotation = Math.max(-90f, birdRotation - ROTATION_DOWN_SPEED_PER_SEC * gameDeltaTime);
                    }
                } else {
                    if (birdVelocityY < 0) {
                        birdRotation = Math.max(-25f, birdRotation - ROTATION_UP_SPEED * gameDeltaTime);
                    } else if (birdVelocityY > ROTATION_DELAY_THRESHOLD_PER_SEC) {
                        birdRotation = Math.min(90f, birdRotation + ROTATION_DOWN_SPEED_PER_SEC * gameDeltaTime);
                    }
                }
                break;
            case HOME:
                baseBirdX = screenWidth / 2f;
                birdX = baseBirdX;
                birdY = (systemBarTop + (getPlayableHeight() - groundHeight) / 2f) + (float) (Math.sin(System.currentTimeMillis() / 200.0) * 6 * scale);
                if (settingUpsideDownEnabled) {
                    birdY += getPlayableHeight() * 0.18f;
                    birdY = screenHeight - birdY;
                }
                birdRotation = 0;
                break;
            case TRANSITION_TO_WAITING:
                birdY = (systemBarTop + (getPlayableHeight() - groundHeight) / 2f) + (float) (Math.sin(System.currentTimeMillis() / 200.0) * 6 * scale);
                if (settingUpsideDownEnabled) birdY = screenHeight - birdY;
                birdRotation = 0;
                if (isFadingOut) {
                    transitionAlpha += TRANSITION_FADE_SPEED_PER_SEC * gameDeltaTime;
                    if (transitionAlpha >= 255) {
                        transitionAlpha = 255;
                        isFadingOut = false;
                        baseBirdX = settingReversePipesEnabled ? screenWidth * 2 / 3f : screenWidth / 3f;
                        birdX = baseBirdX;
                    }
                } else {
                    transitionAlpha -= TRANSITION_FADE_SPEED_PER_SEC * gameDeltaTime;
                    if (transitionAlpha <= 0) {
                        transitionAlpha = 0;
                        gameState = GameState.WAITING;
                    }
                }
                break;
            case WAITING:
                birdX = baseBirdX;
                birdY = (systemBarTop + (getPlayableHeight() - groundHeight) / 2f) + (float) (Math.sin(System.currentTimeMillis() / 200.0) * 6 * scale);
                if (settingUpsideDownEnabled) birdY = screenHeight - birdY;
                birdRotation = 0;
                break;
            case GAME_OVER:
                float groundTopY = screenHeight - systemBarBottom - groundHeight;
                boolean isOnGround;
                if(settingUpsideDownEnabled) {
                    isOnGround = birdY - (birdBitmaps[1].getHeight() * settingBirdSize / 2f) <= systemBarTop;
                } else {
                    isOnGround = birdY + (birdBitmaps[1].getHeight() * settingBirdSize / 2f) >= groundTopY;
                }

                if (!isOnGround) {
                    birdVelocityY += (GRAVITY_PER_SEC * 1.5f) * gameDeltaTime;
                    birdY += birdVelocityY * gameDeltaTime;
                    final float DEATH_ROTATION_SPEED = 2.8f * TARGET_FPS;
                    if(settingUpsideDownEnabled) {
                        if (birdRotation > -90) birdRotation -= DEATH_ROTATION_SPEED * gameDeltaTime;
                    } else {
                        if (birdRotation < 90) birdRotation += DEATH_ROTATION_SPEED * gameDeltaTime;
                    }
                    birdHitGroundTime = 0;
                } else {
                    if (settingUpsideDownEnabled) {
                        birdY = systemBarTop + (birdBitmaps[1].getHeight() * settingBirdSize / 2f);
                        birdRotation = -90f;
                    } else {
                        birdY = groundTopY - (birdBitmaps[1].getHeight() * settingBirdSize / 2f);
                        birdRotation = 90f;
                    }
                    birdFrame = 1;
                    if (birdHitGroundTime == 0) {
                        birdHitGroundTime = System.currentTimeMillis();
                        if(settingScreenShakeEnabled) triggerShake(5.0f, 0.4f);
                    }

                    if (System.currentTimeMillis() - birdHitGroundTime > PANEL_SLIDE_DELAY_MS) {
                        float scaledGameOverTextHeight = gameOverBitmap.getHeight() * uiScaleCorrection;
                        float scaledGap = (20 * scale) * uiScaleCorrection;
                        float scaledPanelHeight = scorePanelBitmap.getHeight() * uiScaleCorrection;
                        float totalUiBlockHeight = scaledGameOverTextHeight + scaledGap + scaledPanelHeight;
                        int playableAreaCenterY = systemBarTop + (getPlayableHeight() - groundHeight) / 2;
                        gameOverElementsTargetY = (int) (playableAreaCenterY - (totalUiBlockHeight / 2));
                        gameOverElementsY = screenHeight;
                        gameState = GameState.PANEL_SLIDING;
                        birdHitGroundTime = 0;
                    }
                }
                break;
            case PANEL_SLIDING:
                float deltaMultiplier = TARGET_FPS * realDeltaTime;
                if (gameOverElementsY > gameOverElementsTargetY) {
                    float portionToMove = 1.0f - (float)Math.pow(PANEL_SLIDE_EASING_BASE, deltaMultiplier);
                    gameOverElementsY -= (gameOverElementsY - gameOverElementsTargetY) * portionToMove;
                    if (gameOverElementsY - gameOverElementsTargetY < 1.0f) {
                        gameOverElementsY = gameOverElementsTargetY;
                    }
                } else {
                    gameOverElementsY = gameOverElementsTargetY;
                    
                    // --- MEDAL SHINE LOGIC ---
                    if (currentMedalBitmap != null && displayedScore == score) {
                        sparkleTimer += realDeltaTime;

                        if (!isSparkleAnimating) {
                            // Waiting to shine
                            if (sparkleTimer >= SPARKLE_INTERVAL) {
                                isSparkleAnimating = true;
                                sparkleTimer = 0;
                                // Pick a random spot on the medal (relative range 0.2 to 0.8)
                                sparkleXOffset = 0.2f + random.nextFloat() * 0.6f;
                                sparkleYOffset = 0.2f + random.nextFloat() * 0.6f;
                            }
                        } else {
                            // Currently shining (Scale up then down)
                            float progress = sparkleTimer / SPARKLE_DURATION;
                            if (progress >= 1.0f) {
                                isSparkleAnimating = false;
                                sparkleTimer = 0;
                                sparkleScaleCurrent = 0;
                            } else {
                                // Sine wave for smooth pop-in/pop-out
                                sparkleScaleCurrent = (float) Math.sin(progress * Math.PI);
                            }
                        }
                    }

                    if (lastScoreTickTime == 0) lastScoreTickTime = System.currentTimeMillis();

                    if (displayedScore < score) {
                        long currentTime = System.currentTimeMillis();
                        long scoreDiff = score - displayedScore;
                        long interval; int increment;
                        if (scoreDiff > 100) { interval = SCORE_ANIMATION_INTERVAL_MS_FAST;
                            increment = 11; }
                        else if (scoreDiff > 10) { interval = SCORE_ANIMATION_INTERVAL_MS_FAST;
                            increment = 3; }
                        else { interval = SCORE_ANIMATION_INTERVAL_MS_SLOW;
                            increment = 1; }
                        if (currentTime - lastScoreTickTime > interval) {
                            displayedScore += increment;
                            if (displayedScore > score) { displayedScore = score; }
                            lastScoreTickTime = currentTime;
                        }
                    }

                    if (panelFinishedSlidingTime == 0) {
                        panelFinishedSlidingTime = System.currentTimeMillis();
                        float margin = screenWidth * UI_MARGIN_HORIZONTAL_PERCENT;
                        gameOverSettingsIconTargetY = systemBarTop + margin;
                    }
                    if (!isGameOverIconAnimationDone && System.currentTimeMillis() - panelFinishedSlidingTime > GAMEOVER_ICON_SLIDE_DELAY_MS) {
                        if (gameOverSettingsIconY == -1) gameOverSettingsIconY = -settingsButtonBitmap.getHeight();
                        if (gameOverSettingsIconY < gameOverSettingsIconTargetY) {
                            float iconPortionToMove = 1.0f - (float)Math.pow(GAMEOVER_ICON_EASING_BASE, deltaMultiplier);
                            gameOverSettingsIconY += (gameOverSettingsIconTargetY - gameOverSettingsIconY) * iconPortionToMove;
                            if (gameOverSettingsIconTargetY - gameOverSettingsIconY < 1.0f) {
                                gameOverSettingsIconY = gameOverSettingsIconTargetY;
                                isGameOverIconAnimationDone = true;
                            }
                        } else {
                            gameOverSettingsIconY = gameOverSettingsIconTargetY;
                            isGameOverIconAnimationDone = true;
                        }
                    }
                }

                int maxAlphaFromSettings = (int) (settingGameOverOpacity / 100.0f * 255.0f);
                if (screenHeight > gameOverElementsTargetY && darkenPaint != null) {
                    float progress = 1.0f - ((gameOverElementsY - gameOverElementsTargetY) / (screenHeight - gameOverElementsTargetY));
                    progress = Math.max(0.0f, Math.min(1.0f, progress));
                    darkenPaint.setAlpha((int) (maxAlphaFromSettings * progress));
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
                break;
        }

        if (gameState == GameState.PLAYING) {
            if (settingMovingPipesEnabled) {
                if (score >= settingPipeMoveTier1 && !hasTier1Triggered) {
                    hasTier1Triggered = true;
                    pipesAreMoving = true; pipesAreStopping = false;
                    pipeAnimationSpeed = 0.03f; pipeMoveStartTime = System.currentTimeMillis();
                }
                if (score >= settingPipeMoveTier2 && !hasTier2Triggered) {
                    hasTier2Triggered = true;
                    pipesAreMoving = true; pipesAreStopping = false;
                    pipeAnimationSpeed = 0.06f; pipeMoveStartTime = System.currentTimeMillis();
                }
                if (pipeMoveStartTime != 0 && !pipesAreStopping && System.currentTimeMillis() - pipeMoveStartTime > PIPE_MOVE_DURATION_MS) {
                    pipesAreStopping = true;
                    pipeMoveStartTime = 0;
                }
                if (pipesAreStopping) {
                    boolean allPipesAtRest = true;
                    for (Pipe pipe : pipes) { if (!pipe.isAtRest()) { allPipesAtRest = false; break;
                    } }
                    if (allPipesAtRest) { pipesAreMoving = false;
                        pipesAreStopping = false; }
                }
                if (pipesAreMoving || pipesAreStopping) {
                    pipeAnimationCounter += (pipeAnimationSpeed * TARGET_FPS) * gameDeltaTime;
                    if (currentPipeMoveRange < maxPipeMoveRange && !pipesAreStopping) {
                        float moveRangeSpeed = (0.15f * scale * TARGET_FPS);
                        currentPipeMoveRange += moveRangeSpeed * gameDeltaTime;
                        currentPipeMoveRange = Math.min(currentPipeMoveRange, maxPipeMoveRange);
                    }
                }
            }
            int playableAreaHeight = getPlayableHeight() - groundHeight;
            for (Pipe pipe : pipes) {
                pipe.x -= PIPE_SPEED_PER_SEC * gameDeltaTime;
                if (settingMovingPipesEnabled) {
                    pipe.updateAnimation(pipesAreMoving, pipeAnimationCounter, currentPipeMoveRange, pipesAreStopping);
                }

                boolean scored;
                if (settingReversePipesEnabled) {
                    scored = !pipe.isScored && pipe.x > birdX;
                } else {
                    scored = !pipe.isScored && pipe.x + pipe.width < birdX;
                }
                if (scored) {
                    score += settingScoreMultiplier;
                    if(pipe.isGolden) {
                        score += settingGoldenPipeBonus;
                    }
                    playSound(soundPoint); pipe.isScored = true;
                }

                boolean recycle;
                if (settingReversePipesEnabled) {
                    recycle = pipe.x - (pipe.width * settingPipeWidth) > screenWidth;
                } else {
                    recycle = pipe.x + (pipe.width * settingPipeWidth) < 0;
                }

                if (recycle) {
                    pipe.x += (pipes.size() * pipeSpacing * (settingReversePipesEnabled ? -1 : 1));
                    pipe.resetHeight(pipeGap, playableAreaHeight, systemBarTop, settingPipeVariation);
                    pipe.isScored = false;
                    if (settingRandomPipeColorsEnabled) {
                        pipe.setColorFilter(createPipeColorFilter(random.nextInt(9)));
                    } else {
                        pipe.setColorFilter(createPipeColorFilter(settingPipeColor));
                    }

                    if (settingGoldenPipeEnabled && random.nextInt(100) < settingGoldenPipeChance) {
                        pipe.isGolden = true;
                    } else {
                        pipe.isGolden = false;
                    }
                }
            }
        }
        
        if (gameState == GameState.PLAYING) { checkCollisions(); }
    }
    
    // --- CINEMATIC ENGINE LOGIC ---
    
    private boolean isMegaStrike = false;
    
    private void startCinematicStrikeSequence(boolean mega) {
        cinematicPhase = 1; // Start Zoom In
        cinematicTimer = 0f;
        timeDilation = 1.0f;
        cameraZoom = 1.0f;
        isMegaStrike = mega;
    }
    
    private void updateCinematicSequence(float realDeltaTime) {
        if (cinematicPhase == 0) return;
        
        cinematicTimer += realDeltaTime;
        
        if (cinematicPhase == 1) { // PHASE 1: ZOOM IN & SLOW MO
            // Duration: 0.2s
            float progress = Math.min(1.0f, cinematicTimer / 0.2f);
            
            // Zoom to 1.5x (Normal) or 1.8x (Mega)
            float targetZoom = isMegaStrike ? 1.8f : 1.5f;
            cameraZoom = 1.0f + ((targetZoom - 1.0f) * progress);
            
            // Slow time to 0.1x (Normal) or 0.05x (Mega)
            float targetTime = isMegaStrike ? 0.05f : 0.1f;
            timeDilation = 1.0f - ((1.0f - targetTime) * progress);
            
            if (progress >= 1.0f) {
                cinematicPhase = 2;
                cinematicTimer = 0f;
                
                if (isMegaStrike) {
                    triggerMegaStrike();
                } else {
                    triggerDirectLightningOnBird(); // Normal Strike
                }
            }
        } 
        else if (cinematicPhase == 2) { // PHASE 2: HOLD THE IMPACT
            // Duration: 0.5s (Stay slow, Stay zoomed)
            timeDilation = isMegaStrike ? 0.05f : 0.1f;
            cameraZoom = isMegaStrike ? 1.8f : 1.5f;
            
            if (cinematicTimer >= (isMegaStrike ? 0.8f : 0.5f)) { // Hold longer for mega
                cinematicPhase = 3;
                cinematicTimer = 0f;
            }
        } 
        else if (cinematicPhase == 3) { // PHASE 3: ZOOM OUT & RESTORE SPEED
            // Duration: 0.3s
            float progress = Math.min(1.0f, cinematicTimer / 0.3f);
            
            float startZoom = isMegaStrike ? 1.8f : 1.5f;
            float startTime = isMegaStrike ? 0.05f : 0.1f;
            
            // Zoom back to 1.0
            cameraZoom = startZoom - ((startZoom - 1.0f) * progress);
            
            // Speed back to 1.0
            timeDilation = startTime + ((1.0f - startTime) * progress);
            
            if (progress >= 1.0f) {
                cinematicPhase = 0; // Done
                timeDilation = 1.0f;
                cameraZoom = 1.0f;
                isMegaStrike = false;
            }
        }
    }

    private void triggerLightning() {
        // Random background lightning
        weatherSystem.triggerLightning(screenWidth, getPlayableHeight() + systemBarTop);
        playSound(soundThunder);
        triggerThunderHaptic();
        if (settingScreenShakeEnabled) {
            triggerShake(7.0f, 0.5f); 
        }
    }
    
    private void triggerDirectLightningOnBird() {
        // Strike specifically on the bird
        weatherSystem.triggerTargetedLightning(birdX, birdY, screenWidth, getPlayableHeight() + systemBarTop);
        playSound(soundThunder);
        triggerThunderHaptic();
        if (settingScreenShakeEnabled) {
            triggerShake(15.0f, 0.8f); // Massive shake
        }
        
        // Ignite the bird (Standard Duration)
        isBirdBurnt = true;
        burntTimer = 10.0f; 
        
        // Trigger white flash for screen
        flashAlpha = 255;
    }
    
    private void triggerMegaStrike() {
        // Multiple bolts from multiple angles
        weatherSystem.triggerMultiDirectionalBolts(birdX, birdY, screenWidth, getPlayableHeight() + systemBarTop);
        
        playSound(soundThunder);
        // Play it twice for volume
        playSound(soundThunder); 
        
        triggerThunderHaptic();
        
        if (settingScreenShakeEnabled) {
            triggerShake(25.0f, 1.5f); // Extreme shake
        }
        
        // Ignite the bird (Long Duration)
        isBirdBurnt = true;
        burntTimer = 20.0f; 
        
        // Electrify Pipes
        pipeElectrificationTimer = 20.0f;
        
        // Trigger white flash for screen
        flashAlpha = 255;
    }

    private void checkCollisions() {
        if (settingNoClipEnabled) return;
        float birdHalfHeight = (birdBitmaps[birdFrame].getHeight() * settingBirdSize) / 2f;
        
        // Collision check with GROUND
        if (settingUpsideDownEnabled) {
            if (birdRect.bottom >= screenHeight - systemBarBottom - groundHeight) {
                gameOver();
                return;
            }
        } else {
            if (birdY + birdHalfHeight >= screenHeight - systemBarBottom - groundHeight) {
                gameOver();
                return;
            }
        }

        for (Pipe pipe : pipes) {
            if (Rect.intersects(birdRect, pipe.getTopHeadRect(settingPipeWidth)) ||
                    Rect.intersects(birdRect, pipe.getTopBodyRect(settingPipeWidth)) ||
                    Rect.intersects(birdRect, pipe.getBottomHeadRect(pipeGap, settingPipeWidth)) ||
                    Rect.intersects(birdRect, pipe.getBottomBodyRect(pipeGap, settingPipeWidth))) {

                gameOver(); return;
            }
        }
    }

    private void triggerShake(float magnitude, float duration) {
        if (!settingScreenShakeEnabled) return;
        this.currentShakeMagnitude = magnitude * scale * settingScreenShakeStrength;
        this.shakeTimer = duration;
    }

    private void triggerThunderHaptic() {
        if (settingHapticFeedbackEnabled && vibrator != null && vibrator.hasVibrator()) {
            long[] pattern = {0, 30, 40, 30, 40, 80, 20, 100};
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createWaveform(pattern, -1));
            } else {
                vibrator.vibrate(pattern, -1);
            }
        }
    }

    private void gameOver() {
        if (gameState == GameState.PLAYING) {
            gameState = GameState.GAME_OVER;
            
            // Reset Cinematic State instantly on death
            cinematicPhase = 0;
            cameraZoom = 1.0f;
            timeDilation = 1.0f;
            
            flashAlpha = 255f; // Trigger white flash
            stopWeatherSounds();
            playSound(soundHit);
            triggerShake(5.0f, 0.4f);
            postDelayed(() -> playSound(soundDie), 300);
            if (score > highScore) { highScore = score; prefs.edit().putInt("highScore", highScore).apply();
            }
            
            // UPDATED MEDAL LOGIC (Correct Mapping)
            // Platinum (40+): medals_0
            // Gold (30+): medals_1
            // Silver (20+): medals_2
            // Bronze (10+): medals_3
            if (score >= 40) currentMedalBitmap = medalBitmaps[0]; 
            else if (score >= 30) currentMedalBitmap = medalBitmaps[1];
            else if (score >= 20) currentMedalBitmap = medalBitmaps[2];
            else if (score >= 10) currentMedalBitmap = medalBitmaps[3];
            else currentMedalBitmap = null;

            displayedScore = 0;
            lastScoreTickTime = 0;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (canvas == null || !isReady) return;

        if (settingMotionBlurEnabled) {
            motionBlurPaint.setAlpha((int) (255 / (Math.max(1f, settingMotionBlurStrength) * 1.5f)));
            canvas.drawPaint(motionBlurPaint);
        } else {
            canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        }

        canvas.save();

        // --- CAMERA TRANSFORM (Zoom & Shake) ---
        
        // 1. Apply Shake Offset
        if (shakeTimer > 0) {
            float magnitude = currentShakeMagnitude * (shakeTimer / 0.5f);
            float xOffset = (random.nextFloat() - 0.5f) * 2 * magnitude;
            float yOffset = (random.nextFloat() - 0.5f) * 2 * magnitude;
            canvas.translate(xOffset, yOffset);
        }
        
        // 2. Apply Cinematic Zoom (Centered on Bird)
        if (cameraZoom > 1.0f) {
            canvas.scale(cameraZoom, cameraZoom, birdX, birdY);
        }

        if (settingUpsideDownEnabled) {
            canvas.scale(1, -1, screenWidth / 2f, screenHeight / 2f);
        }

        // --- DRAWING GAME WORLD ---

        Bitmap bgToDraw;
        if(settingDynamicDayNightEnabled) {
            bgToDraw = bgDayBitmap;
        } else {
            bgToDraw = (backgroundBitmap != null) ? backgroundBitmap : bgDayBitmap;
        }

        float bgDrawWidth = bgToDraw.getWidth();
        float startX = backgroundX % bgDrawWidth;
        if (startX > 0) {
            startX -= bgDrawWidth;
        }
        for (float x = startX; x < screenWidth; x += bgDrawWidth) {
            canvas.drawBitmap(bgToDraw, x, 0, pixelPaint);
        }

        if (settingDynamicDayNightEnabled) {
            float sineProgress = (float) (1 - Math.cos(dayNightCycleProgress * 2 * Math.PI)) / 2;
            int alpha = (int) (sineProgress * 255);
            nightBgPaint.setAlpha(alpha);

            for (float x = startX; x < screenWidth; x += bgDrawWidth) {
                canvas.drawBitmap(bgNightBitmap, x, 0, nightBgPaint);
            }
        }

        // Draw Background Parallax Weather (Depth Layer)
        if (settingWeatherEffect > 0) {
             weatherSystem.drawBack(canvas);
        }

        // Draw Pipes
        if (gameState != GameState.HOME && !(gameState == GameState.TRANSITION_TO_WAITING && isFadingOut)) {
            for (Pipe pipe : pipes) {
                float visualPipeWidth = pipe.width * settingPipeWidth;
                float xPos = pipe.x - (visualPipeWidth - pipe.width) / 2f;

                pipeDestRect.set(xPos, pipe.getTopPipeY(), xPos + visualPipeWidth, pipe.getTopPipeY() + pipe.height);
                canvas.drawBitmap(currentPipeTopBitmap, null, pipeDestRect, pipe.pipePaint);
                
                // Draw Electricity on top pipe if active
                if (pipeElectrificationTimer > 0) {
                     drawPipeElectricity(canvas, xPos, pipe.getTopPipeY(), visualPipeWidth, pipe.height);
                }

                pipeDestRect.set(xPos, pipe.getBottomPipeY(pipeGap), xPos + visualPipeWidth, pipe.getBottomPipeY(pipeGap) + pipe.height);
                canvas.drawBitmap(currentPipeBottomBitmap, null, pipeDestRect, pipe.pipePaint);

                // Draw Electricity on bottom pipe if active
                if (pipeElectrificationTimer > 0) {
                     drawPipeElectricity(canvas, xPos, pipe.getBottomPipeY(pipeGap), visualPipeWidth, pipe.height);
                }

                if (pipe.isGolden) {
                    gradientMatrix.setTranslate(goldenPipeHue, 0);
                    goldenPipeShader.setLocalMatrix(gradientMatrix);

                    pipeDestRect.set(xPos, pipe.getTopPipeY(), xPos + visualPipeWidth, pipe.getTopPipeY() + pipe.height);
                    canvas.drawRect(pipeDestRect, goldenPipePaint);
                    pipeDestRect.set(xPos, pipe.getBottomPipeY(pipeGap), xPos + visualPipeWidth, pipe.getBottomPipeY(pipeGap) + pipe.height);
                    canvas.drawRect(pipeDestRect, goldenPipePaint);
                }
            }
        }
        
        float groundDrawWidth = groundBitmap.getWidth();
        float groundTopY = screenHeight - systemBarBottom - groundHeight;
        startX = groundX % groundDrawWidth;
        if (startX > 0) {
            startX -= groundDrawWidth;
        }
        for (float x = startX; x < screenWidth; x += groundDrawWidth) {
            groundDestRect.set(x, groundTopY, x + groundDrawWidth, screenHeight);
            canvas.drawBitmap(groundBitmap, null, groundDestRect, pixelPaint);
        }

        // Draw Smoke Particles (Behind bird)
        for (SmokeParticle p : smokeParticles) {
            p.draw(canvas);
        }

        drawBirdTrail(canvas);

        birdPaint.setColorFilter(null);
        if(settingRainbowBirdEnabled) {
            ColorMatrix colorMatrix = new ColorMatrix();
            colorMatrix.setRotate(0, rainbowHue);
            colorMatrix.setRotate(1, rainbowHue);
            colorMatrix.setRotate(2, rainbowHue);
            birdPaint.setColorFilter(new ColorMatrixColorFilter(colorMatrix));
        }
        
        // Apply Burnt Filter if active
        if (isBirdBurnt) {
            birdPaint.setColorFilter(burntColorFilter);
        }
        
        if (settingGhostModeEnabled) {
            birdPaint.setAlpha(128);
        }

        Bitmap currentBirdBitmap = birdBitmaps[birdFrame];
        birdMatrix.reset();
        birdMatrix.postTranslate(-currentBirdBitmap.getWidth() / 2f, -currentBirdBitmap.getHeight() / 2f);
        birdMatrix.postRotate(birdRotation);
        birdMatrix.postScale(settingBirdSize, settingBirdSize, 0, 0);
        birdMatrix.postTranslate(birdX, birdY);
        canvas.drawBitmap(currentBirdBitmap, birdMatrix, birdPaint);
        birdPaint.setAlpha(255);
        birdPaint.setColorFilter(null); // Reset filter

        // Draw Sparks (Over bird)
        for (SparkParticle sp : sparkParticles) {
            sp.draw(canvas, sparkPaint);
        }

        // Draw Foreground Weather and Lighting
        if (settingWeatherEffect > 0) {
             weatherSystem.drawFront(canvas);
             weatherSystem.drawLightingOverlay(canvas, screenWidth, screenHeight);
        }

        // --- END CAMERA TRANSFORM ---
        canvas.restore();
        
        // --- UI (DRAWN IN SCREEN SPACE - NO ZOOM) ---

        if (gameState == GameState.HOME) {
            drawHomeScreen(canvas);
        } else if (gameState == GameState.TRANSITION_TO_WAITING) {
            if (isFadingOut) drawHomeScreen(canvas);
            else drawCenteredBitmap(canvas, getReadyBitmap, -(int) (getPlayableHeight() * 0.15));
        } else if (gameState == GameState.WAITING) {
            drawCenteredBitmap(canvas, getReadyBitmap, -(int) (getPlayableHeight() * 0.15));
        } else if (gameState == GameState.PLAYING) {
            drawScoreWithImages(canvas, score, screenWidth / 2, systemBarTop + (int) (getPlayableHeight() * 0.15));
        } else if (gameState == GameState.PANEL_SLIDING || gameState == GameState.GAME_OVER) {
            if (darkenPaint.getAlpha() > 0) {
                canvas.drawRect(0, 0, screenWidth, screenHeight, darkenPaint);
            }
            drawGameOverScreen(canvas);
        }

        if (transitionAlpha > 0) {
            transitionPaint.setAlpha((int) transitionAlpha);
            canvas.drawRect(0, 0, screenWidth, screenHeight, transitionPaint);
        }
        
        // Draw White Flash on top of everything
        if (flashAlpha > 0) {
            flashPaint.setAlpha((int) flashAlpha);
            canvas.drawRect(0, 0, screenWidth, screenHeight, flashPaint);
        }
    }
    
    private void drawPipeElectricity(Canvas canvas, float x, float y, float w, float h) {
        Path path = new Path();
        float currentY = y;
        float endY = y + h;
        
        // Draw jagged lines running up/down the pipe
        for(int i=0; i<3; i++) { // 3 Bolts per pipe segment
            path.reset();
            float startX = x + (random.nextFloat() * w);
            path.moveTo(startX, currentY);
            
            float cy = currentY;
            float cx = startX;
            
            while(cy < endY) {
                 cy += random.nextInt(40) + 10;
                 cx += (random.nextInt(30) - 15);
                 // Constrain to pipe width
                 if(cx < x) cx = x;
                 if(cx > x + w) cx = x + w;
                 path.lineTo(cx, cy);
            }
            canvas.drawPath(path, electricityPipePaint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int touchX = (int) event.getX();
        int touchY = (int) event.getY();
        if (settingUpsideDownEnabled) {
            touchY = screenHeight - touchY;
        }

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                isScreenPressed = true;
                pressedButtonRect = null;
                settingsButtonTargetScale = 1.0f; // Default reset
                
                if (gameState == GameState.HOME) {
                    if (playButtonRect.contains(touchX, touchY)) pressedButtonRect = playButtonRect;
                    else if (scoreButtonRect.contains(touchX, touchY)) pressedButtonRect = scoreButtonRect;
                    else if (settingsButtonRect.contains(touchX, touchY)) {
                        pressedButtonRect = settingsButtonRect;
                        settingsButtonTargetScale = 0.85f; // Shrink on press
                    }
                } else if (gameState == GameState.WAITING) {
                    gameState = GameState.PLAYING;
                    updateWeatherSounds();
                    if (!settingJetpackModeEnabled) flap();
                } else if (gameState == GameState.PLAYING) {
                    if (!settingJetpackModeEnabled) flap();
                } else if (gameState == GameState.PANEL_SLIDING && gameOverElementsY == gameOverElementsTargetY && !isRestarting) {
                    if (displayedScore == score && playButtonRect.contains(touchX, touchY)) {
                        pressedButtonRect = playButtonRect;
                    }
                    if (isGameOverIconAnimationDone && settingsButtonRect.contains(touchX, touchY)) {
                        pressedButtonRect = settingsButtonRect;
                        settingsButtonTargetScale = 0.85f; // Shrink on press
                    }
                }
                if (pressedButtonRect != null) invalidate();
                break;
            case MotionEvent.ACTION_UP:
                isScreenPressed = false;
                settingsButtonTargetScale = 1.0f; // Bounce back
                
                if (pressedButtonRect != null && pressedButtonRect.contains(touchX, touchY)) {
                    if (gameState == GameState.HOME) {
                        if (pressedButtonRect == playButtonRect) {
                            gameState = GameState.TRANSITION_TO_WAITING; isFadingOut = true; playSound(soundSwooshing);
                        } else if (pressedButtonRect == settingsButtonRect) {
                            playSound(soundSwooshing);
                            Intent intent = new Intent(getContext(), SettingsActivity.class);
                            getContext().startActivity(intent);
                        }
                    } else if (gameState == GameState.PANEL_SLIDING) {
                        if (pressedButtonRect == playButtonRect) {
                            isRestarting = true;
                            playSound(soundSwooshing);
                        } else if (pressedButtonRect == settingsButtonRect) {
                            playSound(soundSwooshing);
                            Intent intent = new Intent(getContext(), SettingsActivity.class);
                            getContext().startActivity(intent);
                        }
                    }
                }
                if (pressedButtonRect != null) { pressedButtonRect = null; invalidate(); }
                break;
            case MotionEvent.ACTION_MOVE:
                if (pressedButtonRect != null && !pressedButtonRect.contains(touchX, touchY)) { 
                    pressedButtonRect = null;
                    settingsButtonTargetScale = 1.0f;
                    invalidate(); 
                }
                break;
            case MotionEvent.ACTION_CANCEL:
                isScreenPressed = false;
                settingsButtonTargetScale = 1.0f;
                if (pressedButtonRect != null) { pressedButtonRect = null; invalidate(); }
                break;
        }
        return true;
    }
    private void flap() {
        birdVelocityY = FLAP_VELOCITY_PER_SEC;
        playSound(soundWing);
        if (settingHapticFeedbackEnabled) {
            this.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
        }
        lastFlapTimeMillis = System.currentTimeMillis();
    }
    private void updateBirdRect() {
        Bitmap currentBirdBitmap = birdBitmaps[birdFrame];
        float baseHitboxWidth = currentBirdBitmap.getWidth() * (1.0f - 2.0f * BIRD_HITBOX_PADDING_X);
        float baseHitboxHeight = currentBirdBitmap.getHeight() * (1.0f - 2.0f * BIRD_HITBOX_PADDING_Y);
        float hitboxMultiplier = Math.max(0.0f, settingBirdHitbox);
        float finalHitboxWidth = baseHitboxWidth * hitboxMultiplier * settingBirdSize;
        float finalHitboxHeight = baseHitboxHeight * hitboxMultiplier * settingBirdSize;
        float left = birdX - finalHitboxWidth / 2f;
        float top = birdY - finalHitboxHeight / 2f;
        float right = birdX + finalHitboxWidth / 2f;
        float bottom = birdY + finalHitboxHeight / 2f;
        birdRect.set((int) left, (int) top, (int) right, (int) bottom);
    }

    private void drawBirdTrail(Canvas canvas) {
        if (!settingBirdTrailEnabled || birdTrail.isEmpty()) return;

        int i = 0;
        for (TrailParticle particle : birdTrail) {
            float progress = (float) i / birdTrail.size();
            int alpha = (int) (progress * 100);

            trailPaint.setColorFilter(null);
            if (particle.rainbowHue != -1) {
                ColorMatrix colorMatrix = new ColorMatrix();
                colorMatrix.setRotate(0, particle.rainbowHue);
                colorMatrix.setRotate(1, particle.rainbowHue);
                colorMatrix.setRotate(2, particle.rainbowHue);
                trailPaint.setColorFilter(new ColorMatrixColorFilter(colorMatrix));
            }
            trailPaint.setAlpha(alpha);

            Bitmap trailBitmap = scaledAllBirdBitmaps[particle.colorIndex][particle.frameIndex];
            birdMatrix.reset();
            birdMatrix.postTranslate(-trailBitmap.getWidth() / 2f, -trailBitmap.getHeight() / 2f);
            birdMatrix.postRotate(particle.rotation);
            birdMatrix.postScale(settingBirdSize, settingBirdSize, 0, 0);
            birdMatrix.postTranslate(particle.x, particle.y);
            canvas.drawBitmap(trailBitmap, birdMatrix, trailPaint);
            i++;
        }
    }


    private void drawHomeScreen(Canvas canvas) {
        float titleX = (screenWidth - titleBitmap.getWidth()) / 2f;
        float titleY = getPlayableHeight() * 0.22f + systemBarTop;
        canvas.drawBitmap(titleBitmap, titleX, titleY, pixelPaint);

        float margin = screenWidth * UI_MARGIN_HORIZONTAL_PERCENT;
        if (settingsButtonBitmap != null) {
            float settingsBtnX = screenWidth - settingsButtonBitmap.getWidth() - margin;
            float settingsBtnY = systemBarTop + margin;
            
            // Update rect for hit detection
            settingsButtonRect.set((int) settingsBtnX, (int) settingsBtnY, 
                (int) (settingsBtnX + settingsButtonBitmap.getWidth()), 
                (int) (settingsBtnY + settingsButtonBitmap.getHeight()));
                
            if (!settingHideSettingsIcon) {
                // Scale Animation
                float centerX = settingsButtonRect.centerX();
                float centerY = settingsButtonRect.centerY();
                canvas.save();
                canvas.scale(settingsButtonScale, settingsButtonScale, centerX, centerY);
                
                // Tint when pressed
                if (pressedButtonRect == settingsButtonRect) {
                    pixelPaint.setColorFilter(new PorterDuffColorFilter(Color.GRAY, PorterDuff.Mode.MULTIPLY));
                } else {
                    pixelPaint.setColorFilter(null);
                }
                
                canvas.drawBitmap(settingsButtonBitmap, settingsBtnX, settingsBtnY, pixelPaint);
                canvas.restore();
                
                // Reset paint
                pixelPaint.setColorFilter(null);
            }
        }

        float availableWidth = screenWidth - (2 * margin);
        float originalPlayWidth = playButtonBitmap.getWidth();
        float originalScoreWidth = scoreButtonBitmap.getWidth();
        float originalGap = screenWidth * HOME_BUTTON_GAP_PERCENT;
        float totalIdealWidth = originalPlayWidth + originalScoreWidth + originalGap;

        float scaleCorrection = 1.0f;
        if (totalIdealWidth > availableWidth) scaleCorrection = availableWidth / totalIdealWidth;

        float newPlayWidth = originalPlayWidth * scaleCorrection;
        float newPlayHeight = playButtonBitmap.getHeight() * scaleCorrection;
        float newScoreWidth = originalScoreWidth * scaleCorrection;
        float newScoreHeight = scoreButtonBitmap.getHeight() * scaleCorrection;
        float newGap = originalGap * scaleCorrection;
        float totalNewWidth = newPlayWidth + newScoreWidth + newGap;
        float playBtnX = (screenWidth - totalNewWidth) / 2f;
        float scoreBtnX = playBtnX + newPlayWidth + newGap;
        float buttonsY = titleY + titleBitmap.getHeight() + getPlayableHeight() * 0.18f;
        playButtonRect.set((int) playBtnX, (int) buttonsY, (int) (playBtnX + newPlayWidth), (int) (buttonsY + newPlayHeight));
        scoreButtonRect.set((int) scoreBtnX, (int) buttonsY, (int) (scoreBtnX + newScoreWidth), (int) (buttonsY + newScoreHeight));

        playButtonDestRect.set(playButtonRect);
        scoreButtonDestRect.set(scoreButtonRect);
        float scaledPressOffsetY = pressOffsetY * scaleCorrection;
        if (pressedButtonRect == playButtonRect) playButtonDestRect.offset(0, scaledPressOffsetY);
        if (pressedButtonRect == scoreButtonRect) scoreButtonDestRect.offset(0, scaledPressOffsetY);

        canvas.drawBitmap(playButtonBitmap, null, playButtonDestRect, pixelPaint);
        canvas.drawBitmap(scoreButtonBitmap, null, scoreButtonDestRect, pixelPaint);
        float copyrightX = (screenWidth - copyrightBitmap.getWidth()) / 2f;
        float groundLineY = screenHeight - systemBarBottom - groundHeight;
        float copyrightY = groundLineY + (groundHeight - copyrightBitmap.getHeight()) / 2f;
        canvas.drawBitmap(copyrightBitmap, copyrightX, copyrightY, pixelPaint);
    }

    private void drawGameOverScreen(Canvas canvas) {
        if (gameState == GameState.PANEL_SLIDING || (gameState == GameState.GAME_OVER && birdHitGroundTime > 0)) {
            if (gameState != GameState.PANEL_SLIDING) return;
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
            
            if (currentMedalBitmap != null && displayedScore == score) {
                float medalWidth = currentMedalBitmap.getWidth() * scaleCorrection;
                float medalHeight = currentMedalBitmap.getHeight() * scaleCorrection;
                float medalX = panelX + (SCORE_PANEL_MEDAL_X_OFFSET * scale * scaleCorrection);
                float medalY = panelY + (SCORE_PANEL_MEDAL_Y_OFFSET * scale * scaleCorrection);
                medalDestRect.set(medalX, medalY, medalX + medalWidth, medalY + medalHeight);
                
                // --- APPLY MEDAL OFFSETS (Fix for Platinum/Gold positioning) ---
                float xOffset = 0;
                float yOffset = 0;

                if (currentMedalBitmap == medalBitmaps[0]) { // Platinum
                    xOffset = PLATINUM_OFFSET_X * scale * scaleCorrection;
                    yOffset = PLATINUM_OFFSET_Y * scale * scaleCorrection;
                } else if (currentMedalBitmap == medalBitmaps[1]) { // Gold
                    xOffset = GOLD_OFFSET_X * scale * scaleCorrection;
                    yOffset = GOLD_OFFSET_Y * scale * scaleCorrection;
                }
                
                medalDestRect.offset(xOffset, yOffset);
                // Also update medalX/Y variables so the sparkle follows the offset
                medalX += xOffset;
                medalY += yOffset;

                canvas.drawBitmap(currentMedalBitmap, null, medalDestRect, pixelPaint);
                
                if (isSparkleAnimating && sparkleBitmap != null) {
                    float shineSize = 30 * scale * scaleCorrection * sparkleScaleCurrent; // 30 is base visual size
                    
                    // Calculate center position based on random offsets
                    float centerX = medalX + (medalWidth * sparkleXOffset);
                    float centerY = medalY + (medalHeight * sparkleYOffset);
                    
                    float left = centerX - (shineSize / 2f);
                    float top = centerY - (shineSize / 2f);
                    
                    RectF sparkleDest = new RectF(left, top, left + shineSize, top + shineSize);
                    
                    // Use a smooth paint for the glow effect
                    Paint smoothPaint = new Paint(); 
                    smoothPaint.setFilterBitmap(true);
                    
                    // Rotate the sparkle slightly over time for effect
                    canvas.save();
                    canvas.rotate(sparkleTimer * 100, centerX, centerY);
                    canvas.drawBitmap(sparkleBitmap, null, sparkleDest, smoothPaint);
                    canvas.restore();
                }
            }

            if (displayedScore == score) {
                float playButtonWidth = playButtonBitmap.getWidth() * scaleCorrection;
                float playButtonHeight = playButtonBitmap.getHeight() * scaleCorrection;
                float btnX = (screenWidth - playButtonWidth) / 2f;
                float btnY = panelY + panelHeight + (15 * scale * scaleCorrection);
                playButtonRect.set((int) btnX, (int) btnY, (int) (btnX + playButtonWidth), (int) (btnY + playButtonHeight));

                restartBtnDestRect.set(playButtonRect);
                if (pressedButtonRect == playButtonRect) {
                    restartBtnDestRect.offset(0, pressOffsetY * scaleCorrection);
                }
                canvas.drawBitmap(playButtonBitmap, null, restartBtnDestRect, pixelPaint);
            }

            if (gameOverSettingsIconY != -1 && settingsButtonBitmap != null) {
                float margin = screenWidth * UI_MARGIN_HORIZONTAL_PERCENT;
                float settingsBtnX = screenWidth - settingsButtonBitmap.getWidth() - margin;
                float settingsBtnY = gameOverSettingsIconY;
                settingsButtonRect.set((int) settingsBtnX, (int) settingsBtnY, (int) (settingsBtnX + settingsButtonBitmap.getWidth()), (int) (settingsBtnY + settingsButtonBitmap.getHeight()));
                
                if (!settingHideSettingsIcon) {
                    // Animation logic for settings button on game over screen
                    float centerX = settingsButtonRect.centerX();
                    float centerY = settingsButtonRect.centerY();
                    canvas.save();
                    canvas.scale(settingsButtonScale, settingsButtonScale, centerX, centerY);
                    
                    if (pressedButtonRect == settingsButtonRect) {
                        pixelPaint.setColorFilter(new PorterDuffColorFilter(Color.GRAY, PorterDuff.Mode.MULTIPLY));
                    } else {
                        pixelPaint.setColorFilter(null);
                    }
                    
                    canvas.drawBitmap(settingsButtonBitmap, settingsBtnX, settingsBtnY, pixelPaint);
                    canvas.restore();
                    pixelPaint.setColorFilter(null);
                }
            }
        }
    }

    private void drawScoreWithImages(Canvas canvas, int number, int x, int y) {
        if (numberBitmaps[0] == null) return;
        String numStr = String.valueOf(number);
        float totalWidth = 0;
        for (char c : numStr.toCharArray()) {
            int digit = c - '0';
            if (digit >= 0 && digit < 10 && numberBitmaps[digit] != null) {
                totalWidth += numberBitmaps[digit].getWidth();
            }
        }

        float currentX = x - (totalWidth / 2f);
        for (char c : numStr.toCharArray()) {
            int digit = c - '0';
            if (digit >= 0 && digit < 10 && numberBitmaps[digit] != null) {
                Bitmap digitBitmap = numberBitmaps[digit];
                canvas.drawBitmap(digitBitmap, currentX, y - (digitBitmap.getHeight() / 2f), pixelPaint);
                currentX += digitBitmap.getWidth();
            }
        }
    }

    private void drawScoreWithImagesRightAligned(Canvas canvas, int number, float rightX, float y, Bitmap[] numberSet, float scaleCorrection) {
        if (numberSet[0] == null) return;
        float currentX = rightX;
        String numStr = String.valueOf(number);

        for (int i = numStr.length() - 1; i >= 0; i--) {
            int digit = numStr.charAt(i) - '0';
            if (digit >= 0 && digit < 10 && numberSet[digit] != null) {
                Bitmap digitBitmap = numberSet[digit];
                float digitWidth = digitBitmap.getWidth() * scaleCorrection;
                float digitHeight = digitBitmap.getHeight() * scaleCorrection;
                currentX -= digitWidth;
                scoreDigitDestRect.set(currentX, y - digitHeight, currentX + digitWidth, y);
                canvas.drawBitmap(digitBitmap, null, scoreDigitDestRect, pixelPaint);
            }
        }
    }

    private void drawCenteredBitmap(Canvas canvas, Bitmap bitmap, int yOffset) {
        float scaleCorrection = 1.0f;
        float margin = screenWidth * UI_MARGIN_HORIZONTAL_PERCENT;
        if (bitmap.getWidth() > (screenWidth - (2 * margin))) {
            scaleCorrection = (screenWidth - (2 * margin)) / bitmap.getWidth();
        }

        float newWidth = bitmap.getWidth() * scaleCorrection;
        float newHeight = bitmap.getHeight() * scaleCorrection;
        float left = (screenWidth - newWidth) / 2f;
        float groundY = screenHeight - systemBarBottom - groundHeight;
        float top = (systemBarTop + (groundY - systemBarTop) / 2f) - (newHeight / 2f) + (yOffset * scaleCorrection);
        centeredBitmapDestRect.set(left, top, left + newWidth, top + newHeight);
        canvas.drawBitmap(bitmap, null, centeredBitmapDestRect, pixelPaint);
    }

    private void showAspectRatioWarning() {
        new MaterialAlertDialogBuilder(getContext())
                .setTitle("Display Notice")
                .setMessage("Flappy Bird is not optimized for this screen's aspect ratio. You may encounter visual issues. For the best experience, a device with a smaller screen (and preferably Android 14+) is recommended.")
                .setPositiveButton("Continue", (dialog, which) -> {
                    prefs.edit().putBoolean(PREF_KEY_WARNING_SHOWN, true).apply();
                    dialog.dismiss();
                })
                .setCancelable(false)
                .show();
    }

    private Bitmap extract(Bitmap source, int x, int y, int w, int h) { return Bitmap.createBitmap(source, x, y, w, h);
    }
    private int loc(double coord, int dim) { return (int) (coord * dim);
    }
    private void extractBitmapsFromAtlas() {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inScaled = false;
        Bitmap atlas = BitmapFactory.decodeResource(getResources(), R.drawable.atlas, options);
        final int ATLAS_WIDTH = 1024, ATLAS_HEIGHT = 1024;
        unscaledBgDay = extract(atlas, loc(0.0, ATLAS_WIDTH), loc(0.0, ATLAS_HEIGHT), 288, 512);
        unscaledBgNight = extract(atlas, loc(0.28515625, ATLAS_WIDTH), loc(0.0, ATLAS_HEIGHT), 288, 512);
        unscaledLand = extract(atlas, loc(0.5703125, ATLAS_WIDTH), loc(0.0, ATLAS_HEIGHT), 336, 112);

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
        unscaledTitle = extract(atlas, loc(0.6855469, ATLAS_WIDTH), loc(0.17773438, ATLAS_HEIGHT), 178, 48);
        unscaledCopyright = extract(atlas, loc(0.86328125, ATLAS_WIDTH), loc(0.17773438, ATLAS_HEIGHT), 126, 14);
        unscaledButtonScore = extract(atlas, loc(0.8027344, ATLAS_WIDTH), loc(0.22851562, ATLAS_HEIGHT), 116, 70);
        unscaledTextReady = extract(atlas, loc(0.5703125, ATLAS_WIDTH), loc(0.11328125, ATLAS_HEIGHT), 196, 62);
        unscaledTextGameOver = extract(atlas, loc(0.765625, ATLAS_WIDTH), loc(0.11328125, ATLAS_HEIGHT), 204, 54);
        unscaledScorePanel = extract(atlas, loc(0.0, ATLAS_WIDTH), loc(0.50390625, ATLAS_HEIGHT), 238, 126);
        unscaledButtonPlay = extract(atlas, loc(0.6855469, ATLAS_WIDTH), loc(0.22851562, ATLAS_HEIGHT), 116, 70);

        int settingsResId = getResources().getIdentifier("settingsbutton", "drawable", getContext().getPackageName());
        if (settingsResId != 0) unscaledButtonSettings = BitmapFactory.decodeResource(getResources(), settingsResId, options);
        if (unscaledButtonSettings == null) Log.w("GameView", "Could not load 'settingsbutton.png'.");
        
        // UPDATED: Changed extraction height from 44 to 48 to prevent cut-off for Platinum/Gold
        unscaledMedalsBitmaps[0] = extract(atlas, loc(0.23632812, ATLAS_WIDTH), loc(0.50390625, ATLAS_HEIGHT), 48, 48);
        unscaledMedalsBitmaps[1] = extract(atlas, loc(0.23632812, ATLAS_WIDTH), loc(0.55078125, ATLAS_HEIGHT), 48, 48);
        // Silver and Bronze remain 44 width
        unscaledMedalsBitmaps[2] = extract(atlas, loc(0.21875, ATLAS_WIDTH), loc(0.8847656, ATLAS_HEIGHT), 44, 48);
        unscaledMedalsBitmaps[3] = extract(atlas, loc(0.21875, ATLAS_WIDTH), loc(0.9316406, ATLAS_HEIGHT), 44, 48);
        
        atlas.recycle();
        for (int i = 0; i < 10; i++) {
            int resId = getResources().getIdentifier("number_" + i, "drawable", getContext().getPackageName());
            if (resId != 0) unscaledNumberBitmaps[i] = BitmapFactory.decodeResource(getResources(), resId, options);
            else { Log.e("GameView", "Missing number resource: number_" + i);
                unscaledNumberBitmaps[i] = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888); }
        }
    }
    private Bitmap scaleBitmap(Bitmap bitmap) {
        if (bitmap == null) return null;
        int newW = (int) (bitmap.getWidth() * scale), newH = (int) (bitmap.getHeight() * scale);
        if (newW <= 0 || newH <= 0) return Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);
        return Bitmap.createScaledBitmap(bitmap, newW, newH, false);
    }
    private void scaleAllBitmaps() {
        float bgScale = (float) screenHeight / unscaledBgDay.getHeight();
        int bgWidth = (int) (unscaledBgDay.getWidth() * bgScale);
        bgDayBitmap = Bitmap.createScaledBitmap(unscaledBgDay, bgWidth, screenHeight, false);
        bgNightBitmap = Bitmap.createScaledBitmap(unscaledBgNight, bgWidth, screenHeight, false);
        groundBitmap = scaleBitmap(unscaledLand);

        pipeUpBitmap = scaleBitmap(unscaledPipeGreen);
        Matrix flipMatrix = new Matrix();
        flipMatrix.setScale(1, -1);
        pipeDownBitmap = Bitmap.createBitmap(pipeUpBitmap, 0, 0, pipeUpBitmap.getWidth(), pipeUpBitmap.getHeight(), flipMatrix, true);

        for (int i = 0; i < 3; i++) for (int j = 0; j < 3; j++) scaledAllBirdBitmaps[i][j] = scaleBitmap(unscaledAllBirdBitmaps[i][j]);
        for (int i=0; i<4; i++) medalBitmaps[i] = scaleBitmap(unscaledMedalsBitmaps[i]);
        for (int i = 0; i < 10; i++) numberBitmaps[i] = scaleBitmap(unscaledNumberBitmaps[i]);
        for (int i = 0; i < 10; i++) {
            Bitmap unscaled = unscaledNumberBitmaps[i];
            if (unscaled != null) {
                int newW = (int) (unscaled.getWidth() * scale * SCORE_PANEL_NUMBER_SCALE_MULTIPLIER);
                int newH = (int) (unscaled.getHeight() * scale * SCORE_PANEL_NUMBER_SCALE_MULTIPLIER);
                if (newW > 0 && newH > 0) smallNumberBitmaps[i] = Bitmap.createScaledBitmap(unscaled, newW, newH, false);
                else smallNumberBitmaps[i] = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);
            }
        }
        titleBitmap = scaleBitmap(unscaledTitle);
        copyrightBitmap = scaleBitmap(unscaledCopyright);
        scoreButtonBitmap = scaleBitmap(unscaledButtonScore); getReadyBitmap = scaleBitmap(unscaledTextReady);
        gameOverBitmap = scaleBitmap(unscaledTextGameOver); scorePanelBitmap = scaleBitmap(unscaledScorePanel);
        playButtonBitmap = scaleBitmap(unscaledButtonPlay);
        if (unscaledButtonSettings != null) {
            int newW = (int) (unscaledButtonSettings.getWidth() * scale * SETTINGS_BUTTON_SCALE_MULTIPLIER);
            int newH = (int) (unscaledButtonSettings.getHeight() * scale * SETTINGS_BUTTON_SCALE_MULTIPLIER);
            if (newW > 0 && newH > 0) settingsButtonBitmap = Bitmap.createScaledBitmap(unscaledButtonSettings, newW, newH, false);
        }
    }
    private void loadSounds() {
        AudioAttributes aa = new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_GAME).setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build();
        soundPool = new SoundPool.Builder().setMaxStreams(10).setAudioAttributes(aa).build();
        soundWing = soundPool.load(getContext(), R.raw.wing, 1);
        soundPoint = soundPool.load(getContext(), R.raw.point, 1);
        soundHit = soundPool.load(getContext(), R.raw.hit, 1);
        soundDie = soundPool.load(getContext(), R.raw.die, 1);
        soundSwooshing = soundPool.load(getContext(), R.raw.swooshing, 1);
        soundThunder = soundPool.load(getContext(), R.raw.thunder, 1);

        rainSoundPlayer = MediaPlayer.create(getContext(), R.raw.rain);
        if (rainSoundPlayer != null) {
            rainSoundPlayer.setLooping(true);
        }

        stormSoundPlayer = MediaPlayer.create(getContext(), R.raw.storm_backtrack);
        if (stormSoundPlayer != null) {
            stormSoundPlayer.setLooping(true);
        }
    }

    private void updateWeatherSounds() {
        if (!settingSoundEnabled) {
            stopWeatherSounds();
            return;
        }

        boolean shouldPlayRain = (settingWeatherEffect == 1 || settingWeatherEffect == 2) && (gameState == GameState.PLAYING || gameState == GameState.WAITING);
        boolean shouldPlayStorm = settingWeatherEffect == 2 && (gameState == GameState.PLAYING || gameState == GameState.WAITING);

        try {
            if (shouldPlayRain && rainSoundPlayer != null && !rainSoundPlayer.isPlaying()) {
                rainSoundPlayer.start();
            } else if (!shouldPlayRain && rainSoundPlayer != null && rainSoundPlayer.isPlaying()) {
                rainSoundPlayer.pause();
            }

            if (shouldPlayStorm && stormSoundPlayer != null && !stormSoundPlayer.isPlaying()) {
                stormSoundPlayer.start();
            } else if (!shouldPlayStorm && stormSoundPlayer != null && stormSoundPlayer.isPlaying()) {
                stormSoundPlayer.pause();
            }
        } catch (IllegalStateException e) {
            Log.e("GameView", "MediaPlayer state error in updateWeatherSounds", e);
        }
    }

    private void stopWeatherSounds() {
        try {
            if (rainSoundPlayer != null && rainSoundPlayer.isPlaying()) {
                rainSoundPlayer.pause();
            }
            if (stormSoundPlayer != null && stormSoundPlayer.isPlaying()) {
                stormSoundPlayer.pause();
            }
        } catch (IllegalStateException e) {
            Log.e("GameView", "MediaPlayer state error on stopWeatherSounds", e);
        }
    }

    private void playSound(final int soundID) {
        if (!settingSoundEnabled) return;
        if (soundPool != null && soundID != 0 && soundExecutor != null && !soundExecutor.isShutdown()) {
            soundExecutor.submit(() -> soundPool.play(soundID, 1, 1, 0, 0, 1));
        }
    }
    public void pause() {
        isRunning = false;
        choreographer.removeFrameCallback(this);
        stopWeatherSounds();
    }

    public void resume() {
        if (!isRunning && isReady) {
            loadSettings();
            if (gameState == GameState.HOME || gameState == GameState.PANEL_SLIDING) {
                resetGame();
                gameState = GameState.HOME;
            } else {
                updateWeatherSounds();
            }
            isRunning = true;
            lastFrameTimeNanos = 0;
            choreographer.postFrameCallback(this);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        pause();
        if (soundExecutor != null) { soundExecutor.shutdownNow(); soundExecutor = null;
        }
        if (soundPool != null) { soundPool.release(); soundPool = null;
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
    @Override
    public void doFrame(long frameTimeNanos) {
        if (!isRunning) return;
        if (lastFrameTimeNanos == 0) { lastFrameTimeNanos = frameTimeNanos; choreographer.postFrameCallback(this); return;
        }
        float deltaTime = (frameTimeNanos - lastFrameTimeNanos) / 1_000_000_000.0f;
        lastFrameTimeNanos = frameTimeNanos;
        if (deltaTime > (1.0f / 30.0f)) deltaTime = (1.0f / 30.0f);
        update(deltaTime); invalidate(); choreographer.postFrameCallback(this);
    }

    // --- INNER CLASSES FOR EFFECTS ---

    static class TrailParticle {
        float x, y, rotation, rainbowHue;
        int colorIndex, frameIndex;
        float lifetime = 0.5f;

        TrailParticle(float x, float y, float rotation, int colorIndex, int frameIndex, float rainbowHue) {
            this.x = x; this.y = y; this.rotation = rotation;
            this.colorIndex = colorIndex; this.frameIndex = frameIndex;
            this.rainbowHue = rainbowHue;
        }
        void update(float deltaTime) {
            lifetime -= deltaTime;
        }
    }

    static class SmokeParticle {
        float x, y, radius, alpha;
        float speedX, speedY;
        float expansionRate;
        float maxLife, life;
        Paint particlePaint;

        SmokeParticle(float startX, float startY, float scale) {
            Random r = new Random();
            x = startX + (r.nextFloat() - 0.5f) * 40 * scale;
            y = startY + (r.nextFloat() - 0.5f) * 40 * scale;
            radius = (10 + r.nextFloat() * 10) * scale;
            alpha = 0.8f; 
            
            speedY = -(30 + r.nextFloat() * 40) * scale; 
            speedX = -(50 + r.nextFloat() * 30) * scale; 
            
            expansionRate = 25 * scale;
            maxLife = 1.0f + r.nextFloat() * 0.5f;
            life = maxLife;
            
            particlePaint = new Paint();
            particlePaint.setAntiAlias(true);
            
            updateShader();
        }
        
        private void updateShader() {
            RadialGradient gradient = new RadialGradient(x, y, radius,
                new int[] { Color.argb((int)(alpha * 255), 60, 60, 60), Color.TRANSPARENT },
                null, Shader.TileMode.CLAMP);
            particlePaint.setShader(gradient);
        }

        void update(float dt, float windSpeed) {
            life -= dt;
            x -= windSpeed * dt; 
            x += speedX * dt;
            y += speedY * dt;
            radius += expansionRate * dt;
            alpha = Math.max(0, life / maxLife);
            updateShader();
        }
        
        void draw(Canvas canvas) {
             canvas.drawCircle(x, y, radius, particlePaint);
        }
        
        boolean isDead() { return life <= 0; }
    }
    
    static class SparkParticle {
        float x, y;
        float vx, vy;
        float life, maxLife;
        int color;
        int type; 
        
        SparkParticle(float startX, float startY, float scale, int type) {
            Random r = new Random();
            this.type = type;
            this.x = startX + (r.nextFloat() - 0.5f) * 60 * scale;
            this.y = startY + (r.nextFloat() - 0.5f) * 60 * scale;
            
            if (type == 0) { // Zap
                this.vx = (r.nextFloat() - 0.5f) * 50 * scale;
                this.vy = (r.nextFloat() - 0.5f) * 50 * scale;
                this.maxLife = 0.1f + r.nextFloat() * 0.1f;
            } else { // Fall
                this.vx = (r.nextFloat() - 0.5f) * 200 * scale;
                this.vy = -(100 + r.nextFloat() * 100) * scale; // Pop up then fall
                this.maxLife = 0.5f + r.nextFloat() * 0.5f;
            }
            
            this.life = maxLife;
            this.color = r.nextBoolean() ? Color.WHITE : Color.CYAN;
        }
        
        void update(float dt, float wind) {
            life -= dt;
            x += vx * dt;
            y += vy * dt;
            x -= wind * dt;
            
            if (type == 1) {
                vy += 800f * dt; // Gravity
            }
        }
        
        void draw(Canvas c, Paint p) {
            int alpha = (int)((life / maxLife) * 255);
            p.setColor(color);
            p.setAlpha(alpha);
            c.drawPoint(x, y, p);
        }
        
        boolean isDead() { return life <= 0; }
    }

    /**
     * Advanced Weather System 2.0
     */
    class WeatherSystem {
        private WeatherParticle[] frontParticles;
        private WeatherParticle[] backParticles;
        private List<LightningBolt> bolts = new ArrayList<>();
        private Random random = new Random();
        
        private Paint frontPaint, backPaint;
        private Paint lightningPaint, lightningGlowPaint;
        private Paint stormVignettePaint; 
        private Paint flashTintPaint; 
        private Paint vignettePaint;  
        
        private int currentWeatherType = 0;
        private float lightningIntensity = 0f;
        private float lightningDecay = 4.0f; 
        
        // Apocalypse Mode
        private boolean heavyRain = false;
        
        // Screen specs
        private int width, height;

        public WeatherSystem() {
            frontPaint = new Paint();
            frontPaint.setColor(Color.WHITE);
            frontPaint.setAntiAlias(true);
            
            backPaint = new Paint();
            backPaint.setColor(Color.WHITE);
            backPaint.setAlpha(100);
            backPaint.setAntiAlias(true);
            
            lightningPaint = new Paint();
            lightningPaint.setColor(Color.WHITE);
            lightningPaint.setStyle(Paint.Style.STROKE);
            lightningPaint.setStrokeCap(Paint.Cap.ROUND);
            lightningPaint.setStrokeJoin(Paint.Join.ROUND);
            lightningPaint.setAntiAlias(true);

            lightningGlowPaint = new Paint(lightningPaint);
            lightningGlowPaint.setColor(Color.argb(255, 200, 230, 255)); // Brighter glow
            lightningGlowPaint.setMaskFilter(new BlurMaskFilter(30, BlurMaskFilter.Blur.NORMAL));
            
            stormVignettePaint = new Paint();
            stormVignettePaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OVER));
            
            flashTintPaint = new Paint();
            flashTintPaint.setColor(Color.WHITE);
            flashTintPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.ADD));
            
            vignettePaint = new Paint();
            vignettePaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OVER));
        }

        public void init(int width, int height, float scale) {
            this.width = width;
            this.height = height;
            
            lightningPaint.setStrokeWidth(4 * scale);
            lightningGlowPaint.setStrokeWidth(20 * scale);
            
            int frontCount = 150;
            int backCount = 150;
            
            frontParticles = new WeatherParticle[frontCount];
            backParticles = new WeatherParticle[backCount];
            
            for(int i=0; i<frontCount; i++) frontParticles[i] = new WeatherParticle(width, height, true);
            for(int i=0; i<backCount; i++) backParticles[i] = new WeatherParticle(width, height, false);

            RadialGradient snowVignette = new RadialGradient(
                width / 2f, height / 2f, width * 1.3f,
                new int[]{Color.TRANSPARENT, Color.argb(200, 230, 230, 250)},
                new float[]{0.3f, 1.0f},
                Shader.TileMode.CLAMP
            );
            vignettePaint.setShader(snowVignette);

            RadialGradient stormVignette = new RadialGradient(
                width / 2f, height / 2f, width * 1.3f,
                new int[]{Color.TRANSPARENT, Color.argb(200, 0, 0, 20)},
                new float[]{0.3f, 1.0f},
                Shader.TileMode.CLAMP
            );
            stormVignettePaint.setShader(stormVignette);
        }
        
        public void setHeavyRain(boolean enable) {
            this.heavyRain = enable;
        }
        
        public void setWeatherType(int type) {
            this.currentWeatherType = type;
            for(WeatherParticle p : frontParticles) p.respawn(width, height, type);
            for(WeatherParticle p : backParticles) p.respawn(width, height, type);
        }

        public void triggerLightning(int width, int height) {
            bolts.add(new LightningBolt(random.nextFloat() * width, -50, width, height * 0.8f, -1, -1));
            lightningIntensity = 1.0f; // Full flash
        }
        
        public void triggerTargetedLightning(float targetX, float targetY, int width, int height) {
            bolts.add(new LightningBolt(targetX, -50, width, height, targetX, targetY));
            lightningIntensity = 1.0f;
        }
        
        public void triggerMultiDirectionalBolts(float targetX, float targetY, int width, int height) {
            // Top Center
            bolts.add(new LightningBolt(targetX, -50, width, height, targetX, targetY));
            // Top Left
            bolts.add(new LightningBolt(0, -50, width, height, targetX, targetY));
            // Top Right
            bolts.add(new LightningBolt(width, -50, width, height, targetX, targetY));
            // Side High
            bolts.add(new LightningBolt(-50, height * 0.2f, width, height, targetX, targetY));
            bolts.add(new LightningBolt(width + 50, height * 0.2f, width, height, targetX, targetY));
            
            lightningIntensity = 1.0f;
        }

        public void update(float deltaTime, float windSpeed, float birdVelocity, Rect birdRect) {
            float backWind = windSpeed * 0.5f; 
            for(WeatherParticle p : frontParticles) p.update(deltaTime, width, height, currentWeatherType, windSpeed, birdRect, heavyRain);
            for(WeatherParticle p : backParticles) p.update(deltaTime, width, height, currentWeatherType, backWind, birdRect, heavyRain);
            
            Iterator<LightningBolt> iter = bolts.iterator();
            while(iter.hasNext()) {
                LightningBolt bolt = iter.next();
                bolt.update(deltaTime);
                if(bolt.isDead()) iter.remove();
            }
            
            if (lightningIntensity > 0) {
                lightningIntensity -= lightningDecay * deltaTime;
                if (lightningIntensity < 0) lightningIntensity = 0;
            }
        }

        public void drawBack(Canvas canvas) {
            for(WeatherParticle p : backParticles) p.draw(canvas, backPaint, currentWeatherType, heavyRain);
        }
        
        public void drawFront(Canvas canvas) {
            for(WeatherParticle p : frontParticles) p.draw(canvas, frontPaint, currentWeatherType, heavyRain);
        }
        
        public void drawLightingOverlay(Canvas canvas, int width, int height) {
            int overscan = 100; 
            if (currentWeatherType == 2) {
                canvas.drawRect(-overscan, -overscan, width + overscan, height + overscan, stormVignettePaint);
            } else if (currentWeatherType == 3) {
                 canvas.drawRect(-overscan, -overscan, width + overscan, height + overscan, vignettePaint);
            }
            
            for(LightningBolt bolt : bolts) {
                bolt.draw(canvas, lightningPaint, lightningGlowPaint);
            }
            
            if (lightningIntensity > 0) {
                int alpha = (int)(lightningIntensity * 100); 
                flashTintPaint.setAlpha(alpha);
                canvas.drawRect(-overscan, -overscan, width + overscan, height + overscan, flashTintPaint);
            }
        }
    }

    class LightningBolt {
        private Path path = new Path();
        private float life = 0.25f; // Total duration
        private float maxLife = 0.25f;
        private float alpha = 1.0f;

        public LightningBolt(float startX, float startY, float width, float height, float targetX, float targetY) {
            if (targetX != -1) {
                generateTargeted(startX, startY, targetX, targetY);
            } else {
                generate(startX, startY, height);
            }
        }

        private void generate(float x, float y, float targetHeight) {
            path.reset();
            path.moveTo(x, y);
            float currentX = x;
            float currentY = y;
            Random r = new Random();
            
            while(currentY < targetHeight) {
                float segmentLen = 30 + r.nextFloat() * 50;
                currentY += segmentLen;
                currentX += (r.nextFloat() - 0.5f) * 80;
                path.lineTo(currentX, currentY);
                if (r.nextBoolean()) {
                    float branchX = currentX + (r.nextFloat() - 0.5f) * 100;
                    float branchY = currentY + (50 + r.nextFloat() * 50);
                    path.lineTo(branchX, branchY);
                    path.moveTo(currentX, currentY);
                }
            }
        }
        
        private void generateTargeted(float startX, float startY, float endX, float endY) {
            path.reset();
            path.moveTo(startX, startY);
            
            float currentX = startX;
            float currentY = startY;
            float distX = endX - startX;
            float distY = endY - startY;
            int segments = 8;
            float stepX = distX / segments;
            float stepY = distY / segments;
            
            Random r = new Random();
            
            for(int i=0; i<segments; i++) {
                currentX += stepX;
                currentY += stepY;
                
                // Add jitter
                float jitterX = (r.nextFloat() - 0.5f) * 100;
                float jitterY = (r.nextFloat() - 0.5f) * 50;
                
                // Lock last point to target
                if (i == segments -1) {
                    currentX = endX;
                    currentY = endY;
                } else {
                    currentX += jitterX;
                    currentY += jitterY;
                }
                
                path.lineTo(currentX, currentY);
            }
        }

        public void update(float dt) {
            life -= dt;
            alpha = life / maxLife;
        }

        public boolean isDead() { return life <= 0; }

        public void draw(Canvas c, Paint core, Paint glow) {
            int a = (int)(alpha * 255);
            if (a > 255) a = 255; if (a < 0) a = 0;
            
            glow.setAlpha(a);
            core.setAlpha(a);
            c.drawPath(path, glow);
            c.drawPath(path, core);
        }
    }

    static class WeatherParticle {
        float x, y, z; 
        float ySpeed, size, length;
        float offset; 
        float bounceX = 0, bounceY = 0;
        private static Random random = new Random();

        WeatherParticle(int screenWidth, int screenHeight, boolean isFront) {
            if (isFront) z = 1.0f + random.nextFloat() * 0.5f;
            else z = 0.5f + random.nextFloat() * 0.5f;
            respawn(screenWidth, screenHeight, 0);
            y = random.nextFloat() * screenHeight;
        }

        void respawn(int screenWidth, int screenHeight, int weatherType) {
            x = random.nextFloat() * screenWidth;
            y = -50 - random.nextFloat() * 50;
            bounceX = 0;
            bounceY = 0;
            offset = random.nextFloat() * 100f;
            
            if (weatherType == 3) { // Snow
                size = (2f + random.nextFloat() * 4f) * z;
                ySpeed = (30f + random.nextFloat() * 40f) * z;
            } else { // Rain
                size = (2.0f + random.nextFloat()) * z; 
                length = (30f + random.nextFloat() * 40f) * z; 
                ySpeed = (700f + random.nextFloat() * 300f) * z;
            }
        }

        void update(float deltaTime, int screenWidth, int screenHeight, int weatherType, float windSpeed, Rect birdRect, boolean heavy) {
            float speedMult = heavy ? 2.5f : 1.0f;
            y += ySpeed * speedMult * deltaTime;
            x += bounceX * deltaTime;
            y += bounceY * deltaTime;
            bounceX *= 0.90f; 
            bounceY *= 0.90f; 

            if (weatherType == 3) { // Snow Physics
                float drift = (float)Math.sin(y * 0.01f + offset) * 50f * z;
                x += (windSpeed * 0.1f * z + drift) * deltaTime;
                
                if (birdRect.contains((int)x, (int)y)) {
                    float dx = x - birdRect.centerX();
                    float dy = y - birdRect.centerY();
                    float dist = (float)Math.sqrt(dx*dx + dy*dy);
                    if (dist < 1) dist = 1;
                    bounceX = (dx / dist) * 200f;
                    bounceY = -Math.abs(ySpeed) * 0.8f; 
                    x += (dx/dist) * 5f;
                    y += (dy/dist) * 5f;
                }
                
            } else { // Rain Physics
                x -= windSpeed * z * deltaTime;
            }

            if (y > screenHeight + length || x < -100 || x > screenWidth + 100) {
                respawn(screenWidth, screenHeight, weatherType);
            }
        }

        void draw(Canvas canvas, Paint paint, int weatherType, boolean heavy) {
            if (weatherType == 3) { // Snow
                int alpha = (int)(180 * z); 
                if (alpha > 255) alpha = 255;
                paint.setAlpha(alpha);
                canvas.drawCircle(x, y, size, paint);
            } else { // Rain
                int alpha = (int)(160 * z); 
                if (alpha > 255) alpha = 255;
                paint.setAlpha(alpha);
                paint.setStrokeWidth(size);
                float run = -20f * z; 
                float rise = length * (heavy ? 1.5f : 1.0f);
                
                canvas.drawLine(x, y, x + run, y + rise, paint);
                
                if (heavy) {
                    // Cheap density hack: Draw duplicate lines offset slightly
                    canvas.drawLine(x + 50, y + 50, x + 50 + run, y + 50 + rise, paint);
                    canvas.drawLine(x - 30, y - 70, x - 30 + run, y - 70 + rise, paint);
                }
            }
        }
    }
}

class Pipe {
    float x; int topPipeY; boolean isScored; int width, height;
    private static Random random = new Random();
    public Paint pipePaint;
    public boolean isGolden;

    private Rect topHeadRect = new Rect(), topBodyRect = new Rect();
    private Rect bottomHeadRect = new Rect(), bottomBodyRect = new Rect();

    private static float pipeHeadWidth, pipeHeadHeight;
    private static float pipeBodyWidth, pipeBodyOffsetX;
    private float currentYOffset = 0;
    public Pipe(float x, int width, int height) {
        this.x = x; this.width = width;
        this.height = height; this.isScored = false;
        this.isGolden = false;
        this.pipePaint = new Paint();
        this.pipePaint.setFilterBitmap(false);
        this.pipePaint.setAntiAlias(false);
    }

    public static void initHitboxDimensions(float scale) {
        final int UNSCALED_HEAD_WIDTH = 52;
        final int UNSCALED_HEAD_HEIGHT = 24;
        final int UNSCALED_BODY_WIDTH = 48;

        pipeHeadWidth = UNSCALED_HEAD_WIDTH * scale;
        pipeHeadHeight = UNSCALED_HEAD_HEIGHT * scale;
        pipeBodyWidth = UNSCALED_BODY_WIDTH * scale;
        pipeBodyOffsetX = (pipeHeadWidth - pipeBodyWidth) / 2.0f;
    }

    public void resetHeight(int pipeGap, int playAreaHeight, int topOffset, float variationMultiplier) {
        float baseMarginPercent = 0.08f;
        float effectiveVariation = Math.max(0.001f, variationMultiplier);
        float marginPercent = Math.max(0.0f, Math.min(0.49f, baseMarginPercent / effectiveVariation));

        int margin = (int) (playAreaHeight * marginPercent);
        int availableRange = playAreaHeight - (2 * margin) - pipeGap;
        if (availableRange <= 0) {
            this.topPipeY = (playAreaHeight / 2) + topOffset - pipeGap / 2;
        } else {
            this.topPipeY = random.nextInt(availableRange) + margin + topOffset;
        }

        this.currentYOffset = 0;
    }

    public void updateAnimation(boolean isMoving, float animationCounter, float moveRange, boolean isStopping) {
        if (isStopping) { currentYOffset *= 0.9f;
            if (Math.abs(currentYOffset) < 0.1f) currentYOffset = 0; }
        else if (isMoving) { float phaseShift = (this.x / 500f) * (float)Math.PI;
            this.currentYOffset = ((float) Math.sin(animationCounter + phaseShift) * moveRange); }
        else { currentYOffset = 0;
        }
    }
    public boolean isAtRest() { return currentYOffset == 0;
    }

    private float getCurrentTopPipeY() { return topPipeY + currentYOffset;
    }
    public float getTopPipeY() { return getCurrentTopPipeY() - this.height;
    }
    public float getBottomPipeY(int pipeGap) { return getCurrentTopPipeY() + pipeGap;
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
        // Extend collision box infinitely upwards (negative Y)
        // to prevent flying over the pipes.
        float top = -20000f; 
        float bottom = getCurrentTopPipeY() - pipeHeadHeight;
        float visualWidth = pipeBodyWidth * widthMultiplier;
        float xPos = x + pipeBodyOffsetX + (pipeBodyWidth - visualWidth) / 2.0f;
        topBodyRect.set(
                (int) xPos,
                (int) top,
                (int) (xPos + visualWidth),
                (int) bottom
        );
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
        float bottom = top + (this.height - pipeHeadHeight);
        float visualWidth = pipeBodyWidth * widthMultiplier;
        float xPos = x + pipeBodyOffsetX + (pipeBodyWidth - visualWidth) / 2.0f;
        bottomBodyRect.set(
                (int) xPos,
                (int) top,
                (int) (xPos + visualWidth),
                (int) bottom
        );
        return bottomBodyRect;
    }
}
