# Logisim mousePressed 事件流程图

## 完整的事件流程

```
┌─────────────────────────────────────────────────────────────────┐
│                         用户操作                                  │
│                      点击鼠标按钮                                 │
└───────────────────────────┬─────────────────────────────────────┘
                            │
                            ▼
┌─────────────────────────────────────────────────────────────────┐
│                      Java AWT 事件系统                            │
│                  生成 MouseEvent 对象                             │
└───────────────────────────┬─────────────────────────────────────┘
                            │
                            ▼
┌─────────────────────────────────────────────────────────────────┐
│            Canvas.MyListener.mousePressed(MouseEvent e)          │
│              Location: Canvas.java:185-216                       │
│                                                                   │
│  处理流程：                                                        │
│  1. 清除错误和信息消息                                             │
│  2. 检查是否点击缩放按钮                                           │
│  3. 如果不是缩放按钮：                                             │
│     - 请求焦点                                                    │
│     - drag_tool = getToolFor(e)  // 获取工具                      │
│     - drag_tool.mousePressed(Canvas, Graphics, e)                │
└───────────────────────────┬─────────────────────────────────────┘
                            │
                            ▼
┌─────────────────────────────────────────────────────────────────┐
│                      getToolFor(e)                               │
│                                                                   │
│  根据以下因素确定工具：                                            │
│  - 当前鼠标按钮（左键/右键/中键）                                  │
│  - 修饰键（Ctrl/Shift/Alt）                                       │
│  - MouseMappings 配置                                            │
│                                                                   │
│  返回：Tool 对象                                                  │
└───────────────────────────┬─────────────────────────────────────┘
                            │
                ┌───────────┴───────────┐
                │                       │
                ▼                       ▼
    ┌───────────────────┐   ┌───────────────────┐
    │  Circuit Tools    │   │  Drawing Tools    │
    └────────┬──────────┘   └────────┬──────────┘
             │                       │
             ▼                       ▼

┌────────────────────────────┐    ┌────────────────────────────┐
│  Tool.mousePressed()       │    │ CanvasTool.mousePressed()  │
│  Location: Tool.java:95    │    │ Location:                  │
│                            │    │ CanvasTool.java:44         │
└────────────────────────────┘    └────────────────────────────┘
             │
             │
    ┌────────┴────────┬─────────────┬─────────────┬──────────────┐
    │                 │             │             │              │
    ▼                 ▼             ▼             ▼              ▼
┌─────────┐     ┌──────────┐  ┌─────────┐  ┌──────────┐  ┌──────────┐
│SelectTool│    │ AddTool  │  │PokeTool │  │EditTool  │  │WiringTool│
│         │    │          │  │         │  │          │  │          │
│选择组件  │    │添加组件   │  │交互组件  │  │编辑属性   │  │绘制导线  │
└─────────┘    └──────────┘  └────┬────┘  └──────────┘  └──────────┘
                                   │
                                   │ (如果是可交互组件)
                                   ▼
                    ┌──────────────────────────────┐
                    │  InstancePokerAdapter        │
                    │  Location:                   │
                    │  InstancePokerAdapter.java   │
                    │                              │
                    │  桥接 Tool 和 InstancePoker   │
                    └──────────┬───────────────────┘
                               │
                               ▼
                    ┌──────────────────────────────┐
                    │ InstancePoker.mousePressed() │
                    │ Location:                    │
                    │ InstancePoker.java:32        │
                    └──────────┬───────────────────┘
                               │
            ┌──────────────────┼──────────────────┐
            │                  │                  │
            ▼                  ▼                  ▼
    ┌──────────────┐   ┌──────────────┐  ┌──────────────┐
    │Button.Poker  │   │  Pin.Poker   │  │Clock.Poker   │
    │              │   │              │  │              │
    │按钮点击      │   │引脚切换      │  │时钟触发      │
    │value=TRUE    │   │切换位值      │  │切换时钟      │
    └──────────────┘   └──────────────┘  └──────────────┘

```

## 关键类的层次结构

