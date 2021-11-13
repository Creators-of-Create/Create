package com.simibubi.create.lib.render;

import org.apache.commons.lang3.mutable.MutableBoolean;

public class VirtualRenderingStateManager {
	private static final ThreadLocal<MutableBoolean> VIRTUAL = ThreadLocal.withInitial(MutableBoolean::new);

	public static boolean getVirtualState() {
		return VIRTUAL.get().booleanValue();
	}

	public static void setVirtualState(boolean virtual) {
		VIRTUAL.get().setValue(virtual);
	}

	public static void runVirtually(Runnable action) {
		setVirtualState(true);
		action.run();
		setVirtualState(false);
	}
}
