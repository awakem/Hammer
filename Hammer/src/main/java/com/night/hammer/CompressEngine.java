package com.night.hammer;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;

import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

final class CompressEngine {
    private final IHammerProxy mProxy;
    private final int maxWidth, maxHeight;
    private final long maxSize;
    private final int step;
    private final int srcWidth, srcHeight;

    public CompressEngine(@NonNull IHammerProxy proxy, int maxWidth, int maxHeight, long maxSize, @IntRange(from = 1, to = 100) int step) throws IOException {
        this.mProxy = proxy;
        this.maxWidth = maxWidth;
        this.maxHeight = maxHeight;
        this.maxSize = maxSize;
        this.step = step;
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(mProxy.open(), null, options);
        srcWidth = options.outWidth;
        srcHeight = options.outHeight;
        HammerToos.d("图片压缩==>原始图片宽度: " + srcWidth + " ||原始图片高度: " + srcHeight);
    }

    @WorkerThread
    @NonNull
    public File openTask() throws Exception {
        return initVolume(initSize(initBitmap()));
    }

    @NonNull
    private File initVolume(@NonNull Bitmap bitmap) throws IOException {
        File mResultFile = HammerToos.toFile(mProxy.open(), mProxy.getImageName());
        if (mResultFile == null) {
            mProxy.close();
            throw new FileNotFoundException(HammerToos.getString(R.string.hammer_error_create_file));
        }
        try (ByteArrayOutputStream mByteArrayOutputStream = new ByteArrayOutputStream();
             FileOutputStream mFileOutputStream = new FileOutputStream(mResultFile);
             BufferedOutputStream mBufferedOutputStream = new BufferedOutputStream(mFileOutputStream)) {
            int quality = 100;
            long mNowLength = 0;
            boolean isPNG = HammerToos.isPNG(mProxy);
            do {
                //清空缓存区
                mByteArrayOutputStream.reset();
                boolean isSuccess = bitmap.compress(isPNG ? Bitmap.CompressFormat.PNG : Bitmap.CompressFormat.JPEG, quality, mByteArrayOutputStream);
                if (!isSuccess) {
                    break;
                }
                if (isPNG) {
                    //PNG图片不进行质量压缩
                    break;
                }
                //降低质量
                quality -= step;
                //重新计算缓存区数据大小
                mNowLength = mByteArrayOutputStream.toByteArray().length;
                //如果压缩系数小于0的时候直接跳出循环
                if (quality <= 0) {
                    break;
                }
            } while (mNowLength > maxSize);
            //写入文件
            mBufferedOutputStream.write(mByteArrayOutputStream.toByteArray());
        } catch (IOException e) {
            throw new IOException(HammerToos.getString(R.string.hammer_error));
        } finally {
            mProxy.close();
            if (!bitmap.isRecycled()) {
                HammerToos.e("图片压缩==> 释放initSize");
                bitmap.recycle();
                bitmap = null;
            }
        }
        return mResultFile;
    }

    @NonNull
    private Bitmap initSize(@NonNull Bitmap bitmap) {
        float angle = HammerToos.getImageAngle(mProxy);
        Matrix mMatrix = new Matrix();
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int tagWidth = maxWidth;
        if (tagWidth <= 0) {
            tagWidth = srcWidth;
        }
        int tagHeight = maxHeight;
        if (tagHeight <= 0) {
            tagHeight = srcHeight;
        }
        float scaleX;
        float scaleY;
        if (srcWidth > srcHeight) {
            //长图
            float scale = width / (float) height;
            scaleX = tagWidth / (float) width;
            scaleY = tagWidth / scale / height;
        } else if (srcWidth < srcHeight) {
            //高图
            float scale = width / (float) height;
            scaleY = tagHeight / (float) height;
            scaleX = tagHeight * scale / width;
        } else {
            //方图
            scaleX = scaleY = tagWidth / (float) width;
        }
        mMatrix.setScale(scaleX, scaleY);
        if (angle != 0F) {
            mMatrix.postRotate(angle);
        }
        Bitmap mAngleBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), mMatrix, true);
        if (!bitmap.isRecycled()) {
            HammerToos.e("图片压缩==> 释放initBitmap");
            bitmap.recycle();
            bitmap = null;
        }
        return mAngleBitmap;
    }


    @NonNull
    private Bitmap initBitmap() throws OutOfMemoryError, IOException {
        if (srcWidth <= 0 || srcHeight <= 0) {
            HammerToos.e("图片压缩==> 初始化Bitmap失败，原图尺寸异常");
            mProxy.close();
            throw new FileNotFoundException(HammerToos.getString(R.string.hammer_error_res));
        }
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = false;
        if ((maxWidth <= 0 && maxHeight <= 0) || (srcWidth <= maxWidth || srcHeight <= maxHeight)) {
            options.inSampleSize = 1;
        } else {
            //取最小可缩小倍数
            int inSampleSizeWidth = (int) Math.floor(srcWidth / (float) maxWidth);
            int inSampleSizeHeight = (int) Math.floor(srcHeight / (float) maxHeight);
            options.inSampleSize = Math.min(inSampleSizeWidth, inSampleSizeHeight);
        }
        Bitmap bitmap = BitmapFactory.decodeStream(mProxy.open(), null, options);
        if (bitmap == null) {
            mProxy.close();
            throw new OutOfMemoryError(HammerToos.getString(R.string.hammer_error_oom));
        }
        return bitmap;
    }
}
