/*
 * Copyright (C) 2014 たんらる
 */

package jp.fourthline.mmlTools.core;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Locale;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import java.util.ResourceBundle.Control;

/**
 * properties/ 配下のリソースファイルをUTF-8で読み取る.
 */
public final class ResourceLoader extends Control {
	@Override
	public ResourceBundle newBundle(String baseName, Locale locale, String format, ClassLoader loader, boolean reload) 
			throws IllegalAccessException, InstantiationException, IOException {
		String bundleName = toBundleName(baseName, locale);
		String resourceName = toResourceName(bundleName, "properties");
		InputStream stream = new FileInputStream(getAppPath("properties/"+resourceName));
		ResourceBundle resource = new PropertyResourceBundle(new InputStreamReader(stream, StandardCharsets.UTF_8));
		stream.close();
		return resource;
	}

	public static String getAppPath(String path) {
		String app_path = System.getProperty("app.path");
		if (app_path != null) {
			return Path.of(app_path, path).toString();
		}

		return path;
	}

	public static String getAppConfigPath(String path) {
		String s = System.getProperty("user.home");
		if (s != null) {
			return Path.of(s, path).toString();
		}
		return getAppPath(path);
	}
}
