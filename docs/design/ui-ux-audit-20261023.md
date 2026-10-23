# UI/UX 与功能模块审查

日期：2026-10-23。对象：当前工作区已安装的 Legado Debug 原生 Android 界面。

## 结论

主导航、设置行、多彩图标和阅读外观面板已经形成新的设计语言；当前遗留问题集中在**旧操作流程、辅助面板、管理工具和信息层级**。继续只换颜色、背景或圆角，收益已经有限。

本轮确认悬浮底栏能覆盖滚动内容并呈现玻璃取样效果。“我的”画面中，设置行继续进入底栏后方，没有重现此前内容提前消失的问题。此结论限本次模拟器和当前构建。

建议先改添加书籍/书源引导、朗读控制、目录和菜单，再整理设置分类。功能上以收起专业入口为主；没有足够证据支持直接删除订阅、Web 服务、规则引擎或旧数据兼容层。

## 范围与证据

- Android 17 / API 37 模拟器，1080 × 2400，系统字体缩放 1.0，亮色主题。
- 当前运行中采集并逐张检查了下列 17 张截图；测试书籍是界面验证用的本地示例文本。
- 源码入口清点：[ui/](/Users/lanzhou/Project/Read/app/src/main/java/io/legado/app/ui) 下有 44 个 `*Activity.kt`、63 个 `*Dialog.kt`，[res/layout/](/Users/lanzhou/Project/Read/app/src/main/res/layout) 有 185 个 XML。这是文件数量，不代表屏幕数量或改造完成比例。
- 除审查文档和截图外，本轮未修改应用源码、删除模块或发布安装包。已返回书架。
- 本轮没有运行构建和测试，因为没有变更应用实现。

## 逐步检查结果

“待优化”表示当前状态可用但有明确问题；“待重做”表示视觉或任务引导与现有主界面差距较大。

| 步骤 | 界面 / 操作 | 状态 | 观察与建议 |
| --- | --- | --- | --- |
| 01 | 打开本地书籍 | 基本良好 | 正文对比清晰、干扰少；本轮未更改排版参数。 |
| 02 | 打开阅读主菜单 | 待优化 | 顶栏、亮度长条、四个悬浮按钮和底部菜单同时出现；文字穿过控件后方形成视觉噪声。保留玻璃效果，在按钮标签下增加局部稳定衬底，并压缩同时出现的控制层。 |
| 03 | 打开章节目录 | 待优化 | 当前章有颜色和勾选双重提示；底部仍是贴边实色工具条，上下跳转按钮只有 36dp。换成统一的目录操作区和至少 48dp 点击区域。 |
| 04 | 长按“朗读”打开控制面板 | 待重做 | 与新阅读外观面板相比，仍是白色贴边面板、小播放按钮、裸滑杆；上一句、播放、停止、下一句缺少主次。以播放/暂停为中心，定时和语速采用带值控件。 |
| 05 | 返回书架 | 基本良好 | 封面、留白和玻璃导航已统一；添加书籍缺少明显的主入口。 |
| 06 | 展开书架菜单 | 待重做 | 11 个菜单项混排添加、管理、导出和日志；弹出菜单仍呈直角矩形。提升“添加书籍”，其余按任务分组。 |
| 07 | 打开搜索，无历史 | 待优化 | 只有“搜索历史”和大块空白；没有说明搜索范围或书源准备条件。补充简短引导和书源入口；结果、错误及加载状态尚未验证。 |
| 08 | 打开发现，无发现源 | 待重做 | 背景和图标已更新，但“当前没有发现源！”没有下一步。增加“管理书源/导入书源”，并区分无书源、未启用发现和筛选无结果。 |
| 09 | 打开订阅 | 待优化 | 当前有五个订阅入口，并非空模块；来源图标形状各异、缺少统一容器。统一容器与间距，保留来源原图作为内容身份。未打开第三方内容。 |
| 10 | 打开“我的” | 待优化 | 多彩图标和设置卡片已更新，底栏透射正常；TXT 目录规则、替换净化、字典规则先于常用外观和备份入口。应重新分类。 |
| 11 | 首次进入书源管理的帮助 | 待优化 | 有完整说明，但首次进入先出现大段操作手册；示例用红/绿点表达状态，学习负担大。改为少量就地提示，完整手册按需打开。 |
| 12 | 关闭帮助，查看零书源列表 | 待重做 | 主区完全空白，却保留“全选(0/0) / 反选 / 删除”工具条。空态应突出导入入口；批量操作仅在有内容或选择后出现。 |
| 13 | 展开书源菜单 | 待优化 | 本地、网络、二维码导入都有用途，但隐藏在更多菜单；同样保留直角弹出菜单。合并为明显的“添加书源”入口，再选择来源。 |
| 14 | 打开网络导入弹窗 | 待优化 | 容器圆角已经更新，内部仍只有 `url` 下划线输入和“确定”。改为“书源地址”、格式示例、粘贴和明确“导入”操作；提交后的校验尚未验证。 |
| 15 | 打开其它设置 | 待优化 | 设置行和开关已现代化；主页选项与用户代理、唤醒锁等技术参数仍混在长列表。把连接、同步和开发工具单独分组。 |
| 16 | 打开阅读外观面板 | 基本良好 | 三段分类、值标签和大点击区已更新；“中/粗/细”可改为更易懂的字重选择；字体提示应提到内置字体。无需整体再重做。 |
| 17 | 展开阅读更多菜单 | 待重做 | 12 项平铺；模拟追读、文本清理、日志等与常用操作并列。按“阅读操作 / 本书排版 / 内容处理 / 诊断”分组。 |

