/*
 * Copyright (C) 2014 たんらる
 */

package fourthline.mabiicco;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.ResourceBundle;
import java.util.jar.Manifest;

import javax.swing.ImageIcon;

import fourthline.mmlTools.core.ResourceLoader;

public final class AppResource {
	private final static String RESOURCE_NAME = "appResource";
	private static Manifest mf;
	private final static ResourceBundle bundle = ResourceBundle.getBundle(RESOURCE_NAME, new ResourceLoader());

	private AppResource() {}

	public static ResourceBundle getResourceBundle() {
		return bundle;
	}

	public static String getVersionText() {
		String versionText = AppResource.getManifestValue("Implementation-Version")
				+ " build" + AppResource.getManifestValue("Implementation-Build");
		return versionText;
	}

	public static String getRuntimeVersion() {
		return System.getProperties().get("java.runtime.version").toString();
	}

	public static String getManifestValue(String key) {
		if (mf == null) {
			try {
				InputStream is = AppResource.class.getResourceAsStream("/META-INF/MANIFEST.MF");
				mf = new Manifest(is);
			} catch (IOException e) {
				e.printStackTrace();
				return "";
			}
		}

		return mf.getMainAttributes().getValue(key);
	}

	public static String appText(String key) {
		try {
			return bundle.getString(key);
		} catch (java.util.MissingResourceException e) {
			return key;
		}
	}

	private static HashMap<String, ImageIcon> iconMap = new HashMap<>();
	public static ImageIcon getImageIcon(String path) {
		ImageIcon icon = iconMap.get(path);
		if (icon == null) {
			icon = new ImageIcon(AppResource.class.getResource(path));
			iconMap.put(path, icon);
		}

		return icon;
	}

	public static void main(String args[]) {
		System.getProperties().forEach((s1, s2) -> System.out.println(s1+": "+s2));
	}
}
