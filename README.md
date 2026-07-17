# komari Android WebView

一个轻量的 Android WebView 客户端，用于访问自托管的 [Komari](https://github.com/komari-monitor/komari) 服务器监控面板。

## 功能

- 首次启动填写 Komari 主控地址
- 保存地址、登录 Cookie 和 WebView 状态
- 支持 HTTPS 与自托管 HTTP 地址
- 首页、刷新、修改地址和系统返回导航
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

生成的 APK 位于 `app/build/outputs/apk/debug/`。

## 安全说明

应用不会收集或转发面板数据。主控地址保存在本机 SharedPreferences 中。为兼容未配置 TLS 的自托管面板，应用允许显式 HTTP 地址，但强烈建议使用有效 HTTPS 证书；证书校验失败时应用会终止加载。

GitHub Actions 发布的 APK 使用 Android 调试签名，适合个人侧载使用，不适合提交到应用商店。

## 名称与图标

Komari 名称及应用图标来源于 [komari-monitor/komari-web](https://github.com/komari-monitor/komari-web)。Komari 原项目采用 MIT License。本仓库不是 Komari 官方 Android 客户端。

## License

[MIT](LICENSE)
