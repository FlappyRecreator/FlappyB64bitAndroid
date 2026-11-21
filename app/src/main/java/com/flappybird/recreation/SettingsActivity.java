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
import android.os.SystemClock;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.res.Configuration;
import android.os.PowerManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.transition.AutoTransition;
import android.transition.TransitionManager;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Choreographer;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.view.animation.OvershootInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
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
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.TimeZone;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
import java.util.concurrent.RejectedExecutionException;
import java.security.SecureRandom;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SettingsActivity extends AppCompatActivity {

    public static final String PREFS_NAME = "FlappyBirdPrefs";
    public static final String PREF_BIRD_COLOR = "pref_bird_color";
    public static final String PREF_BACKGROUND = "pref_background";
    public static final String PREF_SOUND_ENABLED = "pref_sound_enabled";
    public static final String PREF_WING_ANIMATION_ENABLED = "pref_wing_animation_enabled";
    public static final String PREF_PIPE_COLOR = "pref_pipe_color";
    public static final String PREF_HIDE_SETTINGS_ICON = "pref_hide_settings_icon";
    public static final String PREF_SHOW_TAP_GUIDE = "pref_show_tap_guide";
    public static final String PREF_SHOW_GET_READY = "pref_show_get_ready";
    public static final String PREF_SHOW_SCORE = "pref_show_score";
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
    public static final String PREF_GOD_FALL_ENABLED = "pref_god_fall_enabled";
    public static final String PREF_GOD_FALL_SECONDS = "pref_god_fall_seconds";
    public static final String PREF_ACTIVITY_LOG_ENABLED = "pref_activity_log_enabled";
    public static final String PREF_UPSIDE_DOWN_ENABLED = "pref_upside_down_enabled";
    public static final String PREF_REVERSE_PIPES_ENABLED = "pref_reverse_pipes_enabled";
    public static final String PREF_SETTINGS_DISCLAIMER_SHOWN = "pref_settings_disclaimer_shown";
    public static final String PREF_HAPTIC_FEEDBACK_ENABLED = "pref_haptic_feedback_enabled";
    public static final String PREF_HAPTIC_STRENGTH = "pref_haptic_strength";
    public static final String PREF_HAPTIC_FLAP = "pref_haptic_flap";
    public static final String PREF_HAPTIC_SCORE = "pref_haptic_score";
    public static final String PREF_HAPTIC_CRASH = "pref_haptic_crash";
    public static final String PREF_HAPTIC_BEST = "pref_haptic_best";
    public static final String PREF_HAPTIC_MENUS = "pref_haptic_menus";
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
    public static final String PREF_ORIGINAL_MOTION_ENABLED = "pref_original_motion_enabled";
    public static final String KEY_PHYSICS_CHOSEN = "physicsChosen";
    public static final String PREF_FIXED_STEP_RATE = "pref_fixed_step_rate";
    public static final String PREF_FIXED_STEP_TIMING = "pref_fixed_step_timing";
    public static final String PREF_FRAME_RATE_LIMIT = "pref_frame_rate_limit";
    public static final String PREF_REPLAY_ENABLED = "pref_replay_enabled";
    public static final String PREF_REPLAY_KEEP = "pref_replay_keep";
    public static final String PREF_REPLAY_RESOLUTION = "pref_replay_resolution";
    public static final String PREF_REPLAY_FPS = "pref_replay_fps";
    public static final String PREF_REPLAY_QUALITY = "pref_replay_quality";
    public static final String PREF_REPLAY_SOUND = "pref_replay_sound";
    public static final String PREF_REPLAY_CREDIT = "pref_replay_credit";
    public static final String PREF_REPLAY_MODE = "pref_replay_mode";
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
    public static final String PREF_KEYBOARD_ENABLED = "pref_keyboard_enabled";
    public static final String PREF_KEY_FLAP = "pref_key_flap";
    public static final String PREF_KEY_FLAP_ALT = "pref_key_flap_alt";
    public static final String PREF_KEY_PAUSE = "pref_key_pause";
    public static final String PREF_KEY_PLAY = "pref_key_play";
    public static final String PREF_KEY_SETTINGS = "pref_key_settings";
    public static final String PREF_RIGHT_CLICK_PAUSES = "pref_right_click_pauses";
    public static final String PREF_HIDE_CURSOR = "pref_hide_cursor";
    public static final String PREF_BOARD_AUTO_POST = "pref_board_auto_post";
    public static final String PREF_BOARD_SHOW_STATUS = "pref_board_show_status";
    public static final String PREF_BOARD_OPEN_ON_BEST = "pref_board_open_on_best";
    public static final String PREF_BOARD_TAB = "pref_board_tab";
    public static final String PREF_BOARD_SIZE = "pref_board_size";

    public static final int DEFAULT_BIRD_COLOR = -1;
    public static final int DEFAULT_BACKGROUND = -1;
    public static final boolean DEFAULT_SOUND_ENABLED = true;
    public static final boolean DEFAULT_WING_ANIMATION_ENABLED = true;
    public static final int DEFAULT_PIPE_COLOR = 0;
    public static final boolean DEFAULT_HIDE_SETTINGS_ICON = false;
    public static final boolean DEFAULT_SHOW_TAP_GUIDE = true;
    public static final boolean DEFAULT_SHOW_GET_READY = true;
    public static final boolean DEFAULT_SHOW_SCORE = true;
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
    public static final boolean DEFAULT_GOD_FALL_ENABLED = true;
    public static final float DEFAULT_GOD_FALL_SECONDS = 2f;
    public static final boolean DEFAULT_ACTIVITY_LOG_ENABLED = true;
    public static final boolean DEFAULT_UPSIDE_DOWN_ENABLED = false;
    public static final boolean DEFAULT_REVERSE_PIPES_ENABLED = false;
    public static final boolean DEFAULT_HAPTIC_FEEDBACK_ENABLED = true;
    public static final int DEFAULT_HAPTIC_STRENGTH = 1;
    public static final boolean DEFAULT_HAPTIC_FLAP = true;
    public static final boolean DEFAULT_HAPTIC_SCORE = true;
    public static final boolean DEFAULT_HAPTIC_CRASH = true;
    public static final boolean DEFAULT_HAPTIC_BEST = true;
    public static final boolean DEFAULT_HAPTIC_MENUS = true;
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
    public static final boolean DEFAULT_ORIGINAL_MOTION_ENABLED = false;
    public static final int DEFAULT_FIXED_STEP_RATE = 60;
    public static final int DEFAULT_FIXED_STEP_TIMING = 0;
    public static final int DEFAULT_FRAME_RATE_LIMIT = 0;
    public static final int STEP_TIMING_SMOOTH = 0;
    public static final int STEP_TIMING_ORIGINAL = 1;
    static final int[] STEP_RATES = {30, 60, 120};
    static final int[] FRAME_LIMITS = {0, 30, 60, 90, 120};

    static int stepRate(int stored) {
        for (int rate : STEP_RATES) if (rate == stored) return rate;
        return DEFAULT_FIXED_STEP_RATE;
    }

    static int frameLimit(int stored) {
        for (int limit : FRAME_LIMITS) if (limit == stored) return limit;
        return DEFAULT_FRAME_RATE_LIMIT;
    }
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
    public static final boolean DEFAULT_KEYBOARD_ENABLED = true;
    public static final int DEFAULT_KEY_FLAP = KeyEvent.KEYCODE_SPACE;
    public static final int DEFAULT_KEY_FLAP_ALT = KeyEvent.KEYCODE_DPAD_UP;
    public static final int DEFAULT_KEY_PAUSE = KeyEvent.KEYCODE_P;
    public static final int DEFAULT_KEY_PLAY = KeyEvent.KEYCODE_ENTER;
    public static final int DEFAULT_KEY_SETTINGS = KeyEvent.KEYCODE_S;
    public static final boolean DEFAULT_RIGHT_CLICK_PAUSES = true;
    public static final boolean DEFAULT_HIDE_CURSOR = true;
    public static final boolean DEFAULT_BOARD_AUTO_POST = true;
    public static final boolean DEFAULT_BOARD_SHOW_STATUS = true;
    public static final boolean DEFAULT_BOARD_OPEN_ON_BEST = false;
    public static final int DEFAULT_BOARD_TAB = 0;
    public static final int DEFAULT_BOARD_SIZE = 0;
    static final int[] BOARD_SIZES = {10, 25, 50, 100};
    public static final boolean DEFAULT_REPLAY_ENABLED = true;
    public static final int DEFAULT_REPLAY_KEEP = 3;
    public static final int DEFAULT_REPLAY_RESOLUTION = 0;
    public static final int DEFAULT_REPLAY_FPS = 60;
    public static final int DEFAULT_REPLAY_QUALITY = 1;
    public static final boolean DEFAULT_REPLAY_SOUND = true;
    public static final boolean DEFAULT_REPLAY_CREDIT = true;
    public static final int DEFAULT_REPLAY_MODE = 0;
    static final int REPLAY_MODE_RECENT = 0;
    static final int REPLAY_MODE_BEST = 1;
    static final int[] REPLAY_KEEPS = {1, 3, 5, 10};
    static final int[] REPLAY_FPS_CHOICES = {30, 60};
    static final int REPLAY_RESOLUTION_ORIGINAL = 0;
    static final int REPLAY_RESOLUTION_1080 = 1;
    static final int REPLAY_RESOLUTION_720 = 2;

    static int replayKeep(int stored) {
        for (int keep : REPLAY_KEEPS) if (keep == stored) return keep;
        return DEFAULT_REPLAY_KEEP;
    }

    static int replayFps(int stored) {
        for (int fps : REPLAY_FPS_CHOICES) if (fps == stored) return fps;
        return DEFAULT_REPLAY_FPS;
    }
    static final int MAX_BOARD_SIZE = 100;

    public static final int FAIR_NONE = 0;
    public static final int FAIR_STANDARD = 1;
    public static final int FAIR_CLASSIC = 2;
    public static final String FAIR_MIXED = "mixed";
    private static final int WEATHER_SLOW_MOTION = 2;

    private static Object[][] fairRules() {
        return new Object[][] {
                {PREF_GAME_SPEED, DEFAULT_GAME_SPEED, CLASSIC_SPEED},
                {PREF_GRAVITY, DEFAULT_GRAVITY, CLASSIC_GRAVITY},
                {PREF_JUMP_STRENGTH, DEFAULT_JUMP_STRENGTH, CLASSIC_FLAP},
                {PREF_BIRD_HANG_DELAY, DEFAULT_BIRD_HANG_DELAY, null},
                {PREF_PIPE_GAP, DEFAULT_PIPE_GAP, CLASSIC_GAP},
                {PREF_PIPE_SPACING, DEFAULT_PIPE_SPACING, CLASSIC_SPACING},
                {PREF_BIRD_HITBOX, DEFAULT_BIRD_HITBOX, null},
                {PREF_PIPE_VARIATION, DEFAULT_PIPE_VARIATION, null},
                {PREF_SCORE_MULTIPLIER, DEFAULT_SCORE_MULTIPLIER, null},
                {PREF_MOVING_PIPES_ENABLED, DEFAULT_MOVING_PIPES_ENABLED, false},
                {PREF_PIPE_MOVE_TIER_1_SCORE, DEFAULT_PIPE_MOVE_TIER_1_SCORE, null},
                {PREF_PIPE_MOVE_TIER_2_SCORE, DEFAULT_PIPE_MOVE_TIER_2_SCORE, null},
                {PREF_NO_CLIP_ENABLED, DEFAULT_NO_CLIP_ENABLED, null},
                {PREF_UPSIDE_DOWN_ENABLED, DEFAULT_UPSIDE_DOWN_ENABLED, null},
                {PREF_REVERSE_PIPES_ENABLED, DEFAULT_REVERSE_PIPES_ENABLED, null},
                {PREF_PIPE_SPEED_VARIATION, DEFAULT_PIPE_SPEED_VARIATION, null},
                {PREF_BIRD_SIZE, DEFAULT_BIRD_SIZE, null},
                {PREF_PIPE_WIDTH, DEFAULT_PIPE_WIDTH, null},
                {PREF_JETPACK_MODE_ENABLED, DEFAULT_JETPACK_MODE_ENABLED, null},
                {PREF_DRUNK_MODE_ENABLED, DEFAULT_DRUNK_MODE_ENABLED, null},
                {PREF_GOLDEN_PIPE_ENABLED, DEFAULT_GOLDEN_PIPE_ENABLED, null},
                {PREF_EXTRA_LIVES, DEFAULT_EXTRA_LIVES, null},
                {PREF_AUTOPILOT_ENABLED, DEFAULT_AUTOPILOT_ENABLED, null},
                {PREF_SPEED_RAMP, DEFAULT_SPEED_RAMP, null},
                {PREF_GAP_SHRINK, DEFAULT_GAP_SHRINK, null},
                {PREF_MAX_FALL_SPEED, DEFAULT_MAX_FALL_SPEED, CLASSIC_MAX_FALL_SPEED},
                {PREF_CEILING_ENABLED, DEFAULT_CEILING_ENABLED, false},
                {PREF_FLAP_LIMIT_ENABLED, DEFAULT_FLAP_LIMIT_ENABLED, true},
                {PREF_HITBOX_SHAPE, DEFAULT_HITBOX_SHAPE, HITBOX_SHAPE_SQUARE},
                {PREF_GAP_PLACEMENT, DEFAULT_GAP_PLACEMENT, GAP_PLACEMENT_CLASSIC},
                {PREF_SCORE_POINT, DEFAULT_SCORE_POINT, SCORE_POINT_CLASSIC},
                {PREF_BIRD_POSITION, DEFAULT_BIRD_POSITION, CLASSIC_BIRD_POSITION},
        };
    }

    private static boolean sameValue(Object stored, Object wanted) {
        if (stored == null) return false;
        if (wanted instanceof Boolean) return wanted.equals(stored);
        if (wanted instanceof Number) {
            return stored instanceof Number && Math.abs(((Number) stored).doubleValue() - ((Number) wanted).doubleValue()) < 0.0005;
        }
        return wanted.equals(stored);
    }

    public static String fairPlayBlocker(Map<String, ?> stored) {
        boolean standard = true;
        boolean classic = true;
        for (Object[] rule : fairRules()) {
            String key = (String) rule[0];
            Object value = stored.containsKey(key) ? stored.get(key) : rule[1];
            boolean asDefault = sameValue(value, rule[1]);
            boolean asClassic = sameValue(value, rule[2] != null ? rule[2] : rule[1]);
            if (!asDefault && !asClassic) return key;
            standard &= asDefault;
            classic &= asClassic;
        }
        Object weather = stored.get(PREF_WEATHER_EFFECT);
        if (weather != null && !(weather instanceof Integer && (Integer) weather != WEATHER_SLOW_MOTION)) return PREF_WEATHER_EFFECT;
        if (!standard && !classic) return FAIR_MIXED;
        boolean stepped = Boolean.TRUE.equals(stored.containsKey(PREF_FIXED_STEP_ENABLED) ? stored.get(PREF_FIXED_STEP_ENABLED) : DEFAULT_FIXED_STEP_ENABLED)
                && Boolean.TRUE.equals(stored.containsKey(PREF_PIXEL_STEPS_ENABLED) ? stored.get(PREF_PIXEL_STEPS_ENABLED) : DEFAULT_PIXEL_STEPS_ENABLED);
        if (stepped && standard) return PREF_PIXEL_STEPS_ENABLED;
        Object rate = stored.get(PREF_FIXED_STEP_RATE);
        int stepRate = stepRate(rate instanceof Integer ? (Integer) rate : DEFAULT_FIXED_STEP_RATE);
        if (rate != null && !(rate instanceof Integer && (Integer) rate == stepRate)) return PREF_FIXED_STEP_RATE;
        if (stepped && stepRate != DEFAULT_FIXED_STEP_RATE) return PREF_FIXED_STEP_RATE;
        return null;
    }

    public static int fairPlayMode(Map<String, ?> stored) {
        if (fairPlayBlocker(stored) != null) return FAIR_NONE;
        for (Object[] rule : fairRules()) {
            String key = (String) rule[0];
            if (stored.containsKey(key) && !sameValue(stored.get(key), rule[1])) return FAIR_CLASSIC;
        }
        return FAIR_STANDARD;
    }

    public static String fairKeyName(String key) {
        if (key == null) return "";
        if (FAIR_MIXED.equals(key)) return "a mix of Classic and normal settings";
        if (PREF_NO_CLIP_ENABLED.equals(key)) return "God mode";
        if (PREF_WEATHER_EFFECT.equals(key)) return "thunderstorm";
        if (PREF_PIXEL_STEPS_ENABLED.equals(key)) return "whole-pixel movement";
        if (PREF_FIXED_STEP_RATE.equals(key)) return "step rate with whole-pixel movement";
        String name = key.startsWith("pref_") ? key.substring(5) : key;
        if (name.endsWith("_enabled")) name = name.substring(0, name.length() - 8);
        if (name.endsWith("_percent")) name = name.substring(0, name.length() - 8);
        return name.replace('_', ' ');
    }

    public static long installSalt(SharedPreferences store) {
        return installSalt(store, store.getAll());
    }

    private static long installSalt(SharedPreferences store, Map<String, ?> all) {
        Object stored = all.get(KEY_INSTALL_SALT);
        if (stored instanceof Long) return (Long) stored;
        long salt = new SecureRandom().nextLong() | 1L;
        store.edit().putLong(KEY_INSTALL_SALT, salt).apply();
        return salt;
    }

    public static String scoreSeal(int score, String assists, long salt) {
        long hash = salt ^ 0x5DEECE66DL;
        hash = hash * 0x100000001B3L + score;
        String text = assists == null ? "" : assists;
        for (int i = 0; i < text.length(); i++) hash = (hash ^ text.charAt(i)) * 0x100000001B3L;
        hash ^= hash >>> 29;
        hash *= 0xBF58476D1CE4E5B9L;
        hash ^= hash >>> 32;
        return Long.toHexString(hash);
    }

    private static int earnedIn(Map<String, ?> all) {
        Object stored = all.get(KEY_EARNED_HIGH_SCORE);
        return stored instanceof Integer ? Math.max(0, (Integer) stored) : 0;
    }

    private static String assistsIn(Map<String, ?> all) {
        Object stored = all.get(KEY_HIGH_SCORE_ASSISTS);
        return stored instanceof String ? (String) stored : "";
    }

    private static boolean sealIntact(SharedPreferences store, Map<String, ?> all) {
        if (!all.containsKey(KEY_EARNED_HIGH_SCORE)) return true;
        Object seal = all.get(KEY_HIGH_SCORE_SEAL);
        return seal instanceof String && seal.equals(scoreSeal(earnedIn(all), assistsIn(all), installSalt(store, all)));
    }

    public static int earnedHighScore(SharedPreferences store) {
        return earnedIn(store.getAll());
    }

    public static String highScoreAssists(SharedPreferences store) {
        return assistsIn(store.getAll());
    }

    public static boolean highScoreSealIntact(SharedPreferences store) {
        return sealIntact(store, store.getAll());
    }

    public static void recordEarnedScore(SharedPreferences store, int score, String assists) {
        String clean = assists == null ? "" : assists;
        int safe = Math.max(0, score);
        store.edit()
                .putInt(KEY_EARNED_HIGH_SCORE, safe)
                .putString(KEY_HIGH_SCORE_ASSISTS, clean)
                .putString(KEY_HIGH_SCORE_SEAL, scoreSeal(safe, clean, installSalt(store)))
                .apply();
    }

    public static void adoptOldHighScore(SharedPreferences store) {
        Map<String, ?> all = store.getAll();
        Object size = all.get(PREF_BOARD_SIZE);
        if (size instanceof Integer && ((Integer) size < 0 || (Integer) size >= BOARD_SIZES.length)) {
            store.edit().putInt(PREF_BOARD_SIZE, BOARD_SIZES.length - 1).apply();
        }
        if (all.containsKey(KEY_EARNED_HIGH_SCORE)) return;
        Object old = all.get(KEY_HIGH_SCORE);
        recordEarnedScore(store, old instanceof Integer ? (Integer) old : 0, "");
    }

    public static int submitRound(SharedPreferences store, int score, String assists) {
        Map<String, ?> all = store.getAll();
        int earned = earnedIn(all);
        boolean intact = sealIntact(store, all);
        boolean cleanTie = score == earned && score > 0 && (assists == null || assists.isEmpty()) && !assistsIn(all).isEmpty();
        if (!intact || score > earned || cleanTie) recordEarnedScore(store, score, assists);
        if (!intact) return -1;
        return score > earned || cleanTie ? earned : Integer.MIN_VALUE;
    }

    public static String appVersion(Context context) {
        try {
            android.content.pm.PackageInfo info = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            long code = Build.VERSION.SDK_INT >= 28 ? info.getLongVersionCode() : info.versionCode;
            return info.versionName + " (" + code + ")";
        } catch (Exception e) {
            return "unknown";
        }
    }

    public static final class Feel {
        public static final int FLAP = 0, SCORE = 1, GOLDEN = 2, CRASH = 3, LIFE = 4, LAND = 5, BEST = 6, MEDAL = 7,
                BUTTON = 8, TICK = 9, PAUSE = 10, TOGGLE = 11, CONFIRM = 12, REJECT = 13, PRESS = 14;
        private static final int KINDS = 15;
        private static final float[] STRENGTHS = {0.55f, 0.8f, 1f};
        private static final int VIEW_ONLY = 0, PREDEFINED = 1, COMPOSED = 2, AMPLITUDE = 3, SIMPLE = 4;

        private final Vibrator vibrator;
        private final View view;
        private final Object[] effects = new Object[KINDS * STRENGTHS.length];
        private int mode = -1;
        private boolean thud, lowTick, rise;
        private boolean on = DEFAULT_HAPTIC_FEEDBACK_ENABLED, flap = DEFAULT_HAPTIC_FLAP, score = DEFAULT_HAPTIC_SCORE,
                crash = DEFAULT_HAPTIC_CRASH, best = DEFAULT_HAPTIC_BEST, menus = DEFAULT_HAPTIC_MENUS;
        private int strength = DEFAULT_HAPTIC_STRENGTH;

        public Feel(Vibrator vibrator, View view) {
            this.vibrator = vibrator;
            this.view = view;
        }

        public Feel(Context context, View view) {
            Vibrator found = null;
            try {
                if (context != null) found = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
            } catch (RuntimeException ignored) {
            }
            this.vibrator = found;
            this.view = view;
        }

        public void load(Map<String, ?> prefs) {
            set(flagOf(prefs, PREF_HAPTIC_FEEDBACK_ENABLED, DEFAULT_HAPTIC_FEEDBACK_ENABLED), intOf(prefs, PREF_HAPTIC_STRENGTH, DEFAULT_HAPTIC_STRENGTH),
                    flagOf(prefs, PREF_HAPTIC_FLAP, DEFAULT_HAPTIC_FLAP), flagOf(prefs, PREF_HAPTIC_SCORE, DEFAULT_HAPTIC_SCORE),
                    flagOf(prefs, PREF_HAPTIC_CRASH, DEFAULT_HAPTIC_CRASH), flagOf(prefs, PREF_HAPTIC_BEST, DEFAULT_HAPTIC_BEST),
                    flagOf(prefs, PREF_HAPTIC_MENUS, DEFAULT_HAPTIC_MENUS));
        }

        public void set(boolean on, int strength, boolean flap, boolean score, boolean crash, boolean best, boolean menus) {
            this.on = on;
            this.strength = Math.max(0, Math.min(STRENGTHS.length - 1, strength));
            this.flap = flap;
            this.score = score;
            this.crash = crash;
            this.best = best;
            this.menus = menus;
        }

        private static boolean flagOf(Map<String, ?> prefs, String key, boolean fallback) {
            Object value = prefs == null ? null : prefs.get(key);
            return value instanceof Boolean ? (Boolean) value : fallback;
        }

        private static int intOf(Map<String, ?> prefs, String key, int fallback) {
            Object value = prefs == null ? null : prefs.get(key);
            return value instanceof Number ? ((Number) value).intValue() : fallback;
        }

        public boolean enabled() {
            return on;
        }

        public boolean allows(int kind) {
            if (!on) return false;
            switch (kind) {
                case FLAP: return flap;
                case SCORE: case GOLDEN: return score;
                case CRASH: case LIFE: case LAND: return crash;
                case BEST: case MEDAL: return best;
                default: return menus;
            }
        }

        public void play(int kind) {
            if (kind < 0 || kind >= KINDS || !allows(kind)) return;
            if (mode < 0) mode = pickMode();
            if (mode != VIEW_ONLY) {
                VibrationEffect effect = mode == SIMPLE ? null : effect(kind);
                try {
                    if (effect != null) {
                        vibrator.vibrate(effect);
                        return;
                    }
                    if (mode == SIMPLE && simple(kind)) return;
                } catch (RuntimeException e) {
                    mode = VIEW_ONLY;
                }
            }
            if (view != null) view.performHapticFeedback(viewFeedback(kind));
        }

        private int pickMode() {
            if (vibrator == null) return VIEW_ONLY;
            try {
                if (!vibrator.hasVibrator()) return VIEW_ONLY;
                if (Build.VERSION.SDK_INT >= 30 && vibrator.areAllPrimitivesSupported(VibrationEffect.Composition.PRIMITIVE_CLICK,
                        VibrationEffect.Composition.PRIMITIVE_TICK)) {
                    rise = vibrator.areAllPrimitivesSupported(VibrationEffect.Composition.PRIMITIVE_QUICK_RISE);
                    if (Build.VERSION.SDK_INT >= 31) {
                        thud = vibrator.areAllPrimitivesSupported(VibrationEffect.Composition.PRIMITIVE_THUD);
                        lowTick = vibrator.areAllPrimitivesSupported(VibrationEffect.Composition.PRIMITIVE_LOW_TICK);
                    }
                    return COMPOSED;
                }
                if (Build.VERSION.SDK_INT >= 29) return PREDEFINED;
                if (Build.VERSION.SDK_INT >= 26 && vibrator.hasAmplitudeControl()) return AMPLITUDE;
                return SIMPLE;
            } catch (RuntimeException e) {
                return VIEW_ONLY;
            }
        }

        private VibrationEffect effect(int kind) {
            int slot = kind * STRENGTHS.length + strength;
            VibrationEffect effect = (VibrationEffect) effects[slot];
            if (effect == null) {
                float level = STRENGTHS[strength];
                if (mode == COMPOSED) effect = composed(kind, level);
                else if (mode == PREDEFINED) effect = predefined(kind, strength);
                else if (mode == AMPLITUDE) effect = waveform(kind, level);
                effects[slot] = effect;
            }
            return effect;
        }

        private static float clamp(float value) {
            return Math.max(0.05f, Math.min(1f, value));
        }

        private VibrationEffect composed(int kind, float s) {
            VibrationEffect.Composition c = VibrationEffect.startComposition();
            int click = VibrationEffect.Composition.PRIMITIVE_CLICK;
            int tick = VibrationEffect.Composition.PRIMITIVE_TICK;
            switch (kind) {
                case FLAP: c.addPrimitive(click, clamp(0.85f * s)); break;
                case SCORE: c.addPrimitive(tick, clamp(0.45f * s)); break;
                case GOLDEN: c.addPrimitive(tick, clamp(0.5f * s)).addPrimitive(tick, clamp(0.85f * s), 40); break;
                case CRASH:
                    if (thud) c.addPrimitive(VibrationEffect.Composition.PRIMITIVE_THUD, clamp(1f * s)).addPrimitive(click, clamp(0.5f * s), 20);
                    else c.addPrimitive(click, clamp(1f * s)).addPrimitive(tick, clamp(0.6f * s), 30);
                    break;
                case LIFE: c.addPrimitive(click, clamp(0.9f * s)).addPrimitive(tick, clamp(0.5f * s), 70); break;
                case LAND:
                    if (lowTick) c.addPrimitive(VibrationEffect.Composition.PRIMITIVE_LOW_TICK, clamp(0.9f * s));
                    else c.addPrimitive(tick, clamp(0.6f * s));
                    break;
                case BEST:
                    if (rise) c.addPrimitive(VibrationEffect.Composition.PRIMITIVE_QUICK_RISE, clamp(0.7f * s)).addPrimitive(click, clamp(1f * s), 20);
                    else c.addPrimitive(click, clamp(0.6f * s)).addPrimitive(click, clamp(1f * s), 70);
                    break;
                case MEDAL: c.addPrimitive(click, clamp(0.8f * s)).addPrimitive(tick, clamp(0.5f * s), 60).addPrimitive(tick, clamp(0.35f * s), 60); break;
                case BUTTON: c.addPrimitive(click, clamp(0.55f * s)); break;
                case PRESS: c.addPrimitive(tick, clamp(0.4f * s)); break;
                case PAUSE: c.addPrimitive(tick, clamp(0.6f * s)).addPrimitive(tick, clamp(0.4f * s), 50); break;
                case TOGGLE: c.addPrimitive(click, clamp(0.5f * s)); break;
                case CONFIRM: c.addPrimitive(click, clamp(0.5f * s)).addPrimitive(click, clamp(0.85f * s), 60); break;
                case REJECT: c.addPrimitive(tick, clamp(0.7f * s)).addPrimitive(tick, clamp(0.7f * s), 60).addPrimitive(tick, clamp(0.7f * s), 60); break;
                default: c.addPrimitive(tick, clamp(0.35f * s)); break;
            }
            return c.compose();
        }

        private static VibrationEffect predefined(int kind, int strength) {
            boolean light = strength == 0;
            int id;
            switch (kind) {
                case CRASH: id = light ? VibrationEffect.EFFECT_CLICK : VibrationEffect.EFFECT_HEAVY_CLICK; break;
                case LIFE: case BEST: case REJECT: id = light ? VibrationEffect.EFFECT_CLICK : VibrationEffect.EFFECT_DOUBLE_CLICK; break;
                case FLAP: case BUTTON: case TOGGLE: case CONFIRM: case MEDAL: case LAND:
                    id = light ? VibrationEffect.EFFECT_TICK : VibrationEffect.EFFECT_CLICK; break;
                default: id = VibrationEffect.EFFECT_TICK; break;
            }
            return VibrationEffect.createPredefined(id);
        }

        private static int amplitude(float value) {
            return Math.max(1, Math.min(255, Math.round(value * 255f)));
        }

        private static VibrationEffect waveform(int kind, float s) {
            switch (kind) {
                case FLAP: return VibrationEffect.createOneShot(14, amplitude(0.55f * s));
                case SCORE: return VibrationEffect.createOneShot(8, amplitude(0.35f * s));
                case GOLDEN: return VibrationEffect.createWaveform(new long[] {0, 8, 40, 10}, new int[] {0, amplitude(0.4f * s), 0, amplitude(0.65f * s)}, -1);
                case CRASH: return VibrationEffect.createWaveform(new long[] {0, 30, 20, 30}, new int[] {0, amplitude(s), 0, amplitude(0.5f * s)}, -1);
                case LIFE: return VibrationEffect.createWaveform(new long[] {0, 20, 60, 12}, new int[] {0, amplitude(0.85f * s), 0, amplitude(0.5f * s)}, -1);
                case LAND: return VibrationEffect.createOneShot(18, amplitude(0.6f * s));
                case BEST: return VibrationEffect.createWaveform(new long[] {0, 12, 40, 25}, new int[] {0, amplitude(0.5f * s), 0, amplitude(s)}, -1);
                case MEDAL: return VibrationEffect.createWaveform(new long[] {0, 18, 60, 10, 60, 8},
                        new int[] {0, amplitude(0.8f * s), 0, amplitude(0.5f * s), 0, amplitude(0.35f * s)}, -1);
                case PAUSE: return VibrationEffect.createWaveform(new long[] {0, 8, 50, 8}, new int[] {0, amplitude(0.5f * s), 0, amplitude(0.35f * s)}, -1);
                case REJECT: return VibrationEffect.createWaveform(new long[] {0, 10, 60, 10, 60, 10},
                        new int[] {0, amplitude(0.6f * s), 0, amplitude(0.6f * s), 0, amplitude(0.6f * s)}, -1);
                case BUTTON: case TOGGLE: case CONFIRM: return VibrationEffect.createOneShot(12, amplitude(0.5f * s));
                default: return VibrationEffect.createOneShot(6, amplitude(0.3f * s));
            }
        }

        @SuppressWarnings("deprecation")
        private boolean simple(int kind) {
            switch (kind) {
                case CRASH: vibrator.vibrate(strength == 0 ? 20 : 35); return true;
                case LIFE: vibrator.vibrate(new long[] {0, 20, 60, 12}, -1); return true;
                case BEST: vibrator.vibrate(new long[] {0, 12, 40, 25}, -1); return true;
                case MEDAL: vibrator.vibrate(18); return true;
                default: return false;
            }
        }

        private static int viewFeedback(int kind) {
            switch (kind) {
                case CRASH: return HapticFeedbackConstants.LONG_PRESS;
                case CONFIRM: return Build.VERSION.SDK_INT >= 30 ? HapticFeedbackConstants.CONFIRM : HapticFeedbackConstants.VIRTUAL_KEY;
                case REJECT: return Build.VERSION.SDK_INT >= 30 ? HapticFeedbackConstants.REJECT : HapticFeedbackConstants.LONG_PRESS;
                case SCORE: case TICK: case GOLDEN: case PAUSE: case LAND: case PRESS: return HapticFeedbackConstants.CLOCK_TICK;
                case TOGGLE: return HapticFeedbackConstants.KEYBOARD_TAP;
                default: return HapticFeedbackConstants.VIRTUAL_KEY;
            }
        }
    }

    public static String[] videoCredit(Context context) {
        String version = null;
        try {
            version = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
        } catch (Exception ignored) {
        }
        version = version == null ? "" : version.trim();
        while (version.startsWith("v") || version.startsWith("V")) version = version.substring(1).trim();
        String line = (version.isEmpty() ? "" : "V" + version + " ") + "by " + MADE_BY + " \u00B7 Unofficial";
        return new String[] {APP_NAME, line};
    }

    public static String deviceSummary(Context context) {
        return "v" + appVersion(context) + ", Android " + Build.VERSION.RELEASE + " (API " + Build.VERSION.SDK_INT + "), "
                + Build.MANUFACTURER + " " + Build.MODEL;
    }

    public static final class ActivityLog {
        private static final String FILE_NAME = "activity_log.txt";
        private static final long MAX_BYTES = 512 * 1024;
        private static final int KEEP_BYTES = 384 * 1024;
        private static final int MAX_PENDING_CHARS = 256 * 1024;
        private static final String INDENT = "\n                                ";
        private static final SimpleDateFormat STAMP = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
        private static final Object PENDING_LOCK = new Object();
        private static final Object FILE_LOCK = new Object();
        private static final StringBuilder pending = new StringBuilder();
        private static File pendingFile;
        private static boolean drainScheduled;
        private static int droppedChars;
        private static ExecutorService writer;
        private static volatile Boolean enabled;

        private ActivityLog() {
        }

        public static void setEnabled(boolean on) {
            enabled = on;
        }

        public static boolean isEnabled(Context context) {
            Boolean on = enabled;
            if (on == null) {
                on = true;
                try {
                    Object stored = context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE).getAll().get(PREF_ACTIVITY_LOG_ENABLED);
                    if (stored instanceof Boolean) on = (Boolean) stored;
                } catch (RuntimeException ignored) {
                }
                enabled = on;
            }
            return on;
        }

        private static File file(Context context) {
            return new File(context.getFilesDir(), FILE_NAME);
        }

        public static void add(Context context, String kind, String message) {
            if (message == null) return;
            add(context, kind, Collections.singletonList(message));
        }

        public static void add(Context context, String kind, List<String> messages) {
            if (context == null || messages == null || messages.isEmpty() || !isEnabled(context)) return;
            try {
                String text = format(kind, messages);
                if (text.isEmpty()) return;
                queue(file(context), text);
                scheduleDrain();
            } catch (RuntimeException e) {
                Log.w("ActivityLog", "Could not queue a log entry", e);
            }
        }

        public static void addNow(Context context, String kind, String message) {
            if (context == null || message == null || !isEnabled(context)) return;
            try {
                queue(file(context), format(kind, Collections.singletonList(message)));
                drain();
            } catch (RuntimeException e) {
                Log.w("ActivityLog", "Could not write a log entry", e);
            }
        }

        public static String read(Context context) {
            if (context == null) return "";
            drain();
            File log;
            try {
                log = file(context);
            } catch (RuntimeException e) {
                return "";
            }
            synchronized (FILE_LOCK) {
                if (!log.exists()) return "";
                try {
                    return new String(readAll(log), "UTF-8");
                } catch (IOException | RuntimeException e) {
                    return "";
                }
            }
        }

        public static long size(Context context) {
            drain();
            try {
                return file(context).length();
            } catch (RuntimeException e) {
                return 0;
            }
        }

        private static String format(String kind, List<String> messages) {
            String tag = kind.length() >= 8 ? kind : (kind + "        ").substring(0, 8);
            String stamp;
            synchronized (STAMP) {
                stamp = STAMP.format(new Date());
            }
            StringBuilder lines = new StringBuilder();
            for (String message : messages) {
                if (message == null) continue;
                String body = message.replace("\r", "").trim().replace("\n", INDENT);
                lines.append(stamp).append("  ").append(tag).append("  ").append(body).append('\n');
            }
            return lines.toString();
        }

        private static void queue(File log, String text) {
            boolean otherFile;
            synchronized (PENDING_LOCK) {
                otherFile = pendingFile != null && !pendingFile.equals(log) && (pending.length() > 0 || droppedChars > 0);
            }
            if (otherFile) drain();
            synchronized (PENDING_LOCK) {
                pendingFile = log;
                if (pending.length() + text.length() > MAX_PENDING_CHARS) droppedChars += text.length();
                else pending.append(text);
            }
        }

        private static void scheduleDrain() {
            synchronized (PENDING_LOCK) {
                if (drainScheduled) return;
                drainScheduled = true;
            }
            try {
                writer().execute(ActivityLog::drain);
            } catch (RejectedExecutionException e) {
                drain();
            }
        }

        private static synchronized ExecutorService writer() {
            if (writer == null) {
                writer = Executors.newSingleThreadExecutor(task -> {
                    Thread thread = new Thread(task, "ActivityLog");
                    thread.setDaemon(true);
                    thread.setPriority(Thread.MIN_PRIORITY);
                    return thread;
                });
            }
            return writer;
        }

        private static void drain() {
            synchronized (FILE_LOCK) {
                String text;
                File log;
                synchronized (PENDING_LOCK) {
                    drainScheduled = false;
                    if (pendingFile == null || (pending.length() == 0 && droppedChars == 0)) return;
                    text = pending.toString();
                    pending.setLength(0);
                    if (droppedChars > 0) {
                        text = text + format("LOG", Collections.singletonList(droppedChars
                                + " characters of log entries were skipped because too many came in at once"));
                        droppedChars = 0;
                    }
                    log = pendingFile;
                }
                try {
                    try (FileOutputStream out = new FileOutputStream(log, true)) {
                        out.write(text.getBytes("UTF-8"));
                    }
                    if (log.length() > MAX_BYTES) trim(log);
                } catch (IOException | RuntimeException e) {
                    Log.w("ActivityLog", "Could not write the activity log", e);
                }
            }
        }

        private static byte[] readAll(File log) throws IOException {
            try (FileInputStream in = new FileInputStream(log)) {
                ByteArrayOutputStream bytes = new ByteArrayOutputStream((int) Math.min(log.length() + 16, MAX_BYTES * 2));
                byte[] buffer = new byte[8192];
                int count;
                while ((count = in.read(buffer)) != -1) bytes.write(buffer, 0, count);
                return bytes.toByteArray();
            }
        }

        private static void trim(File log) throws IOException {
            byte[] all = readAll(log);
            int start = Math.max(0, all.length - KEEP_BYTES);
            while (start < all.length && all[start] != '\n') start++;
            start = Math.min(all.length, start + 1);
            String note = format("LOG", Collections.singletonList("Older entries were removed to keep the log small"));
            try (FileOutputStream out = new FileOutputStream(log, false)) {
                out.write(note.getBytes("UTF-8"));
                out.write(all, start, all.length - start);
            }
        }
    }

    public static final class Leaderboard {
        static String DATABASE_URL = "https://flappybird-recreation09876-default-rtdb.firebaseio.com";
        static String API_KEY = "AIzaSyBALKV08ofRI-1ES4tz_EG8DyX6BgoEtMc";
        static String SIGN_UP_URL = "https://identitytoolkit.googleapis.com/v1/accounts:signUp";
        static String DELETE_ACCOUNT_URL = "https://identitytoolkit.googleapis.com/v1/accounts:delete";
        static String TOKEN_URL = "https://securetoken.googleapis.com/v1/token";
        static boolean allowInsecure;
        static Executor worker;
        static long retryDelayMs = 1500;

        static final int MAX_SCORE = 9999;
        static final int NAME_MAX = 16;
        static final String NAME_REFUSED = "That name isn't allowed. Pick another one";
        private static final String[] BLOCKED_ANYWHERE = {
                "fuck", "fukk", "fvck", "phuk", "phuck", "shit", "cunt", "bitch", "biatch", "nigg", "niga", "nigr", "faggot", "fagot",
                "whore", "slut", "asshole", "arsehole", "dumbass", "jackass", "dickhead", "bigdick", "cocksuck", "motherf", "penis",
                "vagina", "porn", "retard", "hitler", "nazi", "dildo", "jizz", "twat", "wanker", "bastard", "pussy", "boner",
                "blowjob", "handjob", "rimjob", "orgasm", "erection", "molest", "pedophil", "kike", "tranny", "negro", "beaner",
                "wetback", "skank", "horny", "titties", "boobies", "testicle", "scrotum", "clitoris", "killyourself", "goddam",
                "damn", "piss", "douche", "bollock", "bugger", "tosser", "kkk", "suicide", "terrorist", "stripper"};
        private static final String[] BLOCKED_WORDS = {
                "ass", "arse", "cum", "cock", "dick", "dik", "fuk", "fag", "tit", "hoe", "thot", "sex", "sexy", "rape", "raped",
                "rapist", "raping", "coon", "spic", "pedo", "cuck", "hell", "butt", "balls", "homo", "lesbo", "milf", "dilf", "pimp",
                "cracker", "nig", "wtf", "stfu", "gtfo", "lmfao", "kms", "kys", "crap", "prick", "shag", "wank", "anal", "anus",
                "semen", "nude", "nudes", "naked", "boob", "heil", "isis", "chink", "gook", "spick", "slag", "hooker", "sht", "fck", "btch"};

        static boolean nameAllowed(String name) {
            String clean = cleanName(name);
            if (clean.isEmpty()) return false;
            StringBuilder spaced = new StringBuilder();
            for (int i = 0; i < clean.length(); i++) {
                char c = clean.charAt(i);
                char before = i > 0 ? clean.charAt(i - 1) : ' ';
                boolean split = c == '_' || c == ' ' || (Character.isUpperCase(c) && Character.isLowerCase(before));
                if (split && spaced.length() > 0 && spaced.charAt(spaced.length() - 1) != ' ') spaced.append(' ');
                if (c != '_' && c != ' ') spaced.append(c);
            }
            String[] words = spaced.toString().trim().split(" ");
            StringBuilder joined = new StringBuilder();
            StringBuilder plain = new StringBuilder();
            for (String word : words) {
                String flat = flatten(word, true);
                String loose = flatten(word, false);
                if (blockedWord(flat) || blockedWord(loose)) return false;
                joined.append(flat);
                plain.append(loose);
            }
            String squashed = squash(joined.toString());
            if (blockedWord(joined.toString()) || blockedWord(squashed)) return false;
            for (String bad : BLOCKED_ANYWHERE) {
                if (joined.indexOf(bad) >= 0 || squashed.contains(bad) || plain.indexOf(bad) >= 0 || squash(plain.toString()).contains(bad)) return false;
            }
            return true;
        }

        private static boolean blockedWord(String word) {
            if (word.isEmpty()) return false;
            String squashed = squash(word);
            for (String bad : BLOCKED_WORDS) {
                if (word.equals(bad) || squashed.equals(bad) || word.equals(bad + "s") || word.equals(bad + "es")) return true;
            }
            return false;
        }

        private static String flatten(String word, boolean digitsAsLetters) {
            StringBuilder out = new StringBuilder();
            for (int i = 0; i < word.length(); i++) {
                char c = Character.toLowerCase(word.charAt(i));
                if (digitsAsLetters) {
                    switch (c) {
                        case '0': c = 'o'; break;
                        case '1': c = 'i'; break;
                        case '3': c = 'e'; break;
                        case '4': c = 'a'; break;
                        case '5': c = 's'; break;
                        case '7': c = 't'; break;
                        case '8': c = 'b'; break;
                        case '9': c = 'g'; break;
                        default: break;
                    }
                }
                if (c >= 'a' && c <= 'z') out.append(c);
            }
            return out.toString();
        }

        private static String squash(String text) {
            StringBuilder out = new StringBuilder();
            for (int i = 0; i < text.length(); i++) {
                char c = text.charAt(i);
                if (out.length() == 0 || out.charAt(out.length() - 1) != c) out.append(c);
            }
            return out.toString();
        }
        static final int VIEW_ALL_TIME = 0;
        static final int VIEW_WEEKLY = 1;
        static final int VIEW_VERIFIED_ALL_TIME = 2;
        static final int VIEW_VERIFIED_WEEKLY = 3;
        static final String[] VIEW_BRANCHES = {"scores", "weekly", "vscores", "vweekly"};
        static final long BOARD_FRESH_MS = 60000L;
        static final String OLD_VERSION = "V1.5.3 or older";
        static final String COUNTS_DONE = "zt";
        static final float STANDARD_SECONDS_PER_POINT = 1.43f;
        static final float CLASSIC_SECONDS_PER_POINT = 1.25f;
        static final long WEEK_MS = 604800000L;
        static final long WEEK_START_MS = 345600000L;
        static final long WEEK_KEY = 100000L;
        static final String POSTED = "posted";
        static final String REFUSED = "refused";
        static final String CURRENT = "current";
        static final String NO_CONNECTION = "No connection. Try again later";
        static final String NAME_TAKEN = "That name is taken";
        static final String OFFLINE = "offline";
        static final String VERIFYING = "verifying";
        static final int REPLAY_TEXT_MAX = 2500000;

        private static final String KEY_BEST = "boardBest";
        private static final String KEY_CLASSIC = "boardClassic";
        private static final String KEY_SECONDS = "boardSeconds";
        private static final String KEY_FLAPS = "boardFlaps";
        private static final String KEY_SEAL = "boardSeal";
        private static final String KEY_POSTED = "boardPosted";
        private static final String KEY_WEEK_POSTED = "boardWeekPosted";
        private static final String KEY_WEEK = "boardWeek";
        private static final String KEY_NAME = "boardName";
        private static final String KEY_NAME_SENT = "boardNameSent";
        private static final String KEY_NAME_CLAIMED = "boardNameClaimed";
        private static final String KEY_NAME_TAKEN = "boardNameTaken";
        private static final String KEY_ASKED = "boardAsked";
        private static final String KEY_UID = "boardUid";
        private static final String KEY_REFRESH = "boardRefresh";
        private static final String KEY_REPLAY_BEST = "boardReplayBest";
        private static final String KEY_REPLAY_WEEK_BEST = "boardReplayWeekBest";
        private static final String KEY_REPLAY_WEEK = "boardReplayWeek";
        private static final String JSON_TYPE = "application/json; charset=utf-8";
        private static final String FORM_TYPE = "application/x-www-form-urlencoded";
        private static final int TIMEOUT_MS = 8000;
        private static final int MAX_REPLY_BYTES = 512 * 1024;

        private static volatile String appVersionName = "";
        private static String idToken;
        private static long idTokenUntil;
        private static ExecutorService fetchPool;
        private static volatile long serverOffset;

        interface Poster {
            void post(Runnable task);
        }

        interface Callback<T> {
            void done(T value, String error);
        }

        static final class Entry {
            String uid = "";
            String name = "";
            int score;
            boolean classic;
            boolean verified;
            long time;
            String version = "";

            String versionLabel() {
                return version.isEmpty() ? OLD_VERSION : "V" + version;
            }
        }

        static final class Board {
            List<Entry> allTime = new ArrayList<>();
            List<Entry> weekly = new ArrayList<>();
            List<Entry> verifiedAllTime = new ArrayList<>();
            List<Entry> verifiedWeekly = new ArrayList<>();
            Entry mineAllTime;
            Entry mineWeekly;
            Entry mineVerifiedAllTime;
            Entry mineVerifiedWeekly;
            int rankAllTime;
            int rankWeekly;
            int rankVerifiedAllTime;
            int rankVerifiedWeekly;
            boolean verifying;
            int verdictScore;
            boolean verdictOk;
            String verdictWhy = "";
            long verdictTime;
            long weekEnds;
            final boolean[] loaded = new boolean[4];
            final long[] loadedAt = new long[4];
            boolean verdictKnown;
            boolean mineMissing;

            boolean fresh(int view, long now) {
                return view >= 0 && view < 4 && loaded[view] && now - loadedAt[view] >= 0 && now - loadedAt[view] < BOARD_FRESH_MS;
            }

            void absorb(Board other, int view) {
                switch (view) {
                    case VIEW_ALL_TIME:
                        allTime = other.allTime;
                        mineAllTime = other.mineAllTime;
                        rankAllTime = other.rankAllTime;
                        break;
                    case VIEW_WEEKLY:
                        weekly = other.weekly;
                        mineWeekly = other.mineWeekly;
                        rankWeekly = other.rankWeekly;
                        break;
                    case VIEW_VERIFIED_ALL_TIME:
                        verifiedAllTime = other.verifiedAllTime;
                        mineVerifiedAllTime = other.mineVerifiedAllTime;
                        rankVerifiedAllTime = other.rankVerifiedAllTime;
                        break;
                    default:
                        verifiedWeekly = other.verifiedWeekly;
                        mineVerifiedWeekly = other.mineVerifiedWeekly;
                        rankVerifiedWeekly = other.rankVerifiedWeekly;
                        break;
                }
                if (other.verdictKnown) {
                    verdictKnown = true;
                    verifying = other.verifying;
                    verdictScore = other.verdictScore;
                    verdictOk = other.verdictOk;
                    verdictWhy = other.verdictWhy;
                    verdictTime = other.verdictTime;
                }
                if (other.weekEnds > 0) weekEnds = other.weekEnds;
                if (view >= 0 && view < 4) {
                    loaded[view] = true;
                    loadedAt[view] = other.loadedAt[view];
                }
            }
        }

        static final class Replay {
            final byte[] header;
            final int[] events;
            final int count;
            final long seed;
            final int rules;
            final int marks;

            Replay(byte[] header, int[] events, int count, long seed, int rules, int marks) {
                this.header = header;
                this.events = events;
                this.count = count;
                this.seed = seed;
                this.rules = rules;
                this.marks = marks;
            }

            String encode() throws IOException {
                ByteArrayOutputStream bytes = new ByteArrayOutputStream(Math.max(1024, count));
                Deflater deflater = new Deflater(Deflater.BEST_COMPRESSION);
                try {
                    DataOutputStream out = new DataOutputStream(new java.io.BufferedOutputStream(new DeflaterOutputStream(bytes, deflater, 8192), 16384));
                    out.writeInt(header.length);
                    out.write(header);
                    out.writeInt(count);
                    java.nio.ByteBuffer packed = java.nio.ByteBuffer.allocate(4 * Math.max(1, Math.min(count, 16384)));
                    for (int from = 0; from < count; from += 16384) {
                        int to = Math.min(count, from + 16384);
                        packed.clear();
                        packed.asIntBuffer().put(events, from, to - from);
                        out.write(packed.array(), 0, (to - from) * 4);
                    }
                    out.close();
                } finally {
                    deflater.end();
                }
                return Base64.encodeToString(bytes.toByteArray(), Base64.NO_WRAP);
            }
        }

        static final class NameTaken extends Exception {
            private static final long serialVersionUID = 1L;

            NameTaken(String name) {
                super("The name \"" + name + "\" belongs to another player");
            }
        }

        static final class HttpError extends IOException {
            private static final long serialVersionUID = 1L;
            final int code;
            final String body;

            HttpError(int code, String body) {
                super("HTTP " + code + " " + (body.length() > 160 ? body.substring(0, 160) : body));
                this.code = code;
                this.body = body;
            }
        }

        private Leaderboard() {
        }

        static boolean configured() {
            if (API_KEY == null || API_KEY.trim().isEmpty() || DATABASE_URL == null) return false;
            String address = DATABASE_URL.trim();
            return address.startsWith("https://") || (allowInsecure && address.startsWith("http://"));
        }

        static long now() {
            return System.currentTimeMillis() + serverOffset;
        }

        static int week(long time) {
            return (int) Math.floor((time - WEEK_START_MS) / (double) WEEK_MS);
        }

        static long weekEnds(int week) {
            return (week + 1L) * WEEK_MS + WEEK_START_MS;
        }

        private static String base() {
            String address = DATABASE_URL.trim();
            while (address.endsWith("/")) address = address.substring(0, address.length() - 1);
            return address;
        }

        private static SharedPreferences store(Context context) {
            return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        }

        private static Context app(Context context) {
            Context app = context.getApplicationContext();
            return app != null ? app : context;
        }

        private static String text(SharedPreferences store, String key) {
            Object value = store.getAll().get(key);
            return value instanceof String ? (String) value : "";
        }

        private static int whole(Map<String, ?> all, String key) {
            Object value = all.get(key);
            return value instanceof Integer ? (Integer) value : 0;
        }

        private static String recordSeal(SharedPreferences store, int score, boolean classic, int seconds, int flaps) {
            return scoreSeal(score, "board|" + classic + "|" + seconds + "|" + flaps, installSalt(store));
        }

        static boolean recordIntact(SharedPreferences store) {
            Map<String, ?> all = store.getAll();
            if (!all.containsKey(KEY_BEST)) return true;
            Object seal = all.get(KEY_SEAL);
            return seal instanceof String && seal.equals(recordSeal(store, whole(all, KEY_BEST),
                    Boolean.TRUE.equals(all.get(KEY_CLASSIC)), whole(all, KEY_SECONDS), whole(all, KEY_FLAPS)));
        }

        static int best(SharedPreferences store) {
            int best = whole(store.getAll(), KEY_BEST);
            return best > 0 && best <= MAX_SCORE && recordIntact(store) ? best : 0;
        }

        static boolean bestIsClassic(SharedPreferences store) {
            return Boolean.TRUE.equals(store.getAll().get(KEY_CLASSIC));
        }

        static int posted(SharedPreferences store) {
            return whole(store.getAll(), KEY_POSTED);
        }

        static int weekPosted(SharedPreferences store) {
            Map<String, ?> all = store.getAll();
            return whole(all, KEY_WEEK) == week(now()) ? whole(all, KEY_WEEK_POSTED) : 0;
        }

        static int replayBest(SharedPreferences store) {
            return whole(store.getAll(), KEY_REPLAY_BEST);
        }

        static int replayWeekBest(SharedPreferences store) {
            Map<String, ?> all = store.getAll();
            return whole(all, KEY_REPLAY_WEEK) == week(now()) ? whole(all, KEY_REPLAY_WEEK_BEST) : 0;
        }

        static boolean wantsReplay(SharedPreferences store, int score) {
            return score > 0 && score <= MAX_SCORE && (score > replayBest(store) || score > replayWeekBest(store));
        }

        static String name(SharedPreferences store) {
            return text(store, KEY_NAME);
        }

        static String uid(SharedPreferences store) {
            return text(store, KEY_UID);
        }

        static boolean nameRefused(SharedPreferences store) {
            String name = name(store);
            return !name.isEmpty() && !nameAllowed(name);
        }

        static boolean nameTaken(SharedPreferences store) {
            String name = name(store);
            return Boolean.TRUE.equals(store.getAll().get(KEY_NAME_TAKEN)) || (!name.isEmpty() && !nameAllowed(name));
        }

        static String nameKey(String name) {
            return cleanName(name).toLowerCase(Locale.US).replace(' ', '_');
        }

        static boolean asked(SharedPreferences store) {
            return Boolean.TRUE.equals(store.getAll().get(KEY_ASKED));
        }

        static boolean offer(SharedPreferences store, int score, boolean classic, int seconds, int flaps) {
            if (score <= 0 || score > MAX_SCORE || score <= best(store)) return false;
            store.edit()
                    .putInt(KEY_BEST, score)
                    .putBoolean(KEY_CLASSIC, classic)
                    .putInt(KEY_SECONDS, seconds)
                    .putInt(KEY_FLAPS, flaps)
                    .putString(KEY_SEAL, recordSeal(store, score, classic, seconds, flaps))
                    .apply();
            return true;
        }

        static String cleanName(String raw) {
            if (raw == null) return "";
            StringBuilder out = new StringBuilder();
            for (int i = 0; i < raw.length() && out.length() < NAME_MAX; i++) {
                char c = raw.charAt(i);
                boolean letter = (c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z') || (c >= '0' && c <= '9') || c == '_';
                if (letter) out.append(c);
                else if (c == ' ' && out.length() > 0 && out.charAt(out.length() - 1) != ' ') out.append(c);
            }
            return out.toString().trim();
        }

        private static synchronized Executor worker() {
            if (worker == null) {
                worker = Executors.newSingleThreadExecutor(task -> {
                    Thread thread = new Thread(task, "Leaderboard");
                    thread.setDaemon(true);
                    thread.setPriority(Thread.MIN_PRIORITY);
                    return thread;
                });
            }
            return worker;
        }

        private static String readText(InputStream stream) throws IOException {
            try (InputStream in = stream) {
                ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                byte[] buffer = new byte[4096];
                int count;
                while ((count = in.read(buffer)) != -1) {
                    if (bytes.size() + count > MAX_REPLY_BYTES) throw new IOException("Reply too large");
                    bytes.write(buffer, 0, count);
                }
                return bytes.toString("UTF-8");
            }
        }

        private static String request(String method, String address, String body, String type) throws IOException {
            HttpURLConnection connection = (HttpURLConnection) new URL(address).openConnection();
            try {
                boolean patch = "PATCH".equals(method);
                connection.setRequestMethod(patch ? "POST" : method);
                if (patch) connection.setRequestProperty("X-HTTP-Method-Override", "PATCH");
                connection.setConnectTimeout(TIMEOUT_MS);
                connection.setReadTimeout(TIMEOUT_MS);
                connection.setUseCaches(false);
                connection.setRequestProperty("Accept", "application/json");
                if (body != null) {
                    connection.setDoOutput(true);
                    connection.setRequestProperty("Content-Type", type);
                    try (OutputStream out = connection.getOutputStream()) {
                        out.write(body.getBytes("UTF-8"));
                    }
                }
                int code = connection.getResponseCode();
                long date = connection.getDate();
                if (date > 0 && address.startsWith(base())) serverOffset = date - System.currentTimeMillis();
                InputStream stream = code >= 400 ? connection.getErrorStream() : connection.getInputStream();
                String reply = stream == null ? "" : readText(stream);
                if (code >= 400) throw new HttpError(code, reply);
                return reply;
            } finally {
                connection.disconnect();
            }
        }

        private static String string(Object value) {
            return value instanceof String ? (String) value : "";
        }

        private static long number(Object value) {
            if (value instanceof Number) return ((Number) value).longValue();
            if (value instanceof String) {
                try {
                    return Long.parseLong((String) value);
                } catch (NumberFormatException e) {
                    return 0;
                }
            }
            return 0;
        }

        private static JSONObject parse(String reply) throws JSONException {
            String trimmed = reply == null ? "" : reply.trim();
            return trimmed.isEmpty() || trimmed.equals("null") ? null : new JSONObject(trimmed);
        }

        private static synchronized String token(SharedPreferences store, boolean fresh) throws IOException, JSONException {
            long now = System.nanoTime() / 1000000L;
            if (!fresh && idToken != null && now < idTokenUntil && !uid(store).isEmpty()) return idToken;
            String key = URLEncoder.encode(API_KEY.trim(), "UTF-8");
            String refresh = text(store, KEY_REFRESH);
            String token = "";
            String newRefresh = "";
            String user = "";
            long seconds = 0;
            if (!refresh.isEmpty()) {
                try {
                    JSONObject reply = parse(request("POST", TOKEN_URL + "?key=" + key,
                            "grant_type=refresh_token&refresh_token=" + URLEncoder.encode(refresh, "UTF-8"), FORM_TYPE));
                    if (reply != null) {
                        token = string(reply.opt("id_token"));
                        newRefresh = string(reply.opt("refresh_token"));
                        user = string(reply.opt("user_id"));
                        seconds = number(reply.opt("expires_in"));
                    }
                } catch (HttpError e) {
                    if (e.code != 400 && e.code != 401 && e.code != 403) throw e;
                }
            }
            if (token.isEmpty() || user.isEmpty()) {
                JSONObject reply = parse(request("POST", SIGN_UP_URL + "?key=" + key, "{\"returnSecureToken\":true}", JSON_TYPE));
                if (reply == null) throw new IOException("Empty sign-in reply");
                token = string(reply.opt("idToken"));
                newRefresh = string(reply.opt("refreshToken"));
                user = string(reply.opt("localId"));
                seconds = number(reply.opt("expiresIn"));
                if (token.isEmpty() || user.isEmpty()) throw new IOException("Sign-in reply had no token");
            }
            SharedPreferences.Editor editor = store.edit().putString(KEY_UID, user);
            if (!newRefresh.isEmpty()) editor.putString(KEY_REFRESH, newRefresh);
            if (!user.equals(uid(store))) editor.putInt(KEY_POSTED, 0).putInt(KEY_WEEK_POSTED, 0).putInt(KEY_REPLAY_BEST, 0)
                    .putInt(KEY_REPLAY_WEEK_BEST, 0).remove(KEY_NAME_SENT).remove(KEY_NAME_CLAIMED);
            editor.apply();
            idToken = token;
            idTokenUntil = now + Math.max(60L, seconds - 120L) * 1000L;
            return token;
        }

        private static String path(String branch, String user, String token) throws IOException {
            return base() + "/" + branch + "/" + URLEncoder.encode(user, "UTF-8") + ".json?auth=" + URLEncoder.encode(token, "UTF-8");
        }

        private static JSONObject serverTime() throws JSONException {
            return new JSONObject().put(".sv", "timestamp");
        }

        private static JSONObject scoreEntry(String name, int score, boolean classic, int seconds, int flaps, int paused, int version, long time)
                throws JSONException {
            JSONObject entry = new JSONObject();
            entry.put("n", name);
            entry.put("s", score);
            entry.put("c", classic);
            entry.put("d", seconds);
            entry.put("f", flaps);
            entry.put("p", paused);
            entry.put("v", version);
            if (!appVersionName.isEmpty()) entry.put("vn", appVersionName);
            if (time > 0) entry.put("t", time);
            else entry.put("t", serverTime());
            return entry;
        }

        private static JSONObject copyWithName(JSONObject existing, String name) throws JSONException {
            JSONObject entry = new JSONObject();
            java.util.Iterator<String> keys = existing.keys();
            while (keys.hasNext()) {
                String key = keys.next();
                entry.put(key, existing.opt(key));
            }
            entry.put("n", name);
            return entry;
        }

        static String versionName(Context context) {
            try {
                String name = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
                String clean = name == null ? "" : name.trim();
                if (clean.startsWith("v") || clean.startsWith("V")) clean = clean.substring(1);
                return clean.matches("[0-9][0-9A-Za-z._-]{0,15}") ? clean : "";
            } catch (Exception e) {
                return "";
            }
        }

        private static int versionCode(Context context) {
            try {
                return context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionCode;
            } catch (Exception e) {
                return 0;
            }
        }

        private static String nameOwner(String key) throws IOException {
            String reply = request("GET", base() + "/names/" + URLEncoder.encode(key, "UTF-8") + ".json", null, null).trim();
            if (reply.isEmpty() || reply.equals("null")) return null;
            return reply.startsWith("\"") && reply.endsWith("\"") && reply.length() >= 2 ? reply.substring(1, reply.length() - 1) : reply;
        }

        private static void claimName(SharedPreferences store, String token, String name) throws IOException, NameTaken {
            String key = nameKey(name);
            String user = uid(store);
            String owner = nameOwner(key);
            if (owner != null && !owner.equals(user)) throw new NameTaken(name);
            if (owner == null) {
                try {
                    request("PUT", path("names", key, token), JSONObject.quote(user), JSON_TYPE);
                } catch (HttpError e) {
                    if (e.code != 401 && e.code != 403) throw e;
                    String now = nameOwner(key);
                    if (now != null && !now.equals(user)) throw new NameTaken(name);
                    throw e;
                }
            }
        }

        private static JSONObject ensureEntry(Context context, SharedPreferences store, String token) throws IOException, JSONException, NameTaken {
            return ensureEntry(context, store, token, name(store));
        }

        private static JSONObject ensureEntry(Context context, SharedPreferences store, String token, String name)
                throws IOException, JSONException, NameTaken {
            String user = uid(store);
            String key = nameKey(name);
            String claimed = text(store, KEY_NAME_CLAIMED);
            try {
                claimName(store, token, name);
            } catch (NameTaken taken) {
                if (name.equals(name(store))) store.edit().putBoolean(KEY_NAME_TAKEN, true).apply();
                throw taken;
            }
            JSONObject existing = parse(request("GET", path("scores", user, token), null, null));
            if (existing == null) {
                JSONObject fresh = scoreEntry(name, 0, false, 0, 0, 0, versionCode(context), 0);
                request("PUT", path("scores", user, token), fresh.toString(), JSON_TYPE);
                existing = parse(request("GET", path("scores", user, token), null, null));
                if (existing == null) throw new IOException("Entry was not saved");
            } else if (!name.equals(string(existing.opt("n")))) {
                existing = copyWithName(existing, name);
                request("PUT", path("scores", user, token), existing.toString(), JSON_TYPE);
                JSONObject weekly = parse(request("GET", path("weekly", user, token), null, null));
                if (weekly != null && number(weekly.opt("w")) == week(now()) && !name.equals(string(weekly.opt("n")))) {
                    request("PUT", path("weekly", user, token), copyWithName(weekly, name).toString(), JSON_TYPE);
                }
                for (String branch : new String[] {"vscores", "vweekly"}) {
                    try {
                        request("PUT", base() + "/" + branch + "/" + URLEncoder.encode(user, "UTF-8") + "/n.json?auth="
                                + URLEncoder.encode(token, "UTF-8"), JSONObject.quote(name), JSON_TYPE);
                    } catch (HttpError ignored) {
                    }
                }
            }
            if (!claimed.isEmpty() && !claimed.equals(key)) {
                try {
                    request("DELETE", path("names", claimed, token), null, null);
                } catch (IOException ignored) {
                }
            }
            store.edit().putString(KEY_NAME_SENT, name).putString(KEY_NAME_CLAIMED, key).putString(KEY_NAME, name)
                    .putBoolean(KEY_NAME_TAKEN, false).putInt(KEY_POSTED, (int) number(existing.opt("s"))).apply();
            return existing;
        }

        private static String describe(Exception error) {
            if (error instanceof HttpError) {
                HttpError http = (HttpError) error;
                if (http.code == 401 || http.code == 403) return "The leaderboard refused the request";
                if (http.code == 400) return "The leaderboard is not set up correctly";
                return "Leaderboard error " + http.code;
            }
            if (error instanceof IOException) return NO_CONNECTION;
            return "The leaderboard sent something unexpected";
        }

        private static void log(Context context, String message) {
            ActivityLog.add(context, "BOARD", message);
        }

        private static <T> void finish(Poster poster, final Callback<T> callback, final T value, final String error) {
            if (callback == null) return;
            Runnable task = () -> callback.done(value, error);
            if (poster != null) poster.post(task);
            else task.run();
        }

        private static boolean ready(SharedPreferences store) {
            return configured() && !name(store).isEmpty();
        }

        static void setName(final Context context, String rawName, final Poster poster, final Callback<String> callback) {
            final String name = cleanName(rawName);
            if (name.isEmpty()) {
                finish(poster, callback, null, "Use at least one letter or number");
                return;
            }
            if (!nameAllowed(name)) {
                finish(poster, callback, null, NAME_REFUSED);
                return;
            }
            final SharedPreferences store = store(context);
            final Context app = app(context);
            if (!configured()) {
                store.edit().putString(KEY_NAME, name).putBoolean(KEY_ASKED, true).putBoolean(KEY_NAME_TAKEN, false).apply();
                log(app, "Leaderboard name set to \"" + name + "\"");
                finish(poster, callback, CURRENT, null);
                return;
            }
            worker().execute(() -> {
                try {
                    ensureEntry(app, store, token(store, false), name);
                    store.edit().putBoolean(KEY_ASKED, true).apply();
                    log(app, "Leaderboard name set to \"" + name + "\"");
                    finish(poster, callback, CURRENT, null);
                } catch (NameTaken e) {
                    log(app, "Leaderboard name \"" + name + "\" is taken");
                    finish(poster, callback, null, NAME_TAKEN);
                } catch (HttpError e) {
                    log(app, "Saving the leaderboard name failed: " + e);
                    finish(poster, callback, null, describe(e));
                } catch (IOException | JSONException | RuntimeException e) {
                    store.edit().putString(KEY_NAME, name).putBoolean(KEY_ASKED, true).putBoolean(KEY_NAME_TAKEN, false).apply();
                    log(app, "Leaderboard name \"" + name + "\" saved on this device; it will be checked when you're online (" + e + ")");
                    finish(poster, callback, OFFLINE, null);
                }
            });
        }

        static void join(Context context, String rawName, Poster poster, Callback<String> callback) {
            setName(context, rawName, poster, callback);
        }

        static void markAsked(Context context) {
            store(context).edit().putBoolean(KEY_ASKED, true).apply();
        }

        private static final Set<Long> brokenSeeds = new LinkedHashSet<>();
        private static final java.util.concurrent.atomic.AtomicInteger heartbeatsWaiting = new java.util.concurrent.atomic.AtomicInteger();

        static boolean heartbeatBusy() {
            return heartbeatsWaiting.get() > 0;
        }

        private static void breakRound(long seed) {
            synchronized (brokenSeeds) {
                brokenSeeds.add(seed);
                if (brokenSeeds.size() > 16) {
                    Iterator<Long> oldest = brokenSeeds.iterator();
                    oldest.next();
                    oldest.remove();
                }
            }
        }

        static boolean roundBroken(long seed) {
            synchronized (brokenSeeds) {
                return brokenSeeds.contains(seed);
            }
        }

        static void heartbeat(final Context context, final long seed, final int index, final int kind, final String code) {
            final SharedPreferences store = store(context);
            if (!ready(store)) {
                breakRound(seed);
                return;
            }
            heartbeatsWaiting.incrementAndGet();
            worker().execute(() -> {
                try {
                    if (roundBroken(seed)) return;
                    JSONObject mark = new JSONObject().put("c", code).put("m", kind).put("t", serverTime());
                    String body = new JSONObject().put("h/" + index, mark).toString();
                    String token = token(store, false);
                    String place = base() + "/sessions/" + URLEncoder.encode(uid(store), "UTF-8") + ".json?print=silent&auth=";
                    try {
                        request("PATCH", place + URLEncoder.encode(token, "UTF-8"), body, JSON_TYPE);
                    } catch (HttpError e) {
                        if (e.code != 401) throw e;
                        token = token(store, true);
                        request("PATCH", place + URLEncoder.encode(token, "UTF-8"), body, JSON_TYPE);
                    }
                } catch (IOException | JSONException | RuntimeException e) {
                    breakRound(seed);
                } finally {
                    heartbeatsWaiting.decrementAndGet();
                }
            });
        }

        static void startSession(final Context context, final long seed) {
            final SharedPreferences store = store(context);
            if (!ready(store)) {
                breakRound(seed);
                return;
            }
            final Context app = app(context);
            worker().execute(() -> {
                try {
                    String token = token(store, false);
                    if (!name(store).equals(text(store, KEY_NAME_SENT))) ensureEntry(app, store, token);
                    String session = new JSONObject().put("t", serverTime()).put("r", Long.toString(seed)).toString();
                    try {
                        request("PUT", path("sessions", uid(store), token) + "&print=silent", session, JSON_TYPE);
                    } catch (HttpError e) {
                        if (e.code != 401) throw e;
                        token = token(store, true);
                        request("PUT", path("sessions", uid(store), token) + "&print=silent", session, JSON_TYPE);
                    }
                } catch (NameTaken e) {
                    breakRound(seed);
                    log(app, "Leaderboard round not started: " + e.getMessage());
                } catch (IOException | JSONException | RuntimeException e) {
                    breakRound(seed);
                    if (e instanceof HttpError) log(app, "Starting a leaderboard round failed: " + e);
                }
            });
        }

        static void submit(final Context context, final int score, final boolean classic, final int seconds, final int flaps,
                final int paused, final Replay replay, final Poster poster, final Callback<String> callback) {
            final SharedPreferences store = store(context);
            boolean wanted = replay != null && wantsReplay(store, score);
            if (!ready(store) || score <= 0 || score > MAX_SCORE || (score <= posted(store) && score <= weekPosted(store) && !wanted)) {
                finish(poster, callback, CURRENT, null);
                return;
            }
            final Context app = app(context);
            worker().execute(() -> {
                IOException lastError = null;
                for (int attempt = 0; attempt < 3; attempt++) {
                    if (attempt > 0) {
                        try {
                            Thread.sleep(retryDelayMs * attempt);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            break;
                        }
                    }
                    try {
                        String status = post(app, store, score, classic, seconds, flaps, paused, replay);
                        finish(poster, callback, status, null);
                        return;
                    } catch (NameTaken e) {
                        log(app, "Score " + score + " was not posted: " + e.getMessage());
                        finish(poster, callback, REFUSED, NAME_TAKEN);
                        return;
                    } catch (HttpError e) {
                        String why = e.code == 401 || e.code == 403 ? "the server refused it" : "error " + e.code;
                        if (e.code == 401 || e.code == 403) store.edit().remove(KEY_NAME_SENT).remove(KEY_NAME_CLAIMED).apply();
                        log(app, "Score " + score + " was not posted: " + why + " (" + e.getMessage() + ")");
                        finish(poster, callback, REFUSED, describe(e));
                        return;
                    } catch (IOException e) {
                        lastError = e;
                    } catch (JSONException | RuntimeException e) {
                        log(app, "Posting score " + score + " failed: " + e);
                        finish(poster, callback, null, describe(e));
                        return;
                    }
                }
                log(app, "Score " + score + " could not be posted, no connection" + (lastError != null ? " (" + lastError.getMessage() + ")" : ""));
                finish(poster, callback, null, NO_CONNECTION);
            });
        }

        private static String post(Context app, SharedPreferences store, int score, boolean classic, int seconds, int flaps, int paused,
                Replay replay) throws IOException, JSONException, NameTaken {
            String token = token(store, false);
            String user = uid(store);
            JSONObject current = ensureEntry(app, store, token);
            JSONObject weekly = parse(request("GET", path("weekly", user, token), null, null));
            int thisWeek = week(now());
            int allTime = (int) number(current.opt("s"));
            int weekScore = weekly != null && number(weekly.opt("w")) == thisWeek ? (int) number(weekly.opt("s")) : 0;
            store.edit().putInt(KEY_WEEK, thisWeek).putInt(KEY_WEEK_POSTED, weekScore).apply();
            JSONObject update = new JSONObject();
            int version = versionCode(app);
            String name = name(store);
            if (score > allTime) {
                update.put("scores/" + user, scoreEntry(name, score, classic, seconds, flaps, paused, version, 0));
            }
            if (score > weekScore) {
                JSONObject entry = scoreEntry(name, score, classic, seconds, flaps, paused, version, 0);
                entry.put("w", thisWeek);
                entry.put("k", thisWeek * WEEK_KEY + score);
                update.put("weekly/" + user, entry);
            }
            String status = CURRENT;
            if (update.length() > 0) {
                request("PATCH", base() + "/.json?print=silent&auth=" + URLEncoder.encode(token, "UTF-8"), update.toString(), JSON_TYPE);
                SharedPreferences.Editor editor = store.edit().putInt(KEY_WEEK, thisWeek).putInt(KEY_WEEK_POSTED, Math.max(score, weekScore));
                if (score > allTime) editor.putInt(KEY_POSTED, score);
                editor.apply();
                log(app, "Score " + score + " posted to the " + (score > allTime ? "all-time and weekly" : "weekly") + " leaderboard"
                        + (classic ? " (Classic)" : ""));
                status = POSTED;
            }
            if (replay != null && wantsReplay(store, score) && sendReplay(app, store, token, score, classic, replay, version)) {
                status = VERIFYING;
            }
            return status;
        }

        private static boolean sendReplay(Context app, SharedPreferences store, String token, int score, boolean classic, Replay replay,
                int version) throws IOException, JSONException {
            String user = uid(store);
            JSONObject session = parse(request("GET", path("sessions", user, token), null, null));
            String seed = Long.toString(replay.seed);
            if (session == null || !seed.equals(string(session.opt("r"))) || number(session.opt("t")) <= 0) {
                log(app, "Run of " + score + " was not sent for verification: its round was not registered with the leaderboard");
                return false;
            }
            JSONObject stamps = new JSONObject();
            Object marks = session.opt("h");
            for (int i = 0; i < replay.marks; i++) {
                Object one = marks instanceof JSONArray ? ((JSONArray) marks).opt(i)
                        : marks instanceof JSONObject ? ((JSONObject) marks).opt(String.valueOf(i)) : null;
                if (!(one instanceof JSONObject) || roundBroken(replay.seed)) {
                    log(app, "Run of " + score + " was not sent for verification: the connection dropped during the round, so its timing can't be confirmed");
                    return false;
                }
                JSONObject copy = new JSONObject();
                copy.put("c", ((JSONObject) one).opt("c"));
                copy.put("m", ((JSONObject) one).opt("m"));
                copy.put("t", ((JSONObject) one).opt("t"));
                stamps.put(String.valueOf(i), copy);
            }
            if (replay.marks == 0) {
                log(app, "Run of " + score + " was not sent for verification: it has no timing marks");
                return false;
            }
            String data = replay.encode();
            if (data.length() > REPLAY_TEXT_MAX) {
                log(app, "Run of " + score + " was too long to send for verification");
                return false;
            }
            int thisWeek = week(now());
            boolean allTime = score > replayBest(store);
            JSONObject entry = new JSONObject();
            entry.put("s", score);
            entry.put("c", classic);
            entry.put("r", seed);
            entry.put("st", number(session.opt("t")));
            entry.put("t", serverTime());
            entry.put("w", thisWeek);
            entry.put("v", version);
            if (!appVersionName.isEmpty()) entry.put("vn", appVersionName);
            entry.put("rv", replay.rules);
            entry.put("h", stamps);
            entry.put("d", data);
            String slot = allTime ? "a" : "w";
            String place = base() + "/replays/" + URLEncoder.encode(user, "UTF-8") + "/" + slot;
            String auth = "auth=" + URLEncoder.encode(token, "UTF-8");
            long started = number(session.opt("t"));
            try {
                request("PUT", place + ".json?print=silent&" + auth, entry.toString(), JSON_TYPE);
            } catch (HttpError e) {
                if (e.code != 401 && e.code != 403) throw e;
                if (!String.valueOf(started).equals(request("GET", place + "/st.json?" + auth, null, null).trim())) {
                    log(app, "Run of " + score + " was not accepted for verification (" + e.getMessage() + ")");
                    return false;
                }
            }
            try {
                request("PUT", base() + "/ping.json?print=silent&" + auth, serverTime().toString(), JSON_TYPE);
            } catch (IOException ignored) {
            }
            SharedPreferences.Editor editor = store.edit().putInt(KEY_REPLAY_WEEK, thisWeek)
                    .putInt(KEY_REPLAY_WEEK_BEST, Math.max(score, replayWeekBest(store)));
            if (allTime) editor.putInt(KEY_REPLAY_BEST, score);
            editor.apply();
            log(app, "Run of " + score + " sent for verification (" + (data.length() / 1024 + 1) + " KB)");
            return true;
        }

        private static Entry entry(String key, JSONObject raw, boolean verified) {
            Entry entry = new Entry();
            entry.uid = key;
            entry.name = cleanName(string(raw.opt("n")));
            if (!entry.name.isEmpty() && !nameAllowed(entry.name)) entry.name = "Player";
            entry.score = (int) Math.max(0L, Math.min(MAX_SCORE, number(raw.opt("s"))));
            entry.classic = Boolean.TRUE.equals(raw.opt("c"));
            entry.time = number(raw.opt("t"));
            entry.verified = verified || (entry.score > 0 && raw.has("vs") && number(raw.opt("vs")) == entry.score);
            String version = string(raw.opt("vn"));
            entry.version = version.matches("[0-9][0-9A-Za-z._-]{0,15}") ? version : "";
            return entry;
        }

        private static List<Entry> list(JSONObject reply, boolean verified) {
            List<Entry> entries = new ArrayList<>();
            if (reply == null) return entries;
            java.util.Iterator<String> keys = reply.keys();
            while (keys.hasNext()) {
                String key = keys.next();
                JSONObject raw = reply.optJSONObject(key);
                if (raw == null) continue;
                Entry entry = entry(key, raw, verified);
                if (entry.score > 0 && !entry.name.isEmpty()) entries.add(entry);
            }
            java.util.Collections.sort(entries, (a, b) -> a.score != b.score ? Integer.compare(b.score, a.score) : Long.compare(a.time, b.time));
            return entries;
        }

        private static boolean listed(List<Entry> entries, String user) {
            for (Entry entry : entries) if (entry.uid.equals(user)) return true;
            return false;
        }

        private static synchronized ExecutorService fetchPool() {
            if (fetchPool == null) {
                fetchPool = Executors.newFixedThreadPool(4, task -> {
                    Thread thread = new Thread(task, "LeaderboardFetch");
                    thread.setDaemon(true);
                    return thread;
                });
            }
            return fetchPool;
        }

        private static Future<String> later(final String address) {
            return fetchPool().submit(() -> request("GET", address, null, null));
        }

        private static JSONObject result(Future<String> reply) throws IOException, JSONException {
            if (reply == null) return null;
            try {
                return parse(reply.get());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IOException("Interrupted");
            } catch (ExecutionException e) {
                Throwable cause = e.getCause();
                if (cause instanceof IOException) throw (IOException) cause;
                if (cause instanceof JSONException) throw (JSONException) cause;
                if (cause instanceof RuntimeException) throw (RuntimeException) cause;
                throw new IOException(String.valueOf(cause));
            }
        }

        private static JSONObject quiet(Future<String> reply) {
            try {
                return result(reply);
            } catch (IOException | JSONException | RuntimeException e) {
                return null;
            }
        }

        static int rankAbove(JSONObject counts) {
            if (counts == null || !counts.has(COUNTS_DONE)) return 0;
            long above = 0L;
            java.util.Iterator<String> keys = counts.keys();
            while (keys.hasNext()) {
                String key = keys.next();
                if (key.startsWith("s")) above += Math.max(0L, number(counts.opt(key)));
            }
            return (int) Math.min(Integer.MAX_VALUE - 1L, above + 1L);
        }

        static String countKey(int score) {
            return "s" + String.format(java.util.Locale.US, "%04d", Math.max(0, Math.min(MAX_SCORE, score)));
        }

        private static Future<String> countsAbove(String branch, int thisWeek, int score) {
            String place = branch.equals("weekly") || branch.equals("vweekly") ? branch + "/" + thisWeek : branch;
            return later(base() + "/counts/" + place + ".json?orderBy=%22$key%22&startAt=%22" + countKey(score + 1) + "%22");
        }

        static int boardSize(int choice) {
            if (choice < 0) return BOARD_SIZES[DEFAULT_BOARD_SIZE];
            return BOARD_SIZES[Math.min(choice, BOARD_SIZES.length - 1)];
        }

        static void fetch(final Context context, final int limit, final int view, final Poster poster, final Callback<Board> callback) {
            if (!configured()) {
                finish(poster, callback, null, "The leaderboard is not set up in this build");
                return;
            }
            final SharedPreferences store = store(context);
            final Context app = app(context);
            appVersionName = versionName(app);
            worker().execute(() -> {
                try {
                    Board board = new Board();
                    int thisWeek = week(now());
                    long first = thisWeek * WEEK_KEY + 1;
                    long last = thisWeek * WEEK_KEY + MAX_SCORE;
                    int size = Math.max(1, Math.min(MAX_BOARD_SIZE, limit));
                    boolean weeklyView = view == VIEW_WEEKLY || view == VIEW_VERIFIED_WEEKLY;
                    boolean verifiedView = view == VIEW_VERIFIED_ALL_TIME || view == VIEW_VERIFIED_WEEKLY;
                    String branch = VIEW_BRANCHES[view];
                    String range = weeklyView ? "?orderBy=%22k%22&startAt=" + first + "&endAt=" + last + "&limitToLast=" + size
                            : "?orderBy=%22s%22&startAt=1&limitToLast=" + size;
                    Future<String> top = later(base() + "/" + branch + ".json" + range);
                    board.weekEnds = weekEnds(thisWeek);
                    String user = uid(store);
                    Future<String> mine = null, otherMine = null, verdict = null, pending = null, verifiedAll = null, verifiedWeek = null;
                    if (!user.isEmpty()) {
                        String encoded = URLEncoder.encode(user, "UTF-8");
                        String token = null;
                        try {
                            token = token(store, false);
                        } catch (IOException | JSONException e) {
                            token = null;
                        }
                        String auth = token == null ? "" : "?auth=" + URLEncoder.encode(token, "UTF-8");
                        mine = later(base() + "/" + branch + "/" + encoded + ".json" + auth);
                        if (verifiedView && token != null) {
                            otherMine = later(base() + "/" + (weeklyView ? "vscores" : "vweekly") + "/" + encoded + ".json" + auth);
                            verdict = later(path("verdicts", user, token));
                        }
                        if (token != null && (verifiedView || replayBest(store) > 0 || replayWeekBest(store) > 0)) {
                            pending = later(base() + "/replays/" + encoded + ".json?shallow=true&auth=" + URLEncoder.encode(token, "UTF-8"));
                            if (!verifiedView) {
                                verifiedAll = later(base() + "/vscores/" + encoded + ".json" + auth);
                                verifiedWeek = later(base() + "/vweekly/" + encoded + ".json" + auth);
                            }
                        }
                    }
                    List<Entry> entries = list(result(top), verifiedView);
                    if (entries.size() > size) entries = new ArrayList<>(entries.subList(0, size));
                    Entry own = null;
                    boolean ownKnown = true;
                    JSONObject raw;
                    try {
                        raw = result(mine);
                    } catch (IOException | JSONException | RuntimeException e) {
                        if (!verifiedView) throw e;
                        raw = null;
                        ownKnown = false;
                    }
                    if (raw != null && (!weeklyView || number(raw.opt("w")) == thisWeek)) own = entry(user, raw, verifiedView);
                    if (!verifiedView && !weeklyView && mine != null && raw == null && ownKnown) board.mineMissing = true;
                    Future<String> above = null;
                    if (own != null && own.score > 0 && !listed(entries, user)) above = countsAbove(branch, thisWeek, own.score);
                    Entry otherOwn = null;
                    boolean otherKnown = otherMine != null;
                    if (otherMine != null) {
                        try {
                            JSONObject other = result(otherMine);
                            if (other != null && (weeklyView || number(other.opt("w")) == thisWeek)) otherOwn = entry(user, other, true);
                        } catch (IOException | JSONException | RuntimeException e) {
                            otherKnown = false;
                        }
                    }
                    JSONObject judged = quiet(verdict);
                    if (verdict != null) {
                        board.verdictKnown = true;
                        if (judged != null) {
                            board.verdictScore = (int) number(judged.opt("s"));
                            board.verdictOk = Boolean.TRUE.equals(judged.opt("ok"));
                            board.verdictWhy = string(judged.opt("why"));
                            board.verdictTime = number(judged.opt("t"));
                        }
                    }
                    if (pending != null) {
                        boolean pendingKnown = true;
                        boolean waiting = false;
                        try {
                            waiting = result(pending) != null;
                        } catch (IOException | JSONException | RuntimeException e) {
                            pendingKnown = false;
                        }
                        if (verifiedView) board.verifying = waiting;
                        Entry allTimeOwn = weeklyView ? otherOwn : own;
                        Entry weekOwn = weeklyView ? own : otherOwn;
                        boolean known = verifiedView ? ownKnown && otherKnown : true;
                        if (!verifiedView) {
                            try {
                                JSONObject best = result(verifiedAll);
                                allTimeOwn = best == null ? null : entry(user, best, true);
                                JSONObject week = result(verifiedWeek);
                                weekOwn = week != null && number(week.opt("w")) == thisWeek ? entry(user, week, true) : null;
                            } catch (IOException | JSONException | RuntimeException e) {
                                known = false;
                            }
                        }
                        if (pendingKnown && known && !waiting) {
                            int verifiedBest = allTimeOwn != null ? allTimeOwn.score : 0;
                            int verifiedWeekBest = weekOwn != null ? weekOwn.score : 0;
                            store.edit().putInt(KEY_REPLAY_BEST, verifiedBest).putInt(KEY_REPLAY_WEEK_BEST, verifiedWeekBest)
                                    .putInt(KEY_REPLAY_WEEK, thisWeek).apply();
                        }
                    }
                    int rank = 0;
                    if (above != null) {
                        try {
                            rank = rankAbove(result(above));
                        } catch (IOException | JSONException | RuntimeException e) {
                            rank = 0;
                        }
                    }
                    if (board.mineMissing && posted(store) > 0) {
                        store.edit().putInt(KEY_POSTED, 0).apply();
                        log(app, "Your leaderboard entry was cleared after a long break. Your next score will post again");
                    }
                    switch (view) {
                        case VIEW_ALL_TIME: board.allTime = entries; board.mineAllTime = own; board.rankAllTime = rank; break;
                        case VIEW_WEEKLY: board.weekly = entries; board.mineWeekly = own; board.rankWeekly = rank; break;
                        case VIEW_VERIFIED_ALL_TIME:
                            board.verifiedAllTime = entries; board.mineVerifiedAllTime = own; board.rankVerifiedAllTime = rank;
                            board.mineVerifiedWeekly = otherOwn;
                            break;
                        default:
                            board.verifiedWeekly = entries; board.mineVerifiedWeekly = own; board.rankVerifiedWeekly = rank;
                            board.mineVerifiedAllTime = otherOwn;
                            break;
                    }
                    board.loaded[view] = true;
                    board.loadedAt[view] = now();
                    finish(poster, callback, board, null);
                } catch (IOException | JSONException | RuntimeException e) {
                    log(app, "Loading the leaderboard failed: " + e);
                    finish(poster, callback, null, describe(e));
                }
            });
        }

        static void leave(final Context context, final Poster poster, final Callback<Boolean> callback) {
            final SharedPreferences store = store(context);
            final Context app = app(context);
            final Runnable forget = () -> {
                store.edit().remove(KEY_BEST).remove(KEY_CLASSIC).remove(KEY_SECONDS).remove(KEY_FLAPS).remove(KEY_SEAL)
                        .remove(KEY_POSTED).remove(KEY_WEEK_POSTED).remove(KEY_WEEK).remove(KEY_NAME).remove(KEY_NAME_SENT)
                        .remove(KEY_UID).remove(KEY_REFRESH).remove(KEY_NAME_CLAIMED).remove(KEY_NAME_TAKEN).remove(KEY_REPLAY_BEST)
                        .remove(KEY_REPLAY_WEEK_BEST).remove(KEY_REPLAY_WEEK).putBoolean(KEY_ASKED, true).apply();
                synchronized (Leaderboard.class) {
                    idToken = null;
                }
            };
            if (!configured() || uid(store).isEmpty()) {
                forget.run();
                finish(poster, callback, true, null);
                return;
            }
            worker().execute(() -> {
                try {
                    String token = token(store, false);
                    String user = uid(store);
                    for (String branch : new String[] {"replays", "verdicts", "vscores", "vweekly"}) {
                        try {
                            request("DELETE", path(branch, user, token), null, null);
                        } catch (HttpError ignored) {
                        }
                    }
                    request("DELETE", path("scores", user, token), null, null);
                    request("DELETE", path("weekly", user, token), null, null);
                    request("DELETE", path("sessions", user, token), null, null);
                    String claimed = text(store, KEY_NAME_CLAIMED);
                    if (!claimed.isEmpty()) request("DELETE", path("names", claimed, token), null, null);
                    try {
                        request("POST", DELETE_ACCOUNT_URL + "?key=" + URLEncoder.encode(API_KEY.trim(), "UTF-8"),
                                new JSONObject().put("idToken", token).toString(), JSON_TYPE);
                    } catch (IOException ignored) {
                    }
                    forget.run();
                    log(app, "Leaderboard entry removed");
                    finish(poster, callback, true, null);
                } catch (IOException | JSONException | RuntimeException e) {
                    log(app, "Removing the leaderboard entry failed: " + e);
                    finish(poster, callback, false, describe(e));
                }
            });
        }
    }

    public static boolean isUsableKey(int code) {
        if (code == KeyEvent.KEYCODE_UNKNOWN) return true;
        if (code < 0 || code > KeyEvent.getMaxKeyCode()) return false;
        switch (code) {
            case KeyEvent.KEYCODE_BACK:
            case KeyEvent.KEYCODE_HOME:
            case KeyEvent.KEYCODE_POWER:
            case KeyEvent.KEYCODE_APP_SWITCH:
            case KeyEvent.KEYCODE_VOLUME_UP:
            case KeyEvent.KEYCODE_VOLUME_DOWN:
            case KeyEvent.KEYCODE_VOLUME_MUTE:
            case KeyEvent.KEYCODE_SLEEP:
            case KeyEvent.KEYCODE_WAKEUP:
            case KeyEvent.KEYCODE_ASSIST:
            case KeyEvent.KEYCODE_VOICE_ASSIST:
            case KeyEvent.KEYCODE_ENDCALL:
            case KeyEvent.KEYCODE_CALL:
                return false;
            default:
                return true;
        }
    }

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
    public static final String KEY_EARNED_HIGH_SCORE = "earnedHighScore";
    public static final String KEY_HIGH_SCORE_ASSISTS = "highScoreAssists";
    public static final String KEY_HIGH_SCORE_SEAL = "highScoreSeal";
    public static final String KEY_INSTALL_SALT = "installSalt";
    private static final int REQUEST_EXPORT_LOG = 4103;
    private static final int LOG_VIEW_LINES = 500;
    private static final int LOG_SHARE_CHARS = 100000;
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
    private static final String PREF_TEXT_EDITOR_WARNED = "pref_text_editor_warned";
    private static final String PREF_TEXT_EDITOR_NOTES = "pref_text_editor_notes";
    private static final int MAX_TEXT_CHARS = 200000;
    private static final int MAX_TEXT_LINES = 5000;
    private static final int MAX_LINE_CHARS = 2000;
    private static final int MAX_LISTED_PROBLEMS = 15;
    private static final Pattern NUMBER_TEXT = Pattern.compile(
            "^\\+?(-?(?:[0-9]{1,9}(?:[.,][0-9]{0,6})?|[.,][0-9]{1,6}))\\s*(x|%|ms|s|sec|deg|degrees|points?|pts?)?$",
            Pattern.CASE_INSENSITIVE);
    private static final String TEXT_HEADER = "# Flappy Bird settings\n"
            + "# Change a value after the = sign, then tap Apply.\n"
            + "# Lines that start with # are notes and are ignored.\n"
            + "# Values outside the allowed range are moved back inside it.\n"
            + "# Type default as a value to go back to the default.\n"
            + "# Settings you delete keep their current value.\n";
    private static final String CLASSIC_SUMMARY = "Matches the flight, pipes and scoring of the 2013 Android game";
    private static final int TAB_COUNT = 15;
    private static final int CLASSIC_PAGE = 13;
    private static final int ABOUT_PAGE = 10;
    private static final int SCORING_PAGE = 11;
    private static final int LEADERBOARD_PAGE = 12;
    public static final String PROJECT_URL = "https://github.com/FlappyRecreator/FlappyB64bitAndroid";
    private static final String MADE_BY = "Breathe";
    public static final String APP_NAME = "Flappy Bird Recreation";
    private static final int SOUND_PAGE = 7;
    private static final int CONTROLS_PAGE = 6;
    private static final int REPLAYS_PAGE = 14;
    private static final int PRESETS_PAGE = 8;
    private static final int ALL_PAGE = 9;
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

    private static final int[] TAB_ORDER = {ALL_PAGE, CLASSIC_PAGE, 0, 1, 2, 3, SCORING_PAGE, 5, 4, SOUND_PAGE, CONTROLS_PAGE,
            REPLAYS_PAGE, LEADERBOARD_PAGE, PRESETS_PAGE, ABOUT_PAGE};
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
    private final List<TextView> intros = new ArrayList<>();
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
    private ToggleSetting keyboard;
    private final List<KeySetting> keySettings = new ArrayList<>();
    private final Map<View, Setting> rowSettings = new HashMap<>();
    private final List<Setting> twins = new ArrayList<>();
    private final Set<View> twinRows = new HashSet<>();
    private boolean buildingTwin;
    private final Map<ViewGroup, TextView> groupLabels = new HashMap<>();
    private final Map<ViewGroup, String> groupTitles = new HashMap<>();
    private final Set<View> dependencyHidden = new HashSet<>();
    private LinearLayout pageStack;
    private TextView titleView;
    private TextView noResults;
    private ImageView searchButton;
    private EditText searchField;
    private OnBackPressedCallback searchBack;
    private boolean searching;
    private String searchQuery = "";
    private Map<String, Setting> textNames;
    private List<HiddenValue> hiddenValues;
    private ToggleSetting activityLog;
    private final Map<String, String> loggedValues = new HashMap<>();
    private String changeSource;

    private final Runnable saveRunnable = () -> flushSave();
    private final Runnable hideCaptionRunnable = () -> caption.animate().alpha(0f).setDuration(250L).start();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        setTheme(R.style.Theme_Material3Expressive_Dark_NoActionBar);
        super.onCreate(savedInstanceState);
        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        adoptOldHighScore(prefs);
        Object logSetting = prefs.getAll().get(PREF_ACTIVITY_LOG_ENABLED);
        ActivityLog.setEnabled(!(logSetting instanceof Boolean) || (Boolean) logSetting);
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        density = metrics.density;

        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        setContentView(R.layout.settings);
        hideSystemBars();

        bindLayout();
        createPreview();
        buildPages();
        createSettings();
        buildClassicTwins();
        bindPresets();

        readAll(prefs.getAll());
        resolveAllKeyConflicts(null);
        for (Setting setting : settings) loggedValues.put(setting.key, setting.textValue());
        ActivityLog.add(this, "SETTINGS", "Settings opened");
        refreshBoardName();
        syncAllViews();
        refreshDependencies(false, true);
        refreshSlots();
        pushPreview();
        selectPage(savedInstanceState != null ? savedInstanceState.getInt(STATE_PAGE, ALL_PAGE) : ALL_PAGE, false);
        applyPaneLayout(metrics.widthPixels, metrics.heightPixels);

        if (!isDisclaimerShown()) showDisclaimerDialog();
    }

    @Override
    protected void onResume() {
        super.onResume();
        hideSystemBars();
        if (previewView != null) previewView.setHostResumed(true);
        if (currentPage == ABOUT_PAGE) refreshAbout();
        refreshBoardName();
        refreshReplayHero();
    }

    @Override
    protected void onPause() {
        super.onPause();
        flushSave();
        if (isFinishing()) ActivityLog.add(this, "SETTINGS", "Settings closed");
        if (previewView != null) previewView.setHostResumed(false);
    }

    @Override
    protected void onDestroy() {
        if (root != null) {
            root.removeCallbacks(saveRunnable);
            root.removeCallbacks(hideCaptionRunnable);
        }
        if (replayHeroWorker != null) replayHeroWorker.shutdownNow();
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
        styleRoundButton(back);
        back.setImageDrawable(new IconDrawable(IconDrawable.BACK, dp(22), dp(2.5f), COLOR_ON));
        back.setOnClickListener(v -> {
            if (searching) closeSearch();
            else finish();
        });
        createSearch(back);

        View badge = findViewById(R.id.preview_badge);
        badge.setBackground(rounded(COLOR_SKY, dp(12)));
        caption.setBackground(rounded(COLOR_CAPTION, dp(16)));

        TextView presetsTab = findViewById(R.id.tab_presets);
        for (int i = 0; i < TAB_COUNT; i++) {
            final int index = i;
            if (i == SOUND_PAGE) tabs[i] = createTab("Sound", presetsTab, presetsTab);
            else if (i == REPLAYS_PAGE) tabs[i] = createTab("Replays", presetsTab, presetsTab);
            else if (i == ALL_PAGE) tabs[i] = createTab("All", presetsTab, tabs[0]);
            else if (i == ABOUT_PAGE) tabs[i] = createTab("About", presetsTab, null);
            else if (i == SCORING_PAGE) tabs[i] = createTab("Scoring", presetsTab, tabs[4]);
            else if (i == LEADERBOARD_PAGE) tabs[i] = createTab("Leaderboard", presetsTab, presetsTab);
            else if (i == CLASSIC_PAGE) tabs[i] = createTab("Classic", presetsTab, presetsTab);
            else tabs[i] = findViewById(TAB_IDS[i < SOUND_PAGE ? i : i - 1]);
            tabStyles[i] = new ChipStyle(tabs[i], COLOR_ROW, COLOR_PRIMARY, COLOR_ON_VARIANT, COLOR_ON_PRIMARY,
                    dp(12), dp(20), 0, 0);
            tabs[i].setOnClickListener(v -> {
                if (searching) closeSearch();
                if (currentPage != index) feel().play(Feel.TICK);
                selectPage(index, true);
            });
        }

        tabs[4].setText("Screen");
        tabs[CONTROLS_PAGE].setText("Controls");
        ViewGroup bar = (ViewGroup) presetsTab.getParent();
        for (int index : TAB_ORDER) bar.removeView(tabs[index]);
        for (int index : TAB_ORDER) bar.addView(tabs[index]);
        for (int i = 0; i < TAB_ORDER.length; i++) {
            final int index = TAB_ORDER[i];
            tabs[index].setFocusable(true);
            tabs[index].setContentDescription(tabs[index].getText() + " settings");
        }
        categoryScroll.setOnGenericMotionListener((view, event) -> {
            if (event.getActionMasked() != MotionEvent.ACTION_SCROLL) return false;
            float amount = event.getAxisValue(MotionEvent.AXIS_HSCROLL) - event.getAxisValue(MotionEvent.AXIS_VSCROLL);
            if (amount == 0f) return false;
            categoryScroll.smoothScrollBy(Math.round(amount * dp(48)), 0);
            return true;
        });

        ViewCompat.setOnApplyWindowInsetsListener(root, (view, windowInsets) -> {
            Insets safe = windowInsets.getInsets(
                    WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.displayCutout()
                            | WindowInsetsCompat.Type.ime());
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

    private void styleRoundButton(ImageView button) {
        GradientDrawable shape = new GradientDrawable();
        shape.setShape(GradientDrawable.OVAL);
        shape.setColor(COLOR_HIGH);
        button.setBackground(new RippleDrawable(ColorStateList.valueOf(COLOR_RIPPLE), shape, null));
    }

    private void createSearch(ImageView back) {
        titleView = findViewById(R.id.tv_settings_title);
        LinearLayout header = (LinearLayout) back.getParent();

        searchField = dialogInput("", InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS, "Search settings");
        searchField.setBackground(null);
        searchField.setTextColor(COLOR_ON);
        searchField.setHintTextColor(COLOR_ON_VARIANT);
        searchField.setTextSize(18);
        searchField.setSelectAllOnFocus(false);
        searchField.setPadding(0, 0, 0, 0);
        searchField.setImeOptions(EditorInfo.IME_ACTION_SEARCH | EditorInfo.IME_FLAG_NO_EXTRACT_UI);
        searchField.setFilters(new InputFilter[]{new InputFilter.LengthFilter(60)});
        searchField.setVisibility(View.GONE);
        LinearLayout.LayoutParams fieldParams = new LinearLayout.LayoutParams(0, WRAP, 1f);
        fieldParams.setMarginStart(dp(14));
        header.addView(searchField, header.indexOfChild(titleView) + 1, fieldParams);
        searchField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence text, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence text, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable text) {
                if (searching) applySearch(text.toString(), true);
            }
        });
        searchField.setOnEditorActionListener((view, actionId, event) -> {
            hideKeyboard();
            return true;
        });

        searchButton = new ImageView(this);
        searchButton.setScaleType(ImageView.ScaleType.CENTER);
        styleRoundButton(searchButton);
        searchButton.setImageDrawable(new IconDrawable(IconDrawable.SEARCH, dp(22), dp(2.5f), COLOR_ON));
        searchButton.setContentDescription("Search settings");
        LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(dp(44), dp(44));
        buttonParams.setMarginStart(dp(10));
        header.addView(searchButton, buttonParams);
        searchButton.setOnClickListener(v -> {
            if (searching) closeSearch();
            else openSearch();
        });

        searchBack = new OnBackPressedCallback(false) {
            @Override
            public void handleOnBackPressed() {
                closeSearch();
            }
        };
        getOnBackPressedDispatcher().addCallback(this, searchBack);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN && shortcut(event)) return true;
        return super.dispatchKeyEvent(event);
    }

    private boolean shortcut(KeyEvent event) {
        if (tabs[0] == null || scrollView == null) return false;
        int code = event.getKeyCode();
        boolean ctrl = event.isCtrlPressed();
        View focus = getCurrentFocus();
        boolean typing = focus instanceof EditText && focus.getVisibility() == View.VISIBLE;
        if (ctrl && code == KeyEvent.KEYCODE_F || !typing && !ctrl && code == KeyEvent.KEYCODE_SLASH) {
            if (searching) {
                searchField.requestFocus();
                searchField.selectAll();
            } else {
                openSearch();
            }
            return true;
        }
        if (ctrl && (code == KeyEvent.KEYCODE_TAB || code == KeyEvent.KEYCODE_PAGE_DOWN || code == KeyEvent.KEYCODE_PAGE_UP)
                || code == KeyEvent.KEYCODE_BUTTON_R1 || code == KeyEvent.KEYCODE_BUTTON_L1) {
            boolean back = code == KeyEvent.KEYCODE_PAGE_UP || code == KeyEvent.KEYCODE_BUTTON_L1
                    || code == KeyEvent.KEYCODE_TAB && event.isShiftPressed();
            stepPage(back ? -1 : 1);
            return true;
        }
        if (typing) return false;
        int height = Math.max(1, scrollView.getHeight() - dp(64));
        switch (code) {
            case KeyEvent.KEYCODE_PAGE_DOWN: scrollView.smoothScrollBy(0, height); return true;
            case KeyEvent.KEYCODE_PAGE_UP: scrollView.smoothScrollBy(0, -height); return true;
            case KeyEvent.KEYCODE_MOVE_HOME: scrollView.smoothScrollTo(0, 0); return true;
            case KeyEvent.KEYCODE_MOVE_END: scrollView.smoothScrollTo(0, Math.max(0, content.getHeight())); return true;
            default: return false;
        }
    }

    private void stepPage(int direction) {
        if (searching) closeSearch();
        int at = 0;
        for (int i = 0; i < TAB_ORDER.length; i++) if (TAB_ORDER[i] == currentPage) at = i;
        int next = TAB_ORDER[(at + direction + TAB_ORDER.length) % TAB_ORDER.length];
        feel().play(Feel.TICK);
        selectPage(next, true);
    }

    private void openSearch() {
        if (searching) return;
        searching = true;
        searchBack.setEnabled(true);
        titleView.setVisibility(View.GONE);
        searchField.setVisibility(View.VISIBLE);
        searchField.setText("");
        searchButton.setImageDrawable(new IconDrawable(IconDrawable.CLOSE, dp(22), dp(2.5f), COLOR_ON));
        searchButton.setContentDescription("Close search");
        selectPage(ALL_PAGE, false);
        applySearch("", true);
        searchField.requestFocus();
        searchField.post(() -> {
            InputMethodManager keyboardService = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (keyboardService != null && searching) keyboardService.showSoftInput(searchField, InputMethodManager.SHOW_IMPLICIT);
        });
    }

    private void closeSearch() {
        if (!searching) return;
        searching = false;
        searchBack.setEnabled(false);
        hideKeyboard();
        searchField.setText("");
        searchField.clearFocus();
        searchField.setVisibility(View.GONE);
        titleView.setVisibility(View.VISIBLE);
        searchButton.setImageDrawable(new IconDrawable(IconDrawable.SEARCH, dp(22), dp(2.5f), COLOR_ON));
        searchButton.setContentDescription("Search settings");
        noResults.setVisibility(View.GONE);
        for (ViewGroup group : groups) {
            group.setVisibility(View.VISIBLE);
            groupLabels.get(group).setVisibility(View.VISIBLE);
            for (int i = 0; i < group.getChildCount(); i++) {
                View row = group.getChildAt(i);
                row.setVisibility(dependencyHidden.contains(row) ? View.GONE : View.VISIBLE);
            }
        }
        styleAllGroups();
        selectPage(currentPage, false);
    }

    private void hideKeyboard() {
        InputMethodManager keyboardService = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (keyboardService != null) keyboardService.hideSoftInputFromWindow(searchField.getWindowToken(), 0);
    }

    private int pageOf(View view) {
        View parent = view;
        while (parent != null) {
            for (int i = 0; i < pages.length; i++) {
                if (pages[i] == parent) return i;
            }
            parent = parent.getParent() instanceof View ? (View) parent.getParent() : null;
        }
        return 0;
    }

    private String pageName(int page) {
        return tabs[page] != null ? tabs[page].getText().toString() : "";
    }

    private static String searchable(String text) {
        return text == null ? "" : text.toLowerCase(Locale.US).replace('_', ' ').replace('-', ' ');
    }

    private static void collectText(View view, StringBuilder out) {
        if (view instanceof TextView) out.append(' ').append(((TextView) view).getText());
        CharSequence description = view.getContentDescription();
        if (description != null) out.append(' ').append(description);
        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            for (int i = 0; i < group.getChildCount(); i++) collectText(group.getChildAt(i), out);
        }
    }

    private void applySearch(String query, boolean backToTop) {
        searchQuery = query == null ? "" : query;
        String[] words = searchable(searchQuery).trim().split("\\s+");
        int found = 0;
        for (int page = 0; page < pages.length; page++) {
            boolean pageShown = false;
            for (ViewGroup group : groups) {
                if (pageOf(group) != page) continue;
                String context = pageName(page) + " " + groupTitles.get(group);
                int shown = 0;
                for (int i = 0; i < group.getChildCount(); i++) {
                    View row = group.getChildAt(i);
                    boolean visible = !dependencyHidden.contains(row) && !twinRows.contains(row)
                            && (page == ABOUT_PAGE ? titleMatches(row, context, words) : matches(row, context, words));
                    row.setVisibility(visible ? View.VISIBLE : View.GONE);
                    if (visible) shown++;
                }
                group.setVisibility(shown > 0 ? View.VISIBLE : View.GONE);
                groupLabels.get(group).setVisibility(shown > 0 ? View.VISIBLE : View.GONE);
                found += shown;
                pageShown |= shown > 0;
            }
            pages[page].setVisibility(pageShown ? View.VISIBLE : View.GONE);
            pages[page].setAlpha(1f);
            pages[page].setTranslationY(0f);
        }
        for (TextView intro : intros) intro.setVisibility(View.GONE);
        noResults.setText("No settings match \"" + searchQuery.trim() + "\"");
        noResults.setVisibility(found == 0 ? View.VISIBLE : View.GONE);
        styleAllGroups();
        if (backToTop) scrollView.scrollTo(0, 0);
    }

    private boolean titleMatches(View row, String context, String[] words) {
        String title = firstText(row);
        String haystack = searchable(context + " " + (title == null ? "" : title));
        for (String word : words) {
            if (!word.isEmpty() && !haystack.contains(word)) return false;
        }
        return true;
    }

    private boolean matches(View row, String context, String[] words) {
        StringBuilder text = new StringBuilder(context);
        collectText(row, text);
        Setting setting = rowSettings.get(row);
        if (setting != null) text.append(' ').append(setting.key).append(' ').append(textName(setting));
        String haystack = searchable(text.toString());
        for (String word : words) {
            if (!word.isEmpty() && !haystack.contains(word)) return false;
        }
        return true;
    }

    private TextView createTab(String name, TextView template, View before) {
        TextView tab = new TextView(this);
        tab.setText(name);
        tab.setTextSize(14);
        tab.setTypeface(template.getTypeface());
        tab.setGravity(Gravity.CENTER);
        tab.setMaxLines(1);
        tab.setPadding(template.getPaddingLeft(), template.getPaddingTop(), template.getPaddingRight(), template.getPaddingBottom());
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(WRAP, dp(40));
        params.setMarginEnd(dp(6));
        ViewGroup bar = (ViewGroup) template.getParent();
        int position = 0;
        while (position < bar.getChildCount() && bar.getChildAt(position) != before) position++;
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
        int page = index >= 0 && index < pages.length ? index : ALL_PAGE;
        boolean all = page == ALL_PAGE;
        for (int i = 0; i < pages.length; i++) {
            tabStyles[i].set(i == page, animate);
            if (!searching) pages[i].setVisibility((all && i != ABOUT_PAGE && i != CLASSIC_PAGE) || i == page ? View.VISIBLE : View.GONE);
            if (i != ALL_PAGE) {
                ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) pages[i].getLayoutParams();
                params.topMargin = all ? dp(14) : 0;
                pages[i].setLayoutParams(params);
            }
        }
        for (ViewGroup group : groups) {
            int owner = pageOf(group);
            String title = groupTitles.get(group);
            String text = all && owner != ALL_PAGE ? pageName(owner) + "  /  " + title : title;
            groupLabels.get(group).setText(text.toUpperCase(Locale.US));
        }
        for (TextView intro : intros) intro.setVisibility(all || searching ? View.GONE : View.VISIBLE);
        currentPage = page;
        if (page == ABOUT_PAGE) refreshAbout();
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
    public static final String[] PIPE_COLOR_NAMES = {"Green", "Red", "Blue", "Yellow", "White", "Pink", "Black", "Purple", "Orange",
            "Teal", "Sky", "Mint", "Brown", "Navy", "Wine"};
    private static final int[] PIPE_COLORS = {0xFF73BF2E, 0xFFCF5B55, 0xFF638ED4, 0xFFDCC357, 0xFFE6E9EA, 0xFFDD84C7, 0xFF484B51, 0xFF9566CA,
            0xFFDD8D4C, 0xFF58B9AC, 0xFF8AC7EA, 0xFF83D2AC, 0xFFAC815B, 0xFF4F5E9E, 0xFFA14A62};
    public static final int PIPE_COLOR_COUNT = 15;
    public static final int CLASSIC_PIPE_COLOR_COUNT = 9;
    private static final int PIPE_OUTLINE = 0x543847;
    private static final int[][] PIPE_TINTS = {
            null,
            {0xFF9C8A, 0xA8262A, PIPE_OUTLINE},
            {0xA8D4FF, 0x2A55B0, PIPE_OUTLINE},
            {0xFFF6A0, 0xC09A1C, PIPE_OUTLINE},
            {0xF4F6F2, 0xA4ACB4, PIPE_OUTLINE},
            {0xFFC4F0, 0xC24FA6, PIPE_OUTLINE},
            {0x7A7E86, 0x1F2126, 0x101114},
            {0xD9B0FF, 0x5E2A9E, PIPE_OUTLINE},
            {0xFFD08A, 0xC2561A, PIPE_OUTLINE},
            {0x9FF2E2, 0x1E8A80, PIPE_OUTLINE},
            {0xD2F2FF, 0x4FA3D9, PIPE_OUTLINE},
            {0xD8FFE8, 0x3DAE7A, PIPE_OUTLINE},
            {0xE8C59A, 0x7A4A28, 0x3A2418},
            {0x8FA4E8, 0x1B2462, 0x121530},
            {0xE48A9E, 0x6B1530, 0x3A0E1C}};

    public static ColorFilter pipeColorFilter(int index) {
        if (index <= 0 || index >= PIPE_TINTS.length) return null;
        int[] tint = PIPE_TINTS[index];
        return new ColorMatrixColorFilter(new ColorMatrix(pipeTintMatrix(tint[0], tint[1], tint[2])));
    }

    static float[] pipeTintMatrix(int light, int dark, int outline) {
        double[] darkFrom = channels(0x558022);
        double[] along = minus(channels(0xE4FD8B), darkFrom);
        double[] across = minus(channels(PIPE_OUTLINE), darkFrom);
        double[] rest = {along[1] * across[2] - along[2] * across[1], along[2] * across[0] - along[0] * across[2], along[0] * across[1] - along[1] * across[0]};
        double[] darkTo = channels(dark);
        double[] alongTo = minus(channels(light), darkTo);
        double[] acrossTo = minus(channels(outline), darkTo);
        double[][] from = {{along[0], across[0], rest[0]}, {along[1], across[1], rest[1]}, {along[2], across[2], rest[2]}};
        double[][] inverse = invert3(from);
        float[] out = new float[20];
        for (int r = 0; r < 3; r++) {
            double[] to = {alongTo[r], acrossTo[r], 0};
            double offset = darkTo[r];
            for (int c = 0; c < 3; c++) {
                double value = to[0] * inverse[0][c] + to[1] * inverse[1][c] + to[2] * inverse[2][c];
                out[r * 5 + c] = (float) value;
                offset -= value * darkFrom[c];
            }
            out[r * 5 + 4] = (float) offset;
        }
        out[18] = 1f;
        return out;
    }

    private static double[] channels(int color) {
        return new double[] {(color >> 16) & 0xFF, (color >> 8) & 0xFF, color & 0xFF};
    }

    private static double[] minus(double[] a, double[] b) {
        return new double[] {a[0] - b[0], a[1] - b[1], a[2] - b[2]};
    }

    private static double[][] invert3(double[][] m) {
        double a = m[0][0], b = m[0][1], c = m[0][2], d = m[1][0], e = m[1][1], f = m[1][2], g = m[2][0], h = m[2][1], i = m[2][2];
        double co0 = e * i - f * h, co1 = f * g - d * i, co2 = d * h - e * g;
        double det = a * co0 + b * co1 + c * co2;
        return new double[][] {
                {co0 / det, (c * h - b * i) / det, (b * f - c * e) / det},
                {co1 / det, (a * i - c * g) / det, (c * d - a * f) / det},
                {co2 / det, (b * g - a * h) / det, (a * e - b * d) / det}};
    }

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

    private static LinearLayout.LayoutParams weightedHeight() {
        return new LinearLayout.LayoutParams(MATCH, 0, 1f);
    }

    private LinearLayout newLinear(boolean horizontal) {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(horizontal ? LinearLayout.HORIZONTAL : LinearLayout.VERTICAL);
        return layout;
    }

    private LinearLayout addPage(int index) {
        if (pageStack == null) {
            pageStack = newLinear(false);
            content.addView(pageStack, new FrameLayout.LayoutParams(MATCH, WRAP));
            noResults = newText("", 15, COLOR_ON_VARIANT, pixelFont);
            noResults.setGravity(Gravity.CENTER);
            noResults.setPadding(dp(16), dp(40), dp(16), dp(40));
            noResults.setVisibility(View.GONE);
        }
        LinearLayout page = newLinear(false);
        if (index == ALL_PAGE) pageStack.addView(page, 0, linear(MATCH, WRAP));
        else pageStack.addView(page, linear(MATCH, WRAP));
        pages[index] = page;
        return page;
    }

    private void pageIntro(LinearLayout page, String text) {
        TextView intro = newText(text, 14, COLOR_ON_VARIANT, null);
        intro.setLineSpacing(dp(2), 1f);
        LinearLayout.LayoutParams params = linear(MATCH, WRAP);
        params.setMarginStart(dp(8));
        params.setMarginEnd(dp(8));
        params.topMargin = dp(4);
        page.addView(intro, params);
        intros.add(intro);
    }

    private LinearLayout addGroup(LinearLayout page, String title) {
        TextView label = newText(title.toUpperCase(Locale.US), 12, COLOR_PRIMARY, pixelBold);
        label.setLetterSpacing(0.12f);
        LinearLayout.LayoutParams labelParams = linear(WRAP, WRAP);
        labelParams.setMarginStart(dp(8));
        boolean first = page.getChildCount() == 0;
        labelParams.topMargin = dp(first ? 6 : page.getChildCount() == 1 && intros.contains(page.getChildAt(0)) ? 18 : 24);
        labelParams.bottomMargin = dp(10);
        page.addView(label, labelParams);

        LinearLayout group = newLinear(false);
        page.addView(group, linear(MATCH, WRAP));
        groups.add(group);
        groupLabels.put(group, label);
        groupTitles.put(group, title);
        return group;
    }

    private void keyRow(LinearLayout group, String name, String title, String summary, int defaultKey) {
        valueRow(group, name, title, summary, keyName(defaultKey));
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

    private void choiceRowWithNote(LinearLayout group, String name, String title, String summary, String... options) {
        LinearLayout row = addRow(group, name, false, 14, 14);
        addTexts(row, name, title, summary, false, COLOR_ON);
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
        searchField.setTypeface(pixelFont);

        LinearLayout start = addPage(ALL_PAGE);
        LinearLayout quick = addGroup(start, "Start here");
        replayHeroRow(quick);
        actionRow(quick, "button_reset_all_quick", "Reset to defaults",
                "Puts every setting back to normal. Your high score and saved setups are kept", COLOR_PRIMARY);

        LinearLayout page;
        LinearLayout group;
        page = addPage(CLASSIC_PAGE);
        pageIntro(page, "One tap to play like the 2013 original, and a check of every setting it changes");
        group = addGroup(page, "Classic feel");
        actionRow(group, "button_classic_feel", "Use classic feel", CLASSIC_SUMMARY, COLOR_PRIMARY);
        actionRow(group, "button_standard_feel", "Back to standard feel", "Puts every setting below back to its default", COLOR_ON);
        valueRow(group, "row_classic_status", "Matching Classic feel", "Each setting below shows its Classic value", "0/0");

        page = addPage(0);
        pageIntro(page, "How the bird looks, how big it is, where it flies and how it tilts");
        group = addGroup(page, "Look");
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
        group = addGroup(page, "Tilt");
        toggleRow(group, "row_bird_tilt", "Bird tilts", "Noses up on a flap and dives when falling");
        choiceRow(group, "row_tilt_style", "Tilt style", "Smooth", "Classic");
        sliderRow(group, "row_max_tilt_up", "Nose-up angle", null, "25 deg");
        sliderRow(group, "row_max_tilt_down", "Dive angle", null, "90 deg");
        sliderRow(group, "row_tilt_speed", "Tilt speed", null, "1x");

        page = addPage(1);
        pageIntro(page, "The sky behind the game and the weather in it");
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
        pageIntro(page, "How pipes look, how far apart they are and how they move");
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
        pageIntro(page, "How the bird flies and falls, how fast the game runs and how it gets harder");
        group = addGroup(page, "Flight");
        sliderRow(group, "row_gravity", "Gravity", null, "1x");
        sliderRow(group, "row_jump", "Flap strength", null, "1x");
        sliderRow(group, "row_hang_delay", "Hang time", "Pause before the bird tips into a dive", "1x");
        group = addGroup(page, "Falling");
        sliderRow(group, "row_max_fall_speed", "Max fall speed", "Caps how fast the bird drops, compared to a flap", "Off");
        toggleRow(group, "row_ceiling", "Ceiling", "Keeps the bird from flying far above the screen");
        toggleRow(group, "row_flap_limit", "No flapping off screen", "Taps do nothing while the bird is above the top edge");
        sliderRow(group, "row_crash_gravity", "Crash fall", "How hard the bird drops after a crash", "1.5x");
        group = addGroup(page, "Speed");
        sliderRow(group, "row_game_speed", "Game speed", null, "1x");
        sliderRow(group, "row_pipe_speed_variation", "Speed surprise", "Random speed change every round", "0x");
        group = addGroup(page, "Difficulty");
        sliderRow(group, "row_speed_ramp", "Speeds up", "Faster for every 10 points you score", "0%");
        sliderRow(group, "row_ramp_top_speed", "Top speed", "Where the speeding up stops", "3x");
        sliderRow(group, "row_gap_shrink", "Gap shrinks", "Tighter for every 10 points you score", "0%");
        sliderRow(group, "row_ramp_min_gap", "Smallest gap", "Where the shrinking stops", "55%");
        group = addGroup(page, "Timing");
        toggleRow(group, "row_original_motion", "Original motion",
                "Flies exactly like the original's 60 steps a second, drawn smoothly at your screen's full refresh rate");
        toggleRow(group, "row_fixed_step", "Fixed-step physics", "Moves the game in fixed steps instead of once per screen refresh. Smoothest left off");
        choiceRowWithNote(group, "row_fixed_step_rate", "Step rate", "Steps per second. Classic feel uses 60", "30 Hz", "60 Hz", "120 Hz");
        choiceRowWithNote(group, "row_fixed_step_timing", "Step timing",
                "Smooth matches steps to your screen and draws the motion in between. Original is the v1.5.1 timing", "Smooth", "Original");
        toggleRow(group, "row_pixel_steps", "Whole-pixel movement", "The bird moves a whole number of game pixels per step");

        page = addPage(SCORING_PAGE);
        pageIntro(page, "How points are counted and what each medal takes");
        group = addGroup(page, "Points");
        sliderRow(group, "row_score_multiplier", "Points per pipe", null, "1");
        choiceRow(group, "row_score_point", "Point is scored at the pipe's", "Start", "Middle", "End", "Classic");
        group = addGroup(page, "Medals");
        sliderRow(group, "row_medal_bronze", "Bronze", "Score needed for each medal", "10");
        sliderRow(group, "row_medal_silver", "Silver", null, "20");
        sliderRow(group, "row_medal_gold", "Gold", null, "30");
        sliderRow(group, "row_medal_platinum", "Platinum", null, "40");

        page = addPage(5);
        pageIntro(page, "Cheats and twists for fun rounds. Turning any of them on keeps scores off the leaderboard");
        group = addGroup(page, "Cheats");
        toggleRow(group, "row_no_clip", "God mode", "Nothing can kill the bird");
        toggleRow(group, "row_god_fall", "Fall out to end the run", "Leaving the screen for too long ends the round, so the score still counts");
        sliderRow(group, "row_god_fall_seconds", "Time off screen", "How long the bird can stay off screen in God mode", "2 s");
        sliderRow(group, "row_extra_lives", "Extra lives", "Hits you can survive in each round", "0");
        toggleRow(group, "row_autopilot", "Autopilot", "The bird flies itself after you tap to start");
        group = addGroup(page, "Twists");
        toggleRow(group, "row_jetpack_mode", "Jetpack", "Hold the screen to rise instead of tapping");
        toggleRow(group, "row_drunk_mode", "Drunk mode", "The bird sways from side to side");
        sliderRow(group, "row_drunk_mode_strength", "Sway strength", null, "1x");
        toggleRow(group, "row_upside_down", "Upside down", "Flips the whole world over");
        toggleRow(group, "row_reverse_pipes", "Reverse pipes", "Pipes come from the left instead");

        page = addPage(4);
        pageIntro(page, "Effects, what shows on screen during a round, and smoothness");
        group = addGroup(page, "Effects");
        toggleRow(group, "row_screen_shake", "Screen shake", "Rattles the screen on crashes and thunder");
        sliderRow(group, "row_screen_shake_strength", "Shake strength", null, "1x");
        toggleRow(group, "row_motion_blur", "Motion blur", "Smears fast movement like a camera");
        sliderRow(group, "row_motion_blur_strength", "Blur strength", null, "1x");
        group = addGroup(page, "Crash flash");
        toggleRow(group, "row_crash_flash", "Crash flash", "Flashes the screen when the bird hits something");
        choiceRow(group, "row_crash_flash_color", "Flash color", "White", "Red", "Yellow", "Black");
        sliderRow(group, "row_crash_flash_strength", "Brightness", null, "100%");
        sliderRow(group, "row_crash_flash_length", "Length", "How long the flash takes to fade", "170 ms");
        choiceRow(group, "row_crash_flash_fade", "Fade", "Even", "Sharp");
        group = addGroup(page, "Game over");
        sliderRow(group, "row_opacity", "Screen dimming", "How dark it gets behind the score panel", "5%");
        group = addGroup(page, "On screen");
        toggleRow(group, "row_hide_settings_icon", "Hide settings button", "The top right corner still opens settings");
        toggleRow(group, "row_classic_menu", "Original menu buttons", "Rate button on the home screen and a leaderboard button after a crash");
        toggleRow(group, "row_show_get_ready", "Get Ready sign", "Shown before each round starts");
        toggleRow(group, "row_show_tap_guide", "Tap guide", "The tap hint under the bird before each round");
        toggleRow(group, "row_show_score", "Score while playing", "Turn off to play without watching the count. Your score still shows after the round");
        group = addGroup(page, "Performance");
        choiceRowWithNote(group, "row_frame_rate_limit", "Frame rate limit", "Lower limits save battery. Unlimited follows your screen",
                "Unlimited", "30", "60", "90", "120");
        group = addGroup(page, "Debug");
        toggleRow(group, "row_show_fps", "FPS counter", "Shows frames per second in the top left corner");
        toggleRow(group, "row_show_hitboxes", "Show hitboxes", "Outlines everything that can collide");

        page = addPage(SOUND_PAGE);
        pageIntro(page, "Sound effects, volume and vibration");
        group = addGroup(page, "Sound");
        toggleRow(group, "row_sound", "Sound effects", "Flaps, points, crashes and weather");
        sliderRow(group, "row_sound_volume", "Effects volume", null, "100%");
        sliderRow(group, "row_weather_volume", "Weather volume", "Rain and storm loops", "100%");
        group = addGroup(page, "Vibration");
        toggleRow(group, "row_haptic_feedback", "Vibration", "Feel flaps, points, crashes and buttons. Phones without a motor skip it");
        choiceRow(group, "row_haptic_strength", "Strength", "Light", "Normal", "Strong");
        toggleRow(group, "row_haptic_flap", "Flaps", "A short click every time the bird flaps");
        toggleRow(group, "row_haptic_score", "Points", "A soft tick for each pipe, and a sparkle for golden ones");
        toggleRow(group, "row_haptic_crash", "Crashes", "A thud when you hit something, and a bump when you land");
        toggleRow(group, "row_haptic_best", "New best and medals", "A little celebration on the score screen");
        toggleRow(group, "row_haptic_menus", "Buttons and menus", "Clicks for buttons, switches and sliders, and ticks on the leaderboard");

        page = addPage(6);
        pageIntro(page, "Keyboard, mouse and pausing. Works with USB and Bluetooth keyboards and on PC emulators");
        group = addGroup(page, "Keyboard and mouse");
        toggleRow(group, "row_keyboard", "Keyboard controls", "Play with a USB or Bluetooth keyboard");
        keyRow(group, "row_key_flap", "Flap", "Also presses Play on the menus", DEFAULT_KEY_FLAP);
        keyRow(group, "row_key_flap_alt", "Flap, second key", null, DEFAULT_KEY_FLAP_ALT);
        keyRow(group, "row_key_pause", "Pause and resume", "Esc also pauses during a run", DEFAULT_KEY_PAUSE);
        keyRow(group, "row_key_play", "Play and restart", null, DEFAULT_KEY_PLAY);
        keyRow(group, "row_key_settings", "Open settings", "From the home and score screens", DEFAULT_KEY_SETTINGS);
        toggleRow(group, "row_right_click_pause", "Right click pauses", "Right mouse button pauses and resumes a run");
        toggleRow(group, "row_hide_cursor", "Hide the mouse pointer", "Hidden during a run until you move the mouse");
        group = addGroup(page, "Pausing");
        toggleRow(group, "row_pause_button", "Pause button", "Shows a pause button while you play");
        toggleRow(group, "row_auto_pause", "Pause when you leave", "Freezes the round until you tap again");

        page = addPage(REPLAYS_PAGE);
        pageIntro(page, "Watch your rounds again and save them as videos");
        group = addGroup(page, "Watch");
        actionRow(group, "button_replays", "Watch replays", "Watch your rounds again or save them as videos", COLOR_PRIMARY);
        group = addGroup(page, "Recording");
        toggleRow(group, "row_replay_enabled", "Record replays", "Quietly saves your rounds. Uses almost no battery or storage");
        choiceRowWithNote(group, "row_replay_mode", "Replays to keep", "Best only keeps your top round until you beat it", "Latest", "Best only");
        choiceRow(group, "row_replay_keep", "Rounds kept", "1", "3", "5", "10");
        group = addGroup(page, "Videos");
        choiceRow(group, "row_replay_resolution", "Video size", "Full", "1080p", "720p");
        choiceRow(group, "row_replay_fps", "Video frame rate", "30 fps", "60 fps");
        choiceRow(group, "row_replay_quality", "Video quality", "High", "Very high", "Best");
        toggleRow(group, "row_replay_sound", "Sound in videos", "Mixes the game's sounds into saved videos");
        toggleRow(group, "row_replay_credit", "Credit on videos", "A small Flappy Bird Recreation tag with the version and an unofficial note in the corner");

        page = addPage(LEADERBOARD_PAGE);
        pageIntro(page, "Your name, posting scores and how the leaderboard looks");
        group = addGroup(page, "Posting scores");
        toggleRow(group, "row_board_auto_post", "Post scores automatically", "Fair rounds go to the online leaderboard when you're connected");
        actionRow(group, "button_board_name", "Leaderboard name", "Not set", COLOR_ON);
        actionRow(group, "button_board_rules", "What counts as a real score", "Checking your settings...", COLOR_ON);
        toggleRow(group, "row_board_show_status", "Show posting status", "A short note on the score screen says whether the round was posted");
        group = addGroup(page, "Leaderboard screen");
        toggleRow(group, "row_board_open_on_best", "Open the leaderboard after a new best", "Shows where you placed when a posted score beats your best");
        choiceRow(group, "row_board_tab", "Leaderboard opens on", "All time", "Weekly");
        choiceRow(group, "row_board_size", "Scores shown on the leaderboard", "10", "25", "50", "100");

        page = addPage(PRESETS_PAGE);
        pageIntro(page, "Save, share and reset your setup, and send a bug report");
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
        actionRow(group, "button_edit_text", "Edit as text", "See and change every setting at once, past the slider limits", COLOR_ON);
        group = addGroup(page, "Help and bug reports");
        actionRow(group, "button_activity_log", "Activity log", "Every round, setting change and error, ready to send with a bug report", COLOR_ON);
        toggleRow(group, "row_activity_log_enabled", "Activity log", "Records rounds, changes and crashes for bug reports. Turning it off is not recommended");

        buildAboutPage();
        content.removeView(noResults);
        pageStack.addView(noResults, linear(MATCH, WRAP));
    }

    private void buildAboutPage() {
        LinearLayout page = addPage(ABOUT_PAGE);
        LinearLayout group = addGroup(page, "About");
        LinearLayout header = addRow(group, "about_header", true, 20, 20);
        try {
            android.graphics.drawable.Drawable icon = getPackageManager().getApplicationIcon(getPackageName());
            ImageView image = new ImageView(this);
            image.setImageDrawable(icon);
            image.setScaleType(ImageView.ScaleType.FIT_CENTER);
            LinearLayout.LayoutParams iconParams = linear(dp(56), dp(56));
            iconParams.setMarginEnd(dp(16));
            header.addView(image, iconParams);
        } catch (Exception ignored) {
        }
        LinearLayout titles = newLinear(false);
        titles.addView(newText("Flappy Bird Recreation", 20, COLOR_ON, pixelBold), linear(MATCH, WRAP));
        TextView madeBy = newText("Made by " + MADE_BY, 15, COLOR_PRIMARY, mediumFont);
        LinearLayout.LayoutParams madeParams = linear(MATCH, WRAP);
        madeParams.topMargin = dp(4);
        titles.addView(madeBy, madeParams);
        TextView version = newText("Version " + appVersion(this), 13, COLOR_ON_VARIANT, pixelFont);
        LinearLayout.LayoutParams versionParams = linear(MATCH, WRAP);
        versionParams.topMargin = dp(2);
        titles.addView(version, versionParams);
        summaries.put("about_header", version);
        header.addView(titles, weighted(WRAP));

        group = addGroup(page, "With respect to the original");
        LinearLayout respect = addRow(group, "about_respect", false, 16, 16);
        addTexts(respect, "about_respect", "Flappy Bird",
                "Flappy Bird was created by Dong Nguyen and released by .GEARS Studios in 2013. All credit for the original idea, "
                        + "design, characters, artwork and sounds belongs to its creator. Flappy Bird is a trademark of its owner, used here only to "
                        + "describe the game this app recreates.\n\n"
                        + "This recreation was made out of deep respect and admiration for the original game. It is a free, non-commercial fan project "
                        + "and is not affiliated with, endorsed by or connected to the original creator or the owner of the Flappy Bird name.\n\n"
                        + "If the creator or the owner of the name or assets has any concern, the project will be taken down, "
                        + "or handed over in full if they ask.", false, COLOR_ON);

        group = addGroup(page, "This app");
        actionRow(group, "about_version", "Version", appVersion(this), COLOR_ON);
        infoRow(group, "about_build", "Build", "");
        infoRow(group, "about_installed", "Installed and updated", "");
        infoRow(group, "about_device", "Your device", "");
        infoRow(group, "about_install_id", "Install ID", "");
        infoRow(group, "about_high_score", "High score", "");
        infoRow(group, "about_setup", "Your setup", "");
        actionRow(group, "about_copy", "Copy app info", "Version, device and setup details to paste into a bug report", COLOR_ON);

        group = addGroup(page, "Online leaderboard");
        infoRow(group, "about_board_status", "Leaderboard", "");
        infoRow(group, "about_board_entry", "Your entry", "");
        actionRow(group, "about_board_leave", "Remove my leaderboard entry", "Deletes your name and score from the online leaderboard", COLOR_ERROR);
        rowView("about_board_leave").setOnClickListener(v -> confirm("Remove leaderboard entry",
                "Your name and score will be deleted from the online leaderboard. Your local high score is kept.", "Remove",
                this::leaveLeaderboard));

        group = addGroup(page, "Links");
        actionRow(group, "about_github", "GitHub page", "Source code, updates and the license", COLOR_ON);
        actionRow(group, "about_report", "Report a bug", "Opens the issues page. Attaching your activity log helps a lot", COLOR_ON);

        rowView("about_version").setOnClickListener(v -> copyAppInfo());
        rowView("about_copy").setOnClickListener(v -> copyAppInfo());
        rowView("about_github").setOnClickListener(v -> openLink(PROJECT_URL));
        rowView("about_report").setOnClickListener(v -> {
            openLink(PROJECT_URL + "/issues");
            announce("Tip: attach your activity log to the report", "Open log", view -> showActivityLog());
        });
    }

    private String settingTitle(String key) {
        Setting setting = settingsByKey.get(key);
        String title = setting == null || setting.row == null ? null : firstText(setting.row);
        return title != null ? title : fairKeyName(key);
    }

    private String leaderboardStatus() {
        if (!Leaderboard.configured()) return "Not available in this build";
        Map<String, Object> current = snapshot();
        String blocker = fairPlayBlocker(current);
        if (blocker == null) {
            return fairPlayMode(current) == FAIR_CLASSIC ? "Your scores count, with a Classic label" : "Your scores count";
        }
        if (FAIR_MIXED.equals(blocker)) return "Scores are not posted: mix of Classic and normal settings. Use Reset to defaults or Classic feel";
        if (PREF_WEATHER_EFFECT.equals(blocker)) return "Scores are not posted: thunderstorm slows time when lightning strikes";
        return "Scores are not posted: \"" + settingTitle(blocker) + "\" is changed";
    }

    private String leaderboardEntry() {
        String name = Leaderboard.name(prefs);
        if (name.isEmpty()) return "No name set. Choose one in Settings > Leaderboard";
        int best = Leaderboard.best(prefs);
        int posted = Leaderboard.posted(prefs);
        String text = name + "  \u00B7  all-time " + posted + "  \u00B7  this week " + Leaderboard.weekPosted(prefs);
        if (best > posted) text += "  \u00B7  best fair round " + best + (Leaderboard.bestIsClassic(prefs) ? " (Classic)" : "");
        Setting autoPost = settingsByKey.get(PREF_BOARD_AUTO_POST);
        if (autoPost instanceof ToggleSetting && !((ToggleSetting) autoPost).value) text += "  \u00B7  posting is off";
        return text;
    }

    private void leaveLeaderboard() {
        announce("Removing your entry\u2026", null, null);
        Leaderboard.leave(this, this::runOnUiThread, (done, error) -> {
            announce(error == null ? "Leaderboard entry removed" : "Could not remove it: " + error, null, null);
            refreshAbout();
        });
    }

    private void infoRow(LinearLayout group, String name, String title, String summary) {
        LinearLayout row = addRow(group, name, false, 12, 12);
        addTexts(row, name, title, summary, false, COLOR_ON);
    }

    private String buildType() {
        boolean debuggable = (getApplicationInfo().flags & android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE) != 0;
        return (debuggable ? "Debug" : "Release") + " build, target API " + getApplicationInfo().targetSdkVersion;
    }

    private String installDates() {
        try {
            android.content.pm.PackageInfo info = getPackageManager().getPackageInfo(getPackageName(), 0);
            SimpleDateFormat day = new SimpleDateFormat("MMM d, yyyy", Locale.US);
            return "Installed " + day.format(new Date(info.firstInstallTime)) + ", updated " + day.format(new Date(info.lastUpdateTime));
        } catch (Exception e) {
            return "Unknown";
        }
    }

    private String installId() {
        long salt = installSalt(prefs);
        return String.format(Locale.US, "%08x", (int) (salt ^ (salt >>> 32)));
    }

    private int changedSettings() {
        int changed = 0;
        for (Setting setting : settings) {
            Object now = setting.capture();
            if (now != null && !now.equals(setting.defaultObject())) changed++;
        }
        return changed;
    }

    private void refreshAbout() {
        if (summaries.get("about_setup") == null) return;
        String version = appVersion(this);
        summaries.get("about_header").setText("Version " + version);
        summaries.get("about_version").setText(version + "  \u00B7  tap to copy app info");
        String build;
        try {
            build = buildType();
        } catch (RuntimeException e) {
            build = "Unknown";
        }
        summaries.get("about_build").setText(build);
        summaries.get("about_installed").setText(installDates());
        summaries.get("about_device").setText(Build.MANUFACTURER + " " + Build.MODEL + ", Android " + Build.VERSION.RELEASE
                + " (API " + Build.VERSION.SDK_INT + ")");
        summaries.get("about_install_id").setText(installId() + "  \u00B7  also shown in exported activity logs");
        summaries.get("about_high_score").setText(highScoreStatus().replace("Current high score: ", "").replace("\n", ". "));
        int changed = changedSettings();
        summaries.get("about_setup").setText(changed == 0 ? "All " + settings.size() + " settings at their defaults"
                : changed + " of " + settings.size() + " settings changed from the defaults");
        summaries.get("about_board_status").setText(leaderboardStatus());
        summaries.get("about_board_entry").setText(leaderboardEntry());
        boolean joined = !Leaderboard.uid(prefs).isEmpty() || !Leaderboard.name(prefs).isEmpty();
        View leave = rowView("about_board_leave");
        if (joined) dependencyHidden.remove(leave);
        else dependencyHidden.add(leave);
        if (!searching) {
            leave.setVisibility(joined ? View.VISIBLE : View.GONE);
            styleAllGroups();
        }
    }

    private String appInfoText() {
        refreshAbout();
        StringBuilder out = new StringBuilder("Flappy Bird Recreation by ").append(MADE_BY).append('\n');
        out.append("Version: ").append(appVersion(this)).append('\n');
        out.append("Build: ").append(summaries.get("about_build").getText()).append('\n');
        out.append(summaries.get("about_installed").getText()).append('\n');
        out.append("Device: ").append(summaries.get("about_device").getText()).append('\n');
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        out.append("Screen: ").append(metrics.widthPixels).append(" x ").append(metrics.heightPixels).append(" px, ")
                .append(metrics.densityDpi).append(" dpi\n");
        out.append("Install ID: ").append(installId()).append('\n');
        out.append("High score: ").append(summaries.get("about_high_score").getText()).append('\n');
        out.append("Setup: ").append(summaries.get("about_setup").getText()).append('\n');
        out.append("Activity log: ").append(activityLog == null || activityLog.value ? "on" : "off").append('\n');
        return out.toString();
    }

    private void copyAppInfo() {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboard == null) {
            announce("Could not reach the clipboard", null, null);
            return;
        }
        clipboard.setPrimaryClip(ClipData.newPlainText("Flappy Bird app info", appInfoText()));
        announce("App info copied", null, null);
    }

    private void openLink(String url) {
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
        } catch (ActivityNotFoundException e) {
            announce("No browser found to open " + url, null, null);
        }
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
                new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14},
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
        toggle(PREF_ORIGINAL_MOTION_ENABLED, DEFAULT_ORIGINAL_MOTION_ENABLED, rowView("row_original_motion"));
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
        toggle(PREF_GOD_FALL_ENABLED, DEFAULT_GOD_FALL_ENABLED, rowView("row_god_fall")).note = "applies in the game";
        new NumberSetting(PREF_GOD_FALL_SECONDS, DEFAULT_GOD_FALL_SECONDS, false, 0.5f, 10f, 0.5f, 0.1f, 60f, UNIT_SECONDS,
                rowView("row_god_fall_seconds")).note = "applies in the game";
        activityLog = toggle(PREF_ACTIVITY_LOG_ENABLED, DEFAULT_ACTIVITY_LOG_ENABLED, rowView("row_activity_log_enabled"));
        activityLog.randomizable = false;
        activityLog.note = "not recommended to turn off";
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
        ChoiceSetting hapticStrength = new ChoiceSetting(PREF_HAPTIC_STRENGTH, DEFAULT_HAPTIC_STRENGTH, rowView("row_haptic_strength"),
                new int[]{0, 1, 2}, new int[]{COLOR_SKY, COLOR_PRIMARY, 0xFFFC7858}, null);
        hapticStrength.randomizable = false;
        toggle(PREF_HAPTIC_FLAP, DEFAULT_HAPTIC_FLAP, rowView("row_haptic_flap")).randomizable = false;
        toggle(PREF_HAPTIC_SCORE, DEFAULT_HAPTIC_SCORE, rowView("row_haptic_score")).randomizable = false;
        toggle(PREF_HAPTIC_CRASH, DEFAULT_HAPTIC_CRASH, rowView("row_haptic_crash")).randomizable = false;
        toggle(PREF_HAPTIC_BEST, DEFAULT_HAPTIC_BEST, rowView("row_haptic_best")).randomizable = false;
        toggle(PREF_HAPTIC_MENUS, DEFAULT_HAPTIC_MENUS, rowView("row_haptic_menus")).randomizable = false;
        toggle(PREF_PAUSE_BUTTON_ENABLED, DEFAULT_PAUSE_BUTTON_ENABLED, rowView("row_pause_button")).randomizable = false;
        ToggleSetting autoPause = toggle(PREF_AUTO_PAUSE_ENABLED, DEFAULT_AUTO_PAUSE_ENABLED, rowView("row_auto_pause"));
        autoPause.note = "applies in the game";
        autoPause.randomizable = false;
        toggle(PREF_HIDE_SETTINGS_ICON, DEFAULT_HIDE_SETTINGS_ICON, rowView("row_hide_settings_icon")).randomizable = false;
        toggle(PREF_SHOW_GET_READY, DEFAULT_SHOW_GET_READY, rowView("row_show_get_ready")).randomizable = false;
        toggle(PREF_SHOW_TAP_GUIDE, DEFAULT_SHOW_TAP_GUIDE, rowView("row_show_tap_guide")).randomizable = false;
        toggle(PREF_SHOW_SCORE, DEFAULT_SHOW_SCORE, rowView("row_show_score")).randomizable = false;
        ToggleSetting classicMenu = toggle(PREF_CLASSIC_MENU_ENABLED, DEFAULT_CLASSIC_MENU_ENABLED, rowView("row_classic_menu"));
        classicMenu.note = "shown on the home and score screens";
        classicMenu.randomizable = false;
        ToggleSetting showFps = toggle(PREF_SHOW_FPS, DEFAULT_SHOW_FPS, rowView("row_show_fps"));
        showFps.note = "shown in the game";
        showFps.randomizable = false;
        toggle(PREF_SHOW_HITBOXES, DEFAULT_SHOW_HITBOXES, rowView("row_show_hitboxes")).randomizable = false;
        keyboard = toggle(PREF_KEYBOARD_ENABLED, DEFAULT_KEYBOARD_ENABLED, rowView("row_keyboard"));
        keyboard.note = "used in the game";
        keyboard.randomizable = false;
        new KeySetting(PREF_KEY_FLAP, DEFAULT_KEY_FLAP, rowView("row_key_flap"));
        new KeySetting(PREF_KEY_FLAP_ALT, DEFAULT_KEY_FLAP_ALT, rowView("row_key_flap_alt"));
        new KeySetting(PREF_KEY_PAUSE, DEFAULT_KEY_PAUSE, rowView("row_key_pause"));
        new KeySetting(PREF_KEY_PLAY, DEFAULT_KEY_PLAY, rowView("row_key_play"));
        new KeySetting(PREF_KEY_SETTINGS, DEFAULT_KEY_SETTINGS, rowView("row_key_settings"));
        ToggleSetting rightClick = toggle(PREF_RIGHT_CLICK_PAUSES, DEFAULT_RIGHT_CLICK_PAUSES, rowView("row_right_click_pause"));
        rightClick.note = "used in the game";
        rightClick.randomizable = false;
        ToggleSetting hideCursor = toggle(PREF_HIDE_CURSOR, DEFAULT_HIDE_CURSOR, rowView("row_hide_cursor"));
        hideCursor.note = "used in the game";
        hideCursor.randomizable = false;
        ToggleSetting autoPost = toggle(PREF_BOARD_AUTO_POST, DEFAULT_BOARD_AUTO_POST, rowView("row_board_auto_post"));
        autoPost.note = "used in the game";
        autoPost.randomizable = false;
        ToggleSetting showStatus = toggle(PREF_BOARD_SHOW_STATUS, DEFAULT_BOARD_SHOW_STATUS, rowView("row_board_show_status"));
        showStatus.note = "used in the game";
        showStatus.randomizable = false;
        ToggleSetting openOnBest = toggle(PREF_BOARD_OPEN_ON_BEST, DEFAULT_BOARD_OPEN_ON_BEST, rowView("row_board_open_on_best"));
        openOnBest.note = "used in the game";
        openOnBest.randomizable = false;
        ChoiceSetting boardTab = new ChoiceSetting(PREF_BOARD_TAB, DEFAULT_BOARD_TAB, rowView("row_board_tab"),
                new int[]{0, 1}, new int[]{COLOR_PRIMARY, COLOR_SKY}, null);
        boardTab.randomizable = false;
        ChoiceSetting stepRate = new ChoiceSetting(PREF_FIXED_STEP_RATE, DEFAULT_FIXED_STEP_RATE, rowView("row_fixed_step_rate"),
                STEP_RATES, new int[]{COLOR_SKY, COLOR_PRIMARY, 0xFF9C8CFF}, null);
        stepRate.randomizable = false;
        ChoiceSetting stepTiming = new ChoiceSetting(PREF_FIXED_STEP_TIMING, DEFAULT_FIXED_STEP_TIMING, rowView("row_fixed_step_timing"),
                new int[]{STEP_TIMING_SMOOTH, STEP_TIMING_ORIGINAL}, new int[]{COLOR_PRIMARY, COLOR_SKY}, null);
        stepTiming.randomizable = false;
        ChoiceSetting frameLimit = new ChoiceSetting(PREF_FRAME_RATE_LIMIT, DEFAULT_FRAME_RATE_LIMIT, rowView("row_frame_rate_limit"),
                FRAME_LIMITS, new int[]{COLOR_PRIMARY, 0xFFFC7858, COLOR_SKY, 0xFF9C8CFF, 0xFF9BE15D}, null);
        frameLimit.randomizable = false;
        frameLimit.note = "used in the game";
        ChoiceSetting boardSize = new ChoiceSetting(PREF_BOARD_SIZE, DEFAULT_BOARD_SIZE, rowView("row_board_size"),
                new int[]{0, 1, 2, 3}, new int[]{COLOR_PRIMARY, COLOR_SKY, 0xFF9C8CFF, 0xFF9BE15D}, null);
        boardSize.randomizable = false;
        boardSize.note = "used in the game";
        ToggleSetting replayEnabled = toggle(PREF_REPLAY_ENABLED, DEFAULT_REPLAY_ENABLED, rowView("row_replay_enabled"));
        replayEnabled.note = "used in the game";
        replayEnabled.randomizable = false;
        ChoiceSetting replayMode = new ChoiceSetting(PREF_REPLAY_MODE, DEFAULT_REPLAY_MODE, rowView("row_replay_mode"),
                new int[]{REPLAY_MODE_RECENT, REPLAY_MODE_BEST}, new int[]{COLOR_SKY, COLOR_PRIMARY}, null);
        replayMode.randomizable = false;
        ChoiceSetting replayKeep = new ChoiceSetting(PREF_REPLAY_KEEP, DEFAULT_REPLAY_KEEP, rowView("row_replay_keep"),
                REPLAY_KEEPS, new int[]{COLOR_SKY, COLOR_PRIMARY, 0xFF9C8CFF, 0xFF9BE15D}, null);
        replayKeep.randomizable = false;
        ChoiceSetting replayResolution = new ChoiceSetting(PREF_REPLAY_RESOLUTION, DEFAULT_REPLAY_RESOLUTION, rowView("row_replay_resolution"),
                new int[]{REPLAY_RESOLUTION_ORIGINAL, REPLAY_RESOLUTION_1080, REPLAY_RESOLUTION_720}, new int[]{COLOR_PRIMARY, COLOR_SKY, 0xFF9C8CFF}, null);
        replayResolution.randomizable = false;
        ChoiceSetting replayFps = new ChoiceSetting(PREF_REPLAY_FPS, DEFAULT_REPLAY_FPS, rowView("row_replay_fps"),
                REPLAY_FPS_CHOICES, new int[]{COLOR_SKY, COLOR_PRIMARY}, null);
        replayFps.randomizable = false;
        ChoiceSetting replayQuality = new ChoiceSetting(PREF_REPLAY_QUALITY, DEFAULT_REPLAY_QUALITY, rowView("row_replay_quality"),
                new int[]{0, 1, 2}, new int[]{COLOR_SKY, COLOR_PRIMARY, 0xFF9C8CFF}, null);
        replayQuality.randomizable = false;
        ToggleSetting replaySound = toggle(PREF_REPLAY_SOUND, DEFAULT_REPLAY_SOUND, rowView("row_replay_sound"));
        replaySound.randomizable = false;
        toggle(PREF_REPLAY_CREDIT, DEFAULT_REPLAY_CREDIT, rowView("row_replay_credit")).randomizable = false;
        rowView("button_replays").setOnClickListener(v -> openReplays());
        rowView("button_replays_quick").setOnClickListener(v -> openReplays());
        rowView("button_board_rules").setOnClickListener(v -> showScoreRules());
        rowView("button_board_name").setOnClickListener(v -> showNameDialog(this, Leaderboard.name(prefs), false, (name, result) ->
                Leaderboard.setName(this, name, this::runOnUiThread, (status, error) -> {
                    result.done(error);
                    refreshBoardName();
                    refreshAbout();
                    if (Leaderboard.OFFLINE.equals(status)) announce("Name saved. It will be checked when you're online", null, null);
                })));
    }

    public interface NameResult {
        void done(String error);
    }

    public interface NameSaver {
        void save(String name, NameResult result);
    }

    public static AlertDialog showNameDialog(Activity activity, String current, final boolean firstTime, final NameSaver saver) {
        Context themed = new ContextThemeWrapper(activity, R.style.Theme_Material3Expressive_Dark_NoActionBar);
        float density = activity.getResources().getDisplayMetrics().density;
        final TextInputLayout layout = new TextInputLayout(themed);
        layout.setHint("Name");
        layout.setCounterEnabled(true);
        layout.setCounterMaxLength(Leaderboard.NAME_MAX);
        layout.setHelperText("Letters, numbers, spaces and _");
        layout.setEndIconMode(TextInputLayout.END_ICON_CLEAR_TEXT);
        final TextInputEditText field = new TextInputEditText(layout.getContext());
        field.setSingleLine(true);
        field.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS | InputType.TYPE_TEXT_VARIATION_PERSON_NAME);
        field.setImeOptions(EditorInfo.IME_ACTION_DONE);
        field.setFilters(new InputFilter[] {new InputFilter.LengthFilter(Leaderboard.NAME_MAX)});
        if (current != null && !current.isEmpty()) {
            field.setText(current);
            field.setSelection(field.getText() == null ? 0 : field.getText().length());
        }
        layout.addView(field, new LinearLayout.LayoutParams(MATCH, ViewGroup.LayoutParams.WRAP_CONTENT));
        styleNameField(layout, field, density);
        FrameLayout box = new FrameLayout(themed);
        int side = Math.round(24 * density);
        box.setPadding(side, Math.round(8 * density), side, 0);
        box.addView(layout, new FrameLayout.LayoutParams(MATCH, ViewGroup.LayoutParams.WRAP_CONTENT));
        String message = firstTime
                ? "This name appears on the online leaderboard next to your scores.\n\nFair rounds are posted automatically when you're online, "
                + "along with an anonymous ID. You can turn that off in Settings > Leaderboard, or remove your entry in Settings > About."
                : "Shown on the online leaderboard next to your scores.";
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(themed)
                .setTitle(firstTime ? "Choose your name" : "Leaderboard name")
                .setMessage(message)
                .setView(box)
                .setPositiveButton(firstTime ? "Let's go" : "Save", null);
        if (!firstTime) builder.setNegativeButton("Cancel", null);
        final AlertDialog dialog = builder.create();
        dialog.setCancelable(!firstTime);
        dialog.setCanceledOnTouchOutside(!firstTime);
        final boolean[] busy = new boolean[1];
        final Runnable submit = () -> {
            if (busy[0]) return;
            String name = Leaderboard.cleanName(field.getText() == null ? "" : field.getText().toString());
            if (name.isEmpty()) {
                layout.setError("Use at least one letter or number");
                return;
            }
            busy[0] = true;
            layout.setError(null);
            layout.setHelperText("Checking...");
            Button positive = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            if (positive != null) positive.setEnabled(false);
            saver.save(name, error -> {
                busy[0] = false;
                layout.setHelperText("Letters, numbers, spaces and _");
                if (error == null) {
                    dialog.dismiss();
                    return;
                }
                layout.setError(error);
                Button again = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
                if (again != null) again.setEnabled(true);
            });
        };
        field.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence text, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence text, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable text) {
                if (busy[0]) return;
                boolean valid = !Leaderboard.cleanName(text.toString()).isEmpty();
                layout.setError(valid || text.length() == 0 ? null : "Use at least one letter or number");
                Button positive = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
                if (positive != null) positive.setEnabled(valid);
            }
        });
        field.setOnEditorActionListener((view, action, event) -> {
            if (action != EditorInfo.IME_ACTION_DONE) return false;
            submit.run();
            return true;
        });
        dialog.setOnShowListener(shown -> {
            int[][] states = {{-android.R.attr.state_enabled}, {}};
            ColorStateList buttonText = new ColorStateList(states, new int[] {0x61E8ECF6, COLOR_PRIMARY});
            for (int type : new int[] {AlertDialog.BUTTON_POSITIVE, AlertDialog.BUTTON_NEGATIVE}) {
                Button button = dialog.getButton(type);
                if (button == null) continue;
                button.setTextColor(buttonText);
                if (button instanceof MaterialButton) ((MaterialButton) button).setRippleColor(ColorStateList.valueOf(COLOR_PRIMARY_RIPPLE));
            }
            Button positive = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            if (positive != null) {
                positive.setOnClickListener(view -> submit.run());
                positive.setEnabled(!Leaderboard.cleanName(field.getText() == null ? "" : field.getText().toString()).isEmpty());
            }
            field.requestFocus();
        });
        if (dialog.getWindow() != null) dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        dialog.show();
        return dialog;
    }

    private static void styleNameField(TextInputLayout layout, TextInputEditText field, float density) {
        int[][] focus = {{android.R.attr.state_focused}, {}};
        layout.setBoxBackgroundColor(COLOR_HIGH);
        layout.setBoxStrokeColorStateList(new ColorStateList(focus, new int[] {COLOR_PRIMARY, COLOR_OUTLINE}));
        layout.setHintTextColor(ColorStateList.valueOf(COLOR_PRIMARY));
        layout.setDefaultHintTextColor(ColorStateList.valueOf(COLOR_ON_VARIANT));
        layout.setHelperTextColor(ColorStateList.valueOf(COLOR_ON_VARIANT));
        layout.setCounterTextColor(ColorStateList.valueOf(COLOR_ON_VARIANT));
        layout.setEndIconTintList(ColorStateList.valueOf(COLOR_ON_VARIANT));
        field.setTextColor(COLOR_ON);
        field.setHighlightColor(COLOR_SELECTION);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            GradientDrawable cursor = new GradientDrawable();
            cursor.setColor(COLOR_PRIMARY);
            cursor.setSize(Math.max(1, Math.round(2 * density)), Math.round(20 * density));
            field.setTextCursorDrawable(cursor);
            field.setTextSelectHandle(tinted(field.getTextSelectHandle()));
            field.setTextSelectHandleLeft(tinted(field.getTextSelectHandleLeft()));
            field.setTextSelectHandleRight(tinted(field.getTextSelectHandleRight()));
        }
    }

    private void refreshBoardName() {
        TextView summary = summaries.get("button_board_name");
        if (summary == null) return;
        String name = Leaderboard.name(prefs);
        summary.setText(name.isEmpty() ? "Not set" : Leaderboard.nameRefused(prefs) ? name + "  \u00B7  not allowed, choose another"
                : Leaderboard.nameTaken(prefs) ? name + "  \u00B7  taken, choose another" : name);
        TextView rules = summaries.get("button_board_rules");
        if (rules != null) rules.setText(leaderboardStatus());
    }

    private void showScoreRules() {
        String message = "A round counts for the online leaderboard when:\n"
                + "\u2022 every gameplay setting is at its default, or matches Classic feel exactly (shown with a CLASSIC label)\n"
                + "\u2022 you're online when the round starts and when it ends\n"
                + "\u2022 nothing in the game is changed or sped up while you play\n\n"
                + "These never matter: colors, backgrounds, bird and pipe looks, sound, vibration, rain and snow, day and night, "
                + "screen shake, motion blur, dimming, the crash flash, bird tilt, controls, the FPS counter, hitbox outlines, "
                + "the starting height and Fixed 60 Hz physics. Classic feel also counts with Fixed 60 Hz physics or "
                + "whole-pixel movement turned off.\n\n"
                + "These stop scores from counting: God mode, autopilot, extra lives, golden pipes, thunderstorm, and any change "
                + "to gravity, flap strength, speed, pipes, bird size or hitbox, falling rules, scoring or the difficulty ramp, "
                + "unless it matches Classic feel. Whole-pixel movement together with Fixed 60 Hz physics changes how the bird "
                + "falls, so that pair only counts with Classic feel.\n\n"
                + "Pausing is fine. Rounds played offline stay on this phone and are not posted later.\n\n"
                + "Verified scores: when you post a new best, a recording of your taps is sent too. A checker replays it with the "
                + "real game rules, and if it plays out to the same score, the score gets a purple check mark. The leaderboard "
                + "shows verified scores first; tap SHOW UNVERIFIED to see every posted score. Checking usually takes a few "
                + "minutes. Turning or resizing the screen during a round, or changing settings mid-round, means that round "
                + "can't be checked.\n\n"
                + "Right now: " + leaderboardStatus() + ".";
        showDialog(new MaterialAlertDialogBuilder(this)
                .setTitle("What counts as a real score")
                .setMessage(message)
                .setPositiveButton("Got it", null));
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

    private Feel settingsFeel;
    private long sliderFeltAt;

    private void syncFeel() {
        if (settingsFeel == null) settingsFeel = new Feel(this, root);
        if (settingsByKey.get(PREF_HAPTIC_MENUS) == null) {
            settingsFeel.load(prefs.getAll());
            return;
        }
        settingsFeel.set(flag(PREF_HAPTIC_FEEDBACK_ENABLED), option(PREF_HAPTIC_STRENGTH), flag(PREF_HAPTIC_FLAP), flag(PREF_HAPTIC_SCORE),
                flag(PREF_HAPTIC_CRASH), flag(PREF_HAPTIC_BEST), flag(PREF_HAPTIC_MENUS));
    }

    private Feel feel() {
        if (settingsFeel == null) syncFeel();
        return settingsFeel;
    }

    private static int feelSample(String key) {
        if (key.equals(PREF_HAPTIC_FLAP)) return Feel.FLAP;
        if (key.equals(PREF_HAPTIC_SCORE)) return Feel.GOLDEN;
        if (key.equals(PREF_HAPTIC_CRASH)) return Feel.CRASH;
        if (key.equals(PREF_HAPTIC_BEST)) return Feel.BEST;
        return Feel.TOGGLE;
    }

    private void feelChange(Setting setting) {
        if (setting == vibration && vibration.value) settingsFeel.play(Feel.CONFIRM);
        else if (setting.key.equals(PREF_HAPTIC_STRENGTH)) settingsFeel.play(Feel.CRASH);
        else if (setting.key.startsWith("pref_haptic_") && setting instanceof ToggleSetting && ((ToggleSetting) setting).value) settingsFeel.play(feelSample(setting.key));
        else if (setting instanceof ToggleSetting) settingsFeel.play(Feel.TOGGLE);
        else settingsFeel.play(Feel.TICK);
    }

    private void feelSlider(boolean landmark) {
        long now = SystemClock.uptimeMillis();
        if (!landmark && now - sliderFeltAt < 45L) return;
        sliderFeltAt = now;
        feel().play(landmark ? Feel.PRESS : Feel.TICK);
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
        for (Setting twin : twins) {
            twin.restore(twin.source.capture());
            twin.syncView();
        }
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
        refreshBoardName();
        String source = changeSource != null ? changeSource : "changed in settings";
        List<String> changes = new ArrayList<>();
        for (Setting setting : settings) {
            if (!unsaved.contains(setting)) continue;
            String now = setting.textValue();
            String before = loggedValues.get(setting.key);
            if (now.equals(before)) continue;
            loggedValues.put(setting.key, now);
            changes.add(textName(setting) + ": " + before + " -> " + now + "  (" + source + ")");
        }
        boolean logOn = activityLog == null || activityLog.value;
        if (logOn || ActivityLog.isEnabled(this)) {
            ActivityLog.setEnabled(true);
            ActivityLog.add(this, "SETTING", changes);
        }
        ActivityLog.setEnabled(logOn);
        changeSource = null;
        unsaved.clear();
    }

    private void onUserChange(Setting setting) {
        if (setting.source != null) {
            Setting origin = setting.source;
            origin.restore(setting.capture());
            origin.syncView();
            setting = origin;
        }
        syncTwin(setting);
        String extra = null;
        if (setting == weather && weather.value == WEATHER_STORM && !screenShake.value) {
            screenShake.value = true;
            show(screenShake);
            scheduleSave(screenShake);
            extra = "screen shake turned on for thunder";
        }
        if (setting == drunkMode && drunkMode.value && drunkStrength.value <= 0f) {
            drunkStrength.value = 1f;
            show(drunkStrength);
            scheduleSave(drunkStrength);
        }
        if (setting == activityLog && !activityLog.value) {
            extra = "not recommended";
            announce("Activity log off. Bug reports won't show what happened", "Turn on", v -> {
                activityLog.value = true;
                show(activityLog);
                scheduleSave(activityLog);
                showCaption("Activity log: on");
            });
        }
        syncFeel();
        if (!(setting instanceof NumberSetting)) feelChange(setting);
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
                rowOf(PREF_FIXED_STEP_RATE), rowOf(PREF_FIXED_STEP_TIMING),
                rowOf(PREF_RAMP_TOP_SPEED), rowOf(PREF_RAMP_MIN_GAP),
                rowOf(PREF_SOUND_VOLUME), rowOf(PREF_WEATHER_VOLUME),
                rowOf(PREF_HAPTIC_STRENGTH), rowOf(PREF_HAPTIC_FLAP), rowOf(PREF_HAPTIC_SCORE),
                rowOf(PREF_HAPTIC_CRASH), rowOf(PREF_HAPTIC_BEST), rowOf(PREF_HAPTIC_MENUS),
                rowOf(PREF_KEY_FLAP), rowOf(PREF_KEY_FLAP_ALT), rowOf(PREF_KEY_PAUSE),
                rowOf(PREF_KEY_PLAY), rowOf(PREF_KEY_SETTINGS),
                rowOf(PREF_GOD_FALL_ENABLED), rowOf(PREF_GOD_FALL_SECONDS),
                rowOf(PREF_REPLAY_MODE), rowOf(PREF_REPLAY_KEEP)};
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
                fixedStep.value, fixedStep.value,
                number(PREF_SPEED_RAMP) > 0f, number(PREF_GAP_SHRINK) > 0f,
                sound.value, sound.value,
                vibration.value, vibration.value, vibration.value,
                vibration.value, vibration.value, vibration.value,
                keyboard.value, keyboard.value, keyboard.value,
                keyboard.value, keyboard.value,
                flag(PREF_NO_CLIP_ENABLED), flag(PREF_NO_CLIP_ENABLED) && flag(PREF_GOD_FALL_ENABLED),
                flag(PREF_REPLAY_ENABLED), flag(PREF_REPLAY_ENABLED) && option(PREF_REPLAY_MODE) == REPLAY_MODE_RECENT};

        if (!twins.isEmpty()) {
            List<View> allRows = new ArrayList<>(Arrays.asList(rows));
            List<Boolean> allShown = new ArrayList<>();
            for (boolean one : shown) allShown.add(one);
            for (int i = 0; i < rows.length; i++) {
                Setting owner = rowSettings.get(rows[i]);
                if (owner != null && owner.twin != null) {
                    allRows.add(owner.twin.row);
                    allShown.add(shown[i]);
                }
            }
            rows = allRows.toArray(new View[0]);
            shown = new boolean[allShown.size()];
            for (int i = 0; i < shown.length; i++) shown[i] = allShown.get(i);
        }
        boolean changed = false;
        for (int i = 0; i < rows.length; i++) {
            if (dependencyHidden.contains(rows[i]) == shown[i]) changed = true;
        }
        if (changed) {
            for (int i = 0; i < rows.length; i++) {
                if (shown[i]) dependencyHidden.remove(rows[i]);
                else dependencyHidden.add(rows[i]);
            }
            if (searching) {
                applySearch(searchQuery, false);
            } else {
                if (animate) TransitionManager.beginDelayedTransition(content, new AutoTransition().setDuration(180L));
                for (int i = 0; i < rows.length; i++) rows[i].setVisibility(shown[i] ? View.VISIBLE : View.GONE);
            }
        }
        if ((changed || forceStyle) && !searching) styleAllGroups();

        background.setEnabled(!dayNight.value);
        pipeColor.setEnabled(!randomPipeColors.value);
        refreshClassicSummary();
        refreshClassicTab();
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
        resolveAllKeyConflicts(null);
        syncAllViews();
        unsaved.addAll(settings);
        flushSave();
        refreshDependencies(false, true);
        pushPreview();
    }

    private void applyBulkChange(Runnable change, final String message) {
        flushSave();
        final Map<String, Object> before = snapshot();
        changeSource = message;
        change.run();
        afterBulkChange();
        showCaption(message);
        announce(message, "Undo", v -> {
            flushSave();
            changeSource = "undo of: " + message;
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

        rowView("button_reset_all").setOnClickListener(v -> confirmReset());
        rowView("button_reset_all_quick").setOnClickListener(v -> confirmReset());

        highScoreSummary = summaries.get("button_reset_high_score");
        classicSummary = summaries.get("button_classic_feel");
        refreshHighScore();
        View resetScore = rowView("button_reset_high_score");
        resetScore.setOnClickListener(v -> confirm("Reset high score",
                "Your best score of " + storedHighScore() + " will be erased.", "Reset",
                () -> {
                    ActivityLog.add(this, "SCORE", "High score reset from " + storedHighScore() + " (best earned by playing was " + earnedHighScore(prefs) + ")");
                    prefs.edit().remove(KEY_HIGH_SCORE).apply();
                    recordEarnedScore(prefs, 0, "");
                    refreshHighScore();
                    announce("High score reset", null, null);
                }));

        rowView("button_classic_feel").setOnClickListener(v -> applyBulkChange(this::applyClassicFeel, "Classic feel applied"));
        rowView("button_standard_feel").setOnClickListener(v -> applyBulkChange(this::applyStandardFeel, "Standard feel restored"));
        rowView("button_export_settings").setOnClickListener(v -> startExport());
        rowView("button_import_settings").setOnClickListener(v -> startImport());
        rowView("button_share_settings").setOnClickListener(v -> shareSettings());
        rowView("button_paste_settings").setOnClickListener(v -> pasteSettings());
        rowView("button_edit_text").setOnClickListener(v -> openTextEditor());
        rowView("button_activity_log").setOnClickListener(v -> showActivityLog());

        View deleteAll = rowView("button_reset_profiles");
        deleteAll.setOnClickListener(v -> confirm("Delete saved setups",
                "This clears all three slots and cannot be undone.", "Delete",
                () -> {
                    for (int slot = 1; slot <= PROFILE_COUNT; slot++) profile(slot).edit().clear().apply();
                    refreshSlots();
                    ActivityLog.add(this, "FILE", "All saved setups deleted");
                    announce("All saved setups deleted", null, null);
                }));
    }

    private void confirmReset() {
        confirm("Reset to defaults",
                "Every setting goes back to its default. Saved setups and your high score are kept.", "Reset",
                () -> applyBulkChange(() -> {
                    for (Setting setting : settings) setting.reset();
                }, "Settings reset to defaults"));
    }

    private int storedHighScore() {
        Object stored = prefs.getAll().get(KEY_HIGH_SCORE);
        return stored instanceof Integer ? (Integer) stored : 0;
    }

    private void refreshHighScore() {
        highScoreSummary.setText(highScoreStatus());
    }

    private String highScoreStatus() {
        int shown = storedHighScore();
        int earned = earnedHighScore(prefs);
        String assists = highScoreAssists(prefs);
        StringBuilder text = new StringBuilder("Current high score: ").append(shown);
        if (!highScoreSealIntact(prefs)) {
            text.append("\nThe saved record was changed outside the game, so this score can't be trusted");
        } else if (shown != earned) {
            text.append("\nChanged by hand. Best earned by playing: ").append(earned);
        } else if (!assists.isEmpty()) {
            text.append("\nSet with ").append(assists);
        } else if (shown > 0) {
            text.append(" \u00B7 earned by playing");
        }
        return text.toString();
    }

    static final String REPLAYS_OPENED = "replaysOpened";
    private ImageView replayHeroImage;
    private TextView replayHeroBadge;
    private ExecutorService replayHeroWorker;
    private int replayHeroToken;

    private void replayHeroRow(LinearLayout group) {
        LinearLayout row = addRow(group, "button_replays_quick", true, 12, 12);
        row.setMinimumHeight(dp(104));
        row.setPadding(dp(12), dp(12), dp(16), dp(12));
        replayHeroImage = new ImageView(this);
        replayHeroImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
        replayHeroImage.setBackground(rounded(COLOR_PRIMARY_CONTAINER, dp(12)));
        replayHeroImage.setClipToOutline(true);
        replayHeroImage.setImageDrawable(new IconDrawable(IconDrawable.PLAY, dp(28), dp(2f), COLOR_ON_PRIMARY_CONTAINER));
        replayHeroImage.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
        LinearLayout.LayoutParams imageParams = linear(dp(60), dp(80));
        imageParams.setMarginEnd(dp(14));
        row.addView(replayHeroImage, imageParams);
        addTexts(row, "button_replays_quick", "Watch replays", "Every round is recorded. Watch it again or save it as a video", true, COLOR_PRIMARY);
        replayHeroBadge = newText("NEW", 11, COLOR_ON_PRIMARY_CONTAINER, pixelBold != null ? pixelBold : pixelFont);
        replayHeroBadge.setLetterSpacing(0.08f);
        replayHeroBadge.setBackground(rounded(COLOR_PRIMARY_CONTAINER, dp(10)));
        replayHeroBadge.setPadding(dp(10), dp(3), dp(10), dp(3));
        LinearLayout.LayoutParams badgeParams = linear(WRAP, WRAP);
        badgeParams.setMarginStart(dp(12));
        row.addView(replayHeroBadge, badgeParams);
    }

    private void refreshReplayHero() {
        if (replayHeroImage == null || isFinishing()) return;
        Map<String, ?> all = prefs.getAll();
        Object opened = all.get(REPLAYS_OPENED);
        replayHeroBadge.setVisibility(opened instanceof Boolean && (Boolean) opened ? View.GONE : View.VISIBLE);
        Object keepValue = all.get(PREF_REPLAY_KEEP);
        final int keep = replayKeep(keepValue instanceof Integer ? (Integer) keepValue : DEFAULT_REPLAY_KEEP);
        Object enabled = all.get(PREF_REPLAY_ENABLED);
        final boolean recording = enabled instanceof Boolean ? (Boolean) enabled : DEFAULT_REPLAY_ENABLED;
        final File dir = new File(getFilesDir(), GameView.FILM_DIR);
        final int token = ++replayHeroToken;
        if (replayHeroWorker == null) {
            replayHeroWorker = Executors.newSingleThreadExecutor(runnable -> {
                Thread thread = new Thread(runnable, "ReplayCard");
                thread.setPriority(Thread.MIN_PRIORITY);
                return thread;
            });
        }
        try {
            replayHeroWorker.execute(() -> {
                List<File> films = GameView.newestFilms(dir, keep);
                GameView.Film info = null;
                File newest = null;
                for (File file : films) {
                    try {
                        info = GameView.Film.readInfo(file);
                        newest = file;
                        break;
                    } catch (IOException e) {
                        info = null;
                    }
                }
                final GameView.Film latest = info;
                final int count = films.size();
                final Bitmap still = newest != null ? GameView.snapshot(SettingsActivity.this, newest) : null;
                runOnUiThread(() -> showReplayHero(token, latest, count, still, recording));
            });
        } catch (RejectedExecutionException ignored) {
        }
    }

    private void showReplayHero(int token, GameView.Film latest, int count, Bitmap still, boolean recording) {
        if (token != replayHeroToken || isDestroyed() || replayHeroImage == null) return;
        TextView summary = summaries.get("button_replays_quick");
        String text;
        if (latest != null) {
            text = "Latest round: score " + latest.score + ", " + replayWhen(latest.savedAt)
                    + (count > 1 ? "  ·  " + count + " saved" : "") + ". Watch it or save it as a video";
        } else if (recording) {
            text = "Every round is recorded. Play one, then watch it here or save it as a video";
        } else {
            text = "Recording is off. Turn on Record replays in System to save your rounds";
        }
        if (summary != null) summary.setText(text);
        rowView("button_replays_quick").setContentDescription("Watch replays. " + text);
        if (still != null) {
            boolean fresh = !(replayHeroImage.getTag() instanceof Bitmap);
            replayHeroImage.setTag(still);
            replayHeroImage.setImageBitmap(still);
            if (fresh) {
                replayHeroImage.setAlpha(0f);
                replayHeroImage.animate().alpha(1f).setDuration(260).start();
            }
        } else {
            replayHeroImage.setTag(null);
            replayHeroImage.setImageDrawable(new IconDrawable(IconDrawable.PLAY, dp(28), dp(2f), COLOR_ON_PRIMARY_CONTAINER));
        }
    }

    private String replayWhen(long time) {
        java.util.Date date = new java.util.Date(time);
        String clock = android.text.format.DateFormat.getTimeFormat(this).format(date);
        if (android.text.format.DateUtils.isToday(time)) return "today " + clock;
        if (android.text.format.DateUtils.isToday(time + android.text.format.DateUtils.DAY_IN_MILLIS)) return "yesterday " + clock;
        return android.text.format.DateFormat.getMediumDateFormat(this).format(date) + " " + clock;
    }

    private void openReplays() {
        prefs.edit().putBoolean(REPLAYS_OPENED, true).apply();
        if (replayHeroBadge != null) replayHeroBadge.setVisibility(View.GONE);
        Intent replays = new Intent();
        replays.setClassName(getPackageName(), SettingsActivity.class.getPackage().getName() + ".ReplayActivity");
        try {
            startActivity(replays);
        } catch (RuntimeException e) {
            announce("Replays aren't available in this build", null, null);
        }
    }

    private boolean isPersonal(Setting setting) {
        String key = setting.key;
        return key.equals(PREF_SOUND_ENABLED) || key.equals(PREF_HAPTIC_FEEDBACK_ENABLED) || key.startsWith("pref_haptic_")
                || key.equals(PREF_HIDE_SETTINGS_ICON) || key.equals(PREF_PAUSE_BUTTON_ENABLED)
                || key.equals(PREF_SHOW_TAP_GUIDE) || key.equals(PREF_SHOW_GET_READY) || key.equals(PREF_SHOW_SCORE)
                || key.equals(PREF_AUTO_PAUSE_ENABLED) || key.equals(PREF_SOUND_VOLUME)
                || key.equals(PREF_WEATHER_VOLUME) || key.equals(PREF_SHOW_FPS) || key.equals(PREF_SHOW_HITBOXES)
                || key.equals(PREF_CLASSIC_MENU_ENABLED) || key.equals(PREF_KEYBOARD_ENABLED)
                || key.equals(PREF_RIGHT_CLICK_PAUSES) || key.equals(PREF_HIDE_CURSOR) || key.startsWith("pref_replay_")
                || setting instanceof KeySetting;
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

    private static final Object[][] CLASSIC_VALUES = {
            {"Flight", PREF_GRAVITY, CLASSIC_GRAVITY},
            {"Flight", PREF_JUMP_STRENGTH, CLASSIC_FLAP},
            {"Flight", PREF_MAX_FALL_SPEED, CLASSIC_MAX_FALL_SPEED},
            {"Flight", PREF_CEILING_ENABLED, false},
            {"Flight", PREF_FLAP_LIMIT_ENABLED, true},
            {"Flight", PREF_CRASH_GRAVITY, 1f},
            {"Pipes", PREF_GAME_SPEED, CLASSIC_SPEED},
            {"Pipes", PREF_PIPE_GAP, CLASSIC_GAP},
            {"Pipes", PREF_PIPE_SPACING, CLASSIC_SPACING},
            {"Pipes", PREF_GAP_PLACEMENT, GAP_PLACEMENT_CLASSIC},
            {"Pipes", PREF_MOVING_PIPES_ENABLED, false},
            {"Bird", PREF_HITBOX_SHAPE, HITBOX_SHAPE_SQUARE},
            {"Bird", PREF_BIRD_POSITION, (float) CLASSIC_BIRD_POSITION},
            {"Bird", PREF_START_HEIGHT, (float) CLASSIC_START_HEIGHT},
            {"Bird", PREF_WING_SPEED, CLASSIC_WING_SPEED},
            {"Bird", PREF_TILT_STYLE, TILT_STYLE_CLASSIC},
            {"Bird", PREF_MAX_TILT_UP, (float) CLASSIC_TILT_UP},
            {"Bird", PREF_MAX_TILT_DOWN, (float) CLASSIC_TILT_DOWN},
            {"Timing", PREF_ORIGINAL_MOTION_ENABLED, true},
            {"Timing", PREF_FIXED_STEP_ENABLED, false},
            {"Timing", PREF_PIXEL_STEPS_ENABLED, false},
            {"Look", PREF_BG_SCROLL_SPEED, 0f},
            {"Look", PREF_GAMEOVER_OPACITY, 0f},
            {"Look", PREF_SCREEN_SHAKE_ENABLED, false},
            {"Look", PREF_CRASH_FLASH_ENABLED, true},
            {"Look", PREF_CRASH_FLASH_LENGTH, (float) CLASSIC_FLASH_LENGTH},
            {"Look", PREF_CRASH_FLASH_FADE, FLASH_FADE_SHARP},
            {"Scoring", PREF_SCORE_POINT, SCORE_POINT_CLASSIC},
    };

    private static final Set<String> CLASSIC_WHOLE = new HashSet<>(Arrays.asList(PREF_BIRD_POSITION, PREF_START_HEIGHT,
            PREF_MAX_TILT_UP, PREF_MAX_TILT_DOWN, PREF_CRASH_FLASH_LENGTH, PREF_GAMEOVER_OPACITY));

    static void writeClassicFeel(SharedPreferences.Editor editor) {
        for (Object[] entry : CLASSIC_VALUES) {
            String key = (String) entry[1];
            Object value = entry[2];
            if (value instanceof Boolean) editor.putBoolean(key, (Boolean) value);
            else if (value instanceof Integer) editor.putInt(key, (Integer) value);
            else if (CLASSIC_WHOLE.contains(key)) editor.putInt(key, Math.round((Float) value));
            else editor.putFloat(key, (Float) value);
        }
    }

    private void setClassicValue(String key, Object value) {
        if (value instanceof Boolean) setFlag(key, (Boolean) value);
        else if (value instanceof Float) setNumber(key, (Float) value);
        else setOption(key, (Integer) value);
    }

    private void applyClassicFeel() {
        for (Setting setting : settings) {
            if (!isPersonal(setting)) setting.reset();
        }
        for (Object[] entry : CLASSIC_VALUES) setClassicValue((String) entry[1], entry[2]);
    }

    private void applyStandardFeel() {
        for (Object[] entry : CLASSIC_VALUES) {
            Setting setting = settingsByKey.get(entry[1]);
            if (setting != null) setting.reset();
        }
    }

    private void buildClassicTwins() {
        LinearLayout page = (LinearLayout) pages[CLASSIC_PAGE];
        String section = null;
        LinearLayout group = null;
        buildingTwin = true;
        try {
            for (Object[] entry : CLASSIC_VALUES) {
                Setting source = settingsByKey.get(entry[1]);
                if (source == null) continue;
                if (!entry[0].equals(section)) {
                    section = (String) entry[0];
                    group = addGroup(page, section);
                }
                String name = "classic_" + source.key;
                if (source instanceof ToggleSetting) {
                    toggleRow(group, name, source.label, "Classic");
                } else if (source instanceof NumberSetting) {
                    sliderRow(group, name, source.label, "Classic", source.describe());
                } else if (source instanceof ChoiceSetting) {
                    choiceRowWithNote(group, name, source.label, "Classic", ((ChoiceSetting) source).names);
                } else {
                    continue;
                }
                Setting twin = source.twinFor(rowView(name));
                twin.source = source;
                twin.classicValue = entry[2];
                source.twin = twin;
                twinRows.add(twin.row);
                twins.add(twin);
            }
        } finally {
            buildingTwin = false;
        }
    }

    private void syncTwin(Setting setting) {
        Setting twin = setting.twin;
        if (twin == null) return;
        twin.restore(setting.capture());
        twin.syncView();
    }

    private void show(Setting setting) {
        setting.syncView();
        syncTwin(setting);
    }

    private void refreshClassicTab() {
        if (twins.isEmpty()) return;
        int matching = 0;
        for (Setting twin : twins) {
            Object current = twin.capture();
            twin.restore(twin.classicValue);
            String classic = twin.describe();
            twin.restore(current);
            boolean same = twin.classicValue.equals(twin.source.capture());
            if (same) matching++;
            TextView note = summaries.get("classic_" + twin.key);
            if (note != null) {
                note.setText(same ? "Classic: " + classic + "  (matches)" : "Classic: " + classic);
                note.setTextColor(same ? COLOR_PRIMARY : COLOR_ON_VARIANT);
            }
        }
        View status = rowView("row_classic_status");
        TextView pill = (TextView) status.findViewWithTag(TAG_VALUE);
        if (pill != null) pill.setText(matching + "/" + twins.size());
        TextView summary = summaries.get("row_classic_status");
        if (summary != null) {
            summary.setText(matching == twins.size() ? "Every setting below matches Classic feel"
                    : (twins.size() - matching) + " setting" + (twins.size() - matching == 1 ? "" : "s") + " below differ from Classic feel");
        }
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
            TextImport fromText = parseSettingsText(text);
            if (!fromText.values.isEmpty() || !fromText.hidden.isEmpty()) {
                applySettingsText(text, false);
                return;
            }
            ActivityLog.add(this, "FILE", "Import refused: not a settings file");
            announce("That is not a Flappy Bird settings file", null, null);
            return;
        }
        ActivityLog.add(this, "FILE", "Imported a .json settings file with " + imported.size() + " settings");
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
        if (requestCode != REQUEST_EXPORT && requestCode != REQUEST_IMPORT && requestCode != REQUEST_EXPORT_LOG) return;
        if (resultCode != RESULT_OK || data == null || data.getData() == null) return;
        if (requestCode == REQUEST_EXPORT_LOG) writeLogExport(data.getData());
        else if (requestCode == REQUEST_EXPORT) writeExport(data.getData());
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
            ActivityLog.add(this, "FILE", "Settings exported to a file");
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

    private interface Fact {
        String get() throws Exception;
    }

    private static void fact(StringBuilder out, String label, Fact fact) {
        String value;
        try {
            value = fact.get();
        } catch (Throwable e) {
            value = "unavailable";
        }
        if (value != null) out.append(label).append(": ").append(value).append('\n');
    }

    private String logReport() {
        flushSave();
        final String log = ActivityLog.read(this);
        int entries = 0;
        for (int i = 0; i < log.length(); i++) {
            if (log.charAt(i) == '\n' && i + 1 < log.length() && log.charAt(i + 1) != ' ') entries++;
        }
        if (!log.isEmpty()) entries++;
        final int entryCount = entries;
        StringBuilder out = new StringBuilder("Flappy Bird activity log\n");
        out.append("Exported: ").append(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z", Locale.US).format(new Date())).append('\n');
        out.append("\n----- App -----\n");
        fact(out, "Version", () -> appVersion(this));
        fact(out, "Package", this::getPackageName);
        fact(out, "Installed / updated", () -> {
            android.content.pm.PackageInfo info = getPackageManager().getPackageInfo(getPackageName(), 0);
            SimpleDateFormat day = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US);
            return day.format(new Date(info.firstInstallTime)) + " / " + day.format(new Date(info.lastUpdateTime));
        });
        fact(out, "Install ID", () -> {
            long salt = installSalt(prefs);
            return String.format(Locale.US, "%08x", (int) (salt ^ (salt >>> 32)));
        });
        fact(out, "High score", () -> highScoreStatus().replace("\n", ". "));
        fact(out, "Activity log", () -> (activityLog == null || activityLog.value ? "on" : "OFF")
                + ", " + entryCount + " entries, " + (ActivityLog.size(this) / 1024) + " KB");
        out.append("\n----- Device -----\n");
        fact(out, "Android", () -> Build.VERSION.RELEASE + " (API " + Build.VERSION.SDK_INT + ")"
                + (Build.VERSION.SDK_INT >= 23 ? ", security patch " + Build.VERSION.SECURITY_PATCH : ""));
        fact(out, "Model", () -> Build.MANUFACTURER + " " + Build.MODEL);
        fact(out, "Brand / device / product", () -> Build.BRAND + " / " + Build.DEVICE + " / " + Build.PRODUCT);
        fact(out, "Hardware", () -> Build.HARDWARE + ", " + Runtime.getRuntime().availableProcessors() + " cores");
        fact(out, "CPU types", () -> java.util.Arrays.toString(Build.SUPPORTED_ABIS));
        fact(out, "Build", () -> Build.FINGERPRINT);
        fact(out, "Memory", () -> {
            ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
            if (manager == null) return "unavailable";
            ActivityManager.MemoryInfo memory = new ActivityManager.MemoryInfo();
            manager.getMemoryInfo(memory);
            return (memory.availMem >> 20) + " MB free of " + (memory.totalMem >> 20) + " MB"
                    + (memory.lowMemory ? " (low now)" : "") + ", app limit " + manager.getMemoryClass() + " MB"
                    + (manager.isLowRamDevice() ? ", low-RAM device" : "");
        });
        fact(out, "App memory in use", () -> {
            Runtime runtime = Runtime.getRuntime();
            return ((runtime.totalMemory() - runtime.freeMemory()) >> 20) + " MB of " + (runtime.maxMemory() >> 20) + " MB";
        });
        fact(out, "Free storage", () -> (getFilesDir().getUsableSpace() >> 20) + " MB");
        fact(out, "Battery saver", () -> {
            PowerManager power = (PowerManager) getSystemService(POWER_SERVICE);
            return power == null ? "unknown" : power.isPowerSaveMode() ? "on" : "off";
        });
        out.append("\n----- Screen and input -----\n");
        fact(out, "Screen", () -> {
            DisplayMetrics metrics = getResources().getDisplayMetrics();
            String text = metrics.widthPixels + " x " + metrics.heightPixels + " px, " + metrics.densityDpi + " dpi ("
                    + metrics.density + "x)";
            android.view.Display display = getWindow().getDecorView().getDisplay();
            if (display != null) text += ", " + Math.round(display.getRefreshRate()) + " Hz";
            return text;
        });
        fact(out, "Layout", () -> {
            Configuration config = getResources().getConfiguration();
            return (config.orientation == Configuration.ORIENTATION_LANDSCAPE ? "landscape" : "portrait")
                    + ", " + config.screenWidthDp + " x " + config.screenHeightDp + " dp, font scale " + config.fontScale
                    + ((config.uiMode & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES ? ", dark theme" : ", light theme");
        });
        fact(out, "Input", () -> {
            Configuration config = getResources().getConfiguration();
            return (config.touchscreen == Configuration.TOUCHSCREEN_NOTOUCH ? "no touch screen" : "touch screen")
                    + (config.keyboard == Configuration.KEYBOARD_QWERTY ? ", hardware keyboard" : "")
                    + (config.hardKeyboardHidden == Configuration.HARDKEYBOARDHIDDEN_NO ? " (open)" : "");
        });
        fact(out, "Language / time zone", () -> Locale.getDefault() + " / " + TimeZone.getDefault().getID());
        out.append('\n');
        out.append("===== Current settings =====\n");
        out.append(withoutNotes(buildSettingsText())).append('\n');
        out.append("===== Log =====\n");
        out.append(log.isEmpty() ? "(empty)\n" : log);
        return out.toString();
    }

    private void showActivityLog() {
        flushSave();
        String log = ActivityLog.read(this);
        String[] lines = log.isEmpty() ? new String[0] : log.split("\n");
        int from = Math.max(0, lines.length - LOG_VIEW_LINES);
        StringBuilder shown = new StringBuilder();
        if (from > 0) shown.append("Showing the latest ").append(LOG_VIEW_LINES).append(" lines. Export to get everything.\n\n");
        for (int i = from; i < lines.length; i++) shown.append(lines[i]).append('\n');
        if (lines.length == 0) shown.append("Nothing has been logged yet.");
        if (activityLog != null && !activityLog.value) {
            shown.insert(0, "The activity log is turned off in Advanced, so nothing new is being recorded.\n\n");
        }

        TextView text = newText(shown.toString(), 11, COLOR_ON, Typeface.MONOSPACE);
        text.setTextIsSelectable(true);
        text.setPadding(dp(14), dp(12), dp(14), dp(12));
        final NestedScrollView scroller = new NestedScrollView(this);
        scroller.setBackground(rounded(COLOR_ROW, dp(16)));
        scroller.addView(text, new FrameLayout.LayoutParams(MATCH, WRAP));
        LinearLayout body = newLinear(false);
        body.setPadding(dp(24), dp(4), dp(24), 0);
        TextView about = newText("Rounds, new bests, setting changes, imports, hand edits and crashes, newest at the bottom. Export it and attach it to a bug report.",
                12, COLOR_ON_VARIANT, null);
        body.addView(about, linear(MATCH, WRAP));
        LinearLayout.LayoutParams scrollParams = weightedHeight();
        scrollParams.topMargin = dp(12);
        body.addView(scroller, scrollParams);
        scroller.setMinimumHeight(dp(160));
        scroller.post(() -> scroller.fullScroll(View.FOCUS_DOWN));

        showDialog(new MaterialAlertDialogBuilder(this)
                .setTitle("Activity log")
                .setView(body)
                .setPositiveButton("Export", (dialog, which) -> startLogExport())
                .setNeutralButton("Share", (dialog, which) -> shareLog())
                .setNegativeButton("Close", null));
    }

    private void startLogExport() {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("text/plain");
        String day = new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date());
        intent.putExtra(Intent.EXTRA_TITLE, "flappy-bird-log-" + day + ".txt");
        launchPicker(intent, REQUEST_EXPORT_LOG);
    }

    private void writeLogExport(Uri uri) {
        String report = logReport();
        try (OutputStream out = openForWriting(uri)) {
            out.write(report.getBytes("UTF-8"));
            ActivityLog.add(this, "FILE", "Activity log exported");
            announce("Activity log exported", null, null);
        } catch (IOException | SecurityException e) {
            announce("Could not save the file", null, null);
        }
    }

    private void shareLog() {
        String report = logReport();
        if (report.length() > LOG_SHARE_CHARS) {
            int cut = report.indexOf("===== Log =====");
            String head = cut > 0 ? report.substring(0, cut) : "";
            String tail = report.substring(report.length() - Math.max(0, LOG_SHARE_CHARS - head.length()));
            report = head + "===== Log (latest part only, export the file for everything) =====\n" + tail.substring(Math.max(0, tail.indexOf('\n') + 1));
        }
        Intent send = new Intent(Intent.ACTION_SEND);
        send.setType("text/plain");
        send.putExtra(Intent.EXTRA_SUBJECT, "Flappy Bird activity log");
        send.putExtra(Intent.EXTRA_TEXT, report);
        try {
            startActivity(Intent.createChooser(send, "Share activity log"));
            ActivityLog.add(this, "FILE", "Activity log shared");
        } catch (ActivityNotFoundException e) {
            announce("No app can share text on this device", null, null);
        }
    }

    private void shareSettings() {
        String json = exportJson();
        if (json == null) return;
        ActivityLog.add(this, "FILE", "Settings shared as text");
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
        ActivityLog.add(this, "FILE", "Settings pasted from the clipboard");
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
        ActivityLog.add(this, "FILE", "Current setup saved to slot " + slot);
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
            ActivityLog.add(this, "FILE", "Slot " + slot + " cleared");
            refreshSlots();
            announce("Slot " + slot + " cleared", null, null);
        });
    }

    private String textName(Setting setting) {
        String name = setting.key.startsWith("pref_") ? setting.key.substring(5) : setting.key;
        if (name.endsWith("_enabled")) name = name.substring(0, name.length() - 8);
        return name;
    }

    private static String nameKey(String name) {
        return name == null ? "" : name.trim().toLowerCase(Locale.US).replace('-', '_').replace(' ', '_');
    }

    private Map<String, Setting> textLookup() {
        if (textNames != null) return textNames;
        Map<String, Setting> names = new HashMap<>();
        for (Setting setting : settings) {
            names.put(nameKey(setting.key), setting);
            if (setting.key.startsWith("pref_")) names.put(nameKey(setting.key.substring(5)), setting);
        }
        for (Setting setting : settings) {
            String name = nameKey(textName(setting));
            if (!names.containsKey(name)) names.put(name, setting);
        }
        textNames = names;
        return names;
    }

    private final class HiddenValue {
        final String key;
        final String name;
        final String label;
        final boolean flag;
        final int max;
        final Object fallback;

        HiddenValue(String key, String name, String label, boolean flag, int max, Object fallback) {
            this.key = key;
            this.name = name;
            this.label = label;
            this.flag = flag;
            this.max = max;
            this.fallback = fallback;
        }

        Object current() {
            Object stored = prefs.getAll().get(key);
            if (flag) return stored instanceof Boolean ? stored : fallback;
            return stored instanceof Integer ? Math.max(0, Math.min(max, (Integer) stored)) : fallback;
        }

        String textValue() {
            Object value = current();
            return flag ? ((Boolean) value ? "on" : "off") : String.valueOf(value);
        }

        String textHint() {
            return flag ? "on or off" : "0 to " + max;
        }

        Object parse(String text) {
            String word = compact(text);
            if (word.equals("default")) return fallback;
            if (flag) {
                if (word.equals("on") || word.equals("true") || word.equals("yes") || word.equals("1")) return Boolean.TRUE;
                if (word.equals("off") || word.equals("false") || word.equals("no") || word.equals("0")) return Boolean.FALSE;
                return null;
            }
            String trimmed = text.trim();
            if (!trimmed.matches("-?[0-9]{1,12}")) return null;
            long number = Long.parseLong(trimmed);
            return (int) Math.max(0, Math.min(max, number));
        }

        String limitNote(String text) {
            if (flag) return null;
            String trimmed = text.trim();
            if (!trimmed.matches("-?[0-9]{1,12}")) return null;
            long number = Long.parseLong(trimmed);
            if (number > max) return "can't go above " + max + ", so it was set to " + max;
            if (number < 0) return "can't go below 0, so it was set to 0";
            return null;
        }

        void write(SharedPreferences.Editor editor, Object value) {
            if (value == null) editor.remove(key);
            else if (flag) editor.putBoolean(key, (Boolean) value);
            else editor.putInt(key, (Integer) value);
        }
    }

    private List<HiddenValue> hiddenValues() {
        if (hiddenValues != null) return hiddenValues;
        List<HiddenValue> list = new ArrayList<>();
        list.add(new HiddenValue(KEY_HIGH_SCORE, "high_score", "High score (changing it here is recorded and shown next to it in Presets)", false, MAX_SCORE_VALUE, 0));
        list.add(new HiddenValue(PREF_SETTINGS_DISCLAIMER_SHOWN, "settings_disclaimer_shown", "Settings disclaimer already seen", true, 0, Boolean.FALSE));
        list.add(new HiddenValue(PREF_TEXT_EDITOR_WARNED, "text_editor_warned", "Text editor warning already seen", true, 0, Boolean.FALSE));
        list.add(new HiddenValue(PREF_TEXT_EDITOR_NOTES, "text_editor_notes", "Show notes in this editor", true, 0, Boolean.TRUE));
        hiddenValues = list;
        return list;
    }

    private HiddenValue hiddenNamed(String name) {
        String wanted = nameKey(name);
        for (HiddenValue value : hiddenValues()) {
            if (nameKey(value.name).equals(wanted) || nameKey(value.key).equals(wanted)) return value;
        }
        return null;
    }

    private boolean notesShown() {
        return (Boolean) hiddenNamed("text_editor_notes").current();
    }

    private static boolean isNote(String line) {
        return line.trim().startsWith("#");
    }

    private static String settingName(String line) {
        String trimmed = line.trim();
        if (trimmed.isEmpty() || trimmed.startsWith("#") || trimmed.startsWith("[")) return null;
        int split = trimmed.indexOf('=');
        if (split < 0) split = trimmed.indexOf(':');
        return split > 0 ? nameKey(trimmed.substring(0, split)) : null;
    }

    private static String withoutNotes(String text) {
        StringBuilder out = new StringBuilder();
        for (String line : text.split("\\r\\n|\\r|\\n")) {
            String trimmed = line.trim();
            if (trimmed.isEmpty() || isNote(trimmed)) continue;
            if (trimmed.startsWith("[") && out.length() > 0) out.append('\n');
            out.append(line).append('\n');
        }
        return out.toString();
    }

    private String withNotes(String edited) {
        Map<String, String> typed = new HashMap<>();
        List<String> extra = new ArrayList<>();
        for (String line : edited.split("\\r\\n|\\r|\\n")) {
            String name = settingName(line);
            if (name != null && (textLookup().containsKey(name) || hiddenNamed(name) != null)) typed.put(name, line.trim());
            else if (!line.trim().isEmpty() && !isNote(line) && !line.trim().startsWith("[")) extra.add(line);
        }
        StringBuilder out = new StringBuilder();
        for (String line : buildSettingsText().split("\n")) {
            String name = settingName(line);
            String replacement = name != null ? typed.get(name) : null;
            out.append(replacement != null ? replacement : line).append('\n');
        }
        if (!extra.isEmpty()) {
            out.append("\n# These lines were not recognised\n");
            for (String line : extra) out.append(line).append('\n');
        }
        return out.toString();
    }

    private String buildSettingsText() {
        StringBuilder out = new StringBuilder(TEXT_HEADER);
        Set<Setting> written = new HashSet<>();
        for (int page = 0; page < pages.length; page++) {
            boolean pageWritten = false;
            for (ViewGroup group : groups) {
                if (pageOf(group) != page) continue;
                boolean groupWritten = false;
                for (int i = 0; i < group.getChildCount(); i++) {
                    Setting setting = rowSettings.get(group.getChildAt(i));
                    if (setting == null) continue;
                    if (!pageWritten) {
                        out.append("\n[").append(pageName(page)).append("]\n");
                        pageWritten = true;
                    }
                    if (!groupWritten) {
                        out.append("\n## ").append(groupTitles.get(group)).append('\n');
                        groupWritten = true;
                    }
                    out.append("# ").append(setting.label).append(": ").append(setting.textHint()).append('\n');
                    out.append(textName(setting)).append(" = ").append(setting.textValue()).append('\n');
                    written.add(setting);
                }
            }
        }
        boolean otherWritten = false;
        for (Setting setting : settings) {
            if (written.contains(setting)) continue;
            if (!otherWritten) {
                out.append("\n[Other]\n");
                otherWritten = true;
            }
            out.append("# ").append(setting.label).append(": ").append(setting.textHint()).append('\n');
            out.append(textName(setting)).append(" = ").append(setting.textValue()).append('\n');
        }
        out.append("\n[Hidden]\n");
        out.append("# Values the game keeps behind the scenes. They are only changed from this editor.\n");
        for (HiddenValue value : hiddenValues()) {
            out.append("# ").append(value.label).append(": ").append(value.textHint()).append('\n');
            out.append(value.name).append(" = ").append(value.textValue()).append('\n');
        }
        return out.toString();
    }

    private final class TextImport {
        final Map<String, Object> values = new HashMap<>();
        final Map<HiddenValue, Object> hidden = new HashMap<>();
        final List<String> problems = new ArrayList<>();
    }

    private static String clip(String text) {
        return text.length() > 40 ? text.substring(0, 40) + "..." : text;
    }

    private TextImport parseSettingsText(CharSequence text) {
        TextImport result = new TextImport();
        if (text == null) return result;
        String raw = text.toString();
        if (raw.length() > MAX_TEXT_CHARS) {
            raw = raw.substring(0, MAX_TEXT_CHARS);
            result.problems.add("The text is very long, so only the start was read");
        }
        Map<String, Setting> lookup = textLookup();
        String[] lines = raw.split("\r\n|\r|\n");
        int count = Math.min(lines.length, MAX_TEXT_LINES);
        if (lines.length > MAX_TEXT_LINES) result.problems.add("Only the first " + MAX_TEXT_LINES + " lines were read");
        for (int i = 0; i < count; i++) {
            String line = lines[i].trim();
            int number = i + 1;
            if (line.isEmpty() || line.startsWith("#") || (line.startsWith("[") && line.endsWith("]"))) continue;
            if (line.length() > MAX_LINE_CHARS) {
                result.problems.add("Line " + number + " is too long");
                continue;
            }
            int split = line.indexOf('=');
            if (split < 0) split = line.indexOf(':');
            if (split <= 0) {
                result.problems.add("Line " + number + ": write it as name = value");
                continue;
            }
            String name = line.substring(0, split).trim();
            String value = line.substring(split + 1).trim();
            int note = value.indexOf(" #");
            if (note >= 0) value = value.substring(0, note).trim();
            if (value.length() >= 2 && value.startsWith("\"") && value.endsWith("\"")) value = value.substring(1, value.length() - 1).trim();
            Setting setting = lookup.get(nameKey(name));
            HiddenValue hidden = setting == null ? hiddenNamed(name) : null;
            if (hidden != null) {
                Object parsed = hidden.parse(value);
                if (parsed == null) {
                    result.problems.add("Line " + number + ": \"" + clip(value) + "\" can't be used for " + hidden.name + " (" + hidden.textHint() + ")");
                    continue;
                }
                String limited = hidden.limitNote(value);
                if (limited != null) result.problems.add("Line " + number + ": " + hidden.name + " " + limited);
                result.hidden.put(hidden, parsed);
                continue;
            }
            if (setting == null) {
                result.problems.add("Line " + number + ": there is no setting called \"" + clip(name) + "\"");
                continue;
            }
            Object parsed = nameKey(value).equals("default") ? setting.defaultObject() : setting.parseText(value);
            if (parsed == null) {
                result.problems.add("Line " + number + ": \"" + clip(value) + "\" can't be used for " + textName(setting) + " (" + setting.textHint() + ")");
                continue;
            }
            String limited = setting.limitNote(parsed);
            if (limited != null) result.problems.add("Line " + number + ": " + textName(setting) + " " + limited);
            result.values.put(setting.key, parsed);
        }
        return result;
    }

    private boolean looksLikeJson(CharSequence text) {
        if (text == null) return false;
        String trimmed = text.toString().trim();
        return trimmed.startsWith("{");
    }

    private void applySettingsText(final CharSequence text, final boolean fromEditor) {
        final TextImport parsed;
        if (looksLikeJson(text)) {
            parsed = new TextImport();
            Map<String, Object> json = parseSettings(text);
            if (json != null) parsed.values.putAll(json);
            else parsed.problems.add("The text starts like a .json settings file but could not be read");
        } else {
            parsed = parseSettingsText(text);
        }
        if (!fromEditor) parsed.hidden.clear();
        if (parsed.values.isEmpty() && parsed.hidden.isEmpty()) {
            if (parsed.problems.isEmpty()) parsed.problems.add("No settings were found in the text");
            showProblems("Nothing was changed", parsed.problems, fromEditor ? text : null);
            return;
        }
        flushSave();
        final String message;
        final Map<String, Object> before = snapshot();
        final Map<HiddenValue, Object> hiddenBefore = new HashMap<>();
        final Map<HiddenValue, Object> hiddenShown = new HashMap<>();
        Map<String, ?> stored = prefs.getAll();
        for (HiddenValue value : parsed.hidden.keySet()) {
            hiddenBefore.put(value, stored.get(value.key));
            hiddenShown.put(value, value.current());
        }
        changeSource = fromEditor ? "Edit as text" : "imported text";
        try {
            Map<String, Object> merged = snapshot();
            merged.putAll(parsed.values);
            readAll(merged);
            parsed.problems.addAll(resolveAllKeyConflicts(before));
            afterBulkChange();
            writeHidden(parsed.hidden, fromEditor ? "Edit as text" : "import");
        } catch (RuntimeException e) {
            ActivityLog.add(this, "ERROR", "Applying settings text failed, everything was put back: " + Log.getStackTraceString(e));
            changeSource = "automatic undo after an error";
            restore(before);
            afterBulkChange();
            writeHidden(hiddenBefore, "automatic undo after an error");
            showProblems("Nothing was changed", Collections.singletonList("The settings could not be applied, so everything was left as it was"), fromEditor ? text : null);
            return;
        }
        int changed = 0;
        Map<String, Object> after = snapshot();
        for (Map.Entry<String, Object> entry : before.entrySet()) {
            if (!entry.getValue().equals(after.get(entry.getKey()))) changed++;
        }
        for (Map.Entry<HiddenValue, Object> entry : hiddenShown.entrySet()) {
            if (!entry.getValue().equals(entry.getKey().current())) changed++;
        }
        message = changed == 0 ? "No settings changed" : changed == 1 ? "1 setting changed" : changed + " settings changed";
        ActivityLog.add(this, "FILE", (fromEditor ? "Edit as text applied: " : "Settings text imported: ") + message.toLowerCase(Locale.US)
                + (parsed.problems.isEmpty() ? "" : ", " + parsed.problems.size() + " line(s) skipped or limited"));
        showCaption(message);
        if (changed > 0) {
            announce(message, "Undo", v -> {
                flushSave();
                changeSource = "undo of: Edit as text";
                restore(before);
                afterBulkChange();
                writeHidden(hiddenBefore, "undo");
                showCaption("Change undone");
            });
        } else {
            announce(message, null, null);
        }
        if (!parsed.problems.isEmpty()) showProblems(changed == 0 ? "Nothing was changed" : "Applied, with notes", parsed.problems, fromEditor ? text : null);
    }

    private void writeHidden(Map<HiddenValue, Object> values, String source) {
        if (values.isEmpty()) return;
        Map<HiddenValue, String> before = new HashMap<>();
        for (HiddenValue value : values.keySet()) before.put(value, value.textValue());
        SharedPreferences.Editor editor = prefs.edit();
        for (Map.Entry<HiddenValue, Object> entry : values.entrySet()) entry.getKey().write(editor, entry.getValue());
        editor.apply();
        for (HiddenValue value : values.keySet()) {
            String was = before.get(value);
            String now = value.textValue();
            if (was.equals(now)) continue;
            if (value.key.equals(KEY_HIGH_SCORE)) {
                ActivityLog.add(this, "SCORE", "High score changed by hand: " + was + " -> " + now + "  (" + source
                        + "). Best earned by playing stays " + earnedHighScore(prefs));
            } else {
                ActivityLog.add(this, "SETTING", value.name + ": " + was + " -> " + now + "  (" + source + ")");
            }
        }
        refreshHighScore();
    }

    private void showProblems(String title, List<String> problems, final CharSequence editAgain) {
        StringBuilder message = new StringBuilder();
        int shown = Math.min(problems.size(), MAX_LISTED_PROBLEMS);
        for (int i = 0; i < shown; i++) {
            if (i > 0) message.append("\n\n");
            message.append(problems.get(i));
        }
        if (problems.size() > shown) message.append("\n\n").append(problems.size() - shown).append(" more not shown");
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this)
                .setTitle(title)
                .setMessage(message.toString())
                .setPositiveButton("OK", null);
        if (editAgain != null) builder.setNeutralButton("Edit again", (dialog, which) -> showTextEditor(editAgain.toString()));
        showDialog(builder);
    }

    private void openTextEditor() {
        Object warned = prefs.getAll().get(PREF_TEXT_EDITOR_WARNED);
        if (warned instanceof Boolean && (Boolean) warned) {
            showTextEditor(startingText());
            return;
        }
        showDialog(new MaterialAlertDialogBuilder(this)
                .setTitle("Editing settings by hand")
                .setMessage("This shows every setting as text so you can change many at once, including values past the slider limits. "
                        + "Extreme values can make the game very hard or strange to play.\n\n"
                        + "Everything is checked before it is applied, values are kept inside safe limits, and you can undo the change afterward.")
                .setPositiveButton("Continue", (dialog, which) -> {
                    prefs.edit().putBoolean(PREF_TEXT_EDITOR_WARNED, true).apply();
                    showTextEditor(startingText());
                })
                .setNegativeButton("Cancel", null));
    }

    private String startingText() {
        String text = buildSettingsText();
        return notesShown() ? text : withoutNotes(text);
    }

    private void showTextEditor(String text) {
        flushSave();
        final EditText editor = new EditText(this);
        editor.setTypeface(Typeface.MONOSPACE);
        editor.setTextSize(13);
        editor.setTextColor(COLOR_ON);
        editor.setGravity(Gravity.TOP | Gravity.START);
        editor.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        editor.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI);
        editor.setHorizontallyScrolling(false);
        editor.setFilters(new InputFilter[]{new InputFilter.LengthFilter(MAX_TEXT_CHARS)});
        editor.setHighlightColor(COLOR_SELECTION);
        editor.setBackground(rounded(COLOR_ROW, dp(16)));
        editor.setPadding(dp(14), dp(12), dp(14), dp(12));
        editor.setMinimumHeight(dp(120));
        editor.setText(text);

        TextView warning = newText("Values are checked before they are applied. Anything outside the safe range is limited, and lines that can't be read are skipped.",
                12, COLOR_ON_VARIANT, null);
        LinearLayout body = newLinear(false);
        body.setPadding(dp(24), dp(4), dp(24), 0);
        body.addView(warning, linear(MATCH, WRAP));

        LinearLayout notesRow = newLinear(true);
        notesRow.setGravity(Gravity.CENTER_VERTICAL);
        TextView notesLabel = newText("Show notes", 14, COLOR_ON, mediumFont);
        notesRow.addView(notesLabel, weighted(WRAP));
        final MaterialSwitch notesSwitch = new MaterialSwitch(this);
        notesSwitch.setSaveEnabled(false);
        tintSwitch(notesSwitch);
        notesSwitch.setChecked(notesShown());
        notesRow.addView(notesSwitch, linear(WRAP, WRAP));
        notesRow.setOnClickListener(v -> notesSwitch.setChecked(!notesSwitch.isChecked()));
        LinearLayout.LayoutParams notesParams = linear(MATCH, WRAP);
        notesParams.topMargin = dp(8);
        body.addView(notesRow, notesParams);
        notesSwitch.setOnCheckedChangeListener((button, checked) -> {
            String current = editor.getText().toString();
            editor.setText(checked ? withNotes(current) : withoutNotes(current));
            prefs.edit().putBoolean(PREF_TEXT_EDITOR_NOTES, checked).apply();
        });
        LinearLayout.LayoutParams editorParams = weightedHeight();
        editorParams.topMargin = dp(12);
        body.addView(editor, editorParams);

        final AlertDialog dialog = showDialog(new MaterialAlertDialogBuilder(this)
                .setTitle("Edit as text")
                .setView(body)
                .setPositiveButton("Apply", (d, which) -> applySettingsText(editor.getText().toString(), true))
                .setNeutralButton("Copy", null)
                .setNegativeButton("Cancel", null));
        if (dialog.getWindow() != null) dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        Button copy = dialog.getButton(DialogInterface.BUTTON_NEUTRAL);
        if (copy != null) {
            copy.setOnClickListener(v -> {
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                if (clipboard == null) return;
                clipboard.setPrimaryClip(ClipData.newPlainText("Flappy Bird settings", editor.getText().toString()));
                announce("Settings text copied", null, null);
            });
        }
    }

    private List<String> resolveAllKeyConflicts(Map<String, Object> before) {
        List<String> notes = new ArrayList<>();
        for (int i = 0; i < keySettings.size(); i++) {
            KeySetting later = keySettings.get(i);
            if (later.value == KeyEvent.KEYCODE_UNKNOWN) continue;
            for (int j = 0; j < i; j++) {
                KeySetting earlier = keySettings.get(j);
                if (earlier.value != later.value) continue;
                KeySetting cleared = later;
                if (before != null && Integer.valueOf(later.value).equals(before.get(later.key))) cleared = earlier;
                notes.add(keyName(cleared.value) + " was used twice, so it was removed from " + textName(cleared));
                cleared.value = KeyEvent.KEYCODE_UNKNOWN;
                unsaved.add(cleared);
                if (cleared == later) break;
            }
        }
        return notes;
    }

    static String keyName(int code) {
        switch (code) {
            case KeyEvent.KEYCODE_UNKNOWN: return "None";
            case KeyEvent.KEYCODE_SPACE: return "Space";
            case KeyEvent.KEYCODE_ENTER: return "Enter";
            case KeyEvent.KEYCODE_NUMPAD_ENTER: return "Numpad Enter";
            case KeyEvent.KEYCODE_DPAD_UP: return "Up arrow";
            case KeyEvent.KEYCODE_DPAD_DOWN: return "Down arrow";
            case KeyEvent.KEYCODE_DPAD_LEFT: return "Left arrow";
            case KeyEvent.KEYCODE_DPAD_RIGHT: return "Right arrow";
            case KeyEvent.KEYCODE_DPAD_CENTER: return "D-pad center";
            case KeyEvent.KEYCODE_ESCAPE: return "Esc";
            case KeyEvent.KEYCODE_DEL: return "Backspace";
            case KeyEvent.KEYCODE_FORWARD_DEL: return "Delete";
            case KeyEvent.KEYCODE_SHIFT_LEFT: return "Left Shift";
            case KeyEvent.KEYCODE_SHIFT_RIGHT: return "Right Shift";
            case KeyEvent.KEYCODE_CTRL_LEFT: return "Left Ctrl";
            case KeyEvent.KEYCODE_CTRL_RIGHT: return "Right Ctrl";
            case KeyEvent.KEYCODE_ALT_LEFT: return "Left Alt";
            case KeyEvent.KEYCODE_ALT_RIGHT: return "Right Alt";
            default: break;
        }
        String raw = KeyEvent.keyCodeToString(code);
        if (raw == null || !raw.startsWith("KEYCODE_")) return "Key " + code;
        String[] parts = raw.substring(8).toLowerCase(Locale.US).split("_");
        StringBuilder name = new StringBuilder();
        for (String part : parts) {
            if (part.isEmpty()) continue;
            if (name.length() > 0) name.append(' ');
            name.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1));
        }
        return name.length() > 0 ? name.toString() : "Key " + code;
    }

    private static String compact(String text) {
        return text.toLowerCase(Locale.US).replaceAll("[^a-z0-9]", "");
    }

    static int parseKey(String text) {
        if (text == null) return -1;
        String wanted = compact(text);
        if (wanted.isEmpty() || wanted.equals("none") || wanted.equals("off") || wanted.equals("nokey")) return KeyEvent.KEYCODE_UNKNOWN;
        if (wanted.equals("esc")) return KeyEvent.KEYCODE_ESCAPE;
        if (wanted.equals("return")) return KeyEvent.KEYCODE_ENTER;
        if (wanted.equals("spacebar")) return KeyEvent.KEYCODE_SPACE;
        if (wanted.equals("up") || wanted.equals("arrowup")) return KeyEvent.KEYCODE_DPAD_UP;
        if (wanted.equals("down") || wanted.equals("arrowdown")) return KeyEvent.KEYCODE_DPAD_DOWN;
        if (wanted.equals("left") || wanted.equals("arrowleft")) return KeyEvent.KEYCODE_DPAD_LEFT;
        if (wanted.equals("right") || wanted.equals("arrowright")) return KeyEvent.KEYCODE_DPAD_RIGHT;
        if (wanted.equals("shift")) return KeyEvent.KEYCODE_SHIFT_LEFT;
        if (wanted.equals("ctrl") || wanted.equals("control")) return KeyEvent.KEYCODE_CTRL_LEFT;
        if (wanted.equals("alt")) return KeyEvent.KEYCODE_ALT_LEFT;
        String digits = wanted.startsWith("key") ? wanted.substring(3) : wanted;
        if (digits.length() > 0 && digits.length() <= 4 && digits.matches("[0-9]+") && wanted.length() > 1) {
            int code = Integer.parseInt(digits);
            if (code > 0 && code <= KeyEvent.getMaxKeyCode() && !wanted.equals(digits)) return code;
        }
        for (int code = 1; code <= KeyEvent.getMaxKeyCode(); code++) {
            if (compact(keyName(code)).equals(wanted)) return code;
            String raw = KeyEvent.keyCodeToString(code);
            if (raw != null && raw.startsWith("KEYCODE_") && compact(raw.substring(8)).equals(wanted)) return code;
        }
        return -1;
    }

    private void showKeyDialog(final KeySetting setting) {
        final AlertDialog dialog = showDialog(new MaterialAlertDialogBuilder(this)
                .setTitle(setting.label)
                .setMessage("Press a key on your keyboard.\n\nCurrent key: " + setting.describe())
                .setNeutralButton("No key", (d, which) -> setting.setFromUser(KeyEvent.KEYCODE_UNKNOWN))
                .setNegativeButton("Cancel", null));
        final boolean[] captured = {false};
        dialog.setOnKeyListener((d, keyCode, event) -> {
            if (keyCode == KeyEvent.KEYCODE_UNKNOWN || !isUsableKey(keyCode)) return false;
            if (captured[0] || event.getAction() != KeyEvent.ACTION_DOWN || event.getRepeatCount() > 0) return true;
            captured[0] = true;
            d.dismiss();
            setting.setFromUser(keyCode);
            return true;
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
        Setting twin;
        Setting source;
        Object classicValue;

        Setting(String key, View rowId) {
            this.key = key;
            this.row = rowId;
            String text = firstText(row);
            this.label = text != null ? text : key;
            if (buildingTwin) return;
            rowSettings.put(row, this);
            settings.add(this);
            settingsByKey.put(key, this);
        }

        Setting twinFor(View twinRow) {
            throw new IllegalStateException(key + " can't be mirrored");
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

        abstract String textValue();

        abstract String textHint();

        abstract Object parseText(String text);

        abstract Object defaultObject();

        String limitNote(Object parsed) {
            return null;
        }
    }

    private final class ToggleSetting extends Setting {
        final boolean defaultValue;
        final MaterialSwitch toggle;
        boolean value;

        @Override
        Setting twinFor(View twinRow) {
            return new ToggleSetting(key, defaultValue, twinRow);
        }

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

        @Override
        String textValue() {
            return value ? "on" : "off";
        }

        @Override
        String textHint() {
            return "on or off";
        }

        @Override
        Object parseText(String text) {
            String word = compact(text);
            if (word.equals("on") || word.equals("true") || word.equals("yes") || word.equals("1") || word.equals("enabled")) return Boolean.TRUE;
            if (word.equals("off") || word.equals("false") || word.equals("no") || word.equals("0") || word.equals("disabled")) return Boolean.FALSE;
            return null;
        }

        @Override
        Object defaultObject() {
            return defaultValue;
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

        @Override
        Setting twinFor(View twinRow) {
            NumberSetting copy = new NumberSetting(key, defaultValue, integer, sliderMin, sliderMax, step, min, max, unit, twinRow);
            copy.zeroLabel = zeroLabel;
            return copy;
        }

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
                feelSlider(isDefault());
                onUserChange(NumberSetting.this);
            });
            slider.addOnSliderTouchListener(new Slider.OnSliderTouchListener() {
                @Override
                public void onStartTrackingTouch(Slider view) {
                    feel().play(Feel.PRESS);
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
        String textValue() {
            return zeroLabel != null && value == 0f ? zeroLabel : withUnit(value);
        }

        @Override
        String textHint() {
            String range = sliderMin == min && sliderMax == max
                    ? withUnit(min) + " to " + withUnit(max)
                    : "slider " + withUnit(sliderMin) + " to " + withUnit(sliderMax) + ", allowed " + withUnit(min) + " to " + withUnit(max);
            String zero = zeroLabel != null ? ", 0 means " + zeroLabel : "";
            return range + zero + ", default " + withUnit(defaultValue);
        }

        @Override
        Object parseText(String text) {
            String trimmed = text.trim();
            if (zeroLabel != null && compact(trimmed).equals(compact(zeroLabel))) return 0f;
            Matcher match = NUMBER_TEXT.matcher(trimmed);
            if (!match.matches()) return null;
            float parsed = parseNumber(match.group(1), Float.NaN);
            if (Float.isNaN(parsed) || Float.isInfinite(parsed)) return null;
            return parsed;
        }

        @Override
        Object defaultObject() {
            return defaultValue;
        }

        @Override
        String limitNote(Object parsed) {
            float wanted = ((Number) parsed).floatValue();
            if (wanted > max) return "can't go above " + withUnit(max) + ", so it was set to " + withUnit(max);
            if (wanted < min) return "can't go below " + withUnit(min) + ", so it was set to " + withUnit(min);
            return null;
        }

        String withUnit(float number) {
            String text = plain(number);
            if (unit == UNIT_TIMES) return text + "x";
            if (unit == UNIT_PERCENT) return text + "%";
            if (unit == UNIT_DEGREES) return text + " deg";
            if (unit == UNIT_MS) return text + " ms";
            if (unit == UNIT_SECONDS) return text + " s";
            return text;
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

        final int[] colors;
        final String[] swatchNames;

        @Override
        Setting twinFor(View twinRow) {
            ChoiceSetting copy = new ChoiceSetting(key, defaultValue, twinRow, values, colors, swatchNames);
            copy.setEnabled(enabled);
            return copy;
        }

        ChoiceSetting(String key, int defaultValue, View rowId, int[] values, int[] colors, String[] swatchNames) {
            super(key, rowId);
            this.colors = colors;
            this.swatchNames = swatchNames;
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

        @Override
        String textValue() {
            return describe();
        }

        @Override
        String textHint() {
            StringBuilder hint = new StringBuilder();
            for (int i = 0; i < names.length; i++) {
                if (i > 0) hint.append(i == names.length - 1 ? " or " : ", ");
                hint.append(names[i]);
            }
            return hint.toString();
        }

        @Override
        Object parseText(String text) {
            String wanted = compact(text);
            for (int i = 0; i < names.length; i++) {
                if (compact(names[i]).equals(wanted)) return values[i];
            }
            if (wanted.matches("[0-9]{1,3}")) {
                int number = Integer.parseInt(wanted);
                if (indexOf(number) >= 0) return number;
            }
            return null;
        }

        @Override
        Object defaultObject() {
            return defaultValue;
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

        @Override
        String textValue() {
            return value.isEmpty() ? "none" : value.replace(",", ", ");
        }

        @Override
        String textHint() {
            return "scores separated by commas, or none";
        }

        @Override
        Object parseText(String text) {
            String trimmed = text.trim();
            if (trimmed.isEmpty() || compact(trimmed).equals("none")) return "";
            if (!trimmed.matches("[0-9,;\\s]+")) return null;
            return normalizeScores(trimmed);
        }

        @Override
        Object defaultObject() {
            return defaultValue;
        }

        @Override
        String limitNote(Object parsed) {
            String text = (String) parsed;
            int count = text.isEmpty() ? 0 : text.split(",").length;
            return count >= MAX_LIGHTNING_SCORES ? "keeps at most " + MAX_LIGHTNING_SCORES + " scores" : null;
        }
    }

    private final class KeySetting extends Setting {
        final int defaultValue;
        final TextView pill;
        int value;

        KeySetting(String key, int defaultValue, View rowId) {
            super(key, rowId);
            this.defaultValue = defaultValue;
            this.value = defaultValue;
            this.pill = (TextView) row.findViewWithTag(TAG_VALUE);
            pill.setBackground(rounded(COLOR_HIGH, dp(16)));
            randomizable = false;
            note = "used in the game";
            keySettings.add(this);
            row.setOnClickListener(v -> showKeyDialog(KeySetting.this));
        }

        void setFromUser(int code) {
            if (!isUsableKey(code)) return;
            value = code;
            KeySetting moved = null;
            if (code != KeyEvent.KEYCODE_UNKNOWN) {
                for (KeySetting other : keySettings) {
                    if (other == this || other.value != code) continue;
                    other.value = KeyEvent.KEYCODE_UNKNOWN;
                    other.syncView();
                    scheduleSave(other);
                    moved = other;
                }
            }
            syncView();
            onUserChange(this);
            if (moved != null) announce(keyName(code) + " was removed from " + moved.label, null, null);
        }

        private int decode(Object stored) {
            if (stored instanceof Number) {
                double number = ((Number) stored).doubleValue();
                if (number != Math.rint(number) || number < 0 || number > KeyEvent.getMaxKeyCode()) return -1;
                int code = (int) number;
                return isUsableKey(code) ? code : -1;
            }
            if (stored instanceof String) {
                int code = parseKey((String) stored);
                return code >= 0 && isUsableKey(code) ? code : -1;
            }
            return -1;
        }

        @Override
        void read(Map<String, ?> source) {
            Object stored = source.get(key);
            int code = decode(stored);
            value = code >= 0 ? code : defaultValue;
            if (stored != null && (!(stored instanceof Integer) || code < 0)) unsaved.add(this);
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
            int code = decode(captured);
            if (code >= 0) value = code;
        }

        @Override
        String describe() {
            return keyName(value);
        }

        @Override
        void export(JSONObject out) throws JSONException {
            out.put(key, value);
        }

        @Override
        boolean accepts(Object candidate) {
            return decode(candidate) >= 0;
        }

        @Override
        String textValue() {
            return keyName(value);
        }

        @Override
        String textHint() {
            return "a key such as Space, Enter, P, Esc or Up arrow, or none";
        }

        @Override
        Object parseText(String text) {
            int code = parseKey(text);
            return code >= 0 && isUsableKey(code) ? code : null;
        }

        @Override
        Object defaultObject() {
            return defaultValue;
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
            view.setSelected(active);
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

    private static final class IconDrawable extends Drawable {
        static final int BACK = 0;
        static final int SEARCH = 1;
        static final int CLOSE = 2;
        static final int PLAY = 3;
        private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Path path = new Path();
        private final int kind;
        private final int size;

        IconDrawable(int kind, int size, float strokeWidth, int color) {
            this.kind = kind;
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
            path.reset();
            if (kind == SEARCH) {
                float radius = size * 0.25f;
                float lensX = centerX - size * 0.07f;
                float lensY = centerY - size * 0.07f;
                canvas.drawCircle(lensX, lensY, radius, paint);
                float edge = radius * 0.7071f;
                path.moveTo(lensX + edge, lensY + edge);
                path.lineTo(centerX + reach * 0.95f, centerY + reach * 0.95f);
            } else if (kind == PLAY) {
                float unit = size / 24f;
                float left = bounds.left + (bounds.width() - size) / 2f, top = bounds.top + (bounds.height() - size) / 2f;
                path.moveTo(left + 8.5f * unit, top + 5.8f * unit);
                path.lineTo(left + 18.6f * unit, top + 12f * unit);
                path.lineTo(left + 8.5f * unit, top + 18.2f * unit);
                path.close();
                Paint.Style style = paint.getStyle();
                paint.setStyle(Paint.Style.FILL_AND_STROKE);
                canvas.drawPath(path, paint);
                paint.setStyle(style);
                return;
            } else if (kind == CLOSE) {
                float arm = reach * 0.78f;
                path.moveTo(centerX - arm, centerY - arm);
                path.lineTo(centerX + arm, centerY + arm);
                path.moveTo(centerX + arm, centerY - arm);
                path.lineTo(centerX - arm, centerY + arm);
            } else {
                float wing = reach * 0.85f;
                path.moveTo(centerX + reach, centerY);
                path.lineTo(centerX - reach, centerY);
                path.moveTo(centerX - reach + wing, centerY - wing);
                path.lineTo(centerX - reach, centerY);
                path.lineTo(centerX - reach + wing, centerY + wing);
            }
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

            for (int i = 0; i < PIPE_COLOR_COUNT; i++) pipeFilters[i] = pipeColorFilter(i);
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
