package com.night.hammer;

import androidx.annotation.Nullable;

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

    public void onLaunch(@Nullable File file) throws Exception {
        if (file == null || !file.exists() || !file.isFile()) {
            throw new FileNotFoundException(HammerToos.getString(R.string.hammer_error_res));
        }
        new CroppingEngine(new ImageHammer() {
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
}
