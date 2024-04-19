package com.night.hammer;

public class Hammer {
    public static BuilderCompress withCompress() {
        return new BuilderCompress();
    }

    public static BuilderCropping withCropping() {
        return new BuilderCropping();
    }
}
