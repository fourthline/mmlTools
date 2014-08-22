/*
 * Copyright (C) 2013-2014 たんらる
 */

package fourthline.mmlTools.optimizer;

import java.util.ArrayList;
import java.util.List;

import fourthline.mmlTools.core.MMLTokenizer;


public final class MMLLengthKeyword {
	private String sectionName;
	private int index;
	private int score;
	private boolean deleteCandidate = false;

	public MMLLengthKeyword(String sectionName, int index) {
		this.sectionName = sectionName;
		this.index = index;
		this.score = 0;
	}

	public MMLLengthKeyword(String sectionName, int index, int score) {
		this.sectionName = sectionName;
		this.index = index;
		this.score = score;
	}

	public String getSectionName() {
		return sectionName;
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public int getScore() {
		return score;
	}

	public void incrScore() {
		score += sectionName.length();
	}

	public void addScore(MMLLengthKeyword mmlL) {
		score += mmlL.getScore();
	}

	public void setDeleteCandidate(boolean deleteCandidate) {
		this.deleteCandidate = deleteCandidate;
	}

	public boolean getDeleteCandidate() {
		return deleteCandidate;
	}

	@Override
	public String toString() {
		String str = index + ": L" + sectionName + "(" + score + ")";
		if (deleteCandidate) {
			str += "*";
		}

		return str;
	}

	public static List<MMLLengthKeyword> parseMMLLengthList(String mml) {
		MMLTokenizer tokenizer = new MMLTokenizer(mml);
		int noteCount = 0;
		ArrayList<MMLLengthKeyword> list = new ArrayList<>();

		while (tokenizer.hasNext()) {
			String token = tokenizer.next();
			char firstC = token.charAt(0);
			if (MMLTokenizer.isNote(firstC)) {
				noteCount++;
			} else if ( (firstC == 'l') || (firstC == 'L') ) {
				String sectionName = MMLTokenizer.noteNames(token)[1];
				list.add(new MMLLengthKeyword(sectionName, noteCount));
			}
		}

		return list;
	}
}
