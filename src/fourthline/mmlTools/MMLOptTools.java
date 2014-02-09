package fourthline.mmlTools;

import fourthline.mmlTools.core.MMLTokenizer;
import fourthline.mmlTools.core.MelodyParser;
import fourthline.mmlTools.core.ParserWarn3ML;



/**
 * MML修正ツール
 * 連続している音符を置換する（ノイズ除去目的）
 * @author たんらる
 */
public class MMLOptTools {

	private String analysisString;
	private int replaceCount = 0;
	
	public int getReplaceCount() {
		return replaceCount;
	}

	private String orig_tick[] = {
			"32",
			"32.",
			"16",
			"16.",
			"8",
			"8.",
			"4",
			"4.",
			"2",
			"2.",
			"1",
			"1.",

			"6",
			"12",
			"24"
	
	};
	
	/**
	 * 変換パターン
	 * space: 休符
	 */
	private String pattern64_tick[] = {
			"64 64",
			"32 64",
			"21 64",
			"19. 64",
			"9 64",
			"8&21 64",
			"8&9 64",
			"4&9 64",
			"4.&9 64",
			"2&8&9 64",
			"2&4.&9 64",
			"1&4.&9 64",

			"9&24 64",
			"19&64 64",
			"38 64"
	};
	
	/**
	 * フルートノイズを消すための、後方64休符置換
	 * @param token （例：c4,f8,e-32）
	 * @return 合成後の token
	 */
	public String replaceNoise(String mml) throws UndefinedTickException {
		MMLTokenizer mt = new MMLTokenizer(mml);
		MelodyParser parser = new MelodyParser(null);
		int beforeNoteNumber = -1;
		String beforeToken = "";
		String anlyToken = "";
		String beforeWidth = "";
		StringBuilder sb = new StringBuilder();
		StringBuilder anly = new StringBuilder();
		replaceCount = 0;
		
		while (mt.hasNext()) {
			String token = mt.next();
			int gate = 0;

			try {
				gate = parser.noteGT(token);
			} catch (ParserWarn3ML e) {}
			
			int noteNumber = parser.getNoteNumber();
			if (gate <= 0)
				noteNumber = 0;
			
			if ( (gate > 0) && (noteNumber > 0) && (beforeNoteNumber == noteNumber) ) {
				System.out.println("...."+beforeToken);

				String note = toNote(beforeToken);
				
				beforeToken = replaceTail64(note, beforeWidth);
				anlyToken = " ["+note+"] ";
			}

			sb.append(beforeToken);
			anly.append(anlyToken);
			beforeToken = token;
			beforeNoteNumber = noteNumber;
			beforeWidth = parser.getGt();
			anlyToken = beforeToken;
		}

		sb.append(beforeToken);
		anly.append(anlyToken);
		analysisString = anly.toString();
		
		return sb.toString();
	}

	public String getAnalysisString() {
		return analysisString;
	}

	
	private String toNote(String token) {
		int noteIndex = 1;
		if ( (token.length() > 1) && (!Character.isDigit(token.charAt(1))) ) {
			noteIndex++;
		}
		
		String note = token.substring(0, noteIndex);
		
		return note;
	}
	
	
	/**
	 * 後方64休符置換
	 * @param token (例： c4, d16., f+32)
	 * @return 合成後の token
	 */
	public String replaceTail64(String note, String width) throws UndefinedTickException {		
		int patternIndex = -1;
		for (int i = 0; i < orig_tick.length; i++) {
			if ( width.equals(orig_tick[i]) ) {
				patternIndex = i;
				break;
			}
		}
		
		if (patternIndex < 0) {
			return note;
		}
		
		String pattern = pattern64_tick[patternIndex];
		int patternLength = pattern.length();
		StringBuilder sb = new StringBuilder(note);
		for (int i = 0; i < patternLength; i++) {
			char c = pattern.charAt(i);
			switch (c) {
			case '&':
				sb.append('&').append(note);
				break;
			case ' ':
				sb.append('r');
				break;
			default:
				sb.append(c);
				break;
			}
		}
		
		replaceCount++;
		
		return sb.toString();
	}
}
