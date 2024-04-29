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

    public CroppingEngine(IHammerProxy proxy, int width, int height) throws IOException {
        this.mProxy = proxy;
        this.cutWidth = width;
        this.cutHeight = height;
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(mProxy.open(), null, options);
        srcWidth = options.outWidth;
        srcHeight = options.outHeight;
        HammerToos.d("ImageCropping==>Width: " + srcWidth + " ||Height: " + srcHeight);
    }

    @WorkerThread
    @NonNull
    public File openTask() throws Exception {
        Bitmap mBitmap = initSize(initBitmap());
        boolean isPNG = HammerToos.isPNG(mProxy);
        File mResultFile = HammerToos.toFile(mProxy.open(), mProxy.getImageName());
        try (FileOutputStream mFileOutputStream = new FileOutputStream(mResultFile)) {
            boolean compress = mBitmap.compress(isPNG ? Bitmap.CompressFormat.PNG : Bitmap.CompressFormat.JPEG, 100, mFileOutputStream);
            if (!compress) {
                mProxy.close();
                HammerToos.d("ImageCropping==> Bitmap To File Error");
                throw new IOException(HammerToos.getString(R.string.hammer_error));
            }
        } catch (IOException e) {
            HammerToos.d("ImageCropping==> Bitmap To File Error: "+e.getMessage());
            throw new IOException(HammerToos.getString(R.string.hammer_error));
        } finally {
            if (!mBitmap.isRecycled()) {
                HammerToos.d("ImageCropping==> Recycle Cropping Bitmap-3");
                mBitmap.recycle();
                mBitmap = null;
            }
            mProxy.close();
        }
        return mResultFile;
    }

    @NonNull
    private Bitmap initSize(@NonNull Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        if (width == cutWidth && height == cutHeight) {
            HammerToos.d("ImageCropping==> Return Source Resources");
            return bitmap;
        }
        //旋转角度
        int angle = HammerToos.getImageAngle(mProxy);
        Matrix mMatrix = new Matrix();
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
        if (angle != 0F) {
            mMatrix.postRotate(angle);
        }
        //缩放处理
        Bitmap mScaleBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), mMatrix, true);
        int mScaleWidth = mScaleBitmap.getWidth();
        int mScaleHeight = mScaleBitmap.getHeight();
        if (!bitmap.isRecycled()) {
            HammerToos.d("ImageCropping==> Recycle Decode Bitmap-1");
            bitmap.recycle();
            bitmap = null;
        }
        //开始裁剪
        float dx = (mScaleWidth - cutWidth) / 2F;
        float dy = (mScaleHeight - cutHeight) / 2F;
        Bitmap mAngleBitmap = Bitmap.createBitmap(mScaleBitmap, (int) dx, (int) dy, cutWidth, cutHeight);
        if (!mScaleBitmap.isRecycled()) {
            HammerToos.d("ImageCropping==> Recycle Scale Bitmap-2");
            mScaleBitmap.recycle();
            mScaleBitmap = null;
        }
        return mAngleBitmap;
    }

    @NonNull
    private Bitmap initBitmap() throws OutOfMemoryError, IOException {
        if (srcWidth <= 0 || srcHeight <= 0) {
            mProxy.close();
            HammerToos.e("ImageCropping==> Resources Error");
            throw new FileNotFoundException(HammerToos.getString(R.string.hammer_error_res));
        }
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = false;
        if ((cutWidth <= 0 && cutHeight <= 0) || (srcWidth <= cutWidth || srcHeight <= cutHeight)) {
            options.inSampleSize = 1;
        } else {
            //取最小可缩小倍数
            int inSampleSizeWidth = (int) Math.floor(srcWidth / (float) cutWidth);
            int inSampleSizeHeight = (int) Math.floor(srcHeight / (float) cutHeight);
            options.inSampleSize = Math.min(inSampleSizeWidth, inSampleSizeHeight);
        }
        Bitmap bitmap = BitmapFactory.decodeStream(mProxy.open(), null, options);
        if (bitmap == null) {
            mProxy.close();
            HammerToos.e("ImageCropping==> Resources Decode Error");
            throw new OutOfMemoryError(HammerToos.getString(R.string.hammer_error_oom));
        }
        return bitmap;
    }
}