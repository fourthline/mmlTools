/*
 * Copyright (C) 2013 たんらる
 */

package fourthline.dls_proto1;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import javax.sound.midi.*;

@SuppressWarnings("serial")
public class MMLPianoView extends JFrame {
	MabiDLS player;
	JLabel status;
	JComboBox<InstClass> cb;
	
	public MMLPianoView() {
		super("MML Piano View");
		this.player = MabiDLS.getInstance();
		
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setSize(640, 480);
		this.setLocationRelativeTo(null);
		this.setVisible(true);

		JPanel p1;
		Container contentPane = this.getContentPane();
		setLayout(new BorderLayout());
		contentPane.add(p1 = new JPanel(), BorderLayout.NORTH);
		contentPane.add(status = new JLabel("-"), BorderLayout.SOUTH);
		
		JScrollPane sc = new JScrollPane(new PianoRollCanvas(status),
				JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
				JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		contentPane.add(sc, BorderLayout.CENTER);
		
		// スクロールバーの位置調整
		JViewport vp = sc.getViewport();
		Point vp_point = vp.getViewPosition();
		vp_point.translate(0, 160);
		vp.setViewPosition(vp_point);
		
		cb = new JComboBox<InstClass>(player.getInsts());
		p1.add(new JLabel("楽器: "));
		p1.add(cb);
		
		InstClass obj = (InstClass)(cb.getSelectedItem());
		player.getChannel(0).programChange(obj.getBank(), obj.getProgram());
		
		cb.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				InstClass obj = (InstClass)(cb.getSelectedItem());
				if (player.getChannel(0).getProgram() != obj.getProgram()) {
					player.getChannel(0).programChange(obj.getProgram());
				}
			}
		});
	}
	

	public static void main(String[] args) {
		try {
			new MMLPianoView();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}


@SuppressWarnings("serial")
class PianoRollCanvas extends JPanel implements MouseListener, MouseMotionListener {
	MabiDLS player;
	JLabel status;
	
	public PianoRollCanvas(JLabel status) {
		this.player = MabiDLS.getInstance();
		this.status = status;
		
		setSize(getPreferredSize());
		setBackground(Color.white);
		addMouseListener(this);
		addMouseMotionListener(this);
	}
	
	public Dimension getPreferredSize() {
		return new Dimension(649, 649);
	}
	
	/**
	 * 1オクターブ 12 x 6
	 * 9オクターブ分つくると、648
	 */
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		
		for (int i = 0; i <= 9; i++) {
			paintOctPianoLine(g, i, (char)('0'+8-i));
		}
	}
	
	int play_note = -1;
	private int noteByY(int y) {
		int note = (9*12-(y/6)) -1 +12;
		MidiChannel ch0 = player.getChannel(0);
		if (y < 0) {
			ch0.allNotesOff();
			status.setText("-");
			play_note = -1;
			
			return 0;
		}
		if (note != play_note) {
			//		ch0.programChange(0, 2);
			ch0.noteOff(play_note);
			ch0.noteOn(note, 100);
			play_note = note;
		}
		status.setText(""+note);
		return 0;
	}
	
	private void paintOctPianoLine(Graphics g, int pos, char posText) {

		int white_wigth[] = { 10, 10, 10, 11, 10, 10, 11 };
		// ド～シのしろ鍵盤
		g.setColor(new Color(0.3f, 0.3f, 0.3f));
		
		int startY = 12*6*pos;
		int y = startY;
		for (int i = 0; i < white_wigth.length; i++) {
			g.drawRect(0, y, 40, white_wigth[i]);
			y += white_wigth[i];
		}
		// 黒鍵盤
		g.setColor(new Color(0.0f, 0.0f, 0.0f));
		g.fillRect(0, (0*10+5)+1+startY, 20, 6);
		g.fillRect(0, (1*10+5)+2+startY, 20, 6);
		g.fillRect(0, (2*10+5)+3+startY, 20, 6);
		g.fillRect(0, (4*10+5)+1+startY, 20, 6);
		g.fillRect(0, (5*10+5)+3+startY, 20, 6);
		g.setColor(new Color(0.3f, 0.3f, 0.3f));
		g.drawRect(0, (0*10+5)+1+startY, 20, 6);
		g.drawRect(0, (1*10+5)+2+startY, 20, 6);
		g.drawRect(0, (2*10+5)+3+startY, 20, 6);
		g.drawRect(0, (4*10+5)+1+startY, 20, 6);
		g.drawRect(0, (5*10+5)+3+startY, 20, 6);
		
		// グリッド
		y = startY;
		g.setColor(new Color(0.3f, 0.3f, 0.6f));
		g.drawLine(40, y, 100, y);
		for (int i = 1; i < 12; i++) {
			g.drawLine(60, i*6+y, 100, i*6+y);
		}
		
		// drawChars(char[] data, int offset, int length, int x, int y)
		char o_char[] = { 'o', posText };
		g.setFont(new Font("Arial", Font.PLAIN, 12));
		g.drawChars(o_char, 0, o_char.length, 42, startY+(12*6));
	}
	
	public void mouseClicked(MouseEvent e) {}
	public void mousePressed(MouseEvent e) {
		System.out.printf("press: %d %d\n",
				e.getX(),
				e.getY() );
		noteByY(e.getY());
	}
	public void mouseReleased(MouseEvent e) {
		noteByY(-1);
	}
	public void mouseEntered(MouseEvent e) {}
	public void mouseExited(MouseEvent e) {}
	
	public void mouseDragged(MouseEvent e) {
		System.out.printf("drag: %d %d\n",
				e.getX(),
				e.getY() );
		noteByY(e.getY());
	}
	public void mouseMoved(MouseEvent e) {}
}