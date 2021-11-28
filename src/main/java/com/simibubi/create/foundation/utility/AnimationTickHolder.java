package com.simibubi.create.foundation.utility;

import com.simibubi.create.foundation.ponder.PonderWorld;
import com.simibubi.create.foundation.ponder.ui.PonderUI;
import com.simibubi.create.foundation.utility.worldWrappers.WrappedClientWorld;
import com.simibubi.create.lib.helper.MinecraftHelper;

import net.minecraft.client.Minecraft;
import net.minecraft.world.level.LevelAccessor;

public class AnimationTickHolder {

	private static int ticks;
	private static int paused_ticks;

	public static void reset() {
		ticks = 0;
		paused_ticks = 0;
	}

	public static void tick() {
		if (!Minecraft.getInstance()
			.isPaused()) {
			ticks = (ticks + 1) % 1_728_000; // wrap around every 24 hours so we maintain enough floating point precision
		} else {
			paused_ticks = (paused_ticks + 1) % 1_728_000;
		}
	}

	public static int getTicks() {
		return getTicks(false);
	}

	public static int getTicks(boolean includePaused) {
		return includePaused ? ticks + paused_ticks : ticks;
	}

	public static float getRenderTime() {
		return getTicks() + getPartialTicks();
	}

	public static float getPartialTicks() {
		Minecraft mc = Minecraft.getInstance();
		return (mc.isPaused() ? MinecraftHelper.getRenderPartialTicksPaused(mc) : mc.getFrameTime());
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
