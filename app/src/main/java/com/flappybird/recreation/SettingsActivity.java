package com.flappybird.recreation;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Choreographer;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.materialswitch.MaterialSwitch;

public class SettingsActivity extends AppCompatActivity {

    // --- SharedPreferences Keys (public static for MainActivity access) ---
    public static final String PREFS_NAME = "FlappyBirdPrefs";
    // Visuals
    public static final String PREF_BIRD_COLOR = "pref_bird_color";        // int: -1=Random, 0=Yellow, 1=Blue, 2=Red
    public static final String PREF_BACKGROUND = "pref_background";      // int: -1=Random, 0=Day, 1=Night
    public static final String PREF_SOUND_ENABLED = "pref_sound_enabled";  // boolean: true/false
    public static final String PREF_WING_ANIMATION_ENABLED = "pref_wing_animation_enabled"; // boolean
    public static final String PREF_PIPE_COLOR = "pref_pipe_color"; // int: 0=Default, 1=Red, 2=Blue, 3=Yellow, 4=White, 5=Pink, 6=Black, 7=Purple, 8=Orange
    public static final String PREF_HIDE_SETTINGS_ICON = "pref_hide_settings_icon"; // boolean
    public static final String PREF_RAINBOW_BIRD_ENABLED = "pref_rainbow_bird_enabled";  // boolean
    // Gameplay
    public static final String PREF_GAMEOVER_OPACITY = "pref_gameover_opacity_percent"; // int: 0-100
    public static final String PREF_GAME_SPEED = "pref_game_speed";        // float: multiplier, e.g., 1.0
    public static final String PREF_GRAVITY = "pref_gravity";              // float: multiplier, e.g., 1.0
    public static final String PREF_JUMP_STRENGTH = "pref_jump_strength";  // float: multiplier, e.g., 1.0
    public static final String PREF_BIRD_HANG_DELAY = "pref_bird_hang_delay"; // float: multiplier, e.g., 1.0
    public static final String PREF_PIPE_GAP = "pref_pipe_gap";            // float: multiplier, e.g., 1.0
    public static final String PREF_PIPE_SPACING = "pref_pipe_spacing";      // float: multiplier, e.g., 1.0
    public static final String PREF_BIRD_HITBOX = "pref_bird_hitbox";        // float: multiplier, e.g., 1.0
    public static final String PREF_PIPE_VARIATION = "pref_pipe_variation";          // float: multiplier, e.g. 1.0
    public static final String PREF_SCORE_MULTIPLIER = "pref_score_multiplier";        // int
    // Pipe Movement
    public static final String PREF_MOVING_PIPES_ENABLED = "pref_moving_pipes_enabled"; // boolean
    public static final String PREF_PIPE_MOVE_TIER_1_SCORE = "pref_pipe_move_tier_1";   // int
    public static final String PREF_PIPE_MOVE_TIER_2_SCORE = "pref_pipe_move_tier_2";   // int
    // Cheats & Fun
    public static final String PREF_NO_CLIP_ENABLED = "pref_no_clip_enabled"; // boolean
    public static final String PREF_UPSIDE_DOWN_ENABLED = "pref_upside_down_enabled";    // boolean
    public static final String PREF_REVERSE_PIPES_ENABLED = "pref_reverse_pipes_enabled"; // boolean
    public static final String PREF_SETTINGS_DISCLAIMER_SHOWN = "pref_settings_disclaimer_shown"; // boolean

