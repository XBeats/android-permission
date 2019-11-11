package com.xbeats.permission;

import android.Manifest;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by XBeats on 2019/11/11
 */
public class DefaultApplicationInterceptor implements PermissionInterceptor {

    private Map<String, String> mPermissionMap = new HashMap<>();

    {
        mPermissionMap.put(Manifest.permission.READ_CALENDAR, "日历");
        mPermissionMap.put(Manifest.permission.WRITE_CALENDAR, "日历");

        mPermissionMap.put(Manifest.permission.CAMERA, "相机");

        mPermissionMap.put(Manifest.permission.READ_CONTACTS, "联系人");
        mPermissionMap.put(Manifest.permission.WRITE_CONTACTS, "联系人");
        mPermissionMap.put(Manifest.permission.GET_ACCOUNTS, "联系人");

        mPermissionMap.put(Manifest.permission.ACCESS_FINE_LOCATION, "位置");
        mPermissionMap.put(Manifest.permission.ACCESS_COARSE_LOCATION, "位置");

        mPermissionMap.put(Manifest.permission.RECORD_AUDIO, "麦克风");

        mPermissionMap.put(Manifest.permission.READ_PHONE_STATE, "设备信息");
        mPermissionMap.put(Manifest.permission.CALL_PHONE, "设备信息");
        mPermissionMap.put(Manifest.permission.WRITE_CALL_LOG, "设备信息");
        mPermissionMap.put(Manifest.permission.ADD_VOICEMAIL, "设备信息");
        mPermissionMap.put(Manifest.permission.USE_SIP, "设备信息");
        mPermissionMap.put(Manifest.permission.PROCESS_OUTGOING_CALLS, "设备信息");

        mPermissionMap.put(Manifest.permission.BODY_SENSORS, "传感器");

        mPermissionMap.put(Manifest.permission.SEND_SMS, "短信");
        mPermissionMap.put(Manifest.permission.RECEIVE_SMS, "短信");
        mPermissionMap.put(Manifest.permission.READ_SMS, "短信");
        mPermissionMap.put(Manifest.permission.RECEIVE_WAP_PUSH, "短信");
        mPermissionMap.put(Manifest.permission.RECEIVE_MMS, "短信");

        mPermissionMap.put(Manifest.permission.READ_EXTERNAL_STORAGE, "读取");
        mPermissionMap.put(Manifest.permission.WRITE_EXTERNAL_STORAGE, "读取");
    }

    @Override
    public void intercept(@NonNull final Permissions.Chain chain, @NonNull String[] granted, @NonNull String[] denied, @NonNull List<String> request) {
        // 请求权限前解释权限
        StringBuilder stringBuilder = new StringBuilder();
        String title;
        for (String item : request) {
            title = mPermissionMap.get(item);
            if (!TextUtils.isEmpty(title)) {
                stringBuilder.append(title);
            } else {
                stringBuilder.append(item.replace("android.permission.", ""));
            }
            stringBuilder.append(",");
        }
        if (stringBuilder.length() > 0) {
            stringBuilder.delete(stringBuilder.length() - 1, stringBuilder.length());
            stringBuilder.insert(0, "请求权限：");
        }

        // show dialog
        new AlertDialog.Builder(chain.getActivity())
                .setMessage(stringBuilder)
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        chain.ignored();
                    }
                })
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        chain.request();
                    }
                }).show();
    }
}
