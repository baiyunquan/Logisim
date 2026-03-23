/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.draw.tools;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.Icon;

import com.cburch.draw.toolbar.ToolbarItem;
import com.cburch.logisim.prefs.AppPreferences;

public class ToolbarToolItem implements ToolbarItem {
	private AbstractTool tool;
	private Icon icon;

	public ToolbarToolItem(AbstractTool tool) {
		this.tool = tool;
		this.icon = tool.getIcon();
	}

	@Override
	public Dimension getDimension(Object orientation) {
		if (icon == null) {
			int size = AppPreferences.getScaled(16);
			return new Dimension(size, size);
		} else {
			return new Dimension(AppPreferences.getScaled(icon.getIconWidth() + 8),
					AppPreferences.getScaled(icon.getIconHeight() + 8));
		}
	}

	public AbstractTool getTool() {
		return tool;
	}

	@Override
	public String getToolTip() {
		return tool.getDescription() + tool.getCtrlIndex();
	}

	@Override
	public boolean isSelectable() {
		return true;
	}

	@Override
	public void paintIcon(Component destination, Graphics g) {
		if (icon == null) {
			int x = AppPreferences.getScaled(4);
			int box = AppPreferences.getScaled(8);
			int x2 = AppPreferences.getScaled(12);
			g.setColor(new Color(255, 128, 128));
			g.fillRect(x, x, box, box);
			g.setColor(Color.BLACK);
			g.drawLine(x, x, x2, x2);
			g.drawLine(x, x2, x2, x);
			g.drawRect(x, x, box, box);
		} else {
			double scale = AppPreferences.getUiScaleFactor();
			if (g instanceof Graphics2D && scale != 1.0) {
				Graphics2D g2 = (Graphics2D) g.create();
				g2.scale(scale, scale);
				icon.paintIcon(destination, g2, 4, 4);
				g2.dispose();
			} else {
				icon.paintIcon(destination, g, 4, 4);
			}
		}
	}
}
