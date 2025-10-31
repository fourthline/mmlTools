/*
 * Copyright (C) 2025 たんらる
 */

package jp.fourthline.mabiicco;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.IntSupplier;

/**
 * MabiIccoのExecutorを管理する.
 */
public final class MabiIccoExecutor {
	private static final MabiIccoExecutor instance = new MabiIccoExecutor();

	private final ScheduledExecutorService executor = Executors.newScheduledThreadPool(4, (r) -> new Thread(r, "MabiIccoExecutor"));

	private MabiIccoExecutor() {
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			executor.shutdown();
			try {
				if (!executor.awaitTermination(3, TimeUnit.SECONDS)) {
					executor.shutdownNow();
				}
			} catch (InterruptedException e) {
				executor.shutdownNow();
			}
		}));
	}

	public static MabiIccoExecutor getInstance() {
		return instance;
	}

	private Runnable wrap(Runnable task) {
		return () -> {
			try {
				task.run();
			} catch (Throwable e) {
				e.printStackTrace();
			}
		};
	}

	public void submit(Runnable task) {
		executor.submit(wrap(task));
	}

	public void scheduleWithDelay(Runnable task, IntSupplier delay) {
		scheduleWithDelayInternal(wrap(task), delay);
	}

	private void scheduleWithDelayInternal(Runnable task, IntSupplier delay) {
		executor.schedule(() -> {
			task.run();
			scheduleWithDelayInternal(task, delay);
		}, delay.getAsInt(), TimeUnit.MILLISECONDS);
	}

	public ScheduledFuture<?> scheduleWithFixedDelay(Runnable task, long initialDelay, long delay) {
		return executor.scheduleWithFixedDelay(wrap(task), initialDelay, delay, TimeUnit.MILLISECONDS);
	}

	public ScheduledFuture<?> scheduleWithFixedDelay(Runnable task, long delay) {
		return scheduleWithFixedDelay(task, delay, delay);
	}
}
