# Logisim中mousePressed的触发机制详解

## 问题：mousePressed究竟是如何触发的？

这是一个关于Logisim项目中鼠标按下事件（mousePressed）触发机制的完整说明文档。

## 整体架构

Logisim的鼠标事件处理采用了**层次化委托模式**，事件流如下：

```
Java AWT MouseListener → Canvas监听器 → 工具(Tools) → 组件交互器(Component Pokers)
```

## 详细触发流程

### 1. 事件入口点：Canvas.MyListener

**文件位置**: `Logisim-Fork/src/main/java/com/cburch/logisim/gui/main/Canvas.java:185-216`

当用户点击鼠标时，事件首先被Canvas类的内部监听器MyListener接收：

```java
@Override
public void mousePressed(MouseEvent e) {
    viewport.setErrorMessage(null, null);
    viewport.setInfoMessage(null, null);

    // 检查是否点击了缩放按钮
    if (e.getButton() == MouseEvent.BUTTON1 && viewport.zoomButtonVisible
            && AutoZoomButtonClicked(viewport.getSize(), e.getX() * getZoomFactor() - getHorizzontalScrollBar(),
                    e.getY() * getZoomFactor() - getVerticalScrollBar())) {
        viewport.zoomButtonColor = defaultzoomButtonColor.darker();
        viewport.repaint();
    } else {
        Canvas.this.requestFocus();
        drag_tool = getToolFor(e);  // 获取当前应该使用的工具
        if (drag_tool != null) {
            drag_tool.mousePressed(Canvas.this, getGraphics(), e);  // 委托给工具处理
            if (e.getButton() != MouseEvent.BUTTON1) {
                temp_tool = proj.getTool();
                proj.setTool(drag_tool);
            }
        }
        completeAction();
    }
}
```

### 2. 监听器注册

**文件位置**: `Canvas.java:794`

在Canvas构造函数中，MyListener被注册为鼠标事件监听器：

```java
addMouseListener(myListener);
addMouseMotionListener(myListener);
addMouseWheelListener(myListener);
```

这使得Canvas能够接收所有的AWT鼠标事件。

### 3. 工具选择机制

当鼠标按下时，Canvas通过`getToolFor(e)`方法确定应该使用哪个工具。这个方法会：
- 检查鼠标按键映射配置（MouseMappings）
- 根据当前鼠标按钮和修饰键确定工具
- 返回相应的Tool对象

### 4. Tool基类

**文件位置**: `Logisim-Fork/src/main/java/com/cburch/logisim/tools/Tool.java:95-96`

所有电路编辑工具都继承自Tool类：

```java
public void mousePressed(Canvas canvas, Graphics g, MouseEvent e) {
    // 默认为空实现，由子类覆盖
}
```

## 主要工具实现

### 电路编辑工具

1. **SelectTool** (`tools/SelectTool.java:412`)
   - 处理组件选择和移动
   - 检查点击是否在已选择的组件上
   - 配合SHIFT键添加/删除选择

2. **AddTool** (`tools/AddTool.java:390`)
   - 验证电路修改
   - 检查循环依赖
   - 开始组件放置流程

3. **PokeTool** (`tools/PokeTool.java:234`)
   - 处理模拟期间的交互式组件点击
   - 为导线和可点击组件创建Caret
   - 点击时高亮显示导线集

4. **EditTool** (`tools/EditTool.java:362`)
   - 组件属性编辑

5. **WiringTool** (`tools/WiringTool.java:284`)
   - 导线绘制和连接

6. **TextTool** (`tools/TextTool.java:230`)
   - 文本标签放置

7. **MenuTool** (`tools/MenuTool.java:132`)
   - 上下文菜单处理

### 绘图工具（用于外观编辑器）

**基类**: `com.cburch.draw.canvas.CanvasTool`

主要实现：
- **SelectTool** (`draw/tools/SelectTool.java:277`) - 形状选择
- **TextTool** (`draw/tools/TextTool.java:159`) - 文本绘制
- **LineTool** (`draw/tools/LineTool.java:104`) - 直线绘制
- **CurveTool** (`draw/tools/CurveTool.java:112`) - 曲线绘制
- **RectangularTool** (`draw/tools/RectangularTool.java:131`) - 矩形绘制