```
MouseListener (Java AWT 接口)
    ↑
    │ implements
    │
Canvas.MyListener (内部类)
    │
    │ delegates to
    ↓
Tool (抽象基类)
    ├── SelectTool
    ├── AddTool
    ├── PokeTool ─────→ InstancePokerAdapter ─────→ InstancePoker
    ├── EditTool                                         ├── Button.Poker
    ├── WiringTool                                       ├── Pin.Poker
    ├── TextTool                                         ├── Clock.Poker
    └── MenuTool                                         ├── Joystick.Poker
                                                         └── Slider.Poker

CanvasTool (绘图工具基类)
    ├── draw.SelectTool
    ├── draw.TextTool
    ├── LineTool
    ├── CurveTool
    ├── PolyTool
    └── RectangularTool
```

## 事件流示例 1: 点击按钮组件

```
用户点击按钮组件
    ↓
AWT 生成 MouseEvent
    ↓
Canvas.MyListener.mousePressed(e)
    ↓
getToolFor(e) 返回 PokeTool (因为处于模拟模式)
    ↓
PokeTool.mousePressed(canvas, g, e)
    ↓
PokeTool 在点击位置查找组件
    ↓
找到 Button 组件，它是 Pokable 的
    ↓
创建 InstancePokerAdapter
    ↓
adapter.mousePressed() 调用 Button.Poker.mousePressed()
    ↓
Button.Poker 设置 value = TRUE
    ↓
触发电路状态更新
    ↓
Canvas 重新绘制，显示按钮被按下
```

## 事件流示例 2: 添加新组件

```
用户选择 AND Gate 工具后点击画布
    ↓
AWT 生成 MouseEvent
    ↓
Canvas.MyListener.mousePressed(e)
    ↓
getToolFor(e) 返回 AddTool (AND Gate)
    ↓
AddTool.mousePressed(canvas, g, e)
    ↓
AddTool 验证是否可以添加组件:
  - 检查循环依赖
  - 检查位置是否合法
    ↓
如果合法，创建组件实例
    ↓
开始拖动模式，等待用户确认位置
    ↓
用户再次点击或释放鼠标
    ↓
组件被添加到电路中
    ↓
Canvas 重新绘制，显示新组件
```

## 事件流示例 3: 选择和移动组件

```
用户在选择工具模式下点击组件
    ↓
AWT 生成 MouseEvent
    ↓
Canvas.MyListener.mousePressed(e)
    ↓
getToolFor(e) 返回 SelectTool
    ↓
SelectTool.mousePressed(canvas, g, e)
    ↓
SelectTool 检查点击位置:
  - 是否在现有选择中？
  - 是否按下 SHIFT 键？
    ↓
如果在现有选择中：
  - 开始拖动模式
  - state = MOVING
    ↓
如果不在选择中：
  - 如果按下 SHIFT: 添加到选择
  - 如果没按 SHIFT: 替换选择
    ↓
Canvas 重新绘制，显示选择状态
```

## 监听器注册位置

在 Canvas 构造函数中 (Canvas.java:794):

```java
addMouseListener(myListener);      // 注册鼠标事件监听器
addMouseMotionListener(myListener); // 注册鼠标移动监听器
addMouseWheelListener(myListener);  // 注册鼠标滚轮监听器
```

这三个方法调用使得 MyListener 能够接收所有的鼠标相关事件。

## 调试断点建议

如果要调试 mousePressed 相关问题，建议在以下位置设置断点：

1. **Canvas.java:185** - mousePressed 入口点
   - 查看所有鼠标按下事件
   - 检查 MouseEvent 的详细信息

2. **Canvas.java:206** - 获取工具
   - 查看 getToolFor() 返回了哪个工具
   - 检查工具选择逻辑

3. **Canvas.java:208** - 工具调用
   - 查看工具的 mousePressed 是如何被调用的
   - 检查传递的参数

4. **具体工具的 mousePressed 方法**
   - SelectTool.java:412
   - AddTool.java:390
   - PokeTool.java:234
   - 等等

5. **InstancePokerAdapter.java:132**
   - 查看组件交互的桥接过程

6. **具体 Poker 的 mousePressed 方法**
   - Button.Poker.mousePressed()
   - Pin.Poker.mousePressed()
   - 等等
