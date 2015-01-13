/*
 * Copyright (C) 2014 たんらる
 */

package fourthline.mabiicco.preloader;

import javafx.application.Preloader;

public final class MabiIccoPreloaderNotification implements Preloader.PreloaderNotification {
	private String message;
	private double progress;

	public MabiIccoPreloaderNotification(String message, double progress) {
		this.message = message;
		this.progress = progress;
	}

	public String getMessage() {
		return this.message;
	}

	public double getProgress() {
		return this.progress;
	}
}
