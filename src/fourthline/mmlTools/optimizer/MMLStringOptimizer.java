/*
 * Copyright (C) 2013 たんらる
 */

package fourthline.mmlTools.optimizer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import fourthline.mmlTools.core.MMLTokenizer;



/**
 * FIXME: 
 * Ln-section merge式
 * @author fourthline
 */
public class MMLStringOptimizer {

	private String originalMML;

	/**
	 * @param mml   MMLEventListで出力したMML文字列.
	 */
	public MMLStringOptimizer(String mml) {
		originalMML = mml;
	}

	public String toString() {
		return getOptimizedString();
	}

	private boolean updateCounter(HashMap<String, Integer> counter, String s) {
		Integer count = counter.get(s);
		boolean result = false;
		if ( (count == null) || (count == 0) ) {
			count = 0;
			result = true;
		}
		count += s.length();
		counter.put(s, count);

		return result;
	}

	private boolean isMaxScore(HashMap<String, Integer> counter, String s) {
		Collection<String> keys = counter.keySet();
		int target = counter.get(s) - s.length() - 1;

		if (target < 0) {
			return false;
		}

		for (String key : keys) {
			if (key.equals(s)) {
				continue;
			}
			int score = counter.get(key);
			if (key.equals(primaryLength)) {
				score += primaryLength.length() + 1;
			}
			if (target <= score) {
				return false;
			}
		}

		return true;
	}

	private boolean updateScore(String s, int index) {
		if (updateCounter(counter, s)) {
			countStartIndex.put(s, index);
		}
		if (isMaxScore(counter, s)) {
			primaryLength = s;
			int score = counter.get(s);
			stack.add(new MMLLengthKeyword(s, countStartIndex.get(s), score));
			counter.clear();
			return true;
		}

		return false;
	}

	private void endScore() {
		Collection<String> keys = counter.keySet();
		int maxScore = 0;
		String s = null;
		for (String key : keys) {
			int score = counter.get(key);
			if (maxScore < score) {
				maxScore = score;
				s = key;
			}
		}

		if (s != null) {
			stack.add(new MMLLengthKeyword(s, countStartIndex.get(s), maxScore));
		}
	}

	private void primaryLengthScore() {
		Collection<String> keys = counter.keySet();
		int primaryScore = primaryLength.length();
		for (String key : keys) {
			int score = counter.get(key).intValue() - primaryScore;
			if (score < 0) {
				score = 0;
			}
			counter.put(key, score);
		}
	}

	private String primaryLength;
	private HashMap<String, Integer> counter = new HashMap<String, Integer>();
	private HashMap<String, Integer> countStartIndex = new HashMap<String, Integer>();
	private ArrayList<MMLLengthKeyword> stack = new ArrayList<MMLLengthKeyword>();


	private String[] parseLengthArray() {
		MMLTokenizer tokenizer = new MMLTokenizer(originalMML);
		String section = "4";
		ArrayList<String> list = new ArrayList<String>();

		while (tokenizer.hasNext()) {
			String token = tokenizer.next();
			char firstC = token.charAt(0);
			if (MMLTokenizer.isNote(token.charAt(0))) {
				String s[] = MMLTokenizer.noteNames(token);
				String noteLength = s[1];
				if (noteLength.equals("")) {
					noteLength = section;
				} else if (noteLength.equals(".")) {
					noteLength = section + ".";
				}
				list.add(noteLength);
			} else if ( (firstC == 'l') || (firstC == 'L') ) {
				section = MMLTokenizer.noteNames(token)[1];
			}
		}

		String resultArray[] = new String[list.size()];
		return list.toArray(resultArray);
	}

	private void optimizedLengthArray() {
		String originalList[] = parseLengthArray();
		if (originalList == null) {
			return;
		}
		primaryLength = "4";
		stack.add(new MMLLengthKeyword("4", 0, 0));
		for (int i = 0; i < originalList.length; i++) {
			String s = originalList[i];
			if (s.equals(primaryLength)) {
				if (stack.size() > 0) {
					stack.get(stack.size()-1).incrScore();
				}
				primaryLengthScore();
			} else {
				if (!updateScore(s, i)) {
					if (s.endsWith(".")) {
						updateScore(s.substring(0, s.length()-1), i);
					}
				}
			}
		}

		endScore();
	}

	private int searchNextSectionIndex(int startIndex) {
		int count = stack.size();
		String searchName = stack.get(startIndex).getSectionName();
		for (int i = startIndex+1; i < count; i++) {
			String sectionName = stack.get(i).getSectionName();
			if (searchName.equals(sectionName)) {
				return i;
			}
		}

		return -1;
	}

