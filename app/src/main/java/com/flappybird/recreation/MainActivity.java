package com.flappybird.recreation;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.os.Bundle;
import android.util.Log;
import android.view.Choreographer;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import androidx.activity.EdgeToEdge;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import android.widget.FrameLayout;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.BiFunction;

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

    private enum GameState { HOME, TRANSITION_TO_WAITING, WAITING, PLAYING, GAME_OVER, PANEL_SLIDING }
    private GameState gameState = GameState.HOME;
    private Choreographer choreographer;
    private boolean isRunning = false;
    private boolean isReady = false;
    private long lastFrameTimeNanos = 0;
    private int screenWidth, screenHeight;
    private float scale;
    private int systemBarTop = -1, systemBarBottom = -1;

    private Paint pixelPaint, birdPaint;
    private Paint pipePaint;
    private Matrix birdMatrix = new Matrix();

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

    private int birdFrame = 0;
    private float birdX, birdY, birdVelocityY, birdRotation;
    private long lastFlapTimeMillis = 0;
    private Rect birdRect = new Rect(), playButtonRect = new Rect(), scoreButtonRect = new Rect(), settingsButtonRect = new Rect();
    private int currentBirdColor;

    private List<Pipe> pipes;
    private int pipeSpacing, pipeGap;

    private float groundX = 0, backgroundX = 0;
    private int groundHeight;

    private int score = 0, highScore = 0;
    private SharedPreferences prefs;

    private SoundPool soundPool;
    private int soundWing, soundPoint, soundHit, soundDie, soundSwooshing;
    private ExecutorService soundExecutor;
    private boolean hasTier1Triggered = false, hasTier2Triggered = false;
    private float BASE_GRAVITY_PER_SEC, BASE_FLAP_VELOCITY_PER_SEC, BASE_PIPE_SPEED_PER_SEC;
    private float BASE_ROTATION_DELAY_THRESHOLD_PER_SEC, BASE_ROTATION_DOWN_SPEED_PER_SEC;
    private float GRAVITY_PER_SEC, FLAP_VELOCITY_PER_SEC, PIPE_SPEED_PER_SEC;
    private float ROTATION_DELAY_THRESHOLD_PER_SEC, ROTATION_DOWN_SPEED_PER_SEC;
    private Random random = new Random();
    private int flashAlpha = 0;
    private Paint flashPaint;

    private final float UI_MARGIN_HORIZONTAL_PERCENT = 0.025f; // Original value was 0.05f
    private final float HOME_BUTTON_GAP_PERCENT = 0.05f;
    private final float SETTINGS_BUTTON_SCALE_MULTIPLIER = 0.75f;
    private final float PIPE_GAP_BIRD_HEIGHT_MULTIPLIER = 2.2f;
    private final float SCORE_PANEL_NUMBER_SCALE_MULTIPLIER = 0.6f;
    private final int SCORE_PANEL_CURRENT_SCORE_Y_OFFSET = 55;
    private final int SCORE_PANEL_HIGH_SCORE_Y_OFFSET = 98;
    private final int SCORE_PANEL_MEDAL_X_OFFSET = 31;
    private final int SCORE_PANEL_MEDAL_Y_OFFSET = 45;
    private static final String PREF_KEY_WARNING_SHOWN = "hasSeenAspectRatioWarning";
    private final float BIRD_HITBOX_PADDING_X = 0.10f;
    private final float BIRD_HITBOX_PADDING_Y = 0.10f;
    private final long FLAP_ANIMATION_TIMEOUT_MS = 480;
    private float gameOverElementsY;
    private int gameOverElementsTargetY;
    private Rect pressedButtonRect = null;
    private float pressOffsetY = 0;
    private boolean pipesAreMoving = false;
    private boolean pipesAreStopping = false;
    private float pipeAnimationCounter = 0f;
    private float pipeAnimationSpeed = 0.03f;
    private float maxPipeMoveRange;
    private float currentPipeMoveRange = 0f;
    private long pipeMoveStartTime = 0;
    private final long PIPE_MOVE_DURATION_MS = 30_000;
    private final float PIPE_SPAWN_DELAY_SECONDS = 1.5f;
    private final long PANEL_SLIDE_DELAY_MS = 100;
    private final float PANEL_SLIDE_EASING_BASE = 0.95f;
    private final long GAMEOVER_ICON_SLIDE_DELAY_MS = 25;
    private final float GAMEOVER_ICON_EASING_BASE = 0.92f;
    private final float FLASH_FADE_BASE = 0.94f;

    private long birdHitGroundTime = 0;
    private float gameOverSettingsIconY = -1;
    private float gameOverSettingsIconTargetY;
    private boolean isGameOverIconAnimationDone = false;
    private long panelFinishedSlidingTime = 0;


    private Paint transitionPaint;
    private float transitionAlpha = 0;
    private boolean isFadingOut = false;
    private float TRANSITION_FADE_SPEED_PER_SEC;
    private boolean isRestarting = false;

    private Paint darkenPaint;
    private float uiScaleCorrection = 1.0f;

    private int settingBirdColor;
    private int settingBackground;
    private boolean settingSoundEnabled;
    private int settingGameOverOpacity;
    private float settingGameSpeed;
    private float settingGravity;
    private float settingJumpStrength;
    private float settingPipeGap;
    private boolean settingNoClipEnabled;
    private boolean settingWingAnimationEnabled;
    private int settingPipeColor;
    private boolean settingMovingPipesEnabled;
    private int settingPipeMoveTier1;
    private int settingPipeMoveTier2;
    private float settingBirdHangDelay;
    private boolean settingHideSettingsIcon;
    private float settingPipeSpacing;
    private float settingBirdHitbox;
    private boolean settingRainbowBirdEnabled;
    private boolean settingUpsideDownEnabled;
    private boolean settingReversePipesEnabled;
    private float settingPipeVariation;
    private int settingScoreMultiplier;
    private float rainbowHue = 0f;


    private int displayedScore = 0;
    private long lastScoreTickTime = 0;
    private final long SCORE_ANIMATION_INTERVAL_MS_FAST = 8;
    private final long SCORE_ANIMATION_INTERVAL_MS_SLOW = 45;
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
    private static final float TARGET_FPS = 120.0f;


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
        pipePaint = new Paint();
        pipePaint.setFilterBitmap(false);
        pipePaint.setAntiAlias(false);

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

        flashPaint = new Paint();
        flashPaint.setARGB(0, 255, 255, 255);

        transitionPaint = new Paint();
        transitionPaint.setColor(Color.BLACK);
        transitionPaint.setAlpha(0);

        darkenPaint = new Paint();
        darkenPaint.setColor(Color.BLACK);
        darkenPaint.setAlpha(0);
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
        BASE_ROTATION_DELAY_THRESHOLD_PER_SEC = (2.8f * scale) * TARGET_FPS;
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

        resetGame();
        gameState = GameState.HOME;

        isReady = true;
        resume();
    }

    private int getPlayableHeight() { return screenHeight - systemBarTop - systemBarBottom;
    }

    private void resetGame() {
        loadSettings();
        GRAVITY_PER_SEC = BASE_GRAVITY_PER_SEC * settingGravity * (settingUpsideDownEnabled ? -1 : 1);
        FLAP_VELOCITY_PER_SEC = BASE_FLAP_VELOCITY_PER_SEC * settingJumpStrength * (settingUpsideDownEnabled ? -1 : 1);
        PIPE_SPEED_PER_SEC = BASE_PIPE_SPEED_PER_SEC * settingGameSpeed * (settingReversePipesEnabled ? -1 : 1);
        ROTATION_DELAY_THRESHOLD_PER_SEC = BASE_ROTATION_DELAY_THRESHOLD_PER_SEC * settingBirdHangDelay;
        ROTATION_DOWN_SPEED_PER_SEC = BASE_ROTATION_DOWN_SPEED_PER_SEC;
        final int UNSCALED_BIRD_HEIGHT_FOR_GAP = 48;
        pipeGap = (int) (UNSCALED_BIRD_HEIGHT_FOR_GAP * scale * PIPE_GAP_BIRD_HEIGHT_MULTIPLIER * settingPipeGap);

        final float VIRTUAL_SCREEN_WIDTH = unscaledBgDay.getWidth();
        final float VIRTUAL_SPACING_FACTOR = 0.68f;
        pipeSpacing = (int) (VIRTUAL_SCREEN_WIDTH * VIRTUAL_SPACING_FACTOR * scale * settingPipeSpacing);

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
        if (settingBackground == 0) backgroundBitmap = bgDayBitmap;
        else if (settingBackground == 1) backgroundBitmap = bgNightBitmap;
        else backgroundBitmap = random.nextBoolean() ?
        bgNightBitmap : bgDayBitmap;

        currentPipeTopBitmap = pipeDownBitmap;
        currentPipeBottomBitmap = pipeUpBitmap;
        pipePaint.setColorFilter(createPipeColorFilter(settingPipeColor));

        int birdColor;
        if (settingBirdColor >= 0 && settingBirdColor <= 2) birdColor = settingBirdColor;
        else birdColor = random.nextInt(3);
        this.currentBirdColor = birdColor;
        for (int i = 0; i < 3; i++) birdBitmaps[i] = scaledAllBirdBitmaps[birdColor][i];

        birdX = settingReversePipesEnabled ?
        screenWidth * 2 / 3f : screenWidth / 3f;
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
            pipes.add(pipe);
        }

        score = 0;
        displayedScore = 0;
        flashAlpha = 0;
        currentMedalBitmap = null;
        if (darkenPaint != null) darkenPaint.setAlpha(0);
    }

    private ColorFilter createPipeColorFilter(int pipeColorSetting) {
        if(settingReversePipesEnabled) {
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
        if (gameState != GameState.GAME_OVER && gameState != GameState.PANEL_SLIDING) {
            float effectiveSpeed = PIPE_SPEED_PER_SEC;
            float bgWidth = backgroundBitmap.getWidth();
            backgroundX -= (effectiveSpeed / 2) * deltaTime;
            if (effectiveSpeed > 0 && backgroundX <= -bgWidth) {
                backgroundX += bgWidth;
            } else if (effectiveSpeed < 0 && backgroundX >= bgWidth) {
                backgroundX -= bgWidth;
            }

            float groundWidth = groundBitmap.getWidth();
            groundX -= effectiveSpeed * deltaTime;
            if (effectiveSpeed > 0 && groundX <= -groundWidth) {
                groundX += groundWidth;
            } else if (effectiveSpeed < 0 && groundX >= groundWidth) {
                groundX -= groundWidth;
            }
        }

        boolean wingAnimation = settingWingAnimationEnabled && (System.currentTimeMillis() - lastFlapTimeMillis < FLAP_ANIMATION_TIMEOUT_MS);
        if (gameState == GameState.PLAYING) {
             if (wingAnimation) birdFrame = (int) ((System.currentTimeMillis() / 75) % birdBitmaps.length);
             else birdFrame = 1;
        } else if (gameState == GameState.HOME || gameState == GameState.TRANSITION_TO_WAITING || gameState == GameState.WAITING) {
            if (settingWingAnimationEnabled) birdFrame = (int) ((System.currentTimeMillis() / 150) % birdBitmaps.length);
            else birdFrame = 1;
        }

        if (settingRainbowBirdEnabled) {
            rainbowHue = (rainbowHue + 150 * deltaTime) % 360;
        }

        switch (gameState) {
            case PLAYING:
                birdVelocityY += GRAVITY_PER_SEC * deltaTime;
                birdY += birdVelocityY * deltaTime;

                final float ROTATION_UP_SPEED = 6.0f * TARGET_FPS;
                if (settingUpsideDownEnabled) {
                    if (birdVelocityY > 0) {
                        birdRotation = Math.min(25f, birdRotation + ROTATION_UP_SPEED * deltaTime);
                    } else if (birdVelocityY < -ROTATION_DELAY_THRESHOLD_PER_SEC) {
                        birdRotation = Math.max(-90f, birdRotation - ROTATION_DOWN_SPEED_PER_SEC * deltaTime);
                    }
                } else {
                    if (birdVelocityY < 0) {
                        birdRotation = Math.max(-25f, birdRotation - ROTATION_UP_SPEED * deltaTime);
                    } else if (birdVelocityY > ROTATION_DELAY_THRESHOLD_PER_SEC) {
                        birdRotation = Math.min(90f, birdRotation + ROTATION_DOWN_SPEED_PER_SEC * deltaTime);
                    }
                }
                break;
            case HOME:
                birdX = settingReversePipesEnabled ?
                screenWidth * 2 / 3f : screenWidth / 2f;
                birdY = (systemBarTop + (getPlayableHeight() - groundHeight) / 2f) + (float) (Math.sin(System.currentTimeMillis() / 200.0) * 6 * scale);
                if (settingUpsideDownEnabled) birdY = screenHeight - birdY;
                birdRotation = 0;
                break;
            case TRANSITION_TO_WAITING:
                birdY = (systemBarTop + (getPlayableHeight() - groundHeight) / 2f) + (float) (Math.sin(System.currentTimeMillis() / 200.0) * 6 * scale);
                if (settingUpsideDownEnabled) birdY = screenHeight - birdY;
                birdRotation = 0;
                if (isFadingOut) {
                    transitionAlpha += TRANSITION_FADE_SPEED_PER_SEC * deltaTime;
                    if (transitionAlpha >= 255) {
                        transitionAlpha = 255;
                        isFadingOut = false; birdX = settingReversePipesEnabled ? screenWidth * 2 / 3f : screenWidth / 3f;
                    }
                } else {
                    transitionAlpha -= TRANSITION_FADE_SPEED_PER_SEC * deltaTime;
                    if (transitionAlpha <= 0) {
                        transitionAlpha = 0;
                        gameState = GameState.WAITING;
                    }
                }
                break;
            case WAITING:
                birdX = settingReversePipesEnabled ?
                screenWidth * 2 / 3f : screenWidth / 3f;
                birdY = (systemBarTop + (getPlayableHeight() - groundHeight) / 2f) + (float) (Math.sin(System.currentTimeMillis() / 200.0) * 6 * scale);
                if (settingUpsideDownEnabled) birdY = screenHeight - birdY;
                birdRotation = 0;
                break;
            case GAME_OVER:
                float groundTopY = screenHeight - systemBarBottom - groundHeight;
                boolean isOnGround;
                if(settingUpsideDownEnabled) {
                    isOnGround = birdY - (birdBitmaps[1].getHeight() / 2f) <= systemBarTop;
                } else {
                    isOnGround = birdY + (birdBitmaps[1].getHeight() / 2f) >= groundTopY;
                }

                if (!isOnGround) {
                    birdVelocityY += (GRAVITY_PER_SEC * 1.5f) * deltaTime;
                    birdY += birdVelocityY * deltaTime;
                    final float DEATH_ROTATION_SPEED = 2.8f * TARGET_FPS;
                    if(settingUpsideDownEnabled) {
                        if (birdRotation > -90) birdRotation -= DEATH_ROTATION_SPEED * deltaTime;
                    } else {
                        if (birdRotation < 90) birdRotation += DEATH_ROTATION_SPEED * deltaTime;
                    }
                    birdHitGroundTime = 0;
                } else {
                    if (settingUpsideDownEnabled) {
                        birdY = systemBarTop + (birdBitmaps[1].getHeight() / 2f);
                        birdRotation = -90f;
                    } else {
                        birdY = groundTopY - (birdBitmaps[1].getHeight() / 2f);
                        birdRotation = 90f;
                    }
                    birdFrame = 1;
                    if (birdHitGroundTime == 0) {
                        birdHitGroundTime = System.currentTimeMillis();
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
                float deltaMultiplier = TARGET_FPS * deltaTime;
                if (gameOverElementsY > gameOverElementsTargetY) {
                    float portionToMove = 1.0f - (float)Math.pow(PANEL_SLIDE_EASING_BASE, deltaMultiplier);
                    gameOverElementsY -= (gameOverElementsY - gameOverElementsTargetY) * portionToMove;
                    if (gameOverElementsY - gameOverElementsTargetY < 1.0f) {
                        gameOverElementsY = gameOverElementsTargetY;
                    }
                } else {
                    gameOverElementsY = gameOverElementsTargetY;
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
                    transitionAlpha += TRANSITION_FADE_SPEED_PER_SEC * deltaTime;
                    if (transitionAlpha >= 255) {
                        transitionAlpha = 255;
                        resetGame(); gameState = GameState.TRANSITION_TO_WAITING; isFadingOut = false;
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
                    pipeAnimationCounter += (pipeAnimationSpeed * TARGET_FPS) * deltaTime;
                    if (currentPipeMoveRange < maxPipeMoveRange && !pipesAreStopping) {
                        float moveRangeSpeed = (0.15f * scale * TARGET_FPS);
                        currentPipeMoveRange += moveRangeSpeed * deltaTime;
                        currentPipeMoveRange = Math.min(currentPipeMoveRange, maxPipeMoveRange);
                    }
                }
            }
            int playableAreaHeight = getPlayableHeight() - groundHeight;
            for (Pipe pipe : pipes) {
                pipe.x -= PIPE_SPEED_PER_SEC * deltaTime;
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
                    playSound(soundPoint); pipe.isScored = true;
                }

                boolean recycle;
                if (settingReversePipesEnabled) {
                    recycle = pipe.x - pipe.width > screenWidth;
                } else {
                    recycle = pipe.x + pipe.width < 0;
                }

                if (recycle) {
                    pipe.x += (pipes.size() * pipeSpacing * (settingReversePipesEnabled ? -1 : 1));
                    pipe.resetHeight(pipeGap, playableAreaHeight, systemBarTop, settingPipeVariation);
                    pipe.isScored = false;
                }
            }
        }

        if (flashAlpha > 0) {
            float deltaMultiplier = TARGET_FPS * deltaTime;
            flashAlpha = (int)(flashAlpha * Math.pow(FLASH_FADE_BASE, deltaMultiplier));
        }
        updateBirdRect();
        if (gameState == GameState.PLAYING) { checkCollisions(); }
    }

    private void checkCollisions() {
        if (settingNoClipEnabled) return;
        if (settingUpsideDownEnabled) {
            if (birdY - (birdBitmaps[birdFrame].getHeight() / 2f) <= systemBarTop) {
                gameOver();
                return;
            }
            if (birdRect.bottom >= screenHeight - systemBarBottom - groundHeight) {
                gameOver();
                return;
            }
        } else {
            if (birdY + (birdBitmaps[birdFrame].getHeight() / 2f) >= screenHeight - systemBarBottom - groundHeight) {
                gameOver();
                return;
            }
            if (birdRect.top <= systemBarTop) {
                gameOver();
                return;
            }
        }

        for (Pipe pipe : pipes) {
            if (Rect.intersects(birdRect, pipe.getTopHeadRect()) ||
                Rect.intersects(birdRect, pipe.getTopBodyRect()) ||
                Rect.intersects(birdRect, pipe.getBottomHeadRect(pipeGap)) ||
                Rect.intersects(birdRect, pipe.getBottomBodyRect(pipeGap))) {
         
               gameOver(); return;
            }
        }
    }

    private void gameOver() {
        if (gameState == GameState.PLAYING) {
            gameState = GameState.GAME_OVER;
            flashAlpha = 255;
            playSound(soundHit);
            postDelayed(() -> playSound(soundDie), 300);
            if (score > highScore) { highScore = score; prefs.edit().putInt("highScore", highScore).apply();
            }
            if (score >= 40) currentMedalBitmap = medalBitmaps[3];
            else if (score >= 30) currentMedalBitmap = medalBitmaps[2];
            else if (score >= 20) currentMedalBitmap = medalBitmaps[1];
            else if (score >= 10) currentMedalBitmap = medalBitmaps[0];

            displayedScore = 0;
            lastScoreTickTime = 0;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (canvas == null || !isReady) return;

        canvas.save();
        if (settingUpsideDownEnabled) {
            canvas.scale(1, -1, screenWidth / 2f, screenHeight / 2f);
        }

        float bgDrawWidth = backgroundBitmap.getWidth();
        float startX = backgroundX % bgDrawWidth;
        if (startX > 0) {
            startX -= bgDrawWidth;
        }
        for (float x = startX; x < screenWidth; x += bgDrawWidth) {
            canvas.drawBitmap(backgroundBitmap, x, 0, pixelPaint);
        }
        
        if (gameState != GameState.HOME && !(gameState == GameState.TRANSITION_TO_WAITING && isFadingOut)) {
            for (Pipe pipe : pipes) {
                canvas.drawBitmap(currentPipeTopBitmap, pipe.x, pipe.getTopPipeY(), pipePaint);
                canvas.drawBitmap(currentPipeBottomBitmap, pipe.x, pipe.getBottomPipeY(pipeGap), pipePaint);
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

        birdPaint.setColorFilter(null);
        if(settingRainbowBirdEnabled) {
             ColorMatrix colorMatrix = new ColorMatrix();
             colorMatrix.setRotate(0, rainbowHue);
             colorMatrix.setRotate(1, rainbowHue);
             colorMatrix.setRotate(2, rainbowHue);
             birdPaint.setColorFilter(new ColorMatrixColorFilter(colorMatrix));
        }
        Bitmap currentBirdBitmap = birdBitmaps[birdFrame];
        birdMatrix.reset();
        birdMatrix.postTranslate(-currentBirdBitmap.getWidth() / 2f, -currentBirdBitmap.getHeight() / 2f);
        birdMatrix.postRotate(birdRotation);
        birdMatrix.postTranslate(birdX, birdY);
        canvas.drawBitmap(currentBirdBitmap, birdMatrix, birdPaint);
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
            drawGameOverScreen(canvas);
        }

        canvas.restore();

        if (darkenPaint.getAlpha() > 0) {
            canvas.drawRect(0, 0, screenWidth, screenHeight, darkenPaint);
        }
        if (flashAlpha > 0) {
            flashPaint.setAlpha(flashAlpha);
            canvas.drawRect(0, 0, screenWidth, screenHeight, flashPaint);
        }
        if (transitionAlpha > 0) {
            transitionPaint.setAlpha((int) transitionAlpha);
            canvas.drawRect(0, 0, screenWidth, screenHeight, transitionPaint);
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
                pressedButtonRect = null;
                if (gameState == GameState.HOME) {
                    if (playButtonRect.contains(touchX, touchY)) pressedButtonRect = playButtonRect;
                    else if (scoreButtonRect.contains(touchX, touchY)) pressedButtonRect = scoreButtonRect;
                    else if (settingsButtonRect.contains(touchX, touchY)) pressedButtonRect = settingsButtonRect;
                } else if (gameState == GameState.WAITING) {
                    gameState = GameState.PLAYING;
                    flap();
                } else if (gameState == GameState.PLAYING) {
                    flap();
                } else if (gameState == GameState.PANEL_SLIDING && gameOverElementsY == gameOverElementsTargetY && !isRestarting) {
                    if (displayedScore == score && playButtonRect.contains(touchX, touchY)) {
                        pressedButtonRect = playButtonRect;
                    }
                    if (isGameOverIconAnimationDone && settingsButtonRect.contains(touchX, touchY)) {
                        pressedButtonRect = settingsButtonRect;
                    }
                }
                if (pressedButtonRect != null) invalidate();
                break;
            case MotionEvent.ACTION_UP:
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
                if (pressedButtonRect != null) { pressedButtonRect = null;
                invalidate(); }
                break;
            case MotionEvent.ACTION_MOVE:
                if (pressedButtonRect != null && !pressedButtonRect.contains(touchX, touchY)) { pressedButtonRect = null;
                invalidate(); }
                break;
            case MotionEvent.ACTION_CANCEL:
                 if (pressedButtonRect != null) { pressedButtonRect = null;
                 invalidate(); }
                break;
        }
        return true;
    }
    private void flap() {
        birdVelocityY = FLAP_VELOCITY_PER_SEC; playSound(soundWing);
        this.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
        lastFlapTimeMillis = System.currentTimeMillis();
    }
    private void updateBirdRect() {
        Bitmap currentBirdBitmap = birdBitmaps[birdFrame];
        float baseHitboxWidth = currentBirdBitmap.getWidth() * (1.0f - 2.0f * BIRD_HITBOX_PADDING_X);
        float baseHitboxHeight = currentBirdBitmap.getHeight() * (1.0f - 2.0f * BIRD_HITBOX_PADDING_Y);
        float hitboxMultiplier = Math.max(0.0f, settingBirdHitbox);
        float finalHitboxWidth = baseHitboxWidth * hitboxMultiplier;
        float finalHitboxHeight = baseHitboxHeight * hitboxMultiplier;
        float left = birdX - finalHitboxWidth / 2f;
        float top = birdY - finalHitboxHeight / 2f;
        float right = birdX + finalHitboxWidth / 2f;
        float bottom = birdY + finalHitboxHeight / 2f;
        birdRect.set((int) left, (int) top, (int) right, (int) bottom);
    }


    private void drawHomeScreen(Canvas canvas) {
        float titleX = (screenWidth - titleBitmap.getWidth()) / 2f;
        float titleY = getPlayableHeight() * 0.22f + systemBarTop;
        canvas.drawBitmap(titleBitmap, titleX, titleY, pixelPaint);

        float margin = screenWidth * UI_MARGIN_HORIZONTAL_PERCENT;
        if (settingsButtonBitmap != null) {
            float settingsBtnX = screenWidth - settingsButtonBitmap.getWidth() - margin;
            float settingsBtnY = systemBarTop + margin;
            settingsButtonRect.set((int) settingsBtnX, (int) settingsBtnY, (int) (settingsBtnX + settingsButtonBitmap.getWidth()), (int) (settingsBtnY + settingsButtonBitmap.getHeight()));
            if (!settingHideSettingsIcon) {
                settingsButtonDestRect.set(settingsButtonRect);
                if (pressedButtonRect == settingsButtonRect) settingsButtonDestRect.offset(0, pressOffsetY);
                canvas.drawBitmap(settingsButtonBitmap, null, settingsButtonDestRect, pixelPaint);
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
                canvas.drawBitmap(currentMedalBitmap, null, medalDestRect, pixelPaint);
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
                    settingsButtonDestRect.set(settingsButtonRect);
                    if (pressedButtonRect == settingsButtonRect) {
                        settingsButtonDestRect.offset(0, pressOffsetY);
                    }
                    canvas.drawBitmap(settingsButtonBitmap, null, settingsButtonDestRect, pixelPaint);
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
        unscaledMedalsBitmaps[0] = extract(atlas, loc(0.23632812, ATLAS_WIDTH), loc(0.50390625, ATLAS_HEIGHT), 44, 44);
        unscaledMedalsBitmaps[1] = extract(atlas, loc(0.23632812, ATLAS_WIDTH), loc(0.55078125, ATLAS_HEIGHT), 44, 44);
        unscaledMedalsBitmaps[2] = extract(atlas, loc(0.21875, ATLAS_WIDTH), loc(0.8847656, ATLAS_HEIGHT), 44, 44);
        unscaledMedalsBitmaps[3] = extract(atlas, loc(0.21875, ATLAS_WIDTH), loc(0.9316406, ATLAS_HEIGHT), 44, 44);
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
        soundPool = new SoundPool.Builder().setMaxStreams(5).setAudioAttributes(aa).build();
        soundWing = soundPool.load(getContext(), R.raw.wing, 1); soundPoint = soundPool.load(getContext(), R.raw.point, 1);
        soundHit = soundPool.load(getContext(), R.raw.hit, 1);
        soundDie = soundPool.load(getContext(), R.raw.die, 1);
        soundSwooshing = soundPool.load(getContext(), R.raw.swooshing, 1);
    }
    private void playSound(int soundID) {
        if (!settingSoundEnabled) return;
        if (soundPool != null && soundID != 0 && soundExecutor != null && !soundExecutor.isShutdown()) {
            soundExecutor.submit(() -> soundPool.play(soundID, 1, 1, 0, 0, 1));
        }
    }
    public void pause() { isRunning = false; choreographer.removeFrameCallback(this);
    }

    public void resume() {
        if (!isRunning && isReady) {
            loadSettings();
            if (gameState == GameState.HOME) {
                resetGame();
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
}

class Pipe {
    float x; int topPipeY; boolean isScored; int width, height;
    private static Random random = new Random();
    
    private Rect topHeadRect = new Rect(), topBodyRect = new Rect();
    private Rect bottomHeadRect = new Rect(), bottomBodyRect = new Rect();

    private static float pipeHeadWidth, pipeHeadHeight;
    private static float pipeBodyWidth, pipeBodyOffsetX;
    private float currentYOffset = 0;
    public Pipe(float x, int width, int height) { this.x = x; this.width = width;
    this.height = height; this.isScored = false; }

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

    public Rect getTopHeadRect() {
        float top = getCurrentTopPipeY() - pipeHeadHeight;
        float bottom = getCurrentTopPipeY();
        topHeadRect.set((int) x, (int) top, (int) (x + pipeHeadWidth), (int) bottom);
        return topHeadRect;
    }

    public Rect getTopBodyRect() {
        float top = getTopPipeY();
        float bottom = getCurrentTopPipeY() - pipeHeadHeight;
        topBodyRect.set(
            (int) (x + pipeBodyOffsetX), 
            (int) top, 
            (int) (x + pipeBodyOffsetX + pipeBodyWidth), 
            (int) bottom
        );
        return topBodyRect;
    }

    public Rect getBottomHeadRect(int pipeGap) {
        float top = getBottomPipeY(pipeGap);
        float bottom = top + pipeHeadHeight;
        bottomHeadRect.set((int) x, (int) top, (int) (x + pipeHeadWidth), (int) bottom);
        return bottomHeadRect;
    }

    public Rect getBottomBodyRect(int pipeGap) {
        float top = getBottomPipeY(pipeGap) + pipeHeadHeight;
        float bottom = top + (this.height - pipeHeadHeight);
        bottomBodyRect.set(
            (int) (x + pipeBodyOffsetX), 
            (int) top, 
            (int) (x + pipeBodyOffsetX + pipeBodyWidth), 
            (int) bottom
        );
        return bottomBodyRect;
    }
}