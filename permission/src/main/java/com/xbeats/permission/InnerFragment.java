package com.xbeats.permission;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.SparseArray;

import java.util.List;

/**
 * Created by XBeats on 2019/9/23
 */
public class InnerFragment extends Fragment {

    private SparseArray<CachedItem> mCacheCallbacks = new SparseArray<>();
    private int mRequestCode = 1;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    void request(List<String> granted, List<String> denied, List<String> request, Callback callback) {
        mRequestCode = (mRequestCode + 1) % 100;

        mCacheCallbacks.append(mRequestCode, new CachedItem(granted, denied, callback));
        requestPermissions(request.toArray(new String[0]), mRequestCode);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // 记录申请的权限，用于判断权限有没有请求并得到反馈过
        if (getContext() != null) {
            for (String item : permissions) {
                PermissionUtils.addPermission(getContext(), item);
            }
        }

        CachedItem item = mCacheCallbacks.get(requestCode);
        if (item == null) {
            return;
        }

        for (int i = 0; i < permissions.length; ++i) {
            if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                item.granted.add(permissions[i]);
            } else {
                item.denied.add(permissions[i]);
            }
        }

        mCacheCallbacks.remove(requestCode);
        item.mCallback.onResult(item.granted.toArray(new String[0]), item.denied.toArray(new String[0]));
    }

    private static class CachedItem {
        private List<String> granted;
        private List<String> denied;
        private Callback mCallback;

        private CachedItem(List<String> granted, List<String> denied, Callback callback) {
            this.granted = granted;
            this.denied = denied;
            mCallback = callback;
        }
    }
}
