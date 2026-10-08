package com.family.pomostudy;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * 闹钟广播接收器：AlarmManager 到点后触发，拉起全屏响铃的 RingActivity。
 */
public class AlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String title = intent.getStringExtra("title");
        if (title == null) title = "时间到";
        String message = intent.getStringExtra("message");
        if (message == null) message = "";
        int id = intent.getIntExtra("id", AlarmPlugin.ID_FOCUS);
        Intent ring = new Intent(context, RingActivity.class);
        ring.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        ring.putExtra("title", title);
        ring.putExtra("message", message);
        ring.putExtra("id", id);
        context.startActivity(ring);
    }
}
