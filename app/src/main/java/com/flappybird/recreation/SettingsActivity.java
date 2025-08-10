package com.flappybird.recreation;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextWatcher;
import android.view.Choreographer;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RadioGroup;
import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.slider.Slider;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Consumer;

// Statically import all constants from the central GameSettings file
import static com.flappybird.recreation.GameSettings.*;

public class SettingsActivity extends AppCompatActivity {

    private final Map<String, Object> defaultValues = new HashMap<>();
    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;
    private SettingsBackgroundView backgroundView;
    private final List<SettingControl> visualControls = new ArrayList<>();
    private final List<SettingControl> gameplayControls = new ArrayList<>();
    private final List<SettingControl> cheatControls = new ArrayList<>();
    private final List<SettingControl> systemControls = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        configureWindow();
        setContentView(R.layout.settings);
        
        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        editor = prefs.edit();

        setupBackgroundView();
        populateDefaultValues();
        initializeAndBindControls();
        setupClickListeners();

        checkDisclaimer();
    }
    
    private void initializeAndBindControls() {
        // Visuals
        visualControls.add(new RadioSetting(PREF_BIRD_COLOR, R.id.rg_bird_color, new int[]{R.id.rb_bird_random, R.id.rb_bird_yellow, R.id.rb_bird_blue, R.id.rb_bird_red}));
        visualControls.add(new RadioSetting(PREF_BACKGROUND, R.id.rg_background, new int[]{R.id.rb_bg_random, R.id.rb_bg_day, R.id.rb_bg_night}, style -> backgroundView.setBackgroundStyle(style)));
        RadioSetting pipeColorSetting = new RadioSetting(PREF_PIPE_COLOR, R.id.rg_pipe_color, new int[]{R.id.rb_pipe_default, R.id.rb_pipe_red, R.id.rb_pipe_blue, R.id.rb_pipe_yellow, R.id.rb_pipe_white, R.id.rb_pipe_pink, R.id.rb_pipe_black, R.id.rb_pipe_purple, R.id.rb_pipe_orange});
        visualControls.add(pipeColorSetting);
        visualControls.add(new SwitchSetting(PREF_RANDOM_PIPE_COLORS_ENABLED, R.id.switch_random_pipe_colors, isEnabled -> pipeColorSetting.setEnabled(!isEnabled)));

        // Gameplay
        gameplayControls.add(new SliderSetting(PREF_GAME_SPEED, R.id.slider_game_speed, R.id.et_game_speed, speed -> backgroundView.setSpeedMultiplier(speed)));
        gameplayControls.add(new SliderSetting(PREF_BG_SCROLL_SPEED, R.id.slider_bg_scroll, R.id.et_bg_scroll, speed -> backgroundView.setBgScrollMultiplier(speed)));
        gameplayControls.add(new SliderSetting(PREF_GROUND_SCROLL_SPEED, R.id.slider_ground_scroll, R.id.et_ground_scroll, speed -> backgroundView.setGroundScrollMultiplier(speed)));
        gameplayControls.add(new SliderSetting(PREF_BIRD_SIZE, R.id.slider_bird_size, R.id.et_bird_size, null));
        gameplayControls.add(new SliderSetting(PREF_BIRD_HITBOX, R.id.slider_bird_hitbox, R.id.et_bird_hitbox, null));
        gameplayControls.add(new SliderSetting(PREF_GRAVITY, R.id.slider_gravity, R.id.et_gravity, null));
        gameplayControls.add(new SliderSetting(PREF_JUMP_STRENGTH, R.id.slider_jump, R.id.et_jump, null));
        gameplayControls.add(new SliderSetting(PREF_BIRD_HANG_DELAY, R.id.slider_hang_delay, R.id.et_hang_delay, null));
        gameplayControls.add(new SliderSetting(PREF_PIPE_GAP, R.id.slider_pipe_gap, R.id.et_pipe_gap, null));
        gameplayControls.add(new SliderSetting(PREF_PIPE_SPACING, R.id.slider_pipe_spacing, R.id.et_pipe_spacing, null));
        gameplayControls.add(new SliderSetting(PREF_PIPE_WIDTH, R.id.slider_pipe_width, R.id.et_pipe_width, null));
        gameplayControls.add(new SliderSetting(PREF_PIPE_VARIATION, R.id.slider_pipe_variation, R.id.et_pipe_variation, null));
        gameplayControls.add(new SliderSetting(PREF_PIPE_SPEED_VARIATION, R.id.slider_pipe_speed_variation, R.id.et_pipe_speed_variation, null));
        gameplayControls.add(new EditTextSetting(PREF_PIPE_MOVE_TIER_1_SCORE, R.id.et_pipe_move_tier1));
        gameplayControls.add(new EditTextSetting(PREF_PIPE_MOVE_TIER_2_SCORE, R.id.et_pipe_move_tier2));
        gameplayControls.add(new EditTextSetting(PREF_SCORE_MULTIPLIER, R.id.et_score_multiplier));
        gameplayControls.add(new SwitchSetting(PREF_MOVING_PIPES_ENABLED, R.id.switch_moving_pipes, null));

        // Cheats
        cheatControls.add(new SwitchSetting(PREF_NO_CLIP_ENABLED, R.id.switch_no_clip, null));
        cheatControls.add(new SwitchSetting(PREF_INFINITE_FLAP_ENABLED, R.id.switch_infinite_flap, null));
        cheatControls.add(new SwitchSetting(PREF_RAINBOW_BIRD_ENABLED, R.id.switch_rainbow_bird, null));
        cheatControls.add(new SwitchSetting(PREF_BIRD_TRAIL_ENABLED, R.id.switch_bird_trail, null));
        cheatControls.add(new SwitchSetting(PREF_GHOST_MODE_ENABLED, R.id.switch_ghost_mode, null));
        cheatControls.add(new SwitchSetting(PREF_UPSIDE_DOWN_ENABLED, R.id.switch_upside_down, null));
        cheatControls.add(new SwitchSetting(PREF_REVERSE_PIPES_ENABLED, R.id.switch_reverse_pipes, null));

        // System
        systemControls.add(new SwitchSetting(PREF_SOUND_ENABLED, R.id.switch_sound, null));
        systemControls.add(new SwitchSetting(PREF_HAPTIC_FEEDBACK_ENABLED, R.id.switch_haptic_feedback, null));
        systemControls.add(new SwitchSetting(PREF_WING_ANIMATION_ENABLED, R.id.switch_wing_animation, null));
        systemControls.add(new SwitchSetting(PREF_HIDE_SETTINGS_ICON, R.id.switch_hide_settings_icon, null));
        systemControls.add(new SliderSetting(PREF_GAMEOVER_OPACITY, R.id.slider_opacity, R.id.et_opacity, null));

        loadAllSettings();
    }

    private void loadAllSettings() {
        visualControls.forEach(SettingControl::load);
        gameplayControls.forEach(SettingControl::load);
        cheatControls.forEach(SettingControl::load);
        systemControls.forEach(SettingControl::load);
    }

    private void resetCategory(List<SettingControl> controls) {
        controls.forEach(SettingControl::reset);
    }
    
    private void setupClickListeners() {
        findViewById(R.id.button_back).setOnClickListener(v -> finish());
        findViewById(R.id.button_reset_all).setOnClickListener(v -> showResetAllConfirmation());

        setupExpandableSection(R.id.header_visuals, R.id.content_visuals, R.id.icon_visuals);
        setupExpandableSection(R.id.header_gameplay, R.id.content_gameplay, R.id.icon_gameplay);
        setupExpandableSection(R.id.header_cheats, R.id.content_cheats, R.id.icon_cheats);
        setupExpandableSection(R.id.header_system, R.id.content_system, R.id.icon_system);

        findViewById(R.id.button_reset_visuals).setOnClickListener(v -> resetCategory(visualControls));
        findViewById(R.id.button_reset_gameplay).setOnClickListener(v -> resetCategory(gameplayControls));
        findViewById(R.id.button_reset_cheats).setOnClickListener(v -> resetCategory(cheatControls));
        findViewById(R.id.button_reset_system).setOnClickListener(v -> resetCategory(systemControls));
    }

    private void showResetAllConfirmation() {
        new MaterialAlertDialogBuilder(this)
            .setTitle("Reset All Settings")
            .setMessage("Are you sure you want to reset ALL settings to their original defaults? This action cannot be undone.")
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Reset", (dialog, which) -> {
                boolean disclaimer = prefs.getBoolean(PREF_SETTINGS_DISCLAIMER_SHOWN, false);
                editor.clear().putBoolean(PREF_SETTINGS_DISCLAIMER_SHOWN, disclaimer).apply();
                if (backgroundView != null) {
                    backgroundView.resetPositions();
                }
                loadAllSettings();
            })
            .show();
    }
    
    // --- Helper Methods and Classes ---

    private void configureWindow() {
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        WindowInsetsControllerCompat controller = WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
        if (controller != null) {
            controller.hide(WindowInsetsCompat.Type.systemBars());
            controller.setSystemBarsBehavior(WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
        }
    }

    private void setupBackgroundView() {
        FrameLayout rootContainer = findViewById(R.id.settings_root_container);
        backgroundView = new SettingsBackgroundView(this, prefs);
        rootContainer.addView(backgroundView, 0);
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
    
    private void checkDisclaimer() {
        if (!prefs.getBoolean(PREF_SETTINGS_DISCLAIMER_SHOWN, false)) {
            new MaterialAlertDialogBuilder(this)
                .setTitle("Settings Disclaimer")
                .setMessage("Adjusting these settings, especially the gameplay values, can potentially break the game, cause unexpected behavior, or make it unstable. Proceed with caution.")
                .setPositiveButton("I Understand", (dialog, which) -> editor.putBoolean(PREF_SETTINGS_DISCLAIMER_SHOWN, true).apply())
                .setCancelable(false)
                .show();
        }
    }
    
    private interface SettingControl {
        void load();
        void reset();
    }

    private abstract class BaseSetting<T> implements SettingControl {
        protected String key;
        protected T defaultValue;
        
        BaseSetting(String key) {
            this.key = key;
            this.defaultValue = (T) defaultValues.get(key);
        }

        @Override
        public abstract void load();
        
        @Override
        public void reset() {
            if (defaultValue instanceof Float) editor.putFloat(key, (Float) defaultValue);
            else if (defaultValue instanceof Integer) editor.putInt(key, (Integer) defaultValue);
            else if (defaultValue instanceof Boolean) editor.putBoolean(key, (Boolean) defaultValue);
            editor.apply();
            load();
        }
    }

    private class SliderSetting extends BaseSetting<Number> {
        private Slider slider;
        private EditText editText;
        private Consumer<Float> onUpdate;
        private TextWatcher textWatcher;
        private boolean isUpdatingFromSlider = false;

        SliderSetting(String key, @IdRes int sliderId, @IdRes int editTextId, @Nullable Consumer<Float> onUpdate) {
            super(key);
            this.slider = findViewById(sliderId);
            this.editText = findViewById(editTextId);
            this.onUpdate = onUpdate;
            if (defaultValue instanceof Integer) {
                 this.editText.setFilters(new InputFilter[]{});
            } else {
                 this.editText.setFilters(new InputFilter[]{new DecimalDigitsInputFilter()});
            }
        }
        
        @Override
        public void load() {
            Number value;
            if (defaultValue instanceof Float) {
                value = prefs.getFloat(key, (Float) defaultValue);
                editText.setText(String.format(Locale.US, "%.2f", value.floatValue()));
            } else {
                value = prefs.getInt(key, (Integer) defaultValue);
                editText.setText(String.valueOf(value.intValue()));
            }

            float min = slider.getValueFrom();
            float max = slider.getValueTo();
            float valF = value.floatValue();

            float clampedValue = Math.max(min, Math.min(valF, max));
            float stepSize = slider.getStepSize();
            float snappedValue = min + (Math.round((clampedValue - min) / stepSize) * stepSize);

            slider.setValue(snappedValue);
            slider.setEnabled(valF >= min && valF <= max);

            if (onUpdate != null) {
                onUpdate.accept(valF);
            }

            attachListeners();
        }
        
        private void attachListeners() {
            slider.clearOnChangeListeners();
            slider.addOnChangeListener((s, sliderValue, fromUser) -> {
                if (fromUser) {
                    isUpdatingFromSlider = true;
                    if (defaultValue instanceof Float) {
                        float roundedValue = Math.round(sliderValue * 100.0f) / 100.0f;
                        editText.setText(String.format(Locale.US, "%.2f", roundedValue));
                        editor.putFloat(key, roundedValue).apply();
                        if (onUpdate != null) onUpdate.accept(roundedValue);
                    } else {
                        int intValue = Math.round(sliderValue);
                        editText.setText(String.valueOf(intValue));
                        editor.putInt(key, intValue).apply();
                        if (onUpdate != null) onUpdate.accept((float)intValue);
                    }
                    isUpdatingFromSlider = false;
                }
            });

            if (textWatcher != null) editText.removeTextChangedListener(textWatcher);
            textWatcher = new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
                @Override public void afterTextChanged(Editable s) {
                    if (isUpdatingFromSlider) return;

                    try {
                        String valStr = s.toString().trim();
                        if (valStr.isEmpty() || valStr.equals(".") || valStr.equals("-")) {
                            slider.setEnabled(false);
                            return;
                        }
                        
                        if (defaultValue instanceof Float) {
                            float newValue = Float.parseFloat(valStr);
                            editor.putFloat(key, newValue).apply();
                            slider.setEnabled(newValue >= slider.getValueFrom() && newValue <= slider.getValueTo());
                            if (onUpdate != null) onUpdate.accept(newValue);
                        } else {
                            int newValue = Integer.parseInt(valStr);
                            editor.putInt(key, newValue).apply();
                            slider.setEnabled(newValue >= slider.getValueFrom() && newValue <= slider.getValueTo());
                            if (onUpdate != null) onUpdate.accept((float)newValue);
                        }
                    } catch (NumberFormatException e) {
                        slider.setEnabled(false);
                    }
                }
            };
            editText.addTextChangedListener(textWatcher);
        }
    }
    
    private class SwitchSetting extends BaseSetting<Boolean> {
        private MaterialSwitch mSwitch;
        private Consumer<Boolean> onUpdate;

        SwitchSetting(String key, @IdRes int switchId, @Nullable Consumer<Boolean> onUpdate) {
            super(key);
            this.mSwitch = findViewById(switchId);
            this.onUpdate = onUpdate;
        }

        @Override
        public void load() {
            boolean isChecked = prefs.getBoolean(key, defaultValue);
            mSwitch.setChecked(isChecked);
            if (onUpdate != null) onUpdate.accept(isChecked);

            mSwitch.setOnCheckedChangeListener((button, checked) -> {
                editor.putBoolean(key, checked).apply();
                if (onUpdate != null) onUpdate.accept(checked);
            });
        }
    }

    private class RadioSetting extends BaseSetting<Integer> {
        private RadioGroup radioGroup;
        private int[] idMap;
        private Consumer<Integer> onUpdate;
        private boolean hasOffset;

        RadioSetting(String key, @IdRes int groupId, @NonNull int[] idMap) {
            this(key, groupId, idMap, null);
        }

        RadioSetting(String key, @IdRes int groupId, @NonNull int[] idMap, @Nullable Consumer<Integer> onUpdate) {
            super(key);
            this.radioGroup = findViewById(groupId);
            this.idMap = idMap;
            this.onUpdate = onUpdate;
            this.hasOffset = PREF_BIRD_COLOR.equals(key) || PREF_BACKGROUND.equals(key);
        }

        @Override
        public void load() {
            int selection = prefs.getInt(key, defaultValue);
            int mappedIndex = hasOffset ? selection + 1 : selection;
            
            if (mappedIndex >= 0 && mappedIndex < idMap.length) {
                radioGroup.check(idMap[mappedIndex]);
            } else {
                radioGroup.clearCheck();
            }
            if (onUpdate != null) onUpdate.accept(selection);

            radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
                int newSelection = defaultValue;
                for (int i = 0; i < idMap.length; i++) {
                    if (idMap[i] == checkedId) {
                        newSelection = hasOffset ? i - 1 : i;
                        break;
                    }
                }
                editor.putInt(key, newSelection).apply();
                if (onUpdate != null) onUpdate.accept(newSelection);
            });
        }
        
        public void setEnabled(boolean enabled) {
            for (int i = 0; i < radioGroup.getChildCount(); i++) {
                View child = radioGroup.getChildAt(i);
                child.setEnabled(enabled);
                child.setAlpha(enabled ? 1.0f : 0.5f);
            }
        }
    }

    private class EditTextSetting extends BaseSetting<Integer> {
        private EditText editText;
        private TextWatcher textWatcher;
        
        EditTextSetting(String key, @IdRes int editTextId) {
            super(key);
            this.editText = findViewById(editTextId);
        }

        @Override
        public void load() {
            editText.setText(String.valueOf(prefs.getInt(key, defaultValue)));

            if (textWatcher != null) editText.removeTextChangedListener(textWatcher);
            textWatcher = new TextWatcher() {
                 @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                 @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
                 @Override public void afterTextChanged(Editable s) {
                     try {
                         String valStr = s.toString().trim();
                         if (valStr.isEmpty() || valStr.equals("-")) return;
                         editor.putInt(key, Integer.parseInt(valStr)).apply();
                     } catch (NumberFormatException e) { /* Ignore */ }
                 }
            };
            editText.addTextChangedListener(textWatcher);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (backgroundView != null) backgroundView.resume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (backgroundView != null) backgroundView.pause();
        editor.apply();
    }
    
    private void populateDefaultValues() {
        defaultValues.put(PREF_BIRD_COLOR, DEFAULT_BIRD_COLOR);
        defaultValues.put(PREF_BACKGROUND, DEFAULT_BACKGROUND);
        defaultValues.put(PREF_SOUND_ENABLED, DEFAULT_SOUND_ENABLED);
        defaultValues.put(PREF_WING_ANIMATION_ENABLED, DEFAULT_WING_ANIMATION_ENABLED);
        defaultValues.put(PREF_PIPE_COLOR, DEFAULT_PIPE_COLOR);
        defaultValues.put(PREF_HIDE_SETTINGS_ICON, DEFAULT_HIDE_SETTINGS_ICON);
        defaultValues.put(PREF_RAINBOW_BIRD_ENABLED, DEFAULT_RAINBOW_BIRD_ENABLED);
        defaultValues.put(PREF_GAMEOVER_OPACITY, DEFAULT_GAMEOVER_OPACITY);
        defaultValues.put(PREF_GAME_SPEED, DEFAULT_GAME_SPEED);
        defaultValues.put(PREF_GRAVITY, DEFAULT_GRAVITY);
        defaultValues.put(PREF_JUMP_STRENGTH, DEFAULT_JUMP_STRENGTH);
        defaultValues.put(PREF_BIRD_HANG_DELAY, DEFAULT_BIRD_HANG_DELAY);
        defaultValues.put(PREF_PIPE_GAP, DEFAULT_PIPE_GAP);
        defaultValues.put(PREF_PIPE_SPACING, DEFAULT_PIPE_SPACING);
        defaultValues.put(PREF_BIRD_HITBOX, DEFAULT_BIRD_HITBOX);
        defaultValues.put(PREF_PIPE_VARIATION, DEFAULT_PIPE_VARIATION);
        defaultValues.put(PREF_SCORE_MULTIPLIER, DEFAULT_SCORE_MULTIPLIER);
        defaultValues.put(PREF_MOVING_PIPES_ENABLED, DEFAULT_MOVING_PIPES_ENABLED);
        defaultValues.put(PREF_PIPE_MOVE_TIER_1_SCORE, DEFAULT_PIPE_MOVE_TIER_1_SCORE);
        defaultValues.put(PREF_PIPE_MOVE_TIER_2_SCORE, DEFAULT_PIPE_MOVE_TIER_2_SCORE);
        defaultValues.put(PREF_NO_CLIP_ENABLED, DEFAULT_NO_CLIP_ENABLED);
        defaultValues.put(PREF_UPSIDE_DOWN_ENABLED, DEFAULT_UPSIDE_DOWN_ENABLED);
        defaultValues.put(PREF_REVERSE_PIPES_ENABLED, DEFAULT_REVERSE_PIPES_ENABLED);
        defaultValues.put(PREF_HAPTIC_FEEDBACK_ENABLED, DEFAULT_HAPTIC_FEEDBACK_ENABLED);
        defaultValues.put(PREF_BIRD_TRAIL_ENABLED, DEFAULT_BIRD_TRAIL_ENABLED);
        defaultValues.put(PREF_GHOST_MODE_ENABLED, DEFAULT_GHOST_MODE_ENABLED);
        defaultValues.put(PREF_PIPE_SPEED_VARIATION, DEFAULT_PIPE_SPEED_VARIATION);
        defaultValues.put(PREF_BIRD_SIZE, DEFAULT_BIRD_SIZE);
        defaultValues.put(PREF_PIPE_WIDTH, DEFAULT_PIPE_WIDTH);
        defaultValues.put(PREF_BG_SCROLL_SPEED, DEFAULT_BG_SCROLL_SPEED);
        defaultValues.put(PREF_GROUND_SCROLL_SPEED, DEFAULT_GROUND_SCROLL_SPEED);
        defaultValues.put(PREF_RANDOM_PIPE_COLORS_ENABLED, DEFAULT_RANDOM_PIPE_COLORS_ENABLED);
        defaultValues.put(PREF_INFINITE_FLAP_ENABLED, DEFAULT_INFINITE_FLAP_ENABLED);
    }
    
    public static class DecimalDigitsInputFilter implements InputFilter {
        @Override
        public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
            StringBuilder builder = new StringBuilder(dest);
            builder.replace(dstart, dend, source.subSequence(start, end).toString());
            String text = builder.toString();

            int dotIndex = text.indexOf(".");
            if (dotIndex > 0) {
                if (text.length() - dotIndex - 1 > 2) {
                    return "";
                }
            }
            return null;
        }
    }
    
    static class SettingsBackgroundView extends View implements Choreographer.FrameCallback {

        private Choreographer choreographer;
        private boolean isRunning = false;
        private boolean isReady = false;
        private long lastFrameTimeNanos = 0;
        private int screenWidth, screenHeight;
        private Bitmap bgDayBitmap, bgNightBitmap, groundBitmap, currentBgBitmap, unscaledBgDay;
        private float groundX = 0, backgroundX = 0;
        private int groundHeight;
        private float scale, baseSpeed;
        private float speedMultiplier = 1.0f;
        private float bgScrollMultiplier = 0.5f;
        private float groundScrollMultiplier = 1.0f;
        private final Paint pixelPaint = new Paint();
        private int backgroundStyle = -2;
        private static final float TARGET_FPS = 120.0f;


        public SettingsBackgroundView(Context context, SharedPreferences prefs) {
            super(context);
            this.speedMultiplier = prefs.getFloat(PREF_GAME_SPEED, DEFAULT_GAME_SPEED);
            this.bgScrollMultiplier = prefs.getFloat(PREF_BG_SCROLL_SPEED, DEFAULT_BG_SCROLL_SPEED);
            this.groundScrollMultiplier = prefs.getFloat(PREF_GROUND_SCROLL_SPEED, DEFAULT_GROUND_SCROLL_SPEED);
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
            if (w == 0 || h == 0 || isReady) return;

            try {
                screenWidth = w;
                screenHeight = h;
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inScaled = false;
                Bitmap atlas = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.atlas, options);
                if (atlas == null) {
                    isReady = false;
                    return;
                }

                unscaledBgDay = Bitmap.createBitmap(atlas, 0, 0, 288, 512);
                Bitmap unscaledBgNight = Bitmap.createBitmap(atlas, 292, 0, 288, 512);
                Bitmap unscaledLand = Bitmap.createBitmap(atlas, 584, 0, 336, 112);
                atlas.recycle();
                
                scale = (float) screenHeight / unscaledBgDay.getHeight();
                baseSpeed = (1.1f * scale) * TARGET_FPS;

                float heightScale = (float) screenHeight / unscaledBgDay.getHeight();
                int bgWidth = (int) (unscaledBgDay.getWidth() * heightScale);

                bgDayBitmap = Bitmap.createScaledBitmap(unscaledBgDay, bgWidth, screenHeight, false);
                bgNightBitmap = Bitmap.createScaledBitmap(unscaledBgNight, bgWidth, screenHeight, false);
                groundBitmap = Bitmap.createScaledBitmap(unscaledLand, (int)(unscaledLand.getWidth() * heightScale), (int)(unscaledLand.getHeight() * heightScale), false);

                unscaledBgNight.recycle();
                unscaledLand.recycle();

                groundHeight = groundBitmap.getHeight();
                isReady = true;

                applyBackgroundStyle();
                resume();
            } catch (Throwable t) {
                isReady = false;
            }
        }

        public void setBackgroundStyle(int style) {
            this.backgroundStyle = style;
            if (isReady) {
                applyBackgroundStyle();
            }
        }

        private void applyBackgroundStyle() {
            try {
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
            } catch (Exception e) { /* Failsafe */ }
        }

        public void setSpeedMultiplier(float multiplier) { this.speedMultiplier = Math.max(0, multiplier); }
        public void setBgScrollMultiplier(float multiplier) { this.bgScrollMultiplier = Math.max(0, multiplier); }
        public void setGroundScrollMultiplier(float multiplier) { this.groundScrollMultiplier = Math.max(0, multiplier); }
        public void resetPositions() { this.backgroundX = 0; this.groundX = 0; }

        @Override
        public void doFrame(long frameTimeNanos) {
            if (!isRunning || !isReady) {
                if(!isRunning) choreographer.removeFrameCallback(this);
                return;
            }

            try {
                if (lastFrameTimeNanos == 0) {
                    lastFrameTimeNanos = frameTimeNanos;
                    choreographer.postFrameCallback(this);
                    return;
                }

                float deltaTime = (frameTimeNanos - lastFrameTimeNanos) / 1_000_000_000.0f;
                lastFrameTimeNanos = frameTimeNanos;
                if (deltaTime > 0.1f) deltaTime = 0.016f;

                if (currentBgBitmap == null || groundBitmap == null || currentBgBitmap.isRecycled() || groundBitmap.isRecycled()) {
                    choreographer.postFrameCallback(this);
                    return;
                }

                float effectivePipeSpeed = baseSpeed * speedMultiplier;
                float bgDelta = (effectivePipeSpeed * bgScrollMultiplier) * deltaTime;
                backgroundX = (backgroundX - bgDelta) % currentBgBitmap.getWidth();
                float groundDelta = (effectivePipeSpeed * groundScrollMultiplier) * deltaTime;
                groundX = (groundX - groundDelta) % groundBitmap.getWidth();

                invalidate();
                choreographer.postFrameCallback(this);
            } catch (Exception e) { /* Failsafe */ }
        }

        @Override
        protected void onDraw(@NonNull Canvas canvas) {
            super.onDraw(canvas);
            if (!isReady || currentBgBitmap == null || groundBitmap == null || currentBgBitmap.isRecycled() || groundBitmap.isRecycled()) return;

            try {
                for (float x = backgroundX; x < screenWidth; x += currentBgBitmap.getWidth()) {
                    canvas.drawBitmap(currentBgBitmap, x, 0, pixelPaint);
                }
                float groundTopY = screenHeight - groundHeight;
                for (float x = groundX; x < screenWidth; x += groundBitmap.getWidth()) {
                    canvas.drawBitmap(groundBitmap, x, groundTopY, pixelPaint);
                }
            } catch (Exception e) { /* Failsafe */ }
        }

        public void resume() {
            if (!isRunning && isReady) {
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
            try {
                if(unscaledBgDay != null && !unscaledBgDay.isRecycled()) unscaledBgDay.recycle();
                if(bgDayBitmap != null && !bgDayBitmap.isRecycled()) bgDayBitmap.recycle();
                if(bgNightBitmap != null && !bgNightBitmap.isRecycled()) bgNightBitmap.recycle();
                if(groundBitmap != null && !groundBitmap.isRecycled()) groundBitmap.recycle();
            } catch (Exception e) { /* Failsafe */ } 
            finally {
                unscaledBgDay = null; bgDayBitmap = null; bgNightBitmap = null;
                groundBitmap = null; currentBgBitmap = null; isReady = false;
            }
        }
    }
}