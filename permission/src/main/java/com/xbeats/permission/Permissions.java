package com.xbeats.permission;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by XBeats on 2019/9/23
 */
public class Permissions {

    private static final String TAG = Permissions.class.getSimpleName();

    public static Permissions with(@NonNull Context context) {
        return new Permissions(context);
    }

    private Context mContext;

    private Permissions(@NonNull Context context) {
        mContext = context;
    }

    private Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.obj instanceof PermissionRequest) {
                final PermissionRequest permissionRequest = (PermissionRequest) msg.obj;
                final FragmentActivity activity = PermissionUtils.getBaseActivity(permissionRequest.mContext);
                if (activity == null || activity.isFinishing()) {
                    return;
                }

                // 1) 收集没有权限的申请
                List<String> permissionList = new LinkedList<>();
                List<String> noPermissionList = new LinkedList<>();
                for (String item : permissionRequest.permissions) {
                    if (ContextCompat.checkSelfPermission(activity, item) == PackageManager.PERMISSION_GRANTED) {
                        permissionList.add(item);
                    } else {
                        noPermissionList.add(item);
                    }
                }

                // 1.1) 权限全部通过，直接返回
                if (noPermissionList.isEmpty()) {
                    PermissionLogger.log("权限全部通过啦 -->");
                    permissionRequest.mCallback.onResult(permissionList.toArray(new String[0]), noPermissionList.toArray(new String[0]));
                    return;
                }

                // 2. 收集需要解释权限意图的申请
                //    第一次请求权限也需要解释
                List<String> needRequest = new LinkedList<>();
                Iterator<String> iterator = noPermissionList.iterator();
                while (iterator.hasNext()) {
                    String item = iterator.next();
                    if (ActivityCompat.shouldShowRequestPermissionRationale(activity, item)) {
                        PermissionLogger.log("提醒用户开启权限：" + item + " -->");
                        needRequest.add(item);
                        iterator.remove();
                    } else if (PermissionUtils.noPermissionRecord(activity, item)) { // 用户没有请求过权限，则再次提示用户
                        PermissionLogger.log("用户还没申请过权限：" + item + " -->");
                        needRequest.add(item);
                        iterator.remove();
                    }
                }

                // 2.1) 用户选择了“不再提醒”，不能再进行解释
                if (needRequest.isEmpty()) {
                    PermissionLogger.log("用户选择了“不再提醒”，不能再进行解释 -->");
                    permissionRequest.mCallback.onResult(permissionList.toArray(new String[0]), noPermissionList.toArray(new String[0]));
                    return;
                }

                // 3) 查看用户拦截器 (Activity自定义 > Application自定义 > 全局默认)
                // 3.1) 某界面自定义拦截器
                final Chain chain = new Chain(activity, permissionList, noPermissionList, needRequest, permissionRequest.mCallback);
                if (permissionRequest.mPermissionInterceptor != null) {
                    permissionRequest.mPermissionInterceptor.intercept(chain, permissionList.toArray(new String[0]), noPermissionList.toArray(new String[0]), needRequest);
                    return;
                }

                // 3.2) 反射查找全局拦截器
                Class<? extends PermissionInterceptor> interceptorClass = PermissionConfig.instance(activity).getInterceptorClass();
                if (interceptorClass != null) {
                    try {
                        PermissionInterceptor interceptor = interceptorClass.newInstance();
                        interceptor.intercept(chain, permissionList.toArray(new String[0]), noPermissionList.toArray(new String[0]), needRequest);
                    } catch (Exception e) {
                        e.printStackTrace();
                        PermissionLogger.log("反射全局拦截器失败 -->");
                    }
                    return;
                }

                // 3.3) 全局默认拦截器
                if (PermissionConfig.instance(activity).isEnabledDefaultInterceptor()) {
                    new DefaultApplicationInterceptor().intercept(chain, permissionList.toArray(new String[0]), noPermissionList.toArray(new String[0]), needRequest);
                    return;
                }

                // 3.4) 无拦截器，直接发起权限请求
                chain.request();
            }
        }
    };

    public PermissionRequest load(final String... permissions) {
        PermissionRequest request = new PermissionRequest(mContext, permissions);
        Message message = mHandler.obtainMessage();
        message.obj = request;
        message.sendToTarget();
        return request;
    }

    public static class PermissionRequest {
        private Context mContext;
        private String[] permissions;
        private Callback mCallback;
        private PermissionInterceptor mPermissionInterceptor;

        PermissionRequest(Context context, String[] permissions) {
            mContext = context;
            this.permissions = permissions;
        }

        public PermissionRequest callback(Callback callback) {
            mCallback = callback;
            return this;
        }

        public PermissionRequest setInterceptor(PermissionInterceptor permissionInterceptor) {
            mPermissionInterceptor = permissionInterceptor;
            return this;
        }
    }

    public static class Chain {
        private FragmentActivity mActivity;
        private List<String> granted;
        private List<String> denied;
        private List<String> request;
        private Callback callback;

        private Chain(@NonNull FragmentActivity mActivity, List<String> granted, List<String> denied, List<String> request, Callback callback) {
            this.mActivity = mActivity;
            this.granted = granted;
            this.denied = denied;
            this.request = request;
            this.callback = callback;
        }

        @NonNull
        public FragmentActivity getActivity() {
            return mActivity;
        }

        public final void request() {
            FragmentManager fragmentManager = mActivity.getSupportFragmentManager();
            InnerFragment permissionsFragment = (InnerFragment) fragmentManager.findFragmentByTag(TAG);
            if (permissionsFragment == null) {
                permissionsFragment = new InnerFragment();
                fragmentManager
                        .beginTransaction()
                        .add(permissionsFragment, TAG)
                        .commitNowAllowingStateLoss();
            }

            permissionsFragment.request(granted, denied, request, callback);
        }

        public final void ignored() {
            LinkedList<String> denied = new LinkedList<>(this.denied);
            denied.addAll(request);
            callback.onResult(granted.toArray(new String[0]), denied.toArray(new String[0]));
        }
    }
}