## 组件交互器（InstancePoker）

对于可以在模拟期间交互的组件，使用InstancePoker机制：

**基类**: `instance/InstancePoker.java:32-33`

```java
public void mousePressed(InstanceState state, MouseEvent e) {
    // 默认为空实现
}
```

### 主要实现

1. **Button.Poker** (`std/io/Button.java:44`)
   - 按下时设置值为TRUE，释放时设置为FALSE

2. **Pin.Poker** (`std/wiring/Pin.java:220`)
   - 跟踪多位引脚中按下的是哪一位

3. **Clock.Poker** (`std/wiring/Clock.java:53`)
   - 时钟组件交互

4. **Joystick.Poker** (`std/io/Joystick.java:38`)
   - 操纵杆控制

5. **Slider.Poker** (`std/io/Slider.java:55`)
   - 滑块控制

## 事件流示例：点击一个按钮组件

1. **用户点击鼠标** → Java AWT生成MouseEvent
2. **Canvas.MyListener.mousePressed()** 接收事件
3. **getToolFor(e)** 确定当前工具是PokeTool
4. **PokeTool.mousePressed()** 被调用
5. **PokeTool查找点击位置的组件** → 找到Button组件
6. **创建InstancePokerAdapter** 桥接到Button的Poker
7. **Button.Poker.mousePressed()** 被调用
8. **Button状态变为TRUE** → 触发电路更新

## 事件流示例：添加新组件

1. **用户点击鼠标** → Java AWT生成MouseEvent
2. **Canvas.MyListener.mousePressed()** 接收事件
3. **getToolFor(e)** 确定当前工具是AddTool
4. **AddTool.mousePressed()** 被调用
5. **验证电路修改合法性**（检查循环依赖等）
6. **开始组件放置过程**
7. **等待用户移动鼠标并再次点击确认位置**

## 关键类和接口总结

| 类/接口 | 文件路径 | 作用 |
|---------|---------|------|
| Canvas.MyListener | gui/main/Canvas.java:185 | 主要事件入口点 |
| Tool | tools/Tool.java:95 | 工具基类 |
| CanvasTool | draw/canvas/CanvasTool.java:44 | 绘图工具基类 |
| InstancePoker | instance/InstancePoker.java:32 | 组件交互器基类 |
| InstancePokerAdapter | instance/InstancePokerAdapter.java:132 | Tool系统和Poker的桥接 |
| Caret | tools/Caret.java:37 | 文本编辑接口 |

## 总结

Logisim的mousePressed触发机制采用了经典的**责任链模式**和**策略模式**：

1. **责任链**：事件从AWT → Canvas → Tool → Component层层传递
2. **策略模式**：不同的工具（Tool）提供不同的mousePressed实现
3. **适配器模式**：InstancePokerAdapter将Tool接口适配到InstancePoker接口

这种设计使得：
- **易于扩展**：添加新工具或新组件交互只需实现相应接口
- **职责清晰**：每一层只处理自己该处理的事件
- **灵活配置**：通过MouseMappings可以灵活配置鼠标按键到工具的映射

## 调试技巧

如果需要调试mousePressed相关的问题，可以在以下位置设置断点：

1. `Canvas.java:185` - 查看所有鼠标按下事件
2. `Canvas.java:206` - 查看选择了哪个工具
3. `Canvas.java:208` - 查看工具的mousePressed调用
4. 具体工具的mousePressed方法 - 查看工具的具体行为
5. `InstancePokerAdapter.java:132` - 查看组件交互转发

## 相关文档

- Canvas类: `src/main/java/com/cburch/logisim/gui/main/Canvas.java`
- Tool类: `src/main/java/com/cburch/logisim/tools/Tool.java`
- 所有工具实现: `src/main/java/com/cburch/logisim/tools/`目录
- 所有绘图工具: `src/main/java/com/cburch/draw/tools/`目录
