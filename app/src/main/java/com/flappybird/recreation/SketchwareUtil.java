package com.flappybird.recreation;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.Uri;
import android.os.Build;
import android.util.SparseBooleanArray;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public final class SketchwareUtil {

    private static final Random RANDOM = new Random();

    private SketchwareUtil() {
        throw new AssertionError("No instances for you!");
    }

    public static void showCustomToast(@NonNull final Context context, @NonNull final String message, final int textColor, final int textSize, final int bgColor, final int radius, final int gravity, @DrawableRes final int icon) {
        Toast toast = Toast.makeText(context, message, Toast.LENGTH_SHORT);
        View view = toast.getView();
        if (view == null) return;

        TextView textView = view.findViewById(android.R.id.message);
        textView.setTextColor(textColor);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize);

        if (icon != 0) {
            textView.setCompoundDrawablesWithIntrinsicBounds(icon, 0, 0, 0);
            textView.setCompoundDrawablePadding(12);
        }

        GradientDrawable background = new GradientDrawable();
        background.setColor(bgColor);
        background.setCornerRadius(radius);
        view.setBackground(background);
        view.setPadding(24, 16, 24, 16);
        view.setElevation(8);

        toast.setGravity(gravity, 0, gravity == Gravity.CENTER ? 0 : 150);
        toast.show();
    }

    public static void showCustomToast(@NonNull final Context context, @NonNull final String message, final int textColor, final int textSize, final int bgColor, final int radius, final int gravity) {
        showCustomToast(context, message, textColor, textSize, bgColor, radius, gravity, 0);
    }

    public static void sortListMap(@NonNull final ArrayList<HashMap<String, Object>> listMap, @NonNull final String key, final boolean isNumber, final boolean ascending) {
        Collections.sort(listMap, (map1, map2) -> {
            Object val1 = map1.get(key);
            Object val2 = map2.get(key);

            if (val1 == null || val2 == null) return 0;

            int comparison;
            if (isNumber) {
                comparison = Double.compare(Double.parseDouble(val1.toString()), Double.parseDouble(val2.toString()));
            } else {
                comparison = val1.toString().compareTo(val2.toString());
            }

            return ascending ? comparison : -comparison;
        });
    }

    public static void cropImage(@NonNull final Activity activity, @NonNull final Uri imageUri, final int requestCode) {
        Intent cropIntent = new Intent("com.android.camera.action.CROP");
        cropIntent.setDataAndType(imageUri, "image/*");
        cropIntent.putExtra("crop", "true");
        cropIntent.putExtra("aspectX", 1);
        cropIntent.putExtra("aspectY", 1);
        cropIntent.putExtra("outputX", 280);
        cropIntent.putExtra("outputY", 280);
        cropIntent.putExtra("return-data", true);
        cropIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        if (cropIntent.resolveActivity(activity.getPackageManager()) != null) {
            activity.startActivityForResult(cropIntent, requestCode);
        } else {
            Toast.makeText(activity, "No app can perform this crop action.", Toast.LENGTH_SHORT).show();
        }
    }

    public static boolean isConnected(@NonNull final Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            NetworkCapabilities capabilities = cm.getNetworkCapabilities(cm.getActiveNetwork());
            return capabilities != null && (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) || capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR));
        } else {
            return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected();
        }
    }

    public static String streamToString(@NonNull final InputStream inputStream) {
        StringBuilder stringBuilder = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return stringBuilder.toString();
    }

    public static void hideKeyboard(@NonNull final Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        View view = activity.getCurrentFocus();
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public static void showKeyboard(@NonNull final Context context, @NonNull final View view) {
        if (view.requestFocus()) {
            InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
        }
    }

    public static void showMessage(@NonNull final Context context, @NonNull final String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    public static int getLocationX(@NonNull final View view) {
        int[] location = new int[2];
        view.getLocationOnScreen(location);
        return location[0];
    }

    public static int getLocationY(@NonNull final View view) {
        int[] location = new int[2];
        view.getLocationOnScreen(location);
        return location[1];
    }

    public static int getRandom(int min, int max) {
        return RANDOM.nextInt((max - min) + 1) + min;
    }

    public static ArrayList<Integer> getCheckedItemPositions(@NonNull final ListView listView) {
        ArrayList<Integer> result = new ArrayList<>();
        SparseBooleanArray arr = listView.getCheckedItemPositions();
        for (int i = 0; i < arr.size(); i++) {
            if (arr.valueAt(i)) {
                result.add(arr.keyAt(i));
            }
        }
        return result;
    }

    public static int dpToPx(@NonNull final Context context, final float dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.getResources().getDisplayMetrics());
    }

    public static int getDisplayWidthPixels(@NonNull final Context context) {
        return context.getResources().getDisplayMetrics().widthPixels;
    }

    public static int getDisplayHeightPixels(@NonNull final Context context) {
        return context.getResources().getDisplayMetrics().heightPixels;
    }

    @NonNull
    public static ArrayList<String> getKeysFromMap(@NonNull final Map<String, Object> map) {
        return new ArrayList<>(map.keySet());
    }
}
