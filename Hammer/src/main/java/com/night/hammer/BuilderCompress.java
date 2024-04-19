package com.night.hammer;

import androidx.annotation.IntRange;
import androidx.annotation.Nullable;

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

    public BuilderCompress setImageMaxSize(int width, int height) {
        this.maxWidth = width;
        this.maxHeight = height;
        return this;
    }

    public BuilderCompress setImageMaxLength(int length) {
        this.maxLength = length;
        return this;
    }

    public BuilderCompress setStep(@IntRange(from = 1, to = 100) int step) {
        this.stepSize = step;
        return this;
    }

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
        }, maxWidth, maxHeight, maxLength, stepSize).openTask();
    }
}