    // --- Default Values ---
    public static final int DEFAULT_BIRD_COLOR = -1; // Random
    public static final int DEFAULT_BACKGROUND = -1; // Random
    public static final boolean DEFAULT_SOUND_ENABLED = true;
    public static final boolean DEFAULT_WING_ANIMATION_ENABLED = true;
    public static final int DEFAULT_PIPE_COLOR = 0; // Default (Green)
    public static final boolean DEFAULT_HIDE_SETTINGS_ICON = false;
    public static final boolean DEFAULT_RAINBOW_BIRD_ENABLED = false;
    public static final int DEFAULT_GAMEOVER_OPACITY = 5; // 5%
    public static final float DEFAULT_GAME_SPEED = 1.0f;
    public static final float DEFAULT_GRAVITY = 1.0f;
    public static final float DEFAULT_JUMP_STRENGTH = 1.0f;
    public static final float DEFAULT_BIRD_HANG_DELAY = 1.0f;
    public static final float DEFAULT_PIPE_GAP = 1.0f;
    public static final float DEFAULT_PIPE_SPACING = 1.0f;
    public static final float DEFAULT_BIRD_HITBOX = 1.0f;
    public static final float DEFAULT_PIPE_VARIATION = 1.0f;
    public static final int DEFAULT_SCORE_MULTIPLIER = 1;
    public static final boolean DEFAULT_MOVING_PIPES_ENABLED = true;
    public static final int DEFAULT_PIPE_MOVE_TIER_1_SCORE = 2000;
    public static final int DEFAULT_PIPE_MOVE_TIER_2_SCORE = 3000;
    public static final boolean DEFAULT_NO_CLIP_ENABLED = false;
    public static final boolean DEFAULT_UPSIDE_DOWN_ENABLED = false;
    public static final boolean DEFAULT_REVERSE_PIPES_ENABLED = false;

    private SettingsBackgroundView backgroundView;
    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;

    // --- UI Elements ---
    private RadioGroup birdColorGroup, backgroundGroup, pipeColorGroup;
    private MaterialSwitch soundSwitch, noClipSwitch, wingAnimationSwitch, movingPipesSwitch, hideSettingsIconSwitch;
    private MaterialSwitch rainbowBirdSwitch, upsideDownSwitch, reversePipesSwitch;
    private EditText opacityEditText, speedEditText, gravityEditText, jumpEditText, pipeGapEditText, pipeMoveTier1EditText, pipeMoveTier2EditText, hangDelayEditText;
    private EditText pipeSpacingEditText, birdHitboxEditText, pipeVariationEditText, scoreMultiplierEditText;
    private Button backButton, resetButton;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        editor = prefs.edit();

        if (!prefs.getBoolean(PREF_SETTINGS_DISCLAIMER_SHOWN, false)) {
            showDisclaimerDialog();
        }

