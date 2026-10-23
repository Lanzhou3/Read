# 日常界面与操作流程更新

日期：2026-10-23。对应[UI/UX 审查](/Users/lanzhou/Project/Read/docs/design/ui-ux-audit-20261023.md)的第一批实施，基于当前工作区已有的玻璃导航、多彩图标和阅读外观面板继续更新。

## 本轮改动

| 范围 | 更新后的行为 |
| --- | --- |
| 添加书籍 | 书架顶栏提供明显的添加按钮；本地、网址、远程书库、书单采用带说明的任务选择面板，继续调用原有导入功能。 |
| 书源与发现 | 区分零书源、没有可用发现源和筛选无结果；提供添加、管理或清除筛选入口。零书源时隐藏批量删除操作，完整帮助按需打开。 |
| 网络导入 | 使用圆角描边多行输入框，说明地址/JSON 用途，保留历史地址补全；空输入显示就地错误，填好后先预览再导入。 |
| 搜索 | 无历史时说明搜索范围，提供书源管理入口，避免只有空白和历史标题。 |
| 朗读 | 重做播放主次、章节操作、带值语速和睡眠定时；增加明确的“朗读控制”入口，保留短按快速朗读。跟随系统时禁用手动语速，定时可保存为预设。 |
| 自动翻页 | 统一阅读面板样式，显示翻页间隔并提供停止按钮；修改翻页速度不再触发无关的朗读操作。 |
| 目录与阅读菜单 | 目录使用悬浮玻璃操作区及至少 48dp 的主要按钮；正文延伸至操作区后方，末章可以滚出遮挡区。阅读菜单分组，增强文字衬底，修复自动亮度说明中的占位符。 |
| 设置 | “我的”按阅读与外观、书籍与书源、备份与连接、高级工具整理；规则工具默认折叠。其它设置按导航、备份连接、媒体、存储和高级项分组；旧数据导入收进迁移工具。 |
| 详情与工具 | 更新书籍详情的封面、信息卡片和阅读操作；统一订阅图标容器、批量操作栏；文件路径栏扩大到 48dp。远程书库表单增加明确字段说明，去掉旧 Holo 下拉样式。 |
| 功能精简 | 移除仅关闭当前界面的“退出”设置入口；Web 服务运行时提供显式地址操作入口。保留订阅、Web 服务、规则引擎、远程书库和旧数据兼容能力。 |

## 验证结果

环境：Android 17 / API 37 模拟器，1080 × 2400，字体缩放 1.0。实际安装 Debug 构建后操作并检查截图，没有交付或发布 APK。

| 检查 | 结果与范围 |
| --- | --- |
| Debug 构建 | `./gradlew :app:assembleAppDebug` 通过。 |
| Android Lint | `./gradlew :app:lintAppDebug` 仍被 14 条原有错误阻断；与改动前报告按规则、消息和位置比较，没有新增错误。原有类别为 MissingTranslation、WrongConstant、NewApi、MissingClass、RestrictedApi。 |
| 改动格式 | `git diff --check` 通过。 |
| 配置兼容 | 对比 HEAD：其它设置、备份设置保留原有全部配置键；主设置只移除 `exit`，新增 Web 服务地址操作入口。没有数据库结构变更。 |
| 本地添加 | 新选择面板能打开原有本地文件列表。 |
| 书源导入 | 用自建、禁用的 `UI-QA-Source` JSON 实际完成预览、导入及删除测试项；空输入留在表单并显示错误；分组筛选空态能清除筛选。测试书源和临时设备文件已清理。 |
| 阅读控制 | 检查亮色/深色朗读面板、语速跟随开关、5 分钟定时选择；恢复未开启定时、跟随系统及原语速。自动翻页已启动、打开控制面板并停止。未验证真实语音输出。 |
| 横屏 | 目录滚动至末章，底部操作区未遮挡最后一章；朗读面板滚动到底后四个操作按钮完整显示，已修复系统手势区重叠。 |
| 设置与表单 | 高级工具可展开；其它设置分组可见；WebDAV 空地址保存被拦下，键盘显示时密码框和保存入口仍可见。没有填写账号、连接远端或保存服务器。 |

设备已恢复竖屏、原字体缩放及手写输入设置，并返回书架。没有提交或推送代码；原有工作区改动和 AGENTS.md 保持原状。

## 当前边界与后续工作

- 这是主要日常界面的第一批更新，不代表所有专业界面已经完成。规则编辑、HTTP TTS 编辑、漫画、听书、换源、登录/验证码和部分导入预览仍需真实内容状态下逐项更新。
- 书架等溢出菜单已按任务整理，列表内容有圆角；部分系统弹出窗口仍保留外层矩形底色，尚未完全统一。
- 远程书库列表本身、服务器选择列表及批量栏的空状态仍可继续精简；本轮主要更新添加入口和连接表单。
- 媒体会话迁移到 Media3、Firebase 用途与收集策略核实尚未实施；不能根据本轮视觉检查直接删除依赖。
- 没有验证真实网络书源抓取、Web 服务连接、WebDAV 读写、第三方订阅内容和后台音频；没有完成 TalkBack、200% 字体、平板、旧 Android 和真机玻璃性能测试。
- 新增文案提供中文和英文，其他语言回退英文；没有宣称完成全部语言翻译。遵照直接修改后视觉确认的要求，本轮未新增测试用例，也未运行完整单元测试或设备测试套件。

## 主要画面

以下选用本轮已检查的画面。目录内部分较早截图记录中间状态，不作为最终样式依据；其中 11、13 仅记录书源导入流程，21 是安全间距修复前的横屏记录，最终横屏结果见 22。

### 添加书籍与书源

![添加书籍](/Users/lanzhou/Project/Read/docs/design/ui-refresh-implementation/02-add-book.png)
![添加书源](/Users/lanzhou/Project/Read/docs/design/ui-refresh-implementation/08-add-source.png)
![输入校验](/Users/lanzhou/Project/Read/docs/design/ui-refresh-implementation/10-source-address-validation.png)

### 阅读辅助

![朗读面板](/Users/lanzhou/Project/Read/docs/design/ui-refresh-implementation/05-read-aloud.png)
![深色朗读](/Users/lanzhou/Project/Read/docs/design/ui-refresh-implementation/24-read-aloud-dark.png)
![横屏朗读底部安全区](/Users/lanzhou/Project/Read/docs/design/ui-refresh-implementation/22-read-aloud-landscape-bottom.png)
![横屏目录末章](/Users/lanzhou/Project/Read/docs/design/ui-refresh-implementation/19-catalog-landscape.png)

### 设置、书籍详情与搜索

![高级工具](/Users/lanzhou/Project/Read/docs/design/ui-refresh-implementation/25-advanced-tools.png)
![其它设置](/Users/lanzhou/Project/Read/docs/design/ui-refresh-implementation/26-other-settings.png)
![书籍详情](/Users/lanzhou/Project/Read/docs/design/ui-refresh-implementation/16-book-info.png)
![搜索引导与键盘](/Users/lanzhou/Project/Read/docs/design/ui-refresh-implementation/29-search-welcome.png)

更多实际画面：[截图目录](/Users/lanzhou/Project/Read/docs/design/ui-refresh-implementation)。
