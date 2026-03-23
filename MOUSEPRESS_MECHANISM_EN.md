# Detailed Explanation of mousePressed Trigger Mechanism in Logisim

## Question: How exactly is mousePressed triggered?

This document provides a comprehensive explanation of the mouse press event (mousePressed) trigger mechanism in the Logisim project.

## Overall Architecture

Logisim's mouse event handling follows a **hierarchical delegation pattern**:

```
Java AWT MouseListener → Canvas Listeners → Tools → Component Pokers
```

## Detailed Trigger Flow

### 1. Event Entry Point: Canvas.MyListener

**File Location**: `Logisim-Fork/src/main/java/com/cburch/logisim/gui/main/Canvas.java:185-216`

When a user clicks the mouse, the event is first received by the MyListener inner class in the Canvas class:

```java
@Override
public void mousePressed(MouseEvent e) {
    viewport.setErrorMessage(null, null);
    viewport.setInfoMessage(null, null);

    // Check if zoom button was clicked
    if (e.getButton() == MouseEvent.BUTTON1 && viewport.zoomButtonVisible
            && AutoZoomButtonClicked(viewport.getSize(), e.getX() * getZoomFactor() - getHorizzontalScrollBar(),
                    e.getY() * getZoomFactor() - getVerticalScrollBar())) {
        viewport.zoomButtonColor = defaultzoomButtonColor.darker();
        viewport.repaint();
    } else {
        Canvas.this.requestFocus();
        drag_tool = getToolFor(e);  // Get the current tool to use
        if (drag_tool != null) {
            drag_tool.mousePressed(Canvas.this, getGraphics(), e);  // Delegate to tool
            if (e.getButton() != MouseEvent.BUTTON1) {
                temp_tool = proj.getTool();
                proj.setTool(drag_tool);
            }
        }
        completeAction();
    }
}
```

### 2. Listener Registration

**File Location**: `Canvas.java:794`

In the Canvas constructor, MyListener is registered as a mouse event listener:

```java
addMouseListener(myListener);
addMouseMotionListener(myListener);
addMouseWheelListener(myListener);
```

This allows Canvas to receive all AWT mouse events.

### 3. Tool Selection Mechanism

When the mouse is pressed, Canvas determines which tool to use through the `getToolFor(e)` method. This method:
- Checks mouse button mapping configuration (MouseMappings)
- Determines the tool based on current mouse button and modifier keys
- Returns the corresponding Tool object

### 4. Tool Base Class

**File Location**: `Logisim-Fork/src/main/java/com/cburch/logisim/tools/Tool.java:95-96`

All circuit editing tools inherit from the Tool class:

```java
public void mousePressed(Canvas canvas, Graphics g, MouseEvent e) {
    // Empty default implementation, overridden by subclasses
}
```

## Main Tool Implementations

### Circuit Editing Tools

1. **SelectTool** (`tools/SelectTool.java:412`)
   - Handles component selection and movement
   - Checks if click is on already selected components
   - Adds/removes from selection with SHIFT key

2. **AddTool** (`tools/AddTool.java:390`)
   - Validates circuit modifications
   - Checks for circular dependencies
   - Initiates component placement process

3. **PokeTool** (`tools/PokeTool.java:234`)
   - Handles interactive component clicks during simulation
   - Creates Carets for wires and pokable components
   - Highlights wire sets on click

4. **EditTool** (`tools/EditTool.java:362`)
   - Component attribute editing

5. **WiringTool** (`tools/WiringTool.java:284`)
   - Wire drawing and connection

6. **TextTool** (`tools/TextTool.java:230`)
   - Text label placement

7. **MenuTool** (`tools/MenuTool.java:132`)
   - Context menu handling

### Drawing Tools (for Appearance Editor)

**Base Class**: `com.cburch.draw.canvas.CanvasTool`

Main implementations:
- **SelectTool** (`draw/tools/SelectTool.java:277`) - Shape selection
- **TextTool** (`draw/tools/TextTool.java:159`) - Text drawing
- **LineTool** (`draw/tools/LineTool.java:104`) - Line drawing
- **CurveTool** (`draw/tools/CurveTool.java:112`) - Curve drawing
- **RectangularTool** (`draw/tools/RectangularTool.java:131`) - Rectangle drawing

## Component Pokers (InstancePoker)

For components that can be interacted with during simulation, the InstancePoker mechanism is used:

**Base Class**: `instance/InstancePoker.java:32-33`

```java
public void mousePressed(InstanceState state, MouseEvent e) {
    // Empty default implementation
}
```

### Main Implementations

1. **Button.Poker** (`std/io/Button.java:44`)
   - Sets value to TRUE on press, FALSE on release

2. **Pin.Poker** (`std/wiring/Pin.java:220`)
   - Tracks which bit is pressed for multi-bit pins

