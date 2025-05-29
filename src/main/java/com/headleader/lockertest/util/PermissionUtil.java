package com.headleader.lockertest.util;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

public class PermissionUtil {

    ActivityCompat.OnRequestPermissionsResultCallback callback = new ActivityCompat.OnRequestPermissionsResultCallback(){
        @Override
        public void onRequestPermissionsResult( int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults ) {
            int result = requestCode;
        }
    };
    public static void AcquirePermission(Activity activity)
    {
        String[] permissions = {
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
        };
        List<String> permissionList = new ArrayList<>();
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED) {
                permissionList.add(permission);
            }
        }
        if (permissionList.isEmpty()) return;
        permissions = permissionList.toArray(new String[permissionList.size()]);
        ActivityCompat.requestPermissions(activity, permissions,101);
        while (!permissionList.isEmpty()){
            if (ContextCompat.checkSelfPermission(activity, permissionList.get(0)) == PackageManager.PERMISSION_GRANTED) {
                permissionList.remove(permissionList.get(0));
            }
        };
    }
}
