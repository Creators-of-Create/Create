package com.simibubi.create.compat.jei;

import mezz.jei.api.gui.drawable.IDrawable;
import net.minecraft.client.Minecraft;

public abstract class AnimatedKinetics implements IDrawable {

	protected static int ticks;

	public static void tick() {
		ticks++;
	}

	public static float getCurrentAngle() {
		return ((ticks + Minecraft.getInstance().getRenderPartialTicks()) * 4f) % 360;
	}

}
