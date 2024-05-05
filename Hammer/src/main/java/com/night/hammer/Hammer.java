package com.night.hammer;

import android.content.Context;

public class Hammer {
    private static Context mContext;
    private static boolean mDebug = false;

    /**
     * 图片压缩
     *
     * @return {@link BuilderCompress}
     */
    public static BuilderCompress withCompress() {
        return new BuilderCompress();
    }

    /**
     * 图片裁剪
     *
     * @return {@link BuilderCropping}
     */
    public static BuilderCropping withCropping() {
        return new BuilderCropping();
    }

    /**
     * 设置Debug模式
     *
     * @param isDebug 是否运行在Debug模式下
     */
    public static void setDebug(boolean isDebug) {
        mDebug = isDebug;
    }

    static boolean isDebug() {
        return mDebug;
    }

    static Context getContext() {
        return mContext;
    }

    static void initialization(Context context) {
        mContext = context.getApplicationContext();
    }
}
