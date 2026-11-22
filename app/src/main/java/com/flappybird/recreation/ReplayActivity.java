package com.flappybird.recreation;

import android.Manifest;
import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RippleDrawable;
import android.media.AudioFormat;
import android.media.Image;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.text.format.DateFormat;
import android.text.format.DateUtils;
import android.util.TypedValue;
import android.view.Choreographer;
import android.view.Display;
import android.view.Gravity;
import android.view.HapticFeedbackConstants;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.view.animation.PathInterpolator;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.slider.Slider;
import com.google.android.material.snackbar.Snackbar;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

public class ReplayActivity extends AppCompatActivity implements Choreographer.FrameCallback {

    static final int COLOR_BG = 0xFF0D1321;
    static final int COLOR_ROW = 0xFF182134;
    static final int COLOR_HIGH = 0xFF253049;
    static final int COLOR_ON = 0xFFE8ECF6;
    static final int COLOR_ON_VARIANT = 0xFFA4AEC4;
    static final int COLOR_PRIMARY = 0xFFFFD23F;
    static final int COLOR_ON_PRIMARY = 0xFF2B2100;
    static final int COLOR_PRIMARY_CONTAINER = 0xFF4A3D0A;
    static final int COLOR_ON_PRIMARY_CONTAINER = 0xFFFFE38A;
    static final int COLOR_TRACK_OFF = 0xFF2E3A57;
    static final int COLOR_RIPPLE = 0x29FFFFFF;
    static final int COLOR_PRIMARY_RIPPLE = 0x33FFD23F;
    static final int COLOR_SKY = 0xFF63D2E0;
    static final int COLOR_SCRIM = 0xCC0D1321;
    static final int COLOR_SNACKBAR = 0xFFE8ECF6;
    static final int COLOR_ON_SNACKBAR = 0xFF0D1321;
    static final int COLOR_SNACKBAR_ACTION = 0xFF7A5900;
    static final float[] SPEEDS = {0.25f, 0.5f, 1f, 2f};
    static final int NORMAL_SPEED = 2;
    static final long CONTROLS_HIDE_MS = 3000L;
    static final int AUDIO_RATE = 48000;
    static final int AUDIO_BITRATE = 256000;
    static final int AUDIO_FRAMES = 1024;
    static final float[] QUALITY_BITS = {0.12f, 0.2f, 0.32f};
    static final String MOVIES_FOLDER = SettingsActivity.APP_NAME;
    static final int REQUEST_STORAGE = 41;
    static final double LIVE_SEEK_STEPS = 6000;

