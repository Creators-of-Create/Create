package com.simibubi.create.foundation.utility;

import net.minecraft.client.Minecraft;

public class AnimationTickHolder {

	private static int ticks;

	public static void reset() {
		ticks = 0;
	}

	public static void tick() {
		if (!Minecraft.getInstance().isGamePaused()) {
			ticks = (ticks + 1) % 1_728_000; // wrap around every 24 hours so we maintain enough floating point precision
		}
	}

	public static float getRenderTime() {
		return getTicks() + getPartialTicks();
	}

	public static float getPartialTicks() {
		Minecraft mc = Minecraft.getInstance();
		return (mc.isGamePaused() ? mc.renderPartialTicksPaused : mc.getRenderPartialTicks());
	}

	public static int getTicks() {
		return ticks;
	}
}
