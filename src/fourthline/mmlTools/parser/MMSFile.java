/*
 * Copyright (C) 2013 たんらる
 */

package fourthline.mmlTools.parser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

import fourthline.mmlTools.MMLScore;
import fourthline.mmlTools.MMLTrack;

/**
 * まきまびしーくさんのファイルフォーマットを扱います.
 * @author fourthline
 *
 */
public class MMSFile implements IMMLFileParser {

	@Override
	public MMLScore parse(File file) throws MMLParseException {
		MMLScore score = new MMLScore();
		BufferedReader reader = null;
		try {
			FileInputStream fisFile = new FileInputStream(file);
			InputStreamReader isReader = new InputStreamReader(fisFile, "Shift_JIS");
			reader = new BufferedReader(isReader);
			
			String s;
			while (true) {
				s = reader.readLine();
				if (s == null) {
					throw(new MMLParseException());
				}
			
				/* ヘッダチェック */
				if (s.equals("[mms-file]")) {
					break;
				}
			}
			
			/* バージョン */
			s = reader.readLine();
			if ( s == null ) {
				throw(new MMLParseException());
			}
			
			while ( (s = reader.readLine()) != null ) {
				if ( s.matches("\\[part[0-9]+\\]") ) {
					/* MMLパート */
					System.out.println("part");
					MMLTrack track = parseMMSPart(reader);
					System.out.println(track.getMML());
					System.out.println(track.getProgram());
					score.addTrack(track);
				}
			}
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {}
			}
		}
		
		return score;
	}

	/**
	 * mmsファイルのinstrument値は、DLSのものではないので変換を行います.
	 * @param mmsInst
	 * @return DLSのprogram値
	 */
	private int convertInstProgram(int mmsInst) {
		/* MMS->programへの変換テーブル */
		int table[] = new int[] {
			0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 
			10, 11, 12, 13, 14, 15, 16, 17, 18, 66, 
			67, 68,	69, 70, 71, 72, 73, 74, 75, 76, 
			18
		};
		
		return table[mmsInst];
	}
	
	private MMLTrack parseMMSPart(BufferedReader reader) throws IOException {
		String name = null;
		String mml1 = null;
		String mml2 = null;
		String mml3 = null;
		int program = 0;
		int panpot = 64;
		
		String s;
		while ( (s = reader.readLine()) != null ) {
			if ( s.startsWith("instrument=") ) {
				String str = s.substring("instrument=".length());
				program = Integer.parseInt(str);
				program = convertInstProgram(program);
			}
			else if ( s.startsWith("panpot=") ) {
				String str = s.substring("panpot=".length());
				panpot = Integer.parseInt(str);
				panpot += 64;
			}
			else if ( s.startsWith("name=") ) {
				name = s.substring("name=".length());
			}
			else if ( s.startsWith("ch0_mml=") ) {
				mml1 = s.substring("ch0_mml=".length());
			}
			else if ( s.startsWith("ch1_mml=") ) {
				mml2 = s.substring("ch1_mml=".length());
			}
			else if ( s.startsWith("ch2_mml=") ) {
				mml3 = s.substring("ch2_mml=".length());
				break;
			}
		}
		
		MMLTrack track = new MMLTrack(mml1, mml2, mml3, "");
		track.setProgram(program);
		track.setTrackName(name);
		track.setPanpot(panpot);
		return track;
	}
}
