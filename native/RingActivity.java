package com.family.pomostudy;

import android.app.KeyguardManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

/**
 * 全屏响铃界面：锁屏也能显示、点亮屏幕、用系统闹钟音量循环播放铃声，直到点「我知道了」。
 */
public class RingActivity extends AppCompatActivity {
    private Ringtone ringtone = null;
    private PowerManager.WakeLock wakeLock = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 锁屏可见 + 点亮屏幕
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true);
            setTurnScreenOn(true);
        } else {
            getWindow().addFlags(
                android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
                android.view.WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON |
                android.view.WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        }

        // 尝试自动解锁键盘锁（无需密码时直接亮屏）
        KeyguardManager km = (KeyguardManager) getSystemService(KEYGUARD_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            km.requestDismissKeyguard(this, null);
        }

        setContentView(R.layout.activity_ring);

        String title = getIntent().getStringExtra("title");
        if (title == null) title = "时间到";
        String message = getIntent().getStringExtra("message");
        if (message == null) message = "";
        TextView t = findViewById(R.id.ring_title);
        if (t != null) t.setText(title);
        TextView m = findViewById(R.id.ring_message);
        if (m != null) m.setText(message);

        // 唤醒 CPU，保证响铃期间不休眠
        PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
        wakeLock = pm.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, "pomo:AlarmWake");
        wakeLock.acquire();

        // 播放系统默认闹钟铃声（走闹钟音量，最响），循环直到手动停止
        Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        if (uri == null) uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
        ringtone = RingtoneManager.getRingtone(this, uri);
        if (ringtone != null) {
            ringtone.setLooping(true);
            ringtone.play();
        }

        Button stop = findViewById(R.id.ring_stop);
        if (stop != null) stop.setOnClickListener(v -> dismiss());
    }

    private void dismiss() {
        if (ringtone != null) ringtone.stop();
        if (wakeLock != null && wakeLock.isHeld()) wakeLock.release();
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (ringtone != null) ringtone.stop();
        if (wakeLock != null && wakeLock.isHeld()) wakeLock.release();
    }
}
