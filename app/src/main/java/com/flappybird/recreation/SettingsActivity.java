package com.flappybird.recreation;

import android.animation.ValueAnimator;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
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
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.PorterDuffXfermode;
import android.graphics.RadialGradient;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RippleDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.transition.AutoTransition;
import android.transition.TransitionManager;
import android.util.DisplayMetrics;
import android.view.Choreographer;
import android.view.Gravity;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.core.widget.NestedScrollView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.slider.LabelFormatter;
import com.google.android.material.slider.Slider;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;

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
    public static final String PREF_DYNAMIC_DAY_NIGHT_ENABLED = "pref_dynamic_day_night_enabled";
    public static final String PREF_SCREEN_SHAKE_ENABLED = "pref_screen_shake_enabled";
    public static final String PREF_SCREEN_SHAKE_STRENGTH = "pref_screen_shake_strength";
    public static final String PREF_GOLDEN_PIPE_ENABLED = "pref_golden_pipe_enabled";
    public static final String PREF_GOLDEN_PIPE_CHANCE = "pref_golden_pipe_chance";
    public static final String PREF_GOLDEN_PIPE_BONUS = "pref_golden_pipe_bonus";
    public static final String PREF_MOTION_BLUR_ENABLED = "pref_motion_blur_enabled";
    public static final String PREF_MOTION_BLUR_STRENGTH = "pref_motion_blur_strength";
    public static final String PREF_ORIGINAL_MODE_ENABLED = "pref_original_mode_enabled";
    public static final String PREF_LIGHTNING_SCORES = "pref_lightning_scores";
    public static final String PREF_MEGA_STRIKE_SCORE = "pref_mega_strike_score";
    public static final String PREF_EXTRA_LIVES = "pref_extra_lives";
    public static final String PREF_AUTOPILOT_ENABLED = "pref_autopilot_enabled";
    public static final String PREF_PAUSE_BUTTON_ENABLED = "pref_pause_button_enabled";
    public static final String PREF_AUTO_PAUSE_ENABLED = "pref_auto_pause_enabled";
    public static final String PREF_SPEED_RAMP = "pref_speed_ramp_percent";
    public static final String PREF_GAP_SHRINK = "pref_gap_shrink_percent";
    public static final String PREF_BIRD_HUE = "pref_bird_hue";
    public static final String PREF_MEDAL_BRONZE_SCORE = "pref_medal_bronze_score";
    public static final String PREF_MEDAL_SILVER_SCORE = "pref_medal_silver_score";
    public static final String PREF_MEDAL_GOLD_SCORE = "pref_medal_gold_score";
    public static final String PREF_MEDAL_PLATINUM_SCORE = "pref_medal_platinum_score";
    public static final String PREF_CRASH_FLASH_ENABLED = "pref_crash_flash_enabled";
    public static final String PREF_CRASH_FLASH_COLOR = "pref_crash_flash_color";
    public static final String PREF_CRASH_FLASH_STRENGTH = "pref_crash_flash_strength";
    public static final String PREF_CRASH_FLASH_LENGTH = "pref_crash_flash_length_ms";
    public static final String PREF_FIXED_STEP_ENABLED = "pref_fixed_step_enabled";
    public static final String PREF_MAX_FALL_SPEED = "pref_max_fall_speed";
    public static final String PREF_CEILING_ENABLED = "pref_ceiling_enabled";
    public static final String PREF_BIRD_TILT_ENABLED = "pref_bird_tilt_enabled";
    public static final String PREF_MAX_TILT_UP = "pref_max_tilt_up";
    public static final String PREF_MAX_TILT_DOWN = "pref_max_tilt_down";
    public static final String PREF_TILT_SPEED = "pref_tilt_speed";
    public static final String PREF_SCORE_POINT = "pref_score_point";
    public static final String PREF_PIXEL_STEPS_ENABLED = "pref_pixel_steps_enabled";
    public static final String PREF_TILT_STYLE = "pref_tilt_style";
    public static final String PREF_FLAP_LIMIT_ENABLED = "pref_flap_limit_enabled";
    public static final String PREF_HITBOX_SHAPE = "pref_hitbox_shape";
    public static final String PREF_GAP_PLACEMENT = "pref_gap_placement";
    public static final String PREF_CRASH_FLASH_FADE = "pref_crash_flash_fade";
    public static final String PREF_CRASH_GRAVITY = "pref_crash_gravity";
    public static final String PREF_BIRD_POSITION = "pref_bird_position_percent";
    public static final String PREF_START_HEIGHT = "pref_start_height_percent";
    public static final String PREF_WING_SPEED = "pref_wing_speed";
    public static final String PREF_SOUND_VOLUME = "pref_sound_volume";
    public static final String PREF_WEATHER_VOLUME = "pref_weather_volume";
    public static final String PREF_DAY_NIGHT_LENGTH = "pref_day_night_seconds";
    public static final String PREF_SHOW_HITBOXES = "pref_show_hitboxes";
    public static final String PREF_SHOW_FPS = "pref_show_fps";
    public static final String PREF_RAMP_TOP_SPEED = "pref_ramp_top_speed";
    public static final String PREF_RAMP_MIN_GAP = "pref_ramp_min_gap_percent";
    public static final String PREF_CLASSIC_MENU_ENABLED = "pref_classic_menu_enabled";

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
    public static final boolean DEFAULT_DYNAMIC_DAY_NIGHT_ENABLED = false;
    public static final boolean DEFAULT_SCREEN_SHAKE_ENABLED = false;
    public static final float DEFAULT_SCREEN_SHAKE_STRENGTH = 1.0f;
    public static final boolean DEFAULT_GOLDEN_PIPE_ENABLED = false;
    public static final int DEFAULT_GOLDEN_PIPE_CHANCE = 5;
    public static final int DEFAULT_GOLDEN_PIPE_BONUS = 10;
    public static final boolean DEFAULT_MOTION_BLUR_ENABLED = false;
    public static final float DEFAULT_MOTION_BLUR_STRENGTH = 1.0f;
    public static final boolean DEFAULT_ORIGINAL_MODE_ENABLED = false;
    public static final String DEFAULT_LIGHTNING_SCORES = "11,34,79,210";
    public static final int DEFAULT_MEGA_STRIKE_SCORE = 350;
    public static final int DEFAULT_EXTRA_LIVES = 0;
    public static final boolean DEFAULT_AUTOPILOT_ENABLED = false;
    public static final boolean DEFAULT_PAUSE_BUTTON_ENABLED = false;
    public static final boolean DEFAULT_AUTO_PAUSE_ENABLED = true;
    public static final int DEFAULT_SPEED_RAMP = 0;
    public static final int DEFAULT_GAP_SHRINK = 0;
    public static final int DEFAULT_BIRD_HUE = 0;
    public static final int DEFAULT_MEDAL_BRONZE_SCORE = 10;
    public static final int DEFAULT_MEDAL_SILVER_SCORE = 20;
    public static final int DEFAULT_MEDAL_GOLD_SCORE = 30;
    public static final int DEFAULT_MEDAL_PLATINUM_SCORE = 40;
    public static final boolean DEFAULT_CRASH_FLASH_ENABLED = true;
    public static final int DEFAULT_CRASH_FLASH_COLOR = 0;
    public static final int DEFAULT_CRASH_FLASH_STRENGTH = 100;
    public static final int DEFAULT_CRASH_FLASH_LENGTH = 170;
    public static final boolean DEFAULT_FIXED_STEP_ENABLED = false;
    public static final float DEFAULT_MAX_FALL_SPEED = 0f;
    public static final boolean DEFAULT_CEILING_ENABLED = true;
    public static final boolean DEFAULT_BIRD_TILT_ENABLED = true;
    public static final int DEFAULT_MAX_TILT_UP = 25;
    public static final int DEFAULT_MAX_TILT_DOWN = 90;
    public static final float DEFAULT_TILT_SPEED = 1.0f;
    public static final int DEFAULT_SCORE_POINT = 0;
    public static final boolean DEFAULT_PIXEL_STEPS_ENABLED = false;
    public static final int DEFAULT_TILT_STYLE = 0;
    public static final boolean DEFAULT_FLAP_LIMIT_ENABLED = false;
    public static final int DEFAULT_HITBOX_SHAPE = 0;
    public static final int DEFAULT_GAP_PLACEMENT = 0;
    public static final int DEFAULT_CRASH_FLASH_FADE = 0;
    public static final float DEFAULT_CRASH_GRAVITY = 1.5f;
    public static final int DEFAULT_BIRD_POSITION = 33;
    public static final int DEFAULT_START_HEIGHT = 50;
    public static final float DEFAULT_WING_SPEED = 1.0f;
    public static final int DEFAULT_SOUND_VOLUME = 100;
    public static final int DEFAULT_WEATHER_VOLUME = 100;
    public static final int DEFAULT_DAY_NIGHT_LENGTH = 120;
    public static final boolean DEFAULT_SHOW_HITBOXES = false;
    public static final boolean DEFAULT_SHOW_FPS = false;
    public static final float DEFAULT_RAMP_TOP_SPEED = 3.0f;
    public static final int DEFAULT_RAMP_MIN_GAP = 55;
    public static final boolean DEFAULT_CLASSIC_MENU_ENABLED = true;

    public static final int SCORE_POINT_END = 0;
    public static final int SCORE_POINT_MIDDLE = 1;
    public static final int SCORE_POINT_START = 2;
    public static final int SCORE_POINT_CLASSIC = 3;
    public static final int TILT_STYLE_SMOOTH = 0;
    public static final int TILT_STYLE_CLASSIC = 1;
    public static final int HITBOX_SHAPE_BIRD = 0;
    public static final int HITBOX_SHAPE_SQUARE = 1;
    public static final int GAP_PLACEMENT_EVEN = 0;
    public static final int GAP_PLACEMENT_CLASSIC = 1;
    public static final int FLASH_FADE_EVEN = 0;
    public static final int FLASH_FADE_SHARP = 1;

    private static final String PROFILE_PREFS_PREFIX = "FlappyBirdProfile_";
    private static final String PROFILE_SAVED_AT = "profile_saved_at";
    private static final String STATE_PAGE = "settings_page";
    private static final String KEY_HIGH_SCORE = "highScore";
    private static final String EXPORT_FORMAT = "flappy-bird-settings";
    private static final int EXPORT_VERSION = 1;
    private static final int REQUEST_EXPORT = 4101;
    private static final int REQUEST_IMPORT = 4102;
    private static final int MAX_IMPORT_BYTES = 512 * 1024;
    private static final float CLASSIC_GRAVITY = 0.75f;
    private static final float CLASSIC_FLAP = 0.78f;
    private static final float CLASSIC_SPEED = 0.91f;
    private static final float CLASSIC_GAP = 0.91f;
    private static final float CLASSIC_SPACING = 0.8f;
    private static final float CLASSIC_MAX_FALL_SPEED = 1.25f;
    private static final float CLASSIC_WING_SPEED = 1.7f;
    private static final int CLASSIC_TILT_UP = 20;
    private static final int CLASSIC_TILT_DOWN = 90;
    private static final int CLASSIC_FLASH_LENGTH = 1000;
    private static final int CLASSIC_BIRD_POSITION = 31;
    private static final int CLASSIC_START_HEIGHT = 36;
    private static final String CLASSIC_SUMMARY = "Matches the flight, pipes and scoring of the 2013 Android game";
    private static final int TAB_COUNT = 9;
    private static final int ADVANCED_PAGE = 7;
    private static final int PRESETS_PAGE = 8;
    private static final int[] CRASH_FLASH_COLORS = {0xFFFFFFFF, 0xFFFF3B30, 0xFFFFD23F, 0xFF000000};
    private static final String TAG_SWITCH = "switch";
    private static final String TAG_SLIDER = "slider";
    private static final String TAG_VALUE = "value";
    private static final String TAG_OPTION = "option";
    private static final int PROFILE_COUNT = 3;
    private static final long SAVE_DELAY_MS = 250L;
    private static final long CAPTION_VISIBLE_MS = 2200L;
    private static final int MAX_LIGHTNING_SCORES = 24;
    private static final int MAX_SCORE_VALUE = 1000000;

    private static final int WEATHER_STORM = 2;
    private static final int WEATHER_SNOW = 3;

    private static final int UNIT_TIMES = 0;
    private static final int UNIT_PERCENT = 1;
    private static final int UNIT_POINTS = 2;
    private static final int UNIT_SCORE = 3;
    private static final int UNIT_DEGREES = 4;
    private static final int UNIT_MS = 5;
    private static final int UNIT_SECONDS = 6;

    private static final int COLOR_ROW = 0xFF182134;
    private static final int COLOR_HIGH = 0xFF253049;
    private static final int COLOR_ON = 0xFFE8ECF6;
    private static final int COLOR_ON_VARIANT = 0xFFA4AEC4;
    private static final int COLOR_PRIMARY = 0xFFFFD23F;
    private static final int COLOR_ON_PRIMARY = 0xFF2B2100;
    private static final int COLOR_PRIMARY_CONTAINER = 0xFF4A3D0A;
    private static final int COLOR_ON_PRIMARY_CONTAINER = 0xFFFFE38A;
    private static final int COLOR_OUTLINE = 0xFF8791A8;
    private static final int COLOR_TRACK_OFF = 0xFF2E3A57;
    private static final int COLOR_RIPPLE = 0x29FFFFFF;
    private static final int COLOR_PRIMARY_RIPPLE = 0x33FFD23F;
    private static final int COLOR_SELECTION = 0x66FFD23F;
    private static final int COLOR_SKY = 0xFF63D2E0;
    private static final int COLOR_CAPTION = 0xE60D1321;
    private static final int COLOR_SNACKBAR = 0xFFE8ECF6;
    private static final int COLOR_ON_SNACKBAR = 0xFF0D1321;
    private static final int COLOR_SNACKBAR_ACTION = 0xFF7A5900;
    private static final int COLOR_ON_ACCENT = 0xFF101522;

    private static final int[] TAB_IDS = {
            R.id.tab_bird, R.id.tab_world, R.id.tab_pipes, R.id.tab_physics,
            R.id.tab_effects, R.id.tab_modes, R.id.tab_system, R.id.tab_presets};

    private final Random random = new Random();
    private final List<Setting> settings = new ArrayList<>();
    private final Map<String, Setting> settingsByKey = new HashMap<>();
    private final Set<Setting> unsaved = new LinkedHashSet<>();
    private final TextView[] tabs = new TextView[TAB_COUNT];
    private final View[] pages = new View[TAB_COUNT];
    private final ChipStyle[] tabStyles = new ChipStyle[TAB_COUNT];
    private final TextView[] slotSummaries = new TextView[PROFILE_COUNT];
    private final Button[] slotLoadButtons = new Button[PROFILE_COUNT];
    private final Button[] slotClearButtons = new Button[PROFILE_COUNT];

    private SharedPreferences prefs;
    private SettingsPreviewView previewView;
    private float density;
    private View root;
    private LinearLayout panes;
    private LinearLayout previewPane;
    private LinearLayout settingsPane;
    private View previewCard;
    private HorizontalScrollView categoryScroll;
    private NestedScrollView scrollView;
    private ViewGroup content;
    private TextView caption;
    private TextView highScoreSummary;
    private TextView classicSummary;
    private int currentPage;
    private int lastWidth = -1;
    private int lastHeight = -1;

    private ToggleSetting wingAnimation;
    private ToggleSetting dayNight;
    private ToggleSetting randomPipeColors;
    private ToggleSetting movingPipes;
    private ToggleSetting goldenPipes;
    private ToggleSetting screenShake;
    private ToggleSetting motionBlur;
    private ToggleSetting drunkMode;
    private ToggleSetting vibration;
    private ChoiceSetting background;
    private ChoiceSetting weather;
    private ChoiceSetting pipeColor;
    private NumberSetting drunkStrength;
    private ToggleSetting crashFlash;
    private ToggleSetting fixedStep;
    private ToggleSetting birdTilt;
    private NumberSetting maxFall;
    private ToggleSetting sound;

    private final Runnable saveRunnable = () -> flushSave();
    private final Runnable hideCaptionRunnable = () -> caption.animate().alpha(0f).setDuration(250L).start();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        setTheme(R.style.Theme_Material3Expressive_Dark_NoActionBar);
        super.onCreate(savedInstanceState);
        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        density = metrics.density;

        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        setContentView(R.layout.settings);
        hideSystemBars();

        bindLayout();
        createPreview();
        buildPages();
        createSettings();
        bindPresets();

        readAll(prefs.getAll());
        syncAllViews();
        refreshDependencies(false, true);
        refreshSlots();
        pushPreview();
        selectPage(savedInstanceState != null ? savedInstanceState.getInt(STATE_PAGE, 0) : 0, false);
        applyPaneLayout(metrics.widthPixels, metrics.heightPixels);

        if (!isDisclaimerShown()) showDisclaimerDialog();
    }

    @Override
    protected void onResume() {
        super.onResume();
        hideSystemBars();
        if (previewView != null) previewView.setHostResumed(true);
    }

    @Override
    protected void onPause() {
        super.onPause();
        flushSave();
        if (previewView != null) previewView.setHostResumed(false);
    }

    @Override
    protected void onDestroy() {
        if (root != null) {
            root.removeCallbacks(saveRunnable);
            root.removeCallbacks(hideCaptionRunnable);
        }
        super.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(STATE_PAGE, currentPage);
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

    private boolean isDisclaimerShown() {
        Object shown = prefs.getAll().get(PREF_SETTINGS_DISCLAIMER_SHOWN);
        return shown instanceof Boolean && (Boolean) shown;
    }

    private void showDisclaimerDialog() {
        showDialog(new MaterialAlertDialogBuilder(this)
                .setTitle("Settings Disclaimer")
                .setMessage("Adjusting settings can potentially make the game unstable. Proceed with caution.")
                .setPositiveButton("I Understand", (dialog, which) ->
                        prefs.edit().putBoolean(PREF_SETTINGS_DISCLAIMER_SHOWN, true).apply())
                .setCancelable(false)
                );
    }

    private int dp(float value) {
        return Math.round(value * density);
    }

    private static int blend(int from, int to, float amount) {
        float t = Math.max(0f, Math.min(1f, amount));
        int a = Math.round(Color.alpha(from) + (Color.alpha(to) - Color.alpha(from)) * t);
        int r = Math.round(Color.red(from) + (Color.red(to) - Color.red(from)) * t);
        int g = Math.round(Color.green(from) + (Color.green(to) - Color.green(from)) * t);
        int b = Math.round(Color.blue(from) + (Color.blue(to) - Color.blue(from)) * t);
        return Color.argb(a, r, g, b);
    }

    private static GradientDrawable rounded(int color, float radius) {
        GradientDrawable shape = new GradientDrawable();
        shape.setColor(color);
        shape.setCornerRadius(radius);
        return shape;
    }

    private static Drawable rowBackground(float top, float bottom, boolean clickable) {
        GradientDrawable shape = new GradientDrawable();
        shape.setColor(COLOR_ROW);
        shape.setCornerRadii(new float[]{top, top, top, top, bottom, bottom, bottom, bottom});
        if (!clickable) return shape;
        return new RippleDrawable(ColorStateList.valueOf(COLOR_RIPPLE), shape, null);
    }

    private static void collectTagged(View view, String tag, List<View> out) {
        if (tag.equals(view.getTag())) out.add(view);
        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            for (int i = 0; i < group.getChildCount(); i++) collectTagged(group.getChildAt(i), tag, out);
        }
    }

    private static String firstText(View view) {
        if (view instanceof TextView) return ((TextView) view).getText().toString();
        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            for (int i = 0; i < group.getChildCount(); i++) {
                String text = firstText(group.getChildAt(i));
                if (text != null && text.length() > 0) return text;
            }
        }
        return null;
    }

    private void bindLayout() {
        root = findViewById(R.id.settings_container);
        panes = findViewById(R.id.settings_panes);
        previewPane = findViewById(R.id.pane_preview);
        settingsPane = findViewById(R.id.pane_settings);
        previewCard = findViewById(R.id.preview_card);
        categoryScroll = findViewById(R.id.category_scroll);
        scrollView = findViewById(R.id.settings_scrollview);
        content = findViewById(R.id.settings_content_layout);
        caption = findViewById(R.id.preview_caption);

        ImageView back = findViewById(R.id.button_back);
        GradientDrawable backShape = new GradientDrawable();
        backShape.setShape(GradientDrawable.OVAL);
        backShape.setColor(COLOR_HIGH);
        back.setBackground(new RippleDrawable(ColorStateList.valueOf(COLOR_RIPPLE), backShape, null));
        back.setImageDrawable(new ArrowDrawable(dp(22), dp(2.5f), COLOR_ON));
        back.setOnClickListener(v -> finish());

        View badge = findViewById(R.id.preview_badge);
        badge.setBackground(rounded(COLOR_SKY, dp(12)));
        caption.setBackground(rounded(COLOR_CAPTION, dp(16)));

        TextView presetsTab = findViewById(R.id.tab_presets);
        for (int i = 0; i < TAB_COUNT; i++) {
            final int index = i;
            if (i == ADVANCED_PAGE) tabs[i] = createAdvancedTab(presetsTab);
            else tabs[i] = findViewById(TAB_IDS[i < ADVANCED_PAGE ? i : i - 1]);
            tabStyles[i] = new ChipStyle(tabs[i], COLOR_ROW, COLOR_PRIMARY, COLOR_ON_VARIANT, COLOR_ON_PRIMARY,
                    dp(12), dp(20), 0, 0);
            tabs[i].setOnClickListener(v -> selectPage(index, true));
        }

        ViewCompat.setOnApplyWindowInsetsListener(root, (view, windowInsets) -> {
            Insets safe = windowInsets.getInsets(
                    WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.displayCutout());
            view.setPadding(safe.left, safe.top, safe.right, safe.bottom);
            return WindowInsetsCompat.CONSUMED;
        });

        root.addOnLayoutChangeListener((view, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> {
            final int width = right - left - view.getPaddingLeft() - view.getPaddingRight();
            final int height = bottom - top - view.getPaddingTop() - view.getPaddingBottom();
            if (width == lastWidth && height == lastHeight) return;
            view.post(() -> applyPaneLayout(width, height));
        });
    }

    private TextView createAdvancedTab(TextView template) {
        TextView tab = new TextView(this);
        tab.setText("Advanced");
        tab.setTextSize(14);
        tab.setTypeface(template.getTypeface());
        tab.setGravity(Gravity.CENTER);
        tab.setMaxLines(1);
        tab.setPadding(template.getPaddingLeft(), template.getPaddingTop(), template.getPaddingRight(), template.getPaddingBottom());
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(WRAP, dp(40));
        params.setMarginEnd(dp(6));
        ViewGroup bar = (ViewGroup) template.getParent();
        int position = 0;
        while (position < bar.getChildCount() && bar.getChildAt(position) != template) position++;
        bar.addView(tab, position, params);
        return tab;
    }

    private void createPreview() {
        FrameLayout container = findViewById(R.id.live_preview_container);
        previewView = new SettingsPreviewView(this);
        container.addView(previewView, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
    }

    private void applyPaneLayout(int width, int height) {
        if (width <= 0 || height <= 0) return;
        lastWidth = width;
        lastHeight = height;

        boolean sideBySide = width > height && width >= dp(520);
        panes.setOrientation(sideBySide ? LinearLayout.HORIZONTAL : LinearLayout.VERTICAL);

        LinearLayout.LayoutParams previewParams = (LinearLayout.LayoutParams) previewPane.getLayoutParams();
        LinearLayout.LayoutParams settingsParams = (LinearLayout.LayoutParams) settingsPane.getLayoutParams();
        LinearLayout.LayoutParams cardParams = (LinearLayout.LayoutParams) previewCard.getLayoutParams();

        if (sideBySide) {
            previewParams.width = Math.max(dp(220), Math.min(Math.round(width * 0.42f), dp(480)));
            previewParams.height = ViewGroup.LayoutParams.MATCH_PARENT;
            settingsParams.width = 0;
            settingsParams.height = ViewGroup.LayoutParams.MATCH_PARENT;
            cardParams.height = 0;
            cardParams.weight = 1f;
            cardParams.bottomMargin = dp(16);
        } else {
            previewParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
            previewParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            settingsParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
            settingsParams.height = 0;
            cardParams.height = Math.max(dp(120), Math.min(Math.round(height * 0.30f), dp(300)));
            cardParams.weight = 0f;
            cardParams.bottomMargin = dp(6);
        }
        previewParams.weight = 0f;
        settingsParams.weight = 1f;

        previewPane.setLayoutParams(previewParams);
        settingsPane.setLayoutParams(settingsParams);
        previewCard.setLayoutParams(cardParams);
    }

    private void selectPage(int index, boolean animate) {
        int page = index >= 0 && index < pages.length ? index : 0;
        for (int i = 0; i < pages.length; i++) {
            tabStyles[i].set(i == page, animate);
            pages[i].setVisibility(i == page ? View.VISIBLE : View.GONE);
        }
        currentPage = page;
        scrollView.scrollTo(0, 0);

        final View tab = tabs[page];
        categoryScroll.post(() -> categoryScroll.smoothScrollTo(Math.max(0, tab.getLeft() - dp(32)), 0));

        if (animate) {
            View shown = pages[page];
            shown.setAlpha(0f);
            shown.setTranslationY(dp(10));
            shown.animate().alpha(1f).translationY(0f).setDuration(200L).start();
        }
    }

    private void styleGroup(ViewGroup group) {
        float outer = dp(24);
        float inner = dp(6);
        int visible = 0;
        for (int i = 0; i < group.getChildCount(); i++) {
            if (group.getChildAt(i).getVisibility() == View.VISIBLE) visible++;
        }
        int position = 0;
        for (int i = 0; i < group.getChildCount(); i++) {
            View row = group.getChildAt(i);
            if (row.getVisibility() != View.VISIBLE) continue;
            row.setBackground(rowBackground(position == 0 ? outer : inner, position == visible - 1 ? outer : inner, row.isClickable()));
            ViewGroup.LayoutParams params = row.getLayoutParams();
            if (params instanceof ViewGroup.MarginLayoutParams) {
                ((ViewGroup.MarginLayoutParams) params).topMargin = position == 0 ? 0 : dp(3);
                row.setLayoutParams(params);
            }
            position++;
        }
    }

    private void styleAllGroups() {
        for (ViewGroup group : groups) styleGroup(group);
    }

    private void showCaption(String text) {
        caption.animate().cancel();
        caption.setText(text);
        caption.setAlpha(1f);
        root.removeCallbacks(hideCaptionRunnable);
        root.postDelayed(hideCaptionRunnable, CAPTION_VISIBLE_MS);
    }

    private void announce(String message, String actionLabel, View.OnClickListener action) {
        Snackbar bar = Snackbar.make(root, message, actionLabel == null ? Snackbar.LENGTH_SHORT : Snackbar.LENGTH_LONG);
        bar.setBackgroundTint(COLOR_SNACKBAR);
        bar.setTextColor(COLOR_ON_SNACKBAR);
        if (actionLabel != null) {
            bar.setAction(actionLabel, action);
            bar.setActionTextColor(COLOR_SNACKBAR_ACTION);
        }
        bar.show();
    }

    private void confirm(String title, String message, String positive, Runnable onConfirmed) {
        showDialog(new MaterialAlertDialogBuilder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(positive, (dialog, which) -> onConfirmed.run())
                .setNegativeButton("Cancel", null)
                );
    }

    private EditText dialogInput(String text, int inputType, String hint) {
        EditText input = new EditText(this);
        input.setInputType(inputType);
        input.setSingleLine(true);
        input.setHint(hint);
        input.setText(text);
        input.setSelectAllOnFocus(true);
        input.setHighlightColor(COLOR_SELECTION);
        input.setBackgroundTintList(new ColorStateList(
                new int[][]{{android.R.attr.state_focused}, {}},
                new int[]{COLOR_PRIMARY, COLOR_OUTLINE}));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            GradientDrawable cursor = new GradientDrawable();
            cursor.setColor(COLOR_PRIMARY);
            cursor.setSize(dp(2), dp(20));
            input.setTextCursorDrawable(cursor);
            input.setTextSelectHandle(tinted(input.getTextSelectHandle()));
            input.setTextSelectHandleLeft(tinted(input.getTextSelectHandleLeft()));
            input.setTextSelectHandleRight(tinted(input.getTextSelectHandleRight()));
        }
        return input;
    }

    private static Drawable tinted(Drawable drawable) {
        if (drawable == null) return null;
        Drawable copy = drawable.mutate();
        copy.setTint(COLOR_PRIMARY);
        return copy;
    }

    private AlertDialog showDialog(MaterialAlertDialogBuilder builder) {
        AlertDialog dialog = builder.show();
        int[] which = {DialogInterface.BUTTON_POSITIVE, DialogInterface.BUTTON_NEGATIVE, DialogInterface.BUTTON_NEUTRAL};
        for (int type : which) {
            Button button = dialog.getButton(type);
            if (button == null) continue;
            button.setTextColor(COLOR_PRIMARY);
            if (button instanceof MaterialButton) {
                ((MaterialButton) button).setRippleColor(ColorStateList.valueOf(COLOR_PRIMARY_RIPPLE));
            }
        }
        return dialog;
    }

    private View dialogBody(EditText input) {
        FrameLayout holder = new FrameLayout(this);
        holder.setPadding(dp(24), dp(8), dp(24), 0);
        holder.addView(input, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        return holder;
    }

    private static final int MATCH = ViewGroup.LayoutParams.MATCH_PARENT;
    private static final int WRAP = ViewGroup.LayoutParams.WRAP_CONTENT;
    private static final int COLOR_ERROR = 0xFFFF8A80;
    private static final String[] PIPE_COLOR_NAMES = {"Green", "Red", "Blue", "Yellow", "White", "Pink", "Black", "Purple", "Orange"};
    private static final int[] PIPE_COLORS = {0xFF73BF2E, 0xFFD05050, 0xFF6080E0, 0xFFE0E060, 0xFFF2F2F2, 0xFFE879E8, 0xFF505050, 0xFF9040D0, 0xFFE89030};

    private final Map<String, View> rowViews = new HashMap<>();
    private final Map<String, TextView> summaries = new HashMap<>();
    private final List<ViewGroup> groups = new ArrayList<>();
    private final Button[] slotSaveButtons = new Button[PROFILE_COUNT];
    private Typeface pixelFont;
    private Typeface pixelBold;
    private Typeface mediumFont;

    private View rowView(String name) {
        View view = rowViews.get(name);
        if (view == null) throw new IllegalStateException("Missing settings row " + name);
        return view;
    }

    private void loadFonts() {
        TextView title = findViewById(R.id.tv_settings_title);
        pixelBold = title.getTypeface();
        try {
            pixelFont = ResourcesCompat.getFont(this, R.font.flappy_font);
        } catch (RuntimeException e) {
            pixelFont = null;
        }
        if (pixelFont == null) pixelFont = pixelBold;
        mediumFont = Typeface.create("sans-serif-medium", Typeface.NORMAL);
    }

    private TextView newText(String value, float size, int color, Typeface face) {
        TextView view = new TextView(this);
        view.setText(value);
        view.setTextSize(size);
        view.setTextColor(color);
        if (face != null) view.setTypeface(face);
        return view;
    }

    private static LinearLayout.LayoutParams linear(int width, int height) {
        return new LinearLayout.LayoutParams(width, height);
    }

    private static LinearLayout.LayoutParams weighted(int height) {
        return new LinearLayout.LayoutParams(0, height, 1f);
    }

    private LinearLayout newLinear(boolean horizontal) {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(horizontal ? LinearLayout.HORIZONTAL : LinearLayout.VERTICAL);
        return layout;
    }

    private LinearLayout addPage(int index) {
        LinearLayout page = newLinear(false);
        content.addView(page, new FrameLayout.LayoutParams(MATCH, WRAP));
        pages[index] = page;
        return page;
    }

    private LinearLayout addGroup(LinearLayout page, String title) {
        TextView label = newText(title.toUpperCase(Locale.US), 12, COLOR_PRIMARY, pixelBold);
        label.setLetterSpacing(0.12f);
        LinearLayout.LayoutParams labelParams = linear(WRAP, WRAP);
        labelParams.setMarginStart(dp(8));
        labelParams.topMargin = dp(page.getChildCount() == 0 ? 6 : 24);
        labelParams.bottomMargin = dp(10);
        page.addView(label, labelParams);

        LinearLayout group = newLinear(false);
        page.addView(group, linear(MATCH, WRAP));
        groups.add(group);
        return group;
    }

    private LinearLayout addRow(LinearLayout group, String name, boolean horizontal, int top, int bottom) {
        LinearLayout row = newLinear(horizontal);
        row.setPadding(dp(20), dp(top), dp(16), dp(bottom));
        if (horizontal) {
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.setMinimumHeight(dp(64));
        }
        group.addView(row, linear(MATCH, WRAP));
        rowViews.put(name, row);
        return row;
    }

    private void addTexts(LinearLayout parent, String name, String title, String summary, boolean weight, int titleColor) {
        LinearLayout box = newLinear(false);
        box.addView(newText(title, 16, titleColor, mediumFont), linear(MATCH, WRAP));
        if (summary != null) {
            TextView summaryView = newText(summary, 13, COLOR_ON_VARIANT, null);
            LinearLayout.LayoutParams summaryParams = linear(MATCH, WRAP);
            summaryParams.topMargin = dp(2);
            box.addView(summaryView, summaryParams);
            summaries.put(name, summaryView);
        }
        parent.addView(box, weight ? weighted(WRAP) : linear(MATCH, WRAP));
    }

    private void addPill(LinearLayout parent, String sample, boolean wide) {
        TextView pill = newText(sample, 14, COLOR_ON, pixelFont);
        pill.setTag(TAG_VALUE);
        pill.setGravity(Gravity.CENTER);
        pill.setMinWidth(dp(64));
        pill.setPadding(dp(12), 0, dp(12), 0);
        pill.setMaxLines(1);
        if (wide) {
            pill.setMaxWidth(dp(170));
            pill.setEllipsize(TextUtils.TruncateAt.END);
        }
        LinearLayout.LayoutParams params = linear(WRAP, dp(32));
        params.setMarginStart(dp(12));
        parent.addView(pill, params);
    }

    private void toggleRow(LinearLayout group, String name, String title, String summary) {
        LinearLayout row = addRow(group, name, true, 12, 12);
        addTexts(row, name, title, summary, true, COLOR_ON);
        MaterialSwitch toggle = new MaterialSwitch(this);
        toggle.setTag(TAG_SWITCH);
        toggle.setSaveEnabled(false);
        LinearLayout.LayoutParams params = linear(WRAP, WRAP);
        params.setMarginStart(dp(12));
        row.addView(toggle, params);
    }

    private void sliderRow(LinearLayout group, String name, String title, String summary, String sample) {
        LinearLayout row = addRow(group, name, false, 14, 4);
        LinearLayout head = newLinear(true);
        head.setGravity(Gravity.CENTER_VERTICAL);
        addTexts(head, name, title, summary, true, COLOR_ON);
        addPill(head, sample, false);
        row.addView(head, linear(MATCH, WRAP));

        Slider slider = new Slider(this);
        slider.setTag(TAG_SLIDER);
        slider.setSaveEnabled(false);
        slider.setLabelBehavior(LabelFormatter.LABEL_GONE);
        slider.setThumbTintList(ColorStateList.valueOf(COLOR_PRIMARY));
        slider.setTrackActiveTintList(ColorStateList.valueOf(COLOR_PRIMARY));
        slider.setTrackInactiveTintList(ColorStateList.valueOf(COLOR_TRACK_OFF));
        slider.setHaloTintList(ColorStateList.valueOf(0x33FFD23F));
        LinearLayout.LayoutParams params = linear(MATCH, WRAP);
        params.setMarginStart(dp(-8));
        params.setMarginEnd(dp(-8));
        params.topMargin = dp(2);
        row.addView(slider, params);
    }

    private void choiceRow(LinearLayout group, String name, String title, String... options) {
        LinearLayout row = addRow(group, name, false, 14, 14);
        addTexts(row, name, title, null, false, COLOR_ON);
        LinearLayout line = newLinear(true);
        line.setBaselineAligned(false);
        for (int i = 0; i < options.length; i++) {
            TextView chip = newText(options[i], 14, COLOR_ON_VARIANT, mediumFont);
            chip.setTag(TAG_OPTION);
            chip.setGravity(Gravity.CENTER);
            chip.setMaxLines(1);
            LinearLayout.LayoutParams params = weighted(dp(44));
            if (i < options.length - 1) params.setMarginEnd(dp(6));
            line.addView(chip, params);
        }
        LinearLayout.LayoutParams lineParams = linear(MATCH, WRAP);
        lineParams.topMargin = dp(12);
        row.addView(line, lineParams);
    }

    private void swatchRow(LinearLayout group, String name, String title, int[] colors, String[] labels) {
        int perLine = 5;
        LinearLayout row = addRow(group, name, false, 14, 14);
        LinearLayout head = newLinear(true);
        head.setGravity(Gravity.CENTER_VERTICAL);
        addTexts(head, name, title, null, true, COLOR_ON);
        addPill(head, labels[0], false);
        row.addView(head, linear(MATCH, WRAP));

        for (int start = 0; start < colors.length; start += perLine) {
            LinearLayout line = newLinear(true);
            for (int i = 0; i < perLine; i++) {
                View swatch = new View(this);
                int index = start + i;
                if (index < colors.length) {
                    swatch.setTag(TAG_OPTION);
                    swatch.setBackgroundColor(colors[index]);
                    swatch.setContentDescription(labels[index]);
                } else {
                    swatch.setVisibility(View.INVISIBLE);
                }
                LinearLayout.LayoutParams params = weighted(dp(44));
                if (i < perLine - 1) params.setMarginEnd(dp(6));
                line.addView(swatch, params);
            }
            LinearLayout.LayoutParams lineParams = linear(MATCH, WRAP);
            lineParams.topMargin = dp(start == 0 ? 12 : 6);
            row.addView(line, lineParams);
        }
    }

    private void valueRow(LinearLayout group, String name, String title, String summary, String sample) {
        LinearLayout row = addRow(group, name, true, 12, 12);
        addTexts(row, name, title, summary, true, COLOR_ON);
        addPill(row, sample, true);
    }

    private void actionRow(LinearLayout group, String name, String title, String summary, int titleColor) {
        LinearLayout row = addRow(group, name, true, 12, 12);
        addTexts(row, name, title, summary, true, titleColor);
    }

    private Button slotButton(LinearLayout parent, String label, int fill, int textColor, int marginStart) {
        MaterialButton button = new MaterialButton(this);
        button.setText(label);
        button.setTextSize(14);
        button.setAllCaps(false);
        button.setTextColor(textColor);
        button.setBackgroundTintList(ColorStateList.valueOf(fill));
        if (pixelFont != null) button.setTypeface(pixelFont);
        LinearLayout.LayoutParams params = linear(WRAP, dp(48));
        params.setMarginStart(dp(marginStart));
        parent.addView(button, params);
        return button;
    }

    private void slotRow(LinearLayout group, int slot) {
        String name = "slot_" + slot;
        LinearLayout row = addRow(group, "row_" + name, false, 14, 8);
        addTexts(row, name, "Slot " + slot, "Empty", false, COLOR_ON);
        LinearLayout actions = newLinear(true);
        actions.setGravity(Gravity.END | Gravity.CENTER_VERTICAL);
        slotClearButtons[slot - 1] = slotButton(actions, "Clear", Color.TRANSPARENT, COLOR_ERROR, 0);
        slotLoadButtons[slot - 1] = slotButton(actions, "Load", COLOR_HIGH, COLOR_ON, 4);
        slotSaveButtons[slot - 1] = slotButton(actions, "Save", COLOR_PRIMARY, COLOR_ON_PRIMARY, 8);
        slotSummaries[slot - 1] = summaries.get(name);
        LinearLayout.LayoutParams params = linear(MATCH, WRAP);
        params.topMargin = dp(4);
        row.addView(actions, params);
    }

    private void buildPages() {
        loadFonts();

        LinearLayout page = addPage(0);
        LinearLayout group = addGroup(page, "Look");
        choiceRow(group, "row_bird_color", "Color", "Yellow", "Blue", "Red", "Random");
        sliderRow(group, "row_bird_hue", "Tint", "Shifts the bird around the color wheel", "0 deg");
        toggleRow(group, "row_rainbow_bird", "Rainbow bird", "Cycles through every color in flight");
        toggleRow(group, "row_bird_trail", "Trail", "Leaves fading copies behind the bird");
        toggleRow(group, "row_wing_animation", "Wing animation", "Flaps the wings after each tap");
        sliderRow(group, "row_wing_speed", "Wing speed", "How quickly the wings beat", "1x");
        toggleRow(group, "row_flap_timeout", "Keep flapping", "Wings never rest between taps");
        toggleRow(group, "row_ghost_mode", "Ghost bird", "Makes the bird see-through");
        group = addGroup(page, "Size");
        sliderRow(group, "row_bird_size", "Bird size", null, "1x");
        sliderRow(group, "row_bird_hitbox", "Hitbox", "How much of the bird can be hit", "1x");
        choiceRow(group, "row_hitbox_shape", "Hitbox shape", "Bird", "Square");
        group = addGroup(page, "Position");
        sliderRow(group, "row_bird_position", "Distance across", "How far across the screen the bird flies", "33%");
        sliderRow(group, "row_start_height", "Starting height", "Where the bird waits for the first tap", "50%");

        page = addPage(1);
        group = addGroup(page, "Sky");
        toggleRow(group, "row_day_night_cycle", "Day and night cycle", "Slowly fades from day to night and back");
        sliderRow(group, "row_day_night_length", "Cycle length", "Time for one full day and night", "120 s");
        choiceRow(group, "row_background", "Background", "Day", "Night", "Random");
        sliderRow(group, "row_bg_scroll", "Sky speed", "How fast the background drifts", "1x");
        sliderRow(group, "row_ground_scroll", "Ground speed", null, "1x");
        group = addGroup(page, "Weather");
        choiceRow(group, "row_weather", "Weather", "Clear", "Rain", "Storm", "Snow");
        valueRow(group, "row_lightning_scores", "Lightning strikes", "Scores where a bolt hits the bird", "11, 34, 79, 210");
        sliderRow(group, "row_mega_strike_score", "Mega strike", "Score that calls down the big one", "350");

        page = addPage(2);
        group = addGroup(page, "Look");
        swatchRow(group, "row_pipe_color", "Color", PIPE_COLORS, PIPE_COLOR_NAMES);
        toggleRow(group, "row_random_pipe_colors", "Random colors", "Every pipe picks its own color");
        sliderRow(group, "row_pipe_width", "Width", null, "1x");
        group = addGroup(page, "Layout");
        sliderRow(group, "row_pipe_gap", "Gap", "Opening between the top and bottom pipe", "1x");
        sliderRow(group, "row_pipe_spacing", "Spacing", "Distance from one pipe to the next", "1x");
        sliderRow(group, "row_pipe_variation", "Height variation", "How far the gaps wander up and down", "1x");
        choiceRow(group, "row_gap_placement", "Gap heights", "Even", "Classic");
        group = addGroup(page, "Moving pipes");
        toggleRow(group, "row_moving_pipes", "Moving pipes", "Pipes drift up and down at high scores");
        sliderRow(group, "row_pipe_move_tier1", "Start moving at", "Score where the pipes begin to drift", "2000");
        sliderRow(group, "row_pipe_move_tier2", "Move faster at", "Score where they speed up", "3000");
        group = addGroup(page, "Golden pipes");
        toggleRow(group, "row_golden_pipe", "Golden pipes", "Rare shiny pipes worth bonus points");
        sliderRow(group, "row_golden_pipe_chance", "Chance", "How often a pipe turns golden", "5%");
        sliderRow(group, "row_golden_pipe_bonus", "Bonus", "Extra points for passing one", "+10");

        page = addPage(3);
        group = addGroup(page, "Flight");
        sliderRow(group, "row_gravity", "Gravity", null, "1x");
        sliderRow(group, "row_jump", "Flap strength", null, "1x");
        sliderRow(group, "row_hang_delay", "Hang time", "Pause before the bird tips into a dive", "1x");
        group = addGroup(page, "Speed");
        sliderRow(group, "row_game_speed", "Game speed", null, "1x");
        sliderRow(group, "row_pipe_speed_variation", "Speed surprise", "Random speed change every round", "0x");
        group = addGroup(page, "Difficulty");
        sliderRow(group, "row_speed_ramp", "Speeds up", "Faster for every 10 points you score", "0%");
        sliderRow(group, "row_ramp_top_speed", "Top speed", "Where the speeding up stops", "3x");
        sliderRow(group, "row_gap_shrink", "Gap shrinks", "Tighter for every 10 points you score", "0%");
        sliderRow(group, "row_ramp_min_gap", "Smallest gap", "Where the shrinking stops", "55%");
        group = addGroup(page, "Scoring");
        sliderRow(group, "row_score_multiplier", "Points per pipe", null, "1");
        group = addGroup(page, "Medals");
        sliderRow(group, "row_medal_bronze", "Bronze", "Score needed for each medal", "10");
        sliderRow(group, "row_medal_silver", "Silver", null, "20");
        sliderRow(group, "row_medal_gold", "Gold", null, "30");
        sliderRow(group, "row_medal_platinum", "Platinum", null, "40");

        page = addPage(4);
        group = addGroup(page, "Screen");
        toggleRow(group, "row_screen_shake", "Screen shake", "Rattles the screen on crashes and thunder");
        sliderRow(group, "row_screen_shake_strength", "Shake strength", null, "1x");
        toggleRow(group, "row_motion_blur", "Motion blur", "Smears fast movement like a camera");
        sliderRow(group, "row_motion_blur_strength", "Blur strength", null, "1x");
        group = addGroup(page, "Game over");
        sliderRow(group, "row_opacity", "Screen dimming", "How dark it gets behind the score panel", "5%");
        group = addGroup(page, "Crash flash");
        toggleRow(group, "row_crash_flash", "Crash flash", "Flashes the screen when the bird hits something");
        choiceRow(group, "row_crash_flash_color", "Flash color", "White", "Red", "Yellow", "Black");
        sliderRow(group, "row_crash_flash_strength", "Brightness", null, "100%");
        sliderRow(group, "row_crash_flash_length", "Length", "How long the flash takes to fade", "170 ms");
        choiceRow(group, "row_crash_flash_fade", "Fade", "Even", "Sharp");

        page = addPage(5);
        group = addGroup(page, "Cheats");
        toggleRow(group, "row_no_clip", "God mode", "Nothing can kill the bird");
        sliderRow(group, "row_extra_lives", "Extra lives", "Hits you can survive in each round", "0");
        toggleRow(group, "row_autopilot", "Autopilot", "The bird flies itself after you tap to start");
        group = addGroup(page, "Twists");
        toggleRow(group, "row_jetpack_mode", "Jetpack", "Hold the screen to rise instead of tapping");
        toggleRow(group, "row_drunk_mode", "Drunk mode", "The bird sways from side to side");
        sliderRow(group, "row_drunk_mode_strength", "Sway strength", null, "1x");
        toggleRow(group, "row_upside_down", "Upside down", "Flips the whole world over");
        toggleRow(group, "row_reverse_pipes", "Reverse pipes", "Pipes come from the left instead");

        page = addPage(6);
        group = addGroup(page, "Feedback");
        toggleRow(group, "row_sound", "Sound effects", "Flaps, points, crashes and weather");
        sliderRow(group, "row_sound_volume", "Effects volume", null, "100%");
        sliderRow(group, "row_weather_volume", "Weather volume", "Rain and storm loops", "100%");
        toggleRow(group, "row_haptic_feedback", "Vibration", "A light tap with every flap");
        group = addGroup(page, "Pausing");
        toggleRow(group, "row_pause_button", "Pause button", "Shows a pause button while you play");
        toggleRow(group, "row_auto_pause", "Pause when you leave", "Freezes the round until you tap again");
        group = addGroup(page, "Interface");
        toggleRow(group, "row_hide_settings_icon", "Hide settings button", "The top right corner still opens settings");
        toggleRow(group, "row_classic_menu", "Original menu buttons", "Rate button on the home screen and a leaderboard button after a crash");
        group = addGroup(page, "Debug");
        toggleRow(group, "row_show_fps", "FPS counter", "Shows frames per second in the top left corner");
        toggleRow(group, "row_show_hitboxes", "Show hitboxes", "Outlines everything that can collide");

        page = addPage(ADVANCED_PAGE);
        group = addGroup(page, "Classic feel");
        actionRow(group, "button_classic_feel", "Use classic feel", CLASSIC_SUMMARY, COLOR_PRIMARY);
        toggleRow(group, "row_fixed_step", "Fixed 60 Hz physics", "Moves the game in steps of 1/60 s instead of once per screen refresh");
        toggleRow(group, "row_pixel_steps", "Whole-pixel movement", "The bird moves a whole number of game pixels per step");
        group = addGroup(page, "Falling");
        sliderRow(group, "row_max_fall_speed", "Max fall speed", "Caps how fast the bird drops, compared to a flap", "Off");
        toggleRow(group, "row_ceiling", "Ceiling", "Keeps the bird from flying far above the screen");
        toggleRow(group, "row_flap_limit", "No flapping off screen", "Taps do nothing while the bird is above the top edge");
        sliderRow(group, "row_crash_gravity", "Crash fall", "How hard the bird drops after a crash", "1.5x");
        group = addGroup(page, "Tilt");
        toggleRow(group, "row_bird_tilt", "Bird tilts", "Noses up on a flap and dives when falling");
        choiceRow(group, "row_tilt_style", "Tilt style", "Smooth", "Classic");
        sliderRow(group, "row_max_tilt_up", "Nose-up angle", null, "25 deg");
        sliderRow(group, "row_max_tilt_down", "Dive angle", null, "90 deg");
        sliderRow(group, "row_tilt_speed", "Tilt speed", null, "1x");
        group = addGroup(page, "Scoring");
        choiceRow(group, "row_score_point", "Point is scored at the pipe's", "Start", "Middle", "End", "Classic");

        page = addPage(PRESETS_PAGE);
        group = addGroup(page, "Saved setups");
        for (int slot = 1; slot <= PROFILE_COUNT; slot++) slotRow(group, slot);
        group = addGroup(page, "Quick actions");
        actionRow(group, "button_randomize_all", "Randomize", "Shuffles looks, physics and modes", COLOR_ON);
        actionRow(group, "button_reset_all", "Reset to defaults", "Saved setups and your high score are kept", COLOR_ON);
        actionRow(group, "button_reset_high_score", "Reset high score", "Current high score: 0", COLOR_ON);
        actionRow(group, "button_reset_profiles", "Delete saved setups", "Clears all three slots", COLOR_ERROR);
        group = addGroup(page, "Share and back up");
        actionRow(group, "button_export_settings", "Export to file", "Saves every setting as a .json file", COLOR_ON);
        actionRow(group, "button_import_settings", "Import from file", "Loads a settings file, and can be undone", COLOR_ON);
        actionRow(group, "button_share_settings", "Share settings", "Sends your setup to a friend as text", COLOR_ON);
        actionRow(group, "button_paste_settings", "Paste settings", "Imports a setup someone sent you as text", COLOR_ON);
    }

    private void createSettings() {
        new ChoiceSetting(PREF_BIRD_COLOR, DEFAULT_BIRD_COLOR, rowView("row_bird_color"),
                new int[]{0, 1, 2, -1},
                new int[]{0xFFFAD337, 0xFF5AA9F8, 0xFFF26B5E, COLOR_PRIMARY}, null);
        whole(PREF_BIRD_HUE, DEFAULT_BIRD_HUE, 0, 350, 10, 0, 359, UNIT_DEGREES,
                rowView("row_bird_hue"));
        toggle(PREF_RAINBOW_BIRD_ENABLED, DEFAULT_RAINBOW_BIRD_ENABLED, rowView("row_rainbow_bird"));
        toggle(PREF_BIRD_TRAIL_ENABLED, DEFAULT_BIRD_TRAIL_ENABLED, rowView("row_bird_trail"));
        wingAnimation = toggle(PREF_WING_ANIMATION_ENABLED, DEFAULT_WING_ANIMATION_ENABLED, rowView("row_wing_animation"));
        times(PREF_WING_SPEED, DEFAULT_WING_SPEED, 0.25f, 4f, 0.1f, 10f, rowView("row_wing_speed"));
        toggle(PREF_INFINITE_FLAP_ENABLED, DEFAULT_INFINITE_FLAP_ENABLED, rowView("row_flap_timeout"));
        toggle(PREF_GHOST_MODE_ENABLED, DEFAULT_GHOST_MODE_ENABLED, rowView("row_ghost_mode"));
        times(PREF_BIRD_SIZE, DEFAULT_BIRD_SIZE, 0.25f, 3f, 0.1f, 10f, rowView("row_bird_size"));
        times(PREF_BIRD_HITBOX, DEFAULT_BIRD_HITBOX, 0f, 2f, 0f, 5f, rowView("row_bird_hitbox"));
        pair(PREF_HITBOX_SHAPE, DEFAULT_HITBOX_SHAPE, "row_hitbox_shape");
        whole(PREF_BIRD_POSITION, DEFAULT_BIRD_POSITION, 10, 50, 1, 10, 90, UNIT_PERCENT, rowView("row_bird_position"));
        whole(PREF_START_HEIGHT, DEFAULT_START_HEIGHT, 20, 80, 2, 10, 90, UNIT_PERCENT,
                rowView("row_start_height")).note = "applies in the game";

        dayNight = toggle(PREF_DYNAMIC_DAY_NIGHT_ENABLED, DEFAULT_DYNAMIC_DAY_NIGHT_ENABLED, rowView("row_day_night_cycle"));
        whole(PREF_DAY_NIGHT_LENGTH, DEFAULT_DAY_NIGHT_LENGTH, 15, 600, 15, 5, 3600, UNIT_SECONDS,
                rowView("row_day_night_length")).note = "the preview runs it faster";
        background = new ChoiceSetting(PREF_BACKGROUND, DEFAULT_BACKGROUND, rowView("row_background"),
                new int[]{0, 1, -1},
                new int[]{COLOR_SKY, 0xFF9C8CFF, COLOR_PRIMARY}, null);
        times(PREF_BG_SCROLL_SPEED, DEFAULT_BG_SCROLL_SPEED, 0f, 3f, 0f, 10f, rowView("row_bg_scroll"));
        times(PREF_GROUND_SCROLL_SPEED, DEFAULT_GROUND_SCROLL_SPEED, 0f, 3f, 0f, 10f, rowView("row_ground_scroll"));
        weather = new ChoiceSetting(PREF_WEATHER_EFFECT, DEFAULT_WEATHER_EFFECT, rowView("row_weather"),
                new int[]{0, 1, 2, 3},
                new int[]{0xFF9BE15D, 0xFF5AA9F8, COLOR_PRIMARY, 0xFFE6EEFB}, null);
        new ListSetting(PREF_LIGHTNING_SCORES, DEFAULT_LIGHTNING_SCORES, rowView("row_lightning_scores"));
        whole(PREF_MEGA_STRIKE_SCORE, DEFAULT_MEGA_STRIKE_SCORE, 0, 1000, 10, 0, MAX_SCORE_VALUE, UNIT_SCORE,
                rowView("row_mega_strike_score"));

        pipeColor = new ChoiceSetting(PREF_PIPE_COLOR, DEFAULT_PIPE_COLOR, rowView("row_pipe_color"),
                new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8},
                PIPE_COLORS, PIPE_COLOR_NAMES);
        randomPipeColors = toggle(PREF_RANDOM_PIPE_COLORS_ENABLED, DEFAULT_RANDOM_PIPE_COLORS_ENABLED, rowView("row_random_pipe_colors"));
        times(PREF_PIPE_WIDTH, DEFAULT_PIPE_WIDTH, 0.25f, 3f, 0.1f, 10f, rowView("row_pipe_width"));
        times(PREF_PIPE_GAP, DEFAULT_PIPE_GAP, 0.5f, 3f, 0.1f, 10f, rowView("row_pipe_gap"));
        times(PREF_PIPE_SPACING, DEFAULT_PIPE_SPACING, 0.5f, 3f, 0.1f, 10f, rowView("row_pipe_spacing"));
        times(PREF_PIPE_VARIATION, DEFAULT_PIPE_VARIATION, 0f, 3f, 0f, 10f, rowView("row_pipe_variation"));
        pair(PREF_GAP_PLACEMENT, DEFAULT_GAP_PLACEMENT, "row_gap_placement");
        movingPipes = toggle(PREF_MOVING_PIPES_ENABLED, DEFAULT_MOVING_PIPES_ENABLED, rowView("row_moving_pipes"));
        whole(PREF_PIPE_MOVE_TIER_1_SCORE, DEFAULT_PIPE_MOVE_TIER_1_SCORE, 0, 5000, 50, 0, MAX_SCORE_VALUE, UNIT_SCORE,
                rowView("row_pipe_move_tier1")).note = "sample shown";
        whole(PREF_PIPE_MOVE_TIER_2_SCORE, DEFAULT_PIPE_MOVE_TIER_2_SCORE, 0, 5000, 50, 0, MAX_SCORE_VALUE, UNIT_SCORE,
                rowView("row_pipe_move_tier2")).note = "sample shown";
        goldenPipes = toggle(PREF_GOLDEN_PIPE_ENABLED, DEFAULT_GOLDEN_PIPE_ENABLED, rowView("row_golden_pipe"));
        whole(PREF_GOLDEN_PIPE_CHANCE, DEFAULT_GOLDEN_PIPE_CHANCE, 0, 100, 1, 0, 100, UNIT_PERCENT,
                rowView("row_golden_pipe_chance"));
        whole(PREF_GOLDEN_PIPE_BONUS, DEFAULT_GOLDEN_PIPE_BONUS, 0, 100, 1, 0, 100000, UNIT_POINTS,
                rowView("row_golden_pipe_bonus"));

        times(PREF_GRAVITY, DEFAULT_GRAVITY, 0f, 3f, 0f, 10f, rowView("row_gravity"));
        times(PREF_JUMP_STRENGTH, DEFAULT_JUMP_STRENGTH, 0f, 3f, 0f, 10f, rowView("row_jump"));
        times(PREF_BIRD_HANG_DELAY, DEFAULT_BIRD_HANG_DELAY, 0f, 3f, 0f, 10f, rowView("row_hang_delay"));
        times(PREF_GAME_SPEED, DEFAULT_GAME_SPEED, 0.25f, 3f, 0.1f, 10f, rowView("row_game_speed"));
        times(PREF_PIPE_SPEED_VARIATION, DEFAULT_PIPE_SPEED_VARIATION, 0f, 1f, 0f, 1f,
                rowView("row_pipe_speed_variation"));
        whole(PREF_SPEED_RAMP, DEFAULT_SPEED_RAMP, 0, 25, 1, 0, 100, UNIT_PERCENT,
                rowView("row_speed_ramp")).note = "builds up as the score climbs";
        whole(PREF_GAP_SHRINK, DEFAULT_GAP_SHRINK, 0, 25, 1, 0, 100, UNIT_PERCENT,
                rowView("row_gap_shrink")).note = "builds up as the score climbs";
        times(PREF_RAMP_TOP_SPEED, DEFAULT_RAMP_TOP_SPEED, 1f, 5f, 1f, 10f, rowView("row_ramp_top_speed"));
        whole(PREF_RAMP_MIN_GAP, DEFAULT_RAMP_MIN_GAP, 30, 100, 5, 10, 100, UNIT_PERCENT, rowView("row_ramp_min_gap"));
        whole(PREF_SCORE_MULTIPLIER, DEFAULT_SCORE_MULTIPLIER, 1, 10, 1, 0, 1000, UNIT_SCORE,
                rowView("row_score_multiplier"));
        medal(PREF_MEDAL_BRONZE_SCORE, DEFAULT_MEDAL_BRONZE_SCORE, rowView("row_medal_bronze"));
        medal(PREF_MEDAL_SILVER_SCORE, DEFAULT_MEDAL_SILVER_SCORE, rowView("row_medal_silver"));
        medal(PREF_MEDAL_GOLD_SCORE, DEFAULT_MEDAL_GOLD_SCORE, rowView("row_medal_gold"));
        medal(PREF_MEDAL_PLATINUM_SCORE, DEFAULT_MEDAL_PLATINUM_SCORE, rowView("row_medal_platinum"));

        screenShake = toggle(PREF_SCREEN_SHAKE_ENABLED, DEFAULT_SCREEN_SHAKE_ENABLED, rowView("row_screen_shake"));
        times(PREF_SCREEN_SHAKE_STRENGTH, DEFAULT_SCREEN_SHAKE_STRENGTH, 0f, 3f, 0f, 10f,
                rowView("row_screen_shake_strength"));
        motionBlur = toggle(PREF_MOTION_BLUR_ENABLED, DEFAULT_MOTION_BLUR_ENABLED, rowView("row_motion_blur"));
        new NumberSetting(PREF_MOTION_BLUR_STRENGTH, DEFAULT_MOTION_BLUR_STRENGTH, false, 1f, 10f, 0.5f, 1f, 10f, UNIT_TIMES,
                rowView("row_motion_blur_strength"));
        whole(PREF_GAMEOVER_OPACITY, DEFAULT_GAMEOVER_OPACITY, 0, 100, 1, 0, 100, UNIT_PERCENT,
                rowView("row_opacity"));
        crashFlash = toggle(PREF_CRASH_FLASH_ENABLED, DEFAULT_CRASH_FLASH_ENABLED, rowView("row_crash_flash"));
        new ChoiceSetting(PREF_CRASH_FLASH_COLOR, DEFAULT_CRASH_FLASH_COLOR, rowView("row_crash_flash_color"),
                new int[]{0, 1, 2, 3},
                new int[]{0xFFF2F2F2, 0xFFFF6B60, COLOR_PRIMARY, 0xFF8791A8}, null);
        whole(PREF_CRASH_FLASH_STRENGTH, DEFAULT_CRASH_FLASH_STRENGTH, 0, 100, 5, 0, 100, UNIT_PERCENT,
                rowView("row_crash_flash_strength"));
        whole(PREF_CRASH_FLASH_LENGTH, DEFAULT_CRASH_FLASH_LENGTH, 50, 1500, 10, 10, 5000, UNIT_MS,
                rowView("row_crash_flash_length"));
        pair(PREF_CRASH_FLASH_FADE, DEFAULT_CRASH_FLASH_FADE, "row_crash_flash_fade");

        fixedStep = toggle(PREF_FIXED_STEP_ENABLED, DEFAULT_FIXED_STEP_ENABLED, rowView("row_fixed_step"));
        maxFall = times(PREF_MAX_FALL_SPEED, DEFAULT_MAX_FALL_SPEED, 0f, 4f, 0f, 10f, rowView("row_max_fall_speed"));
        maxFall.zeroLabel = "Off";
        toggle(PREF_PIXEL_STEPS_ENABLED, DEFAULT_PIXEL_STEPS_ENABLED, rowView("row_pixel_steps")).randomizable = false;
        toggle(PREF_CEILING_ENABLED, DEFAULT_CEILING_ENABLED, rowView("row_ceiling"));
        toggle(PREF_FLAP_LIMIT_ENABLED, DEFAULT_FLAP_LIMIT_ENABLED, rowView("row_flap_limit")).note = "applies in the game";
        times(PREF_CRASH_GRAVITY, DEFAULT_CRASH_GRAVITY, 0.5f, 3f, 0f, 10f,
                rowView("row_crash_gravity")).note = "applies in the game";
        birdTilt = toggle(PREF_BIRD_TILT_ENABLED, DEFAULT_BIRD_TILT_ENABLED, rowView("row_bird_tilt"));
        pair(PREF_TILT_STYLE, DEFAULT_TILT_STYLE, "row_tilt_style");
        whole(PREF_MAX_TILT_UP, DEFAULT_MAX_TILT_UP, 0, 90, 5, 0, 90, UNIT_DEGREES, rowView("row_max_tilt_up"));
        whole(PREF_MAX_TILT_DOWN, DEFAULT_MAX_TILT_DOWN, 0, 90, 5, 0, 90, UNIT_DEGREES, rowView("row_max_tilt_down"));
        times(PREF_TILT_SPEED, DEFAULT_TILT_SPEED, 0.25f, 3f, 0.1f, 10f, rowView("row_tilt_speed"));
        new ChoiceSetting(PREF_SCORE_POINT, DEFAULT_SCORE_POINT, rowView("row_score_point"),
                new int[]{SCORE_POINT_START, SCORE_POINT_MIDDLE, SCORE_POINT_END, SCORE_POINT_CLASSIC},
                new int[]{COLOR_SKY, 0xFF9C8CFF, COLOR_PRIMARY, 0xFF9BE15D}, null);

        toggle(PREF_NO_CLIP_ENABLED, DEFAULT_NO_CLIP_ENABLED, rowView("row_no_clip")).note = "applies in the game";
        whole(PREF_EXTRA_LIVES, DEFAULT_EXTRA_LIVES, 0, 5, 1, 0, 9, UNIT_SCORE,
                rowView("row_extra_lives"));
        toggle(PREF_AUTOPILOT_ENABLED, DEFAULT_AUTOPILOT_ENABLED, rowView("row_autopilot")).note = "the preview bird already flies itself";
        toggle(PREF_JETPACK_MODE_ENABLED, DEFAULT_JETPACK_MODE_ENABLED, rowView("row_jetpack_mode"));
        drunkMode = toggle(PREF_DRUNK_MODE_ENABLED, DEFAULT_DRUNK_MODE_ENABLED, rowView("row_drunk_mode"));
        drunkStrength = times(PREF_DRUNK_MODE_STRENGTH, DEFAULT_DRUNK_MODE_STRENGTH, 0f, 3f, 0f, 10f,
                rowView("row_drunk_mode_strength"));
        toggle(PREF_UPSIDE_DOWN_ENABLED, DEFAULT_UPSIDE_DOWN_ENABLED, rowView("row_upside_down"));
        toggle(PREF_REVERSE_PIPES_ENABLED, DEFAULT_REVERSE_PIPES_ENABLED, rowView("row_reverse_pipes"));

        fixedStep.randomizable = false;
        sound = toggle(PREF_SOUND_ENABLED, DEFAULT_SOUND_ENABLED, rowView("row_sound"));
        sound.note = "heard in the game";
        sound.randomizable = false;
        volume(PREF_SOUND_VOLUME, DEFAULT_SOUND_VOLUME, "row_sound_volume");
        volume(PREF_WEATHER_VOLUME, DEFAULT_WEATHER_VOLUME, "row_weather_volume");
        vibration = toggle(PREF_HAPTIC_FEEDBACK_ENABLED, DEFAULT_HAPTIC_FEEDBACK_ENABLED, rowView("row_haptic_feedback"));
        vibration.note = "felt in the game";
        vibration.randomizable = false;
        toggle(PREF_PAUSE_BUTTON_ENABLED, DEFAULT_PAUSE_BUTTON_ENABLED, rowView("row_pause_button")).randomizable = false;
        ToggleSetting autoPause = toggle(PREF_AUTO_PAUSE_ENABLED, DEFAULT_AUTO_PAUSE_ENABLED, rowView("row_auto_pause"));
        autoPause.note = "applies in the game";
        autoPause.randomizable = false;
        toggle(PREF_HIDE_SETTINGS_ICON, DEFAULT_HIDE_SETTINGS_ICON, rowView("row_hide_settings_icon")).randomizable = false;
        ToggleSetting classicMenu = toggle(PREF_CLASSIC_MENU_ENABLED, DEFAULT_CLASSIC_MENU_ENABLED, rowView("row_classic_menu"));
        classicMenu.note = "shown on the home and score screens";
        classicMenu.randomizable = false;
        ToggleSetting showFps = toggle(PREF_SHOW_FPS, DEFAULT_SHOW_FPS, rowView("row_show_fps"));
        showFps.note = "shown in the game";
        showFps.randomizable = false;
        toggle(PREF_SHOW_HITBOXES, DEFAULT_SHOW_HITBOXES, rowView("row_show_hitboxes")).randomizable = false;
    }

    private void pair(String key, int defaultValue, String rowName) {
        new ChoiceSetting(key, defaultValue, rowView(rowName), new int[]{0, 1}, new int[]{COLOR_SKY, COLOR_PRIMARY}, null);
    }

    private void volume(String key, int defaultValue, String rowName) {
        NumberSetting setting = whole(key, defaultValue, 0, 100, 5, 0, 100, UNIT_PERCENT, rowView(rowName));
        setting.note = "heard in the game";
        setting.randomizable = false;
    }

    private void medal(String key, int defaultValue, View rowId) {
        NumberSetting setting = whole(key, defaultValue, 0, 200, 5, 0, MAX_SCORE_VALUE, UNIT_SCORE, rowId);
        setting.note = "applies in the game";
        setting.randomizable = false;
    }

    private ToggleSetting toggle(String key, boolean defaultValue, View rowId) {
        return new ToggleSetting(key, defaultValue, rowId);
    }

    private NumberSetting times(String key, float defaultValue, float sliderMin, float sliderMax, float min, float max,
                                View rowId) {
        return new NumberSetting(key, defaultValue, false, sliderMin, sliderMax, 0.05f, min, max, UNIT_TIMES, rowId);
    }

    private NumberSetting whole(String key, int defaultValue, int sliderMin, int sliderMax, int step, int min, int max, int unit,
                                View rowId) {
        return new NumberSetting(key, defaultValue, true, sliderMin, sliderMax, step, min, max, unit, rowId);
    }

    private boolean flag(String key) {
        Setting setting = settingsByKey.get(key);
        return setting instanceof ToggleSetting && ((ToggleSetting) setting).value;
    }

    private float number(String key) {
        Setting setting = settingsByKey.get(key);
        return setting instanceof NumberSetting ? ((NumberSetting) setting).value : 0f;
    }

    private int option(String key) {
        Setting setting = settingsByKey.get(key);
        return setting instanceof ChoiceSetting ? ((ChoiceSetting) setting).value : 0;
    }

    private View rowOf(String key) {
        return settingsByKey.get(key).row;
    }

    private void readAll(Map<String, ?> source) {
        for (Setting setting : settings) setting.read(source);
    }

    private void syncAllViews() {
        for (Setting setting : settings) setting.syncView();
    }

    private Map<String, Object> snapshot() {
        Map<String, Object> values = new HashMap<>();
        for (Setting setting : settings) values.put(setting.key, setting.capture());
        return values;
    }

    private void restore(Map<String, Object> values) {
        for (Setting setting : settings) {
            Object value = values.get(setting.key);
            if (value != null) setting.restore(value);
        }
    }

    private void scheduleSave(Setting setting) {
        unsaved.add(setting);
        root.removeCallbacks(saveRunnable);
        root.postDelayed(saveRunnable, SAVE_DELAY_MS);
    }

    private void flushSave() {
        if (root != null) root.removeCallbacks(saveRunnable);
        if (unsaved.isEmpty() || prefs == null) return;
        SharedPreferences.Editor editor = prefs.edit();
        for (Setting setting : unsaved) setting.write(editor);
        editor.apply();
        unsaved.clear();
    }

    private void onUserChange(Setting setting) {
        String extra = null;
        if (setting == weather && weather.value == WEATHER_STORM && !screenShake.value) {
            screenShake.value = true;
            screenShake.syncView();
            scheduleSave(screenShake);
            extra = "screen shake turned on for thunder";
        }
        if (setting == drunkMode && drunkMode.value && drunkStrength.value <= 0f) {
            drunkStrength.value = 1f;
            drunkStrength.syncView();
            scheduleSave(drunkStrength);
        }
        if (setting == vibration && vibration.value) {
            root.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
        }
        if (setting == movingPipes && movingPipes.value) {
            extra = "sample shown, starts at score " + Math.round(number(PREF_PIPE_MOVE_TIER_1_SCORE));
        }

        scheduleSave(setting);
        refreshDependencies(true, false);
        pushPreview();
        playDemo(setting);

        String note = extra != null ? extra : setting.note;
        showCaption(setting.label + ": " + setting.describe() + (note != null ? " (" + note + ")" : ""));
    }

    private void playDemo(Setting setting) {
        String key = setting.key;
        if (key.equals(PREF_SCREEN_SHAKE_ENABLED) || key.equals(PREF_SCREEN_SHAKE_STRENGTH)) {
            previewView.demo(SettingsPreviewView.DEMO_SHAKE);
        } else if (key.equals(PREF_GAMEOVER_OPACITY)) {
            previewView.demo(SettingsPreviewView.DEMO_DIM);
        } else if (key.equals(PREF_BIRD_HITBOX) || key.equals(PREF_BIRD_SIZE) || key.equals(PREF_HITBOX_SHAPE)) {
            previewView.demo(SettingsPreviewView.DEMO_HITBOX);
        } else if (key.equals(PREF_LIGHTNING_SCORES)) {
            previewView.demo(SettingsPreviewView.DEMO_STRIKE);
        } else if (key.equals(PREF_MEGA_STRIKE_SCORE)) {
            previewView.demo(SettingsPreviewView.DEMO_MEGA_STRIKE);
        } else if (key.equals(PREF_MOVING_PIPES_ENABLED) || key.equals(PREF_PIPE_MOVE_TIER_1_SCORE)) {
            previewView.demo(SettingsPreviewView.DEMO_MOVING_PIPES);
        } else if (key.equals(PREF_CRASH_FLASH_ENABLED) || key.equals(PREF_CRASH_FLASH_COLOR)
                || key.equals(PREF_CRASH_FLASH_STRENGTH) || key.equals(PREF_CRASH_FLASH_LENGTH)
                || key.equals(PREF_CRASH_FLASH_FADE)) {
            previewView.demo(SettingsPreviewView.DEMO_CRASH_FLASH);
        } else if (key.equals(PREF_PIPE_MOVE_TIER_2_SCORE)) {
            previewView.demo(SettingsPreviewView.DEMO_FAST_PIPES);
        }
    }

    private void refreshDependencies(boolean animate, boolean forceStyle) {
        boolean storm = weather.value == WEATHER_STORM;
        View[] rows = {
                rowOf(PREF_INFINITE_FLAP_ENABLED),
                rowOf(PREF_LIGHTNING_SCORES), rowOf(PREF_MEGA_STRIKE_SCORE),
                rowOf(PREF_PIPE_MOVE_TIER_1_SCORE), rowOf(PREF_PIPE_MOVE_TIER_2_SCORE),
                rowOf(PREF_GOLDEN_PIPE_CHANCE), rowOf(PREF_GOLDEN_PIPE_BONUS),
                rowOf(PREF_SCREEN_SHAKE_STRENGTH), rowOf(PREF_MOTION_BLUR_STRENGTH), rowOf(PREF_DRUNK_MODE_STRENGTH),
                rowOf(PREF_CRASH_FLASH_COLOR), rowOf(PREF_CRASH_FLASH_STRENGTH), rowOf(PREF_CRASH_FLASH_LENGTH),
                rowOf(PREF_MAX_TILT_UP), rowOf(PREF_MAX_TILT_DOWN), rowOf(PREF_TILT_SPEED),
                rowOf(PREF_WING_SPEED), rowOf(PREF_DAY_NIGHT_LENGTH), rowOf(PREF_CRASH_FLASH_FADE),
                rowOf(PREF_PIXEL_STEPS_ENABLED), rowOf(PREF_TILT_STYLE),
                rowOf(PREF_RAMP_TOP_SPEED), rowOf(PREF_RAMP_MIN_GAP),
                rowOf(PREF_SOUND_VOLUME), rowOf(PREF_WEATHER_VOLUME)};
        boolean[] shown = {
                wingAnimation.value,
                storm, storm,
                movingPipes.value, movingPipes.value,
                goldenPipes.value, goldenPipes.value,
                screenShake.value, motionBlur.value, drunkMode.value,
                crashFlash.value, crashFlash.value, crashFlash.value,
                birdTilt.value, birdTilt.value, birdTilt.value,
                wingAnimation.value, dayNight.value, crashFlash.value,
                fixedStep.value, birdTilt.value,
                number(PREF_SPEED_RAMP) > 0f, number(PREF_GAP_SHRINK) > 0f,
                sound.value, sound.value};

        boolean changed = false;
        for (int i = 0; i < rows.length; i++) {
            if ((rows[i].getVisibility() == View.VISIBLE) != shown[i]) changed = true;
        }
        if (changed) {
            if (animate) TransitionManager.beginDelayedTransition(content, new AutoTransition().setDuration(180L));
            for (int i = 0; i < rows.length; i++) rows[i].setVisibility(shown[i] ? View.VISIBLE : View.GONE);
        }
        if (changed || forceStyle) styleAllGroups();

        background.setEnabled(!dayNight.value);
        pipeColor.setEnabled(!randomPipeColors.value);
        refreshClassicSummary();
    }

    private void refreshClassicSummary() {
        if (classicSummary == null) return;
        Map<String, Object> current = snapshot();
        applyClassicFeel();
        boolean active = current.equals(snapshot());
        restore(current);
        classicSummary.setText(active ? "Active. " + CLASSIC_SUMMARY : CLASSIC_SUMMARY);
    }

    private void pushPreview() {
        PreviewConfig config = previewView.config;
        config.birdColor = option(PREF_BIRD_COLOR);
        config.birdHue = Math.round(number(PREF_BIRD_HUE));
        config.extraLives = Math.round(number(PREF_EXTRA_LIVES));
        config.speedRamp = Math.round(number(PREF_SPEED_RAMP));
        config.gapShrink = Math.round(number(PREF_GAP_SHRINK));
        config.pauseButton = flag(PREF_PAUSE_BUTTON_ENABLED);
        config.rainbowBird = flag(PREF_RAINBOW_BIRD_ENABLED);
        config.birdTrail = flag(PREF_BIRD_TRAIL_ENABLED);
        config.wingAnimation = flag(PREF_WING_ANIMATION_ENABLED);
        config.keepFlapping = flag(PREF_INFINITE_FLAP_ENABLED);
        config.ghostBird = flag(PREF_GHOST_MODE_ENABLED);
        config.birdSize = number(PREF_BIRD_SIZE);
        config.birdHitbox = number(PREF_BIRD_HITBOX);
        config.dayNight = flag(PREF_DYNAMIC_DAY_NIGHT_ENABLED);
        config.background = option(PREF_BACKGROUND);
        config.bgScroll = number(PREF_BG_SCROLL_SPEED);
        config.groundScroll = number(PREF_GROUND_SCROLL_SPEED);
        config.weather = option(PREF_WEATHER_EFFECT);
        config.pipeColor = option(PREF_PIPE_COLOR);
        config.randomPipeColors = flag(PREF_RANDOM_PIPE_COLORS_ENABLED);
        config.pipeWidth = number(PREF_PIPE_WIDTH);
        config.pipeGap = number(PREF_PIPE_GAP);
        config.pipeSpacing = number(PREF_PIPE_SPACING);
        config.pipeVariation = number(PREF_PIPE_VARIATION);
        config.movingPipes = flag(PREF_MOVING_PIPES_ENABLED);
        config.crashFlash = flag(PREF_CRASH_FLASH_ENABLED);
        config.crashFlashColor = option(PREF_CRASH_FLASH_COLOR);
        config.crashFlashStrength = Math.round(number(PREF_CRASH_FLASH_STRENGTH));
        config.crashFlashLength = Math.round(number(PREF_CRASH_FLASH_LENGTH));
        config.fixedStep = flag(PREF_FIXED_STEP_ENABLED);
        config.maxFallSpeed = number(PREF_MAX_FALL_SPEED);
        config.ceiling = flag(PREF_CEILING_ENABLED);
        config.birdTilt = flag(PREF_BIRD_TILT_ENABLED);
        config.maxTiltUp = Math.round(number(PREF_MAX_TILT_UP));
        config.maxTiltDown = Math.round(number(PREF_MAX_TILT_DOWN));
        config.tiltSpeed = number(PREF_TILT_SPEED);
        config.scorePoint = option(PREF_SCORE_POINT);
        config.pixelSteps = flag(PREF_PIXEL_STEPS_ENABLED);
        config.tiltStyle = option(PREF_TILT_STYLE);
        config.hitboxShape = option(PREF_HITBOX_SHAPE);
        config.gapPlacement = option(PREF_GAP_PLACEMENT);
        config.crashFlashFade = option(PREF_CRASH_FLASH_FADE);
        config.birdPosition = Math.round(number(PREF_BIRD_POSITION));
        config.wingSpeed = number(PREF_WING_SPEED);
        config.dayNightLength = Math.round(number(PREF_DAY_NIGHT_LENGTH));
        config.showHitboxes = flag(PREF_SHOW_HITBOXES);
        config.rampTopSpeed = number(PREF_RAMP_TOP_SPEED);
        config.rampMinGap = Math.round(number(PREF_RAMP_MIN_GAP));
        config.moveTier1 = Math.round(number(PREF_PIPE_MOVE_TIER_1_SCORE));
        config.moveTier2 = Math.round(number(PREF_PIPE_MOVE_TIER_2_SCORE));
        config.goldenPipes = flag(PREF_GOLDEN_PIPE_ENABLED);
        config.goldenChance = Math.round(number(PREF_GOLDEN_PIPE_CHANCE));
        config.goldenBonus = Math.round(number(PREF_GOLDEN_PIPE_BONUS));
        config.gravity = number(PREF_GRAVITY);
        config.jump = number(PREF_JUMP_STRENGTH);
        config.hangDelay = number(PREF_BIRD_HANG_DELAY);
        config.gameSpeed = number(PREF_GAME_SPEED);
        config.speedVariation = number(PREF_PIPE_SPEED_VARIATION);
        config.scoreMultiplier = Math.round(number(PREF_SCORE_MULTIPLIER));
        config.screenShake = flag(PREF_SCREEN_SHAKE_ENABLED);
        config.shakeStrength = number(PREF_SCREEN_SHAKE_STRENGTH);
        config.motionBlur = flag(PREF_MOTION_BLUR_ENABLED);
        config.blurStrength = number(PREF_MOTION_BLUR_STRENGTH);
        config.gameOverOpacity = Math.round(number(PREF_GAMEOVER_OPACITY));
        config.jetpack = flag(PREF_JETPACK_MODE_ENABLED);
        config.drunk = flag(PREF_DRUNK_MODE_ENABLED);
        config.drunkStrength = number(PREF_DRUNK_MODE_STRENGTH);
        config.upsideDown = flag(PREF_UPSIDE_DOWN_ENABLED);
        config.reversePipes = flag(PREF_REVERSE_PIPES_ENABLED);
        config.hideSettingsIcon = flag(PREF_HIDE_SETTINGS_ICON);
        previewView.onConfigChanged();
    }

    private void afterBulkChange() {
        syncAllViews();
        unsaved.addAll(settings);
        flushSave();
        refreshDependencies(false, true);
        pushPreview();
    }

    private void applyBulkChange(Runnable change, String message) {
        final Map<String, Object> before = snapshot();
        change.run();
        afterBulkChange();
        showCaption(message);
        announce(message, "Undo", v -> {
            restore(before);
            afterBulkChange();
            showCaption("Change undone");
        });
    }

    private void bindPresets() {
        for (int i = 0; i < PROFILE_COUNT; i++) {
            final int slot = i + 1;
            slotSaveButtons[i].setOnClickListener(v -> onSaveSlot(slot));
            slotLoadButtons[i].setOnClickListener(v -> onLoadSlot(slot));
            slotClearButtons[i].setOnClickListener(v -> onClearSlot(slot));
        }

        View randomize = rowView("button_randomize_all");
        randomize.setOnClickListener(v -> applyBulkChange(() -> {
            for (Setting setting : settings) {
                if (setting.randomizable) setting.randomize();
            }
        }, "Everything randomized"));

        View resetAll = rowView("button_reset_all");
        resetAll.setOnClickListener(v -> confirm("Reset to defaults",
                "Every setting goes back to its default. Saved setups and your high score are kept.", "Reset",
                () -> applyBulkChange(() -> {
                    for (Setting setting : settings) setting.reset();
                }, "Settings reset to defaults")));

        highScoreSummary = summaries.get("button_reset_high_score");
        classicSummary = summaries.get("button_classic_feel");
        refreshHighScore();
        View resetScore = rowView("button_reset_high_score");
        resetScore.setOnClickListener(v -> confirm("Reset high score",
                "Your best score of " + storedHighScore() + " will be erased.", "Reset",
                () -> {
                    prefs.edit().remove(KEY_HIGH_SCORE).apply();
                    refreshHighScore();
                    announce("High score reset", null, null);
                }));

        rowView("button_classic_feel").setOnClickListener(v -> applyBulkChange(this::applyClassicFeel, "Classic feel applied"));
        rowView("button_export_settings").setOnClickListener(v -> startExport());
        rowView("button_import_settings").setOnClickListener(v -> startImport());
        rowView("button_share_settings").setOnClickListener(v -> shareSettings());
        rowView("button_paste_settings").setOnClickListener(v -> pasteSettings());

        View deleteAll = rowView("button_reset_profiles");
        deleteAll.setOnClickListener(v -> confirm("Delete saved setups",
                "This clears all three slots and cannot be undone.", "Delete",
                () -> {
                    for (int slot = 1; slot <= PROFILE_COUNT; slot++) profile(slot).edit().clear().apply();
                    refreshSlots();
                    announce("All saved setups deleted", null, null);
                }));
    }

    private int storedHighScore() {
        Object stored = prefs.getAll().get(KEY_HIGH_SCORE);
        return stored instanceof Integer ? (Integer) stored : 0;
    }

    private void refreshHighScore() {
        highScoreSummary.setText("Current high score: " + storedHighScore());
    }

    private boolean isPersonal(Setting setting) {
        String key = setting.key;
        return key.equals(PREF_SOUND_ENABLED) || key.equals(PREF_HAPTIC_FEEDBACK_ENABLED)
                || key.equals(PREF_HIDE_SETTINGS_ICON) || key.equals(PREF_PAUSE_BUTTON_ENABLED)
                || key.equals(PREF_AUTO_PAUSE_ENABLED) || key.equals(PREF_SOUND_VOLUME)
                || key.equals(PREF_WEATHER_VOLUME) || key.equals(PREF_SHOW_FPS) || key.equals(PREF_SHOW_HITBOXES)
                || key.equals(PREF_CLASSIC_MENU_ENABLED);
    }

    private void setFlag(String key, boolean value) {
        ((ToggleSetting) settingsByKey.get(key)).value = value;
    }

    private void setNumber(String key, float value) {
        NumberSetting setting = (NumberSetting) settingsByKey.get(key);
        setting.value = setting.sanitize(value);
    }

    private void setOption(String key, int value) {
        ((ChoiceSetting) settingsByKey.get(key)).value = value;
    }

    private void applyClassicFeel() {
        for (Setting setting : settings) {
            if (!isPersonal(setting)) setting.reset();
        }
        setFlag(PREF_FIXED_STEP_ENABLED, true);
        setFlag(PREF_PIXEL_STEPS_ENABLED, true);
        setNumber(PREF_GRAVITY, CLASSIC_GRAVITY);
        setNumber(PREF_JUMP_STRENGTH, CLASSIC_FLAP);
        setNumber(PREF_MAX_FALL_SPEED, CLASSIC_MAX_FALL_SPEED);
        setNumber(PREF_CRASH_GRAVITY, 1f);
        setFlag(PREF_CEILING_ENABLED, false);
        setFlag(PREF_FLAP_LIMIT_ENABLED, true);
        setNumber(PREF_GAME_SPEED, CLASSIC_SPEED);
        setNumber(PREF_PIPE_GAP, CLASSIC_GAP);
        setNumber(PREF_PIPE_SPACING, CLASSIC_SPACING);
        setOption(PREF_GAP_PLACEMENT, GAP_PLACEMENT_CLASSIC);
        setFlag(PREF_MOVING_PIPES_ENABLED, false);
        setOption(PREF_HITBOX_SHAPE, HITBOX_SHAPE_SQUARE);
        setOption(PREF_SCORE_POINT, SCORE_POINT_CLASSIC);
        setOption(PREF_TILT_STYLE, TILT_STYLE_CLASSIC);
        setNumber(PREF_MAX_TILT_UP, CLASSIC_TILT_UP);
        setNumber(PREF_MAX_TILT_DOWN, CLASSIC_TILT_DOWN);
        setNumber(PREF_WING_SPEED, CLASSIC_WING_SPEED);
        setNumber(PREF_BIRD_POSITION, CLASSIC_BIRD_POSITION);
        setNumber(PREF_START_HEIGHT, CLASSIC_START_HEIGHT);
        setNumber(PREF_BG_SCROLL_SPEED, 0f);
        setNumber(PREF_GAMEOVER_OPACITY, 0f);
        setFlag(PREF_SCREEN_SHAKE_ENABLED, false);
        setFlag(PREF_CRASH_FLASH_ENABLED, true);
        setNumber(PREF_CRASH_FLASH_LENGTH, CLASSIC_FLASH_LENGTH);
        setOption(PREF_CRASH_FLASH_FADE, FLASH_FADE_SHARP);
    }

    private String exportJson() {
        flushSave();
        try {
            JSONObject values = new JSONObject();
            for (Setting setting : settings) setting.export(values);
            JSONObject file = new JSONObject();
            file.put("format", EXPORT_FORMAT);
            file.put("version", EXPORT_VERSION);
            file.put("exported", new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US).format(new Date()));
            file.put("settings", values);
            return file.toString(2);
        } catch (JSONException e) {
            return null;
        }
    }

    private Map<String, Object> parseSettings(CharSequence text) {
        if (text == null) return null;
        String raw = text.toString();
        int start = raw.indexOf('{');
        int end = raw.lastIndexOf('}');
        if (start < 0 || end <= start) return null;
        JSONObject root;
        try {
            root = new JSONObject(raw.substring(start, end + 1));
        } catch (JSONException e) {
            return null;
        }
        JSONObject values = root.optJSONObject("settings");
        if (values == null) values = root;
        Map<String, Object> found = new HashMap<>();
        for (Setting setting : settings) {
            Object value = values.opt(setting.key);
            if (value != null && setting.accepts(value)) found.put(setting.key, value);
        }
        return found.isEmpty() ? null : found;
    }

    private void applyImport(CharSequence text) {
        final Map<String, Object> imported = parseSettings(text);
        if (imported == null) {
            announce("That is not a Flappy Bird settings file", null, null);
            return;
        }
        applyBulkChange(() -> {
            Map<String, Object> merged = snapshot();
            merged.putAll(imported);
            readAll(merged);
        }, imported.size() + " settings imported");
    }

    private void startExport() {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/json");
        String day = new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date());
        intent.putExtra(Intent.EXTRA_TITLE, "flappy-settings-" + day + ".json");
        launchPicker(intent, REQUEST_EXPORT);
    }

    private void startImport() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        intent.putExtra(Intent.EXTRA_MIME_TYPES, new String[]{"application/json", "text/plain", "application/octet-stream"});
        launchPicker(intent, REQUEST_IMPORT);
    }

    private void launchPicker(Intent intent, int requestCode) {
        try {
            startActivityForResult(intent, requestCode);
        } catch (ActivityNotFoundException e) {
            announce("No file picker is available on this device", null, null);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode != REQUEST_EXPORT && requestCode != REQUEST_IMPORT) return;
        if (resultCode != RESULT_OK || data == null || data.getData() == null) return;
        if (requestCode == REQUEST_EXPORT) writeExport(data.getData());
        else readImport(data.getData());
    }

    private OutputStream openForWriting(Uri uri) throws IOException {
        OutputStream out;
        try {
            out = getContentResolver().openOutputStream(uri, "wt");
        } catch (FileNotFoundException | IllegalArgumentException e) {
            out = getContentResolver().openOutputStream(uri);
        }
        if (out == null) throw new IOException("No output stream");
        return out;
    }

    private void writeExport(Uri uri) {
        String json = exportJson();
        if (json == null) {
            announce("Could not create the settings file", null, null);
            return;
        }
        try (OutputStream out = openForWriting(uri)) {
            out.write(json.getBytes("UTF-8"));
            announce("Settings exported", null, null);
        } catch (IOException | SecurityException e) {
            announce("Could not save the file", null, null);
        }
    }

    private void readImport(Uri uri) {
        try (InputStream in = getContentResolver().openInputStream(uri)) {
            if (in == null) throw new IOException("No input stream");
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            byte[] buffer = new byte[8192];
            int read;
            while ((read = in.read(buffer)) != -1) {
                bytes.write(buffer, 0, read);
                if (bytes.size() > MAX_IMPORT_BYTES) {
                    announce("That file is too big to be a settings file", null, null);
                    return;
                }
            }
            applyImport(bytes.toString("UTF-8"));
        } catch (IOException | SecurityException e) {
            announce("Could not read that file", null, null);
        }
    }

    private void shareSettings() {
        String json = exportJson();
        if (json == null) return;
        Intent send = new Intent(Intent.ACTION_SEND);
        send.setType("text/plain");
        send.putExtra(Intent.EXTRA_SUBJECT, "Flappy Bird settings");
        send.putExtra(Intent.EXTRA_TEXT, json);
        try {
            startActivity(Intent.createChooser(send, "Share settings"));
        } catch (ActivityNotFoundException e) {
            announce("No app can share text on this device", null, null);
        }
    }

    private void pasteSettings() {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = clipboard != null ? clipboard.getPrimaryClip() : null;
        if (clip == null || clip.getItemCount() == 0) {
            announce("Copy a shared settings text first", null, null);
            return;
        }
        applyImport(clip.getItemAt(0).coerceToText(this));
    }

    private SharedPreferences profile(int slot) {
        return getSharedPreferences(PROFILE_PREFS_PREFIX + slot, MODE_PRIVATE);
    }

    private void refreshSlots() {
        for (int i = 0; i < PROFILE_COUNT; i++) {
            Map<String, ?> stored = profile(i + 1).getAll();
            boolean filled = !stored.isEmpty();
            String status = "Empty";
            if (filled) {
                Object savedAt = stored.get(PROFILE_SAVED_AT);
                status = "Saved";
                if (savedAt instanceof Long) {
                    status = "Saved " + DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(new Date((Long) savedAt));
                }
            }
            slotSummaries[i].setText(status);
            slotLoadButtons[i].setEnabled(filled);
            slotLoadButtons[i].setAlpha(filled ? 1f : 0.38f);
            slotClearButtons[i].setEnabled(filled);
            slotClearButtons[i].setAlpha(filled ? 1f : 0.38f);
        }
    }

    private void onSaveSlot(final int slot) {
        if (profile(slot).getAll().isEmpty()) {
            writeSlot(slot);
            return;
        }
        confirm("Replace slot " + slot, "The setup saved in this slot will be overwritten.", "Replace", () -> writeSlot(slot));
    }

    private void writeSlot(int slot) {
        flushSave();
        SharedPreferences.Editor editor = profile(slot).edit();
        editor.clear();
        for (Setting setting : settings) setting.write(editor);
        editor.putLong(PROFILE_SAVED_AT, System.currentTimeMillis());
        editor.apply();
        refreshSlots();
        announce("Saved to slot " + slot, null, null);
    }

    private void onLoadSlot(final int slot) {
        final Map<String, ?> stored = profile(slot).getAll();
        if (stored.isEmpty()) {
            announce("Slot " + slot + " is empty", null, null);
            return;
        }
        applyBulkChange(() -> readAll(stored), "Slot " + slot + " loaded");
    }

    private void onClearSlot(final int slot) {
        confirm("Clear slot " + slot, "The setup saved in this slot will be deleted.", "Clear", () -> {
            profile(slot).edit().clear().apply();
            refreshSlots();
            announce("Slot " + slot + " cleared", null, null);
        });
    }

    private void showNumberDialog(final NumberSetting setting) {
        int inputType = setting.integer
                ? InputType.TYPE_CLASS_NUMBER
                : InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL;
        final EditText input = dialogInput(setting.plain(setting.value), inputType, setting.plain(setting.defaultValue));
        showDialog(new MaterialAlertDialogBuilder(this)
                .setTitle(setting.label)
                .setMessage("Enter a value from " + setting.plain(setting.min) + " to " + setting.plain(setting.max) + ".")
                .setView(dialogBody(input))
                .setPositiveButton("Set", (dialog, which) -> setting.setFromText(input.getText().toString()))
                .setNeutralButton("Default", (dialog, which) -> setting.setFromUser(setting.defaultValue))
                .setNegativeButton("Cancel", null)
                );
    }

    private void showListDialog(final ListSetting setting) {
        final EditText input = dialogInput(setting.value.replace(",", ", "), InputType.TYPE_CLASS_TEXT, "11, 34, 79, 210");
        showDialog(new MaterialAlertDialogBuilder(this)
                .setTitle(setting.label)
                .setMessage("List the scores where lightning should hit the bird, separated by commas. Leave it empty for none.")
                .setView(dialogBody(input))
                .setPositiveButton("Set", (dialog, which) -> setting.setFromUser(input.getText().toString()))
                .setNeutralButton("Default", (dialog, which) -> setting.setFromUser(setting.defaultValue))
                .setNegativeButton("Cancel", null)
                );
    }

    private static float round2(float value) {
        return Math.round(value * 100f) / 100f;
    }

    private static float parseNumber(String text, float fallback) {
        if (text == null) return fallback;
        try {
            return Float.parseFloat(text.trim().replace(',', '.'));
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    private static String normalizeScores(String text) {
        TreeSet<Integer> scores = new TreeSet<>();
        if (text != null) {
            for (String part : text.split("[^0-9]+")) {
                if (part.isEmpty() || part.length() > 7) continue;
                int score = Integer.parseInt(part);
                if (score <= MAX_SCORE_VALUE && scores.size() < MAX_LIGHTNING_SCORES) scores.add(score);
            }
        }
        StringBuilder joined = new StringBuilder();
        for (int score : scores) {
            if (joined.length() > 0) joined.append(',');
            joined.append(score);
        }
        return joined.toString();
    }

    private void tintSwitch(MaterialSwitch view) {
        int[][] states = {{android.R.attr.state_checked}, {}};
        view.setThumbTintList(new ColorStateList(states, new int[]{COLOR_ON_PRIMARY, COLOR_OUTLINE}));
        view.setTrackTintList(new ColorStateList(states, new int[]{COLOR_PRIMARY, COLOR_TRACK_OFF}));
        view.setTrackDecorationTintList(new ColorStateList(states, new int[]{Color.TRANSPARENT, COLOR_OUTLINE}));
    }

    private abstract class Setting {
        final String key;
        final View row;
        final String label;
        boolean randomizable = true;
        String note;

        Setting(String key, View rowId) {
            this.key = key;
            this.row = rowId;
            String text = firstText(row);
            this.label = text != null ? text : key;
            settings.add(this);
            settingsByKey.put(key, this);
        }

        abstract void read(Map<String, ?> source);

        abstract void write(SharedPreferences.Editor editor);

        abstract void reset();

        abstract void randomize();

        abstract void syncView();

        abstract Object capture();

        abstract void restore(Object captured);

        abstract String describe();

        abstract void export(JSONObject out) throws JSONException;

        abstract boolean accepts(Object value);
    }

    private final class ToggleSetting extends Setting {
        final boolean defaultValue;
        final MaterialSwitch toggle;
        boolean value;

        ToggleSetting(String key, boolean defaultValue, View rowId) {
            super(key, rowId);
            this.defaultValue = defaultValue;
            this.value = defaultValue;
            this.toggle = (MaterialSwitch) row.findViewWithTag(TAG_SWITCH);
            tintSwitch(toggle);
            toggle.setOnCheckedChangeListener((button, checked) -> {
                if (checked == value) return;
                value = checked;
                onUserChange(ToggleSetting.this);
            });
            row.setOnClickListener(v -> toggle.setChecked(!toggle.isChecked()));
        }

        @Override
        void read(Map<String, ?> source) {
            Object stored = source.get(key);
            value = stored instanceof Boolean ? (Boolean) stored : defaultValue;
            if (stored != null && !(stored instanceof Boolean)) unsaved.add(this);
        }

        @Override
        void write(SharedPreferences.Editor editor) {
            editor.putBoolean(key, value);
        }

        @Override
        void reset() {
            value = defaultValue;
        }

        @Override
        void randomize() {
            value = random.nextBoolean();
        }

        @Override
        void syncView() {
            toggle.setChecked(value);
        }

        @Override
        Object capture() {
            return value;
        }

        @Override
        void restore(Object captured) {
            if (captured instanceof Boolean) value = (Boolean) captured;
        }

        @Override
        String describe() {
            return value ? "On" : "Off";
        }

        @Override
        void export(JSONObject out) throws JSONException {
            out.put(key, value);
        }

        @Override
        boolean accepts(Object candidate) {
            return candidate instanceof Boolean;
        }
    }

    private final class NumberSetting extends Setting {
        final float defaultValue;
        final boolean integer;
        final float sliderMin;
        final float sliderMax;
        final float step;
        final float min;
        final float max;
        final int unit;
        final Slider slider;
        final TextView pill;
        final ChipStyle pillStyle;
        String zeroLabel;
        float value;

        NumberSetting(String key, float defaultValue, boolean integer, float sliderMin, float sliderMax, float step,
                      float min, float max, int unit, View rowId) {
            super(key, rowId);
            this.defaultValue = defaultValue;
            this.integer = integer;
            this.sliderMin = sliderMin;
            this.sliderMax = sliderMax;
            this.step = step;
            this.min = min;
            this.max = max;
            this.unit = unit;
            this.value = defaultValue;
            this.slider = (Slider) row.findViewWithTag(TAG_SLIDER);
            this.pill = (TextView) row.findViewWithTag(TAG_VALUE);
            this.pillStyle = new ChipStyle(pill, COLOR_HIGH, COLOR_PRIMARY_CONTAINER, COLOR_ON, COLOR_ON_PRIMARY_CONTAINER,
                    dp(16), dp(16), 0, 0);

            slider.setValueFrom(sliderMin);
            slider.setValueTo(sliderMax);
            slider.setContentDescription(label);
            slider.addOnChangeListener((view, position, fromUser) -> {
                if (!fromUser) return;
                float snapped = snap(position);
                if (snapped == value) return;
                value = snapped;
                renderPill();
                if (isDefault()) view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK);
                onUserChange(NumberSetting.this);
            });
            slider.addOnSliderTouchListener(new Slider.OnSliderTouchListener() {
                @Override
                public void onStartTrackingTouch(Slider view) {
                    view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK);
                }

                @Override
                public void onStopTrackingTouch(Slider view) {
                    syncSlider();
                }
            });
            pill.setOnClickListener(v -> showNumberDialog(NumberSetting.this));
        }

        boolean isDefault() {
            return value == defaultValue;
        }

        private float snap(float position) {
            float snapped = round2(Math.round(position / step) * step);
            snapped = Math.max(sliderMin, Math.min(sliderMax, snapped));
            boolean dense = (sliderMax - sliderMin) / step >= 50f;
            boolean defaultOnSlider = defaultValue >= sliderMin && defaultValue <= sliderMax;
            if (dense && defaultOnSlider && Math.abs(snapped - defaultValue) <= step * 1.01f) snapped = defaultValue;
            return integer ? Math.round(snapped) : snapped;
        }

        private float sanitize(float raw) {
            if (Float.isNaN(raw) || Float.isInfinite(raw)) return defaultValue;
            float clamped = Math.max(min, Math.min(max, raw));
            return integer ? Math.round(clamped) : round2(clamped);
        }

        String plain(float number) {
            if (integer) return String.valueOf(Math.round(number));
            String text = String.format(Locale.US, "%.2f", number);
            if (text.endsWith("00")) return text.substring(0, text.length() - 3);
            if (text.endsWith("0")) return text.substring(0, text.length() - 1);
            return text;
        }

        private void renderPill() {
            pill.setText(describe());
            pillStyle.set(!isDefault(), false);
        }

        private void syncSlider() {
            slider.setValue(Math.max(sliderMin, Math.min(sliderMax, value)));
        }

        void setFromText(String text) {
            float parsed = parseNumber(text, Float.NaN);
            if (Float.isNaN(parsed) || Float.isInfinite(parsed)) {
                announce("That is not a number", null, null);
                return;
            }
            setFromUser(parsed);
        }

        void setFromUser(float number) {
            value = sanitize(number);
            syncView();
            onUserChange(this);
        }

        @Override
        void read(Map<String, ?> source) {
            Object stored = source.get(key);
            float raw = defaultValue;
            if (stored instanceof Number) raw = ((Number) stored).floatValue();
            else if (stored instanceof String) raw = parseNumber((String) stored, defaultValue);
            value = sanitize(raw);
            boolean rightType = integer ? stored instanceof Integer : stored instanceof Float;
            if (stored != null && (!rightType || value != raw)) unsaved.add(this);
        }

        @Override
        void write(SharedPreferences.Editor editor) {
            if (integer) editor.putInt(key, Math.round(value));
            else editor.putFloat(key, value);
        }

        @Override
        void reset() {
            value = defaultValue;
        }

        @Override
        void randomize() {
            value = snap(sliderMin + random.nextFloat() * (sliderMax - sliderMin));
        }

        @Override
        void syncView() {
            renderPill();
            syncSlider();
        }

        @Override
        Object capture() {
            return value;
        }

        @Override
        void restore(Object captured) {
            if (captured instanceof Float) value = sanitize((Float) captured);
        }

        @Override
        void export(JSONObject out) throws JSONException {
            if (integer) out.put(key, Math.round(value));
            else out.put(key, Double.parseDouble(plain(value)));
        }

        @Override
        boolean accepts(Object candidate) {
            float parsed = Float.NaN;
            if (candidate instanceof Number) parsed = ((Number) candidate).floatValue();
            else if (candidate instanceof String) parsed = parseNumber((String) candidate, Float.NaN);
            return !Float.isNaN(parsed) && !Float.isInfinite(parsed);
        }

        @Override
        String describe() {
            if (zeroLabel != null && value == 0f) return zeroLabel;
            String number = plain(value);
            if (unit == UNIT_TIMES) return number + "x";
            if (unit == UNIT_PERCENT) return number + "%";
            if (unit == UNIT_POINTS) return "+" + number;
            if (unit == UNIT_DEGREES) return number + " deg";
            if (unit == UNIT_MS) return number + " ms";
            if (unit == UNIT_SECONDS) return number + " s";
            return number;
        }
    }

    private final class ChoiceSetting extends Setting {
        final int defaultValue;
        final int[] values;
        final String[] names;
        final View[] views;
        final ChipStyle[] chips;
        final TextView pill;
        boolean enabled = true;
        int value;

        ChoiceSetting(String key, int defaultValue, View rowId, int[] values, int[] colors, String[] swatchNames) {
            super(key, rowId);
            this.defaultValue = defaultValue;
            this.values = values;
            this.value = defaultValue;
            List<View> options = new ArrayList<>();
            collectTagged(row, TAG_OPTION, options);
            if (options.size() != values.length) {
                throw new IllegalStateException(key + ": layout has " + options.size() + " options, code has " + values.length);
            }
            this.names = new String[values.length];
            this.views = new View[values.length];
            this.chips = new ChipStyle[values.length];

            boolean swatches = swatchNames != null;
            for (int i = 0; i < values.length; i++) {
                final int index = i;
                View view = options.get(i);
                String text = swatches ? swatchNames[i] : firstText(view);
                views[i] = view;
                names[i] = text != null ? text : String.valueOf(values[i]);
                chips[i] = swatches
                        ? new ChipStyle(view, colors[i], colors[i], 0, 0, dp(10), dp(22), dp(3), COLOR_ON)
                        : new ChipStyle(view, COLOR_HIGH, colors[i], COLOR_ON_VARIANT, COLOR_ON_ACCENT, dp(12), dp(22), 0, 0);
                view.setContentDescription(names[i]);
                view.setOnClickListener(v -> choose(index));
            }

            TextView nameView = (TextView) row.findViewWithTag(TAG_VALUE);
            if (nameView != null) nameView.setBackground(rounded(COLOR_HIGH, dp(16)));
            this.pill = nameView;
        }

        private int indexOf(int candidate) {
            for (int i = 0; i < values.length; i++) {
                if (values[i] == candidate) return i;
            }
            return -1;
        }

        private void choose(int index) {
            if (!enabled || values[index] == value) return;
            value = values[index];
            render(true);
            onUserChange(this);
        }

        private void render(boolean animate) {
            int selected = Math.max(0, indexOf(value));
            for (int i = 0; i < chips.length; i++) chips[i].set(i == selected, animate);
            if (pill != null) pill.setText(names[selected]);
        }

        void setEnabled(boolean on) {
            enabled = on;
            float alpha = on ? 1f : 0.38f;
            for (View view : views) view.setAlpha(alpha);
            if (pill != null) pill.setAlpha(alpha);
        }

        @Override
        void read(Map<String, ?> source) {
            Object stored = source.get(key);
            int raw = stored instanceof Number ? ((Number) stored).intValue() : defaultValue;
            value = indexOf(raw) >= 0 ? raw : defaultValue;
            if (stored != null && (!(stored instanceof Integer) || value != raw)) unsaved.add(this);
        }

        @Override
        void write(SharedPreferences.Editor editor) {
            editor.putInt(key, value);
        }

        @Override
        void reset() {
            value = defaultValue;
        }

        @Override
        void randomize() {
            value = values[random.nextInt(values.length)];
        }

        @Override
        void syncView() {
            render(false);
        }

        @Override
        Object capture() {
            return value;
        }

        @Override
        void restore(Object captured) {
            if (captured instanceof Integer && indexOf((Integer) captured) >= 0) value = (Integer) captured;
        }

        @Override
        String describe() {
            return names[Math.max(0, indexOf(value))];
        }

        @Override
        void export(JSONObject out) throws JSONException {
            out.put(key, value);
        }

        @Override
        boolean accepts(Object candidate) {
            return candidate instanceof Number && indexOf(((Number) candidate).intValue()) >= 0;
        }
    }

    private final class ListSetting extends Setting {
        final String defaultValue;
        final TextView pill;
        String value;

        ListSetting(String key, String defaultValue, View rowId) {
            super(key, rowId);
            this.defaultValue = normalizeScores(defaultValue);
            this.value = this.defaultValue;
            this.pill = (TextView) row.findViewWithTag(TAG_VALUE);
            pill.setBackground(rounded(COLOR_HIGH, dp(16)));
            row.setOnClickListener(v -> showListDialog(ListSetting.this));
        }

        void setFromUser(String text) {
            value = normalizeScores(text);
            syncView();
            onUserChange(this);
        }

        @Override
        void read(Map<String, ?> source) {
            Object stored = source.get(key);
            value = stored instanceof String ? normalizeScores((String) stored) : defaultValue;
            if (stored != null && !value.equals(stored)) unsaved.add(this);
        }

        @Override
        void write(SharedPreferences.Editor editor) {
            editor.putString(key, value);
        }

        @Override
        void reset() {
            value = defaultValue;
        }

        @Override
        void randomize() {
            value = normalizeScores((1 + random.nextInt(20)) + "," + (21 + random.nextInt(50)) + "," + (71 + random.nextInt(100)));
        }

        @Override
        void syncView() {
            pill.setText(describe());
        }

        @Override
        Object capture() {
            return value;
        }

        @Override
        void restore(Object captured) {
            if (captured instanceof String) value = normalizeScores((String) captured);
        }

        @Override
        String describe() {
            return value.isEmpty() ? "None" : value.replace(",", ", ");
        }

        @Override
        void export(JSONObject out) throws JSONException {
            out.put(key, value);
        }

        @Override
        boolean accepts(Object candidate) {
            return candidate instanceof String;
        }
    }

    private static final class ChipStyle {
        private final View view;
        private final GradientDrawable shape = new GradientDrawable();
        private final int restColor;
        private final int activeColor;
        private final int restText;
        private final int activeText;
        private final float restRadius;
        private final float activeRadius;
        private final int ringWidth;
        private final int ringColor;
        private float progress = -1f;
        private ValueAnimator animator;

        ChipStyle(View view, int restColor, int activeColor, int restText, int activeText,
                  float restRadius, float activeRadius, int ringWidth, int ringColor) {
            this.view = view;
            this.restColor = restColor;
            this.activeColor = activeColor;
            this.restText = restText;
            this.activeText = activeText;
            this.restRadius = restRadius;
            this.activeRadius = activeRadius;
            this.ringWidth = ringWidth;
            this.ringColor = ringColor;
            view.setBackground(new RippleDrawable(ColorStateList.valueOf(COLOR_RIPPLE), shape, null));
        }

        void set(boolean active, boolean animate) {
            float target = active ? 1f : 0f;
            if (animator != null) {
                animator.cancel();
                animator = null;
            }
            if (!animate || progress < 0f) {
                render(target);
                return;
            }
            if (progress == target) return;

            animator = ValueAnimator.ofFloat(progress, target);
            animator.setDuration(220L);
            animator.addUpdateListener(animation -> render((Float) animation.getAnimatedValue()));
            animator.start();

            if (active) {
                view.setScaleX(0.9f);
                view.setScaleY(0.9f);
                view.animate().scaleX(1f).scaleY(1f).setDuration(280L).setInterpolator(new OvershootInterpolator(3f)).start();
            }
        }

        private void render(float amount) {
            progress = amount;
            shape.setColor(blend(restColor, activeColor, amount));
            shape.setCornerRadius(restRadius + (activeRadius - restRadius) * amount);
            shape.setStroke(Math.round(ringWidth * amount), ringColor);
            if (view instanceof TextView) ((TextView) view).setTextColor(blend(restText, activeText, amount));
        }
    }

    private static final class ArrowDrawable extends Drawable {
        private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Path path = new Path();
        private final int size;

        ArrowDrawable(int size, float strokeWidth, int color) {
            this.size = size;
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(strokeWidth);
            paint.setStrokeCap(Paint.Cap.ROUND);
            paint.setStrokeJoin(Paint.Join.ROUND);
            paint.setColor(color);
        }

        @Override
        public void draw(Canvas canvas) {
            Rect bounds = getBounds();
            float centerX = bounds.left + bounds.width() / 2f;
            float centerY = bounds.top + bounds.height() / 2f;
            float reach = size * 0.36f;
            float wing = reach * 0.85f;
            path.reset();
            path.moveTo(centerX + reach, centerY);
            path.lineTo(centerX - reach, centerY);
            path.moveTo(centerX - reach + wing, centerY - wing);
            path.lineTo(centerX - reach, centerY);
            path.lineTo(centerX - reach + wing, centerY + wing);
            canvas.drawPath(path, paint);
        }

        @Override
        public void setAlpha(int alpha) {
            paint.setAlpha(alpha);
        }

        @Override
        public void setColorFilter(ColorFilter filter) {
            paint.setColorFilter(filter);
        }

        @Override
        public int getOpacity() {
            return PixelFormat.TRANSLUCENT;
        }

        @Override
        public int getIntrinsicWidth() {
            return size;
        }

        @Override
        public int getIntrinsicHeight() {
            return size;
        }
    }

    static final class PreviewConfig {
        int birdColor = DEFAULT_BIRD_COLOR;
        int birdHue;
        int extraLives;
        int speedRamp;
        int gapShrink;
        boolean pauseButton;
        boolean rainbowBird;
        boolean birdTrail;
        boolean wingAnimation = true;
        boolean keepFlapping;
        boolean ghostBird;
        float birdSize = 1f;
        float birdHitbox = 1f;
        boolean dayNight;
        int background = DEFAULT_BACKGROUND;
        float bgScroll = DEFAULT_BG_SCROLL_SPEED;
        float groundScroll = DEFAULT_GROUND_SCROLL_SPEED;
        int weather;
        int pipeColor;
        boolean randomPipeColors;
        float pipeWidth = 1f;
        float pipeGap = 1f;
        float pipeSpacing = 1f;
        float pipeVariation = 1f;
        boolean crashFlash = DEFAULT_CRASH_FLASH_ENABLED;
        int crashFlashColor;
        int crashFlashStrength = DEFAULT_CRASH_FLASH_STRENGTH;
        int crashFlashLength = DEFAULT_CRASH_FLASH_LENGTH;
        boolean fixedStep;
        float maxFallSpeed;
        boolean ceiling = true;
        boolean birdTilt = true;
        int maxTiltUp = DEFAULT_MAX_TILT_UP;
        int maxTiltDown = DEFAULT_MAX_TILT_DOWN;
        float tiltSpeed = DEFAULT_TILT_SPEED;
        int scorePoint;
        boolean pixelSteps;
        int tiltStyle;
        int hitboxShape;
        int gapPlacement;
        int crashFlashFade;
        int birdPosition = DEFAULT_BIRD_POSITION;
        float wingSpeed = DEFAULT_WING_SPEED;
        int dayNightLength = DEFAULT_DAY_NIGHT_LENGTH;
        boolean showHitboxes;
        float rampTopSpeed = DEFAULT_RAMP_TOP_SPEED;
        int rampMinGap = DEFAULT_RAMP_MIN_GAP;
        boolean movingPipes;
        int moveTier1 = DEFAULT_PIPE_MOVE_TIER_1_SCORE;
        int moveTier2 = DEFAULT_PIPE_MOVE_TIER_2_SCORE;
        boolean goldenPipes;
        int goldenChance;
        int goldenBonus;
        float gravity = 1f;
        float jump = 1f;
        float hangDelay = 1f;
        float gameSpeed = 1f;
        float speedVariation;
        int scoreMultiplier = 1;
        boolean screenShake;
        float shakeStrength = 1f;
        boolean motionBlur;
        float blurStrength = 1f;
        int gameOverOpacity;
        boolean jetpack;
        boolean drunk;
        float drunkStrength;
        boolean upsideDown;
        boolean reversePipes;
        boolean hideSettingsIcon;
    }

    static class SettingsPreviewView extends View implements Choreographer.FrameCallback {

        static final int DEMO_SHAKE = 1;
        static final int DEMO_DIM = 2;
        static final int DEMO_HITBOX = 3;
        static final int DEMO_STRIKE = 4;
        static final int DEMO_MEGA_STRIKE = 5;
        static final int DEMO_MOVING_PIPES = 6;
        static final int DEMO_FAST_PIPES = 7;
        static final int DEMO_CRASH_FLASH = 8;

        private static final float TARGET_FPS = 120f;
        private static final float MAX_FRAME_DELTA = 1f / 30f;
        private static final float REFERENCE_SCALE = 4.5f;
        private static final int SOURCE_WIDTH = 288;
        private static final int SOURCE_HEIGHT = 512;
        private static final int MIN_HEIGHT_PX = 48;
        private static final int MAX_PIPES = 40;
        private static final int PIPE_COLOR_COUNT = 9;
        private static final int TRAIL_LENGTH = 15;
        private static final int HUE_STEPS = 120;
        private static final int MAX_BLUR_SAMPLES = 8;
        private static final float BLUR_SHUTTER_SEC = 1f / 48f;
        private static final float BLUR_MAX_LENGTH = 48f;
        private static final float BLUR_MAX_ROTATION = 60f;
        private static final float GRAVITY_PER_FRAME = 0.10f;
        private static final float FLAP_PER_FRAME = -3.2f;
        private static final float PIPE_SPEED_PER_FRAME = 1.1f;
        private static final float ROTATION_DELAY_PER_FRAME = 2.7f;
        private static final float ROTATION_UP_PER_SEC = 6.0f * TARGET_FPS;
        private static final float ROTATION_DOWN_PER_SEC = 2.5f * TARGET_FPS;
        private static final float FLAP_COOLDOWN_SEC = 0.12f;
        private static final float FLAP_ANIMATION_SEC = 0.48f;
        private static final float WING_FRAME_SEC = 0.075f;
        private static final float DAY_NIGHT_SPEED_UP = 6f;
        private static final float PIXEL_STEP_EPSILON = 0.001f;
        private static final float CLASSIC_TILT_KICK_PER_SEC = -600f;
        private static final float CLASSIC_TILT_PULL_PER_SEC2 = 1440f;
        private static final float SQUARE_HITBOX_SIZE = 20f;
        private static final float CLASSIC_GAP_BOTTOM_CENTER = 270f;
        private static final float CLASSIC_GAP_BOTTOM_SPREAD = 180f;
        private static final float RANDOM_BIRD_SEC = 2.5f;
        private static final float RANDOM_BACKGROUND_SEC = 6f;
        private static final float SPEED_REROLL_SEC = 3f;
        private static final float LIGHTNING_PER_SEC = 0.4f;
        private static final float DIM_PEEK_SEC = 1.6f;
        private static final float HITBOX_PEEK_SEC = 2.5f;
        private static final float MOVING_PEEK_SEC = 6f;
        private static final float MOVING_PIPES_SEC = 30f;
        private static final float MOVE_SPEED_TIER_1 = 0.03f;
        private static final float MOVE_SPEED_TIER_2 = 0.06f;
        private static final float PILOT_STEP_SEC = 1f / 60f;
        private static final int PILOT_STEPS = 96;
        private static final float PILOT_CRASH_COST = 1000f;
        private static final int[] PILOT_WAITS = {0, 3, 6, 9, 12, 16, 20, 25, 30, 38, 45};
        private static final int MAX_SCORE = 99999;
        private static final int RAMP_LOOP_SCORE = 100;

        final PreviewConfig config = new PreviewConfig();

        private final Choreographer choreographer = Choreographer.getInstance();
        private final Random random = new Random();
        private final Weather weather = new Weather(random);
        private final List<PreviewPipe> pipes = new ArrayList<>();

        private final Paint pixelPaint = new Paint();
        private final Paint birdPaint = new Paint();
        private final Paint trailPaint = new Paint();
        private final Paint nightPaint = new Paint();
        private final Paint pipePaint = new Paint();
        private final Paint goldenPaint = new Paint();
        private final Paint overlayPaint = new Paint();
        private final Paint iconPaint = new Paint();
        private final Paint hitboxPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final PorterDuffXfermode additive = new PorterDuffXfermode(PorterDuff.Mode.ADD);
        private final ColorFilter burntFilter = new PorterDuffColorFilter(Color.rgb(40, 40, 40), PorterDuff.Mode.MULTIPLY);
        private final ColorFilter[] pipeFilters = new ColorFilter[PIPE_COLOR_COUNT];
        private final ColorFilter[] hueFilters = new ColorFilter[HUE_STEPS];
        private final ColorMatrix colorMatrix = new ColorMatrix();
        private final Matrix birdMatrix = new Matrix();
        private final Matrix shineMatrix = new Matrix();
        private final RectF destination = new RectF();
        private final Rect edgeSource = new Rect();
        private final Rect birdRect = new Rect();
        private final int[] digitBuffer = new int[10];
        private Shader shineShader;

        private Bitmap sourceDay;
        private Bitmap sourceNight;
        private Bitmap sourceGround;
        private Bitmap sourcePipe;
        private Bitmap sourceGameOver;
        private Bitmap sourceGear;
        private final Bitmap[][] sourceBirds = new Bitmap[3][3];
        private final Bitmap[] sourceDigits = new Bitmap[10];

        private Bitmap day;
        private Bitmap night;
        private Bitmap ground;
        private Bitmap pipeUp;
        private Bitmap pipeDown;
        private Bitmap gameOver;
        private Bitmap gear;
        private final Bitmap[][] birds = new Bitmap[3][3];
        private final Bitmap[] digits = new Bitmap[10];

        private boolean spritesLoaded;
        private boolean digitsLoaded;
        private boolean ready;
        private boolean attached;
        private boolean hostResumed;
        private boolean running;
        private long lastFrameNanos;
        private double clock;

        private int width;
        private int height;
        private float scale = 1f;
        private float fx = 1f;
        private float groundTop;

        private float pipeGap;
        private float pipeSpacing;
        private float pipeSpeed;
        private float speedVariation = 1f;
        private float gravity;
        private float flapVelocity;
        private float jetpackThrust;
        private float rotationDelay;
        private float maxMoveRange;
        private float moveRange;
        private float moveCounter;
        private float moveTimer;
        private float moveSpeed = MOVE_SPEED_TIER_1;
        private boolean tier1Reached;
        private boolean tier2Reached;

        private float birdBaseX;
        private float birdX;
        private float birdY;
        private float birdVelocity;
        private float birdRotation;
        private int birdFrame = 1;
        private int birdColorIndex;
        private int randomBird;
        private boolean randomNight;
        private boolean thrusting;
        private double lastFlap = -10;
        private double lastBirdSwap;
        private double lastBackgroundSwap;
        private double lastSpeedRoll;
        private float drunkPhase;

        private float backgroundX;
        private float groundX;
        private float dayNightProgress;
        private float rainbowHue;
        private float shineOffset;
        private float shinePeriod = 200f;
        private int score;

        private float shakeTimer;
        private float shakeMagnitude;
        private float flash;
        private int flashColor = Color.WHITE;
        private float flashPeak;
        private float flashElapsed;
        private float flashLength = 1f;
        private boolean flashSharp;
        private float tiltVelocity;
        private float stepAccumulator;
        private float fallLimit;
        private float burntTimer;
        private float dimTimer;
        private float hitboxTimer;

        private float backgroundBlur;
        private float groundBlur;
        private float pipeBlur;
        private float birdBlurX;
        private float birdBlurY;
        private float birdBlurRotation;

        private final float[] trailX = new float[TRAIL_LENGTH];
        private final float[] trailY = new float[TRAIL_LENGTH];
        private final float[] trailRotation = new float[TRAIL_LENGTH];
        private final float[] trailHue = new float[TRAIL_LENGTH];
        private final int[] trailColor = new int[TRAIL_LENGTH];
        private final int[] trailFrame = new int[TRAIL_LENGTH];
        private int trailStart;
        private int trailCount;

        private int lastWeather = -1;
        private boolean lastReverse;
        private float lastSpacing = -1f;
        private float lastSpeedVariation = -1f;
        private int lastTier1 = -1;
        private int lastTier2 = -1;
        private int lastRamp = -1;
        private int lastShrink = -1;
        private int tunedScore = -1;
        private float pilotFrameDelta = 1f / TARGET_FPS;

        public SettingsPreviewView(Context context) {
            super(context);
            createPaints();
            loadSprites();
        }

        private void createPaints() {
            Paint[] pixelPaints = {pixelPaint, birdPaint, trailPaint, nightPaint, pipePaint, goldenPaint};
            for (Paint paint : pixelPaints) {
                paint.setFilterBitmap(false);
                paint.setAntiAlias(false);
            }
            goldenPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_ATOP));
            hitboxPaint.setStyle(Paint.Style.STROKE);
            hitboxPaint.setColor(COLOR_SKY);

            pipeFilters[1] = new PorterDuffColorFilter(0xFFD05050, PorterDuff.Mode.MULTIPLY);
            pipeFilters[2] = new PorterDuffColorFilter(0xFF6080E0, PorterDuff.Mode.MULTIPLY);
            pipeFilters[3] = new PorterDuffColorFilter(0xFFE0E060, PorterDuff.Mode.MULTIPLY);
            pipeFilters[4] = new PorterDuffColorFilter(0xFFFFFFFF, PorterDuff.Mode.SRC_ATOP);
            pipeFilters[5] = new PorterDuffColorFilter(0xFFE879E8, PorterDuff.Mode.MULTIPLY);
            pipeFilters[6] = new PorterDuffColorFilter(0xFF505050, PorterDuff.Mode.MULTIPLY);
            pipeFilters[7] = new PorterDuffColorFilter(0xFF9040D0, PorterDuff.Mode.MULTIPLY);
            pipeFilters[8] = new PorterDuffColorFilter(0xFFE89030, PorterDuff.Mode.MULTIPLY);
        }

        private void loadSprites() {
            try {
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inScaled = false;
                Bitmap atlas = BitmapFactory.decodeResource(getResources(), R.drawable.atlas, options);
                if (atlas == null) return;
                sourceDay = Bitmap.createBitmap(atlas, 0, 0, SOURCE_WIDTH, SOURCE_HEIGHT);
                sourceNight = Bitmap.createBitmap(atlas, 292, 0, SOURCE_WIDTH, SOURCE_HEIGHT);
                sourceGround = Bitmap.createBitmap(atlas, 584, 0, 336, 112);
                sourceGameOver = Bitmap.createBitmap(atlas, 784, 116, 204, 54);
                atlas.recycle();

                sourcePipe = BitmapFactory.decodeResource(getResources(), R.drawable.pipe_green, options);
                int[][] birdIds = {
                        {R.drawable.yellowbird_upflap, R.drawable.yellowbird_midflap, R.drawable.yellowbird_downflap},
                        {R.drawable.bluebird_upflap, R.drawable.bluebird_midflap, R.drawable.bluebird_downflap},
                        {R.drawable.redbird_upflap, R.drawable.redbird_midflap, R.drawable.redbird_downflap}};
                boolean complete = sourcePipe != null;
                for (int color = 0; color < 3; color++) {
                    for (int frame = 0; frame < 3; frame++) {
                        sourceBirds[color][frame] = BitmapFactory.decodeResource(getResources(), birdIds[color][frame], options);
                        if (sourceBirds[color][frame] == null) complete = false;
                    }
                }

                String packageName = getContext().getPackageName();
                int gearId = getResources().getIdentifier("settingsbutton", "drawable", packageName);
                if (gearId != 0) sourceGear = BitmapFactory.decodeResource(getResources(), gearId, options);

                digitsLoaded = true;
                for (int i = 0; i < 10; i++) {
                    int digitId = getResources().getIdentifier("number_" + i, "drawable", packageName);
                    if (digitId != 0) sourceDigits[i] = BitmapFactory.decodeResource(getResources(), digitId, options);
                    if (sourceDigits[i] == null) digitsLoaded = false;
                }
                spritesLoaded = complete;
            } catch (RuntimeException e) {
                spritesLoaded = false;
            }
        }

        private static Bitmap rescale(Bitmap previous, Bitmap source, float targetWidth, float targetHeight) {
            if (source == null) return null;
            Bitmap scaled = Bitmap.createScaledBitmap(source, Math.max(1, (int) targetWidth), Math.max(1, (int) targetHeight), false);
            if (previous != null && previous != source && previous != scaled) previous.recycle();
            return scaled;
        }

        private static Bitmap rescale(Bitmap previous, Bitmap source, float factor) {
            if (source == null) return null;
            return rescale(previous, source, source.getWidth() * factor, source.getHeight() * factor);
        }

        @Override
        protected void onAttachedToWindow() {
            super.onAttachedToWindow();
            attached = true;
            updateLoop();
        }

        @Override
        protected void onDetachedFromWindow() {
            super.onDetachedFromWindow();
            attached = false;
            updateLoop();
        }

        void setHostResumed(boolean resumed) {
            hostResumed = resumed;
            updateLoop();
        }

        private void updateLoop() {
            boolean shouldRun = attached && hostResumed && ready;
            if (shouldRun == running) return;
            running = shouldRun;
            if (running) {
                lastFrameNanos = 0;
                choreographer.postFrameCallback(this);
            } else {
                choreographer.removeFrameCallback(this);
            }
        }

        @Override
        protected void onSizeChanged(int w, int h, int oldWidth, int oldHeight) {
            super.onSizeChanged(w, h, oldWidth, oldHeight);
            width = w;
            height = h;
            ready = false;
            if (spritesLoaded && w > 0 && h >= MIN_HEIGHT_PX) {
                try {
                    rebuild();
                    ready = true;
                } catch (RuntimeException e) {
                    ready = false;
                }
            }
            updateLoop();
            invalidate();
        }

        private void rebuild() {
            scale = height / (float) SOURCE_HEIGHT;
            fx = scale / REFERENCE_SCALE;

            day = rescale(day, sourceDay, SOURCE_WIDTH * scale, height);
            night = rescale(night, sourceNight, SOURCE_WIDTH * scale, height);
            ground = rescale(ground, sourceGround, scale);
            pipeUp = rescale(pipeUp, sourcePipe, scale);
            Matrix flip = new Matrix();
            flip.setScale(1f, -1f);
            Bitmap previousDown = pipeDown;
            pipeDown = Bitmap.createBitmap(pipeUp, 0, 0, pipeUp.getWidth(), pipeUp.getHeight(), flip, true);
            if (previousDown != null && previousDown != pipeDown) previousDown.recycle();
            for (int color = 0; color < 3; color++) {
                for (int frame = 0; frame < 3; frame++) {
                    birds[color][frame] = rescale(birds[color][frame], sourceBirds[color][frame], scale);
                }
            }
            for (int i = 0; i < 10; i++) digits[i] = rescale(digits[i], sourceDigits[i], scale);
            gear = rescale(gear, sourceGear, scale * 0.75f);
            gameOver = rescale(gameOver, sourceGameOver, scale);

            groundTop = height - ground.getHeight();
            maxMoveRange = height * 0.08f;
            hitboxPaint.setStrokeWidth(Math.max(2f, 1.5f * scale));

            float shineWidth = Math.max(1f, 100f * fx);
            shinePeriod = shineWidth * 2f;
            shineShader = new LinearGradient(0, 0, shineWidth, 0,
                    new int[]{Color.argb(0, 255, 215, 0), Color.argb(150, 255, 255, 100), Color.argb(0, 255, 215, 0)},
                    new float[]{0f, 0.5f, 1f}, Shader.TileMode.MIRROR);
            goldenPaint.setShader(shineShader);

            weather.resize(width, height, fx);
            weather.setType(config.weather);

            rollSpeed();
            tune();
            spawnPipes();
            birdX = birdBaseX;
            birdY = groundTop / 2f;
            birdVelocity = 0f;
            birdRotation = 0f;
            trailCount = 0;
            backgroundX = 0f;
            groundX = 0f;
            remember();
        }

        private void remember() {
            lastWeather = config.weather;
            lastReverse = config.reversePipes;
            lastSpacing = config.pipeSpacing;
            lastSpeedVariation = config.speedVariation;
            lastTier1 = config.moveTier1;
            lastTier2 = config.moveTier2;
            lastRamp = config.speedRamp;
            lastShrink = config.gapShrink;
        }

        private void rollSpeed() {
            float spread = Math.max(0f, Math.min(1f, config.speedVariation));
            speedVariation = 1f + (random.nextFloat() - 0.5f) * 2f * spread;
            lastSpeedRoll = clock;
        }

        private void tune() {
            float direction = config.reversePipes ? -1f : 1f;
            float flip = config.upsideDown ? -1f : 1f;
            float baseGravity = GRAVITY_PER_FRAME * scale * TARGET_FPS * TARGET_FPS;

            float tensOfPoints = Math.max(0, score) / 10f;
            float topSpeed = Math.max(1f, config.rampTopSpeed);
            float smallestGap = Math.max(10, Math.min(100, config.rampMinGap)) / 100f;
            float speedRamp = Math.min(topSpeed, 1f + Math.max(0, config.speedRamp) / 100f * tensOfPoints);
            float gapSqueeze = Math.max(smallestGap, 1f - Math.max(0, config.gapShrink) / 100f * tensOfPoints);
            tunedScore = score;

            pipeGap = 48f * scale * 2.2f * Math.max(0.1f, config.pipeGap) * gapSqueeze;
            float spacing = SOURCE_WIDTH * 0.68f * scale * Math.max(0.1f, config.pipeSpacing);
            pipeSpacing = Math.max(spacing, width / (float) (MAX_PIPES - 3));
            pipeSpeed = PIPE_SPEED_PER_FRAME * scale * TARGET_FPS * config.gameSpeed * speedVariation * direction * speedRamp;
            if (config.birdPosition == DEFAULT_BIRD_POSITION) {
                birdBaseX = config.reversePipes ? width * 2f / 3f : width / 3f;
            } else {
                float share = Math.max(10, Math.min(90, config.birdPosition)) / 100f;
                birdBaseX = width * (config.reversePipes ? 1f - share : share);
            }
            gravity = baseGravity * config.gravity * flip;
            flapVelocity = FLAP_PER_FRAME * scale * TARGET_FPS * config.jump * flip;
            jetpackThrust = baseGravity * 1.3f * config.jump * flip;
            rotationDelay = ROTATION_DELAY_PER_FRAME * scale * TARGET_FPS * config.hangDelay;
            fallLimit = Math.abs(FLAP_PER_FRAME * scale * TARGET_FPS) * Math.max(0f, config.maxFallSpeed);
        }

        private int requiredPipes() {
            int needed = (int) Math.ceil(width / pipeSpacing) + 2;
            return Math.max(3, Math.min(MAX_PIPES, needed));
        }

        private void spawnPipes() {
            pipes.clear();
            float direction = config.reversePipes ? -1f : 1f;
            float first = config.reversePipes
                    ? birdBaseX - pipeSpacing * 0.75f - pipeUp.getWidth()
                    : birdBaseX + pipeSpacing * 0.75f;
            int count = requiredPipes();
            for (int i = 0; i < count; i++) {
                PreviewPipe pipe = new PreviewPipe();
                pipe.randomize(random);
                pipe.x = first + i * pipeSpacing * direction;
                pipes.add(pipe);
            }
        }

        private void relayoutPipes() {
            if (pipes.isEmpty()) {
                spawnPipes();
                return;
            }
            final float direction = config.reversePipes ? -1f : 1f;
            Collections.sort(pipes, (a, b) -> Float.compare(a.x * direction, b.x * direction));
            float anchor = pipes.get(0).x;
            int count = requiredPipes();
            while (pipes.size() > count) pipes.remove(pipes.size() - 1);
            while (pipes.size() < count) {
                PreviewPipe pipe = new PreviewPipe();
                pipe.randomize(random);
                pipes.add(pipe);
            }
            for (int i = 0; i < pipes.size(); i++) pipes.get(i).x = anchor + i * pipeSpacing * direction;
        }

        void onConfigChanged() {
            if (!ready) return;
            boolean reverseChanged = config.reversePipes != lastReverse;
            boolean spacingChanged = config.pipeSpacing != lastSpacing;
            if (config.speedVariation != lastSpeedVariation) rollSpeed();
            if (config.speedRamp != lastRamp || config.gapShrink != lastShrink) restartScore();
            tune();
            if (reverseChanged) spawnPipes();
            else if (spacingChanged) relayoutPipes();
            if (config.weather != lastWeather) {
                weather.setType(config.weather);
                if (config.weather == WEATHER_STORM) strike();
            }
            if (!config.birdTrail) trailCount = 0;
            if (config.moveTier1 != lastTier1 || config.moveTier2 != lastTier2) {
                tier1Reached = false;
                tier2Reached = false;
            }
            remember();
            invalidate();
        }

        void demo(int type) {
            if (!ready) return;
            if (type == DEMO_SHAKE) {
                startShake(5f, 0.4f);
            } else if (type == DEMO_DIM) {
                dimTimer = DIM_PEEK_SEC;
            } else if (type == DEMO_HITBOX) {
                hitboxTimer = HITBOX_PEEK_SEC;
            } else if (type == DEMO_STRIKE) {
                weather.strikeAt(birdX, birdY);
                burntTimer = 1.5f;
                whiteFlash();
                startShake(15f, 0.8f);
            } else if (type == DEMO_MEGA_STRIKE) {
                weather.megaStrike(birdX, birdY);
                burntTimer = 2.5f;
                whiteFlash();
                startShake(25f, 1.5f);
            } else if (type == DEMO_CRASH_FLASH) {
                if (config.crashFlash) {
                    int index = config.crashFlashColor >= 0 && config.crashFlashColor < CRASH_FLASH_COLORS.length ? config.crashFlashColor : 0;
                    startFlash(CRASH_FLASH_COLORS[index], 255f * Math.max(0, Math.min(100, config.crashFlashStrength)) / 100f,
                            Math.max(10, config.crashFlashLength) / 1000f, config.crashFlashFade == FLASH_FADE_SHARP);
                    startShake(5f, 0.4f);
                }
            } else if (type == DEMO_MOVING_PIPES || type == DEMO_FAST_PIPES) {
                moveTimer = Math.max(moveTimer, MOVING_PEEK_SEC);
                moveSpeed = type == DEMO_FAST_PIPES ? MOVE_SPEED_TIER_2 : MOVE_SPEED_TIER_1;
            }
            invalidate();
        }

        private void restartScore() {
            score = 0;
            tier1Reached = false;
            tier2Reached = false;
        }

        private void strike() {
            weather.strikeRandom();
            startShake(7f, 0.5f);
        }

        private void whiteFlash() {
            startFlash(Color.WHITE, 255f, 0.17f, false);
        }

        private void startFlash(int color, float peak, float length, boolean sharp) {
            flashColor = color;
            flashPeak = peak;
            flash = peak;
            flashLength = Math.max(0.01f, length);
            flashElapsed = 0f;
            flashSharp = sharp;
        }

        private void startShake(float magnitude, float duration) {
            if (!config.screenShake) return;
            shakeMagnitude = magnitude * scale * Math.max(0f, config.shakeStrength);
            shakeTimer = duration;
        }

        @Override
        public void doFrame(long frameTimeNanos) {
            if (!running) return;
            choreographer.postFrameCallback(this);
            if (lastFrameNanos == 0) {
                lastFrameNanos = frameTimeNanos;
                return;
            }
            float delta = (frameTimeNanos - lastFrameNanos) / 1_000_000_000f;
            lastFrameNanos = frameTimeNanos;
            if (delta <= 0f || !ready) return;
            if (config.fixedStep) {
                stepAccumulator += Math.min(delta, 8f / 60f);
                int steps = 0;
                while (stepAccumulator >= 1f / 60f && steps < 8) {
                    update(1f / 60f);
                    stepAccumulator -= 1f / 60f;
                    steps++;
                }
                if (steps == 0) return;
            } else {
                stepAccumulator = 0f;
                update(Math.min(delta, MAX_FRAME_DELTA));
            }
            invalidate();
        }

        private void update(float dt) {
            clock += dt;
            float previousX = birdX;
            float previousY = birdY;
            float previousRotation = birdRotation;

            if (clock - lastBirdSwap > RANDOM_BIRD_SEC) {
                lastBirdSwap = clock;
                randomBird = (randomBird + 1 + random.nextInt(2)) % 3;
            }
            if (clock - lastBackgroundSwap > RANDOM_BACKGROUND_SEC) {
                lastBackgroundSwap = clock;
                randomNight = !randomNight;
            }
            if (config.speedVariation > 0f && clock - lastSpeedRoll > SPEED_REROLL_SEC) {
                rollSpeed();
                tune();
            }
            birdColorIndex = config.birdColor >= 0 && config.birdColor <= 2 ? config.birdColor : randomBird;

            backgroundX = (backgroundX - pipeSpeed * config.bgScroll * dt) % day.getWidth();
            groundX = (groundX - pipeSpeed * config.groundScroll * dt) % ground.getWidth();
            rainbowHue = (rainbowHue + 150f * dt) % 360f;
            shineOffset = (shineOffset + 200f * fx * dt) % shinePeriod;
            if (config.dayNight) {
                float cycle = Math.max(5, config.dayNightLength) / DAY_NIGHT_SPEED_UP;
                dayNightProgress = (dayNightProgress + dt / cycle) % 1f;
            }

            if (shakeTimer > 0f) shakeTimer -= dt;
            if (flash > 0f) {
                flashElapsed += dt;
                float left = Math.max(0f, 1f - flashElapsed / flashLength);
                flash = flashPeak * (flashSharp ? left * left * left * left * left : left);
            }
            if (burntTimer > 0f) burntTimer -= dt;
            if (dimTimer > 0f) dimTimer -= dt;
            if (hitboxTimer > 0f) hitboxTimer -= dt;

            updatePipes(dt);
            updateBird(dt);
            updateBirdRect();
            if (config.birdTrail) recordTrail();

            if (config.weather > 0) {
                weather.update(dt, pipeSpeed * 0.1f, birdRect);
                if (config.weather == WEATHER_STORM && random.nextFloat() < LIGHTNING_PER_SEC * dt) strike();
            }
            updateBlur(dt, previousX, previousY, previousRotation);
        }

        private boolean isGolden(PreviewPipe pipe) {
            return config.goldenPipes && pipe.goldenRoll < config.goldenChance;
        }

        private float gapTop(PreviewPipe pipe) {
            float variation = Math.max(0.001f, config.pipeVariation);
            float marginPercent = Math.max(0f, Math.min(0.49f, 0.08f / variation));
            float margin = groundTop * marginPercent;
            float available = groundTop - 2f * margin - pipeGap;
            float base = available <= 0f ? groundTop / 2f - pipeGap / 2f : margin + pipe.heightRoll * available;
            if (config.gapPlacement == GAP_PLACEMENT_CLASSIC) {
                float spread = CLASSIC_GAP_BOTTOM_SPREAD * Math.max(0f, config.pipeVariation);
                float bottom = (CLASSIC_GAP_BOTTOM_CENTER + (pipe.heightRoll - 0.5f) * spread) * scale;
                float lowest = groundTop - pipeGap;
                base = lowest <= 0f ? lowest / 2f : Math.max(0f, Math.min(lowest, bottom - pipeGap));
            }
            return base + pipe.offset;
        }

        private float gapTopAt(PreviewPipe pipe, float shift, float time) {
            if (moveRange <= 0f) return gapTop(pipe);
            boolean moving = config.movingPipes && moveTimer > time;
            float counter = moveCounter + (moving ? moveSpeed * TARGET_FPS * time : 0f);
            float phase = (pipe.x - shift) / Math.max(1f, 500f * fx) * (float) Math.PI;
            return gapTop(pipe) - pipe.offset + (float) Math.sin(counter + phase) * moveRange;
        }

        private void updatePipes(float dt) {
            boolean reverse = config.reversePipes;
            float direction = reverse ? -1f : 1f;
            float pipeWidth = pipeUp.getWidth();
            float visualWidth = pipeWidth * Math.max(0.1f, config.pipeWidth);

            if (config.movingPipes && score >= config.moveTier1 && !tier1Reached) {
                tier1Reached = true;
                moveTimer = MOVING_PIPES_SEC;
                moveSpeed = MOVE_SPEED_TIER_1;
            }
            if (config.movingPipes && score >= config.moveTier2 && !tier2Reached) {
                tier2Reached = true;
                moveTimer = MOVING_PIPES_SEC;
                moveSpeed = MOVE_SPEED_TIER_2;
            }
            if (moveTimer > 0f) moveTimer -= dt;
            if (config.movingPipes && moveTimer > 0f) {
                moveCounter += moveSpeed * TARGET_FPS * dt;
                moveRange = Math.min(maxMoveRange, moveRange + 0.15f * scale * TARGET_FPS * dt);
            } else {
                moveRange = Math.max(0f, moveRange - maxMoveRange * 3f * dt);
            }
            float waveLength = Math.max(1f, 500f * fx);

            for (int i = 0; i < pipes.size(); i++) {
                PreviewPipe pipe = pipes.get(i);
                pipe.x -= pipeSpeed * dt;
                pipe.offset = moveRange > 0f ? (float) Math.sin(moveCounter + pipe.x / waveLength * Math.PI) * moveRange : 0f;

                float share = config.scorePoint == SCORE_POINT_START ? 0f : config.scorePoint == SCORE_POINT_MIDDLE ? 0.5f : 1f;
                boolean passed = reverse ? pipe.x + pipeWidth * (1f - share) > birdX : pipe.x + pipeWidth * share < birdX;
                if (config.scorePoint == SCORE_POINT_CLASSIC) {
                    passed = reverse ? pipe.x + pipeWidth >= birdRect.right : pipe.x <= birdRect.left;
                }
                if (!pipe.scored && passed) {
                    pipe.scored = true;
                    score += Math.max(0, config.scoreMultiplier);
                    if (isGolden(pipe)) score += Math.max(0, config.goldenBonus);
                    boolean growing = config.speedRamp > 0 || config.gapShrink > 0;
                    if (score > MAX_SCORE || score < 0 || (growing && score >= RAMP_LOOP_SCORE)) restartScore();
                }

                boolean gone = reverse ? pipe.x - visualWidth > width : pipe.x + visualWidth < 0f;
                if (gone) {
                    pipe.x += pipes.size() * pipeSpacing * direction;
                    pipe.randomize(random);
                }
            }
            if (score != tunedScore) tune();
        }

        private PreviewPipe nextPipe(float shift, float pilotX, float birdHalfWidth, float pipeLeftInset, float visualWidth) {
            PreviewPipe best = null;
            float bestDistance = Float.MAX_VALUE;
            for (int i = 0; i < pipes.size(); i++) {
                PreviewPipe pipe = pipes.get(i);
                float left = pipe.x - shift - pipeLeftInset;
                float distance = config.reversePipes
                        ? (pilotX + birdHalfWidth) - left
                        : (left + visualWidth) - (pilotX - birdHalfWidth);
                if (distance >= 0f && distance < bestDistance) {
                    bestDistance = distance;
                    best = pipe;
                }
            }
            return best;
        }

        private boolean wantsLift(float y, float velocity, float top, float halfHeight) {
            boolean flipped = config.upsideDown;
            if (config.jetpack) {
                float side = flipped ? -1f : 1f;
                float aim = (top + pipeGap / 2f) * side;
                float position = y * side;
                float speed = velocity * side;
                float pull = Math.abs(gravity);
                float push = Math.abs(jetpackThrust) - pull;
                float rest;
                if (speed < 0f) rest = pull > 0f ? position - speed * speed / (2f * pull) : -Float.MAX_VALUE;
                else rest = push > 0f ? position + speed * speed / (2f * push) : Float.MAX_VALUE;
                return rest > aim;
            }
            float margin = pipeGap * 0.14f;
            return flipped ? y < top + halfHeight + margin : y > top + pipeGap - halfHeight - margin;
        }

        private float flightCost(boolean liftNow, int waitSteps, float halfWidth, float halfHeight) {
            boolean flipped = config.upsideDown;
            float pipeWidth = pipeUp.getWidth();
            float visualWidth = pipeWidth * Math.max(0.1f, config.pipeWidth);
            float inset = (visualWidth - pipeWidth) / 2f;
            float y = birdY;
            float velocity = birdVelocity;
            float sinceFlap = (float) (clock - lastFlap);
            float drift = Math.min(width, SOURCE_WIDTH * scale) * 0.15f * config.drunkStrength;
            float cost = 0f;

            for (int step = 0; step < PILOT_STEPS; step++) {
                float time = PILOT_STEP_SEC * step;
                float shift = pipeSpeed * time;
                float pilotX = config.drunk
                        ? birdBaseX + (float) Math.sin(drunkPhase + 2f * (time + PILOT_STEP_SEC)) * drift
                        : birdX;
                PreviewPipe next = nextPipe(shift, pilotX, halfWidth, inset, visualWidth);
                float top = next != null ? gapTopAt(next, shift, time) : groundTop / 2f - pipeGap / 2f;
                float bottom = top + pipeGap;

                boolean lift;
                if (step == 0) lift = liftNow;
                else if (step <= waitSteps) lift = config.jetpack && liftNow;
                else lift = wantsLift(y, velocity, top, halfHeight);
                if (config.jetpack) {
                    if (lift) velocity -= jetpackThrust * PILOT_STEP_SEC;
                } else if (lift && sinceFlap > FLAP_COOLDOWN_SEC) {
                    velocity = flapVelocity;
                    sinceFlap = 0f;
                }
                float nextVelocity = velocity + gravity * PILOT_STEP_SEC;
                float cappedVelocity = capFall(nextVelocity);
                if (pixelSteps()) y = moveBird(y, cappedVelocity * PILOT_STEP_SEC);
                else if (cappedVelocity != nextVelocity) y += cappedVelocity * PILOT_STEP_SEC;
                else y += velocity * PILOT_STEP_SEC + gravity * PILOT_STEP_SEC * (PILOT_STEP_SEC + pilotFrameDelta) / 2f;
                velocity = cappedVelocity;
                sinceFlap += PILOT_STEP_SEC;

                boolean crashed = flipped ? y - halfHeight < 0f : y + halfHeight > groundTop;
                if (flipped && y + halfHeight > groundTop) {
                    y = groundTop - halfHeight;
                    if (velocity > 0f) velocity = 0f;
                }
                if (!flipped && y < ceilingY(halfHeight)) {
                    y = ceilingY(halfHeight);
                    if (velocity < 0f) velocity = 0f;
                }
                if (next != null && !crashed) {
                    float left = next.x - shift - inset;
                    boolean inside = pilotX + halfWidth > left && pilotX - halfWidth < left + visualWidth;
                    crashed = inside && (y - halfHeight < top || y + halfHeight > bottom);
                }
                if (crashed) return cost + PILOT_CRASH_COST - step;
                cost += Math.abs(y - (top + bottom) / 2f) / pipeGap * 0.01f;
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

        private void updateBird(float dt) {
            boolean flipped = config.upsideDown;
            float size = Math.max(0.1f, config.birdSize);
            Bitmap sprite = birds[birdColorIndex][1];
            float halfHeight = sprite.getHeight() * size / 2f;
            float hitbox = Math.max(0f, config.birdHitbox);
            boolean square = config.hitboxShape == HITBOX_SHAPE_SQUARE;
            float boxWidth = square ? SQUARE_HITBOX_SIZE * scale : sprite.getWidth() * 0.8f;
            float boxHeight = square ? SQUARE_HITBOX_SIZE * scale : sprite.getHeight() * 0.8f;
            float pilotHalfWidth = boxWidth * hitbox * size / 2f + sprite.getWidth() * size * 0.1f;
            float pilotHalfHeight = Math.max(halfHeight, boxHeight * hitbox * size / 2f + sprite.getHeight() * size * 0.1f);
            pilotFrameDelta = dt;

            if (config.drunk) {
                drunkPhase += dt * 2f;
                float drift = Math.min(width, SOURCE_WIDTH * scale) * 0.15f * config.drunkStrength;
                birdX = birdBaseX + (float) Math.sin(drunkPhase) * drift;
            } else {
                birdX = birdBaseX;
            }

            thrusting = false;
            boolean canAct = config.jetpack || clock - lastFlap > FLAP_COOLDOWN_SEC;
            if (canAct && bestFlightCost(true, pilotHalfWidth, pilotHalfHeight) < bestFlightCost(false, pilotHalfWidth, pilotHalfHeight)) {
                if (config.jetpack) {
                    thrusting = true;
                    birdVelocity -= jetpackThrust * dt;
                    kickTilt();
                } else {
                    flap();
                }
            }

            birdVelocity = capFall(birdVelocity + gravity * dt);
            birdY = moveBird(birdY, birdVelocity * dt);

            if (flipped) {
                if (birdY - halfHeight < 0f) {
                    birdY = halfHeight;
                    bounce();
                }
                if (birdY + halfHeight > groundTop) {
                    birdY = groundTop - halfHeight;
                    if (birdVelocity > 0f) birdVelocity = 0f;
                }
            } else {
                float ceiling = ceilingY(halfHeight);
                if (birdY < ceiling) {
                    birdY = ceiling;
                    if (birdVelocity < 0f) birdVelocity = 0f;
                }
                if (birdY + halfHeight > groundTop) {
                    birdY = groundTop - halfHeight;
                    bounce();
                }
            }

            if (!config.birdTilt) {
                birdRotation = 0f;
            } else {
                float up = Math.max(0, Math.min(90, config.maxTiltUp));
                float down = Math.max(0, Math.min(90, config.maxTiltDown));
                float turn = Math.max(0.05f, config.tiltSpeed);
                if (config.tiltStyle == TILT_STYLE_CLASSIC) {
                    float side = flipped ? -1f : 1f;
                    float angle = birdRotation * side + tiltVelocity * dt;
                    tiltVelocity += CLASSIC_TILT_PULL_PER_SEC2 * turn * turn * dt;
                    birdRotation = Math.max(-up, Math.min(down, angle)) * side;
                } else if (flipped) {
                    if (birdVelocity > 0f) birdRotation = Math.min(up, birdRotation + ROTATION_UP_PER_SEC * turn * dt);
                    else if (birdVelocity < -rotationDelay) birdRotation = Math.max(-down, birdRotation - ROTATION_DOWN_PER_SEC * turn * dt);
                } else {
                    if (birdVelocity < 0f) birdRotation = Math.max(-up, birdRotation - ROTATION_UP_PER_SEC * turn * dt);
                    else if (birdVelocity > rotationDelay) birdRotation = Math.min(down, birdRotation + ROTATION_DOWN_PER_SEC * turn * dt);
                }
            }

            boolean recentlyFlapped = clock - lastFlap < FLAP_ANIMATION_SEC;
            boolean flapping = (config.wingAnimation && (config.keepFlapping || recentlyFlapped)) || (config.jetpack && thrusting);
            float wingSpeed = Math.max(0.1f, Math.min(10f, config.wingSpeed));
            birdFrame = flapping ? (int) (clock / (WING_FRAME_SEC / wingSpeed)) % 3 : 1;
        }

        private boolean pixelSteps() {
            return config.pixelSteps && config.fixedStep;
        }

        private float moveBird(float y, float travel) {
            if (!pixelSteps()) return y + travel;
            float source = (y + travel) / scale;
            float snapped = config.upsideDown
                    ? (float) Math.ceil(source - PIXEL_STEP_EPSILON)
                    : (float) Math.floor(source + PIXEL_STEP_EPSILON);
            return snapped * scale;
        }

        private void kickTilt() {
            tiltVelocity = CLASSIC_TILT_KICK_PER_SEC * Math.max(0.05f, config.tiltSpeed);
        }

        private float capFall(float velocity) {
            if (config.maxFallSpeed <= 0f) return velocity;
            return config.upsideDown ? Math.max(-fallLimit, velocity) : Math.min(fallLimit, velocity);
        }

        private float ceilingY(float halfHeight) {
            return config.ceiling ? -halfHeight * 1.8f : -height * 0.5f;
        }

        private void flap() {
            birdVelocity = flapVelocity;
            lastFlap = clock;
            kickTilt();
        }

        private void bounce() {
            if (config.jetpack) birdVelocity = 0f;
            else flap();
        }

        private void updateBirdRect() {
            Bitmap sprite = birds[birdColorIndex][birdFrame];
            float size = Math.max(0.1f, config.birdSize);
            float hitbox = Math.max(0f, config.birdHitbox);
            float halfWidth = sprite.getWidth() * 0.8f * hitbox * size / 2f;
            float halfHeight = sprite.getHeight() * 0.8f * hitbox * size / 2f;
            if (config.hitboxShape == HITBOX_SHAPE_SQUARE) {
                halfWidth = SQUARE_HITBOX_SIZE * scale * hitbox * size / 2f;
                halfHeight = halfWidth;
            }
            birdRect.set((int) (birdX - halfWidth), (int) (birdY - halfHeight), (int) (birdX + halfWidth), (int) (birdY + halfHeight));
        }

        private void recordTrail() {
            int index;
            if (trailCount < TRAIL_LENGTH) {
                index = (trailStart + trailCount) % TRAIL_LENGTH;
                trailCount++;
            } else {
                index = trailStart;
                trailStart = (trailStart + 1) % TRAIL_LENGTH;
            }
            trailX[index] = birdX;
            trailY[index] = birdY;
            trailRotation[index] = birdRotation;
            trailColor[index] = birdColorIndex;
            trailFrame[index] = birdFrame;
            trailHue[index] = config.rainbowBird ? rainbowHue : (config.birdHue != 0 ? config.birdHue : -1f);
        }

        private void updateBlur(float dt, float previousX, float previousY, float previousRotation) {
            if (!config.motionBlur) {
                backgroundBlur = 0f;
                groundBlur = 0f;
                pipeBlur = 0f;
                birdBlurX = 0f;
                birdBlurY = 0f;
                birdBlurRotation = 0f;
                return;
            }
            float strength = Math.max(1f, Math.min(10f, config.blurStrength));
            float shutter = BLUR_SHUTTER_SEC * strength;
            float limit = BLUR_MAX_LENGTH * scale;
            float travel = Math.abs(pipeSpeed) * shutter;
            backgroundBlur = Math.min(limit, travel * Math.abs(config.bgScroll));
            groundBlur = Math.min(limit, travel * Math.abs(config.groundScroll));
            pipeBlur = Math.min(limit, travel);

            float exposure = shutter / dt;
            birdBlurX = (birdX - previousX) * exposure;
            birdBlurY = (birdY - previousY) * exposure;
            birdBlurRotation = (birdRotation - previousRotation) * exposure;
            float length = (float) Math.hypot(birdBlurX, birdBlurY);
            if (length > limit) {
                float shrink = limit / length;
                birdBlurX *= shrink;
                birdBlurY *= shrink;
                birdBlurRotation *= shrink;
            }
            birdBlurRotation = Math.max(-BLUR_MAX_ROTATION, Math.min(BLUR_MAX_ROTATION, birdBlurRotation));
        }

        private int blurSamples(float length) {
            if (length < 1f) return 1;
            int samples = (int) Math.ceil(length / Math.max(1f, scale)) + 1;
            return Math.max(2, Math.min(MAX_BLUR_SAMPLES, samples));
        }

        private static float samplePosition(int index, int samples) {
            return (index + 0.5f) / samples - 0.5f;
        }

        private static int sampleAlpha(int total, int index, int samples) {
            return Math.round(total * (index + 1) / (float) samples) - Math.round(total * index / (float) samples);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            if (!ready) return;

            boolean shaking = shakeTimer > 0f;
            if (shaking) canvas.drawColor(Color.BLACK);

            canvas.save();
            if (shaking) {
                float magnitude = shakeMagnitude * (shakeTimer / 0.5f);
                canvas.translate((random.nextFloat() - 0.5f) * 2f * magnitude, (random.nextFloat() - 0.5f) * 2f * magnitude);
            }
            if (config.upsideDown) canvas.scale(1f, -1f, width / 2f, height / 2f);

            drawBackground(canvas);
            if (config.weather > 0) weather.drawBack(canvas);
            drawPipes(canvas);
            drawGround(canvas);
            drawTrail(canvas);
            drawBird(canvas);
            if (hitboxTimer > 0f || config.showHitboxes) {
                hitboxPaint.setAlpha(config.showHitboxes ? 255 : Math.round(255f * Math.min(1f, hitboxTimer / 0.6f)));
                canvas.drawRect(birdRect.left, birdRect.top, birdRect.right, birdRect.bottom, hitboxPaint);
            }
            if (config.weather > 0) {
                weather.drawFront(canvas);
                weather.drawOverlay(canvas);
            }
            canvas.restore();

            drawHud(canvas);
        }

        private void drawBackground(Canvas canvas) {
            int samples = blurSamples(backgroundBlur);
            if (!config.dayNight) {
                boolean isNight = config.background == 1 || (config.background != 0 && randomNight);
                drawExposedBackground(canvas, isNight ? night : day, samples);
                return;
            }
            float amount = (float) (1 - Math.cos(dayNightProgress * 2 * Math.PI)) / 2f;
            int nightAlpha = Math.max(0, Math.min(255, (int) (amount * 255)));
            if (nightAlpha < 255) drawExposedBackground(canvas, day, samples);
            if (nightAlpha == 255) {
                drawExposedBackground(canvas, night, samples);
            } else if (nightAlpha > 0 && samples > 1) {
                canvas.saveLayerAlpha(null, nightAlpha);
                drawExposedBackground(canvas, night, samples);
                canvas.restore();
            } else if (nightAlpha > 0) {
                nightPaint.setAlpha(nightAlpha);
                drawBackgroundTiles(canvas, night, nightPaint, 0f);
            }
        }

        private void drawExposedBackground(Canvas canvas, Bitmap bitmap, int samples) {
            for (int sample = 0; sample < samples; sample++) {
                pixelPaint.setAlpha(255 / (sample + 1));
                drawBackgroundTiles(canvas, bitmap, pixelPaint, samplePosition(sample, samples) * backgroundBlur);
            }
            pixelPaint.setAlpha(255);
        }

        private void drawBackgroundTiles(Canvas canvas, Bitmap bitmap, Paint paint, float shift) {
            float tile = bitmap.getWidth();
            float start = (backgroundX + shift) % tile;
            if (start > 0f) start -= tile;
            for (float x = start - tile; x < width + tile; x += tile) {
                canvas.drawBitmap(bitmap, x, 0f, paint);
            }
        }

        private void drawPipes(Canvas canvas) {
            int samples = blurSamples(pipeBlur);
            boolean blurred = samples > 1;
            float reach = blurred ? pipeBlur / 2f : 0f;
            float pipeWidth = pipeUp.getWidth();
            float pipeHeight = pipeUp.getHeight();
            float visualWidth = pipeWidth * Math.max(0.1f, config.pipeWidth);
            boolean shineReady = false;

            if (blurred) canvas.saveLayer(0f, -1f, width, groundTop, null);

            for (int i = 0; i < pipes.size(); i++) {
                PreviewPipe pipe = pipes.get(i);
                float left = pipe.x - (visualWidth - pipeWidth) / 2f;
                float right = left + visualWidth;
                if (right + reach < 0f || left - reach > width) continue;

                float top = gapTop(pipe);
                float topY = top - pipeHeight;
                float bottomY = top + pipeGap;
                int color = config.randomPipeColors ? pipe.randomColor : config.pipeColor;
                pipePaint.setColorFilter(color > 0 && color < PIPE_COLOR_COUNT ? pipeFilters[color] : null);

                if (blurred) pipePaint.setXfermode(additive);
                for (int sample = 0; sample < samples; sample++) {
                    float shift = samplePosition(sample, samples) * pipeBlur;
                    if (blurred) pipePaint.setAlpha(sampleAlpha(255, sample, samples));
                    drawPipePair(canvas, left + shift, right + shift, topY, bottomY, pipeHeight);
                }
                if (blurred) {
                    pipePaint.setXfermode(null);
                    pipePaint.setAlpha(255);
                }

                if (isGolden(pipe)) {
                    if (!shineReady) {
                        shineMatrix.setTranslate(shineOffset, 0f);
                        shineShader.setLocalMatrix(shineMatrix);
                        shineReady = true;
                    }
                    destination.set(left - reach, topY, right + reach, topY + pipeHeight);
                    canvas.drawRect(destination, goldenPaint);
                    destination.set(left - reach, bottomY, right + reach, bottomY + pipeHeight);
                    canvas.drawRect(destination, goldenPaint);
                }
            }

            if (blurred) canvas.restore();
        }

        private void drawPipePair(Canvas canvas, float left, float right, float topY, float bottomY, float pipeHeight) {
            int bitmapWidth = pipeUp.getWidth();
            int bitmapHeight = pipeUp.getHeight();

            destination.set(left, topY, right, topY + pipeHeight);
            canvas.drawBitmap(pipeDown, null, destination, pipePaint);
            if (topY > -1f) {
                edgeSource.set(0, 0, bitmapWidth, 1);
                destination.set(left, -1f, right, topY);
                canvas.drawBitmap(pipeDown, edgeSource, destination, pipePaint);
            }

            destination.set(left, bottomY, right, bottomY + pipeHeight);
            canvas.drawBitmap(pipeUp, null, destination, pipePaint);
            if (bottomY + pipeHeight < groundTop) {
                edgeSource.set(0, bitmapHeight - 1, bitmapWidth, bitmapHeight);
                destination.set(left, bottomY + pipeHeight, right, groundTop + 1f);
                canvas.drawBitmap(pipeUp, edgeSource, destination, pipePaint);
            }
        }

        private void drawGround(Canvas canvas) {
            float tile = ground.getWidth();
            int samples = blurSamples(groundBlur);
            for (int sample = 0; sample < samples; sample++) {
                float start = (groundX + samplePosition(sample, samples) * groundBlur) % tile;
                if (start > 0f) start -= tile;
                pixelPaint.setAlpha(255 / (sample + 1));
                for (float x = start - tile; x < width + tile; x += tile) {
                    destination.set(x, groundTop, x + tile, height);
                    canvas.drawBitmap(ground, null, destination, pixelPaint);
                }
            }
            pixelPaint.setAlpha(255);
        }

        private ColorFilter hueFilter(float hue) {
            int index = (int) (hue / 360f * HUE_STEPS) % HUE_STEPS;
            if (index < 0) index += HUE_STEPS;
            ColorFilter filter = hueFilters[index];
            if (filter == null) {
                colorMatrix.setRotate(2, index * (360f / HUE_STEPS));
                filter = new ColorMatrixColorFilter(colorMatrix);
                hueFilters[index] = filter;
            }
            return filter;
        }

        private void placeBird(Bitmap sprite, float x, float y, float rotation) {
            float size = Math.max(0.1f, config.birdSize);
            birdMatrix.reset();
            birdMatrix.postTranslate(-sprite.getWidth() / 2f, -sprite.getHeight() / 2f);
            birdMatrix.postRotate(rotation);
            birdMatrix.postScale(size, size, 0f, 0f);
            birdMatrix.postTranslate(x, y);
        }

        private void drawTrail(Canvas canvas) {
            if (!config.birdTrail) return;
            for (int i = 0; i < trailCount; i++) {
                int index = (trailStart + i) % TRAIL_LENGTH;
                trailPaint.setColorFilter(trailHue[index] >= 0f ? hueFilter(trailHue[index]) : null);
                trailPaint.setAlpha((int) ((float) i / trailCount * 100f));
                Bitmap sprite = birds[trailColor[index]][trailFrame[index]];
                placeBird(sprite, trailX[index], trailY[index], trailRotation[index]);
                canvas.drawBitmap(sprite, birdMatrix, trailPaint);
            }
        }

        private void drawBird(Canvas canvas) {
            ColorFilter filter = null;
            if (burntTimer > 0f) filter = burntFilter;
            else if (config.rainbowBird) filter = hueFilter(rainbowHue);
            else if (config.birdHue != 0) filter = hueFilter(config.birdHue);
            birdPaint.setColorFilter(filter);

            int alpha = config.ghostBird ? 128 : 255;
            Bitmap sprite = birds[birdColorIndex][birdFrame];
            float span = (sprite.getWidth() + sprite.getHeight()) * Math.max(0.1f, config.birdSize) / 2f;
            float turnTravel = (float) Math.toRadians(Math.abs(birdBlurRotation)) * span / 2f;
            int samples = blurSamples(Math.max((float) Math.hypot(birdBlurX, birdBlurY), turnTravel));

            if (samples <= 1) {
                birdPaint.setAlpha(alpha);
                placeBird(sprite, birdX, birdY, birdRotation);
                canvas.drawBitmap(sprite, birdMatrix, birdPaint);
                return;
            }

            float reachX = span + Math.abs(birdBlurX) / 2f + 2f;
            float reachY = span + Math.abs(birdBlurY) / 2f + 2f;
            canvas.saveLayer(birdX - reachX, birdY - reachY, birdX + reachX, birdY + reachY, null);
            birdPaint.setXfermode(additive);
            for (int sample = 0; sample < samples; sample++) {
                float position = samplePosition(sample, samples);
                birdPaint.setAlpha(sampleAlpha(alpha, sample, samples));
                placeBird(sprite, birdX + birdBlurX * position, birdY + birdBlurY * position, birdRotation + birdBlurRotation * position);
                canvas.drawBitmap(sprite, birdMatrix, birdPaint);
            }
            birdPaint.setXfermode(null);
            birdPaint.setAlpha(alpha);
            canvas.restore();
        }

        private void drawHud(Canvas canvas) {
            if (digitsLoaded) drawScore(canvas);

            float inset = Math.min(width, height) * 0.04f;
            float cornerRight = width - inset;
            if (gear != null && !config.hideSettingsIcon) {
                canvas.drawBitmap(gear, cornerRight - gear.getWidth(), inset, pixelPaint);
                cornerRight -= gear.getWidth() + inset;
            }
            if (config.pauseButton) {
                float size = Math.max(4f, 22f * scale);
                float left = cornerRight - size;
                float bar = size * 0.18f;
                float pad = size * 0.26f;
                iconPaint.setColor(Color.BLACK);
                iconPaint.setAlpha(90);
                canvas.drawRect(left, inset, cornerRight, inset + size, iconPaint);
                iconPaint.setColor(Color.WHITE);
                iconPaint.setAlpha(235);
                canvas.drawRect(left + pad, inset + pad, left + pad + bar, inset + size - pad, iconPaint);
                canvas.drawRect(cornerRight - pad - bar, inset + pad, cornerRight - pad, inset + size - pad, iconPaint);
            }
            if (config.extraLives > 0) drawLives(canvas);

            if (dimTimer > 0f) {
                float envelope = Math.min(1f, (DIM_PEEK_SEC - dimTimer) / 0.15f) * Math.min(1f, dimTimer / 0.4f);
                envelope = Math.max(0f, Math.min(1f, envelope));
                int opacity = Math.max(0, Math.min(100, config.gameOverOpacity));
                overlayPaint.setColor(Color.BLACK);
                overlayPaint.setAlpha(Math.round(opacity / 100f * 255f * envelope));
                canvas.drawRect(0f, 0f, width, height, overlayPaint);

                if (gameOver != null) {
                    float fit = Math.min(1f, width * 0.8f / gameOver.getWidth());
                    float bannerWidth = gameOver.getWidth() * fit;
                    float bannerHeight = gameOver.getHeight() * fit;
                    float left = (width - bannerWidth) / 2f;
                    float top = groundTop * 0.42f - bannerHeight / 2f;
                    destination.set(left, top, left + bannerWidth, top + bannerHeight);
                    pixelPaint.setAlpha(Math.round(255f * envelope));
                    canvas.drawBitmap(gameOver, null, destination, pixelPaint);
                    pixelPaint.setAlpha(255);
                }
            }

            if (flash > 0f) {
                overlayPaint.setColor(flashColor);
                overlayPaint.setAlpha(Math.max(0, Math.min(255, (int) flash)));
                canvas.drawRect(0f, 0f, width, height, overlayPaint);
            }
        }

        private void drawLives(Canvas canvas) {
            Bitmap icon = birds[birdColorIndex][1];
            int lives = Math.min(9, config.extraLives);
            float iconWidth = icon.getWidth() * 0.6f;
            float iconHeight = icon.getHeight() * 0.6f;
            float gap = iconWidth * 0.25f;
            float digitHeight = digitsLoaded ? digits[0].getHeight() : 36f * scale;
            float top = height * 0.15f + digitHeight / 2f + 6f * scale;
            float x = (width - (lives * iconWidth + (lives - 1) * gap)) / 2f;
            for (int i = 0; i < lives; i++) {
                destination.set(x, top, x + iconWidth, top + iconHeight);
                canvas.drawBitmap(icon, null, destination, pixelPaint);
                x += iconWidth + gap;
            }
        }

        private void drawScore(Canvas canvas) {
            int remaining = Math.max(0, score);
            int count = 0;
            do {
                digitBuffer[count++] = remaining % 10;
                remaining /= 10;
            } while (remaining > 0 && count < digitBuffer.length);

            float total = 0f;
            for (int i = 0; i < count; i++) total += digits[digitBuffer[i]].getWidth();
            float x = width / 2f - total / 2f;
            float centerY = height * 0.15f;
            for (int i = count - 1; i >= 0; i--) {
                Bitmap digit = digits[digitBuffer[i]];
                canvas.drawBitmap(digit, x, centerY - digit.getHeight() / 2f, pixelPaint);
                x += digit.getWidth();
            }
        }

        private static final class PreviewPipe {
            float x;
            float heightRoll;
            float goldenRoll;
            float offset;
            int randomColor;
            boolean scored;

            void randomize(Random random) {
                heightRoll = random.nextFloat();
                goldenRoll = random.nextFloat() * 100f;
                randomColor = random.nextInt(PIPE_COLOR_COUNT);
                offset = 0f;
                scored = false;
            }
        }

        private static final class Bolt {
            final Path path = new Path();
            float life = 0.25f;
        }

        private static final class Drop {
            float x;
            float y;
            float depth;
            float speed;
            float size;
            float length;
            float phase;
            float bounceX;
            float bounceY;
        }

        private static final class Weather {
            private static final int LAYER_SIZE = 80;
            private static final int BANDS = 4;
            private static final int BAND_SIZE = LAYER_SIZE / BANDS;
            private static final float BOLT_LIFE_SEC = 0.25f;

            private final Random random;
            private final Drop[] front = new Drop[LAYER_SIZE];
            private final Drop[] back = new Drop[LAYER_SIZE];
            private final float[] lines = new float[BAND_SIZE * 4];
            private final List<Bolt> bolts = new ArrayList<>();
            private final Paint rainPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            private final Paint snowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            private final Paint boltPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            private final Paint glowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            private final Paint stormPaint = new Paint();
            private final Paint frostPaint = new Paint();
            private final Paint flashPaint = new Paint();
            private int type;
            private int width;
            private int height;
            private float fx = 1f;
            private float flash;

            Weather(Random random) {
                this.random = random;
                float bandWidth = 0.5f / BANDS;
                for (int i = 0; i < LAYER_SIZE; i++) {
                    int band = i / BAND_SIZE;
                    front[i] = new Drop();
                    front[i].depth = 1.0f + (band + 0.5f) * bandWidth;
                    back[i] = new Drop();
                    back[i].depth = 0.5f + (band + 0.5f) * bandWidth;
                }
                rainPaint.setColor(Color.WHITE);
                snowPaint.setColor(Color.WHITE);
                boltPaint.setColor(Color.WHITE);
                boltPaint.setStyle(Paint.Style.STROKE);
                boltPaint.setStrokeCap(Paint.Cap.ROUND);
                boltPaint.setStrokeJoin(Paint.Join.ROUND);
                glowPaint.setColor(Color.argb(255, 200, 230, 255));
                glowPaint.setStyle(Paint.Style.STROKE);
                glowPaint.setStrokeCap(Paint.Cap.ROUND);
                glowPaint.setStrokeJoin(Paint.Join.ROUND);
                flashPaint.setColor(Color.WHITE);
                flashPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.ADD));
            }

            void resize(int width, int height, float fx) {
                this.width = width;
                this.height = height;
                this.fx = fx;
                boltPaint.setStrokeWidth(Math.max(1.5f, 4f * REFERENCE_SCALE * fx));
                glowPaint.setStrokeWidth(Math.max(4f, 16f * REFERENCE_SCALE * fx));
                bolts.clear();

                float radius = Math.max(1f, (float) Math.hypot(width, height) / 2f * 1.09f);
                frostPaint.setShader(new RadialGradient(width / 2f, height / 2f, radius,
                        new int[]{Color.TRANSPARENT, Color.argb(200, 230, 230, 250)},
                        new float[]{0.3f, 1f}, Shader.TileMode.CLAMP));
                stormPaint.setShader(new RadialGradient(width / 2f, height / 2f, radius,
                        new int[]{Color.TRANSPARENT, Color.argb(200, 0, 0, 20)},
                        new float[]{0.3f, 1f}, Shader.TileMode.CLAMP));
                scatter();
            }

            void setType(int type) {
                this.type = type;
                scatter();
            }

            private void scatter() {
                for (int i = 0; i < LAYER_SIZE; i++) {
                    respawn(front[i]);
                    front[i].y = random.nextFloat() * height;
                    respawn(back[i]);
                    back[i].y = random.nextFloat() * height;
                }
            }

            private void respawn(Drop drop) {
                drop.x = random.nextFloat() * width;
                drop.y = (-50f - random.nextFloat() * 50f) * fx;
                drop.bounceX = 0f;
                drop.bounceY = 0f;
                drop.phase = random.nextFloat() * 100f;
                if (type == WEATHER_SNOW) {
                    drop.size = (2f + random.nextFloat() * 4f) * drop.depth * fx;
                    drop.speed = (30f + random.nextFloat() * 40f) * drop.depth * fx;
                    drop.length = 0f;
                } else {
                    drop.size = (2f + random.nextFloat()) * drop.depth * fx;
                    drop.length = (30f + random.nextFloat() * 40f) * drop.depth * fx;
                    drop.speed = (700f + random.nextFloat() * 300f) * drop.depth * fx;
                }
            }

            private Bolt newBolt(float startX, float startY) {
                Bolt bolt = new Bolt();
                bolt.life = BOLT_LIFE_SEC;
                bolt.path.moveTo(startX, startY);
                bolts.add(bolt);
                flash = 1f;
                return bolt;
            }

            void strikeRandom() {
                float x = random.nextFloat() * width;
                float y = -50f * fx;
                Bolt bolt = newBolt(x, y);
                float limit = height * 0.8f;
                int guard = 0;
                while (y < limit && guard++ < 200) {
                    y += (30f + random.nextFloat() * 50f) * fx;
                    x += (random.nextFloat() - 0.5f) * 80f * fx;
                    bolt.path.lineTo(x, y);
                    if (random.nextBoolean()) {
                        bolt.path.lineTo(x + (random.nextFloat() - 0.5f) * 100f * fx, y + (50f + random.nextFloat() * 50f) * fx);
                        bolt.path.moveTo(x, y);
                    }
                }
            }

            private void strikeFrom(float startX, float startY, float endX, float endY) {
                Bolt bolt = newBolt(startX, startY);
                int segments = 8;
                float x = startX;
                float y = startY;
                for (int i = 0; i < segments; i++) {
                    x += (endX - startX) / segments;
                    y += (endY - startY) / segments;
                    if (i == segments - 1) {
                        x = endX;
                        y = endY;
                    } else {
                        x += (random.nextFloat() - 0.5f) * 100f * fx;
                        y += (random.nextFloat() - 0.5f) * 50f * fx;
                    }
                    bolt.path.lineTo(x, y);
                }
            }

            void strikeAt(float targetX, float targetY) {
                strikeFrom(targetX, -50f * fx, targetX, targetY);
            }

            void megaStrike(float targetX, float targetY) {
                float away = 50f * fx;
                strikeFrom(targetX, -away, targetX, targetY);
                strikeFrom(0f, -away, targetX, targetY);
                strikeFrom(width, -away, targetX, targetY);
                strikeFrom(-away, height * 0.2f, targetX, targetY);
                strikeFrom(width + away, height * 0.2f, targetX, targetY);
            }

            void update(float dt, float wind, Rect bird) {
                float decay = (float) Math.pow(0.9, dt * TARGET_FPS);
                for (int i = 0; i < LAYER_SIZE; i++) {
                    move(front[i], dt, wind, bird, decay);
                    move(back[i], dt, wind * 0.5f, bird, decay);
                }
                for (int i = bolts.size() - 1; i >= 0; i--) {
                    Bolt bolt = bolts.get(i);
                    bolt.life -= dt;
                    if (bolt.life <= 0f) bolts.remove(i);
                }
                if (flash > 0f) flash = Math.max(0f, flash - 4f * dt);
            }

            private void move(Drop drop, float dt, float wind, Rect bird, float decay) {
                drop.y += drop.speed * dt;
                drop.x += drop.bounceX * dt;
                drop.y += drop.bounceY * dt;
                drop.bounceX *= decay;
                drop.bounceY *= decay;

                if (type == WEATHER_SNOW) {
                    float sway = (float) Math.sin(drop.y / fx * 0.01f + drop.phase) * 50f * drop.depth * fx;
                    drop.x += (wind * 0.1f * drop.depth + sway) * dt;
                    if (bird.contains((int) drop.x, (int) drop.y)) {
                        float dx = drop.x - bird.centerX();
                        float dy = drop.y - bird.centerY();
                        float distance = Math.max(1f, (float) Math.sqrt(dx * dx + dy * dy));
                        drop.bounceX = dx / distance * 200f * fx;
                        drop.bounceY = -Math.abs(drop.speed) * 0.8f;
                        drop.x += dx / distance * 5f * fx;
                        drop.y += dy / distance * 5f * fx;
                    }
                } else {
                    drop.x -= wind * drop.depth * dt;
                }

                float side = 100f * fx;
                if (drop.y > height + drop.length || drop.x < -side || drop.x > width + side) respawn(drop);
            }

            void drawBack(Canvas canvas) {
                drawLayer(canvas, back);
            }

            void drawFront(Canvas canvas) {
                drawLayer(canvas, front);
            }

            private void drawLayer(Canvas canvas, Drop[] drops) {
                if (type == WEATHER_SNOW) {
                    for (int i = 0; i < LAYER_SIZE; i++) {
                        Drop drop = drops[i];
                        snowPaint.setAlpha(Math.min(255, (int) (180 * drop.depth)));
                        canvas.drawCircle(drop.x, drop.y, drop.size, snowPaint);
                    }
                    return;
                }
                for (int band = 0; band < BANDS; band++) {
                    int first = band * BAND_SIZE;
                    int count = 0;
                    for (int i = first; i < first + BAND_SIZE; i++) {
                        Drop drop = drops[i];
                        lines[count++] = drop.x;
                        lines[count++] = drop.y;
                        lines[count++] = drop.x - 20f * drop.depth * fx;
                        lines[count++] = drop.y + drop.length;
                    }
                    float depth = drops[first].depth;
                    rainPaint.setAlpha(Math.min(255, (int) (160 * depth)));
                    rainPaint.setStrokeWidth(Math.max(1f, 2.5f * depth * fx));
                    canvas.drawLines(lines, 0, count, rainPaint);
                }
            }

            void drawOverlay(Canvas canvas) {
                float overscan = Math.max(width, height) * 0.25f;
                if (type == WEATHER_STORM) {
                    canvas.drawRect(-overscan, -overscan, width + overscan, height + overscan, stormPaint);
                } else if (type == WEATHER_SNOW) {
                    canvas.drawRect(-overscan, -overscan, width + overscan, height + overscan, frostPaint);
                }
                for (int i = 0; i < bolts.size(); i++) {
                    Bolt bolt = bolts.get(i);
                    int alpha = Math.max(0, Math.min(255, (int) (bolt.life / BOLT_LIFE_SEC * 255f)));
                    glowPaint.setAlpha((int) (alpha * 0.4f));
                    boltPaint.setAlpha(alpha);
                    canvas.drawPath(bolt.path, glowPaint);
                    canvas.drawPath(bolt.path, boltPaint);
                }
                if (flash > 0f) {
                    flashPaint.setAlpha((int) (flash * 100f));
                    canvas.drawRect(-overscan, -overscan, width + overscan, height + overscan, flashPaint);
                }
            }
        }
    }
}
