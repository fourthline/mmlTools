/*
 * Copyright (C) 2024 たんらる
 */

package jp.fourthline.mabiicco;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public final class Utils {
	public static String compress(String s) {
		try {
			ByteArrayOutputStream bstream = new ByteArrayOutputStream();
			GZIPOutputStream out = new GZIPOutputStream( bstream );
			out.write(s.getBytes());
			out.close();
			bstream.close();
			return Base64.getEncoder().encodeToString(bstream.toByteArray());
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
		return null;
	}

	public static String decompress(String s) {
		try {
			GZIPInputStream in = new GZIPInputStream(
					new ByteArrayInputStream(
							Base64.getDecoder().decode(s.getBytes())));
			ByteArrayOutputStream bstream = new ByteArrayOutputStream();
			while (in.available() != 0) {
				int c = in.read();
				if (c >= 0) {
					bstream.write(c);
				}
			}
			bstream.close();
			return bstream.toString();
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
		return null;
	}
}