        // --- Fullscreen Setup ---
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        WindowInsetsControllerCompat windowInsetsController =
                WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
        windowInsetsController.setSystemBarsBehavior(
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        );
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars());

        setContentView(R.layout.settings);

        FrameLayout container = findViewById(R.id.settings_container);

        // --- Add Animated Background ---
        backgroundView = new SettingsBackgroundView(this);
        container.addView(backgroundView, 0);

        initViews();
        loadSettings();
        setupListeners();
    }

    private void showDisclaimerDialog() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Settings Disclaimer")
                .setMessage("Adjusting these settings, especially the gameplay values, can potentially break the game, cause unexpected behavior, or make it unstable. Proceed with caution.")
                .setPositiveButton("I Understand", (dialog, which) -> {
                    prefs.edit().putBoolean(PREF_SETTINGS_DISCLAIMER_SHOWN, true).apply();
                    dialog.dismiss();
                })
                .setCancelable(false)
                .show();
    }

    private void initViews() {
        backButton = findViewById(R.id.button_back);
        resetButton = findViewById(R.id.button_reset);
        birdColorGroup = findViewById(R.id.rg_bird_color);
        backgroundGroup = findViewById(R.id.rg_background);
        soundSwitch = findViewById(R.id.switch_sound);
        opacityEditText = findViewById(R.id.et_opacity);
        speedEditText = findViewById(R.id.et_game_speed);
        gravityEditText = findViewById(R.id.et_gravity);
        jumpEditText = findViewById(R.id.et_jump);
        pipeGapEditText = findViewById(R.id.et_pipe_gap);
        hangDelayEditText = findViewById(R.id.et_hang_delay);
        pipeSpacingEditText = findViewById(R.id.et_pipe_spacing);
        birdHitboxEditText = findViewById(R.id.et_bird_hitbox);

        // New UI Elements
        noClipSwitch = findViewById(R.id.switch_no_clip);
        wingAnimationSwitch = findViewById(R.id.switch_wing_animation);
        pipeColorGroup = findViewById(R.id.rg_pipe_color);
        movingPipesSwitch = findViewById(R.id.switch_moving_pipes);
        pipeMoveTier1EditText = findViewById(R.id.et_pipe_move_tier1);
        pipeMoveTier2EditText = findViewById(R.id.et_pipe_move_tier2);
        hideSettingsIconSwitch = findViewById(R.id.switch_hide_settings_icon);

        // More New UI Elements
        rainbowBirdSwitch = findViewById(R.id.switch_rainbow_bird);
        upsideDownSwitch = findViewById(R.id.switch_upside_down);
        reversePipesSwitch = findViewById(R.id.switch_reverse_pipes);
        pipeVariationEditText = findViewById(R.id.et_pipe_variation);
        scoreMultiplierEditText = findViewById(R.id.et_score_multiplier);
    }

    private void loadSettings() {
        // --- Bird Color ---
        int birdColor = prefs.getInt(PREF_BIRD_COLOR, DEFAULT_BIRD_COLOR);
        switch (birdColor) {
            case 0: birdColorGroup.check(R.id.rb_bird_yellow); break;
            case 1: birdColorGroup.check(R.id.rb_bird_blue); break;
            case 2: birdColorGroup.check(R.id.rb_bird_red); break;
            default: birdColorGroup.check(R.id.rb_bird_random); break;
        }

        // --- Background ---
        int background = prefs.getInt(PREF_BACKGROUND, DEFAULT_BACKGROUND);
        if (background == 0) {
            backgroundGroup.check(R.id.rb_bg_day);
        } else if (background == 1) {
            backgroundGroup.check(R.id.rb_bg_night);
        } else {
            backgroundGroup.check(R.id.rb_bg_random);
        }
        if (backgroundView != null) {
            backgroundView.setBackgroundStyle(background);
        }

        // --- Switches ---
        soundSwitch.setChecked(prefs.getBoolean(PREF_SOUND_ENABLED, DEFAULT_SOUND_ENABLED));
        noClipSwitch.setChecked(prefs.getBoolean(PREF_NO_CLIP_ENABLED, DEFAULT_NO_CLIP_ENABLED));
        wingAnimationSwitch.setChecked(prefs.getBoolean(PREF_WING_ANIMATION_ENABLED, DEFAULT_WING_ANIMATION_ENABLED));
        movingPipesSwitch.setChecked(prefs.getBoolean(PREF_MOVING_PIPES_ENABLED, DEFAULT_MOVING_PIPES_ENABLED));
        hideSettingsIconSwitch.setChecked(prefs.getBoolean(PREF_HIDE_SETTINGS_ICON, DEFAULT_HIDE_SETTINGS_ICON));
        rainbowBirdSwitch.setChecked(prefs.getBoolean(PREF_RAINBOW_BIRD_ENABLED, DEFAULT_RAINBOW_BIRD_ENABLED));
        upsideDownSwitch.setChecked(prefs.getBoolean(PREF_UPSIDE_DOWN_ENABLED, DEFAULT_UPSIDE_DOWN_ENABLED));
        reversePipesSwitch.setChecked(prefs.getBoolean(PREF_REVERSE_PIPES_ENABLED, DEFAULT_REVERSE_PIPES_ENABLED));

        // --- Pipe Color ---
        int pipeColor = prefs.getInt(PREF_PIPE_COLOR, DEFAULT_PIPE_COLOR);
        switch (pipeColor) {
            case 1: pipeColorGroup.check(R.id.rb_pipe_red); break;
            case 2: pipeColorGroup.check(R.id.rb_pipe_blue); break;
            case 3: pipeColorGroup.check(R.id.rb_pipe_yellow); break;
            case 4: pipeColorGroup.check(R.id.rb_pipe_white); break;
            case 5: pipeColorGroup.check(R.id.rb_pipe_pink); break;
            case 6: pipeColorGroup.check(R.id.rb_pipe_black); break;
            case 7: pipeColorGroup.check(R.id.rb_pipe_purple); break;
            case 8: pipeColorGroup.check(R.id.rb_pipe_orange); break;
            default: pipeColorGroup.check(R.id.rb_pipe_default); break;
        }

        // --- EditText values ---
        opacityEditText.setText(String.valueOf(prefs.getInt(PREF_GAMEOVER_OPACITY, DEFAULT_GAMEOVER_OPACITY)));

        // --- Game Speed (and update background animation) ---
        float gameSpeed = prefs.getFloat(PREF_GAME_SPEED, DEFAULT_GAME_SPEED);
        speedEditText.setText(String.valueOf(gameSpeed));
        if (backgroundView != null) {
            backgroundView.setSpeedMultiplier(gameSpeed);
        }

        gravityEditText.setText(String.valueOf(prefs.getFloat(PREF_GRAVITY, DEFAULT_GRAVITY)));
        jumpEditText.setText(String.valueOf(prefs.getFloat(PREF_JUMP_STRENGTH, DEFAULT_JUMP_STRENGTH)));
        hangDelayEditText.setText(String.valueOf(prefs.getFloat(PREF_BIRD_HANG_DELAY, DEFAULT_BIRD_HANG_DELAY)));
        pipeGapEditText.setText(String.valueOf(prefs.getFloat(PREF_PIPE_GAP, DEFAULT_PIPE_GAP)));
        pipeSpacingEditText.setText(String.valueOf(prefs.getFloat(PREF_PIPE_SPACING, DEFAULT_PIPE_SPACING)));
        birdHitboxEditText.setText(String.valueOf(prefs.getFloat(PREF_BIRD_HITBOX, DEFAULT_BIRD_HITBOX)));
        pipeVariationEditText.setText(String.valueOf(prefs.getFloat(PREF_PIPE_VARIATION, DEFAULT_PIPE_VARIATION)));
        scoreMultiplierEditText.setText(String.valueOf(prefs.getInt(PREF_SCORE_MULTIPLIER, DEFAULT_SCORE_MULTIPLIER)));

        // --- Pipe Movement Scores ---
        pipeMoveTier1EditText.setText(String.valueOf(prefs.getInt(PREF_PIPE_MOVE_TIER_1_SCORE, DEFAULT_PIPE_MOVE_TIER_1_SCORE)));
        pipeMoveTier2EditText.setText(String.valueOf(prefs.getInt(PREF_PIPE_MOVE_TIER_2_SCORE, DEFAULT_PIPE_MOVE_TIER_2_SCORE)));
    }

    private void setupListeners() {
        backButton.setOnClickListener(v -> finish());

        resetButton.setOnClickListener(v -> {
            // Visuals
            editor.putInt(PREF_BIRD_COLOR, DEFAULT_BIRD_COLOR);
            editor.putInt(PREF_BACKGROUND, DEFAULT_BACKGROUND);
            editor.putBoolean(PREF_SOUND_ENABLED, DEFAULT_SOUND_ENABLED);
            editor.putBoolean(PREF_WING_ANIMATION_ENABLED, DEFAULT_WING_ANIMATION_ENABLED);
            editor.putInt(PREF_PIPE_COLOR, DEFAULT_PIPE_COLOR);
            editor.putBoolean(PREF_HIDE_SETTINGS_ICON, DEFAULT_HIDE_SETTINGS_ICON);
            editor.putBoolean(PREF_RAINBOW_BIRD_ENABLED, DEFAULT_RAINBOW_BIRD_ENABLED);
            // Gameplay
            editor.putInt(PREF_GAMEOVER_OPACITY, DEFAULT_GAMEOVER_OPACITY);
            editor.putFloat(PREF_GAME_SPEED, DEFAULT_GAME_SPEED);
            editor.putFloat(PREF_GRAVITY, DEFAULT_GRAVITY);
            editor.putFloat(PREF_JUMP_STRENGTH, DEFAULT_JUMP_STRENGTH);
            editor.putFloat(PREF_BIRD_HANG_DELAY, DEFAULT_BIRD_HANG_DELAY);
            editor.putFloat(PREF_PIPE_GAP, DEFAULT_PIPE_GAP);
            editor.putFloat(PREF_PIPE_SPACING, DEFAULT_PIPE_SPACING);
            editor.putFloat(PREF_BIRD_HITBOX, DEFAULT_BIRD_HITBOX);
            editor.putFloat(PREF_PIPE_VARIATION, DEFAULT_PIPE_VARIATION);
            editor.putInt(PREF_SCORE_MULTIPLIER, DEFAULT_SCORE_MULTIPLIER);
            // Pipe Movement
            editor.putBoolean(PREF_MOVING_PIPES_ENABLED, DEFAULT_MOVING_PIPES_ENABLED);
            editor.putInt(PREF_PIPE_MOVE_TIER_1_SCORE, DEFAULT_PIPE_MOVE_TIER_1_SCORE);
            editor.putInt(PREF_PIPE_MOVE_TIER_2_SCORE, DEFAULT_PIPE_MOVE_TIER_2_SCORE);
            // Cheats & Fun
            editor.putBoolean(PREF_NO_CLIP_ENABLED, DEFAULT_NO_CLIP_ENABLED);
            editor.putBoolean(PREF_UPSIDE_DOWN_ENABLED, DEFAULT_UPSIDE_DOWN_ENABLED);
            editor.putBoolean(PREF_REVERSE_PIPES_ENABLED, DEFAULT_REVERSE_PIPES_ENABLED);

            editor.apply();
            loadSettings();
        });

        birdColorGroup.setOnCheckedChangeListener((group, checkedId) -> {
            int selection;
            if (checkedId == R.id.rb_bird_yellow) {
                selection = 0;
            } else if (checkedId == R.id.rb_bird_blue) {
                selection = 1;
            } else if (checkedId == R.id.rb_bird_red) {
                selection = 2;
            } else {
                selection = -1;
            }
            editor.putInt(PREF_BIRD_COLOR, selection).apply();
        });

        backgroundGroup.setOnCheckedChangeListener((group, checkedId) -> {
            int selection;
            if (checkedId == R.id.rb_bg_day) {
                selection = 0;
            } else if (checkedId == R.id.rb_bg_night) {
                selection = 1;
            } else {
                selection = -1;
            }
            editor.putInt(PREF_BACKGROUND, selection).apply();
            if (backgroundView != null) {
                backgroundView.setBackgroundStyle(selection);
            }
        });

        pipeColorGroup.setOnCheckedChangeListener((group, checkedId) -> {
            int selection;
            if (checkedId == R.id.rb_pipe_red) selection = 1;
            else if (checkedId == R.id.rb_pipe_blue) selection = 2;
            else if (checkedId == R.id.rb_pipe_yellow) selection = 3;
            else if (checkedId == R.id.rb_pipe_white) selection = 4;
            else if (checkedId == R.id.rb_pipe_pink) selection = 5;
            else if (checkedId == R.id.rb_pipe_black) selection = 6;
            else if (checkedId == R.id.rb_pipe_purple) selection = 7;
            else if (checkedId == R.id.rb_pipe_orange) selection = 8;
            else selection = 0; // Default
            editor.putInt(PREF_PIPE_COLOR, selection).apply();
        });

        // --- Switch Listeners ---
        soundSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> editor.putBoolean(PREF_SOUND_ENABLED, isChecked).apply());
        noClipSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> editor.putBoolean(PREF_NO_CLIP_ENABLED, isChecked).apply());
        wingAnimationSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> editor.putBoolean(PREF_WING_ANIMATION_ENABLED, isChecked).apply());
        movingPipesSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> editor.putBoolean(PREF_MOVING_PIPES_ENABLED, isChecked).apply());
        hideSettingsIconSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> editor.putBoolean(PREF_HIDE_SETTINGS_ICON, isChecked).apply());
        rainbowBirdSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> editor.putBoolean(PREF_RAINBOW_BIRD_ENABLED, isChecked).apply());
        upsideDownSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> editor.putBoolean(PREF_UPSIDE_DOWN_ENABLED, isChecked).apply());
        reversePipesSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> editor.putBoolean(PREF_REVERSE_PIPES_ENABLED, isChecked).apply());

        // Specific listener for Game Speed to update animation in real-time
        speedEditText.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) {
                try {
                    String valueStr = s.toString();
                    if (valueStr.isEmpty() || valueStr.equals(".")) {
                        return; // User is still typing
                    }
                    float value = Float.parseFloat(valueStr);
                    editor.putFloat(PREF_GAME_SPEED, value).apply();
                    if (backgroundView != null) {
                        backgroundView.setSpeedMultiplier(value);
                    }
                } catch (NumberFormatException e) {
                    // Ignore invalid formats during typing
                }
            }
        });


        // Generic listeners for other EditText fields
        setupIntEditTextListener(opacityEditText, PREF_GAMEOVER_OPACITY, 100);
        setupFloatEditTextListener(gravityEditText, PREF_GRAVITY);
        setupFloatEditTextListener(jumpEditText, PREF_JUMP_STRENGTH);
        setupFloatEditTextListener(hangDelayEditText, PREF_BIRD_HANG_DELAY);
        setupFloatEditTextListener(pipeGapEditText, PREF_PIPE_GAP);
        setupFloatEditTextListener(pipeSpacingEditText, PREF_PIPE_SPACING);
        setupFloatEditTextListener(birdHitboxEditText, PREF_BIRD_HITBOX);
        setupIntEditTextListener(pipeMoveTier1EditText, PREF_PIPE_MOVE_TIER_1_SCORE, 99999);
        setupIntEditTextListener(pipeMoveTier2EditText, PREF_PIPE_MOVE_TIER_2_SCORE, 99999);
        setupFloatEditTextListener(pipeVariationEditText, PREF_PIPE_VARIATION);
        setupIntEditTextListener(scoreMultiplierEditText, PREF_SCORE_MULTIPLIER, 999);
    }

    private void setupIntEditTextListener(EditText editText, String prefKey, int maxValue) {
        editText.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) {
                try {
                    String valueStr = s.toString();
                    if (valueStr.isEmpty()) return;
                    int value = Integer.parseInt(valueStr);
                    if (value >= 0 && value <= maxValue) {
                        editor.putInt(prefKey, value).apply();
                        editText.setError(null);
                    } else {
                        editText.setError("Value must be 0-" + maxValue);
                    }
                } catch (NumberFormatException e) {
                    // Ignore
                }
            }
        });
    }

    private void setupFloatEditTextListener(EditText editText, String prefKey) {
        editText.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) {
                try {
                    String valueStr = s.toString();
                    if (valueStr.isEmpty() || valueStr.equals(".")) return;
                    float value = Float.parseFloat(valueStr);
                    editor.putFloat(prefKey, value).apply();
                } catch (NumberFormatException e) {
                    // Ignore
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (backgroundView != null) {
            backgroundView.resume();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (backgroundView != null) {
            backgroundView.pause();
        }
    }

    // --- INNER CLASS: SettingsBackgroundView (Corrected Logic) ---
    static class SettingsBackgroundView extends View implements Choreographer.FrameCallback {

        private Choreographer choreographer;
        private boolean isRunning = false;
        private long lastFrameTimeNanos = 0;

        private int screenWidth, screenHeight;

        private Bitmap bgDayBitmap, bgNightBitmap, groundBitmap, currentBgBitmap;
        private float groundX = 0, backgroundX = 0;
        private int groundHeight;

        private float basePipeSpeed;
        private float speedMultiplier = 1.0f;

        private Paint pixelPaint = new Paint();
        private RectF groundDestRect = new RectF();

        // Caches the user's choice (-2 = uninitialized)
        private int backgroundStyle = -2;
        private boolean isReady = false;

        public SettingsBackgroundView(Context context) {
            super(context);
            init();
        }

        private void init() {
            pixelPaint.setFilterBitmap(false);
            pixelPaint.setAntiAlias(false);
            choreographer = Choreographer.getInstance();
        }


        @Override
        protected void onSizeChanged(int w, int h, int oldw, int oldh) {
            super.onSizeChanged(w, h, oldw, oldh);
            if (w == 0 || h == 0) return;

            screenWidth = w;
            screenHeight = h;

            // Load unscaled bitmaps from the atlas. This is crucial for handling screen rotation correctly,
            // as it prevents re-scaling an already scaled bitmap.
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inScaled = false;
            Bitmap atlas = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.atlas, options);
            Bitmap unscaledBgDay = Bitmap.createBitmap(atlas, 0, 0, 288, 512);
            Bitmap unscaledBgNight = Bitmap.createBitmap(atlas, (int)(0.28515625 * 1024), 0, 288, 512);
            Bitmap unscaledLand = Bitmap.createBitmap(atlas, (int)(0.5703125 * 1024), 0, 336, 112);
            atlas.recycle();

            float scale = (float) screenHeight / unscaledBgDay.getHeight();
            int bgWidth = (int) (unscaledBgDay.getWidth() * scale);

            // Scale the local bitmaps into the class member bitmaps
            bgDayBitmap = Bitmap.createScaledBitmap(unscaledBgDay, bgWidth, screenHeight, false);
            bgNightBitmap = Bitmap.createScaledBitmap(unscaledBgNight, bgWidth, screenHeight, false);
            groundBitmap = Bitmap.createScaledBitmap(unscaledLand, (int)(unscaledLand.getWidth() * scale), (int)(unscaledLand.getHeight() * scale), false);
            groundHeight = groundBitmap.getHeight();

            basePipeSpeed = (1.1f * scale) * 120.0f;
            isReady = true;

            // Now that the view is ready and bitmaps are scaled, apply the stored style.
            applyBackgroundStyle();
            resume();
        }

        /**
         * Public method to be called from the Activity. It saves the user's choice and
         * attempts to apply it.
         */
        public void setBackgroundStyle(int style) {
            this.backgroundStyle = style;
            applyBackgroundStyle();
        }

        /**
         * Private method that does the actual work of setting the background. It will only
         * succeed if the view is ready (onSizeChanged has run).
         */
        private void applyBackgroundStyle() {
            // If onSizeChanged has not run yet, just return. This method will be called
            // again from onSizeChanged when ready.
            if (!isReady) {
                return;
            }

            switch (this.backgroundStyle) {
                case 0: // Day
                    currentBgBitmap = bgDayBitmap;
                    break;
                case 1: // Night
                    currentBgBitmap = bgNightBitmap;
                    break;
                case -1: // Random
                default: // Also handles uninitialized case
                    // Only re-randomize if the background hasn't been set yet for this instance.
                    // This prevents the background from changing on every screen rotation.
                    if (currentBgBitmap == null) {
                        currentBgBitmap = Math.random() > 0.5 ? bgDayBitmap : bgNightBitmap;
                    }
                    break;
            }
            invalidate();
        }

        public void setSpeedMultiplier(float multiplier) {
            this.speedMultiplier = Math.max(0, multiplier);
        }

        @Override
        public void doFrame(long frameTimeNanos) {
            if (!isRunning) return;

            if (lastFrameTimeNanos == 0) {
                lastFrameTimeNanos = frameTimeNanos;
                choreographer.postFrameCallback(this);
                return;
            }

            float deltaTime = (frameTimeNanos - lastFrameTimeNanos) / 1_000_000_000.0f;
            lastFrameTimeNanos = frameTimeNanos;

            if (deltaTime > (1.0f / 30.0f)) {
                deltaTime = (1.0f / 30.0f);
            }

            // Safety check
            if (currentBgBitmap == null) {
                choreographer.postFrameCallback(this);
                return;
            }

            float effectiveSpeed = basePipeSpeed * speedMultiplier;

            backgroundX -= (effectiveSpeed / 2) * deltaTime;
            if (backgroundX <= -currentBgBitmap.getWidth()) {
                backgroundX += currentBgBitmap.getWidth();
            }
            groundX -= effectiveSpeed * deltaTime;
            if (groundX <= -groundBitmap.getWidth()) {
                groundX += groundBitmap.getWidth();
            }

            invalidate();
            choreographer.postFrameCallback(this);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            if (canvas == null || currentBgBitmap == null || groundBitmap == null) return;

            for (float x = backgroundX % currentBgBitmap.getWidth(); x < screenWidth; x += currentBgBitmap.getWidth()) {
                canvas.drawBitmap(currentBgBitmap, x, 0, pixelPaint);
            }

            float groundTopY = screenHeight - groundHeight;
            for (float x = groundX % groundBitmap.getWidth(); x < screenWidth; x += groundBitmap.getWidth()) {
                groundDestRect.set(x, groundTopY, x + groundBitmap.getWidth(), screenHeight);
                canvas.drawBitmap(groundBitmap, null, groundDestRect, pixelPaint);
            }
        }

        public void resume() {
            if (!isRunning) {
                isRunning = true;
                lastFrameTimeNanos = 0;
                choreographer.postFrameCallback(this);
            }
        }

        public void pause() {
            isRunning = false;
            choreographer.removeFrameCallback(this);
        }

        @Override
        protected void onDetachedFromWindow() {
            super.onDetachedFromWindow();
            pause();
            isReady = false; // View is detached, needs to be setup again if re-attached
        }
    }
}