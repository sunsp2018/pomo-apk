#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
把原生闹钟代码注入到 Capacitor 自动生成的 android/ 工程里（幂等，可重复执行）。
CI 流程：npx cap add android -> npx cap sync android -> python3 native/inject-android.py
"""
import os
import sys

HERE = os.path.dirname(os.path.abspath(__file__))
NATIVE = os.path.normpath(HERE)  # 本脚本与 .kt / .xml 同目录
ANDROID_DIR = os.path.normpath(os.environ.get("ANDROID_DIR", "android"))
PKG = "com/family/pomostudy"
JAVA_DIR = os.path.join(ANDROID_DIR, "app/src/main/java", PKG)
RES_LAYOUT = os.path.join(ANDROID_DIR, "app/src/main/res/layout")
MANIFEST = os.path.join(ANDROID_DIR, "app/src/main/AndroidManifest.xml")
MAIN = os.path.join(JAVA_DIR, "MainActivity.kt")
if not os.path.exists(MAIN):
    MAIN = os.path.join(JAVA_DIR, "MainActivity.java")


def copy(src, dst):
    with open(src, "r", encoding="utf-8") as f:
        data = f.read()
    os.makedirs(os.path.dirname(dst), exist_ok=True)
    with open(dst, "w", encoding="utf-8") as f:
        f.write(data)
    print("copied ->", dst)


def patch_manifest():
    if not os.path.exists(MANIFEST):
        print("ERROR: manifest not found:", MANIFEST)
        sys.exit(1)
    with open(MANIFEST, "r", encoding="utf-8") as f:
        m = f.read()

    perms = (
        '    <uses-permission android:name="android.permission.SCHEDULE_EXACT_ALARM" />\n'
        '    <uses-permission android:name="android.permission.WAKE_LOCK" />\n'
    )
    if "android.permission.SCHEDULE_EXACT_ALARM" not in m:
        m = m.replace("<application", perms + "\n<application", 1)

    comps = (
        '        <receiver android:name=".AlarmReceiver" android:enabled="true" android:exported="false" />\n'
        '        <activity android:name=".RingActivity" android:exported="false" '
        'android:theme="@style/AppTheme.NoActionBar" android:showOnLockScreen="true" />\n'
    )
    if "AlarmReceiver" not in m:
        m = m.replace("</application>", comps + "    </application>", 1)

    with open(MANIFEST, "w", encoding="utf-8") as f:
        f.write(m)
    print("patched AndroidManifest.xml")


def patch_main():
    if not os.path.exists(MAIN):
        print("ERROR: MainActivity.kt not found:", MAIN)
        sys.exit(1)
    with open(MAIN, "r", encoding="utf-8") as f:
        s = f.read()
    if "registerPlugin(AlarmPlugin" not in s:
        # 按 MainActivity 的语言选择注册写法：Java 用 AlarmPlugin.class，Kotlin 用 AlarmPlugin::class.java
        snippet = "AlarmPlugin::class.java" if MAIN.endswith(".kt") else "AlarmPlugin.class"
        s = s.replace(
            "super.onCreate(savedInstanceState)",
            "super.onCreate(savedInstanceState)\n        this.bridge.registerPlugin(" + snippet + ")",
            1,
        )
        with open(MAIN, "w", encoding="utf-8") as f:
            f.write(s)
        print("patched " + os.path.basename(MAIN))
    else:
        print(os.path.basename(MAIN) + " already patched")


def main():
    if not os.path.isdir(JAVA_DIR):
        os.makedirs(JAVA_DIR, exist_ok=True)
    for f in ["AlarmPlugin.java", "AlarmReceiver.java", "RingActivity.java"]:
        copy(os.path.join(NATIVE, f), os.path.join(JAVA_DIR, f))
    copy(os.path.join(NATIVE, "activity_ring.xml"), os.path.join(RES_LAYOUT, "activity_ring.xml"))
    patch_manifest()
    patch_main()
    print("INJECT_DONE")


if __name__ == "__main__":
    main()
