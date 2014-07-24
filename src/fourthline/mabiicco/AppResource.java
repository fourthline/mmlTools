/*
 * Copyright (C) 2014 たんらる
 */

package fourthline.mabiicco;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.jar.Manifest;

public class AppResource {
	private final static String RESOURCE_NAME = "appResource";
	private final static String BUILD_NUMBER = "/build.number";
	private static Manifest mf;
	private static ResourceBundle bundle = ResourceBundle.getBundle(RESOURCE_NAME);

	private AppResource() {}

	public static String getVersionText() {
		String versionText = AppResource.getManifestValue("Implementation-Version")
				+ " build" + AppResource.getBuildNumber();
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

	public static String getBuildNumber() {
		try {
			InputStream is = AppResource.class.getResourceAsStream(BUILD_NUMBER);
			Properties buildNumber = new Properties();
			buildNumber.load(is);
			return buildNumber.getProperty("build.number");
		} catch (FileNotFoundException e ) {
		} catch (IOException e) {
		} catch (NullPointerException e) {
		}
		return "";
	}

	public static String getText(String key) {
		try {
			return bundle.getString(key);
		} catch (java.util.MissingResourceException e) {
			return key;
		}
	}
}
