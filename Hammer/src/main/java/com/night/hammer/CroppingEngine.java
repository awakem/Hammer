package com.night.hammer;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;

import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

final class CroppingEngine {
    private final IHammerProxy mProxy;
    private final int cutWidth, cutHeight;
    private final int srcWidth, srcHeight;

    private final Matrix mMatrix;

    public CroppingEngine(IHammerProxy proxy, int width, int height) throws IOException {
        this.mProxy = proxy;
        this.cutWidth = width;
        this.cutHeight = height;
        mMatrix = new Matrix();
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(mProxy.open(), null, options);
        srcWidth = options.outWidth;
        srcHeight = options.outHeight;
        HammerToos.d("ImageCropping==>Width: " + srcWidth + " ||Height: " + srcHeight);
    }

    @WorkerThread
    @NonNull
    public File onHammer() throws Exception {
        if (cutWidth <= 0 || cutHeight <= 0) {
            throw new RuntimeException(HammerToos.getString(R.string.hammer_error_param_cut));
        }
        Bitmap mSourceBitmap = null;
        Bitmap mScaleBitmap = null;
        Bitmap mAngleBitmap = null;
        File mResultFile = HammerToos.getImagePath(mProxy.getImageName());
        try (FileOutputStream mFileOutputStream = new FileOutputStream(mResultFile)) {
            //1.加载资源
            if (srcWidth <= 0 || srcHeight <= 0) {
                throw new FileNotFoundException(HammerToos.getString(R.string.hammer_error_res));
            }
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = false;
            if (srcWidth <= cutWidth || srcHeight <= cutHeight) {
                options.inSampleSize = 1;
            } else {
                //取最小可缩小倍数
                int inSampleSizeWidth = (int) Math.floor(srcWidth / (float) cutWidth);
                int inSampleSizeHeight = (int) Math.floor(srcHeight / (float) cutHeight);
                options.inSampleSize = Math.min(inSampleSizeWidth, inSampleSizeHeight);
            }
            mSourceBitmap = BitmapFactory.decodeStream(mProxy.open(), null, options);
            if (mSourceBitmap == null) {
                throw new OutOfMemoryError(HammerToos.getString(R.string.hammer_error_oom));
            }
            //2.尺寸处理
            int width = mSourceBitmap.getWidth();
            int height = mSourceBitmap.getHeight();
            //旋转角度
            int angle = HammerToos.getImageAngle(mProxy);
            if (width == cutWidth && height == cutHeight && angle == 0) {
                HammerToos.d("Images do not require processing");
                writBitmap(mSourceBitmap, mFileOutputStream);
            } else {
                mMatrix.reset();
                if (width != cutWidth || height != cutHeight) {
                    //缩放处理
                    float x = cutWidth / (float) width;
                    float y = cutHeight / (float) height;
                    if (x < 0 && y < 0) {
                        float scale = Math.min(x, y);
                        mMatrix.setScale(scale, scale);
                    } else if (x > 0 && y > 0) {
                        float scale = Math.max(x, y);
                        mMatrix.setScale(scale, scale);
                    } else {
                        float scale = Math.min(x, y);
                        mMatrix.setScale(scale, scale);
                    }
                }
                if (angle != 0) {
                    //旋转处理
                    mMatrix.postRotate(angle);
                }
                mScaleBitmap = Bitmap.createBitmap(mSourceBitmap, 0, 0, width, height, mMatrix, true);
                int mScaleWidth = mScaleBitmap.getWidth();
                int mScaleHeight = mScaleBitmap.getHeight();
                //开始裁剪
                float dx = (mScaleWidth - cutWidth) / 2F;
                float dy = (mScaleHeight - cutHeight) / 2F;
                mAngleBitmap = Bitmap.createBitmap(mScaleBitmap, (int) dx, (int) dy, cutWidth, cutHeight);
                writBitmap(mAngleBitmap, mFileOutputStream);
            }
        } finally {
            HammerToos.toRecycledBitmap(mAngleBitmap, "ImageCropping==> Recycle Cropping Bitmap-1");
            HammerToos.toRecycledBitmap(mScaleBitmap, "ImageCropping==> Recycle Scale Bitmap-2");
            HammerToos.toRecycledBitmap(mSourceBitmap, "ImageCropping==> Recycle Decode Bitmap-3");
            mProxy.close();
            HammerToos.d("ImageCropping==> Close Stream");
        }
        return mResultFile;
    }


    private void writBitmap(Bitmap bitmap, FileOutputStream fos) {
        boolean isPNG = HammerToos.isPNG(mProxy);
        bitmap.compress(isPNG ? Bitmap.CompressFormat.PNG : Bitmap.CompressFormat.JPEG, 100, fos);
    }
}