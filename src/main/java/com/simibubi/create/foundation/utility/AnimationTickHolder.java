package com.simibubi.create.foundation.utility;

import com.simibubi.create.foundation.ponder.PonderWorld;
import com.simibubi.create.foundation.ponder.ui.PonderUI;
import com.simibubi.create.foundation.utility.worldWrappers.WrappedClientWorld;

import net.minecraft.client.Minecraft;
import net.minecraft.world.level.LevelAccessor;

public class AnimationTickHolder {

	private static int ticks;
	private static int pausedTicks;

	public static void reset() {
		ticks = 0;
		pausedTicks = 0;
	}

	public static void tick() {
		if (!Minecraft.getInstance()
			.isPaused()) {
			ticks = (ticks + 1) % 1_728_000; // wrap around every 24 hours so we maintain enough floating point precision
		} else {
			pausedTicks = (pausedTicks + 1) % 1_728_000;
		}
	}

	public static int getTicks() {
		return getTicks(false);
	}

	public static int getTicks(boolean includePaused) {
		return includePaused ? ticks + pausedTicks : ticks;
	}

	public static float getRenderTime() {
		return getTicks() + getPartialTicks();
	}

	public static float getPartialTicks() {
		Minecraft mc = Minecraft.getInstance();
		return (mc.isPaused() ? mc.pausePartialTick : mc.getFrameTime());
	}

	public static int getTicks(LevelAccessor world) {
		if (world instanceof WrappedClientWorld)
			return getTicks(((WrappedClientWorld) world).getWrappedWorld());
		return world instanceof PonderWorld ? PonderUI.ponderTicks : getTicks();
	}

	public static float getRenderTime(LevelAccessor world) {
		return getTicks(world) + getPartialTicks(world);
	}

	public static float getPartialTicks(LevelAccessor world) {
		return world instanceof PonderWorld ? PonderUI.getPartialTicks() : getPartialTicks();
	}
}
