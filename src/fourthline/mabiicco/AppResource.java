/*
 * Copyright (C) 2014 たんらる
 */

package fourthline.mabiicco;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.ResourceBundle;
import java.util.jar.Manifest;

import javax.swing.ImageIcon;

import fourthline.mmlTools.core.ResourceLoader;

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
		String versionText = getManifestValue("MabiIcco-Version");
		return versionText;
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

	public static void main(String args[]) {
		System.getProperties().forEach((s1, s2) -> System.out.println(s1+": "+s2));
	}
}
