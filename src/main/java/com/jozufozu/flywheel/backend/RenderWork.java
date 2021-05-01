package com.jozufozu.flywheel.backend;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class RenderWork {
	private static final Queue<Runnable> runs = new ConcurrentLinkedQueue<>();

	public static void runAll() {
		while (!runs.isEmpty()) {
			runs.remove().run();
		}
	}

	/**
	 * Queue work to be executed at the end of a frame
	 */
	public static void enqueue(Runnable run) {
		runs.add(run);
	}
}
