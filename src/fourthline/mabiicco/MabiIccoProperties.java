/*
 * Copyright (C) 2013 たんらる
 */

package fourthline.mabiicco;

import java.awt.Rectangle;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.InvalidPropertiesFormatException;
import java.util.Properties;

import fourthline.mabiicco.midi.MabiDLS;

public class MabiIccoProperties {

	private Properties properties = new Properties();
	private final String configFile = "config.properties";
	
	private static MabiIccoProperties instance = new MabiIccoProperties();
	
	public static MabiIccoProperties getInstance() {
		return instance;
	}
	
	private MabiIccoProperties() {
		try {
			properties.load(new FileInputStream(configFile));
		} catch (InvalidPropertiesFormatException e) {
		} catch (FileNotFoundException e) {
		} catch (IOException e) {
		}
	}
	
	private void save() {
		try {
			properties.store(new FileOutputStream(configFile), "");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public String getRecentFile() {
		String str = properties.getProperty("app.recent_file", "");
		return str;
	}
	
	public void setRecentFile(String path) {
		properties.setProperty("app.recent_file", path);
		save();
	}
	
	public String getDlsFile() {
		String str = properties.getProperty("app.dls_file", MabiDLS.DEFALUT_DLS_PATH);
		return str;
	}
	
	public void setDlsFile(String path) {
		properties.setProperty("app.dls_file", path);
		save();
	}
	
	public boolean getWindowMaximize() {
		String str = properties.getProperty("window.maximize", "false");
		return Boolean.parseBoolean(str);
	}
	
	public void setWindowMaximize(boolean b) {
		properties.setProperty("window.maximize", Boolean.toString(b));
		save();
	}
	
	public Rectangle getWindowRect() {
		String x = properties.getProperty("window.x", "-1");
		String y = properties.getProperty("window.y", "-1");
		String width = properties.getProperty("window.width", "-1");
		String height = properties.getProperty("window.height", "-1");
		
		Rectangle rect = new Rectangle(
				Integer.parseInt(x), 
				Integer.parseInt(y),
				Integer.parseInt(width),
				Integer.parseInt(height)
				);
		
		return rect;
	}
	
	public void setWindowRect(Rectangle rect) {
		properties.setProperty("window.x", Integer.toString((int)rect.getX()));
		properties.setProperty("window.y", Integer.toString((int)rect.getY()));
		properties.setProperty("window.width", Integer.toString((int)rect.getWidth()));
		properties.setProperty("window.height", Integer.toString((int)rect.getHeight()));
		save();
	}
}
