package com.example.admin.jprod;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

/**
 * Created by gpsdesk on 28/3/18.
 */

public class Permissions {
    public static final int My_PERMISSIONS_REQUEST_CALL = 100;
    Activity activity;
    public Permissions(Activity activity){
        this.activity = activity;
    }
    public boolean checkCallPermission() {
        if (ContextCompat.checkSelfPermission(activity,
                android.Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(activity,
                    android.Manifest.permission.CALL_PHONE)) {
                ActivityCompat.requestPermissions(activity,
                        new String[]{android.Manifest.permission.CALL_PHONE},
                        My_PERMISSIONS_REQUEST_CALL);
            } else {
                ActivityCompat.requestPermissions(activity,
                        new String[]{android.Manifest.permission.CALL_PHONE},
                        My_PERMISSIONS_REQUEST_CALL);
            }
            return false;
        } else {
            return true;
        }
    }
}
