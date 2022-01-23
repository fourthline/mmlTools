/*
 * Copyright (C) 2022 たんらる
 */
package fourthline.mabiicco;

import java.awt.Font;

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import fourthline.mmlTools.MMLEventList;
import fourthline.mmlTools.MMLTrack;
import fourthline.mmlTools.core.UndefinedTickException;
import fourthline.mmlTools.optimizer.MMLStringOptimizer;

public final class MMLTextTool extends JFrame {
	private static final long serialVersionUID = 8535487523093393294L;

	private static final int ROW = 10;
	private static final int COL = 48;

	private final JTextArea input = new JTextArea(ROW, COL);
	private final JTextArea output1 = new JTextArea(ROW, COL);
	private final JTextArea output2 = new JTextArea(ROW, COL);

	public class TLabel extends JPanel {
		private static final long serialVersionUID = -4107906220347424341L;
		public TLabel(String text) {
			add(new JLabel(text));
		}
	}

	public MMLTextTool() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		JPanel p = new JPanel();
		p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
		p.add(new TLabel("Input"));
		p.add(input);
		p.add(new TLabel("Output1"));
		p.add(output1);
		p.add(new TLabel("Output2"));
		p.add(output2);
		setContentPane(p);

		input.setFont(Font.getFont("consolas"));
		input.setLineWrap(true);
		output1.setFont(Font.getFont("consolas"));
		output1.setEditable(false);
		output1.setLineWrap(true);
		output2.setFont(Font.getFont("consolas"));
		output2.setEditable(false);
		output2.setLineWrap(true);

		input.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void removeUpdate(DocumentEvent e) {
				updateText();
			}

			@Override
			public void insertUpdate(DocumentEvent e) {
				updateText();
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
				updateText();
			}
		});
		pack();
	}

	private String makeText(MMLTrack t) throws UndefinedTickException {
		t.generate();
		return t.mmlRankFormat() + "\n" + t.getMabiMML();
	}

	private String makeText(String s) {
		return "length = " + s.length() + "\n" + s;
	}

	private void updateText() {
		try {
			String text = input.getText();
			if (text.startsWith("MML@")) {
				MMLTrack track = new MMLTrack().setMML(text);
				output1.setText(makeText(track));
				track.setMabiMMLOptimizeFunc(t -> t.optimizeGen2());
				output2.setText(makeText(track));
			} else {
				var o = new MMLStringOptimizer(new MMLEventList(text).toMMLString());
				var s1 = o.toString();
				var s2 = o.optimizeGen2();
				output1.setText(makeText(s1));
				output2.setText(makeText(s2));
			}
		} catch (UndefinedTickException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		new MMLTextTool().setVisible(true);
	}
}
