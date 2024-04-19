package com.night.hammer;

import androidx.annotation.NonNull;

import java.io.IOException;
import java.io.InputStream;

public interface IHammerProxy {

    /**
     * 获取输入流
     *
     * @return 输入流
     * @throws IOException IOException
     */
    @NonNull
    InputStream open() throws IOException;

    /**
     * 获取文件名称
     *
     * @return 文件名称
     */
    String getImageName();

    /**
     * 关闭输入流
     *
     * @throws IOException IOException
     */
    void close() throws IOException;
}
