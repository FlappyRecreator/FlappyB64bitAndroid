package com.flappybird.recreation;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.Process;
import android.util.Log;
import com.google.android.material.color.DynamicColors;

public class FlappyClassy extends Application {

    private static Context mApplicationContext;

    public static Context getContext() {
        return mApplicationContext;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mApplicationContext = getApplicationContext();
        if (mApplicationContext == null) {
            mApplicationContext = this;
        }

        DynamicColors.applyToActivitiesIfAvailable(this);

        Thread.setDefaultUncaughtExceptionHandler(
                (thread, throwable) -> {
                    Intent intent = new Intent(getApplicationContext(), DebugActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    intent.putExtra("error", Log.getStackTraceString(throwable));
                    startActivity(intent);
                    Process.killProcess(Process.myPid());
                    System.exit(1);
                });
    }
}