3. **Clock.Poker** (`std/wiring/Clock.java:53`)
   - Clock component interaction

4. **Joystick.Poker** (`std/io/Joystick.java:38`)
   - Joystick control

5. **Slider.Poker** (`std/io/Slider.java:55`)
   - Slider control

## Event Flow Example: Clicking a Button Component

1. **User clicks mouse** → Java AWT generates MouseEvent
2. **Canvas.MyListener.mousePressed()** receives the event
3. **getToolFor(e)** determines current tool is PokeTool
4. **PokeTool.mousePressed()** is called
5. **PokeTool finds component at click location** → finds Button component
6. **Creates InstancePokerAdapter** to bridge to Button's Poker
7. **Button.Poker.mousePressed()** is called
8. **Button state becomes TRUE** → triggers circuit update

## Event Flow Example: Adding a New Component

1. **User clicks mouse** → Java AWT generates MouseEvent
2. **Canvas.MyListener.mousePressed()** receives the event
3. **getToolFor(e)** determines current tool is AddTool
4. **AddTool.mousePressed()** is called
5. **Validates circuit modification legality** (checks circular dependencies, etc.)
6. **Initiates component placement process**
7. **Waits for user to move mouse and click again to confirm position**

## Key Classes and Interfaces Summary

| Class/Interface | File Path | Purpose |
|----------------|-----------|---------|
| Canvas.MyListener | gui/main/Canvas.java:185 | Main event entry point |
| Tool | tools/Tool.java:95 | Tool base class |
| CanvasTool | draw/canvas/CanvasTool.java:44 | Drawing tool base class |
| InstancePoker | instance/InstancePoker.java:32 | Component poker base class |
| InstancePokerAdapter | instance/InstancePokerAdapter.java:132 | Bridge between Tool system and Poker |
| Caret | tools/Caret.java:37 | Text editing interface |

## Summary

Logisim's mousePressed trigger mechanism uses classic **Chain of Responsibility** and **Strategy** patterns:

1. **Chain of Responsibility**: Events pass from AWT → Canvas → Tool → Component layer by layer
2. **Strategy Pattern**: Different tools provide different mousePressed implementations
3. **Adapter Pattern**: InstancePokerAdapter adapts the Tool interface to the InstancePoker interface

This design provides:
- **Easy extensibility**: Adding new tools or component interactions only requires implementing the appropriate interface
- **Clear responsibilities**: Each layer only handles events it should handle
- **Flexible configuration**: MouseMappings allows flexible configuration of mouse button to tool mappings

## Debugging Tips

If you need to debug mousePressed-related issues, set breakpoints at these locations:

1. `Canvas.java:185` - See all mouse press events
2. `Canvas.java:206` - See which tool was selected
3. `Canvas.java:208` - See the tool's mousePressed call
4. Specific tool's mousePressed method - See the tool's specific behavior
5. `InstancePokerAdapter.java:132` - See component interaction forwarding

## Related Documentation

- Canvas class: `src/main/java/com/cburch/logisim/gui/main/Canvas.java`
- Tool class: `src/main/java/com/cburch/logisim/tools/Tool.java`
- All tool implementations: `src/main/java/com/cburch/logisim/tools/` directory
- All drawing tools: `src/main/java/com/cburch/draw/tools/` directory

## Complete List of mousePressed Implementations

### Circuit Tools (47+ implementations found)

1. Canvas.MyListener - Main entry point
2. SelectTool - Component selection
3. AddTool - Component addition
4. PokeTool - Interactive component clicking
5. EditTool - Attribute editing
6. WiringTool - Wire drawing
7. TextTool - Text labels
8. MenuTool - Context menus

### Drawing Tools

9. draw.SelectTool - Shape selection
10. draw.TextTool - Text drawing
11. LineTool - Line drawing
12. CurveTool - Curve drawing
13. PolyTool - Polygon drawing
14. RectangularTool - Rectangle drawing
15. AbstractTool - Base implementation

### Component Pokers

16. Button.Poker
17. Pin.Poker
18. Clock.Poker
19. Joystick.Poker
20. Slider.Poker
21. AbstractFlipFlop.Poker
22. ShiftRegisterPoker
23. SubcircuitPoker

### Adapter/Helper Classes

24. InstancePokerAdapter
25. AbstractCaret
26. TextFieldCaret

### UI Components

27. AppearanceCanvas
28. ProjectExplorer
29. SimulationExplorer
30. ZoomControl.GridIcon
31. LayoutPopupManager
32. TruthTableMouseListener
33. TableTabCaret
34. MouseOptions
35. HorizontalSplitPane.Dragbar
36. ToolbarButton
37. Toolbar
38. hex.Caret

### Memory Components

39-47. ROM, PlaRom, ProgrammableGenerator ContentsCell classes

This comprehensive architecture ensures clean separation of concerns and maintainable, extensible code.
