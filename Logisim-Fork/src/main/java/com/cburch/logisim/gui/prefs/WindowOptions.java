/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.gui.prefs;

import javax.swing.JPanel;

import com.cburch.logisim.data.Direction;
import com.cburch.logisim.prefs.AppPreferences;
import com.cburch.logisim.util.StringGetter;
import com.cburch.logisim.util.TableLayout;

class WindowOptions extends OptionsPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1043476425449770400L;
	private PrefBoolean[] checks;
	private PrefOptionList toolbarPlacement, Refreshrate;
	private PrefOptionListDouble uiScale;

	private static StringGetter fixed(final String value) {
		return new StringGetter() {
			@Override
			public String get() {
				return value;
			}
		};
	}

	public WindowOptions(PreferencesFrame window) {
		super(window);

		checks = new PrefBoolean[] {
				new PrefBoolean(AppPreferences.SHOW_TICK_RATE, Strings.getter("windowTickRate")), };

		toolbarPlacement = new PrefOptionList(AppPreferences.TOOLBAR_PLACEMENT, Strings.getter("windowToolbarLocation"),
				new PrefOption[] { new PrefOption(Direction.NORTH.toString(), Direction.NORTH.getDisplayGetter()),
						new PrefOption(Direction.SOUTH.toString(), Direction.SOUTH.getDisplayGetter()),
						new PrefOption(Direction.EAST.toString(), Direction.EAST.getDisplayGetter()),
						new PrefOption(Direction.WEST.toString(), Direction.WEST.getDisplayGetter()),
						new PrefOption(AppPreferences.TOOLBAR_DOWN_MIDDLE, Strings.getter("windowToolbarDownMiddle")),
						new PrefOption(AppPreferences.TOOLBAR_HIDDEN, Strings.getter("windowToolbarHidden")) });
		Refreshrate = new PrefOptionList(AppPreferences.REFRESH_RATE, Strings.getter("windowRefreshRate"),
				new PrefOption[] { new PrefOption("20", Strings.getter("20Hz")),
						new PrefOption("30", Strings.getter("30Hz")), new PrefOption("60", Strings.getter("60Hz")),
						new PrefOption("120", Strings.getter("120Hz")),
						new PrefOption("144", Strings.getter("144Hz")) });
		uiScale = new PrefOptionListDouble(AppPreferences.UI_SCALE, fixed("UI Scale"),
				new PrefOption[] { new PrefOption(Double.valueOf(0.75), fixed("75%")),
						new PrefOption(Double.valueOf(1.0), fixed("100%")),
						new PrefOption(Double.valueOf(1.25), fixed("125%")),
						new PrefOption(Double.valueOf(1.5), fixed("150%")),
						new PrefOption(Double.valueOf(1.75), fixed("175%")),
						new PrefOption(Double.valueOf(2.0), fixed("200%")) });
		JPanel panel = new JPanel(new TableLayout(2));

		panel.add(toolbarPlacement.getJLabel());
		panel.add(toolbarPlacement.getJComboBox());
		panel.add(Refreshrate.getJLabel());
		panel.add(Refreshrate.getJComboBox());
		panel.add(uiScale.getJLabel());
		panel.add(uiScale.getJComboBox());
		setLayout(new TableLayout(1));
		for (int i = 0; i < checks.length; i++) {
			add(checks[i]);
		}
		add(panel);
	}

	@Override
	public String getHelpText() {
		return Strings.get("windowHelp");
	}

	@Override
	public String getTitle() {
		return Strings.get("windowTitle");
	}

	@Override
	public void localeChanged() {
		for (int i = 0; i < checks.length; i++) {
			checks[i].localeChanged();
		}
		toolbarPlacement.localeChanged();
		Refreshrate.localeChanged();
		uiScale.localeChanged();
	}
}
