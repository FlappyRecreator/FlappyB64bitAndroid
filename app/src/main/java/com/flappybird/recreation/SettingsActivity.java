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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.slider.Slider;

import java.util.Locale;

public class SettingsActivity extends AppCompatActivity {

    public static final String PREFS_NAME = "FlappyBirdPrefs";
    public static final String PREF_BIRD_COLOR = "pref_bird_color";
    public static final String PREF_BACKGROUND = "pref_background";
    public static final String PREF_SOUND_ENABLED = "pref_sound_enabled";
    public static final String PREF_WING_ANIMATION_ENABLED = "pref_wing_animation_enabled";
    public static final String PREF_PIPE_COLOR = "pref_pipe_color";
    public static final String PREF_HIDE_SETTINGS_ICON = "pref_hide_settings_icon";
    public static final String PREF_RAINBOW_BIRD_ENABLED = "pref_rainbow_bird_enabled";
    public static final String PREF_GAMEOVER_OPACITY = "pref_gameover_opacity_percent";
    public static final String PREF_GAME_SPEED = "pref_game_speed";
    public static final String PREF_GRAVITY = "pref_gravity";
    public static final String PREF_JUMP_STRENGTH = "pref_jump_strength";
    public static final String PREF_BIRD_HANG_DELAY = "pref_bird_hang_delay";
    public static final String PREF_PIPE_GAP = "pref_pipe_gap";
    public static final String PREF_PIPE_SPACING = "pref_pipe_spacing";
    public static final String PREF_BIRD_HITBOX = "pref_bird_hitbox";
    public static final String PREF_PIPE_VARIATION = "pref_pipe_variation";
    public static final String PREF_SCORE_MULTIPLIER = "pref_score_multiplier";
    public static final String PREF_MOVING_PIPES_ENABLED = "pref_moving_pipes_enabled";
    public static final String PREF_PIPE_MOVE_TIER_1_SCORE = "pref_pipe_move_tier_1";
    public static final String PREF_PIPE_MOVE_TIER_2_SCORE = "pref_pipe_move_tier_2";
    public static final String PREF_NO_CLIP_ENABLED = "pref_no_clip_enabled";
    public static final String PREF_UPSIDE_DOWN_ENABLED = "pref_upside_down_enabled";
    public static final String PREF_REVERSE_PIPES_ENABLED = "pref_reverse_pipes_enabled";
    public static final String PREF_SETTINGS_DISCLAIMER_SHOWN = "pref_settings_disclaimer_shown";
    public static final String PREF_HAPTIC_FEEDBACK_ENABLED = "pref_haptic_feedback_enabled";
    public static final String PREF_BIRD_TRAIL_ENABLED = "pref_bird_trail_enabled";
    public static final String PREF_GHOST_MODE_ENABLED = "pref_ghost_mode_enabled";
    public static final String PREF_PIPE_SPEED_VARIATION = "pref_pipe_speed_variation";
    public static final String PREF_BIRD_SIZE = "pref_bird_size";
    public static final String PREF_PIPE_WIDTH = "pref_pipe_width";
    public static final String PREF_BG_SCROLL_SPEED = "pref_bg_scroll_speed";
    public static final String PREF_GROUND_SCROLL_SPEED = "pref_ground_scroll_speed";
    public static final String PREF_RANDOM_PIPE_COLORS_ENABLED = "pref_random_pipe_colors_enabled";
    public static final String PREF_INFINITE_FLAP_ENABLED = "pref_infinite_flap_enabled";

