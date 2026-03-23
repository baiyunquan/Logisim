/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.gui.prefs;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JComboBox;
import javax.swing.JLabel;

import com.cburch.logisim.prefs.PrefMonitor;
import com.cburch.logisim.util.StringGetter;

class PrefOptionListDouble implements ActionListener, PropertyChangeListener {
	private PrefMonitor<Double> pref;
	private StringGetter labelStr;

	private JLabel label;
	private JComboBox<PrefOption> combo;

	public PrefOptionListDouble(PrefMonitor<Double> pref, StringGetter labelStr, PrefOption[] options) {
		this.pref = pref;
		this.labelStr = labelStr;

		label = new JLabel(labelStr.get() + " ");
		combo = new JComboBox<PrefOption>();
		for (PrefOption opt : options) {
			combo.addItem(opt);
		}
		combo.addActionListener(this);
		pref.addPropertyChangeListener(this);
		selectOption(pref.get());
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		PrefOption x = (PrefOption) combo.getSelectedItem();
		Object value = x.getValue();
		if (value instanceof Number) {
			pref.set(Double.valueOf(((Number) value).doubleValue()));
		}
	}

	JComboBox<PrefOption> getJComboBox() {
		return combo;
	}

	JLabel getJLabel() {
		return label;
	}

	void localeChanged() {
		label.setText(labelStr.get() + " ");
	}

	@Override
	public void propertyChange(PropertyChangeEvent event) {
		if (pref.isSource(event)) {
			selectOption(pref.get());
		}
	}

	private void selectOption(Object value) {
		for (int i = combo.getItemCount() - 1; i >= 0; i--) {
			PrefOption opt = combo.getItemAt(i);
			if (opt.getValue().equals(value)) {
				combo.setSelectedItem(opt);
				return;
			}
		}
		combo.setSelectedItem(combo.getItemAt(0));
	}
}
