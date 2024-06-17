/*
 * Copyright (C) 2024 たんらる
 */

package jp.fourthline.mabiicco;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.zip.DeflaterInputStream;
import java.util.zip.InflaterInputStream;

public final class Utils {
	public static String compress(String s) {
		return compress(s.getBytes());
	}

	public static String compress(byte[] b) {
		try {
			var in = new DeflaterInputStream(new ByteArrayInputStream(b));
			var data = in.readAllBytes();
			return Base64.getEncoder().encodeToString(data);
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
		return null;
	}

	public static byte[] decompress(String s) {
		try {
			var data = Base64.getDecoder().decode(s);
			var in = new InflaterInputStream(new ByteArrayInputStream(data));
			return in.readAllBytes();
		} catch (IOException | IllegalArgumentException e) {
			System.out.println(e.getMessage());
		}
		return null;
	}
}
