# komari Android WebView

一个轻量的 Android WebView 客户端，用于访问自托管的 [Komari](https://github.com/komari-monitor/komari) 服务器监控面板。

## 功能

- 首次启动填写 Komari 主控地址
- 保存地址、登录 Cookie 和 WebView 状态
- 支持 HTTPS 与自托管 HTTP 地址
- 首页、刷新、修改地址和系统返回导航
- Android 自适应桌面图标，兼容圆形和圆角方形启动器
- 页面导航历史在 Activity 重建后恢复，输入时避让软键盘
- 文件上传、下载和外部协议跳转
- 严格拒绝无效 HTTPS 证书
- GitHub Actions 自动构建 APK

## 使用

从 [Releases](https://github.com/ceigt/komari-webview-android/releases) 下载 APK。首次打开后输入完整主控地址，例如：

```text
https://monitor.example.com
```

如果省略协议，应用默认添加 `https://`。

## 构建

项目要求 JDK 17、Android SDK 35 和 Gradle 8.9：

```bash
gradle :app:assembleDebug
```

本地无签名材料时建议构建 Debug APK，产物位于 `app/build/outputs/apk/debug/`。正式 Release 由 GitHub Actions 使用仓库 Secrets 构建。

Debug 版使用独立包名 `io.github.ceigt.komari.debug`，可与正式版并存。连接设备后运行 `gradle :app:connectedDebugAndroidTest` 验证页面恢复、返回导航和图标资源。

在开发分支手动运行 GitHub Actions 可生成正式签名的测试产物；只有 `main` 分支会发布 Release。APK 文件名和发布版本从构建结果读取，避免新版覆盖旧版下载文件。

## 安全说明

应用不会收集或转发面板数据。主控地址保存在本机 SharedPreferences 中。为兼容未配置 TLS 的自托管面板，应用允许显式 HTTP 地址，但强烈建议使用有效 HTTPS 证书；证书校验失败时应用会终止加载。

外部协议仅允许由主框架中的用户手势打开，禁止内嵌页面和自动跳转唤起外部应用；`intent://` 链接会移除显式组件、选择器和权限授予标记。

自适应图标沿用原角色 PNG，前景按 108 dp 画布中的 66 dp 安全区域布局，背景交给启动器裁切。原图使用 `drawable-nodpi`，避免高密度设备解码时不必要地放大位图。

## 名称与图标

Komari 名称及应用图标来源于 [komari-monitor/komari-web](https://github.com/komari-monitor/komari-web)。Komari 原项目采用 MIT License。本仓库不是 Komari 官方 Android 客户端。

## License

[MIT](LICENSE)
