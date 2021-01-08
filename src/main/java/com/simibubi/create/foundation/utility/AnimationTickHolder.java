package com.simibubi.create.foundation.utility;

import net.minecraft.client.Minecraft;

public class AnimationTickHolder {

	public static int ticks;

	public static void tick() {
		ticks++;
	}

	public static float getRenderTick() {
		Minecraft mc = Minecraft.getInstance();
		return ticks + (mc.isGamePaused() ? mc.renderPartialTicksPaused : mc.getRenderPartialTicks());
	}

}
