# 番茄学习管家 · 安卓 APP 构建说明（给 HEHE）

这个文件夹用来把「番茄学习管家」打包成一个**真正的安卓 APP（APK）**。
和普通网页版最大的区别：**APP 版带系统级闹钟**——即使平板锁屏、熄屏，到时间也会点亮屏幕、用系统闹钟音量响铃，不会漏提醒。

> 网页版（在 Via 浏览器里用）做不到锁屏响铃；这个 APP 版可以。两者数据各自独立。

---

## 你需要做的（只需一次，全程鼠标点）

### 1. 注册一个 GitHub 账号（免费）
打开 https://github.com ，用邮箱注册即可。如果已有账号直接登录。

### 2. 新建一个仓库（Repository）
- 点右上角 **＋ → New repository**
- Repository name 随便起，例如 `pomo-apk`
- 选 **Public**（公开，免费；里面没有隐私数据，只是代码）
- 不要勾选 "Add a README"（我们自带）
- 点 **Create repository**

### 3. 把本文件夹的内容上传进去
- 进到刚建好的仓库，点 **Add file → Upload files**
- 把 **pomo-apk 这个文件夹里的所有内容**（不是整个文件夹本身）拖进去：
  - `package.json`
  - `capacitor.config.json`
  - `www/`（整个文件夹）
  - `native/`（整个文件夹）
  - `.github/`（整个文件夹）
- 拖完拉到最下面，写个说明（如 "init"），点 **Commit changes**

### 4. 让它自动打包
- 进仓库顶部 **Actions** 标签
- 左侧选 **Build Android APK**
- 点 **Run workflow → Run workflow**（如果是推到 main 分支触发的，会自动跑）
- 等大约 3–6 分钟，状态变绿色 ✅ 即成功

### 5. 下载 APK 并安装到华为平板
- 进 **Actions → 刚才那次构建 → Artifacts → app-debug-apk**，下载
- 把 `app-debug.apk` 传到平板（微信/数据线都行），点它安装
- 华为会提示「允许安装未知应用」：点**设置 → 允许**即可
- 装好后桌面出现「番茄学习管家」图标，打开即用

---

## 使用要点
- **闹钟声**用的是平板系统自带的默认闹钟铃声（最响那一档），无需额外音频文件。
- 到点会**全屏弹出红屏 + 「我知道了」按钮**，点一下停止。锁屏也能弹出来。
- APP 内的数据（专注记录、金币等）存在 APP 自己的空间里，和之前 Via 浏览器里的数据**不互通**。
- 想从 Via 那边把历史迁过来：在 Via 版「设置 → 导出数据」存成 JSON，再在 APP 版「设置 → 导入数据」选这个文件。

---

## 如果只是想在华为上用、不想打包
直接用网页版也行：把 `番茄学习管家.html` 用 Via 浏览器打开，用键盘上的 🎙️ 麦克风输入，专注时保持屏幕常亮（设置里默认开），提醒靠蜂鸣+震动+红条。缺点是**锁屏/退到后台时可能漏响**——要彻底解决才需要上面的 APP。

---

## 如果打包失败
构建过程在 GitHub 云端完成，本地不用装任何东西。如果 Actions 变红 ❌：
1. 点进那次失败的构建，看 **Build debug APK** 这一步的红色日志
2. 把报错文字发给我，我来修（多数情况是某行配置，改完重跑即可）

---

## 文件夹结构（供参考，不用改）
```
pomo-apk/
├─ package.json             # 依赖声明（Capacitor 安卓壳）
├─ capacitor.config.json    # APP 包名/名称
├─ www/                     # 网页版（就是番茄学习管家.html）
├─ native/                  # 原生闹钟代码（Java）+ 注入脚本
│  ├─ AlarmPlugin.java       #   预约/取消系统闹钟
│  ├─ AlarmReceiver.java     #   闹钟到点触发
│  ├─ RingActivity.java      #   全屏响铃界面
│  ├─ activity_ring.xml       #   响铃界面布局
│  └─ inject-android.py       #   CI 自动把上面的代码塞进安卓工程
└─ .github/workflows/build-apk.yml  # 云端构建流程
```
