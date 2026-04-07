# IjkPlayer 网络电台播放器

一个基于 Bilibili IjkPlayer 的 Android 网络电台应用。

## 功能特性

- 电台列表展示（名称 + URL）
- 添加/删除电台
- 播放/暂停网络流媒体
- 音量调节
- 自动恢复上次播放的电台
- 播放状态提示（缓冲、播放、暂停、错误）

## 开发环境

- Android Studio Hedgehog | 2023.1.1+
- minSdk = 21, targetSdk = 33
- Kotlin 1.9+
- Gradle 8.0+

## 项目结构

```
app/src/main/java/com/example/ijkradio/
├── MainActivity.kt               # 主界面
├── data/
│   ├── Station.kt              # 电台数据类
│   └── StationStorage.kt        # 电台存储管理
├── player/
│   ├── PlaybackState.kt         # 播放状态密封类
│   └── IjkPlayerManager.kt      # IjkPlayer 单例封装
└── ui/
    └── StationAdapter.kt         # RecyclerView 适配器
```

## 编译运行

1. 用 Android Studio 打开项目
2. 等待 Gradle 同步完成
3. 连接 Android 设备或启动模拟器
4. 点击 Run 按钮编译运行

## 注意事项

- 需要网络权限（已在 AndroidManifest.xml 中配置）
- Android 9+ 需要允许 HTTP 明文传输（已配置 usesCleartextTraffic）
- 播放完成后记得释放播放器资源（在 onDestroy 中调用 release()）
