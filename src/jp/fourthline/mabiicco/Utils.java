/*
 * Copyright (C) 2024-2025 たんらる
 */

package jp.fourthline.mabiicco;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.Base64;
import java.util.List;
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

	private static final List<String> INVALID_NAMES = List.of("CON", "PRN", "AUX", "NUL", "COM0", "COM1", "COM2", "COM3", "COM4", "COM5", "COM6", "COM7", "COM8", "COM9", "LPT0","LPT1", "LPT2", "LPT3", "LPT4", "LPT5", "LPT6", "LPT7", "LPT8", "LPT9");
	public static boolean isValidFile(File file) {
		try {
			var path = file.toPath();
			if (System.getProperty("os.name").startsWith("Windows")) {
				String filename = path.getFileName().toString().toUpperCase();
				int dot = filename.indexOf('.');
				if (filename.indexOf('.') > 1) {
					filename = filename.substring(0, dot);
				}
				if (INVALID_NAMES.contains(filename)) {
					return false;
				}
			}
		} catch (Exception e) {
			return false;
		}
		return true;
	}
}
