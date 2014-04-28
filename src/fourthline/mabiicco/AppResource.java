/*
 * Copyright (C) 2014 たんらる
 */

package fourthline.mabiicco;

import java.io.IOException;
import java.io.InputStream;
import java.util.ResourceBundle;
import java.util.jar.Manifest;

public class AppResource {

	private static Manifest mf;
	private static ResourceBundle bundle;

	private AppResource() {}

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
		if (bundle == null) {
			bundle = ResourceBundle.getBundle("appResource");
		}
		try {
			return bundle.getString(key);
		} catch (java.util.MissingResourceException e) {
			return "(null)";
		}
	}
}
