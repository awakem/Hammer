package com.night.hammer;

import android.annotation.SuppressLint;
import android.content.Context;

import androidx.annotation.NonNull;

public class HammerHelp {
    @SuppressLint("StaticFieldLeak")
    private static Context mContext;
    private static boolean isDebug = false;

    public static void initialization(@NonNull Context context) {
        mContext = context;
    }

    public static Context getContext() {
        if (mContext == null) {
            throw new NullPointerException("Not Initialization Hammer Module");
        }
        return mContext;
    }

    public static void setDebug(boolean isDebug) {
        HammerHelp.isDebug = isDebug;
    }

    public static boolean isDebug() {
        return HammerHelp.isDebug;
    }
}
