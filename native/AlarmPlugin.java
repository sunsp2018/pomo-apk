package com.family.pomostudy;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;

/**
 * 原生闹钟插件：用系统 AlarmManager 预约「精确闹钟」，即使屏幕熄灭 / APP 在后台也能在到点时
 * 唤醒屏幕并播放系统闹钟音（见 AlarmReceiver -> RingActivity）。
 * 网页端在「开始专注」时调用 scheduleAlarm，在暂停 / 结束时调用 cancelAlarm。
 */
@CapacitorPlugin(name = "Alarm")
public class AlarmPlugin extends Plugin {

    public static final String ACTION = "com.family.pomostudy.ALARM";
    public static final int ID_FOCUS = 1;
    public static final int ID_REST = 2;

    @PluginMethod
    public void scheduleAlarm(PluginCall call) {
        long triggerInMs = call.getLong("triggerInMs", 0L);
        int id = call.getInt("id", ID_FOCUS);
        String title = call.getString("title", "时间到");
        String message = call.getString("message", "");
        Context context = getContext();
        AlarmManager alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.setAction(ACTION);
        intent.putExtra("title", title);
        intent.putExtra("message", message);
        intent.putExtra("id", id);
        PendingIntent pi = PendingIntent.getBroadcast(context, id, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        long triggerAt = System.currentTimeMillis() + triggerInMs;
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmMgr.canScheduleExactAlarms()) {
                    alarmMgr.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pi);
                } else {
                    alarmMgr.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pi);
                }
            } else {
                alarmMgr.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pi);
            }
        } catch (Exception e) {
            Log.e("AlarmPlugin", "scheduleAlarm failed", e);
        }
        call.resolve();
    }

    @PluginMethod
    public void cancelAlarm(PluginCall call) {
        int id = call.getInt("id", ID_FOCUS);
        Context context = getContext();
        AlarmManager alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.setAction(ACTION);
        intent.putExtra("id", id);
        PendingIntent pi = PendingIntent.getBroadcast(context, id, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        try {
            alarmMgr.cancel(pi);
        } catch (Exception e) {
            Log.e("AlarmPlugin", "cancelAlarm failed", e);
        }
        call.resolve();
    }
}
