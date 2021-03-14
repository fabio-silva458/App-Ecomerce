package com.example.admin.jprod;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.WakefulBroadcastReceiver;

import com.google.android.gms.gcm.GcmListenerService;

public class GcmBroadcastReceiver extends WakefulBroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        ComponentName comp = new ComponentName(context.getPackageName(),
                GCMNotificationIntentService.class.getName());
        startWakefulService(context, (intent.setComponent(comp)));
        setResultCode(Activity.RESULT_OK);
    }
}

//public class GcmBroadcastReceiver extends GcmListenerService {
//
//    @Override
//    public void onMessageReceived(String from, Bundle data) {
//        Intent intent = new Intent(this, GCMNotificationIntentService.class);
//        intent.putExtras(data);
//        startService(intent);
//    }
//}