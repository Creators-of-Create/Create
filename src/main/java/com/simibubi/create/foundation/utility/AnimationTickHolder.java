package com.simibubi.create.foundation.utility;

import com.simibubi.create.foundation.ponder.PonderUI;
import com.simibubi.create.foundation.ponder.PonderWorld;

import com.simibubi.create.foundation.utility.worldWrappers.WrappedClientWorld;
import net.minecraft.client.Minecraft;
import net.minecraft.world.IWorld;

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
	
	public static int getTicks() {
		return ticks;
	}

	public static float getRenderTime() {
		return getTicks() + getPartialTicks();
	}

	public static float getPartialTicks() {
		Minecraft mc = Minecraft.getInstance();
		return (mc.isGamePaused() ? mc.renderPartialTicksPaused : mc.getRenderPartialTicks());
	}
	
	public static int getTicks(IWorld world) {
		if (world instanceof WrappedClientWorld)
			return getTicks(((WrappedClientWorld) world).getWrappedWorld());
		return world instanceof PonderWorld ? PonderUI.ponderTicks : getTicks();
	}
	
	public static float getRenderTime(IWorld world) {
		return getTicks(world) + getPartialTicks(world);
	}
	
	public static float getPartialTicks(IWorld world) {
		return world instanceof PonderWorld ? PonderUI.getPartialTicks() : getPartialTicks();
	}
}
