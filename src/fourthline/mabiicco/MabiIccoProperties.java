/*
 * Copyright (C) 2013 たんらる
 */

package fourthline.mabiicco;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.InvalidPropertiesFormatException;
import java.util.Properties;

public class MabiIccoProperties {

	private Properties properties = new Properties();
	private final String configFile = "config.properties";
	
	private static MabiIccoProperties instance = new MabiIccoProperties();
	
	public static MabiIccoProperties getInstance() {
		return instance;
	}
	
	private MabiIccoProperties() {
		try {
			properties.loadFromXML(new FileInputStream(configFile));
		} catch (InvalidPropertiesFormatException e) {
		} catch (FileNotFoundException e) {
		} catch (IOException e) {
		}
	}
	
	public String getRecentFile() {
		String str = null;
		str = properties.getProperty("RECENT_FILE");

		if (str == null) {
			str = "";
		}
		return str;
	}
	
	public void setRecentFile(String path) {
		properties.setProperty("RECENT_FILE", path);
		try {
			properties.storeToXML(new FileOutputStream(configFile), "");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
