package com.xbeats.permissionsample;

import android.Manifest;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.xbeats.permission.Callback;
import com.xbeats.permission.PermissionConfig;
import com.xbeats.permission.PermissionInterceptor;
import com.xbeats.permission.Permissions;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        PermissionConfig.instance(this).enableDefaultInterceptor(false);

        PermissionConfig.instance(this).setInterceptorClass(CustomPermissionInterceptor.class);

        findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Permissions.with(v.getContext()).load(Manifest.permission.READ_CONTACTS).callback(new Callback() {
                    @Override
                    public void onResult(@NonNull String[] granted, @NonNull String[] denied) {
                        Toast.makeText(MainActivity.this, denied.length > 0 ? "部分权限没有赋予" : "成功获得所有权限", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private static class CustomPermissionInterceptor implements PermissionInterceptor {

        @Override
        public void intercept(@NonNull Permissions.Chain chain, @NonNull String[] granted, @NonNull String[] denied, @NonNull List<String> request) {
            chain.request();
        }
    }
}
