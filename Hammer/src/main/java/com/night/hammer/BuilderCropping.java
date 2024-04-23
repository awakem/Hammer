package com.night.hammer;

import android.net.Uri;
import android.text.TextUtils;

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

    public BuilderCropping setImageSize(int width, int height) {
        this.mWidth = width;
        this.mHeight = height;
        return this;
    }
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
        }, mWidth, mHeight).openTask();
    }
    @NonNull
    @WorkerThread
    public File onLaunch(@Nullable Uri uri) throws Exception {
        if (uri == null) {
            throw new FileNotFoundException(HammerToos.getString(R.string.hammer_error_res));
        }
        return new CroppingEngine(new ImageHammer() {
            @Override
            InputStream initStream() throws IOException {
                return HammerHelp.getContext().getContentResolver().openInputStream(uri);
            }

            @Override
            String initFileName() {
                DocumentFile documentFile = DocumentFile.fromSingleUri(HammerHelp.getContext(), uri);
                if (documentFile == null) {
                    return HammerToos.getImageName();
                }
                String fileName = documentFile.getName();
                if (TextUtils.isEmpty(fileName)) {
                    return HammerToos.getImageName();
                }
                return fileName;
            }
        }, mWidth, mHeight).openTask();
    }
}
