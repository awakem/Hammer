package com.night.hammer;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.startup.Initializer;

import java.util.ArrayList;
import java.util.List;

public class HammerInitializer implements Initializer<Boolean> {

    @NonNull
    @Override
    public Boolean create(@NonNull Context context) {
        Hammer.initialization(context);
        return true;
    }

    @NonNull
    @Override
    public List<Class<? extends Initializer<?>>> dependencies() {
        return new ArrayList<>();
    }
}