    private final Choreographer choreographer = Choreographer.getInstance();
    private SharedPreferences prefs;
    private Typeface pixelFont;
    private Typeface pixelBold;
    private Typeface mediumFont;
    private float density;
    private FrameLayout root;
    private LinearLayout listContent;
    private View listScreen;
    private FrameLayout playerScreen;
    private FrameLayout stage;
    private LinearLayout controls;
    private LinearLayout topBar;
    private TextView timeText;
    private ImageView playButton;
    private TextView speedButton;
    private ImageView soundButton;
    private MaterialButton saveButton;
    private Slider timeline;
    private GameView player;
    private GameView.Film film;
    private File filmFile;
    private double filmLength;
    private boolean lengthKnown;
    private boolean filmLoaded;
    private boolean playing;
    private boolean soundOn = true;
    private int speedIndex = NORMAL_SPEED;
    private long lastFrameNanos;
    private double playClock;
    private float lastStep;
    private long controlsShownAt;
    private boolean controlsVisible = true;
    private boolean draggingTimeline;
    private boolean resumeAfterDrag;
    private double pendingSeek = -1.0;
    private boolean seekPosted;
    private boolean looping;
    private Insets bars = Insets.NONE;
    private Exporter exporter;
    private AlertDialog exportDialog;
    private TextView exportText;
    private View exportBar;
    private int playerToken;
    private ScrollView listScroll;
    private Thumbs thumbs;
    private ImageView flash;
    private TextView bubble;
    private boolean leaving;
    private boolean animateRows;
    private int rowIndex;
    private long lastTapAt;
    private int lastTapZone;
    private int consumedKey = -1;
    private MorphView morph;
    private boolean morphGrown, barFull, revealing, playOnReveal;
    private float barTarget;
    private ValueAnimator barAnimator;
    private GameView pendingPlayer;
    private GameView.Film pendingFilm;
    private Map<String, Object> pendingValues;
    private int pendingToken;
    private Runnable pendingMeasure;
    private View playerBackdrop;
    private java.util.concurrent.ExecutorService loader;
    private long shownSecond = -1L;
    private long shownLength = -1L;
    static final long DOUBLE_TAP_MS = 300L;
    static final int THUMB_WIDTH_DP = 66, THUMB_HEIGHT_DP = 88;
    static final TimeInterpolator EMPHASIZED_DECELERATE = new PathInterpolator(0.05f, 0.7f, 0.1f, 1f);
    static final TimeInterpolator EMPHASIZED_ACCELERATE = new PathInterpolator(0.3f, 0f, 0.8f, 0.15f);
    static final TimeInterpolator STANDARD = new PathInterpolator(0.2f, 0f, 0f, 1f);

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        setTheme(R.style.Theme_Material3Expressive_Dark_NoActionBar);
        super.onCreate(savedInstanceState);
        prefs = getSharedPreferences(SettingsActivity.PREFS_NAME, MODE_PRIVATE);
        density = getResources().getDisplayMetrics().density;
        loadFonts();
        soundOn = flag(SettingsActivity.PREF_SOUND_ENABLED, true);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        root = new FrameLayout(this);
        root.setBackgroundColor(COLOR_BG);
        setContentView(root);
        hideSystemBars();
        root.post(this::requestHighestRefreshRate);
        ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
            bars = insets.getInsets(WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.displayCutout());
            applyInsets();
            return insets;
        });
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (playerScreen != null) closePlayer();
                else finish();
            }
        });
        showList();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) hideSystemBars();
    }

    private void hideSystemBars() {
        WindowInsetsControllerCompat controller = WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
        controller.setSystemBarsBehavior(WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
        controller.hide(WindowInsetsCompat.Type.systemBars());
    }

    private void requestHighestRefreshRate() {
        SettingsActivity.preferSmoothDisplay(this);
    }

    private void loadFonts() {
        try {
            pixelFont = ResourcesCompat.getFont(this, R.font.flappy_font);
        } catch (RuntimeException e) {
            pixelFont = null;
        }
        try {
            pixelBold = ResourcesCompat.getFont(this, R.font.flappy_bold);
        } catch (RuntimeException e) {
            pixelBold = null;
        }
        if (pixelBold == null) pixelBold = pixelFont;
        mediumFont = Typeface.create("sans-serif-medium", Typeface.NORMAL);
    }

    private boolean flag(String key, boolean fallback) {
        Object stored = prefs.getAll().get(key);
        return stored instanceof Boolean ? (Boolean) stored : fallback;
    }

    private boolean hapticsOn() {
        return flag(SettingsActivity.PREF_HAPTIC_FEEDBACK_ENABLED, SettingsActivity.DEFAULT_HAPTIC_FEEDBACK_ENABLED);
    }

    private void haptic(View view, int kind) {
        if (view != null && hapticsOn() && flag(SettingsActivity.PREF_HAPTIC_MENUS, SettingsActivity.DEFAULT_HAPTIC_MENUS)) view.performHapticFeedback(kind);
    }

    private int confirmHaptic() {
        return Build.VERSION.SDK_INT >= 30 ? HapticFeedbackConstants.CONFIRM : HapticFeedbackConstants.VIRTUAL_KEY;
    }

    private int rejectHaptic() {
        return Build.VERSION.SDK_INT >= 30 ? HapticFeedbackConstants.REJECT : HapticFeedbackConstants.LONG_PRESS;
    }

    private int dp(float value) {
        return Math.round(value * density);
    }

    private TextView text(String value, float size, int color, Typeface face) {
        TextView view = new TextView(this);
        view.setText(value);
        view.setTextSize(TypedValue.COMPLEX_UNIT_SP, size);
        view.setTextColor(color);
        if (face != null) view.setTypeface(face);
        return view;
    }

    private static GradientDrawable rounded(int color, float radius) {
        GradientDrawable shape = new GradientDrawable();
        shape.setColor(color);
        shape.setCornerRadius(radius);
        return shape;
    }

    private static Drawable rowBackground(float top, float bottom) {
        GradientDrawable shape = new GradientDrawable();
        shape.setColor(COLOR_ROW);
        shape.setCornerRadii(new float[] {top, top, top, top, bottom, bottom, bottom, bottom});
        return new RippleDrawable(ColorStateList.valueOf(COLOR_RIPPLE), shape, null);
    }

    private ImageView roundButton(int icon, int fill, int iconColor, int size, String description) {
        ImageView button = new ImageView(this);
        GradientDrawable shape = new GradientDrawable();
        shape.setShape(GradientDrawable.OVAL);
        shape.setColor(fill);
        button.setBackground(new RippleDrawable(ColorStateList.valueOf(fill == COLOR_PRIMARY ? 0x33000000 : COLOR_RIPPLE), shape, null));
        button.setScaleType(ImageView.ScaleType.CENTER);
        button.setImageDrawable(new Icon(icon, dp(size * 0.5f), dp(2.5f), iconColor));
        button.setContentDescription(description);
        button.setClickable(true);
        button.setFocusable(true);
        return button;
    }

    private void setIcon(ImageView button, int icon, int color, int size, String description) {
        button.setImageDrawable(new Icon(icon, dp(size * 0.5f), dp(2.5f), color));
        button.setContentDescription(description);
    }

    private AlertDialog showDialog(MaterialAlertDialogBuilder builder) {
        AlertDialog dialog = builder.show();
        int[] which = {DialogInterface.BUTTON_POSITIVE, DialogInterface.BUTTON_NEGATIVE, DialogInterface.BUTTON_NEUTRAL};
        for (int type : which) {
            Button button = dialog.getButton(type);
            if (button == null) continue;
            button.setTextColor(COLOR_PRIMARY);
            if (button instanceof MaterialButton) ((MaterialButton) button).setRippleColor(ColorStateList.valueOf(COLOR_PRIMARY_RIPPLE));
        }
        return dialog;
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

    private void applyInsets() {
        if (listScreen != null) listScreen.setPadding(bars.left, bars.top, bars.right, 0);
        if (listContent != null) listContent.setPadding(dp(16), dp(4), dp(16), dp(24) + bars.bottom);
        if (topBar != null) topBar.setPadding(dp(14) + bars.left, dp(8) + bars.top, dp(16) + bars.right, dp(24));
        if (controls != null) controls.setPadding(dp(16) + bars.left, dp(24), dp(16) + bars.right, dp(12) + bars.bottom);
        if (stage != null) fitStage();
    }

    private LinearLayout header(String title, View.OnClickListener back) {
        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setGravity(Gravity.CENTER_VERTICAL);
        ImageView backButton = roundButton(Icon.BACK, COLOR_HIGH, COLOR_ON, 44, "Back");
        backButton.setOnClickListener(back);
        pressable(backButton);
        header.addView(backButton, new LinearLayout.LayoutParams(dp(44), dp(44)));
        TextView label = text(title, 26, COLOR_PRIMARY, pixelBold);
        label.setLetterSpacing(0.06f);
        label.setMaxLines(1);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        params.setMarginStart(dp(14));
        header.addView(label, params);
        return header;
    }

    private void pressable(final View view) {
        view.setOnTouchListener((v, event) -> {
            int action = event.getAction();
            if (action == MotionEvent.ACTION_DOWN) {
                v.animate().scaleX(0.96f).scaleY(0.96f).setDuration(110).setInterpolator(STANDARD).start();
            } else if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
                v.animate().scaleX(1f).scaleY(1f).setDuration(260).setInterpolator(new OvershootInterpolator(2f)).start();
            }
            return false;
        });
    }

    private void pop(View view) {
        if (view == null) return;
        view.animate().cancel();
        view.setScaleX(0.84f);
        view.setScaleY(0.84f);
        view.animate().scaleX(1f).scaleY(1f).setDuration(280).setInterpolator(new OvershootInterpolator(2.4f)).start();
    }

    private void showList() {
        LinearLayout screen = new LinearLayout(this);
        screen.setOrientation(LinearLayout.VERTICAL);
        LinearLayout head = header("REPLAYS", v -> finish());
        head.setPadding(dp(14), 0, dp(16), 0);
        screen.addView(head, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(60)));
        listScroll = new ScrollView(this);
        listScroll.setClipToPadding(false);
        listContent = new LinearLayout(this);
        listContent.setOrientation(LinearLayout.VERTICAL);
        listScroll.addView(listContent);
        screen.addView(listScroll, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));
        listScreen = screen;
        root.removeAllViews();
        root.addView(screen, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        applyInsets();
        fillList(true);
        screen.setAlpha(0f);
        screen.setTranslationY(dp(18));
        screen.animate().alpha(1f).translationY(0f).setDuration(340).setInterpolator(EMPHASIZED_DECELERATE).start();
    }

    @Override
    public void finish() {
        if (leaving || listScreen == null || playerScreen != null || root == null) {
            super.finish();
            return;
        }
        leaving = true;
        listScreen.animate().cancel();
        listScreen.animate().alpha(0f).translationY(dp(14)).setDuration(170).setInterpolator(EMPHASIZED_ACCELERATE).withEndAction(() -> {
            ReplayActivity.super.finish();
            overridePendingTransition(0, 0);
        }).start();
    }

    static List<File> recentFilms(File dir, int keep) {
        List<File> all = GameView.films(dir);
        return all.size() > keep ? new ArrayList<>(all.subList(0, keep)) : all;
    }

    static List<File> bestFilms(File dir) {
        List<File> all = GameView.films(dir, GameView.BEST_PREFIX);
        return all.size() > 1 ? new ArrayList<>(all.subList(0, 1)) : all;
    }

    private int keep() {
        Object stored = prefs.getAll().get(SettingsActivity.PREF_REPLAY_KEEP);
        return SettingsActivity.replayKeep(stored instanceof Integer ? (Integer) stored : SettingsActivity.DEFAULT_REPLAY_KEEP);
    }

    private boolean recording() {
        return flag(SettingsActivity.PREF_REPLAY_ENABLED, SettingsActivity.DEFAULT_REPLAY_ENABLED);
    }

    private boolean bestOnly() {
        Object stored = prefs.getAll().get(SettingsActivity.PREF_REPLAY_MODE);
        return (stored instanceof Integer ? (Integer) stored : SettingsActivity.DEFAULT_REPLAY_MODE) == SettingsActivity.REPLAY_MODE_BEST;
    }

    private File filmFolder() {
        return new File(getFilesDir(), GameView.FILM_DIR);
    }

    private void fillList(boolean animate) {
        listContent.removeAllViews();
        rowIndex = 0;
        animateRows = animate;
        File dir = filmFolder();
        List<File> recent = recentFilms(dir, keep());
        List<File> best = bestFilms(dir);
        boolean bestFirst = bestOnly();
        int shown = 0;
        if (bestFirst) {
            shown += addSection(recording() ? "Your best round" : "Best round", best);
            shown += addSection(recent.size() == 1 ? "Earlier round" : "Earlier rounds", recent);
        } else {
            shown += addSection(recent.size() == 1 ? "Latest round" : "Latest " + recent.size() + " rounds", recent);
            shown += addSection("Best round", best);
        }
        List<File> all = new ArrayList<>(recent);
        all.addAll(best);
        thumbs().forget(all);
        if (shown == 0) {
            String message;
            if (!recording()) message = "Replays are off\nTurn on Record replays in Settings";
            else if (bestFirst) message = "No best round yet\nPlay a round and it shows up here";
            else message = "No replays yet\nPlay a round and it shows up here";
            TextView empty = text(message, 15, COLOR_ON_VARIANT, pixelFont);
            empty.setGravity(Gravity.CENTER);
            empty.setLineSpacing(dp(6), 1f);
            empty.setPadding(dp(16), dp(56), dp(16), dp(40));
            listContent.addView(empty, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            enter(empty);
            return;
        }
        String note;
        if (!recording()) note = "Recording is off, so new rounds won't be saved.";
        else if (bestFirst) note = "Only your best round is kept. Beat it and the new one takes its place.";
        else note = "Your newest rounds are kept automatically.";
        TextView hint = text(note + " Long-press a replay to delete it.", 13, COLOR_ON_VARIANT, null);
        hint.setPadding(dp(8), dp(16), dp(8), 0);
        listContent.addView(hint);
        enter(hint);
    }

    private void enter(View view) {
        int index = rowIndex++;
        if (!animateRows) return;
        view.setAlpha(0f);
        view.setTranslationY(dp(14));
        view.animate().alpha(1f).translationY(0f).setStartDelay(60L + Math.min(8, index) * 35L).setDuration(320)
                .setInterpolator(EMPHASIZED_DECELERATE).start();
    }

    private int addSection(String title, List<File> files) {
        List<GameView.Film> loaded = new ArrayList<>();
        List<File> kept = new ArrayList<>();
        for (File file : files) {
            try {
                loaded.add(GameView.Film.readInfo(file));
                kept.add(file);
            } catch (IOException e) {
                file.delete();
            }
        }
        if (loaded.isEmpty()) return 0;
        TextView label = text(title.toUpperCase(Locale.US), 12, COLOR_PRIMARY, pixelBold);
        label.setLetterSpacing(0.12f);
        LinearLayout.LayoutParams labelParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        labelParams.setMarginStart(dp(8));
        labelParams.topMargin = dp(listContent.getChildCount() == 0 ? 6 : 24);
        labelParams.bottomMargin = dp(10);
        listContent.addView(label, labelParams);
        enter(label);
        for (int i = 0; i < loaded.size(); i++) {
            View row = row(kept.get(i), loaded.get(i));
            float top = i == 0 ? dp(24) : dp(6);
            float bottom = i == loaded.size() - 1 ? dp(24) : dp(6);
            row.setBackground(rowBackground(top, bottom));
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.topMargin = i == 0 ? 0 : dp(3);
            listContent.addView(row, params);
            enter(row);
        }
        return loaded.size();
    }

    static String modeName(GameView.Film film) {
        return film.classic() ? "Classic" : film.mode == SettingsActivity.FAIR_STANDARD ? "Standard" : "Custom settings";
    }

    static String describe(GameView.Film film) {
        return modeName(film) + "  ·  " + film.flaps + (film.flaps == 1 ? " flap" : " flaps");
    }

    private String when(long time) {
        Date date = new Date(time);
        String clock = DateFormat.getTimeFormat(this).format(date);
        if (DateUtils.isToday(time)) return "Today " + clock;
        if (DateUtils.isToday(time + DateUtils.DAY_IN_MILLIS)) return "Yesterday " + clock;
        return DateFormat.getMediumDateFormat(this).format(date) + ", " + clock;
    }

    static String clockText(double seconds) {
        long whole = Math.max(0L, (long) Math.floor(seconds));
        return String.format(Locale.US, "%d:%02d", whole / 60, whole % 60);
    }

    static int intPref(Map<String, Object> values, String key, int fallback) {
        Object value = values.get(key);
        return value instanceof Integer ? (Integer) value : fallback;
    }

    private View row(final File file, final GameView.Film loaded) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setMinimumHeight(dp(100));
        row.setPadding(dp(10), dp(10), dp(12), dp(10));
        ThumbView thumb = new ThumbView(this, dp(12), clockText(loaded.seconds), pixelFont, density);
        thumb.setContentDescription("Snapshot of the round");
        row.addView(thumb, new LinearLayout.LayoutParams(dp(THUMB_WIDTH_DP), dp(THUMB_HEIGHT_DP)));
        thumbs().request(file, thumb);
        LinearLayout info = new LinearLayout(this);
        info.setOrientation(LinearLayout.VERTICAL);
        info.setPadding(dp(14), 0, dp(8), 0);
        LinearLayout titleLine = new LinearLayout(this);
        titleLine.setOrientation(LinearLayout.HORIZONTAL);
        titleLine.setGravity(Gravity.CENTER_VERTICAL);
        TextView score = text("SCORE " + loaded.score, 17, COLOR_PRIMARY, pixelBold);
        score.setLetterSpacing(0.04f);
        score.setMaxLines(1);
        titleLine.addView(score);
        if (loaded.score > loaded.bestBefore) {
            TextView best = text("NEW BEST", 10, COLOR_ON_PRIMARY_CONTAINER, pixelBold);
            best.setLetterSpacing(0.08f);
            best.setBackground(rounded(COLOR_PRIMARY_CONTAINER, dp(8)));
            best.setPadding(dp(8), dp(2), dp(8), dp(2));
            LinearLayout.LayoutParams bestParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            bestParams.setMarginStart(dp(8));
            titleLine.addView(best, bestParams);
        }
        info.addView(titleLine);
        TextView date = text(when(loaded.savedAt), 15, COLOR_ON, mediumFont);
        date.setMaxLines(1);
        LinearLayout.LayoutParams dateParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dateParams.topMargin = dp(4);
        info.addView(date, dateParams);
        TextView summary = text(describe(loaded), 13, COLOR_ON_VARIANT, null);
        summary.setMaxLines(1);
        LinearLayout.LayoutParams summaryParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        summaryParams.topMargin = dp(2);
        info.addView(summary, summaryParams);
        row.addView(info, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        ImageView watch = roundButton(Icon.PLAY, COLOR_PRIMARY, COLOR_ON_PRIMARY, 44, "Watch");
        watch.setFocusable(false);
        watch.setOnClickListener(v -> openPlayer(file, loaded));
        pressable(watch);
        row.addView(watch, new LinearLayout.LayoutParams(dp(44), dp(44)));
        row.setClickable(true);
        row.setFocusable(true);
        row.setTag(file);
        row.setContentDescription("Score " + loaded.score + ", " + when(loaded.savedAt) + ", " + describe(loaded) + ", " + clockText(loaded.seconds) + " long");
        row.setOnClickListener(v -> openPlayer(file, loaded));
        row.setOnLongClickListener(v -> {
            askDelete(v, file, loaded);
            return true;
        });
        pressable(row);
        return row;
    }

    private void askDelete(final View row, final File file, final GameView.Film loaded) {
        showDialog(new MaterialAlertDialogBuilder(this)
                .setTitle("Delete this replay?")
                .setMessage("Score " + loaded.score + " from " + when(loaded.savedAt) + ". This can't be undone.")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Delete", (dialog, which) -> {
                    boolean gone = file.delete() || !file.exists();
                    thumbs().drop(file);
                    haptic(root, gone ? confirmHaptic() : rejectHaptic());
                    announce(gone ? "Replay deleted" : "The replay couldn't be deleted", null, null);
                    collapse(row, () -> {
                        if (listContent != null) fillList(false);
                    });
                }));
    }

    private void collapse(final View row, final Runnable after) {
        if (row == null || row.getParent() == null || row.getHeight() <= 0) {
            after.run();
            return;
        }
        final int height = row.getHeight();
        row.animate().alpha(0f).translationX(-dp(24)).setDuration(160).setInterpolator(EMPHASIZED_ACCELERATE).start();
        ValueAnimator shrink = ValueAnimator.ofFloat(1f, 0f);
        shrink.setDuration(240);
        shrink.setStartDelay(90);
        shrink.setInterpolator(STANDARD);
        shrink.addUpdateListener(animation -> {
            ViewGroup.LayoutParams params = row.getLayoutParams();
            params.height = Math.max(1, Math.round(height * (Float) animation.getAnimatedValue()));
            row.setLayoutParams(params);
        });
        shrink.addListener(new android.animation.AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(android.animation.Animator animation) {
                after.run();
            }
        });
        shrink.start();
    }

    static void writePlaybackPrefs(SharedPreferences store, Map<String, Object> values) {
        SharedPreferences.Editor editor = store.edit().clear();
        for (Map.Entry<String, Object> entry : values.entrySet()) {
            Object value = entry.getValue();
            if (value instanceof Integer) editor.putInt(entry.getKey(), (Integer) value);
            else if (value instanceof Float) editor.putFloat(entry.getKey(), (Float) value);
            else if (value instanceof Boolean) editor.putBoolean(entry.getKey(), (Boolean) value);
            else if (value instanceof Long) editor.putLong(entry.getKey(), (Long) value);
            else if (value != null) editor.putString(entry.getKey(), String.valueOf(value));
        }
        editor.commit();
    }

    static double deathTime(GameView.Film film) {
        return GameView.filmDeathTime(film);
    }

    static double estimateLength(GameView.Film film) {
        return deathTime(film) + 3.0;
    }

    private Thumbs thumbs() {
        if (thumbs == null) thumbs = new Thumbs(this);
        return thumbs;
    }

    static final class Thumbs {
        final ReplayActivity owner;
        final ExecutorService worker = Executors.newSingleThreadExecutor(runnable -> {
            Thread thread = new Thread(runnable, "ReplayThumbs");
            thread.setPriority(Thread.MIN_PRIORITY);
            return thread;
        });
        final Map<String, Bitmap> memory = new HashMap<>();

        Thumbs(ReplayActivity owner) {
            this.owner = owner;
        }

        Bitmap get(File file) {
            return memory.get(GameView.snapshotKey(file));
        }

        void request(final File file, final ThumbView view) {
            final String key = GameView.snapshotKey(file);
            Bitmap ready = memory.get(key);
            view.setTag(key);
            if (ready != null && !ready.isRecycled()) {
                view.show(ready, false);
                return;
            }
            worker.execute(() -> {
                final Bitmap made = GameView.snapshot(owner, file);
                owner.runOnUiThread(() -> {
                    if (made == null || owner.isDestroyed()) return;
                    memory.put(key, made);
                    if (key.equals(view.getTag())) view.show(made, true);
                });
            });
        }

        void forget(final List<File> films) {
            final android.content.Context context = owner;
            java.util.Set<String> keep = new java.util.HashSet<>();
            for (File film : films) keep.add(GameView.snapshotKey(film));
            memory.keySet().retainAll(keep);
            worker.execute(() -> GameView.forgetSnapshots(context, films));
        }

        void drop(File film) {
            memory.remove(GameView.snapshotKey(film));
        }

        void shutdown() {
            worker.shutdownNow();
        }
    }

    static final class ThumbView extends View {
        private final Paint fill = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint image = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
        private final Paint pill = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint label = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final RectF bounds = new RectF();
        private final RectF pillBounds = new RectF();
        private final Matrix matrix = new Matrix();
        private final float radius;
        private final String duration;
        private final float density;
        private Bitmap bitmap;
        private float reveal = 1f;
        private ValueAnimator fade;

        ThumbView(android.content.Context context, float radius, String duration, Typeface face, float density) {
            super(context);
            this.radius = radius;
            this.duration = duration;
            this.density = density;
            fill.setColor(COLOR_HIGH);
            pill.setColor(0xB3000000);
            label.setColor(Color.WHITE);
            label.setTextSize(11f * density);
            if (face != null) label.setTypeface(face);
            label.setTextAlign(Paint.Align.CENTER);
        }

        void show(Bitmap shown, boolean animate) {
            bitmap = shown;
            image.setShader(new BitmapShader(shown, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP));
            if (fade != null) fade.cancel();
            if (animate) {
                reveal = 0f;
                fade = ValueAnimator.ofFloat(0f, 1f);
                fade.setDuration(280);
                fade.setInterpolator(STANDARD);
                fade.addUpdateListener(animation -> {
                    reveal = (Float) animation.getAnimatedValue();
                    invalidate();
                });
                fade.start();
            } else {
                reveal = 1f;
            }
            invalidate();
        }

        boolean hasImage() {
            return bitmap != null;
        }

        Bitmap image() {
            return bitmap;
        }

        float radius() {
            return radius;
        }

        @Override
        protected void onDetachedFromWindow() {
            super.onDetachedFromWindow();
            if (fade != null) fade.cancel();
            reveal = 1f;
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            bounds.set(0, 0, getWidth(), getHeight());
            canvas.drawRoundRect(bounds, radius, radius, fill);
            if (bitmap != null && !bitmap.isRecycled() && bitmap.getWidth() > 0 && getWidth() > 0) {
                float scale = Math.max(getWidth() / (float) bitmap.getWidth(), getHeight() / (float) bitmap.getHeight());
                matrix.setScale(scale, scale);
                matrix.postTranslate((getWidth() - bitmap.getWidth() * scale) / 2f, (getHeight() - bitmap.getHeight() * scale) / 2f);
                image.getShader().setLocalMatrix(matrix);
                image.setAlpha(Math.round(255 * reveal));
                canvas.drawRoundRect(bounds, radius, radius, image);
            }
            float textWidth = label.measureText(duration);
            float padX = 6f * density, padY = 3f * density, inset = 5f * density;
            float pillHeight = label.getTextSize() + padY * 2f;
            pillBounds.set(getWidth() - inset - textWidth - padX * 2f, getHeight() - inset - pillHeight, getWidth() - inset, getHeight() - inset);
            canvas.drawRoundRect(pillBounds, pillHeight / 2f, pillHeight / 2f, pill);
            canvas.drawText(duration, pillBounds.centerX(), pillBounds.centerY() + label.getTextSize() * 0.36f, label);
        }
    }

    static final class MorphView extends View {
        private final Paint image = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
        private final Paint softImage = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
        private final Paint shade = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint track = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint fill = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Matrix matrix = new Matrix();
        private final Matrix softMatrix = new Matrix();
        private final RectF from = new RectF(), to = new RectF(), now = new RectF(), bar = new RectF();
        private final float fromRadius, toRadius;
        private final Bitmap bitmap;
        private final Bitmap soft;
        private final float barHeight, barWidth;
        private float progress, blur, loaded, barAlpha;

        MorphView(android.content.Context context, Bitmap bitmap, RectF from, float fromRadius, RectF to, float toRadius) {
            this(context, bitmap, null, from, fromRadius, to, toRadius, 1f);
        }

        MorphView(android.content.Context context, Bitmap bitmap, Bitmap soft, RectF from, float fromRadius, RectF to, float toRadius, float density) {
            super(context);
            this.bitmap = bitmap;
            this.soft = soft;
            this.from.set(from);
            this.to.set(to);
            this.fromRadius = fromRadius;
            this.toRadius = toRadius;
            barHeight = 4f * density;
            barWidth = 168f * density;
            image.setShader(new BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP));
            if (soft != null) softImage.setShader(new BitmapShader(soft, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP));
            shade.setColor(Color.BLACK);
            track.setColor(Color.WHITE);
            fill.setColor(Color.WHITE);
            setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);
        }

        static Bitmap soften(Bitmap source) {
            if (source == null || source.isRecycled() || source.getWidth() < 4 || source.getHeight() < 4) return null;
            try {
                int width = Math.max(12, Math.min(48, source.getWidth() / 7));
                int height = Math.max(12, Math.round(width * source.getHeight() / (float) source.getWidth()));
                Bitmap middle = Bitmap.createScaledBitmap(source, width * 3, height * 3, true);
                Bitmap small = Bitmap.createScaledBitmap(middle, width, height, true);
                if (middle != source && middle != small) middle.recycle();
                int[] pixels = new int[width * height];
                small.getPixels(pixels, 0, width, 0, 0, width, height);
                if (small != source) small.recycle();
                int[] spare = new int[pixels.length];
                for (int pass = 0; pass < 2; pass++) {
                    boxBlur(pixels, spare, width, height, 2, true);
                    boxBlur(spare, pixels, width, height, 2, false);
                }
                return Bitmap.createBitmap(pixels, width, height, Bitmap.Config.ARGB_8888);
            } catch (RuntimeException | OutOfMemoryError e) {
                return null;
            }
        }

        private static void boxBlur(int[] in, int[] out, int width, int height, int radius, boolean across) {
            int lines = across ? height : width;
            int length = across ? width : height;
            for (int line = 0; line < lines; line++) {
                for (int i = 0; i < length; i++) {
                    int r = 0, g = 0, b = 0, count = 0;
                    for (int k = -radius; k <= radius; k++) {
                        int at = Math.max(0, Math.min(length - 1, i + k));
                        int color = in[across ? line * width + at : at * width + line];
                        r += (color >> 16) & 0xFF;
                        g += (color >> 8) & 0xFF;
                        b += color & 0xFF;
                        count++;
                    }
                    out[across ? line * width + i : i * width + line] = 0xFF000000 | ((r / count) << 16) | ((g / count) << 8) | (b / count);
                }
            }
        }

        void setProgress(float value) {
            progress = value;
            invalidate();
        }

        void setBlur(float value) {
            blur = soft == null ? 0f : Math.max(0f, Math.min(1f, value));
            invalidate();
        }

        void setBar(float value) {
            loaded = Math.max(0f, Math.min(1f, value));
            invalidate();
        }

        void setBarAlpha(float value) {
            barAlpha = Math.max(0f, Math.min(1f, value));
            invalidate();
        }

        float bar() {
            return loaded;
        }

        float barAlpha() {
            return barAlpha;
        }

        float blur() {
            return blur;
        }

        float progress() {
            return progress;
        }

        RectF current() {
            return now;
        }

        private static float lerp(float a, float b, float t) {
            return a + (b - a) * t;
        }

        @Override
        public boolean hasOverlappingRendering() {
            return true;
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            float t = progress;
            now.set(lerp(from.left, to.left, t), lerp(from.top, to.top, t), lerp(from.right, to.right, t), lerp(from.bottom, to.bottom, t));
            if (now.width() <= 0f || now.height() <= 0f || bitmap.isRecycled()) return;
            float bw = bitmap.getWidth(), bh = bitmap.getHeight();
            float fromScale = Math.max(from.width() / bw, from.height() / bh);
            float toScale = Math.max(to.width() / bw, to.height() / bh);
            float scale = lerp(fromScale, toScale, t);
            float radius = lerp(fromRadius, toRadius, t);
            boolean softened = soft != null && !soft.isRecycled() && blur > 0f;
            if (!softened || blur < 1f) {
                matrix.setScale(scale, scale);
                matrix.postTranslate(now.centerX() - bw * scale / 2f, now.centerY() - bh * scale / 2f);
                image.getShader().setLocalMatrix(matrix);
                image.setAlpha(255);
                canvas.drawRoundRect(now, radius, radius, image);
            }
            if (softened) {
                float sw = soft.getWidth(), sh = soft.getHeight();
                float softScale = scale * bw / sw;
                softMatrix.setScale(softScale, scale * bh / sh);
                softMatrix.postTranslate(now.centerX() - bw * scale / 2f, now.centerY() - bh * scale / 2f);
                softImage.getShader().setLocalMatrix(softMatrix);
                softImage.setAlpha(Math.round(255f * blur));
                canvas.drawRoundRect(now, radius, radius, softImage);
                shade.setAlpha(Math.round(70f * blur));
                canvas.drawRoundRect(now, radius, radius, shade);
            }
            if (barAlpha > 0f) {
                float width = Math.min(barWidth, now.width() * 0.5f);
                float left = now.centerX() - width / 2f;
                float top = now.centerY() - barHeight / 2f;
                bar.set(left, top, left + width, top + barHeight);
                track.setAlpha(Math.round(70f * barAlpha));
                canvas.drawRoundRect(bar, barHeight / 2f, barHeight / 2f, track);
                bar.right = left + Math.max(barHeight, width * loaded);
                fill.setAlpha(Math.round(255f * barAlpha));
                canvas.drawRoundRect(bar, barHeight / 2f, barHeight / 2f, fill);
            }
        }
    }

    private RectF boundsIn(View view, View container) {
        int[] at = new int[2], base = new int[2];
        view.getLocationInWindow(at);
        container.getLocationInWindow(base);
        float left = at[0] - base[0], top = at[1] - base[1];
        return new RectF(left, top, left + view.getWidth() * view.getScaleX(), top + view.getHeight() * view.getScaleY());
    }

    private RectF stageRect(GameView.Film shown) {
        float width = root.getWidth(), height = root.getHeight();
        if (width <= 0f || height <= 0f || shown.width <= 0 || shown.height <= 0) return new RectF(0, 0, width, height);
        float scale = Math.min(width / shown.width, height / shown.height);
        float w = shown.width * scale, h = shown.height * scale;
        return new RectF((width - w) / 2f, (height - h) / 2f, (width + w) / 2f, (height + h) / 2f);
    }

    private ThumbView thumbFor(File file) {
        if (listContent == null || file == null) return null;
        for (int i = 0; i < listContent.getChildCount(); i++) {
            View child = listContent.getChildAt(i);
            if (file.equals(child.getTag()) && child instanceof ViewGroup && ((ViewGroup) child).getChildCount() > 0
                    && ((ViewGroup) child).getChildAt(0) instanceof ThumbView) {
                return (ThumbView) ((ViewGroup) child).getChildAt(0);
            }
        }
        return null;
    }

    private void openPlayer(File file, GameView.Film info) {
        if (playerScreen != null || leaving) return;
        film = info;
        filmFile = file;
        filmLoaded = false;
        lengthKnown = false;
        filmLength = Math.max(1.0, info.seconds + GameView.FILM_LEAD_SEC + 3.0);
        playing = false;
        speedIndex = NORMAL_SPEED;
        pendingSeek = -1.0;
        final int token = ++playerToken;
        final Map<String, Object> values = GameView.filmPlaybackPrefs(info, true, hapticsOn());
        playerScreen = new FrameLayout(this);
        playerScreen.setClickable(true);
        playerBackdrop = new View(this);
        playerBackdrop.setBackgroundColor(Color.BLACK);
        playerScreen.addView(playerBackdrop, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        stage = new FrameLayout(this);
        stage.setClipChildren(true);
        playerScreen.addView(stage, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        stage.addOnLayoutChangeListener((v, l, t, r, b, ol, ot, or, ob) -> {
            fitStage();
            if (!filmLoaded) startFilm();
        });
        stage.setOnTouchListener(this::stageTouch);
        flash = new ImageView(this);
        flash.setBackground(rounded(0x99000000, dp(40)));
        flash.setScaleType(ImageView.ScaleType.CENTER);
        flash.setAlpha(0f);
        flash.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
        playerScreen.addView(flash, new FrameLayout.LayoutParams(dp(80), dp(80), Gravity.CENTER));
        bubble = text("", 15, COLOR_ON, pixelFont);
        bubble.setBackground(rounded(0x99000000, dp(20)));
        bubble.setPadding(dp(16), dp(8), dp(16), dp(8));
        bubble.setAlpha(0f);
        playerScreen.addView(bubble, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.CENTER));
        morphGrown = false;
        barFull = false;
        revealing = false;
        playOnReveal = false;
        barTarget = 0f;
        dropPending();
        ThumbView source = thumbFor(file);
        Bitmap still = source != null && source.hasImage() ? source.image() : thumbs().get(file);
        if (still != null && root.getWidth() > 0 && source != null && source.getWidth() > 0) {
            morph = new MorphView(this, still, MorphView.soften(still), boundsIn(source, root), source.radius(), stageRect(info), 0f,
                    getResources().getDisplayMetrics().density);
            playerScreen.addView(morph, 2, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        } else {
            morph = null;
        }
        buildControls();
        root.addView(playerScreen, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        applyInsets();
        if (morph != null) {
            final MorphView growing = morph;
            playerBackdrop.setAlpha(0f);
            playerBackdrop.animate().alpha(1f).setDuration(300).setInterpolator(STANDARD).start();
            ValueAnimator grow = ValueAnimator.ofFloat(0f, 1f);
            grow.setDuration(420);
            grow.setInterpolator(EMPHASIZED_DECELERATE);
            grow.addUpdateListener(animation -> {
                float value = (Float) animation.getAnimatedValue();
                growing.setProgress(value);
                growing.setBlur(smooth((value - 0.08f) / 0.6f));
                if (!revealing) growing.setBarAlpha(smooth((value - 0.4f) / 0.35f));
            });
            grow.addListener(new android.animation.AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(android.animation.Animator animation) {
                    if (token != playerToken || morph != growing) return;
                    growing.setProgress(1f);
                    growing.setBlur(1f);
                    if (!revealing) growing.setBarAlpha(1f);
                    morphGrown = true;
                    attachPending();
                    revealPlayer();
                }
            });
            grow.start();
            loadingTo(0.12f);
            if (source != null) source.setVisibility(View.INVISIBLE);
        } else {
            playerScreen.setAlpha(0f);
            playerScreen.setScaleX(0.9f);
            playerScreen.setScaleY(0.9f);
            playerScreen.animate().alpha(1f).scaleX(1f).scaleY(1f).setDuration(340).setInterpolator(EMPHASIZED_DECELERATE).start();
        }
        if (listScreen != null) {
            final View list = listScreen;
            final ThumbView hidden = source;
            list.animate().cancel();
            list.animate().alpha(0f).scaleX(0.96f).scaleY(0.96f).setDuration(260).setInterpolator(EMPHASIZED_ACCELERATE).withLayer()
                    .withEndAction(() -> {
                        if (playerScreen != null) list.setVisibility(View.INVISIBLE);
                        if (hidden != null) hidden.setVisibility(View.VISIBLE);
                    }).start();
        }
        final File source2 = file;
        loader().execute(() -> {
            GameView.Film full;
            GameView view;
            try {
                final double stored = GameView.storedLength(ReplayActivity.this, source2);
                full = info.complete ? info : GameView.Film.read(source2);
                runOnUiThread(() -> {
                    if (token != playerToken) return;
                    if (stored > 0.0 && !lengthKnown) setLength(stored);
                    loadingTo(0.45f);
                });
                view = new GameView(ReplayActivity.this, new MemoryPrefs(values), true);
            } catch (IOException | RuntimeException | OutOfMemoryError e) {
                runOnUiThread(() -> {
                    if (token != playerToken) return;
                    announce("This replay couldn't be opened", null, null);
                    closePlayer();
                });
                return;
            }
            final GameView.Film loaded = full;
            final GameView built = view;
            runOnUiThread(() -> attachPlayer(token, loaded, built, values));
        });
    }

    private java.util.concurrent.ExecutorService loader() {
        if (loader == null) {
            loader = Executors.newSingleThreadExecutor(runnable -> {
                Thread thread = new Thread(runnable, "ReplayLoader");
                thread.setPriority(Thread.NORM_PRIORITY - 1);
                return thread;
            });
        }
        return loader;
    }

    private void attachPlayer(int token, GameView.Film loaded, GameView built, Map<String, Object> values) {
        if (token != playerToken || stage == null || isDestroyed()) {
            built.filmRelease();
            return;
        }
        if (morph != null && !morphGrown) {
            dropPending();
            pendingPlayer = built;
            pendingFilm = loaded;
            pendingValues = values;
            pendingToken = token;
            loadingTo(0.7f);
            return;
        }
        loadingTo(0.8f);
        film = loaded;
        player = built;
        player.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
        player.setFocusable(false);
        player.setAlpha(morph != null ? 0f : 1f);
        stage.addView(player, new FrameLayout.LayoutParams(loaded.width, loaded.height));
        player.post(this::startFilm);
        if (!lengthKnown) {
            if (morph != null) pendingMeasure = () -> {
                if (!lengthKnown && film == loaded) measureLength(loaded, values);
            };
            else measureLength(loaded, values);
        }
    }

    private void attachPending() {
        GameView built = pendingPlayer;
        if (built == null) return;
        GameView.Film loaded = pendingFilm;
        Map<String, Object> values = pendingValues;
        int token = pendingToken;
        pendingPlayer = null;
        pendingFilm = null;
        pendingValues = null;
        attachPlayer(token, loaded, built, values);
    }

    private void dropPending() {
        if (pendingPlayer != null) pendingPlayer.filmRelease();
        pendingPlayer = null;
        pendingFilm = null;
        pendingValues = null;
    }

    private static float smooth(float value) {
        float t = Math.max(0f, Math.min(1f, value));
        return t * t * (3f - 2f * t);
    }

    private void loadingTo(float target) {
        final MorphView shown = morph;
        if (shown == null || revealing || target <= barTarget) return;
        barTarget = Math.min(1f, target);
        if (barAnimator != null) barAnimator.cancel();
        final boolean complete = barTarget >= 1f;
        ValueAnimator fill = ValueAnimator.ofFloat(shown.bar(), barTarget);
        fill.setDuration(complete ? 180 : 240);
        fill.setInterpolator(STANDARD);
        fill.addUpdateListener(animation -> shown.setBar((Float) animation.getAnimatedValue()));
        fill.addListener(new android.animation.AnimatorListenerAdapter() {
            private boolean cancelled;

            @Override
            public void onAnimationCancel(android.animation.Animator animation) {
                cancelled = true;
            }

            @Override
            public void onAnimationEnd(android.animation.Animator animation) {
                if (cancelled || morph != shown) return;
                if (complete) {
                    barFull = true;
                    revealPlayer();
                } else {
                    creepBar(shown);
                }
            }
        });
        barAnimator = fill;
        fill.start();
    }

    private void creepBar(MorphView shown) {
        float from = shown.bar();
        float to = Math.min(0.94f, from + (1f - from) * 0.45f);
        if (to <= from) return;
        ValueAnimator creep = ValueAnimator.ofFloat(from, to);
        creep.setDuration(1600);
        creep.setInterpolator(new DecelerateInterpolator());
        creep.addUpdateListener(animation -> {
            if (morph == shown && !barFull) shown.setBar((Float) animation.getAnimatedValue());
        });
        barAnimator = creep;
        creep.start();
    }

    private void revealPlayer() {
        if (player == null || !filmLoaded) return;
        final MorphView done = morph;
        if (done == null) {
            if (player.getAlpha() >= 1f) return;
            player.animate().cancel();
            player.animate().alpha(1f).setDuration(200).setInterpolator(STANDARD).start();
            return;
        }
        if (!morphGrown || !barFull || revealing) return;
        revealing = true;
        if (barAnimator != null) barAnimator.cancel();
        barAnimator = null;
        done.setBar(1f);
        if (playOnReveal) {
            playOnReveal = false;
            play();
        }
        player.animate().cancel();
        player.animate().alpha(1f).setDuration(160).setInterpolator(STANDARD).start();
        ValueAnimator clear = ValueAnimator.ofFloat(1f, 0f);
        clear.setDuration(320);
        clear.setInterpolator(STANDARD);
        clear.addUpdateListener(animation -> {
            float value = (Float) animation.getAnimatedValue();
            done.setBarAlpha(Math.max(0f, value * 2f - 1f));
            done.setAlpha(value);
        });
        clear.addListener(new android.animation.AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(android.animation.Animator animation) {
                if (morph == done) {
                    if (done.getParent() instanceof ViewGroup) ((ViewGroup) done.getParent()).removeView(done);
                    morph = null;
                }
                Runnable measure = pendingMeasure;
                pendingMeasure = null;
                if (measure != null) measure.run();
            }
        });
        clear.start();
    }

    private void measureLength(final GameView.Film loaded, final Map<String, Object> values) {
        final int token = playerToken;
        final File file = filmFile;
        Thread thread = new Thread(() -> {
            double length;
            try {
                GameView probe = new GameView(ReplayActivity.this, new MemoryPrefs(values), false);
                probe.measure(View.MeasureSpec.makeMeasureSpec(loaded.width, View.MeasureSpec.EXACTLY),
                        View.MeasureSpec.makeMeasureSpec(loaded.height, View.MeasureSpec.EXACTLY));
                probe.layout(0, 0, loaded.width, loaded.height);
                probe.filmLoad(loaded);
                length = probe.filmReady() ? probe.filmLength() : -1.0;
            } catch (RuntimeException | OutOfMemoryError e) {
                length = -1.0;
            }
            final double measured = length;
            if (measured > 0.0 && file != null) GameView.storeLength(ReplayActivity.this, file, measured);
            runOnUiThread(() -> {
                if (token != playerToken || measured <= 0.0) return;
                setLength(measured);
            });
        }, "ReplayLength");
        thread.setPriority(Thread.MIN_PRIORITY);
        thread.start();
    }

    private void setLength(double length) {
        lengthKnown = true;
        filmLength = Math.max(0.1, length);
        if (timeline != null) {
            float value = Math.min(timeline.getValue(), (float) filmLength);
            timeline.setValue(value);
            timeline.setValueTo((float) filmLength);
        }
        updateTime();
    }

    private void startFilm() {
        if (filmLoaded || player == null || film == null) return;
        if (player.getWidth() != film.width || player.getHeight() != film.height) return;
        player.filmLoad(film);
        if (!player.filmReady()) return;
        filmLoaded = true;
        timeline.setValueFrom(0f);
        timeline.setValueTo((float) filmLength);
        timeline.setValue(0f);
        updateTime();
        if (morph == null) {
            play();
            revealPlayer();
        } else {
            playOnReveal = true;
            loadingTo(1f);
        }
    }

    private void fitStage() {
        if (stage == null || player == null || film == null) return;
        float width = stage.getWidth(), height = stage.getHeight();
        if (width <= 0 || height <= 0) return;
        float scale = Math.min(width / film.width, height / film.height);
        player.setPivotX(0f);
        player.setPivotY(0f);
        player.setScaleX(scale);
        player.setScaleY(scale);
        player.setTranslationX((width - film.width * scale) / 2f);
        player.setTranslationY((height - film.height * scale) / 2f);
    }

    private boolean stageTouch(View v, MotionEvent event) {
        if (event.getAction() != MotionEvent.ACTION_UP) return true;
        long now = SystemClock.uptimeMillis();
        float x = event.getX();
        float width = Math.max(1f, v.getWidth());
        int zone = x < width / 3f ? -1 : x > width * 2f / 3f ? 1 : 0;
        boolean doubleTap = now - lastTapAt < DOUBLE_TAP_MS && zone == lastTapZone;
        lastTapAt = doubleTap ? 0L : now;
        lastTapZone = zone;
        if (doubleTap) {
            if (zone == 0) togglePlay(true);
            else skip(zone * 5.0);
            return true;
        }
        if (controlsVisible && playing) hideControls();
        else showControls();
        return true;
    }

    private void buildControls() {
        topBar = new LinearLayout(this);
        topBar.setOrientation(LinearLayout.HORIZONTAL);
        topBar.setGravity(Gravity.CENTER_VERTICAL);
        topBar.setBackground(new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, new int[] {COLOR_SCRIM, 0x000D1321}));
        ImageView back = roundButton(Icon.BACK, COLOR_HIGH, COLOR_ON, 44, "Back");
        back.setOnClickListener(v -> closePlayer());
        pressable(back);
        topBar.addView(back, new LinearLayout.LayoutParams(dp(44), dp(44)));
        LinearLayout titles = new LinearLayout(this);
        titles.setOrientation(LinearLayout.VERTICAL);
        TextView score = text("SCORE " + film.score, 20, COLOR_PRIMARY, pixelBold);
        score.setLetterSpacing(0.06f);
        titles.addView(score);
        TextView detail = text(when(film.savedAt) + "  ·  " + modeName(film), 13, COLOR_ON_VARIANT, null);
        detail.setMaxLines(1);
        titles.addView(detail);
        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        titleParams.setMarginStart(dp(14));
        topBar.addView(titles, titleParams);
        playerScreen.addView(topBar, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.TOP));

        controls = new LinearLayout(this);
        controls.setOrientation(LinearLayout.VERTICAL);
        controls.setBackground(new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP, new int[] {COLOR_SCRIM, 0x000D1321}));
        LinearLayout timeRow = new LinearLayout(this);
        timeRow.setOrientation(LinearLayout.HORIZONTAL);
        timeRow.setGravity(Gravity.CENTER_VERTICAL);
        timeText = text("0:00 / 0:00", 14, COLOR_ON, pixelFont);
        timeText.setPadding(dp(4), 0, 0, 0);
        timeRow.addView(timeText);
        controls.addView(timeRow);
        timeline = new Slider(this);
        timeline.setValueFrom(0f);
        timeline.setValueTo(1f);
        timeline.setValue(0f);
        timeline.setLabelBehavior(com.google.android.material.slider.LabelFormatter.LABEL_GONE);
        timeline.setThumbTintList(ColorStateList.valueOf(COLOR_PRIMARY));
        timeline.setTrackActiveTintList(ColorStateList.valueOf(COLOR_PRIMARY));
        timeline.setTrackInactiveTintList(ColorStateList.valueOf(COLOR_TRACK_OFF));
        timeline.setHaloTintList(ColorStateList.valueOf(0x33FFD23F));
        timeline.setContentDescription("Replay position");
        timeline.addOnChangeListener((slider, value, fromUser) -> {
            if (!fromUser || !filmLoaded) return;
            requestSeek(value);
            showControls();
        });
        timeline.addOnSliderTouchListener(new Slider.OnSliderTouchListener() {
            @Override
            public void onStartTrackingTouch(Slider slider) {
                haptic(slider, HapticFeedbackConstants.CLOCK_TICK);
                draggingTimeline = true;
                resumeAfterDrag = playing;
                if (playing) pause();
                showControls();
            }

            @Override
            public void onStopTrackingTouch(Slider slider) {
                draggingTimeline = false;
                requestSeek(slider.getValue());
                runSeek(true);
                updateTime();
                if (resumeAfterDrag && player != null && !player.filmEnded()) play();
                resumeAfterDrag = false;
                showControls();
            }
        });
        LinearLayout.LayoutParams sliderParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        sliderParams.setMarginStart(dp(-8));
        sliderParams.setMarginEnd(dp(-8));
        controls.addView(timeline, sliderParams);
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        playButton = roundButton(Icon.PAUSE, COLOR_PRIMARY, COLOR_ON_PRIMARY, 52, "Pause");
        playButton.setOnClickListener(v -> {
            haptic(v, HapticFeedbackConstants.VIRTUAL_KEY);
            togglePlay(false);
        });
        pressable(playButton);
        row.addView(playButton, new LinearLayout.LayoutParams(dp(52), dp(52)));
        speedButton = text(speedLabel(), 14, COLOR_ON, pixelFont);
        speedButton.setGravity(Gravity.CENTER);
        speedButton.setMinWidth(dp(64));
        speedButton.setPadding(dp(12), 0, dp(12), 0);
        speedButton.setBackground(new RippleDrawable(ColorStateList.valueOf(COLOR_RIPPLE), rounded(COLOR_HIGH, dp(20)), null));
        speedButton.setClickable(true);
        speedButton.setFocusable(true);
        speedButton.setContentDescription("Playback speed " + speedLabel());
        speedButton.setOnClickListener(v -> {
            haptic(v, HapticFeedbackConstants.CLOCK_TICK);
            changeSpeed(1, true);
        });
        LinearLayout.LayoutParams speedParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, dp(40));
        speedParams.setMarginStart(dp(12));
        row.addView(speedButton, speedParams);
        soundButton = roundButton(soundOn ? Icon.SOUND_ON : Icon.SOUND_OFF, COLOR_HIGH, COLOR_ON, 44, soundOn ? "Mute" : "Unmute");
        soundButton.setOnClickListener(v -> {
            haptic(v, HapticFeedbackConstants.CLOCK_TICK);
            toggleSound();
        });
        pressable(soundButton);
        LinearLayout.LayoutParams soundParams = new LinearLayout.LayoutParams(dp(44), dp(44));
        soundParams.setMarginStart(dp(8));
        row.addView(soundButton, soundParams);
        row.addView(new View(this), new LinearLayout.LayoutParams(0, 1, 1f));
        saveButton = new MaterialButton(this);
        saveButton.setText("Save video");
        saveButton.setAllCaps(false);
        saveButton.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        saveButton.setTextColor(COLOR_ON_PRIMARY);
        saveButton.setBackgroundTintList(ColorStateList.valueOf(COLOR_PRIMARY));
        saveButton.setRippleColor(ColorStateList.valueOf(0x33000000));
        saveButton.setCornerRadius(dp(24));
        saveButton.setInsetTop(0);
        saveButton.setInsetBottom(0);
        if (pixelFont != null) saveButton.setTypeface(pixelFont);
        saveButton.setOnClickListener(v -> {
            haptic(v, HapticFeedbackConstants.VIRTUAL_KEY);
            askExport();
        });
        row.addView(saveButton, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, dp(48)));
        LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        rowParams.topMargin = dp(2);
        controls.addView(row, rowParams);
        playerScreen.addView(controls, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.BOTTOM));
        controlsVisible = true;
        controlsShownAt = SystemClock.uptimeMillis();
        topBar.setTranslationY(-dp(16));
        controls.setTranslationY(dp(20));
        topBar.animate().translationY(0f).setStartDelay(80).setDuration(360).setInterpolator(EMPHASIZED_DECELERATE).start();
        controls.animate().translationY(0f).setStartDelay(80).setDuration(360).setInterpolator(EMPHASIZED_DECELERATE).start();
    }

    private String speedLabel() {
        float speed = SPEEDS[speedIndex];
        return (speed == (int) speed ? String.valueOf((int) speed) : String.valueOf(speed)) + "x";
    }

    private void changeSpeed(int direction, boolean wrap) {
        int next = speedIndex + direction;
        if (wrap) next = (next + SPEEDS.length) % SPEEDS.length;
        else next = Math.max(0, Math.min(SPEEDS.length - 1, next));
        if (next == speedIndex) return;
        speedIndex = next;
        speedButton.setText(speedLabel());
        speedButton.setContentDescription("Playback speed " + speedLabel());
        pop(speedButton);
        refreshFeel();
        if (!controlsVisible) showBubble(speedLabel(), 0);
        showControls();
    }

    private void toggleSound() {
        soundOn = !soundOn;
        setIcon(soundButton, soundOn ? Icon.SOUND_ON : Icon.SOUND_OFF, COLOR_ON, 44, soundOn ? "Mute" : "Unmute");
        pop(soundButton);
        if (player != null) player.filmSetAudible(soundOn && playing);
        if (!controlsVisible) showFlash(soundOn ? Icon.SOUND_ON : Icon.SOUND_OFF);
        showControls();
    }

    private void togglePlay(boolean flashIt) {
        if (!filmLoaded) return;
        if (playing) pause();
        else play();
        pop(playButton);
        if (flashIt) showFlash(playing ? Icon.PLAY : Icon.PAUSE);
        showControls();
    }

    private void showFlash(int icon) {
        if (flash == null) return;
        flash.setImageDrawable(new Icon(icon, dp(36), dp(2.5f), COLOR_ON));
        flash.animate().cancel();
        flash.setAlpha(0f);
        flash.setScaleX(0.7f);
        flash.setScaleY(0.7f);
        flash.animate().alpha(1f).scaleX(1f).scaleY(1f).setDuration(140).setInterpolator(EMPHASIZED_DECELERATE).withEndAction(() -> {
            if (flash != null) flash.animate().alpha(0f).scaleX(1.18f).scaleY(1.18f).setStartDelay(220).setDuration(260).setInterpolator(STANDARD).start();
        }).start();
    }

    private void showBubble(String message, int side) {
        if (bubble == null || playerScreen == null) return;
        bubble.setText(message);
        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) bubble.getLayoutParams();
        params.gravity = Gravity.CENTER_VERTICAL | (side < 0 ? Gravity.START : side > 0 ? Gravity.END : Gravity.CENTER_HORIZONTAL);
        params.setMarginStart(dp(32));
        params.setMarginEnd(dp(32));
        bubble.setLayoutParams(params);
        bubble.animate().cancel();
        bubble.setAlpha(0f);
        bubble.setTranslationX(side * dp(12));
        bubble.animate().alpha(1f).translationX(0f).setDuration(150).setInterpolator(EMPHASIZED_DECELERATE).withEndAction(() -> {
            if (bubble != null) bubble.animate().alpha(0f).setStartDelay(450).setDuration(220).setInterpolator(STANDARD).start();
        }).start();
    }

    private void skip(double seconds) {
        if (!filmLoaded || player == null) return;
        double target = Math.max(0.0, Math.min(filmLength, player.filmTime() + seconds));
        pendingSeek = target;
        runSeek(true);
        if (playing) {
            playClock = player.filmTime();
            lastStep = 0f;
        }
        showBubble((seconds < 0 ? "−" : "+") + (int) Math.abs(seconds) + " s", seconds < 0 ? -1 : 1);
    }

    private void stepFrame(int direction) {
        if (!filmLoaded || player == null) return;
        if (playing) pause();
        if (direction > 0) player.filmStep();
        else player.filmSeek(Math.max(0.0, player.filmTime() - 1.0 / 60.0));
        player.invalidate();
        updateTime();
        showControls();
    }

    private void jumpTo(double fraction) {
        if (!filmLoaded || player == null) return;
        pendingSeek = Math.max(0.0, Math.min(filmLength, filmLength * fraction));
        runSeek(true);
        if (playing) {
            playClock = player.filmTime();
            lastStep = 0f;
        }
        showControls();
    }

    private void showControls() {
        controlsShownAt = SystemClock.uptimeMillis();
        if (controlsVisible || controls == null) return;
        controlsVisible = true;
        controls.animate().cancel();
        topBar.animate().cancel();
        controls.setVisibility(View.VISIBLE);
        topBar.setVisibility(View.VISIBLE);
        controls.animate().alpha(1f).translationY(0f).setStartDelay(0).setDuration(240).setInterpolator(EMPHASIZED_DECELERATE).start();
        topBar.animate().alpha(1f).translationY(0f).setStartDelay(0).setDuration(240).setInterpolator(EMPHASIZED_DECELERATE).start();
    }

    private void hideControls() {
        if (!controlsVisible || controls == null || draggingTimeline) return;
        View focused = getCurrentFocus();
        if (focused != null && (isInside(focused, controls) || isInside(focused, topBar))) return;
        controlsVisible = false;
        controls.animate().alpha(0f).translationY(dp(16)).setStartDelay(0).setDuration(200).setInterpolator(EMPHASIZED_ACCELERATE).withEndAction(() -> {
            if (!controlsVisible && controls != null) controls.setVisibility(View.INVISIBLE);
        }).start();
        topBar.animate().alpha(0f).translationY(-dp(12)).setStartDelay(0).setDuration(200).setInterpolator(EMPHASIZED_ACCELERATE).withEndAction(() -> {
            if (!controlsVisible && topBar != null) topBar.setVisibility(View.INVISIBLE);
        }).start();
    }

    private static boolean isInside(View view, View parent) {
        for (Object at = view; at instanceof View; at = ((View) at).getParent()) {
            if (at == parent) return true;
        }
        return false;
    }

    private void refreshFeel() {
        if (player != null) player.filmSetFeel(playing && speedIndex == NORMAL_SPEED && hapticsOn());
    }

    private void play() {
        if (!filmLoaded || player == null) return;
        if (player.filmEnded() || player.filmTime() >= filmLength - 0.001) player.filmSeek(0.0);
        playClock = player.filmTime();
        lastStep = 0f;
        playing = true;
        setIcon(playButton, Icon.PAUSE, COLOR_ON_PRIMARY, 52, "Pause");
        player.filmSetAudible(soundOn);
        refreshFeel();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        lastFrameNanos = 0L;
        loop(true);
    }

    private void pause() {
        playing = false;
        if (playButton != null) setIcon(playButton, Icon.PLAY, COLOR_ON_PRIMARY, 52, "Play");
        if (player != null) {
            player.filmSetAudible(false);
            player.filmSetFeel(false);
        }
        if (exporter == null) getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        loop(false);
    }

    private void loop(boolean on) {
        if (on == looping) return;
        looping = on;
        if (on) choreographer.postFrameCallback(this);
        else choreographer.removeFrameCallback(this);
    }

    private void requestSeek(double target) {
        pendingSeek = Math.max(0.0, Math.min(filmLength, target));
        if (seekPosted || player == null) return;
        seekPosted = true;
        player.post(() -> {
            seekPosted = false;
            runSeek(false);
        });
    }

    private void runSeek(boolean force) {
        if (player == null || pendingSeek < 0.0) return;
        double target = pendingSeek;
        double now = player.filmTime();
        if (!force && target < now) {
            double perSecond = film.events.length / Math.max(0.1, filmLength);
            if (target * perSecond > LIVE_SEEK_STEPS) {
                updateTime(target);
                return;
            }
        }
        pendingSeek = -1.0;
        player.filmSeek(target);
        player.invalidate();
        updateTime();
    }

    @Override
    public void doFrame(long frameTimeNanos) {
        if (!looping) return;
        choreographer.postFrameCallback(this);
        if (player == null || !filmLoaded) return;
        double elapsed = lastFrameNanos == 0L ? 0.0 : Math.min(0.1, (frameTimeNanos - lastFrameNanos) / 1e9);
        lastFrameNanos = frameTimeNanos;
        if (playing) {
            playClock += elapsed * SPEEDS[speedIndex];
            boolean ended = false;
            while (player.filmTime() + lastStep * 0.5 < playClock) {
                float step = player.filmStep();
                if (step <= 0f) {
                    ended = true;
                    break;
                }
                lastStep = step;
            }
            if (playClock - player.filmTime() > 0.25) playClock = player.filmTime();
            player.invalidate();
            if (ended) {
                if (!lengthKnown) setLength(player.filmTime());
                pause();
                showControls();
            }
        }
        updateTime();
        if (playing && controlsVisible && SystemClock.uptimeMillis() - controlsShownAt > CONTROLS_HIDE_MS) hideControls();
    }

    private void updateTime() {
        if (player == null) return;
        updateTime(player.filmTime());
    }

    private void updateTime(double at) {
        if (timeText == null) return;
        double now = Math.max(0.0, Math.min(filmLength, at));
        long second = (long) Math.floor(now), total = (long) Math.floor(filmLength);
        if (second != shownSecond || total != shownLength) {
            shownSecond = second;
            shownLength = total;
            timeText.setText(clockText(now) + " / " + clockText(filmLength));
        }
        if (!draggingTimeline && filmLoaded) {
            float value = (float) Math.min(timeline.getValueTo(), now);
            if (Math.abs(timeline.getValue() - value) > 0.0005f) timeline.setValue(value);
        }
    }

    private Bitmap captureFrame() {
        if (player == null || film == null || !filmLoaded) return null;
        try {
            int width = GameView.SNAPSHOT_WIDTH, height = Math.max(1, Math.round(width * film.height / (float) film.width));
            Bitmap frame = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            frame.setDensity(Bitmap.DENSITY_NONE);
            Canvas canvas = new Canvas(frame);
            canvas.setDensity(Bitmap.DENSITY_NONE);
            canvas.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.FILTER_BITMAP_FLAG));
            canvas.drawColor(Color.BLACK);
            canvas.scale(width / (float) film.width, height / (float) film.height);
            player.filmDraw(canvas);
            return frame;
        } catch (RuntimeException | OutOfMemoryError e) {
            return null;
        }
    }

    private void closePlayer() {
        if (playerScreen == null) return;
        pause();
        if (exporter != null) exporter.cancel();
        if (player != null) player.filmSetAudible(false);
        playerToken++;
        dropPending();
        pendingMeasure = null;
        playOnReveal = false;
        if (barAnimator != null) barAnimator.cancel();
        barAnimator = null;
        final View closing = playerScreen;
        final GameView.Film shown = film;
        final File shownFile = filmFile;
        Bitmap still = captureFrame();
        if (still == null && shownFile != null) still = thumbs().get(shownFile);
        final ThumbView target = thumbFor(shownFile);
        MorphView shrink = null;
        if (still != null && target != null && listScreen != null && shown != null && root.getWidth() > 0) {
            RectF from = morph != null ? new RectF(morph.current()) : stageRect(shown);
            if (from.width() <= 0f) from = stageRect(shown);
            if (morph != null && morph.getParent() instanceof ViewGroup) ((ViewGroup) morph.getParent()).removeView(morph);
            listScreen.setVisibility(View.VISIBLE);
            listScreen.setScaleX(1f);
            listScreen.setScaleY(1f);
            RectF to = boundsIn(target, root);
            shrink = new MorphView(this, still, from, 0f, to, target.radius());
        }
        player = null;
        film = null;
        filmLoaded = false;
        stage = null;
        controls = null;
        topBar = null;
        timeline = null;
        timeText = null;
        flash = null;
        bubble = null;
        morph = null;
        playerBackdrop = null;
        playerScreen = null;
        shownSecond = -1L;
        closing.setEnabled(false);
        if (listScreen == null) {
            if (closing.getParent() instanceof ViewGroup) ((ViewGroup) closing.getParent()).removeView(closing);
            showList();
            return;
        }
        listScreen.setVisibility(View.VISIBLE);
        listScreen.animate().cancel();
        if (shrink != null) {
            final MorphView flying = shrink;
            root.addView(flying, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            target.setVisibility(View.INVISIBLE);
            closing.animate().cancel();
            closing.animate().alpha(0f).setDuration(200).setInterpolator(STANDARD).withEndAction(() -> {
                if (closing.getParent() instanceof ViewGroup) ((ViewGroup) closing.getParent()).removeView(closing);
            }).start();
            listScreen.setAlpha(0f);
            listScreen.animate().alpha(1f).setStartDelay(60).setDuration(300).setInterpolator(STANDARD).withLayer().start();
            ValueAnimator back = ValueAnimator.ofFloat(0f, 1f);
            back.setDuration(380);
            back.setInterpolator(EMPHASIZED_DECELERATE);
            back.addUpdateListener(animation -> flying.setProgress((Float) animation.getAnimatedValue()));
            back.addListener(new android.animation.AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(android.animation.Animator animation) {
                    target.setVisibility(View.VISIBLE);
                    if (flying.getParent() instanceof ViewGroup) ((ViewGroup) flying.getParent()).removeView(flying);
                }
            });
            back.start();
        } else {
            closing.animate().cancel();
            closing.animate().alpha(0f).scaleX(0.9f).scaleY(0.9f).setDuration(220).setInterpolator(EMPHASIZED_ACCELERATE).withEndAction(() -> {
                if (closing.getParent() instanceof ViewGroup) ((ViewGroup) closing.getParent()).removeView(closing);
            }).start();
            listScreen.animate().alpha(1f).scaleX(1f).scaleY(1f).setDuration(320).setStartDelay(40).setInterpolator(EMPHASIZED_DECELERATE).withLayer().start();
        }
        applyInsets();
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN && handleKey(event)) return true;
        if (event.getAction() == KeyEvent.ACTION_UP && consumedKey == event.getKeyCode()) {
            consumedKey = -1;
            return true;
        }
        return super.dispatchKeyEvent(event);
    }

    private boolean handleKey(KeyEvent event) {
        int code = event.getKeyCode();
        boolean repeat = event.getRepeatCount() > 0;
        boolean handled = playerScreen != null ? playerKey(code, event, repeat) : listKey(code, event, repeat);
        if (handled) consumedKey = code;
        return handled;
    }

    private boolean playerKey(int code, KeyEvent event, boolean repeat) {
        switch (code) {
            case KeyEvent.KEYCODE_SPACE:
            case KeyEvent.KEYCODE_K:
            case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
            case KeyEvent.KEYCODE_BUTTON_A:
                if (!repeat) togglePlay(true);
                return true;
            case KeyEvent.KEYCODE_MEDIA_PLAY:
                if (!playing && !repeat) togglePlay(true);
                return true;
            case KeyEvent.KEYCODE_MEDIA_PAUSE:
                if (playing && !repeat) togglePlay(true);
                return true;
            case KeyEvent.KEYCODE_DPAD_LEFT:
            case KeyEvent.KEYCODE_MEDIA_REWIND:
                if (event.isShiftPressed()) stepFrame(-1);
                else skip(-5.0);
                return true;
            case KeyEvent.KEYCODE_DPAD_RIGHT:
            case KeyEvent.KEYCODE_MEDIA_FAST_FORWARD:
                if (event.isShiftPressed()) stepFrame(1);
                else skip(5.0);
                return true;
            case KeyEvent.KEYCODE_J:
                skip(-10.0);
                return true;
            case KeyEvent.KEYCODE_L:
                skip(10.0);
                return true;
            case KeyEvent.KEYCODE_COMMA:
                if (event.isShiftPressed()) changeSpeed(-1, false);
                else stepFrame(-1);
                return true;
            case KeyEvent.KEYCODE_PERIOD:
                if (event.isShiftPressed()) changeSpeed(1, false);
                else stepFrame(1);
                return true;
            case KeyEvent.KEYCODE_LEFT_BRACKET:
            case KeyEvent.KEYCODE_MINUS:
                changeSpeed(-1, false);
                return true;
            case KeyEvent.KEYCODE_RIGHT_BRACKET:
            case KeyEvent.KEYCODE_EQUALS:
                changeSpeed(1, false);
                return true;
            case KeyEvent.KEYCODE_M:
            case KeyEvent.KEYCODE_VOLUME_MUTE:
                if (!repeat) {
                    haptic(soundButton, HapticFeedbackConstants.CLOCK_TICK);
                    toggleSound();
                }
                return true;
            case KeyEvent.KEYCODE_MOVE_HOME:
            case KeyEvent.KEYCODE_0:
            case KeyEvent.KEYCODE_NUMPAD_0:
                jumpTo(0.0);
                return true;
            case KeyEvent.KEYCODE_MOVE_END:
                jumpTo(1.0);
                return true;
            case KeyEvent.KEYCODE_S:
                if (!repeat && (event.isCtrlPressed() || !event.isShiftPressed())) askExport();
                return true;
            case KeyEvent.KEYCODE_ESCAPE:
            case KeyEvent.KEYCODE_BUTTON_B:
                if (!repeat) closePlayer();
                return true;
            case KeyEvent.KEYCODE_TAB:
            case KeyEvent.KEYCODE_DPAD_UP:
            case KeyEvent.KEYCODE_DPAD_DOWN:
                showControls();
                return false;
            default:
                if (code >= KeyEvent.KEYCODE_1 && code <= KeyEvent.KEYCODE_9) {
                    jumpTo((code - KeyEvent.KEYCODE_0) / 10.0);
                    return true;
                }
                if (code >= KeyEvent.KEYCODE_NUMPAD_0 + 1 && code <= KeyEvent.KEYCODE_NUMPAD_0 + 9) {
                    jumpTo((code - KeyEvent.KEYCODE_NUMPAD_0) / 10.0);
                    return true;
                }
                return false;
        }
    }

    private boolean listKey(int code, KeyEvent event, boolean repeat) {
        if (code == KeyEvent.KEYCODE_ESCAPE || code == KeyEvent.KEYCODE_BUTTON_B) {
            if (!repeat) finish();
            return true;
        }
        View focused = getCurrentFocus();
        if ((code == KeyEvent.KEYCODE_DEL || code == KeyEvent.KEYCODE_FORWARD_DEL) && focused != null && focused.getTag() instanceof File) {
            if (!repeat) focused.performLongClick();
            return true;
        }
        if ((code == KeyEvent.KEYCODE_DPAD_DOWN || code == KeyEvent.KEYCODE_DPAD_UP || code == KeyEvent.KEYCODE_TAB) && (focused == null || !isInside(focused, listContent))) {
            View first = firstRow();
            if (first != null) {
                first.requestFocus();
                return true;
            }
        }
        if ((code == KeyEvent.KEYCODE_ENTER || code == KeyEvent.KEYCODE_NUMPAD_ENTER || code == KeyEvent.KEYCODE_BUTTON_A)
                && focused != null && focused.getTag() instanceof File) {
            if (!repeat) focused.performClick();
            return true;
        }
        return false;
    }

    private View firstRow() {
        if (listContent == null) return null;
        for (int i = 0; i < listContent.getChildCount(); i++) {
            View child = listContent.getChildAt(i);
            if (child.getTag() instanceof File) return child;
        }
        return null;
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (playing) pause();
        showControls();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        loop(false);
        playerToken++;
        if (exporter != null) exporter.cancel();
        if (thumbs != null) thumbs.shutdown();
        if (loader != null) loader.shutdownNow();
        dropPending();
    }

    static final class Icon extends Drawable {
        static final int BACK = 0, PLAY = 1, PAUSE = 2, SOUND_ON = 3, SOUND_OFF = 4;
        private final Paint stroke = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint fill = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Path path = new Path();
        private final int kind;
        private final int size;

        Icon(int kind, int size, float strokeWidth, int color) {
            this.kind = kind;
            this.size = size;
            stroke.setStyle(Paint.Style.STROKE);
            stroke.setStrokeWidth(strokeWidth);
            stroke.setStrokeCap(Paint.Cap.ROUND);
            stroke.setStrokeJoin(Paint.Join.ROUND);
            stroke.setColor(color);
            fill.setStyle(Paint.Style.FILL);
            fill.setColor(color);
        }

        @Override
        public void draw(Canvas canvas) {
            Rect bounds = getBounds();
            float unit = size / 24f;
            canvas.save();
            canvas.translate(bounds.left + (bounds.width() - size) / 2f, bounds.top + (bounds.height() - size) / 2f);
            canvas.scale(unit, unit);
            path.reset();
            if (kind == PLAY) {
                path.moveTo(8.5f, 5.8f);
                path.lineTo(18.6f, 12f);
                path.lineTo(8.5f, 18.2f);
                path.close();
                canvas.drawPath(path, fill);
                stroke.setStrokeWidth(1.6f);
                canvas.drawPath(path, stroke);
            } else if (kind == PAUSE) {
                canvas.drawRoundRect(new RectF(6.2f, 5f, 9.8f, 19f), 1.3f, 1.3f, fill);
                canvas.drawRoundRect(new RectF(14.2f, 5f, 17.8f, 19f), 1.3f, 1.3f, fill);
            } else if (kind == SOUND_ON || kind == SOUND_OFF) {
                canvas.translate(kind == SOUND_ON ? 0.8f : 0f, 0f);
                path.moveTo(3.6f, 9.6f);
                path.lineTo(7f, 9.6f);
                path.lineTo(11.4f, 5.4f);
                path.lineTo(11.4f, 18.6f);
                path.lineTo(7f, 14.4f);
                path.lineTo(3.6f, 14.4f);
                path.close();
                canvas.drawPath(path, fill);
                stroke.setStrokeWidth(1.7f);
                canvas.drawPath(path, stroke);
                stroke.setStrokeWidth(2f);
                if (kind == SOUND_ON) {
                    canvas.drawArc(new RectF(7.2f, 8f, 15.2f, 16f), -44f, 88f, false, stroke);
                    canvas.drawArc(new RectF(6.4f, 4f, 22.4f, 20f), -46f, 92f, false, stroke);
                } else {
                    path.reset();
                    path.moveTo(15.4f, 9.4f);
                    path.lineTo(20.6f, 14.6f);
                    path.moveTo(20.6f, 9.4f);
                    path.lineTo(15.4f, 14.6f);
                    canvas.drawPath(path, stroke);
                }
            } else {
                stroke.setStrokeWidth(2.2f);
                path.moveTo(19f, 12f);
                path.lineTo(5.6f, 12f);
                path.moveTo(11.4f, 6.2f);
                path.lineTo(5.6f, 12f);
                path.lineTo(11.4f, 17.8f);
                canvas.drawPath(path, stroke);
            }
            canvas.restore();
        }

        @Override
        public void setAlpha(int alpha) {
            stroke.setAlpha(alpha);
            fill.setAlpha(alpha);
        }

        @Override
        public void setColorFilter(ColorFilter filter) {
            stroke.setColorFilter(filter);
            fill.setColorFilter(filter);
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

    static final class VideoPlan {
        int width, height, fps, bitrate;
        boolean sound;
        boolean credit;
        String codec;
    }

    static int even(float value) {
        return Math.max(2, ((int) Math.floor(value)) & ~1);
    }

    static int[] targetSize(int width, int height, int choice) {
        int shortSide = Math.min(width, height);
        float limit = choice == SettingsActivity.REPLAY_RESOLUTION_1080 ? 1080f : choice == SettingsActivity.REPLAY_RESOLUTION_720 ? 720f : shortSide;
        float scale = Math.min(1f, limit / shortSide);
        return new int[] {even(width * scale), even(height * scale)};
    }

    static int bitrate(int width, int height, int fps, int quality) {
        float bits = QUALITY_BITS[Math.max(0, Math.min(QUALITY_BITS.length - 1, quality))];
        long rate = (long) (width * (long) height * fps * bits);
        return (int) Math.max(2_000_000L, Math.min(150_000_000L, rate));
    }

    VideoPlan plan(GameView.Film loaded) {
        Map<String, ?> all = prefs.getAll();
        int resolution = intOf(all.get(SettingsActivity.PREF_REPLAY_RESOLUTION), SettingsActivity.DEFAULT_REPLAY_RESOLUTION);
        int fps = SettingsActivity.replayFps(intOf(all.get(SettingsActivity.PREF_REPLAY_FPS), SettingsActivity.DEFAULT_REPLAY_FPS));
        int quality = intOf(all.get(SettingsActivity.PREF_REPLAY_QUALITY), SettingsActivity.DEFAULT_REPLAY_QUALITY);
        Object sound = all.get(SettingsActivity.PREF_REPLAY_SOUND);
        VideoPlan plan = new VideoPlan();
        int[] size = targetSize(loaded.width, loaded.height, resolution);
        plan.width = size[0];
        plan.height = size[1];
        plan.fps = fps;
        plan.sound = sound instanceof Boolean ? (Boolean) sound : SettingsActivity.DEFAULT_REPLAY_SOUND;
        Object credit = all.get(SettingsActivity.PREF_REPLAY_CREDIT);
        plan.credit = credit instanceof Boolean ? (Boolean) credit : SettingsActivity.DEFAULT_REPLAY_CREDIT;
        fitEncoder(plan);
        plan.bitrate = bitrate(plan.width, plan.height, plan.fps, quality);
        clampBitrate(plan);
        return plan;
    }

    static int intOf(Object value, int fallback) {
        return value instanceof Integer ? (Integer) value : fallback;
    }

    static MediaCodecInfo encoderInfo(String mime) {
        MediaCodecList list = new MediaCodecList(MediaCodecList.REGULAR_CODECS);
        MediaCodecInfo fallback = null;
        for (MediaCodecInfo info : list.getCodecInfos()) {
            if (!info.isEncoder()) continue;
            for (String type : info.getSupportedTypes()) {
                if (!type.equalsIgnoreCase(mime)) continue;
                String name = info.getName().toLowerCase(Locale.US);
                boolean software = name.startsWith("omx.google.") || name.startsWith("c2.android.");
                if (!software) return info;
                if (fallback == null) fallback = info;
            }
        }
        return fallback;
    }

    static void fitEncoder(VideoPlan plan) {
        MediaCodecInfo info = encoderInfo(MediaFormat.MIMETYPE_VIDEO_AVC);
        if (info == null) return;
        plan.codec = info.getName();
        MediaCodecInfo.VideoCapabilities caps;
        try {
            caps = info.getCapabilitiesForType(MediaFormat.MIMETYPE_VIDEO_AVC).getVideoCapabilities();
        } catch (RuntimeException e) {
            return;
        }
        if (caps == null) return;
        int align = Math.max(2, Math.max(caps.getWidthAlignment(), caps.getHeightAlignment()));
        int[] best = largestFit(caps, plan.width, plan.height, plan.fps, align);
        if (plan.fps > 30 && (best == null || best[0] < plan.width * 0.75f)) {
            int[] slower = largestFit(caps, plan.width, plan.height, 30, align);
            if (slower != null && (best == null || slower[0] > best[0] * 1.2f)) {
                best = slower;
                plan.fps = 30;
            }
        }
        if (best != null) {
            plan.width = best[0];
            plan.height = best[1];
        }
    }

    static int[] largestFit(MediaCodecInfo.VideoCapabilities caps, int width, int height, int fps, int align) {
        for (int step = 0; step <= 90; step++) {
            float scale = 1f - step * 0.01f;
            int w = Math.max(align, (int) (width * scale / align) * align);
            int h = Math.max(align, (int) (height * scale / align) * align);
            try {
                if (caps.isSizeSupported(w, h) && caps.areSizeAndRateSupported(w, h, fps)) return new int[] {w, h};
            } catch (RuntimeException ignored) {
            }
        }
        return null;
    }

    static void clampBitrate(VideoPlan plan) {
        MediaCodecInfo info = encoderInfo(MediaFormat.MIMETYPE_VIDEO_AVC);
        if (info == null) return;
        try {
            android.util.Range<Integer> range = info.getCapabilitiesForType(MediaFormat.MIMETYPE_VIDEO_AVC).getVideoCapabilities().getBitrateRange();
            plan.bitrate = Math.max(range.getLower(), Math.min(range.getUpper(), plan.bitrate));
        } catch (RuntimeException ignored) {
        }
    }

    private void askExport() {
        if (!filmLoaded || exporter != null || film == null) return;
        pause();
        final VideoPlan plan = plan(film);
        String summary = plan.width + " × " + plan.height + "  ·  " + plan.fps + " fps  ·  "
                + String.format(Locale.US, "%.0f", plan.bitrate / 1_000_000.0) + " Mbps  ·  " + (plan.sound ? "with sound" : "no sound")
                + "\n\nAbout " + clockText(filmLength) + " long. Every frame is drawn from the replay, so it's smoother and sharper than a screen recording."
                + "\n\nChange the size, frame rate, quality and credit in Settings > System > Replays.";
        LinearLayout body = new LinearLayout(this);
        body.setOrientation(LinearLayout.VERTICAL);
        body.setPadding(dp(24), dp(4), dp(24), 0);
        body.addView(text(summary, 14, COLOR_ON_VARIANT, null), new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        LinearLayout creditRow = new LinearLayout(this);
        creditRow.setOrientation(LinearLayout.HORIZONTAL);
        creditRow.setGravity(Gravity.CENTER_VERTICAL);
        creditRow.setMinimumHeight(dp(48));
        LinearLayout creditTexts = new LinearLayout(this);
        creditTexts.setOrientation(LinearLayout.VERTICAL);
        creditTexts.addView(text("Credit in the corner", 15, COLOR_ON, mediumFont));
        String[] credit = SettingsActivity.videoCredit(this);
        creditTexts.addView(text(credit[0] + " " + credit[1], 12, COLOR_ON_VARIANT, null));
        creditRow.addView(creditTexts, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        final MaterialSwitch creditSwitch = new MaterialSwitch(this);
        creditSwitch.setSaveEnabled(false);
        int[][] states = {{android.R.attr.state_checked}, {}};
        creditSwitch.setThumbTintList(new ColorStateList(states, new int[] {COLOR_ON_PRIMARY, 0xFF8791A8}));
        creditSwitch.setTrackTintList(new ColorStateList(states, new int[] {COLOR_PRIMARY, COLOR_TRACK_OFF}));
        creditSwitch.setTrackDecorationTintList(new ColorStateList(states, new int[] {Color.TRANSPARENT, 0xFF8791A8}));
        creditSwitch.setChecked(plan.credit);
        LinearLayout.LayoutParams switchParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        switchParams.leftMargin = dp(12);
        creditRow.addView(creditSwitch, switchParams);
        creditRow.setOnClickListener(v -> creditSwitch.setChecked(!creditSwitch.isChecked()));
        creditSwitch.setOnCheckedChangeListener((button, checked) -> {
            plan.credit = checked;
            prefs.edit().putBoolean(SettingsActivity.PREF_REPLAY_CREDIT, checked).apply();
        });
        LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        rowParams.topMargin = dp(10);
        body.addView(creditRow, rowParams);
        ScrollView scroller = new ScrollView(this);
        scroller.addView(body);
        showDialog(new MaterialAlertDialogBuilder(this)
                .setTitle("Save as a video?")
                .setView(scroller)
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Save video", (dialog, which) -> startExport(plan)));
    }

    private void startExport(VideoPlan plan) {
        if (film == null || exporter != null) return;
        if (Build.VERSION.SDK_INT < 29 && ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            pendingPlan = plan;
            requestPermissions(new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_STORAGE);
            return;
        }
        LinearLayout body = new LinearLayout(this);
        body.setOrientation(LinearLayout.VERTICAL);
        body.setPadding(dp(24), dp(8), dp(24), 0);
        exportText = text("Getting ready", 15, COLOR_ON, mediumFont);
        body.addView(exportText);
        FrameLayout track = new FrameLayout(this);
        track.setBackground(rounded(COLOR_TRACK_OFF, dp(4)));
        exportBar = new View(this);
        exportBar.setBackground(rounded(COLOR_PRIMARY, dp(4)));
        exportBar.setPivotX(0f);
        exportBar.setScaleX(0.001f);
        track.addView(exportBar, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(8)));
        LinearLayout.LayoutParams trackParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(8));
        trackParams.topMargin = dp(14);
        body.addView(track, trackParams);
        TextView note = text("You can leave this screen on while it works.", 13, COLOR_ON_VARIANT, null);
        LinearLayout.LayoutParams noteParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        noteParams.topMargin = dp(12);
        body.addView(note, noteParams);
        exportDialog = showDialog(new MaterialAlertDialogBuilder(this)
                .setTitle("Saving video")
                .setView(body)
                .setCancelable(false)
                .setNegativeButton("Cancel", (dialog, which) -> {
                    if (exporter != null) exporter.cancel();
                }));
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        File cache = new File(getCacheDir(), "video");
        exporter = new Exporter(this, GameView.filmPlaybackPrefs(film, plan.sound, false), film, plan, cache, new Exporter.Listener() {
            @Override
            public void progress(float share, String stage) {
                runOnUiThread(() -> showProgress(share, stage));
            }

            @Override
            public void done(File video, String error) {
                runOnUiThread(() -> exportFinished(video, error));
            }
        });
        exporter.knownLength = lengthKnown ? filmLength : 0.0;
        exporter.start();
    }

    private VideoPlan pendingPlan;

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] results) {
        super.onRequestPermissionsResult(requestCode, permissions, results);
        if (requestCode != REQUEST_STORAGE || pendingPlan == null) return;
        VideoPlan plan = pendingPlan;
        pendingPlan = null;
        if (results.length > 0 && results[0] == PackageManager.PERMISSION_GRANTED) {
            startExport(plan);
        } else {
            haptic(root, rejectHaptic());
            showDialog(new MaterialAlertDialogBuilder(this)
                    .setTitle("Can't save videos")
                    .setMessage("Saving to your Movies folder needs storage access on this version of Android.")
                    .setPositiveButton("OK", null));
        }
    }

    private int lastPercent = -1;
    private String lastStage;

    private void showProgress(float share, String stage) {
        if (exportText == null || exportBar == null) return;
        float clamped = Math.max(0f, Math.min(1f, share));
        int percent = Math.round(clamped * 100f);
        if (percent == lastPercent && stage.equals(lastStage)) return;
        lastPercent = percent;
        lastStage = stage;
        exportText.setText(stage + "  ·  " + percent + "%");
        exportBar.setPivotX(0f);
        exportBar.animate().cancel();
        exportBar.animate().scaleX(Math.max(0.001f, clamped)).setDuration(180).setInterpolator(STANDARD).start();
    }

    private void exportFinished(File video, String error) {
        exporter = null;
        lastPercent = -1;
        lastStage = null;
        if (isFinishing() || isDestroyed()) {
            if (video != null) video.delete();
            return;
        }
        if (!playing) getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        try {
            if (exportDialog != null) exportDialog.dismiss();
        } catch (RuntimeException ignored) {
        }
        exportDialog = null;
        exportText = null;
        exportBar = null;
        if (video == null) {
            if (error != null) {
                haptic(root, rejectHaptic());
                showDialog(new MaterialAlertDialogBuilder(this)
                        .setTitle("The video wasn't saved")
                        .setMessage(error)
                        .setPositiveButton("OK", null));
            }
            return;
        }
        String name = SettingsActivity.APP_NAME + " " + (film != null ? film.score : 0) + " " + new java.text.SimpleDateFormat("yyyy-MM-dd HH-mm-ss", Locale.US).format(new Date()) + ".mp4";
        Uri saved;
        String where;
        try {
            if (Build.VERSION.SDK_INT >= 29) {
                saved = saveToMediaStore(video, name);
                where = "Movies/" + MOVIES_FOLDER;
            } else {
                File folder = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES), MOVIES_FOLDER);
                if (!folder.isDirectory() && !folder.mkdirs()) throw new IOException("Movies folder unavailable");
                File out = new File(folder, name);
                copy(new FileInputStream(video), new java.io.FileOutputStream(out));
                final String folderName = "Movies/" + MOVIES_FOLDER;
                MediaScannerConnection.scanFile(this, new String[] {out.getPath()}, new String[] {"video/mp4"},
                        (path, uri) -> runOnUiThread(() -> {
                            if (!isFinishing() && !isDestroyed()) announceSaved(folderName, uri);
                        }));
                saved = null;
                where = null;
                haptic(root, confirmHaptic());
                return;
            }
        } catch (IOException | RuntimeException e) {
            saved = null;
            where = null;
        } finally {
            video.delete();
        }
        if (where == null) {
            haptic(root, rejectHaptic());
            showDialog(new MaterialAlertDialogBuilder(this)
                    .setTitle("The video wasn't saved")
                    .setMessage("There wasn't room to save it, or storage isn't available right now.")
                    .setPositiveButton("OK", null));
            return;
        }
        haptic(root, confirmHaptic());
        announceSaved(where, saved);
    }

    private void announceSaved(String where, final Uri share) {
        if (share != null) {
            announce("Video saved to " + where, "Share", v -> {
                Intent send = new Intent(Intent.ACTION_SEND);
                send.setType("video/mp4");
                send.putExtra(Intent.EXTRA_STREAM, share);
                send.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                try {
                    startActivity(Intent.createChooser(send, "Share video"));
                } catch (RuntimeException ignored) {
                }
            });
        } else {
            announce("Video saved to " + where, null, null);
        }
    }

    private Uri saveToMediaStore(File video, String name) throws IOException {
        ContentResolver resolver = getContentResolver();
        ContentValues values = new ContentValues();
        values.put(MediaStore.MediaColumns.DISPLAY_NAME, name);
        values.put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4");
        values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_MOVIES + "/" + MOVIES_FOLDER);
        values.put(MediaStore.MediaColumns.IS_PENDING, 1);
        Uri uri = resolver.insert(MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY), values);
        if (uri == null) throw new IOException("Movies folder unavailable");
        try {
            OutputStream out = resolver.openOutputStream(uri);
            if (out == null) throw new IOException("Movies folder unavailable");
            copy(new FileInputStream(video), out);
            ContentValues ready = new ContentValues();
            ready.put(MediaStore.MediaColumns.IS_PENDING, 0);
            resolver.update(uri, ready, null, null);
            return uri;
        } catch (IOException | RuntimeException e) {
            resolver.delete(uri, null, null);
            throw e;
        }
    }

    static void copy(InputStream in, OutputStream out) throws IOException {
        try (InputStream source = in; OutputStream target = out) {
            byte[] buffer = new byte[1 << 16];
            int read;
            while ((read = source.read(buffer)) > 0) target.write(buffer, 0, read);
        }
    }

    static final class SoundEvent {
        final int raw;
        final double at;
        final float volume;

        SoundEvent(int raw, double at, float volume) {
            this.raw = raw;
            this.at = at;
            this.volume = volume;
        }
    }

    static final class LoopSpan {
        final int raw;
        final double from;
        double to = Double.MAX_VALUE;
        final float volume;

        LoopSpan(int raw, double from, float volume) {
            this.raw = raw;
            this.from = from;
            this.volume = volume;
        }
    }

    static final class SoundLog implements GameView.FilmSink {
        final List<SoundEvent> shots = new ArrayList<>();
        final List<LoopSpan> loops = new ArrayList<>();
        final Map<Integer, LoopSpan> open = new HashMap<>();

        @Override
        public void sound(int rawId, double atSec, float volume) {
            shots.add(new SoundEvent(rawId, atSec, volume));
        }

        @Override
        public void loop(int rawId, boolean playing, double atSec, float volume) {
            LoopSpan current = open.get(rawId);
            if (playing && current == null) {
                LoopSpan span = new LoopSpan(rawId, atSec, volume);
                open.put(rawId, span);
                loops.add(span);
            } else if (!playing && current != null) {
                current.to = atSec;
                open.remove(rawId);
            }
        }
    }

    static final class Pcm {
        final float[] left, right;

        Pcm(float[] left, float[] right) {
            this.left = left;
            this.right = right;
        }

        int length() {
            return left.length;
        }
    }

    static final class Mixer {
        final SoundLog log;
        final Map<Integer, Future<Pcm>> sounds;
        long total = Long.MAX_VALUE;
        private final Map<Integer, Pcm> loaded = new HashMap<>();
        private final List<SoundEvent> playing = new ArrayList<>();
        private int nextShot;
        private float[] left = new float[0], right = new float[0];

        Mixer(SoundLog log, Map<Integer, Future<Pcm>> sounds) {
            this.log = log;
            this.sounds = sounds;
        }

        static Map<Integer, Future<Pcm>> ready(Map<Integer, Pcm> sounds) {
            Map<Integer, Future<Pcm>> out = new HashMap<>();
            for (Map.Entry<Integer, Pcm> entry : sounds.entrySet()) {
                final Pcm value = entry.getValue();
                java.util.concurrent.FutureTask<Pcm> done = new java.util.concurrent.FutureTask<>(() -> value);
                done.run();
                out.put(entry.getKey(), done);
            }
            return out;
        }

        void end(long frames) {
            total = frames;
        }

        private Pcm pcm(int raw) {
            if (loaded.containsKey(raw)) return loaded.get(raw);
            Pcm value = null;
            Future<Pcm> future = sounds.get(raw);
            if (future != null) {
                try {
                    value = future.get();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } catch (java.util.concurrent.ExecutionException | RuntimeException ignored) {
                }
            }
            loaded.put(raw, value);
            return value;
        }

        void mix(long start, int frames, short[] out) {
            if (left.length < frames) {
                left = new float[frames];
                right = new float[frames];
            }
            java.util.Arrays.fill(left, 0, frames, 0f);
            java.util.Arrays.fill(right, 0, frames, 0f);
            long end = start + frames;
            while (nextShot < log.shots.size()) {
                SoundEvent shot = log.shots.get(nextShot);
                if (Math.round(shot.at * AUDIO_RATE) >= end) break;
                playing.add(shot);
                nextShot++;
            }
            for (int s = playing.size() - 1; s >= 0; s--) {
                SoundEvent shot = playing.get(s);
                Pcm pcm = pcm(shot.raw);
                long begin = Math.round(shot.at * AUDIO_RATE);
                if (pcm == null || begin + pcm.length() <= start) {
                    playing.remove(s);
                    continue;
                }
                int from = (int) Math.max(0L, begin - start);
                int to = (int) Math.min(frames, begin + pcm.length() - start);
                for (int i = from; i < to; i++) {
                    int index = (int) (start + i - begin);
                    left[i] += pcm.left[index] * shot.volume;
                    right[i] += pcm.right[index] * shot.volume;
                }
            }
            for (int l = 0; l < log.loops.size(); l++) {
                LoopSpan span = log.loops.get(l);
                long begin = Math.round(span.from * AUDIO_RATE);
                long stop = span.to == Double.MAX_VALUE ? Long.MAX_VALUE : Math.round(span.to * AUDIO_RATE);
                if (begin >= end || stop <= start) continue;
                Pcm pcm = pcm(span.raw);
                if (pcm == null || pcm.length() == 0) continue;
                int from = (int) Math.max(0L, begin - start);
                int to = (int) Math.min(frames, stop - start);
                int index = (int) ((start + from - begin) % pcm.length());
                for (int i = from; i < to; i++) {
                    left[i] += pcm.left[index] * span.volume;
                    right[i] += pcm.right[index] * span.volume;
                    if (++index == pcm.length()) index = 0;
                }
            }
            for (int i = 0; i < frames; i++) {
                out[i * 2] = clip(left[i]);
                out[i * 2 + 1] = clip(right[i]);
            }
        }

        static short clip(float value) {
            float magnitude = Math.abs(value);
            float shaped = magnitude <= 0.9f ? magnitude : 0.9f + 0.1f * (float) Math.tanh((magnitude - 0.9f) / 0.1f);
            return (short) Math.round(Math.copySign(Math.min(1f, shaped), value) * 32767f);
        }
    }

    static Pcm resample(short[] samples, int channels, int rate) {
        float[] values = new float[samples.length];
        for (int i = 0; i < samples.length; i++) values[i] = samples[i] / 32768f;
        return resample(values, channels, rate);
    }

    static Pcm resample(float[] samples, int channels, int rate) {
        int frames = channels <= 0 ? 0 : samples.length / channels;
        if (frames == 0 || rate <= 0) return new Pcm(new float[0], new float[0]);
        float[] inLeft = new float[frames], inRight = new float[frames];
        for (int i = 0; i < frames; i++) {
            inLeft[i] = samples[i * channels];
            inRight[i] = channels > 1 ? samples[i * channels + 1] : inLeft[i];
        }
        if (rate == AUDIO_RATE) return new Pcm(inLeft, inRight);
        int outFrames = (int) Math.ceil(frames * (double) AUDIO_RATE / rate);
        float[] left = new float[outFrames], right = new float[outFrames];
        double step = rate / (double) AUDIO_RATE;
        for (int i = 0; i < outFrames; i++) {
            double source = i * step;
            int a = (int) source;
            float t = (float) (source - a);
            left[i] = cubic(inLeft, a, t);
            right[i] = cubic(inRight, a, t);
        }
        return new Pcm(left, right);
    }

    static float cubic(float[] data, int index, float t) {
        int last = data.length - 1;
        float p0 = data[Math.max(0, Math.min(last, index - 1))];
        float p1 = data[Math.max(0, Math.min(last, index))];
        float p2 = data[Math.max(0, Math.min(last, index + 1))];
        float p3 = data[Math.max(0, Math.min(last, index + 2))];
        return 0.5f * (2f * p1 + (p2 - p0) * t + (2f * p0 - 5f * p1 + 4f * p2 - p3) * t * t + (3f * (p1 - p2) + p3 - p0) * t * t * t);
    }

    private static final Map<Integer, java.lang.ref.SoftReference<Pcm>> PCM_CACHE = new HashMap<>();

    static Pcm cachedDecode(android.content.Context context, int raw) throws IOException {
        synchronized (PCM_CACHE) {
            java.lang.ref.SoftReference<Pcm> kept = PCM_CACHE.get(raw);
            Pcm pcm = kept != null ? kept.get() : null;
            if (pcm != null) return pcm;
        }
        Pcm pcm = decode(context, raw);
        synchronized (PCM_CACHE) {
            PCM_CACHE.put(raw, new java.lang.ref.SoftReference<>(pcm));
        }
        return pcm;
    }

    static Pcm decode(android.content.Context context, int raw) throws IOException {
        MediaExtractor extractor = new MediaExtractor();
        AssetFileDescriptor file = null;
        File copy = null;
        MediaCodec decoder = null;
        try {
            try {
                file = context.getResources().openRawResourceFd(raw);
            } catch (RuntimeException e) {
                file = null;
            }
            if (file != null) {
                extractor.setDataSource(file.getFileDescriptor(), file.getStartOffset(), file.getLength());
            } else {
                copy = new File(context.getCacheDir(), "sound_" + raw);
                copy(context.getResources().openRawResource(raw), new java.io.FileOutputStream(copy));
                extractor.setDataSource(copy.getPath());
            }
            int track = -1;
            MediaFormat format = null;
            for (int i = 0; i < extractor.getTrackCount(); i++) {
                MediaFormat candidate = extractor.getTrackFormat(i);
                String mime = candidate.getString(MediaFormat.KEY_MIME);
                if (mime != null && mime.startsWith("audio/")) {
                    track = i;
                    format = candidate;
                    break;
                }
            }
            if (track < 0) throw new IOException("No audio in sound " + raw);
            extractor.selectTrack(track);
            decoder = MediaCodec.createDecoderByType(format.getString(MediaFormat.KEY_MIME));
            decoder.configure(format, null, null, 0);
            decoder.start();
            int rate = format.getInteger(MediaFormat.KEY_SAMPLE_RATE);
            int channels = format.getInteger(MediaFormat.KEY_CHANNEL_COUNT);
            boolean floats = false;
            java.io.ByteArrayOutputStream pcm = new java.io.ByteArrayOutputStream();
            MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();
            boolean inputDone = false;
            for (int guard = 0; guard < 200000; guard++) {
                if (!inputDone) {
                    int index = decoder.dequeueInputBuffer(10000);
                    if (index >= 0) {
                        ByteBuffer input = decoder.getInputBuffer(index);
                        int size = input == null ? -1 : extractor.readSampleData(input, 0);
                        if (size < 0) {
                            decoder.queueInputBuffer(index, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                            inputDone = true;
                        } else {
                            decoder.queueInputBuffer(index, 0, size, extractor.getSampleTime(), 0);
                            extractor.advance();
                        }
                    }
                }
                int out = decoder.dequeueOutputBuffer(info, 10000);
                if (out == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                    MediaFormat changed = decoder.getOutputFormat();
                    rate = changed.getInteger(MediaFormat.KEY_SAMPLE_RATE);
                    channels = changed.getInteger(MediaFormat.KEY_CHANNEL_COUNT);
                    floats = Build.VERSION.SDK_INT >= 24 && changed.containsKey(MediaFormat.KEY_PCM_ENCODING)
                            && changed.getInteger(MediaFormat.KEY_PCM_ENCODING) == AudioFormat.ENCODING_PCM_FLOAT;
                } else if (out >= 0) {
                    ByteBuffer output = decoder.getOutputBuffer(out);
                    if (output != null && info.size > 0) {
                        byte[] chunk = new byte[info.size];
                        output.position(info.offset);
                        output.get(chunk, 0, info.size);
                        pcm.write(chunk, 0, chunk.length);
                    }
                    decoder.releaseOutputBuffer(out, false);
                    if ((info.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) break;
                }
            }
            byte[] bytes = pcm.toByteArray();
            if (floats) {
                float[] values = new float[bytes.length / 4];
                ByteBuffer.wrap(bytes).order(ByteOrder.nativeOrder()).asFloatBuffer().get(values);
                return resample(values, Math.max(1, channels), Math.max(1, rate));
            }
            short[] samples = new short[bytes.length / 2];
            ByteBuffer.wrap(bytes).order(ByteOrder.nativeOrder()).asShortBuffer().get(samples);
            return resample(samples, Math.max(1, channels), Math.max(1, rate));
        } finally {
            if (decoder != null) {
                try {
                    decoder.stop();
                } catch (RuntimeException ignored) {
                }
                decoder.release();
            }
            extractor.release();
            if (file != null) file.close();
            if (copy != null) copy.delete();
        }
    }

    static int lumaOf(int argb) {
        int r = (argb >> 16) & 255, g = (argb >> 8) & 255, b = argb & 255;
        return ((66 * r + 129 * g + 25 * b + 128) >> 8) + 16;
    }

    static int blueOf(int r, int g, int b) {
        return ((-38 * r - 74 * g + 112 * b + 128) >> 8) + 128;
    }

    static int redOf(int r, int g, int b) {
        return ((112 * r - 94 * g - 18 * b + 128) >> 8) + 128;
    }

    static void fillPlanes(int[] argb, int width, int height, ByteBuffer y, int yRow, int yPixel, ByteBuffer u, int uRow, int uPixel,
            ByteBuffer v, int vRow, int vPixel, int fromRow, int toRow) {
        byte[] line = yPixel == 1 ? new byte[width] : null;
        for (int row = fromRow; row < toRow; row++) {
            int base = row * width;
            if (line != null) {
                for (int col = 0; col < width; col++) line[col] = (byte) lumaOf(argb[base + col]);
                ByteBuffer target = y.duplicate();
                target.position(row * yRow);
                target.put(line, 0, width);
            } else {
                for (int col = 0; col < width; col++) y.put(row * yRow + col * yPixel, (byte) lumaOf(argb[base + col]));
            }
            if ((row & 1) != 0) continue;
            int half = row >> 1;
            int below = Math.min(height - 1, row + 1) * width;
            for (int col = 0; col < width; col += 2) {
                int right = Math.min(width - 1, col + 1);
                int p0 = argb[base + col], p1 = argb[base + right], p2 = argb[below + col], p3 = argb[below + right];
                int r = (((p0 >> 16) & 255) + ((p1 >> 16) & 255) + ((p2 >> 16) & 255) + ((p3 >> 16) & 255) + 2) >> 2;
                int g = (((p0 >> 8) & 255) + ((p1 >> 8) & 255) + ((p2 >> 8) & 255) + ((p3 >> 8) & 255) + 2) >> 2;
                int b = ((p0 & 255) + (p1 & 255) + (p2 & 255) + (p3 & 255) + 2) >> 2;
                int c = col >> 1;
                u.put(half * uRow + c * uPixel, (byte) blueOf(r, g, b));
                v.put(half * vRow + c * vPixel, (byte) redOf(r, g, b));
            }
        }
    }

    static final int[] GAME_SOUNDS = {R.raw.wing, R.raw.point, R.raw.hit, R.raw.die, R.raw.swooshing, R.raw.thunder, R.raw.rain, R.raw.storm_backtrack};

    static final class Sample {
        final byte[] data;
        final long pts;
        final int flags;

        Sample(ByteBuffer source, MediaCodec.BufferInfo info, long pts) {
            data = new byte[info.size];
            source.position(info.offset);
            source.limit(info.offset + info.size);
            source.get(data);
            this.pts = pts;
            this.flags = info.flags & ~MediaCodec.BUFFER_FLAG_END_OF_STREAM;
        }

        MediaCodec.BufferInfo info() {
            MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();
            info.set(0, data.length, pts, flags);
            return info;
        }
    }

    static final class AudioStream {
        final SoundLog log = new SoundLog();
        final java.util.concurrent.ConcurrentLinkedQueue<Sample> ready = new java.util.concurrent.ConcurrentLinkedQueue<>();
        volatile MediaFormat format;
        volatile boolean failed;
        volatile boolean finished;
        private final Mixer mixer;
        private final AtomicBoolean cancelled;
        private MediaCodec encoder;
        private final MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();
        private final short[] chunk = new short[AUDIO_FRAMES * 2];
        private long position;
        private boolean inputDone;

        AudioStream(Map<Integer, Future<Pcm>> sounds, AtomicBoolean cancelled) {
            this.mixer = new Mixer(log, sounds);
            this.cancelled = cancelled;
        }

        void start() {
            try {
                MediaFormat wanted = MediaFormat.createAudioFormat(MediaFormat.MIMETYPE_AUDIO_AAC, AUDIO_RATE, 2);
                wanted.setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectLC);
                wanted.setInteger(MediaFormat.KEY_BIT_RATE, AUDIO_BITRATE);
                wanted.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, AUDIO_FRAMES * 4 * 2);
                encoder = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_AUDIO_AAC);
                encoder.configure(wanted, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
                encoder.start();
            } catch (Exception e) {
                fail();
            }
        }

        void pump(double seconds) {
            if (failed || inputDone) return;
            long target = (long) Math.floor(Math.max(0.0, seconds) * AUDIO_RATE);
            feed(target, false);
        }

        void finish(double seconds) {
            if (failed) return;
            mixer.end((long) Math.ceil(Math.max(0.0, seconds) * AUDIO_RATE));
            feed(mixer.total, true);
            for (int guard = 0; guard < 4000 && !failed && !finished && !cancelled.get(); guard++) drain(10000);
        }

        private void feed(long target, boolean last) {
            try {
                for (int guard = 0; guard < 100000 && !cancelled.get(); guard++) {
                    if (position >= target && !(last && !inputDone)) break;
                    int index = encoder.dequeueInputBuffer(position >= target ? 10000 : 2000);
                    if (index < 0) {
                        drain(0);
                        continue;
                    }
                    ByteBuffer input = encoder.getInputBuffer(index);
                    long pts = position * 1_000_000L / AUDIO_RATE;
                    if (position >= target) {
                        encoder.queueInputBuffer(index, 0, 0, pts, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                        inputDone = true;
                        break;
                    }
                    if (input == null) throw new IllegalStateException("No audio input buffer");
                    int frames = (int) Math.min(AUDIO_FRAMES, Math.min(target - position, input.capacity() / 4));
                    mixer.mix(position, frames, chunk);
                    input.clear();
                    input.order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().put(chunk, 0, frames * 2);
                    encoder.queueInputBuffer(index, 0, frames * 4, pts, 0);
                    position += frames;
                }
                drain(0);
            } catch (RuntimeException e) {
                fail();
            }
        }

        private void drain(long timeout) {
            if (failed || finished) return;
            try {
                for (int guard = 0; guard < 1000; guard++) {
                    int out = encoder.dequeueOutputBuffer(info, guard == 0 ? timeout : 0);
                    if (out == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                        format = encoder.getOutputFormat();
                    } else if (out >= 0) {
                        ByteBuffer output = encoder.getOutputBuffer(out);
                        if (output != null && info.size > 0 && (info.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) == 0) {
                            ready.add(new Sample(output, info, info.presentationTimeUs));
                        }
                        encoder.releaseOutputBuffer(out, false);
                        if ((info.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                            finished = true;
                            return;
                        }
                    } else if (out == MediaCodec.INFO_TRY_AGAIN_LATER) {
                        return;
                    }
                }
            } catch (RuntimeException e) {
                fail();
            }
        }

        private void fail() {
            failed = true;
            release();
        }

        void release() {
            MediaCodec codec = encoder;
            encoder = null;
            if (codec == null) return;
            try {
                codec.stop();
            } catch (RuntimeException ignored) {
            }
            codec.release();
        }
    }

    static final class Drain extends Thread {
        final MediaCodec encoder;
        final MediaMuxer muxer;
        final int fps;
        final boolean retime;
        final AudioStream audio;
        final java.util.concurrent.atomic.AtomicLong posted = new java.util.concurrent.atomic.AtomicLong();
        volatile IOException failure;
        volatile boolean muxing;
        volatile boolean reordered;
        volatile long written;
        volatile boolean withAudio;
        private volatile boolean stop;
        private MediaFormat videoFormat;
        private int videoTrack = -1, audioTrack = -1;
        private long lastRaw = Long.MIN_VALUE;
        private final List<Sample> early = new ArrayList<>();

        Drain(MediaCodec encoder, MediaMuxer muxer, int fps, boolean retime, AudioStream audio) {
            super("ReplayExportDrain");
            this.encoder = encoder;
            this.muxer = muxer;
            this.fps = fps;
            this.retime = retime;
            this.audio = audio;
        }

        @Override
        public void run() {
            MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();
            try {
                while (!stop) {
                    int index = encoder.dequeueOutputBuffer(info, 10000);
                    if (index == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                        if (videoFormat != null) throw new IOException("The video format changed while saving");
                        videoFormat = encoder.getOutputFormat();
                        startIfReady(false);
                    } else if (index >= 0) {
                        ByteBuffer output = encoder.getOutputBuffer(index);
                        boolean data = output != null && info.size > 0 && (info.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) == 0;
                        if (data) {
                            if (info.presentationTimeUs < lastRaw) reordered = true;
                            lastRaw = info.presentationTimeUs;
                            long pts = retime ? written * 1_000_000L / fps : info.presentationTimeUs;
                            startIfReady(false);
                            if (muxing) {
                                flushEarly();
                                writeAudioUpTo(pts);
                                info.presentationTimeUs = pts;
                                output.position(info.offset);
                                output.limit(info.offset + info.size);
                                muxer.writeSampleData(videoTrack, output, info);
                            } else {
                                early.add(new Sample(output, info, pts));
                                if (early.size() > fps * 3) startIfReady(true);
                            }
                            written++;
                        }
                        encoder.releaseOutputBuffer(index, false);
                        if ((info.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) break;
                    } else if (!muxing) {
                        startIfReady(false);
                    }
                }
                startIfReady(true);
                if (muxing) {
                    flushEarly();
                    writeAudioUpTo(Long.MAX_VALUE);
                }
            } catch (IOException e) {
                failure = e;
            } catch (RuntimeException e) {
                failure = new IOException("The video encoder stopped (" + e.getClass().getSimpleName() + ")", e);
            }
        }

        private void startIfReady(boolean force) {
            if (muxing || videoFormat == null) return;
            boolean audioSettled = audio == null || audio.format != null || audio.failed;
            if (!audioSettled && !force) return;
            videoTrack = muxer.addTrack(videoFormat);
            if (audio != null && audio.format != null && !audio.failed) {
                audioTrack = muxer.addTrack(audio.format);
                withAudio = true;
            }
            muxer.start();
            muxing = true;
        }

        private void flushEarly() {
            for (Sample sample : early) {
                writeAudioUpTo(sample.pts);
                muxer.writeSampleData(videoTrack, ByteBuffer.wrap(sample.data), sample.info());
            }
            early.clear();
        }

        private void writeAudioUpTo(long pts) {
            if (audio == null) return;
            Sample next;
            while ((next = audio.ready.peek()) != null && next.pts <= pts) {
                audio.ready.poll();
                if (audioTrack >= 0) muxer.writeSampleData(audioTrack, ByteBuffer.wrap(next.data), next.info());
            }
        }

        void finish() {
            try {
                join(20000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            if (isAlive()) {
                stop = true;
                try {
                    join(2000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    static final class Exporter {
        interface Listener {
            void progress(float share, String stage);

            void done(File video, String error);
        }

        final android.content.Context context;
        final android.content.Context owner;
        final Map<String, Object> values;
        GameView renderer;
        final GameView.Film film;
        final VideoPlan plan;
        final File folder;
        final Listener listener;
        final AtomicBoolean cancelled = new AtomicBoolean();
        double knownLength;
        boolean forceSoftware;
        boolean usedSoftware;
        boolean hadAudio;
        int richness;
        Thread thread;
        ExecutorService pool;
        private float lastStep;
        private int reportedPercent = -1;
        private String reportedStage = "";
        private long renderStarted;
        private String[] credit;
        private Paint creditFill, creditEdge;

        Exporter(android.content.Context context, Map<String, Object> values, GameView.Film film, VideoPlan plan, File folder, Listener listener) {
            this.context = context.getApplicationContext() != null ? context.getApplicationContext() : context;
            this.owner = context;
            this.values = values;
            this.film = film;
            this.plan = plan;
            this.folder = folder;
            this.listener = listener;
            this.credit = plan.credit ? SettingsActivity.videoCredit(this.context) : null;
        }

        void start() {
            thread = new Thread(this::run, "ReplayExport");
            thread.setPriority(Thread.NORM_PRIORITY);
            thread.start();
        }

        void cancel() {
            cancelled.set(true);
        }

        private void run() {
            File video = null;
            String error = null;
            try {
                video = render();
                if (cancelled.get()) {
                    if (video != null) video.delete();
                    video = null;
                }
            } catch (IOException | RuntimeException | OutOfMemoryError e) {
                if (video != null) video.delete();
                video = null;
                error = cancelled.get() ? null : "Something went wrong while making the video (" + e.getClass().getSimpleName()
                        + (e.getMessage() != null ? ": " + e.getMessage() : "") + ").";
            } finally {
                if (renderer != null) renderer.filmSetSink(null);
                if (pool != null) pool.shutdownNow();
            }
            listener.done(cancelled.get() ? null : video, error);
        }

        private void report(float share, String stage) {
            int percent = Math.round(Math.max(0f, Math.min(1f, share)) * 100f);
            String label = stage;
            if (stage.equals("Rendering") && share > 0.04f && renderStarted > 0L) {
                double elapsed = (System.nanoTime() - renderStarted) / 1e9;
                long left = Math.round(elapsed / share * (1f - share));
                label = left <= 1 ? "Almost done" : "Rendering, about " + (left < 60 ? left + " s" : (left / 60) + " min " + (left % 60) + " s") + " left";
            }
            if (percent == reportedPercent && label.equals(reportedStage)) return;
            reportedPercent = percent;
            reportedStage = label;
            listener.progress(share, label);
        }

        File render() throws IOException {
            int threads = Math.max(2, Math.min(4, Runtime.getRuntime().availableProcessors() - 1));
            pool = Executors.newFixedThreadPool(threads);
            Map<Integer, Future<Pcm>> sounds = new HashMap<>();
            if (plan.sound) {
                for (final int raw : GAME_SOUNDS) sounds.put(raw, pool.submit(() -> cachedDecode(context, raw)));
            }
            report(0f, "Getting ready");
            renderer = new GameView(owner, new MemoryPrefs(values), false);
            renderer.measure(View.MeasureSpec.makeMeasureSpec(film.width, View.MeasureSpec.EXACTLY),
                    View.MeasureSpec.makeMeasureSpec(film.height, View.MeasureSpec.EXACTLY));
            renderer.layout(0, 0, film.width, film.height);
            renderer.filmLoad(film);
            if (!renderer.filmReady()) throw new IOException("The replay couldn't be set up");
            double length = knownLength > 0.0 ? knownLength : renderer.filmLength();
            if (cancelled.get()) return null;
            if (!folder.isDirectory() && !folder.mkdirs()) throw new IOException("No room for the video");
            File out = new File(folder, "replay_" + System.currentTimeMillis() + ".mp4");
            try {
                if (Build.VERSION.SDK_INT >= 23 && !forceSoftware) {
                    boolean done;
                    try {
                        done = attempt(true, length, out, sounds);
                    } catch (IOException | RuntimeException e) {
                        if (cancelled.get()) throw e;
                        done = false;
                    }
                    if (done || cancelled.get()) return out;
                    out.delete();
                }
                usedSoftware = true;
                attempt(false, length, out, sounds);
                return out;
            } catch (IOException | RuntimeException e) {
                out.delete();
                throw e;
            }
        }

        private List<MediaFormat> formats(boolean surface) {
            List<MediaFormat> list = new ArrayList<>();
            for (int level = 2; level >= 0; level--) {
                MediaFormat format = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC, plan.width, plan.height);
                format.setInteger(MediaFormat.KEY_COLOR_FORMAT, surface ? MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface
                        : MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Flexible);
                format.setInteger(MediaFormat.KEY_BIT_RATE, plan.bitrate);
                format.setInteger(MediaFormat.KEY_FRAME_RATE, plan.fps);
                format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1);
                if (level >= 1) {
                    format.setInteger("max-bframes", 0);
                    if (Build.VERSION.SDK_INT >= 23) format.setInteger(MediaFormat.KEY_PRIORITY, 1);
                    if (Build.VERSION.SDK_INT >= 24) {
                        format.setInteger(MediaFormat.KEY_COLOR_STANDARD, surface ? MediaFormat.COLOR_STANDARD_BT709 : MediaFormat.COLOR_STANDARD_BT601_NTSC);
                        format.setInteger(MediaFormat.KEY_COLOR_RANGE, MediaFormat.COLOR_RANGE_LIMITED);
                        format.setInteger(MediaFormat.KEY_COLOR_TRANSFER, MediaFormat.COLOR_TRANSFER_SDR_VIDEO);
                    }
                }
                if (level >= 2) {
                    if (Build.VERSION.SDK_INT >= 23) format.setInteger(MediaFormat.KEY_OPERATING_RATE, Short.MAX_VALUE);
                    int[] high = highProfile();
                    if (high != null && Build.VERSION.SDK_INT >= 23) {
                        format.setInteger(MediaFormat.KEY_PROFILE, high[0]);
                        format.setInteger(MediaFormat.KEY_LEVEL, high[1]);
                    }
                }
                list.add(format);
            }
            return list;
        }

        private int[] highProfile() {
            try {
                MediaCodecInfo info = encoderInfo(MediaFormat.MIMETYPE_VIDEO_AVC);
                if (info == null) return null;
                int best = -1;
                for (MediaCodecInfo.CodecProfileLevel level : info.getCapabilitiesForType(MediaFormat.MIMETYPE_VIDEO_AVC).profileLevels) {
                    if (level.profile == MediaCodecInfo.CodecProfileLevel.AVCProfileHigh && level.level > best) best = level.level;
                }
                return best > 0 ? new int[] {MediaCodecInfo.CodecProfileLevel.AVCProfileHigh, best} : null;
            } catch (RuntimeException e) {
                return null;
            }
        }

        private MediaCodec configured(boolean surface) throws IOException {
            RuntimeException last = null;
            List<MediaFormat> options = formats(surface);
            for (int i = 0; i < options.size(); i++) {
                MediaCodec codec = plan.codec != null ? MediaCodec.createByCodecName(plan.codec) : MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_VIDEO_AVC);
                try {
                    codec.configure(options.get(i), null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
                    richness = options.size() - 1 - i;
                    return codec;
                } catch (RuntimeException e) {
                    last = e;
                    codec.release();
                }
            }
            if (last != null) throw last;
            throw new IOException("No video encoder");
        }

        private void advanceTo(double at) {
            while (renderer.filmTime() + lastStep * 0.5 < at) {
                float step = renderer.filmStep();
                if (step <= 0f) break;
                lastStep = step;
            }
        }

        private void drawFrame(Canvas canvas) {
            float sx = plan.width / (float) film.width, sy = plan.height / (float) film.height;
            canvas.save();
            canvas.drawColor(Color.BLACK);
            canvas.setDrawFilter(sx < 0.999f || sy < 0.999f ? new PaintFlagsDrawFilter(0, Paint.FILTER_BITMAP_FLAG) : null);
            canvas.scale(sx, sy);
            renderer.filmDraw(canvas);
            canvas.setDrawFilter(null);
            canvas.restore();
            drawCredit(canvas);
        }

        private void drawCredit(Canvas canvas) {
            if (credit == null) return;
            float shortSide = Math.min(plan.width, plan.height);
            if (creditFill == null) {
                Typeface face = Typeface.create("sans-serif-medium", Typeface.NORMAL);
                float size = Math.max(9f, shortSide * 0.022f);
                creditFill = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.SUBPIXEL_TEXT_FLAG);
                creditFill.setTypeface(face);
                creditFill.setTextSize(size);
                creditFill.setTextAlign(Paint.Align.RIGHT);
                creditFill.setColor(0xE6FFFFFF);
                creditEdge = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.SUBPIXEL_TEXT_FLAG);
                creditEdge.setTypeface(face);
                creditEdge.setTextSize(size);
                creditEdge.setTextAlign(Paint.Align.RIGHT);
                creditEdge.setStyle(Paint.Style.STROKE);
                creditEdge.setStrokeJoin(Paint.Join.ROUND);
                creditEdge.setStrokeWidth(Math.max(1.5f, size * 0.18f));
                creditEdge.setColor(0x8C000000);
            }
            float size = creditFill.getTextSize();
            float margin = Math.round(shortSide * 0.03f);
            float x = plan.width - margin;
            float y = plan.height - margin - creditFill.descent();
            for (int i = credit.length - 1; i >= 0; i--) {
                canvas.drawText(credit[i], x, y, creditEdge);
                canvas.drawText(credit[i], x, y, creditFill);
                y -= size * 1.25f;
            }
        }

        private boolean attempt(boolean gpu, double length, File out, Map<Integer, Future<Pcm>> sounds) throws IOException {
            final int fps = plan.fps;
            final long frames = Math.max(1L, (long) Math.ceil(length * fps) + 1L);
            MediaCodec encoder = configured(gpu);
            Surface input = null;
            MediaMuxer muxer = null;
            Drain drain = null;
            AudioStream audio = plan.sound ? new AudioStream(sounds, cancelled) : null;
            Bitmap frame = null;
            int[] pixels = null;
            Canvas canvas = null;
            boolean posted = false;
            try {
                if (gpu) input = encoder.createInputSurface();
                encoder.start();
                muxer = new MediaMuxer(out.getPath(), MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
                if (audio != null) audio.start();
                renderer.filmRestart();
                renderer.filmSetSink(audio != null ? audio.log : null);
                lastStep = 0f;
                if (!gpu) {
                    frame = Bitmap.createBitmap(plan.width, plan.height, Bitmap.Config.ARGB_8888);
                    frame.setDensity(Bitmap.DENSITY_NONE);
                    canvas = new Canvas(frame);
                    canvas.setDensity(Bitmap.DENSITY_NONE);
                    pixels = new int[plan.width * plan.height];
                }
                drain = new Drain(encoder, muxer, fps, gpu, audio);
                drain.start();
                renderStarted = System.nanoTime();
                MediaCodec.BufferInfo unused = new MediaCodec.BufferInfo();
                for (long next = 0; next < frames; next++) {
                    if (cancelled.get() || drain.failure != null) break;
                    advanceTo(next / (double) fps);
                    if (gpu) {
                        Canvas surfaceCanvas = input.lockHardwareCanvas();
                        try {
                            drawFrame(surfaceCanvas);
                        } finally {
                            input.unlockCanvasAndPost(surfaceCanvas);
                        }
                        posted = true;
                    } else {
                        int index = -1;
                        for (int wait = 0; wait < 3000 && index < 0 && !cancelled.get() && drain.failure == null; wait++) {
                            index = encoder.dequeueInputBuffer(10000);
                        }
                        if (index < 0) break;
                        drawFrame(canvas);
                        frame.getPixels(pixels, 0, plan.width, 0, 0, plan.width, plan.height);
                        Image image = encoder.getInputImage(index);
                        if (image == null) throw new IOException("This phone's video encoder isn't supported");
                        fillImage(image, pixels, plan.width, plan.height);
                        encoder.queueInputBuffer(index, 0, plan.width * plan.height * 3 / 2, next * 1_000_000L / fps, 0);
                    }
                    drain.posted.incrementAndGet();
                    if (audio != null) audio.pump(Math.min(renderer.filmTime(), length));
                    report((next + 1) / (float) frames, "Rendering");
                }
                if (audio != null && !cancelled.get()) audio.finish(length);
                if (gpu) {
                    encoder.signalEndOfInputStream();
                } else {
                    int index = -1;
                    for (int wait = 0; wait < 3000 && index < 0 && drain.failure == null; wait++) index = encoder.dequeueInputBuffer(10000);
                    if (index >= 0) encoder.queueInputBuffer(index, 0, 0, frames * 1_000_000L / fps, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                }
                report(1f, "Finishing");
                drain.finish();
                if (drain.failure != null) throw drain.failure;
                if (cancelled.get()) return true;
                if (!drain.muxing) throw new IOException("The video encoder didn't start");
                hadAudio = drain.withAudio;
                if (gpu && (drain.reordered || drain.written != drain.posted.get())) return false;
                return true;
            } catch (IllegalStateException | UnsupportedOperationException e) {
                if (gpu && !posted && !cancelled.get()) return false;
                throw e;
            } finally {
                renderer.filmSetSink(null);
                if (drain != null) drain.finish();
                try {
                    encoder.stop();
                } catch (RuntimeException ignored) {
                }
                encoder.release();
                if (input != null) input.release();
                if (audio != null) audio.release();
                if (muxer != null) {
                    try {
                        if (drain != null && drain.muxing) muxer.stop();
                    } catch (RuntimeException ignored) {
                    }
                    muxer.release();
                }
                if (frame != null) frame.recycle();
            }
        }

        private void fillImage(Image image, int[] pixels, int width, int height) throws IOException {
            Image.Plane[] planes = image.getPlanes();
            if (planes.length < 3) throw new IOException("This phone's video encoder isn't supported");
            final ByteBuffer y = planes[0].getBuffer(), u = planes[1].getBuffer(), v = planes[2].getBuffer();
            final int yRow = planes[0].getRowStride(), yPixel = planes[0].getPixelStride();
            final int uRow = planes[1].getRowStride(), uPixel = planes[1].getPixelStride();
            final int vRow = planes[2].getRowStride(), vPixel = planes[2].getPixelStride();
            int parts = pool == null ? 1 : Math.max(1, Math.min(4, height / 64));
            if (parts == 1) {
                fillPlanes(pixels, width, height, y, yRow, yPixel, u, uRow, uPixel, v, vRow, vPixel, 0, height);
                return;
            }
            List<Future<?>> jobs = new ArrayList<>();
            int band = ((height / parts) + 1) & ~1;
            for (int start = 0; start < height; start += band) {
                final int from = start, to = Math.min(height, start + band);
                jobs.add(pool.submit(() -> fillPlanes(pixels, width, height, y.duplicate(), yRow, yPixel, u.duplicate(), uRow, uPixel,
                        v.duplicate(), vRow, vPixel, from, to)));
            }
            for (Future<?> job : jobs) {
                try {
                    job.get();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new IOException("Cancelled");
                } catch (java.util.concurrent.ExecutionException e) {
                    throw new IOException("Converting a frame failed", e.getCause());
                }
            }
        }
    }
}
