/*
 * Copyright (C) 2017 たんらる
 */

package fourthline.mabiicco.ui.editor;

import java.awt.BorderLayout;
import java.util.function.Consumer;
import java.util.function.Supplier;

import javax.swing.JButton;
import javax.swing.JMenu;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSlider;

import static fourthline.mabiicco.AppResource.appText;


/**
 * ポップアップメニューで音量を変更する.
 */
public class VelocityChangeMenu {

	private final JSlider slider = new JSlider(0, 15, 8);
	private final JMenu menu = new JMenu(appText("edit.velocity"));
	private final JButton defaultButton = new JButton(appText("edit.default"));
	private final JButton applyButton = new JButton(appText("edit.apply"));
	private final JButton cancelButton = new JButton(appText("edit.cancel"));

	/**
	 * @param parent メニューを登録する親
	 * @param getter デフォルト値取得時のgetter
	 * @param setter 値反映時のsetter
	 */
	public VelocityChangeMenu(JPopupMenu parent, Supplier<Integer> getter, Consumer<Integer> setter) {
		slider.setSnapToTicks(true);
		slider.setPaintTicks(true);
		slider.setMinorTickSpacing(1);
		slider.setMajorTickSpacing(3);
		slider.setPaintLabels(true);

		defaultButton.addActionListener(t -> {
			int v = getter.get();
			setValue(v);
		});
		applyButton.addActionListener(t -> {
			setter.accept(slider.getValue());
			parent.setVisible(false);
		});
		cancelButton.addActionListener(t -> {
			parent.setVisible(false);
		});

		JPanel p1 = new JPanel();
		p1.add(defaultButton);
		p1.add(applyButton);
		p1.add(cancelButton);

		JPanel p2 = new JPanel(new BorderLayout());
		p2.add(slider, BorderLayout.CENTER);

		JPanel p3 = new JPanel(new BorderLayout());
		p3.add(p2, BorderLayout.NORTH);
		p3.add(p1, BorderLayout.CENTER);

		menu.add(p3);
		parent.add(menu);
	}

	public void setValue(int v) {
		slider.setValue(v);
	}
}
