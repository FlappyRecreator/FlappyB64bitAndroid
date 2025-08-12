package com.flappybird.recreation;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import android.graphics.PorterDuffXfermode;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.graphics.Shader;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Choreographer;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;
import android.graphics.PorterDuffColorFilter;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.materialswitch.MaterialSwitch;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.Random;

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

    public static final String PREF_JETPACK_MODE_ENABLED = "pref_jetpack_mode_enabled";
    public static final String PREF_DRUNK_MODE_ENABLED = "pref_drunk_mode_enabled";
    public static final String PREF_DRUNK_MODE_STRENGTH = "pref_drunk_mode_strength";
    public static final String PREF_WEATHER_EFFECT = "pref_weather_effect";
    public static final String PREF_WIND_STRENGTH = "pref_wind_strength";
    public static final String PREF_DYNAMIC_DAY_NIGHT_ENABLED = "pref_dynamic_day_night_enabled";
    public static final String PREF_SCREEN_SHAKE_ENABLED = "pref_screen_shake_enabled";
    public static final String PREF_SCREEN_SHAKE_STRENGTH = "pref_screen_shake_strength";
    public static final String PREF_GOLDEN_PIPE_ENABLED = "pref_golden_pipe_enabled";
    public static final String PREF_GOLDEN_PIPE_CHANCE = "pref_golden_pipe_chance";
    public static final String PREF_GOLDEN_PIPE_BONUS = "pref_golden_pipe_bonus";

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

    public static final boolean DEFAULT_JETPACK_MODE_ENABLED = false;
    public static final boolean DEFAULT_DRUNK_MODE_ENABLED = false;
    public static final float DEFAULT_DRUNK_MODE_STRENGTH = 0.0f;
    public static final int DEFAULT_WEATHER_EFFECT = 0;
    public static final float DEFAULT_WIND_STRENGTH = 1.0f;
    public static final boolean DEFAULT_DYNAMIC_DAY_NIGHT_ENABLED = false;
    public static final boolean DEFAULT_SCREEN_SHAKE_ENABLED = false;
    public static final float DEFAULT_SCREEN_SHAKE_STRENGTH = 1.0f;
    public static final boolean DEFAULT_GOLDEN_PIPE_ENABLED = false;
    public static final int DEFAULT_GOLDEN_PIPE_CHANCE = 5;
    public static final int DEFAULT_GOLDEN_PIPE_BONUS = 10;

    private SettingsPreviewView previewView;
    private SharedPreferences prefs;
    private RadioGroup birdColorGroup, backgroundGroup, pipeColorGroup, weatherGroup;
    private MaterialSwitch soundSwitch, noClipSwitch, wingAnimationSwitch, movingPipesSwitch, hideSettingsIconSwitch;
    private MaterialSwitch rainbowBirdSwitch, upsideDownSwitch, reversePipesSwitch, hapticFeedbackSwitch, birdTrailSwitch;
    private MaterialSwitch ghostModeSwitch, randomPipeColorsSwitch, infiniteFlapSwitch, jetpackModeSwitch, drunkModeSwitch;
    private MaterialSwitch dayNightCycleSwitch, screenShakeSwitch, goldenPipeSwitch;
    private EditText opacityEditText, speedEditText, gravityEditText, jumpEditText, pipeGapEditText, pipeMoveTier1EditText, pipeMoveTier2EditText, hangDelayEditText;
    private EditText pipeSpacingEditText, birdHitboxEditText, pipeVariationEditText, scoreMultiplierEditText, pipeSpeedVariationEditText, birdSizeEditText;
    private EditText pipeWidthEditText, bgScrollEditText, groundScrollEditText, drunkModeStrengthEditText, windStrengthEditText;
    private EditText screenShakeStrengthEditText, goldenPipeChanceEditText, goldenPipeBonusEditText;
    private Button backButton, randomizeAllButton, resetAllButton, resetProfilesButton;
    private Button saveProfile1, loadProfile1, resetProfile1;
    private Button saveProfile2, loadProfile2, resetProfile2;
    private Button saveProfile3, loadProfile3, resetProfile3;
    private Random random = new Random();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        if (!prefs.getBoolean(PREF_SETTINGS_DISCLAIMER_SHOWN, false)) {
            showDisclaimerDialog();
        }

        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        WindowInsetsControllerCompat windowInsetsController = WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
        windowInsetsController.setSystemBarsBehavior(WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars());

        setContentView(R.layout.settings);

        FrameLayout previewContainer = findViewById(R.id.live_preview_container);
        previewView = new SettingsPreviewView(this);
        previewContainer.addView(previewView);

        initViews();
        loadSettingsFromSource(prefs);
        setupListeners();
    }

    private void showDisclaimerDialog() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Settings Disclaimer")
                .setMessage("Welcome to the new settings menu! Adjusting these values, especially in the experimental and physics sections, can create weird, fun, or unstable gameplay. Save your favorite setups to profiles and use the 'Randomize All' button for chaotic fun! Proceed with caution.")
                .setPositiveButton("I Understand", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        prefs.edit().putBoolean(PREF_SETTINGS_DISCLAIMER_SHOWN, true).apply();
                        dialog.dismiss();
                    }
                })
                .setCancelable(false)
                .show();
    }

    private void initViews() {
        backButton = findViewById(R.id.button_back);
        randomizeAllButton = findViewById(R.id.button_randomize_all);
        resetAllButton = findViewById(R.id.button_reset_all);
        resetProfilesButton = findViewById(R.id.button_reset_profiles);

        saveProfile1 = findViewById(R.id.button_save_profile_1);
        loadProfile1 = findViewById(R.id.button_load_profile_1);
        resetProfile1 = findViewById(R.id.button_reset_profile_1);
        saveProfile2 = findViewById(R.id.button_save_profile_2);
        loadProfile2 = findViewById(R.id.button_load_profile_2);
        resetProfile2 = findViewById(R.id.button_reset_profile_2);
        saveProfile3 = findViewById(R.id.button_save_profile_3);
        loadProfile3 = findViewById(R.id.button_load_profile_3);
        resetProfile3 = findViewById(R.id.button_reset_profile_3);

        birdColorGroup = findViewById(R.id.rg_bird_color);
        backgroundGroup = findViewById(R.id.rg_background);
        pipeColorGroup = findViewById(R.id.rg_pipe_color);
        weatherGroup = findViewById(R.id.rg_weather);

        soundSwitch = findViewById(R.id.switch_sound);
        wingAnimationSwitch = findViewById(R.id.switch_wing_animation);
        hideSettingsIconSwitch = findViewById(R.id.switch_hide_settings_icon);
        hapticFeedbackSwitch = findViewById(R.id.switch_haptic_feedback);

        rainbowBirdSwitch = findViewById(R.id.switch_rainbow_bird);
        birdTrailSwitch = findViewById(R.id.switch_bird_trail);
        ghostModeSwitch = findViewById(R.id.switch_ghost_mode);

        jetpackModeSwitch = findViewById(R.id.switch_jetpack_mode);
        drunkModeSwitch = findViewById(R.id.switch_drunk_mode);
        dayNightCycleSwitch = findViewById(R.id.switch_day_night_cycle);
        screenShakeSwitch = findViewById(R.id.switch_screen_shake);

        movingPipesSwitch = findViewById(R.id.switch_moving_pipes);
        randomPipeColorsSwitch = findViewById(R.id.switch_random_pipe_colors);
        goldenPipeSwitch = findViewById(R.id.switch_golden_pipe);

        noClipSwitch = findViewById(R.id.switch_no_clip);
        upsideDownSwitch = findViewById(R.id.switch_upside_down);
        reversePipesSwitch = findViewById(R.id.switch_reverse_pipes);
        infiniteFlapSwitch = findViewById(R.id.switch_flap_timeout);

        opacityEditText = findViewById(R.id.et_opacity);
        speedEditText = findViewById(R.id.et_game_speed);
        gravityEditText = findViewById(R.id.et_gravity);
        jumpEditText = findViewById(R.id.et_jump);
        pipeGapEditText = findViewById(R.id.et_pipe_gap);
        hangDelayEditText = findViewById(R.id.et_hang_delay);
        pipeSpacingEditText = findViewById(R.id.et_pipe_spacing);
        birdHitboxEditText = findViewById(R.id.et_bird_hitbox);
        pipeVariationEditText = findViewById(R.id.et_pipe_variation);
        scoreMultiplierEditText = findViewById(R.id.et_score_multiplier);
        pipeMoveTier1EditText = findViewById(R.id.et_pipe_move_tier1);
        pipeMoveTier2EditText = findViewById(R.id.et_pipe_move_tier2);
        pipeSpeedVariationEditText = findViewById(R.id.et_pipe_speed_variation);
        birdSizeEditText = findViewById(R.id.et_bird_size);
        pipeWidthEditText = findViewById(R.id.et_pipe_width);
        bgScrollEditText = findViewById(R.id.et_bg_scroll);
        groundScrollEditText = findViewById(R.id.et_ground_scroll);

        drunkModeStrengthEditText = findViewById(R.id.et_drunk_mode_strength);
        windStrengthEditText = findViewById(R.id.et_wind_strength);
        screenShakeStrengthEditText = findViewById(R.id.et_screen_shake_strength);
        goldenPipeChanceEditText = findViewById(R.id.et_golden_pipe_chance);
        goldenPipeBonusEditText = findViewById(R.id.et_golden_pipe_bonus);
    }

    private List<View> getAllSettingControls() {
        List<View> controls = new ArrayList<>();
        controls.add(birdColorGroup);
        controls.add(backgroundGroup);
        controls.add(pipeColorGroup);
        controls.add(weatherGroup);
        controls.add(soundSwitch);
        controls.add(noClipSwitch);
        controls.add(wingAnimationSwitch);
        controls.add(movingPipesSwitch);
        controls.add(hideSettingsIconSwitch);
        controls.add(rainbowBirdSwitch);
        controls.add(upsideDownSwitch);
        controls.add(reversePipesSwitch);
        controls.add(hapticFeedbackSwitch);
        controls.add(birdTrailSwitch);
        controls.add(ghostModeSwitch);
        controls.add(randomPipeColorsSwitch);
        controls.add(infiniteFlapSwitch);
        controls.add(jetpackModeSwitch);
        controls.add(drunkModeSwitch);
        controls.add(dayNightCycleSwitch);
        controls.add(screenShakeSwitch);
        controls.add(goldenPipeSwitch);
        controls.add(opacityEditText);
        controls.add(speedEditText);
        controls.add(gravityEditText);
        controls.add(jumpEditText);
        controls.add(pipeGapEditText);
        controls.add(pipeMoveTier1EditText);
        controls.add(pipeMoveTier2EditText);
        controls.add(hangDelayEditText);
        controls.add(pipeSpacingEditText);
        controls.add(birdHitboxEditText);
        controls.add(pipeVariationEditText);
        controls.add(scoreMultiplierEditText);
        controls.add(pipeSpeedVariationEditText);
        controls.add(birdSizeEditText);
        controls.add(pipeWidthEditText);
        controls.add(bgScrollEditText);
        controls.add(groundScrollEditText);
        controls.add(drunkModeStrengthEditText);
        controls.add(windStrengthEditText);
        controls.add(screenShakeStrengthEditText);
        controls.add(goldenPipeChanceEditText);
        controls.add(goldenPipeBonusEditText);
        return controls;
    }

    private void loadSettingsFromSource(SharedPreferences source) {
        int birdColor = source.getInt(PREF_BIRD_COLOR, DEFAULT_BIRD_COLOR);
        switch (birdColor) {
            case 0: birdColorGroup.check(R.id.rb_bird_yellow); break;
            case 1: birdColorGroup.check(R.id.rb_bird_blue); break;
            case 2: birdColorGroup.check(R.id.rb_bird_red); break;
            default: birdColorGroup.check(R.id.rb_bird_random); break;
        }

        int background = source.getInt(PREF_BACKGROUND, DEFAULT_BACKGROUND);
        if (background == 0) backgroundGroup.check(R.id.rb_bg_day);
        else if (background == 1) backgroundGroup.check(R.id.rb_bg_night);
        else backgroundGroup.check(R.id.rb_bg_random);

        int weather = source.getInt(PREF_WEATHER_EFFECT, DEFAULT_WEATHER_EFFECT);
        if (weather == 1) weatherGroup.check(R.id.rb_weather_rain);
        else if (weather == 2) weatherGroup.check(R.id.rb_weather_storm);
        else weatherGroup.check(R.id.rb_weather_off);

        soundSwitch.setChecked(source.getBoolean(PREF_SOUND_ENABLED, DEFAULT_SOUND_ENABLED));
        noClipSwitch.setChecked(source.getBoolean(PREF_NO_CLIP_ENABLED, DEFAULT_NO_CLIP_ENABLED));
        wingAnimationSwitch.setChecked(source.getBoolean(PREF_WING_ANIMATION_ENABLED, DEFAULT_WING_ANIMATION_ENABLED));
        movingPipesSwitch.setChecked(source.getBoolean(PREF_MOVING_PIPES_ENABLED, DEFAULT_MOVING_PIPES_ENABLED));
        hideSettingsIconSwitch.setChecked(source.getBoolean(PREF_HIDE_SETTINGS_ICON, DEFAULT_HIDE_SETTINGS_ICON));
        rainbowBirdSwitch.setChecked(source.getBoolean(PREF_RAINBOW_BIRD_ENABLED, DEFAULT_RAINBOW_BIRD_ENABLED));
        upsideDownSwitch.setChecked(source.getBoolean(PREF_UPSIDE_DOWN_ENABLED, DEFAULT_UPSIDE_DOWN_ENABLED));
        reversePipesSwitch.setChecked(source.getBoolean(PREF_REVERSE_PIPES_ENABLED, DEFAULT_REVERSE_PIPES_ENABLED));
        hapticFeedbackSwitch.setChecked(source.getBoolean(PREF_HAPTIC_FEEDBACK_ENABLED, DEFAULT_HAPTIC_FEEDBACK_ENABLED));
        birdTrailSwitch.setChecked(source.getBoolean(PREF_BIRD_TRAIL_ENABLED, DEFAULT_BIRD_TRAIL_ENABLED));
        ghostModeSwitch.setChecked(source.getBoolean(PREF_GHOST_MODE_ENABLED, DEFAULT_GHOST_MODE_ENABLED));
        randomPipeColorsSwitch.setChecked(source.getBoolean(PREF_RANDOM_PIPE_COLORS_ENABLED, DEFAULT_RANDOM_PIPE_COLORS_ENABLED));
        infiniteFlapSwitch.setChecked(source.getBoolean(PREF_INFINITE_FLAP_ENABLED, DEFAULT_INFINITE_FLAP_ENABLED));
        jetpackModeSwitch.setChecked(source.getBoolean(PREF_JETPACK_MODE_ENABLED, DEFAULT_JETPACK_MODE_ENABLED));
        drunkModeSwitch.setChecked(source.getBoolean(PREF_DRUNK_MODE_ENABLED, DEFAULT_DRUNK_MODE_ENABLED));
        dayNightCycleSwitch.setChecked(source.getBoolean(PREF_DYNAMIC_DAY_NIGHT_ENABLED, DEFAULT_DYNAMIC_DAY_NIGHT_ENABLED));
        screenShakeSwitch.setChecked(source.getBoolean(PREF_SCREEN_SHAKE_ENABLED, DEFAULT_SCREEN_SHAKE_ENABLED));
        goldenPipeSwitch.setChecked(source.getBoolean(PREF_GOLDEN_PIPE_ENABLED, DEFAULT_GOLDEN_PIPE_ENABLED));

        int pipeColor = source.getInt(PREF_PIPE_COLOR, DEFAULT_PIPE_COLOR);
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

        opacityEditText.setText(String.valueOf(source.getInt(PREF_GAMEOVER_OPACITY, DEFAULT_GAMEOVER_OPACITY)));
        speedEditText.setText(String.valueOf(source.getFloat(PREF_GAME_SPEED, DEFAULT_GAME_SPEED)));
        gravityEditText.setText(String.valueOf(source.getFloat(PREF_GRAVITY, DEFAULT_GRAVITY)));
        jumpEditText.setText(String.valueOf(source.getFloat(PREF_JUMP_STRENGTH, DEFAULT_JUMP_STRENGTH)));
        hangDelayEditText.setText(String.valueOf(source.getFloat(PREF_BIRD_HANG_DELAY, DEFAULT_BIRD_HANG_DELAY)));
        pipeGapEditText.setText(String.valueOf(source.getFloat(PREF_PIPE_GAP, DEFAULT_PIPE_GAP)));
        pipeSpacingEditText.setText(String.valueOf(source.getFloat(PREF_PIPE_SPACING, DEFAULT_PIPE_SPACING)));
        birdHitboxEditText.setText(String.valueOf(source.getFloat(PREF_BIRD_HITBOX, DEFAULT_BIRD_HITBOX)));
        pipeVariationEditText.setText(String.valueOf(source.getFloat(PREF_PIPE_VARIATION, DEFAULT_PIPE_VARIATION)));
        scoreMultiplierEditText.setText(String.valueOf(source.getInt(PREF_SCORE_MULTIPLIER, DEFAULT_SCORE_MULTIPLIER)));
        pipeMoveTier1EditText.setText(String.valueOf(source.getInt(PREF_PIPE_MOVE_TIER_1_SCORE, DEFAULT_PIPE_MOVE_TIER_1_SCORE)));
        pipeMoveTier2EditText.setText(String.valueOf(source.getInt(PREF_PIPE_MOVE_TIER_2_SCORE, DEFAULT_PIPE_MOVE_TIER_2_SCORE)));
        pipeSpeedVariationEditText.setText(String.valueOf(source.getFloat(PREF_PIPE_SPEED_VARIATION, DEFAULT_PIPE_SPEED_VARIATION)));
        birdSizeEditText.setText(String.valueOf(source.getFloat(PREF_BIRD_SIZE, DEFAULT_BIRD_SIZE)));
        pipeWidthEditText.setText(String.valueOf(source.getFloat(PREF_PIPE_WIDTH, DEFAULT_PIPE_WIDTH)));
        bgScrollEditText.setText(String.valueOf(source.getFloat(PREF_BG_SCROLL_SPEED, DEFAULT_BG_SCROLL_SPEED)));
        groundScrollEditText.setText(String.valueOf(source.getFloat(PREF_GROUND_SCROLL_SPEED, DEFAULT_GROUND_SCROLL_SPEED)));
        drunkModeStrengthEditText.setText(String.valueOf(source.getFloat(PREF_DRUNK_MODE_STRENGTH, DEFAULT_DRUNK_MODE_STRENGTH)));
        windStrengthEditText.setText(String.valueOf(source.getFloat(PREF_WIND_STRENGTH, DEFAULT_WIND_STRENGTH)));
        screenShakeStrengthEditText.setText(String.valueOf(source.getFloat(PREF_SCREEN_SHAKE_STRENGTH, DEFAULT_SCREEN_SHAKE_STRENGTH)));
        goldenPipeChanceEditText.setText(String.valueOf(source.getInt(PREF_GOLDEN_PIPE_CHANCE, DEFAULT_GOLDEN_PIPE_CHANCE)));
        goldenPipeBonusEditText.setText(String.valueOf(source.getInt(PREF_GOLDEN_PIPE_BONUS, DEFAULT_GOLDEN_PIPE_BONUS)));

        updatePreview();
    }

    private void saveSettingsToEditor(SharedPreferences.Editor editor) {
        editor.putInt(PREF_BIRD_COLOR, getBirdColorFromRadio());
        editor.putInt(PREF_BACKGROUND, getBackgroundFromRadio());
        editor.putInt(PREF_PIPE_COLOR, getPipeColorFromRadio());
        editor.putInt(PREF_WEATHER_EFFECT, getWeatherFromRadio());
        editor.putBoolean(PREF_SOUND_ENABLED, soundSwitch.isChecked());
        editor.putBoolean(PREF_WING_ANIMATION_ENABLED, wingAnimationSwitch.isChecked());
        editor.putBoolean(PREF_HIDE_SETTINGS_ICON, hideSettingsIconSwitch.isChecked());
        editor.putBoolean(PREF_HAPTIC_FEEDBACK_ENABLED, hapticFeedbackSwitch.isChecked());
        editor.putBoolean(PREF_RAINBOW_BIRD_ENABLED, rainbowBirdSwitch.isChecked());
        editor.putBoolean(PREF_BIRD_TRAIL_ENABLED, birdTrailSwitch.isChecked());
        editor.putBoolean(PREF_GHOST_MODE_ENABLED, ghostModeSwitch.isChecked());
        editor.putBoolean(PREF_JETPACK_MODE_ENABLED, jetpackModeSwitch.isChecked());
        editor.putBoolean(PREF_DRUNK_MODE_ENABLED, drunkModeSwitch.isChecked());
        editor.putBoolean(PREF_DYNAMIC_DAY_NIGHT_ENABLED, dayNightCycleSwitch.isChecked());
        editor.putBoolean(PREF_SCREEN_SHAKE_ENABLED, screenShakeSwitch.isChecked());
        editor.putBoolean(PREF_MOVING_PIPES_ENABLED, movingPipesSwitch.isChecked());
        editor.putBoolean(PREF_RANDOM_PIPE_COLORS_ENABLED, randomPipeColorsSwitch.isChecked());
        editor.putBoolean(PREF_GOLDEN_PIPE_ENABLED, goldenPipeSwitch.isChecked());
        editor.putBoolean(PREF_NO_CLIP_ENABLED, noClipSwitch.isChecked());
        editor.putBoolean(PREF_UPSIDE_DOWN_ENABLED, upsideDownSwitch.isChecked());
        editor.putBoolean(PREF_REVERSE_PIPES_ENABLED, reversePipesSwitch.isChecked());
        editor.putBoolean(PREF_INFINITE_FLAP_ENABLED, infiniteFlapSwitch.isChecked());
        editor.putInt(PREF_GAMEOVER_OPACITY, parseInt(opacityEditText, DEFAULT_GAMEOVER_OPACITY));
        editor.putFloat(PREF_GAME_SPEED, parseFloat(speedEditText, DEFAULT_GAME_SPEED));
        editor.putFloat(PREF_GRAVITY, parseFloat(gravityEditText, DEFAULT_GRAVITY));
        editor.putFloat(PREF_JUMP_STRENGTH, parseFloat(jumpEditText, DEFAULT_JUMP_STRENGTH));
        editor.putFloat(PREF_BIRD_HANG_DELAY, parseFloat(hangDelayEditText, DEFAULT_BIRD_HANG_DELAY));
        editor.putFloat(PREF_PIPE_GAP, parseFloat(pipeGapEditText, DEFAULT_PIPE_GAP));
        editor.putFloat(PREF_PIPE_SPACING, parseFloat(pipeSpacingEditText, DEFAULT_PIPE_SPACING));
        editor.putFloat(PREF_BIRD_HITBOX, parseFloat(birdHitboxEditText, DEFAULT_BIRD_HITBOX));
        editor.putFloat(PREF_PIPE_VARIATION, parseFloat(pipeVariationEditText, DEFAULT_PIPE_VARIATION));
        editor.putInt(PREF_SCORE_MULTIPLIER, parseInt(scoreMultiplierEditText, DEFAULT_SCORE_MULTIPLIER));
        editor.putInt(PREF_PIPE_MOVE_TIER_1_SCORE, parseInt(pipeMoveTier1EditText, DEFAULT_PIPE_MOVE_TIER_1_SCORE));
        editor.putInt(PREF_PIPE_MOVE_TIER_2_SCORE, parseInt(pipeMoveTier2EditText, DEFAULT_PIPE_MOVE_TIER_2_SCORE));
        editor.putFloat(PREF_PIPE_SPEED_VARIATION, parseFloat(pipeSpeedVariationEditText, DEFAULT_PIPE_SPEED_VARIATION));
        editor.putFloat(PREF_BIRD_SIZE, parseFloat(birdSizeEditText, DEFAULT_BIRD_SIZE));
        editor.putFloat(PREF_PIPE_WIDTH, parseFloat(pipeWidthEditText, DEFAULT_PIPE_WIDTH));
        editor.putFloat(PREF_BG_SCROLL_SPEED, parseFloat(bgScrollEditText, DEFAULT_BG_SCROLL_SPEED));
        editor.putFloat(PREF_GROUND_SCROLL_SPEED, parseFloat(groundScrollEditText, DEFAULT_GROUND_SCROLL_SPEED));
        editor.putFloat(PREF_DRUNK_MODE_STRENGTH, parseFloat(drunkModeStrengthEditText, DEFAULT_DRUNK_MODE_STRENGTH));
        editor.putFloat(PREF_WIND_STRENGTH, parseFloat(windStrengthEditText, DEFAULT_WIND_STRENGTH));
        editor.putFloat(PREF_SCREEN_SHAKE_STRENGTH, parseFloat(screenShakeStrengthEditText, DEFAULT_SCREEN_SHAKE_STRENGTH));
        editor.putInt(PREF_GOLDEN_PIPE_CHANCE, parseInt(goldenPipeChanceEditText, DEFAULT_GOLDEN_PIPE_CHANCE));
        editor.putInt(PREF_GOLDEN_PIPE_BONUS, parseInt(goldenPipeBonusEditText, DEFAULT_GOLDEN_PIPE_BONUS));
        editor.apply();
    }

    private void saveCurrentSettings() {
        saveSettingsToEditor(prefs.edit());
    }

    private int getBirdColorFromRadio() {
        int checkedId = birdColorGroup.getCheckedRadioButtonId();
        if (checkedId == R.id.rb_bird_yellow) return 0;
        if (checkedId == R.id.rb_bird_blue) return 1;
        if (checkedId == R.id.rb_bird_red) return 2;
        return -1;
    }

    private int getBackgroundFromRadio() {
        int checkedId = backgroundGroup.getCheckedRadioButtonId();
        if (checkedId == R.id.rb_bg_day) return 0;
        if (checkedId == R.id.rb_bg_night) return 1;
        return -1;
    }

    private int getPipeColorFromRadio() {
        int checkedId = pipeColorGroup.getCheckedRadioButtonId();
        if (checkedId == R.id.rb_pipe_red) return 1;
        if (checkedId == R.id.rb_pipe_blue) return 2;
        if (checkedId == R.id.rb_pipe_yellow) return 3;
        if (checkedId == R.id.rb_pipe_white) return 4;
        if (checkedId == R.id.rb_pipe_pink) return 5;
        if (checkedId == R.id.rb_pipe_black) return 6;
        if (checkedId == R.id.rb_pipe_purple) return 7;
        if (checkedId == R.id.rb_pipe_orange) return 8;
        return 0;
    }

    private int getWeatherFromRadio() {
        int checkedId = weatherGroup.getCheckedRadioButtonId();
        if (checkedId == R.id.rb_weather_rain) return 1;
        if (checkedId == R.id.rb_weather_storm) return 2;
        return 0;
    }


    private int parseInt(EditText et, int defaultValue) {
        try {
            return Integer.parseInt(et.getText().toString());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private float parseFloat(EditText et, float defaultValue) {
        try {
            return Float.parseFloat(et.getText().toString());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }


    private void setupListeners() {
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        setupProfileListeners(1, saveProfile1, loadProfile1, resetProfile1);
        setupProfileListeners(2, saveProfile2, loadProfile2, resetProfile2);
        setupProfileListeners(3, saveProfile3, loadProfile3, resetProfile3);

        resetAllButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new MaterialAlertDialogBuilder(SettingsActivity.this)
                        .setTitle("Reset All Settings")
                        .setMessage("Are you sure you want to reset all current settings to their defaults? This will not affect your saved profiles.")
                        .setPositiveButton("Reset", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // Step 1: Clear the main preferences file
                                prefs.edit().clear().apply();

                                // Step 2: Manually reset the UI to default values
                                resetUiToDefaults();

                                // Step 3: Save the new default state to the main preferences
                                saveCurrentSettings();

                                // Step 4: Update the live preview
                                updatePreview();

                                Toast.makeText(SettingsActivity.this, "All settings have been reset to default.", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            }
        });

        resetProfilesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new MaterialAlertDialogBuilder(SettingsActivity.this)
                        .setTitle("Reset All Profiles")
                        .setMessage("Are you sure you want to delete all 3 of your saved profiles? This cannot be undone.")
                        .setPositiveButton("Reset", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                for (int i = 1; i <= 3; i++) {
                                    getSharedPreferences("FlappyBirdProfile_" + i, MODE_PRIVATE).edit().clear().apply();
                                }
                                Toast.makeText(SettingsActivity.this, "All profiles have been reset.", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            }
        });


        randomizeAllButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                randomizeAllSettings();
                Toast.makeText(SettingsActivity.this, "Chaos engaged! All settings randomized.", Toast.LENGTH_SHORT).show();
            }
        });

        TextWatcher universalTextWatcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) {
                saveCurrentSettings();
                updatePreview();
            }
        };

        CompoundButton.OnCheckedChangeListener universalCheckListener = new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                saveCurrentSettings();
                updatePreview();
            }
        };

        RadioGroup.OnCheckedChangeListener universalRadioListener = new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                saveCurrentSettings();
                updatePreview();
            }
        };

        for (View v : getAllSettingControls()) {
            if (v instanceof MaterialSwitch) {
                ((MaterialSwitch) v).setOnCheckedChangeListener(universalCheckListener);
            } else if (v instanceof EditText) {
                ((EditText)v).addTextChangedListener(universalTextWatcher);
            } else if (v instanceof RadioGroup) {
                ((RadioGroup)v).setOnCheckedChangeListener(universalRadioListener);
            }
        }
    }
    private void resetUiToDefaults() {
        // Radio Groups
        birdColorGroup.check(R.id.rb_bird_random);
        backgroundGroup.check(R.id.rb_bg_random);
        pipeColorGroup.check(R.id.rb_pipe_default);
        weatherGroup.check(R.id.rb_weather_off);

        // Switches
        soundSwitch.setChecked(DEFAULT_SOUND_ENABLED);
        wingAnimationSwitch.setChecked(DEFAULT_WING_ANIMATION_ENABLED);
        hideSettingsIconSwitch.setChecked(DEFAULT_HIDE_SETTINGS_ICON);
        hapticFeedbackSwitch.setChecked(DEFAULT_HAPTIC_FEEDBACK_ENABLED);
        rainbowBirdSwitch.setChecked(DEFAULT_RAINBOW_BIRD_ENABLED);
        birdTrailSwitch.setChecked(DEFAULT_BIRD_TRAIL_ENABLED);
        ghostModeSwitch.setChecked(DEFAULT_GHOST_MODE_ENABLED);
        jetpackModeSwitch.setChecked(DEFAULT_JETPACK_MODE_ENABLED);
        drunkModeSwitch.setChecked(DEFAULT_DRUNK_MODE_ENABLED);
        dayNightCycleSwitch.setChecked(DEFAULT_DYNAMIC_DAY_NIGHT_ENABLED);
        screenShakeSwitch.setChecked(DEFAULT_SCREEN_SHAKE_ENABLED);
        movingPipesSwitch.setChecked(DEFAULT_MOVING_PIPES_ENABLED);
        randomPipeColorsSwitch.setChecked(DEFAULT_RANDOM_PIPE_COLORS_ENABLED);
        goldenPipeSwitch.setChecked(DEFAULT_GOLDEN_PIPE_ENABLED);
        noClipSwitch.setChecked(DEFAULT_NO_CLIP_ENABLED);
        upsideDownSwitch.setChecked(DEFAULT_UPSIDE_DOWN_ENABLED);
        reversePipesSwitch.setChecked(DEFAULT_REVERSE_PIPES_ENABLED);
        infiniteFlapSwitch.setChecked(DEFAULT_INFINITE_FLAP_ENABLED);

        // EditTexts
        opacityEditText.setText(String.valueOf(DEFAULT_GAMEOVER_OPACITY));
        speedEditText.setText(String.valueOf(DEFAULT_GAME_SPEED));
        gravityEditText.setText(String.valueOf(DEFAULT_GRAVITY));
        jumpEditText.setText(String.valueOf(DEFAULT_JUMP_STRENGTH));
        hangDelayEditText.setText(String.valueOf(DEFAULT_BIRD_HANG_DELAY));
        pipeGapEditText.setText(String.valueOf(DEFAULT_PIPE_GAP));
        pipeSpacingEditText.setText(String.valueOf(DEFAULT_PIPE_SPACING));
        birdHitboxEditText.setText(String.valueOf(DEFAULT_BIRD_HITBOX));
        pipeVariationEditText.setText(String.valueOf(DEFAULT_PIPE_VARIATION));
        scoreMultiplierEditText.setText(String.valueOf(DEFAULT_SCORE_MULTIPLIER));
        pipeMoveTier1EditText.setText(String.valueOf(DEFAULT_PIPE_MOVE_TIER_1_SCORE));
        pipeMoveTier2EditText.setText(String.valueOf(DEFAULT_PIPE_MOVE_TIER_2_SCORE));
        pipeSpeedVariationEditText.setText(String.valueOf(DEFAULT_PIPE_SPEED_VARIATION));
        birdSizeEditText.setText(String.valueOf(DEFAULT_BIRD_SIZE));
        pipeWidthEditText.setText(String.valueOf(DEFAULT_PIPE_WIDTH));
        bgScrollEditText.setText(String.valueOf(DEFAULT_BG_SCROLL_SPEED));
        groundScrollEditText.setText(String.valueOf(DEFAULT_GROUND_SCROLL_SPEED));
        drunkModeStrengthEditText.setText(String.valueOf(DEFAULT_DRUNK_MODE_STRENGTH));
        windStrengthEditText.setText(String.valueOf(DEFAULT_WIND_STRENGTH));
        screenShakeStrengthEditText.setText(String.valueOf(DEFAULT_SCREEN_SHAKE_STRENGTH));
        goldenPipeChanceEditText.setText(String.valueOf(DEFAULT_GOLDEN_PIPE_CHANCE));
        goldenPipeBonusEditText.setText(String.valueOf(DEFAULT_GOLDEN_PIPE_BONUS));
    }


    private void randomizeAllSettings() {
        for (View v : getAllSettingControls()) {
            if (v instanceof MaterialSwitch) {
                ((MaterialSwitch) v).setChecked(random.nextBoolean());
            } else if (v instanceof RadioGroup) {
                RadioGroup rg = (RadioGroup) v;
                int childCount = rg.getChildCount();
                if (childCount > 0) {
                    ((RadioButton) rg.getChildAt(random.nextInt(childCount))).setChecked(true);
                }
            } else if (v instanceof EditText) {
                EditText et = (EditText) v;
                if (et.getId() == R.id.et_opacity || et.getId() == R.id.et_golden_pipe_chance) {
                    et.setText(String.valueOf(random.nextInt(101)));
                } else if (et.getId() == R.id.et_score_multiplier || et.getId() == R.id.et_golden_pipe_bonus) {
                    et.setText(String.valueOf(random.nextInt(100)));
                } else if (et.getId() == R.id.et_pipe_move_tier1 || et.getId() == R.id.et_pipe_move_tier2) {
                    et.setText(String.valueOf(random.nextInt(5001)));
                } else {
                    float randomFloat = random.nextFloat() * 2.0f;
                    if(random.nextBoolean()) randomFloat *= 2.0f;
                    et.setText(String.format("%.2f", randomFloat));
                }
            }
        }
        saveCurrentSettings();
        updatePreview();
    }

    private void setupProfileListeners(final int profileNum, Button saveBtn, Button loadBtn, Button resetBtn) {
        final String profilePrefsName = "FlappyBirdProfile_" + profileNum;

        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences profilePrefs = getSharedPreferences(profilePrefsName, MODE_PRIVATE);
                SharedPreferences.Editor profileEditor = profilePrefs.edit();
                profileEditor.clear();
                // Save the current UI state to the profile editor
                saveSettingsToEditor(profileEditor);
                Toast.makeText(SettingsActivity.this, "Profile " + profileNum + " saved", Toast.LENGTH_SHORT).show();
            }
        });

        loadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences profilePrefs = getSharedPreferences(profilePrefsName, MODE_PRIVATE);
                if(profilePrefs.getAll().isEmpty()){
                    Toast.makeText(SettingsActivity.this, "Profile " + profileNum + " is empty", Toast.LENGTH_SHORT).show();
                    return;
                }
                // Load from the profile into the UI
                loadSettingsFromSource(profilePrefs);
                // Save the newly loaded settings as the current active settings
                saveCurrentSettings();
                Toast.makeText(SettingsActivity.this, "Profile " + profileNum + " loaded", Toast.LENGTH_SHORT).show();
            }
        });

        resetBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new MaterialAlertDialogBuilder(SettingsActivity.this)
                        .setTitle("Reset Profile " + profileNum)
                        .setMessage("Are you sure you want to delete all settings saved in this profile?")
                        .setPositiveButton("Reset", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                SharedPreferences profilePrefs = getSharedPreferences(profilePrefsName, MODE_PRIVATE);
                                profilePrefs.edit().clear().apply();
                                Toast.makeText(SettingsActivity.this, "Profile " + profileNum + " reset", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            }
        });
    }


    private void updatePreview() {
        if (previewView == null) return;
        previewView.setBirdSize(parseFloat(birdSizeEditText, DEFAULT_BIRD_SIZE));
        previewView.setPipeGap(parseFloat(pipeGapEditText, DEFAULT_PIPE_GAP));
        previewView.setBirdColor(getBirdColorFromRadio());
        previewView.setBackground(getBackgroundFromRadio());
        previewView.setRainbowBird(rainbowBirdSwitch.isChecked());
        previewView.setWeather(getWeatherFromRadio());
        previewView.setGoldenPipe(goldenPipeSwitch.isChecked());
        previewView.setBgScrollSpeed(parseFloat(bgScrollEditText, DEFAULT_BG_SCROLL_SPEED));
        previewView.setGroundScrollSpeed(parseFloat(groundScrollEditText, DEFAULT_GROUND_SCROLL_SPEED));
        previewView.setGameSpeed(parseFloat(speedEditText, DEFAULT_GAME_SPEED));
        previewView.setPipeColor(getPipeColorFromRadio());
        previewView.setPipeWidth(parseFloat(pipeWidthEditText, DEFAULT_PIPE_WIDTH));
        previewView.setUpsideDown(upsideDownSwitch.isChecked());
        previewView.setReversePipes(reversePipesSwitch.isChecked());
        previewView.setDrunkMode(drunkModeSwitch.isChecked(), parseFloat(drunkModeStrengthEditText, DEFAULT_DRUNK_MODE_STRENGTH));
        previewView.setBirdTrail(birdTrailSwitch.isChecked());
        previewView.setGhostMode(ghostModeSwitch.isChecked());
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (previewView != null) {
            previewView.resume();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (previewView != null) {
            previewView.pause();
        }
    }

    static class SettingsPreviewView extends View implements Choreographer.FrameCallback {

        private Choreographer choreographer;
        private boolean isRunning = false;
        private long lastFrameTimeNanos = 0;
        private int width, height;

        private Bitmap bgDayBitmap, bgNightBitmap, groundBitmap, currentBgBitmap;
        private Bitmap pipeUpBitmap, pipeDownBitmap;
        private Bitmap[][] birdBitmaps = new Bitmap[3][3];
        private Bitmap currentBirdBitmap;
        private float groundX = 0, backgroundX = 0;
        private int groundHeight;
        private Paint pixelPaint = new Paint();
        private Paint pipePaint = new Paint();
        private Paint birdPaint = new Paint();
        private Paint goldenPaint = new Paint();
        private Paint weatherPaint = new Paint();
        private Paint lightningPaint = new Paint();
        private Paint lightningGlowPaint = new Paint();
        private Paint trailPaint = new Paint();

        private RectF groundDestRect = new RectF();
        private RectF pipeDestRect = new RectF();
        private Matrix birdMatrix = new Matrix();

        // Settings state variables
        private float birdSize = 1.0f;
        private float pipeGapMultiplier = 1.0f;
        private int birdColor = -1;
        private int background = -1;
        private int weather = 0;
        private boolean isRainbowBird = false;
        private boolean isGoldenPipe = false;
        private float bgScrollSpeed = 1.0f;
        private float groundScrollSpeed = 1.0f;
        private float gameSpeed = 1.0f;
        private int pipeColor = 0;
        private float pipeWidthMultiplier = 1.0f;
        private boolean isUpsideDown = false;
        private boolean isReversePipes = false;
        private boolean isDrunkMode = false;
        private float drunkModeStrength = 0.0f;
        private boolean isBirdTrail = false;
        private boolean isGhostMode = false;

        private float birdX, birdY, baseBirdX;
        private float pipeX;
        private float rainbowHue = 0;
        private float goldenHue = 0;
        private float drunkModePhase = 0f;
        private List<float[]> rainParticles = new ArrayList<>();
        private List<LightningBolt> lightningBolts = new ArrayList<>();
        private Deque<TrailParticle> birdTrail;
        private Random random = new Random();
        private BitmapFactory.Options options = new BitmapFactory.Options();

        public SettingsPreviewView(Context context) {
            super(context);
            init(context);
        }

        private void init(Context context) {
            options.inScaled = false;
            pixelPaint.setFilterBitmap(false);
            pixelPaint.setAntiAlias(false);
            pipePaint.setFilterBitmap(false);
            pipePaint.setAntiAlias(false);
            birdPaint.setFilterBitmap(false);
            birdPaint.setAntiAlias(false);
            trailPaint.setFilterBitmap(false);
            trailPaint.setAntiAlias(false);

            weatherPaint.setColor(Color.WHITE);
            weatherPaint.setStrokeWidth(2);
            weatherPaint.setAlpha(150);

            lightningPaint.setColor(Color.WHITE);
            lightningPaint.setStrokeWidth(4);
            lightningPaint.setStyle(Paint.Style.STROKE);
            lightningPaint.setAntiAlias(true);

            lightningGlowPaint.set(lightningPaint);
            lightningGlowPaint.setStrokeWidth(15);
            lightningGlowPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_ATOP));
            lightningGlowPaint.setMaskFilter(new android.graphics.BlurMaskFilter(20, android.graphics.BlurMaskFilter.Blur.NORMAL));

            birdTrail = new ArrayDeque<>(15);
            choreographer = Choreographer.getInstance();
        }

        private Bitmap scaleBitmap(Bitmap bitmap, float scale) {
            if (bitmap == null) return null;
            int newW = (int) (bitmap.getWidth() * scale);
            int newH = (int) (bitmap.getHeight() * scale);
            if (newW <= 0 || newH <= 0) return null;
            return Bitmap.createScaledBitmap(bitmap, newW, newH, false);
        }


        @Override
        protected void onSizeChanged(int w, int h, int oldw, int oldh) {
            super.onSizeChanged(w, h, oldw, oldh);
            if (w == 0 || h == 0) return;

            this.width = w;
            this.height = h;

            Bitmap atlas = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.atlas, options);
            Bitmap unscaledBgDay = Bitmap.createBitmap(atlas, 0, 0, 288, 512);
            Bitmap unscaledBgNight = Bitmap.createBitmap(atlas, (int)(0.28515625 * 1024), 0, 288, 512);
            Bitmap unscaledLand = Bitmap.createBitmap(atlas, (int)(0.5703125 * 1024), 0, 336, 112);
            atlas.recycle();

            birdBitmaps[0][0] = BitmapFactory.decodeResource(getResources(), R.drawable.yellowbird_upflap, options);
            birdBitmaps[0][1] = BitmapFactory.decodeResource(getResources(), R.drawable.yellowbird_midflap, options);
            birdBitmaps[0][2] = BitmapFactory.decodeResource(getResources(), R.drawable.yellowbird_downflap, options);
            birdBitmaps[1][0] = BitmapFactory.decodeResource(getResources(), R.drawable.bluebird_upflap, options);
            birdBitmaps[1][1] = BitmapFactory.decodeResource(getResources(), R.drawable.bluebird_midflap, options);
            birdBitmaps[1][2] = BitmapFactory.decodeResource(getResources(), R.drawable.bluebird_downflap, options);
            birdBitmaps[2][0] = BitmapFactory.decodeResource(getResources(), R.drawable.redbird_upflap, options);
            birdBitmaps[2][1] = BitmapFactory.decodeResource(getResources(), R.drawable.redbird_midflap, options);
            birdBitmaps[2][2] = BitmapFactory.decodeResource(getResources(), R.drawable.redbird_downflap, options);

            Bitmap unscaledPipeGreen = BitmapFactory.decodeResource(getResources(), R.drawable.pipe_green, options);

            float scale = (float) this.height / unscaledBgDay.getHeight();
            int bgWidth = (int) (unscaledBgDay.getWidth() * scale);

            bgDayBitmap = Bitmap.createScaledBitmap(unscaledBgDay, bgWidth, this.height, false);
            bgNightBitmap = Bitmap.createScaledBitmap(unscaledBgNight, bgWidth, this.height, false);
            groundBitmap = scaleBitmap(unscaledLand, scale);
            if(groundBitmap != null) groundHeight = groundBitmap.getHeight();

            pipeUpBitmap = scaleBitmap(unscaledPipeGreen, scale);
            if (pipeUpBitmap != null) {
                Matrix flipMatrix = new Matrix();
                flipMatrix.setScale(1, -1);
                pipeDownBitmap = Bitmap.createBitmap(pipeUpBitmap, 0, 0, pipeUpBitmap.getWidth(), pipeUpBitmap.getHeight(), flipMatrix, true);
            }

            for(int i = 0; i < 3; i++){
                for(int j=0; j<3; j++) {
                    birdBitmaps[i][j] = scaleBitmap(birdBitmaps[i][j], scale);
                }
            }

            baseBirdX = this.width / 3f;
            pipeX = this.width * 0.7f;

            rainParticles.clear();
            for (int i=0; i < 50; i++) {
                rainParticles.add(new float[]{random.nextFloat() * width, random.nextFloat() * height, 1.0f + random.nextFloat() * 2.0f});
            }

            resume();
        }

        public void setBirdSize(float size) { this.birdSize = Math.max(0.1f, size); invalidate(); }
        public void setPipeGap(float gap) { this.pipeGapMultiplier = Math.max(0.1f, gap); invalidate(); }
        public void setBirdColor(int color) { this.birdColor = color; invalidate(); }
        public void setBackground(int bg) { this.background = bg; invalidate(); }
        public void setRainbowBird(boolean isRainbow) { this.isRainbowBird = isRainbow; invalidate(); }
        public void setWeather(int weather) { this.weather = weather; invalidate(); }
        public void setGoldenPipe(boolean isGolden) { this.isGoldenPipe = isGolden; invalidate(); }
        public void setBgScrollSpeed(float speed) { this.bgScrollSpeed = speed; invalidate(); }
        public void setGroundScrollSpeed(float speed) { this.groundScrollSpeed = speed; invalidate(); }
        public void setGameSpeed(float speed) { this.gameSpeed = speed; invalidate(); }
        public void setPipeColor(int color) { this.pipeColor = color; invalidate(); }
        public void setPipeWidth(float width) { this.pipeWidthMultiplier = width; invalidate(); }
        public void setUpsideDown(boolean enabled) { this.isUpsideDown = enabled; invalidate(); }
        public void setReversePipes(boolean enabled) { this.isReversePipes = enabled; invalidate(); }
        public void setDrunkMode(boolean enabled, float strength) { this.isDrunkMode = enabled; this.drunkModeStrength = strength; invalidate(); }
        public void setBirdTrail(boolean enabled) { this.isBirdTrail = enabled; if (!enabled) birdTrail.clear(); invalidate(); }
        public void setGhostMode(boolean enabled) { this.isGhostMode = enabled; invalidate(); }

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
            if (deltaTime > (1.0f / 30.0f)) deltaTime = (1.0f / 30.0f);

            float basePipeSpeed = 100f * gameSpeed;
            float baseBgSpeed = basePipeSpeed * 0.5f;

            float direction = isReversePipes ? -1 : 1;

            pipeX -= (basePipeSpeed * direction * deltaTime);
            if (isReversePipes) {
                if (pipeX > width) pipeX = -pipeUpBitmap.getWidth();
            } else {
                if (pipeX < -pipeUpBitmap.getWidth()) pipeX = width;
            }

            if(currentBgBitmap != null) {
                backgroundX -= (baseBgSpeed * bgScrollSpeed * direction * deltaTime);
                if (direction > 0 && backgroundX <= -currentBgBitmap.getWidth()) backgroundX += currentBgBitmap.getWidth();
                if (direction < 0 && backgroundX >= currentBgBitmap.getWidth()) backgroundX -= currentBgBitmap.getWidth();
            }

            if (groundBitmap != null) {
                groundX -= (basePipeSpeed * groundScrollSpeed * direction * deltaTime);
                if (direction > 0 && groundX <= -groundBitmap.getWidth()) groundX += groundBitmap.getWidth();
                if (direction < 0 && groundX >= groundBitmap.getWidth()) groundX -= groundBitmap.getWidth();
            }

            baseBirdX = isReversePipes ? width * 2 / 3f : width / 3f;
            birdY = (height - groundHeight) / 2f + (float) (Math.sin(System.currentTimeMillis() / 200.0) * 10);

            if (isDrunkMode) {
                drunkModePhase += deltaTime * 2.0f;
                float maxDrift = width * 0.15f * drunkModeStrength;
                float drift = (float)Math.sin(drunkModePhase) * maxDrift;
                birdX = baseBirdX + drift;
            } else {
                birdX = baseBirdX;
            }

            rainbowHue = (rainbowHue + 150 * deltaTime) % 360;
            goldenHue = (goldenHue + 250 * deltaTime) % 360;

            if (isBirdTrail) {
                if (birdTrail.size() >= 15) {
                    birdTrail.pollFirst();
                }
                birdTrail.add(new TrailParticle(birdX, birdY, 0, birdColor, (int)((System.currentTimeMillis()/150)%3), isRainbowBird ? rainbowHue : -1));
            }

            if(weather == 1 || weather == 2) {
                for(float[] p : rainParticles) {
                    p[1] += p[2] * 200 * deltaTime;
                    if(p[1] > height) {
                        p[1] = 0;
                        p[0] = random.nextFloat() * width;
                    }
                }
            }

            if (weather == 2) {
                if (random.nextInt(150) == 0 && lightningBolts.isEmpty()) {
                    lightningBolts.add(new LightningBolt(width, height));
                }
            }

            for (int i = lightningBolts.size() - 1; i >= 0; i--) {
                if (lightningBolts.get(i).isFinished()) {
                    lightningBolts.remove(i);
                } else {
                    lightningBolts.get(i).update(deltaTime);
                }
            }

            invalidate();
            choreographer.postFrameCallback(this);
        }

        private ColorFilter createPipeColorFilter(int pipeColorSetting) {
            if(isReversePipes && pipeColorSetting == 0) {
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

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            if (canvas == null || groundBitmap == null || pipeUpBitmap == null || pipeDownBitmap == null) return;

            canvas.save();

            if (isUpsideDown) {
                canvas.scale(1, -1, width / 2f, height / 2f);
            }

            int cBirdColor = birdColor;
            if(cBirdColor == -1) cBirdColor = (int)((System.currentTimeMillis()/1000) % 3);

            int frame = (int)((System.currentTimeMillis()/150)%3);
            currentBirdBitmap = birdBitmaps[cBirdColor][frame];
            if (currentBirdBitmap == null) {
                canvas.restore();
                return;
            }

            int cBg = background;
            if (cBg == -1) cBg = ((int)(System.currentTimeMillis()/5000) % 2);
            currentBgBitmap = cBg == 0 ? bgDayBitmap : bgNightBitmap;
            if (currentBgBitmap == null) {
                canvas.restore();
                return;
            }

            // Draw Background
            float bgWidth = currentBgBitmap.getWidth();
            float startX = backgroundX % bgWidth;
            if(isReversePipes) {
                if(startX < 0) startX += bgWidth;
                for (float x = startX - bgWidth; x < width; x += bgWidth) {
                    canvas.drawBitmap(currentBgBitmap, x, 0, pixelPaint);
                }
            } else {
                if(startX > 0) startX -= bgWidth;
                for (float x = startX; x < width; x += bgWidth) {
                    canvas.drawBitmap(currentBgBitmap, x, 0, pixelPaint);
                }
            }


            // Draw Pipes
            pipePaint.setColorFilter(createPipeColorFilter(pipeColor));
            float visualPipeWidth = pipeUpBitmap.getWidth() * pipeWidthMultiplier;
            float pipeDrawX = pipeX - (visualPipeWidth - pipeUpBitmap.getWidth()) / 2f;

            float baseGap = currentBirdBitmap.getHeight() * 2.5f;
            float pipeGap = baseGap * pipeGapMultiplier;
            float pipeCenterY = (height - groundHeight) / 2f;
            float topPipeY = pipeCenterY - (pipeGap / 2f) - pipeUpBitmap.getHeight();
            float bottomPipeY = pipeCenterY + (pipeGap / 2f);

            pipeDestRect.set(pipeDrawX, topPipeY, pipeDrawX + visualPipeWidth, topPipeY + pipeUpBitmap.getHeight());
            canvas.drawBitmap(pipeDownBitmap, null, pipeDestRect, pipePaint);
            pipeDestRect.set(pipeDrawX, bottomPipeY, pipeDrawX + visualPipeWidth, bottomPipeY + pipeUpBitmap.getHeight());
            canvas.drawBitmap(pipeUpBitmap, null, pipeDestRect, pipePaint);

            if(isGoldenPipe) {
                int[] colors = {Color.argb(0, 255, 215, 0), Color.argb(150, 255, 255, 100), Color.argb(0, 255, 215, 0)};
                float[] positions = {0, 0.5f, 1};
                Matrix gradientMatrix = new Matrix();
                gradientMatrix.setTranslate(goldenHue, 0);
                Shader shader = new LinearGradient(pipeDrawX, 0, pipeDrawX + visualPipeWidth, 0, colors, positions, Shader.TileMode.MIRROR);
                shader.setLocalMatrix(gradientMatrix);
                goldenPaint.setShader(shader);

                pipeDestRect.set(pipeDrawX, topPipeY, pipeDrawX + visualPipeWidth, topPipeY + pipeUpBitmap.getHeight());
                canvas.drawBitmap(pipeDownBitmap, null, pipeDestRect, goldenPaint);
                pipeDestRect.set(pipeDrawX, bottomPipeY, pipeDrawX + visualPipeWidth, bottomPipeY + pipeUpBitmap.getHeight());
                canvas.drawBitmap(pipeUpBitmap, null, pipeDestRect, goldenPaint);
            }

            // Draw Lightning before Ground/Bird
            for (LightningBolt bolt : lightningBolts) {
                bolt.draw(canvas, lightningPaint, lightningGlowPaint);
            }

            // Draw Ground
            float groundDrawWidth = groundBitmap.getWidth();
            float groundTopY = height - groundHeight;
            startX = groundX % groundDrawWidth;
            if(isReversePipes) {
                if(startX < 0) startX += groundDrawWidth;
                for (float x = startX - groundDrawWidth; x < width; x += groundDrawWidth) {
                    groundDestRect.set(x, groundTopY, x + groundDrawWidth, height);
                    canvas.drawBitmap(groundBitmap, null, groundDestRect, pixelPaint);
                }
            } else {
                if(startX > 0) startX -= groundDrawWidth;
                for (float x = startX; x < width; x += groundDrawWidth) {
                    groundDestRect.set(x, groundTopY, x + groundDrawWidth, height);
                    canvas.drawBitmap(groundBitmap, null, groundDestRect, pixelPaint);
                }
            }


            // Draw Bird Trail
            if (isBirdTrail) {
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

                    Bitmap trailBitmap = birdBitmaps[particle.colorIndex < 0 ? cBirdColor : particle.colorIndex][particle.frameIndex];
                    if (trailBitmap != null) {
                        birdMatrix.reset();
                        birdMatrix.postTranslate(-trailBitmap.getWidth() / 2f, -trailBitmap.getHeight() / 2f);
                        birdMatrix.postRotate(particle.rotation);
                        birdMatrix.postScale(birdSize, birdSize, 0, 0);
                        birdMatrix.postTranslate(particle.x, particle.y);
                        canvas.drawBitmap(trailBitmap, birdMatrix, trailPaint);
                    }
                    i++;
                }
            }

            // Draw Bird
            birdPaint.setColorFilter(null);
            if(isRainbowBird) {
                ColorMatrix colorMatrix = new ColorMatrix();
                colorMatrix.setRotate(0, rainbowHue);
                colorMatrix.setRotate(1, rainbowHue);
                colorMatrix.setRotate(2, rainbowHue);
                birdPaint.setColorFilter(new ColorMatrixColorFilter(colorMatrix));
            }
            birdPaint.setAlpha(isGhostMode ? 128 : 255);
            birdMatrix.reset();
            birdMatrix.postTranslate(-currentBirdBitmap.getWidth() / 2f, -currentBirdBitmap.getHeight() / 2f);
            birdMatrix.postScale(birdSize, birdSize, 0, 0);
            birdMatrix.postTranslate(birdX, birdY);
            canvas.drawBitmap(currentBirdBitmap, birdMatrix, birdPaint);

            // Draw Weather Effects on top
            if (weather == 1 || weather == 2) {
                for(float[] p : rainParticles) {
                    canvas.drawLine(p[0], p[1], p[0], p[1] + 20, weatherPaint);
                }
            }

            canvas.restore();
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
        }

        static class TrailParticle {
            float x, y, rotation, rainbowHue;
            int colorIndex, frameIndex;

            TrailParticle(float x, float y, float rotation, int colorIndex, int frameIndex, float rainbowHue) {
                this.x = x; this.y = y; this.rotation = rotation;
                this.colorIndex = colorIndex; this.frameIndex = frameIndex;
                this.rainbowHue = rainbowHue;
            }
        }

        static class LightningBolt {
            private Path path;
            private float lifetime = 0.4f; // total duration
            private float alpha = 1.0f;
            private float flashDuration = 0.1f;
            private float x, y;
            private float radius;
            private Paint flashPaint = new Paint();
            private Random random = new Random();

            LightningBolt(int screenWidth, int screenHeight) {
                path = new Path();
                float startX = random.nextFloat() * screenWidth;
                float startY = 0;
                path.moveTo(startX, startY);

                this.x = startX;
                this.y = startY;
                this.radius = screenWidth/4f;

                float currentY = 0;
                while (currentY < screenHeight) {
                    float nextX = startX + (random.nextFloat() - 0.5f) * 80;
                    float nextY = currentY + random.nextFloat() * (screenHeight / 5f);
                    path.lineTo(nextX, nextY);
                    currentY = nextY;

                    // Random branches
                    if (random.nextInt(5) == 0) {
                        float branchX = nextX + (random.nextFloat() - 0.5f) * 100;
                        float branchY = nextY + random.nextFloat() * (screenHeight / 6f);
                        path.moveTo(nextX, nextY);
                        path.lineTo(branchX, branchY);
                        path.moveTo(nextX, nextY);
                    }
                }
                flashPaint.setStyle(Paint.Style.FILL);
            }

            void update(float deltaTime) {
                lifetime -= deltaTime;
                if (lifetime < 0) {
                    alpha = 0;
                } else {
                    // Fade out quickly after the initial flash
                    alpha = Math.max(0, (lifetime / 0.3f));
                }
            }

            boolean isFinished() {
                return lifetime <= 0;
            }

            void draw(Canvas canvas, Paint mainPaint, Paint glowPaint) {
                if (alpha <= 0) return;

                // Draw sky flash
                if (lifetime > (0.4f - flashDuration)) {
                    int flashAlpha = (int)(200 * alpha);
                    flashPaint.setShader(new RadialGradient(x, y, radius,
                            Color.argb(flashAlpha, 200, 200, 255), Color.TRANSPARENT, Shader.TileMode.CLAMP));
                    canvas.drawPaint(flashPaint);
                }

                // Draw bolt
                mainPaint.setAlpha((int)(255 * alpha));
                glowPaint.setAlpha((int)(150 * alpha));
                canvas.drawPath(path, glowPaint);
                canvas.drawPath(path, mainPaint);
            }
        }
    }
}