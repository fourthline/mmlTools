/*
 * Copyright (C) 2013-2020 たんらる
 */

package fourthline.mmlTools.core;


import java.util.Arrays;
import java.util.List;


/**
 * 音符の時間変換値
 * @author たんらる
 */
public final class MMLTicks {

	private static final MMLTickTable tickTable = MMLTickTable.createTickTable();

	public static int getTick(String gt) throws UndefinedTickException {
		String str = gt;
		while (!tickTable.getTable().containsKey(str)) {
			if (str.length() == 0) {
				break;
			}
			char ch = str.charAt(str.length()-1);
			if (!Character.isDigit(ch)) {
				int len = str.length();
				str = str.substring(0, len - 1);
			} else {
				throw new UndefinedTickException(gt);
			}
		}

		return tickTable.getTable().get(str);
	}

	private static Integer minimum = null;
	public static int minimumTick() {
		if (minimum != null) {
			return minimum;
		}

		Integer v = null;
		for (Integer i : tickTable.getTable().values()) {
			if ( (v == null) || (i < v) ) {
				v = i;
			}
		}
		minimum = v;
		return minimum;
	}


	private String noteName;
	int tick;
	boolean needTie;

	/**
	 * tick長をMML文字列に変換します.
	 * @param noteName ノート名
	 * @param tick tick長
	 */
	public MMLTicks(String noteName, int tick) {
		this(noteName, tick, true);
	}

	/**
	 * tick長をMML文字列に変換します.
	 * @param noteName ノート名
	 * @param tick tick長
	 * @param needTie noteNameを連結するときに tie が必要かどうかを指定します. 休符 or 音量ゼロのときは, falseを指定してください.
	 */
	public MMLTicks(String noteName, int tick, boolean needTie) {
		this.noteName = noteName;
		this.tick = tick;
		this.needTie = needTie;
	}

	private String mmlNotePart(String phoneticString) {
		return mmlNotePart(Arrays.asList(phoneticString));
	}

	private String mmlNotePart(List<String> phoneticString) {
		StringBuilder sb = new StringBuilder();

		phoneticString.forEach(t -> {
			if (needTie) {
				sb.append('&');
			}
			sb.append(noteName).append(t);
		});

		return sb.toString();
	}

	private String makeMMLText(StringBuilder sb, int remTick) throws UndefinedTickException {
		// 1~64の分割
		if (remTick > 0) {
			for (int base = 1; base <= 64; base *= 2) {
				int baseTick = getTick(""+base);
				if (tickTable.getInvTable().containsKey(remTick)) {
					sb.append( mmlNotePart(tickTable.getInvTable().get(remTick)) );
					remTick = 0;
					break;
				}
				while (remTick >= baseTick) {
					sb.append( mmlNotePart(""+base) );
					remTick -= baseTick;
				}
			}
			if (remTick > 0) {
				throw new UndefinedTickException(remTick + "/" + tick);
			}
		}

		if (needTie) {
			return sb.substring(1);
		} else {
			return sb.toString();
		}
	}

	/**
	 * noteNameとtickをMMLの文字列に変換します.
	 * needTieがtrueのときは、'&amp;' による連結を行います.
	 * @return MML文字列
	 * @throws UndefinedTickException 変換に失敗した
	 */
	public String toMMLText() throws UndefinedTickException {
		int remTick = tick;
		StringBuilder sb = new StringBuilder();

		// "1."
		int mTick = getTick("1.");
		int tick1 = getTick("1");
		while (remTick > (tick1*2)) {
			sb.append( mmlNotePart("1.") );
			remTick -= mTick;
		}

		return makeMMLText(sb, remTick);
	}

	/**
	 * Base長を使って変換します.　（調律用）
	 * @param base 使用する調律指定
	 * @return MML文字列
	 * @throws UndefinedTickException 変換に失敗した
	 */
	public String toMMLTextByBase(TuningBase base) throws UndefinedTickException {
		int remTick = tick;
		StringBuilder sb = new StringBuilder();
		int min = minimumTick();

		int baseTick = base.getTick();
		while (remTick >= baseTick + min) {
			sb.append( mmlNotePart(base.getBase()) );
			remTick -= baseTick;
		}

		return makeMMLText(sb, remTick);
	}
}
