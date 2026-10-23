# 阅读设置界面重设计

## 调整范围

阅读器的“界面”入口改为三个分区：**字体排版、页面布局、主题背景**。减少同屏竞争，设置面板上方保留实际书页，调整即时生效。

- **字体排版**：独立字体卡片；保留字重、缩进、简繁转换；字号、字距、行距、段距统一为数值标签、滑杆和 48dp 加减按钮。
- **字体选择**：直接展示系统默认、衬线、等宽三种字体及中英文示例；本地字体另列，“导入字体”明确打开文件夹选择。关闭子面板后回到原有设置。
- **页面边距**：正文、页眉、页脚分区切换，每组保留独立的上、下、左、右边距；示意图和数值同步更新。示意图辅助理解，实际书页为精确效果。页眉、页脚保留分隔线开关。
- **标题与页码**：章节标题、页眉、页脚、文字颜色分别组织；标题字号展示实际字号，保留原有相对正文字号的存储方式。
- **主题背景**：预设用 Aa 示样和独立名称展示，整张样式可点按或长按；自定义面板统一颜色、图片、透明度和导入导出操作。
- **更多设置**：统一弹层标题、完成入口和多彩设置列表。

采用柔和圆角面板、清晰分段选中态及紫、薄荷、珊瑚色入口。深色界面沿用当前主题强调色。原有配置字段、取值范围和更新事件保留，未重置阅读偏好。

## 视觉记录

以下均为 Android 模拟器原始截图，书籍使用自行编写的验证文本。

| 项目 | 截图 |
| --- | --- |
| 原界面 / 原边距 | [原界面](reading-settings/01-before-appearance.png) · [原边距](reading-settings/02-before-margins.png) |
| 字体排版 | [亮色](reading-settings/03-typography.png) · [深色](reading-settings/14-typography-dark.png) |
| 字体选择 | [亮色](reading-settings/05-font-picker.png) · [深色](reading-settings/16-font-picker-dark.png) |
| 页面布局 / 正文留白 | [布局](reading-settings/06-page-layout.png) · [正文](reading-settings/07-body-margins.png) |
| 页眉 / 页脚留白 | [页眉](reading-settings/09-header-margins.png) · [页脚](reading-settings/10-footer-margins.png) |
| 背景与高级设置 | [预设](reading-settings/11-theme-picker.png) · [自定义](reading-settings/12-theme-editor.png) · [更多](reading-settings/13-more-settings.png) |
| 标题设置 | [深色](reading-settings/15-title-settings-dark.png) |

## 验证

- 按用户要求直接修改，再进行视觉检查；本轮未新增自动化测试。
- 模拟器：Android 17 / API 37，1080 × 2400，字体缩放 1.0。
- 字号由 24 调整为 25，实际书页重新排版；关闭并强制停止、重启后仍为 25，随后恢复 24。[重启回显](reading-settings/04-size-persisted.png)
- 系统衬线字体切换成功，字体入口回显更新；随后恢复默认。
- 正文上边距由 5 调整为 6，数值、留白示意及书页同步更新，随后恢复 5；三组边距切换显示各自原值。[调整记录](reading-settings/08-margin-adjusted.png)
- 字体导入入口可打开文件夹选择，取消后正常回到字体面板；另用模拟器自带 TTF 文件验证本地字体加载、选择及入口回显，随后恢复系统默认并移除验证文件。[本地字体回显](reading-settings/18-local-font-selected.png)
- 修正了字体子面板露出父面板标题的问题。
- 检查亮色、深色、背景自定义和更多设置；修正开关固定宽度问题。
- 横竖屏切换时重新计算弹层尺寸；横屏采用居中的 560dp 面板，内容可滚动，返回竖屏恢复对应分区高度。[横屏](reading-settings/17-landscape.png) · [横屏滚动](reading-settings/19-landscape-scrolled.png)
- 本地调试构建成功。Android Lint 仍报告 14 项既有错误；按问题类型、消息和文件与基线对照，无新增错误。`git diff --check` 通过。

本轮不生成或发布 Release APK。新增文案提供中、英文，其余语言回退英文。
