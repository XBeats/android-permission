package com.xbeats.permission;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;

/**
 * Created by XBeats on 2019/9/23
 */
class PermissionUtils {

    @Nullable
    static FragmentActivity getBaseActivity(Context context) {
        if (context == null) {
            return null;
        } else if (context instanceof FragmentActivity) {
            return (FragmentActivity) context;
        } else if (context instanceof ContextWrapper) {
            return getBaseActivity(((ContextWrapper) context).getBaseContext());
        }
        return null;
    }

    private static final String APP_PERMISSION_RECORD_KEY = "APP_PERMISSION_RECORD_KEY";
    private static final String APP_PERMISSION_PREFIX = "android.permission.";

    static boolean noPermissionRecord(Context context, String permission) {
        permission = permission.replace(APP_PERMISSION_PREFIX, "");
        String str = PreferenceManager.getDefaultSharedPreferences(context).getString(APP_PERMISSION_RECORD_KEY, "");
        return str == null || !str.contains(permission);
    }

    /**
     * 记录初始化的权限，（“shouldShowRequestPermissionRationale()”方法在第一次请求时返回false）
     *
     * @param context
     * @param permission
     */
    static void addPermission(Context context, String permission) {
        permission = permission.replace(APP_PERMISSION_PREFIX, "");
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        String str = sharedPreferences.getString(APP_PERMISSION_RECORD_KEY, "");
        if (str == null || "".equals(str)) {
            sharedPreferences.edit().putString(APP_PERMISSION_RECORD_KEY, permission).apply();
        } else if (!str.contains(permission)) {
            sharedPreferences.edit().putString(APP_PERMISSION_RECORD_KEY, str + "," + permission).apply();
        }
    }

}