    public static final int DEFAULT_BIRD_COLOR = -1;
    public static final int DEFAULT_BACKGROUND = -1;
    public static final boolean DEFAULT_SOUND_ENABLED = true;
    public static final boolean DEFAULT_WING_ANIMATION_ENABLED = true;
    public static final int DEFAULT_PIPE_COLOR = 0;
    public static final boolean DEFAULT_HIDE_SETTINGS_ICON = false;
    public static final boolean DEFAULT_RAINBOW_BIRD_ENABLED = false;
    public static final int DEFAULT_GAMEOVER_OPACITY = 5;
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
    public static final boolean DEFAULT_HAPTIC_FEEDBACK_ENABLED = true;
    public static final boolean DEFAULT_BIRD_TRAIL_ENABLED = false;
    public static final boolean DEFAULT_GHOST_MODE_ENABLED = false;
    public static final float DEFAULT_PIPE_SPEED_VARIATION = 0.0f;
    public static final float DEFAULT_BIRD_SIZE = 1.0f;
    public static final float DEFAULT_PIPE_WIDTH = 1.0f;
    public static final float DEFAULT_BG_SCROLL_SPEED = 0.5f;
    public static final float DEFAULT_GROUND_SCROLL_SPEED = 1.0f;
    public static final boolean DEFAULT_RANDOM_PIPE_COLORS_ENABLED = false;
    public static final boolean DEFAULT_INFINITE_FLAP_ENABLED = false;

    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;
    private SettingsBackgroundView backgroundView;
    private boolean isProgrammaticChange = false;

    private RadioGroup birdColorGroup, backgroundGroup, pipeColorGroup;
    private MaterialSwitch randomPipeColorsSwitch, movingPipesSwitch, noClipSwitch, infiniteFlapSwitch;
    private MaterialSwitch rainbowBirdSwitch, birdTrailSwitch, ghostModeSwitch, upsideDownSwitch;
    private MaterialSwitch reversePipesSwitch, soundSwitch, hapticFeedbackSwitch, wingAnimationSwitch, hideSettingsIconSwitch;
    private Slider gameSpeedSlider, bgScrollSlider, groundScrollSlider, birdSizeSlider, birdHitboxSlider;
    private Slider gravitySlider, jumpSlider, pipeGapSlider, pipeSpacingSlider, opacitySlider;
    private Slider hangDelaySlider, pipeWidthSlider, pipeVariationSlider, pipeSpeedVariationSlider;
    private EditText gameSpeedEdit, bgScrollEdit, groundScrollEdit, birdSizeEdit, birdHitboxEdit;
    private EditText gravityEdit, jumpEdit, pipeGapEdit, pipeSpacingEdit, opacityEdit;
    private EditText hangDelayEdit, pipeWidthEdit, pipeVariationEdit, pipeSpeedVariationEdit;
    private EditText pipeMoveTier1Edit, pipeMoveTier2Edit, scoreMultiplierEdit;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        WindowInsetsControllerCompat controller = WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
        if (controller != null) {
            controller.hide(WindowInsetsCompat.Type.systemBars());
            controller.setSystemBarsBehavior(WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
        }

        setContentView(R.layout.settings);

        FrameLayout rootContainer = findViewById(R.id.settings_root_container);
        backgroundView = new SettingsBackgroundView(this);
        rootContainer.addView(backgroundView, 0);

        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        editor = prefs.edit();

        if (!prefs.getBoolean(PREF_SETTINGS_DISCLAIMER_SHOWN, false)) {
            showDisclaimerDialog();
        }

        initViews();
        loadSettings();
        setupListeners();
    }

