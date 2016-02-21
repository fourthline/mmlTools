/*
 * Copyright (C) 2016 たんらる
 */

package fourthline.mabiicco;


/**
 * 
 */
public final class AppErrorHandler {
	private static AppErrorHandler instance = null;
	public static AppErrorHandler getInstance() {
		if (instance == null) {
			instance = new AppErrorHandler();
		}
		return instance;
	}

	private AppErrorHandler() {}

	public void exec() {
		ActionDispatcher dispatcher = ActionDispatcher.getInstance();
		dispatcher.writeRecoveryData();
	}
}