	private int blockScore(int startIndex, int endIndex) {
		int score = 0;
		for (int i = startIndex+1; i < endIndex; i++) {
			score += stack.get(i).getScore();
		}

		return score;
	}

	private int lCost(int startIndex, int endIndex) {
		int lCost = 0;
		for (int i = startIndex+1; i <= endIndex; i++) {
			lCost += stack.get(i).getSectionName().length();
			lCost += "L".length();
		}

		return lCost;
	}

	private void mergeSection(int startIndex, int endIndex) {
		for (int i = startIndex+1; i < endIndex; i++) {
			stack.get(i).setDeleteCandidate(true);
		}
	}

	private void mergeSection2(int startIndex, int endIndex) {
		MMLLengthKeyword lKey = stack.get(startIndex);
		if (lKey.getDeleteCandidate()) {
			// クロスセクションのスコア判定.
			int score = lKey.getScore();
			for (int i = startIndex+1; i < endIndex; i++) {
				if (score < stack.get(i).getScore()) {
					return;
				}
			}
		}

		// セクションマージ.
		lKey.addScore(stack.get(endIndex));
		for (int i = startIndex+1; i <= endIndex; i++) {
			stack.remove(startIndex+1);
		}
	}

	private boolean sectionForwardMerge() {
		boolean result = false;
		for (int i = stack.size()-2; i >= 0; i--) {
			int nextIndex = searchNextSectionIndex(i);
			if (nextIndex < 0) {
				continue;
			}

			int score = blockScore(i, nextIndex);
			int cost = lCost(i, nextIndex);

			if (score <= cost) {
				mergeSection(i, nextIndex);
				result = true;
			}
		}

		return result;
	}

	private void sectionForwardMerge2() {
		for (int i = stack.size()-2; i >= 0; i--) {
			int nextIndex = searchNextSectionIndex(i);
			if (nextIndex < 0) {
				continue;
			}

			int score = blockScore(i, nextIndex);
			int cost = lCost(i, nextIndex);

			if (score <= cost) {
				mergeSection2(i, nextIndex);
			}
		}
	}

	private void trimSection() {
		if (stack.size() <= 0) {
			return;
		}
		stack.remove(0);
		int index = stack.size() - 1;
		if (index < 0) {
			return;
		}
		MMLLengthKeyword lKey = stack.get(index);
		String section = lKey.getSectionName();
		if (lKey.getScore() <= section.length()+1) {
			stack.remove(index);
		}
	}

	private String optimizeMMLString() {
		StringBuilder sb = new StringBuilder();
		MMLTokenizer tokenizer = new MMLTokenizer(originalMML);
		String section = "4";
		int noteCount = 0;
		String prevToken = "";
		String token = "";

		while (tokenizer.hasNext()) {
			prevToken = token;
			token = tokenizer.next();
			if (MMLTokenizer.isNote(token.charAt(0))) {
				String s[] = MMLTokenizer.noteNames(token);
				String noteName = s[0];
				String noteLength = s[1];

				if ( (stack.size() > 0) && 
						(stack.get(0).getIndex()) <= noteCount) {
					section = stack.get(0).getSectionName();
					stack.remove(0);
					sb.append("l" + section);
					if (prevToken.equals("&")) {
						/* Lの直前に '&' があると、効かなくなるため. */
						sb.deleteCharAt(sb.length()-section.length()-2);
						sb.append('&');
					}
				}

				sb.append(noteName);
				if (noteLength.equals(section)) {
				} else if (noteLength.equals(section+".")) {
					sb.append(".");
				} else {
					sb.append(noteLength);
				}
				noteCount++;
			} else {
				sb.append(token);
			}
		}

		return sb.toString();
	}

	private String optimizedString = null;
	private String getOptimizedString() {
		if (optimizedString == null) {
			optimizedLengthArray();
			sectionForwardMerge();
			sectionForwardMerge2();
			trimSection();
			optimizedString = optimizeMMLString();
		}

		return optimizedString;
	}

	public static void main(String args[]) {
		String mml = "c8c8c8c8";
		MMLStringOptimizer optimizer = new MMLStringOptimizer(mml);
		String optimizedMML = optimizer.toString();

		System.out.println("expect: "+MMLLengthKeyword.parseMMLLengthList(mml));
		System.out.println("actual: "+MMLLengthKeyword.parseMMLLengthList(optimizedMML));
		System.out.println(optimizedMML);
	}
}

