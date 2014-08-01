/*
 * Copyright (C) 2014 たんらる
 */

package fourthline.mabiicco;

import java.io.IOException;
import java.io.InputStream;
import java.util.ResourceBundle;
import java.util.jar.Manifest;

import fourthline.mmlTools.core.ResourceLoader;

public class AppResource {
	private final static String RESOURCE_NAME = "appResource";
	private static Manifest mf;
	private static ResourceBundle bundle = ResourceBundle.getBundle(RESOURCE_NAME, new ResourceLoader());

	private AppResource() {}

	public static String getVersionText() {
		String versionText = AppResource.getManifestValue("Implementation-Version")
				+ " build" + AppResource.getManifestValue("Implementation-Build");
		return versionText;
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

	public static String getText(String key) {
		try {
			return bundle.getString(key);
		} catch (java.util.MissingResourceException e) {
			return key;
		}
	}
}
