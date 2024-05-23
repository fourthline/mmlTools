/*
 * Copyright (C) 2014-2024 たんらる
 */

package jp.fourthline.mabiicco;

import java.awt.Font;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.ResourceBundle;
import java.util.jar.Manifest;

import javax.swing.ImageIcon;

import jp.fourthline.mmlTools.core.ResourceLoader;

public final class AppResource {
	private final static String APP_TITLE = " * MabiIcco * ";
	private final static String RESOURCE_NAME = "appResource";
	private static ArrayList<Manifest> mf;
	private final static ResourceBundle bundle = ResourceBundle.getBundle(RESOURCE_NAME, new ResourceLoader());

	private AppResource() {}

	public static ResourceBundle getResourceBundle() {
		return bundle;
	}

	public static String getVersionText() {
		return getManifestValue("MabiIcco-Version");
	}

	public static String getAppTitle() {
		return APP_TITLE;
	}

	public static String getRuntimeVersion() {
		return System.getProperties().get("java.runtime.version").toString();
	}

	public static synchronized String getManifestValue(String key) {
		if (mf == null) {
			try {
				ClassLoader cl = Thread.currentThread().getContextClassLoader();
				Enumeration<URL> urls = cl.getResources("META-INF/MANIFEST.MF");
				mf = new ArrayList<>();
				while (urls.hasMoreElements()) {
					URL url = urls.nextElement();
					mf.add( new Manifest(url.openStream()) );
				}
			} catch (IOException e) {
				e.printStackTrace();
				return "";
			}
		}

		for (Manifest t : mf) {
			String obj = t.getMainAttributes().getValue(key);
			if (obj != null) {
				return obj;
			}
		}

		return "";
	}

	public static String appText(String key) {
		return appText(key, true);
	}

	private static String appText(String key, boolean v) {
		try {
			StringBuilder sb = new StringBuilder(bundle.getString(key));

			while (v) {
				int start = sb.indexOf("%%");
				int end = sb.indexOf("%%", start + 1);
				if ((start >= 0) && (start + 2 < end)) {
					String str = appText(sb.substring(start+2, end), false);
					sb.replace(start, end+2, str);
				} else {
					v = false;
				}
			}
			return sb.toString();
		} catch (java.util.MissingResourceException e) {
			return key;
		}
	}

	private static final HashMap<String, ImageIcon> iconMap = new HashMap<>();
	public static ImageIcon getImageIcon(String path) {
		ImageIcon icon = iconMap.get(path);
		if (icon == null) {
			URL url = AppResource.class.getResource(path);
			if (url == null) {
				System.err.println("not found icon: " + path + " > " + url);
			} else {
				icon = new ImageIcon(url);
				iconMap.put(path, icon);
			}
		}

		return icon;
	}

	public static String getErrFile() {
		return ResourceLoader.getAppPath("err.txt");
	}

	public static Font getMonoFont(int size) {
		return new Font(Font.MONOSPACED, Font.PLAIN, size);
	}

	public static void main(String[] args) {
		System.getProperties().forEach((s1, s2) -> System.out.println(s1+": "+s2));
	}
}