## 优先处理清单

### P1：影响主要任务

1. **添加入口与空状态**（05—08、11—14）：书架提供明确“添加书籍”，书源空态提供“添加书源”。保留本地、网址、远程、二维码等能力，通过二级选择降低首屏复杂度。不要在没有数据时展示批量删除操作。
2. **朗读面板与目录**（03—04）：复用已建立的阅读面板样式，更新播放主次、语速、定时和章节操作；增加触控区域。开启控制面板不应依赖用户猜测“长按朗读”。短按仍可快速开始，另提供明确的朗读选项入口。
3. **菜单体系**（06、13、17）：统一菜单容器、图标、分组和选中状态。当前实际截图仍显示直角菜单，说明仅修改公共圆角背景尚未覆盖这些路径，需按运行效果核验。

### P2：降低日常使用成本

4. **设置信息架构**（10、15）：建议“阅读与外观 / 书籍与书源 / 备份与连接 / 高级工具”。将 TXT 规则、字典规则、用户代理、源编辑参数、日志和性能兼容开关放入合适分组；保留快捷入口和现有配置。
5. **阅读菜单可读性与术语**（02、16—17）：减少同时显示的悬浮控制层；为玻璃上的文字提供稳定的局部对比度。统一“界面 / 阅读外观”“设置 / 更多设置”等入口含义，并说明本书设置与全局设置的作用范围。
6. **订阅内容容器**（09）：先统一图标容器、行列间距和选中反馈；不要把来源图标全部换成同一个系统图标，避免丢失内容识别信息。

## 功能模块取舍

以下是产品与维护建议，不是未使用代码删除证明。没有用户使用频率、崩溃分布或安装设备分布，不能断定某项功能“没人用”。