    private void showDisclaimerDialog() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Settings Disclaimer")
                .setMessage("Adjusting these settings, especially the gameplay values, can potentially break the game, cause unexpected behavior, or make it unstable. Proceed with caution.")
                .setPositiveButton("I Understand", (dialog, which) -> {
                    editor.putBoolean(PREF_SETTINGS_DISCLAIMER_SHOWN, true).apply();
                    dialog.dismiss();
                })
                .setCancelable(false)
                .show();
    }

    private void initViews() {
        findViewById(R.id.button_back).setOnClickListener(v -> finish());
        findViewById(R.id.button_reset).setOnClickListener(v -> resetToDefaults());

        setupExpandableSection(R.id.header_visuals, R.id.content_visuals, R.id.icon_visuals);
        setupExpandableSection(R.id.header_gameplay, R.id.content_gameplay, R.id.icon_gameplay);
        setupExpandableSection(R.id.header_cheats, R.id.content_cheats, R.id.icon_cheats);
        setupExpandableSection(R.id.header_system, R.id.content_system, R.id.icon_system);

        birdColorGroup = findViewById(R.id.rg_bird_color);
        backgroundGroup = findViewById(R.id.rg_background);
        pipeColorGroup = findViewById(R.id.rg_pipe_color);
        randomPipeColorsSwitch = findViewById(R.id.switch_random_pipe_colors);

        gameSpeedSlider = findViewById(R.id.slider_game_speed);
        gameSpeedEdit = findViewById(R.id.et_game_speed);
        bgScrollSlider = findViewById(R.id.slider_bg_scroll);
        bgScrollEdit = findViewById(R.id.et_bg_scroll);
        groundScrollSlider = findViewById(R.id.slider_ground_scroll);
        groundScrollEdit = findViewById(R.id.et_ground_scroll);
        birdSizeSlider = findViewById(R.id.slider_bird_size);
        birdSizeEdit = findViewById(R.id.et_bird_size);
        birdHitboxSlider = findViewById(R.id.slider_bird_hitbox);
        birdHitboxEdit = findViewById(R.id.et_bird_hitbox);
        gravitySlider = findViewById(R.id.slider_gravity);
        gravityEdit = findViewById(R.id.et_gravity);
        jumpSlider = findViewById(R.id.slider_jump);
        jumpEdit = findViewById(R.id.et_jump);
        pipeGapSlider = findViewById(R.id.slider_pipe_gap);
        pipeGapEdit = findViewById(R.id.et_pipe_gap);
        pipeSpacingSlider = findViewById(R.id.slider_pipe_spacing);
        pipeSpacingEdit = findViewById(R.id.et_pipe_spacing);
        hangDelaySlider = findViewById(R.id.slider_hang_delay);
        hangDelayEdit = findViewById(R.id.et_hang_delay);
        pipeWidthSlider = findViewById(R.id.slider_pipe_width);
        pipeWidthEdit = findViewById(R.id.et_pipe_width);
        pipeVariationSlider = findViewById(R.id.slider_pipe_variation);
        pipeVariationEdit = findViewById(R.id.et_pipe_variation);
        pipeSpeedVariationSlider = findViewById(R.id.slider_pipe_speed_variation);
        pipeSpeedVariationEdit = findViewById(R.id.et_pipe_speed_variation);
        pipeMoveTier1Edit = findViewById(R.id.et_pipe_move_tier1);
        pipeMoveTier2Edit = findViewById(R.id.et_pipe_move_tier2);
        scoreMultiplierEdit = findViewById(R.id.et_score_multiplier);

        movingPipesSwitch = findViewById(R.id.switch_moving_pipes);
        noClipSwitch = findViewById(R.id.switch_no_clip);
        infiniteFlapSwitch = findViewById(R.id.switch_infinite_flap);
        rainbowBirdSwitch = findViewById(R.id.switch_rainbow_bird);
        birdTrailSwitch = findViewById(R.id.switch_bird_trail);
        ghostModeSwitch = findViewById(R.id.switch_ghost_mode);
        upsideDownSwitch = findViewById(R.id.switch_upside_down);
        reversePipesSwitch = findViewById(R.id.switch_reverse_pipes);

        soundSwitch = findViewById(R.id.switch_sound);
        hapticFeedbackSwitch = findViewById(R.id.switch_haptic_feedback);
        wingAnimationSwitch = findViewById(R.id.switch_wing_animation);
        hideSettingsIconSwitch = findViewById(R.id.switch_hide_settings_icon);
        opacitySlider = findViewById(R.id.slider_opacity);
        opacityEdit = findViewById(R.id.et_opacity);
    }

    private void loadSettings() {
        isProgrammaticChange = true;
        loadRadioGroups();

        loadFloat(PREF_GAME_SPEED, DEFAULT_GAME_SPEED, gameSpeedEdit, gameSpeedSlider);
        loadFloat(PREF_BG_SCROLL_SPEED, DEFAULT_BG_SCROLL_SPEED, bgScrollEdit, bgScrollSlider);
        loadFloat(PREF_GROUND_SCROLL_SPEED, DEFAULT_GROUND_SCROLL_SPEED, groundScrollEdit, groundScrollSlider);
        loadFloat(PREF_BIRD_SIZE, DEFAULT_BIRD_SIZE, birdSizeEdit, birdSizeSlider);
        loadFloat(PREF_BIRD_HITBOX, DEFAULT_BIRD_HITBOX, birdHitboxEdit, birdHitboxSlider);
        loadFloat(PREF_GRAVITY, DEFAULT_GRAVITY, gravityEdit, gravitySlider);
        loadFloat(PREF_JUMP_STRENGTH, DEFAULT_JUMP_STRENGTH, jumpEdit, jumpSlider);
        loadFloat(PREF_BIRD_HANG_DELAY, DEFAULT_BIRD_HANG_DELAY, hangDelayEdit, hangDelaySlider);
        loadFloat(PREF_PIPE_GAP, DEFAULT_PIPE_GAP, pipeGapEdit, pipeGapSlider);
        loadFloat(PREF_PIPE_SPACING, DEFAULT_PIPE_SPACING, pipeSpacingEdit, pipeSpacingSlider);
        loadFloat(PREF_PIPE_WIDTH, DEFAULT_PIPE_WIDTH, pipeWidthEdit, pipeWidthSlider);
        loadFloat(PREF_PIPE_VARIATION, DEFAULT_PIPE_VARIATION, pipeVariationEdit, pipeVariationSlider);
        loadFloat(PREF_PIPE_SPEED_VARIATION, DEFAULT_PIPE_SPEED_VARIATION, pipeSpeedVariationEdit, pipeSpeedVariationSlider);

        loadInt(PREF_GAMEOVER_OPACITY, DEFAULT_GAMEOVER_OPACITY, opacityEdit, opacitySlider);
        loadIntNoSlider(PREF_PIPE_MOVE_TIER_1_SCORE, DEFAULT_PIPE_MOVE_TIER_1_SCORE, pipeMoveTier1Edit);
        loadIntNoSlider(PREF_PIPE_MOVE_TIER_2_SCORE, DEFAULT_PIPE_MOVE_TIER_2_SCORE, pipeMoveTier2Edit);
        loadIntNoSlider(PREF_SCORE_MULTIPLIER, DEFAULT_SCORE_MULTIPLIER, scoreMultiplierEdit);

        loadSwitches();

        if (backgroundView != null) {
            backgroundView.setSpeedMultiplier(prefs.getFloat(PREF_GAME_SPEED, DEFAULT_GAME_SPEED));
            int background = prefs.getInt(PREF_BACKGROUND, DEFAULT_BACKGROUND);
            backgroundView.setBackgroundStyle(background);
        }
        isProgrammaticChange = false;
    }

    private void setupListeners() {
        setupRadioGroupListeners();
        setupSwitchListeners();

        setupFloatBinding(gameSpeedEdit, gameSpeedSlider, PREF_GAME_SPEED);
        setupFloatBinding(bgScrollEdit, bgScrollSlider, PREF_BG_SCROLL_SPEED);
        setupFloatBinding(groundScrollEdit, groundScrollSlider, PREF_GROUND_SCROLL_SPEED);
        setupFloatBinding(birdSizeEdit, birdSizeSlider, PREF_BIRD_SIZE);
        setupFloatBinding(birdHitboxEdit, birdHitboxSlider, PREF_BIRD_HITBOX);
        setupFloatBinding(gravityEdit, gravitySlider, PREF_GRAVITY);
        setupFloatBinding(jumpEdit, jumpSlider, PREF_JUMP_STRENGTH);
        setupFloatBinding(hangDelayEdit, hangDelaySlider, PREF_BIRD_HANG_DELAY);
        setupFloatBinding(pipeGapEdit, pipeGapSlider, PREF_PIPE_GAP);
        setupFloatBinding(pipeSpacingEdit, pipeSpacingSlider, PREF_PIPE_SPACING);
        setupFloatBinding(pipeWidthEdit, pipeWidthSlider, PREF_PIPE_WIDTH);
        setupFloatBinding(pipeVariationEdit, pipeVariationSlider, PREF_PIPE_VARIATION);
        setupFloatBinding(pipeSpeedVariationEdit, pipeSpeedVariationSlider, PREF_PIPE_SPEED_VARIATION);

        setupIntBinding(opacityEdit, opacitySlider, PREF_GAMEOVER_OPACITY);
        setupIntBindingNoSlider(pipeMoveTier1Edit, PREF_PIPE_MOVE_TIER_1_SCORE);
        setupIntBindingNoSlider(pipeMoveTier2Edit, PREF_PIPE_MOVE_TIER_2_SCORE);
        setupIntBindingNoSlider(scoreMultiplierEdit, PREF_SCORE_MULTIPLIER);
    }

    private void resetToDefaults() {
        boolean disclaimerShown = prefs.getBoolean(PREF_SETTINGS_DISCLAIMER_SHOWN, false);
        editor.clear().putBoolean(PREF_SETTINGS_DISCLAIMER_SHOWN, disclaimerShown).apply();
        loadSettings();
    }

    private void setupExpandableSection(int headerId, int contentId, int iconId) {
        View header = findViewById(headerId);
        View content = findViewById(contentId);
        ImageView icon = findViewById(iconId);
        header.setOnClickListener(v -> {
            boolean isVisible = content.getVisibility() == View.VISIBLE;
            content.setVisibility(isVisible ? View.GONE : View.VISIBLE);
            icon.animate().rotation(isVisible ? 0f : 180f).setDuration(300).start();
        });
    }

    private void toggleRadioGroupEnabled(RadioGroup group, boolean enabled) {
        for (int i = 0; i < group.getChildCount(); i++) {
            group.getChildAt(i).setEnabled(enabled);
            group.getChildAt(i).setAlpha(enabled ? 1.0f : 0.5f);
        }
    }

    private void updateSliderState(float value, Slider slider) {
        boolean inRange = (value >= slider.getValueFrom() && value <= slider.getValueTo());
        slider.setEnabled(inRange);
        if (inRange) {
            slider.setValue(value);
        }
    }
    
    private void loadFloat(String key, float defValue, EditText editText, Slider slider) {
        float value = prefs.getFloat(key, defValue);
        editText.setText(String.format(Locale.US, "%.2f", value));
        updateSliderState(value, slider);
    }

    private void loadInt(String key, int defValue, EditText editText, Slider slider) {
        int value = prefs.getInt(key, defValue);
        editText.setText(String.valueOf(value));
        updateSliderState(value, slider);
    }

    private void loadIntNoSlider(String key, int defValue, EditText editText) {
        editText.setText(String.valueOf(prefs.getInt(key, defValue)));
    }

    private void loadRadioGroups() {
        int birdColor = prefs.getInt(PREF_BIRD_COLOR, DEFAULT_BIRD_COLOR);
        if (birdColor == 0) birdColorGroup.check(R.id.rb_bird_yellow);
        else if (birdColor == 1) birdColorGroup.check(R.id.rb_bird_blue);
        else if (birdColor == 2) birdColorGroup.check(R.id.rb_bird_red);
        else birdColorGroup.check(R.id.rb_bird_random);

        int background = prefs.getInt(PREF_BACKGROUND, DEFAULT_BACKGROUND);
        if (background == 0) backgroundGroup.check(R.id.rb_bg_day);
        else if (background == 1) backgroundGroup.check(R.id.rb_bg_night);
        else backgroundGroup.check(R.id.rb_bg_random);

        int pipeColor = prefs.getInt(PREF_PIPE_COLOR, DEFAULT_PIPE_COLOR);
        if (pipeColor == 1) pipeColorGroup.check(R.id.rb_pipe_red);
        else if (pipeColor == 2) pipeColorGroup.check(R.id.rb_pipe_blue);
        else if (pipeColor == 3) pipeColorGroup.check(R.id.rb_pipe_yellow);
        else if (pipeColor == 4) pipeColorGroup.check(R.id.rb_pipe_white);
        else if (pipeColor == 5) pipeColorGroup.check(R.id.rb_pipe_pink);
        else if (pipeColor == 6) pipeColorGroup.check(R.id.rb_pipe_black);
        else if (pipeColor == 7) pipeColorGroup.check(R.id.rb_pipe_purple);
        else if (pipeColor == 8) pipeColorGroup.check(R.id.rb_pipe_orange);
        else pipeColorGroup.check(R.id.rb_pipe_default);
    }

    private void loadSwitches() {
        randomPipeColorsSwitch.setChecked(prefs.getBoolean(PREF_RANDOM_PIPE_COLORS_ENABLED, DEFAULT_RANDOM_PIPE_COLORS_ENABLED));
        toggleRadioGroupEnabled(pipeColorGroup, !randomPipeColorsSwitch.isChecked());
        movingPipesSwitch.setChecked(prefs.getBoolean(PREF_MOVING_PIPES_ENABLED, DEFAULT_MOVING_PIPES_ENABLED));
        noClipSwitch.setChecked(prefs.getBoolean(PREF_NO_CLIP_ENABLED, DEFAULT_NO_CLIP_ENABLED));
        infiniteFlapSwitch.setChecked(prefs.getBoolean(PREF_INFINITE_FLAP_ENABLED, DEFAULT_INFINITE_FLAP_ENABLED));
        rainbowBirdSwitch.setChecked(prefs.getBoolean(PREF_RAINBOW_BIRD_ENABLED, DEFAULT_RAINBOW_BIRD_ENABLED));
        birdTrailSwitch.setChecked(prefs.getBoolean(PREF_BIRD_TRAIL_ENABLED, DEFAULT_BIRD_TRAIL_ENABLED));
        ghostModeSwitch.setChecked(prefs.getBoolean(PREF_GHOST_MODE_ENABLED, DEFAULT_GHOST_MODE_ENABLED));
        upsideDownSwitch.setChecked(prefs.getBoolean(PREF_UPSIDE_DOWN_ENABLED, DEFAULT_UPSIDE_DOWN_ENABLED));
        reversePipesSwitch.setChecked(prefs.getBoolean(PREF_REVERSE_PIPES_ENABLED, DEFAULT_REVERSE_PIPES_ENABLED));
        soundSwitch.setChecked(prefs.getBoolean(PREF_SOUND_ENABLED, DEFAULT_SOUND_ENABLED));
        hapticFeedbackSwitch.setChecked(prefs.getBoolean(PREF_HAPTIC_FEEDBACK_ENABLED, DEFAULT_HAPTIC_FEEDBACK_ENABLED));
        wingAnimationSwitch.setChecked(prefs.getBoolean(PREF_WING_ANIMATION_ENABLED, DEFAULT_WING_ANIMATION_ENABLED));
        hideSettingsIconSwitch.setChecked(prefs.getBoolean(PREF_HIDE_SETTINGS_ICON, DEFAULT_HIDE_SETTINGS_ICON));
    }

    private void setupRadioGroupListeners() {
        birdColorGroup.setOnCheckedChangeListener((group, checkedId) -> {
            int selection = -1;
            if (checkedId == R.id.rb_bird_yellow) selection = 0;
            else if (checkedId == R.id.rb_bird_blue) selection = 1;
            else if (checkedId == R.id.rb_bird_red) selection = 2;
            editor.putInt(PREF_BIRD_COLOR, selection).apply();
        });
        backgroundGroup.setOnCheckedChangeListener((group, checkedId) -> {
            int selection = -1;
            if (checkedId == R.id.rb_bg_day) selection = 0;
            else if (checkedId == R.id.rb_bg_night) selection = 1;
            editor.putInt(PREF_BACKGROUND, selection).apply();
            if(backgroundView != null) backgroundView.setBackgroundStyle(selection);
        });
        pipeColorGroup.setOnCheckedChangeListener((group, checkedId) -> {
            int selection = 0;
            if (checkedId == R.id.rb_pipe_red) selection = 1;
            else if (checkedId == R.id.rb_pipe_blue) selection = 2;
            else if (checkedId == R.id.rb_pipe_yellow) selection = 3;
            else if (checkedId == R.id.rb_pipe_white) selection = 4;
            else if (checkedId == R.id.rb_pipe_pink) selection = 5;
            else if (checkedId == R.id.rb_pipe_black) selection = 6;
            else if (checkedId == R.id.rb_pipe_purple) selection = 7;
            else if (checkedId == R.id.rb_pipe_orange) selection = 8;
            editor.putInt(PREF_PIPE_COLOR, selection).apply();
        });
    }

    private void setupSwitchListeners() {
        randomPipeColorsSwitch.setOnCheckedChangeListener((button, isChecked) -> {
            editor.putBoolean(PREF_RANDOM_PIPE_COLORS_ENABLED, isChecked).apply();
            toggleRadioGroupEnabled(pipeColorGroup, !isChecked);
        });
        setupSwitchListener(movingPipesSwitch, PREF_MOVING_PIPES_ENABLED);
        setupSwitchListener(noClipSwitch, PREF_NO_CLIP_ENABLED);
        setupSwitchListener(infiniteFlapSwitch, PREF_INFINITE_FLAP_ENABLED);
        setupSwitchListener(rainbowBirdSwitch, PREF_RAINBOW_BIRD_ENABLED);
        setupSwitchListener(birdTrailSwitch, PREF_BIRD_TRAIL_ENABLED);
        setupSwitchListener(ghostModeSwitch, PREF_GHOST_MODE_ENABLED);
        setupSwitchListener(upsideDownSwitch, PREF_UPSIDE_DOWN_ENABLED);
        setupSwitchListener(reversePipesSwitch, PREF_REVERSE_PIPES_ENABLED);
        setupSwitchListener(soundSwitch, PREF_SOUND_ENABLED);
        setupSwitchListener(hapticFeedbackSwitch, PREF_HAPTIC_FEEDBACK_ENABLED);
        setupSwitchListener(wingAnimationSwitch, PREF_WING_ANIMATION_ENABLED);
        setupSwitchListener(hideSettingsIconSwitch, PREF_HIDE_SETTINGS_ICON);
    }

    private void setupSwitchListener(MaterialSwitch mSwitch, String prefKey) {
        mSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> editor.putBoolean(prefKey, isChecked).apply());
    }

    private void setupFloatBinding(EditText editText, Slider slider, String prefKey) {
        slider.addOnChangeListener((s, value, fromUser) -> {
            if (fromUser) {
                isProgrammaticChange = true;
                editText.setText(String.format(Locale.US, "%.2f", value));
                editor.putFloat(prefKey, value).apply();
                if (prefKey.equals(PREF_GAME_SPEED) && backgroundView != null) {
                    backgroundView.setSpeedMultiplier(value);
                }
                isProgrammaticChange = false;
            }
        });
        editText.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) {
                if (isProgrammaticChange) return;
                try {
                    String valStr = s.toString();
                    if (valStr.isEmpty() || valStr.equals(".") || valStr.equals("-")) return;
                    float value = Float.parseFloat(valStr);
                    editor.putFloat(prefKey, value).apply();
                    updateSliderState(value, slider);
                    if (prefKey.equals(PREF_GAME_SPEED) && backgroundView != null) {
                        backgroundView.setSpeedMultiplier(value);
                    }
                } catch (NumberFormatException e) {
                    // Ignore
                }
            }
        });
    }

    private void setupIntBinding(EditText editText, Slider slider, String prefKey) {
        slider.addOnChangeListener((s, value, fromUser) -> {
            if (fromUser) {
                isProgrammaticChange = true;
                int intValue = (int) value;
                editText.setText(String.valueOf(intValue));
                editor.putInt(prefKey, intValue).apply();
                isProgrammaticChange = false;
            }
        });
        editText.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) {
                if (isProgrammaticChange) return;
                try {
                    String valStr = s.toString();
                    if (valStr.isEmpty()) return;
                    int value = Integer.parseInt(valStr);
                    editor.putInt(prefKey, value).apply();
                    updateSliderState(value, slider);
                } catch (NumberFormatException e) {
                    // Ignore
                }
            }
        });
    }

    private void setupIntBindingNoSlider(EditText editText, String prefKey) {
        editText.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) {
                if (isProgrammaticChange) return;
                try {
                    String valStr = s.toString();
                    if (valStr.isEmpty()) return;
                    int value = Integer.parseInt(valStr);
                    editor.putInt(prefKey, value).apply();
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
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inScaled = false;
            Bitmap atlas = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.atlas, options);
            Bitmap unscaledBgDay = Bitmap.createBitmap(atlas, 0, 0, 288, 512);
            Bitmap unscaledBgNight = Bitmap.createBitmap(atlas, 292, 0, 288, 512);
            Bitmap unscaledLand = Bitmap.createBitmap(atlas, 584, 0, 336, 112);
            atlas.recycle();

            float scale = (float) screenHeight / unscaledBgDay.getHeight();
            int bgWidth = (int) (unscaledBgDay.getWidth() * scale);

            bgDayBitmap = Bitmap.createScaledBitmap(unscaledBgDay, bgWidth, screenHeight, false);
            bgNightBitmap = Bitmap.createScaledBitmap(unscaledBgNight, bgWidth, screenHeight, false);
            groundBitmap = Bitmap.createScaledBitmap(unscaledLand, (int)(unscaledLand.getWidth() * scale), (int)(unscaledLand.getHeight() * scale), false);
            groundHeight = groundBitmap.getHeight();
            basePipeSpeed = (1.1f * scale) * 120.0f;
            isReady = true;
            applyBackgroundStyle();
            resume();
        }

        public void setBackgroundStyle(int style) {
            this.backgroundStyle = style;
            if (isReady) {
                applyBackgroundStyle();
            }
        }

        private void applyBackgroundStyle() {
            switch (this.backgroundStyle) {
                case 0:
                    currentBgBitmap = bgDayBitmap;
                    break;
                case 1:
                    currentBgBitmap = bgNightBitmap;
                    break;
                default:
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

            if (currentBgBitmap == null || groundBitmap == null) {
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
            for (float x = backgroundX; x < screenWidth; x += currentBgBitmap.getWidth()) {
                canvas.drawBitmap(currentBgBitmap, x, 0, pixelPaint);
            }

            float groundTopY = screenHeight - groundHeight;
            for (float x = groundX; x < screenWidth; x += groundBitmap.getWidth()) {
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
        }

        @Override
        protected void onDetachedFromWindow() {
            super.onDetachedFromWindow();
            pause();
            if(bgDayBitmap!=null) bgDayBitmap.recycle();
            if(bgNightBitmap!=null) bgNightBitmap.recycle();
            if(groundBitmap!=null) groundBitmap.recycle();
            isReady = false;
        }
    }
}