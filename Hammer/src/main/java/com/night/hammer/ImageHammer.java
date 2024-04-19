package com.night.hammer;

import androidx.annotation.NonNull;

import java.io.IOException;
import java.io.InputStream;

abstract public class ImageHammer implements IHammerProxy {
    private InputStream mInputStream = null;

    @NonNull
    @Override
    public InputStream open() throws IOException {
        if (mInputStream != null) {
            mInputStream.close();
        }
        mInputStream = initStream();
        return mInputStream;
    }

    @Override
    public String getImageName() {
        return initFileName();
    }

    @Override
    public void close() throws IOException {
        if (mInputStream != null) {
            mInputStream.close();
        }
    }
    /**
     * 初始化输入流
     * @return 输入流
     * @throws IOException IOException
     */
    abstract InputStream initStream() throws IOException;

    /**
     * 获取文件名称
     * @return 文件名称
     */
    abstract String initFileName();
}
