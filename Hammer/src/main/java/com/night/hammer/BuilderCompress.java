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

public class BuilderCompress {
    private int maxWidth = -1;
    private int maxHeight = -1;
    private long maxLength = 102400L;
    private int stepSize = 20;

    /**
     * 设置最大尺寸
     *
     * @param width  宽
     * @param height 高
     * @return {@link BuilderCompress}
     */
    public BuilderCompress setImageMaxSize(int width, int height) {
        this.maxWidth = width;
        this.maxHeight = height;
        return this;
    }

    /**
     * 设置最大体积
     *
     * @param length 最大体积-字节
     * @return {@link BuilderCompress}
     */
    public BuilderCompress setImageMaxLength(long length) {
        this.maxLength = length;
        return this;
    }

    /**
     * 设置压缩梯度
     *
     * @param step 压缩梯度 1-100，数值越小，越接近最大体积。压缩时间越长
     * @return {@link BuilderCompress}
     */
    public BuilderCompress setStep(@IntRange(from = 1, to = 100) int step) {
        this.stepSize = step;
        return this;
    }

    /**
     * 启动任务
     *
     * @param uri 图片URI
     * @return 压缩后文件
     * @throws Exception 压缩失败异常
     */
    @NonNull
    @WorkerThread
    public File onLaunch(@Nullable Uri uri) throws Exception {
        if (uri == null) {
            throw new FileNotFoundException(HammerToos.getString(R.string.hammer_error_res));
        }
        return new CompressEngine(new ImageHammer() {
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
        }, maxWidth, maxHeight, maxLength, stepSize).onHammer();
    }

    /**
     * 启动任务
     *
     * @param file 图片文件
     * @return 压缩后文件
     * @throws Exception 压缩失败异常
     */
    @NonNull
    @WorkerThread
    public File onLaunch(@Nullable File file) throws Exception {
        if (file == null || !file.exists() || !file.isFile()) {
            throw new FileNotFoundException(HammerToos.getString(R.string.hammer_error_res));
        }
        return new CompressEngine(new ImageHammer() {
            @Override
            InputStream initStream() throws IOException {
                return new FileInputStream(file);
            }

            @Override
            String initFileName() {
                return file.getName();
            }
        }, maxWidth, maxHeight, maxLength, stepSize).onHammer();
    }
}
