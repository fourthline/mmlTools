/*
 * Copyright (C) 2014 たんらる
 */

package fourthline.mmlTools.parser;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Base64;
import java.util.Base64.Decoder;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;

import fourthline.mabiicco.midi.InstClass;
import fourthline.mmlTools.Marker;


/**
 * "[3MLE EXTENSION]" parser
 */
public final class Extension3mleTrack {
	private int instrument;
	private int panpot;
	private int trackCount;
	private int trackLimit;
	private int group;
	private String trackName;

	private Extension3mleTrack(int instrument, int group, int panpot, String trackName) {
		this.instrument = instrument;
		this.group = group;
		this.panpot = panpot;
		this.trackName = trackName;
		this.trackCount = 1;
		this.trackLimit = 0;
		for (boolean b : InstClass.getEnablePartByProgram(instrument-1)) {
			if (b) trackLimit++;
		}
	}

	private boolean isLimit() {
		return (trackCount >= trackLimit);
	}

	private void addTrack() {
		trackCount++;
	}

	public int getInstrument() {
		return instrument;
	}

	public int getPanpot() {
		return panpot;
	}

	public int getTrackCount() {
		return trackCount;
	}

	public String getTrackName() {
		return trackName;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("[ ");
		sb.append(group+" ");
		sb.append(instrument+" ");
		sb.append(panpot+" ");
		sb.append(trackCount+" ");
		sb.append(trackName + " ]");
		return sb.toString();
	}

	/**
	 * [3MLE EXTENSION] をパースし, トラック構成情報を取得します.
	 * @param [IN]  str [3MLE EXTENSION] セクションのコンテンツ
	 * @param [OUT] markerList マーカーリスト 
	 * @return トラック構成情報
	 */
	public static List<Extension3mleTrack> parse3mleExtension(String str, List<Marker> markerList) {
		StringBuilder sb = new StringBuilder();
		for (String s : str.split("\n")) {
			if (s.startsWith("d=")) {
				sb.append(s.substring(2));
			}
		}

		byte data[] = decode(sb.toString());
		return parse(data, markerList);
	}

	private static byte[] decode(String dSection) {
		Decoder decoder = Base64.getDecoder();
		byte b[] = decoder.decode(dSection);

		int dataLength = ByteBuffer.wrap(b, 0, 4).order(ByteOrder.LITTLE_ENDIAN).getInt();
		byte data[] = new byte[dataLength];

		try {
			BZip2CompressorInputStream bz2istream = new BZip2CompressorInputStream(new ByteArrayInputStream(b, 12, b.length-12));
			bz2istream.read(data);
			bz2istream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		for (int i = 0; i < dataLength; i++) {
			System.out.printf("%02x ", data[i]);
		}
		System.out.println();
		return data;
	}

	/**
	 * @param [IN]  data decompress済みのバイト列
	 * @param [OUT] markerList マーカーリスト 
	 * @return トラック構成情報
	 */
	private static List<Extension3mleTrack> parse(byte data[], List<Marker> markerList) {
		LinkedList<Extension3mleTrack> trackList = new LinkedList<>();
		trackList.add(new Extension3mleTrack(-1, -1, -1, null)); // dummy

		ByteArrayInputStream istream = new ByteArrayInputStream(data);
		int b = 0;
		int hb = 0;
		while ( (b = istream.read()) != -1) {
			if ( (hb == 0x02) && (b == 0x1c) ) {
				parseTrack(trackList, istream);
			} else if ( (hb == 0x09) && ( (b >= 0x10) && (b < 0x20) )) {
				parseMarker(markerList, istream);
			}

			hb = b;
		}

		trackList.removeFirst();
		return trackList;
	}

	private static void parseTrack(LinkedList<Extension3mleTrack> trackList, ByteArrayInputStream istream) {
		// parse Track
		istream.skip(3);
		int trackNo = istream.read();
		istream.skip(1); // volumn
		int panpot = istream.read();
		istream.skip(13);
		int instrument = istream.read();
		istream.skip(3);
		int group = istream.read();
		istream.skip(13);
		String trackName = readString(istream);
		System.out.println(trackNo+" "+instrument+" "+trackName);

		Extension3mleTrack lastTrack = trackList.getLast();
		if ( (lastTrack.group != group) || (lastTrack.instrument != instrument) || (lastTrack.isLimit())) {
			// new Track
			trackList.add(new Extension3mleTrack(instrument, group, panpot, trackName));
		} else {
			lastTrack.addTrack();
		}
	}

	private static void parseMarker(List<Marker> markerList, ByteArrayInputStream istream) {
		byte b[] = new byte[4];
		try {
			// parse Marker
			istream.skip(7);
			istream.read(b);
			int tickOffset = ByteBuffer.wrap(b).order(ByteOrder.LITTLE_ENDIAN).getInt();
			istream.skip(4);
			String name = readString(istream);
			System.out.println("Marker " + name + "=" + tickOffset);
			if (markerList != null) {
				markerList.add(new Marker(name, tickOffset));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static String readString(InputStream istream) {
		ByteArrayOutputStream ostream = new ByteArrayOutputStream();
		int b;
		try {
			while ( (b = istream.read()) != 0 ) {
				ostream.write(b);
			}
			return new String(ostream.toByteArray(), "Shift_JIS");
		} catch (IOException e) {
			e.printStackTrace();
		}
		return "";
	}

	public static void main(String[] args) {
		String str = "d=4wAAAJvYl0oBAAAAQlpoOTFBWSZTWReDTXYAAEH/i/7U0AQCAHgAQAAEAGwIEABAAECAAAoABKAAcivUCaZGmRiAyNqDEgnqRpkPTUZGh5S6QfOGHRg+AfSJE3ebNDxInstECT3owI1yYiuIY5IwTCLAQz1oZyAogJFOhVYmv39cWsLxsbh0MkELhClECHm5wCBjLYz8XckU4UJAXg012A==";

		List<Extension3mleTrack> trackList = Extension3mleTrack.parse3mleExtension(str, null);
		for (Extension3mleTrack track : trackList) {
			System.out.println(track);
		}
	}
}
