package com.simibubi.create.foundation.utility;

import net.minecraft.client.Minecraft;

public class AnimationTickHolder {

	public static int ticks;

	public static void tick() {
		if (!Minecraft.getInstance().isGamePaused()) ticks++;
	}

	public static float getRenderTick() {
		return ticks + getPartialTicks();
	}

	public static float getPartialTicks() {
		Minecraft mc = Minecraft.getInstance();
		return (mc.isGamePaused() ? mc.renderPartialTicksPaused : mc.getRenderPartialTicks());
	}

}
