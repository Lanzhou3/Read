# 内置阅读字体

## 霞鹜文楷

- 字体：LXGW WenKai / 霞鹜文楷，Regular 常规字重。
- 官方版本：v1.530，字体元数据版本为 `Version 1.530; October 18, 2026`。
- 官方下载：https://github.com/lxgw/LxgwWenKai/releases/download/v1.530/LXGWWenKai-Regular.ttf
- 发布标签对应提交：`4becfb4d5cf0c92cfdb60a6e9b504bda43315325`。
- 文件：`app/src/main/assets/font/LXGWWenKai-Regular.ttf`。
- 原始大小：25,928,940 字节，约 24.73 MiB；文件未经裁剪、转换或修改。
- 调试 APK 中字体条目压缩后为 14,103,654 字节，约 13.45 MiB；这是字体资源对包体的实际贡献，另有少量代码与许可文本开销。
- SHA-256：`2d73eea94ad808999d90c0000c239a84a33b09d4bc6ab6c5b8bbf1eda8a7fc8b`，与 GitHub 官方发布资产的 digest 一致。
- 许可证：SIL OFL 1.1。官方原始版权与许可全文位于 `app/src/main/assets/licenses/lxgw-wenkai.txt`；SHA-256 为 `1a25e35da1031c6c3436fde545bb9cb5aca954e9873afe510c834b8b79bd21a0`。

字体选择器增加“内置字体”分组、实际字体示例与“开源授权”入口。“关于 → 许可协议”也包含完整版权声明和 OFL。

使用稳定引用 `builtin:lxgw-wenkai` 保存选择，直接从应用资源加载并缓存 Typeface，不依赖外部字体文件或存储权限。导出阅读样式只保存内置字体引用，不重复打包字体；导入时识别该引用，未知内置字体回退系统默认。原有本地字体导入逻辑保留。

不替换用户当前字体，用户可在阅读器“界面 → 字体排版 → 阅读字体”中选择。

## 筑紫的核查结果

常见 Fontworks / Monotype 筑紫系列属于商业字体，不是本轮可以直接随开源应用分发的字体。官方另设 App 内嵌与分发授权，因此本轮未下载或内置筑紫。

官方资料：

- https://lets.fontworks.co.jp/services
- https://aws-prod-lets.fontworks.co.jp/fontworks/specs

用户如提供具体第三方衍生字体，需单独检查该文件的来源、字形来源和分发许可，不能只依据“筑紫”名称或免费下载页面判断。

## 验证

本轮按用户要求直接实现并进行模拟器视觉确认，不发布 Release APK。

- 官方下载文件校验通过，包内字体与许可证均已检查。
- 通过实际导出流程生成的阅读样式 ZIP 为 592 字节，仅含 `readConfig.json`；其中 `textFont` 为 `builtin:lxgw-wenkai`，未重复打包字体文件。
- Android 17 / API 37 模拟器中确认字体示例、实际书页和标题使用霞鹜文楷；选择后强制停止再重启，仍回显霞鹜文楷。
- 字体授权入口能展示完整原始许可证。
- 将字体切回系统默认后，通过系统文件选择器重新导入刚才导出的 ZIP，实际书页和字体卡片恢复为霞鹜文楷；验证结束后恢复检查前的系统默认字体，移除模拟器中的临时样式文件。
- 调试构建成功；Lint 仍为 14 项既有错误，对照基线无新增错误。

### 截图

- [内置字体选择](bundled-fonts/01-picker.png)
- [选择后的回显](bundled-fonts/02-selected.png)
- [实际阅读页面](bundled-fonts/03-reading.png)
- [重启后保留选择](bundled-fonts/04-restarted.png)
- [授权说明](bundled-fonts/05-license.png)

- [样式重新导入后的字体回显](bundled-fonts/06-imported-style.png)
