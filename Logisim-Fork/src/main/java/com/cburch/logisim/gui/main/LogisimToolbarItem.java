/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.gui.main;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Component;
import java.awt.Composite;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.Icon;

import com.cburch.draw.toolbar.ToolbarItem;
import com.cburch.logisim.gui.menu.LogisimMenuItem;
import com.cburch.logisim.prefs.AppPreferences;
import com.cburch.logisim.util.Icons;
import com.cburch.logisim.util.StringGetter;

class LogisimToolbarItem implements ToolbarItem {
	private MenuListener menu;
	private Icon icon;
	private LogisimMenuItem action;
	private StringGetter toolTip;

	public LogisimToolbarItem(MenuListener menu, String iconName, LogisimMenuItem action, StringGetter toolTip) {
		this.menu = menu;
		this.icon = Icons.getIcon(iconName);
		this.action = action;
		this.toolTip = toolTip;
	}

	public void doAction() {
		if (menu != null && menu.isEnabled(action)) {
			menu.doAction(action);
		}
	}

	@Override
	public Dimension getDimension(Object orientation) {
		if (icon == null) {
			int size = AppPreferences.getScaled(16);
			return new Dimension(size, size);
		} else {
			int w = icon.getIconWidth();
			int h = icon.getIconHeight();
			return new Dimension(AppPreferences.getScaled(w), AppPreferences.getScaled(h + 2));
		}
	}

	@Override
	public String getToolTip() {
		if (toolTip != null) {
			return toolTip.get();
		} else {
			return null;
		}
	}

	@Override
	public boolean isSelectable() {
		return menu != null && menu.isEnabled(action);
	}

	@Override
	public void paintIcon(Component destination, Graphics g) {
		Graphics gToUse = g;
		Graphics2D g2 = null;
		double scale = AppPreferences.getUiScaleFactor();
		if (g instanceof Graphics2D) {
			g2 = (Graphics2D) g.create();
			gToUse = g2;
			if (scale != 1.0) {
				g2.scale(scale, scale);
			}
		}

		if (!isSelectable() && gToUse instanceof Graphics2D) {
			Composite c = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.3f);
			((Graphics2D) gToUse).setComposite(c);
		}

		if (icon == null) {
			gToUse.setColor(new Color(255, 128, 128));
			gToUse.fillRect(4, 4, 8, 8);
			gToUse.setColor(Color.BLACK);
			gToUse.drawLine(4, 4, 12, 12);
			gToUse.drawLine(4, 12, 12, 4);
			gToUse.drawRect(4, 4, 8, 8);
		} else {
			icon.paintIcon(destination, gToUse, 0, 1);
		}

		if (g2 != null) {
			g2.dispose();
		}
	}

	public void setIcon(String iconName) {
		this.icon = Icons.getIcon(iconName);
	}

	public void setToolTip(StringGetter toolTip) {
		this.toolTip = toolTip;
	}
}
