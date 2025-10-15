/*
 * Copyright (C) 2025 たんらる
 */

package jp.fourthline.mmlTools.logger;

import jp.fourthline.mmlTools.MMLEventList;
import jp.fourthline.mmlTools.MMLTrack;

public interface LogMessage {
	String getLocalizedMessage();

	public interface PartMessage extends LogMessage {
		MMLEventList getRelationPart();
		int getTickOffset();
	}

	public interface TrackMessage extends LogMessage {
		MMLTrack getTrack();
	}
}
