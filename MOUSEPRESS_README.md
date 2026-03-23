# mousePressed 事件触发机制 - 文档索引

## 概述

本文档集详细解释了 Logisim 项目中 `mousePressed` 事件的触发机制。

## 问题

**这个 mousePressed 究竟是如何触发的？**

## 文档列表

### 1. 📖 详细机制说明（中文）
**文件**: [MOUSEPRESS_MECHANISM.md](./MOUSEPRESS_MECHANISM.md)

包含内容：
- 整体架构说明
- 详细触发流程
- 监听器注册机制
- 所有工具实现列表
- 组件交互器（Poker）详解
- 调试技巧

### 2. 📖 Detailed Mechanism Explanation (English)
**File**: [MOUSEPRESS_MECHANISM_EN.md](./MOUSEPRESS_MECHANISM_EN.md)

Contents:
- Overall architecture
- Detailed trigger flow
- Listener registration
- All tool implementations
- Component pokers explanation
- Debugging tips

### 3. 📊 可视化流程图
**文件**: [MOUSEPRESS_FLOW_DIAGRAM.md](./MOUSEPRESS_FLOW_DIAGRAM.md)

包含内容：
- 完整事件流程图
- 类层次结构图
- 三个详细的事件流示例
- 调试断点建议

## 快速参考

### 关键入口点

| 位置 | 文件 | 行号 | 说明 |
|------|------|------|------|
| 主入口 | `Canvas.java` | 185-216 | MyListener.mousePressed() |
| 监听器注册 | `Canvas.java` | 794 | addMouseListener() |
| Tool基类 | `Tool.java` | 95 | mousePressed() 默认实现 |
| Poker基类 | `InstancePoker.java` | 32 | mousePressed() 默认实现 |

### 主要工具类

1. **SelectTool** - 选择和移动组件
2. **AddTool** - 添加新组件
3. **PokeTool** - 模拟期间的组件交互
4. **EditTool** - 编辑组件属性
5. **WiringTool** - 绘制导线
6. **TextTool** - 添加文本标签
7. **MenuTool** - 上下文菜单

### 事件流程简图

```
用户点击 → AWT事件 → Canvas.MyListener → getToolFor() → Tool.mousePressed() → 具体工具处理
                                                                    ↓
                                                        (如果是可交互组件)
                                                                    ↓
                                                        InstancePokerAdapter → InstancePoker.mousePressed()
```

## 架构模式

Logisim 的 mousePressed 机制使用了以下设计模式：

1. **责任链模式** (Chain of Responsibility)
   - 事件从 AWT → Canvas → Tool → Component 层层传递

2. **策略模式** (Strategy Pattern)
   - 不同工具提供不同的 mousePressed 实现

3. **适配器模式** (Adapter Pattern)
   - InstancePokerAdapter 桥接 Tool 和 InstancePoker

## 代码统计

找到的 mousePressed 实现总数：**47+**

- 电路编辑工具：8个
- 绘图工具：7个
- 组件交互器：8个
- UI组件：15+个
- 内存组件：9个

## 如何使用这些文档

1. **如果你想快速了解整体机制**：
   - 先看 [MOUSEPRESS_FLOW_DIAGRAM.md](./MOUSEPRESS_FLOW_DIAGRAM.md) 的流程图

2. **如果你需要详细的代码说明**：
   - 中文：阅读 [MOUSEPRESS_MECHANISM.md](./MOUSEPRESS_MECHANISM.md)
   - English: Read [MOUSEPRESS_MECHANISM_EN.md](./MOUSEPRESS_MECHANISM_EN.md)

3. **如果你需要调试**：
   - 参考任何文档中的"调试技巧"章节
   - 在关键位置设置断点

4. **如果你想添加新工具**：
   - 继承 `Tool` 类（电路工具）或 `CanvasTool` 类（绘图工具）
   - 实现 `mousePressed()` 方法
   - 参考现有工具的实现

5. **如果你想添加可交互组件**：
   - 继承 `InstancePoker` 类
   - 实现 `mousePressed()` 方法
   - 在组件中返回你的 Poker 实例

## 相关源代码文件

### 核心文件
- `src/main/java/com/cburch/logisim/gui/main/Canvas.java` - 主画布和事件入口
- `src/main/java/com/cburch/logisim/tools/Tool.java` - 工具基类
- `src/main/java/com/cburch/logisim/instance/InstancePoker.java` - 组件交互器基类

### 工具实现
- `src/main/java/com/cburch/logisim/tools/` - 所有电路编辑工具
- `src/main/java/com/cburch/draw/tools/` - 所有绘图工具

### 组件实现
- `src/main/java/com/cburch/logisim/std/io/` - I/O 组件（Button, Joystick, Slider等）
- `src/main/java/com/cburch/logisim/std/wiring/` - 布线组件（Pin, Clock等）
- `src/main/java/com/cburch/logisim/std/memory/` - 内存组件

## 贡献

如果发现文档有误或需要补充，请：
1. 在源代码中验证实际实现
2. 更新相应的文档
3. 提交 Pull Request

## 版本信息

- 创建日期：2026-03-23
- 基于代码库版本：当前 master 分支
- 文档语言：中文 + English

---

**注意**：本文档集是对现有代码的分析和说明，不修改任何源代码。所有信息都是基于对 Logisim 源代码的深入分析得出的。
