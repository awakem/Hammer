package com.night.hammer;

import androidx.annotation.Nullable;
import androidx.exifinterface.media.ExifInterface;

import android.graphics.Bitmap;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

class HammerToos {
    private final static SimpleDateFormat mSimpleDateFormat = new SimpleDateFormat("_yyyyMMdd_HHmmss_", Locale.CHINESE);
    private static final String TAG = "Hammer";

    /**
     * 获取资源字符
     *
     * @param id 资源ID
     * @return 资源字符
     */
    static String getString(@StringRes int id) {
        return Hammer.getContext().getString(id);
    }

    /**
     * 资源是否为PNG
     *
     * @param xy {@link ImageHammer}
     * @return 否为PNG
     */
    static boolean isPNG(@NonNull IHammerProxy xy) {
        return (xy.getImageName().contains(".png") || xy.getImageName().contains(".PNG"));
    }

    /**
     * 获取图片旋转角度
     *
     * @param xy {@link ImageHammer}
     * @return 图片旋转角度
     */
    static int getImageAngle(@NonNull IHammerProxy xy) {
        File mCacheFile = null;
        try {
            ExifInterface mExif;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                mExif = new ExifInterface(xy.open());
            } else {
                mCacheFile = toFile(xy.open(), xy.getImageName());
                mExif = new ExifInterface(mCacheFile.getAbsolutePath());
            }
            int orientation = mExif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL
            );
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    return 90;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    return 180;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    return 270;
                default:
                    return 0;

            }
        } catch (IOException e) {
            return 0;
        } finally {
            if (mCacheFile != null) {
                boolean delete = mCacheFile.delete();
                d("Delete temporary conversion file: " + delete);
            }
        }

    }

    static File getImagePath(String name) {
        File mBaseFile = Hammer.getContext().getExternalCacheDir();
        if (mBaseFile == null) {
            mBaseFile = Hammer.getContext().getCacheDir();
        }
        String folderPath = mBaseFile.getAbsolutePath() + File.separator + "TemporaryCache" + File.separator;
        File mFolderPath = new File(folderPath);
        if (mFolderPath.exists() && mFolderPath.isFile()) {
            folderPath = folderPath + "Copy" + File.separator;
            d("The temporary directory has the same name");
        }
        if (!mFolderPath.exists() || !mFolderPath.isDirectory()) {
            boolean mkdirs = mFolderPath.mkdirs();
            d("Temporary directory does not exist, directory creation result: " + mkdirs);
        }
        File mResultFile = new File(folderPath + name);
        if (mResultFile.exists() && mResultFile.isFile()) {
            boolean delete = mResultFile.delete();
            d("File repeat, file deletion result: " + delete);
        }
        return mResultFile;
    }


    static void e(String msg) {
        if (Hammer.isDebug()) {
            Log.e(TAG, msg);
        }
    }

    static void d(String msg) {
        if (Hammer.isDebug()) {
            Log.d(TAG, msg);
        }
    }

    static String getImageName() {
        Date mDate = new Date(System.currentTimeMillis());
        int mRandomInt = new Random().nextInt(9999 - 1000 + 1) + 1000;
        return "IMG" + mSimpleDateFormat.format(mDate) + mRandomInt + ".JPG";
    }

    static void toRecycledBitmap(@Nullable Bitmap bitmap, String msg) {
        if (bitmap == null || bitmap.isRecycled()) {
            return;
        }
        d(msg);
        bitmap.recycle();
    }

    @NonNull
    private static File toFile(InputStream inputStream, String name) throws IOException {
        File mFile = getImagePath(name);
        try (FileOutputStream mFileOutputStream = new FileOutputStream(mFile)) {
            int mReadLength;
            byte[] buffer = new byte[1024];
            while ((mReadLength = inputStream.read(buffer)) != -1) {
                mFileOutputStream.write(buffer, 0, mReadLength);
            }
            return mFile;
        } catch (IOException e) {
            return mFile;
        }
    }
}
