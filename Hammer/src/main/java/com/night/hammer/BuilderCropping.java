package com.night.hammer;

import android.net.Uri;
import android.text.TextUtils;

import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;
import androidx.documentfile.provider.DocumentFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class BuilderCropping {
    private int mWidth = 720;
    private int mHeight = 720;

    /**
     * 设置裁剪尺寸
     *
     * @param width  宽
     * @param height 高
     * @return {@link BuilderCropping}
     */
    public BuilderCropping setImageSize(@IntRange(from = 1) int width, @IntRange(from = 1) int height) {
        this.mWidth = width;
        this.mHeight = height;
        return this;
    }

    /**
     * 启动裁剪任务
     *
     * @param file 图片文件
     * @return 裁剪后文件
     * @throws Exception 裁剪失败异常
     */
    @NonNull
    @WorkerThread
    public File onLaunch(@Nullable File file) throws Exception {
        if (file == null || !file.exists() || !file.isFile()) {
            throw new FileNotFoundException(HammerToos.getString(R.string.hammer_error_res));
        }
        return new CroppingEngine(new ImageHammer() {
            @Override
            InputStream initStream() throws IOException {
                return new FileInputStream(file);
            }

            @Override
            String initFileName() {
                return file.getName();
            }
        }, mWidth, mHeight).onHammer();
    }

    /**
     * 启动裁剪任务
     *
     * @param uri 图片URI
     * @return 裁剪后文件
     * @throws Exception 裁剪失败异常
     */
    @NonNull
    @WorkerThread
    public File onLaunch(@Nullable Uri uri) throws Exception {
        if (uri == null) {
            throw new FileNotFoundException(HammerToos.getString(R.string.hammer_error_res));
        }
        return new CroppingEngine(new ImageHammer() {
            @Override
            InputStream initStream() throws IOException {
                return Hammer.getContext().getContentResolver().openInputStream(uri);
            }

            @Override
            String initFileName() {
                DocumentFile documentFile = DocumentFile.fromSingleUri(Hammer.getContext(), uri);
                if (documentFile == null) {
                    return HammerToos.getImageName();
                }
                String fileName = documentFile.getName();
                if (TextUtils.isEmpty(fileName)) {
                    return HammerToos.getImageName();
                }
                return fileName;
            }
        }, mWidth, mHeight).onHammer();
    }
}
