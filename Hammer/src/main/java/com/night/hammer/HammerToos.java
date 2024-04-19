package com.night.hammer;

import android.media.ExifInterface;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class HammerToos {
    private static final String TAG = "Hammer";

    /**
     * 获取资源字符
     *
     * @param id 资源ID
     * @return 资源字符
     */
    static String getString(@StringRes int id) {
        return HammerHelp.getContext().getString(id);
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
                Log.d(TAG, "获取图片旋转角度缓存图片删除结果: " + delete);
            }
        }

    }

    static File toFile(InputStream inputStream, String name) {
        File mFile = getFile(name);
        try (FileOutputStream mFileOutputStream = new FileOutputStream(mFile)) {
            int mReadLength;
            byte[] buffer = new byte[1024];
            while ((mReadLength = inputStream.read(buffer)) != -1) {
                mFileOutputStream.write(buffer, 0, mReadLength);
            }
            return mFile;
        } catch (IOException e) {
            return null;
        }
    }

    static void e(@StringRes int id) {
        if (HammerHelp.isDebug()) {
            Log.e(TAG, getString(id));
        }
    }

    static void d(@StringRes int id) {
        if (HammerHelp.isDebug()) {
            Log.d(TAG, getString(id));
        }
    }


    static void e(String msg) {
        if (HammerHelp.isDebug()) {
            Log.e(TAG, msg);
        }
    }

    static void d(String msg) {
        if (HammerHelp.isDebug()) {
            Log.d(TAG, msg);
        }
    }


    @NonNull
    private static File getFile(String name) {
        File mBaseFile = HammerHelp.getContext().getExternalCacheDir();
        if (mBaseFile == null) {
            mBaseFile = HammerHelp.getContext().getCacheDir();
        }
        String folderPath = mBaseFile.getAbsolutePath() + File.separator + "TemporaryCache" + File.separator;
        File mFolderPath = new File(folderPath);
        if (!mFolderPath.exists() || !mFolderPath.isDirectory()) {
            boolean mkdirs = mFolderPath.mkdirs();
            Log.d(TAG, "临时文件夹创建结果: " + mkdirs);
        }
        File mResultFile = new File(folderPath + name);
        if (mResultFile.exists() && mResultFile.isFile()) {
            boolean delete = mResultFile.delete();
            Log.d(TAG, "文件重复，删除历史文件: " + delete);
        }
        return mResultFile;
    }
}
