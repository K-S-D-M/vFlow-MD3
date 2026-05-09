# vFlow - 强大的 Android 可视化工作流自动化工具
[English](README_EN.md) | 简体中文
<div>

  <a href="https://discord.gg/7AMqhjdUH6" target="_blank">
    <img src="https://img.shields.io/badge/Join%20our%20Discord-5865F2?style=for-the-badge&logo=discord&logoColor=white" alt="Join Discord"/>
  </a>
  <a href="https://qm.qq.com/q/5OhnIUNHzO" target="_blank" style="margin-left: 6px;">
    <img src="https://img.shields.io/badge/Join%20QQ%20Group-%230366CC?style=for-the-badge&logo=qq&logoColor=white" alt="Join QQ Group"/>
  </a>

</div>

[![Latest Release](https://img.shields.io/github/v/release/ChaoMixian/vFlow?display_name=tag&style=flat-square)](https://github.com/ChaoMixian/vFlow/releases/latest)
[![GitHub Stars](https://img.shields.io/github/stars/ChaoMixian/vFlow?style=flat-square)](https://github.com/ChaoMixian/vFlow/stargazers)
[![GitHub Forks](https://img.shields.io/github/forks/ChaoMixian/vFlow?style=flat-square)](https://github.com/ChaoMixian/vFlow/network/members)
[![GitHub Issues](https://img.shields.io/github/issues/ChaoMixian/vFlow?style=flat-square)](https://github.com/ChaoMixian/vFlow/issues)
[![License](https://img.shields.io/github/license/ChaoMixian/vFlow?style=flat-square)](LICENSE)
[![API](https://img.shields.io/badge/API-21%2B-brightgreen?style=flat-square)](https://android-arsenal.com/api?level=21)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.3.20-blue?style=flat-square&logo=kotlin)](https://kotlinlang.org)

**vFlow** 是一款为 Android 平台设计的、强大且高度可扩展的自动化工具。它允许你通过图形化界面，将一系列"动作模块"自由组合成强大的"工作流"，从而自动完成各种日常的、重复性的屏幕操作任务。

> [!WARNING]
> **免责声明**: 使用本软件即表示您已充分理解并接受所有条款。本软件涉及自动化操作、Root权限执行等高风险功能，可能导致设备损坏、数据丢失、账号封禁等风险。**所有风险由用户自行承担，开发者不承担任何责任。** 请在使用前仔细阅读 [完整免责声明](DISCLAIMER.md)。

## ✨ 项目简介

**vFlow 的核心设计理念** 是将复杂的自动化逻辑分解为一个个独立、可复用、易于理解的模块。无论是简单的"每日签到"，还是包含复杂条件判断和循环的"自动化测试流程"，vFlow 都旨在提供一个直观、灵活且强大的平台。

项目完全采用 Kotlin 编写，并遵循现代 Android 开发实践。其核心架构（模块注册表、动态 UI 生成器、类型安全的执行上下文）被精心设计，不仅保证了当前功能的稳定性，也为未来添加更多、更强大的自动化模块提供了无限可能。无论你是希望解放双手的普通用户，还是寻求灵感和实践的开发者，vFlow 都欢迎你的探索和贡献。

## 🎨 vFlow-MD3 改动说明

本项目基于 [vFlow 原版](https://github.com/ChaoMixian/vFlow) 进行二次开发，主要改动如下：

### UI 主题重构 (Material Design 3)

- **主色调更换为 #FB7299 (Bilibili Pink)**，全面适配 Material Design 3 色彩系统
- **新增深色主题 / 浅色主题 / 跟随系统** 三种主题模式，可在设置页面切换
- **支持 Monet 动态取色**（Android 12+），自动从壁纸提取主题色
- 修改文件：`ThemeUtils.kt`、`VFlowTheme.kt`、`colors.xml`、`colors.xml (night)`

### 首页与工作流列表

- **工作流列表新增双列网格布局**，支持列表/网格视图切换
- 新增 `WorkflowGridCard` 组件，展示工作流卡片缩略信息
- **首页新增"查看全部日志"按钮**，快速跳转执行日志页面
- 修改文件：`WorkflowListScreen.kt`、`HomeScreen.kt`

### 工作流编辑器

- **步骤卡片新增分类彩色边框**：触发器(蓝)、交互(橙)、逻辑(紫)、条件(绿)
- **步骤标题加粗显示**，提升可读性
- **编辑器画布新增网格背景**
- **新增缩放级别指示器**，显示当前画布缩放比例
- **新增跨工作流复制粘贴步骤功能**：可将步骤复制到全局剪贴板，在其他工作流中粘贴，粘贴时自动生成新 UUID 避免冲突
- 修改文件：`WorkflowEditorActivity.kt`、`ActionStepAdapter.kt`、`StepClipboard.kt`（新增）、`popup_step_actions.xml`、`activity_workflow_editor.xml`

### 模块管理器

- **新增双列网格布局**，模块以卡片形式展示
- **新增分类筛选标签栏**，可按触发器、交互、逻辑等分类过滤
- **新增底部弹窗详情页**，点击模块卡片查看完整信息
- 修改文件：`RepositoryScreen.kt`

### 设置页面

- **设置项改为卡片分组布局**，视觉层次更清晰
- **新增主题模式选择**（浅色/深色/跟随系统）
- **新增编辑器缩放比例设置**
- **新增自动保存开关**
- **新增日志保留天数设置**
- **新增关于信息区域**
- 修改文件：`SettingsScreen.kt`、`SettingsViewModel.kt`、`SettingsRoute.kt`

### 新增页面

- **模板市场页面**：提供 8 个预置工作流模板，可一键导入使用
- **执行日志页面**：查看工作流执行历史记录
- 新增文件：`TemplateMarketScreen.kt`、`TemplateModels.kt`、`ExecutionLogScreen.kt`

### 新增自动化模块 (9个)

| 模块 | 类别 | 功能 |
|------|------|------|
| WaitForElementModule | 交互 | 等待界面元素出现 |
| FileOpsModule | 交互 | 文件操作（复制/移动/删除/重命名） |
| PushNotificationModule | 交互 | 发送通知 |
| DeviceStatusModule | 交互 | 获取设备状态信息 |
| AppLauncherModule | 交互 | 启动指定应用 |
| AutoScreenshotModule | 交互 | 自动截屏 |
| WebHttpRequestModule | 交互 | 发送 HTTP 请求 |
| TimeTriggerModule | 触发器 | 定时触发工作流 |
| BatteryTriggerModule | 触发器 | 电量变化触发工作流 |

- 新增文件：`WaitForElementModule.kt`、`FileOpsModule.kt`、`PushNotificationModule.kt`、`DeviceStatusModule.kt`、`AppLauncherModule.kt`、`AutoScreenshotModule.kt`、`WebHttpRequestModule.kt`、`TimeTriggerModule.kt`、`BatteryTriggerModule.kt`
- 修改文件：`ModuleRegistry.kt`（注册所有新模块）

### 导航结构

- **底部导航新增"模板"标签页**，与首页、工作流列表、模块管理器、设置并列
- 修改文件：`MainComposeShell.kt`

### CI/CD

- **新增 GitHub Actions 自动构建流程**，自动编译 APK 并上传至 Release
- 新增文件：`.github/workflows/build-release.yml`

## 📸 应用截图

<table>
  <tr>
    <td><img src="docs/home_fragment.png" width="200"></td>
    <td><img src="docs/workflow_editor.png" width="200"></td>
    <td><img src="docs/module_fragment.png" width="200"></td>
  </tr>
  <tr>
    <td align="center">首页</td>
    <td align="center">工作流编辑</td>
    <td align="center">模块管理器</td>
  </tr>
</table>

## 🚀 主要特性

- **可视化流程编辑器**: 通过拖拽和点击，像搭积木一样构建你的自动化流程。
- **高度模块化**: 每个功能（如点击、查找文本、判断）都是一个独立的模块，易于维护和扩展。
- **动态数据流**: 模块的输出可以作为后续模块的输入（“魔法变量”），实现复杂的逻辑联动。
- **强大的逻辑控制**: 支持“如果/否则”条件判断和“循环”等控制流，让你的工作流更智能。
- **动态参数编辑**: 编辑器 UI 会根据你选择的参数（例如，“如果”模块中变量的类型）动态变化，只显示相关的选项。
- **完善的权限管理**: 在执行前清晰地请求工作流所需的权限，并提供统一的管理入口。
- **现代 UI 设计**: 基于 Material 3 和动态取色，提供美观且个性化的用户界面。
- **导入与导出**: 轻松备份、恢复和分享你的工作流。

## 🛠️ 技术架构概览

vFlow App 的核心是其高度解耦的模块化架构。

1.  **模块 (Module)**

    - 所有自动化动作的实现基础，每个模块都实现了 `ActionModule` 接口。
    - 模块负责定义自身的元数据（名称、图标）、输入输出参数、UI 摘要、执行逻辑以及所需权限。
    - **示例**: `ClickModule`, `IfModule`, `LoopModule`。

2.  **模块注册表 (ModuleRegistry)**

    - 一个单例对象，在应用启动时注册所有可用的模块。
    - 为应用的其他部分（如动作选择器）提供按分类获取模块的能力。

3.  **工作流编辑器 (Workflow Editor)**

    - `WorkflowEditorActivity` 是核心 UI，负责展示和操作 `ActionStep` 列表。
    - `ActionStepAdapter` 将 `ActionStep` 数据渲染为用户可见的卡片列表。
    - `ActionEditorSheet` 是一个通用的底部表单，它能根据任何模块的 `InputDefinition` 动态生成编辑界面，实现了 UI 与模块逻辑的完全解耦。

4.  **工作流执行器 (WorkflowExecutor)**
    - 负责按顺序执行工作流中的每一个步骤。
    - 为每个步骤创建包含上下文信息（如魔法变量值、服务实例）的 `ExecutionContext`。
    - 处理模块返回的不同结果，如成功、失败，或跳转、循环等流程控制信号。

vFlow Core 采用 Master-Worker 多进程架构，基于 TCP Socket 通信与自定义 JSON-RPC 协议，将指令动态路由至 Shell 或 Root 权限的子进程执行，实现了严格的权限隔离与高效的系统服务管控。

![vFlow Core Architecture](docs/vFlow_Core_Architecture.png)

## 📦 如何构建

1.  克隆仓库:
    ```bash
    git clone https://github.com/ChaoMixian/vflow.git
    ```
2.  使用 Android Studio 打开项目。
3.  等待 Gradle 同步完成。
4.  直接运行项目到你的设备或模拟器上。

## 🤝 如何贡献

我们非常欢迎各种形式的贡献！无论是提交 Issue、修复 Bug、添加新功能模块还是改进文档，都对项目意义重大。

1.  **Fork** 本仓库。
2.  创建你的功能分支 (`git checkout -b feature/AmazingFeature`)。
3.  提交你的改动 (`git commit -m 'Add some AmazingFeature'`)。
4.  推送到你的分支 (`git push origin feature/AmazingFeature`)。
5.  创建一个 **Pull Request**。

### 💻 开发一个新模块

vFlow 最常见的贡献方式就是添加新的模块。步骤如下：

1.  在 `com.chaomixian.vflow.core.workflow.module` 包下的相应分类中创建一个新的 Kotlin 类。
2.  让它继承自 `BaseModule` 或 `BaseBlockModule`。
3.  实现 `ActionModule` 接口中的必要属性和方法（如 `id`, `metadata`, `getInputs`, `getOutputs`, `execute`）。
4.  在 `ModuleRegistry.kt` 的 `initialize()` 方法中注册你的新模块。
5.  就是这样！你的模块现在应该会自动出现在动作选择器中并可以正常使用了。

[开发指南](docs/CONTRIBUTION.md)

## 🌟来颗 Star

[![Star History Chart](https://api.star-history.com/svg?repos=ChaoMixian/vFlow&type=date&legend=top-left)](https://www.star-history.com/#ChaoMixian/vFlow&type=date&legend=top-left)

## 📄 许可证

本项目采用 [GNU General Public License v2.0 or later (GPL-2.0-or-later)](LICENSE) 许可证。

## 💰 关于赞助

vFlow 还在成长，相比成熟软件，功能存在较大差距。当前保持为爱发电，**不接受赞助**，感谢。

<details>
<summary>赞助名单 (非通过 vFlow 项目，按时间顺序)</summary>

```
鲨鱼辣椒    RMB 18.88   2026/01/29
罗密欧的沉默  RMB 26.66   2026/02/10
起飞      RMB 200.00      2026/04/08
```

</details>