| 模块 / 入口 | 建议 | 当前依据与取舍条件 |
| --- | --- | --- |
| “退出” | 可考虑移除普通设置入口 | [MyFragment.kt:167](/Users/lanzhou/Project/Read/app/src/main/java/io/legado/app/ui/main/my/MyFragment.kt:167) 仅执行 `activity?.finish()`；不是停止所有后台任务。若有关闭朗读或服务的需求，应在对应功能中明确提供停止操作。 |
| 导入旧版数据 | 收进“迁移工具”，保留兼容实现 | 入口在备份设置；[Restore.kt:140](/Users/lanzhou/Project/Read/app/src/main/java/io/legado/app/help/storage/Restore.kt:140) 仍调用 `ImportOldData.importOldSource()`，不是可以整体删除的死代码。删除会影响旧备份恢复。 |
| RSS / 订阅 | 保留，允许精简导航 | 当前有实际订阅入口；[pref_config_other.xml:41](/Users/lanzhou/Project/Read/app/src/main/res/xml/pref_config_other.xml:41) 已有“显示订阅”开关。可把现有开关放进清晰的导航定制界面，无需再维护一套重复开关。是否默认隐藏需按产品定位决定。 |
| Web 服务 + 内嵌网页 | 保留为可选连接工具 | 直接支持电脑看书和写源；[WebService.kt](/Users/lanzhou/Project/Read/app/src/main/java/io/legado/app/service/WebService.kt) 仍启动 HTTP/WebSocket 服务。复制地址和浏览器打开藏在 [MyFragment.kt:83](/Users/lanzhou/Project/Read/app/src/main/java/io/legado/app/ui/main/my/MyFragment.kt:83) 的长按中，适合改为显式按钮。服务启动、连接和认证流程本轮未运行。 |
| 文件管理 / 远程书籍 | 考虑整合入口，暂不删实现 | 都与书籍导入和存储有关，但目录文件操作、远程书库并非同一能力。可从“添加书籍”进入，在独立工具中保留高级文件操作；本轮仅检查布局和入口。 |
| TXT 目录规则 / 替换净化 / 字典规则 | 保留能力，降低普通用户首屏负担 | 分别解决文本解析、内容清理和查词，不能只因名称专业就视为重复功能。放入规则工具，配示例和即时预览。 |
| 朗读 / 听书 | 保留功能，计划迁移内部媒体层 | [AudioPlayService.kt:16](/Users/lanzhou/Project/Read/app/src/main/java/io/legado/app/service/AudioPlayService.kt:16)、[BaseReadAloudService.kt:17](/Users/lanzhou/Project/Read/app/src/main/java/io/legado/app/service/BaseReadAloudService.kt:17) 仍用 `MediaSessionCompat`；[app/build.gradle:204](/Users/lanzhou/Project/Read/app/build.gradle:204) 同时引入旧 media 和 Media3 播放器，Media3 session 未启用。可逐步统一会话、通知、耳机和后台控制；[Android 官方迁移指南](https://developer.android.com/media/media3/exoplayer/migration-guide)已推荐迁移到 Media3。不能简单删除旧依赖。 |
| Firebase Analytics / Performance | 优先核实是否有明确维护用途，再决定移除或提供控制 | [app/build.gradle:281](/Users/lanzhou/Project/Read/app/build.gradle:281) 引入 analytics/perf；当前已有的 Debug/Release 合并 Manifest 均含 Firebase 初始化与组件注册。源码搜索未发现显式埋点、统计开关或收集禁用配置；隐私说明却只描述崩溃和性能报告。应核实 SDK 配置、实际网络行为和维护需求，并统一说明。**本轮未抓包，不能据此宣称已发生数据上报。** [Firebase 官方文档](https://firebase.google.com/docs/analytics/android/configure-data-collection)提供收集控制方式。 |
| [modules/book](/Users/lanzhou/Project/Read/modules/book) / [modules/rhino](/Users/lanzhou/Project/Read/modules/rhino) | 保留核心模块 | 分别承载书籍解析与书源脚本。删掉会削弱阅读器主要能力；XML Views 也不等于过时，当前无需为视觉更新整体改写框架。 |
| 老 Android 兼容分支和固定版本依赖 | 先核实设备分布，再评估收缩 | 当前最低 API 21；[libs.versions.toml:45](/Users/lanzhou/Project/Read/gradle/libs.versions.toml:45) 对 jsoup、commons-text、Rhino 固定版本明确写有解析兼容或旧系统 API 原因。不能把全部版本限制直接当成无用负担。 |

### 已从代码发现、尚需实际画面复核

- [AutoReadDialog.kt:31](/Users/lanzhou/Project/Read/app/src/main/java/io/legado/app/ui/book/read/config/AutoReadDialog.kt:31) 仍自行设置底部全宽实色窗口，[dialog_auto_read.xml](/Users/lanzhou/Project/Read/app/src/main/res/layout/dialog_auto_read.xml) 是旧式裸滑杆布局；应与朗读面板一起核验并统一。
- [activity_book_info.xml:23](/Users/lanzhou/Project/Read/app/src/main/res/layout/activity_book_info.xml:23) 仍有封面深色遮罩、弧形过渡、小圆角高阴影组合。建议重新设计书籍详情，但本轮未打开实际详情页，不能把布局推断当成截图结论。
- [activity_file_manage.xml:24](/Users/lanzhou/Project/Read/app/src/main/res/layout/activity_file_manage.xml:24) 的路径栏只有 24dp 高，[dialog_webdav_server.xml](/Users/lanzhou/Project/Read/app/src/main/res/layout/dialog_webdav_server.xml) 使用旧工具栏及下划线表单；需要在实际文件、连接和键盘状态下检查。
- [styles.xml:52](/Users/lanzhou/Project/Read/app/src/main/res/values/styles.xml:52) 的 `Spinner` 仍继承 `android:Theme.Holo.Light`，WebDAV 表单引用了该样式。应逐步替换实际使用点，不能仅改父样式而不验证夜间和弹出层。
- 规则编辑、HTTP TTS 编辑、导入预览、漫画设置、音频播放、换源和登录/验证码流程需要补充真实内容状态后检查。

## 无障碍与验证边界

- 目录上下跳转按钮 36dp；朗读多个播放和语速按钮 30dp，见 [fragment_chapter_list.xml:43](/Users/lanzhou/Project/Read/app/src/main/res/layout/fragment_chapter_list.xml:43)、[dialog_read_aloud.xml:42](/Users/lanzhou/Project/Read/app/src/main/res/layout/dialog_read_aloud.xml:42) 等。对应控制器中未找到扩大触控区逻辑；建议采用至少 48dp 触控区，再用实际设备确认。
- 阅读主菜单的界面层级中，“自动亮度”说明仍含 `%s` 占位符（02、17）。应检查格式化和开关状态描述，避免读屏直接念出占位符。
- 书源首次帮助通过红/绿点解释状态（11）；实际列表应同时提供文字或可读状态标签。当前没有书源，未验证有数据行是否已经提供替代提示。
- 玻璃区域随背景变化，截图只能提示可读性风险，不能证明所有背景下的对比度达标。
- 本轮未检查 TalkBack 焦点、200% 字体、横屏/平板、夜间全流程、真实机型玻璃性能，也未完成网络导入、WebDAV、第三方订阅内容、音频播放和漫画的端到端验证。
- 不使用代码文件数量推算“已现代化百分比”，不把当前样本中的空白推断为全部用户都会遇到。

## 推荐实施顺序与验收

1. **主要任务**：添加书籍/书源、发现空态、菜单分组。验收：无书源时能从当前页直接进入添加流程；零内容不展示无效批量操作；首次进入不用先读长手册。
2. **阅读辅助**：朗读、自动翻页、目录工具栏、入口命名与读屏标签。验收：播放主操作明显、按钮触控区足够、面板关闭后回到正文，玻璃标签在复杂正文背景下仍清晰。
3. **管理工具**：书籍详情、文件/远程书库、规则表单、设置分类。验收：键盘不遮挡操作，表单有明确字段与错误提示，配置及导入兼容保持可用。
4. **内部精简**：先核实统计依赖用途，再迁移媒体会话；旧数据兼容和最低系统版本另行评估。每项独立变更，便于验证和回退。

## 本轮截图

所有图片均为当前审查运行中新采集的原图，按上述步骤编号；没有以旧版本截图充当当前证据。

### 01 正文 · 基本良好
![01 正文](/Users/lanzhou/Project/Read/docs/design/ui-ux-audit-20261023/01-reading.png)

### 02 阅读菜单 · 待优化
![02 阅读菜单](/Users/lanzhou/Project/Read/docs/design/ui-ux-audit-20261023/02-reading-menu.png)

### 03 目录 · 待优化
![03 目录](/Users/lanzhou/Project/Read/docs/design/ui-ux-audit-20261023/03-catalog.png)

### 04 朗读面板 · 待重做
![04 朗读面板](/Users/lanzhou/Project/Read/docs/design/ui-ux-audit-20261023/04-read-aloud.png)

### 05 书架 · 基本良好
![05 书架](/Users/lanzhou/Project/Read/docs/design/ui-ux-audit-20261023/05-bookshelf.png)

### 06 书架菜单 · 待重做
![06 书架菜单](/Users/lanzhou/Project/Read/docs/design/ui-ux-audit-20261023/06-bookshelf-menu.png)

### 07 搜索初始页 · 待优化
![07 搜索初始页](/Users/lanzhou/Project/Read/docs/design/ui-ux-audit-20261023/07-search.png)

### 08 发现空态 · 待重做
![08 发现空态](/Users/lanzhou/Project/Read/docs/design/ui-ux-audit-20261023/08-discovery.png)

### 09 订阅 · 待优化
![09 订阅](/Users/lanzhou/Project/Read/docs/design/ui-ux-audit-20261023/09-rss.png)

### 10 我的 · 待优化
![10 我的](/Users/lanzhou/Project/Read/docs/design/ui-ux-audit-20261023/10-my.png)

### 11 书源首次帮助 · 待优化
![11 书源首次帮助](/Users/lanzhou/Project/Read/docs/design/ui-ux-audit-20261023/11-source-help.png)

### 12 零书源列表 · 待重做
![12 零书源列表](/Users/lanzhou/Project/Read/docs/design/ui-ux-audit-20261023/12-sources.png)

### 13 书源菜单 · 待优化
![13 书源菜单](/Users/lanzhou/Project/Read/docs/design/ui-ux-audit-20261023/13-source-menu.png)

### 14 网络导入 · 待优化
![14 网络导入](/Users/lanzhou/Project/Read/docs/design/ui-ux-audit-20261023/14-source-import.png)

### 15 其它设置 · 待优化
![15 其它设置](/Users/lanzhou/Project/Read/docs/design/ui-ux-audit-20261023/15-other-settings.png)

### 16 阅读外观 · 基本良好
![16 阅读外观](/Users/lanzhou/Project/Read/docs/design/ui-ux-audit-20261023/16-reading-style.png)

### 17 阅读更多菜单 · 待重做
![17 阅读更多菜单](/Users/lanzhou/Project/Read/docs/design/ui-ux-audit-20261023/17-reading-more.png)
