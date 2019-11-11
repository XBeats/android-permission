package com.xbeats.permission;

import android.app.Application;
import android.content.ComponentCallbacks;
import android.content.Context;
import android.content.res.Configuration;
import android.support.annotation.NonNull;

/**
 * Created by XBeats on 2019/9/23
 */
public class PermissionConfig {

    private static PermissionConfig sPermissionConfig;
    private boolean mDefaultInterceptor = true;
    @NonNull
    private final InnerComponentCallbacks mInnerComponentCallbacks = new InnerComponentCallbacks();

    public static PermissionConfig instance(Context context) {
        if (sPermissionConfig == null) {
            sPermissionConfig = new PermissionConfig();
            // 注册实例，让application保存该实例，防止被内存回收
            if (context.getApplicationContext() instanceof Application) {
                Application application = (Application) context.getApplicationContext();
                application.registerComponentCallbacks(sPermissionConfig.mInnerComponentCallbacks);
            }
        }
        return sPermissionConfig;
    }

    /**
     * 懒加载模式
     *
     * @param interceptorClass
     */
    public void setInterceptorClass(Class<? extends PermissionInterceptor> interceptorClass) {
        mInnerComponentCallbacks.mInterceptorClass = interceptorClass;
    }

    /**
     * 是否使用库中默认拦截器
     * @param enable
     */
    public void enableDefaultInterceptor(boolean enable) {
        mDefaultInterceptor = enable;
    }

    public boolean isEnabledDefaultInterceptor() {
        return mDefaultInterceptor;
    }

    public Class<? extends PermissionInterceptor> getInterceptorClass() {
        return mInnerComponentCallbacks.mInterceptorClass;
    }

    private class InnerComponentCallbacks implements ComponentCallbacks {
        Class<? extends PermissionInterceptor> mInterceptorClass;

        @Override
        public void onConfigurationChanged(Configuration newConfig) {

        }

        @Override
        public void onLowMemory() {

        }
    }
}
