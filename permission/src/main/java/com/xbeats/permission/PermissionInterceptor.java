package com.xbeats.permission;

import android.support.annotation.NonNull;

import java.util.List;

/**
 * Created by XBeats on 2019/9/23
 */
public interface PermissionInterceptor {
    void intercept(@NonNull Permissions.Chain chain, @NonNull String[] granted, @NonNull String[] denied, @NonNull List<String> request);
}
