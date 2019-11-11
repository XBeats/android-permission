package com.xbeats.permission;

import android.support.annotation.NonNull;

import java.util.List;

/**
 * Created by XBeats on 2019/9/23
 */
public interface Callback {
    void onResult(@NonNull String[] granted, @NonNull String[] denied);
}
